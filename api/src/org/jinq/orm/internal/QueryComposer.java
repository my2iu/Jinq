package org.jinq.orm.internal;

import java.util.Iterator;
import java.util.function.Consumer;

import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.JinqStream.Select;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple;

public interface QueryComposer<T>
{
   public String getDebugQueryString();
   
   // Actually executes the query and returns the results in an iterator
   public Iterator<T> executeAndReturnResultIterator(Consumer<Throwable> exceptionReporter);
   
   // Returns a new query with the given operation integrated in
   // (or returns null if the given operation cannot be integrated)
   public <V extends Comparable<V>> QueryComposer<T> sortedBy(
         JinqStream.CollectComparable<T, V> sorter, boolean isAscending);
   public QueryComposer<T> limit(long n);
   public QueryComposer<T> skip(long n);
   public QueryComposer<T> distinct();
   
   // New stuff for Queryll2
   public <E extends Exception> QueryComposer<T> where(JinqStream.Where<T, E> test);
   public <E extends Exception> QueryComposer<T> where(JinqStream.WhereWithSource<T, E> test);
   public <U> QueryComposer<U> select(JinqStream.Select<T, U> select);
   public <U> QueryComposer<U> select(JinqStream.SelectWithSource<T, U> select);
   public <U> QueryComposer<Pair<T, U>> join(JinqStream.Join<T,U> join);
   public <U> QueryComposer<Pair<T, U>> join(JinqStream.JoinWithSource<T,U> join);
   public <U> QueryComposer<Pair<T, U>> leftOuterJoin(JinqStream.Join<T,U> join);
//   public <U, V> QueryComposer<Pair<U, V>> group(JinqStream.Select<T, U> select, JinqStream.AggregateGroup<U, T, V> aggregate);

   // returns null if the aggregates cannot be calculated
   public Long count();
   public <V extends Number & Comparable<V>> Number sum(JinqStream.CollectNumber<T, V> aggregate, Class<V> collectClass);
   public <V extends Comparable<V>> V max(JinqStream.CollectComparable<T, V> aggregate);
   public <V extends Comparable<V>> V min(JinqStream.CollectComparable<T, V> aggregate);
   public <V extends Number & Comparable<V>> Double avg(JinqStream.CollectNumber<T, V> aggregate);
//   public <U> U selectAggregates(JinqStream.AggregateSelect<T, U> aggregate);
   
   public <U extends Tuple> U multiaggregate(JinqStream.AggregateSelect<T, ?>[] aggregates);
   public <U, W extends Tuple> QueryComposer<W> groupToTuple(Select<T, U> select, JinqStream.AggregateGroup<U, T, ?>[] aggregates);

   public void setHint(String name, Object val);

}
