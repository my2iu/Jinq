package org.jinq.jpa.transform;

import java.util.Map;

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

   public Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> 
      getComparisonMethods()
   {
      return metamodel.getComparisonMethods(isObjectEqualsSafe);
   }

   public SymbExToColumns newSymbExToColumns(JPQLQueryTransformConfiguration config, SymbExArgumentHandler argumentHandler)
   {
      return new SymbExToColumns(config, argumentHandler);
   }
   
   public SymbExToSubQuery newSymbExToSubQuery(JPQLQueryTransformConfiguration config, SymbExArgumentHandler argumentHandler)
   {
      return new SymbExToSubQuery(config, argumentHandler);
   }


   
   public JPQLQueryTransformConfiguration()
   {
   }
}