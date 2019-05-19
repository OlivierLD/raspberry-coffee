/**
 * ===============================================================================
 * $Id: GribGDSPolarStereo.java,v 1.6 2006/07/25 13:46:00 frv_peg Exp $
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
 * GribGDSPolarStereo.java  1.0  01/01/2001
 *
 * based on GribRecordGDS (C) Benjamin Stark
 * Heavily modified by Capt Richard D. Gonzalez to conform to GribGDSFactory
 *   implementation - 4 Sep 02
 * Implements GDS Table D for Polar Stereo grid
 *
 */

package jgrib;

import java.io.IOException;

/**
 * A class that represents the grid definition section (GDS) of a GRIB record.
 *
 * @author  Benjamin Stark
 * @author  Capt Richard D. Gonzalez
 * @version 2.0
 *
 * Modified 4 Sep 02 to be constructed by GribGDSFactory - Richard D. Gonzalez
 */

public class GribGDSPolarStereo extends GribRecordGDS
{

   /**
    * Projection Center Flag.
    */
   protected int grid_proj_center;

   /**
    * starting x value using this projection.
    * This is not a Longitude, but an x value based on the projection
    */
   protected double grid_startx;

   /**
    * starting y value using this projection.
    * This is not a Latitude, but a y value based on the projection
    */
   protected double grid_starty;

   /**
    * Central Scale Factor.  Assumed 1.0
    */
   protected final double SCALE_FACTOR = 1.0;

   /**
    * Latitude of Center - assumed 60 N or 60 S based on note 2 of table D
    */
   protected double latitude_ts = 60.0;  //true scale

   // *** constructors *******************************************************

