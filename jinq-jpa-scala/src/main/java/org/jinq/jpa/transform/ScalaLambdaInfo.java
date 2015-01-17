package org.jinq.jpa.transform;

public class ScalaLambdaInfo extends LambdaInfo
{
   String className;

   protected ScalaLambdaInfo(Object lambdaObject, 
         int lambdaIndex, int numCapturedArgs,
         int numLambdaArgs)
   {
      super(lambdaIndex, numCapturedArgs, numLambdaArgs);
      this.Lambda = lambdaObject;
      className = lambdaObject.getClass().getName();
   }

   @Override
   public String getLambdaSourceString()
   {
      return className;
   }

   @Override
   public LambdaAnalysis fullyAnalyze(MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe, boolean isCollectionContainsSafe, boolean throwExceptionOnFailure)
   {
      return LambdaAnalysis.fullyAnalyzeClassAsLambda(this, new LambdaAnalysis.LambdaAsClassAnalysisConfig(), numLambdaArgs, metamodel, alternateClassLoader, isObjectEqualsSafe, isCollectionContainsSafe, throwExceptionOnFailure);
   }

}
