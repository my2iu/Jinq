package org.jinq.jpa;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.Lineorder;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.jpa.test.entities.Supplier;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple5;
import org.junit.Test;

public class JinqJPAAggregateTest extends JinqJPATestBase
{
   @Test
   public void testCount()
   {
      long count = streams.streamAll(em, Customer.class)
            .count();
      assertEquals("SELECT COUNT(1) FROM Customer A", query);
      assertEquals(5, count);
   }
   
   @Test
   public void testCountWhere()
   {
      long count = streams.streamAll(em, Customer.class)
            .where(c -> c.getCountry().equals("UK") )
            .count();
      assertEquals("SELECT COUNT(1) FROM Customer A WHERE A.country = 'UK'", query);
      assertEquals(1, count);
   }

   @Test
   public void testMax()
   {
      assertEquals(BigInteger.valueOf(11000), 
            streams.streamAll(em, Lineorder.class)
                  .max(lo -> lo.getTransactionConfirmation()));
      assertEquals("SELECT MAX(A.transactionConfirmation) FROM Lineorder A", query);
   }
   
   @Test
   public void testMin()
   {
      Date minDate = streams.streamAll(em, Sale.class)
            .where(s -> s.getCustomer().getName().equals("Dave"))
            .select(s -> s.getDate())
            .getOnlyValue();
      Date date = streams.streamAll(em, Sale.class)
            .min(s -> s.getDate());
      assertEquals(minDate, date);
      assertEquals("SELECT MIN(A.date) FROM Sale A", query);
   }

   @Test
   public void testAvg()
   {
      assertEquals(512, streams.streamAll(em, Customer.class)
            .avg(c -> c.getSalary() * 2), 0.001);
      assertEquals("SELECT AVG(A.salary * 2) FROM Customer A", query);
   }

   @Test
   public void testSum()
   {
      assertEquals(10001500l, (long)streams.streamAll(em, Supplier.class)
            .sumLong(s -> s.getRevenue()));
      assertEquals("SELECT SUM(A.revenue) FROM Supplier A", query);
      
      assertEquals(1117.0, (double)streams.streamAll(em, Item.class)
            .sumDouble(i -> i.getSaleprice()), 0.001);
      assertEquals("SELECT SUM(A.saleprice) FROM Item A", query);
      
      assertEquals(new BigDecimal(2467), streams.streamAll(em, Lineorder.class)
            .sumBigDecimal(lo -> lo.getTotal()));
      assertEquals("SELECT SUM(A.total) FROM Lineorder A", query);

      assertEquals(BigInteger.valueOf(66000l), streams.streamAll(em, Lineorder.class)
            .sumBigInteger(lo -> lo.getTransactionConfirmation()));
      assertEquals("SELECT SUM(A.transactionConfirmation) FROM Lineorder A", query);
   }
   
   @Test
   public void testSumInteger()
   {
      // Sum of integers is a long
      assertEquals(1280, (long)streams.streamAll(em, Customer.class)
            .sumInteger(s -> s.getSalary()));
      assertEquals("SELECT SUM(A.salary) FROM Customer A", query);
   }
   
   @Test
   public void testSumExpression()
   {
      // Sum of integers is a long
      assertEquals(205300, (long)streams.streamAll(em, Customer.class)
            .sumInteger(c -> c.getSalary() * c.getDebt()));
      assertEquals("SELECT SUM(A.salary * A.debt) FROM Customer A", query);
   }

   @Test
   public void testSumJoinCast()
   {
      assertEquals(10466.0, (double)streams.streamAll(em, Lineorder.class)
            .sumDouble(lo -> lo.getQuantity() * lo.getItem().getSaleprice()), 0.001);
      assertEquals("SELECT SUM(A.quantity * A.item.saleprice) FROM Lineorder A", query);
   }
   
   @Test
   public void testMultiAggregate()
   {
      assertEquals(new Pair<>(1280l, 256.0), 
            streams.streamAll(em, Customer.class)
               .aggregate(stream -> stream.sumInteger(c -> c.getSalary()),
                  stream -> stream.avg(c -> c.getSalary())));
      assertEquals("SELECT SUM(A.salary), AVG(A.salary) FROM Customer A", query);
      
      assertEquals(new Tuple3<>(5l, 30, 500), 
            streams.streamAll(em, Customer.class)
               .aggregate(stream -> stream.count(),
                  stream -> stream.min(c -> c.getSalary()),
                  stream -> stream.max(c -> c.getSalary())));
      assertEquals("SELECT COUNT(1), MIN(A.salary), MAX(A.salary) FROM Customer A", query);
   }

   @Test
   public void testMultiAggregateNoAggregate()
   {
      assertEquals(new Tuple3<>(5l, 30, 500), 
            streams.streamAll(em, Customer.class)
               .aggregate(stream -> stream.count(),
                  stream -> 30,
                  stream -> 500));
      assertEquals("SELECT COUNT(1), 30, 500 FROM Customer A", query);
   }

   @Test
   public void testMultiAggregateTuple5()
   {
      assertEquals(new Tuple5<>(5l, 30, 500, 5l, 20), 
            streams.streamAll(em, Customer.class)
               .aggregate(stream -> stream.count(),
                  stream -> stream.min(c -> c.getSalary()),
                  stream -> stream.max(c -> c.getSalary()),
                  stream -> stream.count(),
                  stream -> 20));
      assertEquals("SELECT COUNT(1), MIN(A.salary), MAX(A.salary), COUNT(1), 20 FROM Customer A", query);
   }

