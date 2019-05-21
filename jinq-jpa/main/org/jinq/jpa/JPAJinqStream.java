package org.jinq.jpa;

import java.util.Collection;
import java.util.stream.Stream;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.NonQueryJinqStream;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;
import org.jinq.tuples.Tuple6;
import org.jinq.tuples.Tuple7;
import org.jinq.tuples.Tuple8;

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

   /**
    * Emulates a union of two different streams by merging the queries
    * using an OR operation. JPA does not support UNION operations, so 
    * Jinq must emulate that behavior using ORs. It also provides a mechanism
    * for Jinq to let people create OR expressions programmatically and to specify
    * complex expressions exactly without relying on the Jinq translation algorithm.
    * Due to the limitations of using OR to emulate a UNION, the two streams
    * being UNIONed must be made up of the same queries except for different
    * where restrictions. 
    * 
    * The code that checks whether a merge of the two queries is valid or not
    * is not very robust, so don't rely on Jinq to automatically catch incorrect
    * uses of the method. Some corner cases aren't checked, so be sure to
    * verify that you're calling the orUnion on two valid streams yourself.
    *  
    * @param otherSet the other stream to merge with
    * @return a new stream with the contents of the two streams UNIONed together
    */
   public JPAJinqStream<T> orUnion(JPAJinqStream<T> otherSet);

   /**
    * Emulates an intersect of two different streams by merging the queries
    * using an AND operation. JPA does not support INTERSECT operations, so 
    * Jinq must emulate that behavior using ANDs. It also provides a mechanism
    * for Jinq to let people create AND expressions programmatically and to specify
    * complex expressions exactly without relying on the Jinq translation algorithm.
    * Due to the limitations of using AND to emulate a INTERSECT, the two streams
    * being INTERSECTed must be made up of the same queries except for different
    * where restrictions. 
    * 
    * The code that checks whether a merge of the two queries is valid or not
    * is not very robust, so don't rely on Jinq to automatically catch incorrect
    * uses of the method. Some corner cases aren't checked, so be sure to
    * verify that you're calling the orUnion on two valid streams yourself.
    *  
    * @param otherSet the other stream to merge with
    * @return a new stream with the contents of the two streams INTERSECTed together
    */
   public JPAJinqStream<T> andIntersect(JPAJinqStream<T> otherSet);
   
   /**
    * Emulates a complement of stream by negation of the query
    * using a NOT operation. JPA does not support NOT operations, so 
    * Jinq must emulate that behavior using NOTs. It also provides a mechanism
    * for Jinq to let people create NOT expressions programmatically and to specify
    * complex expressions exactly without relying on the Jinq translation algorithm.
    *<p>
    * Since Universe of collection-based <code>NonQueryJinqStream</code> is not well defined,
    * <code>notComplement()</code> is not supported for such streams.
    * 
    * @return a new stream being a COMPLEMENT of <code>this</code> stream,
    * if <code>this</code> is actually QueryJPAJinqStream and its complement can be succesfully translated
    * @throws UnsupportedOperationException otherwise
    * 
    * @see difference(JPAJinqStream<T> otherSet);
    */   
   public JPAJinqStream<T> notComplement();
   
   
   // andNotDifference of sets
   public JPAJinqStream<T> andNotDifference(JPAJinqStream<T> otherSet);
   
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
         AggregateGroup<U, T, W> aggregate2, AggregateGroup<U, T, X> aggregate3, 
         AggregateGroup<U, T, Y> aggregate4);

   @Override
   public <U, V, W, X, Y, Z> JinqStream<Tuple6<U, V, W, X, Y, Z>> group(
         Select<T, U> select, AggregateGroup<U, T, V> aggregate1,
         AggregateGroup<U, T, W> aggregate2, AggregateGroup<U, T, X> aggregate3, 
         AggregateGroup<U, T, Y> aggregate4, AggregateGroup<U, T, Z> aggregate5);

   @Override
   public <U, V, W, X, Y, Z, A> JinqStream<Tuple7<U, V, W, X, Y, Z, A>> group(
         Select<T, U> select, AggregateGroup<U, T, V> aggregate1,
         AggregateGroup<U, T, W> aggregate2, AggregateGroup<U, T, X> aggregate3, 
         AggregateGroup<U, T, Y> aggregate4, AggregateGroup<U, T, Z> aggregate5,
         AggregateGroup<U, T, A> aggregate6);

   @Override
   public <U, V, W, X, Y, Z, A, B> JinqStream<Tuple8<U, V, W, X, Y, Z, A, B>> group(
         Select<T, U> select, AggregateGroup<U, T, V> aggregate1,
         AggregateGroup<U, T, W> aggregate2, AggregateGroup<U, T, X> aggregate3, 
         AggregateGroup<U, T, Y> aggregate4, AggregateGroup<U, T, Z> aggregate5,
         AggregateGroup<U, T, A> aggregate6, AggregateGroup<U, T, B> aggregate7);

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
   
      /**
    * Easy way to get a JinqStream from a collection.
    */
   public static <U> JPAJinqStream<U> from(Collection<U> collection)
   {
      return new JPAJinqStreamWrapper<>(new NonQueryJinqStream<U>(collection.stream()));
   }

   /**
    * Creates a JinqStream containing a single object.
    */
   public static <U> JPAJinqStream<U> of(U value, Stream<U> universe)
   {
      return new JPAJinqStreamWrapper<>(new NonQueryJinqStream<>(Stream.of(value)));
   }
}
