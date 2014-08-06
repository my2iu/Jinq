package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.JPQLQuery;

public class JPQLMultiLambdaQueryTransform extends JPQLQueryTransform
{

   JPQLMultiLambdaQueryTransform(MetamodelUtil metamodel)
   {
      super(metamodel);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo [] lambdas) throws QueryTransformException
   {
      return null;
   }
}
