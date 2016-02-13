package org.jinq.jpa;

import java.sql.DriverManager
import java.sql.SQLException
import java.util.Map
import _root_.scala.collection.mutable.ArrayBuffer
import org.jinq.jpa.test.entities.Lineorder
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import org.jinq.jpa.test.entities.PhoneNumber

object JinqJPAScalaTestBase {
  var entityManagerFactory: EntityManagerFactory = _
  var streams : JinqJPAScalaIteratorProvider = _

   @BeforeClass
   def setUpBeforeClass() {
      entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
      streams = new JinqJPAScalaIteratorProvider(JinqJPAScalaTestBase.entityManagerFactory);
      
      // Hibernate seems to generate incorrect metamodel data for some types of
      // associations, so we have to manually supply the correct information here.
      streams.registerAssociationAttribute(classOf[Lineorder].getMethod("getItem"), "item", false);
      streams.registerAssociationAttribute(classOf[Lineorder].getMethod("getSale"), "sale", false);
      
      // Register types that are used by AttributeConverters
      streams.registerAttributeConverterType(classOf[PhoneNumber]);

      var em = entityManagerFactory.createEntityManager();
      new CreateJpaDb(em).createDatabase();
      em.close();
   }

   @AfterClass
   def tearDownAfterClass() {
      entityManagerFactory.close();
      try {
         DriverManager.getConnection("jdbc:derby:memory:demoDB;drop=true");
      } catch {
        case e: SQLException => { }
      }
   }
   

}
class JinqJPAScalaTestBase {
   var em : EntityManager = null;
   var query : String = null;
   val queryList = new ArrayBuffer[String]();
   
   @Before
   def setUp() {
      em = JinqJPAScalaTestBase.entityManagerFactory.createEntityManager();
      em.getTransaction().begin();
      queryList.clear();
      query = null
      JinqJPAScalaTestBase.streams.setHint("exceptionOnTranslationFail", java.lang.Boolean.TRUE);
      JinqJPAScalaTestBase.streams.setHint("queryLogger", new JPAQueryLogger() {
         def logQuery(q: String,
               positionParameters: Map[Integer, Object] ,
               namedParameters: Map[String, Object] )
         {
            queryList.append(q);
            query = q;
         }});
   }

   @After
   def tearDown() {
      em.getTransaction().rollback();
      em.close();
   }
}
