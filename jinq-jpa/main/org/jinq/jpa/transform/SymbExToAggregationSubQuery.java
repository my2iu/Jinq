package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
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

public class SymbExToAggregationSubQuery extends TypedValueVisitor<SymbExPassDown, JPQLQuery<?>, TypedValueVisitorException>
{
   final MetamodelUtil metamodel;
   final SymbExArgumentHandler argHandler;
   final ClassLoader alternateClassLoader; 
   
   SymbExToAggregationSubQuery(MetamodelUtil metamodel, ClassLoader alternateClassLoader, SymbExArgumentHandler argumentHandler)
   {
      this.metamodel = metamodel;
      this.argHandler = argumentHandler;
      this.alternateClassLoader = alternateClassLoader;
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
   
   private boolean isAggregateMethod(MethodSignature sig)
   {
      return sig.equals(MethodChecker.streamSumInt)
            || sig.equals(MethodChecker.streamSumDouble)
            || sig.equals(MethodChecker.streamSumLong)
            || sig.equals(MethodChecker.streamSumBigInteger)
            || sig.equals(MethodChecker.streamSumBigDecimal)
            || sig.equals(MethodChecker.streamMax)
            || sig.equals(MethodChecker.streamMin)
            || sig.equals(MethodChecker.streamAvg);
   }
   
   @Override public JPQLQuery<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
//      if (MetamodelUtil.inQueryStream.equals(sig))
//      {
//         if (!(val.base instanceof TypedValue.ArgValue))
//            throw new TypedValueVisitorException("InQueryStreamSource comes from unknown source");
//         int index = ((TypedValue.ArgValue)val.base).getIndex();
//         if (!argHandler.checkIsInQueryStreamSource(index))
//            throw new TypedValueVisitorException("InQueryStreamSource comes from unknown source");
//         if (!(val.args.get(0) instanceof ConstantValue.ClassConstant))
//            throw new TypedValueVisitorException("Streaming an unknown type");
//         Type type = ((ConstantValue.ClassConstant)val.args.get(0)).val;
//         String entityName = metamodel.entityNameFromClassName(type.getClassName());
//         if (entityName == null)
//            throw new TypedValueVisitorException("Streaming an unknown type");
//         return JPQLQuery.findAllEntities(entityName);
//      }
//      else

      if (isAggregateMethod(sig))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, false);
         JPQLQuery<?> subQuery = val.base.visit(this, passdown);
         
         // Extract the lambda used
         LambdaInfo lambda = null;
         if (val.args.size() > 0)
         {
            if (!(val.args.get(0) instanceof LambdaFactory))
               throw new TypedValueVisitorException("Expecting a lambda factory for aggregate method");
            LambdaFactory lambdaFactory = (LambdaFactory)val.args.get(0);
            try {
               lambda = LambdaInfo.analyzeMethod(metamodel, alternateClassLoader, lambdaFactory.getLambdaMethod(), true);
            } catch (Exception e)
            {
               throw new TypedValueVisitorException("Could not analyze the lambda code", e);
            }
         }
            
            
         
         if (sig.equals(MethodChecker.streamSumInt))
         {
            try {
               AggregateTransform.applyToSubquery(metamodel, AggregateTransform.AggregateType.SUM, subQuery, lambda);
            } catch (QueryTransformException e)
            {
               throw new TypedValueVisitorException("Could not derive an aggregate function for a lambda", e);
            }
         }
         
//         if (lambdaContext.joins == null)
//            throw new TypedValueVisitorException("Need a join handler here for subqueries just in case there's an embedded navigational query: " + val);
/*
         if (sig.equals(TransformationClassAnalyzer.streamSumInt)
               || sig.equals(TransformationClassAnalyzer.streamMax)
               || sig.equals(TransformationClassAnalyzer.streamMin))
         {
            // TODO: do subqueries need to be copied before being passed in here?
            SQLQuery<Integer> newQuery = null;
            if (sig.equals(TransformationClassAnalyzer.streamSumInt))
               newQuery = queryMethodHandler.sumInt(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
            else if (sig.equals(TransformationClassAnalyzer.streamMax))
               newQuery = queryMethodHandler.max(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
            else if (sig.equals(TransformationClassAnalyzer.streamMin))
               newQuery = queryMethodHandler.min(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
            return handleAggregationSubQuery(val, newQuery);
         }
 */
         throw new TypedValueVisitorException("Unhandled aggregate operation");
      }
      return super.virtualMethodCallValue(val, in);
   }
   
   @Override public JPQLQuery<?> staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException 
   {
//      MethodSignature sig = val.getSignature();
//      if (sig.equals(TransformationClassAnalyzer.streamFrom))
//      {
//         JPQLQuery<?> nLink = handlePossibleNNavigationalLink(val.args.get(0), in);
//         if (nLink != null) return nLink;
//      }
      
//      if (sig.equals(TransformationClassAnalyzer.integerValueOf)
//            || sig.equals(TransformationClassAnalyzer.longValueOf)
//            || sig.equals(TransformationClassAnalyzer.doubleValueOf)
//            || sig.equals(TransformationClassAnalyzer.booleanValueOf))
//      {
//         // Integer.valueOf() to be like a cast and assume it's correct
//         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
//         ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
//         return base;
//      }
      return super.staticMethodCallValue(val, in);
   }


}
