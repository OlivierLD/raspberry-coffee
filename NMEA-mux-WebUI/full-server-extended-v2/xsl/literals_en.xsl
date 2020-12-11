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
                xmlns:xsl-util="http://www.oracle.com/XSL/Transform/java/calc.calculation.nauticalalmanac.xsl.XSLUtil"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                exclude-result-prefixes="xsl-util"
                version="1.0">

  <xsl:variable name="GEOMUTIL.HTML"   select="0"/>
  <xsl:variable name="GEOMUTIL.SHELL"  select="1"/>
  <xsl:variable name="GEOMUTIL.SWING"  select="2"/>
  <xsl:variable name="GEOMUTIL.NO_DEG" select="3"/>

  <xsl:variable name="GEOMUTIL.NONE" select="0"/>
  <xsl:variable name="GEOMUTIL.NS"   select="1"/>
  <xsl:variable name="GEOMUTIL.EW"   select="2"/>

  <xsl:variable name="GEOMUTIL.LEADING_SIGN"  select="0"/>
  <xsl:variable name="GEOMUTIL.TRAILING_SIGN" select="1"/>

  <xsl:variable name="star-per-col" select="29"/>

  <xsl:variable name="oliv-soft">by Oliv Soft</xsl:variable>
  <xsl:variable name="calculated-with">Calculated with&nbsp;</xsl:variable>
  <xsl:variable name="compare-with">Compare with</xsl:variable>

  <xsl:variable name="SHA-warning">SHA is given for Sun, Moon and Planets. This is redundant, but convenient to plot those bodies in the Star Finder 2102-D.</xsl:variable>

  <xsl:variable name="perpetual-almanac">Long Term Almanac</xsl:variable>
  <xsl:variable name="nautical-almanac">Nautical Almanac</xsl:variable>
  <xsl:variable name="lunar-distances">Lunar Distances</xsl:variable>
  <xsl:variable name="for">for </xsl:variable>

  <xsl:variable name="from">from </xsl:variable>
  <xsl:variable name="to"> to </xsl:variable>

  <xsl:variable name="sun">Sun</xsl:variable>
  <xsl:variable name="moon">Moon</xsl:variable>
  <xsl:variable name="aries">Aries</xsl:variable>

  <xsl:variable name="hp">hp</xsl:variable>
  <xsl:variable name="eot12">EoT at 12:00 UTC : </xsl:variable>
  <xsl:variable name="mp">Meridian Pass. Time : </xsl:variable>
  <xsl:variable name="age">Age : </xsl:variable>
  <xsl:variable name="phase">Phase at 12:00 UTC : </xsl:variable>

  <xsl:variable name="venus">Venus</xsl:variable>
  <xsl:variable name="mars">Mars</xsl:variable>
  <xsl:variable name="jupiter">Jupiter</xsl:variable>
  <xsl:variable name="saturn">Saturn</xsl:variable>

  <xsl:variable name="gha">GHA</xsl:variable>
  <xsl:variable name="gha-moon-planets">GHA</xsl:variable>
  <xsl:variable name="gha-aries">GHA</xsl:variable>
  <xsl:variable name="dec">Dec</xsl:variable>
  <xsl:variable name="z">Z</xsl:variable>

  <xsl:variable name="dist">Dist</xsl:variable>

  <xsl:variable name="rise">Rise</xsl:variable>
  <xsl:variable name="set">Set</xsl:variable>

  <xsl:variable name="set-rise-banner">Calculated at 12:00 UCT at Greenwich</xsl:variable>

  <xsl:variable name="at000uct">Calculated at 00:00:00 U.T.</xsl:variable>
  <xsl:variable name="moe">Mean Obliquity of Ecliptic</xsl:variable>
  <xsl:variable name="toe">True Obliquity of Ecliptic</xsl:variable>
  <xsl:variable name="o0">Obliquity</xsl:variable>
  <xsl:variable name="to0">True Obliquity</xsl:variable>
  <xsl:variable name="jd">Julian Date</xsl:variable>
  <xsl:variable name="jde">Julian Ephemeris Date</xsl:variable>

  <xsl:variable name="stars-banner">Stars at 0000 U.T. (GHA(Star) = SHA(Star) + GHA(Aries))</xsl:variable>

  <xsl:variable name="name">Name</xsl:variable>
  <xsl:variable name="sha">SHA</xsl:variable>

  <xsl:template name="date-fmt">
    <xsl:param name="year"/>
    <xsl:param name="month"/>
    <xsl:param name="day"/>
    <xsl:param name="dow"/>
    <xsl:choose>
      <xsl:when test="$dow = 'monday'">Monday, </xsl:when>
      <xsl:when test="$dow = 'tuesday'">Tuesday, </xsl:when>
      <xsl:when test="$dow = 'wednesday'">Wednesday, </xsl:when>
      <xsl:when test="$dow = 'thursday'">Thursday, </xsl:when>
      <xsl:when test="$dow = 'friday'">Friday, </xsl:when>
      <xsl:when test="$dow = 'saturday'">Saturday, </xsl:when>
      <xsl:when test="$dow = 'sunday'">Sunday, </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$month = 1">January</xsl:when>
      <xsl:when test="$month = 2">February</xsl:when>
      <xsl:when test="$month = 3">March</xsl:when>
      <xsl:when test="$month = 4">April</xsl:when>
      <xsl:when test="$month = 5">May</xsl:when>
      <xsl:when test="$month = 6">June</xsl:when>
      <xsl:when test="$month = 7">July</xsl:when>
      <xsl:when test="$month = 8">August</xsl:when>
      <xsl:when test="$month = 9">September</xsl:when>
      <xsl:when test="$month = 10">October</xsl:when>
      <xsl:when test="$month = 11">November</xsl:when>
      <xsl:when test="$month = 12">December</xsl:when>
    </xsl:choose>
    <!--xsl:text> the </xsl:text-->
    <xsl:text> </xsl:text>
    <xsl:value-of select="$day"/>
    <fo:inline baseline-shift="super" font-size="75%"> <!-- font-size="smaller" -->
      <xsl:choose>
        <xsl:when test="$day = 1 or $day = 21 or $day = 31">st</xsl:when>
        <xsl:when test="$day = 2 or $day = 22">nd</xsl:when>
        <xsl:when test="$day = 3 or $day = 23">rd</xsl:when>
        <xsl:otherwise>th</xsl:otherwise>
      </xsl:choose>
    </fo:inline>
    <xsl:text>, </xsl:text>
    <xsl:value-of select="$year"/>
  </xsl:template>

  <xsl:template name="front-page-date">
    <xsl:param name="nb-month"/>
    <xsl:param name="y"/>
    <xsl:param name="nb-day"/>
    <xsl:param name="m"/>
    <xsl:param name="dow"/>
    <xsl:param name="d"/>

    <xsl:choose>
      <!-- One year -->
      <xsl:when test="$nb-month > 1">
        <xsl:value-of select="$y"/>
      </xsl:when>
      <!-- One month -->
      <xsl:when test="$nb-day > 1">
        <xsl:choose>
          <xsl:when test="$m = 1">January </xsl:when>
          <xsl:when test="$m = 2">February </xsl:when>
          <xsl:when test="$m = 3">March </xsl:when>
          <xsl:when test="$m = 4">April </xsl:when>
          <xsl:when test="$m = 5">May </xsl:when>
          <xsl:when test="$m = 6">June </xsl:when>
          <xsl:when test="$m = 7">July </xsl:when>
          <xsl:when test="$m = 8">August </xsl:when>
          <xsl:when test="$m = 9">September </xsl:when>
          <xsl:when test="$m = 10">October </xsl:when>
          <xsl:when test="$m = 11">November </xsl:when>
          <xsl:when test="$m = 12">December </xsl:when>
        </xsl:choose>
        <xsl:value-of select="$y"/>
      </xsl:when>
      <!-- One day -->
      <xsl:otherwise>
        <!-- Day of Week -->
        <xsl:value-of select="xsl-util:initCap($dow)"/>
        <xsl:text>, </xsl:text>
        <xsl:choose>
          <xsl:when test="$m = 1">January </xsl:when>
          <xsl:when test="$m = 2">February </xsl:when>
          <xsl:when test="$m = 3">March </xsl:when>
          <xsl:when test="$m = 4">April </xsl:when>
          <xsl:when test="$m = 5">May </xsl:when>
          <xsl:when test="$m = 6">June </xsl:when>
          <xsl:when test="$m = 7">July </xsl:when>
          <xsl:when test="$m = 8">August </xsl:when>
          <xsl:when test="$m = 9">September </xsl:when>
          <xsl:when test="$m = 10">October </xsl:when>
          <xsl:when test="$m = 11">November </xsl:when>
          <xsl:when test="$m = 12">December </xsl:when>
        </xsl:choose>
		<!--xsl:text> the </xsl:text-->
		<xsl:text> </xsl:text>
        <xsl:value-of select="$d"/>
        <!-- in English... -->
        <fo:inline baseline-shift="super" font-size="75%"> <!-- font-size="smaller" -->
          <xsl:choose>
            <xsl:when test="$d mod 10 = 1 and not($d = 11)">st</xsl:when>
            <xsl:when test="$d mod 10 = 2 and not($d = 12)">nd</xsl:when>
            <xsl:when test="$d mod 10 = 3 and not($d = 13)">rd</xsl:when>
            <xsl:otherwise>th</xsl:otherwise>
          </xsl:choose>
        </fo:inline>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="$y"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
