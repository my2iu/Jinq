package org.jinq.jpa.transform;

import java.util.Map;

import org.jinq.jpa.jpqlquery.JPQLQuery;

import ch.epfl.labos.iu.orm.queryll2.symbolic.LambdaFactory;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class ScalaSymbExToSubQuery extends SymbExToSubQuery
{
   ScalaSymbExToSubQuery(JPQLQueryTransformConfiguration config,
         SymbExArgumentHandler argumentHandler)
   {
      super(config, argumentHandler, true);
   }


   @Override public JPQLQuery<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (ScalaMetamodelUtil.INQUERYSTREAMSOURCE_STREAM.equals(sig))
      {
         return handleInQueryStreamSource(val.base, val.args.get(0));
      }
      else if (ScalaMetamodelUtil.ITERABLE_TO_JINQ.equals(sig))
      {
         JPQLQuery<?> nLink = handlePossibleNavigationalLink(val.args.get(0), true, in);
         if (nLink != null) return nLink;
      }
      else if (ScalaMetamodelUtil.STREAM_OF.equals(sig))
      {
         JPQLQuery<?> nLink = handlePossibleNavigationalLink(val.args.get(0), false, in);
         if (nLink != null) return nLink;
      }
      else if (ScalaMetamodelUtil.isStreamMethod(sig))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, false);
         
         // Check out what stream we're aggregating
         JPQLQuery<?> subQuery = val.base.visit(this, passdown);
         
         // Extract the lambda used
         LambdaAnalysis lambda = null;
         if (val.args.size() > 0)
         {
            TypedValue arg = val.args.get(0);
            if (arg instanceof LambdaFactory)
            {
               LambdaFactory lambdaFactory = (LambdaFactory)arg;
               try {
                  lambda = LambdaAnalysis.analyzeMethod(config.metamodel, config.alternateClassLoader, config.isObjectEqualsSafe, config.isCollectionContainsSafe,  
                        lambdaFactory.getLambdaMethod(), lambdaFactory.getCapturedArgs(), true);
               } catch (Exception e)
               {
                  throw new TypedValueVisitorException("Could not analyze the lambda code", e);
               }
            }
            else if (arg instanceof MethodCallValue.VirtualMethodCallValue && ((MethodCallValue.VirtualMethodCallValue)arg).isConstructor())
            {
               MethodCallValue.VirtualMethodCallValue lambdaConstructor = (MethodCallValue.VirtualMethodCallValue)arg;
               try {
                  Map<String, TypedValue> indirectParamMapping = config.findLambdaAsClassConstructorParameters(lambdaConstructor.getSignature(), lambdaConstructor.args);
                  lambda = LambdaAnalysis.analyzeClassAsLambda(config.metamodel, config.alternateClassLoader, config.isObjectEqualsSafe, config.isCollectionContainsSafe, new LambdaAnalysis.LambdaAsClassAnalysisConfig(), lambdaConstructor.getSignature().getOwnerType().getClassName(), indirectParamMapping, true);
               } catch (Exception e)
               {
                  throw new TypedValueVisitorException("Could not analyze the lambda code", e);
               }
            }
            else
               throw new TypedValueVisitorException("Expecting a lambda factory for aggregate method");
         }

         try {
            JPQLQuery<?> transformedQuery;
            if (sig.equals(ScalaMetamodelUtil.streamDistinct))
            {
               DistinctTransform transform = new DistinctTransform(config);
               transformedQuery = transform.apply(subQuery, argHandler); 
            }
            else if (sig.equals(ScalaMetamodelUtil.streamSelect))
            {
               SelectTransform transform = new SelectTransform(config, false);
               transformedQuery = transform.apply(subQuery, lambda, argHandler); 
            }
            else if (sig.equals(ScalaMetamodelUtil.streamWhere))
            {
               WhereTransform transform = new WhereTransform(config, false);
               transformedQuery = transform.apply(subQuery, lambda, argHandler); 
            }
            else if (sig.equals(ScalaMetamodelUtil.streamJoin))
            {
               JoinTransform transform = new ScalaJoinTransform(config, false, true);
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
      }
      return super.virtualMethodCallValue(val, in);
   }
}
