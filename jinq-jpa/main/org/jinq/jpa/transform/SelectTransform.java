package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
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
         if (query.isSelectFromWhere() || query.isSelectFromWhereGroup())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            SymbExToColumns translator = new SymbExToColumns(metamodel, alternateClassLoader, 
                  SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, metamodel, null, false));

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in a SELECT at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            ColumnExpressions<U> returnExpr = simplifyAndTranslateMainPathToColumns(lambda, translator, passdown);

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
