package org.jinq.jooq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.jinq.jooq.querygen.ColumnExpressions;
import org.jinq.jooq.querygen.RowReader;
import org.jinq.jooq.querygen.TableRowReader;
import org.jinq.jooq.querygen.TupleRowReader;
import org.jinq.jooq.transform.LambdaInfo;
import org.jinq.jooq.transform.SelectTransform;
import org.jinq.jooq.transform.WhereTransform;
import org.jinq.orm.stream.NextOnlyIterator;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.Field;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.TableImpl;

public class JinqJooqQueryN
{
   JinqJooqContext context;
   List<TableImpl<?>> fromTables = new ArrayList<>();
   List<RowReader<?>> tableReaders = new ArrayList<>();
   Condition whereConditions;
   
   JinqJooqQueryN(JinqJooqContext context, List<TableImpl<?>> froms)
   {
      this(context, froms, null);
   }

   JinqJooqQueryN(JinqJooqContext context, List<TableImpl<?>> froms, Condition whereConditions)
   {
      this.context = context;
      for (TableImpl<?> table: froms)
      {
         fromTables.add(table);
         tableReaders.add(new TableRowReader<>(table));
      }
      this.whereConditions = whereConditions;
   }

   public JinqJooqQueryN where(Object lambda)
   {
      if (whereConditions != null) throw new IllegalArgumentException("Multiple where() lambdas not supported");
      LambdaInfo where = LambdaInfo.analyze(context.metamodel, lambda);
      if (where == null) throw new IllegalArgumentException("Could not create convert Lambda into a query");
      WhereTransform whereTransform = new WhereTransform(context.metamodel, where);
      List<Table<?>> from = new ArrayList<>();
      from.addAll(fromTables);
      Condition cond = whereTransform.apply(from);
      return new JinqJooqQueryN(context, fromTables, cond);
   }

   public <U> ResultStream<U> selectAll()
   {
      // TODO: Decide between defaulting to using cursors or defaulting
      // to using lists.
      Cursor<Record> cursor;
      if (whereConditions != null)
         cursor = context.dsl.select().from(fromTables).where(whereConditions).fetchLazy();
      else
         cursor = context.dsl.select().from(fromTables).fetchLazy();
      RowReader<?>[] readerArray = tableReaders.toArray(new RowReader[tableReaders.size()]);
      RowReader<U> reader = tableReaders.size() == 1 ? 
            (RowReader<U>)tableReaders.get(0)
            : new TupleRowReader<>(readerArray);
      return makeCursorStream(cursor, reader);
   }
   
   public <U> ResultStream<U> select(Object lambda)
   {
      // Figure out which columns to return
      LambdaInfo select = LambdaInfo.analyze(context.metamodel, lambda);
      if (select == null) throw new IllegalArgumentException("Could not create convert Lambda into a query");
      SelectTransform transform = new SelectTransform(context.metamodel, select);
      List<Table<?>> froms = new ArrayList<>();
      froms.addAll(fromTables);
      ColumnExpressions<U> columns = transform.apply(froms);

      // Run the query now
      List<Field<?>> selectColumns = new ArrayList<>();
      for (QueryPart col: columns.columns)
         selectColumns.add((Field<?>)col);
      
      Cursor<Record> cursor;
      if (whereConditions != null)
         cursor = context.dsl.select(selectColumns).from(froms).where(whereConditions).fetchLazy();
      else
         cursor = context.dsl.select(selectColumns).from(froms).fetchLazy();
      return makeCursorStream(cursor, columns.reader);
   }
   
   private <U> ResultStream<U> makeCursorStream(Cursor<Record> cursor, RowReader<U> reader)
   {
      Iterator<U> iterator = new NextOnlyIterator<U>() {
         @Override protected void generateNext()
         {
            if (!cursor.hasNext())
            {
               noMoreElements();
               return;
            }
            nextElement(reader.readResult(cursor.fetchOne()));
         }
      };
      return new ResultStream<>(StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                  iterator, 
                  Spliterator.CONCURRENT), 
            false));
   }
}
