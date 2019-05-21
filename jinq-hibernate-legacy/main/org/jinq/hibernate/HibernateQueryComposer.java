package org.jinq.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.jpqlquery.GeneratedQueryParameter;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.transform.AggregateTransform;
import org.jinq.jpa.transform.CountTransform;
import org.jinq.jpa.transform.CrossJoinTransform;
import org.jinq.jpa.transform.DistinctTransform;
import org.jinq.jpa.transform.GroupingTransform;
import org.jinq.jpa.transform.JPAQueryComposerCache;
import org.jinq.jpa.transform.JPQLMultiLambdaQueryTransform;
import org.jinq.jpa.transform.JPQLNoLambdaQueryTransform;
import org.jinq.jpa.transform.JPQLOneLambdaQueryTransform;
import org.jinq.jpa.transform.JPQLQueryTransformConfiguration;
import org.jinq.jpa.transform.JPQLQueryTransformConfigurationFactory;
import org.jinq.jpa.transform.JPQLTwoQueryMergeQueryTransform;
import org.jinq.jpa.transform.JoinFetchTransform;
import org.jinq.jpa.transform.JoinTransform;
import org.jinq.jpa.transform.LambdaAnalysis;
import org.jinq.jpa.transform.LambdaAnalysisFactory;
import org.jinq.jpa.transform.LambdaInfo;
import org.jinq.jpa.transform.LimitSkipTransform;
import org.jinq.jpa.transform.MetamodelUtil;
import org.jinq.jpa.transform.MultiAggregateTransform;
import org.jinq.jpa.transform.NotTransform;
import org.jinq.jpa.transform.OuterJoinOnTransform;
import org.jinq.jpa.transform.OuterJoinTransform;
import org.jinq.jpa.transform.QueryTransformException;
import org.jinq.jpa.transform.SelectTransform;
import org.jinq.jpa.transform.SetOperationEmulationTransform;
import org.jinq.jpa.transform.SortingTransform;
import org.jinq.jpa.transform.WhereTransform;
import org.jinq.orm.internal.QueryComposer;
import org.jinq.orm.stream.JinqStream.AggregateGroup;
import org.jinq.orm.stream.JinqStream.JoinToIterable;
import org.jinq.orm.stream.JinqStream.JoinWithSource;
import org.jinq.orm.stream.JinqStream.Select;
import org.jinq.orm.stream.JinqStream.WhereForOn;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.NextOnlyIterator;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple;

/**
 * Holds a query and can apply the logic for composing JPQL queries. 
 * It mostly delegates the work to other objects, but this object 
 * does manage caching of queries and substituting of parameters 
 * into queries.
 *
 * @param <T>
 */
class HibernateQueryComposer<T> implements QueryComposer<T>
{
   final MetamodelUtil metamodel;
   final JPAQueryComposerCache cachedQueries;
   final JPQLQueryTransformConfigurationFactory jpqlQueryTransformConfigurationFactory;
   final Session em;
   final JPQLQuery<T> query;
   final JinqJPAHints hints;
   final LambdaAnalysisFactory lambdaAnalyzer;
   
   /**
    * Holds the chain of lambdas that were used to create this query. This is needed
    * because query parameters (which are stored in the lambda objects) are only
    * substituted into the query during query execution, which occurs much later
    * than query generation.
    */
   List<LambdaInfo> lambdas = new ArrayList<>();

   private HibernateQueryComposer(HibernateQueryComposer<?> base, JPQLQuery<T> query, List<LambdaInfo> chainedLambdas, LambdaInfo...additionalLambdas)
   {
      this(base.metamodel, base.cachedQueries, base.lambdaAnalyzer, base.jpqlQueryTransformConfigurationFactory, base.em, base.hints, query, chainedLambdas, additionalLambdas);
   }

