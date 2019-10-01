<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [
 <!ENTITY copy    "&#169;">
 <!ENTITY deg     "&#176;">
 <!ENTITY plusmn  "&#177;">
 <!ENTITY Delta   "&#916;">
 <!ENTITY delta   "&#948;">
 <!ENTITY epsilon "&#949;">
 <!ENTITY psi     "&#968;">
 <!ENTITY micro   "&#181;">
 <!ENTITY pi      "&#960;">
 <!ENTITY Pi      "&#928;">
 <!ENTITY frac12  "&#189;">
 <!ENTITY Agrave  "&#192;">
 <!ENTITY Aacute  "&#193;">
 <!ENTITY Acirc   "&#194;">
 <!ENTITY Atilde  "&#195;">
 <!ENTITY Auml    "&#196;">
 <!ENTITY Aring   "&#197;">
 <!ENTITY AElig   "&#198;">
 <!ENTITY Ccedil  "&#199;">
 <!ENTITY Egrave  "&#200;">
 <!ENTITY Eacute  "&#201;">
 <!ENTITY Ecirc   "&#202;">
 <!ENTITY Euml    "&#203;">
 <!ENTITY Igrave  "&#204;">
 <!ENTITY Iacute  "&#205;">
 <!ENTITY Icirc   "&#206;">
 <!ENTITY Iuml    "&#207;">
 <!ENTITY ETH     "&#208;">
 <!ENTITY Ntilde  "&#209;">
 <!ENTITY Ograve  "&#210;">
 <!ENTITY Oacute  "&#211;">
 <!ENTITY Ocirc   "&#212;">
 <!ENTITY Otilde  "&#213;">
 <!ENTITY Ouml    "&#214;">
 <!ENTITY times   "&#215;">
 <!ENTITY Oslash  "&#216;">
 <!ENTITY Ugrave  "&#217;">
 <!ENTITY Uacute  "&#218;">
 <!ENTITY Ucirc   "&#219;">
 <!ENTITY Uuml    "&#220;">
 <!ENTITY Yacute  "&#221;">
 <!ENTITY THORN   "&#222;">
 <!ENTITY szlig   "&#223;">
 <!ENTITY agrave  "&#224;">
 <!ENTITY aacute  "&#225;">
 <!ENTITY acirc   "&#226;">
 <!ENTITY atilde  "&#227;">
 <!ENTITY auml    "&#228;">
 <!ENTITY aring   "&#229;">
 <!ENTITY aelig   "&#230;">
 <!ENTITY ccedil  "&#231;">
 <!ENTITY egrave  "&#232;">
 <!ENTITY eacute  "&#233;">
 <!ENTITY ecirc   "&#234;">
 <!ENTITY euml    "&#235;">
 <!ENTITY igrave  "&#236;">
 <!ENTITY iacute  "&#237;">
 <!ENTITY icirc   "&#238;">
 <!ENTITY iuml    "&#239;">
 <!ENTITY eth     "&#240;">
 <!ENTITY ntilde  "&#241;">
 <!ENTITY ograve  "&#242;">
 <!ENTITY oacute  "&#243;">
 <!ENTITY ocirc   "&#244;">
 <!ENTITY otilde  "&#245;">
 <!ENTITY ouml    "&#246;">
 <!ENTITY divide  "&#247;">
 <!ENTITY oslash  "&#248;">
 <!ENTITY ugrave  "&#249;">
 <!ENTITY uacute  "&#250;">
 <!ENTITY ucirc   "&#251;">
 <!ENTITY uuml    "&#252;">
 <!ENTITY yacute  "&#253;">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:fox="http://xml.apache.org/fop/extensions"
                xmlns:geom-util="http://www.oracle.com/XSL/Transform/java/user.util.GeomUtil"
                xmlns:j-string="http://www.oracle.com/XSL/Transform/java/java.lang.String"
                xmlns:xsl-util="http://www.oracle.com/XSL/Transform/java/calc.calculation.nauticalalmanac.xsl.XSLUtil"
                exclude-result-prefixes="data j-string xsl-util geom-util"
                version="2.0">
  <xsl:import href="literals.xsl"/>
  <xsl:param name="language">EN</xsl:param>

  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="portrait-page"
                               page-width="8.5in"
                               page-height="11in"> <!-- Portrait -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="20mm"/>
        </fo:simple-page-master>
        <fo:simple-page-master master-name="landscape-page"
                               page-height="8.5in"
                               page-width="11in"> <!-- Portrait -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="20mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="portrait-page"> <!-- landscape-page or portrait-page -->
        <fo:static-content flow-name="footer">
          <!--fo:block text-align="center">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block-->
          <fo:block text-align="center" font-size="8pt">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <fo:block break-after="page"> <!-- background-image="url('bg.jpg')"-->
            <!-- First Page -->
            <fo:block text-align="center" font-family="Book Antiqua" font-size="40pt" font-weight="bold" margin="0.5in">
              <xsl:value-of select="$perpetual-almanac"/>
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="10pt" font-weight="bold" margin="0.25in">
              <xsl:value-of select="$oliv-soft"/>
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="30pt" font-weight="bold" margin="1in">
              For the Sun &amp; Aries
            </fo:block>
            <fo:block text-align="center">
              <fo:external-graphic src="url('sextant.gif')"/>
            </fo:block>
            <fo:block margin="0.5in">
              <fo:block text-align="left" font-family="Arial" font-size="6pt" font-style="italic">
                The maximum error of GHA and Dec is about &plusmn; 0.6'.
              </fo:block>
              <fo:block text-align="left" font-family="Arial" font-size="6pt" font-style="italic">
                EoT is accurate &plusmn; 2s.
              </fo:block>
              <fo:block text-align="left" font-family="Arial" font-size="8pt" font-style="italic" margin="0.1in">
                <fo:table>
                  <fo:table-column column-width="2.25in"/>
                  <fo:table-column column-width="6.25in"/>
                  <fo:table-body>
                    <fo:table-row>
                      <fo:table-cell number-columns-spanned="2"><fo:block text-align="left">y : year, m : month, d : day</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell number-columns-spanned="2"><fo:block text-align="left">T = 367&times;y-floor(1.75&times;(y+floor((m+9)/12)))+floor(275&times;(m/9))+d+(UT[h]/24)-730531.5</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">Mean anomaly of the Sun</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">g[&deg;] = 0.9856003&times;T-2.472</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">Mean longitude of the Sun</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">L<fo:inline font-size="6pt" baseline-shift="sub">m</fo:inline> = 0.9856474&times;T-79.53938</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">True longitude of the Sun</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">L<fo:inline font-size="6pt" baseline-shift="sub">t</fo:inline>[&deg;] = L<fo:inline font-size="6pt" baseline-shift="sub">m</fo:inline>[&deg;]+1.915&times;sin(g)+0.02&times;sin(2&times;g)</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">Obliquity of the ecliptic</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left"><fo:inline font-family="Symbol">e</fo:inline>[&deg;] = 23.439-4&times;T.10-7</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left" font-weight="bold">Declination of the Sun</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">Dec[&deg;] = arcsin(sin(L<fo:inline font-size="6pt" baseline-shift="sub">t</fo:inline>)&times;sin(<fo:inline font-family="Symbol">e</fo:inline>))</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">Right Ascension of the Sun</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">RA[&deg;] = 2&times;arctan((cos(<fo:inline font-family="Symbol">e</fo:inline>)&times;sin(L<fo:inline font-size="6pt" baseline-shift="sub">t</fo:inline>))/(cos(Dec)&times;cos(L<fo:inline font-size="6pt" baseline-shift="sub">t</fo:inline>)))</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">GHA Aries</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">GHA<fo:inline font-size="6pt" baseline-shift="sub">Aries</fo:inline> = 0.9865474&times;T+15&times;UT[h]+100.46062</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left" font-weight="bold">Greenwich hour angle of the Sun</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">GHA[&deg;] = GHA<fo:inline font-size="6pt" baseline-shift="sub">Aries</fo:inline>[&deg;] - RA[&deg;]</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left" font-weight="bold">Equation of Time</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">GAT[h] = (GHA[&deg;] / 15) + 12h</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">&nbsp;</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">(if GAT > 24h, substract 24h.)</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">&nbsp;</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">EoT[h] = GAT[h] - UT[h]</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">&nbsp;</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">(if EoT > +0.3h, substract 24h. If EoT &lt; -0.3h, add 24h.)</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left" font-weight="bold">Semidiameter and Horizontal Parallax</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">R[AU] = 1.00014 - 0.01671&times;cos(g) - 0.00014&times;cos(2&times;g)</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">&nbsp;</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">(1AU = 149.6E6 km)</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">&nbsp;</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">SD['] = 16.0 / R[AU]</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell><fo:block text-align="left">&nbsp;</fo:block></fo:table-cell>
                      <fo:table-cell><fo:block text-align="left">hp['] = 0.15</fo:block></fo:table-cell>
                    </fo:table-row>
                  </fo:table-body>
                </fo:table>
              </fo:block>
              <fo:block margin="0.25in"/> <!-- Filler -->
              <fo:block text-align="left" font-family="Arial" font-size="8pt" font-style="italic">
                &copy; Oliv Cool Stuff Soft (<xsl:value-of select="$language"/>), based on Henning Umland's formula.
              </fo:block>
            </fo:block>
          </fo:block>
          <!-- Data go here -->
          <fo:block margin="0.1in">
            <xsl:for-each select="/perpetual-almanac/year/month">
              <xsl:for-each select="./day">
                <xsl:if test="(position() - 1) mod 4 = 0">
                  <xsl:call-template name="4-sun-aries">
                    <xsl:with-param name="d1" select="."/>
                    <xsl:with-param name="d2" select="./following-sibling::*[1]"/>
                    <xsl:with-param name="d3" select="./following-sibling::*[2]"/>
                    <xsl:with-param name="d4" select="./following-sibling::*[3]"/>
                  </xsl:call-template>
                </xsl:if>
              </xsl:for-each>
            </xsl:for-each>
          </fo:block>
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template name="4-sun-aries">
    <xsl:param name="d1"/>
    <xsl:param name="d2"/>
    <xsl:param name="d3"/>
    <xsl:param name="d4"/>
    <fo:block text-align="center" font-family="Courier" font-size="8pt" break-after="page" margin="0.8in">
      <fo:table>
        <fo:table-column column-width="3.2in"/>
        <fo:table-column column-width="3.2in"/>
        <fo:table-body>
          <fo:table-row>
            <xsl:if test="$d1"><fo:table-cell><fo:block text-align="right"><xsl:call-template name="one-day-sun-aries"><xsl:with-param name="d" select="$d1"/></xsl:call-template></fo:block></fo:table-cell></xsl:if>
            <xsl:if test="$d2"><fo:table-cell><fo:block text-align="left"><xsl:call-template name="one-day-sun-aries"><xsl:with-param name="d" select="$d2"/></xsl:call-template></fo:block></fo:table-cell></xsl:if>
          </fo:table-row>
          <fo:table-row>
            <xsl:if test="$d3"><fo:table-cell><fo:block text-align="right"><xsl:call-template name="one-day-sun-aries"><xsl:with-param name="d" select="$d3"/></xsl:call-template></fo:block></fo:table-cell></xsl:if>
            <xsl:if test="$d4"><fo:table-cell><fo:block text-align="left"><xsl:call-template name="one-day-sun-aries"><xsl:with-param name="d" select="$d4"/></xsl:call-template></fo:block></fo:table-cell></xsl:if>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:block>
  </xsl:template>

  <!--xsl:template match="day" name="sun-aries"-->
  <xsl:template name="one-day-sun-aries">
    <xsl:param name="d"/>
    <!-- Sun, aries -->
    <fo:block text-align="left" font-family="Arial" font-size="8pt" font-style="italic" font-weight="bold" margin="0.05in">
      <xsl:call-template name="date-fmt">
        <xsl:with-param name="lang" select="$language"/>
        <xsl:with-param name="year" select="$d/../../@value"/>
        <xsl:with-param name="month" select="$d/../@value"/>
        <xsl:with-param name="day" select="$d/@value"/>
        <!--xsl:with-param name="dow" select="./data:data/data:misc-data/data:dow"/-->
        <xsl:with-param name="dow" select="''"/>
      </xsl:call-template>
    </fo:block>
    <!-- Sun, Aries -->
    <fo:block margin="0.05in">
      <fo:table> <!-- border="0.5pt solid black"> -->
        <fo:table-column column-width="0.30in"/>   <!-- UT -->
        <fo:table-column column-width="0.80in"/>   <!-- Sun GHA -->
        <fo:table-column column-width="0.80in"/>   <!-- Sun Dec -->
        <fo:table-column column-width="0.80in"/>   <!-- Aries GHA -->
        <fo:table-column column-width="0.30in"/>   <!-- UT -->
        <fo:table-header>
          <fo:table-row>
            <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt">UT</fo:block></fo:table-cell>
            <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$sun"/>&nbsp;<fo:external-graphic src="url('sun.png')"/></fo:block></fo:table-cell>
            <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$aries"/>&nbsp;<fo:external-graphic src="url('aries.png')"/></fo:block></fo:table-cell>
            <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt">UT</fo:block></fo:table-cell>
          </fo:table-row>
          <fo:table-row>
            <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$gha"/></fo:block></fo:table-cell>
            <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$dec"/></fo:block></fo:table-cell>
            <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$gha-aries"/></fo:block></fo:table-cell>
          </fo:table-row>
        </fo:table-header>
        <fo:table-body font-size="8pt">
          <xsl:for-each select="$d/data">
            <fo:table-row>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="xsl-util:formatI2(number(./@hours))"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="xsl-util:decToSex(./sun-gha, $GEOMUTIL.SWING, $GEOMUTIL.NONE)" disable-output-escaping="yes"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="xsl-util:decToSex(./sun-dec, $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)" disable-output-escaping="yes"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="xsl-util:decToSex(./aries-gha, $GEOMUTIL.SWING, $GEOMUTIL.NONE)" disable-output-escaping="yes"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="xsl-util:formatI2(number(./@hours))"/></fo:block></fo:table-cell>
            </fo:table-row>
          </xsl:for-each>
          <!-- Extra -->
          <!--fo:table-row>
            <fo:table-cell number-columns-spanned="5"><fo:block>&nbsp;</fo:block></fo:table-cell>
          </fo:table-row-->
          <fo:table-row>
            <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;&frac12;&Oslash;&nbsp;<fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX4($d/data[@hours=12]/sun-sd)"/>'</fo:inline></fo:block></fo:table-cell>
            <fo:table-cell number-columns-spanned="3" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;<xsl:value-of select="$hp"/> (<fo:inline font-family="Symbol">p</fo:inline>) <fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX4($d/data[@hours=12]/sun-hp)"/>'</fo:inline></fo:block></fo:table-cell>
          </fo:table-row>
          <fo:table-row>
            <fo:table-cell number-columns-spanned="5" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">EoT at 12 UT&nbsp;<fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:eotToString($d/data[@hours=12]/eot)"/></fo:inline></fo:block></fo:table-cell>
          </fo:table-row>
        </fo:table-body>
        <!--fo:table-footer>
          That's it
        </fo:table-footer-->
      </fo:table>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>
