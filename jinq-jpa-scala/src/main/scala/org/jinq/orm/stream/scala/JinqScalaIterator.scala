package org.jinq.orm.stream.scala

import java.math.BigDecimal
import java.math.BigInteger

trait JinqScalaIterator[T] extends Iterator[T] {
  def where(fn: (T) => Boolean) : JinqScalaIterator[T]
  def where(fn: (T, InQueryStreamSource) => Boolean) : JinqScalaIterator[T]
  
  def select[U](fn: (T) => U) : JinqScalaIterator[U]
  def select[U](fn: (T, InQueryStreamSource) => U) : JinqScalaIterator[U]
  
  def join[U](fn: (T) => JinqScalaIterator[U]) : JinqScalaIterator[(T,U)]
  def join[U](fn: (T, InQueryStreamSource) => JinqScalaIterator[U]) : JinqScalaIterator[(T,U)]
  
  def leftOuterJoin[U](fn: (T) => JinqScalaIterator[U]) : JinqScalaIterator[Tuple2[T,U]]
//  def group[U,V](groupingFn: (T) => U, valueFn: (U, JinqScalaStream[T]) => V) : JinqScalaStream[Tuple2[U, V]]    
  def count() : Long
  def sumInteger(fn: (T) => Int) : Long
  def sumLong(fn: (T) => Long) : Long
  def sumDouble(fn: (T) => Double) : Double
  def sumBigDecimal(fn: (T) => BigDecimal) : BigDecimal
  def sumBigInteger(fn: (T) => BigInteger) : BigInteger
  def max[V/*<:java.lang.Comparable[V]*/](fn: (T) => V) : V  // TODO: Use Ordered instead?
  def min[V/*<:java.lang.Comparable[V]*/](fn: (T) => V) : V // V should be Comparable, but we can't do that since Scala's primitive values aren't Comparable, and implicit conversion can get confused sometimes
  def avg[V:Numeric](fn: (T) => V) : Double
  def sortedBy[V/*<:java.lang.Comparable[V]*/](fn: (T) => V) : JinqScalaIterator[T] // V should be Comparable, but we can't do that since Scala's primitive values aren't Comparable, and implicit conversion can get confused sometimes
  def sortedDescendingBy[V/*<:java.lang.Comparable[V]*/](fn: (T) => V) : JinqScalaIterator[T]
  def limit(n: Long) : JinqScalaIterator[T]  
  def skip(n: Long) : JinqScalaIterator[T]  
  def distinct() : JinqScalaIterator[T]  
//  def aggregate[U,V](fn1: (JinqScalaStream[T]) => U, fn2: (JinqScalaStream[T]) => V) : Tuple2[U,V]
  def setHint(name: String, value: Object) : JinqScalaIterator[T]

  def getOnlyValue() : T
}

object JinqScalaIterator {
  def of[T](value : T) : JinqScalaIterator[T] = {
    throw new IllegalArgumentException("Jinq for Scala currently only allows streams to be used when they can be converted into database queries. This usage does not form a valid database query.");
  }
}