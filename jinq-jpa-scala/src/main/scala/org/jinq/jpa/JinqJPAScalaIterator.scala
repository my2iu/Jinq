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

  @Override
  def where(fn: (T) => Boolean): JinqJPAScalaIterator[T] = {
    val newComposer = queryComposer.where(fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def where(fn: (T, InQueryStreamSource) => Boolean): JinqJPAScalaIterator[T] = {
    val newComposer = queryComposer.whereWithSource(fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def select[U](fn: (T) => U): JinqJPAScalaIterator[U] = {
    val newComposer: JPAQueryComposer[U] = queryComposer.select(fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def select[U](fn: (T, InQueryStreamSource) => U): JinqJPAScalaIterator[U] = {
    val newComposer: JPAQueryComposer[U] = queryComposer.selectWithSource(fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def join[U](fn: (T) => JinqIterator[U]): JinqJPAScalaIterator[(T, U)] = {
    val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), false), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def join[U](fn: (T, InQueryStreamSource) => JinqIterator[U]): JinqJPAScalaIterator[(T, U)] = {
    val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), true), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def leftOuterJoin[U](fn: (T) => JinqIterator[U]): JinqJPAScalaIterator[Tuple2[T, U]] = {
    val newComposer: JPAQueryComposer[(T, U)] = queryComposer.applyTransformWithLambda(new ScalaOuterJoinTransform(queryComposer.getConfig()), fn);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def count(): java.lang.Long = {
    val count: java.lang.Long = queryComposer.count();
    if (count != null) return count;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def sumInteger(fn: (T) => java.lang.Integer): java.lang.Long = {
    val value = queryComposer.sum(fn, classOf[Integer]).asInstanceOf[java.lang.Long];
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def sumLong(fn: (T) => java.lang.Long): java.lang.Long = {
    val value = queryComposer.sum(fn, classOf[java.lang.Long]).asInstanceOf[java.lang.Long];
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def sumDouble(fn: (T) => java.lang.Double): java.lang.Double = {
    val value = queryComposer.sum(fn, classOf[java.lang.Double]).asInstanceOf[java.lang.Double];
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def sumBigDecimal(fn: (T) => BigDecimal): BigDecimal = {
    val value = queryComposer.sum(fn, classOf[BigDecimal]).asInstanceOf[BigDecimal];
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def sumBigInteger(fn: (T) => BigInteger): BigInteger = {
    val value = queryComposer.sum(fn, classOf[BigInteger]).asInstanceOf[BigInteger];
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def max[V](fn: (T) => V): V = {
    val value: V = queryComposer.max(fn);
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def min[V](fn: (T) => V): V = {
    val value: V = queryComposer.min(fn);
    if (value != null) return value;
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def avg[V: Numeric](fn: (T) => V): java.lang.Double = {
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
  def sortedDescendingBy[V](fn: (T) => V): JinqJPAScalaIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.sortedBy(fn, false);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def limit(n: Long): JinqJPAScalaIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.limit(n);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def skip(n: Long): JinqJPAScalaIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.skip(n);
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  @Override
  def distinct(): JinqJPAScalaIterator[T] = {
    val newComposer: JPAQueryComposer[T] = queryComposer.distinct();
    if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }

  private def multiaggregate[K](groupingLambdas: Array[Object]) : K = {
    val result : JPAQueryComposer[K] = queryComposer.applyTransformWithLambdas(new ScalaMultiAggregateTransform(queryComposer.getConfig()), groupingLambdas)
    if (result != null) return result.executeAndGetSingleResult(); 
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
  }
  
  def aggregate[U, V](fn1: JinqIterator[T] => U, fn2: JinqIterator[T] => V): (U, V) = {
    val groupingLambdas : Array[Object] = Array(fn1, fn2);
    return multiaggregate(groupingLambdas)
  }
  
  def aggregate[U,V,W](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W) : (U,V,W) = {
    val groupingLambdas : Array[Object] = Array(fn1, fn2, fn3);
    return multiaggregate(groupingLambdas)
  }
  
  def aggregate[U,V,W,X](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X) : (U,V,W,X) = {
    val groupingLambdas : Array[Object] = Array(fn1, fn2, fn3, fn4);
    return multiaggregate(groupingLambdas)
  }
  
  def aggregate[U,V,W,X,Y](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X, fn5: (JinqIterator[T]) => Y) : (U,V,W,X,Y) = {
    val groupingLambdas : Array[Object] = Array(fn1, fn2, fn3, fn4, fn5);
    return multiaggregate(groupingLambdas)
  }

  def groupToTuple[U,K](groupingFn: (T) => U, valueFns: Array[Object]) : JinqIterator[K] = {
    val allLambdas:Array[Object] = new Array(valueFns.length + 1)
    allLambdas(0) = groupingFn
    valueFns.copyToArray(allLambdas, 1)
      val newComposer:JPAQueryComposer[K] = queryComposer.applyTransformWithLambdas(new ScalaGroupingTransform(queryComposer.getConfig()), allLambdas);;
      if (newComposer != null) return new JinqJPAScalaIterator(newComposer, inQueryStreamSource);
    throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
    
  }
  
  def group[U,V](groupingFn: (T) => U, valueFn: (U, JinqIterator[T]) => V) : JinqIterator[(U, V)] = {
    val valueLambdas : Array[Object] = Array(valueFn);
    return groupToTuple(groupingFn, valueLambdas)
  }
  
  def group[U,V,W](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W) : JinqIterator[(U, V, W)] = {
    val valueLambdas : Array[Object] = Array(valueFn1, valueFn2);
    return groupToTuple(groupingFn, valueLambdas)
  }
  
  def group[U,V,W,X](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X) : JinqIterator[(U, V, W, X)] = {
    val valueLambdas : Array[Object] = Array(valueFn1, valueFn2, valueFn3);
    return groupToTuple(groupingFn, valueLambdas)
  }
  
  def group[U,V,W,X,Y](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X, valueFn4: (U, JinqIterator[T]) => Y) : JinqIterator[(U, V, W, X, Y)] = {
    val valueLambdas : Array[Object] = Array(valueFn1, valueFn2, valueFn3, valueFn4);
    return groupToTuple(groupingFn, valueLambdas)
  }     
  
  @Override
  def setHint(name: String, value: Object): JinqJPAScalaIterator[T] = {
    queryComposer.setHint(name, value);
    return this;
  }

  //  @Override
  //  def toList(): List[T] =
  //    {
  //      return JavaToScalaConverters.javaListToList(
  //        StreamSupport.stream(
  //          Spliterators.spliteratorUnknownSize(
  //            queryComposer.executeAndReturnResultIterator(new Consumer[Throwable]() {
  //              def accept(err: Throwable) = {}
  //            }),
  //            Spliterator.CONCURRENT),
  //          false).collect(Collectors.toList()));
  //    }
  //
  //  @Override
  //  def toIterator(): Iterator[T] =
  //    {
  //      return JavaToScalaConverters.javaIteratorToIterator(
  //        queryComposer.executeAndReturnResultIterator(new Consumer[Throwable]() {
  //          def accept(err: Throwable) = {}
  //        }));
  //    }

  @Override
  def getOnlyValue(): T = {
    val vals = toBuffer;
    if (vals.length == 1) return vals(0);
    throw new NoSuchElementException();
  }
}
