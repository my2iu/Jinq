package org.jinq.jooq;

import static org.jinq.jooq.test.generated.Tables.CUSTOMERS;
import static org.jinq.jooq.test.generated.Tables.SALES;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.jinq.jooq.test.generated.App;
import org.jinq.jooq.test.generated.tables.records.CustomersRecord;
import org.jinq.jooq.test.generated.tables.records.SalesRecord;
import org.jinq.tuples.Pair;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JinqJooqFromTest
{
   Connection con;
   DSLContext context;
   JinqJooqContext jinq;
   
   static String dbName = JinqJooqFromTest.class.getName(); 

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      Connection con = DriverManager.getConnection("jdbc:derby:memory:" + dbName + ";create=true");
      new CreateJdbcDb(con).createDatabase();
      con.close();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
   }

   @Before
   public void setUp() throws Exception
   {
      con = DriverManager.getConnection("jdbc:derby:memory:" + dbName);
      context = DSL.using(con, SQLDialect.DERBY);
      jinq = JinqJooqContext.using(context, App.APP);
   }

   @After
   public void tearDown() throws Exception
   {
      con.close();
   }

   @Test
   public void testFrom()
   {
      List<Pair<CustomersRecord, SalesRecord>> results = 
            jinq.from(CUSTOMERS, SALES).selectAll().toList();
      assertEquals(30, results.size());
   }

   @Test
   public void testFromWhere()
   {
      List<Pair<CustomersRecord, SalesRecord>> results = 
            jinq.from(CUSTOMERS, SALES)
            .where((c, s) -> c.getCustomerid() == s.getCustomerid()
                  && c.getName().equals("Alice"))
            .selectAll().toList();
      assertEquals(2, results.size());
      assertEquals("Alice", results.get(0).getOne().getName());
      assertEquals("Alice", results.get(1).getOne().getName());
   }

   @Test
   public void testFromSelect()
   {
      List<Pair<String, SalesRecord>> results = 
            jinq.from(CUSTOMERS, SALES)
            .where((c, s) -> c.getCustomerid() == s.getCustomerid()
                  && s.getDate().equals("2003"))
            .select((c, s) -> new Pair<>(c.getName(), s))
            .toList();
      assertEquals(1, results.size());
      assertEquals("Carol", results.get(0).getOne());
   }
}
