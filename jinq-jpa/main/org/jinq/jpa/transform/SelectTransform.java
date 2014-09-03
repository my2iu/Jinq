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
