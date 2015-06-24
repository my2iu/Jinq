package org.jinq.jpa;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Lineorder;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;
import org.jinq.tuples.Tuple6;
import org.jinq.tuples.Tuple7;
import org.jinq.tuples.Tuple8;
import org.junit.Test;

public class JinqJPASelectTest extends JinqJPATestBase
{
   @Test
   public void testSelect()
   {
      JinqStream<String> customers = streams.streamAll(em, Customer.class)
            .select(c -> c.getCountry());
      assertEquals("SELECT A.country FROM Customer A", customers.getDebugQueryString());
      List<String> results = customers.toList();
      assertEquals(5, results.size());
      Collections.sort(results);
      assertEquals("Canada", results.get(0));
   }

   @Test
   public void testSelectMethodReference()
   {
      List<String> results = streams.streamAll(em, Customer.class)
            .select(Customer::getCountry)
            .toList();
      assertEquals("SELECT A.country FROM Customer A", query);
      assertEquals(5, results.size());
      Collections.sort(results);
      assertEquals("Canada", results.get(0));
   }

   @Test
   public void testSelectMath()
   {
      JinqStream<Integer> customers = streams.streamAll(em, Customer.class)
            .select(c -> c.getDebt() + c.getSalary() * 2);
      assertEquals("SELECT A.debt + A.salary * 2 FROM Customer A", customers.getDebugQueryString());
      List<Integer> results = customers.toList();
      assertEquals(5, results.size());
      Collections.sort(results);
      assertEquals(70, (int)results.get(0));
   }

   @Test
   public void testSelectOperatorPrecedence()
   {
      List<Integer> results = streams.streamAll(em, Customer.class)
            .select(c -> 3 * (c.getDebt() - (c.getSalary() + 2)))
            .toList();
      assertEquals("SELECT 3 * (A.debt - (A.salary + 2)) FROM Customer A", query);
      assertEquals(5, results.size());
      Collections.sort(results);
      assertEquals(-1206, (int)results.get(0));
   }

   @Test
   public void testSelectPair()
   {
      JinqStream<Pair<String, String>> customers = streams.streamAll(em, Customer.class)
            .select(c -> new Pair<>(c.getName(), c.getCountry()));
      assertEquals("SELECT A.name, A.country FROM Customer A", customers.getDebugQueryString());
      List<Pair<String, String>> results = customers.toList();
      assertEquals(5, results.size());
      Collections.sort(results, (p1, p2) -> p1.getOne().compareTo(p2.getOne()));
      assertEquals("Alice", results.get(0).getOne());
   }

   @Test
   public void testSelectPairOfPair()
   {
      JinqStream<Pair<Pair<String, String>, Integer>> customers = streams.streamAll(em, Customer.class)
            .select(c -> new Pair<>(new Pair<>(c.getName(), c.getCountry()), c.getDebt()));
      assertEquals("SELECT A.name, A.country, A.debt FROM Customer A", customers.getDebugQueryString());
      List<Pair<Pair<String, String>, Integer>> results = customers.toList();
      assertEquals(5, results.size());
      Collections.sort(results, (p1, p2) -> p1.getOne().getOne().compareTo(p2.getOne().getOne()));
      assertEquals("Alice", results.get(0).getOne().getOne());
      assertEquals(100, (int)results.get(0).getTwo());
   }

   @Test
   public void testSelectChained()
   {
      JinqStream<Integer> customers = streams.streamAll(em, Customer.class)
            .select(c -> c.getDebt())
            .select(d -> d * 2);
      assertEquals("SELECT A.debt * 2 FROM Customer A", customers.getDebugQueryString());
      List<Integer> results = customers.toList();
      assertEquals(5, results.size());
      Collections.sort(results);
      assertEquals(20, (int)results.get(0));
   }

   @Test
   public void testSelectChainedPair()
   {
      JinqStream<Pair<String, Integer>> customers = streams.streamAll(em, Customer.class)
            .select(c -> new Pair<>(c.getName(), c.getDebt()))
            .where(p -> p.getTwo() > 250);
      assertEquals("SELECT A.name, A.debt FROM Customer A WHERE A.debt > 250", customers.getDebugQueryString());
      List<Pair<String, Integer>> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Carol", results.get(0).getOne());
   }
   
