package org.jinq.jpa;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jinq.jpa.scala.JavaToScalaConverters;
import org.jinq.jpa.transform.OuterJoinTransform;
import org.jinq.jpa.transform.ScalaJoinTransform;
import org.jinq.orm.stream.scala.InQueryStreamSource;
import org.jinq.orm.stream.scala.JinqScalaStream;

import scala.Function1;
import scala.Function2;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.List;
import scala.math.Numeric;


public class JinqJPAScalaStream<T> implements JinqScalaStream<T>
{
   private static final String GENERIC_TRANSLATION_FAIL_MESSAGE = "Could not translate Scala code to a query";
   JPAQueryComposer<T> queryComposer;
   InQueryStreamSource inQueryStreamSource;
   
   public JinqJPAScalaStream(JPAQueryComposer<T> query)
   {
      this(query, null);
   }
   
   public JinqJPAScalaStream(JPAQueryComposer<T> query, InQueryStreamSource inQueryStreamSource)
   {
      this.inQueryStreamSource = inQueryStreamSource;
      this.queryComposer = query;
   }

   @Override
   public JinqJPAScalaStream<T> where(Function1<T, Object> fn)
   {
      JPAQueryComposer<T> newComposer = queryComposer.where(fn);
      if (newComposer != null) return new JinqJPAScalaStream<T>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public JinqScalaStream<T> where(Function2<T, InQueryStreamSource, Object> fn)
   {
      JPAQueryComposer<T> newComposer = queryComposer.whereWithSource(fn);
      if (newComposer != null) return new JinqJPAScalaStream<T>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqJPAScalaStream<U> select(Function1<T, U> fn)
   {
      JPAQueryComposer<U> newComposer = queryComposer.select(fn);
      if (newComposer != null) return new JinqJPAScalaStream<U>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqScalaStream<U> select(Function2<T, InQueryStreamSource, U> fn)
   {
      JPAQueryComposer<U> newComposer = queryComposer.selectWithSource(fn);
      if (newComposer != null) return new JinqJPAScalaStream<U>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqScalaStream<Tuple2<T, U>> join(
         Function1<T, JinqScalaStream<U>> fn)
   {
      JPAQueryComposer<Tuple2<T, U>> newComposer = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), false), fn);
      if (newComposer != null) return new JinqJPAScalaStream<Tuple2<T, U>>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqScalaStream<Tuple2<T, U>> join(
         Function2<T, org.jinq.orm.stream.scala.InQueryStreamSource, JinqScalaStream<U>> fn)
   {
      JPAQueryComposer<Tuple2<T, U>> newComposer = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), true), fn);
      if (newComposer != null) return new JinqJPAScalaStream<Tuple2<T, U>>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqScalaStream<Tuple2<T, U>> leftOuterJoin(
         Function1<T, JinqScalaStream<U>> fn)
   {
      JPAQueryComposer<Tuple2<T, U>> newComposer = queryComposer.applyTransformWithLambda(new OuterJoinTransform(queryComposer.getConfig()), fn);
      if (newComposer != null) return new JinqJPAScalaStream<Tuple2<T, U>>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public long count()
   {
      Long count = queryComposer.count();
      if (count != null) return count;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public long sumInteger(Function1<T, Object> fn)
   {
      Long val = (Long)queryComposer.sum(fn, Integer.class);
      if (val != null) return val;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public long sumLong(Function1<T, Object> fn)
   {
      Long val = (Long)queryComposer.sum(fn, Long.class);
      if (val != null) return val;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }
   
   @Override
   public double sumDouble(Function1<T, Object> fn)
   {
      Double val = (Double)queryComposer.sum(fn, Double.class);
      if (val != null) return val;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE); 
   }

   @Override
   public BigDecimal sumBigDecimal(Function1<T, BigDecimal> fn)
   {
      BigDecimal val = (BigDecimal)queryComposer.sum(fn, BigDecimal.class);
      if (val != null) return val;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public BigInteger sumBigInteger(Function1<T, BigInteger> fn)
   {
      BigInteger val = (BigInteger)queryComposer.sum(fn, BigInteger.class);
      if (val != null) return val;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <V extends Comparable<V>> V max(Function1<T, V> fn)
   {
      V val = (V)queryComposer.max(fn);
      if (val != null) return val;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <V extends Comparable<V>> V min(Function1<T, V> fn)
   {
      V val = (V)queryComposer.min(fn);
      if (val != null) return val;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <V> double avg(Function1<T, V> fn, Numeric<V> evidence$1)
   {
      Double val = queryComposer.avg(fn);
      if (val != null) return val;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <V extends Comparable<V>> JinqScalaStream<T> sortedBy(
         Function1<T, V> fn)
   {
      JPAQueryComposer<T> newComposer = queryComposer.sortedBy(fn, true);
      if (newComposer != null) return new JinqJPAScalaStream<>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <V extends Comparable<V>> JinqScalaStream<T> sortedDescendingBy(
         Function1<T, V> fn)
   {
      JPAQueryComposer<T> newComposer = queryComposer.sortedBy(fn, false);
      if (newComposer != null) return new JinqJPAScalaStream<>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public JinqScalaStream<T> limit(long n)
   {
      JPAQueryComposer<T> newComposer = queryComposer.limit(n);
      if (newComposer != null) return new JinqJPAScalaStream<>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public JinqScalaStream<T> skip(long n)
   {
      JPAQueryComposer<T> newComposer = queryComposer.skip(n);
      if (newComposer != null) return new JinqJPAScalaStream<>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public JinqScalaStream<T> distinct()
   {
      JPAQueryComposer<T> newComposer = queryComposer.distinct();
      if (newComposer != null) return new JinqJPAScalaStream<>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public JinqScalaStream<T> setHint(String name, Object value)
   {
      queryComposer.setHint(name, value);
      return this;
   }

   @Override
   public List<T> toList()
   {
      return JavaToScalaConverters.javaListToList(
            StreamSupport.stream(
                  Spliterators.spliteratorUnknownSize(
                        queryComposer.executeAndReturnResultIterator( err -> {} ), 
                        Spliterator.CONCURRENT), 
                  false).collect(Collectors.toList()));
   }

   @Override
   public Iterator<T> toIterator()
   {
      return JavaToScalaConverters.javaIteratorToIterator(
            queryComposer.executeAndReturnResultIterator( err -> {} ));
   }
   
   @Override
   public T getOnlyValue() 
   {
      java.util.List<T> vals = StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                  queryComposer.executeAndReturnResultIterator( err -> {} ), 
                  Spliterator.CONCURRENT), 
            false).collect(Collectors.toList());
      if (vals.size() == 1) return vals.get(0);
      throw new NoSuchElementException();
   };
}
