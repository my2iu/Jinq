package ch.epfl.labos.iu.orm.queryll2;

import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;

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
         QueryllPathAnalysisSupplementalFactory pathAnalysisFactory = new QueryllPathAnalysisSupplementalFactory(entityInfo, new ArrayList<>()); 
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
}
