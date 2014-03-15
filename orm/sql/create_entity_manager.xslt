<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:import href="../shared/helpers.xslt"/>


<xsl:param name="package"/>


<xsl:template match="entities">
package <xsl:value-of select="$package"/>;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.LazySet;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.Triple;
import ch.epfl.labos.iu.orm.Quartic;
import ch.epfl.labos.iu.orm.Quintic;
import ch.epfl.labos.iu.orm.QueryList;
import ch.epfl.labos.iu.orm.query.RowReader;
import ch.epfl.labos.iu.orm.query.SelectFromWhere;
import ch.epfl.labos.iu.orm.query.QueryLazySet;
import ch.epfl.labos.iu.orm.query2.JDBCConnectionInfo;
import ch.epfl.labos.iu.orm.query2.SQLQueryComposer;
import ch.epfl.labos.iu.orm.query2.SQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReaderColumnDescription;
import ch.epfl.labos.iu.orm.query2.EntityManagerBackdoor;
import org.jinq.orm.stream.QueryJinqStream;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.stream.Stream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.lang.ref.WeakReference;


public class EntityManager implements EntityManagerBackdoor, Serializable
{
	// Note: The EntityManager is not properly serializable, but some queries do access the
	// entity manager. Those Java 8 lambdas need to be serialized to be decoded, so 
	// there needs to be a way to record some sort of placeholder entity manager when
	// serializing the lambda.

	transient DBManager db;
	public int timeout = 0;
	boolean cacheQueries = true;
	boolean partialCacheQueries = true;
	

	public EntityManager(DBManager db)
	{
		this.db = db;
		this.timeout = db.timeout;
		this.cacheQueries = db.useFullQueryCaching;
		this.partialCacheQueries = db.usePartialQueryCaching;
<xsl:apply-templates mode="create_entity_set"/>
	}

	void flushDirty()
	{
		// TODO: this
		// TODO: Flushing of objects with references to other non-persisted objects that later become persisted
<xsl:apply-templates mode="flush_dirty_entity"/>
	}

	public abstract static class EntityReader&lt;T&gt; extends SQLReader &lt;T&gt;
	{
		EntityManager em;
		String entityInternalClassName;
		List&lt;SQLReaderColumnDescription&gt; columns = new Vector&lt;SQLReaderColumnDescription&gt;();
		public EntityReader(EntityManager em, String entityInternalClassName)
		{
			this.em = em;
			this.entityInternalClassName = entityInternalClassName;
		}
		public int getNumColumns()
		{
			// Currently assumes that each entry in the columns list takes
			// up one column
			//
			return columns.size();
		}
		public int getColumnForField(String field)
		{
			for (int n = 0; n &lt; columns.size(); n++)
			{
				if (field.equals(columns.get(n).field))
					return n;
			}
			return -1;
		}
		public int getColumnIndexForColumnName(String col)
		{
			for (int n = 0; n &lt; columns.size(); n++)
			{
				if (col.equals(columns.get(n).columnName))
					return n;
			}
			return -1;
		}
		
		String [] getColumnNames()
		{
			String [] columnNames = new String[columns.size()];
			for (int n = 0; n &lt; columnNames.length; n++)
				columnNames[n] = columns.get(n).columnName;
			return columnNames;
		}
		SQLReader readerForType(String type)
		{
			if (type.equals("int"))
				return new SQLReader.IntegerSQLReader();
			else if (type.equals("float"))
				return new SQLReader.FloatSQLReader();
			else if (type.equals("double"))
				return new SQLReader.DoubleSQLReader();
			else if (type.equals("String"))
				return new SQLReader.StringSQLReader();
			else if (type.equals("Date"))
				return new SQLReader.DateSQLReader();
			return null;
		}
		public SQLReader getReaderForField(String field)
		{
			for (int n = 0; n &lt; columns.size(); n++)
			{
				if (field.equals(columns.get(n).field))
				{
					String type = columns.get(n).type;
					if (type != null)
						return readerForType(type);
					return null;
				}
			}
			return null;
		}
		public boolean isCastConsistent(String internalName)
		{
			return entityInternalClassName.equals(internalName);
		}
		
	}

<xsl:apply-templates/>

   public SQLReader getReaderForEntity(String entity)
   {
		<xsl:for-each select="entity">
			<xsl:variable name="uppername">
				<xsl:call-template name="FirstCaps">
					<xsl:with-param name="name" select="@name"/>
				</xsl:call-template>
			</xsl:variable>
		if ("<xsl:value-of select="@name"/>".equals(entity))
		{
			return new <xsl:value-of select="$uppername"/>SQLReader(this);			
		}
		</xsl:for-each>
		return null;
   }
   public String[] getEntityColumnNames(String entity)
   {
		<xsl:for-each select="entity">
			<xsl:variable name="uppername">
				<xsl:call-template name="FirstCaps">
					<xsl:with-param name="name" select="@name"/>
				</xsl:call-template>
			</xsl:variable>
		if ("<xsl:value-of select="@name"/>".equals(entity))
		{
			return new <xsl:value-of select="$uppername"/>SQLReader(this).getColumnNames();			
		}
		</xsl:for-each>
		return null;
   }
   public String getTableForEntity(String entity)
   {
		<xsl:for-each select="entity">
		if ("<xsl:value-of select="@name"/>".equals(entity))
		{
			return "<xsl:value-of select="@table"/>"; 
		}
		</xsl:for-each>
		return null;
   }
   
