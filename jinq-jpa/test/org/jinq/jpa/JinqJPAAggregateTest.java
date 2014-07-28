package org.jinq.jpa;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.Lineorder;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.jpa.test.entities.Supplier;
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
      assertEquals("SELECT SUM(A.salary * (A.debt)) FROM Customer A", query);
   }

   @Test(expected=IllegalArgumentException.class)
   public void testSumJoinCast()
   {
      // TODO: In order to support certain useful operations, we're going to need a way to 
      // support casts even if the underlying JPQL doesn't.
      assertEquals(1, (double)streams.streamAll(em, Lineorder.class)
            .sumDouble(lo -> lo.getQuantity() * lo.getItem().getSaleprice()), 0.001);
      assertEquals("SELECT SUM(A.quantity * (A.item.saleprice)) FROM Lineorder A", query);
   }
}
