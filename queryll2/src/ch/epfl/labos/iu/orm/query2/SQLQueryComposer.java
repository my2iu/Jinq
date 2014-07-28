package ch.epfl.labos.iu.orm.query2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.JinqStream.CollectNumber;
import org.jinq.tuples.Pair;

import ch.epfl.labos.iu.orm.DBSet.AggregateDouble;
import ch.epfl.labos.iu.orm.DBSet.AggregateGroup;
import ch.epfl.labos.iu.orm.DBSet.AggregateInteger;
import ch.epfl.labos.iu.orm.DBSet.AggregateSelect;
import ch.epfl.labos.iu.orm.DBSet.Join;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;
import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.QueryComposer;
import ch.epfl.labos.iu.orm.QueryComposerWithLists;
import ch.epfl.labos.iu.orm.StringSorter;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.query2.SQLReader.ArrayTupleSQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReader.DoubleSQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReader.IntegerSQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReader.PairSQLReader;

public class SQLQueryComposer<T> implements QueryComposerWithLists<T>
{
   SQLQueryComposer() {}
   SQLQueryComposer(Object emSource, JDBCConnectionInfo jdbc, SQLQueryTransforms transformer, SQLQuery<T> query, int nextLambdaParamIndex, Object[][]params)
   {
      this.nextLambdaParamIndex = nextLambdaParamIndex;
      this.emSource = (EntityManagerBackdoor)emSource;
      this.jdbc = jdbc;
      this.query = query;
      this.transformer = transformer;
      this.params = params;
   }
   // Constructor for reading an entire table of entities
   public SQLQueryComposer(Object emSource, JDBCConnectionInfo jdbc, SQLQueryTransforms transformer, SQLReader<T> reader, String[] columns, String table) 
   {
      this.emSource = (EntityManagerBackdoor)emSource;
      this.jdbc = jdbc;
      this.transformer = transformer;
      nextLambdaParamIndex = 0;
      params = new Object[0][];
      query = new SQLQuery.SelectFromWhere<T>(
            reader, columns, table);
   }
   
   int nextLambdaParamIndex;
   Object[][]params;
   EntityManagerBackdoor emSource;  // Actually, this emSource might be redundant given that we have a SQLQueryTransforms 
   JDBCConnectionInfo jdbc;
   SQLQuery<T> query;
   SQLQueryTransforms transformer;
   
   static class CachedQuery
   {
      CachedQuery(SQLQuery query)
      {
         this.query = query;
         this.paramsToSave = new List[0];
      }
      CachedQuery(SQLQuery query, List<ParameterLocation> paramLocs)
      {
         this.query = query;
         this.paramsToSave = new List[] {paramLocs};
      }
      CachedQuery(SQLQuery query, List<ParameterLocation> paramLocs1, List<ParameterLocation> paramLocs2)
      {
         this.query = query;
         this.paramsToSave = new List[] {paramLocs1, paramLocs2};
      }
      SQLQuery query;
      List<ParameterLocation>[] paramsToSave;
   }
   static class GeneratedCachedQuery
   {
      JDBCFragment sql;
      PreparedStatement stmt;  // TODO: not used currently because I'm too lazy to put in the code for cleaning them up properly
   }
   
   public String getDebugQueryString()
   {
      // TODO: Perhaps, the query should only be executed if the iterator is read, and not immediately.
      if (query == null) return null;
      try
      {
         JDBCFragment sql = null;
         if (emSource.isQueriesCached())
            sql = (JDBCFragment)emSource.getGeneratedQueryCacheEntry(query);
         if (sql == null)
         {
            JDBCQuerySetup setup = new JDBCQuerySetup();
            query.prepareQuery(setup);
            sql = query.generateQuery(setup);
            if (emSource.isQueriesCached())
               emSource.putGeneratedQueryCacheEntry(query, sql);
         }
         return sql.query;
      } catch (QueryGenerationException e)
      {
         e.printStackTrace();
      }
      return null;
   }
   
