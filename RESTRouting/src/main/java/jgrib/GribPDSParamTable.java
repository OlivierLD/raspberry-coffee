/**
 * ===============================================================================
 * $Id: GribPDSParamTable.java,v 1.10 2006/07/25 13:46:00 frv_peg Exp $
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
 * GribPDSParamTable.java  1.0  08/01/2002
 *
 * Newly created, based on GribTables class.  Moved Parameter Table specific
 * functionality to this class.
 * Performs operations related to loading parameter tables stored in files.
 * Removed the embedded table as this limited functionality and
 * made dynamic changes impossible.
 * Through a lookup table (see readParameterTableLookup) all of the supported
 * Parameter Tables are known.  An actual table is not loaded until a parameter
 * from that center/subcenter/table is loaded.
 *
 * For now, the lookup table name is hard coded to ".\\tables\\tablelookup.lst"
 *
 * rdg - Still need to finish implementing SubCenters
 *
 * @Author: Capt Richard D. Gonzalez
 */

package jgrib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.
 */

public class GribPDSParamTable
{
  /**
   * System property variable to search, or set, when using
   * user supplied gribtab directories, and or a stand alone
   * gribtab file
   *
   * !! Important: Remember to format this as an URL !!
   */
  public static final String PROPERTY_GRIBTABURL = "GribTabURL";

  /**
   * Name of directory to search in, when reading
   * buildin parameter tables, ie stored in jgrib.jar
   */
  private static final String TABLE_DIRECTORY = "tables";

  /**
   * Name of file to read, when searching for parameter tables
   * stored in a directory
   */
  private static final String TABLE_LIST = "tablelookup.lst";

  /**
   * There is 256 entries in a parameter table due to the
   * nature of a byte
   */
  private static final int NPARAMETERS = 256;

  /**
   * Identification of center e.g. 88 for Oslo
   */
  protected int center_id;

  /**
   * Identification of center defined sub-center - not fully implemented yet
   *
   */
  protected int subcenter_id;

  /**
   * Identification of parameter table version number
   *
   */
  protected int table_number;

  /**
   * Stores the name of the file containing this table - not opened unless
   * required for lookup.
   */
  protected String filename = null;


  /**
   * URL store corresponding url of filename containint this table.
   * Opened if required for lookup.
   */
  protected URL url = null;

  /**
   * Parameters - stores array of GribPDSParameter classes
   */
  protected GribPDSParameter[] parameters = null;

  /**
   * List of parameter tables
   */
  protected static List<GribPDSParamTable> tables = null;

  /**
   *  Added by Richard D. Gonzalez
   *  static Array with parameter tables used by the GRIB file
   * (should only be one, but not actually limited to that - this allows
   *  GRIB files to be read that have more than one center's information in it)
   *
   */
  private static GribPDSParamTable[] paramTables = null;


  /**
   * Used to store names of files
   */
  private static Map<String, GribPDSParamTable> fileTabMap = new HashMap<String, GribPDSParamTable>();

  /**
   * Default constructor
   */
  private GribPDSParamTable()
  {
  }

  /**
   * Constructor used to add extra parameter tables
   * to the default, so the volume to search can be
   * increased.
   * @param name
   * @param cen
   * @param sub
   * @param tab
   * @param par
   */
  private GribPDSParamTable(String name, int cen, int sub, int tab, GribPDSParameter[] par)
  {
    filename = name;
    center_id = cen;
    subcenter_id = sub;
    table_number = tab;
    url = null;
    parameters = par;
  }
  static /**
         * - peg - As of 2005-12-09
         * Reimplementet static method to allow for user supplied
         * directory structures as known from initFromJar and also to
         * make it possible to read a single gribtab file. IE without
         * having to create a tablelookup.lst file
         */
  {
    try
    {
      // Add default tables to array
      GribPDSParamTable.tables = new ArrayList<GribPDSParamTable>();
      initDefaultTableEntries(tables);
      initFromJAR(tables);

      // Add user supplied tables too - from System.properties()
      String gribtab = System.getProperty(PROPERTY_GRIBTABURL);
      // System.out.println("JGRIB: static: gribtab = "+gribtab);

      if (true || tables.size() == 0)
      {
        if (gribtab != null)
        {
          // Detect if supplied gribtab is pointing to a file or a directory
          URL url = new URL(gribtab);
          File gribTabFile = new File(url.getFile());
          if (gribTabFile.isFile())
          {
            // Use gribtab files supplied by user
            try
            {
              readTableEntry(gribTabFile.toURI().toURL(), tables);
              // System.out.println("Using user supplied gribtab table!");
            }
            catch (IOException e)
            {
              System.err.println("IOException: " + e.getMessage());
            }
            catch (NotSupportedException e)
            {
              System.err.println("NotSupportedException: " + e.getMessage());
            }

          }
          else // isDirectory
          {
            // Use gribtab files supplied by user in jar file or directory
            try
            {
              readTableEntries(gribtab, tables);
              // System.out.println("Using user supplied gribtab table directory!");
              System.out.println("Using user supplied gribtab table directory!");
            }
            catch (IOException e)
            {
              System.err.println("IOException: " + e.getMessage());
            }
          }
        }

      }
      else
      {

        // Use from jar file
        // System.out.println("Using gribtab table from class path (jar)");
      }

      // Make table
      paramTables = tables.toArray(new GribPDSParamTable[tables.size()]);

    }
    catch (IOException e)
    {
      System.err.println("IOException: " + e.getMessage());
    }
  }


