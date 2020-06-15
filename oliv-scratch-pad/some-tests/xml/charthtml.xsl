<?xml version="1.0" encoding="windows-1252" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="yes"/>
  <xsl:template match="/">
    <html>
      <header>
        <link rel="stylesheet" href="print.css" type="text/css"/>
        <title>Chart List</title>
      </header>
      <body>
        <table border="1">
          <xsl:for-each select="/selection-root/chart">
            <tr>
              <td valign="top"><xsl:value-of select="./@chart-no"/></td>
              <td valign="top"><xsl:value-of select="./text()"/></td>
              <td valign="top"><xsl:value-of select="./@provider"/></td>
              <td valign="top"><xsl:value-of select="./@year"/></td>
            </tr>
          </xsl:for-each>
        </table>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
