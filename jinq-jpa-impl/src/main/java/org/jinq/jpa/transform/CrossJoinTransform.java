package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.From;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.OffsetLambdaIndexInExpressionsVisitor;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.TupleRowReader;

public class CrossJoinTransform extends JPQLTwoQueryMergeQueryTransform
{
   public CrossJoinTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
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
         
         // The two queries seem compatible. Do the merge
         SelectFromWhere<?> merged = sfw1.shallowCopy();
         merged.cols = new ColumnExpressions<>(TupleRowReader.createReaderForTuple(merged.cols.reader, sfw2.cols.reader));
         for (Expression col: sfw1.cols.columns)
         {
            merged.cols.columns.add(col);
         }
         for (Expression col: sfw2.cols.columns)
         {
            Expression offsetCol = col.copy();
            offsetCol.visit(new OffsetLambdaIndexInExpressionsVisitor(lambdaOffset));
            merged.cols.columns.add(offsetCol);
         }
         // TODO: do a proper copy of expressions that might be embedded inside FROMs
         for (From from: sfw2.froms)
            merged.froms.add(from);
         Expression offsetWhere = sfw2.where.copy();
         offsetWhere.visit(new OffsetLambdaIndexInExpressionsVisitor(lambdaOffset));
         merged.where = new BinaryExpression("AND", merged.where, offsetWhere);
         return (SelectFromWhere<W>)merged;
      }
      throw new QueryTransformException("Cannot cross join these two query streams");
   }
   
   @Override
   public String getTransformationTypeCachingTag()
   {
      return CrossJoinTransform.class.getName();
   }

}