  /**
   * Load default tables from jar file (class path)
   *
   * Reads in the list of tables available and stores them.  Does not actually
   *    open the parameter tables files, nor store the list of parameters, but
   *    just stores the file names of the parameter tables.
   * Parameters for a table are read in when the table is requested (in the
   *    getParameterTable method).
   * Currently hardcoded the file name to "tablelookup".  May change to command
   *    line later, but would rather minimize command line inputs.
   *
   * Added by Tor C.Bekkvik
   * todo add method for appending more GRIBtables later
   * todo comments
   * todo repeated gribtables in tablelookup : load only 1 copy !
   * todo keep mapping info; keep destination table center,table etc
   * @param aTables
   * @throws IOException
   */
  private static void initFromJAR(List<GribPDSParamTable> aTables)
    throws IOException
  {
    ClassLoader cl = GribPDSParamTable.class.getClassLoader();

	  // Hack. Set to true to force exit.
    boolean force = "true".equals(System.getProperty("jgrib.force.exit", "false"));

    URL baseUrl = cl.getResource(TABLE_DIRECTORY);
    if (baseUrl == null)
    {
      System.out.println(String.format("BaseURL for %s is null", TABLE_DIRECTORY));
      return;
    } else {
      // Hack
      if (force) {
        System.out.println(">>> Forcing return <<<");
        return;
      }
    }
    if ("true".equals(System.getProperty("grib.verbose", "false"))) {
      System.out.println("JGRIB: Building gribtab url = " + baseUrl.toExternalForm());
    }
    readTableEntries(baseUrl.toExternalForm(), aTables);
  }


