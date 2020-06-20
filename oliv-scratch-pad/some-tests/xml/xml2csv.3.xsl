<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csv="csv:csv">
	<xsl:output method="text" encoding="utf-8" />
	<xsl:strip-space elements="*" />

	<xsl:variable name="delimiter" select="','" />

	<csv:columns>
		<column>name</column>
		<column>sublease</column>
		<column>addressBookID</column>
		<column>boundAmount</column>
		<column>rentalAmount</column>
		<column>rentalPeriod</column>
		<column>rentalBillingCycle</column>
		<column>tenureIncome</column>
		<column>tenureBalance</column>
		<column>totalIncome</column>
		<column>balance</column>
		<column>available</column>
	</csv:columns>

	<xsl:template match="/property-manager/properties">
		<!-- Output the CSV header -->
		<xsl:for-each select="document('')/*/csv:columns/*">
			<xsl:value-of select="."/>
			<xsl:if test="position() != last()">
				<xsl:value-of select="$delimiter"/>
			</xsl:if>
		</xsl:for-each>
		<xsl:text>&#xa;</xsl:text>

		<!-- Output rows for each matched property -->
		<xsl:apply-templates select="property" />
	</xsl:template>

	<xsl:template match="property">
		<xsl:variable name="property" select="." />

		<!-- Loop through the columns in order -->
		<xsl:for-each select="document('')/*/csv:columns/*">
			<!-- Extract the column name and value -->
			<xsl:variable name="column" select="." />
			<xsl:variable name="value" select="$property/*[name() = $column]" />

			<!-- Quote the value if required -->
			<xsl:choose>
				<xsl:when test="contains($value, '&quot;')">
					<xsl:variable name="x" select="replace($value, '&quot;',  '&quot;&quot;')"/>
					<xsl:value-of select="concat('&quot;', $x, '&quot;')"/>
				</xsl:when>
				<xsl:when test="contains($value, $delimiter)">
					<xsl:value-of select="concat('&quot;', $value, '&quot;')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$value"/>
				</xsl:otherwise>
			</xsl:choose>

			<!-- Add the delimiter unless we are the last expression -->
			<xsl:if test="position() != last()">
				<xsl:value-of select="$delimiter"/>
			</xsl:if>
		</xsl:for-each>

		<!-- Add a newline at the end of the record -->
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

</xsl:stylesheet>
