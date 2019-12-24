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
    <!ENTITY apos    "&#39;">
    <!ENTITY deg     "&#176;">
		<!ENTITY dot     "&#729;">
		<!ENTITY middot  "&#183;">
		<!ENTITY bull    "&#8226;">
    ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fo="http://www.w3.org/1999/XSL/Format"
        xmlns:fox="http://xml.apache.org/fop/extensions" version="2.0">

  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="landscape-page" page-width="8.5in"
                     page-height="11in">
          <!-- Portrait, Letter -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="20mm"/>
        </fo:simple-page-master>
        <fo:simple-page-master master-name="landscape-page" page-height="8.5in"
                     page-width="11in">
          <!-- Portrait -->
          <fo:region-body margin="0in" background-color="black" color="yellow"/>
          <fo:region-after region-name="footer" extent="20mm" background-color="black" color="yellow"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="landscape-page">
        <fo:static-content flow-name="footer">
          <!--fo:block text-align="center">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block-->
          <!--fo:block text-align="center" font-size="8pt">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block-->
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body" color="white">

          <!-- Put a title here, assume the xml data is a one year document -->
	        <!--fo:block text-align="center" font-family="Helvetica Neue" font-size="24pt" font-weight="bold" margin="0.25in" background-color="black" color="yellow"-->
	        <fo:block text-align="center" font-family="Helvetica Neue" font-size="18pt" font-weight="bold" margin="0.25in" background-color="black" color="yellow">
		        <xsl:value-of select="concat('Moon Calendar, ', /tide/period[1]/@year)"/>
	        </fo:block>

          <fo:block margin="0.1in" background-color="black" color="white">

            <fo:table>
	            <!-- border="0.5pt solid black"> -->
	            <fo:table-column column-width="0.40in"/> <!-- Month column -->

	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-column column-width="0.33in"/>
	            <fo:table-body font-size="7pt">

		            <!-- Day nums on top -->
		            <fo:table-row>
			            <fo:table-cell></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">1</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">2</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">3</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">4</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">5</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">6</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">7</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">8</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">9</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">10</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">11</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">12</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">13</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">14</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">15</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">16</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">17</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">18</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">19</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">20</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">21</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">22</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">23</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">24</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">25</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">26</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">27</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">28</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">29</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">30</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">31</fo:block></fo:table-cell>
		            </fo:table-row>

		            <!-- Moon data, month per month -->
		            <xsl:for-each select="//period">
		              <fo:table-row>
			              <fo:table-cell padding="medium"
			                             vertical-align="middle" text-align="center">
				              <fo:block text-align="center" font-family="Book Antiqua" font-size="10pt" font-weight="bold">
					              <xsl:choose>
						              <xsl:when test="position() = 1"><xsl:value-of select="'Jan'"/></xsl:when>
						              <xsl:when test="position() = 2"><xsl:value-of select="'Feb'"/></xsl:when>
						              <xsl:when test="position() = 3"><xsl:value-of select="'Mar'"/></xsl:when>
						              <xsl:when test="position() = 4"><xsl:value-of select="'Apr'"/></xsl:when>
						              <xsl:when test="position() = 5"><xsl:value-of select="'May'"/></xsl:when>
						              <xsl:when test="position() = 6"><xsl:value-of select="'Jun'"/></xsl:when>
						              <xsl:when test="position() = 7"><xsl:value-of select="'Jul'"/></xsl:when>
						              <xsl:when test="position() = 8"><xsl:value-of select="'Aug'"/></xsl:when>
						              <xsl:when test="position() = 9"><xsl:value-of select="'Sep'"/></xsl:when>
						              <xsl:when test="position() = 10"><xsl:value-of select="'Oct'"/></xsl:when>
						              <xsl:when test="position() = 11"><xsl:value-of select="'Nov'"/></xsl:when>
						              <xsl:when test="position() = 12"><xsl:value-of select="'Dec'"/></xsl:when>
						              <xsl:otherwise><xsl:value-of select="position()"/></xsl:otherwise>
					              </xsl:choose>
				              </fo:block>
			              </fo:table-cell>
		                <xsl:apply-templates select="."/>
		              </fo:table-row>
		            </xsl:for-each>

		            <!-- Day nums at the bottom -->
		            <fo:table-row>
			            <fo:table-cell></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">1</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">2</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">3</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">4</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">5</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">6</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">7</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">8</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">9</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">10</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">11</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">12</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">13</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">14</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">15</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">16</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">17</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">18</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">19</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">20</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">21</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">22</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">23</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">24</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">25</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">26</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">27</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">28</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">29</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">30</fo:block></fo:table-cell>
			            <fo:table-cell padding="medium" vertical-align="middle" text-align="center"><fo:block text-align="center" font-family="Book Antiqua" font-size="12pt" font-weight="bold">31</fo:block></fo:table-cell>
		            </fo:table-row>

	            </fo:table-body>
            </fo:table>
          </fo:block>
          <!--fo:block id="last-page"/-->
          <fo:block text-align="left" font-family="Book Antiqua" font-size="8pt" font-weight="normal" font-style="italic" margin="0.1in" background-color="black" color="yellow">by OlivSoft</fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template match="period" name="one-month-moon">
    <xsl:for-each select="./date">
      <fo:table-cell padding="medium"
                     vertical-align="middle"
                     text-align="center">
        <fo:block>
          <!--xsl:value-of select="./@val"/-->
          <!--xsl:apply-templates select="."/-->
          <xsl:choose>
            <xsl:when test="./@moon-phase = '01'">
              <fo:external-graphic src="url('phase01.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '02'">
              <fo:external-graphic src="url('phase02.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '03'">
              <fo:external-graphic src="url('phase03.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '04'">
              <fo:external-graphic src="url('phase04.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '05'">
              <fo:external-graphic src="url('phase05.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '06'">
              <fo:external-graphic src="url('phase06.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '07'">
              <fo:external-graphic src="url('phase07.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> <!-- FQ --></fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '08'">
              <fo:external-graphic src="url('phase08.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '09'">
              <fo:external-graphic src="url('phase09.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '10'">
              <fo:external-graphic src="url('phase10.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '11'">
              <fo:external-graphic src="url('phase11.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '12'">
              <fo:external-graphic src="url('phase12.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '13'">
              <fo:external-graphic src="url('phase13.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '14'"> <!-- Full moon -->
              <fo:external-graphic src="url('phase14.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white">&times; </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '15'">
              <fo:external-graphic src="url('phase15.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '16'">
              <fo:external-graphic src="url('phase16.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '17'">
              <fo:external-graphic src="url('phase17.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '18'">
              <fo:external-graphic src="url('phase18.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '19'">
              <fo:external-graphic src="url('phase19.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '20'">
              <fo:external-graphic src="url('phase20.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '21'">
              <fo:external-graphic src="url('phase21.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> <!-- LQ --></fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '22'">
              <fo:external-graphic src="url('phase22.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '23'">
              <fo:external-graphic src="url('phase23.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '24'">
              <fo:external-graphic src="url('phase24.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '25'">
              <fo:external-graphic src="url('phase25.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '26'">
              <fo:external-graphic src="url('phase26.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '27'">
              <fo:external-graphic src="url('phase27.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> </fo:block>
            </xsl:when>
            <xsl:when test="./@moon-phase = '28'">
              <fo:external-graphic src="url('phase28.gif')"
                                   vertical-align="middle" text-align="center"/>
	            <fo:block text-align="right" font-size="4pt" font-weight="normal" background-color="black" color="white"> <!-- New --></fo:block>
            </xsl:when>
          </xsl:choose>
        </fo:block>
	      <fo:block text-align="center">
		      <!--xsl:value-of select="position()"/-->
		      <xsl:value-of select="substring(./@val, 1, 3)"/> <!-- Day of Week -->
	      </fo:block>
      </fo:table-cell>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
