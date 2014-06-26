package org.jinq.jpa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

/**
 * Provides helper methods for extracting useful information from
 * a JPA metamodel.
 */
public class MetamodelUtil
{
   final Metamodel metamodel;

   public final Set<Class<?>> safeMethodAnnotations;
   final Map<MethodSignature, SingularAttribute<?,?>> fieldMethods;
   public final Set<MethodSignature> safeMethods;
   public final Set<MethodSignature> safeStaticMethods;

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
      safeMethodAnnotations = new HashSet<Class<?>>();
      safeMethodAnnotations.addAll(TransformationClassAnalyzer.SafeMethodAnnotations);
      safeMethods = new HashSet<MethodSignature>();
      safeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
      safeMethods.add(TransformationClassAnalyzer.integerIntValue);
      safeMethods.add(TransformationClassAnalyzer.longLongValue);
      safeMethods.add(TransformationClassAnalyzer.doubleDoubleValue);
      safeMethods.add(TransformationClassAnalyzer.booleanBooleanValue);
      safeStaticMethods = new HashSet<MethodSignature>();
      safeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);
      safeStaticMethods.add(TransformationClassAnalyzer.integerValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.longValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.doubleValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.booleanValueOf);
      fieldMethods = new HashMap<MethodSignature, SingularAttribute<?,?>>();
      
      findMetamodelGetters();
      safeMethods.addAll(fieldMethods.keySet());
   }
   
   private void findMetamodelGetters()
   {
      for (EntityType<?> entity: metamodel.getEntities())
      {
         for (SingularAttribute<?,?> singularAttrib: entity.getDeclaredSingularAttributes())
         {
            MethodSignature methodSig = new MethodSignature(
                  org.objectweb.asm.Type.getInternalName(singularAttrib.getJavaMember().getDeclaringClass()),
                  singularAttrib.getJavaMember().getName(),
                  org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(singularAttrib.getJavaType())));
            fieldMethods.put(methodSig, singularAttrib);
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
}