	static class QueryCacheKey
	{
		QueryCacheKey(String context, Object baseQuery, Object lambda)
		{
			this.context = context;
			this.baseQuery = baseQuery;
			this.lambda = lambda;
		}
		public int hashCode()
		{
			return context.hashCode() ^ baseQuery.hashCode() ^ lambda.hashCode();
		}
		public boolean equals(Object o)
		{
			if (!(o instanceof QueryCacheKey)) return false;
			QueryCacheKey other = (QueryCacheKey)o;
			return other.context.equals(context)
				&amp;&amp; other.baseQuery.equals(baseQuery)
				&amp;&amp; other.lambda.equals(lambda);
		}
		String context;
		Object baseQuery;
		Object lambda;
	}
	static class QueryCacheValue
	{
		QueryCacheValue(QueryCacheKey key, Object cachedQuery)
		{
			this.key = key;
			this.cachedQuery = cachedQuery;
		}
		QueryCacheKey key;
		Object cachedQuery;
		QueryCacheValue prev = null;
		QueryCacheValue next = null;
	}
	transient Map&lt;QueryCacheKey, QueryCacheValue&gt; queryCache = new HashMap&lt;QueryCacheKey, QueryCacheValue&gt;();
	final static int QUERY_CACHE_LIMIT = 500;
	transient QueryCacheValue queryCacheLRU_head = null;
	transient QueryCacheValue queryCacheLRU_tail = null;
	int queryCacheSize = 0;
   private void dequeueQueryCacheLRU(QueryCacheValue val)
   {
		queryCacheSize--;
		if (val.next == null)
			queryCacheLRU_tail = val.prev;
		else
			val.next.prev = val.prev;
		if (val.prev == null)
			queryCacheLRU_head = val.next;
		else
			val.prev.next = val.next;
		val.prev = null;
		val.next = null;
   }
   private void enqueueQueryCacheLRU(QueryCacheValue val)
   {
		// Add it to the end of the queue
		if (queryCacheLRU_tail == null)
		{
			queryCacheLRU_tail = val;
			queryCacheLRU_head = val;
		}
		else
		{
			val.prev = queryCacheLRU_tail;
			queryCacheLRU_tail.next = val;
			queryCacheLRU_tail = val;
		}
		// Check if we've overflowed
		queryCacheSize++;
		if (queryCacheSize &gt; QUERY_CACHE_LIMIT)
		{
			QueryCacheValue toRemove = queryCacheLRU_head;
			dequeueQueryCacheLRU(toRemove);
			queryCache.remove(toRemove.key);
		}
   }
   public Object getQueryCacheEntry(String context, Object baseQuery, Object lambda)
   {
		QueryCacheKey key = new QueryCacheKey(context, baseQuery, lambda);
		QueryCacheValue cached = queryCache.get(key);
		if (cached == null)
			return null;
		// Move it to the end of the queue
		dequeueQueryCacheLRU(cached);
		enqueueQueryCacheLRU(cached);
		return cached.cachedQuery;
   }
   public void putQueryCacheEntry(String context, Object baseQuery, Object lambda, Object cachedQuery)
   {
		QueryCacheKey key = new QueryCacheKey(context, baseQuery, lambda);
		QueryCacheValue cached = new QueryCacheValue(key, cachedQuery);
		queryCache.put(key, cached);
		enqueueQueryCacheLRU(cached);
   }
	static class GeneratedCachedQuery
	{
		GeneratedCachedQuery(Object key, Object cachedQuery)
		{
			this.key = key;
			this.cachedQuery = cachedQuery;
		}
		Object key;
		Object cachedQuery;
		GeneratedCachedQuery prev = null;
		GeneratedCachedQuery next = null;
	}
	transient Map&lt;Object, GeneratedCachedQuery&gt; generatedQueryCache = new HashMap&lt;Object, GeneratedCachedQuery&gt;();
	final static int GENERATED_QUERY_CACHE_LIMIT = 500;
	transient GeneratedCachedQuery generatedQueryCacheLRU_head = null;
	transient GeneratedCachedQuery generatedQueryCacheLRU_tail = null;
	int generatedQueryCacheSize = 0;
   private void dequeueGeneratedQueryCacheLRU(GeneratedCachedQuery val)
   {
		generatedQueryCacheSize--;
		if (val.next == null)
			generatedQueryCacheLRU_tail = val.prev;
		else
			val.next.prev = val.prev;
		if (val.prev == null)
			generatedQueryCacheLRU_head = val.next;
		else
			val.prev.next = val.next;
		val.prev = null;
		val.next = null;
   }
   private void enqueueGeneratedQueryCacheLRU(GeneratedCachedQuery val)
   {
		// Add it to the end of the queue
		if (generatedQueryCacheLRU_tail == null)
		{
			generatedQueryCacheLRU_tail = val;
			generatedQueryCacheLRU_head = val;
		}
		else
		{
			val.prev = generatedQueryCacheLRU_tail;
			generatedQueryCacheLRU_tail.next = val;
			generatedQueryCacheLRU_tail = val;
		}
		// Check if we've overflowed
		generatedQueryCacheSize++;
		if (generatedQueryCacheSize &gt; GENERATED_QUERY_CACHE_LIMIT)
		{
			GeneratedCachedQuery toRemove = generatedQueryCacheLRU_head;
			dequeueGeneratedQueryCacheLRU(toRemove);
			generatedQueryCache.remove(toRemove.key);
		}
   }
	public Object getGeneratedQueryCacheEntry(Object queryRepresentation)
	{
		GeneratedCachedQuery cached = generatedQueryCache.get(queryRepresentation);
		if (cached == null)
			return null;
		// Move it to the end of the queue
		dequeueGeneratedQueryCacheLRU(cached);
		enqueueGeneratedQueryCacheLRU(cached);
		return cached.cachedQuery;
	}
	public void putGeneratedQueryCacheEntry(Object queryRepresentation, Object generatedQuery)
	{
		GeneratedCachedQuery cached = new GeneratedCachedQuery(queryRepresentation, generatedQuery);
		generatedQueryCache.put(queryRepresentation, cached);
		enqueueGeneratedQueryCacheLRU(cached);
	}
   
