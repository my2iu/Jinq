package org.jinq.jpa.transform;

import scala.Function1;

public class ScalaLambdaAnalysisFactory extends LambdaAnalysisFactory
{
   @Override
   public LambdaInfo extractSurfaceInfo(Object lambda, int lambdaIndex,
         boolean throwExceptionOnFailure)
   {
      if (lambda instanceof Function1)
      {
         return new ScalaLambdaInfo(lambda, lambdaIndex, 0, 1);
      }
      else
         return super.extractSurfaceInfo(lambda, lambdaIndex,
               throwExceptionOnFailure);
   }
}
