package org.jinq.jpa.transform;

import java.io.IOException;

import org.jinq.jpa.MetamodelUtil;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.path.MethodAnalysisResults;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisFactory;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;

import com.user00.thunk.SerializedLambda;

/**
 * Holds a lambda as well as any additional information about it
 * needed to generate a query from it.
 */
public class LambdaInfo
{
   Object Lambda;
   SerializedLambda serializedLambda;
   MethodAnalysisResults symbolicAnalysis;
   
   /**
    * JPAQueryComposer stores the lambdas that are chained together to create a query in a list.
    * The lambdaIndex refers to the index of this lambda in the list of lambdas used to create
    * the resulting query.
    */
   int lambdaIndex;
   
   public static LambdaInfo analyze(MetamodelUtil metamodel, ClassLoader alternateClassLoader, Object lambda, int lambdaIndex, boolean throwExceptionOnFailure)
   {
      SerializedLambda s = SerializedLambda.extractLambda(lambda);
      if (s == null) 
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not extract code from lambda");
         return null;
      }
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      try {
         MethodAnalysisResults analysis = analyzeLambda(metamodel, alternateClassLoader, s);
         if (analysis == null) 
         {
            if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code");
            return null;
         }
         return new LambdaInfo(lambda, s, analysis, lambdaIndex);
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
   public static LambdaInfo analyzeMethod(MetamodelUtil metamodel, ClassLoader alternateClassLoader, Handle lambdaHandle, boolean throwExceptionOnFailure)
   {
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      try {
         MethodAnalysisResults analysis = analyzeLambda(metamodel, alternateClassLoader, lambdaHandle.getOwner(), lambdaHandle.getName(), lambdaHandle.getDesc());
         if (analysis == null) 
         {
            if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code");
            return null;
         }
         return new LambdaInfo(null, null, analysis, -1);
      } 
      catch (Exception e)
      {
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not analyze lambda code", e);
         return null;
      }
   }

   private static MethodAnalysisResults analyzeLambda(MetamodelUtil metamodel, ClassLoader alternateClassLoader, SerializedLambda lambda) throws IOException, AnalyzerException 
   {
      if (lambda == null) return null;
      return analyzeLambda(metamodel, alternateClassLoader, lambda.implClass, lambda.implMethodName, lambda.implMethodSignature);
   }
   
   private static MethodAnalysisResults analyzeLambda(MetamodelUtil metamodel, ClassLoader alternateClassLoader, String className, String methodName, String methodSignature) throws IOException, AnalyzerException 
   {
      // Open up the corresponding class to analyze
      PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(
            () -> new MethodChecker(
                        metamodel.safeMethodAnnotations, 
                        metamodel.safeMethods, metamodel.safeStaticMethods));
      TransformationClassAnalyzer classAnalyzer = 
            new TransformationClassAnalyzer(className, alternateClassLoader);
      MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(methodName, methodSignature, pathAnalysisFactory);
      PathAnalysisSimplifier.cleanAndSimplify(analysis, metamodel.comparisonMethods);
      return analysis;
   }

   LambdaInfo(Object lambda, SerializedLambda serializedLambda, MethodAnalysisResults symbolicAnalysis, int lambdaIndex)
   {
      this.Lambda = lambda;
      this.serializedLambda = serializedLambda;
      this.symbolicAnalysis = symbolicAnalysis;
      this.lambdaIndex = lambdaIndex;
   }
   
   public int getNumCapturedArgs()
   {
      // TODO: Handle sublambdas with parameters correctly
      if (serializedLambda == null)
         return 0;
      return serializedLambda.capturedArgs.length;
   }
   
   public int getNumLambdaArgs()
   {
      // TODO: Handle sublambdas with parameters correctly
      if (serializedLambda == null)
         return 1;
      return Type.getArgumentTypes(serializedLambda.implMethodSignature).length;
   }
   
   public Object getCapturedArg(int argIndex)
   {
      return serializedLambda.capturedArgs[argIndex];
   }
}
