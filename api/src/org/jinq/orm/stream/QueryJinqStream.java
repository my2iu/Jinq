package org.jinq.orm.stream;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jinq.orm.stream.JinqStream.AggregateSelect;
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
      if (newComposer != null) return new QueryJinqStream<T>(newComposer);
      return super.where(test);
   }

   @Override
   public <U> JinqStream<U> select(Select<T, U> select)
   {
      QueryComposer<U> newComposer = queryComposer.select(select);
      if (newComposer != null) return new QueryJinqStream<U>(newComposer);
      return super.select(select);
   }
   
   @Override
   public <U> JinqStream<Pair<T, U>> join(Join<T,U> join)
   {
      QueryComposer<Pair<T, U>> newComposer = queryComposer.join(join);
      if (newComposer != null) return new QueryJinqStream<Pair<T, U>>(newComposer);
      return super.join(join);
   }
   
   @Override
   public JinqStream<T> unique()
   {
      QueryComposer<T> newComposer = queryComposer.unique();
      if (newComposer != null) return new QueryJinqStream<T>(newComposer);
      return super.unique();
   }
   
   @Override
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate)
   {
      QueryComposer<Pair<U, V>> newComposer = queryComposer.group(select, aggregate);
      if (newComposer != null) return new QueryJinqStream<Pair<U, V>>(newComposer);
      return super.group(select, aggregate);
   }

   @Override
   public double sumDouble(AggregateDouble<T> aggregate)
   {
      Double val = queryComposer.sumDouble(aggregate);
      if (val != null) return val;
      return super.sumDouble(aggregate);
   }
   
   @Override
   public int sumInt(AggregateInteger<T> aggregate)
   {
      Integer val = queryComposer.sumInt(aggregate);
      if (val != null) return val;
      return super.sumInt(aggregate);
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
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
      return super.sortedByIntAscending(sorter);
   }

   @Override
   public JinqStream<T> sortedByIntDescending(IntSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByInt(sorter, false);
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
      return super.sortedByIntDescending(sorter);
   }

   @Override
   public JinqStream<T> sortedByDoubleAscending(DoubleSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByDouble(sorter, true);
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
      return super.sortedByDoubleAscending(sorter);
   }

   @Override
   public JinqStream<T> sortedByDoubleDescending(DoubleSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByDouble(sorter, false);
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
      return super.sortedByDoubleDescending(sorter);
   }

   @Override
   public JinqStream<T> sortedByStringAscending(StringSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByString(sorter, true);
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
      return super.sortedByStringAscending(sorter);
   }

   @Override
   public JinqStream<T> sortedByStringDescending(StringSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByString(sorter, false);
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
      return super.sortedByStringDescending(sorter);
   }

   @Override
   public JinqStream<T> sortedByDateAscending(DateSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByDate(sorter, true);
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
      return super.sortedByDateAscending(sorter);
   }

   @Override
   public JinqStream<T> sortedByDateDescending(DateSorter<T> sorter)
   {
      QueryComposer<T> newComposer = queryComposer.sortedByDate(sorter, false);
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
      return super.sortedByDateDescending(sorter);
   }

   @Override
   public JinqStream<T> firstN(int n)
   {
      QueryComposer<T> newComposer = queryComposer.firstN(n);
      if (newComposer != null) return new QueryJinqStream<>(newComposer);
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
