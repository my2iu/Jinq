package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.AggregateFunctionExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SimpleRowReader;

public class CountTransform extends JPQLQueryTransform
{
   public CountTransform(MetamodelUtil metamodel)
   {
      super(metamodel);
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      if (lambda != null) throw new IllegalArgumentException("lambda should be null");
      
      if (query.isSelectFromWhere())
      {
         SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
         
         // Create the new query, merging in the analysis of the method
         SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
         toReturn.isAggregated = true;
         // TODO: It looks like you can stick anything inside the COUNT(),
         //    but I'm not sure. Why does it even take a parameter there?
         // TODO: The difference might be in NULL handling. If a field is
         //    given, then NULLs are ignored and not counted; otherwise, they are
         toReturn.cols = ColumnExpressions.singleColumn(
               new SimpleRowReader<>(),
               new AggregateFunctionExpression(new ConstantExpression("1"), "COUNT")); 
         return toReturn;
      }
      throw new QueryTransformException("Existing query cannot be transformed further");
   }

}
