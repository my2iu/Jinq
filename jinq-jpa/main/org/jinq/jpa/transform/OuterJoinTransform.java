package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.From;
import org.jinq.jpa.jpqlquery.From.FromNavigationalLinksLeftOuterJoin;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.TupleRowReader;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class OuterJoinTransform extends JPQLOneLambdaQueryTransform
{
   public OuterJoinTransform(MetamodelUtil metamodel, ClassLoader alternateClassLoader)
   {
      super(metamodel, alternateClassLoader);
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            
            SymbExToSubQuery translator = new SymbExToSubQuery(metamodel, alternateClassLoader, 
                  SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, metamodel, null, false));

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in a JOIN at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            JPQLQuery<U> returnExpr = (JPQLQuery<U>)PathAnalysisSimplifier
                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), metamodel.comparisonMethods)
                  .visit(translator, passdown);

            // Create the new query, merging in the analysis of the method
            
            // Check if the subquery is simply a stream of all of a certain entity
            if (JoinTransform.isSimpleFrom(returnExpr))
            {
               SelectFromWhere<?> toMerge = (SelectFromWhere<?>)returnExpr;
               SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
               From from = toMerge.froms.get(0);
               if (!(from instanceof From.FromNavigationalLinks))
                  throw new QueryTransformException("Left outer join must be applied to a navigational link");
               From.FromNavigationalLinksLeftOuterJoin outerJoinFrom = From.forNavigationalLinksLeftOuterJoin((From.FromNavigationalLinks)from);
               toReturn.froms.add(outerJoinFrom);
               toReturn.cols = new ColumnExpressions<>(TupleRowReader.createReaderForTuple(TupleRowReader.PAIR_CLASS, sfw.cols.reader, toMerge.cols.reader));
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

}
