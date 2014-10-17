package org.jinq.jpa.transform;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result
               + ((baseQuery == null) ? 0 : baseQuery.hashCode());
         result = prime * result + Arrays.hashCode(lambdaSources);
         result = prime
               * result
               + ((transformationType == null) ? 0 : transformationType
                     .hashCode());
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         CacheKey other = (CacheKey) obj;
         if (baseQuery == null)
         {
            if (other.baseQuery != null)
               return false;
         } else if (!baseQuery.equals(other.baseQuery))
            return false;
         if (!Arrays.equals(lambdaSources, other.lambdaSources))
            return false;
         if (transformationType == null)
         {
            if (other.transformationType != null)
               return false;
         } else if (!transformationType.equals(other.transformationType))
            return false;
         return true;
      }
      String transformationType;
      JPQLQuery<?> baseQuery;
      String[] lambdaSources;
   }

   /**
    * Map of cached query transforms. Maps from a description of the transform
    * to the cached result of the transform.
    */
   Map<CacheKey, Optional<JPQLQuery<?>>> cachedQueryTransforms = new HashMap<>();

   /**
    * Map of cached queries for finding all the entities of a certain type. The
    * map maps from entity name to the corresponding query.
    */
   Map<String, Optional<JPQLQuery<?>>> cachedFindAllEntities = new HashMap<>();

   /**
    * Looks up whether a certain transformation is already in the cache or not.
    * 
    * @param base
    *           query being transformed
    * @param transformationType
    *           type of transformation being applied to the query
    * @param lambdaSources array of descriptions of the lambdas used in the query
    * @return cached transformation result or null if this transformation hasn't
    *         been cached
    */
   public synchronized Optional<JPQLQuery<?>> findInCache(JPQLQuery<?> base,
         String transformationType, String[] lambdaSources)
   {
      return cacheQuery(base, transformationType, lambdaSources, null);
   }

   /**
    * Inserts a transformed query into the cache. If a cache entry is already present, it
    * returns the cached entry; otherwise, it returns resultingQuery
    * @param base query being transformed
    * @param transformationType type of transformation applied to the query
    * @param lambdaSources array of descriptions of the lambdas used in the query
    * @param resultingQuery result of the transformation that should be cached
    * @return the existing cached entry or resultingQuery if nothing is cached
    */
   public synchronized Optional<JPQLQuery<?>> cacheQuery(JPQLQuery<?> base,
         String transformationType, String[] lambdaSources, Optional<JPQLQuery<?>> resultingQuery)
   {
      CacheKey key = new CacheKey();
      key.transformationType = transformationType;
      key.baseQuery = base;
      if (lambdaSources != null)
         key.lambdaSources = Arrays.copyOf(lambdaSources, lambdaSources.length);
      if (cachedQueryTransforms.containsKey(key))
         return cachedQueryTransforms.get(key);
      if (resultingQuery != null)
         cachedQueryTransforms.put(key, resultingQuery);
      return resultingQuery;
   }

   /**
    * Checks if a query for finding all the entities of a certain type has
    * already been cached
    * 
    * @param entityName
    *           name of the type of entity the query should return
    * @return the cached query or null if no query has been cached.
    */
   public synchronized Optional<JPQLQuery<?>> findCachedFindAllEntities(
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
   public synchronized Optional<JPQLQuery<?>> cacheFindAllEntities(
         String entityName, Optional<JPQLQuery<?>> queryToCache)
   {
      if (cachedFindAllEntities.containsKey(entityName))
         return cachedFindAllEntities.get(entityName);
      if (queryToCache != null)
         cachedFindAllEntities.put(entityName, queryToCache);
      return queryToCache;
   }
}
