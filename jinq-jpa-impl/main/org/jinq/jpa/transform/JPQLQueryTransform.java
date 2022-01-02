package org.jinq.jpa.transform;

import java.util.ArrayList;
import java.util.List;

import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.CaseWhenExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.Expression;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysis;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

/**
 * Subclasses of this class are used to hold the logic for applying
 * a lambda to a JPQL query (e.g. how to apply a where lambda to
 * a JPQL query, producing a new JPQL query)
 */
public abstract class JPQLQueryTransform
{
   JPQLQueryTransformConfiguration config = new JPQLQueryTransformConfiguration();

   JPQLQueryTransform(JPQLQueryTransformConfiguration config)
   {
      this.config = config;
   }

   protected <U> ColumnExpressions<U> makeSelectExpression(SymbExToColumns translator, LambdaAnalysis lambda) throws TypedValueVisitorException
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

   protected <U> ColumnExpressions<U> simplifyAndTranslateMainPathToColumns(LambdaAnalysis lambda, SymbExToColumns translator,
         SymbExPassDown passdown) throws TypedValueVisitorException
   {
      return simplifyAndTranslatePathToColumns(lambda, 0, translator, passdown);
   }
   
   protected <U> ColumnExpressions<U> simplifyAndTranslatePathToColumns(LambdaAnalysis lambda, int pathIdx, SymbExToColumns translator,
         SymbExPassDown passdown) throws TypedValueVisitorException
   {
      return (ColumnExpressions<U>)PathAnalysisSimplifier
            .simplify(lambda.symbolicAnalysis.paths.get(pathIdx).getReturnValue(), config.getComparisonMethods(), config.getComparisonStaticMethods(), config.isAllEqualsSafe)
            .visit(translator, passdown);
   }

   protected Expression pathConditionsToExpr(SymbExToColumns translator,
         PathAnalysis path) throws TypedValueVisitorException
   {
      Expression conditionExpr = null;
      for (TypedValue cmp: path.getConditions())
      {
         SymbExPassDown passdown = SymbExPassDown.with(null, true);
         ColumnExpressions<?> col = cmp.visit(translator, passdown);
         if (!col.isSingleColumn()) 
            throw new TypedValueVisitorException("Expecting a single column result for path condition");
         Expression expr = col.getOnlyColumn();
         if (conditionExpr != null)
            conditionExpr = new BinaryExpression("AND", conditionExpr, expr);
         else
            conditionExpr = expr;
      }
      return conditionExpr;
   }

   protected static void pathConditionsToClauses(PathAnalysis path, List<TypedValue> clauses) throws TypedValueVisitorException
   {
      for (TypedValue cmp: path.getConditions())
      {
         clauses.add(cmp);
      }
   }

   /**
    * Returns a String that can be used to identify the type of transformation being applied.
    * This is useful for caching the effects of query transforms.
    */
   public abstract String getTransformationTypeCachingTag();
}
