package org.jinq.hibernate;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.exception.SQLGrammarException;
import org.hibernate.hql.internal.ast.QuerySyntaxException;
import org.jinq.hibernate.test.entities.Customer;
import org.jinq.hibernate.test.entities.Item;
import org.jinq.hibernate.test.entities.Lineorder;
import org.jinq.hibernate.test.entities.Sale;
import org.jinq.hibernate.test.entities.Supplier;
import org.jinq.jpa.JPQL;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple5;
import org.jinq.tuples.Tuple8;
import org.junit.Assert;
import org.junit.Test;

public class JinqJPAAggregateTest extends JinqJPATestBase
{
   @Test
   public void testCount()
   {
      long count = streams.streamAll(em, Customer.class)
            .count();
      assertEquals("SELECT COUNT(A) FROM org.jinq.hibernate.test.entities.Customer A", query);
      assertEquals(5, count);
   }

   @Test
   public void testCountWhere()
   {
      long count = streams.streamAll(em, Customer.class)
            .where(c -> c.getCountry().equals("UK") )
            .count();
      assertEquals("SELECT COUNT(A) FROM org.jinq.hibernate.test.entities.Customer A WHERE A.country = 'UK'", query);
      assertEquals(1, count);
   }

   @Test
   public void testCountMultipleFields()
   {
      long count = streams.streamAll(em, Customer.class)
            .select(c -> new Pair<>(c.getName(), c.getCountry()))
            .count();
      assertEquals("SELECT COUNT(1) FROM org.jinq.hibernate.test.entities.Customer A", query);
      assertEquals(5, count);
   }

   @Test
   public void testCountSorted()
   {
      // Can count a sorted stream
      long count = streams.streamAll(em, Customer.class)
            .where(c -> c.getCountry().equals("UK") )
            .sortedBy(c -> c.getName())
            .count();
      assertEquals("SELECT COUNT(A) FROM org.jinq.hibernate.test.entities.Customer A WHERE A.country = 'UK'", query);
      assertEquals(1, count);
   }

   @Test(expected=IllegalArgumentException.class)
   public void testCountSortedLimit()
   {
      // Cannot count a limited stream
      long count = streams.streamAll(em, Customer.class)
            .where(c -> c.getCountry().equals("UK") )
            .sortedBy(c -> c.getName())
            .limit(2)
            .count();
      assertEquals("SELECT COUNT(A) FROM org.jinq.hibernate.test.entities.Customer A WHERE A.country = 'UK'", query);
      assertEquals(1, count);
   }

   @Test(expected=IllegalArgumentException.class)
   public void testCountSortedInSubquery()
   {
      // Cannot count a sorted stream in a subquery
      long count = streams.streamAll(em, Customer.class)
            .where(c -> JinqStream.from(c.getSales()).sortedBy(s -> s.getDate()).count() > 1 )
            .sortedBy(c -> c.getName())
            .count();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE (SELECT COUNT(B) FROM A.sales B) > 1", query);
      assertEquals(2, count);
   }

   @Test(expected=IllegalArgumentException.class)
   public void testCountSortedInGroup()
   {
      // Cannot count a sorted stream in a group by
      List<Pair<String, Long>> customers = streams.streamAll(em, Customer.class)
            .group(c -> c.getCountry(), (country, c) -> c.sortedBy(cust -> cust.getName()).count())
            .toList();
      assertEquals("SELECT A.country, COUNT(A) FROM org.jinq.hibernate.test.entities.Customer A GROUP BY A.country", query);
      assertEquals(4, customers.size());
   }

   @Test
   public void testMax()
   {
      assertEquals(BigInteger.valueOf(11000), 
            streams.streamAll(em, Lineorder.class)
                  .max(lo -> lo.getTransactionConfirmation()));
      assertEquals("SELECT MAX(A.transactionConfirmation) FROM org.jinq.hibernate.test.entities.Lineorder A", query);
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
      assertEquals("SELECT MIN(A.date) FROM org.jinq.hibernate.test.entities.Sale A", query);
   }

   @Test
   public void testAvg()
   {
      assertEquals(512, streams.streamAll(em, Customer.class)
            .avg(c -> c.getSalary() * 2), 0.001);
      assertEquals("SELECT AVG(A.salary * 2) FROM org.jinq.hibernate.test.entities.Customer A", query);
   }

