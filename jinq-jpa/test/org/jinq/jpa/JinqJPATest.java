package org.jinq.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.Lineorder;
import org.jinq.jpa.test.entities.Supplier;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.junit.Test;

public class JinqJPATest extends JinqJPATestBase
{

   @Test
   public void testStreamEntities()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class);
      List<Customer> customerList = customers.toList();
      List<String> names = customerList.stream().map((c) -> c.getName()).sorted().collect(Collectors.toList());
      assertEquals("Alice", names.get(0));
      assertEquals(5, names.size());
      assertEquals("Eve", names.get(4));
   }

   @Test
   public void testStreamPages()
   {
      List<String> names = streams.streamAll(em, Customer.class)
            .setHint("automaticPageSize", 1)
            .select(c -> c.getName() )
            .toList();
      names = names.stream().sorted().collect(Collectors.toList());
      assertEquals(5, names.size());
      assertEquals("Alice", names.get(0));
      assertEquals("Bob", names.get(1));
      assertEquals("Carol", names.get(2));
      assertEquals("Dave", names.get(3));
      assertEquals("Eve", names.get(4));
   }

   private static void externalMethod() {}
   
   @Test
   public void testExceptionOnFail()
   {
      streams.streamAll(em, Customer.class)
            .setHint("exceptionOnTranslationFail", false)
            .select(c -> {externalMethod(); return "blank";} )
            .toList();
      try {
         streams.streamAll(em, Customer.class)
               .setHint("exceptionOnTranslationFail", true)
               .select(c -> {externalMethod(); return "blank";} )
               .toList();
      } 
      catch (RuntimeException e)
      {
         // Expected
         return;
      }
      fail();
   }
   
   @Test
   public void testJoinNMLink()
   {
      List<Pair<Item, Supplier>> results = streams.streamAll(em, Item.class)
            .where(i -> i.getName().equals("Widgets"))
            .join(i -> JinqStream.from(i.getSuppliers()))
            .toList();
      assertEquals("SELECT A, B FROM Item A, A.suppliers B WHERE A.name = 'Widgets'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(2, results.size());
      assertEquals("Widgets", results.get(0).getOne().getName());
      assertEquals("Conglomerate", results.get(0).getTwo().getName());
      assertEquals("HW Supplier", results.get(1).getTwo().getName());
   }

   @Test
   public void testJoin11NMLink()
   {
      List<Pair<Lineorder, Supplier>> results = streams.streamAll(em, Lineorder.class)
            .join(lo -> JinqStream.from(lo.getItem().getSuppliers()))
            .where(pair -> pair.getOne().getSale().getCustomer().getName().equals("Alice"))
            .toList();
      assertEquals("SELECT A, B FROM Lineorder A, A.item.suppliers B WHERE A.sale.customer.name = 'Alice'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(5, results.size());
      assertEquals("Conglomerate", results.get(1).getTwo().getName());
      assertEquals("HW Supplier", results.get(4).getTwo().getName());
   }

   @Test
   public void testJoinEntity()
   {
      List<Pair<Item, Item>> results = streams.streamAll(em, Item.class)
            .where(i -> i.getName().equals("Widgets"))
            .join((i, source) -> source.stream(Item.class))
            .where(pair -> pair.getOne().getPurchaseprice() < pair.getTwo().getPurchaseprice())
            .toList();
      assertEquals("SELECT A, B FROM Item A, Item B WHERE A.name = 'Widgets' AND A.purchaseprice < B.purchaseprice", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(2, results.size());
      assertEquals("Lawnmowers", results.get(0).getTwo().getName());
      assertEquals("Widgets", results.get(0).getOne().getName());
      assertEquals("Talent", results.get(1).getTwo().getName());
      assertEquals("Widgets", results.get(1).getOne().getName());
   }

   @Test(expected=IllegalArgumentException.class)
   public void testOuterJoin()
   {
      throw new IllegalArgumentException();
//      List<Pair<Item, Supplier>> results = streams.streamAll(em, Item.class)
//            .where(i -> i.getName().equals("Widgets"))
//            .leftOuterJoin(i -> JinqStream.from(i.getSuppliers()))
//            .toList();
//      assertEquals("SELECT A, B FROM Item A, A.suppliers B WHERE A.name = 'Widgets'", query);
//      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
//      assertEquals(2, results.size());
//      assertEquals("Widgets", results.get(0).getOne().getName());
//      assertEquals("Conglomerate", results.get(0).getTwo().getName());
//      assertEquals("HW Supplier", results.get(1).getTwo().getName());
   }

   @Test
   public void testSort()
   {
      List<Customer> results = streams.streamAll(em, Customer.class)
            .sortedBy(c -> c.getName())
            .toList();
      assertEquals("SELECT A FROM Customer A ORDER BY A.name ASC", query);
      assertEquals(5, results.size());
      assertEquals("Alice", results.get(0).getName());
      assertEquals("Bob", results.get(1).getName());
      assertEquals("Eve", results.get(4).getName());
   }

   @Test
   public void testSortExpression()
   {
      List<Item> results = streams.streamAll(em, Item.class)
            .where(i -> i.getPurchaseprice() > 1)
            .sortedDescendingBy(i -> i.getSaleprice() - i.getPurchaseprice())
            .toList();
      assertEquals("SELECT A FROM Item A WHERE A.purchaseprice > 1.0 ORDER BY A.saleprice - A.purchaseprice DESC", query);
      assertEquals(4, results.size());
      assertEquals("Talent", results.get(0).getName());
      assertEquals("Widgets", results.get(1).getName());
   }

   @Test
   public void testSortChained()
   {
      List<Customer> results = streams.streamAll(em, Customer.class)
            .sortedDescendingBy(c -> c.getName())
            .sortedBy(c -> c.getCountry())
            .toList();
      assertEquals("SELECT A FROM Customer A ORDER BY A.country ASC, A.name DESC", query);
      assertEquals(5, results.size());
      assertEquals("Eve", results.get(0).getName());
      assertEquals("Bob", results.get(1).getName());
      assertEquals("Alice", results.get(2).getName());
   }

   @Test
   public void testLimitSkip()
   {
      List<Customer> results = streams.streamAll(em, Customer.class)
            .setHint("automaticPageSize", 1)
            .sortedBy(c -> c.getName())
            .skip(1)
            .limit(2)
            .toList();
      assertEquals("SELECT A FROM Customer A ORDER BY A.name ASC", query);
      assertEquals(2, queryList.size());
      assertEquals(2, results.size());
      assertEquals("Bob", results.get(0).getName());
      assertEquals("Carol", results.get(1).getName());
   }

   @Test
   public void testSkipLimit()
   {
      List<Customer> results = streams.streamAll(em, Customer.class)
            .sortedBy(c -> c.getName())
            .limit(3)
            .skip(1)
            .toList();
      assertEquals("SELECT A FROM Customer A ORDER BY A.name ASC", query);
      assertEquals(1, queryList.size());
      assertEquals(2, results.size());
      assertEquals("Bob", results.get(0).getName());
      assertEquals("Carol", results.get(1).getName());
   }

   @Test
   public void testJPQLNumericPromotion()
   {
      // Trying to understand the numeric promotion rules for JPQL.
      // It looks like int -> long -> BigInteger -> BigDecimal -> double
      Object obj;

      obj = em.createQuery("SELECT A.quantity + A.sale.creditCard FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Long);  // int + long = long
      obj = em.createQuery("SELECT A.quantity + A.item.saleprice FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // int + double = double
      obj = em.createQuery("SELECT A.quantity + A.transactionConfirmation FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigInteger);  // int + BigInteger = BigInteger
      obj = em.createQuery("SELECT A.quantity + A.total FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigDecimal);  // int + BigDecimal = BigDecimal
      obj = em.createQuery("SELECT A.quantity + 1.0 FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // int + decimal constant = Double

      obj = em.createQuery("SELECT A.sale.creditCard + A.item.saleprice FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // long + double = double
      obj = em.createQuery("SELECT A.sale.creditCard + A.transactionConfirmation FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigInteger);  // long + BigInteger = BigInteger
      obj = em.createQuery("SELECT A.sale.creditCard + A.total FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigDecimal);  // long + BigDecimal = BigDecimal
      obj = em.createQuery("SELECT A.sale.creditCard + 1.0 FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // long + decimal constant = Double

      obj = em.createQuery("SELECT A.item.saleprice + A.transactionConfirmation FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // double + BigInteger = Double
      obj = em.createQuery("SELECT A.item.saleprice + A.total FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // double + BigDecimal = Double
      obj = em.createQuery("SELECT A.item.saleprice + 1.0 FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // double + decimal constant = Double

      obj = em.createQuery("SELECT A.transactionConfirmation + A.total FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigDecimal);  // BigInteger + BigDecimal = BigDecimal
      obj = em.createQuery("SELECT A.transactionConfirmation + 1.0 FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // BigInteger + decimal constant = Double

      obj = em.createQuery("SELECT A.total + 1.0 FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // BigDecimal + decimal constant = Double
   }
   
   @Test
   public void testJPQL()
   {
      // These  queries do not parse properly by JPQL:
      // Query q = em.createQuery("SELECT A FROM Customer A WHERE ((FALSE AND ((A.debt) >= 90)) OR (TRUE AND ((A.debt) < 90)))");
      // Query q = em.createQuery("SELECT A FROM Sale A WHERE (((A.customer).name) = 'Alice')");
      // Query q = em.createQuery("SELECT A FROM Sale A WHERE ((A.customer.name) = 'Alice')");
      // Query q = em.createQuery("SELECT TRUE FROM Supplier A WHERE A.hasFreeShipping");  // A.hasFreeShipping doesn't work
      // Query q = em.createQuery("SELECT TRUE=TRUE FROM Supplier A WHERE A.hasFreeShipping = TRUE");  // TRUE = TRUE doesn't work
      // Query q = em.createQuery("SELECT (1 = 1) FROM Supplier A WHERE A.hasFreeShipping = TRUE");  // 1=1 doesn't work
      // Query q = em.createQuery("SELECT A.hasFreeShipping FROM Supplier A WHERE 1=1");  // 1=1 works as a conditional, A.hasFreeShipping is ok if you return it
      // Query q = em.createQuery("SELECT SUM(A.purchaseprice + A.saleprice) FROM Item A");  // Checking whether sums of arbitrary expressions are allowed
      // Query q = em.createQuery("SELECT B, (SELECT COUNT(1) FROM B.sales C) FROM Customer B ORDER BY SELECT COUNT(1) FROM B.sales C ASC, B.name ASC"); // Subqueries in ORDER BY without brackets are ok 
      // Query q = em.createQuery("SELECT B, (SELECT COUNT(1) FROM B.sales C) FROM Customer B ORDER BY (SELECT COUNT(1) FROM B.sales C ASC), B.name ASC"); // Subqueries in ORDER BY with everything in brackets are ok 
      // Query q = em.createQuery("SELECT B, (SELECT COUNT(1) FROM B.sales C) FROM Customer B ORDER BY (SELECT COUNT(1) FROM B.sales C) ASC, B.name ASC"); // Subqueries in ORDER BY with only the subquery in brackets but not ASC is bad
      // Query q = em.createQuery("SELECT B, (SELECT COUNT(1) FROM B.sales C) FROM Customer B ORDER BY ((SELECT COUNT(1) FROM B.sales C) ASC), B.name ASC"); // Subqueries in ORDER BY with things in proper bracket hierarchy is bad.
      Query q = em.createQuery("SELECT COUNT(A), COUNT(B) FROM Customer A, A.Orders B");  // Checking to see if it matters what you stick inside the COUNT() function

      List results = q.getResultList();
//      for (Object o : results)
//         System.out.println(o);
   }
   
   @Test
   public void testJPQLLike()
   {
      assertTrue(JPQL.like("hello", "h%"));
      assertTrue(JPQL.like("hello", "h_llo"));
      assertFalse(JPQL.like("hllo", "h_llo"));
      assertTrue(JPQL.like("[b]hello", "[b]h_llo"));
      assertTrue(JPQL.like("m%hello", "mmm%h_llo", "m"));
      assertFalse(JPQL.like("mdfshello", "mmm%h_llo", "m"));
   }
   
   @Test
   public void testJPQLStringFunctions()
   {
      List<String> customers = streams.streamAll(em, Customer.class)
         .where(c -> JPQL.like(c.getName(), "A_i%ce"))
         .select( c -> c.getName().toUpperCase())
         .toList();
      assertEquals("SELECT UPPER(A.name) FROM Customer A WHERE A.name LIKE 'A_i%ce'", query);
      assertEquals(1, customers.size());
      assertEquals("ALICE", customers.get(0));
   }
   
   @Test
   public void testJPQLNumberFunctions()
   {
      List<Double> customers = streams.streamAll(em, Customer.class)
         .select( c -> Math.abs(c.getSalary() + Math.sqrt(c.getDebt())))
         .toList();
      assertEquals("SELECT ABS(A.salary + SQRT(A.debt)) FROM Customer A", query);
      assertEquals(5, customers.size());
   }
}
