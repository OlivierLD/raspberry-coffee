/**
 * ===============================================================================
 * $Id: GribGDSLatLon.java,v 1.5 2006/07/25 13:46:23 frv_peg Exp $
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

/*
 * GribGDSLatLon.java  1.0  10/15/2002
 *
 * Capt Richard D. Gonzalez
 */

package jgrib;

import java.io.IOException;


/**
 * A class that represents the grid definition section (GDS) of a GRIB record
 * with a Lat/Lon grid projection.
 *
 * @author  Richard Gonzalez
 * based heavily on the original GribRecordGDS
 *
 * @version 1.0
 */

public class GribGDSLatLon extends GribRecordGDS
{

   // Attributes for Lat/Lon grid not included in GribRecordGDS

   // None!  The Lat/Lon grid is the most basic, and all attributes match
   //   the original GribRecordGDS

   /**
    * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
    *
    * See Table D of NCEP Office Note 388 for details
    *
    * @param in bit input stream with GDS content
    * @param header 
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NoValidGribException  if stream contains no valid GRIB file
    * @throws NotSupportedException 
    */
   public GribGDSLatLon(BitInputStream in, int[] header)
          throws IOException, NoValidGribException, NotSupportedException
   {
      super(header);

      int[] data;

      //if (this.grid_type != 0){
      if (this.grid_type != 0 && this.grid_type != 10){
         throw new NoValidGribException("GribGDSLatLon: grid_type is not "+
           "Latitude/Longitude (read grid type " + grid_type + ", needed 0 or 10)");
      }

      data = in.readUI8(this.length - header.length);

      // octets 7-8 (number of points along a parallel)
      this.grid_nx = Bytes2Number.uint2(data[0], data[1]);

      // octets 9-10 (number of points along a meridian)
      this.grid_ny = Bytes2Number.uint2(data[2], data[3]);

      // octets 11-13 (latitude of first grid point)
      this.grid_lat1 = Bytes2Number.int3(data[4], data[5], data[6]) / 1000.0;

      // octets 14-16 (longitude of first grid point)
      this.grid_lon1 = Bytes2Number.int3(data[7], data[8], data[9]) / 1000.0;

      // octet 17 (resolution and component flags -> 128 == 0x80 == increments given.)
      this.grid_mode = data[10];

//      if (this.grid_mode != 128 && this.grid_mode != 0)
//         throw new NotSupportedException("GribGDSLatLon: No other component flag than 128 " +
//               "(increments given) or 0 (not given) supported. " +
//               "Current is: " + this.grid_mode);
      /*
      TABLE 7 - RESOLUTION AND COMPONENT FLAGS
   (GDS Octet 17)

   Bit 		Value 		Meaning
   1	0	Direction increments not given
      1	Direction increments given

   2	0	Earth assumed spherical with radius = 6367.47 km
      1	Earth assumed oblate spheroid with size
         as determined by IAU in 1965:
         6378.160 km, 6356.775 km, f = 1/297.0

   3-4		reserved (set to 0)

   5	0	u- and v-components of vector quantities resolved relative to easterly and northerly directions
      1	u and v components of vector quantities resolved relative to the defined grid in the direction of increasing x and y (or i and j) coordinates respectively

   6-8		reserved (set to 0)
      */
      // octets 18-20 (latitude of last grid point)
      this.grid_lat2 = Bytes2Number.int3(data[11], data[12], data[13]) / 1000.0;

      // octets 21-23 (longitude of last grid point)
      this.grid_lon2 = Bytes2Number.int3(data[14], data[15], data[16]) / 1000.0;

      // increments given
      //if (this.grid_mode == 128)

      //grid_mode_isUVgridIJ = false;
      switch(this.grid_mode)
      {
         case 136:
            //grid_mode_isUVgridIJ = true;
         case 128:
         // octets 24-25 (x increment)
         this.grid_dx = Bytes2Number.uint2(data[17], data[18]) / 1000.0;

         // octets 26-27 (y increment)
         this.grid_dy = Bytes2Number.uint2(data[19], data[20]) / 1000.0;

         // octet 28 (point scanning mode - See table 8)
         this.grid_scan = data[21];
         if ((this.grid_scan & 63) != 0)
            throw new NotSupportedException("GribGDSLatLon: This scanning mode (" +
                  this.grid_scan +") is not supported.");
         if ((this.grid_scan & 128) != 0) this.grid_dx = -this.grid_dx;
// rdg - changed to != 64 here because table 8 shows -j if bit NOT set
         if ((this.grid_scan & 64) != 64) this.grid_dy = -this.grid_dy;
//         if ((this.grid_scan & 64) != 0) this.grid_dy = -this.grid_dy;
//      }
//      else
//      {
            break;
         case 0:
         // calculate increments
         this.grid_dx = (this.grid_lon2 - this.grid_lon1) / this.grid_nx;
         this.grid_dy = (this.grid_lat2 - this.grid_lat1) / this.grid_ny;
            break;
         default:
            throw new NotSupportedException("GribGDSLatLon: Supported grid mode flags are: " +
                  " 136, 128, 0.     Current is: " + this.grid_mode);
      }


      switch (this.grid_type)
      {
         case 0:
            // Standard Lat/Lon grid, no rotation
            this.grid_latsp = -90.0;
            this.grid_lonsp = 0.0;
            this.grid_rotang = 0.0;
            break;

         case 10:
            // Rotated Lat/Lon grid, Lat (octets 33-35), Lon (octets 36-38), rotang (octets 39-42)
            //NB offset = 7 (octet = array index + 7)
            this. grid_latsp = Bytes2Number.int3(data[26], data[27], data[28]) / 1000.0;
            this. grid_lonsp = Bytes2Number.int3(data[29], data[30], data[31]) / 1000.0;
            this. grid_rotang = Bytes2Number.float4(data[32], data[33], data[34], data[35]);
            break;

         default:
            // No knowledge yet
            // NEED to fix this later, if supporting other grid types
            this.grid_latsp = Double.NaN;
            this.grid_lonsp = Double.NaN;
            this.grid_rotang = Double.NaN;
            break;
      }
   }

// *** public methods **************************************************************