   @Test(expected=AssertionError.class)  // Hibernate doesn't do numeric promotion properly
   public void testSum()
   {
      assertEquals(10001500l, (long)streams.streamAll(em, Supplier.class)
            .sumLong(s -> s.getRevenue()));
      assertEquals("SELECT SUM(A.revenue) FROM org.jinq.hibernate.test.entities.Supplier A", query);
      
      assertEquals(1117.0, (double)streams.streamAll(em, Item.class)
            .sumDouble(i -> i.getSaleprice()), 0.001);
      assertEquals("SELECT SUM(A.saleprice) FROM org.jinq.hibernate.test.entities.Item A", query);

      assertEquals(0.0, (double)streams.streamAll(em, Supplier.class)
            .sumDouble(s -> 0.0 + s.getPaymentDiscount()), 0.001);  // sum of float values gets a double?
      assertEquals("SELECT SUM(0.0 + A.paymentDiscount) FROM Supplier A", query);

      assertEquals(new BigDecimal(2467), streams.streamAll(em, Lineorder.class)
            .sumBigDecimal(lo -> lo.getTotal()));
      assertEquals("SELECT SUM(A.total) FROM org.jinq.hibernate.test.entities.Lineorder A", query);

      assertEquals(BigInteger.valueOf(66000l), streams.streamAll(em, Lineorder.class)
            .sumBigInteger(lo -> lo.getTransactionConfirmation()));
      assertEquals("SELECT SUM(A.transactionConfirmation) FROM org.jinq.hibernate.test.entities.Lineorder A", query);
   }
   
   @Test
   public void testSumInteger()
   {
      // Sum of integers is a long
      assertEquals(1280, (long)streams.streamAll(em, Customer.class)
            .sumInteger(s -> s.getSalary()));
      assertEquals("SELECT SUM(A.salary) FROM org.jinq.hibernate.test.entities.Customer A", query);
   }
   
   @Test
   public void testSumExpression()
   {
      // Sum of integers is a long
      assertEquals(205300, (long)streams.streamAll(em, Customer.class)
            .sumInteger(c -> c.getSalary() * c.getDebt()));
      assertEquals("SELECT SUM(A.salary * A.debt) FROM org.jinq.hibernate.test.entities.Customer A", query);
   }

   @Test
   public void testSumJoinCast()
   {
      assertEquals(10466.0, (double)streams.streamAll(em, Lineorder.class)
            .sumDouble(lo -> lo.getQuantity() * lo.getItem().getSaleprice()), 0.001);
      assertEquals("SELECT SUM(A.quantity * A.item.saleprice) FROM org.jinq.hibernate.test.entities.Lineorder A", query);
   }
   
   public void testSumCase()
   {
      // EclipseLink should be returning a Long, since it's a sum of integers, but it's returning
      // an integer instead.
      assertEquals(1, (long)streams.streamAll(em, Supplier.class)
            .sumInteger(s -> s.getHasFreeShipping() ? 1 : 0));
      assertEquals("SELECT SUM(CASE WHEN A.hasFreeShipping = TRUE THEN 1 ELSE 0 END) FROM org.jinq.hibernate.test.entities.Supplier A", query);
   }

   @Test
   public void testMultiAggregate()
   {
      assertEquals(new Pair<>(1280l, 256.0), 
            streams.streamAll(em, Customer.class)
               .aggregate(stream -> stream.sumInteger(c -> c.getSalary()),
                  stream -> stream.avg(c -> c.getSalary())));
      assertEquals("SELECT SUM(A.salary), AVG(A.salary) FROM org.jinq.hibernate.test.entities.Customer A", query);
      
      assertEquals(new Tuple3<>(5l, 30, 500), 
            streams.streamAll(em, Customer.class)
               .aggregate(stream -> stream.count(),
                  stream -> stream.min(c -> c.getSalary()),
                  stream -> stream.max(c -> c.getSalary())));
      assertEquals("SELECT COUNT(A), MIN(A.salary), MAX(A.salary) FROM org.jinq.hibernate.test.entities.Customer A", query);
   }

   @Test
   public void testMultiAggregateNoAggregate()
   {
      assertEquals(new Tuple3<>(5l, 30, 500), 
            streams.streamAll(em, Customer.class)
               .aggregate(stream -> stream.count(),
                  stream -> 30,
                  stream -> 500));
      assertEquals("SELECT COUNT(A), 30, 500 FROM org.jinq.hibernate.test.entities.Customer A", query);
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
      assertEquals("SELECT COUNT(A), MIN(A.salary), MAX(A.salary), COUNT(A), 20 FROM org.jinq.hibernate.test.entities.Customer A", query);
   }

   @Test(expected=ClassCastException.class)  // Hibernate is not doing numeric promotion properly
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

