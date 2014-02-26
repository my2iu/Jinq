<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:import href="../shared/helpers.xslt"/>


<xsl:param name="entity"/>
<xsl:param name="package"/>


<xsl:template match="/">
	<xsl:apply-templates select=".//entity[@name=$entity]"/>
</xsl:template>

<xsl:template match="entity[@name=$entity]">
package <xsl:value-of select="$package"/>;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.LazySet;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.Triple;
import ch.epfl.labos.iu.orm.Quartic;
import ch.epfl.labos.iu.orm.Quintic;

public class <xsl:value-of select="@name"/> implements Cloneable
{
	EntityManager em;

	public <xsl:value-of select="@name"/>()
	{
<!-- Need to do stuff twice to handle tables with links to itself -->
<xsl:apply-templates select="../link[from[@entity=$entity]]" mode="from_default_constructor"/>
<xsl:apply-templates select="../link[to[@entity=$entity]]" mode="to_default_constructor"/>
	}

<xsl:apply-templates mode="class_contents"/>
<xsl:apply-templates select="../link[from[@entity=$entity]]" mode="from_class_contents"/>
<xsl:apply-templates select="../link[to[@entity=$entity]]" mode="to_class_contents"/>

	public void persist(EntityManager em)
	{
		this.em = em;
		idKey = <xsl:call-template name="create_key_from_entity"><xsl:with-param name="entity" select="."/></xsl:call-template>;
		em.newInstance(this);
	}

	public void markAsDirty()
	{
		if (em != null)
		{
			em.dirtyInstance(this);
			// TODO: extend this to sets
		}
	}

	public void dispose()
	{
<xsl:apply-templates select="../link[from[@entity=$entity]]" mode="from_dispose"/>
<xsl:apply-templates select="../link[to[@entity=$entity]]" mode="to_dispose"/>
		if (em != null)
			em.dispose(this);
		em = null;
	}
	
	public <xsl:value-of select="@name"/>(EntityManager em, ResultSet rs, int column) throws SQLException
	{
		this.em = em;
<xsl:apply-templates mode="result_set_w_index"/>
<xsl:apply-templates select="../link[from[@entity=$entity]]" mode="from_result_set_constructor_w_index"/>
<xsl:apply-templates select="../link[to[@entity=$entity]]" mode="to_result_set_constructor_w_index"/>
		idKey = <xsl:call-template name="create_key_from_entity"><xsl:with-param name="entity" select="."/></xsl:call-template>;
	}

	public <xsl:value-of select="@name"/>(EntityManager em, ResultSet rs, String prefix) throws SQLException
	{
		this.em = em;
<xsl:apply-templates mode="result_set"/>
<xsl:apply-templates select="../link[from[@entity=$entity]]" mode="from_result_set_constructor"/>
<xsl:apply-templates select="../link[to[@entity=$entity]]" mode="to_result_set_constructor"/>
		idKey = <xsl:call-template name="create_key_from_entity"><xsl:with-param name="entity" select="."/></xsl:call-template>;
	}

	<xsl:call-template name="EntityKeyType"><xsl:with-param name="entity" select="."/></xsl:call-template> idKey;
	public <xsl:call-template name="EntityKeyType"><xsl:with-param name="entity" select="."/></xsl:call-template> idKey()
	{
		return idKey;
	}
	
	<xsl:value-of select="@name"/> comparisonCopy;
	public <xsl:value-of select="@name"/> copyForComparison()
	{
		if (comparisonCopy != null) return comparisonCopy;
		<xsl:value-of select="@name"/> copy = new <xsl:value-of select="@name"/>();
<xsl:for-each select="field">
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
	</xsl:variable>
		copy.<xsl:value-of select="$lowername"/> = <xsl:value-of select="$lowername"/>;
</xsl:for-each>

<xsl:apply-templates select="../link[from[@entity=$entity]]" mode="from_comparison_copy"/>
<xsl:apply-templates select="../link[to[@entity=$entity]]" mode="to_comparison_copy"/>
		comparisonCopy = copy;
		return copy;
	}
	