   private HibernateQueryComposer(MetamodelUtil metamodel, JPAQueryComposerCache cachedQueries, LambdaAnalysisFactory lambdaAnalyzer, JPQLQueryTransformConfigurationFactory jpqlQueryTransformConfigurationFactory, Session em, JinqJPAHints hints, JPQLQuery<T> query, List<LambdaInfo> chainedLambdas, LambdaInfo...additionalLambdas)
   {
      this.metamodel = metamodel;
      this.cachedQueries = cachedQueries;
      this.lambdaAnalyzer = lambdaAnalyzer;
      this.jpqlQueryTransformConfigurationFactory = jpqlQueryTransformConfigurationFactory;
      this.em = em;
      this.query = query;
      lambdas.addAll(chainedLambdas);
      for (LambdaInfo newLambda: additionalLambdas)
         lambdas.add(newLambda);
      this.hints = new JinqJPAHints(hints);
   }

   public static <U> HibernateQueryComposer<U> findAllEntities(MetamodelUtil metamodel, JPAQueryComposerCache cachedQueries, LambdaAnalysisFactory lambdaAnalyzer, JPQLQueryTransformConfigurationFactory jpqlQueryTransformConfigurationFactory, Session em, JinqJPAHints hints, JPQLQuery<U> findAllQuery)
   {
      return new HibernateQueryComposer<>(metamodel, cachedQueries, lambdaAnalyzer, jpqlQueryTransformConfigurationFactory, em, hints, findAllQuery, new ArrayList<>());
   }

   @Override
   public String getDebugQueryString()
   {
      return query.getQueryString();
   }

   private void translationFail()
   {
      if (hints.dieOnError) throw new IllegalArgumentException("Could not translate code to a query"); 
   }
   
   private void translationFail(Throwable e)
   {
      if (hints.dieOnError) throw new IllegalArgumentException("Could not translate code to a query", e); 
   }
   
   private void fillQueryParameters(Query q, List<GeneratedQueryParameter> parameters)
   {
      for (GeneratedQueryParameter param: parameters)
      {
         Object paramValue;
         if (param.fieldName == null)
         {
            paramValue = lambdas.get(param.lambdaIndex).getCapturedArg(param.argIndex);
         }
         else
         {
            paramValue = lambdas.get(param.lambdaIndex).getField(param.fieldName);
         }
         if (paramValue instanceof Collection)
            q.setParameterList(param.paramName, (Collection<?>)paramValue);
         else
            q.setParameter(param.paramName, paramValue);
      }
   }
   
   private void logQuery(String queryString, Query q)
   {
      if (hints.queryLogger == null) return;
      Map<Integer, Object> positionParams = new HashMap<Integer, Object>();
      Map<String, Object> namedParams = new HashMap<String, Object>();
//      for (Parameter<?> param: q.getParameters())
//      {
//         if (param.getName() != null)
//            namedParams.put(param.getName(), q.getParameterValue(param));
//         if (param.getPosition() != null)
//            positionParams.put(param.getPosition(), q.getParameterValue(param));
//      }
      hints.queryLogger.logQuery(queryString, positionParams, namedParams);
   }
   
   public T executeAndGetSingleResult()
   {
      final String queryString = query.getQueryString();
      final Query q = em.createQuery(queryString);
      fillQueryParameters(q, query.getQueryParameters());
      final RowReader<T> reader = query.getRowReader();
      logQuery(queryString, q);
      return reader.readResult(q.uniqueResult());
   }
   
