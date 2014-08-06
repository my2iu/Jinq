package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.JPQLQuery;

public class JPQLNoLambdaQueryTransform extends JPQLQueryTransform
{

   JPQLNoLambdaQueryTransform(MetamodelUtil metamodel)
   {
      super(metamodel);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query) throws QueryTransformException
   {
      return null;
   }
}
