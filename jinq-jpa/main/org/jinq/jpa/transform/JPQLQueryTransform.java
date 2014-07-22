package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.JPQLQuery;

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
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      return null;
   }
}
