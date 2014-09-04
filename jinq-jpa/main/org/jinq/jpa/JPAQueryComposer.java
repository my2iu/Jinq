package org.jinq.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;

import org.jinq.jpa.jpqlquery.GeneratedQueryParameter;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.SelectFromWhere;
import org.jinq.jpa.transform.AggregateTransform;
import org.jinq.jpa.transform.CountTransform;
import org.jinq.jpa.transform.GroupingTransform;
import org.jinq.jpa.transform.JPQLMultiLambdaQueryTransform;
import org.jinq.jpa.transform.JPQLNoLambdaQueryTransform;
import org.jinq.jpa.transform.JPQLOneLambdaQueryTransform;
import org.jinq.jpa.transform.JoinTransform;
import org.jinq.jpa.transform.LambdaInfo;
import org.jinq.jpa.transform.LimitSkipTransform;
import org.jinq.jpa.transform.MultiAggregateTransform;
import org.jinq.jpa.transform.OuterJoinTransform;
import org.jinq.jpa.transform.QueryTransformException;
import org.jinq.jpa.transform.SelectTransform;
import org.jinq.jpa.transform.SortingTransform;
import org.jinq.jpa.transform.WhereTransform;
import org.jinq.orm.stream.JinqStream.AggregateGroup;
import org.jinq.orm.stream.JinqStream.CollectComparable;
import org.jinq.orm.stream.JinqStream.CollectNumber;
import org.jinq.orm.stream.JinqStream.Select;
import org.jinq.orm.stream.NextOnlyIterator;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple;

import ch.epfl.labos.iu.orm.QueryComposer;

/**
 * Holds a query and can apply the logic for composing JPQL queries. 
 * It mostly delegates the work to other objects, but this object 
 * does manage caching of queries and substituting of parameters 
 * into queries.
 *
 * @param <T>
 */
public class JPAQueryComposer<T> implements QueryComposer<T>
{
   final MetamodelUtil metamodel;
   final EntityManager em;
   final JPQLQuery<T> query;
   final JinqJPAHints hints;
   
   /**
    * Holds the chain of lambdas that were used to create this query. This is needed
    * because query parameters (which are stored in the lambda objects) are only
    * substituted into the query during query execution, which occurs much later
    * than query generation.
    */
   List<LambdaInfo> lambdas = new ArrayList<>();

   private JPAQueryComposer(JPAQueryComposer<?> base, JPQLQuery<T> query, List<LambdaInfo> chainedLambdas, LambdaInfo...additionalLambdas)
   {
      this(base.metamodel, base.em, base.hints, query, chainedLambdas, additionalLambdas);
   }

   private JPAQueryComposer(MetamodelUtil metamodel, EntityManager em, JinqJPAHints hints, JPQLQuery<T> query, List<LambdaInfo> chainedLambdas, LambdaInfo...additionalLambdas)
   {
      this.metamodel = metamodel;
      this.em = em;
      this.query = query;
      lambdas.addAll(chainedLambdas);
      for (LambdaInfo newLambda: additionalLambdas)
         lambdas.add(newLambda);
      this.hints = new JinqJPAHints(hints);
   }

   public static <U> JPAQueryComposer<U> findAllEntities(MetamodelUtil metamodel, EntityManager em, JinqJPAHints hints, String entityName)
   {
      return new JPAQueryComposer<>(metamodel, em, hints, JPQLQuery.findAllEntities(entityName), new ArrayList<>());
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
         q.setParameter(param.paramName, lambdas.get(param.lambdaIndex).getCapturedArg(param.argIndex));
   }
   
   private void logQuery(String queryString, Query q)
   {
      if (hints.queryLogger == null) return;
      Map<Integer, Object> positionParams = new HashMap<Integer, Object>();
      Map<String, Object> namedParams = new HashMap<String, Object>();
      for (Parameter<?> param: q.getParameters())
      {
         if (param.getName() != null)
            namedParams.put(param.getName(), q.getParameterValue(param));
         if (param.getPosition() != null)
            positionParams.put(param.getPosition(), q.getParameterValue(param));
      }
      hints.queryLogger.logQuery(queryString, positionParams, namedParams);
   }
   
