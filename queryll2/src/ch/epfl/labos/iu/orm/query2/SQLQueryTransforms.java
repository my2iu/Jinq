package ch.epfl.labos.iu.orm.query2;

import java.util.function.Predicate;

import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.StringSorter;

// These methods will clobber the query passed into it when making a 
// new one, so be sure to always pass in a copy of the query instead 
//  of the original

public interface SQLQueryTransforms
{
   <T> SQLQuery<T> where(SQLQuery<T> query, int lambdaThisIndex, DBSet.Where<T> test, Object emSource);
   <T, E extends Exception> SQLQuery<T> where(SQLQuery<T> query, int lambdaThisIndex, JinqStream.Where<T, E> test, Object emSource);
   <T,U> SQLQuery<U> select(SQLQuery<T> query, int lambdaThisIndex, DBSet.Select<T,U> select, Object emSource);
   <T,U> SQLQuery<U> select(SQLQuery<T> query, int lambdaThisIndex, JinqStream.Select<T,U> select, Object emSource);
   <T> SQLQuery<Integer> sumInt(SQLQuery<T> query, int lambdaThisIndex, DBSet.AggregateInteger<T> aggregate, Object emSource);
   <T> SQLQuery<Double> sumDouble(SQLQuery<T> query, int lambdaThisIndex, DBSet.AggregateDouble<T> aggregate, Object emSource);
   <T, U extends Number & Comparable<U>, V> SQLQuery<V> sum(SQLQuery<T> query, int lambdaThisIndex, JinqStream.CollectNumber<T, U> aggregate, Class<U> collectClass, Object emSource);
   <T, V extends Comparable<V>> SQLQuery<V> max(SQLQuery<T> query, int lambdaThisIndex, JinqStream.CollectComparable<T, V> aggregate, Object emSource);
   <T, V extends Comparable<V>> SQLQuery<V> min(SQLQuery<T> query, int lambdaThisIndex, JinqStream.CollectComparable<T, V> aggregate, Object emSource);
   <T> SQLQuery<Integer> maxInt(SQLQuery<T> query, int lambdaThisIndex, DBSet.AggregateInteger<T> aggregate, Object emSource);
   <T> SQLQuery<Double> maxDouble(SQLQuery<T> query, int lambdaThisIndex, DBSet.AggregateDouble<T> aggregate, Object emSource);
   <T,U> SQLQuery<Pair<T,U>> join(SQLQuery<T> query, int lambdaThisIndex, DBSet.Join<T,U> join, Object emSource);
   <T,U> SQLQuery<Pair<T,U>> join(SQLQuery<T> query, int lambdaThisIndex, JinqStream.Join<T,U> join, Object emSource);
   <T,U> SQLQuery<U> selectAggregates(SQLQuery<T> query, int lambdaThisIndex, DBSet.AggregateSelect<T,U> select, Object emSource);
   <T,U> SQLQuery<U> selectAggregates(SQLQuery<T> query, int lambdaThisIndex, JinqStream.AggregateSelect<T,U> select, Object emSource);
   <T> SQLQuery<Object[]> multiaggregate(SQLQuery<T> query, int lambdaThisIndex, JinqStream.AggregateSelect<T,?>[] aggregates, Object emSource);
   <T,U,V> SQLQuery<Pair<U,V>> group(SQLQuery<T> query, int lambdaSelectThisIndex, DBSet.Select<T,U> select, int lambdaAggregateThisIndex, DBSet.AggregateGroup<U, T, V> aggregate, Object emSource);
   <T,U,V> SQLQuery<Pair<U,V>> group(SQLQuery<T> query, int lambdaSelectThisIndex, JinqStream.Select<T,U> select, int lambdaAggregateThisIndex, JinqStream.AggregateGroup<U, T, V> aggregate, Object emSource);
   <T> SQLQuery<T> firstN(SQLQuery<T> query, int n, Object emSource);
   <T, V extends Comparable<V>> SQLQuery<T> sortedBy(SQLQuery<T> query, int lambdaThisIndex, JinqStream.CollectComparable<T, V> sorter, boolean isAscending, Object emSource);
   <T> SQLQuery<T> sortedByDate(SQLQuery<T> query, int lambdaThisIndex, DateSorter<T> sorter, boolean isAscending, Object emSource);
   <T> SQLQuery<T> sortedByInt(SQLQuery<T> query, int lambdaThisIndex, IntSorter<T> sorter, boolean isAscending, Object emSource);
   <T> SQLQuery<T> sortedByDouble(SQLQuery<T> query, int lambdaThisIndex, DoubleSorter<T> sorter, boolean isAscending, Object emSource);
   <T> SQLQuery<T> sortedByString(SQLQuery<T> query, int lambdaThisIndex, StringSorter<T> sorter, boolean isAscending, Object emSource);

}
