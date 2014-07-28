package ch.epfl.labos.iu.orm.queryll2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.runtime.ORMEntity;
import ch.epfl.labos.iu.orm.queryll2.runtime.ORMField;
import ch.epfl.labos.iu.orm.queryll2.runtime.QueryllEntityConfigurationInfo;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

public class ORMInformation implements QueryllEntityConfigurationInfo
{
   List<ORMEntity> entities = new Vector<ORMEntity>();
   HashSet<MethodSignature> sideEffectFreeMethods = new HashSet<MethodSignature>();
   HashSet<MethodSignature> sideEffectFreeStaticMethods = new HashSet<MethodSignature>();
   HashMap<MethodSignature, String> fieldMethods = new HashMap<MethodSignature, String>();  // maps method signature to field name
   HashMap<MethodSignature, String> allEntityMethods = new HashMap<MethodSignature, String>();  // maps method signature to entity name
   HashSet<MethodSignature> dbSetMethods = new HashSet<MethodSignature>();  // dbset methods are only safe if the dbset method being used is known. If not, there may be side-effects not accounted for
   HashSet<MethodSignature> jinqStreamMethods = new HashSet<MethodSignature>();  // jinqStream methods are only safe if the jinq Stream method being used is known. If not, there may be side-effects not accounted for
   HashSet<MethodSignature> passThroughMethods = new HashSet<MethodSignature>();  // for the purpose of query generation, these methods have no effect
   HashSet<MethodSignature> passThroughStaticMethods = new HashSet<MethodSignature>();  // for the purpose of query generation, these methods have no effect
   HashMap<MethodSignature, N111NavigationalLink> N111Methods = new HashMap<MethodSignature, N111NavigationalLink>();  // maps method signatures for navigational queries to something
   
   //
   // Called by Queryll when during its analysis to figure out information
   // about the ORM and perhaps how to deal with certain non-ORM methods 
   //
   
   public ORMInformation()
   {
      // TODO: Right now, the master list of side-effect free methods is stored
      // in TransformationClassAnalyzer, but the master list for other types of
      // methods are stored here. This should be made consistent.
      
//      sideEffectFreeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
//      sideEffectFreeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);

      fieldMethods.put(TransformationClassAnalyzer.pairGetOne, "One");
      fieldMethods.put(TransformationClassAnalyzer.pairGetTwo, "Two");
      fieldMethods.put(TransformationClassAnalyzer.tuple3GetOne, "One");
      fieldMethods.put(TransformationClassAnalyzer.tuple3GetTwo, "Two");
      fieldMethods.put(TransformationClassAnalyzer.tuple3GetThree, "Three");
      fieldMethods.put(TransformationClassAnalyzer.tuple4GetOne, "One");
      fieldMethods.put(TransformationClassAnalyzer.tuple4GetTwo, "Two");
      fieldMethods.put(TransformationClassAnalyzer.tuple4GetThree, "Three");
      fieldMethods.put(TransformationClassAnalyzer.tuple4GetFour, "Four");
      fieldMethods.put(TransformationClassAnalyzer.tuple5GetOne, "One");
      fieldMethods.put(TransformationClassAnalyzer.tuple5GetTwo, "Two");
      fieldMethods.put(TransformationClassAnalyzer.tuple5GetThree, "Three");
      fieldMethods.put(TransformationClassAnalyzer.tuple5GetFour, "Four");
      fieldMethods.put(TransformationClassAnalyzer.tuple5GetFive, "Five");
      fieldMethods.put(TransformationClassAnalyzer.tuple8GetOne, "One");
      fieldMethods.put(TransformationClassAnalyzer.tuple8GetTwo, "Two");
      fieldMethods.put(TransformationClassAnalyzer.tuple8GetThree, "Three");
      fieldMethods.put(TransformationClassAnalyzer.tuple8GetFour, "Four");
      fieldMethods.put(TransformationClassAnalyzer.tuple8GetFive, "Five");
      fieldMethods.put(TransformationClassAnalyzer.tuple8GetSix, "Six");
      fieldMethods.put(TransformationClassAnalyzer.tuple8GetSeven, "Seven");
      fieldMethods.put(TransformationClassAnalyzer.tuple8GetEight, "Eight");
      jinqStreamMethods.add(TransformationClassAnalyzer.streamSumInt);
      jinqStreamMethods.add(TransformationClassAnalyzer.streamSumDouble);
      jinqStreamMethods.add(TransformationClassAnalyzer.streamMax);
      jinqStreamMethods.add(TransformationClassAnalyzer.streamMin);
      dbSetMethods.add(TransformationClassAnalyzer.dbsetSumInt);
      dbSetMethods.add(TransformationClassAnalyzer.dbsetSumDouble);
      dbSetMethods.add(TransformationClassAnalyzer.dbsetMaxInt);
      dbSetMethods.add(TransformationClassAnalyzer.dbsetMaxDouble);
      dbSetMethods.add(TransformationClassAnalyzer.dbsetWhere);
      dbSetMethods.add(TransformationClassAnalyzer.dbsetToStream);
      passThroughMethods.add(TransformationClassAnalyzer.integerIntValue);
      passThroughMethods.add(TransformationClassAnalyzer.longLongValue);
      passThroughMethods.add(TransformationClassAnalyzer.doubleDoubleValue);
      passThroughStaticMethods.add(TransformationClassAnalyzer.integerValueOf);
      passThroughStaticMethods.add(TransformationClassAnalyzer.longValueOf);
      passThroughStaticMethods.add(TransformationClassAnalyzer.doubleValueOf);
   }
   
