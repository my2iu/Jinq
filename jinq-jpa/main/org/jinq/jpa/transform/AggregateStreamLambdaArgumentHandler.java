package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

/**
 * Handles the lookup of parameters passed to a lambda. Parameters can
 * be used to represent query parameters or references to the data stream.
 * This class handles the lookup of a data stream of the result of a 
 * Select..From..Where query.
 */
public class AggregateStreamLambdaArgumentHandler extends LambdaParameterArgumentHandler
{
   public AggregateStreamLambdaArgumentHandler(LambdaInfo lambda, MetamodelUtil metamodel, boolean hasInQueryStreamSource)
   {
      super(lambda, metamodel, hasInQueryStreamSource);
   }
   
   @Override
   public JPQLQuery<?> handleSubQueryArg(int argIndex, Type argType)
         throws TypedValueVisitorException
   {
      return super.handleSubQueryArg(argIndex, argType);
   }
}
