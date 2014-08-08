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

final class MethodChecker implements PathAnalysisMethodChecker
{
   private final Set<Class<?>> safeMethodAnnotations;
   private final Set<MethodSignature> safeMethods;
   private final Set<MethodSignature> safeStaticMethods;

   public static MethodSignature streamSumInt = TransformationClassAnalyzer.streamSumInt;
   public static MethodSignature streamSumDouble = TransformationClassAnalyzer.streamSumDouble;
   public static MethodSignature streamSumLong = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumLong", "(Lorg/jinq/orm/stream/JinqStream$CollectLong;)Ljava/lang/Long;");
   public static MethodSignature streamSumBigDecimal = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumBigDecimal", "(Lorg/jinq/orm/stream/JinqStream$CollectBigDecimal;)Ljava/math/BigDecimal;");
   public static MethodSignature streamSumBigInteger = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumBigInteger", "(Lorg/jinq/orm/stream/JinqStream$CollectBigInteger;)Ljava/math/BigInteger;");
   public static MethodSignature streamMax = TransformationClassAnalyzer.streamMax;
   public static MethodSignature streamMin = TransformationClassAnalyzer.streamMin;
   public static MethodSignature streamAvg = new MethodSignature("org/jinq/orm/stream/JinqStream", "avg", "(Lorg/jinq/orm/stream/JinqStream$CollectNumber;)Ljava/lang/Double;");
   public static MethodSignature streamCount = new MethodSignature("org/jinq/orm/stream/JinqStream", "count", "()J");
   
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
      { return safeStaticMethods.contains(m); }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature, ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue, java.util.List)
    */
   @Override
   public boolean isMethodSafe(MethodSignature m, TypedValue base, List<TypedValue> args)
      {
         if (safeMethods.contains(m))
         {
            return true;
         }
         else if (subqueryMethods.contains(m))
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