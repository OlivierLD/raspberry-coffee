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
  <xsl:param name="with-stars">true</xsl:param>

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
      <fo:page-sequence master-reference="portrait-page">
        <fo:static-content flow-name="footer">
          <!--fo:block text-align="center">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block-->
          <fo:block text-align="center" font-size="8pt">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <fo:block break-after="page"> <!-- background-image="url('bg.jpg')"-->
            <!-- First Page -->
            <fo:block text-align="center" font-family="Book Antiqua" font-size="40pt" font-weight="bold" margin="0.5in">
              <xsl:value-of select="$nautical-almanac"/>
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
                &#169; Oliv Cool Stuff Soft (<xsl:value-of select="$language"/>, <xsl:value-of select="$with-stars"/>)
              </fo:block>
            </fo:block>
          </fo:block>
          <fo:block margin="0.1in">
            <xsl:for-each select="//data:day">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </fo:block>
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template match="data:day" name="sun-moon-planets-stars">
    <fo:block text-align="center" font-family="Courier" font-size="10pt" break-after="page" margin="0.1in">
      <!-- Sun, moon, aries, planets, stars -->
      <fo:block text-align="left" font-family="Arial" font-size="10pt" font-style="italic" font-weight="bold" margin="0.1in">
        <xsl:call-template name="date-fmt">
          <xsl:with-param name="lang" select="$language"/>
          <xsl:with-param name="year" select="../../@value"/>
          <xsl:with-param name="month" select="../@value"/>
          <xsl:with-param name="day" select="./@value"/>
          <xsl:with-param name="dow" select="./data:data/data:misc-data/data:dow"/>
        </xsl:call-template>
        <xsl:if test="$with-stars = 'true'">&nbsp;(page 1)</xsl:if>
      </fo:block>
      <!-- Sun, Moon Aries -->
      <fo:block margin="0.15in">
        <fo:table> <!-- border="0.5pt solid black"> -->
          <fo:table-column column-width="0.30in"/>   <!-- UT -->
          <fo:table-column column-width="0.80in"/>   <!-- Sun GHA -->
          <fo:table-column column-width="0.55in"/>   <!-- Sun delta GHA -->
          <fo:table-column column-width="0.80in"/>   <!-- Sun Dec -->
          <fo:table-column column-width="0.50in"/>   <!-- Sun delta Dec -->
          <fo:table-column column-width="0.80in"/>   <!-- Moon GHA -->
          <fo:table-column column-width="0.55in"/>   <!-- Moon delta GHA -->
          <fo:table-column column-width="0.80in"/>   <!-- Moon Dec -->
          <fo:table-column column-width="0.50in"/>   <!-- Moon delta Dec -->
          <fo:table-column column-width="0.60in"/>   <!-- Moon hp -->
          <fo:table-column column-width="0.80in"/>   <!-- Aries GHA -->
          <fo:table-column column-width="0.30in"/>   <!-- UT -->
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="4" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$sun"/>&nbsp;<fo:external-graphic src="url('sun.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="5" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$moon"/>&nbsp;<fo:external-graphic src="url('moon.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$aries"/>&nbsp;<fo:external-graphic src="url('aries.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$gha"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline> gha</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dec"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic">d</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$gha-moon-planets"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline> gha</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dec"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><fo:inline font-style="italic">d</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$hp"/> (<fo:inline font-family="Symbol">p</fo:inline>)</fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$gha-aries"/></fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body font-size="8pt">
            <xsl:for-each select="./data:data">
              <xsl:call-template name="sun-moon-aries">
                <xsl:with-param name="data" select="."/>
              </xsl:call-template>
            </xsl:for-each>
            <!-- Extra -->
            <fo:table-row>
              <fo:table-cell number-columns-spanned="17"><fo:block>&nbsp;</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-rows-spanned="3" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="10pt"/></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;&frac12;&Oslash;&nbsp;<fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX4(./data:data[./@hour='12']/data:body[./@name='Sun']/@sd-minute)"/>'</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;<xsl:value-of select="$hp"/> (<fo:inline font-family="Symbol">p</fo:inline>) <fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX4(./data:data[./@hour='12']/data:body[./@name='Sun']/@hp-minute)"/>'</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;&frac12;&Oslash;&nbsp;<fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX4(./data:data[./@hour='12']/data:body[./@name='Moon']/@sd-minute)"/>'</fo:inline></fo:block></fo:table-cell>
              <xsl:variable name="phase-value" select="xsl-util:formatI2(number(./data:data[./@hour='12']/data:body[./@name='Moon']/@age-in-days + 1))"/>
              <!--fo:table-cell number-columns-spanned="3" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Courier" font-size="8pt"><xsl:value-of select="./data:data[./@hour='12']/data:body[./@name='Moon']/@illum"/>&nbsp;<xsl:value-of select="$phase-img"/></fo:block></fo:table-cell-->
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black">
                <fo:block text-align="center" font-family="Courier" font-size="8pt">
                 <xsl:value-of select="./data:data[./@hour='12']/data:body[./@name='Moon']/@illum"/>
                </fo:block>
              </fo:table-cell>
              <fo:table-cell number-columns-spanned="1" number-rows-spanned="2" padding="medium" border="0.5pt solid black">
                <fo:block text-align="center" vertical-align="middle" font-family="Courier" font-size="8pt">
                 <xsl:choose>
                   <xsl:when test="$phase-value = '01'">
                     <fo:external-graphic src="url('phase01.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '02'">
                     <fo:external-graphic src="url('phase02.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '03'">
                     <fo:external-graphic src="url('phase03.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '04'">
                     <fo:external-graphic src="url('phase04.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '05'">
                     <fo:external-graphic src="url('phase05.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '06'">
                     <fo:external-graphic src="url('phase06.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '07'">
                     <fo:external-graphic src="url('phase07.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '08'">
                     <fo:external-graphic src="url('phase08.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '09'">
                     <fo:external-graphic src="url('phase09.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '10'">
                     <fo:external-graphic src="url('phase10.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '11'">
                     <fo:external-graphic src="url('phase11.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '12'">
                     <fo:external-graphic src="url('phase12.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '13'">
                     <fo:external-graphic src="url('phase13.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '14'">
                     <fo:external-graphic src="url('phase14.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '15'">
                     <fo:external-graphic src="url('phase15.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '16'">
                     <fo:external-graphic src="url('phase16.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '17'">
                     <fo:external-graphic src="url('phase17.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '18'">
                     <fo:external-graphic src="url('phase18.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '19'">
                     <fo:external-graphic src="url('phase19.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '20'">
                     <fo:external-graphic src="url('phase20.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '21'">
                     <fo:external-graphic src="url('phase21.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '22'">
                     <fo:external-graphic src="url('phase22.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '23'">
                     <fo:external-graphic src="url('phase23.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '24'">
                     <fo:external-graphic src="url('phase24.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '25'">
                     <fo:external-graphic src="url('phase25.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '26'">
                     <fo:external-graphic src="url('phase26.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '27'">
                     <fo:external-graphic src="url('phase27.png')" vertical-align="middle"/>
                   </xsl:when>
                   <xsl:when test="$phase-value = '28'">
                     <fo:external-graphic src="url('phase28.png')" vertical-align="middle"/>
                   </xsl:when>
                 </xsl:choose>
                </fo:block>
              </fo:table-cell>
              <fo:table-cell number-rows-spanned="3" number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="10pt"/></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <!--fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="10pt"/></fo:table-cell-->
              <xsl:variable name="delta-eot"><xsl:value-of select="number(./data:data[@hour = '12']/data:body[@name='Sun']/@delta-EoT-in-minutes * 60)"/></xsl:variable>
              <fo:table-cell number-columns-spanned="4" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;<xsl:value-of select="$eot12"/><fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="./data:data[./@hour='12']/data:body[./@name='Sun']/@eot"/> (<fo:inline font-family="Symbol">d</fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX2($delta-eot)"/> s/h)</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="4" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;<xsl:value-of select="$phase"/><fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX2(./data:data[./@hour='12']/data:body[./@name='Moon']/@phase-in-degrees)"/>°</fo:inline></fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <!--fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="10pt"/></fo:table-cell-->
              <fo:table-cell number-columns-spanned="4" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;<xsl:value-of select="$mp"/><fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="geom-util:formatHMS(./data:data[./@hour='12']/data:body[./@name='Sun']/@t-pass-in-hours)"/></fo:inline></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="5" padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Arial" font-size="8pt">&nbsp;<xsl:value-of select="$age"/><fo:inline font-family="Courier" font-size="8pt"><xsl:value-of select="xsl-util:formatX1(./data:data[@hour = '12']/data:body[@name='Moon']/@age-in-days)"/> day(s)</fo:inline></fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
      <!-- Planets -->
      <fo:block margin="0.15in">
        <fo:table> <!--  border="0.5pt solid black"> -->
          <fo:table-column column-width="0.3in"/>
          <fo:table-column column-width="0.8in"/>
          <fo:table-column column-width="0.8in"/>
          <fo:table-column column-width="0.8in"/>
          <fo:table-column column-width="0.8in"/>
          <fo:table-column column-width="0.8in"/>
          <fo:table-column column-width="0.8in"/>
          <fo:table-column column-width="0.8in"/>
          <fo:table-column column-width="0.8in"/>
          <fo:table-column column-width="0.3in"/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$venus"/>&nbsp;<fo:external-graphic src="url('venus.png')"/>&nbsp;<fo:inline  font-size="6pt">(<fo:inline font-family="Symbol">p</fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX2(number(./data:data[@hour='1']/data:planets/data:body[@name='Venus']/@hp * 0.016666666))"/>')</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$mars"/>&nbsp;<fo:external-graphic src="url('mars.png')"/>&nbsp;<fo:inline  font-size="6pt">(<fo:inline font-family="Symbol">p</fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX2(number(./data:data[@hour='1']/data:planets/data:body[@name='Mars']/@hp * 0.016666666))"/>')</fo:inline></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$jupiter"/>&nbsp;<fo:external-graphic src="url('jupiter.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$saturn"/>&nbsp;<fo:external-graphic src="url('saturn.png')"/></fo:block></fo:table-cell>
              <fo:table-cell number-rows-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt">UT</fo:block></fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$gha-moon-planets"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dec"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$gha-moon-planets"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dec"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$gha-moon-planets"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dec"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$gha-moon-planets"/></fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dec"/></fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body font-size="8pt">
            <xsl:for-each select="./data:data">
              <xsl:call-template name="planets">
                <xsl:with-param name="data" select="."/>
              </xsl:call-template>
            </xsl:for-each>
            <!-- Delta GHA and D -->
            <fo:table-row>
              <!-- One blank cell -->
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"></fo:block></fo:table-cell>
              <!-- Venus -->
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX4(./data:data[@hour = '1']/data:planets/data:body[@name='Venus']/@varGHA)"/>&#176;</fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><fo:inline font-style="italic">d</fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX4(number(./data:data[@hour = '1']/data:planets/data:body[@name='Venus']/@varD * 60))"/>'</fo:block></fo:table-cell>
              <!-- Mars -->
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX4(./data:data[@hour = '1']/data:planets/data:body[@name='Mars']/@varGHA)"/>&#176;</fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><fo:inline font-style="italic">d</fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX4(number(./data:data[@hour = '1']/data:planets/data:body[@name='Mars']/@varD * 60))"/>'</fo:block></fo:table-cell>
              <!-- Jupiter -->
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX4(./data:data[@hour = '1']/data:planets/data:body[@name='Jupiter']/@varGHA)"/>&#176;</fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><fo:inline font-style="italic">d</fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX4(number(./data:data[@hour = '1']/data:planets/data:body[@name='Jupiter']/@varD * 60))"/>'</fo:block></fo:table-cell>
              <!-- Saturn -->
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><fo:inline font-style="italic"><fo:inline font-family="Symbol">d</fo:inline></fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX4(./data:data[@hour = '1']/data:planets/data:body[@name='Saturn']/@varGHA)"/>&#176;</fo:block></fo:table-cell>
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center"><fo:inline font-style="italic">d</fo:inline>&nbsp;<xsl:value-of select="xsl-util:formatX4(number(./data:data[@hour = '1']/data:planets/data:body[@name='Saturn']/@varD * 60))"/>'</fo:block></fo:table-cell>
              <!-- One blank cell -->
              <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"></fo:block></fo:table-cell>
            </fo:table-row>
          </fo:table-body>
          <!--fo:table-footer>
            That's it
          </fo:table-footer-->
        </fo:table>
      </fo:block>
    </fo:block>
    <!-- Stars -->
    <xsl:if test="$with-stars = 'true'">
      <fo:block text-align="center" font-family="Courier" font-size="10pt" break-after="page" margin="0.5in">
        <fo:block text-align="left" font-family="Arial" font-size="10pt" font-style="italic" font-weight="bold">
          <xsl:call-template name="date-fmt">
            <xsl:with-param name="lang" select="$language"/>
            <xsl:with-param name="year" select="../../@value"/>
            <xsl:with-param name="month" select="../@value"/>
            <xsl:with-param name="day" select="./@value"/>
            <xsl:with-param name="dow" select="./data:data/data:misc-data/data:dow"/>
          </xsl:call-template>
          (page 2)
        </fo:block>
        <fo:block text-align="left" font-family="Arial" font-size="7pt">
          <xsl:value-of select="$stars-banner"/>
        </fo:block>
        <fo:table>
          <fo:table-column column-width="3.80in"/>
          <fo:table-column column-width="3.80in"/>
          <fo:table-body>
            <fo:table-row>
              <xsl:for-each select="./data:data[@hour=0]/data:stars/data:body">
                <xsl:if test="((position() - 1) mod $star-per-col) = 0">
                  <!-- Open fo:table-cell, create table -->
                  <xsl:text disable-output-escaping="yes">&lt;fo:table-cell&gt; &lt;fo:block margin="0.1pt"&gt; &lt;fo:table&gt;</xsl:text>
                  <fo:table-column column-width="1.1in"/>
                  <fo:table-column column-width="1.15in"/>
                  <fo:table-column column-width="1.3in"/>
                  <xsl:text disable-output-escaping="yes">&lt;fo:table-body&gt;</xsl:text>
                  <fo:table-row>
                    <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$name"/></fo:block></fo:table-cell>
                    <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$sha"/></fo:block></fo:table-cell>
                    <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="10pt"><xsl:value-of select="$dec"/></fo:block></fo:table-cell>
                  </fo:table-row>
                </xsl:if>
                <fo:table-row>
                  <fo:table-cell padding="medium" border="0.5pt solid black">
                    <xsl:if test="./@Dec &lt; 0">
                      <xsl:attribute name="background-color">silver</xsl:attribute>
                    </xsl:if>
                    <fo:block text-align="left" font-family="Arial" font-size="10pt"><xsl:value-of select="./@name"/></fo:block>
                  </fo:table-cell>
                  <xsl:variable name="sha-star"><xsl:value-of select="./@SHA"/></xsl:variable>
                  <xsl:variable name="dec-star"><xsl:value-of select="./@Dec"/></xsl:variable>
                  <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right" font-family="Courier New" font-size="8pt"><xsl:value-of select="xsl-util:decToSex($sha-star, $GEOMUTIL.SWING, $GEOMUTIL.NONE)" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                  <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right" font-family="Courier New" font-size="8pt"><xsl:value-of select="xsl-util:decToSex($dec-star, $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                </fo:table-row>
                <xsl:if test="(position() mod $star-per-col) = 0 or position() = last()">
                  <!-- Close table, close fo:table-cell -->
                  <xsl:text disable-output-escaping="yes">&lt;/fo:table-body&gt;&lt;/fo:table&gt; &lt;/fo:block&gt; &lt;/fo:table-cell&gt;</xsl:text>
                </xsl:if>
              </xsl:for-each>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
        <fo:block text-align="right" font-family="Arial" font-size="7pt"><xsl:text disable-output-escaping="yes">&nbsp;</xsl:text></fo:block>
        <fo:table>
          <fo:table-column column-width="3.80in"/>
          <fo:table-column column-width="3.80in"/>
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell> <!-- Left -->
                <fo:block text-align="left" font-family="Arial" font-size="8pt" font-style="plain">
                  <!-- Day data -->
                  <fo:table margin="0.15in">
                    <fo:table-column column-width="1.5in"/>
                    <fo:table-column column-width="1.5in"/>
                    <fo:table-body>
                      <fo:table-row>
                        <fo:table-cell number-columns-spanned="2" padding="medium" text-align="center" border="0.5pt solid black"><fo:block font-weight="bold"><xsl:value-of select="$at000uct"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell text-align="left"><fo:block><xsl:value-of select="$moe"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block font-family="Courier" font-size="10pt"><xsl:value-of select="geom-util:formatDMS(./data:data[1]/data:misc-data/data:mean-obl-of-ecl)"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell text-align="left"><fo:block><xsl:value-of select="$toe"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block font-family="Courier" font-size="10pt"><xsl:value-of select="geom-util:formatDMS(./data:data[1]/data:misc-data/data:true-obl-of-ecl)"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell text-align="left"><fo:block>Delta <fo:inline font-family="Symbol">y</fo:inline></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block font-family="Courier" font-size="10pt"><xsl:value-of select="xsl-util:formatX3(./data:data[1]/data:misc-data/data:dpsi)"/>&#34;</fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell text-align="left"><fo:block>Delta <fo:inline font-family="Symbol">e</fo:inline></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block font-family="Courier" font-size="10pt"><xsl:value-of select="xsl-util:formatX3(./data:data[1]/data:misc-data/data:deps)"/>&#34;</fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell text-align="left"><fo:block><xsl:value-of select="$o0"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block font-family="Courier" font-size="10pt"><xsl:value-of select="./data:data[1]/data:misc-data/data:obliq"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell text-align="left"><fo:block><xsl:value-of select="$to0"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block font-family="Courier" font-size="10pt"><xsl:value-of select="./data:data[1]/data:misc-data/data:true-obliq"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell text-align="left"><fo:block><xsl:value-of select="$jd"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block font-family="Courier" font-size="10pt"><xsl:value-of select="./data:data[1]/data:misc-data/data:julian-day"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell text-align="left"><fo:block><xsl:value-of select="$jde"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block font-family="Courier" font-size="10pt"><xsl:value-of select="./data:data[1]/data:misc-data/data:julian-ephem-day"/></fo:block></fo:table-cell>
                      </fo:table-row>
                    </fo:table-body>
                  </fo:table>
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="medium"> <!-- Right, rise and set -->
                <fo:table>
                  <fo:table-column column-width="0.40in"/>
                  <fo:table-column column-width="0.50in"/>
                  <fo:table-column column-width="0.35in"/>
                  <fo:table-column column-width="0.50in"/>
                  <fo:table-column column-width="0.35in"/>
                  <fo:table-column column-width="0.50in"/>
                  <fo:table-column column-width="0.50in"/>
                  <fo:table-column column-width="0.40in"/>
                  <fo:table-body>
                    <fo:table-row>
                      <fo:table-cell number-columns-spanned="8" padding="medium"><fo:block text-align="left" font-family="Arial" font-size="7pt"><xsl:value-of select="$set-rise-banner"/></fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt">&nbsp;</fo:block></fo:table-cell>
                      <fo:table-cell number-columns-spanned="4" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$sun"/></fo:block></fo:table-cell>
                      <fo:table-cell number-columns-spanned="2" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$moon"/></fo:block></fo:table-cell>
                      <fo:table-cell number-columns-spanned="1" padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt">&nbsp;</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt">Lat.</fo:block></fo:table-cell>
                      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$rise"/></fo:block></fo:table-cell>
                      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$z"/><fo:inline font-size="6pt"><xsl:value-of select="$rise"/></fo:inline></fo:block></fo:table-cell>
                      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$set"/></fo:block></fo:table-cell>
                      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$z"/><fo:inline font-size="6pt"><xsl:value-of select="$set"/></fo:inline></fo:block></fo:table-cell>
                      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$rise"/></fo:block></fo:table-cell>
                      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt"><xsl:value-of select="$set"/></fo:block></fo:table-cell>
                      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Arial" font-size="8pt">Lat.</fo:block></fo:table-cell>
                    </fo:table-row>
                    <xsl:for-each select="./data:data[@hour=12]/data:rise-set/data:latitude">
                      <xsl:sort select="./@val" data-type="number" order="descending"/>
                      <fo:table-row>
                        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="left" font-family="Courier New" font-size="8pt" font-weight="bold"><xsl:value-of select="geom-util:signedDegrees(./@val)" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Courier New" font-size="8pt"><xsl:value-of select="./data:sun/data:rise" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Courier New" font-size="8pt"><xsl:value-of select="./data:sun/data:rise/@z" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Courier New" font-size="8pt"><xsl:value-of select="./data:sun/data:set" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Courier New" font-size="8pt"><xsl:value-of select="./data:sun/data:set/@z" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Courier New" font-size="8pt"><xsl:value-of select="./data:moon/data:rise" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-family="Courier New" font-size="8pt"><xsl:value-of select="./data:moon/data:set" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                        <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right" font-family="Courier New" font-size="8pt" font-weight="bold"><xsl:value-of select="geom-util:signedDegrees(./@val)" disable-output-escaping="yes"/></fo:block></fo:table-cell>
                      </fo:table-row>
                    </xsl:for-each>
                  </fo:table-body>
                </fo:table>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
      </fo:block>
    </xsl:if>
  </xsl:template>

  <xsl:template match="data:data" name="sun-moon-aries">
    <xsl:param name="data"/>
    <fo:table-row>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:body[@name='Sun']/@GHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:body[@name='Sun']/@varGHA) > 0">
            <!--xsl:value-of select="xsl-util:decToSex($data/data:body[@name='Sun']/@varGHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/>
            <xsl:text> </xsl:text-->
            <xsl:value-of select="xsl-util:formatX4($data/data:body[@name='Sun']/@varGHA)"/><xsl:text>&#176;</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:body[@name='Sun']/@Dec, $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:body[@name='Sun']/@varD) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:body[@name='Sun']/@varD * 60)"/>'
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:body[@name='Moon']/@GHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:body[@name='Moon']/@varGHA) > 0">
            <!--xsl:value-of select="xsl-util:decToSex($data/data:body[@name='Moon']/@varGHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/-->
            <xsl:value-of select="xsl-util:formatX4($data/data:body[@name='Moon']/@varGHA)"/><xsl:text>&#176;</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:body[@name='Moon']/@Dec, $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black">
        <fo:block text-align="right" font-size="6pt">
          <xsl:if test="string-length($data/data:body[@name='Moon']/@varD) > 0">
            <xsl:value-of select="xsl-util:formatX4($data/data:body[@name='Moon']/@varD * 60)"/>'
          </xsl:if>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:formatX4($data/data:body[@name='Moon']/@hp-minute)"/>'</fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:body[@name='Aries']/@GHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
    </fo:table-row>
    <xsl:if test="(number(./@hour) mod 6) = 5">
      <fo:table-row>
        <fo:table-cell number-columns-spanned="17"><fo:block>&nbsp;</fo:block></fo:table-cell>
      </fo:table-row>
    </xsl:if>
  </xsl:template>

  <xsl:template match="data:data" name="planets">
    <xsl:param name="data"/>
    <fo:table-row>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Venus']/@GHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Venus']/@Dec, $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Mars']/@GHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Mars']/@Dec, $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Jupiter']/@GHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Jupiter']/@Dec, $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Saturn']/@GHA, $GEOMUTIL.SWING, $GEOMUTIL.NONE)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="right"><xsl:value-of select="xsl-util:decToSex($data/data:planets/data:body[@name='Saturn']/@Dec, $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)"/></fo:block></fo:table-cell>
      <fo:table-cell padding="medium" border="0.5pt solid black"><fo:block text-align="center" font-weight="bold"><xsl:value-of select="xsl-util:formatI2($data/@hour)"/></fo:block></fo:table-cell>
    </fo:table-row>
    <xsl:if test="(number(./@hour) mod 6) = 5"> <!-- Blank line -->
      <fo:table-row>
        <fo:table-cell number-columns-spanned="17"><fo:block>&nbsp;</fo:block></fo:table-cell>
      </fo:table-row>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
