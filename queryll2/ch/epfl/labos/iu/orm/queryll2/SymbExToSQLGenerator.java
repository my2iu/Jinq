package ch.epfl.labos.iu.orm.queryll2;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.query2.EntityManagerBackdoor;
import ch.epfl.labos.iu.orm.query2.SQLFragment;
import ch.epfl.labos.iu.orm.query2.SQLQuery;
import ch.epfl.labos.iu.orm.query2.SQLReader;
import ch.epfl.labos.iu.orm.query2.SQLSubstitution;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.SyntheticTypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExToSQLGenerator<T> extends TypedValueVisitor<T, SQLColumnValues>
{
   ORMInformation entityInfo;
   SymbExLambdaContext<T> lambdaContext;
   SymbExToSubQueryGenerator<T> subQueryHandler;
   QueryllSQLQueryTransformer queryMethodHandler;
   
   public SymbExToSQLGenerator(ORMInformation entityInfo, SymbExArgHandler<T> argHandler, SymbExGetFieldHandler<T> getFieldHandler, SymbExJoinHandler<T> joinHandler, QueryllSQLQueryTransformer queryMethodHandler) 
   {
      this.entityInfo = entityInfo;
      this.lambdaContext = new SymbExLambdaContext<T>(argHandler, getFieldHandler, joinHandler);
      subQueryHandler = new SymbExToSubQueryGenerator<T>(entityInfo, argHandler, getFieldHandler, joinHandler, queryMethodHandler);
      this.queryMethodHandler = queryMethodHandler;
   }
   public SQLColumnValues generateFor(TypedValue val) throws TypedValueVisitorException
   {
      return val.visit(this, null);
   }

   @Override public SQLColumnValues argValue(TypedValue.ArgValue val, T in) throws TypedValueVisitorException
   {
      if (lambdaContext.args != null)
         return lambdaContext.args.argValue(val, in);
      return super.argValue(val, in);
   }

   @Override public SQLColumnValues getFieldValue(TypedValue.GetFieldValue val, T in) throws TypedValueVisitorException
   {
      if (lambdaContext.fields != null)
         return lambdaContext.fields.getFieldValue(val, in);
      return super.getFieldValue(val, in);
   }

   @Override public SQLColumnValues integerConstantValue(ConstantValue.IntegerConstant val, T in) throws TypedValueVisitorException
   {
      SQLColumnValues sql = new SQLColumnValues(new SQLReader.IntegerSQLReader());
      sql.columns[0] = new SQLFragment(Integer.toString(val.val));
      return sql;
   }

   @Override public SQLColumnValues stringConstantValue(ConstantValue.StringConstant val, T in) throws TypedValueVisitorException
   {
      SQLColumnValues sql = new SQLColumnValues(new SQLReader.StringSQLReader());
      sql.columns[0] = new SQLFragment("'"+ val.val.replaceAll("'", "''") +"'");
      return sql;
   }
   
   @Override public SQLColumnValues castValue(TypedValue.CastValue val, T in) throws TypedValueVisitorException
   {
      // Check if cast is consistent with the reader
      SQLColumnValues toReturn = val.operand.visit(this, in);
      if (!toReturn.reader.isCastConsistent(val.getType().getInternalName()))
         throw new TypedValueVisitorException("Attempting to cast to an inconsistent type");
      return toReturn;
   }

   @Override public SQLColumnValues notOpValue(TypedValue.NotValue val, T in) throws TypedValueVisitorException
   {
      SQLColumnValues sql = new SQLColumnValues(new SQLReader.BooleanSQLReader());
      SQLColumnValues operand = val.operand.visit(this, in);
      assert(operand.reader instanceof SQLReader.BooleanSQLReader);
      sql
         .add("NOT(")
         .add(operand)
         .add(")");
      return sql;
   }


   @Override public SQLColumnValues comparisonOpValue(TypedValue.ComparisonValue val, T in) throws TypedValueVisitorException
   {
      SQLColumnValues left = val.left.visit(this, in);
      SQLColumnValues right = val.right.visit(this, in);
      SQLColumnValues sql = new SQLColumnValues(new SQLReader.BooleanSQLReader());
      if (val.left.getType() == Type.BOOLEAN_TYPE
            || val.right.getType() == Type.BOOLEAN_TYPE)
      {
         // TODO: These simplifications should be put into a separate
         // optimization step, maybe?
         if (val.left instanceof ConstantValue.IntegerConstant
               && ((ConstantValue.IntegerConstant)val.left).val == 0)
         {
            left = new SQLColumnValues(new SQLReader.BooleanSQLReader());
            left.columns[0] = new SQLFragment("FALSE");
         }
         if (val.right instanceof ConstantValue.IntegerConstant
               && ((ConstantValue.IntegerConstant)val.right).val == 0)
         {
            right = new SQLColumnValues(new SQLReader.BooleanSQLReader());
            right.columns[0] = new SQLFragment("FALSE");
         }
      }
      sql.add("(").add(left).add(")");
      sql.add(" " + val.sqlOpString() + " ");
      sql.add("(").add(right).add(")");
      return sql;
   }

   @Override public SQLColumnValues mathOpValue(TypedValue.MathOpValue val, T in) throws TypedValueVisitorException
   {
      SQLColumnValues left = val.left.visit(this, in);
      SQLColumnValues right = val.right.visit(this, in);
      SQLColumnValues sql = new SQLColumnValues(left.reader);
      sql.add("(").add(left).add(")");
      sql.add(" " + val.sqlOpString() + " ");
      sql.add("(").add(right).add(")");
      return sql;
   }

   @Override public SQLColumnValues staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, T in) throws TypedValueVisitorException 
   {
      MethodSignature sig = val.getSignature();
      if (entityInfo.passThroughStaticMethods.contains(sig))
      {
         SQLColumnValues base = val.args.get(0).visit(this, in);
         return base;
      }
      else if (TransformationClassAnalyzer.stringLike.equals(sig))
      {
         SQLColumnValues sql = new SQLColumnValues(new SQLReader.BooleanSQLReader());
         sql.add("(");
         sql.add(val.args.get(0).visit(this, in));
         sql.add(")");
         sql.add(" LIKE ");
         sql.add("(");
         sql.add(val.args.get(1).visit(this, in));
         sql.add(")");
         return sql;
      }

      else
         return super.staticMethodCallValue(val, in);
   }
   
   @Override public SQLColumnValues virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, T in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (TransformationClassAnalyzer.stringEquals.equals(sig))
      {
         assert(false); // This should never happen because the simplifier should eliminate these
         SQLColumnValues sql = new SQLColumnValues(new SQLReader.BooleanSQLReader());
         sql.add("(");
         sql.add(val.base.visit(this, in));
         sql.add(")");
         sql.add(" = ");
         sql.add("(");
         sql.add(val.args.get(0).visit(this, in));
         sql.add(")");
         return sql;
      }
      else if (TransformationClassAnalyzer.newPair.equals(sig)
            || TransformationClassAnalyzer.newTuple3.equals(sig)
            || TransformationClassAnalyzer.newTuple4.equals(sig)
            || TransformationClassAnalyzer.newTuple5.equals(sig)
            || TransformationClassAnalyzer.newTuple8.equals(sig))
      {
         SQLColumnValues [] vals = new SQLColumnValues[val.args.size()];
         for (int n = 0; n < vals.length; n++)
            vals[n] = val.args.get(n).visit(this, in);
         SQLReader [] valReaders = new SQLReader[vals.length];
         for (int n = 0; n < vals.length; n++)
            valReaders[n] = vals[n].reader; 
         
         SQLColumnValues sql = new SQLColumnValues(SQLReader.TupleSQLReader.createReaderForTuple(sig.owner, valReaders));
         int offset = 0;
         for (int n = 0; n < vals.length; n++)
         {
            for (int col = 0; col < vals[n].columns.length; col++)
            {
               sql.columns[offset] = vals[n].columns[col];
               offset++;
            }
         }
         return sql;
      }
      else if (entityInfo.fieldMethods.containsKey(sig))
      {
         String fieldName = entityInfo.fieldMethods.get(sig);
         SQLColumnValues base = val.base.visit(this, in);
         SQLColumnValues sql = new SQLColumnValues(base.reader.getReaderForField(fieldName));
         for (int n = 0; n < sql.reader.getNumColumns(); n++)
            sql.columns[n] = base.columns[base.reader.getColumnForField(fieldName) + n];
         return sql;
      }
      else if (entityInfo.passThroughMethods.contains(sig))
      {
         SQLColumnValues base = val.base.visit(this, in);
         return base;
      }
      else if (entityInfo.dbSetMethods.contains(sig))
      {
         if (lambdaContext.joins == null)
            throw new TypedValueVisitorException("Need a join handler here for subqueries just in case there's an embedded navigational query: " + val);
         // TODO: Handle checking out the constructor and verifying how
         // parameters pass through the constructor
         SQLQuery subQuery = val.base.visit(subQueryHandler, in);
         if (sig.equals(TransformationClassAnalyzer.dbsetSumInt)
               || sig.equals(TransformationClassAnalyzer.dbsetMaxInt))
         {
            // TODO: do subqueries need to be copied before being passed in here?
            SQLQuery<Integer> newQuery = null;
            if (sig.equals(TransformationClassAnalyzer.dbsetSumInt))
               newQuery = queryMethodHandler.sumInt(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
            else if (sig.equals(TransformationClassAnalyzer.dbsetMaxInt))
               newQuery = queryMethodHandler.maxInt(subQuery, val.args.get(0), lambdaContext.joins.getEntityManager());
            if (newQuery == null) throw new TypedValueVisitorException("Could not decode a subquery " + val);
            if (newQuery instanceof SQLQuery.InternalGroup)
            {
               SQLQuery.InternalGroup<Integer> agg = (SQLQuery.InternalGroup<Integer>)newQuery;
               // TODO: This is probably correct but potentially bogus.
               // It should be thought through a bit more wrt what InternalGroup
               // means and whether that is sufficient to allow us to do this
               assert(agg.reader instanceof SQLReader.IntegerSQLReader);
               SQLColumnValues<Integer> toReturn = new SQLColumnValues<Integer>(agg.reader);
               toReturn.columns[0] = agg.columns.get(0);
               return toReturn;
            }
            else if (newQuery instanceof SQLQuery.SelectFromWhere)
            {
               SQLQuery.SelectFromWhere<Integer> subquery = (SQLQuery.SelectFromWhere<Integer>)newQuery;
               assert(subquery.reader instanceof SQLReader.IntegerSQLReader);
               SQLColumnValues<Integer> toReturn = new SQLColumnValues<Integer>(subquery.reader);
               toReturn.columns[0].add(new SQLSubstitution.ScalarSelectFromWhereSubQuery(subquery));
               return toReturn;
            }
            else
               throw new TypedValueVisitorException("Unhandled nesting of a query");
         }
         // TODO: Implement other aggregation functions
         throw new TypedValueVisitorException("Unhandled DBSet operation");
      }
      else if (entityInfo.N111Methods.containsKey(sig))
      {
         SQLColumnValues base = val.base.visit(this, in);
         ORMInformation.N111NavigationalLink link = entityInfo.N111Methods.get(sig);
         if (lambdaContext.joins == null)
            throw new TypedValueVisitorException("Cannot handle navigational queries in this context: " + val);
         assert(link.joinInfo.size() == 1);
         // See if we've already done this join and can reuse it
         List<SQLFragment> fromKey = new ArrayList<SQLFragment>();
         for (int n = 0; n < link.joinInfo.get(0).fromColumns.size(); n++)
         {
            String fromCol = link.joinInfo.get(0).fromColumns.get(n);
            int fromColIdx = base.reader.getColumnIndexForColumnName(fromCol);
            if (fromColIdx < 0) throw new TypedValueVisitorException("Cannot find column for navigational query: " + val);
            fromKey.add(base.getColumn(fromColIdx));
         }
         SQLSubstitution.FromReference from = lambdaContext.joins.findExistingJoin(link.fromEntity, link.name, fromKey);
         if (from == null)
         {
            from = lambdaContext.joins.addFrom(link.joinInfo.get(0).toTableName);
            for (int n = 0; n < link.joinInfo.get(0).fromColumns.size(); n++)
            {
               SQLFragment where = new SQLFragment();
               String fromCol = link.joinInfo.get(0).fromColumns.get(n);
               String toCol = link.joinInfo.get(0).toColumns.get(n);
               int fromColIdx = base.reader.getColumnIndexForColumnName(fromCol);
               if (fromColIdx < 0) throw new TypedValueVisitorException("Cannot find column for navigational query: " + val);
               where.add("(");
               where.add(base.getColumn(fromColIdx));
               where.add(") = (");
               where.add(from);
               where.add("." + toCol);
               where.add(")");
               lambdaContext.joins.addWhere(where);
            }
            lambdaContext.joins.addCachedJoin(link.fromEntity, link.name, fromKey, from);
         }
         EntityManagerBackdoor em = lambdaContext.joins.getEntityManager();
         SQLColumnValues joinedEntity = new SQLColumnValues<T>(em.getReaderForEntity(link.toEntity));
         String []columnNames = em.getEntityColumnNames(link.toEntity); 
         for (int n = 0; n < columnNames.length; n++)
         {
            SQLFragment fragment = new SQLFragment();
            fragment.add(from);
            fragment.add("." + columnNames[n]);
            joinedEntity.columns[n] = fragment;
         }
         return joinedEntity;
//         throw new TypedValueVisitorException("Unhandled N:1 or 1:1 navigational query:" + val);
      }
      else
         return super.virtualMethodCallValue(val, in);
   }
}
