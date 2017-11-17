/**
 * ===============================================================================
 * $Id: GribPDSLevel.java,v 1.6 2006/07/27 13:11:27 frv_peg Exp $
 * ===============================================================================
 * JGRIB library
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Authors:
 * See AUTHORS file
 * ===============================================================================
 */

/**
 * GribPDSLevel.java  1.0  08/01/2002
 *
 * Newly created, based on GribTables class.  Moved level specific
 * functionality to this class.
 * Performs operations related to loading level information from Table 3.
 * Corrected the octet numbers.
 *
 * Author: Capt Richard D. Gonzalez
 */

package jgrib;

/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.<p/>
 *
 * <b>2006-07-26 frv_peg: Added NCEP extention levels for use of GFS files.</b><p/>
 * (level: 117, 211,212,213,222,223,232,233,242,243,244)</p>
 *
 * See:
 * <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html">table3.html</a>
 */

public class GribPDSLevel
{
  /**
   * Index number from table 3 - can be used for comparison even if the
   * description of the level changes
   */
  private int index;

  /**
   * Name of the vertical coordinate/level
   *
   */
  private String name = null;

  /**
   * Value of PDS octet10 if separate from 11, otherwise value from octet10&11
   */
  private float value1;

  /**
   * Value of PDS octet11
   */
  private float value2;

  /**
   * Stores a short name of the level - same as the string "level" in the original
   * GribRecordPDS implementation
   */
  private String level = "";

  /**
   * Stores a descriptive name of the level
   * GribRecordPDS implementation
   */
  private String description = "";

  /**
   * Stores the name of the level - same as the string "level" in the original
   * GribRecordPDS implementation
   */
  private String units = "";

  /**
   * Stores whether this is (usually) a vertical coordinate for a single layer
   *  (e.g. surface, tropopause level) or multiple layers (e.g. hPa, m AGL)
   * Aids in deciding whether to build 2D or 3D grids from the data
   */
  private boolean isSingleLayer = true;

  /**
   * Indicates whether the vertical coordinate increases with height.
   * e.g. false for pressure and sigma, true for height above ground or if unknown
   */
  private boolean isIncreasingUp = true;

  /**
   * True if a numeric values are used for this level (e.g. 1000 mb)
   * False if level doesn't use values (e.g. surface).
   * Basically indicates whether you will be able to get a value for this level.
   */
  private boolean isNumeric = false;


