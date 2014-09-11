package org.jinq.jpa.transform;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

class MethodChecker implements PathAnalysisMethodChecker
{
   private final Set<Class<?>> safeMethodAnnotations;
   private final Set<MethodSignature> safeMethods;
   private final Set<MethodSignature> safeStaticMethods;

   public final static MethodSignature jpqlLike = new MethodSignature("org/jinq/jpa/JPQL", "like", "(Ljava/lang/String;Ljava/lang/String;)Z");
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
// TODO: I'm not sure how to cast integers to strings in JPQL   
//   public final static MethodSignature stringBuilderAppendInt = new MethodSignature("java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
   final static Set<MethodSignature> jpqlFunctionMethods = new HashSet<>();
   final static Set<MethodSignature> jpqlFunctionStaticMethods = new HashSet<>();
   static {
      jpqlFunctionStaticMethods.add(jpqlLike);
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
   }
   
   MethodChecker(Set<Class<?>> safeMethodAnnotations,
         Set<MethodSignature> safeMethods,
         Set<MethodSignature> safeStaticMethods)
   {
      this.safeMethodAnnotations = safeMethodAnnotations;
      this.safeMethods = safeMethods;
      this.safeStaticMethods = safeStaticMethods;
   }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isStaticMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature)
    */
   @Override
   public boolean isStaticMethodSafe(MethodSignature m)
   { 
      return safeStaticMethods.contains(m)
            || jpqlFunctionStaticMethods.contains(m); 
   }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature, ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue, java.util.List)
    */
   @Override
   public boolean isMethodSafe(MethodSignature m, TypedValue base, List<TypedValue> args)
      {
         if (safeMethods.contains(m)
               || subqueryMethods.contains(m)
               || jpqlFunctionMethods.contains(m))
         {
            return true;
         }
         else
         {
            // Use reflection to get info about the method (or would it be better
            // to do this through direct bytecode inspection?), and see if it's
            // annotated as safe
            try
            {
               Method reflectedMethod = Annotations.asmMethodSignatureToReflectionMethod(m);
               if (Annotations.methodHasSomeAnnotations(reflectedMethod, safeMethodAnnotations))
                  return true;
            } catch (ClassNotFoundException|NoSuchMethodException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            return false; 
            
         }
      }
}