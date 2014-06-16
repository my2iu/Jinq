package org.jinq.orm.stream;

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

import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.StringSorter;
import ch.epfl.labos.iu.orm.Tuple3;

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
   
   protected <U> Stream<U> wrap(Stream<U> toWrap)
   {
      return new NonQueryJinqStream<>(toWrap);
   }

   
   @Override
   public <E extends Exception> JinqStream<T> where(Where<T, E> test)
   {
      return new NonQueryJinqStream<>(filter(val -> { 
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
      return new NonQueryJinqStream<>(map( val -> select.select(val) ));
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
         streamBuilder.accept(new Pair<>(entry.getKey(), aggregate.aggregateSelect(entry.getKey(), new NonQueryJinqStream<>(entry.getValue().stream()))));
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
      return aggregate.aggregateSelect(this);
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
      return new NonQueryJinqStream<>(
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
               Iterator<T> inputIterator = new Iterator<T>()
                     {
                        boolean hasRead = false;
                        boolean hasMore = true;
                        T peek;
                        @Override public boolean hasNext()
                        {
                           if (hasRead) return hasMore;
                           hasRead = true;
                           Object taken = DONE;
                           try {
                              taken = inputQueues[idx].take();
                           } catch (InterruptedException e)
                           {
                              Thread.currentThread().interrupt();
                           }
                           if (taken == DONE)
                              hasMore = false;
                           else
                              peek = (T)taken;
                           return hasMore;
                        }

                        @Override public T next()
                        {
                           if (!hasNext()) throw new NoSuchElementException();
                           hasRead = false;
                           return peek;
                        }
                     };
               JinqStream<T> stream = new NonQueryJinqStream<>(
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
