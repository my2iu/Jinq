package org.jinq.test.query;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jinq.orm.stream.NonQueryJinqStream;
import org.jinq.test.entities.DBManager;
import org.jinq.test.entities.EntityManager;

import ch.epfl.labos.iu.orm.DBSet;
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
   public void testSelect()
   {
      // Perform a simple projection operation to get the name column 
      // from a customer table (Names of customers)
      assertEquals("SELECT A.Name AS COL1 FROM Customers AS A",
            em.customerStream()
               .select(c -> c.getName())
               .getDebugQueryString());
   }
   
   @Test
   public void testSelectCase()
   {
      // Conditionals inside of a projection, resulting in a CASE..WHEN (Customers from the UK or not)
      assertEquals("SELECT CASE WHEN ((A.Country) <> ('UK')) THEN 'Not from UK' ELSE 'UK' END AS COL1 FROM Customers AS A",
            em.customerStream()
               .select( c -> c.getCountry().equals("UK")
                     ? "UK" : "Not from UK")
               .getDebugQueryString());
   }
   
   @Test
   public void testSelectIf()
   {
      // If statements and variables inside a projection
      // (Customers from the US are relabelled as coming from USA)
      assertEquals("SELECT CASE WHEN ((A.Country) <> ('US')) THEN A.Country ELSE 'USA' END AS COL1 FROM Customers AS A",
            em.customerStream()
               .select(c -> {
                  String country = c.getCountry();
                  if (country.equals("US"))
                     return "USA";
                  else
                     return country;
                  })
               .getDebugQueryString());
   }

   @Test
   public void testSelectMath()
   {
      // Some math inside a select (Profit on each item)
      assertEquals("SELECT A.ItemId AS COL1, (A.SalePrice) - (A.PurchasePrice) AS COL2 FROM Items AS A",
            em.itemStream()
            .select(i -> new Pair<>(i.getItemId(),
                                    i.getSalePrice() - i.getPurchasePrice()))
            .getDebugQueryString());
   }

   @Test
   public void testWhere()
   {
      // Apply a filter to get the customers from the UK (Customers from the UK)
      assertEquals("SELECT A.CustomerId AS COL1, A.Name AS COL2, A.Country AS COL3, A.Debt AS COL4, A.Salary AS COL5 FROM Customers AS A WHERE ((A.Country) = ('UK'))",
            em.customerStream()
               .where(c -> c.getCountry().equals("UK"))
               .getDebugQueryString());
   }
   
   @Test
   public void testWhereIf()
   {
      // Simple query that is not possible with a simple ORM. (If statements in a where())
      assertEquals("SELECT A.CustomerId AS COL1, A.Name AS COL2, A.Country AS COL3, A.Debt AS COL4, A.Salary AS COL5 FROM Customers AS A WHERE (((A.Salary) <= (50)) AND ((A.Salary) > ((2) * (A.Debt)))) OR (((A.Salary) > (50)) AND ((A.Salary) > (A.Debt)))",
         em.customerStream()
            .where( c -> {
               if (c.getSalary() > 50)
                  return c.getSalary() > c.getDebt();
               else
                  return c.getSalary() > 2 * c.getDebt();
            })
            .getDebugQueryString());
   }
   
   @Test
   public void testWhereParameter()
   {
      // Filter with a parameter (Customers from a country to be specified)
      String stringParam = "UK";
      assertEquals("SELECT A.CustomerId AS COL1, A.Name AS COL2, A.Country AS COL3, A.Debt AS COL4, A.Salary AS COL5 FROM Customers AS A WHERE ((A.Country) = (?))",
            em.customerStream()
               .where(c -> c.getCountry().equals(stringParam))
               .getDebugQueryString());
   }
   
   @Test
   public void testWhereSelectWhere()
   {
      // Apply a filter, project to get two columns, and filter based
      // on those two columns (Customers named John from the UK)
      assertEquals("SELECT A.Name AS COL1, A.Country AS COL2 FROM Customers AS A WHERE (((A.Country) = ('UK'))) AND (((A.Name) = ('John')))",
            em.customerStream()
               .where(c -> c.getCountry().equals("UK"))
               .select(c -> new Pair<String, String>(c.getName(), 
                                                     c.getCountry()))
               .where(pair -> pair.getOne().equals("John"))
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
//      assertEquals("SELECT A.Name AS COL1, B.Date AS COL2 FROM Customers AS A, Sales AS B",
//            em.customerStream()
//               .join(c -> new NonQueryJinqStream<>(c.getPurchases().jinqStream()))
//               .select(pair -> new Pair<>(pair.getOne().getName(),
//                                       pair.getTwo().getDate()))
//               .getDebugQueryString());
   }
   
   @Test
   public void testWhereJoinN1()
   {
      // Join using a N:1 navigational link (Sales made to Bob)
      assertEquals("SELECT A.SaleId AS COL1, A.Date AS COL2, A.CustomerId AS COL3 FROM Sales AS A, Customers AS B WHERE ((A.CustomerId) = (B.CustomerId)) AND (((B.Name) = ('Bob')))",
            em.saleStream()
               .where(s -> s.getPurchaser().getName().equals("Bob"))
               .getDebugQueryString());
   }

   @Test
   public void testWhereJoinN1Select()
   {
      // Simple N:1 navigational link used with filtering and projection
      // (Sales made to Bob (some fields))
      assertEquals("SELECT B.Name AS COL1, A.SaleId AS COL2 FROM Sales AS A, Customers AS B WHERE ((A.CustomerId) = (B.CustomerId)) AND (((B.Name) = ('Bob')))",
            em.saleStream()
               .where(s -> s.getPurchaser().getName().equals("Bob"))
               .select(s -> new Pair<>(s.getPurchaser().getName(),
                                 s.getSaleId()))
               .getDebugQueryString());
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
   public void testMultipleAggregation2()
   {
      // Calculate more than one aggregate
      // (Sum of all line order quantities and number of line orders)
      Pair<Integer, Integer> pairResult = em.lineOrderStream()
         .aggregate(
               data -> data.sumInt(lo -> lo.getQuantity()),
               data -> data.sumInt(lo -> 1));
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
      EntityManager em = this.em;
      assertEquals("SELECT B.SaleId AS COL1, B.Date AS COL2, B.CustomerId AS COL3 FROM Sales AS B WHERE ((((SELECT MAX(A.SaleId) AS COL4 FROM Sales AS A)) = (B.SaleId)))",
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