   /**
    * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
    *
    * @param in bit input stream with GDS content
    * @param header - int array with first six octets of the GDS
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NoValidGribException  if stream contains no valid GRIB file
    * @throws NotSupportedException 
    */
   public GribGDSPolarStereo(BitInputStream in, int [] header)
          throws IOException, NoValidGribException, NotSupportedException
   {
      super(header);
      // System.out.println("Discovered GDS type: PolarStereo");
      int[] data;

      if (this.grid_type != 5 ){
         throw new NoValidGribException("GribGDSPolarStereo: grid_type is not "+
           "Polar Stereo (read grid type " + grid_type + " needed 5)");
      }

      //read in the Grid Description (see Table D) of the GDS
      data = in.readUI8(this.length - header.length);

      // octets 7-8 (Nx - number of points along x-axis)
      this.grid_nx = Bytes2Number.uint2(data[0], data[1]);

      // octets 9-10 (Ny - number of points along y-axis)
      this.grid_ny = Bytes2Number.uint2(data[2], data[3]);

      // octets 11-13 (La1 - latitude of first grid point)
      this.grid_lat1 = Bytes2Number.int3(data[4], data[5], data[6]) / 1000.0;

      // octets 14-16 (Lo1 - longitude of first grid point)
      this.grid_lon1 = Bytes2Number.int3(data[7], data[8], data[9]) / 1000.0;

      // octet 17 (resolution and component flags).  See Table 7
      this.grid_mode = data[10];

      // octets 18-20 (Lov - Orientation of the grid - east lon parallel to y axis)
      this.grid_lon2 = Bytes2Number.int3(data[11], data[12], data[13]) / 1000.0;

      // octets 21-23 (Dx - the X-direction grid length) See Note 2 of Table D
      this.grid_dx = Bytes2Number.int3(data[14], data[15], data[16]);

      // octets 24-26 (Dy - the Y-direction grid length) See Note 2 of Table D
      this.grid_dy = Bytes2Number.uint3(data[17], data[18], data[19]);

      // octets 27 (Projection Center flag) See Note 5 of Table D
      this.grid_proj_center = data[20];
      if ((grid_proj_center & 128) == 128){ // if bit 1 set to 1, SP is on proj plane
        latitude_ts = -60.0;
      }

      // octet 28 (Scanning mode)  See Table 8
      this.grid_scan = data[21];
      if ((this.grid_scan & 63) != 0)
      {
         throw new NotSupportedException("GribRecordGDS: This scanning mode (" +
               this.grid_scan + ") is not supported.");
      }
// rdg = table 8 shows -i if bit set
         if ((this.grid_scan & 128) != 0) this.grid_dx = -this.grid_dx;
// rdg - changed to != 64 here because table 8 shows -j if bit NOT set
         if ((this.grid_scan & 64) != 64) this.grid_dy = -this.grid_dy;
//         if ((this.grid_scan & 64) != 0) this.grid_dy = -this.grid_dy;

      // octets 29-32 are reserved

      prepProjection();
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
    * Overides method from GribRecordGDS
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
    * Method overrides GribRecordGDS.equals, which in turn overrides
    * the default Object.equals method.
    * 
    * @see net.sourceforge.jgrib.GribRecordGDS#equals(java.lang.Object)
    * @return true/false if objects are equal
 	*/     
   public boolean equals(Object obj)
   {
      if (!(obj instanceof GribGDSPolarStereo))
      {
         return false;
      }
      if (this == obj) // Same object
      {
         return true;
      }
      GribGDSPolarStereo gds = (GribGDSPolarStereo) obj;

      if (grid_nx != gds.grid_nx) return false;
      if (grid_ny != gds.grid_ny) return false;
      if (grid_lat1 != gds.grid_lat1) return false;
      if (grid_lon1 != gds.grid_lon1) return false;
      if (grid_mode != gds.grid_mode) return false;
      if (grid_lat2 != gds.grid_lat2) return false;
      if (grid_dx != gds.grid_dx) return false;
      if (grid_dy != gds.grid_dy) return false;
      if (grid_type != gds.grid_type) return false;
      if (grid_proj_center != gds.grid_proj_center) return false;
      if (grid_scan != gds.grid_scan) return false;

      return true;

   }

   /**
    * Get length in bytes of this section.
    *
    * @return length in bytes of this section
    */
   public int getLength()
   {
      return this.length;
   }

   /**
    * Get type of grid.
    *
    * @return type of grid
    */
   public int getGridType()
   {
      return this.grid_type;
   }

   /**
    * Get number of grid columns.
    *
    * @return number of grid columns
    */
   public int getGridNX()
   {
      return this.grid_nx;
   }

   /**
    * Get number of grid rows.
    *
    * @return number of grid rows.
    */
   public int getGridNY()
   {
      return this.grid_ny;
   }

   /**
    * Get y-coordinate/latitude of grid start point.
    *
    * @return y-coordinate/latitude of grid start point
    */
   public double getGridLat1()
   {
      return this.grid_lat1;
   }

   /**
    * Get x-coordinate/longitude of grid start point.
    *
    * @return x-coordinate/longitude of grid start point
    */
   public double getGridLon1()
   {
      return this.grid_lon1;
   }

   /**
    * Get grid mode. <i>Only 128 (increments given) supported so far.</i>
    *
    * @return grid mode
    */
   public int getGridMode()
   {
      return this.grid_mode;
   }

   /**
    * Get East longitude parallel to y-axis
    *
    * @return longitude
    */
   public double getGridLov()
   {
      return this.grid_lon2;
   }

   /**
    * Get x-increment/distance between two grid points.
    *
    * @return x-increment
    */
   public double getGridDX()
   {
      return this.grid_dx;
   }

   /**
    * Get y-increment/distance between two grid points.
    *
    * @return y-increment
    */
   public double getGridDY()
   {
      return this.grid_dy;
   }

   /**
    * Get projection center flag.
    *
    * @return projection center flag
    */
   public int getProjCenterFlag()
   {
      return this.grid_proj_center;
   }

   /**
    * Get scan mode (sign of increments). <i>Only 64, 128 and 192 supported so far.</i>
    *
    * @return scan mode
    */
   public int getGridScanmode()
   {
      return this.grid_scan;
   }

   public double getGridCenterLon() //=LOV
   {
      return this.grid_lon2;
   }

   /**
    * Get the Latitude of the circle where grid lengths are defined
    *
    * @return grid_center_lat
    */
   public double getGridCenterLat()
   {
      return latitude_ts > 0 ? 90 : -90;
   }

   public double getLatitudeTrueScale()
   {
      return latitude_ts;
   }


   /**
    * Get all longitude coordinates
    * @return longitude as double
    */
   public double[] getXCoords()
   {
      double[] xCoords = new double[grid_nx];

      double startx = grid_startx/1000.0;
      double dx = grid_dx/1000.0;

      for (int i = 0; i < grid_nx; i++)
      {
         double x = startx + i * dx;
         xCoords[i] = x;
      }
      return xCoords;
   }

   /**
    * Get all latitude coordinates
    * @return latitude as double
    */
   public double[] getYCoords()
   {
      double[] yCoords = new double[grid_ny];

      double starty = grid_starty/1000.0;
      double dy = grid_dy/1000.0;

      for (int j = 0; j < grid_ny; j++)
      {
         double y = starty + j * dy;
         yCoords[j] = y;
      }
      return yCoords;
   }


   /**
    * Prep the projection and determine the starting x and y values based on
    * Lat1 and Lon1 relative to the origin for this grid.
    *
    * adapted from J.P. Snyder, Map Projections - A Working Manual,
    * U.S. Geological Survey Professional Paper 1395, 1987
    * Maintained his symbols, so the code matches his work.
    * Somewhat hard to follow, if interested, suggest looking up quick reference
    * at http://mathworld.wolfram.com/LambertConformalConicProjection.html
    *
    * Origin is where Lov intersects 60 degrees (from note 2 of Table D) north
    * or south (determined by bit 1 of the Projection Center Flag).
    *
    * This assumes a central scale factor of 1.
    *
    * @return latitide/longitude as doubles
    */
   private void prepProjection()
   {
      double k;
      // peg - variables pi2 and pi4 never used
      //double pi2;
      //double pi4; 
      double cosLat1;
      double sinLat1;
      double cos60;
      double sin60;
      double dLonr;

      cosLat1 = Math.cos(Math.toRadians(grid_lat1));
      sinLat1 = Math.sin(Math.toRadians(grid_lat1));
      cos60 = Math.cos(Math.toRadians(latitude_ts));
      sin60 = Math.sin(Math.toRadians(latitude_ts));
      dLonr = Math.toRadians(grid_lon1 - grid_lon2); //lon2 is lov

      k = 2.0 * SCALE_FACTOR /
          (1 + (sin60 * sinLat1) + cos60 * cosLat1 * Math.cos(dLonr));
      grid_startx = EARTH_RADIUS * k * cosLat1 * Math.sin(dLonr);
      grid_starty = EARTH_RADIUS * k *
                    ((cos60*sinLat1) - (sin60 * cosLat1 * Math.cos(dLonr)));

   }



   /**
    * Get grid coordinates in longitude/latitude
    *
    * adapted from J.P. Snyder, Map Projections - A Working Manual,
    * U.S. Geological Survey Professional Paper 1395, 1987
    * Maintained his symbols, so the code matches his work.
    * Somewhat hard to follow, if interested, suggest looking up quick reference
    * at http://mathworld.wolfram.com/PolarStereoConicProjection.html
    *
    * assumes scale factor of 1.0
    *
    * rdg - may not be correct yet - did not align with display software I
    * was using, but they implemented using a center point, vice LOV
    * TODO verify projection implementation
    * 
    * @see net.sourceforge.jgrib.GribRecordGDS#getGridCoords() 
    * @return longitide/latitude as doubles
    */
   public double[] getGridCoords()
   {
      int count = 0;
      double rho, c, cosC, sinC, cos60, sin60, lon, lat, x, y;
      double[] coords = new double[grid_nx * grid_ny * 2];
      cos60 = Math.cos(Math.toRadians(latitude_ts));
      sin60 = Math.sin(Math.toRadians(latitude_ts));

      for (int j = 0; j < grid_ny; j++)
      {
         y = grid_starty + grid_dy*j;
         for (int i = 0; i < grid_nx; i++)
         {
            x = grid_startx + grid_dx*i;

            rho = Math.sqrt(x*x + y*y);
            c = 2.0 * Math.atan(rho/(2.0 * EARTH_RADIUS * SCALE_FACTOR));
            cosC = Math.cos(Math.toRadians(c));
            sinC = Math.sin(Math.toRadians(c));

            lon = Math.asin(cosC * sin60 + (y * sinC * cos60 / rho));
            lat = grid_lon2 + Math.atan(x * sinC / (rho * cos60 * cosC - y * cos60 * sinC));

            // move x-coordinates to the range -180..180
            if (lon >= 180.0) lon = lon - 360.0;
            if (lon < -180.0) lon = lon + 360.0;
            if (lat > 90.0 || lat < -90.0)
            {
               System.err.println("GribGDSPolarStereo: latitude out of range (-90 to 90).");
            }
            coords[count++] = lon;
            coords[count++] = lat;
         }
      }
      return coords;
   }

   /**
    * @return Value of x start point as double
    */
   public double getGrid_startx()
   {
      return grid_startx;
   }

   /**
    * @return Value of y start point as double
    */
   public double getGrid_starty()
   {
      return grid_starty;
   }

   /**
    * Get a string representation of this GDS.
    * TODO - ensure this returns PS specific info - probably still a copy of LC
    * @return string representation of this GDS
    */
   public String toString()
   {

      String str = "    GDS section:\n      ";

      str += "  Polar Stereo Grid";


      str += "  (" + this.grid_nx + "x" + this.grid_ny + ")\n      ";
      str += "  1st point:  Lat: " + this.grid_lat1;
      str += "  Lon: " + this.grid_lon1 +"\n      ";
      str += "  Grid start X: " + this.grid_startx+ "m; ";
      str += " Y: " + this.grid_starty+ "m;\n      ";
      str += "  Grid length: X-Direction  " + this.grid_dx + "m; ";
      str += " Y-Direction: " + this.grid_dy + "m\n      ";
      str += "  Orientation - East longitude parallel to y-axis: " ;
      str += this.getGridLov() + "\n      ";
      str += "  Resolution and Component Flags: \n      ";
      if ((this.grid_mode & 128) == 128)
      {
         str += "       Direction increments given \n      ";
      }
      else
      {
         str += "       Direction increments not given \n      ";
      }
      if ((this.grid_mode & 64) == 64){
         str += "       Earth assumed oblate spheroid 6378.16 km at equator, " +
                        " 6356.775 km at pole, f=1/297.0\n      ";
      }
      else{
         str += "       Earth assumed spherical with radius = 6367.47 km \n      ";
      }
      if ((this.grid_mode & 8) == 8){
         str += "       u and v components are relative to the grid \n      ";
      }
      else{
         str += "       u and v components are relative to easterly and " +
                        "northerly directions \n      ";
      }
      str += "  Scanning mode:  \n      " ;
      if ((this.grid_scan & 128) == 128){
         str += "       Points scan in the -i direction \n      ";
      }
      else{
         str += "       Points scan in the +i direction \n      ";
      }
      if ((this.grid_scan & 64) == 64){
         str += "       Points scan in the +j direction \n      ";
      }
      else{
         str += "       Points scan in the -j direction \n      ";
      }
      if ((this.grid_scan & 32) == 32){
         str += "       Adjacent points in j direction are consecutive \n      ";
      }
      else{
         str += "       Adjacent points in i direction are consecutive\n";
      }
      str += "        proj_center flag: " + grid_proj_center + "\n";
      str += "        latitude_ts: " + getLatitudeTrueScale() + "\n";
      str += "        center_lon: " + getGridCenterLon() + "\n"; //=lov
      str += "        center_lat: " + getGridCenterLat()+ "\n";

      return str;
   }



}


