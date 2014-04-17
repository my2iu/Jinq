package org.jinq.jpa.transform;

/**
 * Holds a lambda as well as any additional information about it
 * needed to generate a query from it.
 */
public class LambdaInfo
{
   Object Lambda;
   public LambdaInfo(Object lambda)
   {
      this.Lambda = lambda;
   }
}
