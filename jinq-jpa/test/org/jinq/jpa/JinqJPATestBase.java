package org.jinq.jpa;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jinq.jpa.test.entities.Lineorder;
import org.jinq.jpa.test.entities.PhoneNumber;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class JinqJPATestBase
{
   // TODO: If these are static, then multiple tests can't be run
   //   simultaneously.
   static EntityManagerFactory entityManagerFactory;
   static JinqJPAStreamProvider streams;

   EntityManager em;
   String query;
   List<String> queryList = new ArrayList<>();
   
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
      streams = new JinqJPAStreamProvider(entityManagerFactory);
      
      // Hibernate seems to generate incorrect metamodel data for some types of
      // associations, so we have to manually supply the correct information here.
      streams.registerAssociationAttribute(Lineorder.class.getMethod("getItem"), "item", false);
      streams.registerAssociationAttribute(Lineorder.class.getMethod("getSale"), "sale", false);
      
      // Register types that are used by AttributeConverters
      streams.registerAttributeConverterType(PhoneNumber.class);
      
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
      em.getTransaction().begin();
      queryList.clear();
      streams.setHint("exceptionOnTranslationFail", true);
      streams.setHint("queryLogger", new JPAQueryLogger() {
         @Override public void logQuery(String q,
               Map<Integer, Object> positionParameters,
               Map<String, Object> namedParameters)
         {
            queryList.add(q);
            query = q;
         }});
   }

   @After
   public void tearDown() throws Exception
   {
      em.getTransaction().rollback();
      em.close();
   }
}
