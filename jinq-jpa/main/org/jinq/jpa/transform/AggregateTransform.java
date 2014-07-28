package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.AggregateFunctionExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SimpleRowReader;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class AggregateTransform extends JPQLQueryTransform
{
   // TODO: Should I include count() here too?
   public enum AggregateType
   {
      SUM, AVG, MAX, MIN
   }
   
   public AggregateTransform(MetamodelUtil metamodel, AggregateType type)
   {
      super(metamodel);
      aggregateFunction = type.name();
   }
   
   private String aggregateFunction;
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      try  {
         if (query instanceof SelectFromWhere)
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
            SelectFromWhere<U> toReturn = new SelectFromWhere<U>();
            toReturn.froms.addAll(sfw.froms);
            toReturn.cols = returnExpr;
            toReturn.cols = ColumnExpressions.singleColumn(
                  new SimpleRowReader<>(), 
                  new AggregateFunctionExpression(returnExpr.getOnlyColumn(), aggregateFunction)); 
            toReturn.where = sfw.where;
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }

}
