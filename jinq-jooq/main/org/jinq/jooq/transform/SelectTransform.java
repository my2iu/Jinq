package org.jinq.jooq.transform;

import java.util.Collections;
import java.util.List;

import org.jinq.jooq.querygen.ColumnExpressions;
import org.jooq.Table;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SelectTransform
{
   MetamodelUtil metamodel;
   LambdaInfo lambda;
   public SelectTransform(MetamodelUtil metamodel, LambdaInfo lambda)
   {
      this.metamodel = metamodel;
      this.lambda = lambda;
   }
   
   public <U> ColumnExpressions<U> apply(List<Table<?>> fromTables)
   {
      try  {
         SymbExToColumns translator = new SymbExToColumns(metamodel, 
               new SelectFromWhereLambdaArgumentHandler(lambda, fromTables));

         // TODO: Handle this case by translating things to use SELECT CASE 
         if (lambda.symbolicAnalysis.paths.size() > 1) return null;
         
         ColumnExpressions<U> returnExpr = (ColumnExpressions<U>)translator.transform(PathAnalysisSimplifier.simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), Collections.emptyMap(), Collections.emptyMap(), false));

         return returnExpr;
      } catch (TypedValueVisitorException e)
      {
         e.printStackTrace();
         throw new IllegalArgumentException("Could not create query from lambda", e);
      }
   }

}
