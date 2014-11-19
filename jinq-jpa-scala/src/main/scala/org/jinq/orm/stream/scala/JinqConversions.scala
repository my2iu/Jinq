package org.jinq.orm.stream.scala

object JinqConversions {
  implicit def jinq[T](value: java.lang.Iterable[T]) : JinqIterator[T] = {
    new NonQueryJinqIterator(value.toIterator, null)
  }
}