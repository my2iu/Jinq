package ch.epfl.labos.iu.orm;

import java.io.Serializable;
import java.util.Collection;

public interface DBSet<T> extends Collection<T>
{
   public T getValue();
   public T get();
   
   public DBSet<T> with(T toAdd);
   
   public DBSet<T> comparisonClone();
   
   public DBSet<T> sortedByIntAscending(final IntSorter<T> sorter);
   public DBSet<T> sortedByIntDescending(final IntSorter<T> sorter);
   public DBSet<T> sortedByDoubleAscending(final DoubleSorter<T> sorter);
   public DBSet<T> sortedByDoubleDescending(final DoubleSorter<T> sorter);
   public DBSet<T> sortedByStringAscending(final StringSorter<T> sorter);
   public DBSet<T> sortedByStringDescending(final StringSorter<T> sorter);
   public DBSet<T> sortedByDateAscending(final DateSorter<T> sorter);
   public DBSet<T> sortedByDateDescending(final DateSorter<T> sorter);
   
   public DBSet<T> firstN(int n);
   
   // New stuff for Queryll2
   public static interface Where<U> extends Serializable {
      public boolean where(U obj);
   }
   public DBSet<T> where(Where<T> test);
   public static interface Select<U, V> extends Serializable {
      public V select(U val);
   }
   public <U> DBSet<U> select(Select<T, U> select);
   // TODO: Joins are somewhat dangerous because certain types of joins that are
   // expressible here are NOT expressible in SQL. (Moving a join into
   // a from clause is only possible if the join does not access variables from
   // other things in the FROM clause *if* it ends up as a subquery. If we can 
   // express it as not a subquery, then it's ok.
   // TODO: Perhaps only providing a join(DBSet<U> other) is safer because
   // I think it will translate into valid SQL code, but it prevents people from
   // using navigational queries e.g. customers.join(customer -> customer.getPurchases);
   public static interface Join<U, V> extends Serializable {
      public DBSet<V> join(U val);
   }
   public <U> DBSet<Pair<T, U>> join(Join<T,U> join);
   public static interface AggregateDouble<U> extends Serializable {
      public double aggregate(U val);
   }
   public static interface AggregateInteger<U> extends Serializable {
      public int aggregate(U val);
   }
   public static interface AggregateSelect<U, V> extends Serializable {
      public V aggregateSelect(DBSet<U> val);
   }
   public double sumDouble(AggregateDouble<T> aggregate);
   public int sumInt(AggregateInteger<T> aggregate);
   public double maxDouble(AggregateDouble<T> aggregate);
   public int maxInt(AggregateInteger<T> aggregate);
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate);
   public DBSet<T> unique();
   public static interface AggregateGroup<W, U, V> extends Serializable {
      public V aggregateSelect(W key, DBSet<U> val);
   }
   public <U, V> DBSet<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate);
}