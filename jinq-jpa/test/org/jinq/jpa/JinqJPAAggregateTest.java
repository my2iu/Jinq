package org.jinq.jpa;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.junit.Test;

public class JinqJPAAggregateTest extends JinqJPATestBase
{
   @Test
   public void testCount()
   {
      long count = streams.streamAll(em, Customer.class)
            .count();
      assertEquals("SELECT A.customer.name, A.date FROM Sale A WHERE A.customer.country = 'Switzerland' AND (A.customer.debt < 150)", query);
      assertEquals(2, count);
   }
   
   @Test
   public void testCountWhere()
   {
      long count = streams.streamAll(em, Customer.class)
            .where(c -> c.getCountry().equals("UK") )
            .count();
      assertEquals("SELECT A.customer.name, A.date FROM Sale A WHERE A.customer.country = 'Switzerland' AND (A.customer.debt < 150)", query);
      assertEquals(2, count);
   }

}
