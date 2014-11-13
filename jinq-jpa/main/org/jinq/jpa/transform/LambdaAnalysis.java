package org.jinq.jpa.transform;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.path.MethodAnalysisResults;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisFactory;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

import com.user00.thunk.SerializedLambda;

/**
 * Holds analysis information about the code for a lambda.
 */
public class LambdaAnalysis 
{
   public static class LambdaAsClassAnalysisConfig
   {
      public Method findLambdaMethod(Class<?> lambdaClass)
            throws AnalyzerException
      {
         String methodName = "apply";
         Method matchingMethod = null;
         Method[] classMethods = lambdaClass.getDeclaredMethods();
         for (Method m: classMethods)
         {
            if (m.getName().matches(methodName + "\\$mc[^$]*\\$sp"))
            {
               matchingMethod = m;
               break;
            }
            if (!m.getName().equals(methodName)) continue;
            if (matchingMethod != null)
            {
               // Try to choose the most specific method
               int newCount = countObjectParameters(m);
               int oldCount = countObjectParameters(matchingMethod); 
               if (newCount == oldCount)
                  throw new AnalyzerException(null, "Multiple methods have the expected name for the lambda");
               if (newCount > oldCount) continue;
            }
            matchingMethod = m;
         }
         return matchingMethod;
      }
      
   }
   
   private int numCapturedArgs;
   private int numLambdaArgs;
   /**
    * JPAQueryComposer stores the lambdas that are chained together to create a query in a list.
    * The lambdaIndex refers to the index of this lambda in the list of lambdas used to create
    * the resulting query.
    */
   private int lambdaIndex;
   /**
    * Some lambdas are from sublambdas inside other lambdas. In that
    * case, we don't have the actual captured args, but we know how the 
    * captured args are determined from the parent.
    */
   private List<TypedValue> indirectCapturedArgs;
   /**
    * Scala lambdas are actual Java classes and objects. Lambda parameters
    * are stored as fields of the object (as opposed to as extra method
    * arguments). This variable is true if this lambda format is used. 
    */
   private boolean usesParametersAsFields;
   MethodAnalysisResults symbolicAnalysis;

   public static LambdaAnalysis fullyAnalyzeClassAsLambda(LambdaInfo lambdaInfo, LambdaAsClassAnalysisConfig lambdaAsClassConfig, int numLambdaArgs, MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe, boolean throwExceptionOnFailure)
   {
      try {
         MethodAnalysisResults analysis = analyzeScalaLambdaClass(lambdaInfo.Lambda.getClass(), metamodel, lambdaAsClassConfig, lambdaInfo.Lambda.getClass().getClassLoader(), isObjectEqualsSafe);
         if (analysis == null) 
         {
            if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code");
            return null;
         }
         return new LambdaAnalysis(lambdaInfo.Lambda, analysis, numLambdaArgs, lambdaInfo.lambdaIndex);
      } 
      catch (Exception e)
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code", e);
         return null;
      }
   }

   /**
    * Used to analyze a lambda when we only have the name of the class used as the lambda
    * and not an actual reference to the lambda. 
    */
