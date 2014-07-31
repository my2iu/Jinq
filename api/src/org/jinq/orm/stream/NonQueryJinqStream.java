package org.jinq.orm.stream;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jinq.orm.stream.JinqStream.CollectBigDecimal;
import org.jinq.orm.stream.JinqStream.CollectBigInteger;
import org.jinq.orm.stream.JinqStream.CollectDouble;
import org.jinq.orm.stream.JinqStream.CollectInteger;
import org.jinq.orm.stream.JinqStream.CollectLong;
import org.jinq.orm.stream.JinqStream.CollectNumber;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;

import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.StringSorter;

public class NonQueryJinqStream<T> extends LazyWrappedStream<T> implements JinqStream<T>
{
   public NonQueryJinqStream(Stream<T> wrapped)
   {
      this(wrapped, null);
   }

   protected InQueryStreamSource inQueryStreamSource;
   public NonQueryJinqStream(Stream<T> wrapped, InQueryStreamSource inQueryStreamSource)
   {
      super(wrapped);
      this.inQueryStreamSource = inQueryStreamSource;
   }

   NonQueryJinqStream()
   {
      this((InQueryStreamSource)null);
   }
   
   NonQueryJinqStream(InQueryStreamSource inQueryStreamSource)
   {
      super();
      this.inQueryStreamSource = inQueryStreamSource;
   }
   
   protected <U> JinqStream<U> wrap(Stream<U> toWrap)
   {
      return new NonQueryJinqStream<>(toWrap, inQueryStreamSource);
   }

   
   @Override
   public <E extends Exception> JinqStream<T> where(Where<T, E> test)
   {
      return wrap(filter(val -> { 
            try { 
               return test.where(val); 
            } catch (Exception e) {
               // Record that an exception occurred
               propagateException(test, e);
               // Throw a runtime exception to try and kill the stream?
               throw new RuntimeException(e);
            }} ));
   }

   @Override
   public <U> JinqStream<U> select(Select<T, U> select)
   {
      return wrap(map( val -> select.select(val) ));
   }

   @Override
   public <U> JinqStream<Pair<T, U>> join(Join<T,U> join)
   {
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<T,U>> streamBuilder = Stream.builder();
      forEach( left -> {
         join.join(left).forEach( right -> 
            { streamBuilder.accept(new Pair<>(left, right)); });
         });
      return wrap(streamBuilder.build());
   }

   @Override
   public <U> JinqStream<Pair<T, U>> join(JoinWithSource<T,U> join)
   {
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<T,U>> streamBuilder = Stream.builder();
      forEach( left -> {
         join.join(left, inQueryStreamSource).forEach( right -> 
            { streamBuilder.accept(new Pair<>(left, right)); });
         });
      return wrap(streamBuilder.build());
   }

   @Override
   public JinqStream<T> unique()
   {
      return wrap(distinct());
   }
   