  /**
   * Initiate default tables
   * added by Tor C.Bekkvik
   * @param aTables
   */
  private static void initDefaultTableEntries(List<GribPDSParamTable> aTables)
  {
    String[][] defaulttable_ncep_reanal2 =
    {
      /*   0 */
      { "var0", "undefined", "undefined" },
      /*   1 */
      { "pres", "Pressure", "Pa" },
      /*   2 */
      { "prmsl", "Pressure reduced to MSL", "Pa" },
      /*   3 */
      { "ptend", "Pressure tendency", "Pa/s" },
      /*   4 */
      { "var4", "undefined", "undefined" },
      /*   5 */
      { "var5", "undefined", "undefined" },
      /*   6 */
      { "gp", "Geopotential", "m^2/s^2" },
      /*   7 */
      { "hgt", "Geopotential height", "gpm" },
      /*   8 */
      { "dist", "Geometric height", "m" },
      /*   9 */
      { "hstdv", "Std dev of height", "m" },
      /*  10 */
      { "hvar", "Varianance of height", "m^2" },
      /*  11 */
      { "tmp", "Temperature", "K" },
      /*  12 */
      { "vtmp", "Virtual temperature", "K" },
      /*  13 */
      { "pot", "Potential temperature", "K" },
      /*  14 */
      { "epot", "Pseudo-adiabatic pot. temperature", "K" },
      /*  15 */
      { "tmax", "Max. temperature", "K" },
      /*  16 */
      { "tmin", "Min. temperature", "K" },
      /*  17 */
      { "dpt", "Dew point temperature", "K" },
      /*  18 */
      { "depr", "Dew point depression", "K" },
      /*  19 */
      { "lapr", "Lapse rate", "K/m" },
      /*  20 */
      { "visib", "Visibility", "m" },
      /*  21 */
      { "rdsp1", "Radar spectra (1)", "" },
      /*  22 */
      { "rdsp2", "Radar spectra (2)", "" },
      /*  23 */
      { "rdsp3", "Radar spectra (3)", "" },
      /*  24 */
      { "var24", "undefined", "undefined" },
      /*  25 */
      { "tmpa", "Temperature anomaly", "K" },
      /*  26 */
      { "presa", "Pressure anomaly", "Pa" },
      /*  27 */
      { "gpa", "Geopotential height anomaly", "gpm" },
      /*  28 */
      { "wvsp1", "Wave spectra (1)", "" },
      /*  29 */
      { "wvsp2", "Wave spectra (2)", "" },
      /*  30 */
      { "wvsp3", "Wave spectra (3)", "" },
      /*  31 */
      { "wdir", "Wind direction", "deg" },
      /*  32 */
      { "wind", "Wind speed", "m/s" },
      /*  33 */
      { "ugrd", "u wind", "m/s" },
      /*  34 */
      { "vgrd", "v wind", "m/s" },
      /*  35 */
      { "strm", "Stream function", "m^2/s" },
      /*  36 */
      { "vpot", "Velocity potential", "m^2/s" },
      /*  37 */
      { "mntsf", "Montgomery stream function", "m^2/s^2" },
      /*  38 */
      { "sgcvv", "Sigma coord. vertical velocity", "/s" },
      /*  39 */
      { "vvel", "Pressure vertical velocity", "Pa/s" },
      /*  40 */
      { "dzdt", "Geometric vertical velocity", "m/s" },
      /*  41 */
      { "absv", "Absolute vorticity", "/s" },
      /*  42 */
      { "absd", "Absolute divergence", "/s" },
      /*  43 */
      { "relv", "Relative vorticity", "/s" },
      /*  44 */
      { "reld", "Relative divergence", "/s" },
      /*  45 */
      { "vucsh", "Vertical u shear", "/s" },
      /*  46 */
      { "vvcsh", "Vertical v shear", "/s" },
      /*  47 */
      { "dirc", "Direction of current", "deg" },
      /*  48 */
      { "spc", "Speed of current", "m/s" },
      /*  49 */
      { "uogrd", "u of current", "m/s" },
      /*  50 */
      { "vogrd", "v of current", "m/s" },
      /*  51 */
      { "spfh", "Specific humidity", "kg/kg" },
      /*  52 */
      { "rh", "Relative humidity", "%" },
      /*  53 */
      { "mixr", "Humidity mixing ratio", "kg/kg" },
      /*  54 */
      { "pwat", "Precipitable water", "kg/m^2" },
      /*  55 */
      { "vapp", "Vapor pressure", "Pa" },
      /*  56 */
      { "satd", "Saturation deficit", "Pa" },
      /*  57 */
      { "evp", "Evaporation", "kg/m^2" },
      /*  58 */
      { "cice", "Cloud Ice", "kg/m^2" },
      /*  59 */
      { "prate", "Precipitation rate", "kg/m^2/s" },
      /*  60 */
      { "tstm", "Thunderstorm probability", "%" },
      /*  61 */
      { "apcp", "Total precipitation", "kg/m^2" },
      /*  62 */
      { "ncpcp", "Large scale precipitation", "kg/m^2" },
      /*  63 */
      { "acpcp", "Convective precipitation", "kg/m^2" },
      /*  64 */
      { "srweq", "Snowfall rate water equiv.", "kg/m^2/s" },
      /*  65 */
      { "weasd", "Accum. snow", "kg/m^2" },
      /*  66 */
      { "snod", "Snow depth", "m" },
      /*  67 */
      { "mixht", "Mixed layer depth", "m" },
      /*  68 */
      { "tthdp", "Transient thermocline depth", "m" },
      /*  69 */
      { "mthd", "Main thermocline depth", "m" },
      /*  70 */
      { "mtha", "Main thermocline anomaly", "m" },
      /*  71 */
      { "tcdc", "Total cloud cover", "%" },
      /*  72 */
      { "cdcon", "Convective cloud cover", "%" },
      /*  73 */
      { "lcdc", "Low level cloud cover", "%" },
      /*  74 */
      { "mcdc", "Mid level cloud cover", "%" },
      /*  75 */
      { "hcdc", "High level cloud cover", "%" },
      /*  76 */
      { "cwat", "Cloud water", "kg/m^2" },
      /*  77 */
      { "var77", "undefined", "undefined" },
      /*  78 */
      { "snoc", "Convective snow", "kg/m^2" },
      /*  79 */
      { "snol", "Large scale snow", "kg/m^2" },
      /*  80 */
      { "wtmp", "Water temperature", "K" },
      /*  81 */
      { "land", "Land cover (land=1;sea=0)", "fraction" },
      /*  82 */
      { "dslm", "Deviation of sea level from mean", "m" },
      /*  83 */
      { "sfcr", "Surface roughness", "m" },
      /*  84 */
      { "albdo", "Albedo", "%" },
      /*  85 */
      { "tsoil", "Soil temperature", "K" },
      /*  86 */
      { "soilm", "Soil moisture content", "kg/m^2" },
      /*  87 */
      { "veg", "Vegetation", "%" },
      /*  88 */
      { "salty", "Salinity", "kg/kg" },
      /*  89 */
      { "den", "Density", "kg/m^3" },
      /*  90 */
      { "runof", "Runoff", "kg/m^2" },
      /*  91 */
      { "icec", "Ice concentration (ice=1;no ice=0)", "fraction" },
      /*  92 */
      { "icetk", "Ice thickness", "m" },
      /*  93 */
      { "diced", "Direction of ice drift", "deg" },
      /*  94 */
      { "siced", "Speed of ice drift", "m/s" },
      /*  95 */
      { "uice", "u of ice drift", "m/s" },
      /*  96 */
      { "vice", "v of ice drift", "m/s" },
      /*  97 */
      { "iceg", "Ice growth rate", "m/s" },
      /*  98 */
      { "iced", "Ice divergence", "/s" },
      /*  99 */
      { "snom", "Snow melt", "kg/m^2" },
      /* 100 */
      { "htsgw", "Sig height of wind waves and swell", "m" },
      /* 101 */
      { "wvdir", "Direction of wind waves", "deg" },
      /* 102 */
      { "wvhgt", "Sig height of wind waves", "m" },
      /* 103 */
      { "wvper", "Mean period of wind waves", "s" },
      /* 104 */
      { "swdir", "Direction of swell waves", "deg" },
      /* 105 */
      { "swell", "Sig height of swell waves", "m" },
      /* 106 */
      { "swper", "Mean period of swell waves", "s" },
      /* 107 */
      { "dirpw", "Primary wave direction", "deg" },
      /* 108 */
      { "perpw", "Primary wave mean period", "s" },
      /* 109 */
      { "dirsw", "Secondary wave direction", "deg" },
      /* 110 */
      { "persw", "Secondary wave mean period", "s" },
      /* 111 */
      { "nswrs", "Net short wave (surface)", "W/m^2" },
      /* 112 */
      { "nlwrs", "Net long wave (surface)", "W/m^2" },
      /* 113 */
      { "nswrt", "Net short wave (top)", "W/m^2" },
      /* 114 */
      { "nlwrt", "Net long wave (top)", "W/m^2" },
      /* 115 */
      { "lwavr", "Long wave", "W/m^2" },
      /* 116 */
      { "swavr", "Short wave", "W/m^2" },
      /* 117 */
      { "grad", "Global radiation", "W/m^2" },
      /* 118 */
      { "var118", "undefined", "undefined" },
      /* 119 */
      { "var119", "undefined", "undefined" },
      /* 120 */
      { "var120", "undefined", "undefined" },
      /* 121 */
      { "lhtfl", "Latent heat flux", "W/m^2" },
      /* 122 */
      { "shtfl", "Sensible heat flux", "W/m^2" },
      /* 123 */
      { "blydp", "Boundary layer dissipation", "W/m^2" },
      /* 124 */
      { "uflx", "Zonal momentum flux", "N/m^2" },
      /* 125 */
      { "vflx", "Meridional momentum flux", "N/m^2" },
      /* 126 */
      { "wmixe", "Wind mixing energy", "J" },
      /* 127 */
      { "imgd", "Image data", "" },
      /* 128 */
      { "mslsa", "Mean sea level pressure (Std Atm)", "Pa" },
      /* 129 */
      { "mslma", "Mean sea level pressure (MAPS)", "Pa" },
      /* 130 */
      { "mslet", "Mean sea level pressure (ETA model)", "Pa" },
      /* 131 */
      { "lftx", "Surface lifted index", "K" },
      /* 132 */
      { "4lftx", "Best (4-layer) lifted index", "K" },
      /* 133 */
      { "kx", "K index", "K" },
      /* 134 */
      { "sx", "Sweat index", "K" },
      /* 135 */
      { "mconv", "Horizontal moisture divergence", "kg/kg/s" },
      /* 136 */
      { "vssh", "Vertical speed shear", "1/s" },
      /* 137 */
      { "tslsa", "3-hr pressure tendency (Std Atmos Red)", "Pa/s" },
      /* 138 */
      { "bvf2", "Brunt-Vaisala frequency^2", "1/s^2" },
      /* 139 */
      { "pvmw", "Potential vorticity (mass-weighted)", "1/s/m" },
      /* 140 */
      { "crain", "Categorical rain", "yes=1;no=0" },
      /* 141 */
      { "cfrzr", "Categorical freezing rain", "yes=1;no=0" },
      /* 142 */
      { "cicep", "Categorical ice pellets", "yes=1;no=0" },
      /* 143 */
      { "csnow", "Categorical snow", "yes=1;no=0" },
      /* 144 */
      { "soilw", "Volumetric soil moisture", "fraction" },
      /* 145 */
      { "pevpr", "Potential evaporation rate", "W/m^2" },
      /* 146 */
      { "cwork", "Cloud work function", "J/kg" },
      /* 147 */
      { "u-gwd", "Zonal gravity wave stress", "N/m^2" },
      /* 148 */
      { "v-gwd", "Meridional gravity wave stress", "N/m^2" },
      /* 149 */
      { "pvort", "Potential vorticity", "m^2/s/kg" },
      /* 150 */
      { "var150", "undefined", "undefined" },
      /* 151 */
      { "var151", "undefined", "undefined" },
      /* 152 */
      { "var152", "undefined", "undefined" },
      /* 153 */
      { "mfxdv", "Moisture flux divergence", "gr/gr*m/s/m" },
      /* 154 */
      { "vqr154", "undefined", "undefined" },
      /* 155 */
      { "gflux", "Ground heat flux", "W/m^2" },
      /* 156 */
      { "cin", "Convective inhibition", "J/kg" },
      /* 157 */
      { "cape", "Convective Avail. Pot. Energy", "J/kg" },
      /* 158 */
      { "tke", "Turbulent kinetic energy", "J/kg" },
      /* 159 */
      { "condp", "Lifted parcel condensation pressure", "Pa" },
      /* 160 */
      { "csusf", "Clear sky upward solar flux", "W/m^2" },
      /* 161 */
      { "csdsf", "Clear sky downward solar flux", "W/m^2" },
      /* 162 */
      { "csulf", "Clear sky upward long wave flux", "W/m^2" },
      /* 163 */
      { "csdlf", "Clear sky downward long wave flux", "W/m^2" },
      /* 164 */
      { "cfnsf", "Cloud forcing net solar flux", "W/m^2" },
      /* 165 */
      { "cfnlf", "Cloud forcing net long wave flux", "W/m^2" },
      /* 166 */
      { "vbdsf", "Visible beam downward solar flux", "W/m^2" },
      /* 167 */
      { "vddsf", "Visible diffuse downward solar flux", "W/m^2" },
      /* 168 */
      { "nbdsf", "Near IR beam downward solar flux", "W/m^2" },
      /* 169 */
      { "nddsf", "Near IR diffuse downward solar flux", "W/m^2" },
      /* 170 */
      { "ustr", "U wind stress", "N/m^2" },
      /* 171 */
      { "vstr", "V wind stress", "N/m^2" },
      /* 172 */
      { "mflx", "Momentum flux", "N/m^2" },
      /* 173 */
      { "lmh", "Mass point model surface", "" },
      /* 174 */
      { "lmv", "Velocity point model surface", "" },
      /* 175 */
      { "sglyr", "Neraby model level", "" },
      /* 176 */
      { "nlat", "Latitude", "deg" },
      /* 177 */
      { "nlon", "Longitude", "deg" },
      /* 178 */
      { "umas", "Mass weighted u", "gm/m*K*s" },
      /* 179 */
      { "vmas", "Mass weigtted v", "gm/m*K*s" },
      /* 180 */
      { "var180", "undefined", "undefined" },
      /* 181 */
      { "lpsx", "x-gradient of log pressure", "1/m" },
      /* 182 */
      { "lpsy", "y-gradient of log pressure", "1/m" },
      /* 183 */
      { "hgtx", "x-gradient of height", "m/m" },
      /* 184 */
      { "hgty", "y-gradient of height", "m/m" },
      /* 185 */
      { "stdz", "Standard deviation of Geop. hgt.", "m" },
      /* 186 */
      { "stdu", "Standard deviation of zonal wind", "m/s" },
      /* 187 */
      { "stdv", "Standard deviation of meridional wind", "m/s" },
      /* 188 */
      { "stdq", "Standard deviation of spec. hum.", "gm/gm" },
      /* 189 */
      { "stdt", "Standard deviation of temperature", "K" },
      /* 190 */
      { "cbuw", "Covariance between u and omega", "m/s*Pa/s" },
      /* 191 */
      { "cbvw", "Covariance between v and omega", "m/s*Pa/s" },
      /* 192 */
      { "cbuq", "Covariance between u and specific hum", "m/s*gm/gm" },
      /* 193 */
      { "cbvq", "Covariance between v and specific hum", "m/s*gm/gm" },
      /* 194 */
      { "cbtw", "Covariance between T and omega", "K*Pa/s" },
      /* 195 */
      { "cbqw", "Covariance between spec. hum and omeg", "gm/gm*Pa/s" },
      /* 196 */
      { "cbmzw", "Covariance between v and u", "m^2/si^2" },
      /* 197 */
      { "cbtzw", "Covariance between u and T", "K*m/s" },
      /* 198 */
      { "cbtmw", "Covariance between v and T", "K*m/s" },
      /* 199 */
      { "stdrh", "Standard deviation of Rel. Hum.", "%" },
      /* 200 */
      { "sdtz", "Std dev of time tend of geop. hgt", "m" },
      /* 201 */
      { "icwat", "Ice-free water surface", "%" },
      /* 202 */
      { "sdtu", "Std dev of time tend of zonal wind", "m/s" },
      /* 203 */
      { "sdtv", "Std dev of time tend of merid wind", "m/s" },
      /* 204 */
      { "dswrf", "Downward solar radiation flux", "W/m^2" },
      /* 205 */
      { "dlwrf", "Downward long wave radiation flux", "W/m^2" },
      /* 206 */
      { "sdtq", "Std dev of time tend of spec. hum", "gm/gm" },
      /* 207 */
      { "mstav", "Moisture availability", "%" },
      /* 208 */
      { "sfexc", "Exchange coefficient", "(kg/m^3)(m/s)" },
      /* 209 */
      { "mixly", "No. of mixed layers next to surface", "integer" },
      /* 210 */
      { "sdtt", "Std dev of time tend of temperature", "K" },
      /* 211 */
      { "uswrf", "Upward short wave flux", "W/m^2" },
      /* 212 */
      { "ulwrf", "Upward long wave flux", "W/m^2" },
      /* 213 */
      { "cdlyr", "Non-convective cloud", "%" },
      /* 214 */
      { "cprat", "Convective precip. rate", "kg/m^2/s" },
      /* 215 */
      { "ttdia", "Temperature tendency by all physics", "K/s" },
      /* 216 */
      { "ttrad", "Temperature tendency by all radiation", "K/s" },
      /* 217 */
      { "ttphy", "Temperature tendency by non-radiation physics", "K/s" },
      /* 218 */
      { "preix", "Precip index (0.0-1.00)", "fraction" },
      /* 219 */
      { "tsd1d", "Std. dev. of IR T over 1x1 deg area", "K" },
      /* 220 */
      { "nlgsp", "Natural log of surface pressure", "ln(kPa)" },
      /* 221 */
      { "sdtrh", "Std dev of time tend of rel humt", "%" },
      /* 222 */
      { "5wavh", "5-wave geopotential height", "gpm" },
      /* 223 */
      { "cwat", "Plant canopy surface water", "kg/m^2" },
      /* 224 */
      { "pltrs", "Maximum stomato plant resistance", "s/m" },
      /* 225 */
      { "rhcld", "RH-type cloud cover", "%" },
      /* 226 */
      { "bmixl", "Blackadar's mixing length scale", "m" },
      /* 227 */
      { "amixl", "Asymptotic mixing length scale", "m" },
      /* 228 */
      { "pevap", "Potential evaporation", "kg^2" },
      /* 229 */
      { "snohf", "Snow melt heat flux", "W/m^2" },
      /* 230 */
      { "snoev", "Snow sublimation heat flux", "W/m^2" },
      /* 231 */
      { "mflux", "Convective cloud mass flux", "Pa/s" },
      /* 232 */
      { "dtrf", "Downward total radiation flux", "W/m^2" },
      /* 233 */
      { "utrf", "Upward total radiation flux", "W/m^2" },
      /* 234 */
      { "bgrun", "Baseflow-groundwater runoff", "kg/m^2" },
      /* 235 */
      { "ssrun", "Storm surface runoff", "kg/m^2" },
      /* 236 */
      { "var236", "undefined", "undefined" },
      /* 237 */
      { "ozone", "Total column ozone concentration", "Dobson" },
      /* 238 */
      { "snoc", "Snow cover", "%" },
      /* 239 */
      { "snot", "Snow temperature", "K" },
      /* 240 */
      { "glcr", "Permanent snow points", "mask" },
      /* 241 */
      { "lrghr", "Large scale condensation heating rate", "K/s" },
      /* 242 */
      { "cnvhr", "Deep convective heating rate", "K/s" },
      /* 243 */
      { "cnvmr", "Deep convective moistening rate", "kg/kg/s" },
      /* 244 */
      { "shahr", "Shallow convective heating rate", "K/s" },
      /* 245 */
      { "shamr", "Shallow convective moistening rate", "kg/kg/s" },
      /* 246 */
      { "vdfhr", "Vertical diffusion heating rate", "K/s" },
      /* 247 */
      { "vdfua", "Vertical diffusion zonal accel", "m/s/s" },
      /* 248 */
      { "vdfva", "Vertical diffusion meridional accel", "m/s/s" },
      /* 249 */
      { "vdfmr", "Vertical diffusion moistening rate", "kg/kg/s" },
      /* 250 */
      { "swhr", "Solar radiative heating rate", "K/s" },
      /* 251 */
      { "lwhr", "Longwave radiative heating rate", "K/s" },
      /* 252 */
      { "cd", "Drag coefficient", "" },
      /* 253 */
      { "fricv", "Friction velocity", "m/s" },
      /* 254 */
      { "ri", "Richardson number", "" },
      /* 255 */
      { "var255", "undefined", "undefined" } };
    int npar = defaulttable_ncep_reanal2.length;
    //assert npar <= NPARAMETERS;
    GribPDSParameter[] parameters = new GribPDSParameter[npar];
    for (int n = 0; n < npar; ++n)
    {
      String pname = defaulttable_ncep_reanal2[n][0];
      String pdesc = defaulttable_ncep_reanal2[n][1];
      String punit = defaulttable_ncep_reanal2[n][2];
      parameters[n] = new GribPDSParameter(n, pname, pdesc, punit);
    }

    aTables.add(new GribPDSParamTable("ncep_reanal2.1", 7, -1, 1, parameters));
    aTables.add(new GribPDSParamTable("ncep_reanal2.2", 7, -1, 2, parameters));
    aTables.add(new GribPDSParamTable("ncep_reanal2.3", 7, -1, 3, parameters));
    aTables.add(new GribPDSParamTable("ncep_reanal2.4", 81, -1, 3, parameters));
    aTables.add(new GribPDSParamTable("ncep_reanal2.5", 88, -1, 2, parameters));
    aTables.add(new GribPDSParamTable("ncep_reanal2.6", 88, -1, 128, parameters));
  }


