package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;

public class JPQLMultiLambdaQueryTransform extends JPQLQueryTransform
{

   JPQLMultiLambdaQueryTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo [] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      return null;
   }
}
