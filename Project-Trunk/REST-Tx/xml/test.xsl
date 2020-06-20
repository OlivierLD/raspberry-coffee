<?xml version="1.0" encoding="UTF-8" ?>
<!--
 ! With one loop on an array
 +-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ins="urn://some.stuff">
	<xsl:output method="text" indent="no"/>
	<xsl:template match="/">

		<xsl:variable name="var1" select="concat(
		/ins:invoice/ins:customer/@id, ';',
		/ins:invoice/ins:customer/ins:name/ins:first-name, ';',
		/ins:invoice/ins:customer/ins:name/ins:last-name, ';',
		/ins:invoice/ins:customer/ins:address/ins:number, ';',
		/ins:invoice/ins:customer/ins:address/ins:street, ';',
		/ins:invoice/ins:customer/ins:address/ins:city, ';',
		/ins:invoice/ins:customer/ins:address/ins:zip-code, ';')" />

		<xsl:variable name="var3"
				select="concat(
				/ins:invoice/ins:total/@ins:currency, ';',
				/ins:invoice/ins:total/text(), ';')"/>

		<xsl:for-each select="/ins:invoice/ins:items/ins:item">
			<xsl:variable name="var2" select="concat(
			./ins:sku, ';',
			./ins:description, ';',
			./ins:price/text(), ';',
			./ins:price/@ins:currency, ';')"/>

			<xsl:value-of select="$var1"></xsl:value-of>
			<xsl:value-of select="$var2"></xsl:value-of>
			<xsl:value-of select="$var3"></xsl:value-of>
			<xsl:text>&#xa;</xsl:text>

		</xsl:for-each>

	</xsl:template>
</xsl:stylesheet>
