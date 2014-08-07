package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.AggregateFunctionExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SimpleRowReader;
import org.jinq.jpa.jpqlquery.TupleRowReader;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class MultiAggregateTransform extends JPQLMultiLambdaQueryTransform
{
   public MultiAggregateTransform(MetamodelUtil metamodel)
   {
      super(metamodel);
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo[] lambdas) throws QueryTransformException
   {
//      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;

            for (int n = 0; n < lambdas.length; n++)
            {
               SymbExToAggregationSubQuery translator = new SymbExToAggregationSubQuery(metamodel, 
                     new AggregateStreamLambdaArgumentHandler(lambdas[n], metamodel, false));
               
            }

//                        SymbExToColumns translator = new SymbExToColumns(metamodel, 
//                  new SelectFromWhereLambdaArgumentHandler(sfw, lambda, metamodel, false));
//
//            // TODO: Handle this case by translating things to use SELECT CASE 
//            if (lambda.symbolicAnalysis.paths.size() > 1) 
//               throw new QueryTransformException("Can only handle a single path in an aggregate function at the moment");
//            
//            SymbExPassDown passdown = SymbExPassDown.with(null, false);
//            ColumnExpressions<U> returnExpr = (ColumnExpressions<U>)PathAnalysisSimplifier
//                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), metamodel.comparisonMethods)
//                  .visit(translator, passdown);
//
//            // Create the new query, merging in the analysis of the method
//            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy(); 
//            toReturn.isAggregated = true;
//            toReturn.cols = ColumnExpressions.singleColumn(
//                  new SimpleRowReader<>(), 
//                  new AggregateFunctionExpression(returnExpr.getOnlyColumn(), aggregateFunction)); 
//            return toReturn;
            
            
            
            
//
//            // TODO: Handle this case by translating things to use SELECT CASE 
//            if (lambda.symbolicAnalysis.paths.size() > 1) 
//               throw new QueryTransformException("Can only handle a single path in a JOIN at the moment");
//            
//            SymbExPassDown passdown = SymbExPassDown.with(null, false);
//            JPQLQuery<U> returnExpr = (JPQLQuery<U>)PathAnalysisSimplifier
//                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), metamodel.comparisonMethods)
//                  .visit(translator, passdown);
//
//            // Create the new query, merging in the analysis of the method
//            
//            // Check if the subquery is simply a stream of all of a certain entity
//            if (isSimpleFrom(returnExpr))
//            {
//               SelectFromWhere<?> toMerge = (SelectFromWhere<?>)returnExpr;
//               SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
//               toReturn.froms.add(toMerge.froms.get(0));
//               toReturn.cols = new ColumnExpressions<>(TupleRowReader.createReaderForTuple(TupleRowReader.PAIR_CLASS, sfw.cols.reader, toMerge.cols.reader));
//               toReturn.cols.columns.addAll(sfw.cols.columns);
//               toReturn.cols.columns.addAll(toMerge.cols.columns);
//               return toReturn;
//            }
//            
//            // Handle other types of subqueries

         }
         throw new QueryTransformException("Existing query cannot be transformed further");
//      } catch (TypedValueVisitorException e)
//      {
//         throw new QueryTransformException(e);
//      }
   }

}
