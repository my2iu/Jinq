<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:import href="../shared/helpers.xslt"/>


<xsl:param name="package"/>


<xsl:template match="entities">
package <xsl:value-of select="$package"/>;

import ch.epfl.labos.iu.orm.DBSet;

public class EntityManager
{
	DBManager db;

	public EntityManager(DBManager db)
	{
		this.db = db;
	}

<xsl:apply-templates/>
}
</xsl:template>


<xsl:template match="entity">
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

	// TODO: add plural forms of entity names
	DBSet&lt;<xsl:value-of select="$uppername"/>&gt; <xsl:value-of select="$lowername"/> = new LazySet&lt;<xsl:value-of select="$uppername"/>&gt;();	
	public DBSet&lt;<xsl:value-of select="$uppername"/>&gt; all<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	
	// TODO: Think about this! You essentially lose garbage collection is all things are automatically kept this way
	void newInstance(<xsl:value-of select="$uppername"/> obj)
	{
		<xsl:value-of select="$lowername"/>.add(obj);
	}
</xsl:template>


</xsl:stylesheet>