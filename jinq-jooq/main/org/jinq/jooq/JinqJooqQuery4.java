package org.jinq.jooq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jinq.tuples.Tuple4;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

public class JinqJooqQuery4<T extends Record, U extends Record, V extends Record, W extends Record>
{
   JinqJooqQueryN query;

   JinqJooqQuery4(JinqJooqQueryN query)
   {
      this.query = query;
   }

   JinqJooqQuery4(JinqJooqContext context, TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3, TableImpl<W> from4)
   {
      this(context, from1, from2, from3, from4, null);
   }

   JinqJooqQuery4(JinqJooqContext context, TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3, TableImpl<W> from4, Condition whereConditions)
   {
      List<TableImpl<?>> fromTables = new ArrayList<>();
      fromTables.add(from1);
      fromTables.add(from2);
      fromTables.add(from3);
      fromTables.add(from4);
      query = new JinqJooqQueryN(context, fromTables, whereConditions);
   }

   public static interface Where<T, U, V, W, E extends Exception> extends Serializable {
      public boolean where(T obj1, U obj2, V obj3, W obj4) throws E;
   }
   public <E extends Exception> JinqJooqQuery4<T, U, V, W> where(Where<T, U, V, W, E> test)
   {
      return new JinqJooqQuery4<>(query.where(test));
   }

   public ResultStream<Tuple4<T, U, V, W>> selectAll()
   {
      return query.selectAll();
   }
   
   public static interface Select<T, U, V, W, Z> extends Serializable {
      public Z select(T val1, U val2, V val3, W val4);
   }
   public <Z> ResultStream<Z> select(Select<T,U,V,W,Z> lambda)
   {
      return query.select(lambda);
   }
}
