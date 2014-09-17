package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SelectTransform extends JPQLOneLambdaQueryTransform
{
   boolean withSource;
   public SelectTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean withSource)
   {
      super(metamodel, alternateClassLoader);
      this.withSource = withSource;
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere() || query.isSelectFromWhereGroupHaving())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, 
                  SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, metamodel, parentArgumentScope, withSource));

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
            SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, 
                  SelectFromWhereLambdaArgumentHandler.fromSelectOnly(sfw, lambda, metamodel, parentArgumentScope, false));

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

}
