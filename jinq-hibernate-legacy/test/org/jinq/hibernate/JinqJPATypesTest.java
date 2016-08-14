package org.jinq.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.exception.SQLGrammarException;
import org.jinq.hibernate.test.entities.CreditCard;
import org.jinq.hibernate.test.entities.Customer;
import org.jinq.hibernate.test.entities.Item;
import org.jinq.hibernate.test.entities.ItemType;
import org.jinq.hibernate.test.entities.Lineorder;
import org.jinq.hibernate.test.entities.PhoneNumber;
import org.jinq.hibernate.test.entities.Sale;
import org.jinq.hibernate.test.entities.Supplier;
import org.jinq.jpa.JPQL;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.junit.Test;

public class JinqJPATypesTest extends JinqJPATestBase
{
   @Test
   public void testString()
   {
      String val = "UK";
      List<Pair<Customer, String>> customers = streams.streamAll(em, Customer.class)
            .where(c -> c.getCountry().equals(val) || c.getName().equals("Alice"))
            .select(c -> new Pair<>(c, c.getName()))
            .toList();
      customers = customers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.name FROM org.jinq.hibernate.test.entities.Customer A WHERE A.country = :param0 OR A.name = 'Alice'", query);
      assertEquals(2, customers.size());
      assertEquals("Alice", customers.get(0).getTwo());
      assertEquals("Dave", customers.get(1).getTwo());
   }

   @Test
   public void testStringEscape()
   {
      JinqStream<String> customers = streams.streamAll(em, Customer.class)
            .select(c -> "I didn't know \\''");
      assertEquals("SELECT 'I didn''t know \\''''' FROM org.jinq.hibernate.test.entities.Customer A", customers.getDebugQueryString());
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
      assertEquals("SELECT A, A.salary FROM org.jinq.hibernate.test.entities.Customer A WHERE A.salary + 5 + :param0 < 212", query);
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
      assertEquals("SELECT A, A.revenue FROM org.jinq.hibernate.test.entities.Supplier A WHERE A.revenue + 1000 + :param0 < 10000000", query);
      assertEquals(2, suppliers.size());
      assertEquals("HW Supplier", suppliers.get(0).getOne().getName());
      assertEquals(500, (long)suppliers.get(0).getTwo());
      assertEquals("Talent Agency", suppliers.get(1).getOne().getName());
   }

   @Test
   public void testFloat()
   {
      float val = 1;
      List<Pair<Supplier, Float>> suppliers = streams.streamAll(em, Supplier.class)
            .where(s -> s.getPaymentDiscount() > s.getPaymentDiscount() + val + 2)
            .select(s -> new Pair<>(s, s.getPaymentDiscount()))
            .toList();
      suppliers = suppliers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.paymentDiscount FROM org.jinq.hibernate.test.entities.Supplier A WHERE A.paymentDiscount > A.paymentDiscount + :param0 + 2.0", query);
      assertEquals(0, suppliers.size());
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
      assertEquals("SELECT A, A.saleprice FROM org.jinq.hibernate.test.entities.Item A WHERE A.saleprice > A.purchaseprice + :param0 + 2.0", query);
      assertEquals(2, items.size());
      assertEquals("Talent", items.get(0).getOne().getName());
      assertEquals("Widgets", items.get(1).getOne().getName());
      assertTrue(Math.abs((double)items.get(1).getTwo() - 10) < 0.1);
   }
   
   private Date getBogusDate(int year, int month, int day)
   {
      // Performs a bogus conversion of a year-month-day into an old Java Date type
      return Date.from(java.sql.Timestamp.valueOf(LocalDateTime.of(year, month, day, 1, 0)).toInstant());
   }

   private java.sql.Date getBogusSqlDate(int year, int month, int day)
   {
      // All the Java sql date and time stuff is ambiguous about timezones, so we're basically just ignoring major issues here
      return java.sql.Date.valueOf(LocalDate.of(year, month, day));
   }

   private java.sql.Time getBogusSqlTime(int hour, int minute)
   {
      // All the Java sql date and time stuff is ambiguous about timezones, so we're basically just ignoring major issues here
      return java.sql.Time.valueOf(LocalTime.of(hour, minute));
   }

