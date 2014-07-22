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

}
