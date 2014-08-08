package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.JPQLQuery;

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
            || sig.equals(MethodChecker.streamAvg)
            || sig.equals(MethodChecker.streamCount);
   }
   
   @Override public JPQLQuery<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
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
            
         try {
            if (sig.equals(MethodChecker.streamSumInt)
                  || sig.equals(MethodChecker.streamSumLong)
                  || sig.equals(MethodChecker.streamSumDouble)
                  || sig.equals(MethodChecker.streamSumBigDecimal)
                  || sig.equals(MethodChecker.streamSumBigInteger))
               return AggregateTransform.applyToSubquery(metamodel, AggregateTransform.AggregateType.SUM, subQuery, lambda);
            else if (sig.equals(MethodChecker.streamMax))
               return AggregateTransform.applyToSubquery(metamodel, AggregateTransform.AggregateType.MAX, subQuery, lambda);
            else if (sig.equals(MethodChecker.streamMin))
               return AggregateTransform.applyToSubquery(metamodel, AggregateTransform.AggregateType.MIN, subQuery, lambda);
            else if (sig.equals(MethodChecker.streamAvg))
               return AggregateTransform.applyToSubquery(metamodel, AggregateTransform.AggregateType.AVG, subQuery, lambda);
            else if (sig.equals(MethodChecker.streamCount))
               return AggregateTransform.applyToSubquery(metamodel, AggregateTransform.AggregateType.COUNT, subQuery, lambda);
            
         } catch (QueryTransformException e)
         {
            throw new TypedValueVisitorException("Could not derive an aggregate function for a lambda", e);
         }
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
