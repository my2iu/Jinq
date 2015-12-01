package org.jinq.jooq.transform;

import java.io.IOException;
import java.util.Collections;

import org.jinq.rebased.org.objectweb.asm.tree.analysis.AnalyzerException;

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
   
   public static LambdaInfo analyze(MetamodelUtil metamodel, Object lambda)
   {
      SerializedLambda s;
      try {
         s = SerializedLambda.extractLambda(lambda);
      }
      catch (Exception e)
      {
         return null;
      }
      if (s == null) return null;
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      MethodAnalysisResults analysis = analyzeLambda(metamodel, s);
      if (analysis == null) return null;
      return new LambdaInfo(lambda, s, analysis);
   }
   
   private static MethodAnalysisResults analyzeLambda(MetamodelUtil metamodel, SerializedLambda lambda) 
   {
      if (lambda == null) return null;
      return analyzeLambda(metamodel, lambda.implClass, lambda.implMethodName, lambda.implMethodSignature);
   }
   
   private static MethodAnalysisResults analyzeLambda(MetamodelUtil metamodel, String className, String methodName, String methodSignature) 
   {
      try {
         // Open up the corresponding class to analyze
         PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(
               new MethodChecker(metamodel)); 
         TransformationClassAnalyzer classAnalyzer = 
               new TransformationClassAnalyzer(className);
         MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(methodName, methodSignature, pathAnalysisFactory);
         PathAnalysisSimplifier.cleanAndSimplify(analysis, Collections.emptyMap(), Collections.emptyMap(), false);
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

   LambdaInfo(Object lambda, SerializedLambda serializedLambda, MethodAnalysisResults symbolicAnalysis)
   {
      this.Lambda = lambda;
      this.serializedLambda = serializedLambda;
      this.symbolicAnalysis = symbolicAnalysis;
   }
   
   public Object getCapturedArg(int argIndex)
   {
      return serializedLambda.capturedArgs[argIndex];
   }
}
