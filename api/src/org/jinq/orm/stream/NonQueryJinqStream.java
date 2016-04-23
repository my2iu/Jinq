package org.jinq.orm.stream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jinq.orm.stream.JinqStream.JoinWithSource;
import org.jinq.orm.stream.JinqStream.WhereForOn;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;

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
   public <E extends Exception> JinqStream<T> where(
         org.jinq.orm.stream.JinqStream.WhereWithSource<T, E> test)
   {
      return wrap(filter(val -> { 
         try { 
            return test.where(val, inQueryStreamSource); 
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
   public <U> JinqStream<U> select(SelectWithSource<T, U> select)
   {
      return wrap(map( val -> select.select(val, inQueryStreamSource) ));
   }

   @Override
   public <U> JinqStream<U> selectAll(Join<T, U> select)
   {
      return wrap(flatMap( val -> select.join(val) ));
   }

   @Override
   public <U> JinqStream<U> selectAll(JoinWithSource<T, U> select)
   {
      return wrap(flatMap( val -> select.join(val, inQueryStreamSource) ));
   }

   @Override
   public <U> JinqStream<U> selectAllList(JoinToIterable<T, U> select)
   {
      return wrap(flatMap( val -> StreamSupport.stream(select.join(val).spliterator(), false) ));
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
   public <U> JinqStream<Pair<T, U>> joinList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<T,U>> streamBuilder = Stream.builder();
      forEach( left -> {
         for (U right: join.join(left)) {
             streamBuilder.accept(new Pair<>(left, right)); 
         }});
      return wrap(streamBuilder.build());
   }

   @Override
   public <U> JinqStream<Pair<T, U>> leftOuterJoin(Join<T,U> join)
   {
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<T,U>> streamBuilder = Stream.builder();
      forEach( left -> {
         if (join.join(left).count() > 0)
            join.join(left).forEach( right -> 
               { streamBuilder.accept(new Pair<>(left, right)); });
         else
            streamBuilder.accept(new Pair<>(left, null));
         });
      return wrap(streamBuilder.build());
   }

   @Override
   public <U> JinqStream<Pair<T, U>> leftOuterJoinList(
         org.jinq.orm.stream.JinqStream.JoinToIterable<T, U> join)
   {
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<T,U>> streamBuilder = Stream.builder();
      forEach( left -> {
         int count = 0;
         for (U right: join.join(left)) {
            streamBuilder.accept(new Pair<>(left, right));
            count++;
         }
         if (count == 0)
            streamBuilder.accept(new Pair<>(left, null));
         });
      return wrap(streamBuilder.build());
   }

   @Override
   public <U> JinqStream<Pair<T, U>> leftOuterJoin(JoinWithSource<T, U> join, WhereForOn<T, U> on)
   {
      // TODO: This stream should be constructed on the fly
      final Stream.Builder<Pair<T,U>> streamBuilder = Stream.builder();
      forEach( left -> {
         AtomicBoolean wasMatched = new AtomicBoolean();
         join.join(left, inQueryStreamSource).forEach( right -> {
            if (on.where(left, right))
            {
               wasMatched.set(true);
               streamBuilder.accept(new Pair<>(left, right));
            }
         });
         if (!wasMatched.get())
            streamBuilder.accept(new Pair<>(left, null));
         });
      return wrap(streamBuilder.build());
   }
 
   protected <U, W extends Tuple> JinqStream<W> groupToTuple(Select<T, U> select, AggregateGroup<U, T, ?>[] aggregates)
   {
      Map<U, List<T>> groups = collect(Collectors.groupingBy(in -> select.select(in)));
      final Stream.Builder<W> streamBuilder = Stream.builder();
      for (Map.Entry<U, List<T>> entry: groups.entrySet())
      {
         Object[] groupAggregates = new Object[aggregates.length + 1];
         for (int n = 0; n < aggregates.length; n++)
            groupAggregates[n + 1] = aggregates[n].aggregateSelect(entry.getKey(), wrap(entry.getValue().stream()));
         groupAggregates[0] = (Object)entry.getKey();
         streamBuilder.accept(Tuple.createTuple(groupAggregates));
      }
      return (JinqStream<W>) wrap(streamBuilder.build());
   }
   
   @Override
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate)
   {
      @SuppressWarnings("unchecked")
      AggregateGroup<U, T, ?>[] aggregates = new AggregateGroup[] {
            aggregate
      };
      return groupToTuple(select, aggregates);
   }
   
   @Override
   public <U, V, W> JinqStream<Tuple3<U, V, W>> group(
         JinqStream.Select<T, U> select,
         JinqStream.AggregateGroup<U, T, V> aggregate1,
         JinqStream.AggregateGroup<U, T, W> aggregate2)
   {
      @SuppressWarnings("unchecked")
      AggregateGroup<U, T, ?>[] aggregates = new AggregateGroup[] {
            aggregate1, aggregate2,
      };
      return groupToTuple(select, aggregates);
   }

   @Override
   public <U, V, W, X> JinqStream<Tuple4<U, V, W, X>> group(
         JinqStream.Select<T, U> select,
         JinqStream.AggregateGroup<U, T, V> aggregate1,
         JinqStream.AggregateGroup<U, T, W> aggregate2,
         JinqStream.AggregateGroup<U, T, X> aggregate3)
   {
      @SuppressWarnings("unchecked")
      AggregateGroup<U, T, ?>[] aggregates = new AggregateGroup[] {
            aggregate1, aggregate2, aggregate3,
      };
      return groupToTuple(select, aggregates);
   }

   @Override
   public <U, V, W, X, Y> JinqStream<Tuple5<U, V, W, X, Y>> group(
         JinqStream.Select<T, U> select,
         JinqStream.AggregateGroup<U, T, V> aggregate1,
         JinqStream.AggregateGroup<U, T, W> aggregate2,
         JinqStream.AggregateGroup<U, T, X> aggregate3,
         JinqStream.AggregateGroup<U, T, Y> aggregate4)
   {
      @SuppressWarnings("unchecked")
      AggregateGroup<U, T, ?>[] aggregates = new AggregateGroup[] {
            aggregate1, aggregate2, aggregate3, aggregate4,
      };
      return groupToTuple(select, aggregates);
   }

   @SuppressWarnings("unchecked")
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

//   @Override
//   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
//   {
//      return aggregate.aggregateSelect(this);
//   }
//
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
   public JinqStream<T> distinct()
   {
      return wrap(distinct());
   }

   @Override
   public Optional<T> findOne()
   {
      List<T> vals = collect(Collectors.toList());
      if (vals.isEmpty()) return Optional.empty();
      if (vals.size() == 1) return Optional.of(vals.get(0));
      throw new NoSuchElementException();
   }

   @Override
   public T getOnlyValue()
   {
      List<T> vals = collect(Collectors.toList());
      if (vals.size() == 1) return vals.get(0);
      throw new NoSuchElementException();
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

//   @Override
//   public <U> U aggregate(AggregateSelect<T, U> aggregate1)
//   {
//      AggregateSelect<T, ?>[] aggregates = new AggregateSelect[]
//            {
//               aggregate1
//            };
//      Object [] results = multiaggregate(aggregates);
//      return (U)results[0];
//   }
   
   @Override
   public <U, V> Pair<U, V> aggregate(AggregateSelect<T, U> aggregate1, AggregateSelect<T, V> aggregate2)
   {
      @SuppressWarnings("unchecked")
      AggregateSelect<T, ?>[] aggregates = new AggregateSelect[]
            {
               aggregate1, aggregate2
            };
      return  multiaggregate(aggregates);
   }

   @Override
   public <U, V, W> Tuple3<U, V, W> aggregate(AggregateSelect<T, U> aggregate1,
         AggregateSelect<T, V> aggregate2, AggregateSelect<T, W> aggregate3)
   {
      @SuppressWarnings("unchecked")
      AggregateSelect<T, ?>[] aggregates = new AggregateSelect[]
            {
               aggregate1, aggregate2, aggregate3
            };
      return multiaggregate(aggregates);
   }


   @Override
   public <U, V, W, X> Tuple4<U, V, W, X> aggregate(
         JinqStream.AggregateSelect<T, U> aggregate1, JinqStream.AggregateSelect<T, V> aggregate2,
         JinqStream.AggregateSelect<T, W> aggregate3, JinqStream.AggregateSelect<T, X> aggregate4)
   {
      @SuppressWarnings("unchecked")
      AggregateSelect<T, ?>[] aggregates = new AggregateSelect[]
            {
               aggregate1, aggregate2, aggregate3, aggregate4
            };
      return multiaggregate(aggregates);
   }

   @Override
   public <U, V, W, X, Y> Tuple5<U, V, W, X, Y> aggregate(
         JinqStream.AggregateSelect<T, U> aggregate1, JinqStream.AggregateSelect<T, V> aggregate2,
         JinqStream.AggregateSelect<T, W> aggregate3, JinqStream.AggregateSelect<T, X> aggregate4,
         JinqStream.AggregateSelect<T, Y> aggregate5)
   {
      @SuppressWarnings("unchecked")
      AggregateSelect<T, ?>[] aggregates = new AggregateSelect[]
            {
               aggregate1, aggregate2, aggregate3, aggregate4, aggregate5
            };
      return multiaggregate(aggregates);
   }

   <U extends Tuple> U multiaggregate(AggregateSelect<T, ?>[] aggregates)
   {
      IteratorTee<T> tee = new IteratorTee<>(this, aggregates.length);
      
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
               JinqStream<T> stream = 
                     wrap(StreamSupport.stream(
                           Spliterators.spliteratorUnknownSize(
                                 tee.createIterator(idx), 
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
      return Tuple.createTuple(results);
   }
   
   public static class IteratorTee<T> 
   {
      static final int MAX_QUEUE_SIZE = 100;
      final Object DONE = new Object();
      
      ArrayBlockingQueue<Object>[] outputQueues;
      Stream<T> inputStream;
      public IteratorTee(Stream<T> inputStream, int size)
      {
         this.inputStream = inputStream;
         outputQueues = new ArrayBlockingQueue[size];
         for (int n = 0; n < size; n++)
            outputQueues[n] = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
      }
      
      boolean isStarted = false;
      synchronized void startInputStreamPump()
      {
         if (isStarted) return;
         isStarted = true;
         new Thread() {
            @Override public void run()
            {
               inputStream.forEach( val -> {
                  for (int n = 0; n < outputQueues.length; n++)
                  {
                     try {
                        outputQueues[n].put(val);
                     } catch (InterruptedException e)
                     {
                        Thread.currentThread().interrupt();
                     }
                  }
               });
               try {
                  for (int n = 0; n < outputQueues.length; n++)
                     outputQueues[n].put(DONE);
               } catch (InterruptedException e)
               {
                  Thread.currentThread().interrupt();
               }
            }
         }.start();
      }
      public Iterator<T> createIterator(int idx)
      {
         return new NextOnlyIterator<T>()
               {
                  @Override
                  protected void generateNext()
                  {
                     startInputStreamPump();
                     Object taken = DONE;
                     try {
                        taken = outputQueues[idx].take();
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
      }
   }
   
   @Override
   public JinqStream<T> setHint(String name, Object value)
   {
      return this;
   }
}