	public Object clone() throws CloneNotSupportedException
	{	
		// TODO: This is bogus (doesn't handle sets correctly)
		return super.clone();
	}
}
</xsl:template>

<!-- Generates class code for entity fields -->

<xsl:template match="field" mode="class_contents">
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
	
	private <xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="@type"/> get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="@type"/> _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>



<!-- Generates code for reading data out of result sets -->

<xsl:template match="field[@column]" mode="result_set">
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

		<xsl:value-of select="$lowername"/> = rs.<xsl:call-template name="SimplyQueryType"><xsl:with-param name="type" select="@type"/></xsl:call-template>(prefix + "_<xsl:value-of select="@column"/>");
</xsl:template>

<xsl:template match="field[@column]" mode="result_set_w_index">
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

		<xsl:value-of select="$lowername"/> = rs.<xsl:call-template name="SimplyQueryType"><xsl:with-param name="type" select="@type"/></xsl:call-template>(column);
		column++;
</xsl:template>






<!-- Generates class code for links between entities -->

<xsl:template match="link[from[@entity=$entity]][@map='1:N']" mode="from_class_contents">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="fromentity" select="from/@entity"/>
	<xsl:variable name="toentity" select="to/@entity"/>

	class <xsl:value-of select="$uppername"/>Set extends LazySet&lt;<xsl:value-of select="to/@entity"/>&gt;
	{
		protected VectorSet&lt;<xsl:value-of select="to/@entity"/>&gt; createRealizedSet()
		{
			VectorSet&lt;<xsl:value-of select="to/@entity"/>&gt; newset = new VectorSet&lt;<xsl:value-of select="to/@entity"/>&gt;();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$fromentity]"><xsl:with-param name="tableprefix" select="'A'"/><xsl:with-param name="prefix" select="'A_'"/></xsl:apply-templates>"
					+ ", <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$toentity]"><xsl:with-param name="tableprefix" select="'C'"/><xsl:with-param name="prefix" select="'C_'"/></xsl:apply-templates>"
					+ " FROM <xsl:value-of select="//entity[@name=$fromentity]/@table"/> as A, <xsl:value-of select="//entity[@name=$toentity]/@table"/> AS C"
					+ " WHERE 1=1"
	<xsl:for-each select="//entity[@name=$fromentity]/field[@key='true']">
					+ " AND A.<xsl:value-of select="@column"/> = ?"
	</xsl:for-each>
	<xsl:for-each select="column">
					+ " AND A.<xsl:value-of select="@from"/> = C.<xsl:value-of select="@to"/>"
	</xsl:for-each>
					);
				int idx = 0;
	<xsl:for-each select="//entity[@name=$fromentity]/field[@key='true']">
				idx++;
				stmt.setObject(idx, <xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template>);
	</xsl:for-each>

				ResultSet rs = stmt.executeQuery();
				while ( rs.next() ) {
					newset.add(em.create<xsl:value-of select="to/@entity"/>(rs, "C"));
				}
				rs.close();
				stmt.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		
			return newset;
		}	
		public <xsl:value-of select="$uppername"/>Set(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(<xsl:value-of select="to/@entity"/> _val)
		{
			super.add(_val);
			if (em != null)
				em.dirtyInstance(<xsl:value-of select="$fromentity"/>.this);
			_val.set<xsl:value-of select="$upperto"/>(<xsl:value-of select="$entity"/>.this);
			return true;
		}
		public boolean remove(<xsl:value-of select="to/@entity"/> _val)
		{
			super.remove(_val);
			if (em != null)
				em.dirtyInstance(<xsl:value-of select="$fromentity"/>.this);
			_val.set<xsl:value-of select="$upperto"/>(null);
			return true;
		}
	}
	DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; <xsl:value-of select="$lowername"/>;
	public DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:N']" mode="to_class_contents">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="fromentity" select="from/@entity"/>
	<xsl:variable name="toentity" select="to/@entity"/>

	boolean is<xsl:value-of select="$uppername"/>Realized;
	<xsl:for-each select="column[@to]">String _linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@to"/>;
	</xsl:for-each>
	<xsl:value-of select="from/@entity"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="from/@entity"/> get<xsl:value-of select="$uppername"/>()
	{
		if (!is<xsl:value-of select="$uppername"/>Realized)
		{
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$fromentity]"><xsl:with-param name="tableprefix" select="'C'"/><xsl:with-param name="prefix" select="'C_'"/></xsl:apply-templates>"
					+ " FROM <xsl:value-of select="//entity[@name=$fromentity]/@table"/> as C"
					+ " WHERE 1=1"
	<xsl:for-each select="column">
					+ " AND C.<xsl:value-of select="@from"/> = " + _linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@to"/>
	</xsl:for-each>
					);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					<xsl:value-of select="$lowername"/> = em.create<xsl:value-of select="from/@entity"/>(rs, "C");
				rs.close();
				stmt.close();
				is<xsl:value-of select="$uppername"/>Realized = true;
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="from/@entity"/> _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		<xsl:value-of select="$lowername"/> = _val;
		is<xsl:value-of select="$uppername"/>Realized = true;
	}
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:1']" mode="from_class_contents">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="fromentity" select="from/@entity"/>
	<xsl:variable name="toentity" select="to/@entity"/>

	boolean is<xsl:value-of select="$uppername"/>Realized;
	<xsl:for-each select="column[@from]">String _linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@from"/>;
	</xsl:for-each>
	<xsl:value-of select="to/@entity"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="to/@entity"/> get<xsl:value-of select="$uppername"/>()
	{
		if (!is<xsl:value-of select="$uppername"/>Realized)
		{
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$toentity]"><xsl:with-param name="tableprefix" select="'C'"/><xsl:with-param name="prefix" select="'C_'"/></xsl:apply-templates>"
					+ " FROM <xsl:value-of select="//entity[@name=$toentity]/@table"/> as C"
					+ " WHERE 1=1"
	<xsl:for-each select="column">
					+ " AND C.<xsl:value-of select="@to"/> = " + _linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@from"/>
	</xsl:for-each>
					);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					<xsl:value-of select="$lowername"/> = em.create<xsl:value-of select="to/@entity"/>(rs, "C");
				rs.close();
				stmt.close();
				is<xsl:value-of select="$uppername"/>Realized = true;
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="to/@entity"/> _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		if (get<xsl:value-of select="$uppername"/>() != null)
			<xsl:value-of select="$lowername"/>.get<xsl:value-of select="$upperto"/>().remove(this);
		<xsl:value-of select="$lowername"/> = _val;
		_val.get<xsl:value-of select="$upperto"/>().add(this);
	}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:1']" mode="to_class_contents">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="fromentity" select="from/@entity"/>
	<xsl:variable name="toentity" select="to/@entity"/>

	class <xsl:value-of select="$uppername"/>Set extends LazySet&lt;<xsl:value-of select="from/@entity"/>&gt;
	{
		protected VectorSet&lt;<xsl:value-of select="from/@entity"/>&gt; createRealizedSet()
		{
			VectorSet&lt;<xsl:value-of select="from/@entity"/>&gt; newset = new VectorSet&lt;<xsl:value-of select="from/@entity"/>&gt;();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$toentity]"><xsl:with-param name="tableprefix" select="'A'"/><xsl:with-param name="prefix" select="'A_'"/></xsl:apply-templates>"
					+ ", <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$fromentity]"><xsl:with-param name="tableprefix" select="'C'"/><xsl:with-param name="prefix" select="'C_'"/></xsl:apply-templates>"
					+ " FROM <xsl:value-of select="//entity[@name=$toentity]/@table"/> as A, <xsl:value-of select="//entity[@name=$fromentity]/@table"/> AS C"
					+ " WHERE 1=1"
	<xsl:for-each select="//entity[@name=$toentity]/field[@key='true']">
					+ " AND A.<xsl:value-of select="@column"/> = ? "
	</xsl:for-each>
	<xsl:for-each select="column">
					+ " AND A.<xsl:value-of select="@to"/> = C.<xsl:value-of select="@from"/>"
	</xsl:for-each>
					);
				int idx = 0;
	<xsl:for-each select="//entity[@name=$toentity]/field[@key='true']">
				idx++;
				stmt.setObject(idx, <xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template>);
	</xsl:for-each>

				ResultSet rs = stmt.executeQuery();
				while ( rs.next() ) {
					newset.add(em.create<xsl:value-of select="from/@entity"/>(rs, "C"));
				}
				rs.close();
				stmt.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		
			return newset;
		}	
		public <xsl:value-of select="$uppername"/>Set(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(<xsl:value-of select="from/@entity"/> _val)
		{
			if (em != null)
				em.dirtyInstance(<xsl:value-of select="$toentity"/>.this);
			return super.add(_val);
		}
		public boolean remove(<xsl:value-of select="from/@entity"/> _val)
		{
			if (em != null)
				em.dirtyInstance(<xsl:value-of select="$toentity"/>.this);
			return super.remove(_val);
		}
	}
	DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; <xsl:value-of select="$lowername"/>;
	public DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='1:1']" mode="from_class_contents">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="fromentity" select="from/@entity"/>
	<xsl:variable name="toentity" select="to/@entity"/>

	boolean is<xsl:value-of select="$uppername"/>Realized;
	<xsl:for-each select="column[@from]">String _linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@from"/>;
	</xsl:for-each>
	<xsl:value-of select="to/@entity"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="to/@entity"/> get<xsl:value-of select="$uppername"/>()
	{
		if (!is<xsl:value-of select="$uppername"/>Realized)
		{
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$toentity]"><xsl:with-param name="tableprefix" select="'C'"/><xsl:with-param name="prefix" select="'C_'"/></xsl:apply-templates>"
					+ " FROM <xsl:value-of select="//entity[@name=$toentity]/@table"/> as C"
					+ " WHERE 1=1"
	<xsl:for-each select="column">
					+ " AND C.<xsl:value-of select="@to"/> = " + _linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@from"/>
	</xsl:for-each>
					);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					<xsl:value-of select="$lowername"/> = em.create<xsl:value-of select="to/@entity"/>(rs, "C");
				rs.close();
				stmt.close();
				is<xsl:value-of select="$uppername"/>Realized = true;
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="to/@entity"/> _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		if (get<xsl:value-of select="$uppername"/>() != null)
			<xsl:value-of select="$lowername"/>.set<xsl:value-of select="$upperto"/>(null);
		<xsl:value-of select="$lowername"/> = _val;
		_val.set<xsl:value-of select="$upperto"/>(this);
	}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:1']" mode="to_class_contents">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="fromentity" select="from/@entity"/>
	<xsl:variable name="toentity" select="to/@entity"/>

	boolean is<xsl:value-of select="$uppername"/>Realized;
	<xsl:for-each select="column[@to]">String _linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@to"/>;
	</xsl:for-each>
	<xsl:value-of select="from/@entity"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="from/@entity"/> get<xsl:value-of select="$uppername"/>()
	{
		if (!is<xsl:value-of select="$uppername"/>Realized)
		{
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$fromentity]"><xsl:with-param name="tableprefix" select="'C'"/><xsl:with-param name="prefix" select="'C_'"/></xsl:apply-templates>"
					+ " FROM <xsl:value-of select="//entity[@name=$fromentity]/@table"/> as C"
					+ " WHERE 1=1"
	<xsl:for-each select="column">
					+ " AND C.<xsl:value-of select="@from"/> = " + _linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@to"/>
	</xsl:for-each>
					);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					<xsl:value-of select="$lowername"/> = em.create<xsl:value-of select="from/@entity"/>(rs, "C");
				rs.close();
				stmt.close();
				is<xsl:value-of select="$uppername"/>Realized = true;
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="from/@entity"/> _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		is<xsl:value-of select="$uppername"/>Realized = true;
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:M']" mode="from_class_contents">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="fromentity" select="from/@entity"/>
	<xsl:variable name="toentity" select="to/@entity"/>

	class <xsl:value-of select="$uppername"/>Set extends LazySet&lt;<xsl:value-of select="to/@entity"/>&gt;
	{
		protected VectorSet&lt;<xsl:value-of select="to/@entity"/>&gt; createRealizedSet()
		{
			VectorSet&lt;<xsl:value-of select="to/@entity"/>&gt; newset = new VectorSet&lt;<xsl:value-of select="to/@entity"/>&gt;();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$fromentity]"><xsl:with-param name="tableprefix" select="'A'"/><xsl:with-param name="prefix" select="'A_'"/></xsl:apply-templates>"
					+ ", <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$toentity]"><xsl:with-param name="tableprefix" select="'C'"/><xsl:with-param name="prefix" select="'C_'"/></xsl:apply-templates>"
					+ " FROM <xsl:value-of select="//entity[@name=$fromentity]/@table"/> as A, <xsl:value-of select="//entity[@name=$toentity]/@table"/> AS C, <xsl:value-of select="column[@tableto]/@table"/> as B"
					+ " WHERE 1=1"
	<xsl:for-each select="//entity[@name=$fromentity]/field[@key='true']">
					+ " AND A.<xsl:value-of select="@column"/> = ? "
	</xsl:for-each>
	<xsl:for-each select="column[@from][@to]">
					+ " AND A.<xsl:value-of select="@from"/> = C.<xsl:value-of select="@to"/>"
	</xsl:for-each>
	<xsl:for-each select="column[@from][@tableto]">
					+ " AND A.<xsl:value-of select="@from"/> = B.<xsl:value-of select="@tableto"/>"
	</xsl:for-each>
	<xsl:for-each select="column[@tablefrom][@to]">
					+ " AND B.<xsl:value-of select="@tablefrom"/> = C.<xsl:value-of select="@to"/>"
	</xsl:for-each>
					);
				int idx = 0;
	<xsl:for-each select="//entity[@name=$fromentity]/field[@key='true']">
				idx++;
				stmt.setObject(idx, <xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template>);
	</xsl:for-each>
				ResultSet rs = stmt.executeQuery();
				while ( rs.next() ) {
					newset.add(em.create<xsl:value-of select="to/@entity"/>(rs, "C"));
				}
				rs.close();
				stmt.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		
			return newset;
		}	
		public <xsl:value-of select="$uppername"/>Set(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(<xsl:value-of select="to/@entity"/> _val)
		{
			if (em != null)
				em.dirtyInstance(<xsl:value-of select="$fromentity"/>.this);
			super.add(_val);
			_val.get<xsl:value-of select="$upperto"/>().add(<xsl:value-of select="$entity"/>.this);
			return true;
		}
		public boolean remove(<xsl:value-of select="to/@entity"/> _val)
		{
			if (em != null)
				em.dirtyInstance(<xsl:value-of select="$fromentity"/>.this);
			super.remove(_val);
			_val.get<xsl:value-of select="$upperto"/>().remove(<xsl:value-of select="$entity"/>.this);
			return true;
		}
	}
	DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; <xsl:value-of select="$lowername"/>;
	public DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:M']" mode="to_class_contents">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="fromentity" select="from/@entity"/>
	<xsl:variable name="toentity" select="to/@entity"/>

	class <xsl:value-of select="$uppername"/>Set extends LazySet&lt;<xsl:value-of select="from/@entity"/>&gt;
	{
		protected VectorSet&lt;<xsl:value-of select="from/@entity"/>&gt; createRealizedSet()
		{
			VectorSet&lt;<xsl:value-of select="from/@entity"/>&gt; newset = new VectorSet&lt;<xsl:value-of select="from/@entity"/>&gt;();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$toentity]"><xsl:with-param name="tableprefix" select="'A'"/><xsl:with-param name="prefix" select="'A_'"/></xsl:apply-templates>"
					+ ", <xsl:apply-templates mode="AllEntityColumns" select="//entity[@name=$fromentity]"><xsl:with-param name="tableprefix" select="'C'"/><xsl:with-param name="prefix" select="'C_'"/></xsl:apply-templates>"
					+ " FROM <xsl:value-of select="//entity[@name=$toentity]/@table"/> as A, <xsl:value-of select="//entity[@name=$fromentity]/@table"/> AS C, <xsl:value-of select="column[@tableto]/@table"/> as B "
					+ " WHERE 1=1"
	<xsl:for-each select="//entity[@name=$toentity]/field[@key='true']">
					+ " AND A.<xsl:value-of select="@column"/> = ? "
	</xsl:for-each>
	<xsl:for-each select="column[@from][@to]">
					+ " AND A.<xsl:value-of select="@to"/> = C.<xsl:value-of select="@from"/>"
	</xsl:for-each>
	<xsl:for-each select="column[@from][@tableto]">
					+ " AND B.<xsl:value-of select="@tableto"/> = C.<xsl:value-of select="@from"/>"
	</xsl:for-each>
	<xsl:for-each select="column[@tablefrom][@to]">
					+ " AND A.<xsl:value-of select="@to"/> = B.<xsl:value-of select="@tablefrom"/>"
	</xsl:for-each>
					);
				int idx = 0;
	<xsl:for-each select="//entity[@name=$toentity]/field[@key='true']">
				idx++;
				stmt.setObject(idx, <xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template>);
	</xsl:for-each>
				ResultSet rs = stmt.executeQuery();
				while ( rs.next() ) {
					newset.add(em.create<xsl:value-of select="from/@entity"/>(rs, "C"));
				}
				rs.close();
				stmt.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		
			return newset;
		}	
		public <xsl:value-of select="$uppername"/>Set(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(<xsl:value-of select="from/@entity"/> _val)
		{
			if (em != null)
				em.dirtyInstance(<xsl:value-of select="$toentity"/>.this);
			return super.add(_val);
		}
		public boolean remove(<xsl:value-of select="from/@entity"/> _val)
		{
			if (em != null)
				em.dirtyInstance(<xsl:value-of select="$toentity"/>.this);
			return super.remove(_val);
		}
	}
	DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; <xsl:value-of select="$lowername"/>;
	public DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>





