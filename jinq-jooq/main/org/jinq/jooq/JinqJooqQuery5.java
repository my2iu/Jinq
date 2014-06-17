package org.jinq.jooq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jinq.tuples.Tuple5;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

public class JinqJooqQuery5<T extends Record, U extends Record, V extends Record, W extends Record, X extends Record>
{
   JinqJooqQueryN query;

   JinqJooqQuery5(JinqJooqQueryN query)
   {
      this.query = query;
   }

   JinqJooqQuery5(JinqJooqContext context, TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3, TableImpl<W> from4, TableImpl<X> from5)
   {
      this(context, from1, from2, from3, from4, from5, null);
   }

   JinqJooqQuery5(JinqJooqContext context, TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3, TableImpl<W> from4, TableImpl<X> from5, Condition whereConditions)
   {
      List<TableImpl<?>> fromTables = new ArrayList<>();
      fromTables.add(from1);
      fromTables.add(from2);
      fromTables.add(from3);
      fromTables.add(from4);
      fromTables.add(from5);
      query = new JinqJooqQueryN(context, fromTables, whereConditions);
   }

   public static interface Where<T, U, V, W, X, E extends Exception> extends Serializable {
      public boolean where(T obj1, U obj2, V obj3, W obj4, X obj5) throws E;
   }
   public <E extends Exception> JinqJooqQuery5<T, U, V, W, X> where(Where<T, U, V, W, X, E> test)
   {
      return new JinqJooqQuery5<>(query.where(test));
   }

   public ResultStream<Tuple5<T, U, V, W, X>> selectAll()
   {
      return query.selectAll();
   }
   
   public static interface Select<T, U, V, W, X, Z> extends Serializable {
      public Z select(T val1, U val2, V val3, W val4, X val5);
   }
   public <Z> ResultStream<Z> select(Select<T,U,V,W,X,Z> lambda)
   {
      return query.select(lambda);
   }
}