   @Override
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate)
   {
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<U,V>> streamBuilder = Stream.builder();
      
      // TODO: Rewrite to use Collectors.groupingBy()
      HashMap<U, List<T>> map = new HashMap<>();
      forEach( val -> {
         U group = select.select(val);
         if (!map.containsKey(group))
            map.put(group, new ArrayList<>());
         map.get(group).add(val);
      });
      for (Map.Entry<U, List<T>> entry: map.entrySet())
         streamBuilder.accept(new Pair<>(entry.getKey(), aggregate.aggregateSelect(entry.getKey(), wrap(entry.getValue().stream()))));
      return wrap(streamBuilder.build());
   }

   private static <V extends Number> V genericSum(V a, V b)
   {
      if (a == null) return b;
      if (b == null) return a;
      if (!a.getClass().equals(b.getClass())) throw new IllegalArgumentException("Mismatched number types");
      if (a instanceof Long) return (V)Long.valueOf(a.longValue() + b.longValue());
      if (a instanceof Integer) return (V)Integer.valueOf(a.intValue() + b.intValue());
      if (a instanceof Double) return (V)Double.valueOf(a.doubleValue() + b.doubleValue());
      if (a instanceof BigDecimal) return (V)((BigDecimal)a).add((BigDecimal)b);
      if (a instanceof BigInteger) return (V)((BigInteger)a).add((BigInteger)b);
      throw new IllegalArgumentException("Summing unknown number types");
   }
   
   @Override
   public Long sumInteger(CollectInteger<T> aggregate)
   {
      return reduce((Long)null, 
            (accum, val) -> genericSum(accum, (long)aggregate.aggregate(val)),
            (accum1, accum2) -> genericSum(accum1, accum2));
   }
   @Override
   public Long sumLong(CollectLong<T> aggregate)
   {
      return reduce((Long)null, 
            (accum, val) -> genericSum(accum, aggregate.aggregate(val)),
            (accum1, accum2) -> genericSum(accum1, accum2));
   }
   @Override
   public Double sumDouble(CollectDouble<T> aggregate)
   {
      return reduce((Double)null, 
            (accum, val) -> genericSum(accum, aggregate.aggregate(val)),
            (accum1, accum2) -> genericSum(accum1, accum2));
   }
   @Override
   public BigDecimal sumBigDecimal(CollectBigDecimal<T> aggregate)
   {
      return reduce((BigDecimal)null, 
            (accum, val) -> genericSum(accum, aggregate.aggregate(val)),
            (accum1, accum2) -> genericSum(accum1, accum2));
   }
   @Override
   public BigInteger sumBigInteger(CollectBigInteger<T> aggregate)
   {
      return reduce((BigInteger)null, 
            (accum, val) -> genericSum(accum, aggregate.aggregate(val)),
            (accum1, accum2) -> genericSum(accum1, accum2));
   }

   private static <V extends Comparable<V>> V genericCompare(boolean isMax, V a, V b)
   {
      if (a == null) return b;
      if (b == null) return a;
      if (isMax)
         return a.compareTo(b) <= 0 ? b : a;
      else
         return a.compareTo(b) >= 0 ? b : a;
   }
   
   @Override
   public <V extends Comparable<V>> V max(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> aggregate)
   {
      return reduce((V)null,
            (accum, val) -> genericCompare(true, accum, aggregate.aggregate(val)),
            (accum1, accum2) -> genericCompare(true, accum1, accum2));
   }

   @Override
   public <V extends Comparable<V>> V min(
         org.jinq.orm.stream.JinqStream.CollectComparable<T, V> aggregate)
   {
      return reduce((V)null,
            (accum, val) -> genericCompare(false, accum, aggregate.aggregate(val)),
            (accum1, accum2) -> genericCompare(false, accum1, accum2));
   }

   private static class GenericAverage
   {
      double sum = 0;
      int count = 0;
      synchronized <V extends Number> void accumulate(V a)
      {
         if (a == null) return;
         sum += a.doubleValue();
         count++;
      }
   }
   
   @Override
   public <V extends Number & Comparable<V>> Double avg(CollectNumber<T, V> aggregate)
   {
      final GenericAverage avg = new GenericAverage();
      forEach(val -> avg.accumulate(aggregate.aggregate(val)));
      if (avg.count == 0) return null;
      return avg.sum / avg.count;
   }

   @Override
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      return aggregate.aggregateSelect(this);
   }

   @Override
   public <V extends Comparable<V>> JinqStream<T> sortedBy(
         JinqStream.CollectComparable<T, V> sortField)
   {
      return wrap(sorted(
            (o1, o2) -> sortField.aggregate(o1).compareTo(sortField.aggregate(o2))));
   }

   @Override
   public <V extends Comparable<V>> JinqStream<T> sortedDescendingBy(
         JinqStream.CollectComparable<T, V> sortField)
   {
      return wrap(sorted(
            (o1, o2) -> -sortField.aggregate(o1).compareTo(sortField.aggregate(o2))));
   }

   @Override 
   public JinqStream<T> skip(long n)
   {
      return wrap(super.skip(n));
   }
   
   @Override 
   public JinqStream<T> limit(long n)
   {
      return wrap(super.limit(n));
   }

   @Override
   public T getOnlyValue()
   {
      List<T> vals = collect(Collectors.toList());
      if (vals.size() == 1) return vals.get(0);
      throw new NoSuchElementException();
   }
   
   @Override
   public JinqStream<T> with(T toAdd)
   {
      return wrap(
            Stream.concat(this, Stream.of(toAdd)));
   }
   
   @Override
   public List<T> toList()
   {
      return collect(Collectors.toList());
   }
   
   @Override
   public String getDebugQueryString()
   {
      // TODO: It would be nice if this could follow the stream chain
      //    down to get the underlying query (the stream chain isn't currently
      //    recorded, so this is not possible at the moment).
      return null;
   }
   
   protected Map<Object, Throwable> recordedExceptions = new HashMap<>();
   
   @Override
   public void propagateException(Object source, Throwable exception)
   {
      if (!recordedExceptions.containsKey(source))
         recordedExceptions.put(source, exception);
   }

   @Override
   public Collection<Throwable> getExceptions()
   {
      return recordedExceptions.values();
   }

   @Override
   public <U, V> Pair<U, V> aggregate(AggregateSelect<T, U> aggregate1, AggregateSelect<T, V> aggregate2)
   {
      AggregateSelect<T, ?>[] aggregates = new AggregateSelect[]
            {
               aggregate1, aggregate2
            };
      Object [] results = multiaggregate(aggregates);
      return new Pair<>((U)results[0], (V)results[1]);
   }

   @Override
   public <U, V, W> Tuple3<U, V, W> aggregate(AggregateSelect<T, U> aggregate1,
         AggregateSelect<T, V> aggregate2, AggregateSelect<T, W> aggregate3)
   {
      AggregateSelect<T, ?>[] aggregates = new AggregateSelect[]
            {
               aggregate1, aggregate2, aggregate3
            };
      Object [] results = multiaggregate(aggregates);
      return new Tuple3<>((U)results[0], (V)results[1], (W)results[2]);
   }

   Object[] multiaggregate(AggregateSelect<T, ?>[] aggregates)
   {
      final int MAX_QUEUE_SIZE = 100;
      final Object DONE = new Object();
      
      // Make a copy of the input stream for each aggregate being calculated.
      final JinqStream[] inputStreams = new JinqStream[aggregates.length];
      final ArrayBlockingQueue<Object>[] inputQueues = new ArrayBlockingQueue[aggregates.length];
      for (int n = 0; n < aggregates.length; n++)
      {
         inputQueues[n] = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
      }
      
      Runnable startIterator = new Runnable() {
         boolean isStarted = false;
         @Override public synchronized void run()
         {
            if (isStarted) return;
            isStarted = true;
            new Thread() {
               @Override public void run()
               {
                  forEach( val -> {
                     for (int n = 0; n < inputQueues.length; n++)
                     {
                        try {
                           inputQueues[n].put(val);
                        } catch (InterruptedException e)
                        {
                           Thread.currentThread().interrupt();
                        }
                     }
                  });
                  try {
                     for (int n = 0; n < inputQueues.length; n++)
                        inputQueues[n].put(DONE);
                  } catch (InterruptedException e)
                  {
                     Thread.currentThread().interrupt();
                  }
               }
            }.start();
         }};
      
      // Run each aggregator in a separate thread so that we can
      // use producer-consumer queues and hence avoid using too much
      // memory.
      Thread [] aggregateThreads = new Thread[aggregates.length];
      final Object [] results = new Object[aggregates.length];
      for (int n = 0; n < aggregates.length; n++)
      {
         final int idx = n;
         final AggregateSelect<T, ?> fn = aggregates[idx];
         aggregateThreads[n] = new Thread() {
            @Override public void run()
            {
               startIterator.run();
               Iterator<T> inputIterator = new NextOnlyIterator<T>()
                     {
                        @Override
                        protected void generateNext()
                        {
                           Object taken = DONE;
                           try {
                              taken = inputQueues[idx].take();
                           } catch (InterruptedException e)
                           {
                              Thread.currentThread().interrupt();
                           }
                           if (taken == DONE)
                              noMoreElements();
                           else
                              nextElement((T)taken);
                        }
                     };
               JinqStream<T> stream = wrap(
                     StreamSupport.stream(
                           Spliterators.spliteratorUnknownSize(
                                 inputIterator, 
                                 Spliterator.CONCURRENT), 
                           false));
               results[idx] = fn.aggregateSelect(stream);
            }
         };
         aggregateThreads[n].start();
      }
      for (int n = 0; n < aggregateThreads.length; n++)
      {
         try {
            aggregateThreads[n].join();
         } catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
      }
      return results;
   }

   @Override
   public JinqStream<T> setHint(String name, Object value)
   {
      return this;
   }
}
