package org.jinq.jpa

import org.jinq.orm.stream.scala.JinqConversions._
import org.junit.Assert
import org.junit.Test
import _root_.scala.collection.mutable.ListBuffer
import org.jinq.orm.stream.scala.JinqIterator
import org.jinq.orm.stream.scala.NonQueryJinqIterator

class NonQueryJinqIteratorTest {
  @Test
  def testGroup() {
    val stream: JinqIterator[(Int, Int)] = new NonQueryJinqIterator(List((1, 2), (1, 3), (2, 5)).toIterator)
    val results = stream
      .group(pair => pair._1, (key: Int, pairs) => pairs.max(x => x._2))
      .toList;
    Assert.assertEquals(2, results.length);
    Assert.assertTrue(results.contains((1, 3)));
    Assert.assertTrue(results.contains((2, 5)));
  }

  @Test
  def testAggregate() {
    val stream =
      new NonQueryJinqIterator(List(1, 2, 3, 4, 5).toIterator);
    val result =
      stream.aggregate((vals) => vals.sumInteger(x => x),
        (vals) => vals.max(x => x),
        (vals) => vals.sumInteger(x => x + 1));
    Assert.assertEquals(15, result._1.intValue());
    Assert.assertEquals(5, result._2.intValue());
    Assert.assertEquals(20, result._3.intValue());
  }

  @Test
  def testSum() {
    Assert.assertEquals(15l, new NonQueryJinqIterator(List(1, 2, 3, 4, 5).toIterator).sumInteger(n => n));
    Assert.assertTrue(Math.abs(20.0 - new NonQueryJinqIterator(List(1, 2, 3, 4, 5).toIterator).sumDouble(n => n + 1.0)) < 0.01);
  }

  @Test
  def testMax() {
    Assert.assertEquals(6, new NonQueryJinqIterator(List(1, 2, 3, 4, 5).toIterator).max(n => n + 1));
  }

  @Test
  def testAvg() {
    Assert.assertEquals(3, new NonQueryJinqIterator(List(1, 2, 3, 4, 5).toIterator).avg(n => n), 0.001);
  }

  @Test
  def testLeftOuterJoin() {
    val list = new NonQueryJinqIterator(List(0, 1).toIterator)
      .leftOuterJoin(n => List().padTo(n, 1)).toList;
    Assert.assertEquals(List((0, null), (1, 1)), list);
  }

}