   @Test
   public void testSelectN1Link()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Sale.class)
            .select(s -> s.getCustomer());
      assertEquals("SELECT A.customer FROM Sale A", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      Collections.sort(results, (c1, c2) -> c1.getName().compareTo(c2.getName()));
      assertEquals(6, results.size());
      assertEquals("Alice", results.get(0).getName());
   }
   
   @Test
   public void testSelectCase()
   {
      List<Pair<String, String>> customers = streams.streamAll(em, Customer.class)
            .select(c -> new Pair<>(c.getName(), c.getCountry().equals("UK") ? "UK" : "NotUK"))
            .sortedBy(p -> p.getOne())
            .sortedBy(p -> p.getTwo())
            .toList();
      assertEquals("SELECT A.name, CASE WHEN A.country <> 'UK' THEN 'NotUK' ELSE 'UK' END FROM Customer A ORDER BY CASE WHEN A.country <> 'UK' THEN 'NotUK' ELSE 'UK' END ASC, A.name ASC", query);
      assertEquals(5, customers.size());
      assertEquals("UK", customers.get(4).getTwo());
      assertEquals("NotUK", customers.get(3).getTwo());
   }
   
   @Test
   public void testSelectDistinct()
   {
      List<String> itemsSold = streams.streamAll(em, Lineorder.class)
            .select(lo -> lo.getItem().getName())
            .distinct()
            .sortedBy(name -> name)
            .toList();
      assertEquals("SELECT DISTINCT A.item.name FROM Lineorder A ORDER BY A.item.name ASC", query);
      assertEquals(5, itemsSold.size());
      assertEquals("Lawnmowers", itemsSold.get(0));
   }
   
   @Test
   public void testSelectAll()
   {
      List<String> suppliers = streams.streamAll(em, Item.class)
            .where(i -> i.getName().equals("Screws"))
            .selectAll(i -> JinqStream.from(i.getSuppliers()))
            .select(s -> s.getName())
            .toList();
      assertEquals("SELECT B.name FROM Item A JOIN A.suppliers B WHERE A.name = 'Screws'", query);
      assertEquals(1, suppliers.size());
      assertEquals("HW Supplier", suppliers.get(0));
   }
   
   @Test
   public void testSelectAllList()
   {
      List<String> suppliers = streams.streamAll(em, Item.class)
            .where(i -> i.getName().equals("Screws"))
            .selectAllList(i -> i.getSuppliers())
            .select(s -> s.getName())
            .toList();
      assertEquals("SELECT B.name FROM Item A JOIN A.suppliers B WHERE A.name = 'Screws'", query);
      assertEquals(1, suppliers.size());
      assertEquals("HW Supplier", suppliers.get(0));
   }
   
   private void checkTuples(List<?> tuples, Object...elements)
   {
      assertEquals(5, tuples.size());
      assertEquals(Tuple.createTuple(elements), tuples.get(0));
   }
   
   @Test
   public void testTuples()
   {
      List<Pair<Integer, Integer>> tuples2 = 
            streams.streamAll(em, Customer.class)
                  .select(c -> new Pair<>(1, 2))
                  .toList();
      checkTuples(tuples2, 1, 2);
      
      List<Tuple3<Integer, Integer, Integer>> tuples3 = 
            streams.streamAll(em, Customer.class)
                  .select(c -> new Tuple3<>(1, 2, 3))
                  .toList();
      checkTuples(tuples3, 1, 2, 3);

      List<Tuple4<Integer, Integer, Integer, Integer>> tuples4 = 
            streams.streamAll(em, Customer.class)
                  .select(c -> new Tuple4<>(1, 2, 3, 4))
                  .toList();
      checkTuples(tuples4, 1, 2, 3, 4);

      List<Tuple5<Integer, Integer, Integer, Integer, Integer>> tuples5 = 
            streams.streamAll(em, Customer.class)
                  .select(c -> new Tuple5<>(1, 2, 3, 4, 5))
                  .toList();
      checkTuples(tuples5, 1, 2, 3, 4, 5);

      List<Tuple6<Integer, Integer, Integer, Integer, Integer, Integer>> tuples6 = 
            streams.streamAll(em, Customer.class)
                  .select(c -> new Tuple6<>(1, 2, 3, 4, 5, 6))
                  .toList();
      checkTuples(tuples6, 1, 2, 3, 4, 5, 6);

      List<Tuple7<Integer, Integer, Integer, Integer, Integer, Integer, Integer>> tuples7 = 
            streams.streamAll(em, Customer.class)
                  .select(c -> new Tuple7<>(1, 2, 3, 4, 5, 6, 7))
                  .toList();
      checkTuples(tuples7, 1, 2, 3, 4, 5, 6, 7);

      List<Tuple8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> tuples8 = 
            streams.streamAll(em, Customer.class)
                  .select(c -> new Tuple8<>(1, 2, 3, 4, 5, 6, 7, 8))
                  .toList();
      checkTuples(tuples8, 1, 2, 3, 4, 5, 6, 7, 8);
   }
}
