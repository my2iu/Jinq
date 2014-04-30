package org.jinq.jpa;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.transform.JPQLQueryTransform;
import org.jinq.jpa.transform.LambdaInfo;
import org.jinq.jpa.transform.SelectTransform;
import org.jinq.jpa.transform.WhereTransform;

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

   private JPAQueryComposer(MetamodelUtil metamodel, EntityManager em, JPQLQuery<T> query)
   {
      this.metamodel = metamodel;
      this.em = em;
      this.query = query;
   }
   
   public static <U> JPAQueryComposer<U> findAllEntities(MetamodelUtil metamodel, EntityManager em, String entityName)
   {
      return new JPAQueryComposer<>(metamodel, em, JPQLQuery.findAllEntities(entityName));
   }

   @Override
   public VectorSet<T> createRealizedSet()
   {
      VectorSet<T> toReturn = new VectorSet<T>();
      Iterator<T> it = executeAndReturnResultIterator(exception -> {});
      while (it.hasNext()) toReturn.add(it.next());
      return toReturn;
   }

   @Override
   public String getDebugQueryString()
   {
      return query.getQueryString();
   }

   @Override
   public Iterator<T> executeAndReturnResultIterator(
         Consumer<Throwable> exceptionReporter)
   {
      Query q = em.createQuery(query.getQueryString());
      List<T> results = (List<T>)q.getResultList();
      return results.iterator();
   }

   @Override
   public QueryComposer<T> where(Where<T> test)
   {
	   return null;
   }

   @Override
   public <E extends Exception> QueryComposer<T> where(org.jinq.orm.stream.JinqStream.Where<T, E> test)
   {
	   LambdaInfo where = LambdaInfo.analyze(metamodel, test);
	   if (where == null) return null;
	   JPQLQueryTransform whereTransform = new WhereTransform(metamodel, where);
	   JPQLQuery<T> newQuery = whereTransform.apply(query);
	   if (newQuery == null) return null;
	   return new JPAQueryComposer<>(metamodel, em, newQuery);
   }

   @Override
   public QueryComposer<T> with(T toAdd)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public QueryComposer<T> sortedByInt(IntSorter<T> sorter, boolean isAscending)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public QueryComposer<T> sortedByDouble(DoubleSorter<T> sorter,
         boolean isAscending)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public QueryComposer<T> sortedByString(StringSorter<T> sorter,
         boolean isAscending)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public QueryComposer<T> sortedByDate(DateSorter<T> sorter,
         boolean isAscending)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public QueryComposer<T> firstN(int n)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <U> QueryComposer<U> select(Select<T, U> select)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <U> QueryComposer<U> select(
         org.jinq.orm.stream.JinqStream.Select<T, U> selectLambda)
   {
      LambdaInfo select = LambdaInfo.analyze(metamodel, selectLambda);
      if (select == null) return null;
      JPQLQueryTransform selectTransform = new SelectTransform(metamodel, select);
      JPQLQuery<U> newQuery = selectTransform.apply(query);
      if (newQuery == null) return null;
      return new JPAQueryComposer<>(metamodel, em, newQuery);
   }

   @Override
   public <U> QueryComposer<Pair<T, U>> join(Join<T, U> join)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <U> QueryComposer<Pair<T, U>> join(
         org.jinq.orm.stream.JinqStream.Join<T, U> join)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public QueryComposer<T> unique()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <U, V> QueryComposer<Pair<U, V>> group(Select<T, U> select,
         AggregateGroup<U, T, V> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <U, V> QueryComposer<Pair<U, V>> group(
         org.jinq.orm.stream.JinqStream.Select<T, U> select,
         org.jinq.orm.stream.JinqStream.AggregateGroup<U, T, V> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Double sumDouble(AggregateDouble<T> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Double sumDouble(
         org.jinq.orm.stream.JinqStream.AggregateDouble<T> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Integer sumInt(AggregateInteger<T> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Integer sumInt(
         org.jinq.orm.stream.JinqStream.AggregateInteger<T> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Double maxDouble(AggregateDouble<T> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Double maxDouble(
         org.jinq.orm.stream.JinqStream.AggregateDouble<T> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Integer maxInt(AggregateInteger<T> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Integer maxInt(
         org.jinq.orm.stream.JinqStream.AggregateInteger<T> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <U> U selectAggregates(
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, U> aggregate)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Object[] multiaggregate(
         org.jinq.orm.stream.JinqStream.AggregateSelect<T, ?>[] aggregates)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
