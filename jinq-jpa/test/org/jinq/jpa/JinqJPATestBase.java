package org.jinq.jpa;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class JinqJPATestBase
{
   static EntityManagerFactory entityManagerFactory;
   static JinqJPAStreamProvider streams;

   EntityManager em;
   String query;
   
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
      streams = new JinqJPAStreamProvider(entityManagerFactory);
      EntityManager em = entityManagerFactory.createEntityManager();
      new CreateJpaDb(em).createDatabase();
      em.close();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      entityManagerFactory.close();
      try {
         DriverManager.getConnection("jdbc:derby:memory:demoDB;drop=true");
      } catch (SQLException e) { }
   }
   
   @Before
   public void setUp() throws Exception
   {
      em = entityManagerFactory.createEntityManager();
      streams.setHint("exceptionOnTranslationFail", true);
      streams.setHint("queryLogger", new JPAQueryLogger() {
         @Override public void logQuery(String q,
               Map<Integer, Object> positionParameters,
               Map<String, Object> namedParameters)
         {
            query = q;
         }});
   }

   @After
   public void tearDown() throws Exception
   {
           em.close();
   }
}
