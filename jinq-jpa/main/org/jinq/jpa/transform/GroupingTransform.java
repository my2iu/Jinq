package org.jinq.jpa.transform;

import java.util.Arrays;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.GroupedSelectFromWhere;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;
import org.jinq.jpa.jpqlquery.TupleRowReader;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class GroupingTransform extends JPQLMultiLambdaQueryTransform
{
   public GroupingTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
   }

   
   private <U, V, W> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo groupingLambda, LambdaInfo[] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;

            // Figure out the columns needed for the key value
            SelectTransform keyTransform = new SelectTransform(metamodel, alternateClassLoader, false);
            JPQLQuery<W> keyQuery = keyTransform.apply(query, groupingLambda, parentArgumentScope);
            if (!keyQuery.isSelectFromWhere())
               throw new QueryTransformException("Expecting the result of the key calculation to be a SelectFromWhere query"); 
            SelectOnly<W> keySelect = new SelectOnly<>();
            keySelect.cols = ((SelectFromWhere<W>)keyQuery).cols; 
            
            // Handle the aggregates part of the group
            SelectOnly<V> streamTee = new SelectOnly<>();
            streamTee.cols = sfw.cols;
            ColumnExpressions<?> [] aggregatedQueryEntries = new ColumnExpressions<?>[lambdas.length];

            for (int n = 0; n < lambdas.length; n++)
            {
               LambdaInfo lambda = lambdas[n];

               SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader,  
                     new GroupingLambdasArgumentHandler(keySelect, streamTee, lambdas[n], metamodel, parentArgumentScope, false));

               ColumnExpressions<U> returnQuery = makeSelectExpression(translator, lambda);

               // TODO: Confirm that the result actually contains an aggregate
               aggregatedQueryEntries[n] = returnQuery;
            }

            // Create the new query, merging in the analysis of the method
            GroupedSelectFromWhere<U, W> toReturn = (GroupedSelectFromWhere<U, W>)sfw.shallowCopyWithGrouping(); 
            toReturn.isAggregated = true;
            RowReader<?> [] readers = new RowReader<?>[aggregatedQueryEntries.length + 1];
            for (int n = 0; n < aggregatedQueryEntries.length; n++)
               readers[n + 1] = aggregatedQueryEntries[n].reader;
            readers[0] = keySelect.getRowReader();
            ColumnExpressions<U> cols = new ColumnExpressions<>(TupleRowReader.createReaderForTuple(readers));
            cols.columns.addAll(keySelect.cols.columns);
            for (int n = 0; n < aggregatedQueryEntries.length; n++)
               cols.columns.addAll(aggregatedQueryEntries[n].columns);
            toReturn.groupingCols = keySelect.cols;
            toReturn.cols = cols;
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }

   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo[] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      return apply(query, lambdas[0], Arrays.copyOfRange(lambdas, 1, lambdas.length), parentArgumentScope);
   }

}