   @Test(expected=IllegalArgumentException.class) // Hibernate can't handle COUNT(DISTINCT)
   public void testMultiAggregateWithDistinct()
   {
      // Derby doesn't allow for more than one aggregation of distinct things at the same time,
      // so we'll break the test up into two cases.
      assertEquals(new Pair<>(5l, 710l), 
            streams.streamAll(em, Customer.class)
               .aggregate(
                  stream -> stream.distinct().count(),
                  stream -> stream.select(c -> c.getDebt()).sumInteger(s -> s)));
      assertEquals("SELECT COUNT(DISTINCT A), SUM(A.debt) FROM org.jinq.hibernate.test.entities.Customer A", query);

      assertEquals(new Pair<>(5l, 610l), 
            streams.streamAll(em, Customer.class)
               .aggregate(
                  stream -> stream.count(),
                  stream -> stream.select(c -> c.getDebt()).distinct().sumInteger(s -> s)));
      assertEquals("SELECT COUNT(A), SUM(DISTINCT A.debt) FROM org.jinq.hibernate.test.entities.Customer A", query);
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void testMultiAggregateParametersWithDistinctDisallowed()
   {
      // You can only aggregate a distinct stream if you pass the contents of the stream directly to the aggregation function.
      assertEquals(610l, 
            (long)streams.streamAll(em, Customer.class)
                  .select(c -> c.getDebt())
                  .distinct()
                  .sumInteger(s -> s + 1));
   }

   @Test
   public void testSelectDistinct()
   {
      List<String> itemsSold = streams.streamAll(em, Lineorder.class)
            .select(lo -> lo.getItem().getName())
            .distinct()
            .sortedBy(name -> name)
            .toList();
      assertEquals("SELECT DISTINCT A.item.name FROM org.jinq.hibernate.test.entities.Lineorder A ORDER BY A.item.name ASC", query);
      assertEquals(5, itemsSold.size());
      assertEquals("Lawnmowers", itemsSold.get(0));
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
    assertEquals("SELECT A.country, COUNT(A), MIN(A.salary) FROM org.jinq.hibernate.test.entities.Customer A GROUP BY A.country", query);
   }
   
   @Test
   public void testGroupLots()
   {
      List<Tuple8<String, Long, Long, Long, Long, Long, Long, Integer>> results =
            streams.streamAll(em, Customer.class)
            .group(c -> c.getCountry(),
                  (country, stream) -> stream.count(),
                  (country, stream) -> stream.count(),
                  (country, stream) -> stream.count(),
                  (country, stream) -> stream.count(),
                  (country, stream) -> stream.count(),
                  (country, stream) -> stream.count(),
                  (country, stream) -> (Integer)stream.min(c -> c.getSalary()))
            .toList();
      results.sort((a, b) -> a.getOne().compareTo(b.getOne()));
      assertEquals(4, results.size());
      assertEquals(1, (long)results.get(0).getTwo());
      assertEquals("Canada", results.get(0).getOne());
      assertEquals(new Tuple8<>("Switzerland", 2l, 2l, 2l, 2l, 2l, 2l, 200), results.get(1));
      assertEquals("SELECT A.country, COUNT(A), COUNT(A), COUNT(A), COUNT(A), COUNT(A), COUNT(A), MIN(A.salary) FROM org.jinq.hibernate.test.entities.Customer A GROUP BY A.country", query);
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
    assertEquals("SELECT A.country, COUNT(A), MIN(A.salary) FROM org.jinq.hibernate.test.entities.Customer A GROUP BY A.country ORDER BY A.country ASC", query);
   }
   
   @Test(expected=Exception.class)  // Hibernate generating code that Derby can't handle--TODO: investigate further later
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
      assertEquals("SELECT A.customer, COUNT(A) FROM Sale A GROUP BY A.customer", query);
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
      assertEquals("SELECT A.customer.name, A.customer.salary + COUNT(A) FROM org.jinq.hibernate.test.entities.Sale A GROUP BY A.customer.name, A.customer.salary", query);
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
      assertEquals("SELECT A.sale.customer.name, A.sale.customer.salary + COUNT(A.sale) FROM org.jinq.hibernate.test.entities.Lineorder A WHERE 'Screws' = A.item.name GROUP BY A.sale.customer.name, A.sale.customer.salary HAVING A.sale.customer.salary + COUNT(A.sale) < 220 ORDER BY A.sale.customer.name ASC", query);
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
   
   @Test
   public void testSubQueryWithSource()
   {
      List<Sale> sales = streams.streamAll(em, Sale.class)
            .where( (s, source) -> source.stream(Sale.class).max(ss -> ss.getSaleid()) == s.getSaleid())
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Sale A WHERE (SELECT MAX(B.saleid) FROM org.jinq.hibernate.test.entities.Sale B) = A.saleid", query);
      assertEquals(1, sales.size());
   }

   @Test
   public void testSubQueryWithNavigationalLink()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .where( c -> JinqStream.from(c.getSales()).count() > 1)
            .sortedBy( c -> c.getName() )
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE (SELECT COUNT(B) FROM A.sales B) > 1 ORDER BY A.name ASC", query);
      assertEquals(2, customers.size());
      assertEquals("Alice", customers.get(0).getName());
      assertEquals("Carol", customers.get(1).getName());
   }

