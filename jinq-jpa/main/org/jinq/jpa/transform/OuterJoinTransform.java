package org.jinq.jpa.transform;

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

public class OuterJoinTransform extends JPQLOneLambdaQueryTransform
{
   boolean isExpectingStream;
   boolean isJoinFetch;
   public OuterJoinTransform(JPQLQueryTransformConfiguration config, boolean isExpectingStream, boolean isJoinFetch)
   {
      super(config);
      this.isExpectingStream = isExpectingStream;
      this.isJoinFetch = isJoinFetch;
   }

   public OuterJoinTransform(JPQLQueryTransformConfiguration config)
   {
      this(config, true, false);
   }

   public OuterJoinTransform setIsExpectingStream(boolean isExpectingStream)
   {
      this.isExpectingStream = isExpectingStream;
      return this;
   }
   
   public OuterJoinTransform setIsJoinFetch(boolean isJoinFetch)
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
   

   static boolean isLeftOuterJoinCompatible(SelectFromWhere<?> toMerge)
   {
      From from = toMerge.froms.get(0);
      if (!(from instanceof From.FromNavigationalLinks))
         return false;
      Expression navLink = ((From.FromNavigationalLinks)from).links;
      while (!(navLink instanceof FromAliasExpression))
      {
         if (!(navLink instanceof ReadFieldExpression))
            return false;
         navLink = ((ReadFieldExpression)navLink).base;
      }
      return true;
   }
   
   static void rewriteFromAliases(SelectFromWhere<?> toMerge, From oldFrom, From newFrom)
   {
      for (Expression expr: toMerge.cols.columns)
      {
         expr.visit(new RecursiveExpressionVisitor() {
            @Override
            public void visitFromAlias(FromAliasExpression expr)
            {
               if (expr.from == oldFrom)
                  expr.from = newFrom;
               super.visitFromAlias(expr);
            }
         });
      }
   }
   
   @Override
   public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaAnalysis lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException
   {
      try  {
         if (query.isSelectFromWhere())
         {
            SelectFromWhere<V> sfw = (SelectFromWhere<V>)query;
            
            SymbExToSubQuery translator = config.newSymbExToSubQuery(SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, config.metamodel, parentArgumentScope, false), isExpectingStream);

            // TODO: Handle this case by translating things to use SELECT CASE 
            if (lambda.symbolicAnalysis.paths.size() > 1) 
               throw new QueryTransformException("Can only handle a single path in a JOIN at the moment");
            
            SymbExPassDown passdown = SymbExPassDown.with(null, false);
            JPQLQuery<U> returnExpr = (JPQLQuery<U>)PathAnalysisSimplifier
                  .simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), config.getComparisonMethods(), config.getComparisonStaticMethods(), config.isAllEqualsSafe)
                  .visit(translator, passdown);

            // Create the new query, merging in the analysis of the method
            
            // Check if the subquery is simply a stream of all of a certain entity
            if (JoinTransform.isSimpleFrom(returnExpr))
            {
               SelectFromWhere<?> toMerge = (SelectFromWhere<?>)returnExpr;
               SelectFromWhere<U> toReturn = (SelectFromWhere<U>)sfw.shallowCopy();
               From from = toMerge.froms.get(0);
               if (!isLeftOuterJoinCompatible(toMerge))
                  throw new QueryTransformException("Left outer join must be applied to a navigational link");
               From.FromNavigationalLinksGeneric outerJoinFrom;
               if (isJoinFetch)
                  outerJoinFrom = From.forNavigationalLinksLeftOuterJoinFetch((From.FromNavigationalLinks)from);
               else
                  outerJoinFrom = From.forNavigationalLinksLeftOuterJoin((From.FromNavigationalLinks)from);
               if (!isJoinFetch && isChainedLink(outerJoinFrom.links))
               {
                  // The left outer join only applies to the end part of
                  // links. So we'll pull off the earlier part of the chain
                  // and make them a separate alias.
                  ReadFieldExpression outerLinks = (ReadFieldExpression)outerJoinFrom.links;
                  From baseFrom = From.forNavigationalLinks(outerLinks.base);
                  toReturn.froms.add(baseFrom);
                  outerJoinFrom.links = new ReadFieldExpression(new FromAliasExpression(baseFrom), outerLinks.field);
               }
               toReturn.froms.add(outerJoinFrom);
               rewriteFromAliases(toMerge, from, outerJoinFrom);
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
      return OuterJoinTransform.class.getName();
   }
}
