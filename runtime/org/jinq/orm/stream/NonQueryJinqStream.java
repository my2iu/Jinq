package org.jinq.orm.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.QueryComposer;
import ch.epfl.labos.iu.orm.StringSorter;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.DBSet.AggregateDouble;
import ch.epfl.labos.iu.orm.DBSet.AggregateGroup;
import ch.epfl.labos.iu.orm.DBSet.AggregateInteger;
import ch.epfl.labos.iu.orm.DBSet.AggregateSelect;
import ch.epfl.labos.iu.orm.DBSet.Join;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;

public class NonQueryJinqStream<T> extends LazyWrappedStream<T> implements JinqStream<T>
{
   public NonQueryJinqStream(Stream<T> wrapped)
   {
      super(wrapped);
   }

   NonQueryJinqStream()
   {
      super();
   }
   
   @Override
   public JinqStream<T> where(Where<T> test)
   {
      return new NonQueryJinqStream<>(filter( val -> test.where(val) ));
   }

   @Override
   public <U> JinqStream<U> select(Select<T, U> select)
   {
      return new NonQueryJinqStream<>(map( val -> select.select(val) ));
   }

   @Override
   public <U> JinqStream<Pair<T, U>> join(Join<T,U> join)
   {
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<T,U>> streamBuilder = Stream.builder();
      forEach( left -> {
         for (U right : join.join(left))
            streamBuilder.accept(new Pair<>(left, right));
         });
      return new NonQueryJinqStream<>(streamBuilder.build());
   }
   
   @Override
   public JinqStream<T> unique()
   {
      return new NonQueryJinqStream<>(distinct());
   }
   
   @Override
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate)
   {
      // TODO: Change AggregateGroup to use Streams
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<U,V>> streamBuilder = Stream.builder();
      
      // TODO: Rewrite to use Collectors.groupingBy()
      HashMap<U, DBSet<T>> map = new HashMap<>();
      forEach( val -> {
         U group = select.select(val);
         if (!map.containsKey(group))
            map.put(group, new VectorSet<>());
         map.get(group).add(val);
      });
      for (Map.Entry<U, DBSet<T>> entry: map.entrySet())
         streamBuilder.accept(new Pair<>(entry.getKey(), aggregate.aggregateSelect(entry.getKey(), entry.getValue())));
      return new NonQueryJinqStream<>(streamBuilder.build());
   }

   @Override
   public double sumDouble(AggregateDouble<T> aggregate)
   {
      // TODO: Rewrite using a summing collector 
      return reduce(0.0, 
            (accum, val) -> accum + aggregate.aggregate(val),
            (accum1, accum2) -> accum1 + accum2);
   }
   
   @Override
   public int sumInt(AggregateInteger<T> aggregate)
   {
      return reduce(0, 
            (accum, val) -> accum + aggregate.aggregate(val),
            (accum1, accum2) -> accum1 + accum2);
   }

   @Override
   public double maxDouble(AggregateDouble<T> aggregate)
   {
      return reduce(Double.NEGATIVE_INFINITY, 
            (accum, val) -> Math.max(accum, aggregate.aggregate(val)),
            (accum1, accum2) -> Math.max(accum1, accum2));
   }
   
   @Override
   public int maxInt(AggregateInteger<T> aggregate)
   {
      return reduce(Integer.MIN_VALUE, 
            (accum, val) -> Math.max(accum, aggregate.aggregate(val)),
            (accum1, accum2) -> Math.max(accum1, accum2));
   }
   
   @Override
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      // TODO: Modify aggregate to take a stream not a DBSet
      DBSet<T> set = collect(Collectors.toCollection(() -> new VectorSet<>()));
      return aggregate.aggregateSelect(set);
   }

   @Override
   public JinqStream<T> sortedByIntAscending(IntSorter<T> sorter)
   {
      return new NonQueryJinqStream<>(sorted(
         (o1, o2) -> sorter.value(o1) - sorter.value(o2)));
   }

   @Override
   public JinqStream<T> sortedByIntDescending(IntSorter<T> sorter)
   {
      return new NonQueryJinqStream<>(sorted(
            (o1, o2) -> sorter.value(o2) - sorter.value(o1)));
   }

   @Override
   public JinqStream<T> sortedByDoubleAscending(DoubleSorter<T> sorter)
   {
      return new NonQueryJinqStream<>(sorted(
            (o1, o2) -> (int)Math.signum(sorter.value(o1) - sorter.value(o2))));
   }

   @Override
   public JinqStream<T> sortedByDoubleDescending(DoubleSorter<T> sorter)
   {
      return new NonQueryJinqStream<>(sorted(
            (o1, o2) -> (int)Math.signum(sorter.value(o2) - sorter.value(o1))));
   }

   @Override
   public JinqStream<T> sortedByStringAscending(StringSorter<T> sorter)
   {
      return new NonQueryJinqStream<>(sorted(
            (o1, o2) -> sorter.value(o1).compareTo(sorter.value(o2))));
   }

   @Override
   public JinqStream<T> sortedByStringDescending(StringSorter<T> sorter)
   {
      return new NonQueryJinqStream<>(sorted(
            (o1, o2) -> -sorter.value(o1).compareTo(sorter.value(o2))));
   }

   @Override
   public JinqStream<T> sortedByDateAscending(DateSorter<T> sorter)
   {
      return new NonQueryJinqStream<>(sorted(
            (o1, o2) -> sorter.value(o1).compareTo(sorter.value(o2))));
   }

   @Override
   public JinqStream<T> sortedByDateDescending(DateSorter<T> sorter)
   {
      return new NonQueryJinqStream<>(sorted(
            (o1, o2) -> -sorter.value(o1).compareTo(sorter.value(o2))));
   }

   @Override
   public JinqStream<T> firstN(int n)
   {
      return new NonQueryJinqStream<>(limit(n));
   }
   
   public T getOnlyValue()
   {
      List<T> vals = collect(Collectors.toList());
      if (vals.size() == 1) return vals.get(0);
      throw new NoSuchElementException();
   }
   
   public JinqStream<T> with(T toAdd)
   {
      return new NonQueryJinqStream<>(
            Stream.concat(this, Stream.of(toAdd)));
   }
}