   /** 
    * @see net.sourceforge.jgrib.GribRecordGDS#isUVEastNorth()
    */
   public boolean isUVEastNorth() {
    return (grid_mode & 0x08) == 0;
   }
  
   /** 
    * @see net.sourceforge.jgrib.GribRecordGDS#compare(net.sourceforge.jgrib.GribRecordGDS)
    */   
   public int compare(GribRecordGDS gds) {
    if (this.equals(gds)){
       return 0;
    }

    // not equal, so either less than or greater than.
    // check if gds is less, if not, then gds is greater
    if (grid_type > gds.grid_type) return -1;
    if (grid_mode > gds.grid_mode) return -1;
    if (grid_scan > gds.grid_scan) return -1;
    if (grid_nx > gds.grid_nx) return -1;
    if (grid_ny > gds.grid_ny) return -1;
    if (grid_dx > gds.grid_dx) return -1;
    if (grid_dy > gds.grid_dy) return -1;
    if (grid_lat1 > gds.grid_lat1) return -1;
    if (grid_lat2 > gds.grid_lat2) return -1;
    if (grid_latsp > gds.grid_latsp) return -1;
    if (grid_lon1 > gds.grid_lon1) return -1;
    if (grid_lon2 > gds.grid_lon2) return -1;
    if (grid_lonsp > gds.grid_lonsp) return -1;
    if (grid_rotang > gds.grid_rotang) return -1;

    // if here, then something must be greater than something else - doesn't matter what
    return 1;
 }   

   /**
    * @see net.sourceforge.jgrib.GribRecordGDS#hashCode
    * @return integer value of hashCode
    */   
   public int hashCode()
   {
      int result = 17;
      result = 37 * result + grid_nx;
      result = 37 * result + grid_ny;
      int intLat1 = Float.floatToIntBits((float) grid_lat1);
      result = 37 * result + intLat1;
      int intLon1 = Float.floatToIntBits((float) grid_lon1);
      result = 37 * result + intLon1;
      return result;
   }

   /**
    * @see net.sourceforge.jgrib.GribRecordGDS#equals(java.lang.Object)
    * @return true/false if objects are equal
 	*/   
   public boolean equals(Object obj)
   {
      if (!(obj instanceof GribRecordGDS))
      {
         return false;
      }
      if (this == obj)
      {
         // Same object
         return true;
      }
      GribRecordGDS gds = (GribRecordGDS) obj;

      if (grid_type != gds.grid_type) return false;
      if (grid_mode != gds.grid_mode) return false;
      if (grid_scan != gds.grid_scan) return false;
      if (grid_nx != gds.grid_nx) return false;
      if (grid_ny != gds.grid_ny) return false;
      if (grid_dx != gds.grid_dx) return false;
      if (grid_dy != gds.grid_dy) return false;
      if (grid_lat1 != gds.grid_lat1) return false;
      if (grid_lat2 != gds.grid_lat2) return false;
      if (grid_latsp != gds.grid_latsp) return false;
      if (grid_lon1 != gds.grid_lon1) return false;
      if (grid_lon2 != gds.grid_lon2) return false;
      if (grid_lonsp != gds.grid_lonsp) return false;
      if (grid_rotang != gds.grid_rotang) return false;

      return true;
   }

   /**
    * Get length in bytes of this section.
    *
    * @return length in bytes of this section
    */
   public int getLength()
   {
      return length;
   }

   /**
    * Get type of grid.  This is type 0.
    *
    * @return type of grid
    */
   public int getGridType()
   {
      return grid_type;
   }

   /**
    * @return true/false
    */
   public boolean isRotatedGrid()
   {
	   // Implicit IF-THEN
      return grid_type == 10;
   }

