package org.jinq.jpa.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.FunctionExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.ScalaTupleRowReader;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;
import org.jinq.jpa.jpqlquery.SimpleRowReader;
import org.jinq.jpa.jpqlquery.SubqueryExpression;

import ch.epfl.labos.iu.orm.queryll2.symbolic.LambdaFactory;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue.StaticMethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.GetFieldValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class ScalaSymbExToColumns extends SymbExToColumns
{
   ScalaSymbExToColumns(JPQLQueryTransformConfiguration config,
         SymbExArgumentHandler argumentHandler)
   {
      super(config, argumentHandler);
   }
   
   @Override public ColumnExpressions<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (ScalaMetamodelUtil.newTuple2.equals(sig)
            || ScalaMetamodelUtil.newTuple3.equals(sig)
            || ScalaMetamodelUtil.newTuple4.equals(sig)
            || ScalaMetamodelUtil.newTuple5.equals(sig)
            || ScalaMetamodelUtil.newTuple8.equals(sig))
      {
         ColumnExpressions<?> [] vals = new ColumnExpressions<?> [val.args.size()];
         // TODO: This is a little wonky passing down isExpectingConditional, but I think it's right for those times you create a tuple with booleans and then extract the booleans later
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         for (int n = 0; n < vals.length; n++)
            vals[n] = val.args.get(n).visit(this, passdown);
         RowReader<?> [] valReaders = new RowReader[vals.length];
         for (int n = 0; n < vals.length; n++)
            valReaders[n] = vals[n].reader;

         ColumnExpressions<?> toReturn = new ColumnExpressions<>(ScalaTupleRowReader.createReaderForTuple(sig.owner, valReaders));
         for (int n = 0; n < vals.length; n++)
            toReturn.columns.addAll(vals[n].columns);
         return toReturn;
      }
      else if (ScalaMetamodelUtil.TUPLE_ACCESSORS.containsKey(sig))
      {
         int idx = ScalaMetamodelUtil.TUPLE_ACCESSORS.get(sig) - 1;
         // TODO: This is a little wonky passing down isExpectingConditional, but I think it's right for those times you create a tuple with booleans and then extract the booleans later
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.base.visit(this, passdown);
         RowReader<?> subreader = ((ScalaTupleRowReader<?>)base.reader).getReaderForIndex(idx);
         ColumnExpressions<?> toReturn = new ColumnExpressions<>(subreader);
         int baseOffset = ((ScalaTupleRowReader<?>)base.reader).getColumnForIndex(idx);
         for (int n = 0; n < subreader.getNumColumns(); n++)
            toReturn.columns.add(base.columns.get(n + baseOffset));
         return toReturn;
      }
      else if (sig.equals(ScalaMetamodelUtil.PREDEF_INT_TO_INTEGER)
            || sig.equals(ScalaMetamodelUtil.PREDEF_LONG_TO_LONG)  
            || sig.equals(ScalaMetamodelUtil.PREDEF_DOUBLE_TO_DOUBLE)  
            || sig.equals(ScalaMetamodelUtil.PREDEF_BOOLEAN_TO_BOOLEAN)
            || sig.equals(ScalaMetamodelUtil.PREDEF_INTEGER_TO_INT)  
            || sig.equals(ScalaMetamodelUtil.PREDEF_LANGLONG_TO_LONG)  
            || sig.equals(ScalaMetamodelUtil.PREDEF_LANGDOUBLE_TO_DOUBLE)  
            || sig.equals(ScalaMetamodelUtil.PREDEF_LANGBOOLEAN_TO_BOOLEAN))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
         return base;
      }
      else if (ScalaMetamodelUtil.isAggregateMethod(sig))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, false);
         
         // Check out what stream we're aggregating
         SymbExToSubQuery translator = config.newSymbExToSubQuery(argHandler, true);
         JPQLQuery<?> subQuery = val.base.visit(translator, passdown);
         
         // Extract the lambda used
         LambdaAnalysis lambda = null;
         if (val.args.size() > 0)
         {
            TypedValue arg = val.args.get(0); 
            if ((arg instanceof LambdaFactory))
            {
               LambdaFactory lambdaFactory = (LambdaFactory)arg;
               try {
                  lambda = LambdaAnalysis.analyzeMethod(config.metamodel, config.alternateClassLoader, config.isObjectEqualsSafe, config.isCollectionContainsSafe, lambdaFactory.getLambdaMethod(), lambdaFactory.getCapturedArgs(), true);
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
            AggregateTransform transform;
            if (sig.equals(ScalaMetamodelUtil.streamSumInt)
                  || sig.equals(ScalaMetamodelUtil.streamSumLong)
                  || sig.equals(ScalaMetamodelUtil.streamSumDouble)
                  || sig.equals(ScalaMetamodelUtil.streamSumBigDecimal)
                  || sig.equals(ScalaMetamodelUtil.streamSumBigInteger))
               transform = new AggregateTransform(config, AggregateTransform.AggregateType.SUM);
            else if (sig.equals(ScalaMetamodelUtil.streamMax))
               transform = new AggregateTransform(config, AggregateTransform.AggregateType.MAX);
            else if (sig.equals(ScalaMetamodelUtil.streamMin))
               transform = new AggregateTransform(config, AggregateTransform.AggregateType.MIN);
            else if (sig.equals(ScalaMetamodelUtil.streamAvg))
               transform = new AggregateTransform(config, AggregateTransform.AggregateType.AVG);
            else if (sig.equals(ScalaMetamodelUtil.streamCount))
               transform = new AggregateTransform(config, AggregateTransform.AggregateType.COUNT);
            else
               throw new TypedValueVisitorException("Unhandled aggregate operation");
            JPQLQuery<?> aggregatedQuery = transform.apply(subQuery, lambda, argHandler); 
            // Return the aggregated columns that we've now calculated
            if (aggregatedQuery.getClass() == SelectOnly.class)
            {
               SelectOnly<?> select = (SelectOnly<?>)aggregatedQuery;
               return select.cols;
            }
            else if (aggregatedQuery.isValidSubquery() && aggregatedQuery instanceof SelectFromWhere) 
            {
               SelectFromWhere<?> sfw = (SelectFromWhere<?>)aggregatedQuery;
               ColumnExpressions<?> toReturn = new ColumnExpressions<>(sfw.cols.reader);
               for (Expression col: sfw.cols.columns)
               {
                  SelectFromWhere<?> oneColQuery = sfw.shallowCopy();
                  oneColQuery.cols = ColumnExpressions.singleColumn(new SimpleRowReader<>(), col);
                  toReturn.columns.add(SubqueryExpression.from(oneColQuery));
               }
               return toReturn;
            }
            else
            {
               throw new TypedValueVisitorException("Unknown subquery type");
            }
         } catch (QueryTransformException e)
         {
            throw new TypedValueVisitorException("Could not derive an aggregate function for a lambda", e);
         }
      }
      else if (sig.equals(ScalaMetamodelUtil.streamGetOnlyValue))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, false);
         
         // Check out what stream we're aggregating
         SymbExToSubQuery translator = config.newSymbExToSubQuery(argHandler, true);
         JPQLQuery<?> subQuery = val.base.visit(translator, passdown);

         if (subQuery.isValidSubquery() && subQuery instanceof SelectFromWhere) 
         {
            SelectFromWhere<?> sfw = (SelectFromWhere<?>)subQuery;
            ColumnExpressions<?> toReturn = new ColumnExpressions<>(sfw.cols.reader);
            for (Expression col: sfw.cols.columns)
            {
               SelectFromWhere<?> oneColQuery = sfw.shallowCopy();
               oneColQuery.cols = ColumnExpressions.singleColumn(new SimpleRowReader<>(), col);
               toReturn.columns.add(SubqueryExpression.from(oneColQuery));
            }
            return toReturn;
         }

         throw new TypedValueVisitorException("Cannot apply getOnlyValue() to the given subquery");
      }
      else if (sig.equals(ScalaMetamodelUtil.STRINGBUILDER_STRING))
      {
         List<ColumnExpressions<?>> concatenatedStrings = new ArrayList<>();
         MethodCallValue.VirtualMethodCallValue baseVal = val;
         while (true)
         {
            if (!(baseVal.base instanceof MethodCallValue.VirtualMethodCallValue))
               throw new TypedValueVisitorException("Unexpected use of StringBuilder");
            baseVal = (MethodCallValue.VirtualMethodCallValue)baseVal.base;
            if (baseVal.getSignature().equals(ScalaMetamodelUtil.NEW_STRINGBUILDER_STRING))
            {
               SymbExPassDown passdown = SymbExPassDown.with(val, false);
               concatenatedStrings.add(baseVal.args.get(0).visit(this, passdown));
               break;
            }
            else if (baseVal.getSignature().equals(ScalaMetamodelUtil.NEW_STRINGBUILDER))
            {
               break;
            }
            else if (baseVal.getSignature().equals(ScalaMetamodelUtil.STRINGBUILDER_APPEND))
            {
               SymbExPassDown passdown = SymbExPassDown.with(val, false);
               concatenatedStrings.add(baseVal.args.get(0).visit(this, passdown));
            }
            else
               throw new TypedValueVisitorException("Unexpected use of StringBuilder");
         }
         
         if (concatenatedStrings.size() == 1)
            return concatenatedStrings.get(0);
         Expression head = concatenatedStrings.get(concatenatedStrings.size() - 1).getOnlyColumn();
         for (int n = concatenatedStrings.size() - 2; n >= 0 ; n--)
            head = FunctionExpression.twoParam("CONCAT", head, concatenatedStrings.get(n).getOnlyColumn());
         return ColumnExpressions.singleColumn(new SimpleRowReader<>(), head);
      }
      else
         return super.virtualMethodCallValue(val, in);
   }

   @Override
   public ColumnExpressions<?> staticMethodCallValue(StaticMethodCallValue val,
         SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (sig.equals(ScalaMetamodelUtil.BOX_TO_INTEGER)
            || sig.equals(ScalaMetamodelUtil.BOX_TO_LONG)
            || sig.equals(ScalaMetamodelUtil.BOX_TO_DOUBLE)
            || sig.equals(ScalaMetamodelUtil.BOX_TO_BOOLEAN)
            || sig.equals(ScalaMetamodelUtil.UNBOX_TO_INTEGER)
            || sig.equals(ScalaMetamodelUtil.UNBOX_TO_LONG)
            || sig.equals(ScalaMetamodelUtil.UNBOX_TO_DOUBLE)
            || sig.equals(ScalaMetamodelUtil.UNBOX_TO_BOOLEAN))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
         return base;
      }
      return super.staticMethodCallValue(val, in);
   }
   
   @Override
   public ColumnExpressions<?> getFieldValue(GetFieldValue val,
         SymbExPassDown in) throws TypedValueVisitorException
   {
      if (val.operand instanceof TypedValue.ThisValue)
      {
         return argHandler.handleThisFieldRead(val.name, val.getType());
      }
      return super.getFieldValue(val, in);
   }
}
