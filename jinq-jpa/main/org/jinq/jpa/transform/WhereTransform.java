package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ComparisonValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class WhereTransform extends JPQLQueryTransform
{
   LambdaInfo where;
   public WhereTransform(MetamodelUtil metamodel, LambdaInfo where)
   {
      super(metamodel);
      this.where = where;
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query)
   {
      try  {
         if (query instanceof SelectFromWhere)
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            // TODO: froms.get(0) is temporary 
            SymbExToExpression translator = new SymbExToExpression(metamodel, sfw.froms.get(0));
            Expression methodExpr = null;
            for (int n = 0; n < where.symbolicAnalysis.paths.size(); n++)
            {
               PathAnalysis path = where.symbolicAnalysis.paths.get(n);
               
               Expression returnExpr = translator.transform(path.getSimplifiedReturnValue());
               
               // Handle where path conditions
               Expression conditionExpr = null;
               for (TypedValue cmp: path.getSimplifiedConditions())
               {
                  Expression expr = translator.transform(cmp);
                  if (conditionExpr != null)
                     conditionExpr = new BinaryExpression("AND", conditionExpr, expr);
                  else
                     conditionExpr = expr;
               }
               
               // Merge path conditions and return value to create a value for the path
               Expression pathExpr = returnExpr;
               if (conditionExpr != null)
                  pathExpr = new BinaryExpression("AND", pathExpr, conditionExpr);
               
               // Merge into new expression summarizing the method
               if (methodExpr != null)
                  methodExpr = new BinaryExpression("OR", methodExpr, pathExpr);
               else
                  methodExpr = pathExpr;
            }
            
            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = new SelectFromWhere<U>();
            toReturn.froms.addAll(sfw.froms);
            toReturn.cols.addAll(sfw.cols);
            if (sfw.where == null)
               toReturn.where = methodExpr;
            else
               toReturn.where = new BinaryExpression("AND", sfw.where, methodExpr);
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
