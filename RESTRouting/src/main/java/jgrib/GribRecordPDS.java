/**
 * ===============================================================================
 * $Id: GribRecordPDS.java,v 1.13 2006/07/27 13:11:27 frv_peg Exp $
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
 * GribRecordPDS.java  1.1  01/01/2001
 *
 * (C) Benjamin Stark
 * Modified by Richard D. Gonzalez - changes:
 *   Parameters use external tables, so program does not have to be modified to
 *      add support for new tables.
 *   @See the GribPDSParameter, GribPDSParamTable, and GribPDSLevel classes.
 *   Added another time field to differentiate between base time and forecast
 *      time.
 *   Started handling Subcenters (though not completed yet)
 *   Added code to handle time offsets (thanks Hans)
 */

package jgrib;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A class representing the product definition section (PDS) of a GRIB record.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class GribRecordPDS
{
   /**
    * Length in bytes of this PDS.
    */
   protected int length;

   /**
    * Exponent of decimal scale.
    */
   protected int decscale;

   /**
    * ID of grid type.
    */
   protected int grid_id;    // no pre-definied grids supported yet.

   /**
    * True, if GDS exists.
    */
   protected boolean gds_exists;

   /**
    * True, if BMS exists.
    */
   protected boolean bms_exists;

//rdg - placed attributes that came from the Parameter Table into a GribPDSParameter
   /**
    * The parameter as defined in the Parameter Table
    */
    protected GribPDSParameter parameter;

   /**
    * Type of parameter.
    */
//   protected String type;

   /**
    * Description of parameter.
    */
//   protected String description;

   /**
    * Name of unit of parameter.
    */
//   protected String unit;

   /**
    * Class containing the information about the level.  This helps to actually
    * use the data, otherwise the string for level will have to be parsed.
    */
   protected GribPDSLevel level;

//   /**
//    * Name of level (height or pressure) the data belong to.
//    */
//   protected String level;

   /**
    * Model Run/Analysis/Reference time.
    *
    */
   protected Calendar baseTime;

   /**
    * Forecast time.
    * Also used as starting time when times represent a period
    */
   protected Calendar forecastTime;

   /**
    * Ending time when times represent a period
    */
   protected Calendar forecastTime2;

   /**
    * String used in building a string to represent the time(s) for this PDS
    * See the decoder for octet 21 to get an understanding
    */
   protected String timeRange = null;

   /**
    * String used in building a string to represent the time(s) for this PDS
    * See the decoder for octet 21 to get an understanding
    */  
   protected String connector = null;


   /**
    * Parameter Table Version number, currently 3 for international exchange.
    */
   private int table_version;

   /**
    * Identification of center e.g. 88 for Oslo
    */
   private int center_id;

// rdg - added the following:
   /**
    * Identification of subcenter
    */
   private int subcenter_id;

   /**
    * Identification of Generating Process
    */
   private int process_id;

   /**
    * rdg - moved the Parameter table information and functionality into a class.
    * See GribPDSParamTable class for details.
    */
   private GribPDSParamTable parameter_table;

   // *** constructors *******************************************************

   /**
    * Constructs a <tt>GribRecordPDS</tt> object from a bit input stream.
    *
    * @param in bit input stream with PDS content
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NotSupportedException 
    */
   public GribRecordPDS(BitInputStream in) throws NotSupportedException, IOException
   {
      int[] data;      // byte buffer
      int offset = 0;
      int offset2 = 0;

      // parameter attributes
      // peg - variables never used
      //int number;
      //String type = null;
      //String description = null;
      //String unit = null;

      //initTables();

      data = in.readUI8(3);

      // octet 1-3 (length of section)
      this.length = Bytes2Number.uint3(data[0], data[1], data[2]);

      // read rest of section
      data = in.readUI8(this.length - 3);

      // Paramter table octet 4
      this.table_version = data[0];

      // Center  octet 5
      this.center_id = data[1];

      // Center  octet 26 - out of order, but needed now
      this.subcenter_id = data[22];

      // Generating Process - See Table A
      this.process_id = data[2];

      // octet 7 (id of grid type) - not supported yet
      this.grid_id = data[3];

      // octet 8 (flag for presence of GDS and BMS)
      this.gds_exists = (data[4] & 128) == 128;
      this.bms_exists = (data[4] & 64) == 64;

      // octet 9 (parameter and unit)
// rdg - this functionality has been replaced by using a GribPDSParameter class
//      this.type = GribTables.getParameterTag(data[5]);
//      this.description = GribTables.getParameterDescription(data[5]);
//      this.unit = GribTables.getParameterUnit(data[5]);

      // Before getting parameter table values, must get the appropriate table
      // for this center, subcenter (not yet implemented) and parameter table.
      this.parameter_table = GribPDSParamTable.getParameterTable(center_id,
                                                 subcenter_id,table_version);

      // octet 9 (parameter and unit)
      this.parameter = parameter_table.getParameter(data[5]);

      // octets 10-12 (level)
//      this.level = GribTables.getLevel(data[6], data[7], data[8]);
      this.level = new GribPDSLevel(data[6], data[7], data[8]);

      // octets 13-17 (base time of forecast)
      this.baseTime = new GregorianCalendar(data[9] + 100 * (data[21] - 1),
         data[10]-1, data[11], data[12], data[13]);

      // GMT timestamp: zone offset to GMT is 0
      this.baseTime.set(Calendar.ZONE_OFFSET, 0);

      // rdg - adjusted for DST - don't know if this affects everywhere or if
      //       Calendar can figure out where DST is implemented. Still need to find out.
      // GMT timestamp: DST offset to GMT is 0
      this.baseTime.set(Calendar.DST_OFFSET, 0);

      // get info for forecast time

/*  RDG - added this code obtained from the sourceforce forum for jgrib*/
/* change x3/x6/x12 to hours */
// rdg - changed indices to match indices used here

      // octet 18
      switch (data[14])
      {
         case 10: //3 hours
            data[15] *= 3;
            data[16] *= 3;
            data[14] = 1;
            break;
         case 11: // 6 hours
            data[15] *= 6;
            data[16] *= 6;
            data[14] = 1;
            break;
         case 12: // 12 hours
            data[15] *= 12;
            data[16] *= 12;
            data[14] = 1;
            break;
      }
/* RDG - end of code added 4 Aug 02 */

      // octet 21 (time range indicator)
      switch (data[17])
      {
         case 0:
            offset = data[15];
            offset2 = 0;
            break;
         case 1: // analysis product - valid at reference time
            offset = 0;
            offset2 = 0;
            break;
         case 2:
            timeRange = "product valid from ";
            connector = " to ";
            offset = data[15];
            offset2 = data[16];
            break;
         case 3:
            timeRange = "product is an average between ";
            connector = " and ";
            offset = data[15];
            offset2 = data[16];
            break;
         case 4:
            timeRange = "product is an accumulation from ";
            connector = " to ";
            offset = data[15];
            offset2 = data[16];
            break;
         case 5:
            timeRange = "product is the difference of ";
            connector = " minus ";
            offset = data[16];
            offset2 = data[15];
            break;
         case 6:
            timeRange = "product is an average from ";
            connector = " to ";
            offset = -data[15];
            offset2 = -data[16];
            break;
         case 7:
            timeRange = "product is an average from ";
            connector = " to ";
            offset = -data[15];
            offset2 = data[16];
            break;
         case 10:
            offset = Bytes2Number.uint2(data[15], data[16]);
            break;
         default:
         // no reason to crash here - just notify that the time is not discernible
//            throw new NotSupportedException("GribRecordPDS: Time " +
//                  "Range Indicator " + data[17] + " is not yet supported");
            System.err.println("GribRecordPDS: Time Range Indicator "
                   + data[17] + " is not yet supported - continuing, but time of data is not valid");
      }

      // prep for adding the offset - get the base values
      int minute1 = data[13];
      int minute2 = data[13];
      int hour1 = data[12];
      int hour2 = data[12];
      int day1 = data[11];
      int day2 = data[11];
      int month1 = data[10];
      int month2 = data[10];
      int year1 = data[9];
      int year2 = data[9];

      // octet 18 (again) - this time adding offset to get forecast/valid time
      switch (data[14])
      {
         case 0:
            minute1 += offset;
            minute2 += offset2;
            break;  // minute
         case 1:
            hour1 += offset;
            hour2 += offset2;
            break;  // hour
         case 2:
            day1 += offset;
            day2 += offset;
            break;  // day
         case 3:
            month1 += offset;
            month2 += offset2;
            break;  // month
         case 4:
            year1 += offset;
            year2 += offset2;
            break;  // year
         default:
            // no reason to crash here either - just report and continue
//            throw new NotSupportedException("GribRecordPDS: Forecast time unit " +
//                  "> year not supported yet.");
            System.err.println("GribRecordPDS: Forecast time unit, index of "
                   + data[14] + ", is not yet supported - continuing, but time "
                   + "of data is not valid");
      }

      // octets 13-17 (time of forecast)
      this.forecastTime = new GregorianCalendar(year1 + 100 * (data[21] - 1),
         month1-1, day1, hour1, minute1);
      this.forecastTime2 = new GregorianCalendar(year2 + 100 * (data[21] - 1),
         month2-1, day2, hour2, minute2);
      /*
      this.forecastTime = Calendar.getInstance();
      this.forecastTime.set(data[9] + 100 * (data[21] - 1), data[10] - 1, data[11],
            data[12], data[13], 0);
      */

      // GMT timestamp: zone offset to GMT is 0
      this.forecastTime.set(Calendar.ZONE_OFFSET, 0);
      this.forecastTime2.set(Calendar.ZONE_OFFSET, 0);

      // rdg - adjusted for DST - don't know if this affects everywhere or if
      //       Calendar can figure out where DST is implemented. Find out at end of Oct.
      // GMT timestamp: DST offset to GMT is 0
      this.forecastTime.set(Calendar.DST_OFFSET, 0);
      this.forecastTime2.set(Calendar.DST_OFFSET, 0);

      // octet 26, sub center - is loaded after octet 5 now
//      this.sub_center = data[22];

      // octets 27-28 (decimal scale factor)
      /*
      boolean is_negative = (data[23] & 128) == 128;
      this.decscale = data[23] << 8 & data[24];
      */
      this.decscale = Bytes2Number.int2(data[23], data[24]);
   }


   /**
    * Get the byte length of this section.
    *
    * @return length in bytes of this section
    */
   public int getLength()
   {

      return this.length;
   }


   /**
    * Check if GDS exists.
    *
    * @return true, if GDS exists
    */
   public boolean gdsExists()
   {

      return this.gds_exists;
   }


   /**
    * Check if BMS exists.
    *
    * @return true, if BMS exists
    */
   public boolean bmsExists()
   {

      return this.bms_exists;
   }


   /**
    * Get the exponent of the decimal scale used for all data values.
    *
    * @return exponent of decimal scale
    */
   public int getDecimalScale()
   {

      return this.decscale;
   }


   /**
    * Get the type of the parameter.
    *
    * @return type of parameter
    */
   public String getType()
   {

      return this.parameter.getName();
   }


   /**
    * Get a descritpion of the parameter.
    *
    * @return descritpion of parameter
    */
   public String getDescription()
   {

      return this.parameter.getDescription();
   }


   /**
    * Get the name of the unit of the parameter.
    *
    * @return name of the unit of the parameter
    */
   public String getUnit()
   {

      return this.parameter.getUnit();
   }


   /**
    * Get the level of the forecast/analysis.
    *
    * @return name of level (height or pressure)
    */
   public String getLevel()
   {

      return this.level.getLevel();
   }

// rdg - added the following getters for level information though they are
//       just convenience methods.  You could do the same by getting the
//       GribPDSLevel (with getPDSLevel) then calling its methods directly
   /**
    * Get the name for the type of level for this forecast/analysis.
    *
    *
    * @return name of level (height or pressure)
    */
   public String getLevelName()
   {
      return this.level.getName();
   }

   /**
    * Get the long description for this level of the forecast/analysis.
    *
    * @return name of level (height or pressure)
    */
   public String getLevelDesc()
   {
      return this.level.getDesc();
   }

   /**
    * Get the units for the level of the forecast/analysis.
    *
    * @return name of level (height or pressure)
    */
   public String getLevelUnits()
   {
      return this.level.getUnits();
   }

   /**
    * Get the numeric value for this level.
    *
    * @return name of level (height or pressure)
    */
   public float getLevelValue()
   {
      return this.level.getValue1();
   }

   /**
    * Get value 2 (if it exists) for this level.
    *
    * @return name of level (height or pressure)
    */
   public float getLevelValue2()
   {
      return this.level.getValue2();
   }

   /**
    * Get the level of the forecast/analysis.
    *
    * @return name of level (height or pressure)
    */
   public GribPDSLevel getPDSLevel()
   {
      return this.level;
   }

   /**
    * 
    * @return center_id
    */
   public int getCenterId()
   {
      return center_id;
   }

   /**
    * 
    * @return subcenter_id
    */
   public int getSubcenterId()
   {
      return subcenter_id;
   }

   /**
    * 
    * @return table version
    */
   public int getTableVersion()
   {
      return table_version;
   }

   /**
    * 
    * @return process_id
    */
   public int getProcessId()
   {
      return process_id;
   }

   /**
    * Get the Parameter Table that defines this parameter.
    *
    * @return GribPDSParamTable containing parameter table that defined this parameter
    */
   public GribPDSParamTable getParamTable()
   {
      return this.parameter_table;
   }

   /**
    * Get the base (analysis) time of the forecast in local time zone.
    *
    * @return date and time
    */
   public Calendar getLocalBaseTime()
   {
      return this.baseTime;
   }

   /**
    * Get the time of the forecast in local time zone.
    *
    * @return date and time
    */
   public Calendar getLocalForecastTime()
   {
      return this.forecastTime;
   }

   /**
    * Get the parameter for this pds.
    *
    * @return date and time
    */
   public GribPDSParameter getParameter()
   {
      return this.parameter;
   }

   /**
    * Get the base (analysis) time of the forecast in GMT.
    *
    * @return date and time
    */
   public Calendar getGMTBaseTime()
   {
      Calendar gmtTime = baseTime;
      // hopefully this DST offset adjusts to DST automatically
      int dstOffset = gmtTime.get(Calendar.DST_OFFSET)/3600000;
      int gmtOffset = gmtTime.get(Calendar.ZONE_OFFSET)/3600000;//ms to hours
      // System.out.println("offset is " + gmtOffset);
      // System.out.println("dst offset is " + dstOffset);
      //put offset back
      gmtTime.set(Calendar.HOUR,gmtTime.get(Calendar.HOUR)-gmtOffset-dstOffset);
      // System.out.println("new time is " + gmtTime.getTime());
      return gmtTime;
   }

   /**
    * Get the time of the forecast.
    *
    * @return date and time
    */
   public Calendar getGMTForecastTime()
   {
      Calendar gmtTime = forecastTime;
      // System.out.println("forecast time = " + gmtTime.getTime());
      // hopefully this DST offset adjusts to DST automatically
      int dstOffset = gmtTime.get(Calendar.DST_OFFSET)/3600000;
      int gmtOffset = gmtTime.get(Calendar.ZONE_OFFSET)/3600000;//ms to hours
      // System.out.println("offset is " + gmtOffset);
      // System.out.println("dst offset is " + dstOffset);
      gmtTime.set(Calendar.HOUR,gmtTime.get(Calendar.HOUR)-gmtOffset-dstOffset);//put offset back
      // System.out.println("new time is " + gmtTime.getTime());
      return gmtTime;
   }

   /**
    * Get a string representation of this PDS.
    *
    * @return string representation of this PDS
    */
   public String toString()
   {
      return headerToString() +
            "        Type: " + this.getType() + "\n" +
            "        Description: " + this.getDescription() + "\n" +
            "        Unit: " + this.getUnit() + "\n" +
            "        table: " + this.table_version + "\n" +
            "        table version: " + this.table_version + "\n" +
            "        " + this.level + // now formatted in GribPDSLevel
            "        dec.scale: " + this.decscale +
            (this.gds_exists ? "\n        GDS exists" : "") +
            (this.bms_exists ? "\n        BMS exists" : "");
   }

   /**
    * Get a string representation of this Header information for this PDS.
    *
    * @return string representation of the Header for this PDS
    */
   public String headerToString()
   {
      String time1 = this.forecastTime.get(Calendar.DAY_OF_MONTH) + "." +
            (this.forecastTime.get(Calendar.MONTH) + 1) + "." +
            this.forecastTime.get(Calendar.YEAR) + "  " +
            this.forecastTime.get(Calendar.HOUR_OF_DAY) + ":" +
            this.forecastTime.get(Calendar.MINUTE);
      String time2 = this.forecastTime2.get(Calendar.DAY_OF_MONTH) + "." +
            (this.forecastTime.get(Calendar.MONTH) + 1) + "." +
            this.forecastTime.get(Calendar.YEAR) + "  " +
            this.forecastTime.get(Calendar.HOUR_OF_DAY) + ":" +
            this.forecastTime.get(Calendar.MINUTE);
      String timeStr;
      if(timeRange == null){
         timeStr = "time: " + time1;
      }else{
         timeStr = timeRange + time1 + connector + time2;
      }

      return "    PDS header:" + '\n' +
            "        center: " + this.center_id + "\n" +
            "        subcenter: " + this.subcenter_id + "\n" +
            "        table: " + this.table_version + "\n" +
            "        grid_id: " + this.grid_id + "\n" +
            "        " + timeStr + " (dd.mm.yyyy hh:mm) \n";
   }

   /**
    * rdg - added an equals method here
    * 
    * @param obj - to test 
    * @return true/false
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (!(obj instanceof GribRecordPDS))
      {
         return false;
      }
      if (this == obj)
      {
         // Same object
         return true;
      }
      GribRecordPDS pds = (GribRecordPDS) obj;

      if (grid_id != pds.grid_id) return false;
      if (baseTime != pds.baseTime) return false;
      if (forecastTime != pds.forecastTime) return false;
      if (center_id != pds.center_id) return false;
      if (subcenter_id != pds.subcenter_id) return false;
      if (table_version != pds.table_version) return false;
      if (decscale != pds.decscale) return false;
      if (length != pds.length) return false;

      if (!(parameter.equals(pds.getParameter()))) return false;
      if (!(level.equals(pds.getPDSLevel()))) return false;

      return true;

   }

   /**
    * rdg - added this method to be used in a comparator for sorting while
    *       extracting records.
    * Not currently used in the JGrib library, but is used in a library I'm
    *    using that uses JGrib.
    * Compares numerous features from the PDS information to sort according
    *    to a time, level, level-type, y-axis, x-axis order
    * @param pds - GribRecordPDS object    
    * @return - -1 if pds is "less than" this, 0 if equal, 1 if pds is "greater than" this.
    *
    */
   public int compare(GribRecordPDS pds){

      int check;

      if (this.equals(pds)){
         return 0;
      }

      // not equal, so either less than or greater than.
      // check if pds is less; if not, then pds is greater
      if (grid_id > pds.grid_id) return -1;
      if (baseTime.getTime().getTime() > pds.baseTime.getTime().getTime()) return -1;
      if (forecastTime.getTime().getTime() > pds.forecastTime.getTime().getTime()) return -1;
      if (forecastTime2.getTime().getTime() > pds.forecastTime2.getTime().getTime()) return -1;
      if (center_id > pds.center_id) return -1;
      if (subcenter_id > pds.subcenter_id) return -1;
      if (table_version > pds.table_version) return -1;
      if (decscale > pds.decscale) return -1;
      if (length > pds.length) return -1;

      check = parameter.compare(pds.getParameter());
      if (check < 0) return -1;
      check = level.compare(pds.getPDSLevel());
      if (check < 0) return -1;

      // if here, then something must be greater than something else - doesn't matter what
      return 1;
   }

}



