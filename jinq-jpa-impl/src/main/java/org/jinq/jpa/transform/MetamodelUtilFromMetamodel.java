package org.jinq.jpa.transform;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

public class MetamodelUtilFromMetamodel extends MetamodelUtil
{
   final Metamodel metamodel;
   final boolean useHibernateFullEntityNames;

   public MetamodelUtilFromMetamodel(Metamodel metamodel)
   {
      this(metamodel, false);
   }

   public MetamodelUtilFromMetamodel(Metamodel metamodel, boolean useHibernateFullEntityNames)
   {
      this.metamodel = metamodel;
      this.useHibernateFullEntityNames = useHibernateFullEntityNames;
      
      findMetamodelGetters();
      safeMethods.addAll(fieldMethods.keySet());
      safeMethods.addAll(nLinkMethods.keySet());      
   }

   protected void findMetamodelGetters()
   {
      for (EntityType<?> entity: metamodel.getEntities())
      {
         findMetamodelEntityGetters(entity);
      }
   }
   
   @Override public <U> String entityNameFromClass(Class<U> entity)
   {
      if (!useHibernateFullEntityNames)
      {
         EntityType<U> entityType = metamodel.entity(entity);
         if (entityType == null) return null;
         return entityType.getName();
      }
      else
         return entity.getName();
   }
   
   /**
    * Returns the name of the entity referred to by the given class name
    * @param className
    * @return if className refers to a known JPA entity, then the
    * name of the entity if returned. If not, null is returned 
    */
   @Override public String entityNameFromClassName(String className)
   {
      for (EntityType<?> entity: metamodel.getEntities())
      {
         // Envers can insert entities with null Java types into the metamodel
         if (entity.getJavaType() == null) continue;
         
         if (entity.getJavaType().getName().equals(className))
         {
            if (!useHibernateFullEntityNames)
               return entity.getName();
            else
               return className;
         }
      }
      return null;
   }

}
