package ch.epfl.labos.iu.orm;

import java.util.Iterator;
import java.util.function.Predicate;

import org.jinq.orm.stream.JinqStream;

import ch.epfl.labos.iu.orm.DBSet.AggregateDouble;
import ch.epfl.labos.iu.orm.DBSet.AggregateGroup;
import ch.epfl.labos.iu.orm.DBSet.AggregateInteger;
import ch.epfl.labos.iu.orm.DBSet.AggregateSelect;
import ch.epfl.labos.iu.orm.DBSet.Join;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;

public interface QueryComposer<T>
{
   // Actually executes the query and returns the results in a set
   public VectorSet<T> createRealizedSet();

   // Actually executes the query and returns the results in an iterator
   public Iterator<T> executeAndReturnResultIterator();
   
   // Returns a new query with the given operation integrated in
   // (or returns null if the given operation cannot be integrated)
   public QueryComposer<T> with(T toAdd);
   public QueryComposer<T> sortedByInt(final IntSorter<T> sorter, boolean isAscending);
   public QueryComposer<T> sortedByDouble(final DoubleSorter<T> sorter, boolean isAscending);
   public QueryComposer<T> sortedByString(final StringSorter<T> sorter, boolean isAscending);
   public QueryComposer<T> sortedByDate(final DateSorter<T> sorter, boolean isAscending);
   public QueryComposer<T> firstN(int n);
   
   // New stuff for Queryll2
   public QueryComposer<T> where(Where<T> test);
   public QueryComposer<T> where(JinqStream.Where<T> test);
   public <U> QueryComposer<U> select(Select<T, U> select);
   public <U> QueryComposer<Pair<T, U>> join(Join<T,U> join);
   public QueryComposer<T> unique();
   public <U, V> QueryComposer<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate);

   // returns null if the aggregates cannot be calculated
   public Double sumDouble(AggregateDouble<T> aggregate);
   public Integer sumInt(AggregateInteger<T> aggregate);
   public Double maxDouble(AggregateDouble<T> aggregate);
   public Integer maxInt(AggregateInteger<T> aggregate);
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate);
}
