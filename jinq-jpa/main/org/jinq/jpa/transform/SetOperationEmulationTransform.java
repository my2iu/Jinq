package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.OffsetLambdaIndexInExpressionsVisitor;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.UnaryExpression;

public class SetOperationEmulationTransform extends JPQLTwoQueryMergeQueryTransform
{
   public enum SetOperationType
   {
      OR_UNION("OR"), AND_INTERSECT("AND"),
      DIFFERENCE("AND_NOT"); // do not use "AND_NOT" pseudo op; actually, this is composition of two: binary AND with unary NOT and as such results in less brackets
      SetOperationType(String op)
      {
         this.op = op;
      }
      String op;
   }

   SetOperationType type = SetOperationType.OR_UNION;
   
   public SetOperationEmulationTransform(JPQLQueryTransformConfiguration config, SetOperationType type)
   {
      super(config);
      this.type = type;
   }

   @Override
   public <U, V, W> JPQLQuery<W> apply(JPQLQuery<U> query1, JPQLQuery<V> query2, int lambdaOffset)
         throws QueryTransformException
   {
      // Just a stub implementation that handles a very basic situtation
      if (query1.isSelectFromWhere() && query2.isSelectFromWhere())
      {
         SelectFromWhere<U> sfw1 = (SelectFromWhere<U>)query1;
         SelectFromWhere<V> sfw2 = (SelectFromWhere<V>)query2;
         // TODO: These correctness checks aren't conservative enough because
         // it's possible to have identical column expressions, but have the
         // parameters they refer to be different because the lambdas are
         // different
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
         SelectFromWhere<?> merged = sfw1.shallowCopy();
//         for (From from: sfw2.froms)
//            merged.froms.add(from);
         if (merged.where == null && sfw2.where == null)
         {
            if (type == SetOperationType.DIFFERENCE) {
                merged.where = new BinaryExpression("=", new ConstantExpression("0"), new ConstantExpression("1"));
                return (SelectFromWhere<W>)merged;
            }
         }
         
         if (merged.where == null || sfw2.where == null)
         {
            Expression offsetWhere = merged.where;
            if (sfw2.where != null)
            {
               offsetWhere = sfw2.where.copy();
               offsetWhere.visit(new OffsetLambdaIndexInExpressionsVisitor(lambdaOffset));
               
               if (type == SetOperationType.DIFFERENCE) {
                   merged.where = UnaryExpression.prefix("NOT", offsetWhere); 
                   return (SelectFromWhere<W>) merged;
               }
            }
            
            switch (type)
            {
                case DIFFERENCE:
                    merged.where = new BinaryExpression("=", new ConstantExpression("0"), new ConstantExpression("1"));
                    return (SelectFromWhere<W>)merged;
                case AND_INTERSECT:                
                    merged.where = offsetWhere;
                    break;
                case OR_UNION:
                    merged.where = null;
            }
         }
         else
         {
            Expression offsetWhere = sfw2.where.copy();
            offsetWhere.visit(new OffsetLambdaIndexInExpressionsVisitor(lambdaOffset));
            merged.where = type != SetOperationType.DIFFERENCE ? new BinaryExpression(type.op, merged.where, offsetWhere)
                        : new BinaryExpression(SetOperationType.AND_INTERSECT.op, merged.where, UnaryExpression.prefix("NOT", offsetWhere));
         }
         return (SelectFromWhere<W>)merged;
      }
      throw new QueryTransformException("Cannot merge the two query streams");
   }
   
   @Override
   public String getTransformationTypeCachingTag()
   {
      return SetOperationEmulationTransform.class.getName() + ":" + type.name();
   }

}
