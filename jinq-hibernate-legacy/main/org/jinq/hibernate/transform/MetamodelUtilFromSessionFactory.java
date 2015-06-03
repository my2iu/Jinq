package org.jinq.hibernate.transform;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.jinq.jpa.transform.MetamodelUtil;

public class MetamodelUtilFromSessionFactory extends MetamodelUtil
{
   Map<Class<?>, String> classToEntityName = new HashMap<>();
   Map<String, String> classNameToEntityName = new HashMap<>();
   
   public MetamodelUtilFromSessionFactory(SessionFactory factory)
   {
      // Go through all the entities 
      for (String entityClassName: factory.getAllClassMetadata().keySet())
      {
         ClassMetadata entityData = factory.getClassMetadata(entityClassName);
         classToEntityName.put(entityData.getMappedClass(), entityData.getEntityName());
         classNameToEntityName.put(entityClassName, entityData.getEntityName());
         System.out.println(entityClassName + " " + entityData.getMappedClass().getCanonicalName() + " " + entityData.getEntityName());
         // TODO: It turns out all three values are the same, but I think it's ok for now.
      }
   }
   
   @Override
   public <U> String entityNameFromClass(Class<U> entity)
   {
      return classToEntityName.get(entity);
   }

   @Override
   public String entityNameFromClassName(String className)
   {
      return classNameToEntityName.get(className);
   }

}
