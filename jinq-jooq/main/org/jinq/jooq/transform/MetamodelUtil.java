package org.jinq.jooq.transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jinq.rebased.org.objectweb.asm.Type;
import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.Table;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

/**
 * Provides helper methods for extracting useful information from
 * a JPA metamodel.
 */
public class MetamodelUtil
{
   private final Set<Class<?>> safeMethodAnnotations;
   final Map<MethodSignature, Field<?>> fieldMethods;
   private final Set<MethodSignature> safeMethods;
   private final Set<MethodSignature> safeStaticMethods;

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

   public MetamodelUtil(Schema schema)
   {
      safeMethodAnnotations = new HashSet<Class<?>>();
      safeMethodAnnotations.addAll(TransformationClassAnalyzer.SafeMethodAnnotations);
      safeMethods = new HashSet<MethodSignature>();
      safeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
      safeMethods.add(TransformationClassAnalyzer.integerIntValue);
      safeMethods.add(TransformationClassAnalyzer.doubleDoubleValue);
      safeStaticMethods = new HashSet<MethodSignature>();
      safeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);
      safeStaticMethods.add(TransformationClassAnalyzer.integerValueOf);
      safeStaticMethods.add(TransformationClassAnalyzer.doubleValueOf);

      fieldMethods = new HashMap<MethodSignature, Field<?>>();
      findMetamodelGetters(schema);
      safeMethods.addAll(fieldMethods.keySet());
   }
   
   private void findMetamodelGetters(Schema schema)
   {
      for (Table<?> table: schema.getTables())
      {
         String recordClassName = Type.getInternalName(table.getRecordType());
         for (Field<?> field: table.fields())
         {
            String name = field.getName();
            String getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            MethodSignature methodSig = new MethodSignature(
                  recordClassName,
                  getterName,
                  Type.getMethodDescriptor(Type.getType(field.getType())));
            fieldMethods.put(methodSig, field);
         }
      }
   }

   public Set<Class<?>> getSafeMethodAnnotations()
   {
      return safeMethodAnnotations;
   }

   public boolean isSafeMethod(MethodSignature m)
   {
      return safeMethods.contains(m);
   }

   public boolean isSafeStaticMethod(MethodSignature m)
   {
      return safeStaticMethods.contains(m);
   }
   
//   public <U> String entityNameFromClass(Class<U> entity)
//   {
//      EntityType<U> entityType = metamodel.entity(entity);
//      if (entityType == null) return null;
//      return entityType.getName();
//   }
   
   /**
    * Returns true if a method is used as a getter for reading a field of a table
    * @param sig
    * @return
    */
   public boolean isFieldGetterMethod(MethodSignature sig)
   {
      return fieldMethods.containsKey(sig);
   }
   
   /**
    * Given a method used to read a field of an entity, this returns the actual
    * jOOQ field object needed to read the data.
    * @param sig
    * @return
    */
   public Field<?> fieldMethodToField(MethodSignature sig)
   {
      return fieldMethods.get(sig);
   }
}
