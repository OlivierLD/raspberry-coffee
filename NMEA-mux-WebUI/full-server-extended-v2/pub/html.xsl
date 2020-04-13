<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- Root template -->
	<xsl:template match="/">
		<html>
			<head>
				<title>Tables de Dieumegard</title>
			</head>
			<body>
				<h2>Table 1</h2>
				<xsl:for-each select="//table[@id=1]">
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="180"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="195"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="210"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="225"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="240"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="255"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="270"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="285"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="300"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="315"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="330"/>
					</xsl:call-template>
					<xsl:call-template name="table1">
						<xsl:with-param name="inf" select="345"/>
					</xsl:call-template>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="table1" match="//table[@id=1]">
		<xsl:param name="inf" select="0"/>
		<table border="1">
			<xsl:for-each select="min">
				<xsl:if test="position() = 1">
					<!-- First line labels -->
					<tr>
						<td>&nbsp;</td>
						<xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">
							<td align="center">
								<b><xsl:value-of select="./@deg"/>&#176;
								</b>
							</td>
						</xsl:for-each>
						<td>&nbsp;</td>
					</tr>
				</xsl:if>
				<tr>
					<!-- First column -->
					<td>
						<b><xsl:value-of select="./@val"/>&#39;
						</b>
					</td>
					<!-- data -->
					<xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">
						<td>
							<xsl:value-of select="."/>
						</td>
					</xsl:for-each>
					<!-- Last column -->
					<td align="right">
						<b><xsl:value-of select="60 - ./@val"/>&#39;
						</b>
					</td>
				</tr>
				<xsl:if test="position() = last()">
					<!-- Last line labels -->
					<tr>
						<td>&nbsp;</td>
						<xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">
							<td align="center">
								<b><xsl:value-of select="359 - ./@deg"/>&#176;
								</b>
							</td>
						</xsl:for-each>
						<td>&nbsp;</td>
					</tr>
				</xsl:if>
			</xsl:for-each>
		</table>
		<address>page
			<xsl:value-of select="($inf div 15) -11"/>
		</address>
	</xsl:template>
</xsl:stylesheet>
