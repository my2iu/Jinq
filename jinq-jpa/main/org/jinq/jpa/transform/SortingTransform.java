package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SortingTransform extends JPQLOneLambdaQueryTransform
{
   public SortingTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isAscending)
   {
      super(metamodel, alternateClassLoader);
      this.isAscending = isAscending;
   }

   private boolean isAscending;
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query instanceof SelectFromWhere && query.canSort())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, 
                  SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, metamodel, parentArgumentScope, false));

            ColumnExpressions<U> returnExpr = makeSelectExpression(translator, lambda);

            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
            SelectFromWhere.SortingParameters sort = new SelectFromWhere.SortingParameters();
            sort.expr = returnExpr.getOnlyColumn();
            sort.isAscending = isAscending;
            toReturn.sort.add(0, sort);
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }

}
