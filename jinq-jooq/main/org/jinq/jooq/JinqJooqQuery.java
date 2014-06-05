package org.jinq.jooq;

import static org.jinq.jooq.test.generated.Tables.CUSTOMERS;

import java.io.Serializable;
import java.util.List;

import org.jinq.jooq.querygen.RowReader;
import org.jinq.jooq.querygen.TableRowReader;
import org.jinq.jooq.transform.LambdaInfo;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

import ch.epfl.labos.iu.orm.QueryComposer;

public class JinqJooqQuery<T extends Record>
{
   JinqJooqContext context;
   TableImpl<T> from;
   RowReader<T> reader;
   
   JinqJooqQuery(JinqJooqContext context, TableImpl<T> from)
   {
      this.context = context;
      this.from = from;
      this.reader = new TableRowReader<>(from);
   }

   public static interface Where<U, E extends Exception> extends Serializable {
      public boolean where(U obj) throws E;
   }
   public <E extends Exception> JinqJooqQuery<T> where(Where<T, E> test)
   {
      LambdaInfo where = LambdaInfo.analyze(context.metamodel, test);
      if (where == null) return null;
//      JPQLQueryTransform whereTransform = new WhereTransform(metamodel, where);
//      JPQLQuery<T> newQuery = whereTransform.apply(query);
//      if (newQuery == null) return null;
//      return new JPAQueryComposer<>(metamodel, em, newQuery, lambdas, where);
      return null;
   }

   public ResultStream<T> selectAll()
   {
      // TODO: Decide between defaulting to using cursors or defaulting
      // to using lists.
      List<Record> result = context.dsl.select().from(from).fetch();
      return new ResultStream<>(result.stream().map(r -> reader.readResult(r)));
   }
}