   public boolean isQueriesCached()
   {
		return cacheQueries || partialCacheQueries;
   }
   
   public boolean isPreparedStatementCached()
   {
   		return cacheQueries;
   }
   
}
</xsl:template>


<xsl:template match="entity">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="internalname"><xsl:value-of select="concat(translate($package, '.', '/'), '/', $uppername)"/>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
	</xsl:variable>

	// TODO: add plural forms of entity names
	transient SQLQueryComposer&lt;<xsl:value-of select="$uppername"/>&gt; <xsl:value-of select="$lowername"/>Query;
	transient DBSet&lt;<xsl:value-of select="$uppername"/>&gt; <xsl:value-of select="$lowername"/>;
	public DBSet&lt;<xsl:value-of select="$uppername"/>&gt; all<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public Stream&lt;<xsl:value-of select="$uppername"/>&gt; <xsl:value-of select="$lowername"/>Stream()
	{
	   return new QueryJinqStream&lt;&gt;(<xsl:value-of select="$lowername"/>Query);
	}

	void dispose(<xsl:value-of select="$uppername"/> obj)
	{
		flushDirty();
		<xsl:value-of select="$lowername"/>.remove(obj);
		// delete from db immediately
		String sql = "DELETE FROM <xsl:value-of select="@table"/> "
			+ " WHERE 1=1"
	<xsl:for-each select="field[@key='true']">
			+ " AND <xsl:value-of select="@column"/> = ? "</xsl:for-each>;
		if (db.testOut != null) db.testOut.println(sql);
		if (db.con != null)
		{
			try {
				PreparedStatement stmt = db.con.prepareStatement(sql);
				int idx = 0;
	<xsl:for-each select="field[@key='true']">
				++idx;
				stmt.setObject(idx, obj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="@name"/></xsl:call-template>());
	</xsl:for-each>
				stmt.executeUpdate();
				stmt.close();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	void dirtyInstance(<xsl:value-of select="$uppername"/> obj)
	{
		dirty<xsl:value-of select="$uppername"/>.add(obj);
	}
	
	void newInstance(<xsl:value-of select="$uppername"/> obj)
	{
		// TODO: Add handling of objects with auto-generated keys
		// TODO: Add handling of objects whose keys are references to other objects

		assert(obj.em == null);
		obj.em = this;
		// Fix references to other objects that happen to be keys

  <xsl:variable name="entityname" select="@name"/>
  <xsl:for-each select="//link[from/@entity=$entityname]/column[@from]">
    <xsl:variable name="colname" select="@from"/>
    <xsl:variable name="fromentity" select="$entityname"/>
    <xsl:variable name="toentity" select="//link[from/@entity=$entityname]/to/@entity"/>
    <xsl:variable name="tabletocol" select="./@tableto"/>
    <xsl:variable name="tocol" select="./@to | //link[from/@entity=$entityname]/column[@tablefrom=$tabletocol]/@to"/>
        <xsl:if test="//entity[@name=$entityname]/field[@column=$colname and @dummy='true']">
	      <xsl:if test="//entity[@name=$toentity]/field[@column=$tocol]">
		obj.set<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$entityname]/field[@column=$colname and @dummy='true']/@name"/></xsl:call-template>( 
		    obj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="../from/@field"/></xsl:call-template>().get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$toentity]/field[@column=$tocol]/@name"/></xsl:call-template>());
	      </xsl:if>
        </xsl:if>
  </xsl:for-each>
  <xsl:for-each select="//link[to/@entity=$entityname]/column[@to]">
    <xsl:variable name="colname" select="@to"/>
    <xsl:variable name="toentity" select="$entityname"/>
    <xsl:variable name="fromentity" select="//link[to/@entity=$entityname]/from/@entity"/>
    <xsl:variable name="tablefromcol" select="./@tablefrom"/>
    <xsl:variable name="fromcol" select="./@from | //link[to/@entity=$entityname]/column[@tableto=$tablefromcol]/@from"/>
		<xsl:if test="//entity[@name=$entityname]/field[@column=$colname and @dummy='true']">
		  <xsl:if test="//entity[@name=$fromentity]/field[@column=$fromcol]">
		obj.set<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$entityname]/field[@column=$colname and @dummy='true']/@name"/></xsl:call-template>(
		    obj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="../to/@field"/></xsl:call-template>().get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$fromentity]/field[@column=$fromcol]/@name"/></xsl:call-template>());
		  </xsl:if>
		</xsl:if>
  </xsl:for-each>
		flushDirty();
		// Add to DB immediately
		<xsl:value-of select="$lowername"/>.add(obj);
