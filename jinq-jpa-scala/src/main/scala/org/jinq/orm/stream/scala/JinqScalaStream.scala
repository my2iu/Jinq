package org.jinq.orm.stream.scala

trait JinqScalaStream[T] {
  def where(fn: (T) => Boolean) : List[T]
  def select[U](fn: (T) => U) : List[U]
  def toList() : List[T] 
}