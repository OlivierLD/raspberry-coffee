/**
 * ===============================================================================
 * $Id: GribTables.java,v 1.6 2006/07/25 13:46:23 frv_peg Exp $
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
 * GribTables.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 * Updated Kjell RXang, 18/03/2002
 */

package jgrib;



/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.
 *
 * @deprecated - Implementation of tables moved to GribPDSParamTable,
 *   GribPDSParameter, and GribPDSLevel classes.
 */

@Deprecated
public class GribTables
{

//   /**
//    * Identification of center e.g. 88 for Oslo
//    */
//   protected static int center_id;
//
//   /**
//    * Parameter Table Version number, currently 3 for international exchange.
//    */
//   protected static int paramter_table;

   /**
    * Get a description for the level code.
    *
    * @param pds9  part 1 of leve code
    * @param pds10 part 2 of level code
    * @param pds11 part 3 of level code
    *
    * @return description of the level
    */
   public static String getLevel(int pds9, int pds10, int pds11)
   {

      int pds1011 = pds10 << 8 | pds11;

      switch (pds9)
      {

         case 1:
            return "surface";
         case 2:
            return "cloud base level";
         case 3:
            return "cloud top level";
         case 4:
            return "0 degree isotherm level";
         case 5:
            return "condensation level";
         case 6:
            return "maximum wind speed level";
         case 7:
            return "tropopause level";
         case 8:
            return "nominal atmosphere top";
         case 9:
            return "sea bottom";

         case 100:
            return pds1011 + " mb";
         case 101:
            return pds10 + "-" + pds11 + " mb";
         case 102:
            return "mean sea level";
         case 103:
            return pds1011 + " m above mean sea level";
         case 104:
            return (pds10 * 100) + "-" + (pds11 * 100) + " m above mean sea level";
         case 105:
            return pds1011 + " m above ground";
         case 106:
            return (pds10 * 100) + "-" + (pds11 * 100) + " m above ground";
         case 107:
            return "sigma=" + (pds1011 / 10000.0);
         case 108:
            return "sigma " + (pds10 / 100.0) + "-" + (pds11 / 100.0);
         case 109:
            return "hybrid level " + pds1011;
         case 110:
            return "hybrid " + pds10 + "-" + pds11;
         case 111:
            return pds1011 + " cm below ground";
         case 112:
            return pds10 + "-" + pds11 + " cm down";
         case 113:
            return pds1011 + " K";
         case 125:
            return pds1011 + " cm above ground";
         case 160:
            return pds1011 + " m below sea level";

         case 200:
            return "entire atmoshere layer";
         case 201:
            return "entire ocean layer";

         default:
            return "";
      }

   }

// rdg - implemented in GribPDSParameter - need to delete
//   /**
//    * Get the tag/name of the parameter with id <tt>id</tt>.
//    *
//    * @return tag/name of the parameter
//    */
//   public static String getParameterTag(int id)
//   {
//
//      return paramtable_ncep_opn[id][0];
//   }


// rdg - implemented in GribPDSParameter - need to delete
//   /**
//    * Get a description for the parameter with id <tt>id</tt>.
//    *
//    * @return description for the parameter
//    */
//   public static String getParameterDescription(int id)
//   {
//
//      return paramtable_ncep_opn[id][1];
//   }

// rdg - implemented in GribPDSParameter - need to delete
//   /**
//    * Get a description for the unit with id <tt>id</tt>.
//    *
//    * @return description of the unit for the parameter
//    */
//   public static String getParameterUnit(int id)
//   {
//
//      return paramtable_ncep_opn[id][2];
//   }

// rdg - implemented in GribPDSParamTable - need to delete
//   /**
//    * Read paramter table
//    * @param aFileName Name of input file
//    */
//   public static void readParamterTable(String aFileName)
//         throws IOException
//   {
//      ArrayList paraTable = new ArrayList(300);
//      BufferedReader br = new BufferedReader(new FileReader(aFileName));
//      // Read first
//      String line = br.readLine();
//      String[] tableDefArr = SmartStringArray.split(":", line);
//
//      center_id = Integer.parseInt(tableDefArr[1].trim());
//      paramter_table = Integer.parseInt(tableDefArr[3].trim());
//
//      int maxInd = 0;
//      while ((line = br.readLine()) != null)
//      {
//         int end = line.indexOf(':');
//         String indStr = line.substring(0, end).trim();
//         int index = Integer.parseInt(indStr);
//         maxInd = Math.max(maxInd, index);
//         paraTable.add(line);
//      }
//
//      String params[][] = decodeParamters(paraTable, maxInd);
//      paramtable_ncep_opn = params;
//
//   }


// rdg - implemented in GribPDSParamTable - need to delete
//   /**
//    * Decode paramters
//    *
//    */
//   private static String[][] decodeParamters(List aList, int aMaxInd)
//   {
//      String[][] params = new String[aMaxInd + 1][3];
//      for (int i = 0; i < aList.size(); i++)
//      {
//         String[] arr1 = SmartStringArray.split(":", (String) aList.get(i));
//         int index = Integer.parseInt(arr1[0].trim());
//         String type = arr1[1].trim().toLowerCase();
//         String desc = null;
//         String unit = null;
//         // Check if "[" is used
//         if (arr1[2].indexOf('[') == -1)
//         {
//            // Undefined variable
//            unit = desc = arr1[2].trim();
//         }
//         else
//         {
//            String[] arr2 = SmartStringArray.split("[", arr1[2]);
//            desc = arr2[0];
//            // Remove "]"
//            unit = arr2[1].substring(0, arr2[1].lastIndexOf(']')).trim();
//         }
//
//         // Set data
//         String[] setArr = params[index];
//         setArr[0] = type;
//         setArr[1] = desc;
//         setArr[2] = unit;
//      }
//
//
//      return params;
//   }

// rdg - implemented through GribPDSParamTable - need to delete
//   /*
//    * parameter table for NCEP (operations)
//    * center = 7, subcenter != 2 parameter table = 1, 2, 3 etc
//    */
//
//   protected static String[][] paramtable_ncep_opn = {
//
//      /*   0 */   {"var0", "undefined", "undefined"},
//                  /*   1 */   {"pres", "Pressure", "Pa"},
//                  /*   2 */   {"prmsl", "Pressure reduced to MSL", "Pa"},
//                  /*   3 */   {"ptend", "Pressure tendency", "Pa/s"},
//                  /*   4 */   {"var4", "undefined", "undefined"},
//                  /*   5 */   {"var5", "undefined", "undefined"},
//                  /*   6 */   {"gp", "Geopotential", "m^2/s^2"},
//                  /*   7 */   {"hgt", "Geopotential height", "gpm"},
//                  /*   8 */   {"dist", "Geometric height", "m"},
//                  /*   9 */   {"hstdv", "Std dev of height", "m"},
//                  /*  10 */   {"hvar", "Varianance of height", "m^2"},
//                  /*  11 */   {"tmp", "Temperature", "K"},
//                  /*  12 */   {"vtmp", "Virtual temperature", "K"},
//                  /*  13 */   {"pot", "Potential temperature", "K"},
//                  /*  14 */   {"epot", "Pseudo-adiabatic pot. temperature", "K"},
//                  /*  15 */   {"tmax", "Max. temperature", "K"},
//                  /*  16 */   {"tmin", "Min. temperature", "K"},
//                  /*  17 */   {"dpt", "Dew point temperature", "K"},
//                  /*  18 */   {"depr", "Dew point depression", "K"},
//                  /*  19 */   {"lapr", "Lapse rate", "K/m"},
//                  /*  20 */   {"visib", "Visibility", "m"},
//                  /*  21 */   {"rdsp1", "Radar spectra (1)", ""},
//                  /*  22 */   {"rdsp2", "Radar spectra (2)", ""},
//                  /*  23 */   {"rdsp3", "Radar spectra (3)", ""},
//                  /*  24 */   {"var24", "undefined", "undefined"},
//                  /*  25 */   {"tmpa", "Temperature anomaly", "K"},
//                  /*  26 */   {"presa", "Pressure anomaly", "Pa"},
//                  /*  27 */   {"gpa", "Geopotential height anomaly", "gpm"},
//                  /*  28 */   {"wvsp1", "Wave spectra (1)", ""},
//                  /*  29 */   {"wvsp2", "Wave spectra (2)", ""},
//                  /*  30 */   {"wvsp3", "Wave spectra (3)", ""},
//                  /*  31 */   {"wdir", "Wind direction", "deg"},
//                  /*  32 */   {"wind", "Wind speed", "m/s"},
//                  /*  33 */   {"ugrd", "u wind", "m/s"},
//                  /*  34 */   {"vgrd", "v wind", "m/s"},
//                  /*  35 */   {"strm", "Stream function", "m^2/s"},
//                  /*  36 */   {"vpot", "Velocity potential", "m^2/s"},
//                  /*  37 */   {"mntsf", "Montgomery stream function", "m^2/s^2"},
//                  /*  38 */   {"sgcvv", "Sigma coord. vertical velocity", "/s"},
//                  /*  39 */   {"vvel", "Pressure vertical velocity", "Pa/s"},
//                  /*  40 */   {"dzdt", "Geometric vertical velocity", "m/s"},
//                  /*  41 */   {"absv", "Absolute vorticity", "/s"},
//                  /*  42 */   {"absd", "Absolute divergence", "/s"},
//                  /*  43 */   {"relv", "Relative vorticity", "/s"},
//                  /*  44 */   {"reld", "Relative divergence", "/s"},
//                  /*  45 */   {"vucsh", "Vertical u shear", "/s"},
//                  /*  46 */   {"vvcsh", "Vertical v shear", "/s"},
//                  /*  47 */   {"dirc", "Direction of current", "deg"},
//                  /*  48 */   {"spc", "Speed of current", "m/s"},
//                  /*  49 */   {"uogrd", "u of current", "m/s"},
//                  /*  50 */   {"vogrd", "v of current", "m/s"},
//                  /*  51 */   {"spfh", "Specific humidity", "kg/kg"},
//                  /*  52 */   {"rh", "Relative humidity", "%"},
//                  /*  53 */   {"mixr", "Humidity mixing ratio", "kg/kg"},
//                  /*  54 */   {"pwat", "Precipitable water", "kg/m^2"},
//                  /*  55 */   {"vapp", "Vapor pressure", "Pa"},
//                  /*  56 */   {"satd", "Saturation deficit", "Pa"},
//                  /*  57 */   {"evp", "Evaporation", "kg/m^2"},
//                  /*  58 */   {"cice", "Cloud Ice", "kg/m^2"},
//                  /*  59 */   {"prate", "Precipitation rate", "kg/m^2/s"},
//                  /*  60 */   {"tstm", "Thunderstorm probability", "%"},
//                  /*  61 */   {"apcp", "Total precipitation", "kg/m^2"},
//                  /*  62 */   {"ncpcp", "Large scale precipitation", "kg/m^2"},
//                  /*  63 */   {"acpcp", "Convective precipitation", "kg/m^2"},
//                  /*  64 */   {"srweq", "Snowfall rate water equiv.", "kg/m^2/s"},
//                  /*  65 */   {"weasd", "Accum. snow", "kg/m^2"},
//                  /*  66 */   {"snod", "Snow depth", "m"},
//                  /*  67 */   {"mixht", "Mixed layer depth", "m"},
//                  /*  68 */   {"tthdp", "Transient thermocline depth", "m"},
//                  /*  69 */   {"mthd", "Main thermocline depth", "m"},
//                  /*  70 */   {"mtha", "Main thermocline anomaly", "m"},
//                  /*  71 */   {"tcdc", "Total cloud cover", "%"},
//                  /*  72 */   {"cdcon", "Convective cloud cover", "%"},
//                  /*  73 */   {"lcdc", "Low level cloud cover", "%"},
//                  /*  74 */   {"mcdc", "Mid level cloud cover", "%"},
//                  /*  75 */   {"hcdc", "High level cloud cover", "%"},
//                  /*  76 */   {"cwat", "Cloud water", "kg/m^2"},
//                  /*  77 */   {"var77", "undefined", "undefined"},
//                  /*  78 */   {"snoc", "Convective snow", "kg/m^2"},
//                  /*  79 */   {"snol", "Large scale snow", "kg/m^2"},
//                  /*  80 */   {"wtmp", "Water temperature", "K"},
//                  /*  81 */   {"land", "Land cover (land=1;sea=0)", "fraction"},
//                  /*  82 */   {"dslm", "Deviation of sea level from mean", "m"},
//                  /*  83 */   {"sfcr", "Surface roughness", "m"},
//                  /*  84 */   {"albdo", "Albedo", "%"},
//                  /*  85 */   {"tsoil", "Soil temperature", "K"},
//                  /*  86 */   {"soilm", "Soil moisture content", "kg/m^2"},
//                  /*  87 */   {"veg", "Vegetation", "%"},
//                  /*  88 */   {"salty", "Salinity", "kg/kg"},
//                  /*  89 */   {"den", "Density", "kg/m^3"},
//                  /*  90 */   {"runof", "Runoff", "kg/m^2"},
//                  /*  91 */   {"icec", "Ice concentration (ice=1;no ice=0)", "fraction"},
//                  /*  92 */   {"icetk", "Ice thickness", "m"},
//                  /*  93 */   {"diced", "Direction of ice drift", "deg"},
//                  /*  94 */   {"siced", "Speed of ice drift", "m/s"},
//                  /*  95 */   {"uice", "u of ice drift", "m/s"},
//                  /*  96 */   {"vice", "v of ice drift", "m/s"},
//                  /*  97 */   {"iceg", "Ice growth rate", "m/s"},
//                  /*  98 */   {"iced", "Ice divergence", "/s"},
//                  /*  99 */   {"snom", "Snow melt", "kg/m^2"},
//                  /* 100 */   {"htsgw", "Sig height of wind waves and swell", "m"},
//                  /* 101 */   {"wvdir", "Direction of wind waves", "deg"},
//                  /* 102 */   {"wvhgt", "Sig height of wind waves", "m"},
//                  /* 103 */   {"wvper", "Mean period of wind waves", "s"},
//                  /* 104 */   {"swdir", "Direction of swell waves", "deg"},
//                  /* 105 */   {"swell", "Sig height of swell waves", "m"},
//                  /* 106 */   {"swper", "Mean period of swell waves", "s"},
//                  /* 107 */   {"dirpw", "Primary wave direction", "deg"},
//                  /* 108 */   {"perpw", "Primary wave mean period", "s"},
//                  /* 109 */   {"dirsw", "Secondary wave direction", "deg"},
//                  /* 110 */   {"persw", "Secondary wave mean period", "s"},
//                  /* 111 */   {"nswrs", "Net short wave (surface)", "W/m^2"},
//                  /* 112 */   {"nlwrs", "Net long wave (surface)", "W/m^2"},
//                  /* 113 */   {"nswrt", "Net short wave (top)", "W/m^2"},
//                  /* 114 */   {"nlwrt", "Net long wave (top)", "W/m^2"},
//                  /* 115 */   {"lwavr", "Long wave", "W/m^2"},
//                  /* 116 */   {"swavr", "Short wave", "W/m^2"},
//                  /* 117 */   {"grad", "Global radiation", "W/m^2"},
//                  /* 118 */   {"var118", "undefined", "undefined"},
//                  /* 119 */   {"var119", "undefined", "undefined"},
//                  /* 120 */   {"var120", "undefined", "undefined"},
//                  /* 121 */   {"lhtfl", "Latent heat flux", "W/m^2"},
//                  /* 122 */   {"shtfl", "Sensible heat flux", "W/m^2"},
//                  /* 123 */   {"blydp", "Boundary layer dissipation", "W/m^2"},
//                  /* 124 */   {"uflx", "Zonal momentum flux", "N/m^2"},
//                  /* 125 */   {"vflx", "Meridional momentum flux", "N/m^2"},
//                  /* 126 */   {"wmixe", "Wind mixing energy", "J"},
//                  /* 127 */   {"imgd", "Image data", ""},
//                  /* 128 */   {"mslsa", "Mean sea level pressure (Std Atm)", "Pa"},
//                  /* 129 */   {"mslma", "Mean sea level pressure (MAPS)", "Pa"},
//                  /* 130 */   {"mslet", "Mean sea level pressure (ETA model)", "Pa"},
//                  /* 131 */   {"lftx", "Surface lifted index", "K"},
//                  /* 132 */   {"4lftx", "Best (4-layer) lifted index", "K"},
//                  /* 133 */   {"kx", "K index", "K"},
//                  /* 134 */   {"sx", "Sweat index", "K"},
//                  /* 135 */   {"mconv", "Horizontal moisture divergence", "kg/kg/s"},
//                  /* 136 */   {"vssh", "Vertical speed shear", "1/s"},
//                  /* 137 */   {"tslsa", "3-hr pressure tendency (Std Atmos Red)", "Pa/s"},
//                  /* 138 */   {"bvf2", "Brunt-Vaisala frequency^2", "1/s^2"},
//                  /* 139 */   {"pvmw", "Potential vorticity (mass-weighted)", "1/s/m"},
//                  /* 140 */   {"crain", "Categorical rain", "yes=1;no=0"},
//                  /* 141 */   {"cfrzr", "Categorical freezing rain", "yes=1;no=0"},
//                  /* 142 */   {"cicep", "Categorical ice pellets", "yes=1;no=0"},
//                  /* 143 */   {"csnow", "Categorical snow", "yes=1;no=0"},
//                  /* 144 */   {"soilw", "Volumetric soil moisture", "fraction"},
//                  /* 145 */   {"pevpr", "Potential evaporation rate", "W/m^2"},
//                  /* 146 */   {"cwork", "Cloud work function", "J/kg"},
//                  /* 147 */   {"u-gwd", "Zonal gravity wave stress", "N/m^2"},
//                  /* 148 */   {"v-gwd", "Meridional gravity wave stress", "N/m^2"},
//                  /* 149 */   {"pvort", "Potential vorticity", "m^2/s/kg"},
//                  /* 150 */   {"var150", "undefined", "undefined"},
//                  /* 151 */   {"var151", "undefined", "undefined"},
//                  /* 152 */   {"var152", "undefined", "undefined"},
//                  /* 153 */   {"mfxdv", "Moisture flux divergence", "gr/gr*m/s/m"},
//                  /* 154 */   {"vqr154", "undefined", "undefined"},
//                  /* 155 */   {"gflux", "Ground heat flux", "W/m^2"},
//                  /* 156 */   {"cin", "Convective inhibition", "J/kg"},
//                  /* 157 */   {"cape", "Convective Avail. Pot. Energy", "J/kg"},
//                  /* 158 */   {"tke", "Turbulent kinetic energy", "J/kg"},
//                  /* 159 */   {"condp", "Lifted parcel condensation pressure", "Pa"},
//                  /* 160 */   {"csusf", "Clear sky upward solar flux", "W/m^2"},
//                  /* 161 */   {"csdsf", "Clear sky downward solar flux", "W/m^2"},
//                  /* 162 */   {"csulf", "Clear sky upward long wave flux", "W/m^2"},
//                  /* 163 */   {"csdlf", "Clear sky downward long wave flux", "W/m^2"},
//                  /* 164 */   {"cfnsf", "Cloud forcing net solar flux", "W/m^2"},
//                  /* 165 */   {"cfnlf", "Cloud forcing net long wave flux", "W/m^2"},
//                  /* 166 */   {"vbdsf", "Visible beam downward solar flux", "W/m^2"},
//                  /* 167 */   {"vddsf", "Visible diffuse downward solar flux", "W/m^2"},
//                  /* 168 */   {"nbdsf", "Near IR beam downward solar flux", "W/m^2"},
//                  /* 169 */   {"nddsf", "Near IR diffuse downward solar flux", "W/m^2"},
//                  /* 170 */   {"ustr", "U wind stress", "N/m^2"},
//                  /* 171 */   {"vstr", "V wind stress", "N/m^2"},
//                  /* 172 */   {"mflx", "Momentum flux", "N/m^2"},
//                  /* 173 */   {"lmh", "Mass point model surface", ""},
//                  /* 174 */   {"lmv", "Velocity point model surface", ""},
//                  /* 175 */   {"sglyr", "Neraby model level", ""},
//                  /* 176 */   {"nlat", "Latitude", "deg"},
//                  /* 177 */   {"nlon", "Longitude", "deg"},
//                  /* 178 */   {"umas", "Mass weighted u", "gm/m*K*s"},
//                  /* 179 */   {"vmas", "Mass weigtted v", "gm/m*K*s"},
//                  /* 180 */   {"var180", "undefined", "undefined"},
//                  /* 181 */   {"lpsx", "x-gradient of log pressure", "1/m"},
//                  /* 182 */   {"lpsy", "y-gradient of log pressure", "1/m"},
//                  /* 183 */   {"hgtx", "x-gradient of height", "m/m"},
//                  /* 184 */   {"hgty", "y-gradient of height", "m/m"},
//                  /* 185 */   {"stdz", "Standard deviation of Geop. hgt.", "m"},
//                  /* 186 */   {"stdu", "Standard deviation of zonal wind", "m/s"},
//                  /* 187 */   {"stdv", "Standard deviation of meridional wind", "m/s"},
//                  /* 188 */   {"stdq", "Standard deviation of spec. hum.", "gm/gm"},
//                  /* 189 */   {"stdt", "Standard deviation of temperature", "K"},
//                  /* 190 */   {"cbuw", "Covariance between u and omega", "m/s*Pa/s"},
//                  /* 191 */   {"cbvw", "Covariance between v and omega", "m/s*Pa/s"},
//                  /* 192 */   {"cbuq", "Covariance between u and specific hum", "m/s*gm/gm"},
//                  /* 193 */   {"cbvq", "Covariance between v and specific hum", "m/s*gm/gm"},
//                  /* 194 */   {"cbtw", "Covariance between T and omega", "K*Pa/s"},
//                  /* 195 */   {"cbqw", "Covariance between spec. hum and omeg", "gm/gm*Pa/s"},
//                  /* 196 */   {"cbmzw", "Covariance between v and u", "m^2/si^2"},
//                  /* 197 */   {"cbtzw", "Covariance between u and T", "K*m/s"},
//                  /* 198 */   {"cbtmw", "Covariance between v and T", "K*m/s"},
//                  /* 199 */   {"stdrh", "Standard deviation of Rel. Hum.", "%"},
//                  /* 200 */   {"sdtz", "Std dev of time tend of geop. hgt", "m"},
//                  /* 201 */   {"icwat", "Ice-free water surface", "%"},
//                  /* 202 */   {"sdtu", "Std dev of time tend of zonal wind", "m/s"},
//                  /* 203 */   {"sdtv", "Std dev of time tend of merid wind", "m/s"},
//                  /* 204 */   {"dswrf", "Downward solar radiation flux", "W/m^2"},
//                  /* 205 */   {"dlwrf", "Downward long wave radiation flux", "W/m^2"},
//                  /* 206 */   {"sdtq", "Std dev of time tend of spec. hum", "gm/gm"},
//                  /* 207 */   {"mstav", "Moisture availability", "%"},
//                  /* 208 */   {"sfexc", "Exchange coefficient", "(kg/m^3)(m/s)"},
//                  /* 209 */   {"mixly", "No. of mixed layers next to surface", "integer"},
//                  /* 210 */   {"sdtt", "Std dev of time tend of temperature", "K"},
//                  /* 211 */   {"uswrf", "Upward short wave flux", "W/m^2"},
//                  /* 212 */   {"ulwrf", "Upward long wave flux", "W/m^2"},
//                  /* 213 */   {"cdlyr", "Non-convective cloud", "%"},
//                  /* 214 */   {"cprat", "Convective precip. rate", "kg/m^2/s"},
//                  /* 215 */   {"ttdia", "Temperature tendency by all physics", "K/s"},
//                  /* 216 */   {"ttrad", "Temperature tendency by all radiation", "K/s"},
//                  /* 217 */   {"ttphy", "Temperature tendency by non-radiation physics", "K/s"},
//                  /* 218 */   {"preix", "Precip index (0.0-1.00)", "fraction"},
//                  /* 219 */   {"tsd1d", "Std. dev. of IR T over 1x1 deg area", "K"},
//                  /* 220 */   {"nlgsp", "Natural log of surface pressure", "ln(kPa)"},
//                  /* 221 */   {"sdtrh", "Std dev of time tend of rel humt", "%"},
//                  /* 222 */   {"5wavh", "5-wave geopotential height", "gpm"},
//                  /* 223 */   {"cwat", "Plant canopy surface water", "kg/m^2"},
//                  /* 224 */   {"pltrs", "Maximum stomato plant resistance", "s/m"},
//                  /* 225 */   {"rhcld", "RH-type cloud cover", "%"},
//                  /* 226 */   {"bmixl", "Blackadar's mixing length scale", "m"},
//                  /* 227 */   {"amixl", "Asymptotic mixing length scale", "m"},
//                  /* 228 */   {"pevap", "Potential evaporation", "kg^2"},
//                  /* 229 */   {"snohf", "Snow melt heat flux", "W/m^2"},
//                  /* 230 */   {"snoev", "Snow sublimation heat flux", "W/m^2"},
//                  /* 231 */   {"mflux", "Convective cloud mass flux", "Pa/s"},
//                  /* 232 */   {"dtrf", "Downward total radiation flux", "W/m^2"},
//                  /* 233 */   {"utrf", "Upward total radiation flux", "W/m^2"},
//                  /* 234 */   {"bgrun", "Baseflow-groundwater runoff", "kg/m^2"},
//                  /* 235 */   {"ssrun", "Storm surface runoff", "kg/m^2"},
//                  /* 236 */   {"var236", "undefined", "undefined"},
//                  /* 237 */   {"ozone", "Total column ozone concentration", "Dobson"},
//                  /* 238 */   {"snoc", "Snow cover", "%"},
//                  /* 239 */   {"snot", "Snow temperature", "K"},
//                  /* 240 */   {"glcr", "Permanent snow points", "mask"},
//                  /* 241 */   {"lrghr", "Large scale condensation heating rate", "K/s"},
//                  /* 242 */   {"cnvhr", "Deep convective heating rate", "K/s"},
//                  /* 243 */   {"cnvmr", "Deep convective moistening rate", "kg/kg/s"},
//                  /* 244 */   {"shahr", "Shallow convective heating rate", "K/s"},
//                  /* 245 */   {"shamr", "Shallow convective moistening rate", "kg/kg/s"},
//                  /* 246 */   {"vdfhr", "Vertical diffusion heating rate", "K/s"},
//                  /* 247 */   {"vdfua", "Vertical diffusion zonal accel", "m/s/s"},
//                  /* 248 */   {"vdfva", "Vertical diffusion meridional accel", "m/s/s"},
//                  /* 249 */   {"vdfmr", "Vertical diffusion moistening rate", "kg/kg/s"},
//                  /* 250 */   {"swhr", "Solar radiative heating rate", "K/s"},
//                  /* 251 */   {"lwhr", "Longwave radiative heating rate", "K/s"},
//                  /* 252 */   {"cd", "Drag coefficient", ""},
//                  /* 253 */   {"fricv", "Friction velocity", "m/s"},
//                  /* 254 */   {"ri", "Richardson number", ""},
//                  /* 255 */   {"var255", "undefined", "undifined"},
//
//
//   };

//   public static void main(String... args)
//   {
//
//      String fileName = args[0];
//
//      try
//      {
//         GribTables.readParamterTable(fileName);
//      }
//      catch (IOException e)
//      {
//         System.err.println(e);
//      }
//   }

}

