package org.jinq.test.query;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jinq.test.entities.DBManager;
import org.jinq.test.entities.EntityManager;

import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.queryll2.QueryllAnalyzer;

public class JinqStreamTest
{
   StringWriter savedOutput;
   DBManager db;
   EntityManager em;

   @Before
   public void setUp() throws Exception
   {
      savedOutput = new StringWriter();
      db = new DBManager(new PrintWriter(savedOutput));
      QueryllAnalyzer queryll2 = new QueryllAnalyzer(); 
      queryll2.useRuntimeAnalysis(db);
      em = db.begin();
   }
   
   @After
   public void tearDown()
   {
      db.end(em, true);
      db.close();
   }

   @Test
   public void testWhere()
   {
      assertEquals("SELECT A.Name AS COL1 FROM Customers AS A",
         em.customerStream()
            .select(c -> c.getName())
            .getDebugQueryString());
   }

   @Test
   public void testJoin()
   {
      EntityManager em = this.em;
      assertEquals("SELECT A.Name AS COL1, B.Date AS COL2 FROM Customers AS A, Sales AS B",
            em.customerStream()
               .join(c -> em.saleStream())
               .select(pair -> new Pair<>(pair.getOne().getName(),
                                       pair.getTwo().getDate()))
               .getDebugQueryString());
   }
}
