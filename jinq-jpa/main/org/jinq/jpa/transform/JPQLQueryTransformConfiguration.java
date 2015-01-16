package org.jinq.jpa.transform;

import java.util.List;
import java.util.Map;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysis;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class JPQLQueryTransformConfiguration
{
   public MetamodelUtil metamodel;
   /**
    * When dealing with subqueries, we may need to inspect the code of
    * lambdas used in the subquery. This may require us to use a special 
    * class loader to extract that code.
    */
   public ClassLoader alternateClassLoader;
   public boolean isObjectEqualsSafe;
   public boolean isCollectionContainsSafe;

   public Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> 
      getComparisonMethods()
   {
      return metamodel.getComparisonMethods(isObjectEqualsSafe);
   }

   public SymbExToColumns newSymbExToColumns(SymbExArgumentHandler argumentHandler)
   {
      return new SymbExToColumns(this, argumentHandler);
   }
   
   public SymbExToSubQuery newSymbExToSubQuery(SymbExArgumentHandler argumentHandler, boolean isExpectingStream)
   {
      return new SymbExToSubQuery(this, argumentHandler, isExpectingStream);
   }

   public Map<String, TypedValue> findLambdaAsClassConstructorParameters(MethodSignature sig, List<TypedValue> args) throws QueryTransformException
   {
      throw new IllegalArgumentException("Using classes as lambdas is not supported in Java Jinq");
   }

   public void checkLambdaSideEffects(LambdaAnalysis lambda) throws QueryTransformException
   {
      for (PathAnalysis path: lambda.symbolicAnalysis.paths)
      {
         if (!path.getSideEffects().isEmpty())
            throw new QueryTransformException("Lambda has a side-effect that can't be emulated with a database query"); 
      }
   }
   
   public JPQLQueryTransformConfiguration()
   {
   }
}