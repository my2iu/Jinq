package org.jinq.jpa.transform;

import java.io.IOException;
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
   MethodAnalysisResults symbolicAnalysis;

   public static LambdaAnalysis fullyAnalyzeLambda(LambdaInfo lambdaInfo, MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean throwExceptionOnFailure)
   {
      // TODO: The part below will need to be moved to a separate method.
      //   That way, we can used the serialized lambda info to check if
      //   we've cached the results of this analysis already without needing
      //   to redo all this analysis.
      SerializedLambda s = lambdaInfo.serializedLambda;
      try {
         MethodAnalysisResults analysis = analyzeLambda(metamodel, alternateClassLoader, s);
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
   public static LambdaAnalysis analyzeMethod(MetamodelUtil metamodel, ClassLoader alternateClassLoader, Handle lambdaHandle, List<TypedValue> indirectCapturedArgs, boolean throwExceptionOnFailure)
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

   LambdaAnalysis(Object lambda, SerializedLambda serializedLambda, MethodAnalysisResults symbolicAnalysis, int lambdaIndex)
   {
      this.numCapturedArgs = serializedLambda.capturedArgs.length;
      this.numLambdaArgs = Type.getArgumentTypes(serializedLambda.implMethodSignature).length;
      this.lambdaIndex = lambdaIndex;
      this.symbolicAnalysis = symbolicAnalysis;
      this.indirectCapturedArgs = null;
   }

   LambdaAnalysis(MethodAnalysisResults symbolicAnalysis, List<TypedValue> indirectCapturedArgs, int numLambdaArgs)
   {
      this.numCapturedArgs = indirectCapturedArgs.size();
      this.numLambdaArgs = numLambdaArgs;
      this.lambdaIndex = -1;
      this.symbolicAnalysis = symbolicAnalysis;
      this.indirectCapturedArgs = indirectCapturedArgs;
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
}
