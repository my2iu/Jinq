package org.jinq.jpa.transform;

import java.util.ArrayList;
import java.util.List;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.CaseWhenExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SelectTransform extends JPQLOneLambdaQueryTransform
{
   public SelectTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
   }
   
   <U> ColumnExpressions<U> makeSelectExpression(SymbExToColumns translator, LambdaInfo lambda) throws TypedValueVisitorException
   {
      // Handle the case where there is only one path
      if (lambda.symbolicAnalysis.paths.size() == 1)
      {
         SymbExPassDown passdown = SymbExPassDown.with(null, false);
         return simplifyAndTranslateMainPathToColumns(lambda, translator, passdown);
      }

      // Multi-path case
      
      // Calculate the return expressions and path conditions for each path
      List<ColumnExpressions<U>> returnExprs = new ArrayList<>();
      List<Expression> pathExprs = new ArrayList<>();
      int numPaths = lambda.symbolicAnalysis.paths.size();
      for (int n = 0; n < numPaths; n++)
      {
         SymbExPassDown passdown = SymbExPassDown.with(null, false);
         returnExprs.add(simplifyAndTranslatePathToColumns(lambda, n, translator, passdown));
         pathExprs.add(pathConditionsToExpr(translator, lambda.symbolicAnalysis.paths.get(n)));
      }
      
      // Check that the different paths generated consistent data
      for (int n = 1; n < numPaths; n++)
      {
         // TODO: Check that all the readers are compatible (should be due to Java type-checking, though 
         //    the user could do something silly like return Objects instead of something more specific)
         if (returnExprs.get(n).getNumColumns() != returnExprs.get(0).getNumColumns())
            throw new TypedValueVisitorException("Different paths returned different numbers of columns");
      }
      
      // Merge everything into a giant CASE WHEN statement
      // TODO: EclipseLink has problems when CASE WHEN is used to return entities instead of simple fields
      ColumnExpressions<U> toReturn = new ColumnExpressions<>(returnExprs.get(0).reader);
      for (int col = 0; col < returnExprs.get(0).getNumColumns(); col++)
      {
         // Check if all the paths are the same for this particular column
         boolean allSame = true;
         for (int n = 1; n < numPaths; n++)
         {
            if (!returnExprs.get(n).columns.get(col).equals(returnExprs.get(0).columns.get(col)))
            {
               allSame = false;
               break;
            }
         }
         
         if (allSame)
         {
            // Everything in this column in the same
            toReturn.columns.add(returnExprs.get(0).columns.get(col));
         }
         else
         {
            // Use a CASE WHEN... to handle the different possibilities
            List<CaseWhenExpression.ConditionResult> cases = new ArrayList<>();
            for (int n = 0; n < numPaths; n++)
            {
               CaseWhenExpression.ConditionResult c = new CaseWhenExpression.ConditionResult();
               c.condition = pathExprs.get(n);
               c.result = returnExprs.get(n).columns.get(col);
               cases.add(c);
            }
            toReturn.columns.add(new CaseWhenExpression(cases));
         }
      }
      return toReturn;
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere() || query.isSelectFromWhereGroupHaving())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, 
                  SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, metamodel, null, false));

            ColumnExpressions<U> returnExpr = makeSelectExpression(translator, lambda);

            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
            // TODO: translator.transform() should return multiple columns, not just one thing
            toReturn.cols = returnExpr;
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }

}