  /**
   * @param aFileUrl
   * @param aTables
   * @throws IOException
   * @throws NotSupportedException
   */
  protected static void readTableEntry(URL aFileUrl, List<GribPDSParamTable> aTables)
    throws IOException, NotSupportedException
  {
    // System.out.println("JGRIB: readTableEntry: aFileUrl = "+aFileUrl.toString());
    InputStreamReader isr = new InputStreamReader(aFileUrl.openStream());
    BufferedReader br = new BufferedReader(isr);

    String line = br.readLine();
    if (line.length() == 0 || line.startsWith("//"))
    {
      throw new NotSupportedException("Gribtab files cannot start with blanks " +
                                      "or comments, - Please follow standard (-1:center:subcenter:tablenumber)");
    }
    GribPDSParamTable table = new GribPDSParamTable();
    String[] tableDefArr = SmartStringArray.split(":", line);
    table.center_id = Integer.parseInt(tableDefArr[1].trim());
    table.subcenter_id = Integer.parseInt(tableDefArr[2].trim());
    table.table_number = Integer.parseInt(tableDefArr[3].trim());
    table.filename = aFileUrl.toExternalForm();
    table.url = aFileUrl;

    aTables.add(table);

    br.close();
    isr.close();
  }


  /**
   * @param aBaseUrl
   * @param aTables
   * @throws IOException
   */
  private static void readTableEntries(String aBaseUrl, List<GribPDSParamTable> aTables)
    throws IOException
  {

    // Open file
    if ("true".equals(System.getProperty("grib.verbose", "false"))) {
      System.out.println("JGRIB: readTableEntries: aBaseUrl =" + aBaseUrl);
    }
    InputStream is = new URL(aBaseUrl + "/" + TABLE_LIST).openStream(); // TABLE_LIST is tablelookup.lst
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    String line;
    while ((line = br.readLine()) != null)
    {
      // Skip blank lines and comment lines (//)
      if (line.length() == 0 || line.startsWith("//"))
        continue;
      GribPDSParamTable table = new GribPDSParamTable();
      int cix = line.indexOf("//"); //comment index
      if (cix > 0)
      {
        line = line.substring(0, cix);
      }
      String[] tableDefArr = SmartStringArray.split(":", line);
      if (tableDefArr == null || tableDefArr.length < 4)
        continue;
      table.center_id = Integer.parseInt(tableDefArr[0].trim());
      table.subcenter_id = Integer.parseInt(tableDefArr[1].trim());
      table.table_number = Integer.parseInt(tableDefArr[2].trim());
      table.filename = tableDefArr[3].trim();
      table.url = new URL(aBaseUrl + "/" + table.filename);

      aTables.add(table);
    }
    is.close();

  }


