package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;

public abstract class JPQLNoLambdaQueryTransform extends JPQLQueryTransform
{

   JPQLNoLambdaQueryTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      return null;
   }
}
