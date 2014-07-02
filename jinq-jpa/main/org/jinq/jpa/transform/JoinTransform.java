package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class JoinTransform extends JPQLQueryTransform
{
   LambdaInfo lambda;
   boolean withSource;
   public JoinTransform(MetamodelUtil metamodel, LambdaInfo lambda, boolean withSource)
   {
      super(metamodel);
      this.lambda = lambda;
      this.withSource = withSource;
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query) throws QueryTransformException
   {
      try  {
         if (query instanceof SelectFromWhere)
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            
            SymbExToSubQuery translator = new SymbExToSubQuery(metamodel, 
                  new SelectFromWhereLambdaArgumentHandler(sfw, lambda, metamodel));

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in a JOIN at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            JPQLQuery<U> returnExpr = (JPQLQuery<U>)PathAnalysisSimplifier
                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), metamodel.comparisonMethods)
                  .visit(translator, passdown);

            // Create the new query, merging in the analysis of the method
            SelectFromWhere<U> toReturn = new SelectFromWhere<U>();
//            toReturn.froms.addAll(sfw.froms);
//            toReturn.where = sfw.where;
//            toReturn.cols = (ColumnExpressions<U>) sfw.cols;
//            return toReturn;
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }

}
