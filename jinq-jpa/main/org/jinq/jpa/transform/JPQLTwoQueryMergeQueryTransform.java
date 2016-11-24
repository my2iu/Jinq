package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;

public abstract class JPQLTwoQueryMergeQueryTransform extends JPQLQueryTransform
{
   JPQLTwoQueryMergeQueryTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<U> query1, JPQLQuery<V> query2) throws QueryTransformException
   {
      return null;
   }

}