<!-- Generates dispose code for links between entities -->

<xsl:template match="link[from[@entity=$entity]][@map='1:N']" mode="from_dispose">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		get<xsl:value-of select="$uppername"/>().clear();
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:N']" mode="to_dispose">
	<xsl:variable name="upperfrom">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		if (get<xsl:value-of select="$uppername"/>() != null)
			get<xsl:value-of select="$uppername"/>().get<xsl:value-of select="$upperfrom"/>().remove(this);
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:1']" mode="from_dispose">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		set<xsl:value-of select="$uppername"/>(null);
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:1']" mode="to_dispose">
	<xsl:variable name="upperfrom">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		for (<xsl:value-of select="from/@entity"/> obj: get<xsl:value-of select="$uppername"/>())
			obj.set<xsl:value-of select="$upperfrom"/>(null);
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='1:1']" mode="from_dispose">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		set<xsl:value-of select="$uppername"/>(null);
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:1']" mode="to_dispose">
	<xsl:variable name="upperfrom">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		if (get<xsl:value-of select="$uppername"/>() != null)
			get<xsl:value-of select="$uppername"/>().set<xsl:value-of select="$upperfrom"/>(null);
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:M']" mode="from_dispose">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		get<xsl:value-of select="$uppername"/>().clear();
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:M']" mode="to_dispose">
	<xsl:variable name="upperfrom">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		for (<xsl:value-of select="from/@entity"/> obj: get<xsl:value-of select="$uppername"/>())
			obj.get<xsl:value-of select="$upperfrom"/>().remove(this);
