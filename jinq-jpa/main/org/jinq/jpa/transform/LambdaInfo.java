package org.jinq.jpa.transform;

import java.io.IOException;

import org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;

import com.user00.thunk.SerializedLambda;

/**
 * Holds a lambda as well as any additional information about it
 * needed to generate a query from it.
 */
public class LambdaInfo
{
   Object Lambda;
   public static LambdaInfo analyze(Object lambda)
   {
      SerializedLambda s = SerializedLambda.extractLambda(lambda);
      if (s == null) return null;
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      MethodAnalysisResults analysis = analyzeLambda(s);
      if (analysis == null) return null;
      return new LambdaInfo(lambda);
   }
   
   private static MethodAnalysisResults analyzeLambda(SerializedLambda lambda) 
   {
      if (lambda == null) return null;
      return analyzeLambda(lambda.implClass, lambda.implMethodName, lambda.implMethodSignature);
   }
   
   private static MethodAnalysisResults analyzeLambda(String className, String methodName, String methodSignature) 
   {
      try {
         // Open up the corresponding class to analyze
         PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(); 
         TransformationClassAnalyzer classAnalyzer = 
               new TransformationClassAnalyzer(className);
         MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(methodName, methodSignature, pathAnalysisFactory);
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

   
   LambdaInfo(Object lambda)
   {
      this.Lambda = lambda;
   }
}
