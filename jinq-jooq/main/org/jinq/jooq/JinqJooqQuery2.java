package org.jinq.jooq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jinq.tuples.Pair;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

public class JinqJooqQuery2<T extends Record, U extends Record>
{
   JinqJooqQueryN query;

   JinqJooqQuery2(JinqJooqQueryN query)
   {
      this.query = query;
   }

   JinqJooqQuery2(JinqJooqContext context, TableImpl<T> from1, TableImpl<U> from2)
   {
      this(context, from1, from2, null);
   }

   JinqJooqQuery2(JinqJooqContext context, TableImpl<T> from1, TableImpl<U> from2, Condition whereConditions)
   {
      List<TableImpl<?>> fromTables = new ArrayList<>();
      fromTables.add(from1);
      fromTables.add(from2);
      query = new JinqJooqQueryN(context, fromTables, whereConditions);
   }

   public static interface Where<T, U, E extends Exception> extends Serializable {
      public boolean where(T obj1, U obj2) throws E;
   }
   public <E extends Exception> JinqJooqQuery2<T, U> where(Where<T, U, E> test)
   {
      return new JinqJooqQuery2<>(query.where(test));
   }

   public ResultStream<Pair<T, U>> selectAll()
   {
      return query.selectAll();
   }
   
   public static interface Select<T, U, Z> extends Serializable {
      public Z select(T val1, U val2);
   }
   public <Z> ResultStream<Z> select(Select<T,U,Z> lambda)
   {
      return query.select(lambda);
   }
}