</xsl:template>






<!-- Generates comparison copy code for links between entities -->

<xsl:template match="link[from[@entity=$entity]][@map='1:N']" mode="from_comparison_copy">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		copy.<xsl:value-of select="$lowername"/> = <xsl:value-of select="$lowername"/>.comparisonClone();
		if (<xsl:value-of select="$lowername"/> instanceof LazySet &amp;&amp; copy.<xsl:value-of select="$lowername"/> instanceof LazySet)
		{
			((LazySet&lt;<xsl:value-of select="to/@entity"/>&gt;)<xsl:value-of select="$lowername"/>).setRealizeListener((LazySet&lt;<xsl:value-of select="to/@entity"/>&gt;)copy.<xsl:value-of select="$lowername"/>);
		}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:1']" mode="to_comparison_copy">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		copy.<xsl:value-of select="$lowername"/> = <xsl:value-of select="$lowername"/>.comparisonClone();
		if (<xsl:value-of select="$lowername"/> instanceof LazySet)
		{
			((LazySet&lt;<xsl:value-of select="from/@entity"/>&gt;)<xsl:value-of select="$lowername"/>).setRealizeListener((LazySet&lt;<xsl:value-of select="from/@entity"/>&gt;)copy.<xsl:value-of select="$lowername"/>);
		}
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:M']" mode="from_comparison_copy">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		copy.<xsl:value-of select="$lowername"/> = <xsl:value-of select="$lowername"/>.comparisonClone();
		if (<xsl:value-of select="$lowername"/> instanceof LazySet)
		{
			((LazySet&lt;<xsl:value-of select="to/@entity"/>&gt;)<xsl:value-of select="$lowername"/>).setRealizeListener((LazySet&lt;<xsl:value-of select="to/@entity"/>&gt;)copy.<xsl:value-of select="$lowername"/>);
		}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:M']" mode="to_comparison_copy">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		copy.<xsl:value-of select="$lowername"/> = <xsl:value-of select="$lowername"/>.comparisonClone();
		if (<xsl:value-of select="$lowername"/> instanceof LazySet)
		{
			((LazySet&lt;<xsl:value-of select="from/@entity"/>&gt;)<xsl:value-of select="$lowername"/>).setRealizeListener((LazySet&lt;<xsl:value-of select="from/@entity"/>&gt;)copy.<xsl:value-of select="$lowername"/>);
		}
