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

  <xsl:variable name="oliv-soft">par Oliv Soft</xsl:variable>
  <xsl:variable name="calculated-with">Calcul&eacute; avec&nbsp;</xsl:variable>
  <xsl:variable name="compare-with">&Agrave; comparer avec</xsl:variable>
  
  <xsl:variable name="SHA-warning">AHso est donn&eacute; pour le Soleil, la Lune et les Plan&egrave;tes. C'est redondant, mais pratique pour porter ces corps c&eacute;lestes sur le Star Finder 2102-D.</xsl:variable>

  <xsl:variable name="perpetual-almanac">&Eacute;ph&eacute;m&eacute;rides &agrave; long terme</xsl:variable>
  <xsl:variable name="nautical-almanac">&Eacute;ph&eacute;m&eacute;rides Nautiques</xsl:variable>
  <xsl:variable name="lunar-distances">Distances Lunaires</xsl:variable>
  <xsl:variable name="for">pour </xsl:variable>
  
  <xsl:variable name="from">du </xsl:variable>
  <xsl:variable name="to"> au </xsl:variable>

  <xsl:variable name="sun">Soleil</xsl:variable>
  <xsl:variable name="moon">Lune</xsl:variable>
  <xsl:variable name="aries">Pt Vernal</xsl:variable>

  <xsl:variable name="hp">p.h.</xsl:variable>
  <xsl:variable name="eot12">&Eacute;qu. temps &agrave; 12:00 : </xsl:variable>
  <xsl:variable name="mp">Temps Pass. au m&eacute;ridien : </xsl:variable>
  <xsl:variable name="age">Age : </xsl:variable>
  <xsl:variable name="phase">Phase &agrave; 12:00 : </xsl:variable>

  <xsl:variable name="venus">V&eacute;nus</xsl:variable>
  <xsl:variable name="mars">Mars</xsl:variable>
  <xsl:variable name="jupiter">Jupiter</xsl:variable>
  <xsl:variable name="saturn">Saturne</xsl:variable>

  <xsl:variable name="gha">AHvo</xsl:variable>
  <xsl:variable name="gha-moon-planets">AHao</xsl:variable>
  <xsl:variable name="gha-aries">AHso</xsl:variable>
  <xsl:variable name="dec">D</xsl:variable>
  <xsl:variable name="z">Z</xsl:variable>
  
  <xsl:variable name="dist">Dist</xsl:variable>
  
  <xsl:variable name="rise">Lever</xsl:variable>
  <xsl:variable name="set">Coucher</xsl:variable>
  
  <xsl:variable name="set-rise-banner">Calcul&eacute;&nbsp;&agrave; 12:00 UCT &agrave; Greenwich</xsl:variable>
  
  <xsl:variable name="at000uct">Calcul&eacute;&nbsp;&agrave; 00:00:00 U.T.</xsl:variable>
  <xsl:variable name="moe">Obliquit&eacute; moyenne de l'&eacute;cliptique</xsl:variable>
  <xsl:variable name="toe">Obliquit&eacute; vraie de l'&eacute;cliptique</xsl:variable>
  <xsl:variable name="o0">Obliquit&eacute;</xsl:variable>
  <xsl:variable name="to0">Obliquit&eacute; vraie</xsl:variable>
  <xsl:variable name="jd">Jour Julien</xsl:variable>
  <xsl:variable name="jde">Jour Julien des &Eacute;ph&eacute;m&eacute;rides</xsl:variable>

  <xsl:variable name="stars-banner">&Eacute;toiles &agrave; 0000 U.T. (AHao(&Eacute;toile) = AHso(&Eacute;toile) + AHao(Pt Vernal))</xsl:variable>

  <xsl:variable name="name">Nom</xsl:variable>
  <xsl:variable name="sha">AHso</xsl:variable>

  <xsl:template name="date-fmt">
    <xsl:param name="year"/>
    <xsl:param name="month"/>
    <xsl:param name="day"/>
    <xsl:param name="dow"/>
    <xsl:param name="lang"/>
    <xsl:choose>
      <xsl:when test="$dow = 'monday'">Lundi</xsl:when>
      <xsl:when test="$dow = 'tuesday'">Mardi</xsl:when>
      <xsl:when test="$dow = 'wednesday'">Mercredi</xsl:when>
      <xsl:when test="$dow = 'thursday'">Jeudi</xsl:when>
      <xsl:when test="$dow = 'friday'">Vendredi</xsl:when>
      <xsl:when test="$dow = 'saturday'">Samedi</xsl:when>
      <xsl:when test="$dow = 'sunday'">Dimanche</xsl:when>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$day"/>
    <xsl:choose>
      <xsl:when test="$day = 1"><fo:inline baseline-shift="super" font-size="75%">er</fo:inline></xsl:when>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <xsl:choose>
      <xsl:when test="$month = 1">janvier</xsl:when>
      <xsl:when test="$month = 2"><xsl:text disable-output-escaping="yes">f&#233;vrier</xsl:text></xsl:when>
      <xsl:when test="$month = 3">mars</xsl:when>
      <xsl:when test="$month = 4">avril</xsl:when>
      <xsl:when test="$month = 5">mai</xsl:when>
      <xsl:when test="$month = 6">juin</xsl:when>
      <xsl:when test="$month = 7">juillet</xsl:when>
      <xsl:when test="$month = 8"><xsl:text disable-output-escaping="yes">ao&#251;t</xsl:text></xsl:when>
      <xsl:when test="$month = 9">septembre</xsl:when>
      <xsl:when test="$month = 10">octobre</xsl:when>
      <xsl:when test="$month = 11">novembre</xsl:when>
      <xsl:when test="$month = 12"><xsl:text disable-output-escaping="yes">d&#233;cembre</xsl:text></xsl:when>
    </xsl:choose>
    <xsl:text> </xsl:text>
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
          <xsl:when test="$m = 1">janvier </xsl:when>
          <xsl:when test="$m = 2">f&eacute;vrier </xsl:when>
          <xsl:when test="$m = 3">mars </xsl:when>
          <xsl:when test="$m = 4">avril </xsl:when>
          <xsl:when test="$m = 5">mai </xsl:when>
          <xsl:when test="$m = 6">juin </xsl:when>
          <xsl:when test="$m = 7">juillet </xsl:when>
          <xsl:when test="$m = 8">ao&ucirc;t </xsl:when>
          <xsl:when test="$m = 9">septembre </xsl:when>
          <xsl:when test="$m = 10">octobre </xsl:when>
          <xsl:when test="$m = 11">novembre </xsl:when>
          <xsl:when test="$m = 12">d&eacute;cembre </xsl:when>
        </xsl:choose>
        <xsl:value-of select="$y"/>
      </xsl:when>
      <!-- One day -->
      <xsl:otherwise>
        <!-- Day of Week -->
        <xsl:text>le </xsl:text>
        <xsl:choose>
          <xsl:when test="$dow = 'monday'">lundi</xsl:when>
          <xsl:when test="$dow = 'tuesday'">mardi</xsl:when>
          <xsl:when test="$dow = 'wednesday'">mercredi</xsl:when>
          <xsl:when test="$dow = 'thrusday'">jeudi</xsl:when>
          <xsl:when test="$dow = 'friday'">vendredi</xsl:when>
          <xsl:when test="$dow = 'saturday'">samedi</xsl:when>
          <xsl:when test="$dow = 'sunday'">dimanche</xsl:when>
        </xsl:choose>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$d"/>
        <xsl:text> </xsl:text>
        <xsl:choose>
          <xsl:when test="$m = 1">janvier </xsl:when>
          <xsl:when test="$m = 2">f&eacute;vrier </xsl:when>
          <xsl:when test="$m = 3">mars </xsl:when>
          <xsl:when test="$m = 4">avril </xsl:when>
          <xsl:when test="$m = 5">mai </xsl:when>
          <xsl:when test="$m = 6">juin </xsl:when>
          <xsl:when test="$m = 7">juillet </xsl:when>
          <xsl:when test="$m = 8">ao&ucirc;t </xsl:when>
          <xsl:when test="$m = 9">septembre </xsl:when>
          <xsl:when test="$m = 10">octobre </xsl:when>
          <xsl:when test="$m = 11">novembre </xsl:when>
          <xsl:when test="$m = 12">d&eacute;cembre </xsl:when>
        </xsl:choose>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$y"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
