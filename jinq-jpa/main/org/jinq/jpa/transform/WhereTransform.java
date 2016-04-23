package org.jinq.jpa.transform;

import java.util.ArrayList;
import java.util.List;

import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.GroupedSelectFromWhere;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysis;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class WhereTransform extends JPQLOneLambdaQueryTransform
{
   boolean withSource;
   public WhereTransform(JPQLQueryTransformConfiguration config, boolean withSource)
   {
      super(config);
      this.withSource = withSource;
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaAnalysis where, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            Expression methodExpr = computeWhereReturnExpr(where, sfw, parentArgumentScope);
            
            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
            if (sfw.where == null)
               toReturn.where = methodExpr;
            else
               toReturn.where = new BinaryExpression("AND", sfw.where, methodExpr);
            return toReturn;
         }
         else if (query.isSelectFromWhereGroupHaving())
         {
            GroupedSelectFromWhere<V, ?> sfw = (GroupedSelectFromWhere<V, ?>)query;
            Expression methodExpr = computeWhereReturnExpr(where, sfw, parentArgumentScope);
            
            // Create the new query, merging in the analysis of the method
            GroupedSelectFromWhere<U, ?> toReturn = (GroupedSelectFromWhere<U, ?>)sfw.shallowCopy();
            if (sfw.having == null)
               toReturn.having = methodExpr;
            else
               toReturn.having = new BinaryExpression("AND", sfw.having, methodExpr);
            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      }
      catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }

   private <V> Expression computeWhereReturnExpr(LambdaAnalysis where,
         SelectFromWhere<V> sfw, SymbExArgumentHandler parentArgumentScope) 
               throws TypedValueVisitorException, QueryTransformException
   {
      return computeWhereReturnExpr(config, where, sfw, SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, where, config.metamodel, parentArgumentScope, withSource));
   }

   public static <V> Expression computeWhereReturnExpr(JPQLQueryTransformConfiguration config, LambdaAnalysis where,
         SelectFromWhere<V> sfw, LambdaParameterArgumentHandler argumentHandler) 
               throws TypedValueVisitorException, QueryTransformException
   {
      // Gather up the conditions for when the path is true (as a disjunction of conjunctive clauses--disjunctive normal form)
      SymbExToColumns translator = config.newSymbExToColumns(argumentHandler);
      List<List<TypedValue>> disjunction = new ArrayList<>();
      for (int n = 0; n < where.symbolicAnalysis.paths.size(); n++)
      {
         List<TypedValue> clauses = new ArrayList<>();
         PathAnalysis path = where.symbolicAnalysis.paths.get(n);

         TypedValue returnVal = PathAnalysisSimplifier
               .simplifyBoolean(path.getReturnValue(), config.getComparisonMethods(), config.getComparisonStaticMethods(), config.isAllEqualsSafe);
         if (returnVal instanceof ConstantValue.BooleanConstant)
         {
            if (((ConstantValue.BooleanConstant)returnVal).val)
            {
               // This path returns true, so it's redundant to actually
               // put true into the final code.
               returnVal = null;
            }
            else
            {
               // This path returns false, so we can ignore it
               continue;
            }
         }
         if (returnVal != null)
            clauses.add(returnVal);
         
         // Handle where path conditions
         pathConditionsToClauses(path, clauses);
         
         disjunction.add(clauses);
      }
      
      // Check for some common patterns that we can simplify
      checkForOrChain(disjunction);
      
      // Convert the disjunction of clauses into a final expression
      Expression methodExpr = null;
      for (List<TypedValue> conjunction: disjunction)
      {
         Expression pathExpr = null;
         for (TypedValue clause: conjunction)
         {
            SymbExPassDown passdown = SymbExPassDown.with(null, true);
            ColumnExpressions<?> col = clause.visit(translator, passdown);
            if (!col.isSingleColumn()) 
               throw new TypedValueVisitorException("Expecting a single column result for path condition");
            Expression expr = col.getOnlyColumn();

            if (pathExpr == null)
               pathExpr = expr;
            else
               pathExpr = new BinaryExpression("AND", pathExpr, expr);
         }
         // Merge into new expression summarizing the method
         if (methodExpr != null)
            methodExpr = new BinaryExpression("OR", methodExpr, pathExpr);
         else
            methodExpr = pathExpr;
      }
      
      return methodExpr;
   }

   /**
    * Identifies a common pattern of a chain of ORs so that it
    * can be encoded more simply.
    */
   private static void checkForOrChain(List<List<TypedValue>> disjunction)
   {
      // TODO: Handle more complex patterns
      // TODO: generate expressions that do not use NOT (causes problems with NULLs)
      // TODO: do arbitrary expression simplification
      // TODO: do expression generation earlier based on the original code structure
      
      // Just do a simple pattern match for now
      List<TypedValue> canIgnoreClauses = new ArrayList<>();
      for (int n = 0; n < disjunction.size(); n++)
      {
         // If there's only value VAL, then we can remove NOT(VAL) from all other conjunctions 
         List<TypedValue> conjunction = disjunction.get(n);
         if (conjunction.size() != 1)
            break;
         TypedValue not = TypedValue.NotValue.invert(conjunction.get(0));
         canIgnoreClauses.add(not);
         // Remove NOT(VAL) from all subsequent clauses
         for (int i = n + 1; i < disjunction.size(); i++)
            disjunction.get(i).remove(not);
      }
   }
   
   @Override 
   public String getTransformationTypeCachingTag()
   {
      return WhereTransform.class.getName();
   }
}
