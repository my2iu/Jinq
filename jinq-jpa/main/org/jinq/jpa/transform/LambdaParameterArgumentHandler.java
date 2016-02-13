package org.jinq.jpa.transform;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.ParameterAsQuery;
import org.jinq.jpa.jpqlquery.ParameterExpression;
import org.jinq.jpa.jpqlquery.ParameterFieldExpression;
import org.jinq.jpa.jpqlquery.SimpleRowReader;
import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

/**
 * Handles the lookup of parameters passed to a lambda. Parameters can
 * be used to represent query parameters or references to the data stream.
 * This class handles the lookup of a data stream of the result of a 
 * Select..From..Where query.
 */
public class LambdaParameterArgumentHandler implements SymbExArgumentHandler
{
   LambdaAnalysis lambda;
   MetamodelUtil metamodel;
   boolean hasInQueryStreamSource;
   SymbExArgumentHandler parentArgumentScope;
   final int numLambdaCapturedArgs;
   final int numLambdaArgs;
   
   public final static Set<Type> ALLOWED_QUERY_PARAMETER_TYPES = new HashSet<>();
   static {
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.INT_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.DOUBLE_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.FLOAT_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.LONG_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.BOOLEAN_TYPE);
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Integer"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Double"));
      ALLOWED_QUERY_PARAMETER_TYPES.add(Type.getObjectType("java/lang/Float"));
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

   public LambdaParameterArgumentHandler(LambdaAnalysis lambda, MetamodelUtil metamodel, SymbExArgumentHandler parentArgumentScope, boolean hasInQueryStreamSource)
   {
      this.lambda = lambda;
      this.metamodel = metamodel;
      this.hasInQueryStreamSource = hasInQueryStreamSource;
      numLambdaCapturedArgs = lambda.getNumCapturedArgs();
      numLambdaArgs = lambda.getNumLambdaArgs();
      this.parentArgumentScope = parentArgumentScope;
   }

   protected ColumnExpressions<?> handleLambdaArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled lambda arguments");
   }
   
   protected ColumnExpressions<?> handleIndirectLambdaArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      // The actual value for the parameter is not available because this is a sub-lambda.
      // Extract the parent scope to see how the parameter is used in the parent lambda
      TypedValue paramVal = lambda.getIndirectCapturedArg(argIndex);
      
      // Right now, we only support sub-lambda parameters that are simply passthroughs for
      // parameters defined in the parent lambda.
      if (paramVal instanceof TypedValue.ArgValue)
      {
         TypedValue.ArgValue paramArg = (TypedValue.ArgValue)paramVal; 
         int parentArgIndex = paramArg.getIndex();
         if (parentArgumentScope == null)
            throw new TypedValueVisitorException("Cannot find a parent scope to determine how to access as sublambda's parent parameters.");
         // TODO: Right now, we need to be careful about the scope of parent lambdas. Since we only support
         // limited usage of parameters for sublambdas, it's not a problem yet, but more complicated usages
         // might be problematic. (Might have to pass additional parameteres to handleArg etc.)
         return parentArgumentScope.handleArg(parentArgIndex, argType);
      }
      else
      {
         throw new TypedValueVisitorException("Jinq can only passthrough parent lambda parameters directly to sub-lambdas. Sublambdas cannot take parameters that involve computation.");
      }
   }
   
   protected ColumnExpressions<?> getAndValidateArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      // Currently, we only support parameters of a few small simple types.
      // We should also support more complex types (e.g. entities) and allow
      // fields/methods of those entities to be called in the query (code
      // motion will be used to push those field accesses or method calls
      // outside the query where they will be evaluated and then passed in
      // as a parameter)
      try {
         if (!ALLOWED_QUERY_PARAMETER_TYPES.contains(argType) 
               && (Annotations.asmTypeToClass(argType).isPrimitive() || 
                     (!metamodel.isKnownEnumType(argType.getInternalName()) 
                     && !metamodel.isKnownManagedType(argType.getClassName())
                     && !metamodel.isKnownConvertedType(argType.getClassName()))))
            throw new TypedValueVisitorException("Accessing a field with unhandled type: " + Annotations.asmTypeToClass(argType).getName());
      } 
      catch (ClassNotFoundException e) 
      {
         throw new TypedValueVisitorException("Accessing a field with unhandled type", e);
      }

      return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
            new ParameterExpression(lambda.getLambdaIndex(), argIndex)); 
   }

   protected JPQLQuery<?> handleIndirectLambdaSubQueryArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      // The actual value for the parameter is not available because this is a sub-lambda.
      // Extract the parent scope to see how the parameter is used in the parent lambda
      TypedValue paramVal = lambda.getIndirectCapturedArg(argIndex);
      
      // Right now, we only support sub-lambda parameters that are simply passthroughs for
      // parameters defined in the parent lambda.
      if (paramVal instanceof TypedValue.ArgValue)
      {
         TypedValue.ArgValue paramArg = (TypedValue.ArgValue)paramVal; 
         int parentArgIndex = paramArg.getIndex();
         if (parentArgumentScope == null)
            throw new TypedValueVisitorException("Cannot find a parent scope to determine how to access as sublambda's parent parameters.");
         // TODO: Right now, we need to be careful about the scope of parent lambdas. Since we only support
         // limited usage of parameters for sublambdas, it's not a problem yet, but more complicated usages
         // might be problematic. (Might have to pass additional parameteres to handleArg etc.)
         return parentArgumentScope.handleSubQueryArg(parentArgIndex, argType);
      }
      else
      {
         throw new TypedValueVisitorException("Jinq can only passthrough parent lambda parameters directly to sub-lambdas. Sublambdas cannot take parameters that involve computation.");
      }
   }
   
   protected JPQLQuery<?> getAndValidateSubQueryArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      // There are a few cases where collections can be passed into a query.
      try
      {
         if (!Collection.class.isAssignableFrom(Annotations.asmTypeToClass(argType)))
         {
            throw new TypedValueVisitorException("Using an unhandled type as a subquery parameter");
         }
      } 
      catch (ClassNotFoundException e)
      {
         throw new TypedValueVisitorException("Cannot find the class of the object being used as a parameter.");
      }
      
      ParameterAsQuery<?> query = new ParameterAsQuery<>();
      query.cols = ColumnExpressions.singleColumn(new SimpleRowReader<>(),
          new ParameterExpression(lambda.getLambdaIndex(), argIndex));
      return query;
   }

   @Override
   public ColumnExpressions<?> handleArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      if (argIndex < numLambdaCapturedArgs)
      {
         if (lambda == null)
            throw new TypedValueVisitorException("No lambda source was supplied where parameters can be extracted");
         if (lambda.usesIndirectArgs())
         {
            return handleIndirectLambdaArg(argIndex, argType);
         }
         
         return getAndValidateArg(argIndex, argType);
      }
      else if (checkIsInQueryStreamSource(argIndex))
         throw new TypedValueVisitorException("Using InQueryStreamSource as data");
      else
         return handleLambdaArg(argIndex - numLambdaCapturedArgs, argType);
   }

   protected JPQLQuery<?> handleLambdaSubQueryArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled lambda subquery arguments");
   }
   
   @Override
   public JPQLQuery<?> handleSubQueryArg(int argIndex, Type argType) throws TypedValueVisitorException
   {
      if (argIndex < numLambdaCapturedArgs)
      {
         if (lambda == null)
            throw new TypedValueVisitorException("No lambda source was supplied where parameters can be extracted");
         if (lambda.usesIndirectArgs())
         {
            return handleIndirectLambdaSubQueryArg(argIndex, argType);
         }
         
         return getAndValidateSubQueryArg(argIndex, argType);
      }
      else if (!checkIsInQueryStreamSource(argIndex))
      {
         return handleLambdaSubQueryArg(argIndex - numLambdaCapturedArgs, argType);
      }
      throw new TypedValueVisitorException("Cannot use parameters as a subquery");
   }

   protected ColumnExpressions<?> handleIndirectThisFieldRead(String name, Type argType) throws TypedValueVisitorException
   {
      // The actual value for the parameter is not available because this is a sub-lambda.
      // Extract the parent scope to see how the parameter is used in the parent lambda
      TypedValue paramVal = lambda.getIndirectFieldValue(name);
      
      // Right now, we only support sub-lambda parameters that are simply passthroughs for
      // parameters defined in the parent lambda.
      if (paramVal instanceof TypedValue.ArgValue)
      {
         TypedValue.ArgValue paramArg = (TypedValue.ArgValue)paramVal; 
         int parentArgIndex = paramArg.getIndex();
         if (parentArgumentScope == null)
            throw new TypedValueVisitorException("Cannot find a parent scope to determine how to access as sublambda's parent parameters.");
         // TODO: Right now, we need to be careful about the scope of parent lambdas. Since we only support
         // limited usage of parameters for sublambdas, it's not a problem yet, but more complicated usages
         // might be problematic. (Might have to pass additional parameteres to handleArg etc.)
         return parentArgumentScope.handleArg(parentArgIndex, argType);
      }
      else if (paramVal instanceof TypedValue.GetFieldValue)
      {
         TypedValue.GetFieldValue paramArg = (TypedValue.GetFieldValue)paramVal;
         String parentFieldName = paramArg.name;
         if (parentArgumentScope == null)
            throw new TypedValueVisitorException("Cannot find a parent scope to determine how to access as sublambda's parent parameters.");
         // TODO: Right now, we need to be careful about the scope of parent lambdas. Since we only support
         // limited usage of parameters for sublambdas, it's not a problem yet, but more complicated usages
         // might be problematic. (Might have to pass additional parameteres to handleArg etc.)
         return parentArgumentScope.handleThisFieldRead(parentFieldName, argType);
      }
      else
      {
         throw new TypedValueVisitorException("Jinq can only passthrough parent lambda parameters directly to sub-lambdas. Sublambdas cannot take parameters that involve computation.");
      }
   }

   @Override
   public ColumnExpressions<?> handleThisFieldRead(String name, Type argType)
         throws TypedValueVisitorException
   {
      if (lambda.usesParametersAsFields())
      {
         if (lambda.usesIndirectFields())
         {
            return handleIndirectThisFieldRead(name, argType);
         }
         
         // Currently, we only support parameters of a few small simple types.
         // We should also support more complex types (e.g. entities) and allow
         // fields/methods of those entities to be called in the query (code
         // motion will be used to push those field accesses or method calls
         // outside the query where they will be evaluated and then passed in
         // as a parameter)
         try {
            if (!ALLOWED_QUERY_PARAMETER_TYPES.contains(argType) 
                  && (Annotations.asmTypeToClass(argType).isPrimitive() || 
                        (!metamodel.isKnownEnumType(argType.getInternalName()) 
                        && !metamodel.isKnownManagedType(argType.getClassName())
                        && !metamodel.isKnownConvertedType(argType.getClassName()))))
               throw new TypedValueVisitorException("Accessing a field with unhandled type: " + Annotations.asmTypeToClass(argType).getName());
         } 
         catch (ClassNotFoundException e) 
         {
            throw new TypedValueVisitorException("Accessing a field with unhandled type", e);
         }

         return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
               new ParameterFieldExpression(lambda.getLambdaIndex(), name)); 
      }
      throw new TypedValueVisitorException("Cannot read fields of this lambda");
   }

   @Override
   public JPQLQuery<?> handleSubQueryThisFieldRead(String name, Type argType)
         throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Cannot read fields of this lambda for a subquery");
   }

   
   @Override public boolean checkIsInQueryStreamSource(int argIndex)
   {
      return hasInQueryStreamSource && argIndex == numLambdaArgs - 1;
   }
}
