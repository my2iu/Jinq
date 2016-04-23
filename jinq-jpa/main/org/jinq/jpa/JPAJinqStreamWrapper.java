package org.jinq.jpa;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.LazyWrappedStream;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;

/**
 * Wraps a normal JinqStream but provides additional JPA functionality.
 */
class JPAJinqStreamWrapper<T> extends LazyWrappedStream<T> implements JPAJinqStream<T> 
{
   JinqStream<T> wrapped;
   public JPAJinqStreamWrapper(JinqStream<T> wrapped)
   {
      super(wrapped);
      this.wrapped = wrapped;
   }
   
   private <U> JPAJinqStream<U> wrap(JinqStream<U> toWrap)
   {
      return new JPAJinqStreamWrapper<>(toWrap);
   }
   
   @Override
   public Long sumInteger(
         org.jinq.orm.stream.JinqStream.CollectInteger<T> aggregate)
   {
      return wrapped.sumInteger(aggregate);
   }

   @Override
   public Long sumLong(org.jinq.orm.stream.JinqStream.CollectLong<T> aggregate)
   {
      return wrapped.sumLong(aggregate);
   }

   @Override
   public Double sumDouble(
         org.jinq.orm.stream.JinqStream.CollectDouble<T> aggregate)
   {
      return wrapped.sumDouble(aggregate);
   }

   @Override
   public BigDecimal sumBigDecimal(
         org.jinq.orm.stream.JinqStream.CollectBigDecimal<T> aggregate)
   {
      return wrapped.sumBigDecimal(aggregate);
   }

   @Override
   public BigInteger sumBigInteger(
         org.jinq.orm.stream.JinqStream.CollectBigInteger<T> aggregate)
   {
      return wrapped.sumBigInteger(aggregate);
   }

   @Override
   public <V extends Comparable<V>> V max(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> aggregate)
   {
      return wrapped.max(aggregate);
   }

   @Override
   public <V extends Comparable<V>> V min(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> aggregate)
   {
      return wrapped.min(aggregate);
   }

   @Override
   public <V extends Number & Comparable<V>> Double avg(
         org.jinq.orm.stream.JinqStream.CollectNumber<T, V> aggregate)
   {
      return wrapped.avg(aggregate);
   }

   @Override
   public <U, V> Pair<U, V> aggregate(
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, V> aggregate2)
   {
      return wrapped.aggregate(aggregate1, aggregate2);
   }

   @Override
   public <U, V, W> Tuple3<U, V, W> aggregate(
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, V> aggregate2,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, W> aggregate3)
   {
      return wrapped.aggregate(aggregate1, aggregate2, aggregate3);
   }

   @Override
   public <U, V, W, X> Tuple4<U, V, W, X> aggregate(
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, V> aggregate2,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, W> aggregate3,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, X> aggregate4)
   {
      return wrapped.aggregate(aggregate1, aggregate2, aggregate3, aggregate4);
   }

   @Override
   public <U, V, W, X, Y> Tuple5<U, V, W, X, Y> aggregate(
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, V> aggregate2,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, W> aggregate3,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, X> aggregate4,
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, Y> aggregate5)
   {
      return wrapped.aggregate(aggregate1, aggregate2, aggregate3, aggregate4, aggregate5);
   }

   @Override
   public long count()
   {
      return wrapped.count();
   }

   @Override
   public Optional<T> findOne()
   {
      return wrapped.findOne();
   }
   
   @Override
   public T getOnlyValue()
   {
      return wrapped.getOnlyValue();
   }

   @Override
   public List<T> toList()
   {
      return wrapped.toList();
   }

   @Override
   public String getDebugQueryString()
   {
      return wrapped.getDebugQueryString();
   }

   @Override
   public void propagateException(Object source, Throwable exception)
   {
      wrapped.propagateException(source, exception);
   }

   @Override
   public Collection<Throwable> getExceptions()
   {
      return wrapped.getExceptions();
   }

   @Override
   public <E extends Exception> JPAJinqStream<T> where(
         org.jinq.orm.stream.JinqStream.Where<T, E> test)
   {
      return wrap(wrapped.where(test));
   }

