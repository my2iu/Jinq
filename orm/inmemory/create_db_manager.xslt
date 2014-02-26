<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:import href="../shared/helpers.xslt"/>


<xsl:param name="package"/>


<xsl:template match="entities">
package <xsl:value-of select="$package"/>;

public class DBManager
{
	EntityManager em = new EntityManager(this);

	public EntityManager begin()
	{
	}
	
	public void end(EntityManager em, boolean commit)
	{
	}

	public void doTransaction(Transaction transaction)
	{
		transaction.execute(em);
	}

	public static interface Transaction
	{
		public boolean execute(EntityManager em);
	}

	public void close()
	{
	}

<xsl:apply-templates/>
	public DBManager(boolean logQueries) {}
	public DBManager() {}

}
</xsl:template>


</xsl:stylesheet>