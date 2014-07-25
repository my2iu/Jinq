package ch.epfl.labos.iu.orm;

import java.util.Iterator;
import java.util.function.Consumer;

import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;

public interface QueryComposer<T>
{
   public String getDebugQueryString();
   
   // Actually executes the query and returns the results in an iterator
   public Iterator<T> executeAndReturnResultIterator(Consumer<Throwable> exceptionReporter);
   
   // Returns a new query with the given operation integrated in
   // (or returns null if the given operation cannot be integrated)
   public QueryComposer<T> with(T toAdd);
   public QueryComposer<T> sortedByInt(final IntSorter<T> sorter, boolean isAscending);
   public QueryComposer<T> sortedByDouble(final DoubleSorter<T> sorter, boolean isAscending);
   public QueryComposer<T> sortedByString(final StringSorter<T> sorter, boolean isAscending);
   public QueryComposer<T> sortedByDate(final DateSorter<T> sorter, boolean isAscending);
   public QueryComposer<T> firstN(int n);
   
   // New stuff for Queryll2
   public <E extends Exception> QueryComposer<T> where(JinqStream.Where<T, E> test);
   public <U> QueryComposer<U> select(JinqStream.Select<T, U> select);
   public <U> QueryComposer<Pair<T, U>> join(JinqStream.Join<T,U> join);
   public <U> QueryComposer<Pair<T, U>> join(JinqStream.JoinWithSource<T,U> join);
   public QueryComposer<T> unique();
   public <U, V> QueryComposer<Pair<U, V>> group(JinqStream.Select<T, U> select, JinqStream.AggregateGroup<U, T, V> aggregate);

   // returns null if the aggregates cannot be calculated
   public Long count();
   public <V extends Number> V sum(JinqStream.CollectNumber<T, V> aggregate);
   public Double sumDouble(JinqStream.AggregateDouble<T> aggregate);
   public Integer sumInt(JinqStream.AggregateInteger<T> aggregate);
   public Double maxDouble(JinqStream.AggregateDouble<T> aggregate);
   public Integer maxInt(JinqStream.AggregateInteger<T> aggregate);
   public <U> U selectAggregates(JinqStream.AggregateSelect<T, U> aggregate);
   
   public Object[] multiaggregate(JinqStream.AggregateSelect<T, ?>[] aggregates);
   
   public void setHint(String name, Object val);

}