  /**
   * Looks for the parameter table which matches the center, subcenter
   *    and table version from the tables array.
   * If this is the first time asking for this table, then the parameters for
   *    this table have not been read in yet, so this is done as well.
   *
   * @param center - integer from PDS octet 5, representing Center.
   * @param subcenter - integer from PDS octet 26, representing Subcenter
   * @param number - integer from PDS octet 4, representing Parameter Table Version
   * @return GribPDSParamTable matching center, subcenter, and number
   * @throws NotSupportedException
   */
  public static GribPDSParamTable getParameterTable(int center, int subcenter, int number)
    throws NotSupportedException
  {
    /* 1) search excact match                   (center, table)
         2) if (1) failed, search matching table  ( - ,table(1..3))
      */
    for (int i = paramTables.length - 1; i >= 0; i--)
    {
      //      for (int i=0; i < paramTables.length; i++){
      GribPDSParamTable table = paramTables[i];
      if (table.center_id == -1)
        continue;
      if (center == table.center_id)
      {
        if (table.subcenter_id == -1 || subcenter == table.subcenter_id)
        {
          if (number == table.table_number)
          {
            // now that this table is being used, check to see if the
            //   parameters for this table have been read in yet.
            // If not, initialize table and read them in now.
            table.readParameterTable();
            return table;
          }
        }
      }
    }
    //search matching table  ( - ,table(1..3))
    for (int i = paramTables.length - 1; i >= 0; i--)
    {
      //      for (int i = 0; i < paramTables.length; i++){
      GribPDSParamTable table = paramTables[i];
      if (table.center_id == -1 && number == table.table_number)
      {
        table.readParameterTable();
        return table;
      }
    }

    throw new NotSupportedException("Grib table not supported; center " + center + ", sub " + subcenter + ", table " + number);

  //System.out.println("cent, sub, tab: "+center+" "+subcenter+" "+number);
  //return null;
  }

