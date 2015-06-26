package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectOnly;
import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

/**
 * Handles the lookup of parameters passed to a lambda. Parameters can
 * be used to represent query parameters or references to the data stream.
 * This class handles the lookup of the grouping key and the grouped data 
 * stream of the result of a grouping query.
 */
public class GroupingLambdasArgumentHandler extends LambdaParameterArgumentHandler
{
   SelectOnly<?> groupKey;
   SelectOnly<?> stream; 
   
   public GroupingLambdasArgumentHandler(SelectOnly<?> groupKey, SelectOnly<?> stream, LambdaAnalysis lambda, MetamodelUtil metamodel, SymbExArgumentHandler parentArgumentScope, boolean hasInQueryStreamSource)
   {
      super(lambda, metamodel, parentArgumentScope, hasInQueryStreamSource);
      this.groupKey = groupKey;
      this.stream = stream;
   }
   
   @Override
   protected ColumnExpressions<?> handleLambdaArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      if (argIndex == 0)
         return groupKey.cols;
      throw new TypedValueVisitorException("Lambda trying to access unknown lambda parameter");
   }

   @Override
   protected JPQLQuery<?> handleLambdaSubQueryArg(int argIndex, Type argType) 
         throws TypedValueVisitorException
   {
      if (argIndex == 1)
         return stream;
      throw new TypedValueVisitorException("Lambda trying to access unknown lambda parameter");
   }
}
