package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.JPQLQuery;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class WhereTransform extends JPQLQueryTransform
{
   LambdaInfo where;
   public WhereTransform(LambdaInfo where)
   {
      this.where = where;
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query)
   {
      try  {
         SymbExToExpression translator = new SymbExToExpression();
         for (int n = 0; n < where.symbolicAnalysis.paths.size(); n++)
         {
            PathAnalysis path = where.symbolicAnalysis.paths.get(n);
            
            Expression returnExpr = translator.transform(path.getSimplifiedReturnValue());

            // TODO: Handle where conditions
            
            // TODO: Merge into new query
         }
         return null;
      } catch (TypedValueVisitorException e)
      {
         e.printStackTrace();
         return null;
      }
   }

}
