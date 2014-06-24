package org.jinq.jpa.transform;

import java.io.IOException;

import org.jinq.jpa.MetamodelUtil;
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
   
   public static LambdaInfo analyze(MetamodelUtil metamodel, ClassLoader alternateClassLoader, Object lambda, int lambdaIndex)
   {
      SerializedLambda s = SerializedLambda.extractLambda(lambda);
      if (s == null) return null;
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      MethodAnalysisResults analysis = analyzeLambda(metamodel, alternateClassLoader, s);
      if (analysis == null) return null;
      return new LambdaInfo(lambda, s, analysis, lambdaIndex);
   }
   
   private static MethodAnalysisResults analyzeLambda(MetamodelUtil metamodel, ClassLoader alternateClassLoader, SerializedLambda lambda) 
   {
      if (lambda == null) return null;
      return analyzeLambda(metamodel, alternateClassLoader, lambda.implClass, lambda.implMethodName, lambda.implMethodSignature);
   }
   
   private static MethodAnalysisResults analyzeLambda(MetamodelUtil metamodel, ClassLoader alternateClassLoader, String className, String methodName, String methodSignature) 
   {
      try {
         // Open up the corresponding class to analyze
         PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(
               () -> new MethodChecker(
                           metamodel.safeMethodAnnotations, 
                           metamodel.safeMethods, metamodel.safeStaticMethods));
         TransformationClassAnalyzer classAnalyzer = 
               new TransformationClassAnalyzer(className, alternateClassLoader);
         MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(methodName, methodSignature, pathAnalysisFactory);
         PathAnalysisSimplifier.cleanAndSimplify(analysis);
         return analysis;
      } catch (IOException e)
      {
         e.printStackTrace();
         return null;
      }
      catch (AnalyzerException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   LambdaInfo(Object lambda, SerializedLambda serializedLambda, MethodAnalysisResults symbolicAnalysis, int lambdaIndex)
   {
      this.Lambda = lambda;
      this.serializedLambda = serializedLambda;
      this.symbolicAnalysis = symbolicAnalysis;
      this.lambdaIndex = lambdaIndex;
   }
   
   public Object getCapturedArg(int argIndex)
   {
      return serializedLambda.capturedArgs[argIndex];
   }
}
