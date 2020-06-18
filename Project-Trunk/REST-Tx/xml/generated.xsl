<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ins="urn://some.stuff">
	<xsl:output method="text" indent="no"/>
	<xsl:template match="/">
		<xsl:value-of select="/ins:invoice/ins:customer/@id"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:value-of select="/ins:invoice/ins:customer/ins:name/ins:first-name"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:value-of select="/ins:invoice/ins:customer/ins:name/ins:last-name"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:value-of select="/ins:invoice/ins:customer/ins:address/ins:number"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:value-of select="/ins:invoice/ins:customer/ins:address/ins:street"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:value-of select="/ins:invoice/ins:customer/ins:address/ins:city"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:value-of select="/ins:invoice/ins:customer/ins:address/ins:zip-code"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:value-of select="/ins:invoice/ins:total/@ins:currency"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:value-of select="/ins:invoice/ins:total/text()"></xsl:value-of><xsl:text>;</xsl:text>
		<xsl:text>
</xsl:text>
	</xsl:template>
</xsl:stylesheet>
