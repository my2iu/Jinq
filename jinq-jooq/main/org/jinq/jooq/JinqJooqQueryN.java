package org.jinq.jooq;

import java.util.ArrayList;
import java.util.List;

import org.jinq.jooq.querygen.ColumnExpressions;
import org.jinq.jooq.querygen.RowReader;
import org.jinq.jooq.querygen.TableRowReader;
import org.jinq.jooq.querygen.TupleRowReader;
import org.jinq.jooq.transform.LambdaInfo;
import org.jinq.jooq.transform.SelectTransform;
import org.jinq.jooq.transform.WhereTransform;
import org.jooq.Condition;
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
      List<Record> result;
      if (whereConditions != null)
         result = context.dsl.select().from(fromTables).where(whereConditions).fetch();
      else
         result = context.dsl.select().from(fromTables).fetch();
      RowReader<?>[] readerArray = tableReaders.toArray(new RowReader[tableReaders.size()]);
      RowReader<U> reader = tableReaders.size() == 1 ? 
            (RowReader<U>)tableReaders.get(0)
            :new TupleRowReader<>(readerArray);
      return new ResultStream<>(result.stream().map(r -> reader.readResult(r)));
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
      
      List<Record> result;
      if (whereConditions != null)
         result = context.dsl.select(selectColumns).from(froms).where(whereConditions).fetch();
      else
         result = context.dsl.select(selectColumns).from(froms).fetch();
      return new ResultStream<>(result.stream().map(r -> columns.reader.readResult(r)));
   }
}
