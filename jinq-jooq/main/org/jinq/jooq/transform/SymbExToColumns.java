package org.jinq.jooq.transform;

import org.jinq.jooq.querygen.ColumnExpressions;
import org.jinq.jooq.querygen.RowReader;
import org.jinq.jooq.querygen.SimpleRowReader;
import org.jinq.jooq.querygen.TableRowReader;
import org.jinq.jooq.querygen.TupleRowReader;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExToColumns extends TypedValueVisitor<Void, ColumnExpressions<?>, TypedValueVisitorException>
{
   final MetamodelUtil metamodel;
   final SymbExArgumentHandler argHandler;
   
   SymbExToColumns(MetamodelUtil metamodel, SymbExArgumentHandler argumentHandler)
   {
      this.metamodel = metamodel;
      this.argHandler = argumentHandler;
   }
   
   ColumnExpressions<?> transform(TypedValue val) throws TypedValueVisitorException
   {
      return val.visit(this, null);
   }

   @Override public ColumnExpressions<?> defaultValue(TypedValue val, Void in) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled symbolic execution operation: " + val);
   }

   @Override public ColumnExpressions<?> argValue(TypedValue.ArgValue val, Void in) throws TypedValueVisitorException
   {
      int index = val.getIndex();
      return argHandler.handleArg(index, val.getType());
   }
   
   @Override public ColumnExpressions<?> booleanConstantValue(ConstantValue.BooleanConstant val, Void in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<Boolean>(),
            DSL.val(val.val));
   }

   @Override public ColumnExpressions<?> integerConstantValue(ConstantValue.IntegerConstant val, Void in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<Integer>(),
            DSL.val(val.val)); 
   }

   @Override public ColumnExpressions<?> stringConstantValue(ConstantValue.StringConstant val, Void in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<String>(),
            DSL.val(val.val));
   }

   @Override public ColumnExpressions<?> castValue(TypedValue.CastValue val, Void in) throws TypedValueVisitorException
   {
      // TODO: Check if cast is consistent with the reader
//      SQLColumnValues toReturn = val.operand.visit(this, in);
//      if (!toReturn.reader.isCastConsistent(val.getType().getInternalName()))
//         throw new TypedValueVisitorException("Attempting to cast to an inconsistent type");
      return val.operand.visit(this, in);
   }

   @Override public ColumnExpressions<?> mathOpValue(TypedValue.MathOpValue val, Void in) throws TypedValueVisitorException
   {
      ColumnExpressions<?> left = val.left.visit(this, in);
      ColumnExpressions<?> right = val.right.visit(this, in);
      Field leftField = (Field)left.getOnlyColumn();
      Field rightField = (Field)right.getOnlyColumn();
      Field resultField;
      switch(val.op)
      {
      case minus: resultField = leftField.minus(rightField); break;
      case plus: resultField = leftField.plus(rightField); break;
      case mul: resultField = leftField.mul(rightField); break;
      default:
         throw new TypedValueVisitorException("Unknown math operator");
      }
      return ColumnExpressions.singleColumn(left.reader, resultField); 
   }

   @Override public ColumnExpressions<?> comparisonOpValue(TypedValue.ComparisonValue val, Void in) throws TypedValueVisitorException
   {
      ColumnExpressions<?> left = val.left.visit(this, in);
      ColumnExpressions<?> right = val.right.visit(this, in);
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
      if (!left.isSingleColumn() || !right.isSingleColumn())
         throw new TypedValueVisitorException("Do not know how to compare multiple columns together");
      Field leftField = (Field)left.getOnlyColumn();
      Field rightField = (Field)right.getOnlyColumn();
      Condition result = null;
      switch (val.compOp)
      {
         case eq:
            result = leftField.eq(rightField);
            break;
         case ge:
            result = leftField.ge(rightField);
            break;
         case gt:
            result = leftField.gt(rightField);
            break;
         case le:
            result = leftField.le(rightField);
            break;
         case lt:
            result = leftField.lt(rightField);
            break;
         case ne:
            result = leftField.ne(rightField);
            break;
         default:
            throw new TypedValueVisitorException("Unknown comparison operator");
      }
      return ColumnExpressions.singleColumn(left.reader, result);
   }
   
   @Override public ColumnExpressions<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, Void in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (TransformationClassAnalyzer.newPair.equals(sig)
            || TransformationClassAnalyzer.newTuple3.equals(sig)
            || TransformationClassAnalyzer.newTuple4.equals(sig)
            || TransformationClassAnalyzer.newTuple5.equals(sig)
            || TransformationClassAnalyzer.newTuple6.equals(sig)
            || TransformationClassAnalyzer.newTuple7.equals(sig)
            || TransformationClassAnalyzer.newTuple8.equals(sig))
      {
         ColumnExpressions<?> [] vals = new ColumnExpressions<?> [val.args.size()];
         for (int n = 0; n < vals.length; n++)
            vals[n] = val.args.get(n).visit(this, in);
         RowReader<?> [] valReaders = new RowReader[vals.length];
         for (int n = 0; n < vals.length; n++)
            valReaders[n] = vals[n].reader;

         ColumnExpressions<?> toReturn = new ColumnExpressions<>(TupleRowReader.createReaderForTuple(sig.owner, valReaders));
         for (int n = 0; n < vals.length; n++)
            toReturn.columns.addAll(vals[n].columns);
         return toReturn;
      }
      else if (metamodel.isFieldGetterMethod(sig))
      {
         Field<?> field = metamodel.fieldMethodToField(sig);
         ColumnExpressions<?> base = val.base.visit(this, in);
         if (!(base.reader instanceof TableRowReader))
            throw new TypedValueVisitorException("Expecting a table");
         TableRowReader<?> tableReader = (TableRowReader<?>)base.reader;
         RowReader<?> columnReader = tableReader.getReaderForField(field);
         ColumnExpressions<?> newColumns = new ColumnExpressions<>(columnReader);
         int idx = tableReader.getIndexForField(field);
         for (int n = 0; n < columnReader.getNumColumns(); n++)
            newColumns.columns.add(base.columns.get(n + idx));
         return newColumns;
      }
      else if (MetamodelUtil.TUPLE_ACCESSORS.containsKey(sig))
      {
         int idx = MetamodelUtil.TUPLE_ACCESSORS.get(sig) - 1;
         ColumnExpressions<?> base = val.base.visit(this, in);
         RowReader<?> subreader = ((TupleRowReader<?>)base.reader).getReaderForIndex(idx);
         ColumnExpressions<?> toReturn = new ColumnExpressions<>(subreader);
         int baseOffset = ((TupleRowReader<?>)base.reader).getColumnForIndex(idx);
         for (int n = 0; n < subreader.getNumColumns(); n++)
            toReturn.columns.add(base.columns.get(n + baseOffset));
         return toReturn;
      }
      else if (sig.equals(TransformationClassAnalyzer.integerIntValue)
            || sig.equals(TransformationClassAnalyzer.doubleDoubleValue))
      {
         ColumnExpressions<?> base = val.base.visit(this, in);
         return base;
      }
