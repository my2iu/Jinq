package org.jinq.jpa;

import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date

import org.jinq.jpa.test.entities.Customer
import org.jinq.jpa.test.entities.Item
import org.jinq.jpa.test.entities.ItemType
import org.jinq.jpa.test.entities.Lineorder
import org.jinq.jpa.test.entities.PhoneNumber
import org.jinq.jpa.test.entities.Sale
import org.jinq.jpa.test.entities.Supplier
import org.jinq.orm.stream.scala.InQueryStreamSource
import org.jinq.orm.stream.scala.JinqConversions.jinq
import org.jinq.orm.stream.scala.JinqIterator
import org.junit.Assert
import org.junit.Test

import javax.persistence.EntityManager

class JinqJPAScalaTest extends JinqJPAScalaTestBase {
  private def streamAll[U](em: EntityManager, entityClass: java.lang.Class[U]): JinqIterator[U] = {
    JinqJPAScalaTestBase.streams.streamAll(em, entityClass);
  }

  @Test
  def testStreamEntities {
    var customers = streamAll(em, classOf[Customer])
      .toList
      .sortBy((c) => c.getName())
    Assert.assertEquals("Alice", customers(0).getName);
    Assert.assertEquals(5, customers.length);
    Assert.assertEquals("Eve", customers(4).getName);
  }

  @Test
  def testWhereParametersMixed {
    val name = "UK"
    val debt = 100
    var customers = streamAll(em, classOf[Customer])
      .where((c) => c.getCountry == name && c.getDebt == debt)
      .toList;
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
  def testJoinFetchList() {
    val results = streamAll(em, classOf[Sale])
      .joinFetch(_.getLineorders())
      .where(_.getCustomer().getName().equals("Alice"))
      .distinct
      .toList;
    Assert.assertEquals("SELECT DISTINCT A FROM Sale A JOIN FETCH A.lineorders B WHERE A.customer.name = 'Alice'", query);
    // The semantics of JOIN FETCH are a little inconsistent
    // so it's hard to know exactly will be returned. EclipseLink seems
    // to treat it like a regular join, so you need to use DISTINCT to prevent
    // the same result from appearing too many times, but Hibernate will leave 
    // the join fetched items out of result sets even if you include it there..
    Assert.assertEquals(2, results.length);
  }

  @Test
  def testJoinEntity() {
    var results = streamAll(em, classOf[Item])
      .where(i => i.getName().equals("Widgets"))
      .join((i, source) => source.stream(classOf[Item]))
      .where(pair => pair._1.getPurchaseprice() < pair._2.getPurchaseprice())
      .toList;
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
      .toList;
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
      .toList;
    Assert.assertEquals("SELECT A, B FROM Item A LEFT OUTER JOIN A.suppliers B WHERE A.name = 'Widgets'", query);
    results = results.sortBy(c1 => c1._2.getName());
    Assert.assertEquals(2, results.length);
    Assert.assertEquals("Widgets", results(0)._1.getName());
    Assert.assertEquals("Conglomerate", results(0)._2.getName());
    Assert.assertEquals("HW Supplier", results(1)._2.getName());
  }
     
  @Test
  def testOuterJoinOn() {
    var results = streamAll(em, classOf[Item])
      .leftOuterJoin(
          (i, source) => source.stream(classOf[Supplier]),
          (item, supplier : Supplier) => item.getName().substring(0, 1) == supplier.getName().substring(0, 1))
      .toList;
    
    Assert.assertEquals("SELECT A, B FROM Item A LEFT OUTER JOIN Supplier B ON SUBSTRING(A.name, 0 + 1, 1 - 0) IS NOT NULL AND SUBSTRING(A.name, 0 + 1, 1 - 0) = SUBSTRING(B.name, 0 + 1, 1 - 0) OR SUBSTRING(A.name, 0 + 1, 1 - 0) IS NULL AND SUBSTRING(B.name, 0 + 1, 1 - 0) IS NULL", query);
    results = results.sortBy(c1 => c1._1.getName());
    Assert.assertEquals(5, results.length);
    Assert.assertEquals("Lawnmowers", results(0)._1.getName());
    Assert.assertNull(results(0)._2);
    Assert.assertEquals("Talent", results(2)._1.getName());
    Assert.assertEquals("Talent Agency", results(2)._2.getName());
  }
   
  @Test
  def testOuterJoinOnTrueAndNavigationalLinks() {
    var results = streamAll(em, classOf[Item])
      .leftOuterJoin(
          (i, source) => i.getSuppliers,
          (item, supplier : Supplier) => true)
      .toList;
    Assert.assertEquals("SELECT A, B FROM Item A LEFT OUTER JOIN A.suppliers B", query);
    Assert.assertEquals(6, results.length);
  }

  @Test
  def testOuterJoinOnWithParametersAndIndirect() {
    val m = "Screws";
    var results = streamAll(em, classOf[Item])
      .select(i => i.getName())
      .leftOuterJoin(
          (i, source) => source.stream(classOf[Supplier]),
          (item, supplier : Supplier) => item == m)
      .toList;
    Assert.assertEquals("SELECT A.name, B FROM Item A LEFT OUTER JOIN Supplier B ON A.name IS NOT NULL AND A.name = :param0 OR A.name IS NULL AND :param1 IS NULL", query);
    results = results.sortBy(c1 => c1._1);
    Assert.assertEquals(7, results.length);
    Assert.assertEquals("Lawnmowers", results(0)._1);
    Assert.assertNull(results(0)._2);
    Assert.assertEquals("Screws", results(1)._1);
    Assert.assertEquals("Screws", results(2)._1);
    Assert.assertEquals("Screws", results(3)._1);
  }

  //   @Test
  //   def testStreamPages()
  //   {
  //      List<String> names = streamAll(em, classOf[Customer])
  //            .setHint("automaticPageSize", 1)
  //            .select(c => c.getName() )
  //            .toList;
  //      names = names.stream().sorted().collect(Collectors.toList);
  //      Assert.assertEquals(5, names.length);
  //      Assert.assertEquals("Alice", names(0));
  //      Assert.assertEquals("Bob", names(1));
  //      Assert.assertEquals("Carol", names(2));
  //      Assert.assertEquals("Dave", names(3));
  //      Assert.assertEquals("Eve", names(4));
  //   }
  //
  //   private static void externalMethod() {}
  //   
  //   @Test
  //   def testExceptionOnFail()
  //   {
  //      streamAll(em, classOf[Customer])
  //            .setHint("exceptionOnTranslationFail", false)
  //            .select(c => {externalMethod(); return "blank";} )
  //            .toList;
  //      try {
  //         streamAll(em, classOf[Customer])
  //               .setHint("exceptionOnTranslationFail", true)
  //               .select(c => {externalMethod(); return "blank";} )
  //               .toList;
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
      .toList;
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
      .toList;
    Assert.assertEquals("SELECT A, C FROM Lineorder A JOIN A.item B LEFT OUTER JOIN B.suppliers C WHERE A.item.name IS NOT NULL AND A.item.name = 'Talent' OR A.item.name IS NULL AND 'Talent' IS NULL", query);
    results = results.sortBy(c1 => c1._2.getName());
    Assert.assertEquals(1, results.length);
  }

