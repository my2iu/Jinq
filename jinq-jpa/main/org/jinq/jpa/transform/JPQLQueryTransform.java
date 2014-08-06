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
   JPQLQueryTransform(MetamodelUtil metamodel)
   {
      this.metamodel = metamodel;
   }
}
