package org.jinq.jpa;

import java.lang.reflect.Method
import java.util.Optional

import org.jinq.jpa.jpqlquery.JPQLQuery
import org.jinq.jpa.transform.JPAQueryComposerCache
import org.jinq.jpa.transform.MetamodelUtilAttribute
import org.jinq.jpa.transform.ScalaJPQLQueryTransformConfigurationFactory
import org.jinq.jpa.transform.ScalaLambdaAnalysisFactory
import org.jinq.jpa.transform.ScalaMetamodelUtil
import org.jinq.orm.stream.scala.InQueryStreamSource
import org.jinq.orm.stream.scala.JinqIterator
import org.jinq.rebased.org.objectweb.asm.Type

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.metamodel.Metamodel

/**
 * Creates a JinqIterator of JPA entities.
 */
class JinqJPAScalaIteratorProvider(_metamodel: Metamodel) {
  protected var metamodel: ScalaMetamodelUtil = new ScalaMetamodelUtil(_metamodel);
  protected val cachedQueries: JPAQueryComposerCache = new JPAQueryComposerCache();
  protected var hints: JinqJPAHints = new JinqJPAHints();
  hints.isObjectEqualsSafe = true;
  protected val lambdaAnalyzer: ScalaLambdaAnalysisFactory = new ScalaLambdaAnalysisFactory();
  protected val jpqlQueryTransformConfigurationFactory: ScalaJPQLQueryTransformConfigurationFactory = new ScalaJPQLQueryTransformConfigurationFactory();

  def this(factory: EntityManagerFactory) {
    this(factory.getMetamodel());
  }

  /**
   * Returns a stream of all the entities of a particular type in a
   * database.
   * @param em EntityManager connection to use to access the database
   * @param entity type of the entity
   * @return a stream of the results of querying the database for all
   *    entities of the given type.
   */
  def streamAll[U](em: EntityManager, entity: Class[U]): JinqJPAScalaIterator[U] = {
    val entityName = metamodel.entityNameFromClass(entity);
    var cachedQuery = if (hints.useCaching)
      cachedQueries.findCachedFindAllEntities(entityName) else null;
    if (cachedQuery == null) {
      val query = JPQLQuery.findAllEntities(entityName);
      cachedQuery = Optional.of(query);
      if (hints.useCaching)
        cachedQuery = cachedQueries.cacheFindAllEntities(entityName, cachedQuery);
    }
    val query = cachedQuery.get().asInstanceOf[JPQLQuery[U]];
    return new JinqJPAScalaIterator(JPAQueryComposer.findAllEntities(
      metamodel, cachedQueries, lambdaAnalyzer,
      jpqlQueryTransformConfigurationFactory, em, hints, query),
      new InQueryStreamSource() {
        def stream[U](entityClass: Class[U]): JinqIterator[U] =
          {
            return streamAll(em, entityClass);
          }
      });
  }

  /**
   * Sets a hint for how queries should be executed by Jinq
   * @param name
   * @param val
   * @return true if the hint was valid
   */
  def setHint(name: String, value: Object): Boolean =
    {
      return hints.setHint(name, value);
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
  def registerAssociationAttribute(m: Method, fieldName: String, isPlural: Boolean) {
    val attrib = new MetamodelUtilAttribute(fieldName, true);
    metamodel.insertAssociationAttribute(
      new MethodSignature(
        Type.getInternalName(m.getDeclaringClass()),
        m.getName(),
        Type.getMethodDescriptor(m)),
      attrib, isPlural);
  }
  
  /**
    * When using fields in JPA with AttributeConverters, you need to register the class
    * used by the field with Jinq so that it will allow objects of that type to be used in queries
    * @param convertedType class of the object used with AttributeConverters that Jinq should allow
    */
  def registerAttributeConverterType[U](convertedType : Class[U]) {
    metamodel.insertConvertedType(convertedType.getName());
  }

}
