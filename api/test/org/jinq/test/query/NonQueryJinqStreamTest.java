package org.jinq.test.query;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.NonQueryJinqStream;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;

public class NonQueryJinqStreamTest
{
   @Before
   public void setUp() throws Exception
   {
   }
   
   @After
   public void tearDown()
   {
   }

   @Test
   public void testGroup()
   {
      JinqStream<Pair<Integer, Integer>> stream = 
            new NonQueryJinqStream<>(Stream.of(
                  new Pair<>(1, 2), new Pair<>(1, 3), new Pair<>(2, 5)));
      List<Pair<Integer, Integer>> results = stream
            .group(pair -> pair.getOne(), (key, pairs) -> pairs.maxInt(x -> x.getTwo()))
            .toList();
      assertEquals(2, results.size());
      assertTrue(results.contains(new Pair<>(1, 3)));
      assertTrue(results.contains(new Pair<>(2, 5)));
   }

   @Test
   public void testAggregate()
   {
      JinqStream<Integer> stream = 
            new NonQueryJinqStream<>(Stream.of(1, 2, 3, 4, 5));
      Tuple3<Long, Integer, Long> result = 
            stream.aggregate((vals) -> vals.sumInteger(x -> x), 
                  (vals) -> vals.maxInt(x -> x),
                  (vals) -> vals.sumInteger(x -> x + 1));
      assertEquals(15, result.getOne().intValue());
      assertEquals(5, result.getTwo().intValue());
      assertEquals(20, result.getThree().intValue());
   }
   
   @Test
   public void testSum()
   {
      assertEquals(15, (long)new NonQueryJinqStream<>( Stream.of(1, 2, 3, 4, 5)).sumInteger(n -> n));
      assertTrue(Math.abs(20.0 - new NonQueryJinqStream<>( Stream.of(1, 2, 3, 4, 5)).sumDouble(n -> n + 1.0)) < 0.01);
   }
}
