package org.jinq.jooq.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jinq.jooq.querygen.ColumnExpressions;
import org.jinq.jooq.querygen.SimpleRowReader;
import org.jinq.jooq.querygen.TableRowReader;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

/**
 * Handles the lookup of parameters passed to a lambda. Parameters can
 * be used to represent query parameters or references to the data stream.
 * This class handles the lookup of a data stream of the result of a 
 * Select..From..Where query.
 */
public class SelectFromWhereLambdaArgumentHandler implements SymbExArgumentHandler
{
   List<Table<?>> fromList;
   LambdaInfo lambda;
   final int numLambdaCapturedArgs;

   public final static Set<Type> ALLOWED_QUERY_PARAMETER_TYPES = new HashSet<>();
   static {
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.INT_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.DOUBLE_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Integer"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Double"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/String"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/sql/Date"));
//    allowedQueryParameterTypes.put(Type.INT_TYPE, new SQLReader.IntegerSQLReader());
//    allowedQueryParameterTypes.put(Type.DOUBLE_TYPE, new SQLReader.DoubleSQLReader());
//    allowedQueryParameterTypes.put(Type.getObjectType("java/lang/Integer"), new SQLReader.IntegerSQLReader());
//    allowedQueryParameterTypes.put(Type.getObjectType("java/lang/Double"), new SQLReader.DoubleSQLReader());
//    allowedQueryParameterTypes.put(Type.getObjectType("java/lang/String"), new SQLReader.StringSQLReader());
//    allowedQueryParameterTypes.put(Type.getObjectType("java/sql/Date"), new SQLReader.DateSQLReader());
 }

   
   public SelectFromWhereLambdaArgumentHandler(LambdaInfo lambda, List<Table<?>> fromTables)
   {
      this.lambda = lambda;
      numLambdaCapturedArgs = lambda.serializedLambda.capturedArgs.length;
      this.fromList = fromTables;
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
         if (!ALLOWED_QUERY_PARAMETER_TYPES.contains(argType))
            throw new TypedValueVisitorException("Accessing a field with unhandled type");

         return ColumnExpressions.singleColumn(new SimpleRowReader<>(), DSL.val(lambda.getCapturedArg(argIndex)));
      }
      else
      {
         Table<?> table = fromList.get(argIndex - numLambdaCapturedArgs);
         // TODO: Should this return a single column or all the columns of the table?
         ColumnExpressions<?> columns = new ColumnExpressions<>(new TableRowReader<>(table));
         for (Field<?> field: table.fields())
            columns.columns.add(field);
         return columns;
      }
   }

}
