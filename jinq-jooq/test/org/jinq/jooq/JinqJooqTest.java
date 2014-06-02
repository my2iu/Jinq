package org.jinq.jooq;

import static org.jinq.jooq.test.generated.Tables.CUSTOMERS;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

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
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      Connection con = DriverManager.getConnection("jdbc:derby:memory:demoDB;create=true");
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
      con = DriverManager.getConnection("jdbc:derby:memory:demoDB;create=true");
   }

   @After
   public void tearDown() throws Exception
   {
      con.close();
   }

   @Test
   public void testJooq() 
   {
      DSLContext create = DSL.using(con, SQLDialect.DERBY);
      Result<Record> result = create.select().from(CUSTOMERS).fetch();
      for (Record r : result) {
         Integer id = r.getValue(CUSTOMERS.CUSTOMERID);
         String name = r.getValue(CUSTOMERS.NAME);

         System.out.println("ID: " + id + " name: " + name);
     }
   }
}
