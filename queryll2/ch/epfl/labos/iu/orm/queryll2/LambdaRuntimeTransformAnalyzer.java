package ch.epfl.labos.iu.orm.queryll2;

import java.io.IOException;

import org.objectweb.asm.tree.analysis.AnalyzerException;

import com.user00.thunk.SerializedLambda;

public class LambdaRuntimeTransformAnalyzer
{
   ORMInformation entityInfo;
   public LambdaRuntimeTransformAnalyzer(ORMInformation entityInfo)
   {
      this.entityInfo = entityInfo;
   }
   
   public MethodAnalysisResults analyzeLambda(SerializedLambda lambda) 
   {
      if (lambda == null) return null;
      return analyzeLambda(lambda.implClass, lambda.implMethodName, lambda.implMethodSignature);
   }
   
   public MethodAnalysisResults analyzeLambda(String className, String methodName, String methodSignature) 
   {
      try {
         // Open up the corresponding class to analyze
         TransformationClassAnalyzer classAnalyzer = 
               new TransformationClassAnalyzer(className, entityInfo);
         MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(methodName, methodSignature);
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
}