  /**
   * Get the parameter with id <tt>id</tt>.
   * @param id
   * @return description of the unit for the parameter
   */
  public GribPDSParameter getParameter(int id)
  {
    if (id < 0 || id >= NPARAMETERS)
      throw new IllegalArgumentException("Bad id: " + id);
    if (parameters[id] == null)
      return new GribPDSParameter(id, "undef_" + id, "undef", "undef");
    return parameters[id];
  }

  /**
   * Get the tag/name of the parameter with id <tt>id</tt>.
   *
   * @param id
   * @return tag/name of the parameter
   */
  public String getParameterTag(int id)
  {
    return getParameter(id).getName();
  }

  /**
   * Get a description for the parameter with id <tt>id</tt>.
   *
   * @param id
   * @return description for the parameter
   */
  public String getParameterDescription(int id)
  {
    return getParameter(id).getDescription();
  }

  /**
   * Get a description for the unit with id <tt>id</tt>.
   *
   * @param id
   * @return description of the unit for the parameter
   */
  public String getParameterUnit(int id)
  {
    return getParameter(id).getUnit();
  }

  /**
   * @param pdsPar
   * @return true/false
   */
  private boolean setParameter(GribPDSParameter pdsPar)
  {
    if (pdsPar == null)
      return false;
    int id = pdsPar.getNumber();
    if (id < 0 || id >= NPARAMETERS)
      return false;
    parameters[id] = pdsPar;
    return true;
  }

