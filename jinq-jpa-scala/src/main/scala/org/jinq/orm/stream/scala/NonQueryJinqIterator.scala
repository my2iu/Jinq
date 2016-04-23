package org.jinq.orm.stream.scala

import java.math.BigDecimal
import java.math.BigInteger;
import scala.collection.GenTraversableOnce

class NonQueryJinqIterator[T](_wrapped: Iterator[T], _inQueryStreamSource: InQueryStreamSource) extends JinqIterator[T] {
  val inQueryStreamSource = _inQueryStreamSource
  val wrapped = _wrapped

  def this(it: Iterator[T]) = this(it, null)

  protected def wrap[U](it: Iterator[U]): NonQueryJinqIterator[U] = {
    new NonQueryJinqIterator(it, inQueryStreamSource)
  }

  override def hasNext: Boolean = {
    wrapped.hasNext
  }

  override def next(): T = {
    wrapped.next
  }

  override def where(fn: (T) => Boolean): NonQueryJinqIterator[T] = {
    wrap(filter(fn))
  }

  override def where(fn: (T, InQueryStreamSource) => Boolean): NonQueryJinqIterator[T] = {
    wrap(filter(fn(_, inQueryStreamSource)))
  }

  override def select[U](fn: (T) => U): NonQueryJinqIterator[U] = {
    wrap(map(fn))
  }

  override def select[U](fn: (T, InQueryStreamSource) => U): NonQueryJinqIterator[U] = {
    wrap(map(fn(_, inQueryStreamSource)))
  }

  override def selectAll[U](fn: (T) => GenTraversableOnce[U]): JinqIterator[U] = {
    wrap(flatMap(fn))
  }

  override def selectAll[U](fn: (T, InQueryStreamSource) => GenTraversableOnce[U]): JinqIterator[U] = {
    wrap(flatMap(fn(_, inQueryStreamSource)))
  }

  override def join[U](fn: (T) => JinqIterator[U]): NonQueryJinqIterator[(T, U)] = {
    val joined = flatMap((left: T) => fn(left).map(right => (left, right)));
    wrap(joined.toIterator);
  }

  override def join[U](fn: (T, InQueryStreamSource) => JinqIterator[U]): NonQueryJinqIterator[(T, U)] = {
    val joined = flatMap(left => fn(left, inQueryStreamSource).map(right => (left, right)));
    wrap(joined.toIterator);
  }

  override def leftOuterJoin[U](fn: (T) => JinqIterator[U]): NonQueryJinqIterator[Tuple2[T, U]] = {
    wrap(flatMap((left: T) => {
      val joined = fn(left).toBuffer
      if (joined.isEmpty)
        List((left, null.asInstanceOf[U]))
      else
        joined.map(right => (left, right))
    }))
  }
  
  override def leftOuterJoin[U](join: (T, InQueryStreamSource) => JinqIterator[U], on: (T, U) => Boolean) : JinqIterator[Tuple2[T, U]] = {
    wrap(flatMap((left: T) => {
      val joined = join(left, inQueryStreamSource).filter(right => on(left, right)).toBuffer
      if (joined.isEmpty)
        List((left, null.asInstanceOf[U]))
      else
        joined.map(right => (left, right))
    }))
  }
  
  override def joinFetch[U](fn: (T) => JinqIterator[U]): JinqIterator[T] = {
    this;    
  }

  override def leftOuterJoinFetch[U](fn: (T) => JinqIterator[U]): JinqIterator[T] = {
    this;
  }

  override def count(): java.lang.Long = {
    val count = length
    count.asInstanceOf[Long]
  }

  override def sumInteger(fn: (T) => java.lang.Integer): java.lang.Long = {
    foldLeft(0)((value, element) => value + fn(element)).asInstanceOf[Long]
  }

  override def sumLong(fn: (T) => java.lang.Long): java.lang.Long = {
    foldLeft(0l)((value, element) => value + fn(element))
  }

  override def sumDouble(fn: (T) => java.lang.Double): java.lang.Double = {
    foldLeft(0.0)((value, element) => value + fn(element))
  }

  override def sumBigDecimal(fn: (T) => java.math.BigDecimal): BigDecimal = {
    foldLeft(BigDecimal.valueOf(0))((value, element) => value.add(fn(element)))
  }

  override def sumBigInteger(fn: (T) => BigInteger): BigInteger = {
    foldLeft(BigInteger.valueOf(0))((value, element) => value.add(fn(element)))
  }

  override def max[V <% Comparable[V]](fn: (T) => V): V = {
    val (it1, it2) = duplicate
    val count = it1.length
    if (count == 0)
      null.asInstanceOf[V]
    else
      it2.foldLeft(fn(it2.next))((v1, element) => {
        val v2 = fn(element)
        if (v1.compareTo(v2) > 0) v1 else v2
      })
  }

  override def min[V <% Comparable[V]](fn: (T) => V): V = {
    val (it1, it2) = duplicate
    val count = it1.length
    if (count == 0)
      null.asInstanceOf[V]
    else
      it2.foldLeft(fn(it2.next))((v1, element) => {
        val v2 = fn(element)
        if (v1.compareTo(v2) < 0) v1 else v2
      })
  }

