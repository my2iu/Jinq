package org.jinq.jpa;

import java.math.BigDecimal
import java.math.BigInteger
import java.util.NoSuchElementException
import java.util.function.Consumer
import _root_.scala.math.Numeric
import org.jinq.jpa.transform.ScalaGroupingTransform
import org.jinq.jpa.transform.ScalaJoinTransform
import org.jinq.jpa.transform.ScalaMultiAggregateTransform
import org.jinq.jpa.transform.ScalaOuterJoinTransform
import org.jinq.orm.stream.scala.InQueryStreamSource
import org.jinq.orm.stream.scala.JinqIterator
import org.jinq.orm.stream.scala.NonQueryJinqIterator
import _root_.scala.collection.GenTraversableOnce
import org.jinq.jpa.transform.JoinFetchTransform
import org.jinq.jpa.transform.ScalaOuterJoinOnTransform

class JinqJPAScalaIterator[T](_query: JPAQueryComposer[T], _inQueryStreamSource: InQueryStreamSource) extends JinqIterator[T] {
  val GENERIC_TRANSLATION_FAIL_MESSAGE = "Could not translate Scala code to a query";
  var queryComposer: JPAQueryComposer[T] = _query;
  var inQueryStreamSource: InQueryStreamSource = _inQueryStreamSource;
  var lazyIterator: java.util.Iterator[T] = null;

  def this(query: JPAQueryComposer[T]) = this(query, null);

  private def realizeIterator() {
    if (lazyIterator == null) {
      lazyIterator = queryComposer.executeAndReturnResultIterator(new Consumer[Throwable]() {
        def accept(err: Throwable) = {}
      });
    }
  }

  def hasNext: Boolean = {
    realizeIterator();
    return lazyIterator.hasNext();
  }
  def next(): T = {
    realizeIterator();
    return lazyIterator.next();
  }

  private def asNonQuery() = {
    new NonQueryJinqIterator(this, inQueryStreamSource)
  }

  override def where(fn: (T) => Boolean): JinqIterator[T] = {
    val newComposer = queryComposer.where(fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().where(fn)
  }

  override def where(fn: (T, InQueryStreamSource) => Boolean): JinqIterator[T] = {
    val newComposer = queryComposer.whereWithSource(fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().where(fn)
  }

  override def select[U](fn: (T) => U): JinqIterator[U] = {
    val newComposer: JPAQueryComposer[U] = queryComposer.select(fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().select(fn)
  }

  override def select[U](fn: (T, InQueryStreamSource) => U): JinqIterator[U] = {
    val newComposer: JPAQueryComposer[U] = queryComposer.selectWithSource(fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().select(fn)
  }

  override def selectAll[U](fn: (T) => GenTraversableOnce[U]): JinqIterator[U] = {
    val newComposer: JPAQueryComposer[U] = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), false, false), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().selectAll(fn)
  }

  override def selectAll[U](fn: (T, InQueryStreamSource) => GenTraversableOnce[U]): JinqIterator[U] = {
    val newComposer: JPAQueryComposer[U] = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), true, false), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().selectAll(fn)
  }

