package org.jinq.jpa.scala

import scala.collection.JavaConverters._

object JavaToScalaConverters {
  def javaListToList[U](list: java.util.List[U]): List[U] = {
    return list.asScala.toList
  }
}