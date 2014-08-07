package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

/**
 * Handles the lookup of parameters passed to a lambda. Parameters can
 * be used to represent query parameters or references to the data stream.
 * This class handles the lookup of a data stream of the result of a 
 * Select..From..Where query.
 */
public class SelectFromWhereLambdaArgumentHandler extends LambdaParameterArgumentHandler
{
   SelectFromWhere<?> sfw;
   
   public SelectFromWhereLambdaArgumentHandler(SelectFromWhere<?> sfw, LambdaInfo lambda, MetamodelUtil metamodel, boolean hasInQueryStreamSource)
   {
      super(lambda, metamodel, hasInQueryStreamSource);
      this.sfw = sfw;
   }
   
   @Override
   protected ColumnExpressions<?> handleLambdaArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      // TODO: For JPQL queries, I don't think it's necessary to make a copy of the columns
      //    because I think JPQL lets you substitute the same parameter into multiple locations
      //    in a query (unlike JDBC), which means we don't need separate state for query fragments
      //    that appear multiple times in the query tree.
      return sfw.cols;
   }
}