</xsl:template>







<!-- Generates default constructor code for links between entities -->

<xsl:template match="link[from[@entity=$entity]][@map='1:N']" mode="from_default_constructor">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(true);
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:N']" mode="to_default_constructor">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = true;
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:1']" mode="from_default_constructor">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = true;
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:1']" mode="to_default_constructor">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(true);
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='1:1']" mode="from_default_constructor">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = true;
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:1']" mode="to_default_constructor">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = true;
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:M']" mode="from_default_constructor">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(true);
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:M']" mode="to_default_constructor">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(true);
</xsl:template>








<!-- Generates result set constructor code for links between entities -->

<xsl:template match="link[from[@entity=$entity]][@map='1:N']" mode="from_result_set_constructor">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(false);
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:N']" mode="to_result_set_constructor">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = false;
		<xsl:for-each select="column[@to]">_linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@to"/> = rs.getString("LINK_" + prefix + "_<xsl:value-of select="@to"/>");
		</xsl:for-each>
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:1']" mode="from_result_set_constructor">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = false;
		<xsl:for-each select="column[@from]">_linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@from"/> = rs.getString("LINK_" + prefix + "_<xsl:value-of select="@from"/>");
		</xsl:for-each>

</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:1']" mode="to_result_set_constructor">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(false);
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='1:1']" mode="from_result_set_constructor">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = false;
		<xsl:for-each select="column[@from]">_linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@from"/> = rs.getString("LINK_" + prefix + "_<xsl:value-of select="@from"/>");
		</xsl:for-each>

