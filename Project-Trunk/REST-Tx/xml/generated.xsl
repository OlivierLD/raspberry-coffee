<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns0="http://www.oracle.com/pcbpel/Invoice">
	<xsl:output method="text" indent="no"/>
	<xsl:template match="/">
		<xsl:variable name="var1" select="concat(/InvoiceType/InvoiceDate, ';',
/InvoiceType/Comment, ';',
/InvoiceType/CustomerID, ';',
/InvoiceType/ShippedTo/Name, ';',
/InvoiceType/ShippedTo/Address/Address1, ';',
/InvoiceType/ShippedTo/Address/City, ';',
/InvoiceType/ShippedTo/Address/Zip, ';')"/>
		<xsl:for-each select="/InvoiceType/ShippedItems/Item">
			<xsl:variable name="var2" select="concat(
./itemNumber, ';',
./ProductName, ';',
./Quantity, ';',
./PriceCharged, ';')"/>
			<xsl:value-of select="$var1"></xsl:value-of>
			<xsl:value-of select="$var2"></xsl:value-of>
			<xsl:text>&#xa;</xsl:text>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
