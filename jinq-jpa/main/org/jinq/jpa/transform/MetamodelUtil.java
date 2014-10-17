package org.jinq.jpa.transform;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

/**
 * Provides helper methods for extracting useful information from
 * a JPA metamodel.
 */
public class MetamodelUtil
{
   final Metamodel metamodel;

   public final Set<Class<?>> safeMethodAnnotations;
   final Map<MethodSignature, MetamodelUtilAttribute> fieldMethods;
   final Map<MethodSignature, MetamodelUtilAttribute> nLinkMethods;
   public final Set<MethodSignature> safeMethods;
   public final Set<MethodSignature> safeStaticMethods;
   final Map<String, List<Enum<?>>> enums;
   public final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods; 
   
   public static final MethodSignature inQueryStream = new MethodSignature("org/jinq/orm/stream/InQueryStreamSource", "stream", "(Ljava/lang/Class;)Lorg/jinq/orm/stream/JinqStream;");
   
   public static final Map<MethodSignature, Integer> TUPLE_ACCESSORS = new HashMap<>();
   static {
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.pairGetOne, 1);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.pairGetTwo, 2);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple3GetOne, 1);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple3GetTwo, 2);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple3GetThree, 3);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple4GetOne, 1);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple4GetTwo, 2);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple4GetThree, 3);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple4GetFour, 4);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetOne, 1);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetTwo, 2);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetThree, 3);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetFour, 4);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetFive, 5);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetOne, 1);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetTwo, 2);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetThree, 3);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetFour, 4);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetFive, 5);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetSix, 6);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetSeven, 7);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetEight, 8);
   }

   public MetamodelUtil(Metamodel metamodel)
   {
      this.metamodel = metamodel;
      enums = new HashMap<>();
      comparisonMethods = new HashMap<>();
      safeMethodAnnotations = new HashSet<Class<?>>();
      safeMethodAnnotations.addAll(TransformationClassAnalyzer.SafeMethodAnnotations);
      safeMethods = new HashSet<MethodSignature>();
      safeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
      safeMethods.add(TransformationClassAnalyzer.integerIntValue);
      safeMethods.add(TransformationClassAnalyzer.longLongValue);
      safeMethods.add(TransformationClassAnalyzer.doubleDoubleValue);
      safeMethods.add(TransformationClassAnalyzer.booleanBooleanValue);
      safeMethods.add(inQueryStream);
      safeStaticMethods = new HashSet<MethodSignature>();
      safeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);
      safeStaticMethods.add(TransformationClassAnalyzer.integerValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.longValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.doubleValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.booleanValueOf);
      fieldMethods = new HashMap<>();
      nLinkMethods = new HashMap<>();
      
      findMetamodelGetters();
      safeMethods.addAll(fieldMethods.keySet());
      safeMethods.addAll(nLinkMethods.keySet());
   }
   
   /**
    * The Hibernate metamodel seems to hold incorrect information about
    * composite keys or entities that use other entities as keys or something.
    * This method provides a way for programmers to specify correct 
    * information for those types of mappings.
    */
   public void insertAssociationAttribute(MethodSignature sig, MetamodelUtilAttribute attribute, boolean isPlural)
   {
      if (isPlural)
         nLinkMethods.put(sig, attribute);
      else
         fieldMethods.put(sig, attribute);
      safeMethods.add(sig);
   }
   
   private void findMetamodelGetters()
   {
      for (EntityType<?> entity: metamodel.getEntities())
      {
         for (SingularAttribute<?,?> singularAttrib: entity.getDeclaredSingularAttributes())
         {
            Class<?> fieldJavaType = singularAttrib.getJavaType();
            Member javaMember = singularAttrib.getJavaMember();
            String name = javaMember.getName(); 
            if (javaMember instanceof Field)
            {
               // We'll have to guess the getter name based on the name of the field.
               name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            }
            MethodSignature methodSig = new MethodSignature(
                  org.objectweb.asm.Type.getInternalName(javaMember.getDeclaringClass()),
                  name,
                  org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(fieldJavaType)));
            if (fieldJavaType.isEnum())
            {
               // Record the enum, and mark equals() using the enum as safe
               String enumTypeName = org.objectweb.asm.Type.getInternalName(fieldJavaType); 
               enums.put(enumTypeName, Arrays.asList(((Class<Enum<?>>)fieldJavaType).getEnumConstants()));
               MethodSignature eqMethod = new MethodSignature(enumTypeName, "equals", "(Ljava/lang/Object;)Z"); 
               comparisonMethods.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
               safeMethods.add(eqMethod);
            }
            fieldMethods.put(methodSig, new MetamodelUtilAttribute(singularAttrib));
         }
         for (PluralAttribute<?,?,?> pluralAttrib: entity.getDeclaredPluralAttributes())
         {
            Member javaMember = pluralAttrib.getJavaMember();
            String name = javaMember.getName(); 
            if (javaMember instanceof Field)
            {
               // We'll have to guess the getter name based on the name of the field.
               name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            }
            MethodSignature methodSig = new MethodSignature(
                  org.objectweb.asm.Type.getInternalName(javaMember.getDeclaringClass()),
                  javaMember.getName(),
                  org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(pluralAttrib.getJavaType())));
            nLinkMethods.put(methodSig, new MetamodelUtilAttribute(pluralAttrib));
         }
      }
   }
   
   public <U> String entityNameFromClass(Class<U> entity)
   {
      EntityType<U> entityType = metamodel.entity(entity);
      if (entityType == null) return null;
      return entityType.getName();
   }
   
   /**
    * Returns the name of the entity referred to by the given class name
    * @param className
    * @return if className refers to a known JPA entity, then the
    * name of the entity if returned. If not, null is returned 
    */
   public String entityNameFromClassName(String className)
   {
      for (EntityType<?> entity: metamodel.getEntities())
         if (entity.getJavaType().getName().equals(className))
            return entity.getName();
      return null;
   }
   
   /**
    * Returns true if a method is used to get a singular attribute field from an entity
    * @param sig
    * @return
    */
   public boolean isSingularAttributeFieldMethod(MethodSignature sig)
   {
      return fieldMethods.containsKey(sig);
   }
   
   /**
    * Given a method used to read a field of an entity, this returns the actual
    * field name on the entity.
    * @param sig
    * @return
    */
   public String fieldMethodToFieldName(MethodSignature sig)
   {
      return fieldMethods.get(sig).getName();
   }

   /**
    * Given a method used to read a field of an entity, this returns whether
    * the field is an association type (i.e. represents a 1:1 or N:1 link)
    * @param sig
    * @return
    */
   public boolean isFieldMethodAssociationType(MethodSignature sig)
   {
      return fieldMethods.get(sig).isAssociation();
   }

   /**
    * Returns true if a method is used to get a plural attribute field from an entity
    * @param sig
    * @return
    */
   public boolean isPluralAttributeLinkMethod(MethodSignature sig)
   {
      return nLinkMethods.containsKey(sig);
   }
   
   /**
    * Given a method used for a 1:N or N:M navigational link, this returns the actual
    * name of the link.
    * @param sig
    * @return
    */
   public String nLinkMethodToLinkName(MethodSignature sig)
   {
      return nLinkMethods.get(sig).getName();
   }

   /**
    * Returns true if a Class refers to a known enum type
    * @param className class name using asm style / between package parts
    * @return
    */
   public boolean isKnownEnumType(String className)
   {
      return enums.containsKey(className);
   }
   
   /**
    * If className.name refers to an enum constant, then the method will
    * return the full name of that enum constant so that it can be 
    * embedded in a JPQL query. Otherwise, returns null.  
    * @param className
    * @param name
    * @return
    */
   public String getFullEnumConstantName(String className, String name)
   {
      List<Enum<?>> enumConstants = enums.get(className);
      if (enumConstants == null) return null;
      for (Enum<?> e: enumConstants)
      {
         if (e.name().equals(name))
            return className.replace("/", ".") + "." + name;
      }
      return null;
   }
}
