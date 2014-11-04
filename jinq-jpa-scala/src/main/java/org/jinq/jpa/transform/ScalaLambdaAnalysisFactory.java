package org.jinq.jpa.transform;

import scala.Function1;
import scala.Function2;

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
      else if (lambda instanceof Function2)
      {
         return new ScalaLambdaInfo(lambda, lambdaIndex, 0, 2);
      }
      else
         return super.extractSurfaceInfo(lambda, lambdaIndex,
               throwExceptionOnFailure);
   }
}
