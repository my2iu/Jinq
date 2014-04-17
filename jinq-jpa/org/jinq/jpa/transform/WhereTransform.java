package org.jinq.jpa.transform;

import org.jinq.jpq.jpqlquery.JPQLQuery;

public class WhereTransform extends JPQLQueryTransform
{
   LambdaInfo where;
   public WhereTransform(LambdaInfo where)
   {
      this.where = where;
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query)
   {
      return null;
   }

}