  /**
   * Constructor.  Creates a GribPDSLevel based on octets 10-12 of the PDS.
   * Implements tables 3 and 3a.
   *
   * @param pds10 part 1 of level code
   * @param pds11 part 2 of level code
   * @param pds12 part 3 of level code
   */
  public GribPDSLevel(int pds10, int pds11, int pds12)
  {

    int pds1112 = pds11 << 8 | pds12;
    this.index = pds10;
    switch (index)
    {
      case 1:
        name = description = level = "surface";
        break;
      case 2:
        name = description = level = "cloud base level";
        break;
      case 3:
        name = description = level = "cloud top level";
        break;
      case 4:
        name = description = level = "0 degree isotherm level";
        break;
      case 5:
        name = description = level = "condensation level";
        break;
      case 6:
        name = description = level = "maximum wind level";
        break;
      case 7:
        name = description = level = "tropopause level";
        break;
      case 8:
        name = description = level = "nominal atmosphere top";
        break;
      case 9:
        name = description = level = "sea bottom";
        break;
      case 20:
        name = "Isothermal level";
        value1 = pds1112;
        units = "K";
        isNumeric = true;
        level = value1 + " K";
        description = "Isothermal level at " + value1 / 100 + units;
        break;
      case 100:
        name = "P";
        value1 = pds1112;
        units = "hPa";
        isNumeric = true;
        isIncreasingUp = false;
        isSingleLayer = false;
        level = pds1112 + " " + units;
        description = "pressure at " + value1 + " " + units;
        break;
      case 101:
        name = "layer between two isobaric levels";
        value1 = pds11 * 10; // convert from kPa to hPa - who uses kPa???
        value2 = pds12 * 10;
        units = "hPa";
        level = value1 + " - " + value2 + " " + units;
        description = "layer between " + value1 + " and " + value2 + " " + units;
        break;
      case 102:
        name = description = level = "mean sea level";
        break;
      case 103:
        name = "Altitude above MSL";
        value1 = pds1112;
        units = "m";
        isNumeric = true;
        isSingleLayer = false;
        level = pds1112 + " " + units;
        description = value1 + " m above mean sea level";
        break;
      case 104:
        name = "Layer between two altitudes above MSL";
        value1 = (pds11 * 100); // convert hm to m
        value2 = (pds12 * 100);
        units = "m";
        level = value1 + "-" + value2 + " " + units;
        description = "Layer between " + pds11 + " and " + pds12 + " m above mean sea level";
        break;
      case 105:
        name = "fixed height above ground";
        value1 = pds1112;
        units = "m";
        isNumeric = true;
        isSingleLayer = false;
        level = value1 + units;
        description = value1 + " m above ground";
        break;
      case 106:
        name = "layer between two height levels";
        value1 = (pds11 * 100); // convert hm to m
        value2 = (pds12 * 100);
        units = "m";
        isNumeric = true;
        level = value1 + "-" + value2 + " m AGL";
        description = "Layer between " + value1 + " and " + value2 + " m above ground";
        break;
      case 107:
        name = "Sigma level";
        value1 = (pds1112 / 10000.0f);
        level = "sigma=" + value1;
        units = "sigma";
        isNumeric = true;
        isSingleLayer = false;
        isIncreasingUp = false;
        description = "sigma = " + value1;
        break;
      case 108:
        name = "Layer between two sigma layers";
        value1 = (pds11 / 100.0f);
        value2 = (pds12 / 100.0f);
        isNumeric = true;
        level = "sigma " + value1 + "-" + value2;
        description = "Layer between sigma levels " + value1 + " and " + value2;
        break;
      case 109:
        name = "hybrid level";
        value1 = pds1112;
        isNumeric = true;
        level = "hybrid level " + value1;
        description = "hybrid level " + value1;
        break;
      case 110:
        name = "Layer between two hybrid levels";
        value1 = pds11;
        value2 = pds12;
        isNumeric = true;
        level = "hybrid " + value1 + "-" + value2;
        description = "Layer between hybrid levels " + value1 + " and " + value2;
        break;
      case 111:
        name = "Depth below land surface";
        value1 = pds1112;
        units = "cm";
        isNumeric = true;
        level = value1 + " " + units;
        description = value1 + " " + units;
        break;
      case 112:
        name = "Layer between two levels below land surface";
        value1 = pds11;
        value2 = pds12;
        units = "cm";
        isNumeric = true;
        level = value1 + " - " + value2 + " " + units;
        description = "Layer between " + value1 + " and " + value2 + " cm below land surface";
        break;
      case 113:
        name = "Isentropic (theta) level";
        value1 = pds1112;
        units = "K";
        isNumeric = true;
        isSingleLayer = false;
        level = value1 + " K";
        description = value1 + " K";
        break;
      case 114:
        name = "Layer between two isentropic layers";
        value1 = (pds11 + 475);
        value2 = (pds12 + 475);
        units = "K";
        isNumeric = true;
        description = "Layer between " + value1 + " and " + value2 + " K";
        break;
      case 116:
        name = "Layer between pressure differences from ground to levels";
        value1 = pds11;
        value2 = pds12;
        units = "hPa";
        isNumeric = true;
        level = value1 + units + " - " + value2 + units;
        description = "Layer between pressure differences from ground: " + value1 + " and " + value2 + " K";
        break;
      case 117:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = "potential vorticity(pv) surface";
        value1 = pds1112;
        units = "10^9 km^2/kgs";
        isNumeric = false;
        level = "surface";
        description = name;
        break;
      case 125:
        name = "Height above ground (high precision)";
        value1 = pds1112;
        units = "cm";
        isNumeric = true;
        isSingleLayer = false;
        level = value1 + " " + units;
        description = value1 + " " + units + " above ground";
        break;
      case 160:
        name = "Depth below sea level";
        value1 = pds1112;
        units = "m";
        isNumeric = true;
        level = value1 + " m below sea level";
        description = pds1112 + " m below sea level";
        break;
      case 200:
        name = description = level = "entire atmosphere layer";
        break;
      case 201:
        name = description = level = "entire ocean layer";
        break;
      case 204:
        name = description = level = "Highest tropospheric freezing level";
        break;
      case 211:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "Boundary layer cloud layer (BCY)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 212:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "Low cloud bottom level (LCBL)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 213:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "Low cloud top level (LCTL)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 214:
        name = description = level = "Low Cloud Layer";
        break;
      case 222:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "Middle cloud bottom level (MCBL)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 223:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "Middle cloud top level (MCTL)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 224:
        name = description = level = "Middle Cloud Layer";
        break;
      case 232:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "High cloud bottom level (HCBL)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 233:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "High cloud top level (HCTL)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 234:
        name = description = level = "High Cloud Layer";
        break;
      case 242:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "Convective cloud bottom level (CCBL)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 243:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "Convective cloud top level (CCTL)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;
      case 244:
        // frv_peg - 2006-07-26 values from
        // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
        name = level = "Convective cloud layer (CCY)";
        description = "NCEP extention: " + "name";
        isNumeric = false;
        break;

      default:
        name = description = "undefined level";
        units = "undefined units";
        System.err.println("GribPDSLevel: Table 3 level " + index + " is not implemented yet");
        break;
    }
  }

