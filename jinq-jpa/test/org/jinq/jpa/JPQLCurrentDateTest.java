package org.jinq.jpa;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jinq.jpa.test.entities.Sale;
import org.junit.Test;

public class JPQLCurrentDateTest extends JinqJPATestBase
{

	@Test
	public void testCurrentTimeStamp() 
	{
		List<Sale> list = streams.streamAll(em, Sale.class)
	            .where(s -> s.getSqlTimestamp().before(JPQL.currentTimestamp()))
	            .sortedBy(s -> s.getCustomer().getName())
	            .toList();
		
		
		assertEquals("SELECT A FROM Sale A WHERE A.sqlTimestamp < CURRENT_TIMESTAMP ORDER BY A.customer.name ASC", query);
		assertEquals(6, list.size());
		assertEquals("Alice", list.get(0).getCustomer().getName());
	}
	
	@Test
	public void testCurrentTimeStampWithUtilDate() 
	{
		List<Sale> list = streams.streamAll(em, Sale.class)
	            .where(s -> s.getDate().before(JPQL.currentTimestamp()))
	            .sortedBy(s -> s.getCustomer().getName())
	            .toList();
		
		
		assertEquals("SELECT A FROM Sale A WHERE A.date < CURRENT_TIMESTAMP ORDER BY A.customer.name ASC", query);
		assertEquals(6, list.size());
		assertEquals("Alice", list.get(0).getCustomer().getName());
	}
	
	@Test
	public void testCurrentTime() 
	{
		List<Sale> list = streams.streamAll(em, Sale.class)
	            .where(s -> s.getSqlTime().before(JPQL.currentTime()))
	            .sortedBy(s -> s.getCustomer().getName())
	            .toList();
		
		
		assertEquals("SELECT A FROM Sale A WHERE A.sqlTime < CURRENT_TIME ORDER BY A.customer.name ASC", query);
		assertEquals(6, list.size());
		assertEquals("Alice", list.get(0).getCustomer().getName());
	}
	
	@Test
	public void testCurrentDate() 
	{
		List<Sale> list = streams.streamAll(em, Sale.class)
	            .where(s -> s.getSqlDate().before(JPQL.currentDate()))
	            .sortedBy(s -> s.getCustomer().getName())
	            .toList();
		
		
		assertEquals("SELECT A FROM Sale A WHERE A.sqlDate < CURRENT_DATE ORDER BY A.customer.name ASC", query);
		assertEquals(6, list.size());
		assertEquals("Alice", list.get(0).getCustomer().getName());
	}
}
