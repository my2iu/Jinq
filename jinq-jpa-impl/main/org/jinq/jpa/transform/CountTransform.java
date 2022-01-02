package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;

public class CountTransform extends JPQLNoLambdaQueryTransform
{
   AggregateTransform transform;
   public CountTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
      transform = new AggregateTransform(config, AggregateTransform.AggregateType.COUNT);
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      return transform.apply(query, null, parentArgumentScope);
   }

   @Override 
   public String getTransformationTypeCachingTag()
   {
      return CountTransform.class.getName();
   }
}