   @Test(expected=Exception.class)  // Hibernate generating code that Derby can't handle--TODO: investigate further later
   public void testSubQueryWithNavigationalLinkInSelect()
   {
      List<Pair<Customer, Long>> customers = streams.streamAll(em, Customer.class)
            .select( c -> new Pair<>(c, JinqStream.from(c.getSales()).count()))
            .sortedBy( c -> c.getOne().getName() )
            .sortedBy( c -> c.getTwo())
            .toList();
// EclipseLink on Derby is returning the result of the subquery as an integer and not a long, causing a cast problem here
//      customers.sort(Comparator.comparing(pair -> pair.getOne().getName()));
//      customers.sort(Comparator.comparing(pair -> pair.getTwo()));
      assertEquals("SELECT B, (SELECT COUNT(A) FROM B.sales A) FROM Customer B ORDER BY (SELECT COUNT(A) FROM B.sales A ASC), B.name ASC", query);
      assertEquals(5, customers.size());
// EclipseLink on Derby just isn't handling the sorting by subqueries very well, so the result doesn't
// seem to be sorted correctly
   }
   
   @Test
   public void testSubQueryWithSelectSourceAndWhere()
   {
      List<Pair<String, Object>> sales = streams.streamAll(em, Customer.class)
            .where( c -> JinqStream.from(c.getSales()).join(s -> JinqStream.from(s.getLineorders())).where(p -> p.getTwo().getItem().getName().equals("Widgets")).count() > 0)
            .select( (c, source) -> new Pair<String, Object>(c.getName(), source.stream(Customer.class).where( c2 -> c2.getSalary() > c.getSalary()).count()) )
            .sortedBy( pair -> pair.getOne())
            .toList();
      assertEquals("SELECT B.name, (SELECT COUNT(A) FROM org.jinq.hibernate.test.entities.Customer A WHERE A.salary > B.salary) FROM org.jinq.hibernate.test.entities.Customer B WHERE (SELECT COUNT(1) FROM B.sales C JOIN C.lineorders D WHERE D.item.name = 'Widgets') > 0 ORDER BY B.name ASC", query);
      assertEquals("Alice", sales.get(0).getOne());
      assertEquals("Carol", sales.get(1).getOne());
      assertEquals("Eve", sales.get(2).getOne());
      // EclipseLink returns Integers instead of Longs here for some reason
      // So we need this workaround to use Numbers instead to avoid a ClassCastException.
      assertEquals(3, ((Number)sales.get(0).getTwo()).longValue());
      assertEquals(2, ((Number)sales.get(1).getTwo()).longValue());
      assertEquals(4, ((Number)sales.get(2).getTwo()).longValue());
   }

   @Test
   public void testSubQueryFrom()
   {
      // Subqueries in FROM clauses are generally not supported in JPQL
      // (and what support there exists is usually pretty poor.)
   }

