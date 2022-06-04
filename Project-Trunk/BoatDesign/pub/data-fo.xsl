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
              B&#233;zier Boat Design
            </fo:block>
            <fo:block text-align="center" font-family="Book Antiqua" font-size="32pt" font-weight="bold" margin="1in">
              <xsl:value-of select="/boat-design/boat-data/boat-name"/>
            </fo:block>
            <fo:block text-align="left" font-family="Book Antiqua" font-size="12pt" font-weight="bold" margin="0.5in">
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
            <fo:block text-align="left" font-family="Book Antiqua" font-size="10pt" font-style="normal" margin="0.5in">
              Published <xsl:value-of select="/boat-design/published"/>
            </fo:block>
            <fo:block text-align="left" font-family="Times" font-size="8pt" font-style="italic" margin="0.5in">
              &#169; Oliv Cool Stuff Soft  
            </fo:block>
          </fo:block>

          <fo:block margin="0.4in" break-after="page">
            <!-- Ctrl Points coordinates -->
            <fo:block text-align="center" font-family="Courier New" font-weight="bold" font-size="12pt" font-style="italic" margin="0in">
              B&#233;zier Control Points (values in cm)
            </fo:block>

            <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt"  margin="5mm">
              Keel - (<xsl:value-of select="count(/boat-design/boat-data/ctrl-points/keel)"/> point(s))
            </fo:block>

            <!--xsl:for-each select="/boat-design/boat-data/default-points/keel">
              <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt">
                X: <xsl:value-of select="x"/>,
                Y: <xsl:value-of select="y"/>,
                Z: <xsl:value-of select="z"/>.
              </fo:block>
            </xsl:for-each-->

            <fo:table border="0">
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>

              <fo:table-header>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:block font-weight="bold">X</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Y</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Z</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-header>

              <fo:table-body>

                <xsl:for-each select="/boat-design/boat-data/ctrl-points/keel">
                  <fo:table-row>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="x"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="y"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="z"/> </fo:block></fo:table-cell>
                  </fo:table-row>
                </xsl:for-each>
              </fo:table-body>

            </fo:table>

            <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt" margin="5mm">
              Rail - (<xsl:value-of select="count(/boat-design/boat-data/ctrl-points/rail)"/> point(s))
            </fo:block>
            <fo:table border="0">
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>

              <fo:table-header>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:block font-weight="bold">X</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Y</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Z</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-header>

              <fo:table-body>
                <xsl:for-each select="/boat-design/boat-data/ctrl-points/rail">
                  <fo:table-row>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="x"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="y"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="z"/> </fo:block></fo:table-cell>
                  </fo:table-row>
                </xsl:for-each>
              </fo:table-body>

            </fo:table>


            <fo:block margin-top="15mm">
              <fo:block text-align="center" font-family="Courier New" font-weight="bold" font-size="12pt" font-style="italic" margin="0in">
                Dimensions (values in meters)
              </fo:block>
              <fo:table>
                <fo:table-column column-width="40mm"/>
                <fo:table-column column-width="30mm"/>
                <fo:table-column column-width="50mm"/>
                <fo:table-column column-width="50mm"/>

                <fo:table-body>
                  <fo:table-row border-width="1px" border-style="solid">
                    <fo:table-cell><fo:block font-family="Courier" font-style="italic"> LOA </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="/boat-design/boat-data/calculated/lengths/loa"/> </fo:block></fo:table-cell>
                  </fo:table-row>
                  <fo:table-row border-width="1px" border-style="solid">
                    <fo:table-cell><fo:block font-family="Courier" font-style="italic"> LWL </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="/boat-design/boat-data/calculated/lengths/lwl"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > From <xsl:value-of select="/boat-design/boat-data/calculated/lengths/lwl-start"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > To <xsl:value-of select="/boat-design/boat-data/calculated/lengths/lwl-end"/> </fo:block></fo:table-cell>
                  </fo:table-row>
                  <fo:table-row border-width="1px" border-style="solid">
                    <fo:table-cell><fo:block font-family="Courier" font-style="italic"> Max Beam </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="/boat-design/boat-data/calculated/widths/max-width"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > At <xsl:value-of select="/boat-design/boat-data/calculated/widths/max-width-x"/> </fo:block></fo:table-cell>
                  </fo:table-row>
                  <fo:table-row border-width="1px" border-style="solid">
                    <fo:table-cell><fo:block font-family="Courier" font-style="italic"> Max Draft </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="/boat-design/boat-data/calculated/depths/max-depth"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > At <xsl:value-of select="/boat-design/boat-data/calculated/depths/max-depth-x"/> </fo:block></fo:table-cell>
                  </fo:table-row>
                  <fo:table-row border-width="1px" border-style="solid">
                    <fo:table-cell><fo:block font-family="Courier" font-style="italic"> D </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="/boat-design/boat-data/calculated/D/displ"/> m<fo:inline padding-left="1pt" baseline-shift="super" font-size="8pt">3</fo:inline> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > CC-x <xsl:value-of select="/boat-design/boat-data/calculated/D/cc-x"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > CC-z <xsl:value-of select="/boat-design/boat-data/calculated/D/cc-z"/> </fo:block></fo:table-cell>
                  </fo:table-row>
                </fo:table-body>
              </fo:table>
            </fo:block>

          </fo:block>

          <fo:block break-after="page"  margin-left="1cm">
            <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt"  margin="5mm">
              Keel - (values in cm)
            </fo:block>

            <fo:table border="0">
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>

              <fo:table-header>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:block font-weight="bold">X</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Z</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-header>

              <fo:table-body>

                <xsl:for-each select="/boat-design/keel-and-rails/keel/keel">
                  <fo:table-row>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./@x"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="."/> </fo:block></fo:table-cell>
                  </fo:table-row>
                </xsl:for-each>
              </fo:table-body>

            </fo:table>

            <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt" margin="5mm">
              Rail - (values in cm)
            </fo:block>
            <fo:table border="0">
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>

              <fo:table-header>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:block font-weight="bold">X</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Y</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Z</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-header>

              <fo:table-body>
                <xsl:for-each select="/boat-design/keel-and-rails/rail/rail">
                  <fo:table-row>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./@x"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./y"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./z"/> </fo:block></fo:table-cell>
                  </fo:table-row>
                </xsl:for-each>
              </fo:table-body>

            </fo:table>

          </fo:block>

          <fo:block margin="12mm" break-after="page">
            <!-- Drawings -->
            <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt" margin="5mm">
              Water Lines
            </fo:block>

            <fo:block text-align="center">
              <!--fo:external-graphic src="url('../XY.png')"/-->
              <fo:external-graphic src="url('{/boat-design/drawings/water-lines}')" />
            </fo:block>

          </fo:block>

          <fo:block margin="12mm" break-after="page">
            <!-- Drawings -->
            <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt" margin="5mm">
              Buttocks
            </fo:block>

            <fo:block text-align="center">
              <!--fo:external-graphic src="url('../XY.png')"/-->
              <fo:external-graphic src="url('{/boat-design/drawings/buttocks}')" />
            </fo:block>

          </fo:block>

          <fo:block margin="12mm" break-after="page">
            <!-- Drawings -->
            <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt" margin="5mm">
              Frames
            </fo:block>

            <fo:block text-align="center">
              <!--fo:external-graphic src="url('../XY.png')"/-->
              <fo:external-graphic src="url('{/boat-design/drawings/frames}')" />
            </fo:block>

            <fo:block text-align="left" font-weight="bold" font-family="Courier" font-size="10pt" margin="5mm">
              Frames Coordinates (values in cm)
            </fo:block>
            <fo:table border="0">
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>
              <fo:table-column column-width="50mm"/>

              <fo:table-header>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:block font-weight="bold">X</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Y</fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block font-weight="bold">Z</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-header>

              <fo:table-body>
                <xsl:for-each select="/boat-design/drawings/frame-coordinates/frame">
                  <!-- keel -->
                  <fo:table-row>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./@x"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <!--xsl:value-of select="y"/--> 0.0 </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./keel/z"/> </fo:block></fo:table-cell>
                  </fo:table-row>

                  <xsl:for-each select="./frame-coord-z">
                    <fo:table-row>
                      <fo:table-cell><fo:block font-family="Courier" > </fo:block></fo:table-cell>
                      <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./@w"/> </fo:block></fo:table-cell>
                      <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="."/> </fo:block></fo:table-cell>
                    </fo:table-row>
                  </xsl:for-each>

                  <!-- rail -->
                  <fo:table-row>
                    <fo:table-cell><fo:block font-family="Courier" > <!--xsl:value-of select="./@x"/--> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./rail/y"/> </fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-family="Courier" > <xsl:value-of select="./rail/z"/> </fo:block></fo:table-cell>
                  </fo:table-row>

                </xsl:for-each>
              </fo:table-body>

            </fo:table>

          </fo:block>

          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

</xsl:stylesheet>
