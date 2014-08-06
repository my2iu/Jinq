package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.JPQLQuery;

public class JPQLOneLambdaQueryTransform extends JPQLQueryTransform
{

   JPQLOneLambdaQueryTransform(MetamodelUtil metamodel)
   {
      super(metamodel);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      return null;
   }
}
