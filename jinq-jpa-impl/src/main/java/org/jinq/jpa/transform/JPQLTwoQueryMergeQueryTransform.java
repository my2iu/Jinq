package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;

public abstract class JPQLTwoQueryMergeQueryTransform extends JPQLQueryTransform
{
   JPQLTwoQueryMergeQueryTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
   }

   /**
    * 
    * @param query1 first query to merge
    * @param query2 second query to merge
    * @param lambdaOffset number of lambdas in the first query, so all lambda indices in the second query will be offset by this amount
    * @return
    * @throws QueryTransformException
    */
   public <U, V, W> JPQLQuery<W> apply(JPQLQuery<U> query1, JPQLQuery<V> query2, int lambdaOffset) throws QueryTransformException
   {
      return null;
   }

}