   //
   // These are exported to the DBManager which uses these methods to
   // tell the class about the ORM mapping
   //
   
   public void registerORMEntity(ORMEntity entity)
   {
      entities.add(entity);
      for (ORMField field: entity.fields)
      {
         MethodSignature m = makeFieldMethodSignature(entity.entityPackage, entity.name, 
                                                 field.name, 
                                                 ormTypeStringToType(field.type));
         sideEffectFreeMethods.add(m);
         fieldMethods.put(m, field.name);
      }
      MethodSignature getAllEntities =
         makeMethodSignature(entity.entityPackage, 
                             "EntityManager",
                             "all" + capitalizeFirstLetter(entity.name),
                             Type.getObjectType("ch/epfl/labos/iu/orm/DBSet"));
      sideEffectFreeMethods.add(getAllEntities);
      allEntityMethods.put(getAllEntities, entity.name);
   }

   ORMEntity entityFor(String name)
   {
      for (ORMEntity e: entities)
         if (e.name.equals(name))
            return e;
      return null;
   }
   
   public void registerORMNMLink(String entityPackage, 
                                 String fromEntity, String fromField, String fromCol,
                                 String linkTable, String linkInCol, String linkOutCol, 
                                 String toEntity, String toField, String toCol)
   {
   }

   public void registerORMSimpleLink(String entityPackage, String map,
                                     String fromEntityName, String fromField, String fromCol, 
                                     String toEntityName, String toField, String toCol)
   {
      ORMEntity fromEntity = entityFor(fromEntityName);
      ORMEntity toEntity = entityFor(toEntityName);
      String fromInternalName = makeInternalName(fromEntity.entityPackage, fromEntity.name);
      String toInternalName = makeInternalName(toEntity.entityPackage, toEntity.name);
      MethodSignature from1Signature =
         makeMethodSignature(entityPackage, 
                             fromEntity.name,
                             "get" + capitalizeFirstLetter(fromField),
                             Type.getObjectType(toInternalName));
      MethodSignature to1Signature = 
         makeMethodSignature(entityPackage, 
                             toEntity.name,
                             "get" + capitalizeFirstLetter(toField),
                             Type.getObjectType(fromInternalName));

      if ("1:1".equals(map))
      {
         N111Methods.put(from1Signature,
             new N111NavigationalLink(fromEntityName, fromField, toEntityName,
                                      new JoinInfo(fromInternalName, fromEntity.table, fromCol,
                                                   toInternalName, toEntity.table, toCol)));
         N111Methods.put(to1Signature,
             new N111NavigationalLink(toEntityName, toField, fromEntityName,
                                      new JoinInfo(toInternalName, toEntity.table, toCol,
                                                   fromInternalName, fromEntity.table, fromCol)));
         sideEffectFreeMethods.add(from1Signature);
         sideEffectFreeMethods.add(to1Signature);
      } 
      else if ("N:1".equals(map))
      {
         N111Methods.put(from1Signature,
             new N111NavigationalLink(fromEntityName, fromField, toEntityName,
                                      new JoinInfo(fromInternalName, fromEntity.table, fromCol,
                                                   toInternalName, toEntity.table, toCol)));
         sideEffectFreeMethods.add(from1Signature);
      } 
      else // 1:N
      {
         N111Methods.put(to1Signature,
             new N111NavigationalLink(toEntityName, toField, fromEntityName,
                                      new JoinInfo(toInternalName, toEntity.table, toCol,
                                                   fromInternalName, fromEntity.table, fromCol)));
         sideEffectFreeMethods.add(to1Signature);
      }
   }
   
