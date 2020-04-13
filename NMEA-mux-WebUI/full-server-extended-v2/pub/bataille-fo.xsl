<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:fox="http://xml.apache.org/fop/extensions" 
                version="1.0">
  <xsl:variable name="table-I-width" select="16"/>
  <xsl:variable name="table-II-width" select="15"/>
  
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
            <fo:block text-align="center" font-family="Book Antiqua" font-size="15pt" font-weight="bold" margin="0.51in">
              L. Bataille
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="20pt" font-weight="bold" margin="0.5in">
              Nouvelles tables nautiques
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="40pt" font-weight="bold" margin="1in">
              Azimut des astres par l'heure
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="18pt" font-weight="bold" margin="0.5in">
              pour tous les astres, par toutes les latitudes et &#224; tous les instants
            </fo:block>
            <!--fo:block text-align="center">
              <fo:external-graphic src="url('sextant.gif')"/>
            </fo:block-->
            <fo:block text-align="left" font-family="Times" font-size="8pt" font-style="italic" margin="0.5in">
              &#169; Oliv Cool Stuff Soft  
            </fo:block>
            <!--fo:block text-align="left" font-family="Times" font-size="8pt" font-style="italic" margin="0.5in">
              <fo:inline font-style="normal">Note:</fo:inline> contrairement &#224; la convention habituelle pour les valeurs n&#233;gatives, 
              o&#249; la partie enti&#232;re est surlign&#233;e,
              on l'a ici <fo:inline font-weight="bold">soulign&#233;e</fo:inline>.
            </fo:block-->
          </fo:block>
          <fo:block margin="0.4in">
            <xsl:for-each select="//table[@id=1]">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="//table[@id=2]">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
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
          <xsl:with-param name="inf" select="1"/>
        </xsl:call-template>
        <xsl:call-template name="table1">
          <xsl:with-param name="inf" select="$table-I-width + 1"/>
        </xsl:call-template>
        <xsl:call-template name="table1">
          <xsl:with-param name="inf" select="($table-I-width * 2) + 1"/>
        </xsl:call-template>
        <xsl:call-template name="table1">
          <xsl:with-param name="inf" select="($table-I-width * 3) + 1"/>
        </xsl:call-template>
      </xsl:for-each>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="table[@id=2]">
    <fo:block text-align="center" font-family="Courier" font-size="6.5pt">
      <xsl:for-each select=".">
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="1"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="$table-II-width + 1"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="($table-II-width * 2) + 1"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="($table-II-width * 3) + 1"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="($table-II-width * 4) + 1"/>
        </xsl:call-template>
        <xsl:call-template name="table2">
          <xsl:with-param name="inf" select="($table-II-width * 5) + 1"/>
        </xsl:call-template>
      </xsl:for-each>
    </fo:block>
  </xsl:template>
  
  <xsl:template name="table1">
    <xsl:param name="inf" select="0"/>
    <fo:block  break-after="page">
      <fo:block text-align="left"  font-weight="bold" font-family="Courier" font-size="10pt">Table I.</fo:block>
      <fo:table border="0">
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
      
        <xsl:for-each select="lat">
          <fo:table-body>
            <xsl:if test="position() = 1">
              <!-- First line labels -->
              <fo:table-row>
                <fo:table-cell/>
                <xsl:for-each select="value[position() &gt;= $inf and position() &lt; ($inf + $table-I-width)]">                  
                  <fo:table-cell padding="medium"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="underline"--><xsl:value-of select="./@h"/><!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell/>
              </fo:table-row>
              <fo:table-row>
                <fo:table-cell/>
                <xsl:for-each select="value[position() &gt;= $inf and position() &lt; ($inf + $table-I-width)]">                  
                  <fo:table-cell padding="medium"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="underline"--><xsl:value-of select="./@p"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell/>
              </fo:table-row>
            </xsl:if>
            <fo:table-row>
              <!-- First column -->
              <fo:table-cell><fo:block text-align="left" font-weight="bold"><xsl:value-of select="./@val"/>&#176;</fo:block></fo:table-cell>
              <!-- data -->
              <xsl:for-each select="value[position() &gt;= $inf and position() &lt; ($inf + $table-I-width)]">                  
                <fo:table-cell text-align="center">
                  <fo:block>
                    <xsl:value-of select="."/>
                  </fo:block>
              </fo:table-cell>
              </xsl:for-each>
              <!-- Last column -->
              <fo:table-cell><fo:block text-align="right" font-weight="bold"><xsl:value-of select="90 - ./@val"/>&#176;</fo:block></fo:table-cell>
            </fo:table-row>
            <xsl:if test="position() = last()">
              <!-- Last line labels -->
              <fo:table-row>
                <fo:table-cell/>
                <xsl:for-each select="value[position() &gt;= $inf and position() &lt; ($inf + $table-I-width)]">                  
                  <fo:table-cell padding="medium" border="0"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="overline"--><xsl:value-of select="180 - ./@p"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell/>
              </fo:table-row>
              <fo:table-row>
                <fo:table-cell/>
                <xsl:for-each select="value[position() &gt;= $inf and position() &lt; ($inf + $table-I-width)]">                  
                  <fo:table-cell padding="medium"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="underline"--><xsl:value-of select="./@h2"/><!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell/>
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
      <fo:block text-align="left"  font-weight="bold" font-family="Courier" font-size="10pt">Table II.</fo:block>
      <fo:table border="0">
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
        <fo:table-column column-width="0.4in"/>
      
        <xsl:for-each select="lat">
          <fo:table-body>
            <xsl:if test="position() = 1">
              <!-- First line labels -->
              <!--fo:table-row>
                <fo:table-cell number-columns-spanned="18"><fo:block font-style="italic" font-weight="bold">Tangente</fo:block></fo:table-cell>
              </fo:table-row>
              <fo:table-row>
                <fo:table-cell/>
                <fo:table-cell number-columns-spanned="15"><fo:block font-style="normal" font-weight="bold">Declinaison</fo:block></fo:table-cell>
                <fo:table-cell/>
                <fo:table-cell/>
              </fo:table-row-->
              <fo:table-row>
                <fo:table-cell><fo:block text-align="left">Lat.</fo:block></fo:table-cell>
                <xsl:for-each select="value[position() &gt;= $inf and position() &lt; ($inf + $table-II-width)]">                  
                  <fo:table-cell padding="medium"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="underline"--><xsl:value-of select="./@p"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
                </xsl:for-each>
                <fo:table-cell number-columns-spanned="2"><fo:block>Angle au P&#244;le</fo:block></fo:table-cell>
              </fo:table-row>
            </xsl:if>
            <fo:table-row>
              <!-- First column -->
              <fo:table-cell><fo:block text-align="left" font-weight="bold"><xsl:value-of select="./@val"/>&#176;</fo:block></fo:table-cell>
              <!-- data -->
              <xsl:for-each select="value[position() &gt;= $inf and position() &lt; ($inf + $table-II-width)]">                  
                <fo:table-cell text-align="center">
                  <fo:block>
                    <xsl:value-of select="."/>
                  </fo:block>
              </fo:table-cell>
              </xsl:for-each>
              <!-- Last columns -->
              <fo:table-cell><fo:block text-align="right" font-weight="bold"><xsl:value-of select="./@h1"/></fo:block></fo:table-cell>
              <fo:table-cell><fo:block text-align="right" font-weight="bold"><xsl:value-of select="./@h2"/></fo:block></fo:table-cell>
            </fo:table-row>
            <xsl:if test="position() = last()">
              <!-- Last line labels -->
              <fo:table-row>
                <fo:table-cell>&nbsp;</fo:table-cell>
                <xsl:for-each select="value[position() &gt;= $inf and position() &lt; ($inf + $table-II-width)]">                  
                  <fo:table-cell padding="medium" border="0"><fo:block text-align="center" font-weight="bold"><!--fo:inline text-decoration="overline"--><xsl:value-of select="90 - ./@p"/>&#176;<!--/fo:inline--></fo:block></fo:table-cell>
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