   private T executeAndGetSingleResult()
   {
      final String queryString = query.getQueryString();
      final Query q = em.createQuery(queryString);
      fillQueryParameters(q, query.getQueryParameters());
      final RowReader<T> reader = query.getRowReader();
      logQuery(queryString, q);
      return reader.readResult(q.getSingleResult());
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
                  List<Object> results = q.getResultList();
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
                  List<Object> results = q.getResultList();
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

   private <U> JPAQueryComposer<U> applyTransformWithLambda(JPQLNoLambdaQueryTransform transform)
   {
      try {
         JPQLQuery<U> newQuery = transform.apply(query);
         if (newQuery == null) { translationFail(); return null; }
         return new JPAQueryComposer<>(this, newQuery, lambdas);
      }
      catch (QueryTransformException e)
      {
         translationFail(e);
         return null;
      }
   }
   
   private <U> JPAQueryComposer<U> applyTransformWithLambda(JPQLOneLambdaQueryTransform transform, Object lambda)
   {
      LambdaInfo lambdaInfo = LambdaInfo.analyze(metamodel, hints.lambdaClassLoader, lambda, lambdas.size(), hints.dieOnError);
      if (lambdaInfo == null) { translationFail(); return null; }
      try {
         JPQLQuery<U> newQuery = transform.apply(query, lambdaInfo);
         if (newQuery == null) { translationFail(); return null; }
         return new JPAQueryComposer<>(this, newQuery, lambdas, lambdaInfo);
      }
      catch (QueryTransformException e)
      {
         translationFail(e);
         return null;
      }
   }

   private <U> JPAQueryComposer<U> applyTransformWithLambdas(JPQLMultiLambdaQueryTransform transform, Object [] groupingLambdas)
   {
      LambdaInfo[] lambdaInfos = new LambdaInfo[groupingLambdas.length];
      for (int n = 0; n < groupingLambdas.length; n++)
      {
         lambdaInfos[n] = LambdaInfo.analyze(metamodel, hints.lambdaClassLoader, groupingLambdas[n], lambdas.size() + n, hints.dieOnError);
         if (lambdaInfos[n] == null) { translationFail(); return null; }
      }
      try {
         JPQLQuery<U> newQuery = transform.apply(query, lambdaInfos);
         if (newQuery == null) { translationFail(); return null; }
         return new JPAQueryComposer<>(this, newQuery, lambdas, lambdaInfos);
      }
      catch (QueryTransformException e)
      {
         translationFail(e);
         return null;
      }
   }

   @Override
   public <E extends Exception> QueryComposer<T> where(org.jinq.orm.stream.JinqStream.Where<T, E> test)
   {
      return applyTransformWithLambda(new WhereTransform(metamodel, hints.lambdaClassLoader, false), test);
   }
   
   @Override
   public <E extends Exception> QueryComposer<T> where(org.jinq.orm.stream.JinqStream.WhereWithSource<T, E> test)
   {
      return applyTransformWithLambda(new WhereTransform(metamodel, hints.lambdaClassLoader, true), test);
   }

   @Override
   public QueryComposer<T> with(T toAdd)
   {
      // TODO Auto-generated method stub
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Comparable<V>> QueryComposer<T> sortedBy(
         CollectComparable<T, V> sorter, boolean isAscending)
   {
      return applyTransformWithLambda(new SortingTransform(metamodel, hints.lambdaClassLoader, isAscending), sorter);
   }

   @Override
   public QueryComposer<T> limit(long n)
   {
      return applyTransformWithLambda(new LimitSkipTransform(metamodel, hints.lambdaClassLoader, true, n));
   }

   @Override
   public QueryComposer<T> skip(long n)
   {
      return applyTransformWithLambda(new LimitSkipTransform(metamodel, hints.lambdaClassLoader, false, n));
   }

   @Override
   public <U> QueryComposer<U> select(
         org.jinq.orm.stream.JinqStream.Select<T, U> selectLambda)
   {
      return applyTransformWithLambda(new SelectTransform(metamodel, hints.lambdaClassLoader), selectLambda);
   }

   @Override
   public <U> QueryComposer<Pair<T, U>> join(
         org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinTransform(metamodel, hints.lambdaClassLoader, false), joinLambda);
   }

   @Override
   public <U> QueryComposer<Pair<T, U>> join(
         org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> joinLambda)
   {
      return applyTransformWithLambda(new JoinTransform(metamodel, hints.lambdaClassLoader, true), joinLambda);
   }
   
   @Override
   public <U> QueryComposer<Pair<T, U>> leftOuterJoin(
         org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda)
   {
      return applyTransformWithLambda(new OuterJoinTransform(metamodel, hints.lambdaClassLoader), joinLambda);
   }

   @Override
   public QueryComposer<T> unique()
   {
      // TODO Auto-generated method stub
      translationFail(); 
      return null;
   }

   @Override
   public Long count()
   {
      JPAQueryComposer<Long> result = applyTransformWithLambda(new CountTransform(metamodel, hints.lambdaClassLoader));
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Number & Comparable<V>> Number sum(
         CollectNumber<T, V> aggregate, Class<V> collectClass)
   {
      JPAQueryComposer<V> result = applyTransformWithLambda(new AggregateTransform(metamodel, hints.lambdaClassLoader, AggregateTransform.AggregateType.SUM), aggregate);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Comparable<V>> V max(CollectComparable<T, V> aggregate)
   {
      JPAQueryComposer<V> result = applyTransformWithLambda(new AggregateTransform(metamodel, hints.lambdaClassLoader, AggregateTransform.AggregateType.MAX), aggregate);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Comparable<V>> V min(CollectComparable<T, V> aggregate)
   {
      JPAQueryComposer<V> result = applyTransformWithLambda(new AggregateTransform(metamodel, hints.lambdaClassLoader, AggregateTransform.AggregateType.MIN), aggregate);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <V extends Number & Comparable<V>> Double avg(
         CollectNumber<T, V> aggregate)
   {
      JPAQueryComposer<Double> result = applyTransformWithLambda(new AggregateTransform(metamodel, hints.lambdaClassLoader, AggregateTransform.AggregateType.AVG), aggregate);
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
      JPAQueryComposer<U> result = applyTransformWithLambdas(new MultiAggregateTransform(metamodel, hints.lambdaClassLoader), groupingLambdas);
      if (result != null)
         return result.executeAndGetSingleResult();
      translationFail(); 
      return null;
   }

   @Override
   public <U, W extends Tuple> QueryComposer<W> groupToTuple(
         Select<T, U> select, AggregateGroup<U, T, ?>[] aggregates)
   {
      Object [] groupingLambdas = new Object[aggregates.length + 1];
      groupingLambdas[0] = select;
      System.arraycopy(aggregates, 0, groupingLambdas, 1, aggregates.length);
      return applyTransformWithLambdas(new GroupingTransform(metamodel, hints.lambdaClassLoader), groupingLambdas);
   }

   @Override
   public void setHint(String name, Object val)
   {
      hints.setHint(name, val);
   }
}
