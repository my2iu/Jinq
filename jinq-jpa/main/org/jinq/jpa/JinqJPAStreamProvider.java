package org.jinq.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Metamodel;

import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.QueryJinqStream;

/**
 * Creates JinqStreams of JPA entities. 
 */
public class JinqJPAStreamProvider
{
   MetamodelUtil metamodel;
   JinqJPAHints hints = new JinqJPAHints();
   
   public JinqJPAStreamProvider(EntityManagerFactory factory)
   {
      this(factory.getMetamodel());
   }

   public JinqJPAStreamProvider(Metamodel metamodel)
   {
      this.metamodel = new MetamodelUtil(metamodel);
   }

   /**
    * Returns a stream of all the entities of a particular type in a
    * database.
    * @param em EntityManager connection to use to access the database
    * @param entity type of the entity
    * @return a stream of the results of querying the database for all
    *    entities of the given type.
    */
   public <U> JinqStream<U> streamAll(EntityManager em, Class<U> entity)
   {
      return new QueryJinqStream<>(JPAQueryComposer.findAllEntities(
            metamodel, em, hints, metamodel.entityNameFromClass(entity)));
   }

   /**
    * Sets a hint for how queries should be executed by Jinq
    * @param name 
    * @param val
    */
   public void setHint(String name, Object val)
   {
      hints.setHint(name, val);
   }
}
