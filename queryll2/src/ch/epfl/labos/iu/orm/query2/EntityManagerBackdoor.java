package ch.epfl.labos.iu.orm.query2;

// At times, we need to reach into EntityManagers to get stuff, so 
// this is the interface for that

public interface EntityManagerBackdoor
{
   SQLReader getReaderForEntity(String entity);
   String[] getEntityColumnNames(String entity);
   String getTableForEntity(String entity);
   Object getGeneratedQueryCacheEntry(Object queryRepresentation);
   // TODO: when the generated query cache is full, old entries are discarded (also when the connection is released), but these entries may contain PreparedStatement objects, which are supposed to be manually resource managed (ie explicitly closed), which is not currently done
   void putGeneratedQueryCacheEntry(Object queryRepresentation, Object generatedQuery);  
   Object getQueryCacheEntry(String context, Object baseQuery, Object lambda);
   void putQueryCacheEntry(String context, Object baseQuery, Object lambda, Object cachedQuery);
   boolean isQueriesCached();
   boolean isPreparedStatementCached();
}