   @Override
   public Iterator<T> executeAndReturnResultIterator(
         Consumer<Throwable> exceptionReporter)
   {
      final String queryString = query.getQueryString();
      final Query q = em.createQuery(queryString);
      fillQueryParameters(q, query.getQueryParameters());
      final RowReader<T> reader = query.getRowReader();
      long skip = 0;
      long limit = Long.MAX_VALUE;
      if (query instanceof SelectFromWhere)
      {
         SelectFromWhere<?> sfw = (SelectFromWhere<?>)query;
         if (sfw.limit >= 0)
            limit = sfw.limit;
         if (sfw.skip >= 0)
            skip = sfw.skip;
      }
      final long initialOffset = skip;
      final long maxTotalResults = limit;
      
      // To handle the streaming of giant result sets, we will break
      // them down into pages. Technically, this is not really correct
      // because a database can return the results in different orders
      // and this is potentially slow depending on the underlying 
      // database, but it helps us avoid running out of memory.
      return new NextOnlyIterator<T>() {
         boolean hasNextPage = false;
         Iterator<Object> resultIterator;
         int offset = (int)initialOffset;
         long totalRead = 0;
         @Override protected void generateNext()
         {
            if (resultIterator == null)
            {
               if (offset > 0) q.setFirstResult(offset);
               long pageSize = Long.MAX_VALUE;
               if (hints.automaticResultsPagingSize > 0)
                  pageSize = hints.automaticResultsPagingSize + 1;
               if (maxTotalResults != Long.MAX_VALUE)
                  pageSize = Math.min(pageSize, maxTotalResults - totalRead);
               if (pageSize != Long.MAX_VALUE)
                  q.setMaxResults((int)pageSize);
               if (hints.automaticResultsPagingSize > 0)
               {
                  logQuery(queryString, q);
                  List<Object> results = q.list();
                  if (results.size() > hints.automaticResultsPagingSize)
                  {
                     hasNextPage = true;
                     offset += hints.automaticResultsPagingSize;
                     results.remove(hints.automaticResultsPagingSize);
                  }
                  totalRead += results.size();
                  resultIterator = results.iterator();
               }
               else
               {
                  logQuery(queryString, q);
                  List<Object> results = q.list();
                  resultIterator = results.iterator();
               }
            }
            if (resultIterator.hasNext())
            {
               nextElement(reader.readResult(resultIterator.next()));
            }
            else 
            {
               if (hasNextPage)
               {
                  hasNextPage = false;
                  resultIterator = null;
                  generateNext();
               }
               else
               {
                  noMoreElements();
               }
            }
         }
      };
   }