   @Override
   public <E extends Exception> JPAJinqStream<T> where(
         org.jinq.orm.stream.JinqStream.WhereWithSource<T, E> test)
   {
      return wrap(wrapped.where(test));
   }

   @Override
   public <U> JPAJinqStream<U> select(
         org.jinq.orm.stream.JinqStream.Select<T, U> select)
   {
      return wrap(wrapped.select(select));
   }

   @Override
   public <U> JPAJinqStream<U> select(
         org.jinq.orm.stream.JinqStream.SelectWithSource<T, U> select)
   {
      return wrap(wrapped.select(select));
   }

   @Override
   public <U> JPAJinqStream<U> selectAll(
         org.jinq.orm.stream.JinqStream.Join<T, U> select)
   {
      return wrap(wrapped.selectAll(select));
   }

   @Override
   public <U> JPAJinqStream<U> selectAll(
         org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> select)
   {
      return wrap(wrapped.selectAll(select));
   }

   @Override
   public <U> JPAJinqStream<U> selectAllList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> select)
   {
      return wrap(wrapped.selectAllList(select));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> join(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      return wrap(wrapped.join(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> join(
         org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> join)
   {
      return wrap(wrapped.join(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> joinList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      return wrap(wrapped.joinList(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoin(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      return wrap(wrapped.leftOuterJoin(join));
   }

   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoinList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      return wrap(wrapped.leftOuterJoinList(join));
   }
   
   @Override
   public <U> JPAJinqStream<Pair<T, U>> leftOuterJoin(
         org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> join, 
         org.jinq.orm.stream.JinqStream.WhereForOn<T, U> on)
   {
      return wrap(wrapped.leftOuterJoin(join, on));
   }


   @Override
   public <U, V> JPAJinqStream<Pair<U, V>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate)
   {
      return wrap(wrapped.group(select, aggregate));
   }

   @Override
   public <U, V, W> JPAJinqStream<Tuple3<U, V, W>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2)
   {
      return wrap(wrapped.group(select, aggregate1, aggregate2));
   }

   @Override
   public <U, V, W, X> JPAJinqStream<Tuple4<U, V, W, X>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3)
   {
      return wrap(wrapped.group(select, aggregate1, aggregate2, aggregate3));
   }

   @Override
   public <U, V, W, X, Y> JPAJinqStream<Tuple5<U, V, W, X, Y>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate1,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, W> aggregate2,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, X> aggregate3,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, Y> aggregate4)
   {
      return wrap(wrapped.group(select, aggregate1, aggregate2, aggregate3, aggregate4));
   }

   @Override
   public <V extends Comparable<V>> JPAJinqStream<T> sortedBy(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> sortField)
   {
      return wrap(wrapped.sortedBy(sortField));
   }

   @Override
   public <V extends Comparable<V>> JPAJinqStream<T> sortedDescendingBy(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> sortField)
   {
      return wrap(wrapped.sortedDescendingBy(sortField));
   }

   @Override
   public JPAJinqStream<T> skip(long n)
   {
      return wrap(wrapped.skip(n));
   }

   @Override
   public JPAJinqStream<T> limit(long n)
   {
      return wrap(wrapped.limit(n));
   }

   @Override
   public JPAJinqStream<T> distinct()
   {
      return wrap(wrapped.distinct());
   }

   @Override
   public JPAJinqStream<T> setHint(String name, Object value)
   {
      return wrap(wrapped.setHint(name, value));
   }


   // New JPAJinqStream API
   
   // TODO: These methods don't properly emulate the full behavior of 
   //   JOIN FETCH and LEFT OUTER JOIN FETCH
   
   @Override
   public <U> JPAJinqStream<T> joinFetch(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      return this;
   }

   @Override
   public <U> JPAJinqStream<T> joinFetchList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      return this;
   }

   @Override
   public <U> JPAJinqStream<T> leftOuterJoinFetch(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      return this;
   }

   @Override
   public <U> JPAJinqStream<T> leftOuterJoinFetchList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      return this;
   }


}
