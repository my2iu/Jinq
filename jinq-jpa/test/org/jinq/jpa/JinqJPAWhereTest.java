package org.jinq.jpa;

import static org.junit.Assert.assertEquals;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JinqJPAWhereTest
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
   public void testWhere()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
    		  .where((c) -> c.getCountry().equals("UK"));
      assertEquals("SELECT A FROM Customer A WHERE A.country = 'UK'", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Dave", results.get(0).getName());
   }

   @Test
   public void testWherePaths()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
           .where((c) -> c.getCountry().equals("UK") ? c.getName().equals("Bob") : c.getName().equals("Alice"));
      assertEquals("SELECT A FROM Customer A WHERE A.name = 'Alice' AND (A.country <> 'UK') OR (A.name = 'Bob' AND (A.country = 'UK'))", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Alice", results.get(0).getName());
   }

   @Test
   public void testWhereIntegerComparison()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
           .where(c -> c.getDebt() < 90);
      assertEquals("SELECT A FROM Customer A WHERE A.debt < 90", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Eve", results.get(0).getName());
   }

   @Test
   public void testWhereChained()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
           .where(c -> c.getCountry().equals("Switzerland"))
           .where(c -> c.getName().equals("Bob"));
      assertEquals("SELECT A FROM Customer A WHERE A.country = 'Switzerland' AND (A.name = 'Bob')", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Bob", results.get(0).getName());
   }

   @Test
   public void testWhereParameter()
   {
      int param = 90;
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
           .where(c -> c.getDebt() < param);
      assertEquals("SELECT A FROM Customer A WHERE A.debt < :param0", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Eve", results.get(0).getName());
   }

   @Test
   public void testWhereParameterChainedString()
   {
      String param = "UK";
      JinqStream<String> customers = streams.streamAll(em, Customer.class)
            .select(c -> new Pair<String, String>(c.getName(), c.getCountry()))
            .where(p -> p.getTwo().equals(param))
            .select(p -> p.getOne());
      assertEquals("SELECT A.name FROM Customer A WHERE A.country = :param0", customers.getDebugQueryString());
      List<String> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Dave", results.get(0));
   }

   @Test
   public void testWhereParameters()
   {
      int paramLower = 150;
      int paramUpper = 250;
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
           .where(c -> c.getDebt() > paramLower && c.getDebt() < paramUpper);
      assertEquals("SELECT A FROM Customer A WHERE A.debt > :param0 AND (A.debt < :param1)", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Bob", results.get(0).getName());
   }
   
   @Test
   public void testWhereN1Link()
   {
      JinqStream<Sale> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getCustomer().getName().equals("Alice"));
      assertEquals("SELECT A FROM Sale A WHERE A.customer.name = 'Alice'", sales.getDebugQueryString());
      List<Sale> results = sales.toList();
      assertEquals(2, results.size());
      assertEquals("Alice", results.get(0).getCustomer().getName());
   }
   
   @Test
   public void testWhereN1Links()
   {
      JinqStream<Pair<String, Date>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getCustomer().getCountry().equals("Switzerland"))
            .where(s -> s.getCustomer().getDebt() < 150)
            .select(s -> new Pair<>(s.getCustomer().getName(), s.getDate()));
      assertEquals("SELECT A.customer.name, A.date FROM Sale A WHERE A.customer.country = 'Switzerland' AND (A.customer.debt < 150)", sales.getDebugQueryString());
      List<Pair<String, Date>> results = sales.toList();
      assertEquals(2, results.size());
      assertEquals("Alice", results.get(0).getOne());
   }

}
