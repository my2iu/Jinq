package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;

public abstract class JPQLMultiLambdaQueryTransform extends JPQLQueryTransform
{

   JPQLMultiLambdaQueryTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaAnalysis [] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      return null;
   }
}