  @Test
  def testOuterJoin11() {
    var results = streamAll(em, classOf[Lineorder])
      .leftOuterJoin(lo => JinqIterator.of(lo.getItem()))
      .where(pair => pair._2.getName() == "Talent")
      .toList;
    Assert.assertEquals("SELECT A, B FROM Lineorder A LEFT OUTER JOIN A.item B WHERE B.name IS NOT NULL AND B.name = 'Talent' OR B.name IS NULL AND 'Talent' IS NULL", query);
    results = results.sortBy(c1 => c1._2.getName());
    Assert.assertEquals(1, results.length);
  }

  @Test
  def testOuterJoinFetch()
  {
    val results = streamAll(em, classOf[Sale])
      .leftOuterJoinFetch(_.getLineorders())
      .where(_.getCustomer().getName().equals("Alice"))
      .distinct
      .toList;
     Assert.assertEquals("SELECT DISTINCT A FROM Sale A LEFT OUTER JOIN FETCH A.lineorders B WHERE A.customer.name = 'Alice'", query);
     // The semantics of JOIN FETCH are a little inconsistent
     // so it's hard to know exactly will be returned. EclipseLink seems
     // to treat it like a regular join, so you need to use DISTINCT to prevent
     // the same result from appearing too many times, but Hibernate will leave 
     // the join fetched items out of result sets even if you include it there..
     Assert.assertEquals(2, results.length);
  }

  //   @Test(expected=IllegalArgumentException.class)
  //   def testOuterJoinField()
  //   {
  //      // Cannot do outer joins on normal fields. Only navigational links.
  //      List<Pair<Customer, String>> results = streamAll(em, classOf[Customer])
  //            .leftOuterJoin(c => JinqStream.of(c.getCountry()))
  //            .toList;
  //      Assert.assertEquals("SELECT A, B FROM Customer A LEFT OUTER JOIN A.country B", query);
  //      Assert.assertEquals(5, results.length);
  //   }

  @Test
  def testSort {
    val results = streamAll(em, classOf[Customer])
      .sortedBy(c => c.getName())
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A ORDER BY A.name ASC", query);
    Assert.assertEquals(5, results.length);
    Assert.assertEquals("Alice", results(0).getName());
    Assert.assertEquals("Bob", results(1).getName());
    Assert.assertEquals("Eve", results(4).getName());
  }

  @Test
  def testSortExpression() {
    val results = streamAll(em, classOf[Item])
      .where(i => i.getPurchaseprice() > 1)
      .sortedDescendingBy(i => i.getSaleprice() - i.getPurchaseprice())
      .toList;
    Assert.assertEquals("SELECT A FROM Item A WHERE A.purchaseprice > 1 ORDER BY A.saleprice - A.purchaseprice DESC", query);
    Assert.assertEquals(4, results.length);
    Assert.assertEquals("Talent", results(0).getName());
    Assert.assertEquals("Widgets", results(1).getName());
  }

  @Test
  def testSortChained() {
    val results = streamAll(em, classOf[Customer])
      .sortedDescendingBy(c => c.getName())
      .sortedBy(c => c.getCountry())
      .toList;
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
      .toList;
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
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A ORDER BY A.name ASC", query);
    Assert.assertEquals(1, queryList.length);
    Assert.assertEquals(2, results.length);
    Assert.assertEquals("Bob", results(0).getName());
    Assert.assertEquals("Carol", results(1).getName());
  }

