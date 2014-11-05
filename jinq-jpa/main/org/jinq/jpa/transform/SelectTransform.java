package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SelectTransform extends JPQLOneLambdaQueryTransform
{
   boolean withSource;
   public SelectTransform(JPQLQueryTransformConfiguration config, boolean withSource)
   {
      super(config);
      this.withSource = withSource;
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaAnalysis lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere() || query.isSelectFromWhereGroupHaving())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            SymbExToColumns translator = config.newSymbExToColumns(SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, config.metamodel, parentArgumentScope, withSource));

            ColumnExpressions<U> returnExpr = makeSelectExpression(translator, lambda);

            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
            // TODO: translator.transform() should return multiple columns, not just one thing
            toReturn.cols = returnExpr;
            return toReturn;
         }
         else if (query.isSelectOnly())
         {
            SelectOnly<V> sfw = (SelectOnly<V>)query;
            SymbExToColumns translator = config.newSymbExToColumns(SelectFromWhereLambdaArgumentHandler.fromSelectOnly(sfw, lambda, config.metamodel, parentArgumentScope, false));

            ColumnExpressions<U> returnExpr = makeSelectExpression(translator, lambda);

            // Create the new query, merging in the analysis of the method
            SelectOnly<U> toReturn = (SelectOnly<U>)sfw.shallowCopy();
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

   @Override 
   public String getTransformationTypeCachingTag()
   {
      return SelectTransform.class.getName();
   }
}