</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:1']" mode="to_result_set_constructor">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = false;
		<xsl:for-each select="column[@to]">_linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@to"/> = rs.getString("LINK_" + prefix + "_<xsl:value-of select="@to"/>");
		</xsl:for-each>

</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:M']" mode="from_result_set_constructor">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(false);
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:M']" mode="to_result_set_constructor">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(false);
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='1:N']" mode="from_result_set_constructor_w_index">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(false);
</xsl:template>



<!-- Generates result set constructor code for links between entities-with index variation -->


<xsl:template match="link[to[@entity=$entity]][@map='1:N']" mode="to_result_set_constructor_w_index">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = false;
		<xsl:for-each select="column[@to]">_linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@to"/> = rs.getString(column);
		column++;
		</xsl:for-each>
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:1']" mode="from_result_set_constructor_w_index">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = false;
		<xsl:for-each select="column[@from]">_linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@from"/> = rs.getString(column);
		column++;
		</xsl:for-each>

</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:1']" mode="to_result_set_constructor_w_index">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(false);
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='1:1']" mode="from_result_set_constructor_w_index">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = false;
		<xsl:for-each select="column[@from]">_linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@from"/> = rs.getString(column);
		column++;
		</xsl:for-each>

</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:1']" mode="to_result_set_constructor_w_index">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		is<xsl:value-of select="$uppername"/>Realized = false;
		<xsl:for-each select="column[@to]">_linkcol_<xsl:value-of select="$lowername"/>_<xsl:value-of select="@to"/> = rs.getString(column);
		column++;
		</xsl:for-each>