   @Test
   public void testSubQueryNoAggregation()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .where( (c, source) -> 
                  c.getDebt() < source.stream(Customer.class)
                        .where(c2 -> c2.getName().equals("Alice"))
                        .select(c2 -> c2.getDebt())
                        .getOnlyValue() )
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE A.debt < (SELECT B.debt FROM org.jinq.hibernate.test.entities.Customer B WHERE B.name = 'Alice')", query);
      assertEquals(1, customers.size());
      assertEquals("Eve", customers.get(0).getName());
   }
   
   @Test
   public void testExists()
   {
      Assert.assertTrue(streams.streamAll(em, Customer.class)
            .where((c) -> c.getCountry().equals("USA"))
            .exists());
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE A.country = 'USA'", query);
      
      Assert.assertFalse(streams.streamAll(em, Customer.class)
            .where((c) -> c.getName().equals("John"))
            .exists());
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE A.name = 'John'", query);
   }
   
   @Test
   public void testSubQueryExists()
   {
      List<String> customers = streams.streamAll(em, Customer.class)
            .where((c, db) -> db.stream(Supplier.class).where((s) -> s.getCountry().equals(c.getCountry())).exists())
            .select(c -> c.getName())
            .sortedBy(c -> c)
            .toList();
      assertEquals("SELECT A.name FROM org.jinq.hibernate.test.entities.Customer A WHERE EXISTS (SELECT B FROM org.jinq.hibernate.test.entities.Supplier B WHERE B.country = A.country) ORDER BY A.name ASC", query);
      assertEquals(4, customers.size());
      assertEquals(Arrays.asList("Alice", "Bob", "Carol", "Eve"), customers);
   }

   @Test
   public void testSubQueryNotExists()
   {
      List<String> customers = streams.streamAll(em, Customer.class)
            .where((c, db) -> !db.stream(Supplier.class).where((s) -> s.getCountry().equals(c.getCountry())).exists())
            .select(c -> c.getName())
            .sortedBy(c -> c)
            .toList();
      assertEquals("SELECT A.name FROM org.jinq.hibernate.test.entities.Customer A WHERE NOT EXISTS (SELECT B FROM org.jinq.hibernate.test.entities.Supplier B WHERE B.country = A.country) ORDER BY A.name ASC", query);
      assertEquals(1, customers.size());
      assertEquals(Arrays.asList("Dave"), customers);
   }

   @Test
   public void testSubQueryExistsNavigationalLink()
   {
      List<Pair<String, Date>> sales = streams.streamAll(em, Sale.class)
            .where((Sale s) -> 
               JinqStream.from(s.getLineorders())
                     .select((lo) -> lo.getItem())
                     .selectAllList((i) -> i.getSuppliers())
                     .where((supplier) -> supplier.getCountry().equals("USA"))
                     .exists())
            .select(s -> new Pair<>(s.getCustomer().getName(), s.getDate()))
            .toList();
      assertEquals("SELECT A.customer.name, A.date FROM org.jinq.hibernate.test.entities.Sale A WHERE EXISTS (SELECT C FROM A.lineorders B JOIN B.item.suppliers C WHERE C.country = 'USA')", query);
      assertEquals(1, sales.size());
      assertEquals("Dave", sales.get(0).getOne());
   }

   @Test
   public void testIsInSubQueryWithSelectSource()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .where((c, source) ->
                  JPQL.isIn(c.getName(), source.stream(Item.class)
                        .where(i -> i.getName().equals("Widgets"))
                        .selectAll(i -> JinqStream.from(i.getLineorders()))
                        .select(lo -> lo.getSale())
                        .join( (sale, source2) -> source2.stream(Customer.class))
                        .where( pair -> pair.getTwo().getSalary() == pair.getOne().getCreditCard().getCvv() % 10 * 100 )
                        .select(pair -> pair.getTwo().getName())
                  ))
            .sortedBy(c -> c.getName())
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE A.name IN (SELECT D.name FROM org.jinq.hibernate.test.entities.Item B JOIN B.lineorders C, org.jinq.hibernate.test.entities.Customer D WHERE B.name = 'Widgets' AND D.salary = MOD(C.sale.creditCard.cvv, 10) * 100) ORDER BY A.name ASC", query);
      assertEquals(1, customers.size());
      assertEquals("Dave", customers.get(0).getName());
   }

   @Test
   public void testIsIn()
   {
      ArrayList<String> names = new ArrayList<>();
      names.add("Alice");
      names.add("John");
      assertEquals(null, JPQL.isInList(null, names));
      assertEquals(true, JPQL.isInList("John", names));
      assertEquals(false, JPQL.isInList("Bob", names));
      names.add(null);
      assertEquals(null, JPQL.isInList("Bob", names));
      assertEquals(true, JPQL.isInList("John", names));
   }
   
   @Test
   public void testIsInStream()
   {
      List<Customer> customers = streams.streamAll(em, Customer.class)
            .where(c -> !JPQL.isIn("Widgets", JinqStream.from(c.getSales())
                  .selectAllList(s -> s.getLineorders())
                  .select(lo -> lo.getItem().getName())))
            .sortedBy(c -> c.getName())
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A WHERE NOT 'Widgets' IN (SELECT C.item.name FROM A.sales B JOIN B.lineorders C) ORDER BY A.name ASC", query);
      assertEquals(2, customers.size());
      assertEquals("Bob", customers.get(0).getName());
      assertEquals("Dave", customers.get(1).getName());
   }
}
