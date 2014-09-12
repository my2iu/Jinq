package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.AggregateFunctionExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;
import org.jinq.jpa.jpqlquery.SimpleRowReader;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class AggregateTransform extends JPQLOneLambdaQueryTransform
{
   public enum AggregateType
   {
      SUM, AVG, MAX, MIN,
      COUNT, // COUNT is only usable for multiaggregate and grouping subqueries
   }
   
   public AggregateTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader, AggregateType type)
   {
      super(metamodel, alternateClassLoader);
      this.type = type;
   }
   
   private AggregateType type;
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      return applyAggregationToSubquery(query, lambda, null);
   }

   public <U, V> JPQLQuery<U> applyAggregationToSubquery(JPQLQuery<V> query, LambdaInfo lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere() || query instanceof SelectOnly)
         {
            SelectOnly<V> select = (SelectOnly<V>)query;
            Expression aggregatedExpr = null;
            SymbExArgumentHandler argumentHandler;
            if (type != AggregateType.COUNT)
            {
               if (select.isDistinct)
               {
                  // Can only perform an aggregation like SUM() or AVG() on distinct streams if we don't
                  // further modify those streams (i.e. we just pass the data through directly).
                  argumentHandler = SelectFromWhereLambdaArgumentHandler.forPassthroughTest(lambda, metamodel, parentArgumentScope, false);
                  SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, argumentHandler);
                  aggregatedExpr = makeSelectExpression(translator, lambda).getOnlyColumn();
                  if (aggregatedExpr != SelectFromWhereLambdaArgumentHandler.passthroughColsForTesting.getOnlyColumn())
                     throw new TypedValueVisitorException("Applying an aggregation to a distinct stream, but modifying the stream after the distinct but before the aggregation");
               }
               if (select.isSelectFromWhere())
               {
                  SelectFromWhere<V> sfw = (SelectFromWhere<V>)select;
                  argumentHandler = SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, metamodel, parentArgumentScope, false);
               }
               else // if (query instanceof SelectOnly)
               {
                  argumentHandler = SelectFromWhereLambdaArgumentHandler.fromSelectOnly(select, lambda, metamodel, parentArgumentScope, false);
               }
               SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, argumentHandler);
               aggregatedExpr = makeSelectExpression(translator, lambda).getOnlyColumn();
            }
            else
            {
               if (select.cols.isSingleColumn())
                  aggregatedExpr = select.cols.getOnlyColumn();
               else
                  aggregatedExpr = new ConstantExpression("1");
            }
            // Create the new query, merging in the analysis of the method
            SelectOnly<U> toReturn = (SelectOnly<U>)select.shallowCopy(); 
            toReturn.isAggregated = true;
            toReturn.cols = ColumnExpressions.singleColumn(
                  new SimpleRowReader<>(), 
                  new AggregateFunctionExpression(aggregatedExpr, type.name(), select.isDistinct)); 
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }
}
