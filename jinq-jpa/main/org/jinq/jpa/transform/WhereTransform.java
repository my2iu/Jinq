package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class WhereTransform extends JPQLQueryTransform
{
   LambdaInfo where;
   public WhereTransform(MetamodelUtil metamodel, LambdaInfo where)
   {
      super(metamodel);
      this.where = where;
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query)
   {
      try  {
         if (query instanceof SelectFromWhere)
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            // TODO: froms.get(0) is temporary 
            SymbExToExpression translator = new SymbExToExpression(metamodel, sfw.froms.get(0));
            for (int n = 0; n < where.symbolicAnalysis.paths.size(); n++)
            {
               PathAnalysis path = where.symbolicAnalysis.paths.get(n);
               
               Expression returnExpr = translator.transform(path.getSimplifiedReturnValue());
               
               // TODO: Temporary
               SelectFromWhere<U> toReturn = new SelectFromWhere<U>();
               toReturn.froms.addAll(sfw.froms);
               toReturn.cols.addAll(sfw.cols);
               toReturn.where = returnExpr;
               return toReturn;
               
               // TODO: Handle where conditions
               
               // TODO: Merge into new query
            }
         }
         return null;
      } catch (TypedValueVisitorException e)
      {
         e.printStackTrace();
         return null;
      }
   }

}
