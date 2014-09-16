package org.jinq.jpa.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.Expression;
import org.jinq.jpa.jpqlquery.FunctionExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.ReadFieldExpression;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.jpqlquery.SelectOnly;
import org.jinq.jpa.jpqlquery.SimpleRowReader;
import org.jinq.jpa.jpqlquery.SubqueryExpression;
import org.jinq.jpa.jpqlquery.TupleRowReader;
import org.jinq.jpa.jpqlquery.UnaryExpression;
import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue.NullConstant;
import ch.epfl.labos.iu.orm.queryll2.symbolic.LambdaFactory;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExToColumns extends TypedValueVisitor<SymbExPassDown, ColumnExpressions<?>, TypedValueVisitorException>
{
   final MetamodelUtil metamodel;
   final SymbExArgumentHandler argHandler;
   final ClassLoader alternateClassLoader; 

   SymbExToColumns(MetamodelUtil metamodel, ClassLoader alternateClassLoader, SymbExArgumentHandler argumentHandler)
   {
      this.metamodel = metamodel;
      this.argHandler = argumentHandler;
      this.alternateClassLoader = alternateClassLoader;
   }
   
   @Override public ColumnExpressions<?> defaultValue(TypedValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled symbolic execution operation: " + val);
   }

   @Override public ColumnExpressions<?> argValue(TypedValue.ArgValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      int index = val.getIndex();
      return argHandler.handleArg(index, val.getType());
   }
   
   @Override public ColumnExpressions<?> booleanConstantValue(ConstantValue.BooleanConstant val, SymbExPassDown in) throws TypedValueVisitorException
   {
      if (in.isExpectingConditional)
      {
         return ColumnExpressions.singleColumn(new SimpleRowReader<Integer>(),
               new ConstantExpression(val.val ? "(1=1)" : "(1!=1)")); 
      }
      else
      {
         return ColumnExpressions.singleColumn(new SimpleRowReader<Integer>(),
               new ConstantExpression(val.val ? "TRUE" : "FALSE")); 
      }
   }

   @Override public ColumnExpressions<?> integerConstantValue(ConstantValue.IntegerConstant val, SymbExPassDown in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<Integer>(),
            new ConstantExpression(Integer.toString(val.val))); 
   }
   
   @Override public ColumnExpressions<?> longConstantValue(ConstantValue.LongConstant val, SymbExPassDown in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<Long>(),
            new ConstantExpression(Long.toString(val.val))); 
   }

   @Override public ColumnExpressions<?> doubleConstantValue(ConstantValue.DoubleConstant val, SymbExPassDown in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<Double>(),
            new ConstantExpression(Double.toString(val.val))); 
   }

   @Override public ColumnExpressions<?> stringConstantValue(ConstantValue.StringConstant val, SymbExPassDown in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<String>(),
            new ConstantExpression("'"+ val.val.replaceAll("'", "''") +"'")); 
   }
   
   @Override public ColumnExpressions<?> nullConstantValue(NullConstant val, SymbExPassDown in) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unexpected NULL value");
   }
   
   @Override public ColumnExpressions<?> unaryMathOpValue(TypedValue.UnaryMathOpValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      SymbExPassDown passdown = SymbExPassDown.with(val, false);
      ColumnExpressions<?> left = val.operand.visit(this, passdown);
      return ColumnExpressions.singleColumn(left.reader,
            UnaryExpression.prefix(val.op.getOpString(), left.getOnlyColumn())); 
   }

   @Override public ColumnExpressions<?> notOpValue(TypedValue.NotValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      SymbExPassDown passdown = SymbExPassDown.with(val, true);
      ColumnExpressions<?> left = val.operand.visit(this, passdown);
      return ColumnExpressions.singleColumn(left.reader,
            UnaryExpression.prefix("NOT", left.getOnlyColumn())); 
   }

   @Override public ColumnExpressions<?> getStaticFieldValue(TypedValue.GetStaticFieldValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      // Check if we're just reading an enum constant
      if (metamodel.isKnownEnumType(val.owner))
      {
         String enumFullName = metamodel.getFullEnumConstantName(val.owner, val.name);
         if (enumFullName != null)
            return ColumnExpressions.singleColumn(new SimpleRowReader<Enum<?>>(),
                  new ConstantExpression(enumFullName)); 
      }
      return defaultValue(val, in);
   }

   @Override public ColumnExpressions<?> castValue(TypedValue.CastValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      // We need to handle casts between primitive types carefully 
      // because JPQL doesn't really support them directly
      if (val.isPrimitive())
      {
         throw new TypedValueVisitorException("Casts of primitive values are not support in JPQL");
      }
      // TODO: Check if cast is consistent with the reader
//      SQLColumnValues toReturn = val.operand.visit(this, in);
//      if (!toReturn.reader.isCastConsistent(val.getType().getInternalName()))
//         throw new TypedValueVisitorException("Attempting to cast to an inconsistent type");
      return val.operand.visit(this, SymbExPassDown.with(val, in.isExpectingConditional));
   }

   private boolean isWideningCast(TypedValue val)
   {
      if (val instanceof TypedValue.CastValue)
      {
         TypedValue.CastValue castedVal = (TypedValue.CastValue)val;
         Type toType = castedVal.getType();
         Type fromType = castedVal.operand.getType();
         if (!numericPromotionPriority.containsKey(fromType)) return false;
         if (!numericPromotionPriority.containsKey(toType)) return false;
         if (numericPromotionPriority.get(toType) > numericPromotionPriority.get(fromType))
            return true;
      }
      else if (val instanceof MethodCallValue.VirtualMethodCallValue)
      {
         MethodCallValue methodCall = (MethodCallValue.VirtualMethodCallValue)val;
         MethodSignature sig = methodCall.getSignature();
         if (sig.equals(TransformationClassAnalyzer.newBigDecimalLong)
               || sig.equals(TransformationClassAnalyzer.newBigDecimalInt)
               || sig.equals(TransformationClassAnalyzer.newBigDecimalBigInteger))
         {
            return true;
         }
         else if (sig.equals(TransformationClassAnalyzer.bigDecimalDoubleValue)
               || sig.equals(TransformationClassAnalyzer.bigIntegerDoubleValue))
         {
            return true;
         }

      }
      else if (val instanceof MethodCallValue.StaticMethodCallValue)
      {
         MethodCallValue methodCall = (MethodCallValue.StaticMethodCallValue)val;
         MethodSignature sig = methodCall.getSignature();
         if (sig.equals(TransformationClassAnalyzer.bigIntegerValueOfLong))
         {
            return true;
         }
      }
      return false;
   }

   private TypedValue skipWideningCast(TypedValue val) throws TypedValueVisitorException
   {
      if (!isWideningCast(val)) return val;
      if (val instanceof TypedValue.CastValue)
      {
         TypedValue.CastValue castedVal = (TypedValue.CastValue)val;
         return skipWideningCast(castedVal.operand);
      }
      else if (val instanceof MethodCallValue.VirtualMethodCallValue)
      {
         MethodCallValue.VirtualMethodCallValue methodCall = (MethodCallValue.VirtualMethodCallValue)val;
         MethodSignature sig = methodCall.getSignature();
         if (sig.equals(TransformationClassAnalyzer.newBigDecimalLong)
               || sig.equals(TransformationClassAnalyzer.newBigDecimalInt)
               || sig.equals(TransformationClassAnalyzer.newBigDecimalBigInteger))
         {
            return skipWideningCast(methodCall.args.get(0));
         }
         else if (sig.equals(TransformationClassAnalyzer.bigDecimalDoubleValue)
               || sig.equals(TransformationClassAnalyzer.bigIntegerDoubleValue))
         {
            return skipWideningCast(methodCall.base);
         }
      }
      else if (val instanceof MethodCallValue.StaticMethodCallValue)
      {
         MethodCallValue methodCall = (MethodCallValue.StaticMethodCallValue)val;
         MethodSignature sig = methodCall.getSignature();
         if (sig.equals(TransformationClassAnalyzer.bigIntegerValueOfLong))
         {
            return skipWideningCast(methodCall.args.get(0));
         }
      }
      throw new IllegalArgumentException("Cannot skip an unknown widening cast type");
   }

   private <U> ColumnExpressions<U> binaryOpWithNull(String opString, TypedValue leftVal, TypedValue rightVal, SymbExPassDown passdown) throws TypedValueVisitorException
   {
      if (!("=".equals(opString) || "<>".equals(opString)))
         throw new TypedValueVisitorException("Unhandled operation involving NULL");
      if (leftVal instanceof ConstantValue.NullConstant && rightVal instanceof ConstantValue.NullConstant)
         throw new TypedValueVisitorException("Cannot handle comparisons involving two NULLs");
      TypedValue operandVal;
      if (leftVal instanceof ConstantValue.NullConstant)
         operandVal = rightVal;
      else
         operandVal = leftVal;
      ColumnExpressions<U> operand = (ColumnExpressions<U>)operandVal.visit(this, passdown);
      if ("=".equals(opString))
         return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
               UnaryExpression.postfix("IS NULL", operand.getOnlyColumn())); 
      else
         return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
               UnaryExpression.postfix("IS NOT NULL", operand.getOnlyColumn())); 
   }
   
   private <U> ColumnExpressions<U> binaryOpWithNumericPromotion(String opString, TypedValue leftVal, TypedValue rightVal, SymbExPassDown passdown) throws TypedValueVisitorException
   {
      boolean isFinalTypeFromLeft = true;
      // Handle operations with NULL separately
      if (leftVal instanceof ConstantValue.NullConstant || rightVal instanceof ConstantValue.NullConstant)
         return binaryOpWithNull(opString, leftVal, rightVal, passdown);
      // Check if we have a valid numeric promotion (i.e. one side has a widening cast
      // to match the type of the other side).
      assert(leftVal.getType().equals(rightVal.getType()));
      if (isWideningCast(leftVal))
      {
         if (!isWideningCast(rightVal))
         {
            leftVal = skipWideningCast(leftVal);
            isFinalTypeFromLeft = false;
         }
      }
      else if (isWideningCast(rightVal))
      {
         rightVal = skipWideningCast(rightVal);
      }
      // Actually translate the expressions now
      ColumnExpressions<U> left = (ColumnExpressions<U>)leftVal.visit(this, passdown);
      ColumnExpressions<U> right = (ColumnExpressions<U>)rightVal.visit(this, passdown);
      return ColumnExpressions.singleColumn(isFinalTypeFromLeft ? left.reader : right.reader,
            new BinaryExpression(opString, left.getOnlyColumn(), right.getOnlyColumn())); 
   }
   
   @Override public ColumnExpressions<?> mathOpValue(TypedValue.MathOpValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      if (val.op == TypedValue.MathOpValue.Op.cmp)
         throw new TypedValueVisitorException("cmp operator was not converted to a boolean operator");
      if (val.op == TypedValue.MathOpValue.Op.mod)
      {
         if (val.left.getType().equals(Type.INT_TYPE) || val.right.getType().equals(Type.INT_TYPE))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            ColumnExpressions<?> left = (ColumnExpressions<?>)val.left.visit(this, passdown);
            ColumnExpressions<?> right = (ColumnExpressions<?>)val.right.visit(this, passdown);
            return ColumnExpressions.singleColumn(left.reader,
                  FunctionExpression.twoParam("MOD", left.getOnlyColumn(), right.getOnlyColumn()));
         }
         throw new TypedValueVisitorException("mod operator cannot be used for the given types.");
      }
      SymbExPassDown passdown = SymbExPassDown.with(val, false);
      return binaryOpWithNumericPromotion(val.sqlOpString(), val.left, val.right, passdown);
   }

   @Override public ColumnExpressions<?> comparisonOpValue(TypedValue.ComparisonValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      SymbExPassDown passdown = SymbExPassDown.with(val, false);
      return binaryOpWithNumericPromotion(val.sqlOpString(), val.left, val.right, passdown);
//      if (val.left.getType() == Type.BOOLEAN_TYPE
//            || val.right.getType() == Type.BOOLEAN_TYPE)
//      {
//         // TODO: These simplifications should be put into a separate
//         // optimization step, maybe?
//         if (val.left instanceof ConstantValue.IntegerConstant
//               && ((ConstantValue.IntegerConstant)val.left).val == 0)
//         {
//            left = new SQLColumnValues(new SQLReader.BooleanSQLReader());
//            left.columns[0] = new SQLFragment("FALSE");
//         }
//         if (val.right instanceof ConstantValue.IntegerConstant
//               && ((ConstantValue.IntegerConstant)val.right).val == 0)
//         {
//            right = new SQLColumnValues(new SQLReader.BooleanSQLReader());
//            right.columns[0] = new SQLFragment("FALSE");
//         }
//      }
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
   
   @Override public ColumnExpressions<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (TransformationClassAnalyzer.newPair.equals(sig)
            || TransformationClassAnalyzer.newTuple3.equals(sig)
            || TransformationClassAnalyzer.newTuple4.equals(sig)
            || TransformationClassAnalyzer.newTuple5.equals(sig)
            || TransformationClassAnalyzer.newTuple8.equals(sig))
      {
         ColumnExpressions<?> [] vals = new ColumnExpressions<?> [val.args.size()];
         // TODO: This is a little wonky passing down isExpectingConditional, but I think it's right for those times you create a tuple with booleans and then extract the booleans later
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         for (int n = 0; n < vals.length; n++)
            vals[n] = val.args.get(n).visit(this, passdown);
         RowReader<?> [] valReaders = new RowReader[vals.length];
         for (int n = 0; n < vals.length; n++)
            valReaders[n] = vals[n].reader;

         ColumnExpressions<?> toReturn = new ColumnExpressions<>(TupleRowReader.createReaderForTuple(sig.owner, valReaders));
         for (int n = 0; n < vals.length; n++)
            toReturn.columns.addAll(vals[n].columns);
         return toReturn;
      }
      else if (metamodel.isSingularAttributeFieldMethod(sig))
      {
         String fieldName = metamodel.fieldMethodToFieldName(sig);
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.base.visit(this, passdown);
         if (in.isExpectingConditional &&
               (sig.getReturnType().equals(Type.BOOLEAN_TYPE) 
               || sig.getReturnType().equals(Type.getObjectType("java/lang/Boolean"))))
         {
            return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
                  new BinaryExpression("=", new ReadFieldExpression(base.getOnlyColumn(), fieldName), new ConstantExpression("TRUE"))); 
         }
         return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
               new ReadFieldExpression(base.getOnlyColumn(), fieldName)); 
