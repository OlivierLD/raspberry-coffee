<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:fox="http://xml.apache.org/fop/extensions" 
                version="1.0">
  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="my-page"
                               page-width="8.5in"
                               page-height="11in">
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="10mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="my-page">
        <fo:static-content flow-name="footer">
          <fo:block text-align="center" font-size="6pt">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <fo:block break-after="page"> <!-- background-image="url('bg.jpg')"-->
            <fo:block text-align="center" font-family="Book Antiqua" font-size="15pt" font-weight="bold" margin="1in">
              B&#233;ziers Boat Design
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="20pt" font-weight="bold" margin="1in">
              <xsl:value-of select="/boat-design/boat-data/file-name"/>
            </fo:block>
            <fo:block text-align="left" font-family="Book Antiqua" font-size="14pt" font-weight="bold" margin="0.5in">
              <xsl:for-each select="/boat-design/boat-data/comments/comment">
                <fo:block><xsl:value-of select="."/></fo:block>
              </xsl:for-each>
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="14pt" font-weight="bold" margin="0.5in">
              <xsl:value-of select="/boat-design/boat-data/description"/>
            </fo:block>
            <!--fo:block text-align="center">
              <fo:external-graphic src="url('sextant.gif')"/>
            </fo:block-->
            <fo:block text-align="left" font-family="Times" font-size="8pt" font-style="italic" margin="0.5in">
              &#169; Oliv Cool Stuff Soft  
            </fo:block>
          </fo:block>

          <fo:block margin="0.4in">

            <fo:block text-align="center">
              <!--fo:external-graphic src="url('../XY.png')"/-->
              <fo:external-graphic src="url('{/boat-design/drawings/from-top}')" />
            </fo:block>

          </fo:block>

          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
  <xsl:template match="table[@id=1]">
    <fo:block text-align="center" font-family="Courier" font-size="6.5pt">
      <xsl:for-each select=".">
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
    </fo:block>
  </xsl:template>
  
  <xsl:template match="table[@id=2]">
    <fo:block text-align="center" font-family="Courier" font-size="6.5pt">
      <xsl:for-each select=".">
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="0"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="15"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="30"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="45"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="60"/>
        </xsl:call-template>
      </xsl:for-each>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="table[@id=3]">
    <fo:block text-align="center" font-family="Courier" font-size="6.5pt">
      <xsl:for-each select=".">
        <xsl:call-template name="table3">
          <xsl:with-param name="inf" select="0"/>
        </xsl:call-template>
        <xsl:call-template name="table3">
          <xsl:with-param name="inf" select="15"/>
        </xsl:call-template>
        <xsl:call-template name="table3">
          <xsl:with-param name="inf" select="30"/>
        </xsl:call-template>
        <xsl:call-template name="table3">
          <xsl:with-param name="inf" select="45"/>
        </xsl:call-template>
        <xsl:call-template name="table3">
          <xsl:with-param name="inf" select="60"/>
        </xsl:call-template>
        <xsl:call-template name="table3">
          <xsl:with-param name="inf" select="75"/>
        </xsl:call-template>
      </xsl:for-each>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="table[@id='A']">
    <fo:block text-align="center" font-family="Courier" font-size="6.5pt">
      <xsl:for-each select=".">
        <xsl:call-template name="tableA">
          <xsl:with-param name="inf" select="0"/>
        </xsl:call-template>
        <xsl:call-template name="tableA">
          <xsl:with-param name="inf" select="15"/>
        </xsl:call-template>
        <xsl:call-template name="tableA">
          <xsl:with-param name="inf" select="30"/>
        </xsl:call-template>
        <xsl:call-template name="tableA">
          <xsl:with-param name="inf" select="45"/>
        </xsl:call-template>
        <xsl:call-template name="tableA">
          <xsl:with-param name="inf" select="60"/>
        </xsl:call-template>
        <xsl:call-template name="tableA">
          <xsl:with-param name="inf" select="75"/>
        </xsl:call-template>
      </xsl:for-each>
    </fo:block>
  </xsl:template>
  
  <xsl:template name="table1">
    <xsl:param name="inf" select="0"/>
    <fo:block  break-after="page">
      <fo:block font-weight="bold" font-family="Courier" font-size="10pt">Table 1</fo:block>
      <fo:table border="0">
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.4in"/>
      
        <xsl:for-each select="min">
          <fo:table-body>
            <xsl:if test="position() = 1">
              <!-- First line labels -->
              <fo:table-row>
                <fo:table-cell>&nbsp;</fo:table-cell>
                <xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">                  
                  <fo:table-cell padding="medium"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="underline"--><xsl:value-of select="./@deg"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell>&nbsp;</fo:table-cell>
              </fo:table-row>
              <fo:table-row height="auto">
                <fo:table-cell number-columns-spanned="16">&nbsp;</fo:table-cell>
              </fo:table-row>
            </xsl:if>
            <fo:table-row>
              <!-- First column -->
              <fo:table-cell><fo:block text-align="left" font-weight="bold"><xsl:value-of select="./@val"/>&#39;</fo:block></fo:table-cell>
              <!-- data -->
              <xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">                  
                <fo:table-cell text-align="right">
                  <fo:block>
                    <xsl:choose>
                      <xsl:when test="./@neg &gt; 0">
                        <fo:inline text-decoration="underline"><xsl:value-of select="./@neg"/></fo:inline>.<xsl:value-of select="mant"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="int"/>.<xsl:value-of select="mant"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </fo:block>
              </fo:table-cell>
              </xsl:for-each>
              <!-- Last column -->
              <fo:table-cell><fo:block text-align="right" font-weight="bold"><xsl:value-of select="60 - ./@val"/>&#39;</fo:block></fo:table-cell>
            </fo:table-row>
            <xsl:if test="position() = last()">
              <!-- Last line labels -->
              <fo:table-row height="auto">
                <fo:table-cell number-columns-spanned="16">&nbsp;</fo:table-cell>
              </fo:table-row>
              <fo:table-row>
                <fo:table-cell>&nbsp;</fo:table-cell>
                <xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">                  
                  <fo:table-cell padding="medium" border="0"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="overline"--><xsl:value-of select="359 - ./@deg"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell>&nbsp;</fo:table-cell>
              </fo:table-row>
            </xsl:if>
          </fo:table-body>
        </xsl:for-each>
      </fo:table>
    </fo:block>
  </xsl:template>
  
  <xsl:template name="table2">
    <xsl:param name="inf" select="0"/>
    <fo:block  break-after="page">
      <fo:block font-weight="bold" font-family="Courier" font-size="10pt">Table 2</fo:block>
      <fo:table border="0">
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
      
        <xsl:for-each select="min">
          <fo:table-body>
            <xsl:if test="position() = 1">
              <!-- First line labels -->
              <fo:table-row>
                <fo:table-cell>&nbsp;</fo:table-cell>
                <xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">                  
                  <fo:table-cell padding="medium"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="underline"--><xsl:value-of select="./@deg"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
              </fo:table-row>
              <fo:table-row height="auto">
                <fo:table-cell number-columns-spanned="15">&nbsp;</fo:table-cell>
              </fo:table-row>
            </xsl:if>
            <fo:table-row>
              <!-- First column -->
              <fo:table-cell><fo:block text-align="left" font-weight="bold"><xsl:value-of select="./@val"/>&#39;</fo:block></fo:table-cell>
              <!-- data -->
              <xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">                  
                <fo:table-cell text-align="right"><fo:block><xsl:value-of select="."/></fo:block></fo:table-cell>
              </xsl:for-each>
            </fo:table-row>
          </fo:table-body>
        </xsl:for-each>
      </fo:table>
    </fo:block>
  </xsl:template>
  
  <xsl:template name="table3">
    <xsl:param name="inf" select="0"/>
    <fo:block  break-after="page">
      <fo:block font-weight="bold" font-family="Courier" font-size="10pt">Table 3</fo:block>
      <fo:table border="0">
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
      
        <xsl:for-each select="du">
          <fo:table-body>
            <xsl:if test="position() = 1">
              <!-- First line labels -->
              <fo:table-row>
                <fo:table-cell>&nbsp;</fo:table-cell>
                <xsl:for-each select="value[@mc &gt;= $inf and @mc &lt;= ($inf + 14)]">                  
                  <fo:table-cell padding="medium"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="underline"--><xsl:value-of select="./@mc"/><!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
              </fo:table-row>
              <fo:table-row height="auto">
                <fo:table-cell number-columns-spanned="15">&nbsp;</fo:table-cell>
              </fo:table-row>
            </xsl:if>
            <fo:table-row>
              <!-- First column -->
              <fo:table-cell><fo:block text-align="left" font-weight="bold"><xsl:value-of select="./@val"/></fo:block></fo:table-cell>
              <!-- data -->
              <xsl:for-each select="value[@mc &gt;= $inf and @mc &lt;= ($inf + 14)]">                  
                <fo:table-cell text-align="right"><fo:block><xsl:value-of select="."/></fo:block></fo:table-cell>
              </xsl:for-each>
            </fo:table-row>
          </fo:table-body>
        </xsl:for-each>
      </fo:table>
    </fo:block>
  </xsl:template>
  
  <xsl:template name="tableA">
    <xsl:param name="inf" select="0"/>
    <fo:block  break-after="page">
      <fo:block font-weight="bold" font-family="Courier" font-size="10pt">Table A</fo:block>
      <fo:table border="0">
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.45in"/>
        <fo:table-column column-width="0.4in"/>
      
        <xsl:for-each select="min">
          <fo:table-body>
            <xsl:if test="position() = 1">
              <!-- First line labels -->
              <fo:table-row>
                <fo:table-cell>&nbsp;</fo:table-cell>
                <xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">                  
                  <fo:table-cell padding="medium"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="underline"--><xsl:value-of select="./@deg"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell>&nbsp;</fo:table-cell>
              </fo:table-row>
              <fo:table-row height="auto">
                <fo:table-cell number-columns-spanned="16">&nbsp;</fo:table-cell>
              </fo:table-row>
            </xsl:if>
            <fo:table-row>
              <!-- First column -->
              <fo:table-cell><fo:block text-align="left" font-weight="bold"><xsl:value-of select="./@val"/>&#39;</fo:block></fo:table-cell>
              <!-- data -->
              <xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">                  
                <fo:table-cell text-align="right"><fo:block><xsl:value-of select="."/></fo:block></fo:table-cell>
              </xsl:for-each>
              <!-- Last column -->
              <fo:table-cell><fo:block text-align="right" font-weight="bold"><xsl:value-of select="60 - ./@val"/>&#39;</fo:block></fo:table-cell>
            </fo:table-row>
            <xsl:if test="position() = last()">
              <!-- Last line labels -->
              <fo:table-row height="auto">
                <fo:table-cell number-columns-spanned="16">&nbsp;</fo:table-cell>
              </fo:table-row>
              <fo:table-row>
                <fo:table-cell>&nbsp;</fo:table-cell>
                <xsl:for-each select="value[@deg &gt;= $inf and @deg &lt;= ($inf + 14)]">                  
                  <fo:table-cell padding="medium" border="0"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="overline"--><xsl:value-of select="89 - ./@deg"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell>&nbsp;</fo:table-cell>
              </fo:table-row>
            </xsl:if>
          </fo:table-body>
        </xsl:for-each>
      </fo:table>
    </fo:block>
  </xsl:template>
  
</xsl:stylesheet>
