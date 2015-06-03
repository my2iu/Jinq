package org.jinq.jpa.transform;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
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
public abstract class MetamodelUtil
{
   private final Set<Class<?>> safeMethodAnnotations;
   final Map<MethodSignature, MetamodelUtilAttribute> fieldMethods;
   final Map<MethodSignature, MetamodelUtilAttribute> nLinkMethods;
   protected final Set<MethodSignature> safeMethods;
   protected final Set<MethodSignature> safeStaticMethods;
   final Map<String, List<Enum<?>>> enums;
   private final Set<String> knownEmbeddedtypes = new HashSet<>();
   public final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods; 
   public final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethodsWithObjectEquals;
   
   /**
    * The classes that have been analyzed or are in the process of being analyzed to
    * extract getter method information (this is here to prevent infinite loops in case there
    * are cycles in the entities being analyzed--I'm not sure that's actually possible though)
    */
   private Set<String> scannedClasses = new HashSet<>();
   
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

   public MetamodelUtil()
   {
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
      comparisonMethodsWithObjectEquals = new HashMap<>();
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
   
   private void insertFieldMethod(String className, String methodName, String returnType, MetamodelUtilAttribute fieldAttribute)
   {
      MethodSignature methodSig = new MethodSignature(
            className,
            methodName, 
            returnType);
      fieldMethods.put(methodSig, fieldAttribute);
   }
   
   private void insertNLinkMethod(String className, String methodName, String returnType, MetamodelUtilAttribute pluralAttribute)
   {
      MethodSignature methodSig = new MethodSignature(
            className,
            methodName, 
            returnType);
      nLinkMethods.put(methodSig, pluralAttribute);
   }

   protected void findMetamodelEntityGetters(ManagedType<?> entity)
   {
      // Apparently, this can happen with Envers and its generated audit tables
      if (entity.getJavaType() == null) return;
      
      // Make sure we don't scan the same entity twice
      if (scannedClasses.contains(entity.getJavaType().getName()))
         return;
      scannedClasses.add(entity.getJavaType().getName());
      
      // Actually scan the entity now and extract its getters
      findMetamodelEntityGetters(entity, new ArrayList<>());
   }
   
   private void findMetamodelEntityGetters(ManagedType<?> entity, Collection<String> subclassNames)
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
         if (fieldJavaType.isEnum())
         {
            // Record the enum, and mark equals() using the enum as safe
            String enumTypeName = org.objectweb.asm.Type.getInternalName(fieldJavaType); 
            enums.put(enumTypeName, Arrays.asList(((Class<Enum<?>>)fieldJavaType).getEnumConstants()));
            MethodSignature eqMethod = new MethodSignature(enumTypeName, "equals", "(Ljava/lang/Object;)Z"); 
            comparisonMethods.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
            safeMethods.add(eqMethod);
         }
         String returnType = org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(fieldJavaType));
         // EclipseLink sometimes lists a different Java type in the attribute than 
         // what's used in the actual method (e.g. a Timestamp instead of a Date because
         // I guess that's what is being used internally). In those cases, we'll 
         // record both versions.
         String alternateReturnType = null;
         if (javaMember instanceof Field)
         {
            alternateReturnType = org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(((Field)javaMember).getType()));
            if (returnType.equals(alternateReturnType)) alternateReturnType = null;
         } 
         else if (javaMember instanceof Method)
         {
            alternateReturnType = org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(((Method)javaMember).getReturnType()));
            if (returnType.equals(alternateReturnType)) alternateReturnType = null;
         }
         // EclipseLink lists the fields of superclasses in their subclasses, so
         // we can register those immediately without having to recurse into the
         // superclass
         String declaredClassName = org.objectweb.asm.Type.getInternalName(javaMember.getDeclaringClass());
         String entityClassName = org.objectweb.asm.Type.getInternalName(entity.getJavaType());
         if (entityClassName.equals(declaredClassName)) entityClassName = null;
         // Register the method to field mapping
         MetamodelUtilAttribute fieldAttribute = new MetamodelUtilAttribute(singularAttrib);
         insertFieldMethod(declaredClassName, name, returnType, fieldAttribute);
         if (entityClassName != null)
            insertFieldMethod(entityClassName, name, returnType, fieldAttribute);
         if (alternateReturnType != null)
            insertFieldMethod(declaredClassName, name, alternateReturnType, fieldAttribute);
         if (alternateReturnType != null && entityClassName != null)
            insertFieldMethod(entityClassName, name, alternateReturnType, fieldAttribute);
         // The method is also callable from its subclasses, so register the method
         // in its subclasses as well.
         for (String className: subclassNames)
         {
            insertFieldMethod(className, name, returnType, fieldAttribute);
            if (alternateReturnType != null)
               insertFieldMethod(className, name, alternateReturnType, fieldAttribute);
         }
         // The attribute might be an embedded type, in which case, we need to scan the 
         // embedded type for getters as well since it won't show up as an entity.
         if (singularAttrib.getType() instanceof EmbeddableType)
         {
            EmbeddableType embed = (EmbeddableType)singularAttrib.getType();
            knownEmbeddedtypes.add(embed.getJavaType().getName());
            findMetamodelEntityGetters(embed);
         }
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
         String returnType = org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(pluralAttrib.getJavaType())); 
         // EclipseLink lists the fields of superclasses in their subclasses, so
         // we can register those immediately without having to recurse into the
         // superclass
         String declaredClassName = org.objectweb.asm.Type.getInternalName(javaMember.getDeclaringClass());
         String entityClassName = org.objectweb.asm.Type.getInternalName(entity.getJavaType());
         if (entityClassName.equals(declaredClassName)) entityClassName = null;
         // Register the method and variants
         MetamodelUtilAttribute nLinkAttrib = new MetamodelUtilAttribute(pluralAttrib);
         insertNLinkMethod(declaredClassName, name, returnType, nLinkAttrib);
         if (entityClassName != null)
            insertNLinkMethod(entityClassName, name, returnType, nLinkAttrib);
         // The method is also callable from its subclasses
         for (String className: subclassNames)
         {
            insertNLinkMethod(className, name, returnType, nLinkAttrib);
         }
      }
      if (entity instanceof IdentifiableType)
      {
         IdentifiableType idEntity = (IdentifiableType)entity; 
         if (idEntity.getSupertype() != null)
         {
            IdentifiableType<?> jpaObject = idEntity.getSupertype();
            String className = org.objectweb.asm.Type.getInternalName(entity.getJavaType());
            List<String> newSubclasses = new ArrayList<>();
            newSubclasses.add(className);
            findMetamodelEntityGetters(jpaObject, newSubclasses);
         }
      }
   }
   
   public <U> boolean isKnownManagedType(String entityClassName)
   {
      return entityNameFromClassName(entityClassName) != null 
            || knownEmbeddedtypes.contains(entityClassName);
   }
   
   public abstract <U> String entityNameFromClass(Class<U> entity);
   
   /**
    * Returns the name of the entity referred to by the given class name
    * @param className
    * @return if className refers to a known JPA entity, then the
    * name of the entity if returned. If not, null is returned 
    */
   public abstract String entityNameFromClassName(String className);
   
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
   
   public Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> 
      getComparisonMethods(boolean withObjectEquals)
   {
      if (withObjectEquals)
         return comparisonMethodsWithObjectEquals;
      else
         return comparisonMethods;
   }

   public Set<Class<?>> getSafeMethodAnnotations()
   {
      return safeMethodAnnotations;
   }

   public Set<MethodSignature> getSafeMethods()
   {
      return safeMethods;
   }

   public Set<MethodSignature> getSafeStaticMethods()
   {
      return safeStaticMethods;
   }
   
   public MethodChecker getMethodChecker(boolean isObjectEqualsSafe, boolean isCollectionContainsSafe)
   {
      return new MethodChecker(
            getSafeMethodAnnotations(), 
            getSafeMethods(), getSafeStaticMethods(),
            isObjectEqualsSafe, isCollectionContainsSafe);
   }
}
