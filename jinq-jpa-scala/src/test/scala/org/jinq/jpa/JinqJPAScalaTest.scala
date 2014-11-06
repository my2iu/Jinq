package org.jinq.jpa;

import org.jinq.jpa.test.entities.Customer
import org.jinq.orm.stream.scala.JinqScalaStream
import org.junit.Assert
import org.junit.Test
import javax.persistence.EntityManager
import org.jinq.jpa.test.entities.Item
import org.jinq.jpa.test.entities.Lineorder
import java.math.BigDecimal
import java.math.BigInteger
import org.jinq.jpa.test.entities.Supplier
import org.jinq.jpa.test.entities.Sale
import org.jinq.orm.stream.scala.JinqConversions._

class JinqJPAScalaTest extends JinqJPAScalaTestBase {
  private def streamAll[U](em: EntityManager, entityClass: java.lang.Class[U]): JinqScalaStream[U] = {
    JinqJPAScalaTestBase.streams.streamAll(em, entityClass);
  }

  @Test
  def testStreamEntities {
    var customers = streamAll(em, classOf[Customer])
      .toList()
      .sortBy((c) => c.getName())
    Assert.assertEquals("Alice", customers(0).getName);
    Assert.assertEquals(5, customers.length);
    Assert.assertEquals("Eve", customers(4).getName);
  }

