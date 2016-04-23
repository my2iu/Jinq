package org.jinq.hibernate;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.jinq.hibernate.JinqHibernateStreamProvider;
import org.jinq.hibernate.test.entities.Lineorder;
import org.jinq.hibernate.test.entities.PhoneNumber;
import org.jinq.jpa.JPAQueryLogger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class JinqJPATestBase
{
   // TODO: If these are static, then multiple tests can't be run
   //   simultaneously.
   static SessionFactory sessionFactory;
//   static EntityManagerFactory entityManagerFactory;
   static JinqHibernateStreamProvider streams;

//   EntityManager em;
   Session em;
   String query;
   List<String> queryList = new ArrayList<>();
   
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      Configuration configuration = new Configuration().configure("META-INF/hibernate.cfg.xml");
//      ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
//         .applySettings(configuration.getProperties())
//         .build();
//      sessionFactory = configuration.buildSessionFactory(serviceRegistry);
      // Hibernate keeps changing how it's configured. The old way no longer works, apparently.
      sessionFactory = configuration.buildSessionFactory();
      streams = new JinqHibernateStreamProvider(sessionFactory);

      // Hibernate's ClassMetadata doesn't have as much information as the Criteria API 
      // metamodel, so I'm going to have to manually specify some of the associations
      
      // Oddly enough, these two attributes are the same ones incorrectly encoded in 
      // Hibernate's Criteria API metamodel as well.
      streams.registerAssociationAttribute(Lineorder.class.getMethod("getItem"), "item", false);
      streams.registerAssociationAttribute(Lineorder.class.getMethod("getSale"), "sale", false);
      
      // Register types that are used by AttributeConverters
      streams.registerAttributeConverterType(PhoneNumber.class);

      Session session = sessionFactory.openSession();
      new CreateHibernateDb(session).createDatabase();
      session.close();

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      sessionFactory.close();
      try {
         DriverManager.getConnection("jdbc:derby:memory:demoDB;drop=true");
      } catch (SQLException e) { }
   }
   
   @Before
   public void setUp() throws Exception
   {
      em = sessionFactory.openSession();
      em.beginTransaction();
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