  @Test
  def testJPQLStringFunctions() {
    val customers = streamAll(em, classOf[Customer])
      .where(c => JPQL.like(c.getName(), "A_i%ce") && c.getName().length() > c.getName().indexOf("l"))
      .select(c => c.getName().toUpperCase().trim() + c.getCountry().substring(0, 1))
      .toList;
    Assert.assertEquals("SELECT CONCAT(TRIM(UPPER(A.name)), SUBSTRING(A.country, 0 + 1, 1 - 0)) FROM Customer A WHERE A.name LIKE 'A_i%ce' AND LENGTH(A.name) > LOCATE('l', A.name) - 1", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("ALICES", customers(0));
  }

  @Test
  def testJPQLStringConcat() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => c.getName() + " " + c.getCountry())
      .sortedBy(s => s)
      .toList;
    Assert.assertEquals("SELECT CONCAT(CONCAT(A.name, ' '), A.country) FROM Customer A ORDER BY CONCAT(CONCAT(A.name, ' '), A.country) ASC", query);
    Assert.assertEquals(5, customers.length);
    Assert.assertEquals("Alice Switzerland", customers(0));
  }

  @Test
  def testJPQLNumberFunctions() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => Math.abs(c.getSalary() + Math.sqrt(c.getDebt())) + (c.getSalary() % c.getDebt()))
      .toList;
    Assert.assertEquals("SELECT ABS(A.salary + SQRT(A.debt)) + MOD(A.salary, A.debt) FROM Customer A", query);
    Assert.assertEquals(5, customers.length);
  }

  @Test
  def testCount() {
    val count = streamAll(em, classOf[Customer])
      .count();
    Assert.assertEquals("SELECT COUNT(A) FROM Customer A", query);
    Assert.assertEquals(5l, count);
  }

  @Test
  def testCountWhere() {
    val count = streamAll(em, classOf[Customer])
      .where(c => c.getCountry().equals("UK"))
      .count();
    Assert.assertEquals("SELECT COUNT(A) FROM Customer A WHERE A.country = 'UK'", query);
    Assert.assertEquals(1l, count);
  }

  @Test
  def testCountMultipleFields() {
    val count = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), c.getCountry()))
      .count();
    Assert.assertEquals("SELECT COUNT(1) FROM Customer A", query);
    Assert.assertEquals(5l, count);
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
    Assert.assertEquals(1280l, streamAll(em, classOf[Customer])
      .sumInteger(s => s.getSalary()));
    Assert.assertEquals("SELECT SUM(A.salary) FROM Customer A", query);
  }

  @Test
  def testSumExpression() {
    // Sum of integers is a long
    Assert.assertEquals(205300l, streamAll(em, classOf[Customer])
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
  //   def testSumCase()
  //   {
  //      // EclipseLink should be returning a Long, since it's a sum of integers, but it's returning
  //      // an integer instead.
  //      Assert.assertEquals(1, (long)streamAll(em, classOf[Supplier])
  //            .sumInteger(s => s.getHasFreeShipping() ? 1 : 0));
  //      Assert.assertEquals("SELECT SUM(CASE WHEN A.hasFreeShipping = TRUE THEN 1 ELSE 0 END) FROM Customer A", query);
  //   }

  @Test
  def testMultiAggregate() {
    Assert.assertEquals((1280l, 256.0),
      streamAll(em, classOf[Customer])
        .aggregate(stream => stream.sumInteger(c => c.getSalary()),
          stream => stream.avg(c => c.getSalary())));
    Assert.assertEquals("SELECT SUM(A.salary), AVG(A.salary) FROM Customer A", query);

    Assert.assertEquals((5l, 30, 500),
      streamAll(em, classOf[Customer])
        .aggregate(stream => stream.count(),
          stream => stream.min(c => c.getSalary()),
          stream => stream.max(c => c.getSalary())));
    Assert.assertEquals("SELECT COUNT(A), MIN(A.salary), MAX(A.salary) FROM Customer A", query);
    
    Assert.assertEquals((new BigDecimal(2467), BigInteger.valueOf(66000)),
      streamAll(em, classOf[Lineorder])
        .aggregate(stream => stream.sumBigDecimal(lo => lo.getTotal()),
          stream => stream.sumBigInteger(lo => lo.getTransactionConfirmation())));
    Assert.assertEquals("SELECT SUM(A.total), SUM(A.transactionConfirmation) FROM Lineorder A", query);

  }

  @Test
  def testMultiAggregateNoAggregate() {
    Assert.assertEquals((5l, 30, 500),
      streamAll(em, classOf[Customer])
        .aggregate(stream => stream.count(),
          stream => 30,
          stream => 500));
    Assert.assertEquals("SELECT COUNT(A), 30, 500 FROM Customer A", query);
  }

  @Test
  def testMultiAggregateTuple5() {
    Assert.assertEquals((5l, 30, 500, 5l, 20),
      streamAll(em, classOf[Customer])
        .aggregate(stream => stream.count(),
          stream => stream.min(c => c.getSalary()),
          stream => stream.max(c => c.getSalary()),
          stream => stream.count(),
          stream => 20));
    Assert.assertEquals("SELECT COUNT(A), MIN(A.salary), MAX(A.salary), COUNT(A), 20 FROM Customer A", query);
  }

  @Test
  def testMultiAggregateParameters() {
    val param: Int = 1;
    Assert.assertEquals((1285l, 257.0),
      streamAll(em, classOf[Customer])
        .aggregate(
          // Need to put param inside a "val" subparam, otherwise Scala treats it as a closure when passing it to the subquery.
          stream => { val subparam = param; stream.sumInteger(c => c.getSalary() + subparam) },
          stream => param + stream.avg(c => c.getSalary())));
    Assert.assertEquals("SELECT SUM(A.salary + :param0), :param1 + AVG(A.salary) FROM Customer A", query);
  }

  @Test
  def testMultiAggregateWithDistinct() {
    // Derby doesn't allow for more than one aggregation of distinct things at the same time,
    // so we'll break the test up into two cases.
    Assert.assertEquals((5l, 710l),
      streamAll(em, classOf[Customer])
        .aggregate(
          stream => stream.distinct().count(),
          stream => stream.select(c => c.getDebt()).sumInteger(s => s)));
    Assert.assertEquals("SELECT COUNT(DISTINCT A), SUM(A.debt) FROM Customer A", query);

    Assert.assertEquals((5l, 610l),
      streamAll(em, classOf[Customer])
        .aggregate(
          stream => stream.count(),
          stream => stream.select(c => c.getDebt()).distinct().sumInteger(s => s)));
    Assert.assertEquals("SELECT COUNT(A), SUM(DISTINCT A.debt) FROM Customer A", query);
  }

  //   @Test(expected=IllegalArgumentException.class)
  //   def testMultiAggregateParametersWithDistinctDisallowed()
  //   {
  //      // You can only aggregate a distinct stream if you pass the contents of the stream directly to the aggregation function.
  //      Assert.assertEquals(610l, 
  //            (long)streamAll(em, classOf[Customer])
  //                  .select(c => c.getDebt())
  //                  .distinct()
  //                  .sumInteger(s => s + 1));
  //   }

  @Test
  def testSelectDistinct() {
    val itemsSold = streamAll(em, classOf[Lineorder])
      .select(lo => lo.getItem().getName())
      .distinct()
      .sortedBy(name => name)
      .toList;
    Assert.assertEquals("SELECT DISTINCT A.item.name FROM Lineorder A ORDER BY A.item.name ASC", query);
    Assert.assertEquals(5, itemsSold.length);
    Assert.assertEquals("Lawnmowers", itemsSold(0));
  }

  @Test
  def testGroup() {
    val results =
      streamAll(em, classOf[Customer])
        .group(c => c.getCountry(),
          (country: String, stream) => stream.count(),
          (country: String, stream) => stream.min(c => c.getSalary()))
        .toList;
    results.sortBy(a => a._1);
    Assert.assertEquals(4, results.length);
    Assert.assertEquals(1l, results(0)._2);
    Assert.assertEquals("Canada", results(0)._1);
    Assert.assertEquals(("Switzerland", 2l, 200), results(1));
    Assert.assertEquals("SELECT A.country, COUNT(A), MIN(A.salary) FROM Customer A GROUP BY A.country", query);
  }

  @Test
  def testGroupSortLimit() {
    val results =
      streamAll(em, classOf[Customer])
        .group(c => c.getCountry(),
          (country: String, stream) => stream.count(),
          (country: String, stream) => stream.min(c => c.getSalary()))
        .sortedBy(g => g._1)
        .skip(1)
        .limit(1)
        .toList;
    Assert.assertEquals(1, results.length);
    Assert.assertEquals(("Switzerland", 2l, 200), results(0));
    Assert.assertEquals("SELECT A.country, COUNT(A), MIN(A.salary) FROM Customer A GROUP BY A.country ORDER BY A.country ASC", query);
  }

  @Test
  def testGroupByLinkEntity() {
    var results =
      streamAll(em, classOf[Sale])
        .group(s => s.getCustomer(),
          (c: Customer, stream) => stream.count())
        .toList
        .sortBy(group => group._1.getName())
        .sortBy(group => group._2);
    Assert.assertEquals(4, results.length);
    Assert.assertEquals("Dave", results(0)._1.getName());
    Assert.assertEquals(2l, results(2)._2);
    Assert.assertEquals("Alice", results(2)._1.getName());
    Assert.assertEquals("SELECT A.customer, COUNT(A) FROM Sale A GROUP BY A.customer", query);
  }

  @Test
  def testGroupByMixKeyAggregate() {
    val results =
      streamAll(em, classOf[Sale])
        .group(s => (s.getCustomer().getName(), s.getCustomer().getSalary()),
          (c: (String, Int), stream) => c._2 + stream.count())
        .select(group => (group._1._1, group._2))
        .toList
        .sortBy(group => group._1);
    Assert.assertEquals(("Alice", 202l),
      results(0));
    Assert.assertEquals("SELECT A.customer.name, A.customer.salary + COUNT(A) FROM Sale A GROUP BY A.customer.name, A.customer.salary", query);
  }

  @Test
  def testGroupByHaving() {
    val results =
      streamAll(em, classOf[Lineorder])
        .where(lo => "Screws".equals(lo.getItem().getName()))
        .select(lo => lo.getSale())
        .group(s => (s.getCustomer().getName(), s.getCustomer().getSalary()),
          (c: (String, Int), stream) => c._2 + stream.count())
        .where(group => group._2 < 220)
        .select(group => (group._1._1, group._2))
        .sortedBy(group => group._1)
        .toList;
    Assert.assertEquals(2, results.length);
    Assert.assertEquals(("Alice", 201l),
      results(0));
    Assert.assertEquals("SELECT A.sale.customer.name, A.sale.customer.salary + COUNT(A.sale) FROM Lineorder A WHERE 'Screws' = A.item.name GROUP BY A.sale.customer.name, A.sale.customer.salary HAVING A.sale.customer.salary + COUNT(A.sale) < 220 ORDER BY A.sale.customer.name ASC", query);
  }

  //   @Test(expected=IllegalArgumentException.class)
  //   def testSortGroupByFail()
  //   {
  //      // Cannot do a group by after a sort
  //      streamAll(em, classOf[Customer])
  //            .sortedBy(c => c.getName())
  //            .group(c => c.getCountry(), 
  //                  (c, stream) => stream.count())
  //            .toList;
  //      Assert.assertEquals("SELECT A.country, COUNT(1), MIN(A.salary) FROM Customer A GROUP BY A.country ORDER BY A.country", query);
  //   }

  @Test
  def testSubQueryWithSource() {
    val sales = streamAll(em, classOf[Sale])
      .where((s, source) => source.stream(classOf[Sale]).max(ss => ss.getSaleid()) == s.getSaleid())
      .toList;
    Assert.assertEquals("SELECT A FROM Sale A WHERE (SELECT MAX(B.saleid) FROM Sale B) = A.saleid", query);
    Assert.assertEquals(1, sales.length);
  }

  @Test
  def testSubQueryWithNavigationalLink() {
    val customers = streamAll(em, classOf[Customer])
      .where(c => c.getSales().count() > 1)
      .sortedBy(c => c.getName())
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE (SELECT COUNT(B) FROM A.sales B) > 1 ORDER BY A.name ASC", query);
    Assert.assertEquals(2, customers.length);
    Assert.assertEquals("Alice", customers(0).getName());
    Assert.assertEquals("Carol", customers(1).getName());
  }

  @Test
  def testSubQueryWithNavigationalLinkInSelect() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => (c, c.getSales().count()))
      .sortedBy(c => c._1.getName())
      .sortedBy(c => c._2)
      .toList;
    // EclipseLink on Derby is returning the result of the subquery as an integer and not a long, causing a cast problem here
    //      customers.sort(Comparator.comparing(pair => pair._1.getName()));
    //      customers.sort(Comparator.comparing(pair => pair._2));
    Assert.assertEquals("SELECT B, (SELECT COUNT(A) FROM B.sales A) FROM Customer B ORDER BY (SELECT COUNT(A) FROM B.sales A ASC), B.name ASC", query);
    Assert.assertEquals(5, customers.length);
    // EclipseLink on Derby just isn't handling the sorting by subqueries very well, so the result doesn't
    // seem to be sorted correctly
  }

  @Test
  def testSubQueryWithSelectSourceAndWhere() {
    val sales: List[(String, java.lang.Number)] = streamAll(em, classOf[Customer])
      .where(c => c.getSales().join(s => s.getLineorders()).where(p => p._2.getItem().getName().equals("Widgets")).count() > 0)
      .select((c: Customer, source: InQueryStreamSource) => (c.getName(), java.lang.Long.valueOf(source.stream(classOf[Customer]).where(c2 => c2.getSalary() > c.getSalary()).count())))
      .sortedBy(pair => pair._1)
      .toList;
    Assert.assertEquals("SELECT B.name, (SELECT COUNT(A) FROM Customer A WHERE A.salary > B.salary) FROM Customer B WHERE (SELECT COUNT(1) FROM B.sales C JOIN C.lineorders D WHERE D.item.name = 'Widgets') > 0 ORDER BY B.name ASC", query);
    Assert.assertEquals("Alice", sales(0)._1);
    Assert.assertEquals("Carol", sales(1)._1);
    Assert.assertEquals("Eve", sales(2)._1);
    // EclipseLink returns Integers instead of Longs here for some reason
    // So we need this workaround to use Numbers instead to avoid a ClassCastException.
    Assert.assertEquals(3, (sales(0)._2).asInstanceOf[java.lang.Number].longValue());
    Assert.assertEquals(2, (sales(1)._2).asInstanceOf[java.lang.Number].longValue());
    Assert.assertEquals(4, (sales(2)._2).asInstanceOf[java.lang.Number].longValue());
  }

  @Test
  def testSubQueryFrom() {
    // Subqueries in FROM clauses are generally not supported in JPQL
    // (and what support there exists is usually pretty poor.)
  }

  @Test
  def testSubQueryNoAggregation() {
    val customers = streamAll(em, classOf[Customer])
      .where((c, source) =>
        c.getDebt() < source.stream(classOf[Customer])
          .where(c2 => c2.getName().equals("Alice"))
          .select(c2 => c2.getDebt())
          .getOnlyValue())
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.debt < (SELECT B.debt FROM Customer B WHERE B.name = 'Alice')", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Eve", customers(0).getName());
  }

  @Test
  def testIsInStream() {
    val customers = streamAll(em, classOf[Customer])
      .where(!_.getSales.selectAll(_.getLineorders).select(_.getItem.getName).contains("Widgets"))
      .sortedBy(_.getName)
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE NOT 'Widgets' IN (SELECT C.item.name FROM A.sales B JOIN B.lineorders C) ORDER BY A.name ASC", query);
    Assert.assertEquals(2, customers.length);
    Assert.assertEquals("Bob", customers(0).getName);
    Assert.assertEquals("Dave", customers(1).getName);
    
//    val talentSale = streamAll(em, classOf[Lineorder])
//      .where(_.getItem.getName == "Talent")
//      .select(_.getSale)
//      .getOnlyValue;
//    val buyer = streamAll(em, classOf[Customer])
//      .where(_.getSales.contains(talentSale))
//      .toList
//    Assert.assertEquals("SELECT A FROM Customer A WHERE :param0 IN A.sales", query);
//    Assert.assertEquals(1, buyer.length);
//    Assert.assertEquals("Dave", buyer(0).getName);
  }
  
  @Test
  def testSelectMath() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => c.getDebt() + c.getSalary() * 2)
      .toList;
    Assert.assertEquals("SELECT A.debt + A.salary * 2 FROM Customer A", query);
    val results = customers.sortBy(c => c);
    Assert.assertEquals(5, results.length);
    Assert.assertEquals(70, results(0));
  }

  @Test
  def testSelectOperatorPrecedence() {
    val results = streamAll(em, classOf[Customer])
      .select(c => 3 * (c.getDebt() - (c.getSalary() + 2)))
      .toList
      .sortBy(num => num);
    Assert.assertEquals("SELECT 3 * (A.debt - (A.salary + 2)) FROM Customer A", query);
    Assert.assertEquals(5, results.length);
    Assert.assertEquals(-1206, results(0));
  }

  @Test
  def testSelectPair() {
    var customers = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), c.getCountry()))
      .toList;
    Assert.assertEquals("SELECT A.name, A.country FROM Customer A", query);
    Assert.assertEquals(5, customers.length);
    customers = customers.sortBy(c => c._1);
    Assert.assertEquals("Alice", customers(0)._1);
  }

  @Test
  def testSelectPairOfPair() {
    var customers = streamAll(em, classOf[Customer])
      .select(c => ((c.getName(), c.getCountry()), c.getDebt()))
      .toList;
    Assert.assertEquals("SELECT A.name, A.country, A.debt FROM Customer A", query);
    var results = customers.sortBy(p => p._1._1)
    Assert.assertEquals(5, results.length);
    Assert.assertEquals("Alice", results(0)._1._1);
    Assert.assertEquals(100, results(0)._2);
  }

  @Test
  def testSelectChained() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => c.getDebt())
      .select(d => d * 2)
      .toList;
    Assert.assertEquals("SELECT A.debt * 2 FROM Customer A", query);
    val results = customers.sortBy(c => c);
    Assert.assertEquals(5, results.length);
    Assert.assertEquals(20, results(0));
  }

  @Test
  def testSelectChainedPair() {
    val results = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), c.getDebt()))
      .where(p => p._2 > 250)
      .toList;
    Assert.assertEquals("SELECT A.name, A.debt FROM Customer A WHERE A.debt > 250", query);
    Assert.assertEquals(1, results.length);
    Assert.assertEquals("Carol", results(0)._1);
  }

  @Test
  def testSelectN1Link() {
    val customers = streamAll(em, classOf[Sale])
      .select(s => s.getCustomer())
      .toList
      .sortBy(c => c.getName());
    Assert.assertEquals("SELECT A.customer FROM Sale A", query);
    Assert.assertEquals(6, customers.length);
    Assert.assertEquals("Alice", customers(0).getName());
  }

  @Test
  def testSelectCase() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), if (c.getCountry() == "UK") "UK" else "NotUK"))
      .sortedBy(p => p._1)
      .sortedBy(p => p._2)
      .toList;
    Assert.assertEquals("SELECT A.name, CASE WHEN A.country IS NOT NULL AND A.country <> 'UK' THEN 'NotUK' WHEN A.country IS NOT NULL AND A.country = 'UK' THEN 'UK' WHEN A.country IS NULL AND 'UK' IS NULL THEN 'UK' ELSE 'NotUK' END FROM Customer A ORDER BY CASE WHEN A.country IS NOT NULL AND A.country <> 'UK' THEN 'NotUK' WHEN A.country IS NOT NULL AND A.country = 'UK' THEN 'UK' WHEN A.country IS NULL AND 'UK' IS NULL THEN 'UK' ELSE 'NotUK' END ASC, A.name ASC", query);
    Assert.assertEquals(5, customers.length);
    Assert.assertEquals("UK", customers(4)._2);
    Assert.assertEquals("NotUK", customers(3)._2);
  }

  @Test
  def testSelectAll() {
    val suppliers = streamAll(em, classOf[Item])
      .where(_.getName() eq "Screws")
      .selectAll(_.getSuppliers)
      .select(_.getName())
      .toList;
    Assert.assertEquals("SELECT B.name FROM Item A JOIN A.suppliers B WHERE A.name = 'Screws'", query);
    Assert.assertEquals(1, suppliers.length);
    Assert.assertEquals("HW Supplier", suppliers(0));
  }

  @Test
  def testString() {
    val value = "UK";
    val customers = streamAll(em, classOf[Customer])
      .where(c => c.getCountry() == value || c.getName() == "Alice")
      .select(c => (c, c.getName()))
      .toList
      .sortBy(a => a._1.getName());
    Assert.assertEquals("SELECT A, A.name FROM Customer A WHERE A.country IS NOT NULL AND A.country = :param0 OR A.country IS NOT NULL AND A.country <> :param1 AND A.name IS NOT NULL AND A.name = 'Alice' OR A.country IS NOT NULL AND A.country <> :param2 AND A.name IS NULL AND 'Alice' IS NULL OR A.country IS NULL AND :param3 IS NULL OR A.country IS NULL AND :param4 IS NOT NULL AND A.name IS NOT NULL AND A.name = 'Alice' OR A.country IS NULL AND :param5 IS NOT NULL AND A.name IS NULL AND 'Alice' IS NULL", query);
    Assert.assertEquals(2, customers.length);
    Assert.assertEquals("Alice", customers(0)._2);
    Assert.assertEquals("Dave", customers(1)._2);
  }

  @Test
  def testStringEscape() {
    val customers = streamAll(em, classOf[Customer])
      .select(c => "I didn't know \\''")
      .toList;
    Assert.assertEquals("SELECT 'I didn''t know \\''''' FROM Customer A", query);
    Assert.assertEquals(5, customers.length);
    Assert.assertEquals("I didn't know \\''", customers(0));
  }

  @Test
  def testInteger() {
    val value = 5;
    val customers = streamAll(em, classOf[Customer])
      .where(c => c.getSalary() + 5 + value < 212)
      .select(c => (c, c.getSalary()))
      .toList
      .sortBy(a => a._1.getName());
    Assert.assertEquals("SELECT A, A.salary FROM Customer A WHERE A.salary + 5 + :param0 < 212", query);
    Assert.assertEquals(2, customers.length);
    Assert.assertEquals("Alice", customers(0)._1.getName());
    Assert.assertEquals(200, customers(0)._2);
    Assert.assertEquals("Eve", customers(1)._1.getName());
  }

  @Test
  def testLong() {
    val value: Long = 5;
    val suppliers = streamAll(em, classOf[Supplier])
      .where(s => s.getRevenue() + 1000 + value < 10000000L)
      .select(s => (s, s.getRevenue()))
      .toList
      .sortBy(a => a._1.getName());
    Assert.assertEquals("SELECT A, A.revenue FROM Supplier A WHERE A.revenue + 1000 + :param0 < 10000000", query);
    Assert.assertEquals(2, suppliers.length);
    Assert.assertEquals("HW Supplier", suppliers(0)._1.getName());
    Assert.assertEquals(500, suppliers(0)._2);
    Assert.assertEquals("Talent Agency", suppliers(1)._1.getName());
  }

  @Test
  def testDouble() {
    val value: Double = 1;
    val items = streamAll(em, classOf[Item])
      .where(i => i.getSaleprice() > i.getPurchaseprice() + value + 2)
      .select(i => (i, i.getSaleprice()))
      .toList
      .sortBy(a => a._1.getName());
    Assert.assertEquals("SELECT A, A.saleprice FROM Item A WHERE A.saleprice > A.purchaseprice + :param0 + 2", query);
    Assert.assertEquals(2, items.length);
    Assert.assertEquals("Talent", items(0)._1.getName());
    Assert.assertEquals("Widgets", items(1)._1.getName());
    Assert.assertTrue(Math.abs(items(1)._2 - 10) < 0.1);
  }
  
  private def getBogusDate(year:Int, month:Int, day:Int) : Date = {
     // Performs a bogus conversion of a year-month-day into an old Java Date type
     Date.from(java.sql.Timestamp.valueOf(LocalDateTime.of(year, month, day, 1, 0)).toInstant());
  }

  private def getBogusSqlDate(year:Int, month:Int, day:Int) : java.sql.Date = {
     // All the Java sql date and time stuff is ambiguous about timezones, so we're basically just ignoring major issues here
     java.sql.Date.valueOf(LocalDate.of(year, month, day));
  }

  private def getBogusSqlTime(hour:Int, minute:Int) : java.sql.Time = {
     // All the Java sql date and time stuff is ambiguous about timezones, so we're basically just ignoring major issues here
     java.sql.Time.valueOf(LocalTime.of(hour, minute));
  }

  private def getBogusSqlTimestamp(year:Int, month:Int, day:Int, hour:Int, minute:Int) : java.sql.Timestamp = {
     // All the Java sql date and time stuff is ambiguous about timezones, so we're basically just ignoring major issues here
     java.sql.Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute));
  }

  @Test
  def testDate() {
    val value = getBogusDate(2002, 1, 1);
    val sales = streamAll(em, classOf[Sale])
      .where(s => s.getDate().before(value))
      .select(s => (s.getCustomer(), s.getDate()))
      .toList;
    Assert.assertEquals("SELECT A.customer, A.date FROM Sale A WHERE A.date < :param0", query);
    Assert.assertEquals(1, sales.length);
    Assert.assertEquals("Dave", sales(0)._1.getName());
    Assert.assertEquals(2001, LocalDateTime.ofInstant(sales(0)._2.toInstant(), ZoneOffset.UTC).getYear());
  }

  @Test
  def testDateEquals() {
    val value = getBogusDate(2001, 1, 1);
    val sales = streamAll(em, classOf[Sale])
      .where(s => s.getDate() == value)
      .select(s => s.getCustomer().getName())
      .toList;
    Assert.assertEquals("SELECT A.customer.name FROM Sale A WHERE A.date IS NOT NULL AND A.date = :param0 OR A.date IS NULL AND :param1 IS NULL", query);
    Assert.assertEquals(1, sales.length);
    Assert.assertEquals("Dave", sales(0));
  }

  @Test
  def testCalendar() {
    val val1 = Calendar.getInstance();
    val1.setTime(getBogusDate(2002, 1, 1));
    val val2 = Calendar.getInstance();
    val2.setTime(getBogusDate(2003, 1, 1));
    val sales = streamAll(em, classOf[Sale])
      .where(s => s.getCalendar().before(val1) || s.getCalendar().equals(val2))
      .select(s => (s.getCustomer(), s.getCalendar()))
      .toList
      .sortWith((a, b) => a._2.before(b._2));
    Assert.assertEquals("SELECT A.customer, A.calendar FROM Sale A WHERE A.calendar < :param0 OR A.calendar = :param1", query);
    Assert.assertEquals(2, sales.length);
    Assert.assertEquals("Dave", sales(0)._1.getName());
    Assert.assertEquals("Carol", sales(1)._1.getName());
  }

  @Test
  def testSqlDate() {
    val val1 = getBogusSqlDate(2002, 1, 1);
    val val2 = getBogusSqlDate(2003, 1, 1);
    val sales = streamAll(em, classOf[Sale])
      .where(s => s.getSqlDate().before(val1) || s.getSqlDate().equals(val2))
      .select(s => (s.getCustomer(), s.getSqlDate()))
      .toList
      .sortWith((a, b) => a._2.before(b._2));
    Assert.assertEquals("SELECT A.customer, A.sqlDate FROM Sale A WHERE A.sqlDate < :param0 OR A.sqlDate = :param1", query);
    Assert.assertEquals(2, sales.length);
    Assert.assertEquals("Dave", sales(0)._1.getName());
    Assert.assertEquals("Carol", sales(1)._1.getName());
  }

  @Test
  def testSqlTime() {
    val val1 = getBogusSqlTime(6, 0);
    val val2 = getBogusSqlTime(5, 0);
    val sales = streamAll(em, classOf[Sale])
      .where(s => s.getSqlTime().after(val1) || s.getSqlTime().equals(val2))
      .select(s => (s.getCustomer(), s.getSqlTime()))
      .toList
      .sortWith((a, b) => a._2.before(b._2));
    Assert.assertEquals("SELECT A.customer, A.sqlTime FROM Sale A WHERE A.sqlTime > :param0 OR A.sqlTime = :param1", query);
    Assert.assertEquals(2, sales.length);
    Assert.assertEquals("Carol", sales(0)._1.getName());
    Assert.assertEquals("Alice", sales(1)._1.getName());
  }

  @Test
  def testSqlTimestamp() {
    val val1 = getBogusSqlTimestamp(2002, 1, 1, 1, 0);
    val val2 = getBogusSqlTimestamp(2003, 1, 1, 1, 0);
    val sales = streamAll(em, classOf[Sale])
      .where(s => s.getSqlTimestamp().before(val1) || s.getSqlTimestamp().equals(val2))
      .select(s => (s.getCustomer(), s.getSqlTimestamp()))
      .toList
      .sortWith((a, b) => a._2.before(b._2));
    Assert.assertEquals("SELECT A.customer, A.sqlTimestamp FROM Sale A WHERE A.sqlTimestamp < :param0 OR A.sqlTimestamp = :param1", query);
    Assert.assertEquals(2, sales.length);
    Assert.assertEquals("Dave", sales(0)._1.getName());
    Assert.assertEquals("Carol", sales(1)._1.getName());
  }

  @Test
  def testBoolean() {
    val value = false;
    // Direct access to boolean variables in a WHERE must be converted to a comparison 
    var suppliers = streamAll(em, classOf[Supplier])
      .where(s => s.getHasFreeShipping())
      .select(s => (s, s.getHasFreeShipping()))
      .toList;
    Assert.assertEquals("SELECT A, A.hasFreeShipping FROM Supplier A WHERE A.hasFreeShipping = TRUE", query);
    Assert.assertEquals(1, suppliers.length);
    Assert.assertEquals("Talent Agency", suppliers(0)._1.getName());
    Assert.assertTrue(suppliers(0)._2);

    // Boolean parameters 
    suppliers = streamAll(em, classOf[Supplier])
      .where(s => s.getHasFreeShipping() == value)
      .select(s => (s, s.getHasFreeShipping()))
      .toList
      .sortBy(a => a._1.getName());
    Assert.assertEquals("SELECT A, A.hasFreeShipping FROM Supplier A WHERE A.hasFreeShipping = :param0", query);
    Assert.assertEquals(2, suppliers.length);
    Assert.assertEquals("Conglomerate", suppliers(0)._1.getName());
    Assert.assertEquals("HW Supplier", suppliers(1)._1.getName());
  }

  //   @Test(expected=ClassCastException.class)
  //   def testBooleanOperations()
  //   {
  //      // Comparisons in a SELECT must be converted to a CASE...WHEN... or something
  //      // TODO: CASE...WHEN... is now done, and I've inserted a little hack to convert the 1 and 0 constants 
  //      //    into booleans, but EclipseLink is treating TRUE and FALSE and integers in the return type.
  //      List<Pair<Supplier, Boolean>> suppliers = streamAll(em, classOf[Supplier])
  //            .where(s => s.getHasFreeShipping())
  //            .select(s => new Pair<>(s, s.getHasFreeShipping() != true))
  //            .toList;
  //      Assert.assertTrue("SELECT A, CASE WHEN NOT A.hasFreeShipping = TRUE THEN TRUE ELSE FALSE END FROM Supplier A WHERE A.hasFreeShipping = TRUE".equals(query)
  //            || "SELECT A, CASE WHEN A.hasFreeShipping = TRUE THEN FALSE ELSE TRUE END FROM Supplier A WHERE A.hasFreeShipping = TRUE".equals(query));
  //      Assert.assertEquals(1, suppliers.length);
  //      Assert.assertEquals("Talent Agency", suppliers(0)._1.getName());
  //      Assert.assertTrue(!suppliers(0)._2);
  //   }

  @Test
  def testBooleanJavaBeanNaming() {
      // Direct access to boolean variables in a WHERE must be converted to a comparison 
    val suppliers = streamAll(em, classOf[Supplier])
      .where(s => s.isPreferredSupplier())
      .select(s => (s, s.isPreferredSupplier()))
      .toList;
    Assert.assertEquals("SELECT A, A.preferredSupplier FROM Supplier A WHERE A.preferredSupplier = TRUE", query);
    Assert.assertEquals(1, suppliers.length);
    Assert.assertEquals("Conglomerate", suppliers(0)._1.getName());
    Assert.assertTrue(suppliers(0)._2);

    val rushSales = streamAll(em, classOf[Sale])
      .where(s => s.isRush())
      .select(s => (s, s.isRush()))
      .toList;
    Assert.assertEquals("SELECT A, A.rush FROM Sale A WHERE A.rush = TRUE", query);
    Assert.assertEquals(1, rushSales.length);
    Assert.assertEquals("Carol", rushSales(0)._1.getCustomer().getName());
    Assert.assertTrue(rushSales(0)._2);
  }

  @Test
  def testEnum() {
    val value = ItemType.OTHER;
    val items = streamAll(em, classOf[Item])
      .where(i => i.getType() == value || i.getType().equals(ItemType.BIG))
      .select(i => (i, i.getType()))
      .toList
      .sortBy(a => a._1.getName());
    Assert.assertEquals("SELECT A, A.type FROM Item A WHERE A.type IS NOT NULL AND A.type = :param0 OR A.type IS NOT NULL AND A.type <> :param1 AND A.type = org.jinq.jpa.test.entities.ItemType.BIG OR A.type IS NULL AND :param2 IS NULL OR A.type IS NULL AND :param3 IS NOT NULL AND A.type = org.jinq.jpa.test.entities.ItemType.BIG", query);
    Assert.assertEquals(2, items.length);
    Assert.assertEquals("Lawnmowers", items(0)._1.getName());
    Assert.assertEquals("Talent", items(1)._1.getName());
    Assert.assertEquals(ItemType.OTHER, items(1)._2);
  }
  
  @Test
  def testConverter() {
    val value = new PhoneNumber("1", "555", "5552222");
    val phones = streamAll(em, classOf[Customer])
      .where(c => c.getPhone().equals(value))
      .select(c => (c, c.getPhone()))
      .toList
      .sortBy(c => c._1.getName());
    Assert.assertEquals("SELECT A, A.phone FROM Customer A WHERE A.phone = :param0", query);
    Assert.assertEquals(1, phones.length);
    Assert.assertEquals("Bob", phones(0)._1.getName());
    Assert.assertEquals(value, phones(0)._2);
  }

  @Test
  def testBigDecimal() {
    val val1 = BigDecimal.valueOf(4);
    val val2 = BigDecimal.valueOf(2);
    val lineorders = streamAll(em, classOf[Lineorder])
      .where(c => c.getTotal().compareTo(val1.multiply(val2)) < 0)
      .select(c => (c, c.getTotal()))
      .toList
      .sortBy(a => a._2);
    Assert.assertEquals("SELECT A, A.total FROM Lineorder A WHERE A.total < :param0 * :param1", query);
    Assert.assertEquals(5, lineorders.length);
    Assert.assertEquals(1, lineorders(0)._2.intValue());
    Assert.assertEquals(5, lineorders(3)._2.intValue());
  }

  @Test
  def testBigInteger() {
    val val1 = BigInteger.valueOf(3600);
    val val2 = BigInteger.valueOf(500);
    val lineorders = streamAll(em, classOf[Lineorder])
      .where(c => c.getTransactionConfirmation().add(val2).compareTo(val1) < 0)
      .select(c => (c, c.getTransactionConfirmation()))
      .toList
      .sortBy(a => a._2);
    Assert.assertEquals("SELECT A, A.transactionConfirmation FROM Lineorder A WHERE A.transactionConfirmation + :param0 < :param1", query);
    Assert.assertEquals(3, lineorders.length);
    Assert.assertEquals(1000, lineorders(0)._2.intValue());
    Assert.assertEquals(3000, lineorders(2)._2.intValue());
  }

  @Test
  def testBlob() {
    val suppliers = streamAll(em, classOf[Supplier])
      .select(s => (s, s.getSignature()))
      .toList
      .sortBy(a => a._1.getName());
    Assert.assertEquals("SELECT A, A.signature FROM Supplier A", query);
    Assert.assertEquals(3, suppliers.length);
    Assert.assertEquals("Conglomerate", Charset.forName("UTF-8").decode(ByteBuffer.wrap(suppliers(0)._2)).toString());
  }

  @Test
  def testDivide() {
    val value = 5.0;
    var resultDouble = streamAll(em, classOf[Customer])
      .select(c => value / 2.0).toList;
    Assert.assertEquals("SELECT :param0 / 2.0 FROM Customer A", query);
    Assert.assertEquals(2.5, resultDouble(0), 0.001);

    val valInt = 5;
    val resultInteger = streamAll(em, classOf[Customer])
      .select(c => valInt / 2).toList;
    Assert.assertEquals("SELECT :param0 / 2 FROM Customer A", query);
    Assert.assertEquals(2, resultInteger(0));

    resultDouble = streamAll(em, classOf[Customer])
      .select(c => value * 2.0 / valInt)
      .sortedBy(num => num).toList;
    Assert.assertEquals("SELECT :param0 * 2.0 / :param1 FROM Customer A ORDER BY :param0 * 2.0 / :param1 ASC", query);
    Assert.assertEquals(2.0, resultDouble(0), 0.001);
  }

  //   @Test(expected=ClassCastException.class)
  //   def testJPQLWeirdness()
  //   {
  //      // EclipseLink seems to think two parameters divided by each other results in a BigDouble.
  //      // Perhaps the problem is that EclipseLink cannot determine the type until after the
  //      // parameters are substituted in, but it performs its type checking earlier than that?
  //      double val = 5.0;
  //      int valInt = 5;
  //      List<Double> resultDouble = streamAll(em, classOf[Customer])
  //            .select(c => val / valInt).toList;
  //      Assert.assertEquals("SELECT :param0 / :param1 FROM Customer A", query);
  //      Assert.assertEquals(1.0, resultDouble(0).doubleValue(), 0.001);
  //   }
  //
  //   @Test(expected=IllegalArgumentException.class)
  //   def testJPQLWeirdness2()
  //   {
  //      // EclipseLink seems to have problems when ordering things without using
  //      // any fields of data.
  //      double val = 5.0;
  //      List<Customer> results = streamAll(em, classOf[Customer])
  //            .sortedBy(c => 5 * val).toList;
  //      Assert.assertEquals("SELECT A FROM Customer A ORDER BY 5 * :param0", query);
  //      Assert.assertEquals(5, results.length);
  //   }

  @Test
  def testNumericPromotionMath() {
    val lineorders = streamAll(em, classOf[Lineorder])
      .sortedBy(lo => lo.getQuantity() * lo.getItem().getSaleprice())
      .toList;
    Assert.assertEquals("SELECT A FROM Lineorder A ORDER BY A.quantity * A.item.saleprice ASC", query);
    Assert.assertEquals(11, lineorders.length);
    Assert.assertEquals("Screws", lineorders(0).getItem().getName());
  }

  @Test
  def testNumericPromotionBigDecimal() {
    val value: Long = 3;
    val lineorders = streamAll(em, classOf[Lineorder])
      .select(lo => (lo.getTotal().add(new BigDecimal(value))).doubleValue() + lo.getItem().getSaleprice())
      .sortedBy(num => num)
      .toList;
    Assert.assertEquals("SELECT A.total + :param0 + A.item.saleprice FROM Lineorder A ORDER BY A.total + :param0 + A.item.saleprice ASC", query);
    Assert.assertEquals(11, lineorders.length);
    Assert.assertEquals(6, lineorders(0).longValue());
  }

  @Test
  def testNumericPromotionBigInteger() {
    val lineorders = streamAll(em, classOf[Lineorder])
      .select(lo => lo.getTransactionConfirmation().add(BigInteger.valueOf(lo.getQuantity())))
      .sortedBy(num => num)
      .toList;
    Assert.assertEquals("SELECT A.transactionConfirmation + A.quantity FROM Lineorder A ORDER BY A.transactionConfirmation + A.quantity ASC", query);
    Assert.assertEquals(11, lineorders.length);
    Assert.assertEquals(1001, lineorders(0).longValue());
  }

  @Test
  def testNumericPromotionComparison() {
    val lineorders = streamAll(em, classOf[Lineorder])
      .where(lo => lo.getQuantity() + 20 < lo.getItem().getSaleprice())
      .sortedBy(lo => lo.getItem().getName())
      .toList;
    Assert.assertEquals("SELECT A FROM Lineorder A WHERE A.quantity + 20 < A.item.saleprice ORDER BY A.item.name ASC", query);
    Assert.assertEquals(3, lineorders.length);
    Assert.assertEquals("Lawnmowers", lineorders(0).getItem().getName());
  }

  @Test
  def testNull() {
    // TODO: I'm not sure if translating == NULL to "IS NULL" is the best route to go
    // Although it is the most intuitive, it isn't consistent with JPQL NULL handling rules
    streamAll(em, classOf[Sale])
      .where(s => s.getCustomer() == null)
      .toList;
    Assert.assertEquals("SELECT A FROM Sale A WHERE A.customer IS NULL", query);
  }

  @Test
  def testNonNull() {
    streamAll(em, classOf[Supplier])
      .where(s => null != s.getCountry())
      .toList;
    Assert.assertEquals("SELECT A FROM Supplier A WHERE A.country IS NOT NULL", query);
  }

  @Test
  def testSimpleWhere {
    var customers = streamAll(em, classOf[Customer])
      .where((c) => c.getCountry == "UK")
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.country IS NOT NULL AND A.country = 'UK' OR A.country IS NULL AND 'UK' IS NULL", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Dave", customers(0).getName());
  }

  @Test
  def testWherePaths() {
    val customers = streamAll(em, classOf[Customer])
      .where((c) => if (c.getCountry().equals("UK")) c.getName().equals("Bob") else c.getName().equals("Alice"))
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.name = 'Alice' AND A.country <> 'UK' OR A.name = 'Bob' AND A.country = 'UK'", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Alice", customers(0).getName());
  }

  @Test
  def testWhereIntegerComparison() {
    val customers = streamAll(em, classOf[Customer])
      .where(c => c.getDebt() < 90)
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.debt < 90", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Eve", customers(0).getName());
  }

  @Test
  def testWhereNegation() {
    val customers = streamAll(em, classOf[Customer])
      .where(c => c.getDebt() > -c.getSalary() + 1)
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.debt > - A.salary + 1", query);
    Assert.assertEquals(5, customers.length);
  }

  @Test
  def testWhereNot() {
    val suppliers = streamAll(em, classOf[Supplier])
      .where(s => !s.getHasFreeShipping())
      .where(s => !(s.getName().equals("Conglomerate") || s.getName().equals("Talent Agency")))
      .toList;
    Assert.assertEquals("SELECT A FROM Supplier A WHERE NOT A.hasFreeShipping = TRUE AND (A.name <> 'Conglomerate' AND A.name <> 'Talent Agency')", query);
    Assert.assertEquals(1, suppliers.length);
    Assert.assertEquals("HW Supplier", suppliers(0).getName());
  }

  @Test
  def testWhereChained() {
    val customers = streamAll(em, classOf[Customer])
      .where(c => c.getCountry() == "Switzerland")
      .where(c => c.getName() == "Bob")
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE (A.country IS NOT NULL AND A.country = 'Switzerland' OR A.country IS NULL AND 'Switzerland' IS NULL) AND (A.name IS NOT NULL AND A.name = 'Bob' OR A.name IS NULL AND 'Bob' IS NULL)", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Bob", customers(0).getName());
  }

  @Test
  def testWhereParameter() {
    val param = 90;
    val customers = streamAll(em, classOf[Customer])
      .where(c => c.getDebt() < param)
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.debt < :param0", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Eve", customers(0).getName());
  }

  @Test
  def testWhereParameterChainedString() {
    val param = "UK";
    val customers = streamAll(em, classOf[Customer])
      .select(c => (c.getName(), c.getCountry()))
      .where(p => p._2 == param)
      .select(p => p._1)
      .toList;
    Assert.assertEquals("SELECT A.name FROM Customer A WHERE A.country IS NOT NULL AND A.country = :param0 OR A.country IS NULL AND :param1 IS NULL", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Dave", customers(0));
  }

  @Test
  def testWhereParameters() {
    val paramLower = 150;
    val paramUpper = 250;
    val customers = streamAll(em, classOf[Customer])
      .where(c => c.getDebt() > paramLower && c.getDebt() < paramUpper)
      .toList;
    Assert.assertEquals("SELECT A FROM Customer A WHERE A.debt > :param0 AND A.debt < :param1", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Bob", customers(0).getName());
  }

  @Test
  def testWhereN1Link() {
    val sales = streamAll(em, classOf[Sale])
      .where(s => s.getCustomer().getName() == "Alice")
      .toList;
    Assert.assertEquals("SELECT A FROM Sale A WHERE A.customer.name IS NOT NULL AND A.customer.name = 'Alice' OR A.customer.name IS NULL AND 'Alice' IS NULL", query);
    Assert.assertEquals(2, sales.length);
    Assert.assertEquals("Alice", sales(0).getCustomer().getName());
  }

  @Test
  def testWhereN1Links() {
    val sales = streamAll(em, classOf[Sale])
      .where(s => s.getCustomer().getCountry() == "Switzerland")
      .where(s => s.getCustomer().getDebt() < 150)
      .select(s => (s.getCustomer().getName(), s.getDate()))
      .toList;
    Assert.assertEquals("SELECT A.customer.name, A.date FROM Sale A WHERE (A.customer.country IS NOT NULL AND A.customer.country = 'Switzerland' OR A.customer.country IS NULL AND 'Switzerland' IS NULL) AND A.customer.debt < 150", query);
    Assert.assertEquals(2, sales.length);
    Assert.assertEquals("Alice", sales(0)._1);
  }

  @Test
  def testScalaIteratorFunctions() {
    val customers = streamAll(em, classOf[Customer])
      .filter(c => c.getCountry == "UK")
      .map(c => c.getName)
      .toList
    Assert.assertEquals("SELECT A.name FROM Customer A WHERE A.country IS NOT NULL AND A.country = 'UK' OR A.country IS NULL AND 'UK' IS NULL", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Dave", customers(0));
  }

  @Test
  def testScalaComprehensions() {
    val customers =
      (for (c <- streamAll(em, classOf[Customer]) if c.getCountry == "UK")
        yield c.getName).toList
    Assert.assertEquals("SELECT A.name FROM Customer A WHERE A.country IS NOT NULL AND A.country = 'UK' OR A.country IS NULL AND 'UK' IS NULL", query);
    Assert.assertEquals(1, customers.length);
    Assert.assertEquals("Dave", customers(0));
  }

  @Test
  def testCaching() = {
    // Ensure the base "find all customers" query is in the cache
    val result1 = repeatedQuery(streamAll(em, classOf[Customer]), 1).toList
    val firstQuery = query

    // Repeat the query, and check that we get the exact same string object as the query (showing that it is reused)
    query = null
    val result2 = repeatedQuery(streamAll(em, classOf[Customer]), 2).toList
    val secondQuery = query
    Assert.assertTrue(firstQuery eq secondQuery)
  }

  private def repeatedQuery(it: JinqIterator[Customer], param: Int) = {
    it.where(c => c.getDebt == param)
  }

}