  override def avg[V](fn: (T) => V)(implicit num: Numeric[V]): java.lang.Double = {
    val (it1, it2) = duplicate
    val count = it1.length
    it2.foldLeft(0.0)((value, element) => value + num.toDouble(fn(element))) / count
  }

  override def sortedBy[V <% Comparable[V]](fn: (T) => V): NonQueryJinqIterator[T] = {
    wrap(toBuffer.sortBy(fn).toIterator)
  }

  override def sortedDescendingBy[V <% Comparable[V]](fn: (T) => V): NonQueryJinqIterator[T] = {
    wrap(toBuffer.sortBy(fn).reverseIterator)
  }

  override def limit(n: Long): NonQueryJinqIterator[T] = {
    wrap(take(n.asInstanceOf[Int]))
  }

  override def skip(n: Long): NonQueryJinqIterator[T] = {
    wrap(drop(n.asInstanceOf[Int]))
  }

  override def distinct(): NonQueryJinqIterator[T] = {
    wrap(toBuffer.distinct.toIterator)
  }

  override def aggregate[U, V](fn1: JinqIterator[T] => U, fn2: JinqIterator[T] => V): (U, V) = {
    val (it1, it2) = duplicate
    (fn1(wrap(it1)), fn2(wrap(it2)))
  }

  override def aggregate[U, V, W](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W): (U, V, W) = {
    val (it1, it1copy) = duplicate
    val (it2, it3) = it1copy.duplicate
    (fn1(wrap(it1)), fn2(wrap(it2)), fn3(wrap(it3)))
  }

  override def aggregate[U, V, W, X](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X): (U, V, W, X) = {
    val (it1, it1copy) = duplicate
    val (it2, it2copy) = it1copy.duplicate
    val (it3, it4) = it2copy.duplicate
    (fn1(wrap(it1)), fn2(wrap(it2)), fn3(wrap(it3)), fn4(wrap(it4)))
  }

  override def aggregate[U, V, W, X, Y](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X, fn5: (JinqIterator[T]) => Y): (U, V, W, X, Y) = {
    val (it1, it1copy) = duplicate
    val (it2, it2copy) = it1copy.duplicate
    val (it3, it3copy) = it2copy.duplicate
    val (it4, it5) = it3copy.duplicate
    (fn1(wrap(it1)), fn2(wrap(it2)), fn3(wrap(it3)), fn4(wrap(it4)), fn5(wrap(it5)))
  }

  override def group[U, V](groupingFn: (T) => U, valueFn: (U, JinqIterator[T]) => V): NonQueryJinqIterator[(U, V)] = {
    val groups: Map[U, Seq[T]] = toSeq.groupBy((value) => groupingFn(value: T))
    val aggregated = groups.transform((g: U, els: Seq[T]) => valueFn(g, wrap(els.toIterator)))
    wrap(aggregated.toIterator)
  }

  override def group[U, V, W](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W): NonQueryJinqIterator[(U, V, W)] = {
    val groups: Map[U, Seq[T]] = toSeq.groupBy((value: T) => groupingFn(value))
    val aggregated = groups.transform((g: U, els: Seq[T]) => (valueFn1(g, wrap(els.toIterator)), valueFn2(g, wrap(els.toIterator))))
    wrap(aggregated.toIterator.map((a) => (a._1, a._2._1, a._2._2)).toIterator)
  }

  override def group[U, V, W, X](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X): NonQueryJinqIterator[(U, V, W, X)] = {
    val groups: Map[U, Seq[T]] = toSeq.groupBy((value: T) => groupingFn(value))
    val aggregated = groups.transform((g: U, els: Seq[T]) => (valueFn1(g, wrap(els.toIterator)), valueFn2(g, wrap(els.toIterator)), valueFn3(g, wrap(els.toIterator))))
    wrap(aggregated.toIterator.map((a) => (a._1, a._2._1, a._2._2, a._2._3)).toIterator)
  }

  override def group[U, V, W, X, Y](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X, valueFn4: (U, JinqIterator[T]) => Y): NonQueryJinqIterator[(U, V, W, X, Y)] = {
    val groups: Map[U, Seq[T]] = toSeq.groupBy((value: T) => groupingFn(value))
    val aggregated = groups.transform((g: U, els: Seq[T]) => (valueFn1(g, wrap(els.toIterator)), valueFn2(g, wrap(els.toIterator)), valueFn3(g, wrap(els.toIterator)), valueFn4(g, wrap(els.toIterator))))
    wrap(aggregated.toIterator.map((a) => (a._1, a._2._1, a._2._2, a._2._3, a._2._4)).toIterator)
  }

  override def setHint(name: String, value: Object): NonQueryJinqIterator[T] = {
    return this;
  }

  override def getOnlyValue(): T = {
    val vals = toBuffer;
    if (vals.length == 1) return vals(0);
    throw new NoSuchElementException();
  }

}