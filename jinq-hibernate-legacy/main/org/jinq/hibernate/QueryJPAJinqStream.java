package org.jinq.hibernate;

import org.jinq.jpa.JPAJinqStream;
import org.jinq.orm.internal.QueryComposer;
import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.QueryJinqStream;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;

class QueryJPAJinqStream<T> extends QueryJinqStream<T> implements JPAJinqStream<T> 
{
   final HibernateQueryComposer<T> jpaComposer;
   
   public QueryJPAJinqStream(QueryComposer<T> query)
   {
      super(query);
      if (!(query instanceof HibernateQueryComposer)) throw new IllegalArgumentException("Cannot make a JPA stream without a JPA Query Composer");
      jpaComposer = (HibernateQueryComposer<T>)query; 
   }
   
   public QueryJPAJinqStream(QueryComposer<T> query,
         InQueryStreamSource inQueryStreamSource)
   {
      super(query, inQueryStreamSource);
      if (!(query instanceof HibernateQueryComposer)) throw new IllegalArgumentException("Cannot make a JPA stream without a JPA Query Composer");
      jpaComposer = (HibernateQueryComposer<T>)query; 
   }

   protected <U> JPAJinqStream<U> makeQueryStream(QueryComposer<U> query, InQueryStreamSource inQueryStreamSource)
   {
      return new QueryJPAJinqStream<>(query, inQueryStreamSource);
   }
   
   private <U> JPAJinqStream<U> wrap(JinqStream<U> toWrap)
   {
      if (toWrap instanceof JPAJinqStream) return (JPAJinqStream<U>)toWrap;
      if (toWrap instanceof QueryJinqStream) throw new IllegalArgumentException("Should not be possible to get a non-JPA JinqStream here");
      return new JPAJinqStreamWrapper<>(toWrap);
   }
   
   // New JPA-specific API
   @Override
   public <U> JPAJinqStream<T> joinFetch(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      QueryComposer<T> newComposer = jpaComposer.joinFetch(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return new JPAJinqStreamWrapper<>(this).joinFetch(join);
   }

   @Override
   public <U> JPAJinqStream<T> joinFetchList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      QueryComposer<T> newComposer = jpaComposer.joinFetchIterable(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return new JPAJinqStreamWrapper<>(this).joinFetchList(join);
   }

   @Override
   public <U> JPAJinqStream<T> leftOuterJoinFetch(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      QueryComposer<T> newComposer = jpaComposer.leftOuterJoinFetch(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return new JPAJinqStreamWrapper<>(this).leftOuterJoinFetch(join);
   }

   @Override
   public <U> JPAJinqStream<T> leftOuterJoinFetchList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      QueryComposer<T> newComposer = jpaComposer.leftOuterJoinFetchIterable(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return new JPAJinqStreamWrapper<>(this).leftOuterJoinFetchList(join);
   }

   // Wrapped versions of old API
   
   @Override
   public <E extends Exception> JPAJinqStream<T> where(
         org.jinq.orm.stream.JinqStream.Where<T, E> test)
   {
      return wrap(super.where(test));
   }

   @Override
   public <E extends Exception> JPAJinqStream<T> where(
         org.jinq.orm.stream.JinqStream.WhereWithSource<T, E> test)
   {
      return wrap(super.where(test));
   }

   @Override
   public <U> JPAJinqStream<U> select(
         org.jinq.orm.stream.JinqStream.Select<T, U> select)
   {
      return wrap(super.select(select));
   }

   @Override
   public <U> JPAJinqStream<U> select(
         org.jinq.orm.stream.JinqStream.SelectWithSource<T, U> select)
   {
      return wrap(super.select(select));
   }

   @Override
   public <U> JPAJinqStream<U> selectAll(
         org.jinq.orm.stream.JinqStream.Join<T, U> select)
   {
      return wrap(super.selectAll(select));
   }

   @Override
   public <U> JPAJinqStream<U> selectAll(
         org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> select)
   {
      return wrap(super.selectAll(select));
   }

   @Override
   public <U> JPAJinqStream<U> selectAllList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> select)
   {
      return wrap(super.selectAllList(select));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> join(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      return wrap(super.join(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> join(
         org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> join)
   {
      return wrap(super.join(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> joinList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      return wrap(super.joinList(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoin(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      return wrap(super.leftOuterJoin(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoinList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      return wrap(super.leftOuterJoinList(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoin(
         org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> join,
         org.jinq.orm.stream.JinqStream.WhereForOn<T, U> on)
   {
      return wrap(super.leftOuterJoin(join, on));
   }
   
   @Override
   public <U, V> JPAJinqStream<Pair<U, V>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate)
   {
      return wrap(super.group(select, aggregate));
   }

   @Override
   public <U, V, W> JPAJinqStream<Tuple3<U, V, W>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2)
   {
      return wrap(super.group(select, aggregate1, aggregate2));
   }

   @Override
   public <U, V, W, X> JPAJinqStream<Tuple4<U, V, W, X>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3)
   {
      return wrap(super.group(select, aggregate1, aggregate2, aggregate3));
   }

   @Override
   public <U, V, W, X, Y> JPAJinqStream<Tuple5<U, V, W, X, Y>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Y> aggregate4)
   {
      return wrap(super.group(select, aggregate1, aggregate2, aggregate3, aggregate4));
   }

   @Override
   public <V extends Comparable<V>> JPAJinqStream<T> sortedBy(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> sortField)
   {
      return wrap(super.sortedBy(sortField));
   }

   @Override
   public <V extends Comparable<V>> JPAJinqStream<T> sortedDescendingBy(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> sortField)
   {
      return wrap(super.sortedDescendingBy(sortField));
   }

   @Override
   public JPAJinqStream<T> skip(long n)
   {
      return wrap(super.skip(n));
   }

   @Override
   public JPAJinqStream<T> limit(long n)
   {
      return wrap(super.limit(n));
   }

   @Override
   public JPAJinqStream<T> distinct()
   {
      return wrap(super.distinct());
   }

   @Override
   public JPAJinqStream<T> setHint(String name, Object value)
   {
      return wrap(super.setHint(name, value));
   }
}
