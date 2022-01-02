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
 * This class handles the lookup of a data stream of the result of a 
 * Select..From..Where query.
 */
public class SelectFromWhereLambdaArgumentHandler extends LambdaParameterArgumentHandler
{
   final static ColumnExpressions<?> passthroughColsForTesting = ColumnExpressions.singleColumn(new SimpleRowReader(), new ConstantExpression("PASSTHROUGH TEST"));
   
   ColumnExpressions<?> cols;
   
   public static SelectFromWhereLambdaArgumentHandler fromSelectFromWhere(SelectFromWhere<?> sfw, LambdaAnalysis lambda, MetamodelUtil metamodel, SymbExArgumentHandler parentArgumentScope, boolean hasInQueryStreamSource)
   {
      return new SelectFromWhereLambdaArgumentHandler(sfw.cols, lambda, metamodel, parentArgumentScope, hasInQueryStreamSource);
   }

   public static SelectFromWhereLambdaArgumentHandler fromSelectOnly(SelectOnly<?> select, LambdaAnalysis lambda, MetamodelUtil metamodel, SymbExArgumentHandler parentArgumentScope, boolean hasInQueryStreamSource)
   {
      return new SelectFromWhereLambdaArgumentHandler(select.cols, lambda, metamodel, parentArgumentScope, hasInQueryStreamSource);
   }

   public static SelectFromWhereLambdaArgumentHandler forPassthroughTest(LambdaAnalysis lambda, MetamodelUtil metamodel, SymbExArgumentHandler parentArgumentScope, boolean hasInQueryStreamSource)
   {
      return new SelectFromWhereLambdaArgumentHandler(passthroughColsForTesting, lambda, metamodel, parentArgumentScope, hasInQueryStreamSource);
   }

   private SelectFromWhereLambdaArgumentHandler(ColumnExpressions<?> cols, LambdaAnalysis lambda, MetamodelUtil metamodel, SymbExArgumentHandler parentArgumentScope, boolean hasInQueryStreamSource)
   {
      super(lambda, metamodel, parentArgumentScope, hasInQueryStreamSource);
      this.cols = cols;
   }
   
   @Override
   protected ColumnExpressions<?> handleLambdaArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      // TODO: For JPQL queries, I don't think it's necessary to make a copy of the columns
      //    because I think JPQL lets you substitute the same parameter into multiple locations
      //    in a query (unlike JDBC), which means we don't need separate state for query fragments
      //    that appear multiple times in the query tree.
      if (argIndex == 0)
         return cols;
      throw new TypedValueVisitorException("Lambda trying to access unknown lambda parameter");
   }
}
