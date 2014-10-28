package org.jinq.jpa.transform;

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

   public JPQLQueryTransformConfiguration()
   {
   }
}