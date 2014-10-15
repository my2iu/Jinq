package org.jinq.jpa.transform;

import java.util.HashMap;
import java.util.Map;

import org.jinq.jpa.jpqlquery.JPQLQuery;

/**
 * Used to cache query transformations so that we don't have to repeat the work
 * of applying transformations if we've applied the same transformation before.
 */
public class JPAQueryComposerCache
{
   /**
    * Internal key used to represent a query transformation.
    */
   private static class CacheKey
   {
      String transformationType;
      JPQLQuery<?> baseQuery;
   }

   /**
    * Map of cached query transforms. Maps from a description of the transform
    * to the cached result of the transform.
    */
   Map<CacheKey, JPQLQuery<?>> cachedQueryTransforms = new HashMap<>();

   /**
    * Map of cached queries for finding all the entities of a certain type. The
    * map maps from entity name to the corresponding query.
    */
   Map<String, JPQLQuery<?>> cachedFindAllEntities = new HashMap<>();

   /**
    * Looks up whether a certain transformation is already in the cache or not.
    * 
    * @param base
    *           query being transformed
    * @param transformationType
    *           type of transformation being applied to the query
    * @return cached transformation result or null if this transformation hasn't
    *         been cached
    */
   public <U, V> JPQLQuery<V> findInCache(JPQLQuery<U> base,
         String transformationType)
   {
      return null;
   }

   /**
    * Checks if a query for finding all the entities of a certain type has
    * already been cached
    * 
    * @param entityName
    *           name of the type of entity the query should return
    * @return the cached query or null if no query has been cached.
    */
   public synchronized <U> JPQLQuery<U> findCachedFindAllEntities(
         String entityName)
   {
      return cacheFindAllEntities(entityName, null);
   }

   /**
    * Caches a query for finding all the entities of a certain type
    * 
    * @param entityName
    *           the name of the entity the query returns
    * @param queryToCache
    *           a query that returns all of the entities of the given type.
    *           queryToCache can be null if the programmer just wants to see if
    *           a certain query is already in the cache but doesn't want to
    *           insert a new query.
    * @return if a query has already been cached, that query is returned;
    *         otherwise, queryToCache is inserted into the cache and returned.
    */
   public synchronized <U> JPQLQuery<U> cacheFindAllEntities(String entityName,
         JPQLQuery<U> queryToCache)
   {
      if (cachedFindAllEntities.containsKey(entityName))
         return (JPQLQuery<U>) cachedFindAllEntities.get(entityName);
      if (queryToCache != null)
         cachedFindAllEntities.put(entityName, queryToCache);
      return queryToCache;
   }
}