  @Test
  def testSimpleWhere {
    var customers = streamAll(em, classOf[Customer])
      .where((c) => c.getCountry == "UK")
      .toList();
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.country IS NOT NULL AND A.country = 'UK' OR A.country IS NULL AND 'UK' IS NULL", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Dave", customers(0).getName());
  }

  @Test
  def testWhereParameters {
    val name = "UK"
    val debt = 100
    var customers = streamAll(em, classOf[Customer])
      .where((c) => c.getCountry == name && c.getDebt == debt)
      .toList();
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.country IS NOT NULL AND A.country = :param0 AND A.debt = :param1 OR A.country IS NULL AND :param2 IS NULL AND A.debt = :param3", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Dave", customers(0).getName());
  }

  @Test
  def testSelect() {
    var countries = streamAll(em, classOf[Customer])
      .select(c => c.getCountry)
      .toList;
    Assert.assertEquals("SELECT A.country FROM Customer A", query);
    Assert.assertEquals(5, countries.length);
    countries = countries.sortBy(c => c);
    Assert.assertEquals("Canada", countries(0));
  }

  @Test
  def testJoinEntity() {
    var results = streamAll(em, classOf[Item])
      .where(i => i.getName().equals("Widgets"))
      .join((i, source) => source.stream(classOf[Item]))
      .where(pair => pair._1.getPurchaseprice() < pair._2.getPurchaseprice())
      .toList();
    Assert.assertEquals("SELECT A, B FROM Item A, Item B WHERE A.name = 'Widgets' AND A.purchaseprice < B.purchaseprice", query);
    results = results.sortBy(c => c._2.getName())
    Assert.assertEquals(2, results.length);
    Assert.assertEquals("Lawnmowers", results(0)._2.getName());
    Assert.assertEquals("Widgets", results(0)._1.getName());
    Assert.assertEquals("Talent", results(1)._2.getName());
    Assert.assertEquals("Widgets", results(1)._1.getName());
  }

  @Test
  def testJoin11NMLink() {
    var results = streamAll(em, classOf[Lineorder])
      .join(lo => lo.getItem().getSuppliers())
      .where(pair => pair._1.getSale().getCustomer().getName() == "Alice")
      .toList();
    Assert.assertEquals("SELECT A, B FROM Lineorder A JOIN A.item.suppliers B WHERE A.sale.customer.name IS NOT NULL AND A.sale.customer.name = 'Alice' OR A.sale.customer.name IS NULL AND 'Alice' IS NULL", query);
    results = results.sortBy(c1 => c1._2.getName());
    Assert.assertEquals(5, results.length);
    Assert.assertEquals("Conglomerate", results(1)._2.getName());
    Assert.assertEquals("HW Supplier", results(4)._2.getName());
  }

  @Test
  def testOuterJoin() {
    var results = streamAll(em, classOf[Item])
      .where(i => i.getName().equals("Widgets"))
      .leftOuterJoin(i => i.getSuppliers())
      .toList();
    Assert.assertEquals("SELECT A, B FROM Item A LEFT OUTER JOIN A.suppliers B WHERE A.name = 'Widgets'", query);
    results = results.sortBy(c1 => c1._2.getName());
    Assert.assertEquals(2, results.length);
    Assert.assertEquals("Widgets", results(0)._1.getName());
    Assert.assertEquals("Conglomerate", results(0)._2.getName());
    Assert.assertEquals("HW Supplier", results(1)._2.getName());
  }

  //   @Test
  //   public void testStreamPages()
  //   {
  //      List<String> names = streams.streamAll(em, Customer.class)
  //            .setHint("automaticPageSize", 1)
  //            .select(c -> c.getName() )
  //            .toList();
  //      names = names.stream().sorted().collect(Collectors.toList());
  //      assertEquals(5, names.size());
  //      assertEquals("Alice", names.get(0));
  //      assertEquals("Bob", names.get(1));
  //      assertEquals("Carol", names.get(2));
  //      assertEquals("Dave", names.get(3));
  //      assertEquals("Eve", names.get(4));
  //   }
  //
  //   private static void externalMethod() {}
  //   
  //   @Test
  //   public void testExceptionOnFail()
  //   {
  //      streams.streamAll(em, Customer.class)
  //            .setHint("exceptionOnTranslationFail", false)
  //            .select(c -> {externalMethod(); return "blank";} )
  //            .toList();
  //      try {
  //         streams.streamAll(em, Customer.class)
  //               .setHint("exceptionOnTranslationFail", true)
  //               .select(c -> {externalMethod(); return "blank";} )
  //               .toList();
  //      } 
  //      catch (RuntimeException e)
  //      {
  //         // Expected
  //         return;
  //      }
  //      fail();
  //   }

  @Test
  def testJoinNMLink() {
    var results = streamAll(em, classOf[Item])
      .where(i => i.getName() == "Widgets")
      .join(i => i.getSuppliers())
      .toList();
    Assert.assertEquals("SELECT A, B FROM Item A JOIN A.suppliers B WHERE A.name IS NOT NULL AND A.name = 'Widgets' OR A.name IS NULL AND 'Widgets' IS NULL", query);
    results = results.sortBy(c1 => c1._2.getName());
    Assert.assertEquals(2, results.length);
    Assert.assertEquals("Widgets", results(0)._1.getName());
    Assert.assertEquals("Conglomerate", results(0)._2.getName());
    Assert.assertEquals("HW Supplier", results(1)._2.getName());
  }

  @Test
  def testOuterJoinChain() {
    var results = streamAll(em, classOf[Lineorder])
      .where(lo => lo.getItem().getName() == "Talent")
      .leftOuterJoin(lo => lo.getItem().getSuppliers())
      .toList();
    Assert.assertEquals("SELECT A, C FROM Lineorder A JOIN A.item B LEFT OUTER JOIN B.suppliers C WHERE A.item.name IS NOT NULL AND A.item.name = 'Talent' OR A.item.name IS NULL AND 'Talent' IS NULL", query);
    results = results.sortBy(c1 => c1._2.getName());
    Assert.assertEquals(1, results.length);
  }

  @Test
  def testOuterJoin11() {
    var results = streamAll(em, classOf[Lineorder])
      .leftOuterJoin(lo => JinqScalaStream.of(lo.getItem()))
      .where(pair => pair._2.getName() == "Talent")
      .toList();
    Assert.assertEquals("SELECT A, B FROM Lineorder A LEFT OUTER JOIN A.item B WHERE B.name IS NOT NULL AND B.name = 'Talent' OR B.name IS NULL AND 'Talent' IS NULL", query);
    results = results.sortBy(c1 => c1._2.getName());
    Assert.assertEquals(1, results.length);
  }

  //   @Test(expected=IllegalArgumentException.class)
  //   public void testOuterJoinField()
  //   {
  //      // Cannot do outer joins on normal fields. Only navigational links.
  //      List<Pair<Customer, String>> results = streams.streamAll(em, Customer.class)
  //            .leftOuterJoin(c -> JinqStream.of(c.getCountry()))
  //            .toList();
  //      assertEquals("SELECT A, B FROM Customer A LEFT OUTER JOIN A.country B", query);
  //      assertEquals(5, results.size());
  //   }

  @Test
  def testSort {
    val results = streamAll(em, classOf[Customer])
      .sortedBy(c => c.getName())
      .toList();
    Assert.assertEquals("SELECT A FROM Customer A ORDER BY A.name ASC", query);
    Assert.assertEquals(5, results.length);
    Assert.assertEquals("Alice", results(0).getName());
    Assert.assertEquals("Bob", results(1).getName());
    Assert.assertEquals("Eve", results(4).getName());
  }

  //   @Test
  //   public void testSortExpression()
  //   {
  //      List<Item> results = streams.streamAll(em, Item.class)
  //            .where(i -> i.getPurchaseprice() > 1)
  //            .sortedDescendingBy(i -> i.getSaleprice() - i.getPurchaseprice())
  //            .toList();
  //      assertEquals("SELECT A FROM Item A WHERE A.purchaseprice > 1.0 ORDER BY A.saleprice - A.purchaseprice DESC", query);
  //      assertEquals(4, results.size());
  //      assertEquals("Talent", results.get(0).getName());
  //      assertEquals("Widgets", results.get(1).getName());
  //   }

  @Test
  def testSortChained() {
    val results = streamAll(em, classOf[Customer])
      .sortedDescendingBy(c => c.getName())
      .sortedBy(c => c.getCountry())
      .toList();
    Assert.assertEquals("SELECT A FROM Customer A ORDER BY A.country ASC, A.name DESC", query);
    Assert.assertEquals(5, results.length);
    Assert.assertEquals("Eve", results(0).getName());
    Assert.assertEquals("Bob", results(1).getName());
    Assert.assertEquals("Alice", results(2).getName());
  }

  @Test
  def testLimitSkip() {
    val results = streamAll(em, classOf[Customer])
      .setHint("automaticPageSize", (1).underlying)
      .sortedBy(c => c.getName())
      .skip(1)
      .limit(2)
      .toList();
    Assert.assertEquals("SELECT A FROM Customer A ORDER BY A.name ASC", query);
    Assert.assertEquals(2, queryList.length);
    Assert.assertEquals(2, results.length);
    Assert.assertEquals("Bob", results(0).getName());
    Assert.assertEquals("Carol", results(1).getName());
  }

  @Test
  def testSkipLimit() {
    val results = streamAll(em, classOf[Customer])
      .sortedBy(c => c.getName())
      .limit(3)
      .skip(1)
      .toList();
    Assert.assertEquals("SELECT A FROM Customer A ORDER BY A.name ASC", query);
    Assert.assertEquals(1, queryList.length);
    Assert.assertEquals(2, results.length);
    Assert.assertEquals("Bob", results(0).getName());
    Assert.assertEquals("Carol", results(1).getName());
  }

  //   @Test(expected=IllegalArgumentException.class)
  //   public void testTooManyPaths()
  //   {
  //      List<Customer> results = streams.streamAll(em, Customer.class)
  //            .where(c -> (c.getName().equals("Alice") && c.getSalary() == 5)
  //                  || (c.getName().equals("Bob") && c.getSalary() == 6)
  //                  || (c.getName().equals("Dave") && c.getSalary() == 7)
  //                  || (c.getName().equals("Eve") && c.getSalary() == 8)
  //                  || (c.getName().equals("Carol") && c.getSalary() == 9)
  //                  || (c.getName().equals("Alice") && c.getSalary() == 10)
  //                  || (c.getName().equals("Bob") && c.getSalary() == 11)
  //                  || (c.getName().equals("Carol") && c.getSalary() == 12))
  //            .toList();
  //   }
  //   
  //   @Test
  //   public void testCaching()
  //   {
  //      // Ensure the base "find all customers" query is in the cache
  //      streams.streamAll(em, Customer.class);
  //      // Create a query composer for finding all customers.
  //      Optional<JPQLQuery<?>> cachedQuery = streams.cachedQueries.findCachedFindAllEntities("Customer");
  //      JPAQueryComposer<Customer> composer = JPAQueryComposer.findAllEntities(streams.metamodel, streams.cachedQueries, em, streams.hints, (JPQLQuery<Customer>)cachedQuery.get());
  //      // Apply a where restriction to it
  //      JPAQueryComposer<Customer> where1 = repeatedQuery(composer, 1);
  //      JPAQueryComposer<Customer> where2 = repeatedQuery(composer, 2);
  //      JPAQueryComposer<Customer> where3 = repeatedQuery(composer, 3);
  //      // Check that the queries have the exact same underlying query object
  //      assertTrue(where1.query == where2.query);
  //      assertTrue(where2.query == where3.query);
  //   }
  //   
  //   private JPAQueryComposer<Customer> repeatedQuery(JPAQueryComposer<Customer> composer, int param)
  //   {
  //      return composer.where(c -> c.getDebt() == param);
  //   }
  //   @Test
  //   public void testJPQLStringFunctions()
  //   {
  //      List<String> customers = streams.streamAll(em, Customer.class)
  //         .where(c -> JPQL.like(c.getName(), "A_i%ce") && c.getName().length() > c.getName().indexOf("l"))
  //         .select( c -> c.getName().toUpperCase().trim() + c.getCountry().substring(0, 1))
  //         .toList();
  //      assertEquals("SELECT CONCAT(TRIM(UPPER(A.name)), SUBSTRING(A.country, 0 + 1, 1 - 0)) FROM Customer A WHERE A.name LIKE 'A_i%ce' AND LENGTH(A.name) > LOCATE('l', A.name) - 1", query);
  //      assertEquals(1, customers.size());
  //      assertEquals("ALICES", customers.get(0));
  //   }
  //
  //   @Test
  //   public void testJPQLStringConcat()
  //   {
  //      List<String> customers = streams.streamAll(em, Customer.class)
  //         .select( c -> c.getName() + " " + c.getCountry())
  //         .sortedBy( s -> s)
  //         .toList();
  //      assertEquals("SELECT CONCAT(CONCAT(A.name, ' '), A.country) FROM Customer A ORDER BY CONCAT(CONCAT(A.name, ' '), A.country) ASC", query);
  //      assertEquals(5, customers.size());
  //      assertEquals("Alice Switzerland", customers.get(0));
  //   }
  //
  //   @Test
  //   public void testJPQLNumberFunctions()
  //   {
  //      List<Double> customers = streams.streamAll(em, Customer.class)
  //         .select( c -> Math.abs(c.getSalary() + Math.sqrt(c.getDebt())) + (c.getSalary() % c.getDebt()))
  //         .toList();
  //      assertEquals("SELECT ABS(A.salary + SQRT(A.debt)) + MOD(A.salary, A.debt) FROM Customer A", query);
  //      assertEquals(5, customers.size());
  //   }

  @Test
  def testCount() {
    val count = streamAll(em, classOf[Customer])
      .count();
    Assert.assertEquals("SELECT COUNT(A) FROM Customer A", query);
    Assert.assertEquals(5, count);
  }

  @Test
  def testCountWhere() {
    val count = streamAll(em, classOf[Customer])
      .where(c => c.getCountry().equals("UK"))
      .count();
    Assert.assertEquals("SELECT COUNT(A) FROM Customer A WHERE A.country = 'UK'", query);
    Assert.assertEquals(1, count);
  }

  @Test
  def testCountMultipleFields() {
    val count = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), c.getCountry()))
      .count();
    Assert.assertEquals("SELECT COUNT(1) FROM Customer A", query);
    Assert.assertEquals(5, count);
  }

  @Test
  def testMax() {
    Assert.assertEquals(BigInteger.valueOf(11000),
      streamAll(em, classOf[Lineorder])
        .max(lo => lo.getTransactionConfirmation()));
    Assert.assertEquals("SELECT MAX(A.transactionConfirmation) FROM Lineorder A", query);
  }

  @Test
  def testMin() {
    val minDate = streamAll(em, classOf[Sale])
      .where(s => s.getCustomer().getName().equals("Dave"))
      .select(s => s.getDate())
      .getOnlyValue();
    val date = streamAll(em, classOf[Sale])
      .min(s => s.getDate());
    Assert.assertEquals(minDate, date);
    Assert.assertEquals("SELECT MIN(A.date) FROM Sale A", query);
  }

  @Test
  def testAvg() {
    Assert.assertEquals(512, streamAll(em, classOf[Customer])
      .avg(c => c.getSalary() * 2), 0.001);
    Assert.assertEquals("SELECT AVG(A.salary * 2) FROM Customer A", query);
  }

  @Test
  def testSum() {
    Assert.assertEquals(10001500l, streamAll(em, classOf[Supplier])
      .sumLong(s => s.getRevenue()));
    Assert.assertEquals("SELECT SUM(A.revenue) FROM Supplier A", query);

    Assert.assertEquals(1117.0, streamAll(em, classOf[Item])
      .sumDouble(i => i.getSaleprice()), 0.001);
    Assert.assertEquals("SELECT SUM(A.saleprice) FROM Item A", query);

    Assert.assertEquals(new BigDecimal(2467), streamAll(em, classOf[Lineorder])
      .sumBigDecimal(lo => lo.getTotal()));
    Assert.assertEquals("SELECT SUM(A.total) FROM Lineorder A", query);

    Assert.assertEquals(BigInteger.valueOf(66000l), streamAll(em, classOf[Lineorder])
      .sumBigInteger(lo => lo.getTransactionConfirmation()));
    Assert.assertEquals("SELECT SUM(A.transactionConfirmation) FROM Lineorder A", query);
  }

  @Test
  def testSumInteger() {
    // Sum of integers is a long
    Assert.assertEquals(1280, streamAll(em, classOf[Customer])
      .sumInteger(s => s.getSalary()));
    Assert.assertEquals("SELECT SUM(A.salary) FROM Customer A", query);
  }

  @Test
  def testSumExpression() {
    // Sum of integers is a long
    Assert.assertEquals(205300, streamAll(em, classOf[Customer])
      .sumInteger(c => c.getSalary() * c.getDebt()));
    Assert.assertEquals("SELECT SUM(A.salary * A.debt) FROM Customer A", query);
  }

  @Test
  def testSumJoinCast() {
    Assert.assertEquals(10466.0, streamAll(em, classOf[Lineorder])
      .sumDouble(lo => lo.getQuantity() * lo.getItem().getSaleprice()), 0.001);
    Assert.assertEquals("SELECT SUM(A.quantity * A.item.saleprice) FROM Lineorder A", query);
  }

  //   @Test(expected=ClassCastException.class)
  //   public void testSumCase()
  //   {
  //      // EclipseLink should be returning a Long, since it's a sum of integers, but it's returning
  //      // an integer instead.
  //      assertEquals(1, (long)streams.streamAll(em, Supplier.class)
  //            .sumInteger(s -> s.getHasFreeShipping() ? 1 : 0));
  //      assertEquals("SELECT SUM(CASE WHEN A.hasFreeShipping = TRUE THEN 1 ELSE 0 END) FROM Customer A", query);
  //   }
  //
  //   @Test
  //   public void testMultiAggregate()
  //   {
  //      assertEquals(new Pair<>(1280l, 256.0), 
  //            streams.streamAll(em, Customer.class)
  //               .aggregate(stream -> stream.sumInteger(c -> c.getSalary()),
  //                  stream -> stream.avg(c -> c.getSalary())));
  //      assertEquals("SELECT SUM(A.salary), AVG(A.salary) FROM Customer A", query);
  //      
  //      assertEquals(new Tuple3<>(5l, 30, 500), 
  //            streams.streamAll(em, Customer.class)
  //               .aggregate(stream -> stream.count(),
  //                  stream -> stream.min(c -> c.getSalary()),
  //                  stream -> stream.max(c -> c.getSalary())));
  //      assertEquals("SELECT COUNT(A), MIN(A.salary), MAX(A.salary) FROM Customer A", query);
  //   }
  //
  //   @Test
  //   public void testMultiAggregateNoAggregate()
  //   {
  //      assertEquals(new Tuple3<>(5l, 30, 500), 
  //            streams.streamAll(em, Customer.class)
  //               .aggregate(stream -> stream.count(),
  //                  stream -> 30,
  //                  stream -> 500));
  //      assertEquals("SELECT COUNT(A), 30, 500 FROM Customer A", query);
  //   }
  //
  //   @Test
  //   public void testMultiAggregateTuple5()
  //   {
  //      assertEquals(new Tuple5<>(5l, 30, 500, 5l, 20), 
  //            streams.streamAll(em, Customer.class)
  //               .aggregate(stream -> stream.count(),
  //                  stream -> stream.min(c -> c.getSalary()),
  //                  stream -> stream.max(c -> c.getSalary()),
  //                  stream -> stream.count(),
  //                  stream -> 20));
  //      assertEquals("SELECT COUNT(A), MIN(A.salary), MAX(A.salary), COUNT(A), 20 FROM Customer A", query);
  //   }
  //
  //   @Test
  //   public void testMultiAggregateParameters()
  //   {
  //      int param = 1;
  //      assertEquals(new Pair<>(1285l, 257.0), 
  //            streams.streamAll(em, Customer.class)
  //               .aggregate(
  //                  stream -> stream.sumInteger(c -> c.getSalary() + param),
  //                  stream -> param + stream.avg(c -> c.getSalary())));
  //      assertEquals("SELECT SUM(A.salary + :param0), :param1 + AVG(A.salary) FROM Customer A", query);
  //   }
  //
  //   @Test
  //   public void testMultiAggregateParametersWithDistinct()
  //   {
  //      // Derby doesn't allow for more than one aggregation of distinct things at the same time,
  //      // so we'll break the test up into two cases.
  //      assertEquals(new Pair<>(5l, 710l), 
  //            streams.streamAll(em, Customer.class)
  //               .aggregate(
  //                  stream -> stream.distinct().count(),
  //                  stream -> stream.select(c -> c.getDebt()).sumInteger(s -> s)));
  //      assertEquals("SELECT COUNT(DISTINCT A), SUM(A.debt) FROM Customer A", query);
  //
  //      assertEquals(new Pair<>(5l, 610l), 
  //            streams.streamAll(em, Customer.class)
  //               .aggregate(
  //                  stream -> stream.count(),
  //                  stream -> stream.select(c -> c.getDebt()).distinct().sumInteger(s -> s)));
  //      assertEquals("SELECT COUNT(A), SUM(DISTINCT A.debt) FROM Customer A", query);
  //   }
  //   
  //   @Test(expected=IllegalArgumentException.class)
  //   public void testMultiAggregateParametersWithDistinctDisallowed()
  //   {
  //      // You can only aggregate a distinct stream if you pass the contents of the stream directly to the aggregation function.
  //      assertEquals(610l, 
  //            (long)streams.streamAll(em, Customer.class)
  //                  .select(c -> c.getDebt())
  //                  .distinct()
  //                  .sumInteger(s -> s + 1));
  //   }

  @Test
  def testSelectDistinct() {
    val itemsSold = streamAll(em, classOf[Lineorder])
      .select(lo => lo.getItem().getName())
      .distinct()
      .sortedBy(name => name)
      .toList();
    Assert.assertEquals("SELECT DISTINCT A.item.name FROM Lineorder A ORDER BY A.item.name ASC", query);
    Assert.assertEquals(5, itemsSold.length);
    Assert.assertEquals("Lawnmowers", itemsSold(0));
  }

  //   @Test
  //   public void testGroup()
  //   {
  //    List<Tuple3<String, Long, Integer>> results =
  //          streams.streamAll(em, Customer.class)
  //          .group(c -> c.getCountry(),
  //                (country, stream) -> stream.count(),
  //                (country, stream) -> (Integer)stream.min(c -> c.getSalary()))
  //          .toList();
  //    results.sort((a, b) -> a.getOne().compareTo(b.getOne()));
  //    assertEquals(4, results.size());
  //    assertEquals(1, (long)results.get(0).getTwo());
  //    assertEquals("Canada", results.get(0).getOne());
  //    assertEquals(new Tuple3<>("Switzerland", 2l, 200), results.get(1));
  //    assertEquals("SELECT A.country, COUNT(A), MIN(A.salary) FROM Customer A GROUP BY A.country", query);
  //   }
  //   
  //   @Test
  //   public void testGroupSortLimit()
  //   {
  //    List<Tuple3<String, Long, Integer>> results =
  //          streams.streamAll(em, Customer.class)
  //          .group(c -> c.getCountry(),
  //                (country, stream) -> stream.count(),
  //                (country, stream) -> (Integer)stream.min(c -> c.getSalary()))
  //          .sortedBy(g -> g.getOne())
  //          .skip(1)
  //          .limit(1)
  //          .toList();
  //    assertEquals(1, results.size());
  //    assertEquals(new Tuple3<>("Switzerland", 2l, 200), results.get(0));
  //    assertEquals("SELECT A.country, COUNT(A), MIN(A.salary) FROM Customer A GROUP BY A.country ORDER BY A.country ASC", query);
  //   }
  //   
  //   @Test 
  //   public void testGroupByLinkEntity()
  //   {
  //      List<Pair<Customer, Long>> results = 
  //            streams.streamAll(em, Sale.class)
  //                  .group(s -> s.getCustomer(), 
  //                        (c, stream) -> stream.count())
  //                  .toList();
  //      results.sort(Comparator.<Pair<Customer, Long>, Long>comparing(group -> group.getTwo())
  //            .thenComparing(group -> group.getOne().getName()));
  //      assertEquals(4, results.size());
  //      assertEquals("Dave", results.get(0).getOne().getName());
  //      assertEquals(2, (long)results.get(2).getTwo());
  //      assertEquals("Alice", results.get(2).getOne().getName());
  //      assertEquals("SELECT A.customer, COUNT(A) FROM Sale A GROUP BY A.customer", query);
  //   }
  //   
  //   @Test 
  //   public void testGroupByMixKeyAggregate()
  //   {
  //      List<Pair<String, Long>> results = 
  //            streams.streamAll(em, Sale.class)
  //                  .group(s -> new Pair<String, Integer>(s.getCustomer().getName(), s.getCustomer().getSalary()), 
  //                        (c, stream) -> c.getTwo() + stream.count())
  //                  .select(group -> new Pair<>(group.getOne().getOne(), group.getTwo()))
  //                  .toList();
  //      results.sort(Comparator.comparing(group -> group.getOne()));
  //      assertEquals(new Pair<>("Alice", 202l),
  //            results.get(0));
  //      assertEquals("SELECT A.customer.name, A.customer.salary + COUNT(A) FROM Sale A GROUP BY A.customer.name, A.customer.salary", query);
  //   }
  //
  //   @Test 
  //   public void testGroupByHaving()
  //   {
  //      List<Pair<String, Long>> results = 
  //            streams.streamAll(em, Lineorder.class)
  //                  .where(lo -> "Screws".equals(lo.getItem().getName()))
  //                  .select(lo -> lo.getSale())
  //                  .group(s -> new Pair<String, Integer>(s.getCustomer().getName(), s.getCustomer().getSalary()), 
  //                        (c, stream) -> c.getTwo() + stream.count())
  //                  .where(group -> group.getTwo() < 220)
  //                  .select(group -> new Pair<>(group.getOne().getOne(), group.getTwo()))
  //                  .sortedBy(group -> group.getOne())
  //                  .toList();
  //      assertEquals(2, results.size());
  //      assertEquals(new Pair<>("Alice", 201l),
  //            results.get(0));
  //      assertEquals("SELECT A.sale.customer.name, A.sale.customer.salary + COUNT(A.sale) FROM Lineorder A WHERE 'Screws' = A.item.name GROUP BY A.sale.customer.name, A.sale.customer.salary HAVING A.sale.customer.salary + COUNT(A.sale) < 220 ORDER BY A.sale.customer.name ASC", query);
  //   }
  //
  //   @Test(expected=IllegalArgumentException.class)
  //   public void testSortGroupByFail()
  //   {
  //      // Cannot do a group by after a sort
  //      streams.streamAll(em, Customer.class)
  //            .sortedBy(c -> c.getName())
  //            .group(c -> c.getCountry(), 
  //                  (c, stream) -> stream.count())
  //            .toList();
  //      assertEquals("SELECT A.country, COUNT(1), MIN(A.salary) FROM Customer A GROUP BY A.country ORDER BY A.country", query);
  //   }
  //   
  //   @Test
  //   public void testSubQueryWithSource()
  //   {
  //      List<Sale> sales = streams.streamAll(em, Sale.class)
  //            .where( (s, source) -> source.stream(Sale.class).max(ss -> ss.getSaleid()) == s.getSaleid())
  //            .toList();
  //      assertEquals("SELECT A FROM Sale A WHERE (SELECT MAX(B.saleid) FROM Sale B) = A.saleid", query);
  //      assertEquals(1, sales.size());
  //   }
  //
  //   @Test
  //   public void testSubQueryWithNavigationalLink()
  //   {
  //      List<Customer> customers = streams.streamAll(em, Customer.class)
  //            .where( c -> JinqStream.from(c.getSales()).count() > 1)
  //            .sortedBy( c -> c.getName() )
  //            .toList();
  //      assertEquals("SELECT A FROM Customer A WHERE (SELECT COUNT(B) FROM A.sales B) > 1 ORDER BY A.name ASC", query);
  //      assertEquals(2, customers.size());
  //      assertEquals("Alice", customers.get(0).getName());
  //      assertEquals("Carol", customers.get(1).getName());
  //   }
  //
  //   @Test
  //   public void testSubQueryWithNavigationalLinkInSelect()
  //   {
  //      List<Pair<Customer, Long>> customers = streams.streamAll(em, Customer.class)
  //            .select( c -> new Pair<>(c, JinqStream.from(c.getSales()).count()))
  //            .sortedBy( c -> c.getOne().getName() )
  //            .sortedBy( c -> c.getTwo())
  //            .toList();
  //// EclipseLink on Derby is returning the result of the subquery as an integer and not a long, causing a cast problem here
  ////      customers.sort(Comparator.comparing(pair -> pair.getOne().getName()));
  ////      customers.sort(Comparator.comparing(pair -> pair.getTwo()));
  //      assertEquals("SELECT B, (SELECT COUNT(A) FROM B.sales A) FROM Customer B ORDER BY (SELECT COUNT(A) FROM B.sales A ASC), B.name ASC", query);
  //      assertEquals(5, customers.size());
  //// EclipseLink on Derby just isn't handling the sorting by subqueries very well, so the result doesn't
  //// seem to be sorted correctly
  //   }
  //   
  //   @Test
  //   public void testSubQueryWithSelectSourceAndWhere()
  //   {
  //      List<Pair<String, Object>> sales = streams.streamAll(em, Customer.class)
  //            .where( c -> JinqStream.from(c.getSales()).join(s -> JinqStream.from(s.getLineorders())).where(p -> p.getTwo().getItem().getName().equals("Widgets")).count() > 0)
  //            .select( (c, source) -> new Pair<String, Object>(c.getName(), source.stream(Customer.class).where( c2 -> c2.getSalary() > c.getSalary()).count()) )
  //            .sortedBy( pair -> pair.getOne())
  //            .toList();
  //      assertEquals("SELECT B.name, (SELECT COUNT(A) FROM Customer A WHERE A.salary > B.salary) FROM Customer B WHERE (SELECT COUNT(1) FROM B.sales C JOIN C.lineorders D WHERE D.item.name = 'Widgets') > 0 ORDER BY B.name ASC", query);
  //      assertEquals("Alice", sales.get(0).getOne());
  //      assertEquals("Carol", sales.get(1).getOne());
  //      assertEquals("Eve", sales.get(2).getOne());
  //      // EclipseLink returns Integers instead of Longs here for some reason
  //      // So we need this workaround to use Numbers instead to avoid a ClassCastException.
  //      assertEquals(3, ((Number)sales.get(0).getTwo()).longValue());
  //      assertEquals(2, ((Number)sales.get(1).getTwo()).longValue());
  //      assertEquals(4, ((Number)sales.get(2).getTwo()).longValue());
  //   }
  //
  //   @Test
  //   public void testSubQueryFrom()
  //   {
  //      // Subqueries in FROM clauses are generally not supported in JPQL
  //      // (and what support there exists is usually pretty poor.)
  //   }
  //
  //   @Test
  //   public void testSubQueryNoAggregation()
  //   {
  //      List<Customer> customers = streams.streamAll(em, Customer.class)
  //            .where( (c, source) -> 
  //                  c.getDebt() < source.stream(Customer.class)
  //                        .where(c2 -> c2.getName().equals("Alice"))
  //                        .select(c2 -> c2.getDebt())
  //                        .getOnlyValue() )
  //            .toList();
  //      assertEquals("SELECT A FROM Customer A WHERE A.debt < (SELECT B.debt FROM Customer B WHERE B.name = 'Alice')", query);
  //      assertEquals(1, customers.size());
  //      assertEquals("Eve", customers.get(0).getName());
  //   }
  //
  @Test
  def testSelectMath() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => c.getDebt() + c.getSalary() * 2)
      .toList();
    Assert.assertEquals("SELECT A.debt + A.salary * 2 FROM Customer A", query);
    val results = customers.sortBy(c => c);
    Assert.assertEquals(5, results.length);
    Assert.assertEquals(70, results(0));
  }

  //   @Test
  //   public void testSelectOperatorPrecedence()
  //   {
  //      List<Integer> results = streams.streamAll(em, Customer.class)
  //            .select(c -> 3 * (c.getDebt() - (c.getSalary() + 2)))
  //            .toList();
  //      assertEquals("SELECT 3 * (A.debt - (A.salary + 2)) FROM Customer A", query);
  //      assertEquals(5, results.size());
  //      Collections.sort(results);
  //      assertEquals(-1206, (int)results.get(0));
  //   }

  @Test
  def testSelectPair() {
    var customers = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), c.getCountry()))
      .toList();
    Assert.assertEquals("SELECT A.name, A.country FROM Customer A", query);
    Assert.assertEquals(5, customers.length);
    customers = customers.sortBy(c => c._1);
    Assert.assertEquals("Alice", customers(0)._1);
  }

  @Test
  def testSelectPairOfPair() {
    var customers = streamAll(em, classOf[Customer])
      .select(c => ((c.getName(), c.getCountry()), c.getDebt()))
      .toList();
    Assert.assertEquals("SELECT A.name, A.country, A.debt FROM Customer A", query);
    var results = customers.sortBy(p => p._1._1)
    Assert.assertEquals(5, results.length);
    Assert.assertEquals("Alice", results(0)._1._1);
    Assert.assertEquals(100, results(0)._2);
  }

  //       @Test
  //       def testSelectChained()
  //       {
  //          val customers = streamAll(em, classOf[Customer])
  //                .select(c => c.getDebt())
  //                .select(d => d * 2)
  //                .toList();
  //          Assert.assertEquals("SELECT A.debt * 2 FROM Customer A", query);
  //          val results = customers.sortBy(c => c);
  //          Assert.assertEquals(5, results.length);
  //          Assert.assertEquals(20, results(0));
  //       }

  @Test
  def testSelectChainedPair() {
    val results = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), c.getDebt()))
      .where(p => p._2 > 250)
      .toList();
    Assert.assertEquals("SELECT A.name, A.debt FROM Customer A WHERE A.debt > 250", query);
    Assert.assertEquals(1, results.length);
    Assert.assertEquals("Carol", results(0)._1);
  }

  //   @Test
  //   public void testSelectN1Link()
  //   {
  //      JinqStream<Customer> customers = streams.streamAll(em, Sale.class)
  //            .select(s -> s.getCustomer());
  //      assertEquals("SELECT A.customer FROM Sale A", customers.getDebugQueryString());
  //      List<Customer> results = customers.toList();
  //      Collections.sort(results, (c1, c2) -> c1.getName().compareTo(c2.getName()));
  //      assertEquals(6, results.size());
  //      assertEquals("Alice", results.get(0).getName());
  //   }

  @Test
  def testSelectCase() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), if (c.getCountry() == "UK") "UK" else "NotUK"))
      .sortedBy(p => p._1)
      .sortedBy(p => p._2)
      .toList();
    Assert.assertEquals("SELECT A.name, CASE WHEN A.country IS NOT NULL AND A.country <> 'UK' THEN 'NotUK' WHEN A.country IS NOT NULL AND A.country = 'UK' THEN 'UK' WHEN A.country IS NULL AND 'UK' IS NULL THEN 'UK' ELSE 'NotUK' END FROM Customer A ORDER BY CASE WHEN A.country IS NOT NULL AND A.country <> 'UK' THEN 'NotUK' WHEN A.country IS NOT NULL AND A.country = 'UK' THEN 'UK' WHEN A.country IS NULL AND 'UK' IS NULL THEN 'UK' ELSE 'NotUK' END ASC, A.name ASC", query);
    Assert.assertEquals(5, customers.length);
    Assert.assertEquals("UK", customers(4)._2);
    Assert.assertEquals("NotUK", customers(3)._2);
  }

        @Test
     def testString()
     {
        val value = "UK";
        val customers = streamAll(em, classOf[Customer])
              .where(c => c.getCountry() == value || c.getName() == "Alice")
              .select(c => (c, c.getName()))
              .toList()
              .sortBy(a => a._1.getName());
        Assert.assertEquals("SELECT A, A.name FROM Customer A WHERE A.country IS NOT NULL AND A.country = :param0 OR A.country IS NOT NULL AND A.country <> :param1 AND A.name IS NOT NULL AND A.name = 'Alice' OR A.country IS NOT NULL AND A.country <> :param2 AND A.name IS NULL AND 'Alice' IS NULL OR A.country IS NULL AND :param3 IS NULL OR A.country IS NULL AND :param4 IS NOT NULL AND A.name IS NOT NULL AND A.name = 'Alice' OR A.country IS NULL AND :param5 IS NOT NULL AND A.name IS NULL AND 'Alice' IS NULL", query);
        Assert.assertEquals(2, customers.length);
        Assert.assertEquals("Alice", customers(0)._2);
        Assert.assertEquals("Dave", customers(1)._2);
     }
  
     @Test
     def testStringEscape()
     {
        val customers = streamAll(em, classOf[Customer])
              .select(c => "I didn't know \\''")
              .toList();
        Assert.assertEquals("SELECT 'I didn''t know \\''''' FROM Customer A", query);
        Assert.assertEquals(5, customers.length);
        Assert.assertEquals("I didn't know \\''", customers(0));
     }
  
  //   @Test
  //   public void testInteger()
  //   {
  //      int val = 5;
  //      List<Pair<Customer, Integer>> customers = streams.streamAll(em, Customer.class)
  //            .where(c -> c.getSalary() + 5 + val < 212)
  //            .select(c -> new Pair<>(c, c.getSalary()))
  //            .toList();
  //      customers = customers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
  //      assertEquals("SELECT A, A.salary FROM Customer A WHERE A.salary + 5 + :param0 < 212", query);
  //      assertEquals(2, customers.size());
  //      assertEquals("Alice", customers.get(0).getOne().getName());
  //      assertEquals(200, (int)customers.get(0).getTwo());
  //      assertEquals("Eve", customers.get(1).getOne().getName());
  //   }
  //
  //   @Test
  //   public void testLong()
  //   {
  //      long val = 5;
  //      List<Pair<Supplier, Long>> suppliers = streams.streamAll(em, Supplier.class)
  //            .where(s -> s.getRevenue() + 1000 + val < 10000000L)
  //            .select(s -> new Pair<>(s, s.getRevenue()))
  //            .toList();
  //      suppliers = suppliers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
  //      assertEquals("SELECT A, A.revenue FROM Supplier A WHERE A.revenue + 1000 + :param0 < 10000000", query);
  //      assertEquals(2, suppliers.size());
  //      assertEquals("HW Supplier", suppliers.get(0).getOne().getName());
  //      assertEquals(500, (long)suppliers.get(0).getTwo());
  //      assertEquals("Talent Agency", suppliers.get(1).getOne().getName());
  //   }
  //
  //   @Test
  //   public void testDouble()
  //   {
  //      double val = 1;
  //      List<Pair<Item, Double>> items = streams.streamAll(em, Item.class)
  //            .where(i -> i.getSaleprice() > i.getPurchaseprice() + val + 2)
  //            .select(i -> new Pair<>(i, i.getSaleprice()))
  //            .toList();
  //      items = items.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
  //      assertEquals("SELECT A, A.saleprice FROM Item A WHERE A.saleprice > A.purchaseprice + :param0 + 2.0", query);
  //      assertEquals(2, items.size());
  //      assertEquals("Talent", items.get(0).getOne().getName());
  //      assertEquals("Widgets", items.get(1).getOne().getName());
  //      assertTrue(Math.abs((double)items.get(1).getTwo() - 10) < 0.1);
  //   }
  //
  //   @Test
  //   public void testDate()
  //   {
  //      Date val = Date.from(LocalDateTime.of(2002, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
  //      List<Pair<Customer, Date>> sales = streams.streamAll(em, Sale.class)
  //            .where(s -> s.getDate().before(val))
  //            .select(s -> new Pair<>(s.getCustomer(), s.getDate()))
  //            .toList();
  //      assertEquals("SELECT A.customer, A.date FROM Sale A WHERE A.date < :param0", query);
  //      assertEquals(1, sales.size());
  //      assertEquals("Dave", sales.get(0).getOne().getName());
  //      assertEquals(2001, LocalDateTime.ofInstant(sales.get(0).getTwo().toInstant(), ZoneOffset.UTC).getYear());
  //   }
  //
  //   @Test
  //   public void testDateEquals()
  //   {
  //      Date val = Date.from(LocalDateTime.of(2001, 1, 1, 1, 0).toInstant(ZoneOffset.UTC));
  //      List<String> sales = streams.streamAll(em, Sale.class)
  //            .where(s -> s.getDate().equals(val))
  //            .select(s -> s.getCustomer().getName())
  //            .toList();
  //      assertEquals("SELECT A.customer.name FROM Sale A WHERE A.date = :param0", query);
  //      assertEquals(1, sales.size());
  //      assertEquals("Dave", sales.get(0));
  //   }
  //
  //   @Test
  //   public void testCalendar()
  //   {
  //      Calendar val = Calendar.getInstance();
  //      val.setTime(Date.from(LocalDateTime.of(2002, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)));
  //      Calendar val2 = Calendar.getInstance();
  //      val2.setTime(Date.from(LocalDateTime.of(2003, 1, 1, 1, 0).toInstant(ZoneOffset.UTC)));
  //      List<Pair<Customer, Calendar>> sales = streams.streamAll(em, Sale.class)
  //            .where(s -> s.getCalendar().before(val) || s.getCalendar().equals(val2))
  //            .select(s -> new Pair<>(s.getCustomer(), s.getCalendar()))
  //            .toList();
  //      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
  //      assertEquals("SELECT A.customer, A.calendar FROM Sale A WHERE A.calendar < :param0 OR A.calendar >= :param1 AND A.calendar = :param2", query);
  //      assertEquals(2, sales.size());
  //      assertEquals("Dave", sales.get(0).getOne().getName());
  //      assertEquals("Carol", sales.get(1).getOne().getName());
  //   }
  //
  //   @SuppressWarnings("deprecation")
  //   @Test
  //   public void testSqlDate()
  //   {
  //      java.sql.Date val = new java.sql.Date(2002, 1, 1);
  //      java.sql.Date val2 = new java.sql.Date(2003, 1, 1);
  //      List<Pair<Customer, java.sql.Date>> sales = streams.streamAll(em, Sale.class)
  //            .where(s -> s.getSqlDate().before(val) || s.getSqlDate().equals(val2))
  //            .select(s -> new Pair<>(s.getCustomer(), s.getSqlDate()))
  //            .toList();
  //      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
  //      assertEquals("SELECT A.customer, A.sqlDate FROM Sale A WHERE A.sqlDate < :param0 OR A.sqlDate >= :param1 AND A.sqlDate = :param2", query);
  //      assertEquals(2, sales.size());
  //      assertEquals("Dave", sales.get(0).getOne().getName());
  //      assertEquals("Carol", sales.get(1).getOne().getName());
  //   }
  //
  //   @SuppressWarnings("deprecation")
  //   @Test
  //   public void testSqlTime()
  //   {
  //      java.sql.Time val = new java.sql.Time(6, 0, 0);
  //      java.sql.Time val2 = new java.sql.Time(5, 0, 0);
  //      List<Pair<Customer, java.sql.Time>> sales = streams.streamAll(em, Sale.class)
  //            .where(s -> s.getSqlTime().after(val) || s.getSqlTime().equals(val2))
  //            .select(s -> new Pair<>(s.getCustomer(), s.getSqlTime()))
  //            .toList();
  //      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
  //      assertEquals("SELECT A.customer, A.sqlTime FROM Sale A WHERE A.sqlTime > :param0 OR A.sqlTime <= :param1 AND A.sqlTime = :param2", query);
  //      assertEquals(2, sales.size());
  //      assertEquals("Carol", sales.get(0).getOne().getName());
  //      assertEquals("Alice", sales.get(1).getOne().getName());
  //   }
  //
  //   @SuppressWarnings("deprecation")
  //   @Test
  //   public void testSqlTimestamp()
  //   {
  //      java.sql.Timestamp val = new java.sql.Timestamp(2002, 1, 1, 1, 0, 0, 0);
  //      java.sql.Timestamp val2 = new java.sql.Timestamp(2003, 1, 1, 1, 0, 0, 0);
  //      List<Pair<Customer, java.sql.Timestamp>> sales = streams.streamAll(em, Sale.class)
  //            .where(s -> s.getSqlTimestamp().before(val) || s.getSqlTimestamp().equals(val2))
  //            .select(s -> new Pair<>(s.getCustomer(), s.getSqlTimestamp()))
  //            .toList();
  //      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
  //      assertEquals("SELECT A.customer, A.sqlTimestamp FROM Sale A WHERE A.sqlTimestamp < :param0 OR A.sqlTimestamp >= :param1 AND A.sqlTimestamp = :param2", query);
  //      assertEquals(2, sales.size());
  //      assertEquals("Dave", sales.get(0).getOne().getName());
  //      assertEquals("Carol", sales.get(1).getOne().getName());
  //   }
  //
  //   @Test
  //   public void testBoolean()
  //   {
  //      boolean val = false;
  //      // Direct access to boolean variables in a WHERE must be converted to a comparison 
  //      List<Pair<Supplier, Boolean>> suppliers = streams.streamAll(em, Supplier.class)
  //            .where(s -> s.getHasFreeShipping())
  //            .select(s -> new Pair<>(s, s.getHasFreeShipping()))
  //            .toList();
  //      assertEquals("SELECT A, A.hasFreeShipping FROM Supplier A WHERE A.hasFreeShipping = TRUE", query);
  //      assertEquals(1, suppliers.size());
  //      assertEquals("Talent Agency", suppliers.get(0).getOne().getName());
  //      assertTrue(suppliers.get(0).getTwo());
  //      
  //      // Boolean parameters 
  //      suppliers = streams.streamAll(em, Supplier.class)
  //            .where(s -> s.getHasFreeShipping() == val)
  //            .select(s -> new Pair<>(s, s.getHasFreeShipping()))
  //            .toList();
  //      suppliers = suppliers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
  //      assertEquals("SELECT A, A.hasFreeShipping FROM Supplier A WHERE A.hasFreeShipping = :param0", query);
  //      assertEquals(2, suppliers.size());
  //      assertEquals("Conglomerate", suppliers.get(0).getOne().getName());
  //      assertEquals("HW Supplier", suppliers.get(1).getOne().getName());
  //   }
  //   
  //   @Test(expected=ClassCastException.class)
  //   public void testBooleanOperations()
  //   {
  //      // Comparisons in a SELECT must be converted to a CASE...WHEN... or something
  //      // TODO: CASE...WHEN... is now done, and I've inserted a little hack to convert the 1 and 0 constants 
  //      //    into booleans, but EclipseLink is treating TRUE and FALSE and integers in the return type.
  //      List<Pair<Supplier, Boolean>> suppliers = streams.streamAll(em, Supplier.class)
  //            .where(s -> s.getHasFreeShipping())
  //            .select(s -> new Pair<>(s, s.getHasFreeShipping() != true))
  //            .toList();
  //      assertTrue("SELECT A, CASE WHEN NOT A.hasFreeShipping = TRUE THEN TRUE ELSE FALSE END FROM Supplier A WHERE A.hasFreeShipping = TRUE".equals(query)
  //            || "SELECT A, CASE WHEN A.hasFreeShipping = TRUE THEN FALSE ELSE TRUE END FROM Supplier A WHERE A.hasFreeShipping = TRUE".equals(query));
  //      assertEquals(1, suppliers.size());
  //      assertEquals("Talent Agency", suppliers.get(0).getOne().getName());
  //      assertTrue(!suppliers.get(0).getTwo());
  //   }
  //   
  //   @Test
  //   public void testEnum()
  //   {
  //      ItemType val = ItemType.OTHER;
  //      List<Pair<Item, ItemType>> items = streams.streamAll(em, Item.class)
  //            .where(i -> i.getType() == val || i.getType().equals(ItemType.BIG))
  //            .select(i -> new Pair<>(i, i.getType()))
  //            .toList();
  //      items = items.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
  //      assertEquals("SELECT A, A.type FROM Item A WHERE A.type = :param0 OR A.type <> :param1 AND A.type = org.jinq.jpa.test.entities.ItemType.BIG", query);
  //      assertEquals(2, items.size());
  //      assertEquals("Lawnmowers", items.get(0).getOne().getName());
  //      assertEquals("Talent", items.get(1).getOne().getName());
  //      assertEquals(ItemType.OTHER, items.get(1).getTwo());
  //   }
  //   
  //   @Test
  //   public void testBigDecimal()
  //   {
  //      BigDecimal val = BigDecimal.valueOf(4);
  //      BigDecimal val2 = BigDecimal.valueOf(2);
  //      List<Pair<Lineorder, BigDecimal>> lineorders = streams.streamAll(em, Lineorder.class)
  //            .where(c -> c.getTotal().compareTo(val.multiply(val2)) < 0)
  //            .select(c -> new Pair<>(c, c.getTotal()))
  //            .toList();
  //      lineorders = lineorders.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
  //      assertEquals("SELECT A, A.total FROM Lineorder A WHERE A.total < :param0 * :param1", query);
  //      assertEquals(5, lineorders.size());
  //      assertEquals(1, lineorders.get(0).getTwo().intValue());
  //      assertEquals(5, lineorders.get(3).getTwo().intValue());
  //   }
  //
  //   @Test
  //   public void testBigInteger()
  //   {
  //      BigInteger val = BigInteger.valueOf(3600);
  //      BigInteger val2 = BigInteger.valueOf(500);
  //      List<Pair<Lineorder, BigInteger>> lineorders = streams.streamAll(em, Lineorder.class)
  //            .where(c -> c.getTransactionConfirmation().add(val2).compareTo(val) < 0)
  //            .select(c -> new Pair<>(c, c.getTransactionConfirmation()))
  //            .toList();
  //      lineorders = lineorders.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
  //      assertEquals("SELECT A, A.transactionConfirmation FROM Lineorder A WHERE A.transactionConfirmation + :param0 < :param1", query);
  //      assertEquals(3, lineorders.size());
  //      assertEquals(1000, lineorders.get(0).getTwo().intValue());
  //      assertEquals(3000, lineorders.get(2).getTwo().intValue());
  //   }
  //
  //   @Test
  //   public void testBlob()
  //   {
  //      List<Pair<Supplier, byte[]>> suppliers = streams.streamAll(em, Supplier.class)
  //            .select(s -> new Pair<>(s, s.getSignature()))
  //            .toList();
  //      suppliers = suppliers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
  //      assertEquals("SELECT A, A.signature FROM Supplier A", query);
  //      assertEquals(3, suppliers.size());
  //      assertEquals("Conglomerate", Charset.forName("UTF-8").decode(ByteBuffer.wrap(suppliers.get(0).getTwo())).toString());
  //   }
  //
  //   @Test
  //   public void testDivide()
  //   {
  //      double val = 5.0;
  //      List<Double> resultDouble = streams.streamAll(em, Customer.class)
  //            .select(c -> val / 2.0).toList();
  //      assertEquals("SELECT :param0 / 2.0 FROM Customer A", query);
  //      assertEquals(2.5, resultDouble.get(0), 0.001);
  //      
  //      int valInt = 5;
  //      List<Integer> resultInteger = streams.streamAll(em, Customer.class)
  //            .select(c -> valInt / 2).toList();
  //      assertEquals("SELECT :param0 / 2 FROM Customer A", query);
  //      assertEquals(2, (int)resultInteger.get(0));
  //
  //      resultDouble = streams.streamAll(em, Customer.class)
  //            .select(c -> val * 2.0 / valInt)
  //            .sortedBy( num -> num ).toList();
  //      assertEquals("SELECT :param0 * 2.0 / :param1 FROM Customer A ORDER BY :param0 * 2.0 / :param1 ASC", query);
  //      assertEquals(2.0, resultDouble.get(0), 0.001);
  //   }
  //   
  //   @Test(expected=ClassCastException.class)
  //   public void testJPQLWeirdness()
  //   {
  //      // EclipseLink seems to think two parameters divided by each other results in a BigDouble.
  //      // Perhaps the problem is that EclipseLink cannot determine the type until after the
  //      // parameters are substituted in, but it performs its type checking earlier than that?
  //      double val = 5.0;
  //      int valInt = 5;
  //      List<Double> resultDouble = streams.streamAll(em, Customer.class)
  //            .select(c -> val / valInt).toList();
  //      assertEquals("SELECT :param0 / :param1 FROM Customer A", query);
  //      assertEquals(1.0, resultDouble.get(0).doubleValue(), 0.001);
  //   }
  //
  //   @Test(expected=IllegalArgumentException.class)
  //   public void testJPQLWeirdness2()
  //   {
  //      // EclipseLink seems to have problems when ordering things without using
  //      // any fields of data.
  //      double val = 5.0;
  //      List<Customer> results = streams.streamAll(em, Customer.class)
  //            .sortedBy(c -> 5 * val).toList();
  //      assertEquals("SELECT A FROM Customer A ORDER BY 5 * :param0", query);
  //      assertEquals(5, results.size());
  //   }
  //
  //   @Test
  //   public void testNumericPromotionMath()
  //   {
  //      List<Lineorder> lineorders = streams.streamAll(em, Lineorder.class)
  //            .sortedBy(lo -> lo.getQuantity() * lo.getItem().getSaleprice())
  //            .toList();
  //      assertEquals("SELECT A FROM Lineorder A ORDER BY A.quantity * A.item.saleprice ASC", query);
  //      assertEquals(11, lineorders.size());
  //      assertEquals("Screws", lineorders.get(0).getItem().getName());
  //   }
  //
  //   @Test
  //   public void testNumericPromotionBigDecimal()
  //   {
  //      long val = 3;
  //      List<Double> lineorders = streams.streamAll(em, Lineorder.class)
  //            .select(lo -> (lo.getTotal().add(new BigDecimal(val))).doubleValue() + lo.getItem().getSaleprice())
  //            .sortedBy(num -> num)
  //            .toList();
  //      assertEquals("SELECT A.total + :param0 + A.item.saleprice FROM Lineorder A ORDER BY A.total + :param0 + A.item.saleprice ASC", query);
  //      assertEquals(11, lineorders.size());
  //      assertEquals(6, lineorders.get(0).longValue());
  //   }
  //
  //   @Test
  //   public void testNumericPromotionBigInteger()
  //   {
  //      List<BigInteger> lineorders = streams.streamAll(em, Lineorder.class)
  //            .select(lo -> lo.getTransactionConfirmation().add(BigInteger.valueOf(lo.getQuantity())))
  //            .sortedBy(num -> num)
  //            .toList();
  //      assertEquals("SELECT A.transactionConfirmation + A.quantity FROM Lineorder A ORDER BY A.transactionConfirmation + A.quantity ASC", query);
  //      assertEquals(11, lineorders.size());
  //      assertEquals(1001, lineorders.get(0).longValue());
  //   }
  //   
  //   @Test
  //   public void testNumericPromotionComparison()
  //   {
  //      List<Lineorder> lineorders = streams.streamAll(em, Lineorder.class)
  //            .where(lo -> lo.getQuantity() + 20 < lo.getItem().getSaleprice())
  //            .sortedBy(lo -> lo.getItem().getName())
  //            .toList();
  //      assertEquals("SELECT A FROM Lineorder A WHERE A.quantity + 20 < A.item.saleprice ORDER BY A.item.name ASC", query);
  //      assertEquals(3, lineorders.size());
  //      assertEquals("Lawnmowers", lineorders.get(0).getItem().getName());
  //   }
  //   
  //   @Test
  //   public void testNull()
  //   {
  //      // TODO: I'm not sure if translating == NULL to "IS NULL" is the best route to go
  //      // Although it is the most intuitive, it isn't consistent with JPQL NULL handling rules
  //      streams.streamAll(em, Sale.class)
  //            .where(s -> s.getCustomer() == null)
  //            .toList();
  //      assertEquals("SELECT A FROM Sale A WHERE A.customer IS NULL", query);
  //   }
  //   
  //   @Test
  //   public void testNonNull()
  //   {
  //      streams.streamAll(em, Supplier.class)
  //            .where(s -> null != s.getCountry())
  //            .toList();
  //      assertEquals("SELECT A FROM Supplier A WHERE A.country IS NOT NULL", query);
  //   }
  //
  //      @Test
  //   public void testWhere()
  //   {
  //      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
  //          .where((c) -> c.getCountry().equals("UK"));
  //      assertEquals("SELECT A FROM Customer A WHERE A.country = 'UK'", customers.getDebugQueryString());
  //      List<Customer> results = customers.toList();
  //      assertEquals(1, results.size());
  //      assertEquals("Dave", results.get(0).getName());
  //   }
  //
  //   @Test
  //   public void testWherePaths()
  //   {
  //      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
  //           .where((c) -> c.getCountry().equals("UK") ? c.getName().equals("Bob") : c.getName().equals("Alice"));
  //      assertEquals("SELECT A FROM Customer A WHERE A.name = 'Alice' AND A.country <> 'UK' OR A.name = 'Bob' AND A.country = 'UK'", customers.getDebugQueryString());
  //      List<Customer> results = customers.toList();
  //      assertEquals(1, results.size());
  //      assertEquals("Alice", results.get(0).getName());
  //   }
  
     @Test
     def testWhereIntegerComparison()
     {
        val customers = streamAll(em, classOf[Customer])
             .where(c => c.getDebt() < 90)
             .toList();
        Assert.assertEquals("SELECT A FROM Customer A WHERE A.debt < 90", query);
        Assert.assertEquals(1, customers.length);
        Assert.assertEquals("Eve", customers(0).getName());
     }
  
  //   @Test
  //   public void testWhereNegation()
  //   {
  //      List<Customer> customers = streams.streamAll(em, Customer.class)
  //           .where(c -> c.getDebt() > -c.getSalary() + 1)
  //           .toList();
  //      assertEquals("SELECT A FROM Customer A WHERE A.debt > - A.salary + 1", query);
  //      assertEquals(5, customers.size());
  //   }
  
     @Test
     def testWhereNot()
     {
        val suppliers = streamAll(em, classOf[Supplier])
             .where(s => !s.getHasFreeShipping())
             .where(s => !(s.getName().equals("Conglomerate") || s.getName().equals("Talent Agency")))
             .toList();
        Assert.assertEquals("SELECT A FROM Supplier A WHERE NOT A.hasFreeShipping = TRUE AND (A.name <> 'Conglomerate' AND A.name <> 'Talent Agency')", query);
        Assert.assertEquals(1, suppliers.length);
        Assert.assertEquals("HW Supplier", suppliers(0).getName());
     }
  
  //   @Test
  //   public void testWhereChained()
  //   {
  //      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
  //           .where(c -> c.getCountry().equals("Switzerland"))
  //           .where(c -> c.getName().equals("Bob"));
  //      assertEquals("SELECT A FROM Customer A WHERE A.country = 'Switzerland' AND A.name = 'Bob'", customers.getDebugQueryString());
  //      List<Customer> results = customers.toList();
  //      assertEquals(1, results.size());
  //      assertEquals("Bob", results.get(0).getName());
  //   }
  //
  //   @Test
  //   public void testWhereParameter()
  //   {
  //      int param = 90;
  //      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
  //           .where(c -> c.getDebt() < param);
  //      assertEquals("SELECT A FROM Customer A WHERE A.debt < :param0", customers.getDebugQueryString());
  //      List<Customer> results = customers.toList();
  //      assertEquals(1, results.size());
  //      assertEquals("Eve", results.get(0).getName());
  //   }
  //
  //   @Test
  //   public void testWhereParameterChainedString()
  //   {
  //      String param = "UK";
  //      JinqStream<String> customers = streams.streamAll(em, Customer.class)
  //            .select(c -> new Pair<String, String>(c.getName(), c.getCountry()))
  //            .where(p -> p.getTwo().equals(param))
  //            .select(p -> p.getOne());
  //      assertEquals("SELECT A.name FROM Customer A WHERE A.country = :param0", customers.getDebugQueryString());
  //      List<String> results = customers.toList();
  //      assertEquals(1, results.size());
  //      assertEquals("Dave", results.get(0));
  //   }
  //
  //   @Test
  //   public void testWhereParameters()
  //   {
  //      int paramLower = 150;
  //      int paramUpper = 250;
  //      JinqStream<Customer> customers = streams.streamAll(em, Customer.class)
  //           .where(c -> c.getDebt() > paramLower && c.getDebt() < paramUpper);
  //      assertEquals("SELECT A FROM Customer A WHERE A.debt > :param0 AND A.debt < :param1", customers.getDebugQueryString());
  //      List<Customer> results = customers.toList();
  //      assertEquals(1, results.size());
  //      assertEquals("Bob", results.get(0).getName());
  //   }
  //   
  //   @Test
  //   public void testWhereN1Link()
  //   {
  //      JinqStream<Sale> sales = streams.streamAll(em, Sale.class)
  //            .where(s -> s.getCustomer().getName().equals("Alice"));
  //      assertEquals("SELECT A FROM Sale A WHERE A.customer.name = 'Alice'", sales.getDebugQueryString());
  //      List<Sale> results = sales.toList();
  //      assertEquals(2, results.size());
  //      assertEquals("Alice", results.get(0).getCustomer().getName());
  //   }
  //   
  //   @Test
  //   public void testWhereN1Links()
  //   {
  //      JinqStream<Pair<String, Date>> sales = streams.streamAll(em, Sale.class)
  //            .where(s -> s.getCustomer().getCountry().equals("Switzerland"))
  //            .where(s -> s.getCustomer().getDebt() < 150)
  //            .select(s -> new Pair<>(s.getCustomer().getName(), s.getDate()));
  //      assertEquals("SELECT A.customer.name, A.date FROM Sale A WHERE A.customer.country = 'Switzerland' AND A.customer.debt < 150", sales.getDebugQueryString());
  //      List<Pair<String, Date>> results = sales.toList();
  //      assertEquals(2, results.size());
  //      assertEquals("Alice", results.get(0).getOne());
  //   }
  //

}
