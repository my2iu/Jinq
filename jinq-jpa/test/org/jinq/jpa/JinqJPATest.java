package org.jinq.jpa;

import static org.junit.Assert.*;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.jpa.test.entities.Supplier;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.NonQueryJinqStream;
import org.jinq.tuples.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JinqJPATest
{
   static EntityManagerFactory entityManagerFactory;
   static JinqJPAStreamProvider streams;

   EntityManager em;
   String query;
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
      streams = new JinqJPAStreamProvider(entityManagerFactory);
      EntityManager em = entityManagerFactory.createEntityManager();
      new CreateJpaDb(em).createDatabase();
      em.close();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      entityManagerFactory.close();
      try {
         DriverManager.getConnection("jdbc:derby:memory:demoDB;drop=true");
      } catch (SQLException e) { }
   }

   @Before
   public void setUp() throws Exception
   {
      em = entityManagerFactory.createEntityManager();
      streams.setHint("exceptionOnTranslationFail", true);
      streams.setHint("queryLogger", new JPAQueryLogger() {
         @Override public void logQuery(String q,
               Map<Integer, Object> positionParameters,
               Map<String, Object> namedParameters)
         {
            query = q;
         }});
   }

   @After
   public void tearDown() throws Exception
   {
	   em.close();
   }

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
   public void testSelectMath()
   {
      JinqStream<Integer> customers = streams.streamAll(em, Customer.class)
            .select(c -> c.getDebt() + c.getSalary() * 2);
      assertEquals("SELECT A.debt + (A.salary * 2) FROM Customer A", customers.getDebugQueryString());
      List<Integer> results = customers.toList();
      assertEquals(5, results.size());
      Collections.sort(results);
      assertEquals(70, (int)results.get(0));
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

   @Test(expected=IllegalArgumentException.class)
   public void testJoinNMLink()
   {
      // TODO: Support not implemented yet
      List<Pair<Item, Supplier>> results = streams.streamAll(em, Item.class)
            .where(i -> i.getName().equals("Widgets"))
            .join(i -> JinqStream.from(i.getSuppliers()))
            .toList();
      assertEquals("SELECT A.customer FROM Sale A", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(2, results.size());
      assertEquals("Widgets", results.get(0).getOne().getName());
      assertEquals("Conglomerate", results.get(0).getTwo().getName());
      assertEquals("HW Supplier", results.get(1).getTwo().getName());
   }

   @Test
   public void testJoinEntity()
   {
      List<Pair<Item, Item>> results = streams.streamAll(em, Item.class)
            .where(i -> i.getName().equals("Widgets"))
            .join((i, source) -> source.stream(Item.class))
            .where(pair -> pair.getOne().getPurchaseprice() < pair.getTwo().getPurchaseprice())
            .toList();
      assertEquals("SELECT A, B FROM Item A, Item B WHERE A.name = 'Widgets' AND (A.purchaseprice < (B.purchaseprice))", query);
      Collections.sort(results, (c1, c2) -> c1.getTwo().getName().compareTo(c2.getTwo().getName()));
      assertEquals(2, results.size());
      assertEquals("Lawnmowers", results.get(0).getTwo().getName());
      assertEquals("Widgets", results.get(0).getOne().getName());
      assertEquals("Talent", results.get(1).getTwo().getName());
      assertEquals("Widgets", results.get(1).getOne().getName());
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
      Query q = em.createQuery("SELECT A.hasFreeShipping FROM Supplier A WHERE 1=1");  // 1=1 works as a conditional, A.hasFreeShipping is ok if you return it
      List results = q.getResultList();
//      for (Object o : results)
//         System.out.println(o);
   }
}
