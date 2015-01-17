package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.From;
import org.jinq.jpa.jpqlquery.FromAliasExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.ReadFieldExpression;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SimpleRowReader;
import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.LambdaFactory;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExToSubQuery extends TypedValueVisitor<SymbExPassDown, JPQLQuery<?>, TypedValueVisitorException>
{
   final SymbExArgumentHandler argHandler;
   JPQLQueryTransformConfiguration config = new JPQLQueryTransformConfiguration();
   boolean isExpectingStream;

   SymbExToSubQuery(JPQLQueryTransformConfiguration config, SymbExArgumentHandler argumentHandler, boolean isExpectingStream)
   {
      this.config = config;
      this.argHandler = argumentHandler;
      this.isExpectingStream = isExpectingStream;
   }
   
   @Override public JPQLQuery<?> defaultValue(TypedValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled symbolic execution operation: " + val);
   }

   @Override public JPQLQuery<?> argValue(TypedValue.ArgValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      int index = val.getIndex();
      return argHandler.handleSubQueryArg(index, val.getType());
   }

   private boolean isStreamMethod(MethodSignature sig)
   {
      return sig.equals(MethodChecker.streamDistinct)
            || sig.equals(MethodChecker.streamSelect)
            || sig.equals(MethodChecker.streamSelectAll)
            || sig.equals(MethodChecker.streamSelectAllList)
            || sig.equals(MethodChecker.streamWhere)
            || sig.equals(MethodChecker.streamJoin)
            || sig.equals(MethodChecker.streamJoinList);
   }
   
   @Override public JPQLQuery<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (isExpectingStream)
      {
         if (MetamodelUtil.inQueryStream.equals(sig))
         {
            return handleInQueryStreamSource(val.base, val.args.get(0));
         }
         else if (isStreamMethod(sig))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            
            // Check out what stream we're aggregating
            JPQLQuery<?> subQuery = val.base.visit(this, passdown);
            
            // Extract the lambda used
            LambdaAnalysis lambda = null;
            if (val.args.size() > 0)
            {
               if (!(val.args.get(0) instanceof LambdaFactory))
                  throw new TypedValueVisitorException("Expecting a lambda factory for aggregate method");
               LambdaFactory lambdaFactory = (LambdaFactory)val.args.get(0);
               try {
                  lambda = LambdaAnalysis.analyzeMethod(config.metamodel, config.alternateClassLoader, config.isObjectEqualsSafe, config.isCollectionContainsSafe,  
                        lambdaFactory.getLambdaMethod(), lambdaFactory.getCapturedArgs(), true);
               } catch (Exception e)
               {
                  throw new TypedValueVisitorException("Could not analyze the lambda code", e);
               }
            }

            try {
               JPQLQuery<?> transformedQuery;
               if (sig.equals(MethodChecker.streamDistinct))
               {
                  DistinctTransform transform = new DistinctTransform(config);
                  transformedQuery = transform.apply(subQuery, argHandler); 
               }
               else if (sig.equals(MethodChecker.streamSelect))
               {
                  SelectTransform transform = new SelectTransform(config, false);
                  transformedQuery = transform.apply(subQuery, lambda, argHandler); 
               }
               else if (sig.equals(MethodChecker.streamSelectAll))
               {
                  JoinTransform transform = new JoinTransform(config).setJoinAsPairs(false);
                  transformedQuery = transform.apply(subQuery, lambda, argHandler); 
               }
               else if (sig.equals(MethodChecker.streamSelectAllList))
               {
                  JoinTransform transform = new JoinTransform(config).setJoinAsPairs(false).setIsExpectingStream(false);
                  transformedQuery = transform.apply(subQuery, lambda, argHandler); 
               }
               else if (sig.equals(MethodChecker.streamWhere))
               {
                  WhereTransform transform = new WhereTransform(config, false);
                  transformedQuery = transform.apply(subQuery, lambda, argHandler); 
               }
               else if (sig.equals(MethodChecker.streamJoin))
               {
                  JoinTransform transform = new JoinTransform(config);
                  transformedQuery = transform.apply(subQuery, lambda, argHandler); 
               }
               else if (sig.equals(MethodChecker.streamJoinList))
               {
                  JoinTransform transform = new JoinTransform(config).setIsExpectingStream(false);
                  transformedQuery = transform.apply(subQuery, lambda, argHandler); 
               }
               else
                  throw new TypedValueVisitorException("Unknown stream operation: " + sig);

               return transformedQuery;
            } 
            catch (QueryTransformException e)
            {
               throw new TypedValueVisitorException("Subquery could not be transformed.", e);
            }
//            // Return the aggregated columns that we've now calculated
//            if (transformedQuery.getClass() == SelectOnly.class)
//            {
//               SelectOnly<?> select = (SelectOnly<?>)transformedQuery;
//               return select.cols;
//            }
//            else if (transformedQuery.isValidSubquery() && transformedQuery instanceof SelectFromWhere) 
//            {
//               SelectFromWhere<?> sfw = (SelectFromWhere<?>)transformedQuery;
//               ColumnExpressions<?> toReturn = new ColumnExpressions<>(sfw.cols.reader);
//               for (Expression col: sfw.cols.columns)
//               {
//                  SelectFromWhere<?> oneColQuery = sfw.shallowCopy();
//                  oneColQuery.cols = ColumnExpressions.singleColumn(new SimpleRowReader<>(), col);
//                  toReturn.columns.add(SubqueryExpression.from(oneColQuery));
//               }
//               return toReturn;
//            }
//            else
//            {
//               throw new TypedValueVisitorException("Unknown subquery type");
//            }

         }
      }
      else
      {
         JPQLQuery<?> nLink = handlePossibleNavigationalLink(val, true, in);
         if (nLink != null) return nLink;
      }
      return super.virtualMethodCallValue(val, in);
   }

   protected JPQLQuery<?> handleInQueryStreamSource(
         TypedValue methodBase, TypedValue entity) 
         throws TypedValueVisitorException
   {
      if (!(methodBase instanceof TypedValue.ArgValue))
         throw new TypedValueVisitorException("InQueryStreamSource comes from unknown source");
      int index = ((TypedValue.ArgValue)methodBase).getIndex();
      if (!argHandler.checkIsInQueryStreamSource(index))
         throw new TypedValueVisitorException("InQueryStreamSource comes from unknown source");
      if (!(entity instanceof ConstantValue.ClassConstant))
         throw new TypedValueVisitorException("Streaming an unknown type");
      Type type = ((ConstantValue.ClassConstant)entity).val;
      String entityName = config.metamodel.entityNameFromClassName(type.getClassName());
      if (entityName == null)
         throw new TypedValueVisitorException("Streaming an unknown type");
      return JPQLQuery.findAllEntities(entityName);
   }

   /**
    * if unknownVal is not a handled navigational link, null will be 
    * returned. Otherwise, a query representing the link will be returned
    */
   protected JPQLQuery<?> handlePossibleNavigationalLink(TypedValue unknownVal, boolean expectingPluralLink, SymbExPassDown in) throws TypedValueVisitorException
   {
      // Figure out if it's an 1:N or N:M navigational link
      if (unknownVal instanceof MethodCallValue.VirtualMethodCallValue)
      {
         MethodCallValue.VirtualMethodCallValue val = (MethodCallValue.VirtualMethodCallValue)unknownVal;
         MethodSignature sig = val.getSignature();
         if ((expectingPluralLink && config.metamodel.isPluralAttributeLinkMethod(sig))
               || (!expectingPluralLink && config.metamodel.isSingularAttributeFieldMethod(sig) && config.metamodel.isFieldMethodAssociationType(sig))) 
         {
            String linkName = expectingPluralLink ? 
                  config.metamodel.nLinkMethodToLinkName(sig) : config.metamodel.fieldMethodToFieldName(sig);
            SymbExToColumns translator = config.newSymbExToColumns(argHandler);
            
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            ColumnExpressions<?> nLinkBase = val.base.visit(translator, passdown);
            // Traverse the chain, it should be a FromAlias at its base with 
            // possible field accesses around it
            if (nLinkBase.isSingleColumn())
            {
               Expression expr = nLinkBase.getOnlyColumn(); 
               if (!(expr instanceof FromAliasExpression
                     || expr instanceof ReadFieldExpression))
                  return null;
            }
            
            // Create the query
            SelectFromWhere<?> query = new SelectFromWhere<>();
            From from = From.forNavigationalLinks(
                  new ReadFieldExpression(nLinkBase.getOnlyColumn(), linkName));
            query.cols = ColumnExpressions.singleColumn(
                  new SimpleRowReader<>(), new FromAliasExpression(from));
            query.froms.add(from);
            return query;
         }
      }
      return null;
   }
   
   @Override public JPQLQuery<?> staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException 
   {
      MethodSignature sig = val.getSignature();
      if (sig.equals(TransformationClassAnalyzer.streamFrom))
      {
         JPQLQuery<?> nLink = handlePossibleNavigationalLink(val.args.get(0), true, in);
         if (nLink != null) return nLink;
      }
      else if (sig.equals(TransformationClassAnalyzer.streamOf))
      {
         JPQLQuery<?> nLink = handlePossibleNavigationalLink(val.args.get(0), false, in);
         if (nLink != null) return nLink;
      }
      return super.staticMethodCallValue(val, in);
   }


}
