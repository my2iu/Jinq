package org.jinq.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.Lineorder;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.jpa.test.entities.Supplier;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.JinqStream.Where;
import org.jinq.tuples.Pair;
import org.junit.Assert;
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
      assertEquals("SELECT A, B FROM Item A JOIN A.suppliers B WHERE A.name = 'Widgets'", query);
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
      assertEquals("SELECT A, B FROM Lineorder A JOIN A.item.suppliers B WHERE A.sale.customer.name = 'Alice'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(5, results.size());
      assertEquals("Conglomerate", results.get(1).getTwo().getName());
      assertEquals("HW Supplier", results.get(4).getTwo().getName());
   }
   
   @Test
   public void testJoinList()
   {
      List<Pair<Lineorder, Supplier>> results = streams.streamAll(em, Lineorder.class)
            .joinList(lo -> lo.getItem().getSuppliers())
            .where(pair -> pair.getOne().getSale().getCustomer().getName().equals("Alice"))
            .toList();
      assertEquals("SELECT A, B FROM Lineorder A JOIN A.item.suppliers B WHERE A.sale.customer.name = 'Alice'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(5, results.size());
      assertEquals("Conglomerate", results.get(1).getTwo().getName());
      assertEquals("HW Supplier", results.get(4).getTwo().getName());
   }
   
   @Test
   public void testJoinFetchList()
   {
      List<Sale> results = streams.streamAll(em, Sale.class)
            .joinFetchList(s -> s.getLineorders())
            .where(s -> s.getCustomer().getName().equals("Alice"))
            .distinct()
            .toList();
      assertEquals("SELECT DISTINCT A FROM Sale A JOIN FETCH A.lineorders B WHERE A.customer.name = 'Alice'", query);
      // The semantics of JOIN FETCH are a little inconsistent
      // so it's hard to know exactly will be returned. EclipseLink seems
      // to treat it like a regular join, so you need to use DISTINCT to prevent
      // the same result from appearing too many times, but Hibernate will leave 
      // the join fetched items out of result sets even if you include it there..
      assertEquals(2, results.size());
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

   @Test
   public void testOuterJoin()
   {
      List<Pair<Item, Supplier>> results = streams.streamAll(em, Item.class)
            .where(i -> i.getName().equals("Widgets"))
            .leftOuterJoin(i -> JinqStream.from(i.getSuppliers()))
            .toList();
      assertEquals("SELECT A, B FROM Item A LEFT OUTER JOIN A.suppliers B WHERE A.name = 'Widgets'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(2, results.size());
      assertEquals("Widgets", results.get(0).getOne().getName());
      assertEquals("Conglomerate", results.get(0).getTwo().getName());
      assertEquals("HW Supplier", results.get(1).getTwo().getName());
   }

   @Test
   public void testOuterJoinList()
   {
      List<Pair<Item, Supplier>> results = streams.streamAll(em, Item.class)
            .where(i -> i.getName().equals("Widgets"))
            .leftOuterJoinList(i -> i.getSuppliers())
            .toList();
      assertEquals("SELECT A, B FROM Item A LEFT OUTER JOIN A.suppliers B WHERE A.name = 'Widgets'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(2, results.size());
      assertEquals("Widgets", results.get(0).getOne().getName());
      assertEquals("Conglomerate", results.get(0).getTwo().getName());
      assertEquals("HW Supplier", results.get(1).getTwo().getName());
   }

   @Test
   public void testOuterJoinChain()
   {
      List<Pair<Lineorder, Supplier>> results = streams.streamAll(em, Lineorder.class)
            .where(lo -> lo.getItem().getName().equals("Talent"))
            .leftOuterJoin(lo -> JinqStream.from(lo.getItem().getSuppliers()))
            .toList();
      assertEquals("SELECT A, C FROM Lineorder A JOIN A.item B LEFT OUTER JOIN B.suppliers C WHERE A.item.name = 'Talent'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(1, results.size());
   }

   @Test
   public void testOuterJoin11()
   {
      List<Pair<Lineorder, Item>> results = streams.streamAll(em, Lineorder.class)
            .leftOuterJoin(lo -> JinqStream.of(lo.getItem()))
            .where(pair -> pair.getTwo().getName().equals("Talent"))
            .toList();
      assertEquals("SELECT A, B FROM Lineorder A LEFT OUTER JOIN A.item B WHERE B.name = 'Talent'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(1, results.size());
   }

   @Test
   public void testOuterJoinFetch()
   {
      List<Sale> results = streams.streamAll(em, Sale.class)
            .leftOuterJoinFetch(s -> JinqStream.from(s.getLineorders()))
            .where(s -> s.getCustomer().getName().equals("Alice"))
            .distinct()
            .toList();
      assertEquals("SELECT DISTINCT A FROM Sale A LEFT OUTER JOIN FETCH A.lineorders B WHERE A.customer.name = 'Alice'", query);
      // The semantics of JOIN FETCH are a little inconsistent
      // so it's hard to know exactly will be returned. EclipseLink seems
      // to treat it like a regular join, so you need to use DISTINCT to prevent
      // the same result from appearing too many times, but Hibernate will leave 
      // the join fetched items out of result sets even if you include it there..
      assertEquals(2, results.size());
   }

   @Test(expected=IllegalArgumentException.class)
   public void testOuterJoinField()
   {
      // Cannot do outer joins on normal fields. Only navigational links.
      List<Pair<Customer, String>> results = streams.streamAll(em, Customer.class)
            .leftOuterJoin(c -> JinqStream.of(c.getCountry()))
            .toList();
      assertEquals("SELECT A, B FROM Customer A LEFT OUTER JOIN A.country B", query);
      assertEquals(5, results.size());
   }
   
   @Test
   public void testOuterJoinOn()
   {
      List<Pair<Item, Supplier>> results = streams.streamAll(em, Item.class)
            .leftOuterJoin(
                  (i, source) -> source.stream(Supplier.class),
                  (item, supplier) -> item.getName().substring(0, 1).equals(supplier.getName().substring(0, 1)))
            .toList();
      assertEquals("SELECT A, B FROM Item A LEFT OUTER JOIN Supplier B ON SUBSTRING(A.name, 0 + 1, 1 - 0) = SUBSTRING(B.name, 0 + 1, 1 - 0)", query);
      Collections.sort(results, (c1, c2) -> c1.getOne().getName().compareTo(c2.getOne().getName()));
      assertEquals(5, results.size());
      assertEquals("Lawnmowers", results.get(0).getOne().getName());
      Assert.assertNull(results.get(0).getTwo());
      assertEquals("Talent", results.get(2).getOne().getName());
      assertEquals("Talent Agency", results.get(2).getTwo().getName());
   }
   
   @Test
   public void testOuterJoinOnTrueAndNavigationalLinks()
   {
      List<Pair<Item, Supplier>> results = streams.streamAll(em, Item.class)
            .leftOuterJoin(
                  (i, source) -> JinqStream.from(i.getSuppliers()),
                  (item, supplier) -> true)
            .toList();
      assertEquals("SELECT A, B FROM Item A LEFT OUTER JOIN A.suppliers B", query);
      assertEquals(6, results.size());
   }

   @Test
   public void testOuterJoinOnWithParametersAndIndirect()
   {
      String match = "Screws";
      List<Pair<String, Supplier>> results = streams.streamAll(em, Item.class)
            .select(i -> i.getName())
            .leftOuterJoin(
                  (i, source) -> source.stream(Supplier.class),
                  (itemName, supplier) -> itemName.equals(match))
            .toList();
      assertEquals("SELECT A.name, B FROM Item A LEFT OUTER JOIN Supplier B ON A.name = :param0", query);
      Collections.sort(results, (c1, c2) -> c1.getOne().compareTo(c2.getOne()));
      assertEquals(7, results.size());
      assertEquals("Lawnmowers", results.get(0).getOne());
      Assert.assertNull(results.get(0).getTwo());
      assertEquals("Screws", results.get(1).getOne());
      assertEquals("Screws", results.get(2).getOne());
      assertEquals("Screws", results.get(3).getOne());
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

   @Test(expected=IllegalArgumentException.class)
   public void testTooManyPaths()
   {
      List<Customer> results = streams.streamAll(em, Customer.class)
            .where(c -> (c.getName().equals("Alice") && c.getSalary() == 5)
                  || (c.getName().equals("Bob") && c.getSalary() == 6)
                  || (c.getName().equals("Dave") && c.getSalary() == 7)
                  || (c.getName().equals("Eve") && c.getSalary() == 8)
                  || (c.getName().equals("Carol") && c.getSalary() == 9)
                  || (c.getName().equals("Alice") && c.getSalary() == 10)
                  || (c.getName().equals("Bob") && c.getSalary() == 11)
                  || (c.getName().equals("Carol") && c.getSalary() == 12))
            .toList();
   }
   
   @Test
   public void testCaching()
   {
      // Ensure the base "find all customers" query is in the cache
      streams.streamAll(em, Customer.class);
      // Create a query composer for finding all customers.
      Optional<JPQLQuery<?>> cachedQuery = streams.cachedQueries.findCachedFindAllEntities("Customer");
      JPAQueryComposer<Customer> composer = JPAQueryComposer.findAllEntities(streams.metamodel, streams.cachedQueries, streams.lambdaAnalyzer, streams.jpqlQueryTransformConfigurationFactory, em, streams.hints, (JPQLQuery<Customer>)cachedQuery.get());
      // Apply a where restriction to it
      JPAQueryComposer<Customer> where1 = repeatedQuery(composer, 1);
      JPAQueryComposer<Customer> where2 = repeatedQuery(composer, 2);
      JPAQueryComposer<Customer> where3 = repeatedQuery(composer, 3);
      // Check that the queries have the exact same underlying query object
      assertTrue(where1.query == where2.query);
      assertTrue(where2.query == where3.query);
   }
   
   private JPAQueryComposer<Customer> repeatedQuery(JPAQueryComposer<Customer> composer, int param)
   {
      Where<Customer, RuntimeException> where = (c -> c.getDebt() == param);
      return composer.where(where);
   }

   @Test
   public void testJPQLNumericPromotion()
   {
      // Trying to understand the numeric promotion rules for JPQL.
      // It looks like int -> long -> BigInteger -> BigDecimal -> double
      Object obj;

      obj = em.createQuery("SELECT A.quantity + A.sale.creditCard.number FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Long);  // int + long = long
      obj = em.createQuery("SELECT A.quantity + A.item.saleprice FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // int + double = double
      obj = em.createQuery("SELECT A.quantity + A.transactionConfirmation FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigInteger);  // int + BigInteger = BigInteger
      obj = em.createQuery("SELECT A.quantity + A.total FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigDecimal);  // int + BigDecimal = BigDecimal
      obj = em.createQuery("SELECT A.quantity + 1.0 FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // int + decimal constant = Double

      obj = em.createQuery("SELECT A.sale.creditCard.number + A.item.saleprice FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof Double);  // long + double = double
      obj = em.createQuery("SELECT A.sale.creditCard.number + A.transactionConfirmation FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigInteger);  // long + BigInteger = BigInteger
      obj = em.createQuery("SELECT A.sale.creditCard.number + A.total FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
      assertTrue(obj instanceof BigDecimal);  // long + BigDecimal = BigDecimal
      obj = em.createQuery("SELECT A.sale.creditCard.number + 1.0 FROM Lineorder A WHERE A.item.name='Talent'").getSingleResult();
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
      // Query q = em.createQuery("SELECT COUNT(A), COUNT(B) FROM Customer A, A.Orders B");  // Checking to see if it matters what you stick inside the COUNT() function
      // Query q = em.createQuery("SELECT B FROM Customer D, (SELECT DICTINCT A FROM Sale A) B");  // Trying to see how subqueries in a FROM work--it seems like subqueries in FROM are not implemented or barely working
      // Query q = em.createQuery("SELECT A.name FROM Customer A WHERE A.salary < (SELECT B.salary FROM Customer B WHERE B.name = 'Alice') ");  // Checking for JPQL support for subqueries returning a single value
      // Query q = em.createQuery("SELECT A, B FROM Sale A join A.customer B WHERE B.name = 'Alice'");  // Hibernate seems to require you to actually use the "join" keyword when using a plural navigational link instead of letting you use commas.
      // Query q = em.createQuery("SELECT A, B FROM Item A join A.suppliers B WHERE A.name = 'Widgets'");  // Hibernate seems to require you to actually use the "join" keyword when using a plural navigational link instead of letting you use commas.
      // Query q = em.createQuery("SELECT A, B FROM Item A JOIN FETCH A.suppliers B WHERE A.name = 'Widgets'");  // In a JOIN FETCH, Hibernate doesn't include the "JOINed" supplier in the result set.  
      Query q = em.createQuery("SELECT A.name, B.name FROM Item A LEFT JOIN Supplier B ON A.itemid = B.supplierid");  // Is LEFT OUTER JOIN with ON supported?  
      // Query q = em.createQuery("SELECT A FROM Customer A WHERE ((A.debt) >= 90) IS FALSE");  // Test of existence of IS FALSE (it doesn't exist) 

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
         .where(c -> JPQL.like(c.getName(), "A_i%ce") && c.getName().length() > c.getName().indexOf("l"))
         .select( c -> c.getName().toUpperCase().trim() + c.getCountry().substring(0, 1))
         .toList();
      assertEquals("SELECT CONCAT(TRIM(UPPER(A.name)), SUBSTRING(A.country, 0 + 1, 1 - 0)) FROM Customer A WHERE A.name LIKE 'A_i%ce' AND LENGTH(A.name) > LOCATE('l', A.name) - 1", query);
      assertEquals(1, customers.size());
      assertEquals("ALICES", customers.get(0));
   }

   @Test
   public void testJPQLStringConcat()
   {
      List<String> customers = streams.streamAll(em, Customer.class)
         .select( c -> c.getName() + " " + c.getCountry())
         .sortedBy( s -> s)
         .toList();
      assertEquals("SELECT CONCAT(CONCAT(A.name, ' '), A.country) FROM Customer A ORDER BY CONCAT(CONCAT(A.name, ' '), A.country) ASC", query);
      assertEquals(5, customers.size());
      assertEquals("Alice Switzerland", customers.get(0));
   }

   @Test
   public void testJPQLStringContains()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
         .where( c -> c.getName().contains("Al"))
         .toList();
      assertEquals("SELECT A FROM Customer A WHERE LOCATE('Al', A.name) > 0", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());

      customers = streams.streamAll(em, Customer.class)
            .where( c -> !c.getName().contains("Al"))
            .toList();
      assertEquals("SELECT A FROM Customer A WHERE NOT LOCATE('Al', A.name) > 0", query);
      assertEquals(4, customers.size());
   }

   @Test(expected=IllegalArgumentException.class)
   public void testJPQLStringContainsCharSequence1()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .where( c -> c.getName().contains(new StringBuilder("A").append("l")))
            .toList();
      assertEquals("SELECT A FROM Customer A WHERE LOCATE('Al', A.name) > 0", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void testJPQLStringContainsCharSequence2()
   {
      StringBuilder al = new StringBuilder("A").append("l");
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .where( c -> c.getName().contains(al))
            .toList();
      assertEquals("SELECT A FROM Customer A WHERE LOCATE('Al', A.name) > 0", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());
   }
   
   @Test
   public void testJPQLNumberFunctions()
   {
      List<Double> customers = streams.streamAll(em, Customer.class)
         .select( c -> Math.abs(c.getSalary() + Math.sqrt(c.getDebt())) + (c.getSalary() % c.getDebt()))
         .toList();
      assertEquals("SELECT ABS(A.salary + SQRT(A.debt)) + MOD(A.salary, A.debt) FROM Customer A", query);
      assertEquals(5, customers.size());
   }
}
