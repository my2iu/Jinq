package ch.epfl.labos.iu.orm.queryll2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.JinqStream.AggregateSelect;
import org.jinq.orm.stream.JinqStream.CollectComparable;
import org.jinq.orm.stream.JinqStream.CollectNumber;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.DBSet.AggregateDouble;
import ch.epfl.labos.iu.orm.DBSet.AggregateGroup;
import ch.epfl.labos.iu.orm.DBSet.AggregateInteger;
import ch.epfl.labos.iu.orm.DBSet.Join;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;
import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.StringSorter;
import ch.epfl.labos.iu.orm.query2.EntityManagerBackdoor;
import ch.epfl.labos.iu.orm.query2.ParameterLocation;
import ch.epfl.labos.iu.orm.query2.SQLFragment;
import ch.epfl.labos.iu.orm.query2.SQLFrom;
import ch.epfl.labos.iu.orm.query2.SQLQuery;
import ch.epfl.labos.iu.orm.query2.SQLQuery.SelectFromWhere;
import ch.epfl.labos.iu.orm.query2.SQLQueryTransforms;
import ch.epfl.labos.iu.orm.query2.SQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReader.DoubleSQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReader.IntegerSQLReader;
import ch.epfl.labos.iu.orm.query2.SQLSubstitution;
import ch.epfl.labos.iu.orm.query2.SQLSubstitution.FromReference;
import ch.epfl.labos.iu.orm.queryll2.path.StaticMethodAnalysisStorage;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.LambdaFactory;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ArgValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

import com.user00.thunk.SerializedLambda;

//These methods will clobber the query passed into it when making a 
//new one, so be sure to always pass in a copy of the query instead 
//of the original

