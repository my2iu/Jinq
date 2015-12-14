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
         SelectFromWhere<V> sfw, SymbExArgumentHandler parentArgumentScope) throws TypedValueVisitorException,
         QueryTransformException
   {
      // Gather up the conditions for when the path is true (as a disjunction of conjunctive clauses--disjunctive normal form)
      SymbExToColumns translator = config.newSymbExToColumns(SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, where, config.metamodel, parentArgumentScope, withSource));
      List<List<Expression>> disjunction = new ArrayList<>();
      for (int n = 0; n < where.symbolicAnalysis.paths.size(); n++)
      {
         List<Expression> clauses = new ArrayList<>();
         PathAnalysis path = where.symbolicAnalysis.paths.get(n);

         TypedValue returnVal = PathAnalysisSimplifier
               .simplifyBoolean(path.getReturnValue(), config.getComparisonMethods(), config.getComparisonStaticMethods(), config.isAllEqualsSafe);
         SymbExPassDown returnPassdown = SymbExPassDown.with(null, true);
         ColumnExpressions<?> returnColumns = returnVal.visit(translator, returnPassdown);
         if (!returnColumns.isSingleColumn())
            throw new QueryTransformException("Expecting single column");
         Expression returnExpr = returnColumns.getOnlyColumn();

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
         if (returnExpr != null)
            clauses.add(returnExpr);
         
         // Handle where path conditions
         pathConditionsToClauses(translator, path, clauses);
         
         disjunction.add(clauses);
         
      }
      // Convert the disjunction of clauses into a final expression
      Expression methodExpr = null;
      for (List<Expression> conjunction: disjunction)
      {
         Expression pathExpr = null;
         for (Expression clause: conjunction)
         {
            if (pathExpr == null)
               pathExpr = clause;
            else
               pathExpr = new BinaryExpression("AND", pathExpr, clause);
         }
         // Merge into new expression summarizing the method
         if (methodExpr != null)
            methodExpr = new BinaryExpression("OR", methodExpr, pathExpr);
         else
            methodExpr = pathExpr;
      }
      
      return methodExpr;
   }

   @Override 
   public String getTransformationTypeCachingTag()
   {
      return WhereTransform.class.getName();
   }
}