   public Iterator<T> executeAndReturnResultIterator(final Consumer<Throwable> exceptionReporter)
   {
      // TODO: Perhaps, the query should only be executed if the iterator is read, and not immediately.
      if (query == null)
         return Collections.emptyIterator();
      try
      {
         JDBCFragment sql = null;
         if (emSource.isQueriesCached())
            sql = (JDBCFragment)emSource.getGeneratedQueryCacheEntry(query);
         if (sql == null)
         {
            JDBCQuerySetup setup = new JDBCQuerySetup();
            query.prepareQuery(setup);
            sql = query.generateQuery(setup);
            if (emSource.isQueriesCached())
               emSource.putGeneratedQueryCacheEntry(query, sql);
         }
         if (jdbc.testOut != null)
         {
            jdbc.testOut.println(sql.query);
            jdbc.testOut.flush();
         }
         if (jdbc.connection != null)
         {
            PreparedStatement stmt = 
               jdbc.connection.prepareStatement(sql.query);
            for (int n = 0; n < sql.paramLinks.size(); n++)
               sql.paramLinks.get(n).configureParameters(stmt, params, n+1);
            final ResultSet rs = stmt.executeQuery();
            
            final SQLReader<T> reader = query.getReader();

            return new Iterator<T>()
               {
                  boolean hasMore = true;
                  boolean hasRead = false;
                  boolean hasClosed = false;
                  @Override public boolean hasNext()
                  {
                     try {
                        if (!hasRead)
                           hasMore = rs.next();
                        hasRead = true;
                        if (!hasMore) close();
                        return hasMore;
                     } catch (SQLException e) {
                        // TODO: Find a better way to handle these exceptions
                        hasMore = false;
                        close();
                        exceptionReporter.accept(e);
                        return false;
                     }
                  }
                  
                  public void close()
                  {
                     if (!hasClosed)
                     {
                        hasClosed = true;
                        try { rs.close(); } catch (SQLException e) {}
                        try { stmt.close(); } catch (SQLException e) {}
                     }
                  }

                  @Override public T next()
                  {
                     try {
                        if (!hasRead) 
                           hasMore = rs.next();
                        if (!hasMore) close();
                        if (!hasMore) throw new NoSuchElementException();
                        T toReturn = (T)reader.readData(rs, 1);
                        hasRead = false;
                        return toReturn;
                     } catch (SQLException e) {
                        // TODO: Find a better way to handle these exceptions
                        hasMore = false;
                        close();
                        exceptionReporter.accept(e);
                        return null;
                     }
                  }
            
               };
         }
      } catch (QueryGenerationException e)
      {
         e.printStackTrace();
         exceptionReporter.accept(e);
      }
      catch (SQLException e)
      {
         e.printStackTrace();
         exceptionReporter.accept(e);
      }
      // TODO: What to return here?
      return Collections.emptyIterator();
   }
   
