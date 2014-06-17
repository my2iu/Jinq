package org.jinq.jooq;

import static org.jinq.jooq.test.generated.Tables.CUSTOMERS;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.List;

import org.jinq.jooq.test.generated.App;
import org.jinq.jooq.test.generated.tables.records.CustomersRecord;
import org.jinq.tuples.Pair;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JinqJooqTest
{
   Connection con;
   DSLContext context;
   JinqJooqContext jinq;
   
   static String dbName = JinqJooqTest.class.getName(); 
   
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
   public void testBasicFrom()
   {
      List<CustomersRecord> results = jinq.from(CUSTOMERS)
         .selectAll().toList();
      Collections.sort(results, (c1, c2) -> c1.getName().compareTo(c2.getName()));
      assertEquals(5, results.size());
      assertEquals(1, (int)results.get(0).getCustomerid());
      assertEquals("Alice", results.get(0).getName());
   }

   @Test
   public void testWhereBasic()
   {
      List<CustomersRecord> results = jinq.from(CUSTOMERS)
            .where( c -> c.getCountry().equals("UK"))
            .selectAll().toList();
      assertEquals(1, results.size());
      assertEquals("Dave", results.get(0).getName());
   }

   @Test
   public void testWhereBasic2()
   {
      List<CustomersRecord> results = jinq.from(CUSTOMERS)
            .where( c -> c.getDebt() > c.getSalary())
            .selectAll().toList();
      assertEquals(1, results.size());
      assertEquals("Carol", results.get(0).getName());
   }

   @Test
   public void testWhereBasic3()
   {
      List<CustomersRecord> results = jinq.from(CUSTOMERS)
            .where( c -> c.getDebt() <= 10 )
            .selectAll().toList();
      assertEquals(1, results.size());
      assertEquals("Eve", results.get(0).getName());
   }

   @Test
   public void testWhereParameter()
   {
      int amount = 10;
      List<CustomersRecord> results = jinq.from(CUSTOMERS)
            .where( c -> c.getDebt() <= amount )
            .selectAll().toList();
      assertEquals(1, results.size());
      assertEquals("Eve", results.get(0).getName());
   }

   @Test
   public void testSelect()
   {
      List<String> results = jinq.from(CUSTOMERS)
            .where( c -> c.getName().equals("Alice") )
            .select( c -> c.getName() ).toList();
      assertEquals(1, results.size());
      assertEquals("Alice", results.get(0));
   }

   @Test
   public void testSelectMath()
   {
      List<Integer> results = jinq.from(CUSTOMERS)
            .where( c -> c.getName().equals("Alice") )
            .select( c -> c.getDebt() * 2 + 5 ).toList();
      assertEquals(1, results.size());
      assertEquals(205, (int)results.get(0));
   }

   @Test
   public void testSelectTuple()
   {
      List<Pair<String, Integer>> results = jinq.from(CUSTOMERS)
            .where( c -> c.getName().equals("Alice") )
            .select( c -> new Pair<>(c.getName(), c.getDebt())).toList();
      assertEquals(1, results.size());
      assertEquals("Alice", results.get(0).getOne());
      assertEquals(100, (int)results.get(0).getTwo());
   }

   @Test
   public void testSelectTable()
   {
      List<CustomersRecord> results = jinq.from(CUSTOMERS)
            .where( c -> c.getName().equals("Alice") )
            .select( c -> c ).toList();
      assertEquals(1, results.size());
      assertEquals("Alice", results.get(0).getName());
   }
   
   @Test
   public void testJooq() 
   {
      Result<Record> result = context.select().from(CUSTOMERS).fetch();
      for (Record r : result) {
         Integer id = r.getValue(CUSTOMERS.CUSTOMERID);
         String name = r.getValue(CUSTOMERS.NAME);

         System.out.println("ID: " + id + " name: " + name);
     }
   }
}
