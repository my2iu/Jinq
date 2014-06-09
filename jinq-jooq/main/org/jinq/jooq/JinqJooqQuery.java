package org.jinq.jooq;

import static org.jinq.jooq.test.generated.Tables.CUSTOMERS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jinq.jooq.querygen.ColumnExpressions;
import org.jinq.jooq.querygen.RowReader;
import org.jinq.jooq.querygen.TableRowReader;
import org.jinq.jooq.transform.LambdaInfo;
import org.jinq.jooq.transform.SelectTransform;
import org.jinq.jooq.transform.WhereTransform;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.TableImpl;

import ch.epfl.labos.iu.orm.QueryComposer;

public class JinqJooqQuery<T extends Record>
{
   JinqJooqContext context;
   TableImpl<T> from;
   RowReader<T> reader;
   Condition whereConditions;
   
   JinqJooqQuery(JinqJooqContext context, TableImpl<T> from)
   {
      this(context, from, null);
   }

   JinqJooqQuery(JinqJooqContext context, TableImpl<T> from, Condition whereConditions)
   {
      this.context = context;
      this.from = from;
      this.reader = new TableRowReader<>(from);
      this.whereConditions = whereConditions;
   }

   public static interface Where<U, E extends Exception> extends Serializable {
      public boolean where(U obj) throws E;
   }
   public <E extends Exception> JinqJooqQuery<T> where(Where<T, E> test)
   {
      if (whereConditions != null) throw new IllegalArgumentException("Multiple where() lambdas not supported");
      LambdaInfo where = LambdaInfo.analyze(context.metamodel, test);
      if (where == null) throw new IllegalArgumentException("Could not create convert Lambda into a query");
      WhereTransform whereTransform = new WhereTransform(context.metamodel, where);
      List<Table<?>> fromTables = new ArrayList<>();
      fromTables.add(from);
      Condition cond = whereTransform.apply(fromTables);
      return new JinqJooqQuery<>(context, from, cond);
//      JPQLQuery<T> newQuery = whereTransform.apply(query);
//      if (newQuery == null) return null;
//      return new JPAQueryComposer<>(metamodel, em, newQuery, lambdas, where);
   }

   public ResultStream<T> selectAll()
   {
      // TODO: Decide between defaulting to using cursors or defaulting
      // to using lists.
      List<Record> result;
      if (whereConditions != null)
         result = context.dsl.select().from(from).where(whereConditions).fetch();
      else
         result = context.dsl.select().from(from).fetch();
      return new ResultStream<>(result.stream().map(r -> reader.readResult(r)));
   }
   
   public static interface Select<U, V> extends Serializable {
      public V select(U val);
   }
   public <U> ResultStream<U> select(Select<T,U> lambda)
   {
      // Figure out which columns to return
      LambdaInfo select = LambdaInfo.analyze(context.metamodel, lambda);
      if (select == null) throw new IllegalArgumentException("Could not create convert Lambda into a query");
      SelectTransform transform = new SelectTransform(context.metamodel, select);
      List<Table<?>> fromTables = new ArrayList<>();
      fromTables.add(from);
      ColumnExpressions<U> columns = transform.apply(fromTables);

      // Run the query now
      List<Field<?>> selectColumns = new ArrayList<>();
      for (QueryPart col: columns.columns)
      {
         selectColumns.add((Field<?>)col);
      }
      
      List<Record> result;
      if (whereConditions != null)
         result = context.dsl.select(selectColumns).from(from).where(whereConditions).fetch();
      else
         result = context.dsl.select(selectColumns).from(from).fetch();
      return new ResultStream<>(result.stream().map(r -> columns.reader.readResult(r)));
   }
}