   public VectorSet<T> createRealizedSet()
   {
      VectorSet<T> toReturn = new VectorSet<T>();
      if (query != null)
      {
         try
         {
            JDBCFragment sql = null;
            if (emSource.isQueriesCached())
               sql = (JDBCFragment)emSource.getGeneratedQueryCacheEntry(query);
            if (sql == null)
            {
               JDBCQuerySetup setup = new JDBCQuerySetup();
               query.prepareQuery(setup);
               sql = query.generateQuery(setup);
               if (emSource.isQueriesCached())
                  emSource.putGeneratedQueryCacheEntry(query, sql);
            }
            if (jdbc.testOut != null)
            {
               jdbc.testOut.println(sql.query);
               jdbc.testOut.flush();
            }
            if (jdbc.connection != null)
            {
               PreparedStatement stmt = 
                  jdbc.connection.prepareStatement(sql.query);
               for (int n = 0; n < sql.paramLinks.size(); n++)
                  sql.paramLinks.get(n).configureParameters(stmt, params, n+1);
               ResultSet rs = stmt.executeQuery();
               
               SQLReader<T> reader = query.getReader();
               while ( rs.next() ) {
                  toReturn.add((T)reader.readData(rs, 1));
               }
               try { rs.close(); } catch (SQLException e) {}
               try { stmt.close(); } catch (SQLException e) {}
            }
         } catch (QueryGenerationException e)
         {
            e.printStackTrace();
         }
         catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
      return toReturn;
   }
   
   // Evaluates a query that returns a single row only
   <U> U evaluateRowQuery(SQLQuery<U> query, Object[][]newParams)
   {
      U toReturn = null;
      if (query != null)
      {
         try
         {
            JDBCFragment sql = null;
            if (emSource.isQueriesCached())
               sql = (JDBCFragment)emSource.getGeneratedQueryCacheEntry(query);
            if (sql == null)
            {
               JDBCQuerySetup setup = new JDBCQuerySetup();
               query.prepareQuery(setup);
               sql = query.generateQuery(setup);
               if (emSource.isQueriesCached())
                  emSource.putGeneratedQueryCacheEntry(query, sql);
            }
            if (jdbc.testOut != null)
            {
               jdbc.testOut.println(sql.query);
               jdbc.testOut.flush();
            }
            if (jdbc.connection != null)
            {
               PreparedStatement stmt = 
                  jdbc.connection.prepareStatement(sql.query);
               for (int n = 0; n < sql.paramLinks.size(); n++)
                  sql.paramLinks.get(n).configureParameters(stmt, newParams, n+1);
               ResultSet rs = stmt.executeQuery();
               
               SQLReader<U> reader = query.getReader();
               if ( rs.next() ) {
                  toReturn = reader.readData(rs, 1);
               }
               try { rs.close(); } catch (SQLException e) {}
               try { stmt.close(); } catch (SQLException e) {}
            }
            else
            {
               // Insert dummy return values
               if (query.reader instanceof IntegerSQLReader)
                  toReturn = (U)Integer.valueOf(0);
               else if (query.reader instanceof DoubleSQLReader)
                  toReturn = (U)Double.valueOf(0);
               else if (query.reader instanceof PairSQLReader)
                  toReturn = (U) new Pair(null, null);
               else if (query.reader instanceof ArrayTupleSQLReader)
                  toReturn = (U) new Object[((ArrayTupleSQLReader)query.reader).subreaders.length];
            }
         } catch (QueryGenerationException e)
         {
            e.printStackTrace();
         }
         catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
      return toReturn;
   }
   
   static Object[][] gatherParams(Object[][] existingParams, List<ParameterLocation> newParams, Object lambda) throws QueryGenerationException
   {
      Object[][] toReturn = new Object[existingParams.length + 1][];
      System.arraycopy(existingParams, 0, toReturn, 0, existingParams.length);
      Object[] lambdaParams = new Object[newParams.size()];
      for (int n = 0; n < lambdaParams.length; n++)
         lambdaParams[n] = newParams.get(n).getParameter(lambda);
      toReturn[toReturn.length - 1] = lambdaParams;
      return toReturn;
   }
   
   private CachedQuery findQueryInCache(String context, Object lambda1, Object lambda2)
   {
      // TODO: Add support for caching queries constructed from Java 8 lambdas
      if (!emSource.isQueriesCached()) return null;

      String lambdaRep = "";
      if (lambda1 != null)
         lambdaRep += lambda1.getClass().getName();
      if (lambda2 != null)
         lambdaRep += lambda2.getClass().getName();
      return (CachedQuery)emSource.getQueryCacheEntry(context, query, lambdaRep);
   }
   
   private Object[][] gatherParamsForCachedQuery(CachedQuery lookup, Object lambda1, Object lambda2) throws QueryGenerationException
   {
      Object[][] newParams = params;
      assert(lookup.paramsToSave.length < 3);
      if (lookup.paramsToSave.length > 0)
         newParams = gatherParams(newParams, lookup.paramsToSave[0], lambda1);
      if (lookup.paramsToSave.length == 2 || lambda2 != null)
      {
         assert(lookup.paramsToSave.length == 2 && lambda2 != null);
         newParams = gatherParams(newParams, lookup.paramsToSave[1], lambda2);
      }
      return newParams;
   }

   private <U> QueryComposerWithLists<U> lookupQueryCache(String context, Object lambda1, Object lambda2)
   {
      CachedQuery lookup = findQueryInCache(context, lambda1, lambda2);
      if (lookup == null) return null;
      try {
         Object[][] newParams = gatherParamsForCachedQuery(lookup, lambda1, lambda2);
         return new SQLQueryComposer<>(emSource, jdbc, transformer, lookup.query, nextLambdaParamIndex + lookup.paramsToSave.length, newParams);
      } catch(QueryGenerationException e) {return null;}
   }

   private <U> U lookupQueryCacheRow(String context, Object lambda1, Object lambda2)
   {
      CachedQuery lookup = findQueryInCache(context, lambda1, lambda2);
      if (lookup == null) return null;
      try {
         Object[][] newParams = gatherParamsForCachedQuery(lookup, lambda1, lambda2);
         return evaluateRowQuery((SQLQuery<U>)lookup.query, newParams);
      } catch(QueryGenerationException e) {return null;}
   }

   private void storeInQueryCache(String context, SQLQuery cached, Object lambda1, List<ParameterLocation> params1, Object lambda2, List<ParameterLocation> params2)
   {
      if (!emSource.isQueriesCached()) return;
      String lambdaRep = "";
      if (lambda1 != null)
         lambdaRep += lambda1.getClass().getName();
      if (lambda2 != null)
         lambdaRep += lambda2.getClass().getName();
      CachedQuery cachedQuery;
      if (params2 != null)
         cachedQuery = new CachedQuery(cached, params1, params2);
      else if (params1 != null)
         cachedQuery = new CachedQuery(cached, params1);
      else
         cachedQuery = new CachedQuery(cached);
      emSource.putQueryCacheEntry(context, query, lambdaRep, cachedQuery);
   }
   
   private List<ParameterLocation> getAndStoreParamLinks(
         int lambdaParamIndex,
         SQLQuery<?> newQuery) throws QueryGenerationException
   {
      List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
      newQuery.storeParamLinks(lambdaParamIndex, paramLocs);
      return paramLocs;
   }
   
   private <U> U cacheQueryAndEvaluateRow(
         String context, 
         Object lambda,
         SQLQuery<U> newQuery,
         int lambdaParamIndex) 
   {
      try {
         List<ParameterLocation> paramLocs = getAndStoreParamLinks(lambdaParamIndex, newQuery);
         storeInQueryCache(context, newQuery, lambda, paramLocs, null, null);
         Object[][] newParams = gatherParams(params, paramLocs, lambda);
         return evaluateRowQuery(newQuery, newParams);
      } catch(QueryGenerationException e) {return null;}
   }
   
   private <U> QueryComposerWithLists<U> cacheQueryAndNewComposer(String context, 
         Object lambda1,
         Object lambda2,
         SQLQuery<U> newQuery, int lambdaParamIndex) 
   {
      try {
         if (lambda2 == null)
         {
            List<ParameterLocation> paramLocs = getAndStoreParamLinks(lambdaParamIndex, newQuery);
            storeInQueryCache(context, newQuery, lambda1, paramLocs, null, null);
            Object[][] newParams = gatherParams(params, paramLocs, lambda1);
            return new SQLQueryComposer<U>(emSource, jdbc, transformer, newQuery, lambdaParamIndex + 1, newParams);
         }
         else
         {
            List<ParameterLocation> paramLocs1 = getAndStoreParamLinks(lambdaParamIndex, newQuery);
            List<ParameterLocation> paramLocs2 = getAndStoreParamLinks(lambdaParamIndex + 1, newQuery);
            storeInQueryCache(context, newQuery, lambda1, paramLocs1, lambda2, paramLocs2);
            Object[][] newParams = gatherParams(gatherParams(params, paramLocs1, lambda1), paramLocs2, lambda2);
            return new SQLQueryComposer<U>(emSource, jdbc, transformer, newQuery, lambdaParamIndex + 2, newParams);
         }
      } catch(QueryGenerationException e) {return null;}
   }

   private <U, V, W> QueryComposerWithLists<U> composeQuery(String context, V lambda1, W lambda2, Supplier<SQLQuery<U>> transform)
   {
      if (transformer == null) return null;
      QueryComposerWithLists<U> cached = lookupQueryCache(context, lambda1, null);
      if (cached != null) return cached;
      SQLQuery<U> newQuery = transform.get();
      if (newQuery == null) return null;
      return cacheQueryAndNewComposer(context, lambda1, lambda2, newQuery, nextLambdaParamIndex);
   }

   private <U, V> U composeQueryRow(String context, V lambda, Supplier<SQLQuery<U>> transform)
   {
      if (transformer == null) return null;
      U cached = lookupQueryCacheRow(context, lambda, null);
      if (cached != null) return cached;
      SQLQuery<U> newQuery = transform.get();
      if (newQuery == null) return null;
      return cacheQueryAndEvaluateRow(context, lambda, newQuery, nextLambdaParamIndex);
   }

   public <U, V> QueryComposerWithLists<Pair<U, V>> group(Select<T, U> select,
                                                 AggregateGroup<U, T, V> aggregate) 
   {
      return composeQuery("group", select, aggregate, 
            () -> transformer.group(query.copy(), nextLambdaParamIndex, select, nextLambdaParamIndex + 1, aggregate, emSource));
   }

   public <U, V> QueryComposer<Pair<U, V>> group(JinqStream.Select<T, U> select,
         JinqStream.AggregateGroup<U, T, V> aggregate) 
   {
      return composeQuery("group", select, aggregate, 
            () -> transformer.group(query.copy(), nextLambdaParamIndex, select, nextLambdaParamIndex + 1, aggregate, emSource));
   }

   public <U> QueryComposerWithLists<Pair<T, U>> join(Join<T,U> join)
   {
      return composeQuery("join", join,
            null, () -> transformer.join(query.copy(), nextLambdaParamIndex, join, emSource));
   }

   public <U> QueryComposer<Pair<T, U>> join(JinqStream.Join<T,U> join)
   {
      return composeQuery("join", join,
            null, () -> transformer.join(query.copy(), nextLambdaParamIndex, join, emSource));
   }
   
   public <U> QueryComposer<Pair<T, U>> join(JinqStream.JoinWithSource<T,U> join)
   {
      // Not supported
      return null;
   }

   public <U> QueryComposerWithLists<U> select(Select<T, U> select)
   {
      return composeQuery("select", select,
            null, () -> transformer.select(query.copy(), nextLambdaParamIndex, select, emSource));
   }

   public <U> QueryComposer<U> select(JinqStream.Select<T, U> select)
   {
      return composeQuery("select", select,
            null, () -> transformer.select(query.copy(), nextLambdaParamIndex, select, emSource));
   }

   public QueryComposer<T> unique()
   {
      if (transformer == null) return null;
      // TODO Auto-generated method stub
      return null;
   }

   public QueryComposerWithLists<T> where(Where<T> test)
   {
      return composeQuery("where", test,
            null, () -> transformer.where(query.copy(), nextLambdaParamIndex, test, emSource));
   }

   public <E extends Exception> QueryComposer<T> where(JinqStream.Where<T, E> test)
   {
      return composeQuery("where", test,
            null, () -> transformer.where(query.copy(), nextLambdaParamIndex, test, emSource));
   }

   public QueryComposer<T> with(T toAdd)
   {
      if (transformer == null) return null;
      // TODO Auto-generated method stub
      return null;
   }

   public Long count()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public <V extends Number & Comparable<V>> Number sum(JinqStream.CollectNumber<T, V> aggregate, Class<V> collectClass)
   {
      if (collectClass.equals(Double.class))
      {
         JinqStream.CollectDouble<T> collect = (JinqStream.CollectDouble<T>) aggregate;
         return composeQueryRow("sumDouble", aggregate,
               () -> transformer.sum(query.copy(), nextLambdaParamIndex, collect, Double.class, emSource));
      }
      else if (collectClass.equals(Integer.class))
      {
         JinqStream.CollectInteger<T> collect = (JinqStream.CollectInteger<T>) aggregate;
         return ((Integer)composeQueryRow("sumInt", aggregate,
               () -> transformer.sum(query.copy(), nextLambdaParamIndex, collect, Integer.class, emSource))).longValue();
      }
      // TODO Auto-generated method stub
      return null;
   }

   public Double sumDouble(AggregateDouble<T> aggregate)
   {
      return composeQueryRow("sumDouble", aggregate,
            () -> transformer.sumDouble(query.copy(), nextLambdaParamIndex, aggregate, emSource));
   }

   public Integer sumInt(AggregateInteger<T> aggregate)
   {
      return composeQueryRow("sumInt", aggregate,
            () -> transformer.sumInt(query.copy(), nextLambdaParamIndex, aggregate, emSource));
   }
   
   public <V extends Comparable<V>> V max(JinqStream.CollectComparable<T, V> aggregate)
   {
      return composeQueryRow("max", aggregate,
            () -> transformer.max(query.copy(), nextLambdaParamIndex, aggregate, emSource));
   }
   
   public <V extends Comparable<V>> V min(JinqStream.CollectComparable<T, V> aggregate)
   {
      return composeQueryRow("max", aggregate,
            () -> transformer.min(query.copy(), nextLambdaParamIndex, aggregate, emSource));
   }

   @Override
   public <V extends Number & Comparable<V>> Double avg(
         CollectNumber<T, V> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Double maxDouble(AggregateDouble<T> aggregate)
   {
      return composeQueryRow("maxDouble", aggregate,
            () -> transformer.maxDouble(query.copy(), nextLambdaParamIndex, aggregate, emSource));
   }

   public Integer maxInt(AggregateInteger<T> aggregate)
   {
      return composeQueryRow("maxInt", aggregate,
            () -> transformer.maxInt(query.copy(), nextLambdaParamIndex, aggregate, emSource));
   }

   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      return composeQueryRow("selectAggregates", aggregate,
            () -> transformer.selectAggregates(query.copy(), nextLambdaParamIndex, aggregate, emSource));
   }

   public <U> U selectAggregates(JinqStream.AggregateSelect<T, U> aggregate)
   {
      return composeQueryRow("selectAggregates", aggregate,
            () -> transformer.selectAggregates(query.copy(), nextLambdaParamIndex, aggregate, emSource));
   }

   public Object[] multiaggregate(JinqStream.AggregateSelect<T, ?>[] aggregates)
   {
      // TODO: Implement caching for this.
      SQLQuery<Object[]> newQuery = transformer.multiaggregate(query.copy(), nextLambdaParamIndex, aggregates, emSource);
      if (newQuery == null) return null;
      List<ParameterLocation> [] paramLocs = new ArrayList[aggregates.length];
      Object[][] newParams = params;
      try {
         for (int n = 0; n < aggregates.length; n++)
            paramLocs[n] = getAndStoreParamLinks(nextLambdaParamIndex + n, newQuery);
         for (int n = 0; n < aggregates.length; n++)
            newParams = gatherParams(newParams, paramLocs[n], aggregates[n]);
      } catch (QueryGenerationException e)
      {
         return null;
      }
      return evaluateRowQuery(newQuery, newParams);
   }
   
   public QueryComposer<T> firstN(int n)
   {
      if (transformer == null) return null;
      QueryComposer<T> cached = lookupQueryCache("firstN", null, null);
      if (cached != null) return cached;
      SQLQuery<T> newQuery = transformer.firstN(query.copy(), n, emSource);
      if (newQuery == null) return null;
      // TODO: I'm not sure firstN can be cached using this framework because the "n" isn't properly 
      // treated as a parameter by the cache.
      storeInQueryCache("firstN", newQuery, null, null, null, null);
      return new SQLQueryComposer<T>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex, params);
   }

   public QueryComposer<T> sortedByDate(DateSorter<T> sorter,
                                        boolean isAscending)
   {
      return composeQuery(isAscending ? "sortedByDateAscending" : "sortedByDateDescending", sorter, 
            null, () -> transformer.sortedByDate(query.copy(), nextLambdaParamIndex, sorter, isAscending, emSource));
   }

   public QueryComposer<T> sortedByDouble(DoubleSorter<T> sorter,
                                          boolean isAscending)
   {
      return composeQuery(isAscending ? "sortedByDoubleAscending" : "sortedByDoubleDescending", sorter, 
            null, () -> transformer.sortedByDouble(query.copy(), nextLambdaParamIndex, sorter, isAscending, emSource));
   }

   public QueryComposer<T> sortedByInt(IntSorter<T> sorter, boolean isAscending)
   {
      return composeQuery(isAscending ? "sortedByIntAscending" : "sortedByIntDescending", sorter, 
            null, () -> transformer.sortedByInt(query.copy(), nextLambdaParamIndex, sorter, isAscending, emSource));
   }

   public QueryComposer<T> sortedByString(StringSorter<T> sorter,
                                          boolean isAscending)
   {
      return composeQuery(isAscending ? "sortedByStringAscending" : "sortedByStringDescending", sorter, 
            null, () -> transformer.sortedByString(query.copy(), nextLambdaParamIndex, sorter, isAscending, emSource));
   }
   
   public void setHint(String name, Object val)
   {
      // No hints
   }
}