//   public static LambdaAnalysis analyzeClassAsLambda(MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe, Handle lambdaHandle, List<TypedValue> indirectCapturedArgs, boolean throwExceptionOnFailure)
   public static LambdaAnalysis analyzeClassAsLambda(MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe, LambdaAsClassAnalysisConfig lambdaAsClassConfig, String className, boolean throwExceptionOnFailure) 
   {
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      try {
         Class<?> c = Class.forName(className);
         MethodAnalysisResults analysis = analyzeScalaLambdaClass(c, metamodel, lambdaAsClassConfig, alternateClassLoader, isObjectEqualsSafe);
         if (analysis == null) 
         {
            if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code");
            return null;
         }
         // TODO: Handle lambda arguments properly
         System.err.println("Handle number of lambda args for subqueries properly.");
         System.err.println("Handle lambda arguments in general properly");
         return new LambdaAnalysis(analysis, new ArrayList<>(), 1);
      }
      catch (IOException e)
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Encountered problems when trying to load the code for your lambdas. You may need to supply a lambdaClassLoader hint to Jinq to help it find your lambdas.", e);
         return null;
      }
      catch (AnalyzerException e)
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code", e);
         return null;
      } catch (ClassNotFoundException e)
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not load the class of the lambda", e);
         return null;
      }
   }

   public static LambdaAnalysis fullyAnalyzeLambda(LambdaInfo lambdaInfo, MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe, boolean throwExceptionOnFailure)
   {
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      SerializedLambda s = lambdaInfo.serializedLambda;
      try {
         if (s == null) return null;
         MethodAnalysisResults analysis = analyzeLambda(metamodel, alternateClassLoader, isObjectEqualsSafe, s.implClass, s.implMethodName, s.implMethodSignature);
         if (analysis == null) 
         {
            if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code");
            return null;
         }
         return new LambdaAnalysis(lambdaInfo.Lambda, s, analysis, lambdaInfo.lambdaIndex);
      } 
      catch (Exception e)
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code", e);
         return null;
      }
   }

   /**
    * Used to analyze a lambda when we only have the name of the method used in the lambda
    * and not an actual reference to the lambda. 
    */
   public static LambdaAnalysis analyzeMethod(MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe, Handle lambdaHandle, List<TypedValue> indirectCapturedArgs, boolean throwExceptionOnFailure)
   {
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      try {
         MethodAnalysisResults analysis = analyzeLambda(metamodel, alternateClassLoader, isObjectEqualsSafe, lambdaHandle.getOwner(), lambdaHandle.getName(), lambdaHandle.getDesc());
         if (analysis == null) 
         {
            if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code");
            return null;
         }
         // TODO: Handle lambda arguments properly
         return new LambdaAnalysis(analysis, indirectCapturedArgs, Type.getArgumentTypes(lambdaHandle.getDesc()).length);
      }
      catch (IOException e)
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Encountered problems when trying to load the code for your lambdas. You may need to supply a lambdaClassLoader hint to Jinq to help it find your lambdas.", e);
         return null;
      }
      catch (AnalyzerException e)
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code", e);
         return null;
      }
   }

   private static MethodAnalysisResults analyzeLambda(MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe, String className, String methodName, String methodSignature) throws IOException, AnalyzerException 
   {
      // Open up the corresponding class to analyze
      PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(
            metamodel.getMethodChecker(isObjectEqualsSafe));
      TransformationClassAnalyzer classAnalyzer = 
            new TransformationClassAnalyzer(className, alternateClassLoader);
      MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(methodName, methodSignature, pathAnalysisFactory);
      PathAnalysisSimplifier.cleanAndSimplify(analysis, metamodel.getComparisonMethods(isObjectEqualsSafe));
      return analysis;
   }

   private static MethodAnalysisResults analyzeScalaLambdaClass(Class<?> lambdaClass, MetamodelUtil metamodel, LambdaAsClassAnalysisConfig lambdaAsClass, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe) throws IOException, AnalyzerException 
   {
      // Open up the corresponding class to analyze
      TransformationClassAnalyzer classAnalyzer = 
            new TransformationClassAnalyzer(lambdaClass.getName(), alternateClassLoader);
      Method matchingMethod = lambdaAsClass.findLambdaMethod(lambdaClass);
      if (matchingMethod == null)
         throw new AnalyzerException(null, "Could not find a lambda method with the expected name in the class");
      PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(
            metamodel.getMethodChecker(isObjectEqualsSafe));
      MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(matchingMethod.getName(), Type.getMethodDescriptor(matchingMethod), pathAnalysisFactory);
      PathAnalysisSimplifier.cleanAndSimplify(analysis, metamodel.getComparisonMethods(isObjectEqualsSafe));
      return analysis;
   }

   
   private static int countObjectParameters(Method m)
   {
      int count = 0;
      if (m.getReturnType().getName().equals("java.lang.Object")) count++;
      for (Class<?> c: m.getParameterTypes())
         if (c.getName().equals("java.lang.Object")) count++;
      return count;
   }

   LambdaAnalysis(Object lambda, SerializedLambda serializedLambda, MethodAnalysisResults symbolicAnalysis, int lambdaIndex)
   {
      this.numCapturedArgs = serializedLambda.capturedArgs.length;
      this.numLambdaArgs = Type.getArgumentTypes(serializedLambda.implMethodSignature).length;
      this.lambdaIndex = lambdaIndex;
      this.symbolicAnalysis = symbolicAnalysis;
      this.indirectCapturedArgs = null;
      this.usesParametersAsFields = false;
   }

   LambdaAnalysis(MethodAnalysisResults symbolicAnalysis, List<TypedValue> indirectCapturedArgs, int numLambdaArgs)
   {
      this.numCapturedArgs = indirectCapturedArgs.size();
      this.numLambdaArgs = numLambdaArgs;
      this.lambdaIndex = -1;
      this.symbolicAnalysis = symbolicAnalysis;
      this.indirectCapturedArgs = indirectCapturedArgs;
      this.usesParametersAsFields = false;
   }

   LambdaAnalysis(Object lambda, MethodAnalysisResults symbolicAnalysis, int numLambdaArgs, int lambdaIndex)
   {
      this.numCapturedArgs = 0;
      this.numLambdaArgs = numLambdaArgs;
      this.lambdaIndex = lambdaIndex;
      this.symbolicAnalysis = symbolicAnalysis;
      this.indirectCapturedArgs = null;
      this.usesParametersAsFields = true;
   }

   public boolean usesIndirectArgs()
   {
      return indirectCapturedArgs != null;
   }


   public int getNumCapturedArgs()
   {
      return numCapturedArgs;
   }

   public int getNumLambdaArgs()
   {
      return numLambdaArgs;
   }

   public TypedValue getIndirectCapturedArg(int argIndex)
   {
      return indirectCapturedArgs.get(argIndex);
   }
   
   public int getLambdaIndex()
   {
      return lambdaIndex;
   }

   public boolean usesParametersAsFields()
   {
      return usesParametersAsFields;
   }
}
