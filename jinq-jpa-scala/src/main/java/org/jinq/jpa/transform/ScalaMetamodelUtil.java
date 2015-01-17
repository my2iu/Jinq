package org.jinq.jpa.transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Metamodel;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

public class ScalaMetamodelUtil extends MetamodelUtil
{
   public final static MethodSignature INQUERYSTREAMSOURCE_STREAM = new MethodSignature("org/jinq/orm/stream/scala/InQueryStreamSource", "stream", "(Ljava/lang/Class;)Lorg/jinq/orm/stream/scala/JinqIterator;");
   public final static MethodSignature ITERABLE_TO_JINQ = new MethodSignature("org/jinq/orm/stream/scala/JinqConversions$", "jinq", "(Ljava/lang/Iterable;)Lorg/jinq/orm/stream/scala/JinqIterator;");
   public final static MethodSignature STREAM_OF = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator$", "of", "(Ljava/lang/Object;)Lorg/jinq/orm/stream/scala/JinqIterator;");
   public final static MethodSignature PREDEF_INT_TO_INTEGER = new MethodSignature("scala/Predef$", "int2Integer", "(I)Ljava/lang/Integer;");
   public final static MethodSignature PREDEF_LONG_TO_LONG = new MethodSignature("scala/Predef$", "long2Long", "(J)Ljava/lang/Long;");
   public final static MethodSignature PREDEF_DOUBLE_TO_DOUBLE = new MethodSignature("scala/Predef$", "double2Double", "(D)Ljava/lang/Double;");
   public final static MethodSignature PREDEF_BOOLEAN_TO_BOOLEAN = new MethodSignature("scala/Predef$", "boolean2Boolean", "(Z)Ljava/lang/Boolean;");
   public final static MethodSignature PREDEF_INTEGER_TO_INT = new MethodSignature("scala/Predef$", "Integer2int", "(Ljava/lang/Integer;)I");
   public final static MethodSignature PREDEF_LANGLONG_TO_LONG = new MethodSignature("scala/Predef$", "Long2long", "(Ljava/lang/Long;)J");
   public final static MethodSignature PREDEF_LANGDOUBLE_TO_DOUBLE = new MethodSignature("scala/Predef$", "Double2double", "(Ljava/lang/Double;)D");
   public final static MethodSignature PREDEF_LANGBOOLEAN_TO_BOOLEAN = new MethodSignature("scala/Predef$", "Boolean2boolean", "(Ljava/lang/Boolean;)Z");
   public final static MethodSignature BOX_TO_INTEGER = new MethodSignature("scala/runtime/BoxesRunTime", "boxToInteger", "(I)Ljava/lang/Integer;");
   public final static MethodSignature BOX_TO_LONG = new MethodSignature("scala/runtime/BoxesRunTime", "boxToLong", "(J)Ljava/lang/Long;");
   public final static MethodSignature BOX_TO_DOUBLE = new MethodSignature("scala/runtime/BoxesRunTime", "boxToDouble", "(D)Ljava/lang/Double;");
   public final static MethodSignature BOX_TO_BOOLEAN = new MethodSignature("scala/runtime/BoxesRunTime", "boxToBoolean", "(Z)Ljava/lang/Boolean;");
   public final static MethodSignature UNBOX_TO_INTEGER = new MethodSignature("scala/runtime/BoxesRunTime", "unboxToInt", "(Ljava/lang/Object;)I");
   public final static MethodSignature UNBOX_TO_LONG = new MethodSignature("scala/runtime/BoxesRunTime", "unboxToLong", "(Ljava/lang/Object;)J");
   public final static MethodSignature UNBOX_TO_DOUBLE = new MethodSignature("scala/runtime/BoxesRunTime", "unboxToDouble", "(Ljava/lang/Object;)D");
   public final static MethodSignature UNBOX_TO_BOOLEAN = new MethodSignature("scala/runtime/BoxesRunTime", "unboxToBoolean", "(Ljava/lang/Object;)Z");
   public final static MethodSignature NEW_STRINGBUILDER = new MethodSignature("scala/collection/mutable/StringBuilder", "<init>", "()V");
   public final static MethodSignature NEW_STRINGBUILDER_STRING = new MethodSignature("scala/collection/mutable/StringBuilder", "<init>", "(Ljava/lang/String;)V");
   public final static MethodSignature STRINGBUILDER_APPEND = new MethodSignature("scala/collection/mutable/StringBuilder", "append", "(Ljava/lang/Object;)Lscala/collection/mutable/StringBuilder;");
   public final static MethodSignature STRINGBUILDER_STRING = new MethodSignature("scala/collection/mutable/StringBuilder", "toString", "()Ljava/lang/String;");
   public final static MethodSignature newTuple2 = new MethodSignature("scala/Tuple2", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature tuple2GetOne = new MethodSignature("scala/Tuple2", "_1", "()Ljava/lang/Object;");
   public final static MethodSignature tuple2GetTwo = new MethodSignature("scala/Tuple2", "_2", "()Ljava/lang/Object;");
   public final static MethodSignature newTuple3 = new MethodSignature("scala/Tuple3", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature tuple3GetOne = new MethodSignature("scala/Tuple3", "_1", "()Ljava/lang/Object;");
   public final static MethodSignature tuple3GetTwo = new MethodSignature("scala/Tuple3", "_2", "()Ljava/lang/Object;");
   public final static MethodSignature tuple3GetThree = new MethodSignature("scala/Tuple3", "_3", "()Ljava/lang/Object;");
   public final static MethodSignature newTuple4 = new MethodSignature("scala/Tuple4", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature tuple4GetOne = new MethodSignature("scala/Tuple4", "_1", "()Ljava/lang/Object;");
   public final static MethodSignature tuple4GetTwo = new MethodSignature("scala/Tuple4", "_2", "()Ljava/lang/Object;");
   public final static MethodSignature tuple4GetThree = new MethodSignature("scala/Tuple4", "_3", "()Ljava/lang/Object;");
   public final static MethodSignature tuple4GetFour = new MethodSignature("scala/Tuple4", "_4", "()Ljava/lang/Object;");
   public final static MethodSignature newTuple5 = new MethodSignature("scala/Tuple5", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature tuple5GetOne = new MethodSignature("scala/Tuple5", "_1", "()Ljava/lang/Object;");
   public final static MethodSignature tuple5GetTwo = new MethodSignature("scala/Tuple5", "_2", "()Ljava/lang/Object;");
   public final static MethodSignature tuple5GetThree = new MethodSignature("scala/Tuple5", "_3", "()Ljava/lang/Object;");
   public final static MethodSignature tuple5GetFour = new MethodSignature("scala/Tuple5", "_4", "()Ljava/lang/Object;");
   public final static MethodSignature tuple5GetFive = new MethodSignature("scala/Tuple5", "_5", "()Ljava/lang/Object;");
   public final static MethodSignature newTuple8 = new MethodSignature("scala/Tuple8", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature tuple8GetOne = new MethodSignature("scala/Tuple8", "_1", "()Ljava/lang/Object;");
   public final static MethodSignature tuple8GetTwo = new MethodSignature("scala/Tuple8", "_2", "()Ljava/lang/Object;");
   public final static MethodSignature tuple8GetThree = new MethodSignature("scala/Tuple8", "_3", "()Ljava/lang/Object;");
   public final static MethodSignature tuple8GetFour = new MethodSignature("scala/Tuple8", "_4", "()Ljava/lang/Object;");
   public final static MethodSignature tuple8GetFive = new MethodSignature("scala/Tuple8", "_5", "()Ljava/lang/Object;");
   public final static MethodSignature tuple8GetSix = new MethodSignature("scala/Tuple8", "_6", "()Ljava/lang/Object;");
   public final static MethodSignature tuple8GetSeven = new MethodSignature("scala/Tuple8", "_7", "()Ljava/lang/Object;");
   public final static MethodSignature tuple8GetEight = new MethodSignature("scala/Tuple8", "_8", "()Ljava/lang/Object;");

   public static final Map<MethodSignature, Integer> TUPLE_ACCESSORS = new HashMap<>();
   static {
      TUPLE_ACCESSORS.put(tuple2GetOne, 1);
      TUPLE_ACCESSORS.put(tuple2GetTwo, 2);
      // scala.Tuple2 is specialized, so we need to handle the different specialized variants of it.
      for (String specialization: new String[] {"I", "Z", "D", "C", "J"})
      {
         TUPLE_ACCESSORS.put(
               new MethodSignature(
                     "scala/Tuple2", 
                     "_1$mc" + specialization + "$sp", 
                     "()" + specialization),
               1);
         TUPLE_ACCESSORS.put(
               new MethodSignature(
                     "scala/Tuple2", 
                     "_2$mc" + specialization + "$sp", 
                     "()" + specialization),
               2);
      }
      TUPLE_ACCESSORS.put(tuple3GetOne, 1);
      TUPLE_ACCESSORS.put(tuple3GetTwo, 2);
      TUPLE_ACCESSORS.put(tuple3GetThree, 3);
      TUPLE_ACCESSORS.put(tuple4GetOne, 1);
      TUPLE_ACCESSORS.put(tuple4GetTwo, 2);
      TUPLE_ACCESSORS.put(tuple4GetThree, 3);
      TUPLE_ACCESSORS.put(tuple4GetFour, 4);
      TUPLE_ACCESSORS.put(tuple5GetOne, 1);
      TUPLE_ACCESSORS.put(tuple5GetTwo, 2);
      TUPLE_ACCESSORS.put(tuple5GetThree, 3);
      TUPLE_ACCESSORS.put(tuple5GetFour, 4);
      TUPLE_ACCESSORS.put(tuple5GetFive, 5);
      TUPLE_ACCESSORS.put(tuple8GetOne, 1);
      TUPLE_ACCESSORS.put(tuple8GetTwo, 2);
      TUPLE_ACCESSORS.put(tuple8GetThree, 3);
      TUPLE_ACCESSORS.put(tuple8GetFour, 4);
      TUPLE_ACCESSORS.put(tuple8GetFive, 5);
      TUPLE_ACCESSORS.put(tuple8GetSix, 6);
      TUPLE_ACCESSORS.put(tuple8GetSeven, 7);
      TUPLE_ACCESSORS.put(tuple8GetEight, 8);
   }

   public final static MethodSignature streamSumInt = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "sumInteger", "(Lscala/Function1;)Ljava/lang/Long;");
   public final static MethodSignature streamSumDouble = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "sumDouble", "(Lscala/Function1;)Ljava/lang/Double;");
   public final static MethodSignature streamSumLong = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "sumLong", "(Lscala/Function1;)Ljava/lang/Long;");
   public final static MethodSignature streamSumBigDecimal = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "sumBigDecimal", "(Lscala/Function1;)Ljava/lang/BigDecimal;");
   public final static MethodSignature streamSumBigInteger = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "sumBigInteger", "(Lscala/Function1;)Ljava/lang/BigInteger;");
   public final static MethodSignature streamMax = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "max", "(Lscala/Function1;Lscala/Function1;)Ljava/lang/Object;");
   public final static MethodSignature streamMin = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "min", "(Lscala/Function1;Lscala/Function1;)Ljava/lang/Object;");
   public final static MethodSignature streamAvg = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "avg", "(Lscala/Function1;Lscala/math/Numeric;)Ljava/lang/Double;");
   public final static MethodSignature streamCount = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "count", "()Ljava/lang/Long;");
   public final static MethodSignature streamDistinct = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "distinct", "()Lorg/jinq/orm/stream/scala/JinqIterator;");
   public final static MethodSignature streamSelect = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "select", "(Lscala/Function1;)Lorg/jinq/orm/stream/scala/JinqIterator;");
   public final static MethodSignature streamWhere = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "where", "(Lscala/Function1;)Lorg/jinq/orm/stream/scala/JinqIterator;");
   public final static MethodSignature streamJoin = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "join", "(Lscala/Function1;)Lorg/jinq/orm/stream/scala/JinqIterator;");
   public final static MethodSignature streamGetOnlyValue = new MethodSignature("org/jinq/orm/stream/scala/JinqIterator", "getOnlyValue", "()Ljava/lang/Object;");
   
   private static final Set<MethodSignature> subqueryMethods = 
         new HashSet<>();
   static {
      subqueryMethods.add(streamSumInt);
      subqueryMethods.add(streamSumDouble);
      subqueryMethods.add(streamSumLong);
      subqueryMethods.add(streamSumBigInteger);
      subqueryMethods.add(streamSumBigDecimal);
      subqueryMethods.add(streamMax);
      subqueryMethods.add(streamMin);
      subqueryMethods.add(streamAvg);
      subqueryMethods.add(streamCount);
      subqueryMethods.add(streamDistinct);
      subqueryMethods.add(streamSelect);
      subqueryMethods.add(streamWhere);
      subqueryMethods.add(streamJoin);
      subqueryMethods.add(streamGetOnlyValue);
   }

   static Set<MethodSignature> KnownSafeMethods = new HashSet<>();
   static Set<MethodSignature> KnownSafeStaticMethods = new HashSet<>();
   static {
      KnownSafeMethods.addAll(TUPLE_ACCESSORS.keySet());
      KnownSafeMethods.add(newTuple2);
      KnownSafeMethods.add(newTuple3);
      KnownSafeMethods.add(newTuple4);
      KnownSafeMethods.add(newTuple5);
      KnownSafeMethods.add(newTuple8);
      KnownSafeMethods.add(INQUERYSTREAMSOURCE_STREAM);
      KnownSafeMethods.add(ITERABLE_TO_JINQ);
      KnownSafeMethods.add(STREAM_OF);
      KnownSafeMethods.add(PREDEF_INT_TO_INTEGER);
      KnownSafeMethods.add(PREDEF_LONG_TO_LONG);
      KnownSafeMethods.add(PREDEF_DOUBLE_TO_DOUBLE);
      KnownSafeMethods.add(PREDEF_BOOLEAN_TO_BOOLEAN);
      KnownSafeMethods.add(PREDEF_INTEGER_TO_INT);
      KnownSafeMethods.add(PREDEF_LANGLONG_TO_LONG);
      KnownSafeMethods.add(PREDEF_LANGDOUBLE_TO_DOUBLE);
      KnownSafeMethods.add(PREDEF_LANGBOOLEAN_TO_BOOLEAN);
      KnownSafeMethods.add(NEW_STRINGBUILDER);
      KnownSafeMethods.add(NEW_STRINGBUILDER_STRING);
      KnownSafeMethods.add(STRINGBUILDER_APPEND);
      KnownSafeMethods.add(STRINGBUILDER_STRING);
      KnownSafeMethods.addAll(subqueryMethods);
      KnownSafeStaticMethods.add(BOX_TO_INTEGER);
      KnownSafeStaticMethods.add(BOX_TO_LONG);
      KnownSafeStaticMethods.add(BOX_TO_DOUBLE);
      KnownSafeStaticMethods.add(BOX_TO_BOOLEAN);
      KnownSafeStaticMethods.add(UNBOX_TO_INTEGER);
      KnownSafeStaticMethods.add(UNBOX_TO_LONG);
      KnownSafeStaticMethods.add(UNBOX_TO_DOUBLE);
      KnownSafeStaticMethods.add(UNBOX_TO_BOOLEAN);
   }
   
   private Set<MethodSignature> safeMethods;
   private Set<MethodSignature> oldSafeMethods = null;
   private Set<MethodSignature> safeStaticMethods;
   private Set<MethodSignature> oldSafeStaticMethods = null;

   public ScalaMetamodelUtil(Metamodel metamodel)
   {
      super(metamodel);
   }

   private void calculateScalaSafeMethods(Set<MethodSignature> javaSafeMethods)
   {
      Set<MethodSignature> newSafeMethods = new HashSet<>();
      newSafeMethods.addAll(javaSafeMethods);
      newSafeMethods.addAll(KnownSafeMethods);
      safeMethods = newSafeMethods;
      oldSafeMethods = javaSafeMethods;
   }

   @Override
   public Set<MethodSignature> getSafeMethods()
   {
      Set<MethodSignature> superSafeMethods = super.getSafeMethods();
      if (superSafeMethods == oldSafeMethods)
         return safeMethods;
      calculateScalaSafeMethods(superSafeMethods);
      return safeMethods;
   }
   
   private void calculateScalaSafeStaticMethods(Set<MethodSignature> javaSafeMethods)
   {
      Set<MethodSignature> newSafeStaticMethods = new HashSet<>();
      newSafeStaticMethods.addAll(javaSafeMethods);
      newSafeStaticMethods.addAll(KnownSafeStaticMethods);
      safeStaticMethods = newSafeStaticMethods;
      oldSafeStaticMethods = javaSafeMethods;
   }

   @Override
   public Set<MethodSignature> getSafeStaticMethods()
   {
      Set<MethodSignature> superSafeStaticMethods = super.getSafeStaticMethods();
      if (superSafeStaticMethods == oldSafeStaticMethods)
         return safeStaticMethods;
      calculateScalaSafeStaticMethods(superSafeStaticMethods);
      return safeStaticMethods;
   }
   
   @Override
   public MethodChecker getMethodChecker(boolean isObjectEqualsSafe, boolean isCollectionContainsSafe)
   {
      return new ScalaMethodChecker(
            getSafeMethodAnnotations(), 
            getSafeMethods(), getSafeStaticMethods(),
            isObjectEqualsSafe, isCollectionContainsSafe);
   }
   
   static boolean isAggregateMethod(MethodSignature sig)
   {
      return sig.equals(streamSumInt)
            || sig.equals(streamSumDouble)
            || sig.equals(streamSumLong)
            || sig.equals(streamSumBigInteger)
            || sig.equals(streamSumBigDecimal)
            || sig.equals(streamMax)
            || sig.equals(streamMin)
            || sig.equals(streamAvg)
            || sig.equals(streamCount);
   }
   
   static boolean isStreamMethod(MethodSignature sig)
   {
      return sig.equals(ScalaMetamodelUtil.streamDistinct)
            || sig.equals(ScalaMetamodelUtil.streamSelect)
            || sig.equals(ScalaMetamodelUtil.streamWhere)
            || sig.equals(ScalaMetamodelUtil.streamJoin);
   }


}
