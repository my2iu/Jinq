package org.jinq.jpa.transform;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter.OperationSideEffect;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

class MethodChecker implements PathAnalysisMethodChecker
{
   private final Set<Class<?>> safeMethodAnnotations;
   private final Set<MethodSignature> safeMethods;
   private final Set<MethodSignature> safeStaticMethods;
   private final boolean isObjectEqualsSafe;

   public final static MethodSignature objectEquals = new MethodSignature("java/lang/Object", "equals", "(Ljava/lang/Object;)Z");

   public final static MethodSignature jpqlLike = new MethodSignature("org/jinq/jpa/JPQL", "like", "(Ljava/lang/String;Ljava/lang/String;)Z");
   public final static MethodSignature jpqlIn = new MethodSignature("org/jinq/jpa/JPQL", "in", "(Ljava/lang/Object;Ljava/util/Collection;)Z");
   public final static MethodSignature mathSqrt = new MethodSignature("java/lang/Math", "sqrt", "(D)D");
   public final static MethodSignature mathAbsDouble = new MethodSignature("java/lang/Math", "abs", "(D)D");
   public final static MethodSignature mathAbsInt = new MethodSignature("java/lang/Math", "abs", "(I)I");
   public final static MethodSignature mathAbsLong = new MethodSignature("java/lang/Math", "abs", "(J)J");
   public final static MethodSignature bigDecimalAbs = new MethodSignature("java/math/BigDecimal", "abs", "()Ljava/math/BigDecimal;");
   public final static MethodSignature bigIntegerAbs = new MethodSignature("java/math/BigInteger", "abs", "()Ljava/math/BigInteger;");
   public final static MethodSignature stringToUpper = new MethodSignature("java/lang/String", "toUpperCase", "()Ljava/lang/String;");
   public final static MethodSignature stringToLower = new MethodSignature("java/lang/String", "toLowerCase", "()Ljava/lang/String;");
   public final static MethodSignature stringValueOfObject = new MethodSignature("java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
   public final static MethodSignature stringTrim = new MethodSignature("java/lang/String", "trim", "()Ljava/lang/String;");
   public final static MethodSignature stringLength = new MethodSignature("java/lang/String", "length", "()I");
   public final static MethodSignature stringSubstring = new MethodSignature("java/lang/String", "substring", "(II)Ljava/lang/String;");
   public final static MethodSignature stringIndexOf = new MethodSignature("java/lang/String", "indexOf", "(Ljava/lang/String;)I");
   public final static MethodSignature collectionContains = new MethodSignature("java/util/Collection", "contains", "(Ljava/lang/Object;)Z");
   public final static MethodSignature setContains = new MethodSignature("java/util/Set", "contains", "(Ljava/lang/Object;)Z");
   public final static MethodSignature listContains = new MethodSignature("java/util/List", "contains", "(Ljava/lang/Object;)Z");
// TODO: I'm not sure how to cast integers to strings in JPQL   
//   public final static MethodSignature stringBuilderAppendInt = new MethodSignature("java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
   final static Set<MethodSignature> jpqlFunctionMethods = new HashSet<>();
   final static Set<MethodSignature> jpqlFunctionStaticMethods = new HashSet<>();
   static {
      jpqlFunctionStaticMethods.add(jpqlLike);
      jpqlFunctionStaticMethods.add(jpqlIn);
      jpqlFunctionStaticMethods.add(mathSqrt);
      jpqlFunctionStaticMethods.add(mathAbsDouble);
      jpqlFunctionStaticMethods.add(mathAbsInt);
      jpqlFunctionStaticMethods.add(mathAbsLong);
      jpqlFunctionStaticMethods.add(stringValueOfObject);
      jpqlFunctionMethods.add(bigDecimalAbs);
      jpqlFunctionMethods.add(bigIntegerAbs);
      jpqlFunctionMethods.add(stringToUpper);
      jpqlFunctionMethods.add(stringToLower);
      jpqlFunctionMethods.add(stringTrim);
      jpqlFunctionMethods.add(stringLength);
      jpqlFunctionMethods.add(stringSubstring);
      jpqlFunctionMethods.add(stringIndexOf);
      jpqlFunctionMethods.add(collectionContains);
      jpqlFunctionMethods.add(setContains);
      jpqlFunctionMethods.add(listContains);
   }
   
   public final static MethodSignature streamSumInt = TransformationClassAnalyzer.streamSumInt;
   public final static MethodSignature streamSumDouble = TransformationClassAnalyzer.streamSumDouble;
   public final static MethodSignature streamSumLong = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumLong", "(Lorg/jinq/orm/stream/JinqStream$CollectLong;)Ljava/lang/Long;");
   public final static MethodSignature streamSumBigDecimal = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumBigDecimal", "(Lorg/jinq/orm/stream/JinqStream$CollectBigDecimal;)Ljava/math/BigDecimal;");
   public final static MethodSignature streamSumBigInteger = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumBigInteger", "(Lorg/jinq/orm/stream/JinqStream$CollectBigInteger;)Ljava/math/BigInteger;");
   public final static MethodSignature streamMax = TransformationClassAnalyzer.streamMax;
   public final static MethodSignature streamMin = TransformationClassAnalyzer.streamMin;
   public final static MethodSignature streamAvg = new MethodSignature("org/jinq/orm/stream/JinqStream", "avg", "(Lorg/jinq/orm/stream/JinqStream$CollectNumber;)Ljava/lang/Double;");
   public final static MethodSignature streamCount = new MethodSignature("org/jinq/orm/stream/JinqStream", "count", "()J");
   public final static MethodSignature streamDistinct = new MethodSignature("org/jinq/orm/stream/JinqStream", "distinct", "()Lorg/jinq/orm/stream/JinqStream;");
   public final static MethodSignature streamSelect = new MethodSignature("org/jinq/orm/stream/JinqStream", "select", "(Lorg/jinq/orm/stream/JinqStream$Select;)Lorg/jinq/orm/stream/JinqStream;");
   public final static MethodSignature streamWhere = new MethodSignature("org/jinq/orm/stream/JinqStream", "where", "(Lorg/jinq/orm/stream/JinqStream$Where;)Lorg/jinq/orm/stream/JinqStream;");
   public final static MethodSignature streamJoin = new MethodSignature("org/jinq/orm/stream/JinqStream", "join", "(Lorg/jinq/orm/stream/JinqStream$Join;)Lorg/jinq/orm/stream/JinqStream;");
   public final static MethodSignature streamGetOnlyValue = new MethodSignature("org/jinq/orm/stream/JinqStream", "getOnlyValue", "()Ljava/lang/Object;");
   
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
   
   MethodChecker(Set<Class<?>> safeMethodAnnotations,
         Set<MethodSignature> safeMethods,
         Set<MethodSignature> safeStaticMethods, 
         boolean isObjectEqualsSafe)
   {
      this.safeMethodAnnotations = safeMethodAnnotations;
      this.safeMethods = safeMethods;
      this.safeStaticMethods = safeStaticMethods;
      this.isObjectEqualsSafe = isObjectEqualsSafe;
   }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isStaticMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature)
    */
   @Override
   public OperationSideEffect isStaticMethodSafe(MethodSignature m)
   { 
      return (safeStaticMethods.contains(m)
            || jpqlFunctionStaticMethods.contains(m)) ? OperationSideEffect.NONE : OperationSideEffect.UNSAFE; 
   }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature, ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue, java.util.List)
    */
   @Override
   public OperationSideEffect isMethodSafe(MethodSignature m, TypedValue base,
         List<TypedValue> args)
   {
      if (isObjectEqualsSafe && objectEquals.equals(m))
      {
         return OperationSideEffect.NONE;
      } else if (safeMethods.contains(m) || subqueryMethods.contains(m)
            || jpqlFunctionMethods.contains(m))
      {
         return OperationSideEffect.NONE;
      } else
      {
         // Use reflection to get info about the method (or would it be better
         // to do this through direct bytecode inspection?), and see if it's
         // annotated as safe
         try
         {
            Method reflectedMethod = Annotations
                  .asmMethodSignatureToReflectionMethod(m);
            if (Annotations.methodHasSomeAnnotations(reflectedMethod,
                  safeMethodAnnotations))
               return OperationSideEffect.NONE;
         } catch (ClassNotFoundException | NoSuchMethodException e)
         {
            // Eat the error
         }
         return OperationSideEffect.UNSAFE;

      }
   }

   @Override
   public boolean isFluentChaining(MethodSignature sig)
   {
      if (TransformationClassAnalyzer.stringBuilderAppendString.equals(sig))
         return true;
      return false;
   }

   @Override
   public boolean isPutFieldAllowed()
   {
      return false;
   }
}