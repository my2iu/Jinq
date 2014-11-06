package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.FromAliasExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.TupleRowReader;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class JoinTransform extends JPQLOneLambdaQueryTransform
{
   boolean withSource;
   public JoinTransform(JPQLQueryTransformConfiguration config, boolean withSource)
   {
      super(config);
      this.withSource = withSource;
   }
   
   static boolean isSimpleFrom(JPQLQuery<?> query)
   {
      if (!query.isSelectFromWhere()) return false;
      SelectFromWhere<?> sfw = (SelectFromWhere<?>)query;
      if (sfw.where != null) return false;
      if (sfw.froms.size() != 1) return false;
      if (!sfw.cols.isSingleColumn()) return false;
      Expression expr = sfw.cols.getOnlyColumn();
      if (!(expr instanceof FromAliasExpression)) return false;
      if (((FromAliasExpression)expr).from != sfw.froms.get(0)) return false;
      return true;      
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaAnalysis lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            
            SymbExToSubQuery translator = config.newSymbExToSubQuery(SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, config.metamodel, parentArgumentScope, withSource));

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in a JOIN at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            JPQLQuery<U> returnExpr = (JPQLQuery<U>)PathAnalysisSimplifier
                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), config.getComparisonMethods())
                  .visit(translator, passdown);

            // Create the new query, merging in the analysis of the method
            
            // Check if the subquery is simply a stream of all of a certain entity
            if (isSimpleFrom(returnExpr))
            {
               SelectFromWhere<?> toMerge = (SelectFromWhere<?>)returnExpr;
               SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
               toReturn.froms.add(toMerge.froms.get(0));
               toReturn.cols = new ColumnExpressions<>(createPairReader(sfw.cols.reader, toMerge.cols.reader));
               toReturn.cols.columns.addAll(sfw.cols.columns);
               toReturn.cols.columns.addAll(toMerge.cols.columns);
               return toReturn;
            }
            
            // Handle other types of subqueries
         }
         throw new QueryTransformException("Existing query cannot be transformed further");
      } catch (TypedValueVisitorException e)
      {
         throw new QueryTransformException(e);
      }
   }
   
   protected <U> RowReader<U> createPairReader(RowReader<?> a, RowReader<?> b)
   {
      return TupleRowReader.createReaderForTuple(TupleRowReader.PAIR_CLASS, a, b);
   }

   @Override 
   public String getTransformationTypeCachingTag()
   {
      return JoinTransform.class.getName();
   }
}