</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:M']" mode="from_result_set_constructor_w_index">
	<xsl:variable name="upperto">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="from/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(false);
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:M']" mode="to_result_set_constructor_w_index">
	<xsl:variable name="uppername">
		<xsl:call-template name="FirstCaps">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="lowername">
		<xsl:call-template name="FirstLower">
			<xsl:with-param name="name" select="to/@field"/>
		</xsl:call-template>
	</xsl:variable>

		<xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set(false);
</xsl:template>








<!-- Generates the primary key holder for this entity -->

<xsl:template name="create_key_from_entity">
  <xsl:param name="entity"/>
  <xsl:variable name="num_keys" select="count($entity/field[@key='true'])"/>
  
  <xsl:choose>
    <xsl:when test="$num_keys=0">NoKey</xsl:when>
    <xsl:when test="$num_keys=1"><xsl:call-template name="FirstLower"><xsl:with-param name="name" select="$entity/field[@key='true']/@name"/></xsl:call-template></xsl:when>
    <xsl:when test="$num_keys=2">new Pair&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;(<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>)</xsl:when>
    <xsl:when test="$num_keys=3">new Triple&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;(<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>)</xsl:when>
    <xsl:when test="$num_keys=4">new Quartic&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;(<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>)</xsl:when>
    <xsl:when test="$num_keys=5">new Quintic&lt;<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="NonPrimitive"><xsl:with-param name="type" select="@type"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>&gt;(<xsl:for-each select="$entity/field[@key='true']">
      <xsl:choose>
        <xsl:when test="position()=1"><xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template></xsl:when>
        <xsl:otherwise>, <xsl:call-template name="FirstLower"><xsl:with-param name="name" select="@name"/></xsl:call-template></xsl:otherwise>
      </xsl:choose></xsl:for-each>)</xsl:when>
  </xsl:choose>
    
</xsl:template>



</xsl:stylesheet>