package ch.epfl.labos.iu.orm.queryll2.path;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jinq.orm.annotations.EntitySupplier;
import org.jinq.orm.annotations.NoSideEffects;
import org.jinq.tuples.Tuple6;
import org.jinq.tuples.Tuple7;
import org.jinq.tuples.Tuple8;
import org.jinq.rebased.org.objectweb.asm.ClassReader;
import org.jinq.rebased.org.objectweb.asm.tree.ClassNode;
import org.jinq.rebased.org.objectweb.asm.tree.MethodNode;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

public class TransformationClassAnalyzer
{
   public static final int MAX_PATHS = 64;
   
   // Mapping of transformation classes to the name of the method
   // containing the actual transformation
   public static Map<String, String> TransformationClassMethods =
      new HashMap<String, String>();
   public static Set<MethodSignature> KnownSafeMethods =
      new HashSet<MethodSignature>();
   public static Set<MethodSignature> KnownSafeStaticMethods =
      new HashSet<MethodSignature>();
   public static Set<Class<?>> SafeMethodAnnotations =
      new HashSet<Class<?>>();
   
   public final static String WHERE_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$Where";
   public final static String WHERE_METHOD = "where";
   public final static String SELECT_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$Select";
   public final static String SELECT_METHOD = "select";
   public final static String AGGREGATESELECT_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$AggregateSelect";
   public final static String AGGREGATESELECT_METHOD = "aggregateSelect";
   public final static String AGGREGATEINTEGER_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$AggregateInteger";
   public final static String AGGREGATEINTEGER_METHOD = "aggregate";
   public final static String AGGREGATEDOUBLE_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$AggregateDouble";
   public final static String AGGREGATEDOUBLE_METHOD = "aggregate";
   public final static String JOIN_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$Join";
   public final static String JOIN_METHOD = "join";
   public final static String GROUP_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$AggregateGroup";
   public final static String GROUP_METHOD = "aggregateSelect";
   public final static String DATESORTER_SUPERCLASS = "ch/epfl/labos/iu/orm/DateSorter";
   public final static String DATESORTER_METHOD = "value";
   public final static String INTSORTER_SUPERCLASS = "ch/epfl/labos/iu/orm/IntSorter";
   public final static String INTSORTER_METHOD = "value";
   public final static String DOUBLESORTER_SUPERCLASS = "ch/epfl/labos/iu/orm/DoubleSorter";
   public final static String DOUBLESORTER_METHOD = "value";
   public final static String STRINGSORTER_SUPERCLASS = "ch/epfl/labos/iu/orm/StringSorter";
   public final static String STRINGSORTER_METHOD = "value";
   public final static String DBSET_CLASS = "Lch/epfl/labos/iu/orm/DBSet;";
   public final static String JINQSTREAM_CLASS = "Lorg/jinq/orm/stream/JinqStream;";
   public final static MethodSignature stringEquals = new MethodSignature("java/lang/String", "equals", "(Ljava/lang/Object;)Z");
   public final static MethodSignature stringCompareTo = new MethodSignature("java/lang/String", "compareTo", "(Ljava/lang/String;)I");
   public final static MethodSignature stringLike = new MethodSignature("ch/epfl/labos/iu/orm/Util", "SQLStringLike", "(Ljava/lang/String;Ljava/lang/String;)Z");
   public final static MethodSignature newStringBuilderString = new MethodSignature("java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
   public final static MethodSignature newStringBuilder = new MethodSignature("java/lang/StringBuilder", "<init>", "()V");
   public final static MethodSignature stringBuilderAppendString = new MethodSignature("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
   public final static MethodSignature stringBuilderToString = new MethodSignature("java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
   public final static MethodSignature booleanEquals;
   public final static MethodSignature dateEquals = new MethodSignature("java/util/Date", "equals", "(Ljava/lang/Object;)Z");
   public final static MethodSignature dateBefore = new MethodSignature("java/util/Date", "before", "(Ljava/util/Date;)Z");
   public final static MethodSignature dateAfter = new MethodSignature("java/util/Date", "after", "(Ljava/util/Date;)Z");
   public final static MethodSignature calendarEquals = new MethodSignature("java/util/Calendar", "equals", "(Ljava/lang/Object;)Z");
   public final static MethodSignature calendarBefore = new MethodSignature("java/util/Calendar", "before", "(Ljava/lang/Object;)Z");
   public final static MethodSignature calendarAfter = new MethodSignature("java/util/Calendar", "after", "(Ljava/lang/Object;)Z");
   public final static MethodSignature sqlDateEquals = new MethodSignature("java/sql/Date", "equals", "(Ljava/lang/Object;)Z");
   public final static MethodSignature sqlDateBefore = new MethodSignature("java/sql/Date", "before", "(Ljava/util/Date;)Z");
   public final static MethodSignature sqlDateAfter = new MethodSignature("java/sql/Date", "after", "(Ljava/util/Date;)Z");
   public final static MethodSignature sqlTimeEquals = new MethodSignature("java/sql/Time", "equals", "(Ljava/lang/Object;)Z");
   public final static MethodSignature sqlTimeBefore = new MethodSignature("java/sql/Time", "before", "(Ljava/util/Date;)Z");
   public final static MethodSignature sqlTimeAfter = new MethodSignature("java/sql/Time", "after", "(Ljava/util/Date;)Z");
   public final static MethodSignature sqlTimestampEquals = new MethodSignature("java/sql/Timestamp", "equals", "(Ljava/sql/Timestamp;)Z");
   public final static MethodSignature sqlTimestampBefore = new MethodSignature("java/sql/Timestamp", "before", "(Ljava/sql/Timestamp;)Z");
   public final static MethodSignature sqlTimestampAfter = new MethodSignature("java/sql/Timestamp", "after", "(Ljava/sql/Timestamp;)Z");
   public final static MethodSignature bigDecimalCompareTo = new MethodSignature("java/math/BigDecimal", "compareTo", "(Ljava/math/BigDecimal;)I");
   public final static MethodSignature bigDecimalEquals = new MethodSignature("java/math/BigDecimal", "equals", "(Ljava/lang/Object;)Z");
   public final static MethodSignature bigDecimalAdd = new MethodSignature("java/math/BigDecimal", "add", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;");
   public final static MethodSignature bigDecimalDivide = new MethodSignature("java/math/BigDecimal", "divide", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;");
   public final static MethodSignature bigDecimalMultiply = new MethodSignature("java/math/BigDecimal", "multiply", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;");
   public final static MethodSignature bigDecimalSubtract = new MethodSignature("java/math/BigDecimal", "subtract", "(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;");
   public final static MethodSignature bigIntegerCompareTo = new MethodSignature("java/math/BigInteger", "compareTo", "(Ljava/math/BigInteger;)I");
   public final static MethodSignature bigIntegerEquals = new MethodSignature("java/math/BigInteger", "equals", "(Ljava/lang/Object;)Z");
   public final static MethodSignature bigIntegerAdd = new MethodSignature("java/math/BigInteger", "add", "(Ljava/math/BigInteger;)Ljava/math/BigInteger;");
   public final static MethodSignature bigIntegerDivide = new MethodSignature("java/math/BigInteger", "divide", "(Ljava/math/BigInteger;)Ljava/math/BigInteger;");
   public final static MethodSignature bigIntegerMultiply = new MethodSignature("java/math/BigInteger", "multiply", "(Ljava/math/BigInteger;)Ljava/math/BigInteger;");
   public final static MethodSignature bigIntegerSubtract = new MethodSignature("java/math/BigInteger", "subtract", "(Ljava/math/BigInteger;)Ljava/math/BigInteger;");
   public final static MethodSignature newPair = new MethodSignature("org/jinq/tuples/Pair", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature pairGetOne = new MethodSignature("org/jinq/tuples/Pair", "getOne", "()Ljava/lang/Object;");
   public final static MethodSignature pairGetTwo = new MethodSignature("org/jinq/tuples/Pair", "getTwo", "()Ljava/lang/Object;");
   public final static MethodSignature newTuple3 = new MethodSignature("org/jinq/tuples/Tuple3", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature tuple3GetOne = new MethodSignature("org/jinq/tuples/Tuple3", "getOne", "()Ljava/lang/Object;");
   public final static MethodSignature tuple3GetTwo = new MethodSignature("org/jinq/tuples/Tuple3", "getTwo", "()Ljava/lang/Object;");
   public final static MethodSignature tuple3GetThree = new MethodSignature("org/jinq/tuples/Tuple3", "getThree", "()Ljava/lang/Object;");
   public final static MethodSignature newTuple4 = new MethodSignature("org/jinq/tuples/Tuple4", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature tuple4GetOne = new MethodSignature("org/jinq/tuples/Tuple4", "getOne", "()Ljava/lang/Object;");
   public final static MethodSignature tuple4GetTwo = new MethodSignature("org/jinq/tuples/Tuple4", "getTwo", "()Ljava/lang/Object;");
   public final static MethodSignature tuple4GetThree = new MethodSignature("org/jinq/tuples/Tuple4", "getThree", "()Ljava/lang/Object;");
   public final static MethodSignature tuple4GetFour = new MethodSignature("org/jinq/tuples/Tuple4", "getFour", "()Ljava/lang/Object;");
   public final static MethodSignature newTuple5 = new MethodSignature("org/jinq/tuples/Tuple5", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public final static MethodSignature tuple5GetOne = new MethodSignature("org/jinq/tuples/Tuple5", "getOne", "()Ljava/lang/Object;");
   public final static MethodSignature tuple5GetTwo = new MethodSignature("org/jinq/tuples/Tuple5", "getTwo", "()Ljava/lang/Object;");
   public final static MethodSignature tuple5GetThree = new MethodSignature("org/jinq/tuples/Tuple5", "getThree", "()Ljava/lang/Object;");
   public final static MethodSignature tuple5GetFour = new MethodSignature("org/jinq/tuples/Tuple5", "getFour", "()Ljava/lang/Object;");
   public final static MethodSignature tuple5GetFive = new MethodSignature("org/jinq/tuples/Tuple5", "getFive", "()Ljava/lang/Object;");
   public final static MethodSignature newTuple6;
   public final static MethodSignature tuple6GetOne;
   public final static MethodSignature tuple6GetTwo;
   public final static MethodSignature tuple6GetThree;
   public final static MethodSignature tuple6GetFour;
   public final static MethodSignature tuple6GetFive;
   public final static MethodSignature tuple6GetSix;
   public final static MethodSignature newTuple7;
   public final static MethodSignature tuple7GetOne;
   public final static MethodSignature tuple7GetTwo;
   public final static MethodSignature tuple7GetThree;
   public final static MethodSignature tuple7GetFour;
   public final static MethodSignature tuple7GetFive;
   public final static MethodSignature tuple7GetSix;
   public final static MethodSignature tuple7GetSeven;
   public final static MethodSignature newTuple8;
   public final static MethodSignature tuple8GetOne;
   public final static MethodSignature tuple8GetTwo;
   public final static MethodSignature tuple8GetThree;
   public final static MethodSignature tuple8GetFour;
   public final static MethodSignature tuple8GetFive;
   public final static MethodSignature tuple8GetSix;
   public final static MethodSignature tuple8GetSeven;
   public final static MethodSignature tuple8GetEight;
   public final static MethodSignature streamSumInt = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumInteger", "(Lorg/jinq/orm/stream/JinqStream$CollectInteger;)Ljava/lang/Long;");
   public final static MethodSignature streamSumDouble = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumDouble", "(Lorg/jinq/orm/stream/JinqStream$CollectDouble;)Ljava/lang/Double;");
   public final static MethodSignature streamMax = new MethodSignature("org/jinq/orm/stream/JinqStream", "max", "(Lorg/jinq/orm/stream/JinqStream$CollectComparable;)Ljava/lang/Comparable;");
   public final static MethodSignature streamMin = new MethodSignature("org/jinq/orm/stream/JinqStream", "min", "(Lorg/jinq/orm/stream/JinqStream$CollectComparable;)Ljava/lang/Comparable;");
   public final static MethodSignature streamFrom = new MethodSignature("org/jinq/orm/stream/JinqStream", "from", "(Ljava/util/Collection;)Lorg/jinq/orm/stream/JinqStream;");
   public final static MethodSignature streamOf = new MethodSignature("org/jinq/orm/stream/JinqStream", "of", "(Ljava/lang/Object;)Lorg/jinq/orm/stream/JinqStream;");
   public final static MethodSignature dbsetSumInt = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "sumInt", "(Lch/epfl/labos/iu/orm/DBSet$AggregateInteger;)I");
   public final static MethodSignature dbsetSumDouble = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "sumDouble", "(Lch/epfl/labos/iu/orm/DBSet$AggregateDouble;)D");
   public final static MethodSignature dbsetMaxInt = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "maxInt", "(Lch/epfl/labos/iu/orm/DBSet$AggregateInteger;)I");
   public final static MethodSignature dbsetMaxDouble = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "maxDouble", "(Lch/epfl/labos/iu/orm/DBSet$AggregateDouble;)D");
   public final static MethodSignature dbsetWhere = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "where", "(Lch/epfl/labos/iu/orm/DBSet$Where;)Lch/epfl/labos/iu/orm/DBSet;");
   public final static MethodSignature dbsetToStream = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "jinqStream", "()Lorg/jinq/orm/stream/JinqStream;");
   public final static MethodSignature integerValueOf = new MethodSignature("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
   public final static MethodSignature longValueOf = new MethodSignature("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
   public final static MethodSignature doubleValueOf = new MethodSignature("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
   public final static MethodSignature floatValueOf = new MethodSignature("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
   public final static MethodSignature booleanValueOf = new MethodSignature("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
   public final static MethodSignature integerIntValue = new MethodSignature("java/lang/Integer", "intValue", "()I");
   public final static MethodSignature longLongValue = new MethodSignature("java/lang/Long", "longValue", "()J");
   public final static MethodSignature doubleDoubleValue = new MethodSignature("java/lang/Double", "doubleValue", "()D");
   public final static MethodSignature floatFloatValue = new MethodSignature("java/lang/Float", "floatValue", "()F");
   public final static MethodSignature booleanBooleanValue = new MethodSignature("java/lang/Boolean", "booleanValue", "()Z");
   public final static MethodSignature bigIntegerValueOfLong = new MethodSignature("java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;");
   public final static MethodSignature newBigDecimalLong = new MethodSignature("java/math/BigDecimal", "<init>", "(J)V");
   public final static MethodSignature newBigDecimalDouble = new MethodSignature("java/math/BigDecimal", "<init>", "(D)V");
   public final static MethodSignature newBigDecimalInt = new MethodSignature("java/math/BigDecimal", "<init>", "(I)V");
   public final static MethodSignature newBigDecimalBigInteger = new MethodSignature("java/math/BigDecimal", "<init>", "(Ljava/math/BigInteger;)V");
   public final static MethodSignature bigDecimalDoubleValue = new MethodSignature("java/math/BigDecimal", "doubleValue", "()D");
   public final static MethodSignature bigIntegerDoubleValue = new MethodSignature("java/math/BigInteger", "doubleValue", "()D");

   static {
      try {
         booleanEquals = MethodSignature.fromMethod(Boolean.class.getMethod("equals", Object.class));
         newTuple6 = MethodSignature.fromConstructor(Tuple6.class.getConstructor(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
         tuple6GetOne = MethodSignature.fromMethod(Tuple6.class.getMethod("getOne"));
         tuple6GetTwo = MethodSignature.fromMethod(Tuple6.class.getMethod("getTwo"));
         tuple6GetThree = MethodSignature.fromMethod(Tuple6.class.getMethod("getThree"));
         tuple6GetFour = MethodSignature.fromMethod(Tuple6.class.getMethod("getFour"));
         tuple6GetFive = MethodSignature.fromMethod(Tuple6.class.getMethod("getFive"));
         tuple6GetSix = MethodSignature.fromMethod(Tuple6.class.getMethod("getSix"));
         newTuple7 = MethodSignature.fromConstructor(Tuple7.class.getConstructor(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
         tuple7GetOne = MethodSignature.fromMethod(Tuple7.class.getMethod("getOne"));
         tuple7GetTwo = MethodSignature.fromMethod(Tuple7.class.getMethod("getTwo"));
         tuple7GetThree = MethodSignature.fromMethod(Tuple7.class.getMethod("getThree"));
         tuple7GetFour = MethodSignature.fromMethod(Tuple7.class.getMethod("getFour"));
         tuple7GetFive = MethodSignature.fromMethod(Tuple7.class.getMethod("getFive"));
         tuple7GetSix = MethodSignature.fromMethod(Tuple7.class.getMethod("getSix"));
         tuple7GetSeven = MethodSignature.fromMethod(Tuple7.class.getMethod("getSeven"));
         newTuple8 = MethodSignature.fromConstructor(Tuple8.class.getConstructor(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
         tuple8GetOne = MethodSignature.fromMethod(Tuple8.class.getMethod("getOne"));
         tuple8GetTwo = MethodSignature.fromMethod(Tuple8.class.getMethod("getTwo"));
         tuple8GetThree = MethodSignature.fromMethod(Tuple8.class.getMethod("getThree"));
         tuple8GetFour = MethodSignature.fromMethod(Tuple8.class.getMethod("getFour"));
         tuple8GetFive = MethodSignature.fromMethod(Tuple8.class.getMethod("getFive"));
         tuple8GetSix = MethodSignature.fromMethod(Tuple8.class.getMethod("getSix"));
         tuple8GetSeven = MethodSignature.fromMethod(Tuple8.class.getMethod("getSeven"));
         tuple8GetEight = MethodSignature.fromMethod(Tuple8.class.getMethod("getEight"));
      } 
      catch (NoSuchMethodException | SecurityException e)
      {
         e.printStackTrace();
         throw new IllegalArgumentException("Cannot find a method needed for analysis", e);
      }
      
   }
   
   static {
      TransformationClassMethods.put(GROUP_INTERFACE, GROUP_METHOD);
      TransformationClassMethods.put(JOIN_INTERFACE, JOIN_METHOD);
      TransformationClassMethods.put(AGGREGATEDOUBLE_INTERFACE, AGGREGATEDOUBLE_METHOD);
      TransformationClassMethods.put(AGGREGATEINTEGER_INTERFACE, AGGREGATEINTEGER_METHOD);
      TransformationClassMethods.put(AGGREGATESELECT_INTERFACE, AGGREGATESELECT_METHOD);
      TransformationClassMethods.put(SELECT_INTERFACE, SELECT_METHOD);
      TransformationClassMethods.put(WHERE_INTERFACE, WHERE_METHOD);
      TransformationClassMethods.put(DATESORTER_SUPERCLASS, DATESORTER_METHOD);
      TransformationClassMethods.put(INTSORTER_SUPERCLASS, INTSORTER_METHOD);
      TransformationClassMethods.put(DOUBLESORTER_SUPERCLASS, DOUBLESORTER_METHOD);
      TransformationClassMethods.put(STRINGSORTER_SUPERCLASS, STRINGSORTER_METHOD);
      KnownSafeMethods.add(stringEquals);
      KnownSafeMethods.add(stringCompareTo);
      KnownSafeStaticMethods.add(stringLike);
      KnownSafeMethods.add(newStringBuilderString);
      KnownSafeMethods.add(newStringBuilder);
      KnownSafeMethods.add(stringBuilderAppendString);
      KnownSafeMethods.add(stringBuilderToString);
      KnownSafeMethods.add(booleanEquals);
      KnownSafeMethods.add(dateEquals);
      KnownSafeMethods.add(dateBefore);
      KnownSafeMethods.add(dateAfter);
      KnownSafeMethods.add(calendarEquals);
      KnownSafeMethods.add(calendarBefore);
      KnownSafeMethods.add(calendarAfter);
      KnownSafeMethods.add(sqlDateEquals);
      KnownSafeMethods.add(sqlDateBefore);
      KnownSafeMethods.add(sqlDateAfter);
      KnownSafeMethods.add(sqlTimeEquals);
      KnownSafeMethods.add(sqlTimeBefore);
      KnownSafeMethods.add(sqlTimeAfter);
      KnownSafeMethods.add(sqlTimestampEquals);
      KnownSafeMethods.add(sqlTimestampBefore);
      KnownSafeMethods.add(sqlTimestampAfter);
      KnownSafeMethods.add(bigDecimalCompareTo);
      KnownSafeMethods.add(bigDecimalEquals);
      KnownSafeMethods.add(bigDecimalAdd);
      KnownSafeMethods.add(bigDecimalDivide);
      KnownSafeMethods.add(bigDecimalMultiply);
      KnownSafeMethods.add(bigDecimalSubtract);
      KnownSafeMethods.add(bigIntegerCompareTo);
      KnownSafeMethods.add(bigIntegerEquals);
      KnownSafeMethods.add(bigIntegerAdd);
      KnownSafeMethods.add(bigIntegerDivide);
      KnownSafeMethods.add(bigIntegerMultiply);
      KnownSafeMethods.add(bigIntegerSubtract);
      KnownSafeMethods.add(newPair);
      KnownSafeMethods.add(pairGetOne);
      KnownSafeMethods.add(pairGetTwo);
      KnownSafeMethods.add(newTuple3);
      KnownSafeMethods.add(tuple3GetOne);
      KnownSafeMethods.add(tuple3GetTwo);
      KnownSafeMethods.add(tuple3GetThree);
      KnownSafeMethods.add(newTuple4);
      KnownSafeMethods.add(tuple4GetOne);
      KnownSafeMethods.add(tuple4GetTwo);
      KnownSafeMethods.add(tuple4GetThree);
      KnownSafeMethods.add(tuple4GetFour);
      KnownSafeMethods.add(newTuple5);
      KnownSafeMethods.add(tuple5GetOne);
      KnownSafeMethods.add(tuple5GetTwo);
      KnownSafeMethods.add(tuple5GetThree);
      KnownSafeMethods.add(tuple5GetFour);
      KnownSafeMethods.add(tuple5GetFive);
      KnownSafeMethods.add(newTuple6);
      KnownSafeMethods.add(tuple6GetOne);
      KnownSafeMethods.add(tuple6GetTwo);
      KnownSafeMethods.add(tuple6GetThree);
      KnownSafeMethods.add(tuple6GetFour);
      KnownSafeMethods.add(tuple6GetFive);
      KnownSafeMethods.add(tuple6GetSix);
      KnownSafeMethods.add(newTuple7);
      KnownSafeMethods.add(tuple7GetOne);
      KnownSafeMethods.add(tuple7GetTwo);
      KnownSafeMethods.add(tuple7GetThree);
      KnownSafeMethods.add(tuple7GetFour);
      KnownSafeMethods.add(tuple7GetFive);
      KnownSafeMethods.add(tuple7GetSix);
      KnownSafeMethods.add(tuple7GetSeven);
      KnownSafeMethods.add(newTuple8);
      KnownSafeMethods.add(tuple8GetOne);
      KnownSafeMethods.add(tuple8GetTwo);
      KnownSafeMethods.add(tuple8GetThree);
      KnownSafeMethods.add(tuple8GetFour);
      KnownSafeMethods.add(tuple8GetFive);
      KnownSafeMethods.add(tuple8GetSix);
      KnownSafeMethods.add(tuple8GetSeven);
      KnownSafeMethods.add(tuple8GetEight);
      KnownSafeStaticMethods.add(streamFrom);
      KnownSafeStaticMethods.add(streamOf);
      KnownSafeStaticMethods.add(bigIntegerValueOfLong);
      KnownSafeMethods.add(newBigDecimalLong);
      KnownSafeMethods.add(newBigDecimalDouble);
      KnownSafeMethods.add(newBigDecimalInt);
      KnownSafeMethods.add(newBigDecimalBigInteger);
      KnownSafeMethods.add(bigDecimalDoubleValue);
      KnownSafeMethods.add(bigIntegerDoubleValue);

      SafeMethodAnnotations.add(NoSideEffects.class);
      SafeMethodAnnotations.add(EntitySupplier.class);
   }
   
   ClassNode cl = new ClassNode();
   public TransformationClassAnalyzer(File f) throws IOException
   {
      FileInputStream fis = new FileInputStream(f);
      ClassReader reader = new ClassReader(fis);
      reader.accept(cl,0);
      fis.close();
   }

   public TransformationClassAnalyzer(String className) throws IOException
   {
      this(className, null);
   }

   public TransformationClassAnalyzer(String className, ClassLoader alternateClassLoader) throws IOException
   {
      ClassReader reader = null;
      try {
         reader = new ClassReader(className);
      } 
      catch (IOException e)
      {
         // The system class loader didn't work. Try using our own 
         // class loader to load the class instead
         String classFileName = className.replace(".", "/") + ".class";
         InputStream classStream = this.getClass().getClassLoader().getResourceAsStream(classFileName);
         // Try the alternate class loader if the user supplied one.
         if (classStream == null && alternateClassLoader != null)
            classStream = alternateClassLoader.getResourceAsStream(classFileName);
         reader = new ClassReader(classStream);
         // TODO: Ideally, we should find the classloader of the lambda itself,
         // and use that to load the class file of the lambda.
      }
      reader.accept(cl,0);
   }
   
   public <T, U> void analyze(StaticMethodAnalysisStorage analysisResults, PathAnalysisSupplementalFactory<T, U> pathAnalysisFactory)
   {
      // TODO: Analyze the constructor
      
      // Find the method containing the transformation code
      List<String> interfacesAndParent = new ArrayList<String>();
      interfacesAndParent.addAll((List<String>)cl.interfaces);
      interfacesAndParent.add(cl.superName);
      for (String i: interfacesAndParent)
      {
         String expectedMethodName = TransformationClassMethods.get(i);
         if (expectedMethodName == null) continue;
         MethodNode nonSpecificMethod = null;
         MethodNode specificMethod = null;
         int specificity = Integer.MAX_VALUE;
         for (MethodNode m: (List<MethodNode>)cl.methods)
         {
            if (!m.name.equals(expectedMethodName))
               continue;
            // TODO: Check the method parameters properly here instead
            // of this quick hack
            if (m.desc.split("Ljava/lang/Object").length < specificity)
            {
               specificMethod = m;
               specificity = m.desc.split("Ljava/lang/Object").length;
            }
            else
               nonSpecificMethod = m;
         }
         
         // TODO: Right now we assume that there is always a specific
         // (reified) method and that we can ignore the generic version
         // of the method. Plus we assume that the generic version of the
         // method always passes control directly to the specific version
         try {
//            System.out.println(specificMethod.name + " " + specificMethod.signature + " " + specificMethod.desc);
            U analysis = analyzeMethod(specificMethod, pathAnalysisFactory);
            if (analysis != null)
            {
               analysisResults.storeMethodAnalysis(i, cl.name, analysis);
//               System.out.println("Ok");
            }
         } catch(AnalyzerException e)
         {
            e.printStackTrace();
         }
      }
   }

   public <T, U> U analyzeLambdaMethod(String methodName, String methodSignature, PathAnalysisSupplementalFactory<T, U> pathAnalysisFactory) throws AnalyzerException
   {
      MethodNode specificMethod = null;
      for (MethodNode m: (List<MethodNode>)cl.methods)
      {
         if (!m.name.equals(methodName) || !m.desc.equals(methodSignature))
            continue;
         specificMethod = m;
      }
      if (specificMethod != null)
         return analyzeMethod(specificMethod, pathAnalysisFactory);
      return null;
   }
   
   <T, U> U analyzeMethod(MethodNode m, PathAnalysisSupplementalFactory<T, U> pathAnalysisFactory) throws AnalyzerException
   {
      // TODO: Various checks (e.g. no try/catch blocks, exceptions, etc.)
      if (m.tryCatchBlocks.size() > 0) return null;
      
      // Build a control flow graph
      //
      CFG cfg = new CFG(cl.name, m);
      
      // Given the index of an instruction, returns a list of indices of 
      // successor instructions.
      
      // Check that the control flow graph has no loops.
      int[]visitStatus = new int[m.instructions.size()];  
      if (hasLoops(cfg, visitStatus, 0)) return null;
      
      // TODO: Check for instructions causing side-effects
      
      // Break the code into paths
      List<CodePath> paths = CodePath.breakIntoPaths(cfg, m, cl.name, MAX_PATHS);
      if (paths == null)
         return null;
      
      // Symbolically execute each path to figure out what each path does
      U analysis = pathAnalysisFactory.createMethodAnalysisResults();
      for (CodePath path: paths)
      {
         PathAnalysisMethodChecker methodChecker = pathAnalysisFactory.createMethodChecker();

         PathAnalysis pathResults = 
            path.calculateReturnValueAndConditions(cl, m, methodChecker);
         pathAnalysisFactory.addPath(analysis, pathResults.sideEffects, pathResults.returnValue, pathResults.conditions, methodChecker);
      }
      return analysis;
   }
   
   boolean hasLoops(CFG cfg, int[]visitStatus, int index)
   {
      final int VISITING = 1;
      final int VISITED = 2;
      
      // reached an instruction with no successor (probably a return instruction)
      if (cfg.succsOf(index) == null) return false;
      
      // Check if we've visited the node and finished exploring all sub-branches
      if (visitStatus[index] == VISITED) return false;

      // Check if we've looped back to visit a node we haven't finished exploring
      if (visitStatus[index] == VISITING) return true;
      
      // Mark node as being explored
      visitStatus[index] = VISITING;
      
      // Visit successors of this node and see if we loop back
      for (int succ: cfg.succsOf(index))
         if (hasLoops(cfg, visitStatus, succ))
            return true;
      
      // Mark this node as having been explored successfully 
      visitStatus[index] = VISITED;
      return false;
   }
}
