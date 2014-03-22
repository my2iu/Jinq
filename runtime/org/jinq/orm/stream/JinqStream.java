package org.jinq.orm.stream;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.StringSorter;

public interface JinqStream<T> extends Stream<T>
{
   public static interface Where<U> extends Serializable {
      public boolean where(U obj);
   }
   public JinqStream<T> where(Where<T> test);
   public static interface Select<U, V> extends Serializable {
      public V select(U val);
   }
   public <U> JinqStream<U> select(Select<T, U> select);
   // TODO: Joins are somewhat dangerous because certain types of joins that are
   // expressible here are NOT expressible in SQL. (Moving a join into
   // a from clause is only possible if the join does not access variables from
   // other things in the FROM clause *if* it ends up as a subquery. If we can 
   // express it as not a subquery, then it's ok.
   // TODO: Perhaps only providing a join(DBSet<U> other) is safer because
   // I think it will translate into valid SQL code, but it prevents people from
   // using navigational queries e.g. customers.join(customer -> customer.getPurchases);
   public static interface Join<U, V> extends Serializable {
      public JinqStream<V> join(U val);
   }
   // TODO: Rewrite join so that it doesn't take a DBSet
   public <U> JinqStream<Pair<T, U>> join(Join<T,U> join);
   public JinqStream<T> unique();
   public static interface AggregateGroup<W, U, V> extends Serializable {
      public V aggregateSelect(W key, JinqStream<U> val);
   }
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate);
   public static interface AggregateDouble<U> extends Serializable {
      public double aggregate(U val);
   }
   public static interface AggregateInteger<U> extends Serializable {
      public int aggregate(U val);
   }
   public static interface AggregateSelect<U, V> extends Serializable {
      public V aggregateSelect(JinqStream<U> val);
   }
   public double sumDouble(AggregateDouble<T> aggregate);
   public int sumInt(AggregateInteger<T> aggregate);
   public double maxDouble(AggregateDouble<T> aggregate);
   public int maxInt(AggregateInteger<T> aggregate);
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate);

   public JinqStream<T> sortedByIntAscending(final IntSorter<T> sorter);
   public JinqStream<T> sortedByIntDescending(final IntSorter<T> sorter);
   public JinqStream<T> sortedByDoubleAscending(final DoubleSorter<T> sorter);
   public JinqStream<T> sortedByDoubleDescending(final DoubleSorter<T> sorter);
   public JinqStream<T> sortedByStringAscending(final StringSorter<T> sorter);
   public JinqStream<T> sortedByStringDescending(final StringSorter<T> sorter);
   public JinqStream<T> sortedByDateAscending(final DateSorter<T> sorter);
   public JinqStream<T> sortedByDateDescending(final DateSorter<T> sorter);
   
   public JinqStream<T> firstN(int n);
   
   public T getOnlyValue();
   public JinqStream<T> with(T toAdd);
   
   // TODO: Should toList() throw an exception?
   public List<T> toList();
   
   /**
    * Used for recording an exception that occurred during processing
    * somewhere in the stream chain.
    *  
    * @param source lambda object that caused the exception (used so that
    *    if the same lambda causes multiple exceptions, only some of them 
    *    need to be recorded in order to avoid memory issues)
    * @param exception actual exception object
    */
   public void propagateException(Object source, Throwable exception);
   
   public Collection<Throwable> getExceptions();
}
