package org.jinq.orm.stream.scala

trait JinqScalaStream[T] {
  def where(fn: (T) => Boolean) : JinqScalaStream[T]
  def select[U](fn: (T) => U) : JinqScalaStream[U]
  def toList() : List[T] 
}