package org.jinq.jooq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jinq.tuples.Tuple3;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

public class JinqJooqQuery3<T extends Record, U extends Record, V extends Record>
{
   JinqJooqQueryN query;

   JinqJooqQuery3(JinqJooqQueryN query)
   {
      this.query = query;
   }

   JinqJooqQuery3(JinqJooqContext context, TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3)
   {
      this(context, from1, from2, from3, null);
   }

   JinqJooqQuery3(JinqJooqContext context, TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3, Condition whereConditions)
   {
      List<TableImpl<?>> fromTables = new ArrayList<>();
      fromTables.add(from1);
      fromTables.add(from2);
      fromTables.add(from3);
         query = new JinqJooqQueryN(context, fromTables, whereConditions);
   }

   public static interface Where<T, U, V, E extends Exception> extends Serializable {
      public boolean where(T obj1, U obj2, V obj3) throws E;
   }
   public <E extends Exception> JinqJooqQuery3<T, U, V> where(Where<T, U, V, E> test)
   {
      return new JinqJooqQuery3<>(query.where(test));
   }

   public ResultStream<Tuple3<T, U, V>> selectAll()
   {
      return query.selectAll();
   }
   
   public static interface Select<T, U, V, Z> extends Serializable {
      public Z select(T val1, U val2, V val3);
   }
   public <Z> ResultStream<Z> select(Select<T,U,V,Z> lambda)
   {
      return query.select(lambda);
   }
}
