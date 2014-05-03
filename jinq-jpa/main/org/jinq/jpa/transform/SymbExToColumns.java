package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.From;
import org.jinq.jpa.jpqlquery.FromAliasExpression;
import org.jinq.jpa.jpqlquery.ReadFieldExpression;
import org.jinq.jpa.jpqlquery.SimpleRowReader;

import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExToColumns extends TypedValueVisitor<Void, ColumnExpressions<?>, TypedValueVisitorException>
{
   // TODO: This is temporary
   final From from;
   
   final MetamodelUtil metamodel;
   
   SymbExToColumns(MetamodelUtil metamodel, From from)
   {
      this.metamodel = metamodel;
      this.from = from;
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
      // TODO: This is just temporary
      return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
            new FromAliasExpression(from)); 
   }
   
   @Override public ColumnExpressions<?> booleanConstantValue(ConstantValue.BooleanConstant val, Void in) throws TypedValueVisitorException
   {
      // TODO: Explore deeper into why EclipseLink doesn't seem to like boolean 
      // literals like TRUE and FALSE
      return ColumnExpressions.singleColumn(new SimpleRowReader<Integer>(),
            new ConstantExpression(val.val ? "(1=1)" : "(1!=1)")); 
   }

   @Override public ColumnExpressions<?> integerConstantValue(ConstantValue.IntegerConstant val, Void in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<Integer>(),
            new ConstantExpression(Integer.toString(val.val))); 
   }

   @Override public ColumnExpressions<?> stringConstantValue(ConstantValue.StringConstant val, Void in) throws TypedValueVisitorException
   {
      return ColumnExpressions.singleColumn(new SimpleRowReader<String>(),
            new ConstantExpression("'"+ val.val.replaceAll("'", "''") +"'")); 
   }

   @Override public ColumnExpressions<?> castValue(TypedValue.CastValue val, Void in) throws TypedValueVisitorException
   {
      // TODO: Check if cast is consistent with the reader
//      SQLColumnValues toReturn = val.operand.visit(this, in);
//      if (!toReturn.reader.isCastConsistent(val.getType().getInternalName()))
//         throw new TypedValueVisitorException("Attempting to cast to an inconsistent type");
      return val.operand.visit(this, in);
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
         throw new TypedValueVisitorException("Do not know how to add multiple columns together");
      return ColumnExpressions.singleColumn(left.reader,
            new BinaryExpression(val.sqlOpString(), left.getOnlyColumn(), right.getOnlyColumn())); 
   }
   
   @Override public ColumnExpressions<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, Void in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
//      if (TransformationClassAnalyzer.stringEquals.equals(sig))
//      {
//         assert(false); // This should never happen because the simplifier should eliminate these
//         SQLColumnValues sql = new SQLColumnValues(new SQLReader.BooleanSQLReader());
//         sql.add("(");
//         sql.add(val.base.visit(this, in));
//         sql.add(")");
//         sql.add(" = ");
//         sql.add("(");
//         sql.add(val.args.get(0).visit(this, in));
//         sql.add(")");
//         return sql;
//      }
//      else if (TransformationClassAnalyzer.newPair.equals(sig)
//            || TransformationClassAnalyzer.newTuple3.equals(sig)
//            || TransformationClassAnalyzer.newTuple4.equals(sig)
//            || TransformationClassAnalyzer.newTuple5.equals(sig)
//            || TransformationClassAnalyzer.newTuple8.equals(sig))
//      {
//         SQLColumnValues [] vals = new SQLColumnValues[val.args.size()];
//         for (int n = 0; n < vals.length; n++)
//            vals[n] = val.args.get(n).visit(this, in);
//         SQLReader [] valReaders = new SQLReader[vals.length];
//         for (int n = 0; n < vals.length; n++)
//            valReaders[n] = vals[n].reader; 
//         
//         SQLColumnValues sql = new SQLColumnValues(SQLReader.TupleSQLReader.createReaderForTuple(sig.owner, valReaders));
//         int offset = 0;
//         for (int n = 0; n < vals.length; n++)
//         {
//            for (int col = 0; col < vals[n].columns.length; col++)
//            {
//               sql.columns[offset] = vals[n].columns[col];
//               offset++;
//            }
//         }
//         return sql;
//      }
//      else 
      if (metamodel.isSingularAttributeFieldMethod(sig))
      {
         String fieldName = metamodel.fieldMethodToFieldName(sig);
         ColumnExpressions<?> base = val.base.visit(this, in);
         return ColumnExpressions.singleColumn(new SimpleRowReader<>(),
               new ReadFieldExpression(base.getOnlyColumn(), fieldName)); 
//         SQLColumnValues sql = new SQLColumnValues(base.reader.getReaderForField(fieldName));
//         for (int n = 0; n < sql.reader.getNumColumns(); n++)
//            sql.columns[n] = base.columns[base.reader.getColumnForField(fieldName) + n];
      }
//      else if (entityInfo.passThroughMethods.contains(sig))
//      {
//         SQLColumnValues base = val.base.visit(this, in);
//         return base;
//      }
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

}
