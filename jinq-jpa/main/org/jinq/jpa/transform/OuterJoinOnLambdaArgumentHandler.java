package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;
import org.jinq.jpa.jpqlquery.SimpleRowReader;
import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

/**
 * Handles the lookup of parameters passed to a lambda. Parameters can
 * be used to represent query parameters or references to the data stream.
 * This class handles the lookup for the ON lambda of a LEFT OUTER JOIN...ON
 * query. This lambda takes two parameters instead of the usual one.
 */
public class OuterJoinOnLambdaArgumentHandler extends LambdaParameterArgumentHandler
{
   ColumnExpressions<?> leftCols;
   ColumnExpressions<?> rightCols;
   
   public OuterJoinOnLambdaArgumentHandler(ColumnExpressions<?> leftCols, ColumnExpressions<?> rightCols, LambdaAnalysis lambda, MetamodelUtil metamodel, SymbExArgumentHandler parentArgumentScope)
   {
      super(lambda, metamodel, parentArgumentScope, false);
      this.leftCols = leftCols;
      this.rightCols = rightCols;
   }
   
   @Override
   protected ColumnExpressions<?> handleLambdaArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      // TODO: For JPQL queries, I don't think it's necessary to make a copy of the columns
      //    because I think JPQL lets you substitute the same parameter into multiple locations
      //    in a query (unlike JDBC), which means we don't need separate state for query fragments
      //    that appear multiple times in the query tree.
      if (argIndex == 0)
         return leftCols;
      else if (argIndex == 1)
         return rightCols;
      throw new TypedValueVisitorException("Lambda trying to access unknown lambda parameter");
   }
}
