package org.jinq.jpa;

import java.math.BigDecimal
import java.math.BigInteger
import java.util.NoSuchElementException
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import org.jinq.jpa.scala.JavaToScalaConverters
import org.jinq.jpa.transform.OuterJoinTransform
import org.jinq.jpa.transform.ScalaJoinTransform
import org.jinq.jpa.transform.ScalaOuterJoinTransform
import org.jinq.orm.stream.scala.InQueryStreamSource
import org.jinq.orm.stream.scala.JinqScalaStream
import _root_.scala.Function1
import _root_.scala.Function2
import _root_.scala.Tuple2
import _root_.scala.collection.Iterator
import _root_.scala.collection.immutable.List
import _root_.scala.math.Numeric;
import java.util.function.Consumer

class JinqJPAScalaIterator[T](_query: JPAQueryComposer[T], _inQueryStreamSource: InQueryStreamSource) extends JinqScalaStream[T] {
  val GENERIC_TRANSLATION_FAIL_MESSAGE = "Could not translate Scala code to a query";
  var queryComposer: JPAQueryComposer[T] = _query;
  var inQueryStreamSource: InQueryStreamSource = _inQueryStreamSource;

  def this(query: JPAQueryComposer[T]) = this(query, null);

  @Override
  def where(fn: (T) => Boolean): JinqJPAScalaIterator[T] =
    {
      val newComposer = queryComposer.where(fn);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def where(fn: (T, InQueryStreamSource) => Boolean): JinqJPAScalaIterator[T] =
    {
      val newComposer = queryComposer.whereWithSource(fn);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def select[U](fn: (T) => U): JinqJPAScalaIterator[U] =
    {
      val newComposer: JPAQueryComposer[U] = queryComposer.select(fn);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def select[U](fn: (T, InQueryStreamSource) => U): JinqJPAScalaIterator[U] =
    {
      val newComposer: JPAQueryComposer[U] = queryComposer.selectWithSource(fn);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def join[U](fn: (T) => JinqScalaStream[U]): JinqJPAScalaIterator[(T, U)] =
    {
      val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), false), fn);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def join[U](fn: (T, InQueryStreamSource) => JinqScalaStream[U]): JinqJPAScalaIterator[(T, U)] =
    {
      val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), true), fn);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def leftOuterJoin[U](fn: (T) => JinqScalaStream[U]): JinqJPAScalaIterator[Tuple2[T, U]] =
    {
      val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaOuterJoinTransform(queryComposer.getConfig()), fn);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def count(): Long =
    {
      val count : java.lang.Long = queryComposer.count();
      if (count != null) return count;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def sumInteger(fn: (T) => Int): Long =
    {
      val value = queryComposer.sum(fn, classOf[Integer]).asInstanceOf[java.lang.Long];
      if (value != null) return value;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def sumLong(fn: (T) => Long): Long =
    {
      val value = queryComposer.sum(fn, classOf[java.lang.Long]).asInstanceOf[java.lang.Long];
      if (value != null) return value;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def sumDouble(fn: (T) => Double): Double =
    {
      val value = queryComposer.sum(fn, classOf[java.lang.Double]).asInstanceOf[java.lang.Double];
      if (value != null) return value;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def sumBigDecimal(fn: (T) => BigDecimal): BigDecimal =
    {
      val value = queryComposer.sum(fn, classOf[BigDecimal]).asInstanceOf[BigDecimal];
      if (value != null) return value;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def sumBigInteger(fn: (T) => BigInteger): BigInteger =
    {
      val value = queryComposer.sum(fn, classOf[BigInteger]).asInstanceOf[BigInteger];
      if (value != null) return value;
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def max[V](fn: (T) => V): V = {
    val value : V = queryComposer.max(fn);
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def min[V](fn: (T) => V): V = {
    val value : V = queryComposer.min(fn);
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def avg[V: Numeric](fn: (T) => V): Double = {
    val value = queryComposer.avg(fn);
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def sortedBy[V](fn: (T) => V): JinqJPAScalaIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.sortedBy(fn, true);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def sortedDescendingBy[V](fn: (T) => V): JinqJPAScalaIterator[T] =
    {
      val newComposer: JPAQueryComposer[T] = queryComposer.sortedBy(fn, false);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def limit(n: Long): JinqJPAScalaIterator[T] =
    {
      val newComposer: JPAQueryComposer[T] = queryComposer.limit(n);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def skip(n: Long): JinqJPAScalaIterator[T] =
    {
      val newComposer: JPAQueryComposer[T] = queryComposer.skip(n);
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def distinct(): JinqJPAScalaIterator[T] =
    {
      val newComposer: JPAQueryComposer[T] = queryComposer.distinct();
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    }

  @Override
  def setHint(name: String, value: Object): JinqJPAScalaIterator[T] =
    {
      queryComposer.setHint(name, value);
      return this;
    }

  @Override
  def toList(): List[T] =
    {
      return JavaToScalaConverters.javaListToList(
        StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(
            queryComposer.executeAndReturnResultIterator(new Consumer[Throwable]() {
              def accept(err: Throwable) = {}
            }),
            Spliterator.CONCURRENT),
          false).collect(Collectors.toList()));
    }

  @Override
  def toIterator(): Iterator[T] =
    {
      return JavaToScalaConverters.javaIteratorToIterator(
        queryComposer.executeAndReturnResultIterator(new Consumer[Throwable]() {
          def accept(err: Throwable) = {}
        }));
    }

  @Override
  def getOnlyValue(): T =
    {
      val vals = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
          queryComposer.executeAndReturnResultIterator(new Consumer[Throwable]() {
            def accept(err: Throwable) = {}
          }),
          Spliterator.CONCURRENT),
        false).collect(Collectors.toList());
      if (vals.size() == 1) return vals.get(0);
      throw new NoSuchElementException();
    }
}