//         SQLColumnValues sql = new SQLColumnValues(base.reader.getReaderForField(fieldName));
//         for (int n = 0; n < sql.reader.getNumColumns(); n++)
//            sql.columns[n] = base.columns[base.reader.getColumnForField(fieldName) + n];
      }
      else if (MetamodelUtil.TUPLE_ACCESSORS.containsKey(sig))
      {
         int idx = MetamodelUtil.TUPLE_ACCESSORS.get(sig) - 1;
         // TODO: This is a little wonky passing down isExpectingConditional, but I think it's right for those times you create a tuple with booleans and then extract the booleans later
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.base.visit(this, passdown);
         RowReader<?> subreader = ((TupleRowReader<?>)base.reader).getReaderForIndex(idx);
         ColumnExpressions<?> toReturn = new ColumnExpressions<>(subreader);
         int baseOffset = ((TupleRowReader<?>)base.reader).getColumnForIndex(idx);
         for (int n = 0; n < subreader.getNumColumns(); n++)
            toReturn.columns.add(base.columns.get(n + baseOffset));
         return toReturn;
      }
      else if (sig.equals(TransformationClassAnalyzer.integerIntValue)
            || sig.equals(TransformationClassAnalyzer.longLongValue)
            || sig.equals(TransformationClassAnalyzer.doubleDoubleValue)
            || sig.equals(TransformationClassAnalyzer.booleanBooleanValue))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.base.visit(this, passdown);
         return base;
      }
      else if (sig.equals(TransformationClassAnalyzer.newBigDecimalLong)
            || sig.equals(TransformationClassAnalyzer.newBigDecimalDouble)
            || sig.equals(TransformationClassAnalyzer.newBigDecimalInt)
            || sig.equals(TransformationClassAnalyzer.newBigDecimalBigInteger))
      {
         throw new TypedValueVisitorException("New BigDecimals can only be created in the context of numeric promotion");
      }
      else if (isAggregateMethod(sig))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, false);
         
         // Check out what stream we're aggregating
         SymbExToSubQuery translator = new SymbExToSubQuery(metamodel, alternateClassLoader,
               argHandler);
         JPQLQuery<?> subQuery = val.base.visit(translator, passdown);
         
         // Extract the lambda used
         LambdaInfo lambda = null;
         if (val.args.size() > 0)
         {
            if (!(val.args.get(0) instanceof LambdaFactory))
               throw new TypedValueVisitorException("Expecting a lambda factory for aggregate method");
            LambdaFactory lambdaFactory = (LambdaFactory)val.args.get(0);
            try {
               lambda = LambdaInfo.analyzeMethod(metamodel, alternateClassLoader, lambdaFactory.getLambdaMethod(), lambdaFactory.getCapturedArgs(), true);
            } catch (Exception e)
            {
               throw new TypedValueVisitorException("Could not analyze the lambda code", e);
            }
         }
            
         try {
            AggregateTransform transform;
            if (sig.equals(MethodChecker.streamSumInt)
                  || sig.equals(MethodChecker.streamSumLong)
                  || sig.equals(MethodChecker.streamSumDouble)
                  || sig.equals(MethodChecker.streamSumBigDecimal)
                  || sig.equals(MethodChecker.streamSumBigInteger))
               transform = new AggregateTransform(metamodel, alternateClassLoader, AggregateTransform.AggregateType.SUM);
            else if (sig.equals(MethodChecker.streamMax))
               transform = new AggregateTransform(metamodel, alternateClassLoader, AggregateTransform.AggregateType.MAX);
            else if (sig.equals(MethodChecker.streamMin))
               transform = new AggregateTransform(metamodel, alternateClassLoader, AggregateTransform.AggregateType.MIN);
            else if (sig.equals(MethodChecker.streamAvg))
               transform = new AggregateTransform(metamodel, alternateClassLoader, AggregateTransform.AggregateType.AVG);
            else if (sig.equals(MethodChecker.streamCount))
               transform = new AggregateTransform(metamodel, alternateClassLoader, AggregateTransform.AggregateType.COUNT);
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
      else if (MethodChecker.jpqlFunctionMethods.contains(sig))
      {
         if (sig.equals(MethodChecker.bigDecimalAbs)
               || sig.equals(MethodChecker.bigIntegerAbs))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
            ColumnExpressions<?> base = val.base.visit(this, passdown);
            return ColumnExpressions.singleColumn(base.reader,
                  FunctionExpression.singleParam("ABS", base.getOnlyColumn())); 
         }
         else if (sig.equals(MethodChecker.stringToUpper))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            ColumnExpressions<?> base = val.base.visit(this, passdown);
            return ColumnExpressions.singleColumn(base.reader,
                  FunctionExpression.singleParam("UPPER", base.getOnlyColumn())); 
         }
         else if (sig.equals(MethodChecker.stringToLower))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            ColumnExpressions<?> base = val.base.visit(this, passdown);
            return ColumnExpressions.singleColumn(base.reader,
                  FunctionExpression.singleParam("LOWER", base.getOnlyColumn())); 
         }
         else if (sig.equals(MethodChecker.stringLength))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            ColumnExpressions<?> base = val.base.visit(this, passdown);
            return ColumnExpressions.singleColumn(base.reader,
                  FunctionExpression.singleParam("LENGTH", base.getOnlyColumn())); 
         }
         else if (sig.equals(MethodChecker.stringTrim))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            ColumnExpressions<?> base = val.base.visit(this, passdown);
            return ColumnExpressions.singleColumn(base.reader,
                  FunctionExpression.singleParam("TRIM", base.getOnlyColumn())); 
         }
         else if (sig.equals(MethodChecker.stringSubstring))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            ColumnExpressions<?> base = val.base.visit(this, passdown);
            ColumnExpressions<?> startIndex = val.args.get(0).visit(this, passdown);
            ColumnExpressions<?> endIndex = val.args.get(1).visit(this, passdown);
            return ColumnExpressions.singleColumn(base.reader,
                  FunctionExpression.threeParam("SUBSTRING", 
                        base.getOnlyColumn(),
                        new BinaryExpression("+", startIndex.getOnlyColumn(), new ConstantExpression("1")),
                        new BinaryExpression("-", endIndex.getOnlyColumn(), startIndex.getOnlyColumn())));
         }
         else if (sig.equals(MethodChecker.stringIndexOf))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, false);
            ColumnExpressions<?> base = val.base.visit(this, passdown);
            ColumnExpressions<?> search = val.args.get(0).visit(this, passdown);
            return ColumnExpressions.singleColumn(base.reader,
                  new BinaryExpression("-", 
                        FunctionExpression.twoParam("LOCATE", 
                              search.getOnlyColumn(),
                              base.getOnlyColumn())
                        , new ConstantExpression("1")));
         }
         throw new TypedValueVisitorException("Do not know how to translate the method " + sig + " into a JPQL function");
      }
      else if (sig.equals(TransformationClassAnalyzer.stringBuilderToString))
      {
         List<ColumnExpressions<?>> concatenatedStrings = new ArrayList<>();
         MethodCallValue.VirtualMethodCallValue baseVal = val;
         while (true)
         {
            if (!(baseVal.base instanceof MethodCallValue.VirtualMethodCallValue))
               throw new TypedValueVisitorException("Unexpected use of StringBuilder");
            baseVal = (MethodCallValue.VirtualMethodCallValue)baseVal.base;
            if (baseVal.getSignature().equals(TransformationClassAnalyzer.newStringBuilderString))
            {
               SymbExPassDown passdown = SymbExPassDown.with(val, false);
               concatenatedStrings.add(baseVal.args.get(0).visit(this, passdown));
               break;
            }
            else if (baseVal.getSignature().equals(TransformationClassAnalyzer.newStringBuilder))
            {
               break;
            }
            else if (baseVal.getSignature().equals(TransformationClassAnalyzer.stringBuilderAppendString))
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

   @Override public ColumnExpressions<?> staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException 
   {
      MethodSignature sig = val.getSignature();
      if (sig.equals(TransformationClassAnalyzer.integerValueOf)
            || sig.equals(TransformationClassAnalyzer.longValueOf)
            || sig.equals(TransformationClassAnalyzer.doubleValueOf)
            || sig.equals(TransformationClassAnalyzer.booleanValueOf))
      {
         // Integer.valueOf() to be like a cast and assume it's correct
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
         return base;
      }
      else if (sig.equals(TransformationClassAnalyzer.bigIntegerValueOfLong))
      {
         throw new TypedValueVisitorException("New BigIntegers can only be created in the context of numeric promotion");
      }
      else if (sig.equals(MethodChecker.stringValueOfObject))
      {
         if (!val.args.get(0).getType().equals(Type.getObjectType("java/lang/String")))
            throw new TypedValueVisitorException("Do not know how to convert type " + val.args.get(0).getType() + " to a string");
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
         return base;
      }
      else if (MethodChecker.jpqlFunctionStaticMethods.contains(sig))
      {
         if (sig.equals(MethodChecker.jpqlLike))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
            ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
            ColumnExpressions<?> pattern = val.args.get(1).visit(this, passdown);
            return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
                  new BinaryExpression("LIKE", base.getOnlyColumn(), pattern.getOnlyColumn())); 
         }
         else if (sig.equals(MethodChecker.mathAbsDouble)
               || sig.equals(MethodChecker.mathAbsInt)
               || sig.equals(MethodChecker.mathAbsLong))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
            ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
            return ColumnExpressions.singleColumn(base.reader,
                  FunctionExpression.singleParam("ABS", base.getOnlyColumn())); 
         }
         else if (sig.equals(MethodChecker.mathSqrt))
         {
            SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
            TypedValue baseVal = val.args.get(0);
            if (isWideningCast(baseVal))
               baseVal = skipWideningCast(baseVal);
            ColumnExpressions<?> base = baseVal.visit(this, passdown);
            return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
                  FunctionExpression.singleParam("SQRT", base.getOnlyColumn())); 
         }
         throw new TypedValueVisitorException("Do not know how to translate the method " + sig + " into a JPQL function");
      }
      else
         return super.staticMethodCallValue(val, in);
   }

   // Tracks which numeric types are considered to have more information than
   // other types.
   static Map<Type, Integer> numericPromotionPriority = new HashMap<>();
   static {
      int n = 0;
      numericPromotionPriority.put(Type.INT_TYPE, n);
      numericPromotionPriority.put(Type.getObjectType("java/lang/Integer"), n);
      n++;
      numericPromotionPriority.put(Type.LONG_TYPE, n);
      numericPromotionPriority.put(Type.getObjectType("java/lang/Long"), n);
      n++;
      numericPromotionPriority.put(Type.getObjectType("java/math/BigInteger"), n);
      n++;
      numericPromotionPriority.put(Type.getObjectType("java/math/BigDecimal"), n);
      n++;
      numericPromotionPriority.put(Type.DOUBLE_TYPE, n);
      numericPromotionPriority.put(Type.getObjectType("java/lang/Double"), n);
      n++;
   }
}