   /**
    * Get number of grid columns.
    *
    * @return number of grid columns
    */
   public int getGridNX()
   {
      return grid_nx;
   }

   /**
    * Get number of grid rows.
    *
    * @return number of grid rows.
    */
   public int getGridNY()
   {
      return grid_ny;
   }

   /**
    * Get latitude of grid start point.
    *
    * @return latitude of grid start point
    */
   public double getGridLat1()
   {
      return grid_lat1;
   }

   /**
    * Get longitude of grid start point.
    *
    * @return longitude of grid start point
    */
   public double getGridLon1()
   {
      return grid_lon1;
   }

   /**
    * Get grid mode. <i>Only 128 (increments given) supported so far.</i>
    *
    * @return grid mode
    */
   public int getGridMode()
   {
      return grid_mode;
   }

   /**
    * Get latitude of grid end point.
    *
    * @return latitude of grid end point
    */
   public double getGridLat2()
   {
      return grid_lat2;
   }

   /**
    * Get longitude of grid end point.
    *
    * @return longitude of grid end point
    */
   public double getGridLon2()
   {
      return grid_lon2;
   }

   /**
    * Get delta-Lon between two grid points.
    *
    * @return Lon increment
    */
   public double getGridDX()
   {
      return grid_dx;
   }

   /**
    * Get delta-Lat between two grid points.
    *
    * @return Lat increment
    */
   public double getGridDY()
   {
      return grid_dy;
   }

   /**
    * Get scan mode (sign of increments). <i>Only 64, 128 and 192 supported so far.</i>
    *
    * @return scan mode
    */
   public int getGridScanmode()
   {
      return grid_scan;
   }

   /**
    * Get longitide coordinates converted to the range +/- 180
    * @return longtitude as double
    */
   public double[] getXCoords()
   {
      return getXCoords(true);
   }

   /**
    * Get longitide coordinates
    * @param convertTo180 
    * @return longtitude as double
    */
   public double[] getXCoords(boolean convertTo180)
   {
      double[] coords = new double[grid_nx];

      int k = 0;

      for (int x = 0; x < grid_nx; x++)
      {
         double longi = grid_lon1 + x * grid_dx;

         if (convertTo180){ // move x-coordinates to the range -180..180
            if (longi >= 180.0) longi = longi - 360.0;
            if (longi < -180.0) longi = longi + 360.0;
         }else{ // handle wrapping at 360
            if (longi >= 360.0) longi = longi - 360.0;
         }
         coords[k++] = longi;
      }
      return coords;
   }

   /**
    * Get all latitude coordinates
    * @return latitude as double
    */
   public double[] getYCoords()
   {
      double[] coords = new double[grid_ny];

      int k = 0;

      for (int y = 0; y < grid_ny; y++)
      {
         double lati = grid_lat1 + y * grid_dy;
         if (lati > 90.0 || lati < -90.0)
            System.err.println("GribGDSLatLon.getYCoords: latitude out of range (-90 to 90).");

         coords[k++] = lati;
      }
      return coords;
   }

   /**
    * Get grid coordinates in longitude/latitude pairs
    * Longitude is returned in the range +/- 180 degrees
    * 
    * @see net.sourceforge.jgrib.GribRecordGDS#getGridCoords() 
    * @return longitide/latituide as doubles
    */
   public double[] getGridCoords()
   {

      double[] coords = new double[grid_ny * grid_nx * 2];

      int k = 0;
      for (int y = 0; y < grid_ny; y++){
         for (int x = 0; x < grid_nx; x++){
            double longi = grid_lon1 + x * grid_dx;
            double lati = grid_lat1 + y * grid_dy;

            // move x-coordinates to the range -180..180
            if (longi >= 180.0) longi = longi - 360.0;
            if (longi < -180.0) longi = longi + 360.0;
            if (lati > 90.0 || lati < -90.0){
               System.err.println("GribGDSLatLon.getGridCoords: latitude out of range (-90 to 90).");
            }
            coords[k++] = longi;
            coords[k++] = lati;
         }
      }
      return coords;
   }


   /**
    * Get a string representation of this GDS.
    * TODO include more information about this projection
    * @return string representation of this GDS
    */
   public String toString() {
      
      String str = "    GDS section:\n      ";
      if (this.grid_type == 0) str += "  LatLon Grid";
      if (this.grid_type == 10) str += "  Rotated LatLon Grid";
      
      str += "  (" + this.grid_nx + "x" + this.grid_ny + ")\n      ";
      str += "  lon: " + this.grid_lon1 + " to " + this.grid_lon2;
      str += "  (dx " + this.grid_dx + ")\n      ";
      str += "  lat: " + this.grid_lat1 + " to " + this.grid_lat2;
      str += "  (dy " + this.grid_dy + ")";
      
      if (this.grid_type == 10) {
        str += "\n        south pole: lon " + this.grid_lonsp + " lat " + this.grid_latsp;
        str += "\n        rot angle: " + this.grid_rotang;
      }
      
      return str;
   }
}


