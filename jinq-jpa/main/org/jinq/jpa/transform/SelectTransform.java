package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SelectTransform extends JPQLQueryTransform
{
   LambdaInfo lambda;
   public SelectTransform(MetamodelUtil metamodel, LambdaInfo lambda)
   {
      super(metamodel);
      this.lambda = lambda;
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query)
   {
      try  {
         if (query instanceof SelectFromWhere)
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            // TODO: froms.get(0) is temporary 
            SymbExToColumns translator = new SymbExToColumns(metamodel, sfw.froms.get(0));

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) return null;
            
            ColumnExpressions<U> returnExpr = (ColumnExpressions<U>)translator.transform(lambda.symbolicAnalysis.paths.get(0).getSimplifiedReturnValue());

            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = new SelectFromWhere<U>();
            toReturn.froms.addAll(sfw.froms);
            // TODO: translator.transform() should return multiple columns, not just one thing
            toReturn.cols = returnExpr;
            toReturn.where = sfw.where;
            return toReturn;
         }
         return null;
      } catch (TypedValueVisitorException e)
      {
         e.printStackTrace();
         return null;
      }
   }

}