  override def join[U](fn: (T) => JinqIterator[U]): JinqIterator[(T, U)] = {
    val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), false, true), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().join(fn)
  }

  override def join[U](fn: (T, InQueryStreamSource) => JinqIterator[U]): JinqIterator[(T, U)] = {
    val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), true, true), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().join(fn)
  }

  override def leftOuterJoin[U](fn: (T) => JinqIterator[U]): JinqIterator[Tuple2[T, U]] = {
    val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaOuterJoinTransform(queryComposer.getConfig()), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().leftOuterJoin(fn)
  }
  
  override def leftOuterJoin[U](join: (T, InQueryStreamSource) => JinqIterator[U], on: (T, U) => Boolean) : JinqIterator[Tuple2[T, U]] = {
    val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithTwoLambdas(new ScalaOuterJoinOnTransform(queryComposer.getConfig()), join, on);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().leftOuterJoin(join, on)
  }
  
  override def joinFetch[U](fn: (T) => JinqIterator[U]): JinqIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.applyTransformWithLambda(new JoinFetchTransform(queryComposer.getConfig()).setIsExpectingStream(true).setIsOuterJoinFetch(false), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    this
  }

  override def leftOuterJoinFetch[U](fn: (T) => JinqIterator[U]): JinqIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.applyTransformWithLambda(new JoinFetchTransform(queryComposer.getConfig()).setIsExpectingStream(true).setIsOuterJoinFetch(true), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    this
  }

  override def count(): java.lang.Long = {
    val count: java.lang.Long = queryComposer.count();
    if (count != null) return count;
    asNonQuery().count()

  }

  override def sumInteger(fn: (T) => java.lang.Integer): java.lang.Long = {
    val value = queryComposer.sum(fn, classOf[Integer]).asInstanceOf[java.lang.Long];
    if (value != null) return value;
    asNonQuery().sumInteger(fn)
  }

  override def sumLong(fn: (T) => java.lang.Long): java.lang.Long = {
    val value = queryComposer.sum(fn, classOf[java.lang.Long]).asInstanceOf[java.lang.Long];
    if (value != null) return value;
    asNonQuery().sumLong(fn)
  }

  override def sumDouble(fn: (T) => java.lang.Double): java.lang.Double = {
    val value = queryComposer.sum(fn, classOf[java.lang.Double]).asInstanceOf[java.lang.Double];
    if (value != null) return value;
    asNonQuery().sumDouble(fn)
  }

  override def sumBigDecimal(fn: (T) => BigDecimal): BigDecimal = {
    val value = queryComposer.sum(fn, classOf[BigDecimal]).asInstanceOf[BigDecimal];
    if (value != null) return value;
    asNonQuery().sumBigDecimal(fn)
  }

  override def sumBigInteger(fn: (T) => BigInteger): BigInteger = {
    val value = queryComposer.sum(fn, classOf[BigInteger]).asInstanceOf[BigInteger];
    if (value != null) return value;
    asNonQuery().sumBigInteger(fn)
  }

  override def max[V <% java.lang.Comparable[V]](fn: (T) => V): V = {
    val value: V = queryComposer.max(fn);
    if (value != null) return value;
    asNonQuery().max(fn)
  }

  override def min[V <% java.lang.Comparable[V]](fn: (T) => V): V = {
    val value: V = queryComposer.min(fn);
    if (value != null) return value;
    asNonQuery().min(fn)
  }

  override def avg[V](fn: (T) => V)(implicit num: Numeric[V]): java.lang.Double = {
    val value = queryComposer.avg(fn);
    if (value != null) return value;
    asNonQuery().avg(fn)
  }

  override def sortedBy[V <% java.lang.Comparable[V]](fn: (T) => V): JinqIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.sortedBy(fn, true);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().sortedBy(fn)
  }

  override def sortedDescendingBy[V <% java.lang.Comparable[V]](fn: (T) => V): JinqIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.sortedBy(fn, false);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().sortedDescendingBy(fn)
  }

  override def limit(n: Long): JinqIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.limit(n);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().limit(n)
  }

  override def skip(n: Long): JinqIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.skip(n);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().skip(n)
  }

  override def distinct(): JinqIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.distinct();
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().distinct()
  }

  private def multiaggregate[K](groupingLambdas: Array[Object]): JPAQueryComposer[K] = {
    queryComposer.applyTransformWithLambdas(new ScalaMultiAggregateTransform(queryComposer.getConfig()), groupingLambdas)
  }

  def aggregate[U, V](fn1: JinqIterator[T] => U, fn2: JinqIterator[T] => V): (U, V) = {
    val groupingLambdas: Array[Object] = Array(fn1, fn2);
    val result = multiaggregate(groupingLambdas)
    if (result != null) return result.executeAndGetSingleResult();
    asNonQuery().aggregate(fn1, fn2)
  }

  def aggregate[U, V, W](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W): (U, V, W) = {
    val groupingLambdas: Array[Object] = Array(fn1, fn2, fn3);
    val result = multiaggregate(groupingLambdas)
    if (result != null) return result.executeAndGetSingleResult();
    asNonQuery().aggregate(fn1, fn2, fn3)
  }

  def aggregate[U, V, W, X](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X): (U, V, W, X) = {
    val groupingLambdas: Array[Object] = Array(fn1, fn2, fn3, fn4);
    val result = multiaggregate(groupingLambdas)
    if (result != null) return result.executeAndGetSingleResult();
    asNonQuery().aggregate(fn1, fn2, fn3, fn4)
  }

  def aggregate[U, V, W, X, Y](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X, fn5: (JinqIterator[T]) => Y): (U, V, W, X, Y) = {
    val groupingLambdas: Array[Object] = Array(fn1, fn2, fn3, fn4, fn5);
    val result = multiaggregate(groupingLambdas)
    if (result != null) return result.executeAndGetSingleResult();
    asNonQuery().aggregate(fn1, fn2, fn3, fn4, fn5)
  }

  def groupToTuple[U, K](groupingFn: (T) => U, valueFns: Array[Object]): JPAQueryComposer[K] = {
    val allLambdas: Array[Object] = new Array(valueFns.length + 1)
    allLambdas(0) = groupingFn
    valueFns.copyToArray(allLambdas, 1)
    queryComposer.applyTransformWithLambdas(new ScalaGroupingTransform(queryComposer.getConfig()), allLambdas); ;
  }

  def group[U, V](groupingFn: (T) => U, valueFn: (U, JinqIterator[T]) => V): JinqIterator[(U, V)] = {
    val valueLambdas: Array[Object] = Array(valueFn);
    val newComposer: JPAQueryComposer[(U, V)] = groupToTuple(groupingFn, valueLambdas)
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().group(groupingFn, valueFn)
  }

  def group[U, V, W](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W): JinqIterator[(U, V, W)] = {
    val valueLambdas: Array[Object] = Array(valueFn1, valueFn2);
    val newComposer: JPAQueryComposer[(U, V, W)] = groupToTuple(groupingFn, valueLambdas)
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().group(groupingFn, valueFn1, valueFn2)
  }

  def group[U, V, W, X](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X): JinqIterator[(U, V, W, X)] = {
    val valueLambdas: Array[Object] = Array(valueFn1, valueFn2, valueFn3);
    val newComposer: JPAQueryComposer[(U, V, W, X)] = groupToTuple(groupingFn, valueLambdas)
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().group(groupingFn, valueFn1, valueFn2, valueFn3)
  }

  def group[U, V, W, X, Y](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X, valueFn4: (U, JinqIterator[T]) => Y): JinqIterator[(U, V, W, X, Y)] = {
    val valueLambdas: Array[Object] = Array(valueFn1, valueFn2, valueFn3, valueFn4);
    val newComposer: JPAQueryComposer[(U, V, W, X, Y)] = groupToTuple(groupingFn, valueLambdas)
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    asNonQuery().group(groupingFn, valueFn1, valueFn2, valueFn3, valueFn4)
  }

  override def setHint(name: String, value: Object): JinqJPAScalaIterator[T] = {
    queryComposer.setHint(name, value);
    return this;
  }

  override def getOnlyValue(): T = {
    val vals = toBuffer;
    if (vals.length == 1) return vals(0);
    throw new NoSuchElementException();
  }

  // Allow the use of some standard Scala Iterator functional operations.
  override def filter(p: T => Boolean) = {
    where(p)
  }
  override def map[B](p: T => B) = {
    select(p)
  }
  override def flatMap[B](p: T => GenTraversableOnce[B]) = {
    selectAll(p)
  }
  override def length = {
    count().asInstanceOf[Int]
  }
}
