package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.JPQLQuery;

public class CountTransform extends JPQLNoLambdaQueryTransform
{
   AggregateTransform transform;
   public CountTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
      transform = new AggregateTransform(metamodel, alternateClassLoader, AggregateTransform.AggregateType.COUNT);
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      return transform.apply(query, null, parentArgumentScope);
   }

}