   //
   // Various helper methods
   //
   
   static Type ormTypeStringToType(String typeString)
   {
      if (typeString.equals("int"))
         return Type.INT_TYPE;
      else if (typeString.equals("float"))
         return Type.FLOAT_TYPE;
      else if (typeString.equals("double"))
         return Type.DOUBLE_TYPE;
      else if (typeString.equals("String"))
         return Type.getObjectType("java/lang/String");
      else if (typeString.equals("Date"))
         return Type.getObjectType("java/sql/Date");
      else
         return Type.VOID_TYPE;
   }
   static String makeInternalName(String packageName, String className)
   {
      return packageName.replaceAll("[.]", "/") + "/" + className;
   }
   static String capitalizeFirstLetter(String str)
   {
      return str.substring(0, 1).toUpperCase(Locale.US) + str.substring(1);
   }
   static MethodSignature makeMethodSignature(String packageName, String className, String methodName, Type returnType)
   {
      String owner = makeInternalName(packageName, className);
      String name = methodName;
      String desc = Type.getMethodDescriptor(returnType, new Type[0]);
      return new MethodSignature(owner, name, desc);
   }
   static MethodSignature makeFieldMethodSignature(String packageName, String className, String fieldName, Type returnType)
   {
      return makeMethodSignature(packageName, 
                                 className, 
                                 "get" + capitalizeFirstLetter(fieldName), 
                                 returnType);
   }
   
   public static class N111NavigationalLink
   {
      public String fromEntity;
      public String name;
      public List<JoinInfo> joinInfo;
      public String toEntity;
      public N111NavigationalLink(String fromEntity, String name, String toEntity, JoinInfo...joins)
      {
         this.fromEntity = fromEntity;
         this.name = name;
         this.toEntity = toEntity;
         joinInfo = Arrays.asList(joins);
      }
   }
   
   public static class JoinInfo
   {
      public JoinInfo(String fromType, String fromTable, String fromCol,
                      String toType, String toTable, String toCol)
      {
         this(fromType, fromTable, Arrays.asList(fromCol), toType, toTable, Arrays.asList(toCol));
      }
      public JoinInfo(String fromType, String fromTable, Collection<String> fromCols,
                      String toType, String toTable, Collection<String> toCols)
      {
         fromTableName = fromTable;
         fromTypeName = fromType;
         fromColumns = new Vector<String>(fromCols);
         toTableName = toTable;
         toTypeName = toType;
         toColumns = new Vector<String>(toCols);
      }
      public String fromTableName;
      public String fromTypeName;  // Can be null
      public Vector<String> fromColumns;
      public String toTableName;
      public String toTypeName;    // Can be null
      public Vector<String> toColumns;
   }
}
