package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

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

   protected <U> ColumnExpressions<U> simplifyAndTranslateMainPathToColumns(LambdaInfo lambda, SymbExToColumns translator,
         SymbExPassDown passdown) throws TypedValueVisitorException
   {
      return (ColumnExpressions<U>)PathAnalysisSimplifier
            .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), metamodel.comparisonMethods)
            .visit(translator, passdown);
   }
}
