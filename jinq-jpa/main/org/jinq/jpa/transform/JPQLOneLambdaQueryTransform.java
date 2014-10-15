package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;

public abstract class JPQLOneLambdaQueryTransform extends JPQLQueryTransform
{

   JPQLOneLambdaQueryTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      return null;
   }
}