  /**
   * @return true if negative z-value
   */
  public boolean isDepth()
  {
    return index == 111 || index == 160;
  }

  /**
   * @return Index
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * @return Name
   */
  public String getName()
  {
    return name;
  }

  /**
   *
   * @return Level
   */
  public String getLevel()
  {
    return level;
  }

  /**
   *
   * @return Description
   */
  public String getDesc()
  {
    return description;
  }

  /**
   *
   * @return Unit
   */
  public String getUnits()
  {
    return units;
  }

  /**
   *
   * @return Value1
   */
  public float getValue1()
  {
    return value1;
  }

  /**
   *
   * @return Value2
   */
  public float getValue2()
  {
    return value2;
  }

  /**
   *
   * @return true/false if numeric
   */
  public boolean getIsNumeric()
  {
    return isNumeric;
  }

  /**
   *
   * @return true/false if increasing up
   */
  public boolean getIsIncreasingUp()
  {
    return isIncreasingUp;
  }

  /**
   *
   * @return true/false, if is 2D variable
   */
  public boolean getIsSingleLayer()
  {
    return isSingleLayer;
  }

  /**
   * Formats the class for output
   * @return String holding description of the object parameters
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "Level description:" + '\n' + "        \tparameter id: " + this.index + "\n" +
      "        \tname: " + this.name + "\n" +
      "        \tdescription: " + this.description + "\n" +
      "        \tunits: " + this.units + "\n" +
      "        \tshort descr: " + this.level + "\n" +
      "        \tincreasing up?: " + this.isIncreasingUp + "\n" +
      "        \tsingle layer?: " + this.isSingleLayer + "\n" +
      "        \tvalue1: " + this.value1 + "\n" +
      "        \tvalue2: " + this.value2 + "\n";
  }

  /**
   * rdg - added equals method
   * didn't check everything as most are set in the constructor
   *
   * @param obj - Object to check
   * @return true/false depends upon succes
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (!(obj instanceof GribPDSLevel))
      return false;

    // quick check to see if same object
    if (this == obj)
      return true;

    GribPDSLevel lvl = (GribPDSLevel) obj;
    if (index != lvl.getIndex())
      return false;
    if (value1 != lvl.getValue1())
      return false;
    if (value2 != lvl.getValue2())
      return false;

    return true;
  }

  /**
   * rdg - added this method to be used in a comparator for sorting while
   *       extracting records.
   * @param level
   *
   * @return - -1 if level is "less than" this, 0 if equal, 1 if level is "greater than" this.
   *
   */
  public int compare(GribPDSLevel level)
  {
    if (this.equals(level))
      return 0;

    // check if level is less than this
    if (index > level.getIndex())
      return -1;
    if (value1 > level.getValue1())
      return -1;
    if (value2 > level.getValue2())
      return -1;

    return 1;
  }
}