   private java.sql.Timestamp getBogusSqlTimestamp(int year, int month, int day, int hour, int minute)
   {
      // All the Java sql date and time stuff is ambiguous about timezones, so we're basically just ignoring major issues here
      return java.sql.Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute));
   }

   @Test
   public void testDate()
   {
      Date val = getBogusDate(2002, 1, 1);
      List<Pair<Customer, Date>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getDate().before(val))
            .select(s -> new Pair<>(s.getCustomer(), s.getDate()))
            .toList();
      assertEquals("SELECT A.customer, A.date FROM org.jinq.hibernate.test.entities.Sale A WHERE A.date < :param0", query);
      assertEquals(1, sales.size());
      assertEquals("Dave", sales.get(0).getOne().getName());
      assertEquals(2001, LocalDateTime.ofInstant(sales.get(0).getTwo().toInstant(), ZoneOffset.UTC).getYear());
   }

   @Test
   public void testDateEquals()
   {
      Date val = getBogusDate(2001, 1, 1);
      List<String> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getDate().equals(val))
            .select(s -> s.getCustomer().getName())
            .toList();
      assertEquals("SELECT A.customer.name FROM org.jinq.hibernate.test.entities.Sale A WHERE A.date = :param0", query);
      assertEquals(1, sales.size());
      assertEquals("Dave", sales.get(0));
   }

   @Test
   public void testCalendar()
   {
      Calendar val = Calendar.getInstance();
      val.setTime(getBogusDate(2002, 1, 1));
      Calendar val2 = Calendar.getInstance();
      val2.setTime(getBogusDate(2003, 1, 1));
      List<Pair<Customer, Calendar>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getCalendar().before(val) || s.getCalendar().equals(val2))
            .select(s -> new Pair<>(s.getCustomer(), s.getCalendar()))
            .toList();
      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A.customer, A.calendar FROM org.jinq.hibernate.test.entities.Sale A WHERE A.calendar < :param0 OR A.calendar = :param1", query);
      assertEquals(2, sales.size());
      assertEquals("Dave", sales.get(0).getOne().getName());
      assertEquals("Carol", sales.get(1).getOne().getName());
   }

   @Test
   public void testSqlDate()
   {
      java.sql.Date val = getBogusSqlDate(2002, 1, 1);
      java.sql.Date val2 = getBogusSqlDate(2003, 1, 1);
      List<Pair<Customer, java.sql.Date>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getSqlDate().before(val) || s.getSqlDate().equals(val2))
            .select(s -> new Pair<>(s.getCustomer(), s.getSqlDate()))
            .toList();
      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A.customer, A.sqlDate FROM org.jinq.hibernate.test.entities.Sale A WHERE A.sqlDate < :param0 OR A.sqlDate = :param1", query);
      assertEquals(2, sales.size());
      assertEquals("Dave", sales.get(0).getOne().getName());
      assertEquals("Carol", sales.get(1).getOne().getName());
   }

   @Test
   public void testSqlTime()
   {
      java.sql.Time val = getBogusSqlTime(6, 0);
      java.sql.Time val2 = getBogusSqlTime(5, 0);
      List<Pair<Customer, java.sql.Time>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getSqlTime().after(val) || s.getSqlTime().equals(val2))
            .select(s -> new Pair<>(s.getCustomer(), s.getSqlTime()))
            .toList();
      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A.customer, A.sqlTime FROM org.jinq.hibernate.test.entities.Sale A WHERE A.sqlTime > :param0 OR A.sqlTime = :param1", query);
      assertEquals(2, sales.size());
      assertEquals("Carol", sales.get(0).getOne().getName());
      assertEquals("Alice", sales.get(1).getOne().getName());
   }

   @Test
   public void testSqlTimestamp()
   {
      java.sql.Timestamp val = getBogusSqlTimestamp(2002, 1, 1, 1, 0);
      java.sql.Timestamp val2 = getBogusSqlTimestamp(2003, 1, 1, 1, 0);
      List<Pair<Customer, java.sql.Timestamp>> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getSqlTimestamp().before(val) || s.getSqlTimestamp().equals(val2))
            .select(s -> new Pair<>(s.getCustomer(), s.getSqlTimestamp()))
            .toList();
      sales = sales.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A.customer, A.sqlTimestamp FROM org.jinq.hibernate.test.entities.Sale A WHERE A.sqlTimestamp < :param0 OR A.sqlTimestamp = :param1", query);
      assertEquals(2, sales.size());
      assertEquals("Dave", sales.get(0).getOne().getName());
      assertEquals("Carol", sales.get(1).getOne().getName());
   }

   @Test(expected=SQLGrammarException.class)  // Hibernate generating code that Derby can't handle related to booleans--TODO: investigate further later
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
   }
   
   @Test(expected=SQLGrammarException.class)  // Hibernate generating code that Derby can't handle related to booleans--TODO: investigate further later
   public void testBooleanOperations()
   {
      // Comparisons in a SELECT must be converted to a CASE...WHEN... or something
      // TODO: CASE...WHEN... is now done, and I've inserted a little hack to convert the 1 and 0 constants 
      //    into booleans, but EclipseLink is treating TRUE and FALSE and integers in the return type.
      List<Pair<Supplier, Boolean>> suppliers = streams.streamAll(em, Supplier.class)
            .where(s -> s.getHasFreeShipping())
            .select(s -> new Pair<>(s, s.getHasFreeShipping() != true))
            .toList();
      assertTrue("SELECT A, CASE WHEN NOT A.hasFreeShipping = TRUE THEN TRUE ELSE FALSE END FROM Supplier A WHERE A.hasFreeShipping = TRUE".equals(query)
            || "SELECT A, CASE WHEN A.hasFreeShipping = TRUE THEN FALSE ELSE TRUE END FROM Supplier A WHERE A.hasFreeShipping = TRUE".equals(query));
      assertEquals(1, suppliers.size());
      assertEquals("Talent Agency", suppliers.get(0).getOne().getName());
      assertTrue(!suppliers.get(0).getTwo());
   }
   
   @Test
   public void testBooleanJavaBeanNaming()
   {
      // Direct access to boolean variables in a WHERE must be converted to a comparison 
      JinqStream<Pair<Supplier, Boolean>> supplierStream = streams.streamAll(em, Supplier.class)
            .where(s -> s.isPreferredSupplier())
            .select(s -> new Pair<>(s, s.isPreferredSupplier()));
      assertEquals("SELECT A, A.preferredSupplier FROM org.jinq.hibernate.test.entities.Supplier A WHERE A.preferredSupplier = TRUE", supplierStream.getDebugQueryString());
      List<Pair<Supplier, Boolean>> suppliers = supplierStream.toList();
      assertEquals("SELECT A, A.preferredSupplier FROM org.jinq.hibernate.test.entities.Supplier A WHERE A.preferredSupplier = TRUE", query);
      assertEquals(1, suppliers.size());
      assertEquals("Conglomerate", suppliers.get(0).getOne().getName());
      assertTrue(suppliers.get(0).getTwo());

      List<Pair<Sale, Boolean>> rushSales = streams.streamAll(em, Sale.class)
            .where(s -> s.isRush())
            .select(s -> new Pair<>(s, s.isRush()))
            .toList();
      assertEquals("SELECT A, A.rush FROM org.jinq.hibernate.test.entities.Sale A WHERE A.rush = TRUE", query);
      assertEquals(1, rushSales.size());
      assertEquals("Carol", rushSales.get(0).getOne().getCustomer().getName());
      assertTrue(rushSales.get(0).getTwo());
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
      assertEquals("SELECT A, A.type FROM org.jinq.hibernate.test.entities.Item A WHERE A.type = :param0 OR A.type = org.jinq.hibernate.test.entities.ItemType.BIG", query);
      assertEquals(2, items.size());
      assertEquals("Lawnmowers", items.get(0).getOne().getName());
      assertEquals("Talent", items.get(1).getOne().getName());
      assertEquals(ItemType.OTHER, items.get(1).getTwo());
   }
   
   @Test
   public void testConverter()
   {
      PhoneNumber val = new PhoneNumber("1", "555", "5552222");
      List<Pair<Customer, PhoneNumber>> phones= streams.streamAll(em, Customer.class)
            .where(c -> c.getPhone().equals(val))
            .select(c -> new Pair<>(c, c.getPhone()))
            .toList();
      phones = phones.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.phone FROM org.jinq.hibernate.test.entities.Customer A WHERE A.phone = :param0", query);
      assertEquals(1, phones.size());
      assertEquals("Bob", phones.get(0).getOne().getName());
      assertEquals(val, phones.get(0).getTwo());
   }

   @Test(expected=Exception.class)  // Hibernate generating code that Derby can't handle--TODO: investigate further later
   public void testBigDecimal()
   {
      BigDecimal val = BigDecimal.valueOf(4);
      BigDecimal val2 = BigDecimal.valueOf(2);
      List<Pair<Lineorder, BigDecimal>> lineorders = streams.streamAll(em, Lineorder.class)
            .where(c -> c.getTotal().compareTo(val.multiply(val2)) < 0)
            .select(c -> new Pair<>(c, c.getTotal()))
            .toList();
      lineorders = lineorders.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A, A.total FROM Lineorder A WHERE A.total < :param0 * :param1", query);
      assertEquals(5, lineorders.size());
      assertEquals(1, lineorders.get(0).getTwo().intValue());
      assertEquals(5, lineorders.get(3).getTwo().intValue());
   }

   @Test
   public void testBigInteger()
   {
      BigInteger val = BigInteger.valueOf(3600);
      BigInteger val2 = BigInteger.valueOf(500);
      List<Pair<Lineorder, BigInteger>> lineorders = streams.streamAll(em, Lineorder.class)
            .where(c -> c.getTransactionConfirmation().add(val2).compareTo(val) < 0)
            .select(c -> new Pair<>(c, c.getTransactionConfirmation()))
            .toList();
      lineorders = lineorders.stream().sorted((a, b) -> a.getTwo().compareTo(b.getTwo())).collect(Collectors.toList());
      assertEquals("SELECT A, A.transactionConfirmation FROM org.jinq.hibernate.test.entities.Lineorder A WHERE A.transactionConfirmation + :param0 < :param1", query);
      assertEquals(3, lineorders.size());
      assertEquals(1000, lineorders.get(0).getTwo().intValue());
      assertEquals(3000, lineorders.get(2).getTwo().intValue());
   }

   @Test
   public void testBlob()
   {
      List<Pair<Supplier, byte[]>> suppliers = streams.streamAll(em, Supplier.class)
            .select(s -> new Pair<>(s, s.getSignature()))
            .toList();
      suppliers = suppliers.stream().sorted((a, b) -> a.getOne().getName().compareTo(b.getOne().getName())).collect(Collectors.toList());
      assertEquals("SELECT A, A.signature FROM org.jinq.hibernate.test.entities.Supplier A", query);
      assertEquals(3, suppliers.size());
      assertEquals("Conglomerate", Charset.forName("UTF-8").decode(ByteBuffer.wrap(suppliers.get(0).getTwo())).toString());
   }

   @Test
   public void testEntity()
   {
      Item widgets = streams.streamAll(em, Item.class).where(i -> i.getName().equals("Widgets")).getOnlyValue();
      List<Lineorder> orders = streams.streamAll(em, Lineorder.class)
            .where(lo -> lo.getItem() == widgets)
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Lineorder A WHERE A.item = :param0", query);
      assertEquals(3, orders.size());
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void testEntityField()
   {
      Item widgets = streams.streamAll(em, Item.class).where(i -> i.getName().equals("Widgets")).getOnlyValue();
      streams.streamAll(em, Lineorder.class)
            .where(lo -> lo.getItem().getName().equals(widgets.getName()));
   }

   @Test(expected=ClassCastException.class) // Hibernate is doing numeric promotion incorrectly
   public void testDivide()
   {
      double val = 5.0;
      List<Double> resultDouble = streams.streamAll(em, Customer.class)
            .select(c -> val / 2.0).toList();
      assertEquals("SELECT :param0 / 2.0 FROM org.jinq.hibernate.test.entities.Customer A", query);
      assertEquals(2.5, resultDouble.get(0), 0.001);
      
      int valInt = 5;
      List<Integer> resultInteger = streams.streamAll(em, Customer.class)
            .select(c -> valInt / 2).toList();
      assertEquals("SELECT :param0 / 2 FROM org.jinq.hibernate.test.entities.Customer A", query);
      assertEquals(2, (int)resultInteger.get(0));

      resultDouble = streams.streamAll(em, Customer.class)
            .select(c -> val * 2.0 / valInt)
            .sortedBy( num -> num ).toList();
      assertEquals("SELECT :param0 * 2.0 / :param1 FROM org.jinq.hibernate.test.entities.Customer A ORDER BY :param0 * 2.0 / :param1 ASC", query);
      assertEquals(2.0, resultDouble.get(0), 0.001);
   }
   
   @Test(expected=Exception.class)  // Hibernate generating code that Derby can't handle--TODO: investigate further later
   public void testJPQLWeirdness()
   {
      // EclipseLink seems to think two parameters divided by each other results in a BigDouble.
      // Perhaps the problem is that EclipseLink cannot determine the type until after the
      // parameters are substituted in, but it performs its type checking earlier than that?
      double val = 5.0;
      int valInt = 5;
      List<Double> resultDouble = streams.streamAll(em, Customer.class)
            .select(c -> val / valInt).toList();
      assertEquals("SELECT :param0 / :param1 FROM Customer A", query);
      assertEquals(1.0, resultDouble.get(0).doubleValue(), 0.001);
   }

   @Test//(expected=IllegalArgumentException.class)
   public void testJPQLWeirdness2()
   {
      // EclipseLink seems to have problems when ordering things without using
      // any fields of data.
      double val = 5.0;
      List<Customer> results = streams.streamAll(em, Customer.class)
            .sortedBy(c -> 5 * val).toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Customer A ORDER BY 5.0 * :param0 ASC", query);
      assertEquals(5, results.size());
   }

   @Test
   public void testNumericPromotionMath()
   {
      List<Lineorder> lineorders = streams.streamAll(em, Lineorder.class)
            .sortedBy(lo -> lo.getQuantity() * lo.getItem().getSaleprice())
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Lineorder A ORDER BY A.quantity * A.item.saleprice ASC", query);
      assertEquals(11, lineorders.size());
      assertEquals("Screws", lineorders.get(0).getItem().getName());
   }

   @Test(expected=ClassCastException.class)  // Hibernate doesn't do numeric promotion properly
   public void testNumericPromotionBigDecimal()
   {
      long val = 3;
      List<Double> lineorders = streams.streamAll(em, Lineorder.class)
            .select(lo -> (lo.getTotal().add(new BigDecimal(val))).doubleValue() + lo.getItem().getSaleprice())
            .sortedBy(num -> num)
            .toList();
      assertEquals("SELECT A.total + :param0 + A.item.saleprice FROM Lineorder A ORDER BY A.total + :param0 + A.item.saleprice ASC", query);
      assertEquals(11, lineorders.size());
      assertEquals(6, lineorders.get(0).longValue());
   }

   @Test
   public void testNumericPromotionBigInteger()
   {
      List<BigInteger> lineorders = streams.streamAll(em, Lineorder.class)
            .select(lo -> lo.getTransactionConfirmation().add(BigInteger.valueOf(lo.getQuantity())))
            .sortedBy(num -> num)
            .toList();
      assertEquals("SELECT A.transactionConfirmation + A.quantity FROM org.jinq.hibernate.test.entities.Lineorder A ORDER BY A.transactionConfirmation + A.quantity ASC", query);
      assertEquals(11, lineorders.size());
      assertEquals(1001, lineorders.get(0).longValue());
   }
   
   @Test
   public void testNumericPromotionComparison()
   {
      List<Lineorder> lineorders = streams.streamAll(em, Lineorder.class)
            .where(lo -> lo.getQuantity() + 20 < lo.getItem().getSaleprice())
            .sortedBy(lo -> lo.getItem().getName())
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Lineorder A WHERE A.quantity + 20 < A.item.saleprice ORDER BY A.item.name ASC", query);
      assertEquals(3, lineorders.size());
      assertEquals("Lawnmowers", lineorders.get(0).getItem().getName());
   }
   
   @Test
   public void testNull()
   {
      // TODO: I'm not sure if translating == NULL to "IS NULL" is the best route to go
      // Although it is the most intuitive, it isn't consistent with JPQL NULL handling rules
      streams.streamAll(em, Sale.class)
            .where(s -> s.getCustomer() == null)
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Sale A WHERE A.customer IS NULL", query);
   }
   
   @Test
   public void testNonNull()
   {
      streams.streamAll(em, Supplier.class)
            .where(s -> null != s.getCountry())
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Supplier A WHERE A.country IS NOT NULL", query);
   }
   
   @Test(expected=ClassCastException.class) // Don't know what Hibernate is doing here
   public void testCollection()
   {
      ArrayList<String> names = new ArrayList<>();
      names.add("Alice");
      names.add("John");
      List<Customer> customers = streams.streamAll(em, Customer.class)
         .where(c -> JPQL.isInList(c.getName(), names))
         .toList();
      assertEquals("SELECT A FROM Customer A WHERE A.name IN :param0", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());
   }

   @Test(expected=ClassCastException.class) // Don't know what Hibernate is doing here
   public void testCollectionContains()
   {
      // Technically, we can't be sure whether calls to Collection.contains()
      // has weird side-effects that we can't emulate using a database query,
      // but people really want to be able to write code like this, and it's
      // normally safe, so we'll let it go through.
      HashSet<String> names = new HashSet<>();
      names.add("Alice");
      names.add("John");
      List<Customer> customers = streams.streamAll(em, Customer.class)
         .where(c -> names.contains(c.getName()))
         .toList();
      assertEquals("SELECT A FROM Customer A WHERE A.name IN :param0", query);
      assertEquals(1, customers.size());
      assertEquals("Alice", customers.get(0).getName());
   }

   @Test//(expected=javax.persistence.PersistenceException.class)
   public void testCollectionSubQuery()
   {
      // EclipseLink can't seem to handle IN where the elements are entities,
      // but Hibernate is actually ok with it.
      Item widgets = streams.streamAll(em, Item.class).where(i -> i.getName().equals("Widgets")).getOnlyValue();
      List<Supplier> suppliers = streams.streamAll(em, Supplier.class)
            .where(s -> JPQL.listContains(s.getItems(), widgets))
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Supplier A WHERE :param0 IN (SELECT B FROM A.items B)", query);
      assertEquals(2, suppliers.size());
   }

   @Test
   public void testInheritedDate()
   {
      // EclipseLink has a weird behaviour where the Criteria API reports that
      // the type of SignatureSupplier.getSignatureExpiry() contains a java.sql.Timestamp
      // and not a java.util.Date. BUT, the Java member does list the correct type
      // and the member is shown existing in subclasses too with the correct type
      // (but Jinq ignores the versions in the subclasses because the member is
      // attached to the parent).
      List <Date> suppliers = streams.streamAll(em, Supplier.class)
            .select(s -> s.getSignatureExpiry())
            .toList();
      assertEquals("SELECT A.signatureExpiry FROM org.jinq.hibernate.test.entities.Supplier A", query);
      assertEquals(3, suppliers.size());
   }

   @Test
   public void testEmbedded()
   {
      List<Sale> sales = streams.streamAll(em, Sale.class)
            .where(s -> s.getCreditCard().getName().equals("Alice"))
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Sale A WHERE A.creditCard.name = 'Alice'", query);
      assertEquals(2, sales.size());
      assertEquals("Alice", sales.get(0).getCreditCard().getName());
      
      // Use an embedded type as a query parameter
      CreditCard card = new CreditCard();
      card.setName("Dave");
      card.setNumber(123456);
      card.setCvv(123);
      sales = streams.streamAll(em, Sale.class)
            .setHint("isObjectEqualsSafe", true)  // compiler translated .equals() to Object.equals() here for some reason
            .where(s -> s.getCreditCard().equals(card))
            .toList();
      assertEquals("SELECT A FROM org.jinq.hibernate.test.entities.Sale A WHERE A.creditCard = :param0", query);
      assertEquals(1, sales.size());
      assertEquals("Dave", sales.get(0).getCreditCard().getName());
   }
   
   public static class CustomerPurchases
   {
      public CustomerPurchases(String name, long count)
      {
         this.name = name;
         this.count = count;
      }

      
      String name;
      long count;
      
      public String getName() { return name; }
      public long getCount() { return count; }
      
      public static CustomerPurchases from(String name, long count)
      {
         return new CustomerPurchases(name, count);
      }
   }
   
   @Test
   public void testCustomTuple() throws NoSuchMethodException, SecurityException
   {
      streams.registerCustomTupleStaticBuilder(CustomerPurchases.class.getMethod("from", String.class, Long.TYPE));
      List<CustomerPurchases> purchases = streams.streamAll(em, Customer.class)
            .select(c -> CustomerPurchases.from(c.getName(), JinqStream.from(c.getSales()).count()))
            .toList();
      assertEquals("SELECT B.name, (SELECT COUNT(A) FROM B.sales A) FROM org.jinq.hibernate.test.entities.Customer B", query);
      assertEquals(5, purchases.size());
      purchases.sort(Comparator.comparing(cp -> cp.name));
      assertEquals("Alice", purchases.get(0).name);
      assertEquals(2, purchases.get(0).count);
      assertEquals("Carol", purchases.get(2).name);
      assertEquals(2, purchases.get(2).count);
      assertEquals("Eve", purchases.get(4).name);
      assertEquals(1, purchases.get(4).count);
   }
   
   @Test
   public void testCustomTupleReadIndex() throws NoSuchMethodException, SecurityException
   {
      streams.registerCustomTupleStaticBuilder(
            CustomerPurchases.class.getMethod("from", String.class, Long.TYPE),
            CustomerPurchases.class.getMethod("getName"),
            CustomerPurchases.class.getMethod("getCount"));
      List<CustomerPurchases> purchases = streams.streamAll(em, Customer.class)
            .select(c -> CustomerPurchases.from(c.getName(), JinqStream.from(c.getSales()).count()))
            .sortedBy(cp -> cp.getName())
            .toList();
      assertEquals("SELECT B.name, (SELECT COUNT(A) FROM B.sales A) FROM org.jinq.hibernate.test.entities.Customer B ORDER BY B.name ASC", query);
      assertEquals(5, purchases.size());
      assertEquals("Alice", purchases.get(0).name);
      assertEquals(2, purchases.get(0).count);
      assertEquals("Carol", purchases.get(2).name);
      assertEquals(2, purchases.get(2).count);
      assertEquals("Eve", purchases.get(4).name);
      assertEquals(1, purchases.get(4).count);
   }

   @Test
   public void testCustomTupleConstructor() throws NoSuchMethodException, SecurityException
   {
      streams.registerCustomTupleConstructor(
            CustomerPurchases.class.getConstructor(String.class, Long.TYPE),
            CustomerPurchases.class.getMethod("getName"),
            CustomerPurchases.class.getMethod("getCount"));
      List<CustomerPurchases> purchases = streams.streamAll(em, Customer.class)
            .select(c -> new CustomerPurchases(c.getName(), JinqStream.from(c.getSales()).count()))
            .sortedBy(cp -> cp.getName())
            .toList();
      assertEquals("SELECT B.name, (SELECT COUNT(A) FROM B.sales A) FROM org.jinq.hibernate.test.entities.Customer B ORDER BY B.name ASC", query);
      assertEquals(5, purchases.size());
      assertEquals("Alice", purchases.get(0).name);
      assertEquals(2, purchases.get(0).count);
      assertEquals("Carol", purchases.get(2).name);
      assertEquals(2, purchases.get(2).count);
      assertEquals("Eve", purchases.get(4).name);
      assertEquals(1, purchases.get(4).count);
   }
}
