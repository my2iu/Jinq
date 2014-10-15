package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;

public abstract class JPQLNoLambdaQueryTransform extends JPQLQueryTransform
{

   JPQLNoLambdaQueryTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      return null;
   }
}