  /**
   *
   * @return center_id
   */
  public int getCenter_id()
  {
    return center_id;
  }

  /**
   *
   * @return subcenter_id
   */
  public int getSubcenter_id()
  {
    return subcenter_id;
  }

  /**
   *
   * @return table number
   */
  public int getTable_number()
  {
    return table_number;
  }

  /**
   *
   * @return filename
   */
  public String getFilename()
  {
    return filename;
  }

  /**
   *
   * @return url to file
   */
  public URL getUrl()
  {
    return url;
  }

  /**
   * Read parameter table
   */
  //public void readParameterTable(String aFileName)
  private void readParameterTable()
    //         throws IOException
  {

    if (this.parameters != null)
      return;
    parameters = new GribPDSParameter[NPARAMETERS];

    int center;
    int subcenter;
    int number;

    try
    {

      BufferedReader br;
      if (filename != null && filename.length() > 0)
      {
        GribPDSParamTable tab = fileTabMap.get(filename);
        if (tab != null)
        {
          this.parameters = tab.parameters;
          return;
        }
      }
      if (url != null)
      {
        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
      }
      else
      {
        br = new BufferedReader(new FileReader("tables\\" + filename));
      }


      // Read first
      String line = br.readLine();
      String[] tableDefArr = SmartStringArray.split(":", line);

      center = Integer.parseInt(tableDefArr[1].trim());
      subcenter = Integer.parseInt(tableDefArr[2].trim());
      number = Integer.parseInt(tableDefArr[3].trim());
      //      if (center != center_id && subcenter != subcenter_id &&
      //         number!= table_number){
      //         throw new java.io.IOException("parameter table header values do not " +
      //               " match values in GRIB file.  Possible error in lookup table.");
      //      }

      //int i=0;  // peg - Variable never used
      // rdg - added the 0 line length check to cover the case of blank lines at
      //       the end of the parameter table file.
      while ((line = br.readLine()) != null)
      {
        line = line.trim();
        if (line.length() == 0 || line.startsWith("//"))
          continue;

        GribPDSParameter parameter = new GribPDSParameter();
        tableDefArr = SmartStringArray.split(":", line);
        parameter.number = Integer.parseInt(tableDefArr[0].trim());
        parameter.name = tableDefArr[1].trim();
        // check to see if unit defined, if not, parameter is undefined
        if (tableDefArr[2].indexOf('[') == -1)
        {
          // Undefined unit
          parameter.description = parameter.unit = tableDefArr[2].trim();
        }
        else
        {
          String[] arr2 = SmartStringArray.split("[", tableDefArr[2]);
          parameter.description = arr2[0].trim();
          // Remove "]"
          parameter.unit = arr2[1].substring(0, arr2[1].lastIndexOf(']')).trim();
          //            parameter.unit = arr2[1].substring(0, arr2[1].lastIndexOf(']')).trim();
        }
        //this.parameters[i++]=parameter;
        if (!this.setParameter(parameter))
        {
          System.err.println("Warning, bad parameter ignored (" + filename + "): " + parameter.toString());
        }
      }
      if (filename != null && filename.length() > 0)
      {
        GribPDSParamTable loadedTable = new GribPDSParamTable(filename, center, subcenter, number, this.parameters);
        fileTabMap.put(filename, loadedTable);
      }
    }
    catch (IOException ioError)
    {
      System.err.println("An error occurred in GribPDSParamTable while " + "trying to open the parameter table " + filename +
                         " : " + ioError);
    }
  }

  /**
   * Overrides Object.toString()
   *
   * @see java.lang.Object#toString()
   * @return String representation of the object
   */
  public String toString()
  {
    StringBuffer str = new StringBuffer();
    str.append("-1:" + center_id + ":" + subcenter_id + ":" + table_number + "\n");
    if (parameters != null)
    {
      for (int i = 0; i < parameters.length; i++)
      {
        if (parameters[i] == null)
          continue;
        str.append(parameters[i].toString() + "\n");
      }
    }
    return str.toString();
  }

}

