package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.UnaryExpression;


public class NotTransform extends JPQLNoLambdaQueryTransform
{
   public NotTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      if (query.isSelectFromWhere())
      { 
         SelectFromWhere<U> sfw = (SelectFromWhere<U>)query;
         SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
         if (toReturn.where != null) toReturn.where = UnaryExpression.prefix("NOT", toReturn.where);
         else toReturn.where = new BinaryExpression("=", new ConstantExpression("0"), new ConstantExpression("1"));
         
         return toReturn;
      }
      throw new QueryTransformException("Existing query cannot be transformed further");
   }
   
   @Override 
   public String getTransformationTypeCachingTag()
   {
      return NotTransform.class.getName();
   }

}
