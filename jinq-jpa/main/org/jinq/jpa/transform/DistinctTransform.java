package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;

public class DistinctTransform extends JPQLNoLambdaQueryTransform
{
   public DistinctTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query) throws QueryTransformException
   {
      return apply(query, null);
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      if (query.canDistinct())
      {
         SelectOnly<V> select = (SelectOnly<V>)query;
         
         // Create the new query, merging in the analysis of the method
         SelectOnly<U> toReturn = (SelectOnly<U>)select.shallowCopy();
         toReturn.isDistinct = true;
         
         return toReturn;
      }
      throw new QueryTransformException("Existing query cannot be transformed further");
   }
}
