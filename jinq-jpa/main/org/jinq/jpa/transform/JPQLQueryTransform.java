package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;

/**
 * Subclasses of this class are used to hold the logic for applying
 * a lambda to a JPQL query (e.g. how to apply a where lambda to
 * a JPQL query, producing a new JPQL query)
 */
public class JPQLQueryTransform
{
   final MetamodelUtil metamodel;
   
   /**
    * When dealing with subqueries, we may need to inspect the code of
    * lambdas used in the subquery. This may require us to use a special 
    * class loader to extract that code.
    */
   final ClassLoader alternateClassLoader;
   
   JPQLQueryTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      this.metamodel = metamodel;
      this.alternateClassLoader = alternateClassLoader;
   }
}
