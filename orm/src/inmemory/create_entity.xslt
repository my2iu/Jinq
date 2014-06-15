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

public class <xsl:value-of select="@name"/>
{
	EntityManager em;

	public <xsl:value-of select="@name"/>(EntityManager em)
	{
		this.em = em;
		em.newInstance(this);
	}

<xsl:apply-templates mode="class_contents"/>
<xsl:apply-templates select="../link" mode="class_contents"/>

	public void dispose()
	{
<xsl:apply-templates select="../link" mode="dispose"/>
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
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>



<!-- Generates class code for links between entities -->

<xsl:template match="link[from[@entity=$entity]][@map='1:N']" mode="class_contents">
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

	class <xsl:value-of select="$uppername"/>Set extends DBSet&lt;<xsl:value-of select="to/@entity"/>&gt;
	{
		public boolean add(<xsl:value-of select="to/@entity"/> _val)
		{
			super.add(_val);
			_val.set<xsl:value-of select="$upperto"/>(<xsl:value-of select="$entity"/>.this);
			return true;
		}
		public boolean remove(<xsl:value-of select="to/@entity"/> _val)
		{
			super.remove(_val);
			_val.set<xsl:value-of select="$upperto"/>(null);
			return true;
		}
	}
	DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; <xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set();
	public DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:N']" mode="class_contents">
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

	<xsl:value-of select="from/@entity"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="from/@entity"/> get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="from/@entity"/> _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:1']" mode="class_contents">
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

	<xsl:value-of select="to/@entity"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="to/@entity"/> get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="to/@entity"/> _val)
	{
		if (<xsl:value-of select="$lowername"/> != null)
			<xsl:value-of select="$lowername"/>.get<xsl:value-of select="$upperto"/>().remove(this);
		<xsl:value-of select="$lowername"/> = _val;
		_val.get<xsl:value-of select="$upperto"/>().add(this);
	}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:1']" mode="class_contents">
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

	DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; <xsl:value-of select="$lowername"/> = new DBSet&lt;<xsl:value-of select="from/@entity"/>&gt;();
	public DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='1:1']" mode="class_contents">
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

	<xsl:value-of select="to/@entity"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="to/@entity"/> get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="to/@entity"/> _val)
	{
		if (<xsl:value-of select="$lowername"/> != null)
			<xsl:value-of select="$lowername"/>.set<xsl:value-of select="$upperto"/>(null);
		<xsl:value-of select="$lowername"/> = _val;
		_val.set<xsl:value-of select="$upperto"/>(this);
	}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='1:1']" mode="class_contents">
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

	<xsl:value-of select="from/@entity"/><xsl:text> </xsl:text><xsl:value-of select="$lowername"/>;
	public <xsl:value-of select="from/@entity"/> get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(<xsl:value-of select="from/@entity"/> _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[from[@entity=$entity]][@map='N:M']" mode="class_contents">
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

	class <xsl:value-of select="$uppername"/>Set extends DBSet&lt;<xsl:value-of select="to/@entity"/>&gt;
	{
		public boolean add(<xsl:value-of select="to/@entity"/> _val)
		{
			super.add(_val);
			_val.get<xsl:value-of select="$upperto"/>().add(<xsl:value-of select="$entity"/>.this);
			return true;
		}
		public boolean remove(<xsl:value-of select="to/@entity"/> _val)
		{
			super.remove(_val);
			_val.get<xsl:value-of select="$upperto"/>().remove(<xsl:value-of select="$entity"/>.this);
			return true;
		}
	}
	DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; <xsl:value-of select="$lowername"/> = new <xsl:value-of select="$uppername"/>Set();
	public DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; get<xsl:value-of select="$uppername"/>()
	{
		return <xsl:value-of select="$lowername"/>;
	}
	public void set<xsl:value-of select="$uppername"/>(DBSet&lt;<xsl:value-of select="to/@entity"/>&gt; _val)
	{
		<xsl:value-of select="$lowername"/> = _val;
	}
</xsl:template>

<xsl:template match="link[to[@entity=$entity]][@map='N:M']" mode="class_contents">
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

	DBSet&lt;<xsl:value-of select="from/@entity"/>&gt; <xsl:value-of select="$lowername"/> = new DBSet&lt;<xsl:value-of select="from/@entity"/>&gt;();
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

<xsl:template match="link[from[@entity=$entity]][@map='1:N']" mode="dispose">
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

<xsl:template match="link[to[@entity=$entity]][@map='1:N']" mode="dispose">
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

<xsl:template match="link[from[@entity=$entity]][@map='N:1']" mode="dispose">
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

<xsl:template match="link[to[@entity=$entity]][@map='N:1']" mode="dispose">
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

<xsl:template match="link[from[@entity=$entity]][@map='1:1']" mode="dispose">
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

<xsl:template match="link[to[@entity=$entity]][@map='1:1']" mode="dispose">
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

<xsl:template match="link[from[@entity=$entity]][@map='N:M']" mode="dispose">
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

<xsl:template match="link[to[@entity=$entity]][@map='N:M']" mode="dispose">
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


</xsl:stylesheet>