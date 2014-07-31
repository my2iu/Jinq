package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.AggregateFunctionExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SimpleRowReader;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SortingTransform extends JPQLQueryTransform
{
   public SortingTransform(MetamodelUtil metamodel, boolean isAscending)
   {
      super(metamodel);
      this.isAscending = isAscending;
   }

   private boolean isAscending;
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      try  {
         if (query instanceof SelectFromWhere && query.canSort())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            SymbExToColumns translator = new SymbExToColumns(metamodel, 
                  new SelectFromWhereLambdaArgumentHandler(sfw, lambda, metamodel, false));

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in an aggregate function at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            ColumnExpressions<U> returnExpr = (ColumnExpressions<U>)PathAnalysisSimplifier
                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), metamodel.comparisonMethods)
                  .visit(translator, passdown);

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