   private <U> HibernateQueryComposer<U> applyTransformWithLambda(JPQLNoLambdaQueryTransform transform)
   {
      Optional<JPQLQuery<?>> cachedQuery = hints.useCaching ?
            cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), null) : null;
      if (cachedQuery == null)
      {
         cachedQuery = Optional.empty();
         JPQLQuery<U> newQuery = null;
         try {
            newQuery = transform.apply(query, null);
         }
         catch (QueryTransformException e)
         {
            translationFail(e);
         }
         finally 
         {
            // Always cache the resulting query, even if it is an error
            cachedQuery = Optional.ofNullable(newQuery);
            if (hints.useCaching)
               cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), null, cachedQuery);
         }
      }
      if (!cachedQuery.isPresent()) { translationFail(); return null; }
      return new HibernateQueryComposer<>(this, (JPQLQuery<U>)cachedQuery.get(), lambdas);
   }
   
   public <U> HibernateQueryComposer<U> applyTransformWithLambda(JPQLOneLambdaQueryTransform transform, Object lambda)
   {
      LambdaInfo lambdaInfo = lambdaAnalyzer.extractSurfaceInfo(lambda, lambdas.size(), hints.dieOnError);
      if (lambdaInfo == null) { translationFail(); return null; }
      Optional<JPQLQuery<?>> cachedQuery = hints.useCaching ?
            cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), new String[] {lambdaInfo.getLambdaSourceString()}) : null;
      if (cachedQuery == null)
      {
         cachedQuery = Optional.empty();
         JPQLQuery<U> newQuery = null;
         try {
            LambdaAnalysis lambdaAnalysis = lambdaInfo.fullyAnalyze(metamodel, hints.lambdaClassLoader, hints.isObjectEqualsSafe, hints.isAllEqualsSafe, hints.isCollectionContainsSafe, hints.dieOnError);
            if (lambdaAnalysis == null) { translationFail(); return null; }
            getConfig().checkLambdaSideEffects(lambdaAnalysis);
            newQuery = transform.apply(query, lambdaAnalysis, null);
         }
         catch (QueryTransformException e)
         {
            translationFail(e);
         }
         finally 
         {
            // Always cache the resulting query, even if it is an error
            cachedQuery = Optional.ofNullable(newQuery);
            if (hints.useCaching)
               cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), new String[] {lambdaInfo.getLambdaSourceString()}, cachedQuery);
         }
      }
      if (!cachedQuery.isPresent()) { translationFail(); return null; }
      return new HibernateQueryComposer<>(this, (JPQLQuery<U>)cachedQuery.get(), lambdas, lambdaInfo);
   }

   public <U> HibernateQueryComposer<U> applyTransformWithTwoLambdas(OuterJoinOnTransform transform, Object lambda1, Object lambda2)
   {
      LambdaInfo lambdaInfo1 = lambdaAnalyzer.extractSurfaceInfo(lambda1, lambdas.size(), hints.dieOnError);
      if (lambdaInfo1 == null) { translationFail(); return null; }
      LambdaInfo lambdaInfo2 = lambdaAnalyzer.extractSurfaceInfo(lambda2, lambdas.size() + 1, hints.dieOnError);
      if (lambdaInfo2 == null) { translationFail(); return null; }
      Optional<JPQLQuery<?>> cachedQuery = hints.useCaching ?
            cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), new String[] {lambdaInfo1.getLambdaSourceString(), lambdaInfo2.getLambdaSourceString()}) : null;
      if (cachedQuery == null)
      {
         cachedQuery = Optional.empty();
         JPQLQuery<U> newQuery = null;
         try {
            LambdaAnalysis lambdaAnalysis1 = lambdaInfo1.fullyAnalyze(metamodel, hints.lambdaClassLoader, hints.isObjectEqualsSafe, hints.isAllEqualsSafe, hints.isCollectionContainsSafe, hints.dieOnError);
            if (lambdaAnalysis1 == null) { translationFail(); return null; }
            LambdaAnalysis lambdaAnalysis2 = lambdaInfo2.fullyAnalyze(metamodel, hints.lambdaClassLoader, hints.isObjectEqualsSafe, hints.isAllEqualsSafe, hints.isCollectionContainsSafe, hints.dieOnError);
            if (lambdaAnalysis2 == null) { translationFail(); return null; }
            getConfig().checkLambdaSideEffects(lambdaAnalysis1);
            getConfig().checkLambdaSideEffects(lambdaAnalysis2);
            newQuery = transform.apply(query, lambdaAnalysis1, lambdaAnalysis2, null);
         }
         catch (QueryTransformException e)
         {
            translationFail(e);
         }
         finally 
         {
            // Always cache the resulting query, even if it is an error
            cachedQuery = Optional.ofNullable(newQuery);
            if (hints.useCaching)
               cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), new String[] {lambdaInfo1.getLambdaSourceString(), lambdaInfo2.getLambdaSourceString()}, cachedQuery);
         }
      }
      if (!cachedQuery.isPresent()) { translationFail(); return null; }
      return new HibernateQueryComposer<>(this, (JPQLQuery<U>)cachedQuery.get(), lambdas, lambdaInfo1, lambdaInfo2);
   }

   public <U> HibernateQueryComposer<U> applyTransformWithLambdas(JPQLMultiLambdaQueryTransform transform, Object [] groupingLambdas)
   {
      LambdaInfo[] lambdaInfos = new LambdaInfo[groupingLambdas.length];
      String [] lambdaSources = new String[lambdaInfos.length]; 
      for (int n = 0; n < groupingLambdas.length; n++)
      {
         lambdaInfos[n] = lambdaAnalyzer.extractSurfaceInfo(groupingLambdas[n], lambdas.size() + n, hints.dieOnError);
         if (lambdaInfos[n] == null) { translationFail(); return null; }
         lambdaSources[n] = lambdaInfos[n].getLambdaSourceString();
      }
      
      Optional<JPQLQuery<?>> cachedQuery = hints.useCaching ? 
            cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), lambdaSources) : null;
      if (cachedQuery == null)
      {
         cachedQuery = Optional.empty();
         JPQLQuery<U> newQuery = null;
         try {
            LambdaAnalysis[] lambdaAnalyses = new LambdaAnalysis[lambdaInfos.length];
            for (int n = 0; n < lambdaInfos.length; n++)
            {
               lambdaAnalyses[n] = lambdaInfos[n].fullyAnalyze(metamodel, hints.lambdaClassLoader, hints.isObjectEqualsSafe, hints.isAllEqualsSafe, hints.isCollectionContainsSafe, hints.dieOnError);
               if (lambdaAnalyses[n] == null) { translationFail(); return null; }
               getConfig().checkLambdaSideEffects(lambdaAnalyses[n]);
            }
            newQuery = transform.apply(query, lambdaAnalyses, null);
         }
         catch (QueryTransformException e)
         {
            translationFail(e);
         }
         finally 
         {
            // Always cache the resulting query, even if it is an error
            cachedQuery = Optional.ofNullable(newQuery);
            if (hints.useCaching)
               cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), lambdaSources, cachedQuery);
         }
      }
      if (!cachedQuery.isPresent()) { translationFail(); return null; }
      return new HibernateQueryComposer<>(this, (JPQLQuery<U>)cachedQuery.get(), lambdas, lambdaInfos);
   }

   private <U, V> HibernateQueryComposer<V> applyTransformWithTwoQueryMerge(JPQLTwoQueryMergeQueryTransform transform, JinqStream<U> otherSet)
   {
      // Check that the other stream is of a query
      if (!(otherSet instanceof QueryJPAJinqStream))
      {
         translationFail(new IllegalArgumentException("The other stream must be a query"));
         return null;
      }
      // The other stream should be from the same entity manager
      HibernateQueryComposer<U> otherComposer = ((QueryJPAJinqStream<U>)otherSet).jpaComposer; 
      if (otherComposer.em != em)
      {
         translationFail(new IllegalArgumentException("Both queries need to come from the same entity manager"));
         return null;
      }
      
      JPQLQuery<?> otherQuery = otherComposer.query;
      Optional<JPQLQuery<?>> cachedQuery = hints.useCaching ?
            cachedQueries.findInCache(query, otherQuery, transform.getTransformationTypeCachingTag(), null) : null;
      if (cachedQuery == null)
      {
         cachedQuery = Optional.empty();
         JPQLQuery<V> newQuery = null;
         try {
            newQuery = transform.apply(query, otherQuery, lambdas.size());
         }
         catch (QueryTransformException e)
         {
            translationFail(e);
         }
         finally 
         {
            // Always cache the resulting query, even if it is an error
            cachedQuery = Optional.ofNullable(newQuery);
            if (hints.useCaching)
               cachedQueries.cacheQuery(query, otherQuery, transform.getTransformationTypeCachingTag(), null, cachedQuery);
         }
      }
      if (!cachedQuery.isPresent()) { translationFail(); return null; }
      return new HibernateQueryComposer<V>(this, (JPQLQuery<V>)cachedQuery.get(), lambdas, otherComposer.lambdas.toArray(new LambdaInfo[0]));
   }

   /**
    * Holds configuration information used when transforming this composer to a new composer.
    * Since a JPAQueryComposer can only be transformed once, we only need one transformationConfig
    * (and it is instantiated lazily).  
    */
   private JPQLQueryTransformConfiguration transformationConfig = null; 
   public JPQLQueryTransformConfiguration getConfig()
   {
      if (transformationConfig == null)
      {
         transformationConfig = jpqlQueryTransformConfigurationFactory.createConfig();
         transformationConfig.metamodel = metamodel;
         transformationConfig.alternateClassLoader = hints.lambdaClassLoader;
         transformationConfig.isObjectEqualsSafe = hints.isObjectEqualsSafe;
         transformationConfig.isAllEqualsSafe = hints.isAllEqualsSafe;
         transformationConfig.isCollectionContainsSafe = hints.isCollectionContainsSafe;
      }
      return transformationConfig;
   }

   @Override
   public <E extends Exception> HibernateQueryComposer<T> where(Object testLambda)
   {
      return applyTransformWithLambda(new WhereTransform(getConfig(), false), testLambda);
   }
   
   @Override
   public <E extends Exception> HibernateQueryComposer<T> whereWithSource(Object test)
   {
      return applyTransformWithLambda(new WhereTransform(getConfig(), true), test);
   }

   @Override
   public <V extends Comparable<V>> HibernateQueryComposer<T> sortedBy(
         Object sorter, boolean isAscending)
   {
      return applyTransformWithLambda(new SortingTransform(getConfig(), isAscending), sorter);
   }

   @Override
   public HibernateQueryComposer<T> limit(long n)
   {
      return applyTransformWithLambda(new LimitSkipTransform(getConfig(), true, n));
   }

   @Override
   public HibernateQueryComposer<T> skip(long n)
   {
      return applyTransformWithLambda(new LimitSkipTransform(getConfig(), false, n));
   }

   @Override
   public HibernateQueryComposer<T> distinct()
   {
      return applyTransformWithLambda(new DistinctTransform(getConfig()));
   }

   @Override
   public <U> HibernateQueryComposer<U> select(
         Object selectLambda)
   {
      return applyTransformWithLambda(new SelectTransform(getConfig(), false), selectLambda);
   }

   @Override
   public <U> HibernateQueryComposer<U> selectWithSource(Object selectLambda)
   {
      return applyTransformWithLambda(new SelectTransform(getConfig(), true), selectLambda);
   }

   @Override
   public <U> QueryComposer<U> selectAll(Object selectLambda)
   {
      return applyTransformWithLambda(new JoinTransform(getConfig()).setWithSource(false).setJoinAsPairs(false).setIsExpectingStream(true), selectLambda);
   }

   @Override
   public <U> QueryComposer<U> selectAllWithSource(Object selectLambda)
   {
      return applyTransformWithLambda(new JoinTransform(getConfig()).setWithSource(true).setJoinAsPairs(false).setIsExpectingStream(true), selectLambda);
   }

   @Override
   public <U> QueryComposer<U> selectAllIterable(Object selectLambda)
   {
      return applyTransformWithLambda(new JoinTransform(getConfig()).setWithSource(false).setJoinAsPairs(false).setIsExpectingStream(false), selectLambda);
   }

   @Override
   public <U> HibernateQueryComposer<Pair<T, U>> join(
         org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinTransform(getConfig()).setWithSource(false).setJoinAsPairs(true).setIsExpectingStream(true), joinLambda);
   }

   @Override
   public <U> HibernateQueryComposer<Pair<T, U>> joinWithSource(
         org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinTransform(getConfig()).setWithSource(true).setJoinAsPairs(true).setIsExpectingStream(true), joinLambda);
   }

   @Override
   public <U> QueryComposer<Pair<T, U>> joinIterable(JoinToIterable<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinTransform(getConfig()).setWithSource(false).setJoinAsPairs(true).setIsExpectingStream(false), joinLambda);
   }

   public <U> HibernateQueryComposer<T> joinFetch(
         org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinFetchTransform(getConfig()).setIsExpectingStream(true).setIsOuterJoinFetch(false), joinLambda);
   }

   public <U> QueryComposer<T> joinFetchIterable(JoinToIterable<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinFetchTransform(getConfig()).setIsExpectingStream(false).setIsOuterJoinFetch(false), joinLambda);
   }

   @Override
   public <U> HibernateQueryComposer<Pair<T, U>> leftOuterJoin(
         org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda)
   {
      return applyTransformWithLambda(new OuterJoinTransform(getConfig()), joinLambda);
   }

   @Override
   public <U> QueryComposer<Pair<T, U>> leftOuterJoinIterable(
         JoinToIterable<T, U> joinLambda)
   {
      return applyTransformWithLambda(new OuterJoinTransform(getConfig()).setIsExpectingStream(false), joinLambda);
   }

   public <U> HibernateQueryComposer<T> leftOuterJoinFetch(
         org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinFetchTransform(getConfig()).setIsOuterJoinFetch(true), joinLambda);
   }

   public <U> QueryComposer<T> leftOuterJoinFetchIterable(
         JoinToIterable<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinFetchTransform(getConfig()).setIsExpectingStream(false).setIsOuterJoinFetch(true), joinLambda);
   }
   
   @Override
   public <U> QueryComposer<Pair<T, U>> leftOuterJoinWithSource(
         JoinWithSource<T, U> join, WhereForOn<T, U> on)
   {
      return applyTransformWithTwoLambdas(new OuterJoinOnTransform(getConfig()).setIsExpectingStream(true), join, on);
   }
   
   @Override
   public <U> QueryComposer<Pair<T, U>> crossJoin(JinqStream<U> join)
   {
      return applyTransformWithTwoQueryMerge(new CrossJoinTransform(getConfig()), join);
   }

   public QueryComposer<T> orUnion(JPAJinqStream<T> otherSet)
   {
      return applyTransformWithTwoQueryMerge(new SetOperationEmulationTransform(getConfig(), SetOperationEmulationTransform.SetOperationType.OR_UNION), otherSet);
   }

   public QueryComposer<T> andIntersect(JPAJinqStream<T> otherSet)
   {
      return applyTransformWithTwoQueryMerge(new SetOperationEmulationTransform(getConfig(), SetOperationEmulationTransform.SetOperationType.AND_INTERSECT), otherSet);
   }

   public QueryComposer<T> notComplement()
   {
      return applyTransformWithLambda(new NotTransform(getConfig()));
   }
   
   
   public QueryComposer<T> difference(JPAJinqStream<T> otherSet)
   {
      return applyTransformWithTwoQueryMerge(new SetOperationEmulationTransform(getConfig(), SetOperationEmulationTransform.SetOperationType.DIFFERENCE), otherSet);
   }   
   

   @Override
   public Long count()
   {
      HibernateQueryComposer<Long> result = applyTransformWithLambda(new CountTransform(getConfig()));
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Number & Comparable<V>> Number sum(
         Object aggregate, Class<V> collectClass)
   {
      HibernateQueryComposer<V> result = applyTransformWithLambda(new AggregateTransform(getConfig(), AggregateTransform.AggregateType.SUM), aggregate);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Comparable<V>> V max(Object aggregate)
   {
      HibernateQueryComposer<V> result = applyTransformWithLambda(new AggregateTransform(getConfig(), AggregateTransform.AggregateType.MAX), aggregate);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Comparable<V>> V min(Object aggregate)
   {
      HibernateQueryComposer<V> result = applyTransformWithLambda(new AggregateTransform(getConfig(), AggregateTransform.AggregateType.MIN), aggregate);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Number & Comparable<V>> Double avg(
         Object aggregate)
   {
      HibernateQueryComposer<Double> result = applyTransformWithLambda(new AggregateTransform(getConfig(), AggregateTransform.AggregateType.AVG), aggregate);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }
   
//   @Override
//   public <U> U selectAggregates(
//         org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate)
//   {
//      // TODO Auto-generated method stub
//      translationFail(); 
//      return null;
//   }
//
   @Override
   public <U extends Tuple> U multiaggregate(
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, ?>[] aggregates)
   {
      Object [] groupingLambdas = new Object[aggregates.length];
      System.arraycopy(aggregates, 0, groupingLambdas, 0, aggregates.length);
      HibernateQueryComposer<U> result = applyTransformWithLambdas(new MultiAggregateTransform(getConfig()), groupingLambdas);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <U, W extends Tuple> HibernateQueryComposer<W> groupToTuple(
         Select<T, U> select, AggregateGroup<U, T, ?>[] aggregates)
   {
      Object [] groupingLambdas = new Object[aggregates.length + 1];
      groupingLambdas[0] = select;
      System.arraycopy(aggregates, 0, groupingLambdas, 1, aggregates.length);
      return applyTransformWithLambdas(new GroupingTransform(getConfig()), groupingLambdas);
   }

   @Override
   public boolean setHint(String name, Object val)
   {
      return hints.setHint(name, val);
   }
}
