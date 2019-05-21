package org.jinq.hibernate;

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

import org.jinq.hibernate.test.entities.Customer;
import org.jinq.hibernate.test.entities.Item;
import org.jinq.hibernate.test.entities.Lineorder;
import org.jinq.hibernate.test.entities.Sale;
import org.jinq.hibernate.test.entities.Supplier;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JPQL;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.JinqStream.Where;
import org.jinq.orm.stream.NonQueryJinqStream;
import org.jinq.tuples.Pair;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
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
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Item A JOIN A.suppliers B WHERE A.name = 'Widgets'", query);
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
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Lineorder A JOIN A.item.suppliers B WHERE A.sale.customer.name = 'Alice'", query);
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
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Lineorder A JOIN A.item.suppliers B WHERE A.sale.customer.name = 'Alice'", query);
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
      assertEquals("SELECT DISTINCT A FROM org.jinq.hibernate.test.entities.Sale A JOIN FETCH A.lineorders B WHERE A.customer.name = 'Alice'", query);
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
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Item A, org.jinq.hibernate.test.entities.Item B WHERE A.name = 'Widgets' AND A.purchaseprice < B.purchaseprice", query);
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
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Item A LEFT OUTER JOIN A.suppliers B WHERE A.name = 'Widgets'", query);
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
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Item A LEFT OUTER JOIN A.suppliers B WHERE A.name = 'Widgets'", query);
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
      assertEquals("SELECT A, C FROM org.jinq.hibernate.test.entities.Lineorder A JOIN A.item B LEFT OUTER JOIN B.suppliers C WHERE A.item.name = 'Talent'", query);
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
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Lineorder A LEFT OUTER JOIN A.item B WHERE B.name = 'Talent'", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(1, results.size());
   }

   
   @Test
   public void testOuterJoinOn()
   {
      List<Pair<Item, Supplier>> results = streams.streamAll(em, Item.class)
            .leftOuterJoin(
                  (i, source) -> source.stream(Supplier.class),
                  (item, supplier) -> item.getName().substring(0, 1).equals(supplier.getName().substring(0, 1)))
            .toList();
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Item A LEFT OUTER JOIN org.jinq.hibernate.test.entities.Supplier B ON SUBSTRING(A.name, 0 + 1, 1 - 0) = SUBSTRING(B.name, 0 + 1, 1 - 0)", query);
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
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Item A LEFT OUTER JOIN A.suppliers B", query);
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
      assertEquals("SELECT A.name, B FROM org.jinq.hibernate.test.entities.Item A LEFT OUTER JOIN org.jinq.hibernate.test.entities.Supplier B ON A.name = :param0", query);
      Collections.sort(results, (c1, c2) -> c1.getOne().compareTo(c2.getOne()));
      assertEquals(7, results.size());
      assertEquals("Lawnmowers", results.get(0).getOne());
      Assert.assertNull(results.get(0).getTwo());
      assertEquals("Screws", results.get(1).getOne());
      assertEquals("Screws", results.get(2).getOne());
      assertEquals("Screws", results.get(3).getOne());
   }

   @Test
   public void testOuterJoinFetch()
   {
      List<Sale> results = streams.streamAll(em, Sale.class)
            .leftOuterJoinFetch(s -> JinqStream.from(s.getLineorders()))
            .where(s -> s.getCustomer().getName().equals("Alice"))
            .distinct()
            .toList();
      assertEquals("SELECT DISTINCT A FROM org.jinq.hibernate.test.entities.Sale A LEFT OUTER JOIN FETCH A.lineorders B WHERE A.customer.name = 'Alice'", query);
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
   public void testCrossJoin()
   {
      String country = "Switzerland";
      double price = 5.0;
      List<Pair<Customer, Item>> results = streams.streamAll(em, Customer.class)
            .where(c -> c.getCountry().equals(country))
            .crossJoin(streams.streamAll(em, Item.class).where(i -> i.getPurchaseprice() > price))
            .toList();
      assertEquals("SELECT A, B FROM org.jinq.hibernate.test.entities.Customer A, org.jinq.hibernate.test.entities.Item B WHERE A.country = :param0 AND B.purchaseprice > :param1", query);
      Collections.sort(results, (c1, c2) -> c1.getOne().getName().compareTo(c2.getOne().getName()));
      assertEquals(4, results.size());
   }

   @Test
   public void testSort()
   {
      List<Customer> results = streams.streamAll(em, Customer.class)
            .sortedBy(c -> c.getName())
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY A.name ASC", query);
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
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Item A WHERE A.purchaseprice > 1.0 ORDER BY A.saleprice - A.purchaseprice DESC", query);
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
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY A.country ASC, A.name DESC", query);
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
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY A.name ASC", query);
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
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY A.name ASC", query);
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
      Optional<JPQLQuery<?>> cachedQuery = streams.cachedQueries.findCachedFindAllEntities("org.jinq.hibernate.test.entities.Customer");
      HibernateQueryComposer<Customer> composer = HibernateQueryComposer.findAllEntities(streams.metamodel, streams.cachedQueries, streams.lambdaAnalyzer, streams.jpqlQueryTransformConfigurationFactory, em, streams.hints, (JPQLQuery<Customer>)cachedQuery.get());
      // Apply a where restriction to it
      HibernateQueryComposer<Customer> where1 = repeatedQuery(composer, 1);
      HibernateQueryComposer<Customer> where2 = repeatedQuery(composer, 2);
      HibernateQueryComposer<Customer> where3 = repeatedQuery(composer, 3);
      // Check that the queries have the exact same underlying query object
      assertTrue(where1.query == where2.query);
      assertTrue(where2.query == where3.query);
   }
   
   @Test
   public void testCachingSort()
   {
      // Regression test. Before, the caching key wasn't properly distinguishing ascending and descending sorts
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .sortedBy(Customer::getName)
            .toList();
      Assert.assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY A.name ASC", query);
      Assert.assertEquals("Alice", customers.get(0).getName());
      customers = streams.streamAll(em, Customer.class)
            .sortedDescendingBy(Customer::getName)
            .toList();
      Assert.assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY A.name DESC", query);
      Assert.assertEquals("Eve", customers.get(0).getName());

      JinqStream.CollectComparable<Customer, String> nameLambda = (Customer c) -> c.getName();
      customers = streams.streamAll(em, Customer.class)
            .sortedBy(nameLambda)
            .toList();
      Assert.assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY A.name ASC", query);
      Assert.assertEquals("Alice", customers.get(0).getName());
      customers = streams.streamAll(em, Customer.class)
            .sortedDescendingBy(Customer::getName)
            .toList();
      Assert.assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY A.name DESC", query);
      Assert.assertEquals("Eve", customers.get(0).getName());

   }
   
   private HibernateQueryComposer<Customer> repeatedQuery(HibernateQueryComposer<Customer> composer, int param)
   {
      Where<Customer, RuntimeException> where = (c -> c.getDebt() == param);
      return composer.where(where);
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
      assertEquals("SELECT CONCAT(TRIM(UPPER(A.name)), SUBSTRING(A.country, 0 + 1, 1 - 0)) FROM org.jinq.hibernate.test.entities.Customer A WHERE A.name LIKE 'A_i%ce' AND LENGTH(A.name) > LOCATE('l', A.name) - 1", query);
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
      assertEquals("SELECT CONCAT(CONCAT(A.name, ' '), A.country) FROM org.jinq.hibernate.test.entities.Customer A ORDER BY CONCAT(CONCAT(A.name, ' '), A.country) ASC", query);
      assertEquals(5, customers.size());
      assertEquals("Alice Switzerland", customers.get(0));
   }
   
   @Test
   public void testJPQLStringContains()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
         .where( c -> c.getName().contains("Al"))
         .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE LOCATE('Al', A.name) > 0", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());

      customers = streams.streamAll(em, Customer.class)
            .where( c -> !c.getName().contains("Al"))
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE NOT LOCATE('Al', A.name) > 0", query);
      assertEquals(4, customers.size());
   }

   @Test(expected=IllegalArgumentException.class)
   public void testJPQLStringContainsCharSequence1()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .where( c -> c.getName().contains(new StringBuilder("A").append("l")))
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE LOCATE('Al', A.name) > 0", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void testJPQLStringContainsCharSequence2()
   {
      StringBuilder al = new StringBuilder("A").append("l");
//      List<Pair<String, Integer>> customers2 = streams.streamAll(em, Customer.class)
//            .select(c -> new Pair<String, Integer>(c.getName(), c.getName().indexOf(al)))
//            .toList();
//      for (Pair<String, Integer> p: customers2)
//         System.out.println(p);
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .where( c -> c.getName().contains(al))
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE LOCATE(:param0, A.name) > 0", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());
   }

   @Test
   public void testJPQLStringStartsWith()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
         .where( c -> c.getName().startsWith("Al"))
         .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE LOCATE('Al', A.name) = 1", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());

      customers = streams.streamAll(em, Customer.class)
            .where( c -> !c.getName().startsWith("Al"))
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE NOT LOCATE('Al', A.name) = 1", query);
      assertEquals(4, customers.size());
   }

   @Test
   public void testJPQLNumberFunctions()
   {
      List<Double> customers = streams.streamAll(em, Customer.class)
         .select( c -> Math.abs(c.getSalary() + Math.sqrt(c.getDebt())) + (c.getSalary() % c.getDebt()))
         .toList();
      assertEquals("SELECT ABS(A.salary + SQRT(A.debt)) + MOD(A.salary, A.debt) FROM org.jinq.hibernate.test.entities.Customer A", query);
      assertEquals(5, customers.size());
   }
   
   @Test
   public void testOrUnion()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
         .where(c -> c.getName().equals("Eve"))
         .orUnion(streams.streamAll(em, Customer.class)
               .where(c -> c.getName().equals("Alice")))
         .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE A.name = 'Eve' OR A.name = 'Alice'", query);
      assertEquals(2, customers.size());
   }
   
   @Test
   public void testOrUnionParameters()
   {
      String name1 = "Alice";
      String name2 = "Eve";
      List<Customer> customers = streams.streamAll(em, Customer.class)
         .where(c -> c.getName().equals(name2))
         .orUnion(streams.streamAll(em, Customer.class)
               .where(c -> c.getName().equals(name1)))
         .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE A.name = :param0 OR A.name = :param1", query);
      assertEquals(2, customers.size());
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testNotComplement()
   {
      JPAJinqStream<Customer> base = streams.streamAll(em, Customer.class);
      JPAJinqStream<Customer> Bob = base.where( c -> c.getName().equals("Bob") );
      JPAJinqStream<Customer> notBob = Bob.notComplement();
      List<Customer> notBobList = notBob.toList();
      
      assertEquals("SELECT A FROM Customer A WHERE NOT A.name = 'Bob'", query);
            
      JinqStream<Customer> BobStream =  new NonQueryJinqStream<>(base);
      JPAJinqStream<Customer> notBobStream = (new JPAJinqStreamWrapper<>(BobStream)).notComplement();        
      Assert.fail();
   }
   
   @Test
   public void testNotComplementQueries()
   {
      JPAJinqStream<Customer> base = streams.streamAll(em, Customer.class);
      JPAJinqStream<Customer> Dave = base.where(c -> c.getName().equals("Dave"));
      JPAJinqStream<Customer> allCustomers = base;
      
      JPAJinqStream<Customer> notDave = Dave.notComplement();
      List<Customer> notDaveList = notDave.toList();
      assertEquals("SELECT A FROM Customer A WHERE NOT A.name = 'Dave'", query);

      JPAJinqStream<Customer> notAll = allCustomers.notComplement();
      List<Customer> notAllList =  notAll.toList();
      assertEquals("SELECT A FROM Customer A WHERE 0 = 1", query);

      JPAJinqStream<Customer> notNotAll = notAll.notComplement();
      List<Customer> notNotAllList =  notNotAll.toList();
      assertEquals("SELECT A FROM Customer A WHERE NOT 0 = 1", query);  
      
      JPAJinqStream<Customer> q = notNotAll.andIntersect(notDave);
      List<Customer> qList = q.toList();
      assertEquals("SELECT A FROM Customer A WHERE NOT 0 = 1 AND NOT A.name = 'Dave'", query);
      
      JPAJinqStream<Customer> notQ = q.notComplement();
      List<Customer> notQList = notQ.toList();
      assertEquals("SELECT A FROM Customer A WHERE NOT (NOT 0 = 1 AND NOT A.name = 'Dave')", query);      
   }      
   
   
   @Test
   public void testDifference()
   {
      JPAJinqStream<Customer> base = streams.streamAll(em, Customer.class);
      JPAJinqStream<Customer> Dave = base.where(c -> c.getName().equals("Dave"));
      String bobName = "Bob";  
      JPAJinqStream<Customer> Bob = base.where(c -> c.getName().equals(bobName));
      JPAJinqStream<Customer> allCustomers = base;
      
      JPAJinqStream<Customer> allDiffDave = allCustomers.andNotDifference(Dave);
      List<Customer> allDiffDaveList =  allDiffDave.toList();
      assertEquals("SELECT A FROM Customer A WHERE NOT A.name = 'Dave'", query);

      JPAJinqStream<Customer> BobDiffDave = Bob.andNotDifference(Dave);
      List<Customer> BobDiffDaveResult = BobDiffDave.toList();
      assertEquals("SELECT A FROM Customer A WHERE A.name = :param0 AND NOT A.name = 'Dave'", query);

      JPAJinqStream<Customer> allDiffAll = allCustomers.andNotDifference(allCustomers);
      List<Customer> allDiffAllList =  allDiffAll.toList();
      assertEquals("SELECT A FROM Customer A WHERE 0 = 1", query);

      JPAJinqStream<Customer> DaveDiffAll = Dave.andNotDifference(allCustomers);
      List<Customer> DaveDiffAllList =  DaveDiffAll.toList();
      assertEquals("SELECT A FROM Customer A WHERE 0 = 1", query);  
            
   }

   
   @Test
   public void testAndIntersect()
   {
      JPAJinqStream<Customer> base = streams.streamAll(em, Customer.class);
      List<String> customers = base.where(c -> c.getName().equals("Dave"))
         .orUnion(base.where(c -> c.getCountry().equals("Switzerland"))
               .andIntersect(base.where(c -> c.getSalary() > 250)))
         .select(c -> c.getName())
         .sortedBy(name -> name)
         .toList();
      assertEquals("SELECT A.name FROM org.jinq.hibernate.test.entities.Customer A WHERE A.name = 'Dave' OR A.country = 'Switzerland' AND A.salary > 250 ORDER BY A.name ASC", query);
      assertEquals(2, customers.size());
      assertEquals("Bob", customers.get(0));
      assertEquals("Dave", customers.get(1));
   }
   
}
