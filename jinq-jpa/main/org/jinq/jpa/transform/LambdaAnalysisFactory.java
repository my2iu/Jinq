package org.jinq.jpa.transform;

public class LambdaAnalysisFactory
{
   public LambdaInfo extractSurfaceInfo(Object lambda, int lambdaIndex, boolean throwExceptionOnFailure)
   {
      return LambdaInfo.analyze(lambda, lambdaIndex, throwExceptionOnFailure);
   }
}