//      else if (entityInfo.dbSetMethods.contains(sig))
//      {
//         if (lambdaContext.joins == null)
//            throw new TypedValueVisitorException("Need a join handler here for subqueries just in case there's an embedded navigational query: " + val);
//         // TODO: Handle checking out the constructor and verifying how
//         // parameters pass through the constructor
//         SQLQuery subQuery = val.base.visit(subQueryHandler, in);
//         if (sig.equals(TransformationClassAnalyzer.dbsetSumInt)
//               || sig.equals(TransformationClassAnalyzer.dbsetMaxInt))
//         {
//            // TODO: do subqueries need to be copied before being passed in here?
//            SQLQuery<Integer> newQuery = null;
//            if (sig.equals(TransformationClassAnalyzer.dbsetSumInt))
//               newQuery = queryMethodHandler.sumInt(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
//            else if (sig.equals(TransformationClassAnalyzer.dbsetMaxInt))
//               newQuery = queryMethodHandler.maxInt(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
//            return handleAggregationSubQuery(val, newQuery);
//         }
//         // TODO: Implement other aggregation functions
//         throw new TypedValueVisitorException("Unhandled DBSet operation");
//      }
//      else if (entityInfo.jinqStreamMethods.contains(sig))
//      {
//         if (lambdaContext.joins == null)
//            throw new TypedValueVisitorException("Need a join handler here for subqueries just in case there's an embedded navigational query: " + val);
//         // TODO: Handle checking out the constructor and verifying how
//         // parameters pass through the constructor
//         SQLQuery subQuery = val.base.visit(subQueryHandler, in);
//         if (sig.equals(TransformationClassAnalyzer.streamSumInt)
//               || sig.equals(TransformationClassAnalyzer.streamMaxInt))
//         {
//            // TODO: do subqueries need to be copied before being passed in here?
//            SQLQuery<Integer> newQuery = null;
//            if (sig.equals(TransformationClassAnalyzer.streamSumInt))
//               newQuery = queryMethodHandler.sumInt(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
//            else if (sig.equals(TransformationClassAnalyzer.streamMaxInt))
//               newQuery = queryMethodHandler.maxInt(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
//            return handleAggregationSubQuery(val, newQuery);
//         }
//         // TODO: Implement other aggregation functions
//         throw new TypedValueVisitorException("Unhandled DBSet operation");
//      }
//      else if (entityInfo.N111Methods.containsKey(sig))
//      {
//         SQLColumnValues base = val.base.visit(this, in);
//         ORMInformation.N111NavigationalLink link = entityInfo.N111Methods.get(sig);
//         if (lambdaContext.joins == null)
//            throw new TypedValueVisitorException("Cannot handle navigational queries in this context: " + val);
//         assert(link.joinInfo.size() == 1);
//         // See if we've already done this join and can reuse it
//         List<SQLFragment> fromKey = new ArrayList<SQLFragment>();
//         for (int n = 0; n < link.joinInfo.get(0).fromColumns.size(); n++)
//         {
//            String fromCol = link.joinInfo.get(0).fromColumns.get(n);
//            int fromColIdx = base.reader.getColumnIndexForColumnName(fromCol);
//            if (fromColIdx < 0) throw new TypedValueVisitorException("Cannot find column for navigational query: " + val);
//            fromKey.add(base.getColumn(fromColIdx));
//         }
//         SQLSubstitution.FromReference from = lambdaContext.joins.findExistingJoin(link.fromEntity, link.name, fromKey);
//         if (from == null)
//         {
//            from = lambdaContext.joins.addFrom(link.joinInfo.get(0).toTableName);
//            for (int n = 0; n < link.joinInfo.get(0).fromColumns.size(); n++)
//            {
//               SQLFragment where = new SQLFragment();
//               String fromCol = link.joinInfo.get(0).fromColumns.get(n);
//               String toCol = link.joinInfo.get(0).toColumns.get(n);
//               int fromColIdx = base.reader.getColumnIndexForColumnName(fromCol);
//               if (fromColIdx < 0) throw new TypedValueVisitorException("Cannot find column for navigational query: " + val);
//               where.add("(");
//               where.add(base.getColumn(fromColIdx));
//               where.add(") = (");
//               where.add(from);
//               where.add("." + toCol);
//               where.add(")");
//               lambdaContext.joins.addWhere(where);
//            }
//            lambdaContext.joins.addCachedJoin(link.fromEntity, link.name, fromKey, from);
//         }
//         EntityManagerBackdoor em = lambdaContext.joins.getEntityManager();
//         SQLColumnValues joinedEntity = new SQLColumnValues<T>(em.getReaderForEntity(link.toEntity));
//         String []columnNames = em.getEntityColumnNames(link.toEntity); 
//         for (int n = 0; n < columnNames.length; n++)
//         {
//            SQLFragment fragment = new SQLFragment();
//            fragment.add(from);
//            fragment.add("." + columnNames[n]);
//            joinedEntity.columns[n] = fragment;
//         }
//         return joinedEntity;
////         throw new TypedValueVisitorException("Unhandled N:1 or 1:1 navigational query:" + val);
//      }
      else
         return super.virtualMethodCallValue(val, in);
   }

   @Override public ColumnExpressions<?> staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, Void in) throws TypedValueVisitorException 
   {
      MethodSignature sig = val.getSignature();
      if (sig.equals(TransformationClassAnalyzer.integerValueOf)
            || sig.equals(TransformationClassAnalyzer.doubleValueOf))
      {
         // Integer.valueOf() to be like a cast and assume it's correct
         ColumnExpressions<?> base = val.args.get(0).visit(this, in);
         return base;
      }
//      else if (TransformationClassAnalyzer.stringLike.equals(sig))
//      {
//         SQLColumnValues sql = new SQLColumnValues(new SQLReader.BooleanSQLReader());
//         sql.add("(");
//         sql.add(val.args.get(0).visit(this, in));
//         sql.add(")");
//         sql.add(" LIKE ");
//         sql.add("(");
//         sql.add(val.args.get(1).visit(this, in));
//         sql.add(")");
//         return sql;
//      }
      else
         return super.staticMethodCallValue(val, in);
   }

}
