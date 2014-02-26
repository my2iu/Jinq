<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:template name="FirstCaps">
  <xsl:param name="name"/>
  <xsl:value-of select="concat(translate(substring($name, 1, 1),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), substring($name, 2))"/>
</xsl:template>

<xsl:template name="FirstLower">
  <xsl:param name="name"/>
  <xsl:value-of select="concat(translate(substring($name, 1, 1), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), substring($name, 2))"/>
</xsl:template>

<xsl:template name="BackSlashEscape">
  <xsl:param name="string"/>
  <xsl:choose>
    <xsl:when test="substring-before($string,'\')=''"><xsl:value-of select="$string"/></xsl:when>
    <xsl:otherwise><xsl:variable name="after"><xsl:call-template name="BackSlashEscape"><xsl:with-param name="string" select="substring-after($string, '\')"/></xsl:call-template></xsl:variable><xsl:value-of select="concat(substring-before($string, '\'), '\\', $after)"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="FullType">
  <xsl:param name="type"/>
  <xsl:choose>
    <xsl:when test="$type='String'">java.lang.String</xsl:when>
    <xsl:when test="$type='Date'">java.sql.Date</xsl:when>
    <xsl:otherwise><xsl:value-of select="$type"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="SimplyQueryType">
  <xsl:param name="type"/>
  <xsl:choose>
    <xsl:when test="$type='int'">getInt</xsl:when>
    <xsl:when test="$type='double'">getDouble</xsl:when>
    <xsl:when test="$type='String'">getString</xsl:when>
    <xsl:when test="$type='Date'">getDate</xsl:when>
    <xsl:otherwise>getUnknownType</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="NonPrimitive">
  <xsl:param name="type"/>
  <xsl:choose>
    <xsl:when test="$type='int'">Integer</xsl:when>
    <xsl:when test="$type='float'">Float</xsl:when>
    <xsl:when test="$type='double'">Double</xsl:when>
    <xsl:otherwise><xsl:value-of select="$type"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="entity" mode="EntityColumnHashMap">
  <xsl:param name="map" select="'map'"/>
  <xsl:param name="obj" select="'obj'"/>
  <xsl:variable name="entityname" select="@name"/>
		HashMap&lt;String, Object&gt; <xsl:value-of select="$map"/> = new HashMap&lt;String, Object&gt;();
		<xsl:for-each select="field[@column]"><xsl:value-of select="$map"/>.put("<xsl:value-of select="@column"/>", <xsl:value-of select="$obj"/>.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="@name"/></xsl:call-template>());
		</xsl:for-each>        
  <xsl:for-each select="//link[from/@entity=$entityname]/column[@from]">
    <xsl:variable name="colname" select="@from"/>
    <xsl:variable name="fromentity" select="$entityname"/>
    <xsl:variable name="toentity" select="//link[from/@entity=$entityname]/to/@entity"/>
    <xsl:variable name="tabletocol" select="./@tableto"/>
    <xsl:variable name="tocol" select="./@to | //link[from/@entity=$entityname]/column[@tablefrom=$tabletocol]/@to"/>
		<xsl:choose>
          <xsl:when test="//entity[@name=$entityname]/field[@column=$colname]">
		    <xsl:value-of select="$map"/>.put("<xsl:value-of select="$colname"/>", <xsl:value-of select="$obj"/>.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$entityname]/field[@column=$colname]/@name"/></xsl:call-template>());
          </xsl:when>
	      <xsl:when test="//entity[@name=$toentity]/field[@column=$tocol]">
		    <xsl:value-of select="$map"/>.put("<xsl:value-of select="$colname"/>", <xsl:value-of select="$obj"/>.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="../from/@field"/></xsl:call-template>().get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$toentity]/field[@column=$tocol]/@name"/></xsl:call-template>());
	      </xsl:when>
		</xsl:choose>
  </xsl:for-each>
  <xsl:for-each select="//link[to/@entity=$entityname]/column[@to]">
    <xsl:variable name="colname" select="@to"/>
    <xsl:variable name="toentity" select="$entityname"/>
    <xsl:variable name="fromentity" select="//link[to/@entity=$entityname]/from/@entity"/>
    <xsl:variable name="tablefromcol" select="./@tablefrom"/>
    <xsl:variable name="fromcol" select="./@from | //link[to/@entity=$entityname]/column[@tableto=$tablefromcol]/@from"/>
		<xsl:choose>
		  <xsl:when test="//entity[@name=$entityname]/field[@column=$colname]">
		    <xsl:value-of select="$map"/>.put("<xsl:value-of select="$colname"/>", <xsl:value-of select="$obj"/>.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$entityname]/field[@column=$colname]/@name"/></xsl:call-template>());
		  </xsl:when>
		  <xsl:when test="//entity[@name=$fromentity]/field[@column=$fromcol]">
		    <xsl:value-of select="$map"/>.put("<xsl:value-of select="$colname"/>", <xsl:value-of select="$obj"/>.get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="../to/@field"/></xsl:call-template>().get<xsl:call-template name="FirstCaps"><xsl:with-param name="name" select="//entity[@name=$fromentity]/field[@column=$fromcol]/@name"/></xsl:call-template>());
		  </xsl:when>
		</xsl:choose>
  </xsl:for-each>
</xsl:template>

<xsl:template match="entity" mode="AllEntityColumns">
  <xsl:param name="tableprefix"/>
  <xsl:param name="prefix"/>
  <xsl:variable name="entityname" select="string(@name)"/>1<xsl:for-each select="field[@column]">, <xsl:value-of select="$tableprefix"/>.<xsl:value-of select="@column"/> as <xsl:value-of select="$prefix"/><xsl:value-of select="@column"/></xsl:for-each>
  <xsl:for-each select="//link[from/@entity=$entityname]"><xsl:for-each select="column[@from]">, <xsl:value-of select="$tableprefix"/>.<xsl:value-of select="@from"/> as LINK_<xsl:value-of select="$prefix"/><xsl:value-of select="@from"/></xsl:for-each></xsl:for-each>
  <xsl:for-each select="//link[to/@entity=$entityname]"><xsl:for-each select="column[@to]">, <xsl:value-of select="$tableprefix"/>.<xsl:value-of select="@to"/> as LINK_<xsl:value-of select="$prefix"/><xsl:value-of select="@to"/></xsl:for-each></xsl:for-each>
</xsl:template>

<xsl:template name="EntityKeyType">
  <xsl:param name="entity"/>
  <xsl:variable name="num_keys" select="count($entity/field[@key='true'])"/>
  
  <xsl:choose>
    <xsl:when test="$num_keys=0">NoKey</xsl:when>
    <xsl:when test="$num_keys=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="$entity/field[@key='true']/@type"/></xsl:call-template></xsl:when>
    <xsl:when test="$num_keys=2">Pair&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;</xsl:when>
    <xsl:when test="$num_keys=3">Triple&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;</xsl:when>
    <xsl:when test="$num_keys=4">Quartic&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;</xsl:when>
    <xsl:when test="$num_keys=5">Quintic&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;</xsl:when>
  </xsl:choose>
    
</xsl:template>

</xsl:stylesheet>