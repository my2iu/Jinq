package org.jinq.jpa;

import static org.junit.Assert.*;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
   public void testLong()
   {
      long val = 5;
      List<Pair<Supplier, Long>> suppliers = streams.streamAll(em, Supplier.class)
            .where(s -> s.getRevenue() + 1000 + val < 10000000L)
            .select(s -> new Pair<>(s, s.getRevenue()))
            .toList();
      suppliers = suppliers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.revenue FROM Supplier A WHERE A.revenue + 1000 + :param0 < 10000000", query);
      assertEquals(2, suppliers.size());
      assertEquals("HW Supplier", suppliers.get(0).getOne().getName());
      assertEquals(500, (long)suppliers.get(0).getTwo());
      assertEquals("Talent Agency", suppliers.get(1).getOne().getName());
   }

   @Test
   public void testDouble()
   {
      double val = 1;
      List<Pair<Item, Double>> items = streams.streamAll(em, Item.class)
            .where(i -> i.getSaleprice() > i.getPurchaseprice() + val + 2)
            .select(i -> new Pair<>(i, i.getSaleprice()))
            .toList();
      items = items.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.saleprice FROM Item A WHERE A.saleprice > (A.purchaseprice + :param0 + 2.0)", query);
      assertEquals(2, items.size());
      assertEquals("Talent", items.get(0).getOne().getName());
      assertEquals("Widgets", items.get(1).getOne().getName());
      assertTrue(Math.abs((double)items.get(1).getTwo() - 10) < 0.1);
   }

   @Test
   public void testDate()
   {
      Date val = Date.from(LocalDateTime.of(2002, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
      List<Pair<Customer, Date>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getDate().before(val))
            .select(s -> new Pair<>(s.getCustomer(), s.getDate()))
            .toList();
      assertEquals("SELECT A.customer, A.date FROM Sale A WHERE A.date < :param0", query);
      assertEquals(1, sales.size());
      assertEquals("Dave", sales.get(0).getOne().getName());
      assertEquals(2001, LocalDateTime.ofInstant(sales.get(0).getTwo().toInstant(), ZoneOffset.UTC).getYear());
   }

   @Test
   public void testDateEquals()
   {
      Date val = Date.from(LocalDateTime.of(2001, 1, 1, 1, 0).toInstant(ZoneOffset.UTC));
      List<String> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getDate().equals(val))
            .select(s -> s.getCustomer().getName())
            .toList();
      assertEquals("SELECT A.customer.name FROM Sale A WHERE A.date = :param0", query);
      assertEquals(1, sales.size());
      assertEquals("Dave", sales.get(0));
   }

   @Test
   public void testCalendar()
   {
      Calendar val = Calendar.getInstance();
      val.setTime(Date.from(LocalDateTime.of(2002, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)));
      Calendar val2 = Calendar.getInstance();
      val2.setTime(Date.from(LocalDateTime.of(2003, 1, 1, 1, 0).toInstant(ZoneOffset.UTC)));
      List<Pair<Customer, Calendar>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getCalendar().before(val) || s.getCalendar().equals(val2))
            .select(s -> new Pair<>(s.getCustomer(), s.getCalendar()))
            .toList();
      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A.customer, A.calendar FROM Sale A WHERE A.calendar < :param0 OR (A.calendar >= :param1 AND (A.calendar = :param2))", query);
      assertEquals(2, sales.size());
      assertEquals("Dave", sales.get(0).getOne().getName());
      assertEquals("Carol", sales.get(1).getOne().getName());
   }

   @Test
   public void testSqlDate()
   {
      java.sql.Date val = new java.sql.Date(2002, 1, 1);
      java.sql.Date val2 = new java.sql.Date(2003, 1, 1);
      List<Pair<Customer, java.sql.Date>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getSqlDate().before(val) || s.getSqlDate().equals(val2))
            .select(s -> new Pair<>(s.getCustomer(), s.getSqlDate()))
            .toList();
      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A.customer, A.sqlDate FROM Sale A WHERE A.sqlDate < :param0 OR (A.sqlDate >= :param1 AND (A.sqlDate = :param2))", query);
      assertEquals(2, sales.size());
      assertEquals("Dave", sales.get(0).getOne().getName());
      assertEquals("Carol", sales.get(1).getOne().getName());
   }

   @Test
   public void testSqlTime()
   {
      java.sql.Time val = new java.sql.Time(6, 0, 0);
      java.sql.Time val2 = new java.sql.Time(5, 0, 0);
      List<Pair<Customer, java.sql.Time>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getSqlTime().after(val) || s.getSqlTime().equals(val2))
            .select(s -> new Pair<>(s.getCustomer(), s.getSqlTime()))
            .toList();
      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A.customer, A.sqlTime FROM Sale A WHERE A.sqlTime > :param0 OR (A.sqlTime <= :param1 AND (A.sqlTime = :param2))", query);
      assertEquals(2, sales.size());
      assertEquals("Carol", sales.get(0).getOne().getName());
      assertEquals("Alice", sales.get(1).getOne().getName());
   }

   @Test
   public void testSqlTimestamp()
   {
      java.sql.Timestamp val = new java.sql.Timestamp(2002, 1, 1, 1, 0, 0, 0);
      java.sql.Timestamp val2 = new java.sql.Timestamp(2003, 1, 1, 1, 0, 0, 0);
      List<Pair<Customer, java.sql.Timestamp>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getSqlTimestamp().before(val) || s.getSqlTimestamp().equals(val2))
            .select(s -> new Pair<>(s.getCustomer(), s.getSqlTimestamp()))
            .toList();
      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A.customer, A.sqlTimestamp FROM Sale A WHERE A.sqlTimestamp < :param0 OR (A.sqlTimestamp >= :param1 AND (A.sqlTimestamp = :param2))", query);
      assertEquals(2, sales.size());
      assertEquals("Dave", sales.get(0).getOne().getName());
      assertEquals("Carol", sales.get(1).getOne().getName());
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

   @Test
   public void testStringEscape()
   {
      JinqStream<String> customers = streams.streamAll(em, Customer.class)
            .select(c -> "I didn't know \\''");
      assertEquals("SELECT 'I didn''t know \\''''' FROM Customer A", customers.getDebugQueryString());
      List<String> results = customers.toList();
      assertEquals(5, results.size());
      assertEquals("I didn't know \\''", results.get(0));
   }

   @Test
   public void testJPQL()
   {
      // These  queries do not parse properly by JPQL:
      // Query q = em.createQuery("SELECT A FROM Customer A WHERE ((FALSE AND ((A.debt) >= 90)) OR (TRUE AND ((A.debt) < 90)))");
      // Query q = em.createQuery("SELECT A FROM Sale A WHERE (((A.customer).name) = 'Alice')");
      Query q = em.createQuery("SELECT A FROM Sale A WHERE ((A.customer.name) = 'Alice')");
      List results = q.getResultList();
//      for (Object o : results)
//         System.out.println(o);
   }
}
