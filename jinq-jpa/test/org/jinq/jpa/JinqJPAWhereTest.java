package org.jinq.jpa;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.jpa.test.entities.Supplier;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.junit.Test;

public class JinqJPAWhereTest extends JinqJPATestBase
{
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
      assertEquals("SELECT A FROM Customer A WHERE A.name = 'Alice' AND A.country <> 'UK' OR A.name = 'Bob' AND A.country = 'UK'", customers.getDebugQueryString());
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
   public void testWhereNegation()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
           .where(c -> c.getDebt() > -c.getSalary() + 1)
           .toList();
      assertEquals("SELECT A FROM Customer A WHERE A.debt > - A.salary + 1", query);
      assertEquals(5, customers.size());
   }

   @Test
   public void testWhereNot()
   {
      List<Supplier> suppliers = streams.streamAll(em, Supplier.class)
           .where(s -> !s.getHasFreeShipping())
           .where(s -> !(s.getName().equals("Conglomerate") || s.getName().equals("Talent Agency")))
           .toList();
      assertEquals("SELECT A FROM Supplier A WHERE NOT A.hasFreeShipping = TRUE AND (A.name <> 'Conglomerate' AND A.name <> 'Talent Agency')", query);
      assertEquals(1, suppliers.size());
      assertEquals("HW Supplier", suppliers.get(0).getName());
   }

   @Test
   public void testWhereChained()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
           .where(c -> c.getCountry().equals("Switzerland"))
           .where(c -> c.getName().equals("Bob"));
      assertEquals("SELECT A FROM Customer A WHERE A.country = 'Switzerland' AND A.name = 'Bob'", customers.getDebugQueryString());
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
      assertEquals("SELECT A FROM Customer A WHERE A.debt > :param0 AND A.debt < :param1", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Bob", results.get(0).getName());
   }
   
   @Test
   public void testWhereJPQLIn()
   {
       List<String> names = Arrays.asList("Bob", "Alice", "Dave");
       JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
	           .where(c -> JPQL.in(c.getName(), names));
       assertEquals("SELECT A FROM Customer A WHERE A.name IN :param0", customers.getDebugQueryString());
       List<Customer> results = customers.toList();
       assertEquals(3, results.size());
   }
   
   @Test
   public void testWhereContains()
   {
       Set<String> names = new HashSet<>(Arrays.asList("Bob", "Alice", "Dave"));
       JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
	           .where(c -> names.contains(c.getName()));
       assertEquals("SELECT A FROM Customer A WHERE A.name IN :param0", customers.getDebugQueryString());
       List<Customer> results = customers.toList();
       assertEquals(3, results.size());
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
      assertEquals("SELECT A.customer.name, A.date FROM Sale A WHERE A.customer.country = 'Switzerland' AND A.customer.debt < 150", sales.getDebugQueryString());
      List<Pair<String, Date>> results = sales.toList();
      assertEquals(2, results.size());
      assertEquals("Alice", results.get(0).getOne());
   }

}
