package org.jinq.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.QueryJinqStream;

/**
 * Creates JinqStreams of JPA entities. 
 */
public class JinqJPAStreamProvider
{
   MetamodelUtil metamodel;
   
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
      return new QueryJinqStream<>(new JPABootstrapQueryComposer<>(
            em, metamodel.entityNameFromClass(entity)));
   }
}
