package org.jinq.jpa.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jinq.rebased.org.objectweb.asm.Type;

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
   protected final Map<MethodSignature, MetamodelUtilAttribute> fieldMethods;
   protected final Map<MethodSignature, MetamodelUtilAttribute> nLinkMethods;
   protected final Set<MethodSignature> safeMethods;
   protected final Set<MethodSignature> safeStaticMethods;
   protected final Map<String, List<Enum<?>>> enums;
   protected final Set<String> convertedTypes;
   protected final Set<String> knownEmbeddedtypes = new HashSet<>();
   protected final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods; 
   protected final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethodsWithObjectEquals;
   protected final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonStaticMethods; 
   protected final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonStaticMethodsWithObjectEquals;
   protected final Map<MethodSignature, CustomTupleInfo> customTupleStaticBuilderMethods;
   protected final Map<MethodSignature, CustomTupleInfo> customTupleConstructorMethods;
   protected final Map<MethodSignature, Integer> customTupleAccessorMethods;
   
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
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetOne, 1);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetTwo, 2);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetThree, 3);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetFour, 4);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetFive, 5);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetSix, 6);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetOne, 1);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetTwo, 2);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetThree, 3);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetFour, 4);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetFive, 5);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetSix, 6);
      TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetSeven, 7);
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
      convertedTypes = new HashSet<>();
      comparisonMethods = new HashMap<>();
      safeMethodAnnotations = new HashSet<Class<?>>();
      safeMethodAnnotations.addAll(TransformationClassAnalyzer.SafeMethodAnnotations);
      safeMethods = new HashSet<MethodSignature>();
      safeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
      safeMethods.add(TransformationClassAnalyzer.integerIntValue);
      safeMethods.add(TransformationClassAnalyzer.longLongValue);
      safeMethods.add(TransformationClassAnalyzer.floatFloatValue);
      safeMethods.add(TransformationClassAnalyzer.doubleDoubleValue);
      safeMethods.add(TransformationClassAnalyzer.booleanBooleanValue);
      safeMethods.add(inQueryStream);
      safeStaticMethods = new HashSet<MethodSignature>();
      safeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);
      safeStaticMethods.add(TransformationClassAnalyzer.integerValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.longValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.floatValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.doubleValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.booleanValueOf);
      fieldMethods = new HashMap<>();
      nLinkMethods = new HashMap<>();
      comparisonMethodsWithObjectEquals = new HashMap<>();
      comparisonMethodsWithObjectEquals.put(MethodChecker.objectEquals, TypedValue.ComparisonValue.ComparisonOp.eq);
      comparisonStaticMethods = new HashMap<>();
      comparisonStaticMethodsWithObjectEquals = new HashMap<>();
      comparisonStaticMethodsWithObjectEquals.put(MethodChecker.guavaObjectsEqual, TypedValue.ComparisonValue.ComparisonOp.eq);
      comparisonStaticMethodsWithObjectEquals.put(MethodChecker.objectsEquals, TypedValue.ComparisonValue.ComparisonOp.eq);
      customTupleStaticBuilderMethods = new HashMap<>();
      customTupleConstructorMethods = new HashMap<>();
      customTupleAccessorMethods = new HashMap<>();
   }
   
   /**
    * Allows you to register the existence of a class that JPA uses AttributeConverters to 
    * convert for database use.
    * @param className full class name of the type 
    */
   public void insertConvertedType(String className)
   {
      convertedTypes.add(className);
   }
   
   /**
    * Allows you to register your own Java class that can be used as a tuple in some limited
    * situations.
    */
   public void insertCustomTupleBuilder(String className, Method builderMethod, Method...tupleIndexReaders)
   {
      if (!Modifier.isStatic(builderMethod.getModifiers()))
         throw new IllegalArgumentException("Builder method for custom tuple must be a static method");

      MethodSignature builderSig = MethodSignature.fromMethod(builderMethod);
      safeStaticMethods.add(builderSig);
      insertCustomTupleIndexGetters(tupleIndexReaders);
      
      CustomTupleInfo tupleInfo = new CustomTupleInfo();
      tupleInfo.className = className;
      tupleInfo.staticBuilder = builderMethod;
      tupleInfo.staticBuilderSig = builderSig;
      customTupleStaticBuilderMethods.put(builderSig, tupleInfo);
   }

   /**
    * Allows you to register your own Java class that can be used as a tuple in some limited
    * situations.
    */
   public void insertCustomTupleConstructor(String className, Constructor<?> constructor, Method...tupleIndexReaders)
   {
      MethodSignature constructorSig = MethodSignature.fromConstructor(constructor);
      safeMethods.add(constructorSig);
      insertCustomTupleIndexGetters(tupleIndexReaders);
      
      CustomTupleInfo tupleInfo = new CustomTupleInfo();
      tupleInfo.className = className;
      tupleInfo.constructor = constructor;
      tupleInfo.constructorSig = constructorSig;
      customTupleConstructorMethods.put(constructorSig, tupleInfo);
   }
   
   private void insertCustomTupleIndexGetters(Method...tupleIndexReaders)
   {
      if (tupleIndexReaders != null)
      {
         int idx = 1;
         for (Method tupleIndex: tupleIndexReaders)
         {
            if (Modifier.isStatic(tupleIndex.getModifiers()))
               throw new IllegalArgumentException("Method for reading a value from a custom tuple must be a virtual method declared on the custom tuple class");
            if (tupleIndex.getParameterCount() != 0)
               throw new IllegalArgumentException("Method for reading a value from a custom tuple must not take any arguments");
            MethodSignature sig = new MethodSignature(
                  org.jinq.rebased.org.objectweb.asm.Type.getInternalName(tupleIndex.getDeclaringClass()),
                  tupleIndex.getName(),
                  org.jinq.rebased.org.objectweb.asm.Type.getMethodDescriptor(tupleIndex));
            customTupleAccessorMethods.put(sig, idx);
            safeMethods.add(sig);
            idx++;
         }
      }
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
   
   private void insertFieldMethod(String className, String methodName, String altMethodName, String returnType, MetamodelUtilAttribute fieldAttribute)
   {
      MethodSignature methodSig = new MethodSignature(
            className,
            methodName, 
            returnType);
      fieldMethods.put(methodSig, fieldAttribute);
      if (altMethodName != null)
      {
         MethodSignature altMethodSig = new MethodSignature(
               className,
               altMethodName, 
               returnType);
         fieldMethods.put(altMethodSig, fieldAttribute);
      }
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
         String altName = null;
         if (javaMember instanceof Field)
         {
            // Special handling of naming of boolean getters
            if (fieldJavaType == Boolean.TYPE || "java.lang.Boolean".equals(fieldJavaType.getName()))
               altName = "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
            // We'll have to guess the getter name based on the name of the field.
            name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
         }
         if (fieldJavaType.isEnum())
         {
            registerEnum(fieldJavaType);
         }
         String returnType = Type.getMethodDescriptor(Type.getType(fieldJavaType));
         // EclipseLink sometimes lists a different Java type in the attribute than 
         // what's used in the actual method (e.g. a Timestamp instead of a Date because
         // I guess that's what is being used internally). In those cases, we'll 
         // record both versions.
         String alternateReturnType = null;
         if (javaMember instanceof Field)
         {
            alternateReturnType = Type.getMethodDescriptor(Type.getType(((Field)javaMember).getType()));
            if (returnType.equals(alternateReturnType)) alternateReturnType = null;
         } 
         else if (javaMember instanceof Method)
         {
            alternateReturnType = Type.getMethodDescriptor(Type.getType(((Method)javaMember).getReturnType()));
            if (returnType.equals(alternateReturnType)) alternateReturnType = null;
         }
         // EclipseLink lists the fields of superclasses in their subclasses, so
         // we can register those immediately without having to recurse into the
         // superclass
         String declaredClassName = Type.getInternalName(javaMember.getDeclaringClass());
         String entityClassName = Type.getInternalName(entity.getJavaType());
         if (entityClassName.equals(declaredClassName)) entityClassName = null;
         // Register the method to field mapping
         MetamodelUtilAttribute fieldAttribute = new MetamodelUtilAttribute(singularAttrib);
         insertFieldMethod(declaredClassName, name, altName, returnType, fieldAttribute);
         if (entityClassName != null)
            insertFieldMethod(entityClassName, name, altName, returnType, fieldAttribute);
         if (alternateReturnType != null)
            insertFieldMethod(declaredClassName, name, altName, alternateReturnType, fieldAttribute);
         if (alternateReturnType != null && entityClassName != null)
            insertFieldMethod(entityClassName, name, altName, alternateReturnType, fieldAttribute);
         // The method is also callable from its subclasses, so register the method
         // in its subclasses as well.
         for (String className: subclassNames)
         {
            insertFieldMethod(className, name, altName, returnType, fieldAttribute);
            if (alternateReturnType != null)
               insertFieldMethod(className, name, altName, alternateReturnType, fieldAttribute);
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
         String returnType = Type.getMethodDescriptor(Type.getType(pluralAttrib.getJavaType())); 
         // EclipseLink lists the fields of superclasses in their subclasses, so
         // we can register those immediately without having to recurse into the
         // superclass
         String declaredClassName = Type.getInternalName(javaMember.getDeclaringClass());
         String entityClassName = Type.getInternalName(entity.getJavaType());
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
            String className = Type.getInternalName(entity.getJavaType());
            List<String> newSubclasses = new ArrayList<>(subclassNames);
            newSubclasses.add(className);
            findMetamodelEntityGetters(jpaObject, newSubclasses);
         }
      }
   }

   protected void registerEnum(Class<?> fieldJavaType)
   {
      // Record the enum, and mark equals() using the enum as safe
      String enumTypeName = Type.getInternalName(fieldJavaType); 
      enums.put(enumTypeName, Arrays.asList(((Class<Enum<?>>)fieldJavaType).getEnumConstants()));
      MethodSignature eqMethod = new MethodSignature(enumTypeName, "equals", "(Ljava/lang/Object;)Z"); 
      comparisonMethods.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
      comparisonMethodsWithObjectEquals.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
      safeMethods.add(eqMethod);
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
    * Returns true if a Class refers to a known type used by AttributeConverters
    * @param className class name using asm style / between package parts
    * @return
    */
   public boolean isKnownConvertedType(String className)
   {
      return convertedTypes.contains(className);
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

   public Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> 
         getComparisonStaticMethods(boolean withObjectEquals)
   {
      if (withObjectEquals)
         return comparisonStaticMethodsWithObjectEquals;
      else
         return comparisonStaticMethods;
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
   
   public MethodChecker getMethodChecker(boolean isObjectEqualsSafe, boolean isAllEqualsSafe, boolean isCollectionContainsSafe)
   {
      return new MethodChecker(
            getSafeMethodAnnotations(), 
            getSafeMethods(), getSafeStaticMethods(),
            isObjectEqualsSafe, isAllEqualsSafe, isCollectionContainsSafe);
   }
}
