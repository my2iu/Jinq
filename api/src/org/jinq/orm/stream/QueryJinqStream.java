package org.jinq.orm.stream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jinq.orm.stream.JinqStream.CollectBigDecimal;
import org.jinq.orm.stream.JinqStream.CollectBigInteger;
import org.jinq.orm.stream.JinqStream.CollectDouble;
import org.jinq.orm.stream.JinqStream.CollectInteger;
import org.jinq.orm.stream.JinqStream.CollectLong;
import org.jinq.tuples.Pair;

import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.QueryComposer;
import ch.epfl.labos.iu.orm.StringSorter;

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
      if (newComposer != null) return new QueryJinqStream<T>(newComposer, inQueryStreamSource);
      return super.where(test);
   }

   @Override
   public <U> JinqStream<U> select(Select<T, U> select)
   {
      QueryComposer<U> newComposer = queryComposer.select(select);
      if (newComposer != null) return new QueryJinqStream<U>(newComposer, inQueryStreamSource);
      return super.select(select);
   }
   
   @Override
   public <U> JinqStream<Pair<T, U>> join(Join<T,U> join)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.join(join);
      if (newComposer != null) return new QueryJinqStream<Pair<T, U>>(newComposer, inQueryStreamSource);
      return super.join(join);
   }
   
   @Override
   public <U> JinqStream<Pair<T, U>> join(JoinWithSource<T,U> join)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.join(join);
      if (newComposer != null) return new QueryJinqStream<Pair<T, U>>(newComposer, inQueryStreamSource);
      return super.join(join);
   }

   @Override
   public JinqStream<T> unique()
   {
      QueryComposer<T> newComposer = queryComposer.unique();
      if (newComposer != null) return new QueryJinqStream<T>(newComposer, inQueryStreamSource);
      return super.unique();
   }
   
   @Override
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate)
   {
      QueryComposer<Pair<U, V>> newComposer = queryComposer.group(select, aggregate);
      if (newComposer != null) return new QueryJinqStream<Pair<U, V>>(newComposer, inQueryStreamSource);
      return super.group(select, aggregate);
   }
   
   @Override
   public long count()
   {
      Long count = queryComposer.count();
      if (count != null) return count;
      return super.count();
   }

//   @Override
//   public <V extends Number> V sum(
//         org.jinq.orm.stream.JinqStream.CollectNumber<T, V> aggregate)
//   {
//      V val = queryComposer.sum(aggregate);
//      if (val != null) return val;
//      return super.sum(aggregate);
//   }
   
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
   public double maxDouble(AggregateDouble<T> aggregate)
   {
      Double val = queryComposer.maxDouble(aggregate);
      if (val != null) return val;
      return super.maxDouble(aggregate);
   }
   
   @Override
   public int maxInt(AggregateInteger<T> aggregate)
   {
      Integer val = queryComposer.maxInt(aggregate);
      if (val != null) return val;
      return super.maxInt(aggregate);
   }
   
   @Override
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      U val = queryComposer.selectAggregates(aggregate);
      if (val != null) return val;
      return super.selectAggregates(aggregate);
   }

   @Override
   public JinqStream<T> sortedByIntAscending(IntSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByInt(sorter, true);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.sortedByIntAscending(sorter);
   }

   @Override
   public JinqStream<T> sortedByIntDescending(IntSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByInt(sorter, false);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.sortedByIntDescending(sorter);
   }

   @Override
   public JinqStream<T> sortedByDoubleAscending(DoubleSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByDouble(sorter, true);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.sortedByDoubleAscending(sorter);
   }

   @Override
   public JinqStream<T> sortedByDoubleDescending(DoubleSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByDouble(sorter, false);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.sortedByDoubleDescending(sorter);
   }

   @Override
   public JinqStream<T> sortedByStringAscending(StringSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByString(sorter, true);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.sortedByStringAscending(sorter);
   }

   @Override
   public JinqStream<T> sortedByStringDescending(StringSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByString(sorter, false);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.sortedByStringDescending(sorter);
   }

   @Override
   public JinqStream<T> sortedByDateAscending(DateSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByDate(sorter, true);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.sortedByDateAscending(sorter);
   }

   @Override
   public JinqStream<T> sortedByDateDescending(DateSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByDate(sorter, false);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.sortedByDateDescending(sorter);
   }

   @Override
   public JinqStream<T> firstN(int n)
   {
      QueryComposer<T> newComposer = queryComposer.firstN(n);
      if (newComposer != null) return new QueryJinqStream<>(newComposer, inQueryStreamSource);
      return super.firstN(n);
   }
   
   @Override
   public String getDebugQueryString()
   {
      return queryComposer.getDebugQueryString();
   }
   
   @Override
   Object[] multiaggregate(AggregateSelect<T, ?>[] aggregates)
   {
      Object [] vals = queryComposer.multiaggregate(aggregates);
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
