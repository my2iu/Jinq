package org.jinq.jooq.transform;

import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class WhereTransform 
{
   LambdaInfo where;
   public WhereTransform(MetamodelUtil metamodel, LambdaInfo where)
   {
      this.where = where;
   }
   
   public void apply()
   {
      try  {
//         if (query instanceof SelectFromWhere)
//         {
//            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
//            SymbExToColumns translator = new SymbExToColumns(metamodel, 
//                  new SelectFromWhereLambdaArgumentHandler(sfw, where));
//            Expression methodExpr = null;
            for (int n = 0; n < where.symbolicAnalysis.paths.size(); n++)
            {
               PathAnalysis path = where.symbolicAnalysis.paths.get(n);

               TypedValue returnVal = path.getSimplifiedBooleanReturnValue();
//               ColumnExpressions<?> returnColumns = translator.transform(returnVal);
//               if (!returnColumns.isSingleColumn()) return null;
//               Expression returnExpr = returnColumns.getOnlyColumn();
//
//               if (returnVal instanceof ConstantValue.BooleanConstant)
//               {
//                  if (((ConstantValue.BooleanConstant)returnVal).val)
//                  {
//                     // This path returns true, so it's redundant to actually
//                     // put true into the final code.
//                     returnExpr = null;
//                  }
//                  else
//                  {
//                     // This path returns false, so we can ignore it
//                     continue;
//                  }
//               }
//               
//               // Handle where path conditions
//               Expression conditionExpr = null;
//               for (TypedValue cmp: path.getSimplifiedBooleanConditions())
//               {
//                  ColumnExpressions<?> col = translator.transform(cmp);
//                  if (!col.isSingleColumn()) return null;
//                  Expression expr = col.getOnlyColumn();
//                  if (conditionExpr != null)
//                     conditionExpr = new BinaryExpression("AND", conditionExpr, expr);
//                  else
//                     conditionExpr = expr;
//               }
//               
//               // Merge path conditions and return value to create a value for the path
//               Expression pathExpr = returnExpr;
//               if (conditionExpr != null)
//               {
//                  if (pathExpr == null)
//                     pathExpr = conditionExpr;
//                  else
//                     pathExpr = new BinaryExpression("AND", pathExpr, conditionExpr);
//               }
//               
//               // Merge into new expression summarizing the method
//               if (methodExpr != null)
//                  methodExpr = new BinaryExpression("OR", methodExpr, pathExpr);
//               else
//                  methodExpr = pathExpr;
            }
            
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
         throw new TypedValueVisitorException("Could not create query from lambda");
      } catch (TypedValueVisitorException e)
      {
         e.printStackTrace();
         throw new IllegalArgumentException("Could not create query from lambda");
      }
   }

}
