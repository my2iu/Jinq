package org.jinq.test.query;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jinq.orm.stream.NonQueryJinqStream;
import org.jinq.test.entities.DBManager;
import org.jinq.test.entities.EntityManager;

import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.queryll2.QueryllAnalyzer;

public class JinqStreamTest
{
   StringWriter savedOutput;
   DBManager db;
   EntityManager em;

   @Before
   public void setUp() throws Exception
   {
      savedOutput = new StringWriter();
      db = new DBManager(new PrintWriter(savedOutput));
      QueryllAnalyzer queryll2 = new QueryllAnalyzer(); 
      queryll2.useRuntimeAnalysis(db);
      em = db.begin();
   }
   
   @After
   public void tearDown()
   {
      db.end(em, true);
      db.close();
   }

   @Test
   public void testWhere()
   {
      assertEquals("SELECT A.Name AS COL1 FROM Customers AS A",
         em.customerStream()
            .select(c -> c.getName())
            .getDebugQueryString());
   }

   @Test
   public void testJoin()
   {
      // Join between customer and sales tables
      EntityManager em = this.em;
      assertEquals("SELECT A.Name AS COL1, B.Date AS COL2 FROM Customers AS A, Sales AS B",
            em.customerStream()
               .join(c -> em.saleStream())
               .select(pair -> new Pair<>(pair.getOne().getName(),
                                       pair.getTwo().getDate()))
               .getDebugQueryString());
   }
   
   @Test
   public void testJoinNavigational()
   {
      // Join to another table via a navigational link
      EntityManager em = this.em;
      fail();
      // TODO: Implement a way to get navigational queries as a stream
//      assertEquals("SELECT A.Name AS COL1, B.Date AS COL2 FROM Customers AS A, Sales AS B",
//            em.customerStream()
//               .join(c -> new NonQueryJinqStream<>(c.getPurchases().stream()))
//               .select(pair -> new Pair<>(pair.getOne().getName(),
//                                       pair.getTwo().getDate()))
//               .getDebugQueryString());
   }

   @Test
   public void testAggregation()
   {
      // Aggregation operations (Sum of all line order quantities)
      int total = em.lineOrderStream()
         .sumInt(lo -> lo.getQuantity());
      assertEquals("SELECT SUM(A.Quantity) AS COL1 FROM LineOrders AS A",
         savedOutput.toString().trim());
   }

   @Test
   public void testAggregationMath()
   {
      // Aggregation operations (Sum over an expression)
      double total = em.itemStream()
         .sumDouble(i -> i.getSalePrice() - i.getPurchasePrice());
      assertEquals("SELECT SUM((A.SalePrice) - (A.PurchasePrice)) AS COL1 FROM Items AS A",
         savedOutput.toString().trim());
   }

   @Test 
   public void testMultipleAggregation()
   {
      // Calculate more than one aggregate
      // (Sum of all line order quantities and number of line orders)
      
      // TODO: This syntax isn't quite right because it doesn't work in Java (you can't
      // use a stream twice like this).
      Pair<Integer, Integer> pairResult = em.lineOrderStream()
         .selectAggregates(loset ->
            new Pair<>(loset.sumInt(lo -> lo.getQuantity()),
                       loset.sumInt(lo -> 1)));
      assertEquals("SELECT SUM(A.Quantity) AS COL1, SUM(1) AS COL2 FROM LineOrders AS A",
            savedOutput.toString().trim());
   }
   
   @Test
   public void testGroup()
   {
      // Group together results (Customers and the number of sales to them)
      assertEquals("SELECT B.CustomerId AS COL3, SUM(1) AS COL4 FROM Sales AS A, Customers AS B WHERE (A.CustomerId) = (B.CustomerId) GROUP BY B.CustomerId",
            em.saleStream()
               .group(
                  s -> s.getPurchaser().getCustomerId(),
                  (key, sales) -> sales.sumInt(sale -> 1))
               .getDebugQueryString());
   }
   
   @Test
   public void testSubQuery()
   {
      // Basic subquery (Most recent sale)
      // TODO: Port over support for stream sources to subqueries 
      fail();
      assertEquals("SELECT A.Name AS COL2 FROM Customers AS A ORDER BY A.Name ASC",
            em.saleStream()
                  .where((s) -> em.saleStream().maxInt(ss -> ss.getSaleId())
                        == s.getSaleId())
                  .getDebugQueryString());
   }
   
   @Test
   public void testSorting()
   {
      // Sorting of results (Sorted list of customer names)
      assertEquals("SELECT A.Name AS COL2 FROM Customers AS A ORDER BY A.Name ASC",
         em.customerStream()
            .select(c -> c.getName())
            .sortedByStringAscending(name -> name)
            .getDebugQueryString());
   }
}
