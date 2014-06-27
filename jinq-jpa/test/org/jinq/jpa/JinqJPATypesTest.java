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
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.ItemType;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.jpa.test.entities.Supplier;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JinqJPATypesTest
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
   public void testString()
   {
      String val = "UK";
      List<Pair<Customer, String>> customers = streams.streamAll(em, Customer.class)
            .where(c -> c.getCountry().equals(val) || c.getName().equals("Alice"))
            .select(c -> new Pair<>(c, c.getName()))
            .toList();
      customers = customers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.name FROM Customer A WHERE A.country = :param0 OR (A.country <> :param1 AND (A.name = 'Alice'))", query);
      assertEquals(2, customers.size());
      assertEquals("Alice", customers.get(0).getTwo());
      assertEquals("Dave", customers.get(1).getTwo());
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
   public void testInteger()
   {
      int val = 5;
      List<Pair<Customer, Integer>> customers = streams.streamAll(em, Customer.class)
            .where(c -> c.getSalary() + 5 + val < 212)
            .select(c -> new Pair<>(c, c.getSalary()))
            .toList();
      customers = customers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.salary FROM Customer A WHERE A.salary + 5 + :param0 < 212", query);
      assertEquals(2, customers.size());
      assertEquals("Alice", customers.get(0).getOne().getName());
      assertEquals(200, (int)customers.get(0).getTwo());
      assertEquals("Eve", customers.get(1).getOne().getName());
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
   public void testBoolean()
   {
      boolean val = false;
      // Direct access to boolean variables in a WHERE must be converted to a comparison 
      List<Pair<Supplier, Boolean>> suppliers = streams.streamAll(em, Supplier.class)
            .where(s -> s.getHasFreeShipping())
            .select(s -> new Pair<>(s, s.getHasFreeShipping()))
            .toList();
      assertEquals("SELECT A, A.hasFreeShipping FROM Supplier A WHERE A.hasFreeShipping = TRUE", query);
      assertEquals(1, suppliers.size());
      assertEquals("Talent Agency", suppliers.get(0).getOne().getName());
      assertTrue(suppliers.get(0).getTwo());
      
      // Boolean parameters 
      suppliers = streams.streamAll(em, Supplier.class)
            .where(s -> s.getHasFreeShipping() == val)
            .select(s -> new Pair<>(s, s.getHasFreeShipping()))
            .toList();
      suppliers = suppliers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.hasFreeShipping FROM Supplier A WHERE A.hasFreeShipping = :param0", query);
      assertEquals(2, suppliers.size());
      assertEquals("Conglomerate", suppliers.get(0).getOne().getName());
      assertEquals("HW Supplier", suppliers.get(1).getOne().getName());

      // Comparisons in a SELECT must be converted to a CASE...WHEN... or something
      // TODO: Handle this case
      try {
         suppliers = streams.streamAll(em, Supplier.class)
               .where(s -> s.getHasFreeShipping())
               .select(s -> new Pair<>(s, s.getHasFreeShipping() != true))
               .toList();
         assertEquals("SELECT A, A.hasFreeShipping FROM Supplier A WHERE A.hasFreeShipping = TRUE", query);
         assertEquals(1, suppliers.size());
         assertEquals("Talent Agency", suppliers.get(0).getOne().getName());
         assertTrue(suppliers.get(0).getTwo());
      }
      catch (RuntimeException e)
      {
         // Expected: This case isn't handled yet
      }

   }
   
   @Test
   public void testEnum()
   {
      ItemType val = ItemType.OTHER;
      List<Pair<Item, ItemType>> items = streams.streamAll(em, Item.class)
            .where(i -> i.getType() == val || i.getType().equals(ItemType.BIG))
            .select(i -> new Pair<>(i, i.getType()))
            .toList();
      items = items.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.type FROM Item A WHERE A.type = :param0 OR (A.type <> :param1 AND (A.type = org.jinq.jpa.test.entities.ItemType.BIG))", query);
      assertEquals(2, items.size());
      assertEquals("Lawnmowers", items.get(0).getOne().getName());
      assertEquals("Talent", items.get(1).getOne().getName());
      assertEquals(ItemType.OTHER, items.get(1).getTwo());
   }

}
