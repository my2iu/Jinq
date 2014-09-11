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
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            Expression aggregatedExpr;
            if (type != AggregateType.COUNT)
            {
               SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, 
                     SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, metamodel, null, false));
               aggregatedExpr = makeSelectExpression(translator, lambda).getOnlyColumn();
            }
            else
            {
               if (sfw.cols.isSingleColumn())
                  aggregatedExpr = sfw.cols.getOnlyColumn();
               else
                  aggregatedExpr = new ConstantExpression("1");
            }
            
            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy(); 
            toReturn.isAggregated = true;
            toReturn.cols = ColumnExpressions.singleColumn(
                  new SimpleRowReader<>(), 
                  new AggregateFunctionExpression(aggregatedExpr, type.name())); 
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }

   public <U, V> JPQLQuery<U> applyAggregationToSubquery(JPQLQuery<V> query, LambdaInfo lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query instanceof SelectOnly)
         {
            SelectOnly<V> select = (SelectOnly<V>)query;
            Expression aggregatedExpr = null;
            if (type != AggregateType.COUNT)
            {
               // TODO: Handle parameters on the SelectOnly
               SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, 
                     SelectFromWhereLambdaArgumentHandler.fromSelectOnly(select, lambda, metamodel, parentArgumentScope, false));

               ColumnExpressions<U> returnExpr = makeSelectExpression(translator, lambda);
               aggregatedExpr = returnExpr.getOnlyColumn(); 
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
                  new AggregateFunctionExpression(aggregatedExpr, type.name())); 
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }
}
