package org.jinq.test.query;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jinq.test.entities.DBManager;
import org.jinq.test.entities.EntityManager;

import ch.epfl.labos.iu.orm.queryll2.QueryllAnalyzer;

public class JinqStreamTest
{
   StringWriter savedOutput;
   DBManager db;

   @Before
   public void setUp() throws Exception
   {
      savedOutput = new StringWriter();
      db = new DBManager(new PrintWriter(savedOutput));
      QueryllAnalyzer queryll2 = new QueryllAnalyzer(); 
      queryll2.useRuntimeAnalysis(db);
   }
   
   @After
   public void tearDown()
   {
      db.close();
   }

   @Test
   public void testWhere()
   {
      EntityManager em = db.begin();
      assertEquals("SELECT A.Name AS COL1 FROM Customers AS A",
         em.customerStream()
            .select(c -> c.getName())
            .getDebugQueryString());
      db.end(em, true);
   }

}
