package org.jinq.jpa.transform;

import java.util.HashSet;
import java.util.Set;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.ParameterExpression;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SimpleRowReader;
import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

/**
 * Handles the lookup of parameters passed to a lambda. Parameters can
 * be used to represent query parameters or references to the data stream.
 * This class handles the lookup of a data stream of the result of a 
 * Select..From..Where query.
 */
public class SelectFromWhereLambdaArgumentHandler implements SymbExArgumentHandler
{
   SelectFromWhere<?> sfw;
   LambdaInfo lambda;
   MetamodelUtil metamodel;
   boolean hasInQueryStreamSource;
   final int numLambdaCapturedArgs;
   
   public final static Set<Type> ALLOWED_QUERY_PARAMETER_TYPES = new HashSet<>();
   static {
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.INT_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.DOUBLE_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.LONG_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.BOOLEAN_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Integer"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Double"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Long"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Boolean"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/String"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/sql/Date"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/sql/Time"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/sql/Timestamp"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/util/Date"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/util/Calendar"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/math/BigDecimal"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/math/BigInteger"));
   }

   
   public SelectFromWhereLambdaArgumentHandler(SelectFromWhere<?> sfw, LambdaInfo lambda, MetamodelUtil metamodel, boolean hasInQueryStreamSource)
   {
      this.sfw = sfw;
      this.lambda = lambda;
      this.metamodel = metamodel;
      this.hasInQueryStreamSource = hasInQueryStreamSource; 
      numLambdaCapturedArgs = lambda.serializedLambda.capturedArgs.length;
   }
   
   @Override
   public ColumnExpressions<?> handleArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      if (argIndex < numLambdaCapturedArgs)
      {
         // Currently, we only support parameters of a few small simple types.
         // We should also support more complex types (e.g. entities) and allow
         // fields/methods of those entities to be called in the query (code
         // motion will be used to push those field accesses or method calls
         // outside the query where they will be evaluated and then passed in
         // as a parameter)
         if (!ALLOWED_QUERY_PARAMETER_TYPES.contains(argType) && !metamodel.isKnownEnumType(argType.getInternalName()))
            throw new TypedValueVisitorException("Accessing a field with unhandled type");

         return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
               new ParameterExpression(lambda.lambdaIndex, argIndex)); 

/*         
         try
         {
            // TODO: Careful here. ParameterLocation is relative to the base
            // lambda, but if we arrive here from inside a nested query, "this"
            // might refer to a lambda nested inside the base lambda. (Of course,
            // nested queries with parameters aren't currently supported, so it
            // doesn't matter.)
            ParameterLocation paramLoc = ParameterLocation.createJava8LambdaArgAccess(val.getIndex(), lambdaIndex);
            SQLColumnValues toReturn = new SQLColumnValues(allowedQueryParameterTypes.get(t));
            assert(toReturn.getNumColumns() == 1);
            toReturn.columns[0].add(new SQLSubstitution.ExternalParameterLink(paramLoc));
            return toReturn;
         } catch (Exception e)
         {
            throw new TypedValueVisitorException(e); 
         } 
*/
      }
      else if (hasInQueryStreamSource && argIndex == numLambdaCapturedArgs+1)
         throw new TypedValueVisitorException("Using InQueryStreamSource as data");
      else
      // TODO: For JPQL queries, I don't think it's necessary to make a copy of the columns
      //    because I think JPQL lets you substitute the same parameter into multiple locations
      //    in a query (unlike JDBC), which means we don't need separate state for query fragments
      //    that appear multiple times in the query tree.
      return sfw.cols;
   }
   
   @Override public boolean checkIsInQueryStreamSource(int argIndex)
   {
      return hasInQueryStreamSource && argIndex == numLambdaCapturedArgs+1;
   }
}
