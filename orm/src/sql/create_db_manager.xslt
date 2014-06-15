<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:import href="../shared/helpers.xslt"/>


<xsl:param name="package"/>


<xsl:template match="entities">
package <xsl:value-of select="$package"/>;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Method;
import java.io.PrintWriter;
import ch.epfl.labos.iu.orm.query2.SQLQueryTransforms;
import ch.epfl.labos.iu.orm.queryll2.runtime.QueryllEntityConfigurationInfo;
import ch.epfl.labos.iu.orm.queryll2.runtime.ConfigureQueryll;
import ch.epfl.labos.iu.orm.queryll2.runtime.ORMEntity;
import ch.epfl.labos.iu.orm.queryll2.runtime.ORMField;

public class DBManager implements ConfigureQueryll
{
	public PrintWriter testOut = null;  // When non-null, we're in test mode
	public Connection con = null;
	public boolean isQueryOnly = false;
	public boolean usePartialQueryCaching = true;
	public boolean useFullQueryCaching = true;

	EntityManager cachedEntityManager;

	public EntityManager begin()
	{
		if (con != null)
		{
			if (!isQueryOnly)
			{
				try {
					con.setAutoCommit(false);
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		if (isQueryOnly &amp;&amp; cachedEntityManager != null)
		{
			EntityManager toReturn = cachedEntityManager;
			cachedEntityManager = null;
			return toReturn;
		} 
		return new EntityManager(this);

	}
	
	public void end(EntityManager em, boolean commit)
	{
		try {
			if (!isQueryOnly)
			{
				if (commit)
				{
					em.flushDirty();
					if (con != null) con.commit();
				}
				else
				{
					if (con != null) con.rollback();
				}
				if (con != null) con.setAutoCommit(true);
			}
			else
				cachedEntityManager = em;
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void doTransaction(Transaction transaction)
	{
		EntityManager em = begin();
		end(em, transaction.execute(em));
	}

	public static interface Transaction
	{
		// returns true to commit the changes
		public boolean execute(EntityManager em);
	}

	public void close()
	{
		try {
			if (con != null) con.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

<xsl:apply-templates/>

	public void configureQueryll(QueryllEntityConfigurationInfo config)
	{
		<xsl:apply-templates select="entity" mode="configurequeryll">
			<xsl:with-param name="package" select="/entities/@package"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="link" mode="configurequeryll">
			<xsl:with-param name="package" select="/entities/@package"/>
		</xsl:apply-templates>
	}
	SQLQueryTransforms queryll;  // Place for storing opaque queryll information
	public void storeQueryllAnalysisResults(SQLQueryTransforms queryllAnalysis)
	{
		this.queryll = queryllAnalysis;
	}

	// Creates a debug DBManager that displays queries directly to output 
	// and does not start or query a real database
	// 
	public DBManager(PrintWriter output)
	{
		this(output, null, null, false);
	}

	public DBManager(boolean logQueries)
	{
		this(null, "<xsl:value-of select="jdbc/@driver"/>", "<xsl:value-of select="jdbc/@db"/>", logQueries); 
	}

	public DBManager()
	{
		this(true);
	}

	public DBManager(PrintWriter output, String jdbcDriver, String jdbcPath, boolean logQueries)
	{
		testOut = output;
		if (jdbcPath != null)
		{
			try {
				if (jdbcDriver != null)
					Class.forName(jdbcDriver);
				con = DriverManager.getConnection(
				      jdbcPath/*, "APP", "APP"*/);
				
				if (logQueries)
				{
					con = (Connection)Class.forName("ch.epfl.labos.iu.orm.trace.LoggedConnection")
						.getConstructor(Class.forName("java.sql.Connection"))
						.newInstance(con);
				}
			} catch(Exception e)
			{
				e.printStackTrace();
			}
	        try
	        {
	           Method m = java.lang.Class.forName("<xsl:value-of select="$package"/>.OptimizationBackdoor").getMethod("initializeOptimizations", new Class[0]);
	           m.invoke(null, new Object[0]);
	        } catch (Exception e) {}
		}
	}

	
	public int timeout = 0;
}
</xsl:template>


<!--  
Code for passing ORM information to Queryll2 so it can configure
itself for runtime code analysis and query generation. 
-->

<xsl:template match="entity" mode="configurequeryll">
	<xsl:param name="package"/>
		config.registerORMEntity(new ORMEntity(
			"<xsl:value-of select="$package"/>",
			"<xsl:value-of select="@name"/>",
			"<xsl:value-of select="@table"/>",
			new ORMField[] {
				<xsl:apply-templates select="field" mode="configurequeryll"/>			
				}));
</xsl:template>

<xsl:template match="field" mode="configurequeryll">
				new ORMField("<xsl:value-of select="@name"/>",
					"<xsl:value-of select="@type"/>",
					"<xsl:value-of select="@column"/>",
					<xsl:choose><xsl:when test="@key"><xsl:value-of select="@key"/></xsl:when><xsl:otherwise>false</xsl:otherwise></xsl:choose>,
					<xsl:choose><xsl:when test="@dummy"><xsl:value-of select="@dummy"/></xsl:when><xsl:otherwise>false</xsl:otherwise></xsl:choose>
					),
</xsl:template>	

<xsl:template match="link[@map!='N:M']" mode="configurequeryll">
	<xsl:param name="package"/>
		config.registerORMSimpleLink(
			"<xsl:value-of select="$package"/>",
			"<xsl:value-of select="@map"/>",
			"<xsl:value-of select="from/@entity"/>",
			"<xsl:value-of select="from/@field"/>",
			"<xsl:value-of select="column/@from"/>",
			"<xsl:value-of select="to/@entity"/>",
			"<xsl:value-of select="to/@field"/>",
			"<xsl:value-of select="column/@to"/>");
</xsl:template>			

<xsl:template match="link[@map='N:M']" mode="configurequeryll">
	<xsl:param name="package"/>
		config.registerORMNMLink(
			"<xsl:value-of select="$package"/>",
			"<xsl:value-of select="from/@entity"/>",
			"<xsl:value-of select="from/@field"/>",
			"<xsl:value-of select="column[@tableto]/@from"/>",
			"<xsl:value-of select="column[@tableto]/@table"/>",
			"<xsl:value-of select="column[@tableto]/@tableto"/>",
			"<xsl:value-of select="column[@tablefrom]/@tablefrom"/>",
			"<xsl:value-of select="to/@entity"/>",
			"<xsl:value-of select="to/@field"/>",
			"<xsl:value-of select="column[@tablefrom]/@to"/>");
</xsl:template>			
</xsl:stylesheet>