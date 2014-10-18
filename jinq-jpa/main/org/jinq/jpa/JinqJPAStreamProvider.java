package org.jinq.jpa;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Metamodel;

import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.transform.JPAQueryComposerCache;
import org.jinq.jpa.transform.MetamodelUtil;
import org.jinq.jpa.transform.MetamodelUtilAttribute;
import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.QueryJinqStream;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

/**
 * Creates JinqStreams of JPA entities. 
 */
public class JinqJPAStreamProvider
{
   MetamodelUtil metamodel;
   JPAQueryComposerCache cachedQueries = new JPAQueryComposerCache();
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
   public <U> JinqStream<U> streamAll(final EntityManager em, Class<U> entity)
   {
      String entityName = metamodel.entityNameFromClass(entity);
      Optional<JPQLQuery<?>> cachedQuery = hints.useCaching ?
         cachedQuery = cachedQueries.findCachedFindAllEntities(entityName) : null;
      if (cachedQuery == null)
      {
         JPQLQuery<U> query = JPQLQuery.findAllEntities(entityName);
         cachedQuery = Optional.of(query);
         if (hints.useCaching)
            cachedQuery = cachedQueries.cacheFindAllEntities(entityName, cachedQuery);
      }
      JPQLQuery<U> query = (JPQLQuery<U>)cachedQuery.get();
      return new QueryJinqStream<>(JPAQueryComposer.findAllEntities(
                  metamodel, cachedQueries, em, hints, query),
            new InQueryStreamSource() {
               @Override public <S> JinqStream<S> stream(Class<S> entityClass) {
                  return streamAll(em, entityClass);
               }});
   }

   /**
    * Sets a hint for how queries should be executed by Jinq
    * @param name 
    * @param val
    * @return true if the hint was valid
    */
   public boolean setHint(String name, Object val)
   {
      return hints.setHint(name, val);
   }
   
   /**
    * The Hibernate metamodel seems to hold incorrect information about
    * composite keys or entities that use other entities as keys or something.
    * This method provides a way for programmers to specify correct 
    * information for those types of mappings.
    * @param m entity method that Jinq should rewrite into a field access for queries
    * @param fieldName name of the field that Jinq should use in queries when it encounters the method call
    * @param isPlural whether the method returns a single entity or a collection of them
    */
   public void registerAssociationAttribute(Method m, String fieldName, boolean isPlural)
   {
      MetamodelUtilAttribute attrib = new MetamodelUtilAttribute(fieldName, true);
      metamodel.insertAssociationAttribute(
            new MethodSignature(
                  org.objectweb.asm.Type.getInternalName(m.getDeclaringClass()),
                  m.getName(),
                  org.objectweb.asm.Type.getMethodDescriptor(m)),
            attrib, isPlural);
   }
}
