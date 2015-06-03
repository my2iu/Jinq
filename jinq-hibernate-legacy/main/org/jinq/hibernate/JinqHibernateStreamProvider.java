package org.jinq.hibernate;

import java.lang.reflect.Method;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jinq.hibernate.transform.MetamodelUtilFromSessionFactory;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.transform.JPAQueryComposerCache;
import org.jinq.jpa.transform.JPQLQueryTransformConfigurationFactory;
import org.jinq.jpa.transform.LambdaAnalysisFactory;
import org.jinq.jpa.transform.MetamodelUtil;
import org.jinq.jpa.transform.MetamodelUtilAttribute;
import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.JinqStream;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

/**
 * Creates JinqStreams of JPA entities. 
 */
public class JinqHibernateStreamProvider
{
   MetamodelUtil metamodel;
   JPAQueryComposerCache cachedQueries = new JPAQueryComposerCache();
   LambdaAnalysisFactory lambdaAnalyzer = new LambdaAnalysisFactory();
   JPQLQueryTransformConfigurationFactory jpqlQueryTransformConfigurationFactory = new JPQLQueryTransformConfigurationFactory();
   JinqJPAHints hints = new JinqJPAHints();
   
   public JinqHibernateStreamProvider(SessionFactory factory)
   {
      this.metamodel = new MetamodelUtilFromSessionFactory(factory);
   }

//   public JinqHibernateStreamProvider(Metamodel metamodel)
//   {
//      this.metamodel = new MetamodelUtil(metamodel);
//   }
   
   /**
    * Returns a stream of all the entities of a particular type in a
    * database.
    * @param em EntityManager connection to use to access the database
    * @param entity type of the entity
    * @return a stream of the results of querying the database for all
    *    entities of the given type.
    */
   public <U> JPAJinqStream<U> streamAll(final Session em, Class<U> entity)
   {
      String entityName = metamodel.entityNameFromClass(entity);
      Optional<JPQLQuery<?>> cachedQuery = hints.useCaching ?
         cachedQueries.findCachedFindAllEntities(entityName) : null;
      if (cachedQuery == null)
      {
         JPQLQuery<U> query = JPQLQuery.findAllEntities(entityName);
         cachedQuery = Optional.of(query);
         if (hints.useCaching)
            cachedQuery = cachedQueries.cacheFindAllEntities(entityName, cachedQuery);
      }
      JPQLQuery<U> query = (JPQLQuery<U>)cachedQuery.get();
      return new QueryJPAJinqStream<>(HibernateQueryComposer.findAllEntities(
                  metamodel, cachedQueries, lambdaAnalyzer, jpqlQueryTransformConfigurationFactory,
                  em, hints, query),
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
    * information for those types of mappings. Note: this method does not handle
    * inherited methods properly (the method will always be attached to class that
    * the method was declared in). Use the alternate form of this method if you  
    * want to register a method declared in a superclass. 
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
   
   /**
    * This is an alternate version of registerAssociationAttribute() that
    * allows you to manually register a method of a class that is declared in 
    * a superclass.
    * @param m entity method that Jinq should rewrite into a field access for queries
    * @param methodClass the class that the method will be called against
    * @param fieldName name of the field that Jinq should use in queries when it encounters the method call
    * @param isPlural whether the method returns a single entity or a collection of them
    */
   public void registerAssociationAttribute(Method m, Class<?> methodClass, String fieldName, boolean isPlural)
   {
      MetamodelUtilAttribute attrib = new MetamodelUtilAttribute(fieldName, true);
      metamodel.insertAssociationAttribute(
            new MethodSignature(
                  org.objectweb.asm.Type.getInternalName(methodClass),
                  m.getName(),
                  org.objectweb.asm.Type.getMethodDescriptor(m)),
            attrib, isPlural);
   }
}
