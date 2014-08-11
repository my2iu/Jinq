package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;
import org.jinq.jpa.jpqlquery.TupleRowReader;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class MultiAggregateTransform extends JPQLMultiLambdaQueryTransform
{
   public MultiAggregateTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo[] lambdas) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;

            SelectOnly<V> streamTee = new SelectOnly<>();
            streamTee.cols = sfw.cols;
            ColumnExpressions<?> [] aggregatedQueryEntries = new ColumnExpressions<?>[lambdas.length];

            for (int n = 0; n < lambdas.length; n++)
            {
               LambdaInfo lambda = lambdas[n];

               SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader,  
                     new AggregateStreamLambdaArgumentHandler(streamTee, lambdas[n], metamodel, null, false));

               // TODO: Handle this case by translating things to use SELECT CASE 
               if (lambda.symbolicAnalysis.paths.size() > 1) 
                  throw new QueryTransformException("Can only handle a single path in an aggregate function at the moment");

               SymbExPassDown passdown = SymbExPassDown.with(null, false);
               ColumnExpressions<?> returnQuery = simplifyAndTranslateMainPathToColumns(lambda, translator, passdown);
               
               // TODO: Confirm that the result actually contains an aggregate
               aggregatedQueryEntries[n] = returnQuery;
            }

            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy(); 
            toReturn.isAggregated = true;
            RowReader<?> [] readers = new RowReader<?>[aggregatedQueryEntries.length];
            for (int n = 0; n < readers.length; n++)
               readers[n] = aggregatedQueryEntries[n].reader;
            ColumnExpressions<U> cols = new ColumnExpressions<>(TupleRowReader.createReaderForTuple(readers));
            for (int n = 0; n < readers.length; n++)
               cols.columns.addAll(aggregatedQueryEntries[n].columns);
            toReturn.cols = cols;
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }

}
