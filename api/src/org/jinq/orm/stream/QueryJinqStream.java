package org.jinq.orm.stream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jinq.orm.internal.QueryComposer;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple;

public class QueryJinqStream<T> extends NonQueryJinqStream<T> implements JinqStream<T>
{
   QueryComposer<T> queryComposer;
   public QueryJinqStream(QueryComposer<T> query)
   {
      this(query, null);
   }
   
   public QueryJinqStream(QueryComposer<T> query, InQueryStreamSource inQueryStreamSource)
   {
      super(inQueryStreamSource);
      this.queryComposer = query;
   }
   
   protected <U> JinqStream<U> makeQueryStream(QueryComposer<U> query, InQueryStreamSource inQueryStreamSource)
   {
      return new QueryJinqStream<>(query, inQueryStreamSource);
   }
   
   protected Stream<T> createWrappedStream() 
   {
      return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                  queryComposer.executeAndReturnResultIterator( err -> propagateException(this, err) ), 
                  Spliterator.CONCURRENT), 
            false);
   }
   
   @Override
   public <E extends Exception> JinqStream<T> where(final Where<T, E> test)
   {
      QueryComposer<T> newComposer = queryComposer.where(test);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.where(test);
   }

   @Override
   public <E extends Exception> JinqStream<T> where(
         org.jinq.orm.stream.JinqStream.WhereWithSource<T, E> test)
   {
      QueryComposer<T> newComposer = queryComposer.whereWithSource(test);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.where(test);
   }
   
   @Override
   public <U> JinqStream<U> select(Select<T, U> select)
   {
      QueryComposer<U> newComposer = queryComposer.select(select);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.select(select);
   }

   @Override
   public <U> JinqStream<U> select(SelectWithSource<T, U> select)
   {
      QueryComposer<U> newComposer = queryComposer.selectWithSource(select);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.select(select);
   }

   @Override
   public <U> JinqStream<U> selectAll(Join<T, U> select)
   {
      QueryComposer<U> newComposer = queryComposer.selectAll(select);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.selectAll(select);
   }

   @Override
   public <U> JinqStream<U> selectAll(JoinWithSource<T, U> select)
   {
      QueryComposer<U> newComposer = queryComposer.selectAllWithSource(select);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.selectAll(select);
   }

   @Override
   public <U> JinqStream<U> selectAllList(JoinToIterable<T, U> select)
   {
      QueryComposer<U> newComposer = queryComposer.selectAllIterable(select);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.selectAllList(select);
   }
   
   @Override
   public <U> JinqStream<Pair<T, U>> join(Join<T,U> join)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.join(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.join(join);
   }
   
   @Override
   public <U> JinqStream<Pair<T, U>> join(JoinWithSource<T,U> join)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.joinWithSource(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.join(join);
   }

   @Override
   public <U> JinqStream<Pair<T, U>> joinList(JoinToIterable<T, U> join)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.joinIterable(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.joinList(join);
   }
   
   @Override
   public <U> JinqStream<Pair<T, U>> leftOuterJoin(Join<T,U> join)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.leftOuterJoin(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.leftOuterJoin(join);
   }

   @Override
   public <U> JinqStream<Pair<T, U>> leftOuterJoinList(JoinToIterable<T, U> join)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.leftOuterJoinIterable(join);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.leftOuterJoinList(join);
   }

   @Override
   public <U> JinqStream<Pair<T, U>> leftOuterJoin(JoinWithSource<T, U> join, WhereForOn<T, U> on)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.leftOuterJoinWithSource(join, on);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.leftOuterJoin(join, on);
   }
   
   @Override
   protected <U, W extends Tuple> JinqStream<W> groupToTuple(Select<T, U> select, AggregateGroup<U, T, ?>[] aggregates)
   {
      QueryComposer<W> newComposer = queryComposer.groupToTuple(select, aggregates);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.groupToTuple(select, aggregates);
   }

