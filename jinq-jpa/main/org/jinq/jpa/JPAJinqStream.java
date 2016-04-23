package org.jinq.jpa;

import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;

/**
 * An extension of the JinqStream that includes support for JPQL specific
 * features like JOIN FETCH 
 */
public interface JPAJinqStream<T> extends JinqStream<T>
{
   // New JPA-specific API
   
   /**
    * When executing the query, the items referred to be the plural 
    * association will be fetched as well. The stream itself will 
    * still return the same elements though.
    * 
    * <pre>
    * {@code JinqStream<Country> stream = ...;
    * JinqStream<Country> result = 
    *    stream.joinFetch(c -> JinqStream.from(c.getCities()));
    * }
    * </pre>
    * 
    * @param join
    *           function applied to the elements of the stream. When passed an
    *           element from the stream, the function should return a stream of
    *           values that should be fetched as well 
    */
   public <U> JPAJinqStream<T> joinFetch(Join<T, U> join);
   
   /**
    * @see #joinFetch(Join)
    */
   public <U> JPAJinqStream<T> joinFetchList(JoinToIterable<T, U> join);
   
   /**
    * @see #joinFetch(Join)
    */
   public <U> JPAJinqStream<T> leftOuterJoinFetch(Join<T, U> join);
   
   /**
    * @see #joinFetch(Join)
    */
   public <U> JPAJinqStream<T> leftOuterJoinFetchList(JoinToIterable<T, U> join);

   
   

   // Variants of the existing JinqStream API that return a JPAJinqStream instead
   // of a JinqStream.
   
   @Override
   public <E extends Exception> JPAJinqStream<T> where(Where<T, E> test);

   @Override
   public <E extends Exception> JPAJinqStream<T> where(WhereWithSource<T, E> test);

   @Override
   public <U> JPAJinqStream<U> select(Select<T, U> select);

   @Override
   public <U> JPAJinqStream<U> select(SelectWithSource<T, U> select);

   @Override
   public <U> JPAJinqStream<U> selectAll(Join<T, U> select);

   @Override
   public <U> JPAJinqStream<U> selectAll(JoinWithSource<T, U> select);

   @Override
   public <U> JPAJinqStream<U> selectAllList(JoinToIterable<T, U> select);
   
   @Override
   public <U> JPAJinqStream<Pair<T, U>> join(Join<T, U> join);

   @Override
   public <U> JPAJinqStream<Pair<T, U>> join(JoinWithSource<T, U> join);

   @Override
   public <U> JPAJinqStream<Pair<T, U>> joinList(JoinToIterable<T, U> join);

   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoin(Join<T, U> join);

   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoinList(JoinToIterable<T, U> join);

   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoin(
         JinqStream.JoinWithSource<T, U> join,
         JinqStream.WhereForOn<T, U> on);
   
   @Override
   public <U, V> JPAJinqStream<Pair<U, V>> group(Select<T, U> select,
         AggregateGroup<U, T, V> aggregate);

   @Override
   public <U, V, W> JPAJinqStream<Tuple3<U, V, W>> group(Select<T, U> select,
         AggregateGroup<U, T, V> aggregate1, AggregateGroup<U, T, W> aggregate2);

   @Override
   public <U, V, W, X> JPAJinqStream<Tuple4<U, V, W, X>> group(
         Select<T, U> select, AggregateGroup<U, T, V> aggregate1,
         AggregateGroup<U, T, W> aggregate2, AggregateGroup<U, T, X> aggregate3);
   
   @Override
   public <U, V, W, X, Y> JPAJinqStream<Tuple5<U, V, W, X, Y>> group(
         Select<T, U> select, AggregateGroup<U, T, V> aggregate1,
         AggregateGroup<U, T, W> aggregate2,
         AggregateGroup<U, T, X> aggregate3, AggregateGroup<U, T, Y> aggregate4);


   @Override
   public <V extends Comparable<V>> JPAJinqStream<T> sortedBy(
         CollectComparable<T, V> sortField);

   @Override
   public <V extends Comparable<V>> JPAJinqStream<T> sortedDescendingBy(
         CollectComparable<T, V> sortField);

   @Override
   public JPAJinqStream<T> skip(long n);

   @Override
   public JPAJinqStream<T> limit(long n);

   @Override
   public JPAJinqStream<T> distinct();
   
   public JPAJinqStream<T> setHint(String name, Object value);
}
