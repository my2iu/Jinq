package org.jinq.jooq.transform;

import java.util.Collections;
import java.util.List;

import org.jinq.jooq.querygen.ColumnExpressions;
import org.jooq.Condition;
import org.jooq.QueryPart;
import org.jooq.Table;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysis;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class WhereTransform 
{
   MetamodelUtil metamodel;
   LambdaInfo where;
   public WhereTransform(MetamodelUtil metamodel, LambdaInfo where)
   {
      this.metamodel = metamodel;
      this.where = where;
   }
   
   public Condition apply(List<Table<?>> fromList)
   {
      try  {
//         if (query instanceof SelectFromWhere)
//         {
//            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            SymbExToColumns translator = new SymbExToColumns(metamodel, 
                  new SelectFromWhereLambdaArgumentHandler(where, fromList));
            Condition methodExpr = null;
            for (int n = 0; n < where.symbolicAnalysis.paths.size(); n++)
            {
               PathAnalysis path = where.symbolicAnalysis.paths.get(n);

               TypedValue returnVal = PathAnalysisSimplifier.simplifyBoolean(path.getReturnValue(), Collections.emptyMap(), Collections.emptyMap(), false);
               ColumnExpressions<?> returnColumns = translator.transform(returnVal);
               if (!returnColumns.isSingleColumn()) throw new IllegalArgumentException("Where lambda should only return a single column of data");
               QueryPart returnExpr = returnColumns.getOnlyColumn();

               if (returnVal instanceof ConstantValue.BooleanConstant)
               {
                  if (((ConstantValue.BooleanConstant)returnVal).val)
                  {
                     // This path returns true, so it's redundant to actually
                     // put true into the final code.
                     returnExpr = null;
                  }
                  else
                  {
                     // This path returns false, so we can ignore it
                     continue;
                  }
               }
               
               // Handle where path conditions
               Condition conditionExpr = null;
               for (TypedValue cmp: path.getConditions())
               {
                  ColumnExpressions<?> col = translator.transform(cmp);
                  if (!col.isSingleColumn()) throw new IllegalArgumentException("Expecting a single column");
                  Condition expr = (Condition)col.getOnlyColumn();
                  if (conditionExpr != null)
                     conditionExpr = conditionExpr.and(expr);
                  else
                     conditionExpr = expr;
               }
               
               // Merge path conditions and return value to create a value for the path
               Condition pathExpr = (Condition)returnExpr;
               if (conditionExpr != null)
               {
                  if (pathExpr == null)
                     pathExpr = conditionExpr;
                  else
                     pathExpr = pathExpr.and(conditionExpr);
               }
               
               // Merge into new expression summarizing the method
               if (methodExpr != null)
                  methodExpr = methodExpr.or(pathExpr);
               else
                  methodExpr = pathExpr;
            }
            
            return methodExpr;
//            // Create the new query, merging in the analysis of the method
//            SelectFromWhere<U> toReturn = new SelectFromWhere<U>();
//            toReturn.froms.addAll(sfw.froms);
//            toReturn.cols = (ColumnExpressions<U>) sfw.cols;
//            if (sfw.where == null)
//               toReturn.where = methodExpr;
//            else
//               toReturn.where = new BinaryExpression("AND", sfw.where, methodExpr);
//            return toReturn;
//         }
      } catch (TypedValueVisitorException e)
      {
         e.printStackTrace();
         throw new IllegalArgumentException("Could not create query from lambda", e);
      }
   }

}
