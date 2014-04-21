package org.jinq.jpa;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.orm.stream.JinqStream;
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
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
      streams = new JinqJPAStreamProvider(entityManagerFactory);
      createDatabase();
   }

   static Customer createCustomer(String name, String country, int debt, int salary)
   {
	   Customer c = new Customer();
	   c.setName(name);
	   c.setDebt(debt);
	   c.setSalary(salary);
	   c.setCountry(country);
	   return c;
   }
   
   static void createDatabase()
   {
	   EntityManager em = entityManagerFactory.createEntityManager();
	   em.getTransaction().begin();

	   em.persist(createCustomer("Alice", "Switzerland", 100, 200));
	   em.persist(createCustomer("Bob", "Switzerland", 200, 300));
	   em.persist(createCustomer("Carol", "USA", 300, 250));
	   em.persist(createCustomer("Dave", "UK", 100, 500));
	   em.persist(createCustomer("Eve", "Canada", 10, 30));

	   em.getTransaction().commit();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
   }

   @Before
   public void setUp() throws Exception
   {
      em = entityManagerFactory.createEntityManager();
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
   public void testWhere()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
    		  .where((c) -> c.getCountry().equals("UK"));
      assertEquals("", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Dave", results.get(0).getName());
   }
}
