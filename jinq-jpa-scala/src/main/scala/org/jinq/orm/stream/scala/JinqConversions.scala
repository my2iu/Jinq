package org.jinq.orm.stream.scala

object JinqConversions {
  implicit def jinq[T](value: java.lang.Iterable[T]) : JinqScalaStream[T] = {
    throw new IllegalArgumentException("Jinq for Scala currently only allows streams to be used when they can be converted into database queries. This usage does not form a valid database query.");
  }
}