package org.jinq.orm.stream.scala

import java.math.BigDecimal
import java.math.BigInteger

trait JinqIterator[T] extends Iterator[T] {
  def where(fn: (T) => Boolean) : JinqIterator[T]
  def where(fn: (T, InQueryStreamSource) => Boolean) : JinqIterator[T]
  
  def select[U](fn: (T) => U) : JinqIterator[U]
  def select[U](fn: (T, InQueryStreamSource) => U) : JinqIterator[U]
  
  def join[U](fn: (T) => JinqIterator[U]) : JinqIterator[(T,U)]
  def join[U](fn: (T, InQueryStreamSource) => JinqIterator[U]) : JinqIterator[(T,U)]
  
  def leftOuterJoin[U](fn: (T) => JinqIterator[U]) : JinqIterator[Tuple2[T,U]]
  def group[U,V](groupingFn: (T) => U, valueFn: (U, JinqIterator[T]) => V) : JinqIterator[(U, V)]    
  def group[U,V,W](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W) : JinqIterator[(U, V, W)]    
  def group[U,V,W,X](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X) : JinqIterator[(U, V, W, X)]    
  def group[U,V,W,X,Y](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X, valueFn4: (U, JinqIterator[T]) => Y) : JinqIterator[(U, V, W, X, Y)]    
  def count() : Long
  def sumInteger(fn: (T) => Int) : Long
  def sumLong(fn: (T) => Long) : Long
  def sumDouble(fn: (T) => Double) : Double
  def sumBigDecimal(fn: (T) => BigDecimal) : BigDecimal
  def sumBigInteger(fn: (T) => BigInteger) : BigInteger
  def max[V/*<:java.lang.Comparable[V]*/](fn: (T) => V) : V  // TODO: Use Ordered instead?
  def min[V/*<:java.lang.Comparable[V]*/](fn: (T) => V) : V // V should be Comparable, but we can't do that since Scala's primitive values aren't Comparable, and implicit conversion can get confused sometimes
  def avg[V:Numeric](fn: (T) => V) : Double
  def sortedBy[V/*<:java.lang.Comparable[V]*/](fn: (T) => V) : JinqIterator[T] // V should be Comparable, but we can't do that since Scala's primitive values aren't Comparable, and implicit conversion can get confused sometimes
  def sortedDescendingBy[V/*<:java.lang.Comparable[V]*/](fn: (T) => V) : JinqIterator[T]
  def limit(n: Long) : JinqIterator[T]  
  def skip(n: Long) : JinqIterator[T]  
  def distinct() : JinqIterator[T]  
  def aggregate[U,V](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V) : (U,V)
  def aggregate[U,V,W](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W) : (U,V,W)
  def aggregate[U,V,W,X](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X) : (U,V,W,X)
  def aggregate[U,V,W,X,Y](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X, fn5: (JinqIterator[T]) => Y) : (U,V,W,X,Y)
  def setHint(name: String, value: Object) : JinqIterator[T]

  def getOnlyValue() : T
}

object JinqIterator {
  def of[T](value : T) : JinqIterator[T] = {
    throw new IllegalArgumentException("Jinq for Scala currently only allows streams to be used when they can be converted into database queries. This usage does not form a valid database query.");
  }
}