//   @Override
//   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate)
//   {
//      QueryComposer<Pair<U, V>> newComposer = queryComposer.group(select, aggregate);
//      if (newComposer != null) return new QueryJinqStream<Pair<U, V>>(newComposer, inQueryStreamSource);
//      return super.group(select, aggregate);
//   }
   
   @Override
   public long count()
   {
      Long count = queryComposer.count();
      if (count != null) return count;
      return super.count();
   }

   @Override
   public Long sumInteger(CollectInteger<T> aggregate)
   {
      Long val = (Long)queryComposer.sum(aggregate, Integer.class);
      if (val != null) return val;
      return super.sumInteger(aggregate);
   }
   @Override
   public Long sumLong(CollectLong<T> aggregate)
   {
      Long val = (Long)queryComposer.sum(aggregate, Long.class);
      if (val != null) return val;
      return super.sumLong(aggregate);
   }
   @Override
   public Double sumDouble(CollectDouble<T> aggregate)
   {
      Double val = (Double)queryComposer.sum(aggregate, Double.class);
      if (val != null) return val;
      return super.sumDouble(aggregate);
   }
   @Override
   public BigDecimal sumBigDecimal(CollectBigDecimal<T> aggregate)
   {
      BigDecimal val = (BigDecimal)queryComposer.sum(aggregate, BigDecimal.class);
      if (val != null) return val;
      return super.sumBigDecimal(aggregate);
   }
   @Override
   public BigInteger sumBigInteger(CollectBigInteger<T> aggregate)
   {
      BigInteger val = (BigInteger)queryComposer.sum(aggregate, BigInteger.class);
      if (val != null) return val;
      return super.sumBigInteger(aggregate);
   }
   
   @Override
   public <V extends Comparable<V>> V max(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> aggregate)
   {
      V val = (V)queryComposer.max(aggregate);
      if (val != null) return val;
      return super.max(aggregate);
   }

   @Override
   public <V extends Comparable<V>> V min(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> aggregate)
   {
      V val = (V)queryComposer.min(aggregate);
      if (val != null) return val;
      return super.min(aggregate);
   }

   @Override
   public <V extends Number & Comparable<V>> Double avg(
         org.jinq.orm.stream.JinqStream.CollectNumber<T, V> aggregate)
   {
      Double val = queryComposer.avg(aggregate);
      if (val != null) return val;
      return super.avg(aggregate);
   }
   
//   @Override
//   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
//   {
//      U val = queryComposer.selectAggregates(aggregate);
//      if (val != null) return val;
//      return super.selectAggregates(aggregate);
//   }
//   
   @Override
   public <V extends Comparable<V>> JinqStream<T> sortedBy(
         JinqStream.CollectComparable<T, V> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedBy(sorter, true);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.sortedBy(sorter);
   }

   @Override
   public <V extends Comparable<V>> JinqStream<T> sortedDescendingBy(
         JinqStream.CollectComparable<T, V> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedBy(sorter, false);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.sortedDescendingBy(sorter);
   }

   @Override
   public JinqStream<T> limit(long n)
   {
      QueryComposer<T> newComposer = queryComposer.limit(n);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.limit(n);
   }

   @Override
   public JinqStream<T> skip(long n)
   {
      QueryComposer<T> newComposer = queryComposer.skip(n);
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.skip(n);
   }
   
   @Override 
   public JinqStream<T> distinct()
   {
      QueryComposer<T> newComposer = queryComposer.distinct();
      if (newComposer != null) return makeQueryStream(newComposer, inQueryStreamSource);
      return super.distinct();
   }

   @Override
   public String getDebugQueryString()
   {
      return queryComposer.getDebugQueryString();
   }
   
   @Override
   <U extends Tuple> U multiaggregate(AggregateSelect<T, ?>[] aggregates)
   {
      U vals = queryComposer.multiaggregate(aggregates);
      if (vals != null) return vals;
      return super.multiaggregate(aggregates);
   }
   
   @Override
   public JinqStream<T> setHint(String name, Object value)
   {
      queryComposer.setHint(name, value);
      return this;
   }
}
