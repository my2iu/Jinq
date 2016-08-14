package org.jinq.hibernate.transform;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.BigIntegerType;
import org.hibernate.type.BinaryType;
import org.hibernate.type.CalendarDateType;
import org.hibernate.type.CalendarType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CustomType;
import org.hibernate.type.DateType;
import org.hibernate.type.EnumType;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimeType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.converter.AttributeConverterTypeAdapter;
import org.jinq.jpa.transform.MetamodelUtil;
import org.jinq.jpa.transform.MetamodelUtilAttribute;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

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
         //System.out.println(entityClassName + " " + entityData.getMappedClass().getCanonicalName() + " " + entityData.getEntityName());
         // TODO: It turns out all three values are the same, but I think it's ok for now.
         
         scanClassMetadata(entityData);
      }
   }

   private void scanClassMetadata(ClassMetadata meta)
   {
      Class<?> entityClass = meta.getMappedClass();
      String[] names = meta.getPropertyNames();
      Type[] types = meta.getPropertyTypes();
      for (int n = 0; n < names.length; n++)
      {
         String fieldName = names[n];
         Type type = types[n];
         registerEntityField(entityClass, fieldName, type);
      }
      if (meta.getIdentifierPropertyName() != null)
         registerEntityField(entityClass, meta.getIdentifierPropertyName(), meta.getIdentifierType());
      
      //System.out.println(names + " " + types);
   }
   
   private void registerEntityField(Class<?> entityClass, String fieldName,
         Type type)
   {
      // Guess the name of the getter based on the field name
      String name = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
      tryToRegisterFieldGetter(name, fieldName, entityClass, type);
      if (type instanceof PrimitiveType && ((PrimitiveType)type).getPrimitiveClass() == Boolean.TYPE)
      {
         String altName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
         tryToRegisterFieldGetter(altName, fieldName, entityClass, type);
      }
   }

   private void tryToRegisterFieldGetter(String name, String fieldName, Class<?> entityClass, Type type)
   {
      try {
         // Try to find the getter method
         Method m = entityClass.getMethod(name);
         MethodSignature sig = new MethodSignature(
               org.jinq.rebased.org.objectweb.asm.Type.getInternalName(entityClass),
               m.getName(),
               org.jinq.rebased.org.objectweb.asm.Type.getMethodDescriptor(m)); 
         
         MetamodelUtilAttribute attrib = new MetamodelUtilAttribute(fieldName, true);
         
         // Basic fields
         if (type instanceof PrimitiveType
               || type instanceof StringType
               || type instanceof BigDecimalType
               || type instanceof BigIntegerType
               || type instanceof CalendarType
               || type instanceof CalendarDateType
               || type instanceof TimestampType
               || type instanceof DateType
               || type instanceof TimeType
               || type instanceof BinaryType
               || (type.isAssociationType() && !type.isCollectionType())
               || type instanceof AttributeConverterTypeAdapter)
         {
            insertAssociationAttribute(sig, attrib, false);
         }
         else if (type.isAssociationType() && type.isCollectionType())
         {
            insertAssociationAttribute(sig, attrib, true);
         }
         else if (type instanceof CustomType
               && ((CustomType)type).getUserType() instanceof EnumType
               && type.getReturnedClass().isEnum())
         {
            insertAssociationAttribute(sig, attrib, false);
            registerEnum(type.getReturnedClass());
         }
         else if (type instanceof ComponentType)
         {
            insertAssociationAttribute(sig, attrib, false);
            ComponentType embed = (ComponentType)type;
            knownEmbeddedtypes.add(type.getReturnedClass().getName());
            String [] names = embed.getPropertyNames();
            Type [] types = embed.getSubtypes();
            for (int n = 0; n < names.length; n++)
               registerEntityField(type.getReturnedClass(), names[n], types[n]);
         }
               
      }
      catch (Exception e) 
      {
         // Eat the error
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