public class QueryllSQLQueryTransformer implements SQLQueryTransforms, StaticMethodAnalysisStorage<MethodAnalysisResults>
{
   ORMInformation entityInfo;
   LambdaRuntimeTransformAnalyzer runtimeAnalyzer;
   public QueryllSQLQueryTransformer(ORMInformation entityInfo, LambdaRuntimeTransformAnalyzer runtimeAnalyzer)
   {
      this.entityInfo = entityInfo;
      this.runtimeAnalyzer = runtimeAnalyzer;
   }
   
   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.StaticMethodAnalysisStorage#storeMethodAnalysis(java.lang.String, java.lang.String, ch.epfl.labos.iu.orm.queryll2.MethodAnalysisResults)
    */
   @Override
   public void storeMethodAnalysis(String interfaceName, String className, MethodAnalysisResults methodAnalysis)
   {
      MethodAnalysisResults analysis = (MethodAnalysisResults)methodAnalysis;
      if (interfaceName.equals(TransformationClassAnalyzer.WHERE_INTERFACE))
         whereAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.SELECT_INTERFACE))
         selectAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.AGGREGATESELECT_INTERFACE))
         aggregateSelectAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.AGGREGATEINTEGER_INTERFACE))
         aggregateIntegerAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.AGGREGATEDOUBLE_INTERFACE))
         aggregateDoubleAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.JOIN_INTERFACE))
         joinAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.GROUP_INTERFACE))
         groupAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.DATESORTER_SUPERCLASS))
         dateSorterAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.INTSORTER_SUPERCLASS))
         intSorterAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.DOUBLESORTER_SUPERCLASS))
         doubleSorterAnalysis.put(className, analysis);
      else if (interfaceName.equals(TransformationClassAnalyzer.STRINGSORTER_SUPERCLASS))
         stringSorterAnalysis.put(className, analysis);
   }
   
   Map<String, MethodAnalysisResults> groupAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> joinAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> aggregateDoubleAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> aggregateIntegerAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> aggregateSelectAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> selectAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> whereAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> dateSorterAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> intSorterAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> doubleSorterAnalysis =
      new HashMap<String, MethodAnalysisResults>();
   Map<String, MethodAnalysisResults> stringSorterAnalysis =
      new HashMap<String, MethodAnalysisResults>();

   static Map<Type, SQLReader> allowedQueryParameterTypes;
   static {
      allowedQueryParameterTypes = new HashMap<Type, SQLReader>();
      allowedQueryParameterTypes.put(Type.INT_TYPE, new SQLReader.IntegerSQLReader());
      allowedQueryParameterTypes.put(Type.DOUBLE_TYPE, new SQLReader.DoubleSQLReader());
      allowedQueryParameterTypes.put(Type.getObjectType("java/lang/Integer"), new SQLReader.IntegerSQLReader());
      allowedQueryParameterTypes.put(Type.getObjectType("java/lang/Double"), new SQLReader.DoubleSQLReader());
      allowedQueryParameterTypes.put(Type.getObjectType("java/lang/String"), new SQLReader.StringSQLReader());
      allowedQueryParameterTypes.put(Type.getObjectType("java/sql/Date"), new SQLReader.DateSQLReader());
   }

   static class SelectFromWhereExtensionJoin<T, U> implements SymbExJoinHandler<T>
   {
      SQLQuery.SelectFromWhere<U> sfw;
      EntityManagerBackdoor em;
      SelectFromWhereExtensionJoin(SQLQuery.SelectFromWhere<U> sfw, EntityManagerBackdoor em)
      {
         this.sfw = sfw;
         this.em = em;
      }
      public static <T, U> SelectFromWhereExtensionJoin<T, U> fromSfw(SQLQuery.SelectFromWhere<U> sfw, EntityManagerBackdoor em)
      {
         return new SelectFromWhereExtensionJoin<T, U>(sfw, em);
      }
      public void addWhere(SQLFragment where)
      {
         if (sfw.where.isEmpty())
         {
            sfw.where.add(where);
         }
         else
         {
            SQLFragment sql = new SQLFragment("(");
            sql.add(sfw.where);
            sql.add(") AND (");
            sql.add(where);
            sql.add(")");
            sfw.where = sql;
         }
      }
      public FromReference addFrom(String tableName)
      {
         SQLFrom from = SQLFrom.fromTable(tableName);
         sfw.from.add(from);
         return new SQLSubstitution.FromReference(from);
      }
      public EntityManagerBackdoor getEntityManager()
      {
         return em;
      }
      public FromReference findExistingJoin(String fromEntity, String name,
                                        List<SQLFragment> joinKey)
      {
         return sfw.findCachedN111Link(fromEntity, name, joinKey);
      }
      public void addCachedJoin(String fromEntity, String name,
                                List<SQLFragment> joinKey,
                                FromReference entityTable)
      {
         sfw.addCachedN111Link(fromEntity, name, joinKey, entityTable);
      }
   }

   static class ParamsToJava8LambdaDescription
   {
      int numCapturedArgs;
      
      static ParamsToJava8LambdaDescription fromSerializedLambda(SerializedLambda s)
      {
         ParamsToJava8LambdaDescription params = new ParamsToJava8LambdaDescription();
         params.numCapturedArgs = s.capturedArgs.length; 
         return params;
      }
      static ParamsToJava8LambdaDescription fromLambda(Object lambda)
      {
         SerializedLambda s = SerializedLambda.extractLambda(lambda);
         if (s != null)
            return ParamsToJava8LambdaDescription.fromSerializedLambda(s);
         return new ParamsToJava8LambdaDescription();
      }
      
      int getNumCapturedArgs()
      {
         return numCapturedArgs;
      }
   }
   
   static class SelectFromWhereExtensionArg<T, U> implements SymbExArgHandler<T> 
   {
      SQLColumnValues dataSource;
      ParamsToJava8LambdaDescription java8LambdaParams;
      int lambdaIndex = 0;
      public SelectFromWhereExtensionArg(SQLColumnValues dataSource, ParamsToJava8LambdaDescription java8LambdaParams, int lambdaIndex)
      {
         this.dataSource = dataSource;
         this.java8LambdaParams = java8LambdaParams;
         this.lambdaIndex = lambdaIndex;
      }
      public static <T, U> SelectFromWhereExtensionArg<T, U> fromReaderColumns(SQLReader<U> reader, List<SQLFragment> columns, ParamsToJava8LambdaDescription java8LambdaParams, int lambdaIndex)
      {
         SQLColumnValues<U> dataSource = new SQLColumnValues<U>(reader);
         for (int n = 0; n < dataSource.reader.getNumColumns(); n++)
            dataSource.columns[n] = columns.get(n);
         return new SelectFromWhereExtensionArg<T,U>(dataSource, java8LambdaParams, lambdaIndex);
      }
      public static <T, U> SelectFromWhereExtensionArg<T, U> fromSfw(SQLQuery.SelectFromWhere<U> sfw, ParamsToJava8LambdaDescription java8LambdaParams, int lambdaIndex)
      {
         return fromReaderColumns(sfw.getReader(), sfw.columns, java8LambdaParams, lambdaIndex);
      }

      public SQLColumnValues argValue(ArgValue val, T in)
         throws TypedValueVisitorException
      {
         if (java8LambdaParams != null && val.getIndex() < java8LambdaParams.getNumCapturedArgs())
         {
            // Currently, we only support parameters of a few small simple types.
            // We should also support more complex types (e.g. entities) and allow
            // fields/methods of those entities to be called in the query (code
            // motion will be used to push those field accesses or method calls
            // outside the query where they will be evaluated and then passed in
            // as a parameter)
            Type t = val.getType();
            if (!allowedQueryParameterTypes.containsKey(t))
               throw new TypedValueVisitorException("Accessing a field with unhandled type");
            
            try
            {
               // TODO: Careful here. ParameterLocation is relative to the base
               // lambda, but if we arrive here from inside a nested query, "this"
               // might refer to a lambda nested inside the base lambda. (Of course,
               // nested queries with parameters aren't currently supported, so it
               // doesn't matter.)
               ParameterLocation paramLoc = ParameterLocation.createJava8LambdaArgAccess(val.getIndex(), lambdaIndex);
               SQLColumnValues toReturn = new SQLColumnValues(allowedQueryParameterTypes.get(t));
               assert(toReturn.getNumColumns() == 1);
               toReturn.columns[0].add(new SQLSubstitution.ExternalParameterLink(paramLoc));
               return toReturn;
            } catch (Exception e)
            {
               throw new TypedValueVisitorException(e); 
            } 
         }
         else
            return dataSource;
         
         
         
      }

      public SQLQuery argSubQueryValue(ArgValue val, T in)
            throws TypedValueVisitorException
      {
         throw new TypedValueVisitorException("Accessing argument as a subquery when it is a value");
      }
   }
   static class SelectFromWhereExtensionSelectAggregatesArg<T, U> implements SymbExArgHandler<T> 
   {
      SQLColumnValues dataSource;
      public SelectFromWhereExtensionSelectAggregatesArg(SQLColumnValues dataSource)
      {
         this.dataSource = dataSource;
      }
      public static <T, U> SelectFromWhereExtensionSelectAggregatesArg<T, U> fromSfw(SQLQuery.SelectFromWhere<U> sfw)
      {
         SQLColumnValues dataSource = new SQLColumnValues(sfw.getReader());
         for (int n = 0; n < dataSource.reader.getNumColumns(); n++)
            dataSource.columns[n] = sfw.columns.get(n);
         return new SelectFromWhereExtensionSelectAggregatesArg<T,U>(dataSource);
      }

      public SQLColumnValues argValue(ArgValue val, T in)
         throws TypedValueVisitorException
      {
         throw new TypedValueVisitorException("Accessing argument as a value when it is a subquery");
      }

      public SQLQuery argSubQueryValue(ArgValue val, T in)
            throws TypedValueVisitorException
      {
         SQLQuery.InternalGroup group = new SQLQuery.InternalGroup(dataSource.reader);
         for (SQLFragment col: dataSource.columns)
            group.columns.add(col);
         return group;
      }
   }
   static class SelectFromWhereExtensionGroupArg<T, U> implements SymbExArgHandler<T> 
   {
      SQLColumnValues keySource;
      SQLColumnValues dataSource;
      SerializedLambda java8Lambda;
      public SelectFromWhereExtensionGroupArg(SQLColumnValues keySource, SQLColumnValues dataSource, SerializedLambda java8Lambda)
      {
         this.keySource = keySource;
         this.dataSource = dataSource;
         this.java8Lambda = java8Lambda;
      }
      public static <T, U, V> SelectFromWhereExtensionGroupArg<T, U> 
         fromReaderColumns(SQLReader<U> keyReader, List<SQLFragment> keyColumns, 
                           SQLReader<V> reader, List<SQLFragment> columns,
                           SerializedLambda java8Lambda)
      {
         SQLColumnValues<U> keySource = new SQLColumnValues<U>(keyReader);
         for (int n = 0; n < keySource.reader.getNumColumns(); n++)
            keySource.columns[n] = keyColumns.get(n);
         
         SQLColumnValues<V> dataSource = new SQLColumnValues<V>(reader);
         for (int n = 0; n < dataSource.reader.getNumColumns(); n++)
            dataSource.columns[n] = columns.get(n);

         return new SelectFromWhereExtensionGroupArg<T,U>(keySource, dataSource, java8Lambda);
      }
      public static <T, U, V> SelectFromWhereExtensionGroupArg<T, U> 
         fromSfw(SQLReader<U> keyReader, List<SQLFragment> keyColumns,
                 SQLQuery.SelectFromWhere<V> sfw, SerializedLambda java8Lambda)
      {
         return fromReaderColumns(keyReader, keyColumns, sfw.getReader(), sfw.columns, java8Lambda);
      }

      public SQLColumnValues argValue(ArgValue val, T in)
         throws TypedValueVisitorException
      {
         if (val.getIndex() != 0)
            throw new TypedValueVisitorException("Accessing argument as a value when it is a subquery");
         return keySource;
      }

      public SQLQuery argSubQueryValue(ArgValue val, T in)
            throws TypedValueVisitorException
      {
         if (val.getIndex() != 1)
            throw new TypedValueVisitorException("Accessing argument as a subquery when it is a value");
         SQLQuery.InternalGroup group = new SQLQuery.InternalGroup(dataSource.reader);
         for (SQLFragment col: dataSource.columns)
            group.columns.add(col);
         return group;
      }
   }
   static class ParamHandler<T> implements SymbExGetFieldHandler<T>
   {
      int lambdaIndex;
      Object lambda;   // TODO: Remove the lambda here
      Object emSource; // TODO: once caching is used, there won't be any checking of entity managers
      public ParamHandler(int lambdaThisIndex, Object lambda, Object emSource)
      {
         this.lambdaIndex = lambdaThisIndex;
         this.lambda = lambda;
         this.emSource = emSource;
      }
      public SQLColumnValues getFieldValue(TypedValue.GetFieldValue val, T in) throws TypedValueVisitorException
      {
         if (!(val.operand instanceof TypedValue.ThisValue))
            throw new TypedValueVisitorException("Unhandled access to a field");
         // Currently, we only support parameters of a few small simple types.
         // We should also support more complex types (e.g. entities) and allow
         // fields/methods of those entities to be called in the query (code
         // motion will be used to push those field accesses or method calls
         // outside the query where they will be evaluated and then passed in
         // as a parameter)
         Type t = val.getType();
         if (!allowedQueryParameterTypes.containsKey(t))
            throw new TypedValueVisitorException("Accessing a field with unhandled type");
         
         try
         {
            // TODO: Careful here. ParameterLocation is relative to the base
            // lambda, but if we arrive here from inside a nested query, "this"
            // might refer to a lambda nested inside the base lambda. (Of course,
            // nested queries with parameters aren't currently supported, so it
            // doesn't matter.)
            ParameterLocation paramLoc = ParameterLocation.createThisFieldAccess(val.name, lambdaIndex);
//            Object param = paramLoc.getParameter(lambda);
            SQLColumnValues toReturn = new SQLColumnValues(allowedQueryParameterTypes.get(t));
            assert(toReturn.getNumColumns() == 1);
            toReturn.columns[0].add(new SQLSubstitution.ExternalParameterLink(paramLoc));
            return toReturn;
         } catch (Exception e)
         {
            throw new TypedValueVisitorException(e); 
         } 
      }
      public EntityManagerBackdoor getAndCheckEntityManager(TypedValue val)
      {
         try {
            if (val instanceof TypedValue.ArgValue)
            {
               // TODO: The entity manager here is just a fake one used as a placeholder for now
               // so we'll just assume it's the same one as the one used elsewhere (though technically,
               // we could use some sort of clever serialization scheme to make sure we get the same
               // entity manager back)
               if (!(emSource instanceof EntityManagerBackdoor)) return null;
               return (EntityManagerBackdoor)emSource;
            }
            else if (val instanceof TypedValue.GetFieldValue)
            {
               TypedValue.GetFieldValue getField = (TypedValue.GetFieldValue)val;
               if (!(getField.operand instanceof TypedValue.ThisValue)) return null;
               // TODO: Does this pick up inherited fields?
               Field field = lambda.getClass().getDeclaredField(getField.name);
               field.setAccessible(true);
               Object em = field.get(lambda);
               // TODO: Will need to change to support DBSet.with() ?
               if (em != emSource) return null;  // different entity manager than we're currently using
               if (!(em instanceof EntityManagerBackdoor)) return null;
               return (EntityManagerBackdoor)em;
            }
            else
               return null;
         } catch (Exception e)
         {
            return null;
         } 
      }
      public SQLQuery getFieldSubQueryValue(TypedValue.GetFieldValue val, T in) throws TypedValueVisitorException
      {
         throw new TypedValueVisitorException("Unhandled: getting a parameter that is a subquery");
      }
      
   }
   static class SQLGeneratorWithParams<T, U> extends SymbExToSQLGenerator<T>
   {

      public SQLGeneratorWithParams(ORMInformation entityInfo,
            SymbExArgHandler<T> argHandler,
            SymbExGetFieldHandler<T> getFieldHandler,
            SymbExJoinHandler<T> joinHandler,
            QueryllSQLQueryTransformer queryMethodHandler)
      {
         super(entityInfo, argHandler, getFieldHandler, joinHandler, queryMethodHandler);
      }
   }

   static class SubQueryGeneratorWithParams<T, U> extends SymbExToSubQueryGenerator<T>
   {

      public SubQueryGeneratorWithParams(ORMInformation entityInfo,
            SymbExArgHandler<T> argHandler,
            SymbExGetFieldHandler<T> getFieldHandler,
            SymbExJoinHandler<T> joinHandler,
            StaticMethodAnalysisStorage queryMethodHandler)
      {
         super(entityInfo, argHandler, getFieldHandler, joinHandler, queryMethodHandler);
      }
   }
   
   boolean doRuntimeCheckForSideEffects(MethodAnalysisResults m)
   {
      // There are certain method calls that may result in side-effects, but
      // we cannot determine this statically and need to check these at
      // runtime. This method returns true if everything checks out
      
      // TODO: Check that all newly constructed Transformation classes do not
      // have side-effects in their constructors
      
      // TODO: Check that DBSet methods are called on DBSet instances that are
      // known not to have weird side-effects
      
      return true;
   }

   public <T> SQLQuery<T> where(SQLQuery<T> query, int lambdaThisIndex, Where<T> test, Object emSource)
   {
      MethodAnalysisResults analysis = null;
      SerializedLambda s = SerializedLambda.extractLambda(test);
      if (s != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(s);
         if (analysis == null) return null;
      }
      else
      {
         String className = Type.getInternalName(test.getClass());
         if (!whereAnalysis.containsKey(className)) return null;
         analysis = whereAnalysis.get(className);
      }
      return where(query, lambdaThisIndex, test, emSource, analysis, s);
   }

   // Version of where analysis used by JinqStream.where()
   public <T, E extends Exception> SQLQuery<T> where(SQLQuery<T> query, int lambdaThisIndex, JinqStream.Where<T, E> test, Object emSource)
   {
      SerializedLambda s = SerializedLambda.extractLambda(test);
      if (s == null) return null;
      MethodAnalysisResults analysis = runtimeAnalyzer.analyzeLambda(s);
      if (analysis == null) return null;
      return where(query, lambdaThisIndex, test, emSource, analysis, s);
   }
   
   private <T> SQLQuery<T> where(SQLQuery<T> query, int lambdaThisIndex,
         Object test, Object emSource, MethodAnalysisResults analysis,
         SerializedLambda s)
   {
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      if (query instanceof SQLQuery.SelectFromWhere)
      {
         final SQLQuery.SelectFromWhere<T> sfw = (SQLQuery.SelectFromWhere<T>)query;
         SymbExToSQLGenerator gen = 
            new SQLGeneratorWithParams<Object, T>(entityInfo, 
                  SelectFromWhereExtensionArg.fromSfw(sfw, ParamsToJava8LambdaDescription.fromSerializedLambda(s), lambdaThisIndex), 
                  new ParamHandler<Object>(lambdaThisIndex, test, emSource),
                  SelectFromWhereExtensionJoin.fromSfw(sfw, (EntityManagerBackdoor)emSource),
                  this);

         try {
            SQLFragment newWhere = new SQLFragment();
            if (analysis.paths.size() > 1)
            {
               for (PathAnalysis<QueryllPathAnalysisSupplementalInfo> path: analysis.paths)
               {
                  TypedValue pathReturn = path.getSimplifiedIsTrueReturnValue();
                  if (pathReturn instanceof ConstantValue.IntegerConstant
                        && ((ConstantValue.IntegerConstant)pathReturn).val == 0)
                     continue;
                  SQLFragment pathWhere = new SQLFragment();
                  if (pathReturn instanceof ConstantValue.IntegerConstant
                        && ((ConstantValue.IntegerConstant)pathReturn).val != 0)
                  {
                     // do nothing
                  } 
                  else 
                  {
                     SQLColumnValues colVals = gen.generateFor(pathReturn);
                     assert(colVals.getNumColumns() == 1);
                     pathWhere.add("(");
                     pathWhere.add(colVals.columns[0]);
                     pathWhere.add(")");
                  }
                  for (TypedValue condition: path.getSimplifiedConditions())
                  {
                     if (condition instanceof ConstantValue.IntegerConstant
                           && ((ConstantValue.IntegerConstant)condition).val != 0)
                        continue;
                     if (condition instanceof ConstantValue.IntegerConstant
                           && ((ConstantValue.IntegerConstant)condition).val == 0)
                     {
                        if (!pathWhere.isEmpty())
                           pathWhere.add(" AND ");
                        pathWhere.add("(1<>1)");
                     }
                     else
                     {
                        SQLColumnValues condColVals = gen.generateFor(condition);
                        assert(condColVals.getNumColumns() == 1);
                        if (!pathWhere.isEmpty())
                           pathWhere.add(" AND ");
                        pathWhere.add("(");
                        pathWhere.add(condColVals.columns[0]);
                        pathWhere.add(")");
                     }
                  }
                  if (!pathWhere.isEmpty())
                  {
                     if (!newWhere.isEmpty())
                        newWhere.add(" OR ");
                     newWhere.add("(");
                     newWhere.add(pathWhere);
                     newWhere.add(")");
                  }
               }
            }
            else
            {
               PathAnalysis path = analysis.paths.get(0);
               SQLColumnValues colVals = gen.generateFor(path.getSimplifiedReturnValue());
               assert(colVals.reader.getNumColumns() == 1);
               if (!newWhere.isEmpty())
                  newWhere.add(" OR ");
               newWhere.add("(");
               newWhere.add(colVals.columns[0]);
               newWhere.add(")");
            }
            
            if (!sfw.where.isEmpty())
            {
               SQLFragment sql = new SQLFragment("(");
               sql.add(sfw.where);
               sql.add(") AND (");
               sql.add(newWhere);
               sql.add(")");
               sfw.where = sql;
            }
            else
               sfw.where = newWhere;
            return sfw;
         } catch(TypedValueVisitorException e)
         {
            e.printStackTrace();
         }
         
      }
      return null;
   }

   @Override
   public <T, U> SQLQuery<U> select(SQLQuery<T> query, int lambdaThisIndex,
         JinqStream.Select<T, U> select, Object emSource)
   {
      SerializedLambda s = SerializedLambda.extractLambda(select);
      if (s == null) return null;
      MethodAnalysisResults analysis = runtimeAnalyzer.analyzeLambda(s);
      if (analysis == null) return null;
      return select(query, lambdaThisIndex, select, emSource, analysis, s);
   }

   public <T, U> SQLQuery<U> select(SQLQuery<T> query, int lambdaThisIndex, Select<T, U> select, Object emSource)
   {
      MethodAnalysisResults analysis = null;
      SerializedLambda s = SerializedLambda.extractLambda(select);
      if (s != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(s);
         if (analysis == null) return null;
      }
      else
      {
         String className = Type.getInternalName(select.getClass());
         if (!selectAnalysis.containsKey(className)) return null;
         analysis = selectAnalysis.get(className);
      }
      return select(query, lambdaThisIndex, select, emSource, analysis, s);
   }

   private <T, U> SQLQuery<U> select(SQLQuery<T> query, int lambdaThisIndex,
         Object select, Object emSource, MethodAnalysisResults analysis,
         SerializedLambda s)
   {
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      if (query instanceof SQLQuery.SelectFromWhere)
      {
         final SQLQuery.SelectFromWhere<T> sfw = (SQLQuery.SelectFromWhere<T>)query;
         SymbExToSQLGenerator gen = 
            new SQLGeneratorWithParams<Object, T>(entityInfo, 
                  SelectFromWhereExtensionArg.fromSfw(sfw, ParamsToJava8LambdaDescription.fromSerializedLambda(s), lambdaThisIndex), 
                  new ParamHandler<Object>(lambdaThisIndex, select, emSource),
                  SelectFromWhereExtensionJoin.fromSfw(sfw, (EntityManagerBackdoor)emSource),
                  this);

         try {
            List<SQLFragment> columns = new ArrayList<SQLFragment>();
            sfw.reader = generateSelect(analysis, gen, columns);
            sfw.columns = columns;
            return (SQLQuery<U>)sfw;
         } catch(TypedValueVisitorException e)
         {
            e.printStackTrace();
         }
         
      }
      return null;
   }

   private SQLReader generateSelect(MethodAnalysisResults analysis,
                                    SymbExToSQLGenerator gen,
                                    List<SQLFragment> columns)
         throws TypedValueVisitorException
   {
      SQLFragment [] wheres = new SQLFragment[analysis.paths.size()];
      SQLColumnValues[] selects = new SQLColumnValues[analysis.paths.size()];
      for (int n = 0; n < analysis.paths.size(); n++)
      {
         PathAnalysis<QueryllPathAnalysisSupplementalInfo> path = analysis.paths.get(n);
         wheres[n] = new SQLFragment();
         selects[n] = gen.generateFor(path.getSimplifiedReturnValue());
         for (TypedValue condition: path.getSimplifiedConditions())
         {
            if (condition instanceof ConstantValue.IntegerConstant
                  && ((ConstantValue.IntegerConstant)condition).val != 0)
               continue;
            if (condition instanceof ConstantValue.IntegerConstant
                  && ((ConstantValue.IntegerConstant)condition).val == 0)
            {
               if (!wheres[n].isEmpty())
                  wheres[n].add(" AND ");
               wheres[n].add("(1<>1)");
            }
            else
            {
               SQLColumnValues condColVals = gen.generateFor(condition);
               assert(condColVals.getNumColumns() == 1);
               if (!wheres[n].isEmpty())
                  wheres[n].add(" AND ");
               wheres[n].add("(");
               wheres[n].add(condColVals.columns[0]);
               wheres[n].add(")");
            }
         }
      }
      
      // TODO: Check that return type and number of columns is the same
      // for all paths
      SQLReader reader = selects[0].reader;
      if (wheres.length > 1)
      {
         for (int n = 0; n < selects[0].columns.length; n++)
         {
            SQLFragment col = new SQLFragment();
            boolean isAllSame = true;
            for (SQLColumnValues pathSql: selects)
            {
               if (!pathSql.columns[n].equals(selects[0].columns[n]))
                  isAllSame = false;
            }
            if (!isAllSame)
            {
               col.add("CASE");
               for (int i = 0; i < selects.length - 1; i++)
               {
                  col.add(" WHEN ");
                  if (wheres[i].isEmpty())
                     col.add("(1=1)");
                  else
                     col.add(wheres[i]);
                  col.add(" THEN ");
                  col.add(selects[i].columns[n]);
               }
               col.add(" ELSE ");
               col.add(selects[selects.length-1].columns[n]);
               col.add(" END");
            }
            else
               col = selects[0].columns[n];
            columns.add(col);
         }
      }
      else
      {
         columns.addAll(Arrays.asList(selects[0].columns));
      }
      return reader;
   }

   // If a subquery is defined using a Java 8 lambda, this returns the method underlying
   // that lambda.
   Handle getLambdaMethodHandle(TypedValue lambda)
   {
      if (!(lambda instanceof LambdaFactory)) return null;
      return ((LambdaFactory)lambda).getLambdaMethod();
   }
   
   // Returns the name of the class implementing the subquery lambda (or null if one cannot be resolved
   // or null if Java 8 lambdas were used).
   String getLambdaClassName(TypedValue lambda)
   {
      if (!(lambda instanceof MethodCallValue.VirtualMethodCallValue)) return null;
      MethodCallValue.VirtualMethodCallValue constructor = (MethodCallValue.VirtualMethodCallValue)lambda;
      if (!constructor.isConstructor()) return null;
      return constructor.owner;
   }

   public <T,U,V> SQLQuery<U> handleSimpleAggregate(SQLQuery<T> query,
                                                  MethodAnalysisResults analysis,
                                                  String aggregateOperator,
                                                  Class desiredSQLReader,
                                                  ParamsToJava8LambdaDescription java8LambdaParams,
                                                  ParamHandler<Object> paramHandler,
                                                  EntityManagerBackdoor emSource
                                                  )
   {
      try {
         if (query instanceof SQLQuery.SelectFromWhere)
         {
            final SQLQuery.SelectFromWhere<T> sfw = (SQLQuery.SelectFromWhere<T>)query;
            SymbExToSQLGenerator gen = 
               new SQLGeneratorWithParams<Object, T>(entityInfo, 
                     SelectFromWhereExtensionArg.fromSfw(sfw, java8LambdaParams, -1),
                     paramHandler,
                     SelectFromWhereExtensionJoin.fromSfw(sfw, (EntityManagerBackdoor)emSource),
                     this);
            List<SQLFragment> columns = new ArrayList<SQLFragment>();
            sfw.reader = generateSelect(analysis, gen, columns);
            assert(desiredSQLReader == null || desiredSQLReader.isInstance(sfw.reader));
            assert(columns.size() == 1);
            SQLFragment summedColumn = new SQLFragment();
            summedColumn.add(aggregateOperator + "(");
            summedColumn.add(columns.get(0));
            summedColumn.add(")");
            sfw.columns = new ArrayList<SQLFragment>();
            sfw.columns.add(summedColumn);
            return (SQLQuery<U>)sfw;
         }
         else if (query instanceof SQLQuery.InternalGroup)
         {
            final SQLQuery.InternalGroup<T> ig = (SQLQuery.InternalGroup<T>)query;
            SymbExToSQLGenerator gen = 
               new SQLGeneratorWithParams<Object, T>(entityInfo, 
                     SelectFromWhereExtensionArg.fromReaderColumns(ig.reader, ig.columns, java8LambdaParams, -1), 
                     null,//new ParamHandler<Object>(transformClass, emSource),
                     null,//SelectFromWhereExtensionJoin.fromSfw(sfw),
                     this);
            List<SQLFragment> columns = new ArrayList<SQLFragment>();
            ig.reader = generateSelect(analysis, gen, columns);
            assert(desiredSQLReader.isInstance(ig.reader));
            assert(columns.size() == 1);
            SQLFragment summedColumn = new SQLFragment();
            summedColumn.add(aggregateOperator + "(");
            summedColumn.add(columns.get(0));
            summedColumn.add(")");
            ig.columns = new ArrayList<SQLFragment>();
            ig.columns.add(summedColumn);
            return (SQLQuery<U>)ig;
         }
      } catch(TypedValueVisitorException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   public <T,U> SQLQuery<U> handleSimpleAggregate(SQLQuery<T> query,
                                                  MethodAnalysisResults analysis,
                                                  int lambdaThisIndex, 
                                                  Object transformClass,
                                                  String aggregateOperator,
                                                  Class desiredSQLReader,
                                                  Object emSource
                                                  )
   {
      return handleSimpleAggregate(query, analysis, aggregateOperator, desiredSQLReader, ParamsToJava8LambdaDescription.fromLambda(transformClass), new ParamHandler<Object>(lambdaThisIndex, transformClass, emSource), (EntityManagerBackdoor)emSource);
   }

   MethodAnalysisResults analyzeSimpleAggregateLambda(Object aggregate, Map<String, MethodAnalysisResults> aggregateAnalysisBaseClasses)
   {
      MethodAnalysisResults analysis = null;
      SerializedLambda s = SerializedLambda.extractLambda(aggregate);
      if (s != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(s);
         if (analysis == null) return null;
      }
      else
      {
         String className = Type.getInternalName(aggregate.getClass());
         if (!aggregateAnalysisBaseClasses.containsKey(className)) return null;
         analysis = aggregateAnalysisBaseClasses.get(className);
      }
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      return analysis;
   }
   
   public <T> SQLQuery<Integer> sumInt(SQLQuery<T> query,
                                       TypedValue lambda,
                                       EntityManagerBackdoor emSource) throws TypedValueVisitorException
   {
      MethodAnalysisResults analysis;
      String className = getLambdaClassName(lambda);
      Handle methodHandle = getLambdaMethodHandle(lambda);
      if (className != null)
      {
         if (!aggregateIntegerAnalysis.containsKey(className)) return null;
         analysis = aggregateIntegerAnalysis.get(className);
      }
      else if (methodHandle != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(methodHandle.getOwner(), methodHandle.getName(), methodHandle.getDesc());
         if (analysis == null) return null;
      }
      else
         return null;
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      // TODO: Need a proper param handler here
      return handleSimpleAggregate(query, analysis, "SUM", IntegerSQLReader.class, new ParamsToJava8LambdaDescription(), null, emSource);
   }

   @Override
   public <T, U extends Number & Comparable<U>, V> SQLQuery<V> sum(
         SQLQuery<T> query, int lambdaThisIndex, CollectNumber<T, U> aggregate,
         Class<U> collectClass,
         Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSimpleAggregateLambda(aggregate, aggregateIntegerAnalysis);
      if (analysis == null) return null;
      if (collectClass.equals(Double.class))
         return handleSimpleAggregate(query, analysis, lambdaThisIndex, aggregate, "SUM", DoubleSQLReader.class, emSource);
      else
         return handleSimpleAggregate(query, analysis, lambdaThisIndex, aggregate, "SUM", IntegerSQLReader.class, emSource);
   }

   public <T> SQLQuery<Integer> sumInt(SQLQuery<T> query, int lambdaThisIndex, 
                                       AggregateInteger<T> aggregate, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSimpleAggregateLambda(aggregate, aggregateIntegerAnalysis);
      if (analysis == null) return null;
      return handleSimpleAggregate(query, analysis, lambdaThisIndex, aggregate, "SUM", IntegerSQLReader.class, emSource);
   }

   public <T> SQLQuery<Double> sumDouble(SQLQuery<T> query, int lambdaThisIndex, 
                                         AggregateDouble<T> aggregate, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSimpleAggregateLambda(aggregate, aggregateDoubleAnalysis);
      if (analysis == null) return null;
      return handleSimpleAggregate(query, analysis, lambdaThisIndex, aggregate, "SUM", DoubleSQLReader.class, emSource);
   }

   public <T> SQLQuery<Integer> maxInt(SQLQuery<T> query,
                                       TypedValue lambda,
                                       EntityManagerBackdoor emSource) throws TypedValueVisitorException
   {
      MethodAnalysisResults analysis;
      String className = getLambdaClassName(lambda);
      Handle methodHandle = getLambdaMethodHandle(lambda);
      if (className != null) 
      {
         if (!aggregateIntegerAnalysis.containsKey(className)) return null;
         analysis = aggregateIntegerAnalysis.get(className);
      }
      else if (methodHandle != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(methodHandle.getOwner(), methodHandle.getName(), methodHandle.getDesc());
         if (analysis == null) return null;
      }
      else
         return null;
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      // TODO: Need a proper param handler here
      return handleSimpleAggregate(query, analysis, "MAX", IntegerSQLReader.class, null, null, emSource);
   }
   public <T, V extends Comparable<V>> SQLQuery<V> max(SQLQuery<T> query,
         TypedValue lambda,
         EntityManagerBackdoor emSource) throws TypedValueVisitorException
   {
      MethodAnalysisResults analysis;
      String className = getLambdaClassName(lambda);
      Handle methodHandle = getLambdaMethodHandle(lambda);
      if (className != null) 
      {
         if (!aggregateIntegerAnalysis.containsKey(className)) return null;
         analysis = aggregateIntegerAnalysis.get(className);
      }
      else if (methodHandle != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(methodHandle.getOwner(), methodHandle.getName(), methodHandle.getDesc());
         if (analysis == null) return null;
      }
      else
         return null;
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      // TODO: Need a proper param handler here
      return handleSimpleAggregate(query, analysis, "MAX", null, null, null, emSource);
   }

   public <T, V extends Comparable<V>> SQLQuery<V> min(SQLQuery<T> query,
         TypedValue lambda,
         EntityManagerBackdoor emSource) throws TypedValueVisitorException
   {
      MethodAnalysisResults analysis;
      String className = getLambdaClassName(lambda);
      Handle methodHandle = getLambdaMethodHandle(lambda);
      if (className != null) 
      {
         if (!aggregateIntegerAnalysis.containsKey(className)) return null;
         analysis = aggregateIntegerAnalysis.get(className);
      }
      else if (methodHandle != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(methodHandle.getOwner(), methodHandle.getName(), methodHandle.getDesc());
         if (analysis == null) return null;
      }
      else
         return null;
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      // TODO: Need a proper param handler here
      return handleSimpleAggregate(query, analysis, "MIN", null, null, null, emSource);
   }


   public <T> SQLQuery<Integer> maxInt(SQLQuery<T> query, int lambdaThisIndex, 
                                       AggregateInteger<T> aggregate, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSimpleAggregateLambda(aggregate, aggregateIntegerAnalysis);
      if (analysis == null) return null;
      return handleSimpleAggregate(query, analysis, lambdaThisIndex, aggregate, "MAX", IntegerSQLReader.class, emSource);
   }

   public <T, V extends Comparable<V>> SQLQuery<V> max(SQLQuery<T> query,
         int lambdaThisIndex,
         JinqStream.CollectComparable<T, V> aggregate,
         Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSimpleAggregateLambda(aggregate, aggregateDoubleAnalysis);
      if (analysis == null) return null;
      return handleSimpleAggregate(query, analysis, lambdaThisIndex, aggregate, "MAX", DoubleSQLReader.class, emSource);
   }

   public <T, V extends Comparable<V>> SQLQuery<V> min(SQLQuery<T> query,
         int lambdaThisIndex,
         JinqStream.CollectComparable<T, V> aggregate,
         Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSimpleAggregateLambda(aggregate, aggregateDoubleAnalysis);
      if (analysis == null) return null;
      return handleSimpleAggregate(query, analysis, lambdaThisIndex, aggregate, "MAX", DoubleSQLReader.class, emSource);
   }
   
   public <T> SQLQuery<Double> maxDouble(SQLQuery<T> query, int lambdaThisIndex, 
                                         AggregateDouble<T> aggregate, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSimpleAggregateLambda(aggregate, aggregateDoubleAnalysis);
      if (analysis == null) return null;
      return handleSimpleAggregate(query, analysis, lambdaThisIndex, aggregate, "MAX", DoubleSQLReader.class, emSource);
   }

   @Override
   public <T, U> SQLQuery<Pair<T, U>> join(SQLQuery<T> query,
         int lambdaThisIndex, JinqStream.Join<T, U> join,
         Object emSource)
   {
      SerializedLambda s = SerializedLambda.extractLambda(join);
      if (s == null) return null;
      MethodAnalysisResults analysis = runtimeAnalyzer.analyzeLambda(s);
      if (analysis == null) return null;
      return join(query, lambdaThisIndex, join, emSource, analysis, s);
   }
   
   public <T, U> SQLQuery<Pair<T, U>> join(SQLQuery<T> query, int lambdaThisIndex, Join<T, U> join, Object emSource)
   {
      SerializedLambda s = SerializedLambda.extractLambda(join);
      MethodAnalysisResults analysis;
      if (s != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(s);
         if (analysis == null) return null;
      }
      else
      {
         String className = Type.getInternalName(join.getClass());
         if (!joinAnalysis.containsKey(className)) return null;
         analysis = joinAnalysis.get(className);
      }
      return join(query, lambdaThisIndex, join, emSource, analysis, s);
   }

   private <T, U> SQLQuery<Pair<T, U>> join(SQLQuery<T> query,
         int lambdaThisIndex, Object join, Object emSource,
         MethodAnalysisResults analysis, SerializedLambda s)
   {
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      if (query instanceof SQLQuery.SelectFromWhere)
      {
         final SQLQuery.SelectFromWhere<T> sfw = (SQLQuery.SelectFromWhere<T>)query;
         SymbExToSubQueryGenerator gen = 
            new SubQueryGeneratorWithParams<Object, T>(entityInfo, 
                  SelectFromWhereExtensionArg.fromSfw(sfw, ParamsToJava8LambdaDescription.fromSerializedLambda(s), lambdaThisIndex), 
                  new ParamHandler<Object>(lambdaThisIndex, join, emSource),
                  SelectFromWhereExtensionJoin.fromSfw(sfw, (EntityManagerBackdoor)emSource),
                  this);
         try {
            if (analysis.paths.size() > 1) return null;
            PathAnalysis<QueryllPathAnalysisSupplementalInfo> path = analysis.paths.get(0);
            SQLQuery subquery = gen.generateFor(path.getSimplifiedReturnValue());
            if (subquery instanceof SQLQuery.SelectFromWhere)
            {
               final SQLQuery.SelectFromWhere<T> subsfw = (SQLQuery.SelectFromWhere<T>)subquery;
               // TODO: Is this sufficient for finding out if a subquery can
               // be safely put into a FROM clause (i.e. the part put into the
               // FROM clause does not use values from the SELECT or elsewhere)
               for (SQLFrom from: subsfw.from)
                  if (!(from instanceof SQLFrom.SQLFromTable))
                     return null;
               sfw.from.addAll(subsfw.from);
               sfw.columns.addAll(subsfw.columns);
               sfw.reader = new SQLReader.PairSQLReader(
                     sfw.getReader(), subsfw.getReader());
               return (SQLQuery<Pair<T, U>>)sfw;
            }
         } catch(TypedValueVisitorException e)
         {
            e.printStackTrace();
         }
      }
      return null;
   }

   @Override
   public <T, U> SQLQuery<U> selectAggregates(SQLQuery<T> query,
         int lambdaThisIndex, JinqStream.AggregateSelect<T, U> select, Object emSource)
   {
      SerializedLambda s = SerializedLambda.extractLambda(select);
      if (s == null) return null;
      MethodAnalysisResults analysis = runtimeAnalyzer.analyzeLambda(s);
      if (analysis == null) return null;
      return selectAggregates(query, lambdaThisIndex, select, analysis, emSource);
   }

   public <T,U> SQLQuery<U> selectAggregates(SQLQuery<T> query, int lambdaThisIndex, DBSet.AggregateSelect<T,U> select, Object emSource)
   {
      SerializedLambda s = SerializedLambda.extractLambda(select);
      MethodAnalysisResults analysis;
      if (s != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(s);
         if (analysis == null) return null;
      }
      else
      {
         String className = Type.getInternalName(select.getClass());
         if (!aggregateSelectAnalysis.containsKey(className)) return null;
         analysis = aggregateSelectAnalysis.get(className);
      }
      return selectAggregates(query, lambdaThisIndex, select, analysis, emSource);
   }

   private <T, U> SQLQuery<U> selectAggregates(SQLQuery<T> query,
         int lambdaThisIndex, Object select,
         MethodAnalysisResults analysis, Object emSource)
   {
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      if (query instanceof SQLQuery.SelectFromWhere)
      {
         final SQLQuery.SelectFromWhere<T> sfw = (SQLQuery.SelectFromWhere<T>)query;
         SymbExToSQLGenerator gen = 
            new SQLGeneratorWithParams<Object, T>(entityInfo, 
                  SelectFromWhereExtensionSelectAggregatesArg.fromSfw(sfw), 
                  new ParamHandler<Object>(lambdaThisIndex, select, emSource),
                  SelectFromWhereExtensionJoin.fromSfw(sfw, (EntityManagerBackdoor)emSource),
                  this);

         try {
            List<SQLFragment> columns = new ArrayList<SQLFragment>();
            sfw.reader = generateSelect(analysis, gen, columns);
            sfw.columns = columns;
            return (SQLQuery<U>)sfw;
         } catch(TypedValueVisitorException e)
         {
            e.printStackTrace();
         }
      }
      return null;
   }
   
   @Override
   public <T, U extends Tuple> SQLQuery<U> multiaggregate(SQLQuery<T> query,
         int lambdaThisIndex, AggregateSelect<T, ?>[] aggregates,
         Object emSource)
   {
      SerializedLambda [] s = new SerializedLambda[aggregates.length];
      MethodAnalysisResults [] analyses = new MethodAnalysisResults[aggregates.length];
      for (int n = 0; n < aggregates.length; n++)
      {
         s[n] = SerializedLambda.extractLambda(aggregates[n]);
         if (s[n] == null) return null;
         analyses[n] = runtimeAnalyzer.analyzeLambda(s[n]);
         if (analyses[n] == null) return null;
      }
      
      for (int n = 0; n < aggregates.length; n++)
         if (!doRuntimeCheckForSideEffects(analyses[n])) return null;
      if (query instanceof SQLQuery.SelectFromWhere)
      {
         final SQLQuery.SelectFromWhere sfw = (SQLQuery.SelectFromWhere<?>)query;
         // TODO: This probably isn't quite right because it might accept some forms that
         // it shouldn't, but I'll look at this again later when I rewrite the the query
         // builder.
         List<SQLFragment> [] columns = new ArrayList[aggregates.length];
         SQLReader<?> [] readers = new SQLReader[aggregates.length];
         for (int n = 0; n < aggregates.length; n++)
         {
            SymbExToSQLGenerator gen = 
                  new SQLGeneratorWithParams<Object, T>(entityInfo, 
                        SelectFromWhereExtensionSelectAggregatesArg.fromSfw(sfw), 
                        new ParamHandler<Object>(lambdaThisIndex + n, aggregates[n], emSource),
                        SelectFromWhereExtensionJoin.fromSfw(sfw, (EntityManagerBackdoor)emSource),
                        this);

            try {
               columns[n] = new ArrayList<SQLFragment>();
               readers[n] = generateSelect(analyses[n], gen, columns[n]);
            } catch(TypedValueVisitorException e)
            {
               e.printStackTrace();
               return null;
            }
         }
         sfw.reader = SQLReader.TupleSQLReader.createReaderForTuple(readers);
         ArrayList<SQLFragment> finalColumns = new ArrayList<>();
         for (int n = 0; n < aggregates.length; n++)
            finalColumns.addAll(columns[n]);
         sfw.columns = finalColumns;
         return (SQLQuery<U>)sfw;
      }
      return null;
   }

   @Override
   public <T, U, V> SQLQuery<Pair<U, V>> group(SQLQuery<T> query,
         int lambdaSelectThisIndex,
         JinqStream.Select<T, U> select,
         int lambdaAggregateThisIndex,
         JinqStream.AggregateGroup<U, T, V> aggregate,
         Object emSource)
   {
      SerializedLambda sSelect = SerializedLambda.extractLambda(select);
      if (sSelect == null) return null;
      MethodAnalysisResults analysisForSelect = runtimeAnalyzer.analyzeLambda(sSelect);
      if (analysisForSelect == null) return null;

      SerializedLambda sAggregate = SerializedLambda.extractLambda(aggregate);
      if (sAggregate == null) return null;
      MethodAnalysisResults analysisForGroup = runtimeAnalyzer.analyzeLambda(sAggregate);
      if (analysisForGroup == null) return null;
      return group(query, 
            lambdaSelectThisIndex, select, analysisForSelect, sSelect,
            lambdaAggregateThisIndex, aggregate, analysisForGroup, sAggregate,
            emSource);
   }

   public <T, U, V> SQLQuery<Pair<U, V>> group(SQLQuery<T> query,
                                               int lambdaSelectThisIndex, 
                                               Select<T, U> select,
                                               int lambdaAggregateThisIndex, 
                                               AggregateGroup<U, T, V> aggregate,
                                               Object emSource)
   {
      SerializedLambda sSelect = SerializedLambda.extractLambda(select);
      MethodAnalysisResults analysisForSelect;
      if (sSelect != null)
      {
         analysisForSelect = runtimeAnalyzer.analyzeLambda(sSelect);
         if (analysisForSelect == null) return null;
      }
      else
      {
         String selectClassName = Type.getInternalName(select.getClass());
         if (!selectAnalysis.containsKey(selectClassName)) return null;
         analysisForSelect = selectAnalysis.get(selectClassName);
      }

      SerializedLambda sAggregate = SerializedLambda.extractLambda(aggregate);
      MethodAnalysisResults analysisForGroup;
      if (sAggregate != null)
      {
         analysisForGroup = runtimeAnalyzer.analyzeLambda(sAggregate);
         if (analysisForGroup == null) return null;
      }
      else
      {
         String groupClassName = Type.getInternalName(aggregate.getClass());
         if (!groupAnalysis.containsKey(groupClassName)) return null;
         analysisForGroup = groupAnalysis.get(groupClassName);
      }
      return group(query, 
            lambdaSelectThisIndex, select, analysisForSelect, sSelect,
            lambdaAggregateThisIndex, aggregate, analysisForGroup, sAggregate,
            emSource);
   }

   private <T, U, V> SQLQuery<Pair<U, V>> group(SQLQuery<T> query,
         int lambdaSelectThisIndex, Object select, MethodAnalysisResults analysisForSelect, SerializedLambda sSelect,
         int lambdaAggregateThisIndex, Object aggregate, MethodAnalysisResults analysisForGroup, SerializedLambda sAggregate,
         Object emSource)
   {
      if (!doRuntimeCheckForSideEffects(analysisForSelect)) return null;
      if (!doRuntimeCheckForSideEffects(analysisForGroup)) return null;

      try {
         if (query instanceof SQLQuery.SelectFromWhere)
         {
            final SQLQuery.SelectFromWhere<T> sfw = (SQLQuery.SelectFromWhere<T>)query;
            SymbExToSQLGenerator genForSelect = 
               new SQLGeneratorWithParams<Object, T>(entityInfo, 
                     SelectFromWhereExtensionArg.fromSfw(sfw, ParamsToJava8LambdaDescription.fromSerializedLambda(sSelect), lambdaSelectThisIndex), 
                     new ParamHandler<Object>(lambdaSelectThisIndex, select, emSource),
                     SelectFromWhereExtensionJoin.fromSfw(sfw, (EntityManagerBackdoor)emSource),
                     this);
   
            List<SQLFragment> columnsForSelect = new ArrayList<SQLFragment>();
            SQLReader<U> readerForSelect = generateSelect(analysisForSelect, genForSelect, columnsForSelect);
            
            SymbExToSQLGenerator genForGroup = 
               new SQLGeneratorWithParams<Object, T>(entityInfo, 
                     SelectFromWhereExtensionGroupArg.fromSfw(readerForSelect, columnsForSelect, sfw, sAggregate), 
                     new ParamHandler<Object>(lambdaAggregateThisIndex, aggregate, emSource),
                     SelectFromWhereExtensionJoin.fromSfw(sfw, (EntityManagerBackdoor)emSource),
                     this);
            List<SQLFragment> columnsForGroup = new ArrayList<SQLFragment>();
            SQLReader<V> readerForGroup = generateSelect(analysisForGroup, genForGroup, columnsForGroup);

            SQLQuery.SelectFromWhereGroup<U,V> toReturn = new SQLQuery.SelectFromWhereGroup<U,V>(new SQLReader.PairSQLReader(readerForSelect, readerForGroup));
            sfw.reader = new SQLReader.PairSQLReader(readerForSelect, readerForGroup);
            sfw.columns = new ArrayList<SQLFragment>();
            sfw.columns.addAll(columnsForSelect);
            sfw.columns.addAll(columnsForGroup);
            toReturn.sfw = (SelectFromWhere<Pair<U, V>>) sfw;
            toReturn.groupColumns = columnsForSelect;
            return toReturn;
         }
      } catch(TypedValueVisitorException e)
      {
         e.printStackTrace();
      }
      
      return null;
   }

   public <T> SQLQuery<T> firstN(SQLQuery<T> query, int n, Object emSource)
   {
      if (query instanceof SQLQuery.SortedAndLimited)
      {
         SQLQuery.SortedAndLimited<T> sortLimit = (SQLQuery.SortedAndLimited<T>)query;
         if (sortLimit.firstN == -1 || sortLimit.firstN > n)
            sortLimit.firstN = n;
         return query;
      }
      else if (query instanceof SQLQuery.SelectFromWhere
            || query instanceof SQLQuery.SelectFromWhereGroup)
      {
         SQLQuery.SortedAndLimited<T> toReturn = null;
         toReturn = new SQLQuery.SortedAndLimited<T>(query.reader);
         toReturn.sfw = query;
         toReturn.firstN = n;
         return toReturn;
      }
      return null;
   }

   <T> SQLQuery<T> handleSorter(
         SQLQuery<T> query, 
         MethodAnalysisResults analysis, 
         Object sorter, 
         boolean isAscending, 
         SymbExGetFieldHandler paramHandler,
         Class desiredSQLReader, 
         Object emSource)
   {
      try {
         if (query instanceof SQLQuery.SelectFromWhere
               || query instanceof SQLQuery.SelectFromWhereGroup
               || query instanceof SQLQuery.SortedAndLimited)
         {
            if (query instanceof SQLQuery.SortedAndLimited)
            {
               if (((SQLQuery.SortedAndLimited<T>)query).firstN != -1)
                  return null;  // cannot sort something that is limited
            }

            final SQLQuery.SelectFromWhereColumns sfw = (SQLQuery.SelectFromWhereColumns)query;
            SymbExToSQLGenerator gen = 
               new SQLGeneratorWithParams<Object, T>(entityInfo, 
                     SelectFromWhereExtensionArg.fromReaderColumns(query.reader, sfw.getColumns(), ParamsToJava8LambdaDescription.fromLambda(sorter), -1),
                     paramHandler,
                     null,//SelectFromWhereExtensionJoin.fromSfw(sfw),
                     this);
            List<SQLFragment> columns = new ArrayList<SQLFragment>();
            SQLReader reader = generateSelect(analysis, gen, columns);
            assert(desiredSQLReader == null || desiredSQLReader.isInstance(reader));
            assert(columns.size() == 1);

            SQLQuery.SortedAndLimited<T> toReturn = null;
            if (!(query instanceof SQLQuery.SortedAndLimited))
            {
               toReturn = new SQLQuery.SortedAndLimited<T>(query.reader);
               toReturn.sfw = query;
            }
            else
            {
               toReturn = (SQLQuery.SortedAndLimited<T>)query;
            }
            toReturn.sortColumns.add(columns.get(0));
            toReturn.sortIsAscending.add(isAscending);
            return toReturn;
         }
      } catch(TypedValueVisitorException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   MethodAnalysisResults analyzeSorterLambda(Object sorterLambda, Map<String, MethodAnalysisResults> preAnalyzedClasses)
   {
      SerializedLambda s = SerializedLambda.extractLambda(sorterLambda);
      MethodAnalysisResults analysis;
      if (s != null)
      {
         analysis = runtimeAnalyzer.analyzeLambda(s);
         if (analysis == null) return null;
      }
      else 
      {
         String className = Type.getInternalName(sorterLambda.getClass());
         if (!preAnalyzedClasses.containsKey(className)) return null;
         analysis = preAnalyzedClasses.get(className);
      }
      if (!doRuntimeCheckForSideEffects(analysis)) return null;
      return analysis;
   }

   @Override
   public <T, V extends Comparable<V>> SQLQuery<T> sortedBy(SQLQuery<T> query,
         int lambdaThisIndex, CollectComparable<T, V> sorter,
         boolean isAscending, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSorterLambda(sorter, new HashMap<>());
      return handleSorter(query, analysis, sorter, isAscending, new ParamHandler<Object>(lambdaThisIndex, sorter, emSource), null, emSource);
   }

   public <T> SQLQuery<T> sortedByDate(SQLQuery<T> query, int lambdaThisIndex, DateSorter<T> sorter,
         boolean isAscending, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSorterLambda(sorter, dateSorterAnalysis);
      return handleSorter(query, analysis, sorter, isAscending, new ParamHandler<Object>(lambdaThisIndex, sorter, emSource), SQLReader.DateSQLReader.class, emSource);
   }

   public <T> SQLQuery<T> sortedByDouble(SQLQuery<T> query,
         int lambdaThisIndex, DoubleSorter<T> sorter, boolean isAscending, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSorterLambda(sorter, doubleSorterAnalysis);
      return handleSorter(query, analysis, sorter, isAscending, new ParamHandler<Object>(lambdaThisIndex, sorter, emSource), SQLReader.IntegerSQLReader.class, emSource);
   }

   public <T> SQLQuery<T> sortedByInt(SQLQuery<T> query, int lambdaThisIndex, IntSorter<T> sorter,
         boolean isAscending, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSorterLambda(sorter, intSorterAnalysis);
      return handleSorter(query, analysis, sorter, isAscending, new ParamHandler<Object>(lambdaThisIndex, sorter, emSource), SQLReader.DoubleSQLReader.class, emSource);
   }

   public <T> SQLQuery<T> sortedByString(SQLQuery<T> query,
         int lambdaThisIndex, StringSorter<T> sorter, boolean isAscending, Object emSource)
   {
      MethodAnalysisResults analysis = analyzeSorterLambda(sorter, stringSorterAnalysis);
      return handleSorter(query, analysis, sorter, isAscending, new ParamHandler<Object>(lambdaThisIndex, sorter, emSource), SQLReader.StringSQLReader.class, emSource);
   }
}
