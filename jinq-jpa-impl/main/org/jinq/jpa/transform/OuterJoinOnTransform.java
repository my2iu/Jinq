package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.From;
import org.jinq.jpa.jpqlquery.FromAliasExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.ReadFieldExpression;
import org.jinq.jpa.jpqlquery.RecursiveExpressionVisitor;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.TupleRowReader;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class OuterJoinOnTransform extends JPQLQueryTransform
{
   boolean isExpectingStream;
   boolean isJoinFetch;
   public OuterJoinOnTransform(JPQLQueryTransformConfiguration config, boolean isExpectingStream, boolean isJoinFetch)
   {
      super(config);
      this.isExpectingStream = isExpectingStream;
      this.isJoinFetch = isJoinFetch;
   }

   public OuterJoinOnTransform(JPQLQueryTransformConfiguration config)
   {
      this(config, true, false);
   }

   public OuterJoinOnTransform setIsExpectingStream(boolean isExpectingStream)
   {
      this.isExpectingStream = isExpectingStream;
      return this;
   }
   
   public OuterJoinOnTransform setIsJoinFetch(boolean isJoinFetch)
   {
      this.isJoinFetch = isJoinFetch;
      return this;
   }

   static boolean isChainedLink(Expression links)
   {
      if (links instanceof ReadFieldExpression)
         return ((ReadFieldExpression)links).base instanceof ReadFieldExpression;
      return false;
   }
   
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaAnalysis joinLambda, LambdaAnalysis onLambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            
            SymbExToSubQuery translator = config.newSymbExToSubQuery(SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, joinLambda, config.metamodel, parentArgumentScope, true), isExpectingStream);

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (joinLambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in a JOIN at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            JPQLQuery<U> returnExpr = (JPQLQuery<U>)PathAnalysisSimplifier
                  .simplify(joinLambda.symbolicAnalysis.paths.get(0).getReturnValue(), config.getComparisonMethods(), config.getComparisonStaticMethods(), config.isAllEqualsSafe)
                  .visit(translator, passdown);

            // Create the new query, merging in the analysis of the method
            
            // Check if the subquery is simply a stream of all of a certain entity
            if (JoinTransform.isSimpleFrom(returnExpr))
            {
               SelectFromWhere<?> toMerge = (SelectFromWhere<?>)returnExpr;
               SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
               From from = toMerge.froms.get(0);
               From.FromLeftOuterJoinSettableOn outerJoinFrom;
               if (isJoinFetch)
                  throw new QueryTransformException("Generating queries with LEFT OUTER JOIN FETCH ... ON is currently not supported");
               else
                  outerJoinFrom = From.forLeftOuterJoinOn(from);
               toReturn.froms.add((From)outerJoinFrom);
               OuterJoinTransform.rewriteFromAliases(toMerge, from, (From)outerJoinFrom);
               Expression onExpr = WhereTransform.computeWhereReturnExpr(
                     config, onLambda, sfw, 
                     new OuterJoinOnLambdaArgumentHandler(sfw.cols, toMerge.cols, onLambda, config.metamodel, parentArgumentScope));
               outerJoinFrom.setOn(onExpr);
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
      return OuterJoinOnTransform.class.getName();
   }
}