   @Test
   public void testMultiAggregateParameters()
   {
      int param = 1;
      assertEquals(new Pair<>(1285l, 257.0), 
            streams.streamAll(em, Customer.class)
               .aggregate(
                  stream -> stream.sumInteger(c -> c.getSalary() + param),
                  stream -> param + stream.avg(c -> c.getSalary())));
      assertEquals("SELECT SUM(A.salary + :param0), :param1 + AVG(A.salary) FROM Customer A", query);
   }

   @Test
   public void testGroup()
   {
    List<Tuple3<String, Long, Integer>> results =
          streams.streamAll(em, Customer.class)
          .group(c -> c.getCountry(),
                (country, stream) -> stream.count(),
                (country, stream) -> (Integer)stream.min(c -> c.getSalary()))
          .toList();
    results.sort((a, b) -> a.getOne().compareTo(b.getOne()));
    assertEquals(4, results.size());
    assertEquals(1, (long)results.get(0).getTwo());
    assertEquals("Canada", results.get(0).getOne());
    assertEquals(new Tuple3<>("Switzerland", 2l, 200), results.get(1));
    assertEquals("SELECT A.country, COUNT(1), MIN(A.salary) FROM Customer A GROUP BY A.country", query);
   }
   
   @Test
   public void testGroupSortLimit()
   {
    List<Tuple3<String, Long, Integer>> results =
          streams.streamAll(em, Customer.class)
          .group(c -> c.getCountry(),
                (country, stream) -> stream.count(),
                (country, stream) -> (Integer)stream.min(c -> c.getSalary()))
          .sortedBy(g -> g.getOne())
          .skip(1)
          .limit(1)
          .toList();
    assertEquals(1, results.size());
    assertEquals(new Tuple3<>("Switzerland", 2l, 200), results.get(0));
    assertEquals("SELECT A.country, COUNT(1), MIN(A.salary) FROM Customer A GROUP BY A.country ORDER BY A.country ASC", query);
   }
   
   @Test 
   public void testGroupByLinkEntity()
   {
      List<Pair<Customer, Long>> results = 
            streams.streamAll(em, Sale.class)
                  .group(s -> s.getCustomer(), 
                        (c, stream) -> stream.count())
                  .toList();
      results.sort(Comparator.<Pair<Customer, Long>, Long>comparing(group -> group.getTwo())
            .thenComparing(group -> group.getOne().getName()));
      assertEquals(4, results.size());
      assertEquals("Dave", results.get(0).getOne().getName());
      assertEquals(2, (long)results.get(2).getTwo());
      assertEquals("Alice", results.get(2).getOne().getName());
      assertEquals("SELECT A.customer, COUNT(1) FROM Sale A GROUP BY A.customer", query);
   }
   
   @Test 
   public void testGroupByMixKeyAggregate()
   {
      List<Pair<String, Long>> results = 
            streams.streamAll(em, Sale.class)
                  .group(s -> new Pair<String, Integer>(s.getCustomer().getName(), s.getCustomer().getSalary()), 
                        (c, stream) -> c.getTwo() + stream.count())
                  .select(group -> new Pair<>(group.getOne().getOne(), group.getTwo()))
                  .toList();
      results.sort(Comparator.comparing(group -> group.getOne()));
      assertEquals(new Pair<>("Alice", 202l),
            results.get(0));
      assertEquals("SELECT A.customer.name, A.customer.salary + COUNT(1) FROM Sale A GROUP BY A.customer.name, A.customer.salary", query);
   }

   @Test 
   public void testGroupByHaving()
   {
      List<Pair<String, Long>> results = 
            streams.streamAll(em, Lineorder.class)
                  .where(lo -> "Screws".equals(lo.getItem().getName()))
                  .select(lo -> lo.getSale())
                  .group(s -> new Pair<String, Integer>(s.getCustomer().getName(), s.getCustomer().getSalary()), 
                        (c, stream) -> c.getTwo() + stream.count())
                  .where(group -> group.getTwo() < 220)
                  .select(group -> new Pair<>(group.getOne().getOne(), group.getTwo()))
                  .sortedBy(group -> group.getOne())
                  .toList();
      assertEquals(2, results.size());
      assertEquals(new Pair<>("Alice", 201l),
            results.get(0));
      assertEquals("SELECT A.sale.customer.name, A.sale.customer.salary + COUNT(1) FROM Lineorder A WHERE 'Screws' = A.item.name GROUP BY A.sale.customer.name, A.sale.customer.salary HAVING A.sale.customer.salary + COUNT(1) < 220 ORDER BY A.sale.customer.name ASC", query);
   }

   @Test(expected=IllegalArgumentException.class)
   public void testSortGroupByFail()
   {
      // Cannot do a group by after a sort
      streams.streamAll(em, Customer.class)
            .sortedBy(c -> c.getName())
            .group(c -> c.getCountry(), 
                  (c, stream) -> stream.count())
            .toList();
      assertEquals("SELECT A.country, COUNT(1), MIN(A.salary) FROM Customer A GROUP BY A.country ORDER BY A.country", query);
   }
}
