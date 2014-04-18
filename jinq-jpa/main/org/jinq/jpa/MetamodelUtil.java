package org.jinq.jpa;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

/**
 * Provides helper methods for extracting useful information from
 * a JPA metamodel.
 */
public class MetamodelUtil
{
   final Metamodel metamodel;
   
   public MetamodelUtil(Metamodel metamodel)
   {
      this.metamodel = metamodel;
   }
   
   public <U> String entityNameFromClass(Class<U> entity)
   {
      EntityType<U> entityType = metamodel.entity(entity);
      if (entityType == null) return null;
      return entityType.getName();
   }
}
