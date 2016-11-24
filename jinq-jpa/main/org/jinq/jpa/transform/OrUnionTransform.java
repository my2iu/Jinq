package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.From;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

public class OrUnionTransform extends JPQLTwoQueryMergeQueryTransform
{
   public enum SetOperationType
   {
      OR_UNION
   }

   SetOperationType type = SetOperationType.OR_UNION;
   
   public OrUnionTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
   }

   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<U> query1, JPQLQuery<V> query2)
         throws QueryTransformException
   {
      // Just a stub implementation that handles a very basic situtation
      if (query1.isSelectFromWhere() && query2.isSelectFromWhere())
      {
         SelectFromWhere<U> sfw1 = (SelectFromWhere<U>)query1;
         SelectFromWhere<V> sfw2 = (SelectFromWhere<V>)query2;
         if (!sfw1.cols.isSingleColumn() || !sfw2.cols.isSingleColumn())
            throw new QueryTransformException("Cannot only merge queries that return one field of data");
         if (!sfw1.cols.getOnlyColumn().equals(sfw2.cols.getOnlyColumn()))
            throw new QueryTransformException("Cannot only merge queries that return the exact same SELECTed data");
         if (sfw1.froms.size() != sfw2.froms.size())
            throw new QueryTransformException("Cannot only merge queries that are based on the same data source");
         for (int n = 0; n < sfw1.froms.size(); n++)
         {
            if (!sfw1.froms.get(n).equals(sfw2.froms.get(n)))
               throw new QueryTransformException("Cannot only merge queries that are based on the same data source");
         }
         
         // The two queries seem compatible. Do the merge
         SelectFromWhere<U> merged = sfw1.shallowCopy();
//         for (From from: sfw2.froms)
//            merged.froms.add(from);
         merged.where = new BinaryExpression("OR", merged.where, sfw2.where);
         return merged;
      }
      throw new QueryTransformException("Cannot merge the two query streams");
   }
   
   @Override
   public String getTransformationTypeCachingTag()
   {
      return OrUnionTransform.class.getName() + ":" + type.name();
   }

}
