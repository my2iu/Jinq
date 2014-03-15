package ch.epfl.labos.iu.orm.query2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.QueryComposer;
import ch.epfl.labos.iu.orm.StringSorter;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.query2.SQLReader.DoubleSQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReader.IntegerSQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReader.PairSQLReader;

public class SQLQueryComposer<T> implements QueryComposer<T>
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
   
   public Iterator<T> executeAndReturnResultIterator()
   {
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
                        // TODO: Find a better way to handle this
                        throw new RuntimeException(e);
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
                        // TODO: Find a better way to handle this
                        throw new RuntimeException(e);
                     }
                  }
            
               };
         }
      } catch (QueryGenerationException e)
      {
         e.printStackTrace();
      }
      catch (SQLException e)
      {
         e.printStackTrace();
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

   private QueryComposer lookupQueryCache(String context, Object lambda1, Object lambda2)
   {
      if (!emSource.isQueriesCached()) return null;

      String lambdaRep = "";
      if (lambda1 != null)
         lambdaRep += lambda1.getClass().getName();
      if (lambda2 != null)
         lambdaRep += lambda2.getClass().getName();
      CachedQuery lookup = (CachedQuery)emSource.getQueryCacheEntry(context, query, lambdaRep);
      if (lookup != null)
      {
         try {
            Object[][] newParams = params;
            assert(lookup.paramsToSave.length < 3);
            if (lookup.paramsToSave.length > 0)
               newParams = gatherParams(newParams, lookup.paramsToSave[0], lambda1);
            if (lookup.paramsToSave.length == 2 || lambda2 != null)
            {
               assert(lookup.paramsToSave.length == 2 && lambda2 != null);
               newParams = gatherParams(newParams, lookup.paramsToSave[1], lambda2);
            }
            return new SQLQueryComposer(emSource, jdbc, transformer, lookup.query, nextLambdaParamIndex + lookup.paramsToSave.length, newParams);
         } catch(QueryGenerationException e) {return null;}
      }
      return null;
   }

   private <U> U lookupQueryCacheRow(String context, Object lambda1, Object lambda2)
   {
      if (!emSource.isQueriesCached()) return null;

      String lambdaRep = "";
      if (lambda1 != null)
         lambdaRep += lambda1.getClass().getName();
      if (lambda2 != null)
         lambdaRep += lambda2.getClass().getName();
      CachedQuery lookup = (CachedQuery)emSource.getQueryCacheEntry(context, query, lambdaRep);
      if (lookup != null)
      {
         try {
            Object[][] newParams = params;
            assert(lookup.paramsToSave.length < 3);
            if (lookup.paramsToSave.length > 0)
               newParams = gatherParams(newParams, lookup.paramsToSave[0], lambda1);
            if (lookup.paramsToSave.length == 2 || lambda2 != null)
            {
               assert(lookup.paramsToSave.length == 2 && lambda2 != null);
               newParams = gatherParams(newParams, lookup.paramsToSave[1], lambda2);
            }
            return evaluateRowQuery((SQLQuery<U>)lookup.query, newParams);
         } catch(QueryGenerationException e) {return null;}
      }
      return null;
   }

   private void storeInQueryCache(String context, SQLQuery cached, Object lambda1, List<ParameterLocation> params1, Object lambda2, List<ParameterLocation> params2)
   {
      if (!emSource.isQueriesCached()) return;
      String lambdaRep = "";
      if (lambda1 != null)
         lambdaRep += lambda1.getClass().getName();
      if (lambda2 != null)
         lambdaRep += lambda2.getClass().getName();
      List<ParameterLocation>[] paramLocs;
      CachedQuery cachedQuery;
      if (params2 != null)
         cachedQuery = new CachedQuery(cached, params1, params2);
      else if (params1 != null)
         cachedQuery = new CachedQuery(cached, params1);
      else
         cachedQuery = new CachedQuery(cached);
      emSource.putQueryCacheEntry(context, query, lambdaRep, cachedQuery);
   }
   
   public <U, V> QueryComposer<Pair<U, V>> group(Select<T, U> select,
                                                 AggregateGroup<U, T, V> aggregate) 
   {
      try {
         if (transformer == null) return null;
         QueryComposer cached = lookupQueryCache("group", select, aggregate);
         if (cached != null) return cached;
         SQLQuery<Pair<U,V>> newQuery = transformer.group(query.copy(), nextLambdaParamIndex, select, nextLambdaParamIndex + 1, aggregate, emSource);
         List<ParameterLocation> paramLocSelect = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocSelect);
            List<ParameterLocation> paramLocAggregate = new ArrayList<ParameterLocation>();
            newQuery.storeParamLinks(nextLambdaParamIndex+1, paramLocAggregate);
            Object[][] newParams = gatherParams(gatherParams(params, paramLocSelect, select), paramLocAggregate, aggregate);
            storeInQueryCache("group", newQuery, select, paramLocSelect, aggregate, paramLocAggregate);
            return new SQLQueryComposer<Pair<U,V>>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex + 2, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public <U> QueryComposer<Pair<T, U>> join(Join<T,U> join)
   {
      try {
         if (transformer == null) return null;
         QueryComposer cached = lookupQueryCache("join", join, null);
         if (cached != null) return cached;
         SQLQuery<Pair<T,U>> newQuery = transformer.join(query.copy(), nextLambdaParamIndex, join, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, join);
            storeInQueryCache("join", newQuery, join, paramLocs, null, null);
            return new SQLQueryComposer<Pair<T,U>>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex + 1, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public <U> QueryComposer<U> select(Select<T, U> select)
   {
      try {
         if (transformer == null) return null;
         QueryComposer cached = lookupQueryCache("select", select, null);
         if (cached != null) return cached;
         SQLQuery<U> newQuery = transformer.select(query.copy(), nextLambdaParamIndex, select, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
           newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
           Object[][] newParams = gatherParams(params, paramLocs, select);
           storeInQueryCache("select", newQuery, select, paramLocs, null, null);
           return new SQLQueryComposer<U>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex + 1, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public QueryComposer<T> unique()
   {
      if (transformer == null) return null;
      // TODO Auto-generated method stub
      return null;
   }

   public QueryComposer<T> where(Where<T> test)
   {
      try {
         if (transformer == null) return null;
         QueryComposer cached = lookupQueryCache("where", test, null);
         if (cached != null) return cached;
         SQLQuery<T> newQuery = transformer.where(query.copy(), nextLambdaParamIndex, test, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, test);
            storeInQueryCache("where", newQuery, test, paramLocs, null, null);
            return new SQLQueryComposer<T>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex + 1, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public QueryComposer<T> with(T toAdd)
   {
      if (transformer == null) return null;
      // TODO Auto-generated method stub
      return null;
   }

   public Double sumDouble(AggregateDouble<T> aggregate)
   {
      try {
         if (transformer == null) return null;
         Double cached = lookupQueryCacheRow("sumDouble", aggregate, null);
         if (cached != null) return cached;
         SQLQuery<Double> newQuery = transformer.sumDouble(query.copy(), nextLambdaParamIndex, aggregate, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, aggregate);
            storeInQueryCache("sumDouble", newQuery, aggregate, paramLocs, null, null);
            return evaluateRowQuery(newQuery, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public Integer sumInt(AggregateInteger<T> aggregate)
   {
      try {
         if (transformer == null) return null;
         Integer cached = lookupQueryCacheRow("sumInt", aggregate, null);
         if (cached != null) return cached;
         SQLQuery<Integer> newQuery = transformer.sumInt(query.copy(), nextLambdaParamIndex, aggregate, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, aggregate);
            storeInQueryCache("sumInt", newQuery, aggregate, paramLocs, null, null);
            return evaluateRowQuery(newQuery, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public Double maxDouble(AggregateDouble<T> aggregate)
   {
      try {
         if (transformer == null) return null;
         Double cached = lookupQueryCacheRow("maxDouble", aggregate, null);
         if (cached != null) return cached;
         SQLQuery<Double> newQuery = transformer.maxDouble(query.copy(), nextLambdaParamIndex, aggregate, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, aggregate);
            storeInQueryCache("maxDouble", newQuery, aggregate, paramLocs, null, null);
            return evaluateRowQuery(newQuery, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public Integer maxInt(AggregateInteger<T> aggregate)
   {
      try {
         if (transformer == null) return null;
         Integer cached = lookupQueryCacheRow("maxInt", aggregate, null);
         if (cached != null) return cached;
         SQLQuery<Integer> newQuery = transformer.maxInt(query.copy(), nextLambdaParamIndex, aggregate, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, aggregate);
            storeInQueryCache("maxInt", newQuery, aggregate, paramLocs, null, null);
            return evaluateRowQuery(newQuery, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      try {
         if (transformer == null) return null;
         U cached = lookupQueryCacheRow("selectAggregates", aggregate, null);
         if (cached != null) return cached;
         SQLQuery<U> newQuery = transformer.selectAggregates(query.copy(), nextLambdaParamIndex, aggregate, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, aggregate);
            storeInQueryCache("selectAggregates", newQuery, aggregate, paramLocs, null, null);
            return evaluateRowQuery(newQuery, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public QueryComposer<T> firstN(int n)
   {
      if (transformer == null) return null;
      QueryComposer cached = lookupQueryCache("firstN", null, null);
      if (cached != null) return cached;
      SQLQuery<T> newQuery = transformer.firstN(query.copy(), n, emSource);
      if (newQuery != null)
      {
         storeInQueryCache("firstN", newQuery, null, null, null, null);
         return new SQLQueryComposer<T>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex, params);
      }
      return null;
   }

   public QueryComposer<T> sortedByDate(DateSorter<T> sorter,
                                        boolean isAscending)
   {
      try {
         if (transformer == null) return null;
         QueryComposer cached = lookupQueryCache(isAscending ? "sortedByDateAscending" : "sortedByDateDescending", sorter, null);
         if (cached != null) return cached;
         SQLQuery<T> newQuery = transformer.sortedByDate(query.copy(), nextLambdaParamIndex, sorter, isAscending, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, sorter);
            storeInQueryCache(isAscending ? "sortedByDateAscending" : "sortedByDateDescending", newQuery, sorter, paramLocs, null, null);
            return new SQLQueryComposer<T>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex + 1, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public QueryComposer<T> sortedByDouble(DoubleSorter<T> sorter,
                                          boolean isAscending)
   {
      try {
         if (transformer == null) return null;
         QueryComposer cached = lookupQueryCache(isAscending ? "sortedByDoubleAscending" : "sortedByDoubleDescending", sorter, null);
         if (cached != null) return cached;
         SQLQuery<T> newQuery = transformer.sortedByDouble(query.copy(), nextLambdaParamIndex, sorter, isAscending, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, sorter);
            storeInQueryCache(isAscending ? "sortedByDoubleAscending" : "sortedByDoubleDescending", newQuery, sorter, paramLocs, null, null);
            return new SQLQueryComposer<T>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex + 1, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public QueryComposer<T> sortedByInt(IntSorter<T> sorter, boolean isAscending)
   {
      try {
         if (transformer == null) return null;
         QueryComposer cached = lookupQueryCache(isAscending ? "sortedByIntAscending" : "sortedByIntDescending", sorter, null);
         if (cached != null) return cached;
         SQLQuery<T> newQuery = transformer.sortedByInt(query.copy(), nextLambdaParamIndex, sorter, isAscending, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, sorter);
            storeInQueryCache(isAscending ? "sortedByIntAscending" : "sortedByIntDescending", newQuery, sorter, paramLocs, null, null);
            return new SQLQueryComposer<T>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex + 1, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }

   public QueryComposer<T> sortedByString(StringSorter<T> sorter,
                                          boolean isAscending)
   {
      try {
         if (transformer == null) return null;
         QueryComposer cached = lookupQueryCache(isAscending ? "sortedByStringAscending" : "sortedByStringDescending", sorter, null);
         if (cached != null) return cached;
         SQLQuery<T> newQuery = transformer.sortedByString(query.copy(), nextLambdaParamIndex, sorter, isAscending, emSource);
         List<ParameterLocation> paramLocs = new ArrayList<ParameterLocation>();
         if (newQuery != null)
         {
            newQuery.storeParamLinks(nextLambdaParamIndex, paramLocs);
            Object[][] newParams = gatherParams(params, paramLocs, sorter);
            storeInQueryCache(isAscending ? "sortedByStringAscending" : "sortedByStringDescending", newQuery, sorter, paramLocs, null, null);
            return new SQLQueryComposer<T>(emSource, jdbc, transformer, newQuery, nextLambdaParamIndex + 1, newParams);
         }
         return null;
      } catch(QueryGenerationException e) {return null;}
   }
}
