package org.jinq.jpa;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.Lineorder;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.jpa.test.entities.Supplier;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.junit.Assert;
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
   public void testWhereOrNull()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
           .where((c) -> c.getCountry().equals("UK") || c.getCountry() == null);
      assertEquals("SELECT A FROM Customer A WHERE A.country = 'UK' OR A.country IS NULL", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Dave", results.get(0).getName());
   }

   @Test
   public void testWhereOrChain()
   {
      List<String> customers = streams.streamAll(em, Customer.class)
           .where((c) -> c.getCountry().equals("UK") || c.getCountry() == null || c.getName().equals("Bob"))
           .select(c -> c.getName())
           .toList();
      assertEquals("SELECT A.name FROM Customer A WHERE A.country = 'UK' OR A.country IS NULL OR A.name = 'Bob'", query);
      assertEquals(2, customers.size());
      Assert.assertTrue(customers.contains("Dave"));
      Assert.assertTrue(customers.contains("Bob"));
   }

   @Test
   public void testWhereOrChainWithAnd()
   {
      List<String> customers = streams.streamAll(em, Customer.class)
            .where((c) -> c.getCountry().equals("UK") || c.getCountry() == null || (c.getName().equals("Bob") && c.getCountry().equals("US")) || c.getCountry().equals("Switzerland"))
            .select(c -> c.getName())
            .toList();
      assertEquals("SELECT A.name FROM Customer A WHERE A.country = 'UK' OR A.country IS NULL OR A.name <> 'Bob' AND A.country = 'Switzerland' OR A.name = 'Bob' AND A.country = 'US' OR A.name = 'Bob' AND A.country <> 'US' AND A.country = 'Switzerland'", query);
      assertEquals(3, customers.size());
      Assert.assertTrue(customers.contains("Alice"));
      Assert.assertTrue(customers.contains("Dave"));
      Assert.assertTrue(customers.contains("Bob"));
   }

   @Test
   public void testWhereOrChainPrecededByAnd()
   {
      List<String> customers = streams.streamAll(em, Customer.class)
            .where((c) -> (c.getCountry().equals("UK") && c.getName().equals("Dave")) || c.getName().equals("Bob"))
            .select(c -> c.getName())
            .toList();
      assertEquals("SELECT A.name FROM Customer A WHERE A.country <> 'UK' AND A.name = 'Bob' OR A.country = 'UK' AND A.name = 'Dave' OR A.country = 'UK' AND A.name <> 'Dave' AND A.name = 'Bob'", query);
      assertEquals(2, customers.size());
      Assert.assertTrue(customers.contains("Dave"));
      Assert.assertTrue(customers.contains("Bob"));
   }

   @Test
   public void testWhereAndOr()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
           .where((c) -> (c.getCountry().equals("UK") || c.getCountry().equals("US")) && (c.getName().equals("Alice") || c.getName().equals("Dave")));
      assertEquals("SELECT A FROM Customer A WHERE A.country = 'UK' AND A.name = 'Alice' OR A.country = 'UK' AND A.name <> 'Alice' AND A.name = 'Dave' OR A.country <> 'UK' AND A.country = 'US' AND A.name = 'Alice' OR A.country <> 'UK' AND A.country = 'US' AND A.name <> 'Alice' AND A.name = 'Dave'", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Dave", results.get(0).getName());
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

   @Test
   public void testWhereObjectEquals()
   {
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
            .where((c) -> Objects.equals(c.getCountry(), "UK"));
      assertEquals("SELECT A FROM Customer A WHERE A.country = 'UK'", customers.getDebugQueryString());
      List<Customer> results = customers.toList();
      assertEquals(1, results.size());
      assertEquals("Dave", results.get(0).getName());
   }

   @Test
   public void testEntityEquals()
   {
      Item widgets = streams.streamAll(em, Item.class).where(i -> i.getName().equals("Widgets")).getOnlyValue();
      List<Lineorder> orders = streams.streamAll(em, Lineorder.class)
            .setHint("isAllEqualsSafe", true)
            .where(lo -> lo.getItem().equals(widgets))
            .toList();
      assertEquals("SELECT A FROM Lineorder A WHERE A.item = :param0", query);
      assertEquals(3, orders.size());
   }

   @Test(expected=IllegalArgumentException.class)
   public void testEntityEqualsOff()
   {
      Item widgets = streams.streamAll(em, Item.class).where(i -> i.getName().equals("Widgets")).getOnlyValue();
      List<Lineorder> orders = streams.streamAll(em, Lineorder.class)
            .setHint("isAllEqualsSafe", false)
            .where(lo -> lo.getItem().equals(widgets))
            .toList();
      assertEquals("SELECT A FROM Lineorder A WHERE A.item = :param0", query);
      assertEquals(3, orders.size());
   }
}
