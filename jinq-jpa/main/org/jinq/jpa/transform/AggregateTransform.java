package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.AggregateFunctionExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;
import org.jinq.jpa.jpqlquery.SimpleRowReader;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class AggregateTransform extends JPQLOneLambdaQueryTransform
{
   // TODO: Should I include count() here too?
   public enum AggregateType
   {
      SUM, AVG, MAX, MIN,
      COUNT, // COUNT is only usable for multiaggregate and grouping subqueries
   }
   
   public AggregateTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader, AggregateType type)
   {
      super(metamodel, alternateClassLoader);
      aggregateFunction = type.name();
   }
   
   private String aggregateFunction;
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            SymbExToColumns translator = new SymbExToColumns(metamodel, 
                  SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, metamodel, false));

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in an aggregate function at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            ColumnExpressions<U> returnExpr = (ColumnExpressions<U>)PathAnalysisSimplifier
                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), metamodel.comparisonMethods)
                  .visit(translator, passdown);

            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy(); 
            toReturn.isAggregated = true;
            toReturn.cols = ColumnExpressions.singleColumn(
                  new SimpleRowReader<>(), 
                  new AggregateFunctionExpression(returnExpr.getOnlyColumn(), aggregateFunction)); 
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }
   
   public static <U, V> JPQLQuery<U> applyToSubquery(MetamodelUtil metamodel, AggregateType type, JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      try  {
         if (query instanceof SelectOnly)
         {
            SelectOnly<V> select = (SelectOnly<V>)query;
            // TODO: Handle parameters on the SelectOnly
            SymbExToColumns translator = new SymbExToColumns(metamodel, 
                  SelectFromWhereLambdaArgumentHandler.fromSelectOnly(select, lambda, metamodel, false));

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in an aggregate function at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            ColumnExpressions<U> returnExpr = (ColumnExpressions<U>)PathAnalysisSimplifier
                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), metamodel.comparisonMethods)
                  .visit(translator, passdown);

//            // Create the new query, merging in the analysis of the method
//            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy(); 
//            toReturn.isAggregated = true;
//            toReturn.cols = ColumnExpressions.singleColumn(
//                  new SimpleRowReader<>(), 
//                  new AggregateFunctionExpression(returnExpr.getOnlyColumn(), aggregateFunction)); 
//            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }
}
