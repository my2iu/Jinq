package org.jinq.jooq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

public class JinqJooqQuery<T extends Record>
{
   JinqJooqQueryN query;

   JinqJooqQuery(JinqJooqQueryN query)
   {
      this.query = query;
   }
   
   JinqJooqQuery(JinqJooqContext context, TableImpl<T> from)
   {
      this(context, from, null);
   }

   JinqJooqQuery(JinqJooqContext context, TableImpl<T> from, Condition whereConditions)
   {
      List<TableImpl<?>> fromTables = new ArrayList<>();
      fromTables.add(from);
      query = new JinqJooqQueryN(context, fromTables, whereConditions);
   }

   public static interface Where<U, E extends Exception> extends Serializable {
      public boolean where(U obj) throws E;
   }
   public <E extends Exception> JinqJooqQuery<T> where(Where<T, E> test)
   {
      return new JinqJooqQuery<>(query.where(test));
   }

   public ResultStream<T> selectAll()
   {
      return query.selectAll();
   }
   
   public static interface Select<U, V> extends Serializable {
      public V select(U val);
   }
   public <U> ResultStream<U> select(Select<T,U> lambda)
   {
      return query.select(lambda);
   }
}
