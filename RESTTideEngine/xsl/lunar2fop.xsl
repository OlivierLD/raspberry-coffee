<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [
 <!ENTITY copy    "&#169;">
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
                xmlns:data="urn:nautical-almanac"
                xmlns:geom-util="http://www.oracle.com/XSL/Transform/java/user.util.GeomUtil"
                xmlns:j-string="http://www.oracle.com/XSL/Transform/java/java.lang.String"
                xmlns:xsl-util="http://www.oracle.com/XSL/Transform/java/calc.calculation.nauticalalmanac.xsl.XSLUtil"
                exclude-result-prefixes="data j-string xsl-util geom-util"
                version="1.0">
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
                               page-width="11in"> <!-- Landscape -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="20mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="portrait-page">
        <fo:static-content flow-name="footer">
          <!--fo:block text-align="center">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block-->
          <fo:block text-align="center" font-size="8pt">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <fo:block break-after="page"> <!-- background-image="url('bg.jpg')"-->
            <!-- First Page -->
            <fo:block text-align="center" font-family="Book Antiqua" font-size="40pt" font-weight="bold" margin="0.5in">
              <xsl:value-of select="$lunar-distances"/>
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="10pt" font-weight="bold" margin="0.25in">
              <xsl:value-of select="$oliv-soft"/>
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="30pt" font-weight="bold" margin="1in">
              <xsl:choose>
                <xsl:when test="/data:almanac/@type = 'continuous'">
                  <xsl:value-of select="$for"/>
                  <xsl:call-template name="front-page-date">
                    <xsl:with-param name="nb-month"><xsl:value-of select="count(/data:almanac/data:year/data:month)"/></xsl:with-param>
                    <xsl:with-param name="y"><xsl:value-of select="/data:almanac/data:year/@value"/></xsl:with-param>
                    <xsl:with-param name="nb-day"><xsl:value-of select="count(/data:almanac/data:year/data:month/data:day)"/></xsl:with-param>
                    <xsl:with-param name="m"><xsl:value-of select="number(/data:almanac/data:year/data:month/@value)"/></xsl:with-param>
                    <xsl:with-param name="dow"><xsl:value-of select="//data:misc-data/data:dow"/></xsl:with-param>
                    <xsl:with-param name="d"><xsl:value-of select="number(/data:almanac/data:year/data:month/data:day/@value)"/></xsl:with-param>
                  </xsl:call-template>
                </xsl:when>
                <xsl:when test="/data:almanac/@type = 'from-to'">
                  <xsl:value-of select="$from"/>
                  <xsl:call-template name="date-fmt">
                    <xsl:with-param name="lang" select="$language"/>
                    <xsl:with-param name="year" select="//data:year[1]/@value"/>
                    <xsl:with-param name="month" select="//data:year[1]/data:month[1]/@value"/>
                    <xsl:with-param name="day" select="//data:year[1]/data:month[1]/data:day[1]/@value"/>
                    <xsl:with-param name="dow" select="//data:year[1]/data:month[1]/data:day[1]/data:data[1]/data:misc-data/data:dow"/>
                  </xsl:call-template>
                  <xsl:value-of select="$to"/>

                  <xsl:call-template name="date-fmt">
                    <xsl:with-param name="lang" select="$language"/>
                    <xsl:with-param name="year" select="//data:year[last()]/@value"/>
                    <xsl:with-param name="month" select="//data:year[last()]/data:month[last()]/@value"/>
                    <xsl:with-param name="day" select="//data:year[last()]/data:month[last()]/data:day[last()]/@value"/>
                    <xsl:with-param name="dow" select="//data:year[last()]/data:month[last()]/data:day[last()]/data:data[1]/data:misc-data/data:dow"/>
                  </xsl:call-template>
                </xsl:when>
              </xsl:choose>
            </fo:block>
            <fo:block text-align="center">
              <fo:external-graphic src="url('sextant.gif')"/>
            </fo:block>
            <fo:block margin="0.5in">
              <fo:block text-align="left" font-family="Arial" font-size="8pt" font-style="italic">
                <xsl:value-of select="$calculated-with"/> <fo:inline font-family="Symbol">D</fo:inline> T: <xsl:value-of select="/data:almanac/@deltaT"/>s
              </fo:block>
              <fo:block text-align="left" font-family="Arial" font-size="8pt" font-style="italic">
                <xsl:value-of select="$compare-with"/> http://aa.usno.navy.mil/data/docs/celnavtable.php, http://maia.usno.navy.mil/
              </fo:block>
              <fo:block margin="1in"/>
              <fo:block text-align="left" font-family="Arial" font-size="8pt" font-style="italic">
                &#169; Oliv Cool Stuff Soft (<xsl:value-of select="$language"/>)
              </fo:block>
            </fo:block>
          </fo:block>
          <fo:block margin="0.1in">
            <xsl:for-each select="//data:day">
              <xsl:call-template name="page-one">
                <xsl:with-param name="day" select="."/>
              </xsl:call-template>
              <xsl:call-template name="page-two">
                <xsl:with-param name="day" select="."/>
              </xsl:call-template>
            </xsl:for-each>
          </fo:block>
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template name="page-one">
    <xsl:param name="day"/>
    <fo:block text-align="center" font-family="Courier" font-size="10pt" break-after="page" margin="0.1in">
      <fo:block text-align="left" font-family="Arial" font-size="10pt" font-style="italic" font-weight="bold" margin="0.1in">
        <xsl:call-template name="date-fmt">
          <xsl:with-param name="lang" select="$language"/>
          <xsl:with-param name="year" select="../../@value"/>
          <xsl:with-param name="month" select="../@value"/>
          <xsl:with-param name="day" select="./@value"/>
          <xsl:with-param name="dow" select="$day/data:data/data:misc-data/data:dow"/>
        </xsl:call-template>
        (page 1)
      </fo:block>
      <fo:block margin="0.15in">
        <!-- Sun and planets -->
        <fo:table> <!-- border="0.5pt solid black"> -->
          <fo:table-column column-width="0.25in"/>   <!-- UT -->
          <fo:table-column column-width="0.60in"/>   <!-- Moon hp -->
          <fo:table-column column-width="0.60in"/>   <!-- Moon sd -->
          <fo:table-column column-width="0.75in"/>   <!-- Sun Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- Sun delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Venus Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- Venus delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Mars Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- Mars delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Jupiter Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- Jupiter delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Saturn Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- Saturn delta -->
          <fo:table-column column-width="0.25in"/>   <!-- UT -->
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$moon"/>&nbsp;<fo:external-graphic src="url('moon.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$sun"/>&nbsp;<fo:external-graphic src="url('sun.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$venus"/>&nbsp;<fo:external-graphic src="url('venus.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$mars"/>&nbsp;<fo:external-graphic src="url('mars.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$jupiter"/>&nbsp;<fo:external-graphic src="url('jupiter.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$saturn"/>&nbsp;<fo:external-graphic src="url('saturn.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$hp"/> (<fo:inline font-family="Symbol">p</fo:inline>)</fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">&frac12;&nbsp;&Oslash;</fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body font-size="8pt">
            <xsl:for-each select="$day/data:data">
              <xsl:call-template name="lunar-dist-one">
                <xsl:with-param name="data" select="."/>
              </xsl:call-template>
            </xsl:for-each>
          </fo:table-body>
        </fo:table>
        <!-- Extra data for sun, venus, mars -->
        <fo:block>
          <fo:block text-align="left" font-family="Arial" font-size="8pt">
            <fo:block font-weight="bold"><xsl:value-of select="$at000uct"/></fo:block>
            <xsl:value-of select="$sun"/>&nbsp;&frac12;&nbsp;&Oslash;&nbsp;<fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX4($day/data:data[./@hour='0']/data:body[./@name='Sun']/@sd-minute)"/>'</fo:inline>,
            <xsl:value-of select="$venus"/>&nbsp;<xsl:value-of select="$hp"/> (<fo:inline font-family="Symbol">p</fo:inline>)&nbsp;<fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX2($day/data:data[./@hour='0']/data:planets/data:body[./@name='Venus']/@hp * 0.0166666)"/>'</fo:inline>,
            <xsl:value-of select="$mars"/>&nbsp;<xsl:value-of select="$hp"/> (<fo:inline font-family="Symbol">p</fo:inline>)&nbsp;<fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX2($day/data:data[./@hour='0']/data:planets/data:body[./@name='Mars']/@hp * 0.0166666)"/>'</fo:inline>
          </fo:block>
        </fo:block>
        <!-- First star block -->
        <fo:block margin="0.15in">
          <fo:table> <!-- border="0.5pt solid black"> -->
            <fo:table-column column-width="0.25in"/>   <!-- UT -->
            <fo:table-column column-width="0.75in"/>   <!-- Elnath Distance -->
            <fo:table-column column-width="0.50in"/>   <!-- delta -->
            <fo:table-column column-width="0.75in"/>   <!-- Aldebaran Distance -->
            <fo:table-column column-width="0.50in"/>   <!-- delta -->
            <fo:table-column column-width="0.75in"/>   <!-- Pollux Distance -->
            <fo:table-column column-width="0.50in"/>   <!-- delta -->
            <fo:table-column column-width="0.75in"/>   <!-- Regulus Distance -->
            <fo:table-column column-width="0.50in"/>   <!-- delta -->
            <fo:table-column column-width="0.75in"/>   <!-- Spica Distance -->
            <fo:table-column column-width="0.50in"/>   <!-- delta -->
            <fo:table-column column-width="0.25in"/>   <!-- UT -->
            <fo:table-header>
              <fo:table-row>
                <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
                <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Elnath</fo:block></fo:table-cell>
                <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Aldebaran</fo:block></fo:table-cell>
                <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Pollux</fo:block></fo:table-cell>
                <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Regulus</fo:block></fo:table-cell>
                <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Spica</fo:block></fo:table-cell>
                <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
              </fo:table-row>
              <fo:table-row>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
                <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              </fo:table-row>
            </fo:table-header>
            <fo:table-body font-size="8pt">
              <xsl:for-each select="$day/data:data">
                <xsl:call-template name="stars-lunar-dist-table-one">
                  <xsl:with-param name="data" select="."/>
                </xsl:call-template>
              </xsl:for-each>
            </fo:table-body>
          </fo:table>
        </fo:block>


      </fo:block>
    </fo:block>
  </xsl:template>

  <xsl:template match="data:data" name="lunar-dist-one"> <!-- For one line of sun and planets -->
    <xsl:param name="data"/>
    <fo:table-row>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>

      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:formatX4($data/data:body[./@name='Moon']/@hp-minute)"/>'</fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:formatX4($data/data:body[./@name='Moon']/@sd-minute)"/>'</fo:block></fo:table-cell>

      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:body[@name='Sun']/@moonDist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:body[@name='Sun']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:body[@name='Sun']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Venus']/@moonDist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:planets/data:body[@name='Venus']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:planets/data:body[@name='Venus']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Mars']/@moonDist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:planets/data:body[@name='Mars']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:planets/data:body[@name='Mars']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Jupiter']/@moonDist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:planets/data:body[@name='Jupiter']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:planets/data:body[@name='Jupiter']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Saturn']/@moonDist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:planets/data:body[@name='Saturn']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:planets/data:body[@name='Saturn']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
    </fo:table-row>
    <xsl:if test="(number(./@hour) mod 6) = 5">
      <fo:table-row>
        <fo:table-cell number-columns-spanned="12"><fo:block>&nbsp;</fo:block></fo:table-cell>
      </fo:table-row>
    </xsl:if>
  </xsl:template>

  <xsl:template name="page-two">
    <xsl:param name="day"/>
    <fo:block text-align="center" font-family="Courier" font-size="10pt" break-after="page" margin="0.1in">
      <fo:block text-align="left" font-family="Arial" font-size="10pt" font-style="italic" font-weight="bold" margin="0.1in">
        <xsl:call-template name="date-fmt">
          <xsl:with-param name="lang" select="$language"/>
          <xsl:with-param name="year" select="../../@value"/>
          <xsl:with-param name="month" select="../@value"/>
          <xsl:with-param name="day" select="./@value"/>
          <xsl:with-param name="dow" select="$day/data:data/data:misc-data/data:dow"/>
        </xsl:call-template>
        (page 2)
      </fo:block>
      <fo:block margin="0.15in">
        <fo:table> <!-- border="0.5pt solid black"> -->
          <fo:table-column column-width="0.25in"/>   <!-- UT -->
          <fo:table-column column-width="0.75in"/>   <!-- Antares Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Altair Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Fomalhaut Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Markab Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Vega Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.25in"/>   <!-- UT -->
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Antares</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Altair</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Fomalhaut</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Markab</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Vega</fo:block></fo:table-cell>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body font-size="8pt">
            <xsl:for-each select="$day/data:data">
              <xsl:call-template name="stars-lunar-dist-table-two">
                <xsl:with-param name="data" select="."/>
              </xsl:call-template>
            </xsl:for-each>
          </fo:table-body>
        </fo:table>
      </fo:block>
      <fo:block margin="0.15in">
        <fo:table> <!-- border="0.5pt solid black"> -->
          <fo:table-column column-width="0.25in"/>   <!-- UT -->
          <fo:table-column column-width="0.75in"/>   <!-- Arcturus Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Betelgeuse Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Canopus Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Capella Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.75in"/>   <!-- Sirius Distance -->
          <fo:table-column column-width="0.50in"/>   <!-- delta -->
          <fo:table-column column-width="0.25in"/>   <!-- UT -->
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Arcturus</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Betelgeuse</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Canopus</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Capella</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">Sirius</fo:block></fo:table-cell>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dist"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline></fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body font-size="8pt">
            <xsl:for-each select="$day/data:data">
              <xsl:call-template name="stars-lunar-dist-table-three">
                <xsl:with-param name="data" select="."/>
              </xsl:call-template>
            </xsl:for-each>
          </fo:table-body>
        </fo:table>
      </fo:block>
    </fo:block>
  </xsl:template>

  <xsl:template match="data:data" name="stars-lunar-dist-table-one"> <!-- For one line -->
    <xsl:param name="data"/>
    <fo:table-row>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Elnath']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Elnath']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Elnath']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Aldebaran']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Aldebaran']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Aldebaran']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Pollux']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Pollux']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Pollux']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Regulus']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Regulus']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Regulus']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Spica']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Spica']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Spica']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
    </fo:table-row>
    <xsl:if test="(number(./@hour) mod 6) = 5">
      <fo:table-row>
        <fo:table-cell number-columns-spanned="12"><fo:block>&nbsp;</fo:block></fo:table-cell>
      </fo:table-row>
    </xsl:if>
  </xsl:template>

  <xsl:template match="data:data" name="stars-lunar-dist-table-two"> <!-- For one line -->
    <xsl:param name="data"/>
    <fo:table-row>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Antares']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Antares']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Antares']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Altair']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Altair']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Altair']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Fomalhaut']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Fomalhaut']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Fomalhaut']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Markab']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Markab']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Markab']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Vega']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Vega']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Vega']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
    </fo:table-row>
    <xsl:if test="(number(./@hour) mod 6) = 5">
      <fo:table-row>
        <fo:table-cell number-columns-spanned="12"><fo:block>&nbsp;</fo:block></fo:table-cell>
      </fo:table-row>
    </xsl:if>
  </xsl:template>

  <xsl:template match="data:data" name="stars-lunar-dist-table-three"> <!-- For one line -->
    <xsl:param name="data"/>
    <fo:table-row>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Arcturus']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Arcturus']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Arcturus']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Betelgeuse']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Betelgeuse']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Betelgeuse']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Canopus']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Canopus']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Canopus']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Capella']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Capella']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Capella']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:stars/data:body[@name='Sirius']/@lunar-dist, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:stars/data:body[@name='Sirius']/@delta-lunar) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:stars/data:body[@name='Sirius']/@delta-lunar * 60)"/><xsl:text>'</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
    </fo:table-row>
    <xsl:if test="(number(./@hour) mod 6) = 5">
      <fo:table-row>
        <fo:table-cell number-columns-spanned="12"><fo:block>&nbsp;</fo:block></fo:table-cell>
      </fo:table-row>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