<xsl:apply-templates select="." mode="EntityColumnHashMap"/>
		String sql = "INSERT INTO <xsl:value-of select="@table"/> (";
		boolean isFirst = true;
		for (Map.Entry entry : map.entrySet())
		{
			if (!isFirst) sql = sql + ", ";
			isFirst = false;
			sql = sql + entry.getKey();
		}
		sql = sql + ") VALUES (";
		for (int n = 0; n &lt; map.size(); n++)
		{
			if (n != 0) sql = sql + ", ";
			sql = sql + "?";
		}
        sql = sql + ")";
        if (db.testOut != null) db.testOut.println(sql);
        if (db.con != null)
        {
			try {
				PreparedStatement stmt = db.con.prepareStatement(sql);
				int idx = 1;
				for (Map.Entry entry : map.entrySet())
				{
					stmt.setObject(idx, entry.getValue());
					idx++;
				}
				stmt.executeUpdate();
				stmt.close();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
        }
		known<xsl:value-of select="$uppername"/>.put(obj.idKey(), new WeakReference&lt;<xsl:value-of select="$uppername"/>&gt;(obj));
		original<xsl:value-of select="$uppername"/>.put(obj, (<xsl:value-of select="$uppername"/>)obj.copyForComparison());
	}

	<xsl:value-of select="$uppername"/> create<xsl:value-of select="$uppername"/>(ResultSet rs, int column) throws SQLException
	{
		<xsl:value-of select="$uppername"/> obj = null;

		<xsl:call-template name="EntityKeyType"><xsl:with-param name="entity" select="."/></xsl:call-template> key = <xsl:call-template name="create_key_from_result_set_columns">
				<xsl:with-param name="entity" select="."/>
			</xsl:call-template>;
		WeakReference&lt;<xsl:value-of select="$uppername"/>&gt; ref = known<xsl:value-of select="$uppername"/>.get(key);
		if (ref != null) {
			obj = ref.get();
			if (obj != null)
			{
				// I'm not sure if this is necessary, but it's better to be safe
				if (original<xsl:value-of select="$uppername"/>.get(obj) == null)
					original<xsl:value-of select="$uppername"/>.put(obj, (<xsl:value-of select="$uppername"/>)obj.copyForComparison());
				return obj;
			}
		}

		obj = new <xsl:value-of select="$uppername"/>(this, rs, column);
		known<xsl:value-of select="$uppername"/>.put(obj.idKey(), new WeakReference&lt;<xsl:value-of select="$uppername"/>&gt;(obj));
		original<xsl:value-of select="$uppername"/>.put(obj, (<xsl:value-of select="$uppername"/>)obj.copyForComparison());

		// TODO: this
		return obj;
	}
	
	<xsl:value-of select="$uppername"/> create<xsl:value-of select="$uppername"/>(ResultSet rs, String prefix) throws SQLException
	{
		<xsl:value-of select="$uppername"/> obj = null;

		<xsl:call-template name="EntityKeyType"><xsl:with-param name="entity" select="."/></xsl:call-template> key = <xsl:call-template name="create_key_from_result_set">
				<xsl:with-param name="entity" select="."/>
			</xsl:call-template>;
		WeakReference&lt;<xsl:value-of select="$uppername"/>&gt; ref = known<xsl:value-of select="$uppername"/>.get(key);
		if (ref != null) {
			obj = ref.get();
			if (obj != null)
			{
				// I'm not sure if this is necessary, but it's better to be safe
				if (original<xsl:value-of select="$uppername"/>.get(obj) == null)
					original<xsl:value-of select="$uppername"/>.put(obj, (<xsl:value-of select="$uppername"/>)obj.copyForComparison());
				return obj;
			}
		}

		obj = new <xsl:value-of select="$uppername"/>(this, rs, prefix);
		known<xsl:value-of select="$uppername"/>.put(obj.idKey(), new WeakReference&lt;<xsl:value-of select="$uppername"/>&gt;(obj));
		original<xsl:value-of select="$uppername"/>.put(obj, (<xsl:value-of select="$uppername"/>)obj.copyForComparison());

		// TODO: this
		return obj;
	}
	
	transient HashSet&lt;<xsl:value-of select="$uppername"/>&gt; dirty<xsl:value-of select="$uppername"/> = new HashSet&lt;<xsl:value-of select="$uppername"/>&gt;();	
	transient WeakHashMap&lt;<xsl:value-of select="$uppername"/>, <xsl:value-of select="$uppername"/>&gt; original<xsl:value-of select="$uppername"/> = new WeakHashMap&lt;<xsl:value-of select="$uppername"/>, <xsl:value-of select="$uppername"/>&gt;();	
	transient WeakHashMap&lt;<xsl:call-template name="EntityKeyType"><xsl:with-param name="entity" select="."/></xsl:call-template>, WeakReference&lt;<xsl:value-of select="$uppername"/>&gt;&gt; known<xsl:value-of select="$uppername"/> = new WeakHashMap&lt;<xsl:call-template name="EntityKeyType"><xsl:with-param name="entity" select="."/></xsl:call-template>, WeakReference&lt;<xsl:value-of select="$uppername"/>&gt;&gt;();	

	public static class <xsl:value-of select="$uppername"/>SQLReader extends EntityReader&lt;<xsl:value-of select="$uppername"/>&gt;
	{
		public <xsl:value-of select="$uppername"/>SQLReader(EntityManager em)
		{
			super(em, "<xsl:value-of select="$internalname"/>");
		<xsl:for-each select="field[@column]">
			columns.add(new SQLReaderColumnDescription("<xsl:value-of select="@name"/>", "<xsl:value-of select="@column"/>", "<xsl:value-of select="@type"/>"));
		</xsl:for-each>
		<xsl:for-each select="../link[from[@entity=$entityname]]">
			<xsl:choose>
				<xsl:when test="./@map='N:1'">
			columns.add(new SQLReaderColumnDescription(null, "<xsl:value-of select="column/@from"/>", null));
				</xsl:when>
				<xsl:when test="./@map='1:1'">
			columns.add(new SQLReaderColumnDescription(null, "<xsl:value-of select="column/@from"/>", null));
				</xsl:when>
				<xsl:when test="./@map='N:M'">
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
		<xsl:for-each select="../link[to[@entity=$entityname]]">
			<xsl:choose>
				<xsl:when test="./@map='1:N'">
			columns.add(new SQLReaderColumnDescription(null, "<xsl:value-of select="column/@to"/>", null));
				</xsl:when>
				<xsl:when test="./@map='1:1'">
			columns.add(new SQLReaderColumnDescription(null, "<xsl:value-of select="column/@to"/>", null));
				</xsl:when>
				<xsl:when test="./@map='N:M'">
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
		}
		public <xsl:value-of select="$uppername"/> readData(ResultSet result, int column)
		{
			try {
				if (em.db.isQueryOnly)
					return new <xsl:value-of select="$uppername"/>(em, result, column);
				else
					return em.create<xsl:value-of select="$uppername"/>(result, column);
			} catch (SQLException e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	public class <xsl:value-of select="$uppername"/>RowReader implements RowReader &lt;<xsl:value-of select="$uppername"/>&gt;
	{
		public int column;
		public String prefix;
		public <xsl:value-of select="$uppername"/>RowReader(String prefix)
		{
			this.prefix = prefix;
		}
		protected <xsl:value-of select="$uppername"/>RowReader(String prefix, int column)
		{
			this.prefix = prefix;
			this.column = column;
		}
		public <xsl:value-of select="$uppername"/> readSqlRow(ResultSet rs)
		{
			try {
				return create<xsl:value-of select="$uppername"/>(rs, column);
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		public void configureQuery(SelectFromWhere query)
		{
			int firstColumn =  -1;
			int col;

		<xsl:for-each select="field[@column]">
			col = query.addSelection(prefix + ".<xsl:value-of select="@column"/>", prefix + "_<xsl:value-of select="@column"/>");
			if (firstColumn == -1) firstColumn = col;
		</xsl:for-each>
		<xsl:for-each select="../link[to[@entity=$entityname]][@map='1:N']/column[@to]">
			col = query.addSelection(prefix + ".<xsl:value-of select="@to"/>", "LINK_" + prefix + "_<xsl:value-of select="@to"/>");
			if (firstColumn == -1) firstColumn = col;
		</xsl:for-each>
		<xsl:for-each select="../link[from[@entity=$entityname]][@map='N:1']/column[@from]">
			col = query.addSelection(prefix + ".<xsl:value-of select="@from"/>", "LINK_" + prefix + "_<xsl:value-of select="@from"/>");
			if (firstColumn == -1) firstColumn = col;
		</xsl:for-each>
		<xsl:for-each select="../link[from[@entity=$entityname]][@map='1:1']/column[@from]">
			col = query.addSelection(prefix + ".<xsl:value-of select="@from"/>", "LINK_" + prefix + "_<xsl:value-of select="@from"/>");
			if (firstColumn == -1) firstColumn = col;
		</xsl:for-each>
		<xsl:for-each select="../link[to[@entity=$entityname]][@map='1:1']/column[@to]">
			col = query.addSelection(prefix + ".<xsl:value-of select="@to"/>", "LINK_" + prefix + "_<xsl:value-of select="@to"/>");
			if (firstColumn == -1) firstColumn = col;
		</xsl:for-each>
			column = firstColumn;
		}
		public RowReader &lt;<xsl:value-of select="$uppername"/>&gt; copy()
		{
			return new <xsl:value-of select="$uppername"/>RowReader(prefix, column);
		}
		public String queryString()
		{
			return prefix;
		}
	<xsl:for-each select="../link[to[@entity=$entityname]]">
		<xsl:variable name="fromentity" select="from/@entity"/>
		<xsl:variable name="toentity" select="to/@entity"/>
		<xsl:variable name="upperfromname">
			<xsl:call-template name="FirstCaps">
				<xsl:with-param name="name" select="//entity[@name=$fromentity]/@name"/>
			</xsl:call-template>
		</xsl:variable>

		String <xsl:value-of select="to/@field"/>Prefix;
		public String materialize<xsl:value-of select="to/@field"/>JoinString(SelectFromWhere query)
		{
			if (<xsl:value-of select="to/@field"/>Prefix == null)
			{
			<xsl:if test="column/@table">
				String middlePrefix = query.addTable("<xsl:value-of select="column/@table"/>");
			</xsl:if>
				<xsl:value-of select="to/@field"/>Prefix = query.addTable("<xsl:value-of select="//entity[@name=$fromentity]/@table"/>");
			<xsl:for-each select="column[@from][@to]">
				query.addWhereClause(<xsl:value-of select="../to/@field"/>Prefix + ".<xsl:value-of select="@from"/> = " + prefix + ".<xsl:value-of select="@to"/>");
			</xsl:for-each>
			<xsl:for-each select="column[@from][@tableto]">
				query.addWhereClause(<xsl:value-of select="../to/@field"/>Prefix + ".<xsl:value-of select="@from"/> = " + middlePrefix + ".<xsl:value-of select="@tableto"/>");
			</xsl:for-each>
			<xsl:for-each select="column[@tablefrom][@to]">
				query.addWhereClause(middlePrefix + ".<xsl:value-of select="@tablefrom"/> = " + prefix + ".<xsl:value-of select="@to"/>");
			</xsl:for-each>
			}
			return <xsl:value-of select="to/@field"/>Prefix;
		}
		public <xsl:value-of select="$upperfromname"/>RowReader materialize<xsl:value-of select="to/@field"/>JoinRowReader(SelectFromWhere query)
		{
			return new <xsl:value-of select="$upperfromname"/>RowReader(materialize<xsl:value-of select="to/@field"/>JoinString(query));
		}
	</xsl:for-each>
	<xsl:for-each select="../link[from[@entity=$entityname]]">
		<xsl:variable name="fromentity" select="from/@entity"/>
		<xsl:variable name="toentity" select="to/@entity"/>
		<xsl:variable name="uppertoname">
			<xsl:call-template name="FirstCaps">
				<xsl:with-param name="name" select="//entity[@name=$toentity]/@name"/>
			</xsl:call-template>
		</xsl:variable>

		String <xsl:value-of select="from/@field"/>Prefix;
		public String materialize<xsl:value-of select="from/@field"/>JoinString(SelectFromWhere query)
		{
			if (<xsl:value-of select="from/@field"/>Prefix == null)
			{
			<xsl:if test="column/@table">
				String middlePrefix = query.addTable("<xsl:value-of select="column/@table"/>");
			</xsl:if>
				<xsl:value-of select="from/@field"/>Prefix = query.addTable("<xsl:value-of select="//entity[@name=$toentity]/@table"/>");
			<xsl:for-each select="column[@from][@to]">
				query.addWhereClause(prefix + ".<xsl:value-of select="@from"/> = " + <xsl:value-of select="../from/@field"/>Prefix + ".<xsl:value-of select="@to"/>");
			</xsl:for-each>
			<xsl:for-each select="column[@from][@tableto]">
				query.addWhereClause(prefix + ".<xsl:value-of select="@from"/> = " + middlePrefix + ".<xsl:value-of select="@tableto"/>");
			</xsl:for-each>
			<xsl:for-each select="column[@tablefrom][@to]">
				query.addWhereClause(middlePrefix + ".<xsl:value-of select="@tablefrom"/> = " + <xsl:value-of select="../from/@field"/>Prefix + ".<xsl:value-of select="@to"/>");
			</xsl:for-each>
			}
			return <xsl:value-of select="from/@field"/>Prefix;
		}
		public <xsl:value-of select="$uppertoname"/>RowReader materialize<xsl:value-of select="from/@field"/>JoinRowReader(SelectFromWhere query)
		{
			return new <xsl:value-of select="$uppertoname"/>RowReader(materialize<xsl:value-of select="from/@field"/>JoinString(query));
		}
	</xsl:for-each>
	}
</xsl:template>






<xsl:template match="entity" mode="create_entity_set">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
	</xsl:variable>

		{
		
//			SelectFromWhere query = new SelectFromWhere();
//			String prefix = query.addTable("<xsl:value-of select="@table"/>");
//			RowReader &lt;<xsl:value-of select="$uppername"/>&gt; reader = new <xsl:value-of select="$uppername"/>RowReader(prefix);
//			// TODO: Use of db con connection here that needs to be removed
//			<xsl:value-of select="$lowername"/> = new QueryLazySet&lt;<xsl:value-of select="$uppername"/>&gt;(db.con, query, reader);
			JDBCConnectionInfo jdbc = new JDBCConnectionInfo();
			jdbc.connection = db.con;
			jdbc.timeout = db.timeout;
			jdbc.testOut = db.testOut;
			<xsl:value-of select="$uppername"/>SQLReader reader = new <xsl:value-of select="$uppername"/>SQLReader(this); 
			SQLQueryComposer&lt;<xsl:value-of select="$uppername"/>&gt; query =
				new SQLQueryComposer&lt;<xsl:value-of select="$uppername"/>&gt;(
						this,
						jdbc,
						db.queryll,
						reader,
						reader.getColumnNames(),
						"<xsl:value-of select="@table"/>"
					);
			<xsl:value-of select="$lowername"/>Query = query;
			<xsl:value-of select="$lowername"/> = new QueryList&lt;<xsl:value-of select="$uppername"/>&gt;(query);
		}
</xsl:template>





<xsl:template match="entity" mode="flush_dirty_entity">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
	</xsl:variable>

		for (<xsl:value-of select="$uppername"/> obj : dirty<xsl:value-of select="$uppername"/>)
		{
<xsl:apply-templates select="." mode="EntityColumnHashMap"/>
			String sql = "UPDATE <xsl:value-of select="@table"/> SET ";
			boolean isFirst = true;
			for (Map.Entry entry : map.entrySet())
			{
				if (!isFirst) sql = sql + ", ";
				isFirst = false;
				sql = sql + entry.getKey() + "= ?";
			}
			sql = sql + " WHERE 1=1"
	<xsl:for-each select="field[@key='true']">
			+ " AND <xsl:value-of select="@column"/> = ? "</xsl:for-each>;
			if (db.testOut != null) db.testOut.println(sql);
			if (sql != null)
			{
				try {
					PreparedStatement stmt = db.con.prepareStatement(sql);
					int idx = 0;
					for (Map.Entry entry : map.entrySet())
					{
						idx++;
						stmt.setObject(idx, entry.getValue());
					}
	<xsl:for-each select="field[@key='true']">
					idx++;
					stmt.setObject(idx, obj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="@name"/></xsl:call-template>());
	</xsl:for-each>
					stmt.executeUpdate();
					stmt.close();
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
	<xsl:for-each select="//link[from/@entity=$uppername and @map='N:M']">
			DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; added = obj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="from/@field"/></xsl:call-template>().comparisonClone();
			added.removeAll(original<xsl:value-of select="$uppername"/>.get(obj).get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="from/@field"/></xsl:call-template>());
			DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; removed = original<xsl:value-of select="$uppername"/>.get(obj).get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="from/@field"/></xsl:call-template>().comparisonClone();
			removed.removeAll(obj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="from/@field"/></xsl:call-template>());
			for (<xsl:value-of select="to/@entity"/> toobj: added)
			{
				HashMap&lt;String, Object&gt; linkmap = new HashMap&lt;String, Object&gt;();
	<xsl:for-each select="column[@tableto]">
	    <xsl:variable name="colname" select="@from"/>
				linkmap.put("<xsl:value-of select="@tableto"/>", obj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$uppername]/field[@column=$colname]/@name"/></xsl:call-template>());
	</xsl:for-each>
	<xsl:for-each select="column[@tablefrom]">
	    <xsl:variable name="colname" select="@to"/>
	    <xsl:variable name="toentity" select="//link[from/@entity=$uppername]/to/@entity"/>
	    <xsl:variable name="tabletocol" select="./@tableto"/>
	    <xsl:variable name="tocol" select="./@to | //link[from/@entity=$uppername]/column[@tablefrom=$tabletocol]/@to"/>
				linkmap.put("<xsl:value-of select="@tablefrom"/>", toobj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$toentity]/field[@column=$tocol]/@name"/></xsl:call-template>());
	</xsl:for-each>
				String linksql = "INSERT INTO <xsl:value-of select="column/@table"/> (";
				isFirst = true;
				for (Map.Entry entry : linkmap.entrySet())
				{
					if (!isFirst) linksql = linksql + ", ";
					isFirst = false;
					linksql = linksql + entry.getKey();
				}
				linksql = linksql + ") VALUES (";
				for (int n = 0; n &lt; linkmap.size(); n++)
				{
					if (n != 0) linksql = linksql + ", ";
					linksql = linksql + "?";
				}
		        linksql = linksql + ")";
		        if (db.testOut != null) db.testOut.println(linksql);
		        if (db.con != null)
		        {
					try {
						PreparedStatement stmt = db.con.prepareStatement(linksql);
						int idx = 0;
						for (Map.Entry entry : linkmap.entrySet())
						{
							idx++;
							stmt.setObject(idx, entry.getValue());
						}
						stmt.executeUpdate();
						stmt.close();
					} catch (SQLException e)
					{
						e.printStackTrace();
					}
		        }
			}
			for (<xsl:value-of select="to/@entity"/> toobj: removed)
			{
				HashMap&lt;String, Object&gt; linkmap = new HashMap&lt;String, Object&gt;();
	<xsl:for-each select="column[@tableto]">
	    <xsl:variable name="colname" select="@from"/>
				linkmap.put("<xsl:value-of select="@tableto"/>", obj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$uppername]/field[@column=$colname]/@name"/></xsl:call-template>());
	</xsl:for-each>
	<xsl:for-each select="column[@tablefrom]">
	    <xsl:variable name="colname" select="@to"/>
	    <xsl:variable name="toentity" select="//link[from/@entity=$uppername]/to/@entity"/>
	    <xsl:variable name="tabletocol" select="./@tableto"/>
	    <xsl:variable name="tocol" select="./@to | //link[from/@entity=$uppername]/column[@tablefrom=$tabletocol]/@to"/>
				linkmap.put("<xsl:value-of select="@tablefrom"/>", toobj.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$toentity]/field[@column=$tocol]/@name"/></xsl:call-template>());
	</xsl:for-each>
				String linksql = "DELETE FROM <xsl:value-of select="column/@table"/> "
					+ " WHERE 1=1";
				for (Map.Entry entry : linkmap.entrySet())
				{
					linksql = linksql + " AND " + entry.getKey() + " = ?";
				}
				if (db.testOut != null) db.testOut.println(linksql);
				if (db.con != null)
				{
					try {
						PreparedStatement stmt = db.con.prepareStatement(linksql);
						int idx = 0;
						for (Map.Entry entry : linkmap.entrySet())
						{
							idx++;
							stmt.setObject(idx, entry.getValue());
						}
						stmt.executeUpdate();
						stmt.close();
					} catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
			}
	</xsl:for-each>

		}
		dirty<xsl:value-of select="$uppername"/>.clear();
</xsl:template>














<!-- Generates a primary key for an entity from a result set -->

<!-- assumes each column has a prefix and a distinct name -->
<xsl:template name="create_key_from_result_set">
  <xsl:param name="entity"/>
  <xsl:variable name="num_keys" select="count($entity/field[@key='true'])"/>
  <xsl:variable name="TupleName">
	<xsl:call-template name="KeyTupleType">
		<xsl:with-param name="size" select="$num_keys"/>
	</xsl:call-template>
  </xsl:variable>
  
  <xsl:choose>
    <xsl:when test="$num_keys=0">NoKey</xsl:when>
    <xsl:when test="$num_keys=1">rs.<xsl:call-template name="SimplyQueryType"><xsl:with-param name="type" select="$entity/field[@key='true']/@type"/></xsl:call-template>(prefix + "_<xsl:value-of select="$entity/field[@key='true']/@column"/>")</xsl:when>
    <xsl:when test="$num_keys > 1">new <xsl:value-of select="$TupleName"/>&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;(<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1">rs.<xsl:call-template name="SimplyQueryType"><xsl:with-param name="type" select="@type"/></xsl:call-template>(prefix + "_<xsl:value-of select="@column"/>")</xsl:when>
        <xsl:otherwise>, rs.<xsl:call-template name="SimplyQueryType"><xsl:with-param name="type" select="@type"/></xsl:call-template>(prefix + "_<xsl:value-of select="@column"/>")</xsl:otherwise>
      </xsl:choose></xsl:for-each>)</xsl:when>
  </xsl:choose>
    
</xsl:template>


<!-- assumes columns are in a set order -->

<xsl:template name="create_key_from_result_set_columns">
  <xsl:param name="entity"/>
  <xsl:variable name="num_keys" select="count($entity/field[@key='true'])"/>
  <xsl:variable name="TupleName">
	<xsl:call-template name="KeyTupleType">
		<xsl:with-param name="size" select="$num_keys"/>
	</xsl:call-template>
  </xsl:variable>
  
  <xsl:choose>
    <xsl:when test="$num_keys=0">NoKey</xsl:when>
    <xsl:when test="$num_keys=1">rs.<xsl:call-template name="SimplyQueryType"><xsl:with-param name="type" select="$entity/field[@key='true']/@type"/></xsl:call-template>(column + <xsl:value-of select="count($entity/field[@key='true']/preceding-sibling::field)"/>)</xsl:when>
    <xsl:when test="$num_keys > 1">new <xsl:value-of select="$TupleName"/>&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;(<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1">rs.<xsl:call-template name="SimplyQueryType"><xsl:with-param name="type" select="@type"/></xsl:call-template>(column + <xsl:value-of select="count(preceding-sibling::field)"/>)</xsl:when>
        <xsl:otherwise>, rs.<xsl:call-template name="SimplyQueryType"><xsl:with-param name="type" select="@type"/></xsl:call-template>(column + <xsl:value-of select="count(preceding-sibling::field)"/>)</xsl:otherwise>
      </xsl:choose></xsl:for-each>)</xsl:when>
  </xsl:choose>
    
</xsl:template>



<xsl:template name="KeyTupleType">
  <xsl:param name="size"/>
  <xsl:choose>
    <xsl:when test="$size=2"><xsl:value-of select="'Pair'"/></xsl:when>
    <xsl:when test="$size=3"><xsl:value-of select="'Triple'"/></xsl:when>
    <xsl:when test="$size=4"><xsl:value-of select="'Quartic'"/></xsl:when>
    <xsl:when test="$size=5"><xsl:value-of select="'Quintic'"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="'Tuple'"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>


</xsl:stylesheet>