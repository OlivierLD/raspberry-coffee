/**
 * ===============================================================================
 * $Id: GribRecordGDS.java,v 1.7 2006/07/25 13:46:23 frv_peg Exp $
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
 * GribRecordGDS.java  1.0  01/01/2001
 *                     2.0  04 Sep 02
 *
 * (C) Benjamin Stark
 */

package jgrib;

import java.io.IOException;

/**
 * A class that represents the grid definition section (GDS) of a GRIB record.
 * <p>
 * 5 Okt 05 - Changed class to become abstract as intended by RDG all common
 * methods between this class and all known subclasses is changed to 
 * abstract methods, so it becomes more clear, which methods one should 
 * actually implement, when adding support for a new type of GRIB files.
 * <p>
 * 4 Sep 02 - Modified to be implemented using GribGDSFactory class.
 * <p>
 * This class is used to store the first 32 octets of the GDS, which are
 * common, or similar, in all GDS types.
 * Sometimes names vary slightly in Table D, but functionality is similar, e.g.
 * <p><code>
 *    Grid type     Octet    Id
 *     Lat/Lon       7-8      Ni - Number of points along a latitude circle
 *     Lambert       7-8      Nx - Number of points along x-axis
 * </code><p>
 * Other times, functionality is different, e.g.
 * <p><code>
 *     Lat/Lon      18-20     La2 - latitude of grid point
 *     Lambert      18-20     Lov - the orientation of the grid
 * </code><p>
 * However, all sets have at least 32 octets.  Those 32 are stored here, and the
 * differences are resolved in the child classes, and therefore, all
 * attributes are set from the Child classes.
 * <p>
 * The names of the attributes are the same JGrib originally used , for
 * simplicity and continuity.  The fact that some grids use a different number
 * of octets for doubles is irrelevant, as the conversion is stored, not the
 * octets.
 * <p>
 * The child classes should call the proper setters and getters.
 * <p>
 * The class retains every bit of the original functionality, so it can continue
 * to be used in legacy programs (still limited to grid_type 0 and 10).
 * <p>
 * New users should not create instances of this class directly (in fact, it
 *   should be changed to an abstract class - it's on the to do list), but use the
 *   GribGDS factory instead, and add new child classes (e.g. GribGDSXxxx) as
 *   needed for additional grid_types.
 *   
 * @author  Benjamin Stark <p>
 * @author  Capt Richard D. Gonzalez, USAF (Modified original code) <p>
 * @author  Peter Gylling <peg at frv.dk> (Made class abstract)<p>
 * @version 3.0
 */

public abstract class GribRecordGDS
{
   /**
    * Radius of earth used in calculating projections
    * per table 7 - assumes spheroid
    */
   protected final double EARTH_RADIUS=6367470; 
	
   /**
    * Length in bytes of this section.
    */
   protected int length;

   /**
    * Type of grid (See table 6)
    */
   protected int grid_type;

   /**
    * Number of grid columns. (Also Ni)
    */
   protected int grid_nx;

   /**
    * Number of grid rows. (Also Nj)
    */
   protected int grid_ny;

   /**
    * Latitude of grid start point.
    */
   protected double grid_lat1;

   /**
    * Longitude of grid start point.
    */
   protected double grid_lon1;

   /**
    * Mode of grid (See table 7)
    * only 128 supported == increments given)
    */
   protected int grid_mode;

   /**
    * Latitude of grid end point.
    */
   protected double grid_lat2;

   /**
    * Longitude of grid end point.
    */
   protected double grid_lon2;

   /**
    * x-distance between two grid points
    * can be delta-Lon or delta x.
    */
   protected double grid_dx;

   /**
    * y-distance of two grid points
    * can be delta-Lat or delta y.
    */
   protected double grid_dy;

   /**
    * Scanning mode (See table 8).
    */
   protected int grid_scan;

   // rdg - the remaining coordinates are not common to all types, and as such
   //    should be removed.  They are left here (temporarily) for continuity.
   //    These should be implemented in a GribGDSxxxx child class.

   /**
    * y-coordinate/latitude of south pole of a rotated lat/lon grid.
    */
   protected double grid_latsp;

   /**
    * x-coordinate/longitude of south pole of a rotated lat/lon grid.
    */
   protected double grid_lonsp;

   /**
    * Rotation angle of rotated lat/lon grid.
    */
   protected double grid_rotang;

   // *** constructors *******************************************************

   /**
    * New constructor created for child classes, which has to be public!
    *
    * @param header - integer array of header data (octets 1-6) read in
    * GribGDSFactory
    *
    * exceptions are thrown in children and passed up
    * @see net.sourceforge.jgrib.GribGDSFactory#getGDS(BitInputStream)
    */
   public GribRecordGDS(int[] header){

      // octets 1-3 (GDS section length)
      this.length = Bytes2Number.uint3(header[0], header[1], header[2]);

      // octet 4 (number of vertical coordinate parameters) and
      // octet 5 (octet location of vertical coordinate parameters
      // not implemented yet

      // octet 6 (grid type)
      this.grid_type = header[5];
   }


   /**
    * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
    *
    * @param in bit input stream with GDS content
    * @deprecated - Call GribGDSFactory.getGDS()
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NoValidGribException  if stream contains no valid GRIB file
    * @see jgrib.GribGDSFactory#getGDS(BitInputStream)
    */
   @Deprecated
   public GribRecordGDS(BitInputStream in) 
   		throws IOException, NoValidGribException
   {
      int[] data;

      // octets 1-3 (Length of GDS)
      data = in.readUI8(3);
      this.length = Bytes2Number.uint3(data[0], data[1], data[2]);

      // octets 4-5 not implemented yet

      data = in.readUI8(this.length - 3);

      // octet 6 (grid type)
      this.grid_type = data[2];
      if (this.grid_type != 0 && this.grid_type != 10)
         throw new NoValidGribException("GribRecordGDS: Only supporting grid type 0 " +
               "(latlon grid) and 10 (rotated latlon grid).");


      data = in.readUI8(this.length - 4);

      // octets 7-8 (number of points along a parallel)
      this.grid_nx = Bytes2Number.uint2(data[3], data[4]);

      // octets 9-10 (number of points along a meridian)
      this.grid_ny = Bytes2Number.uint2(data[5], data[6]);

      // octets 11-13 (latitude of first grid point)
      this.grid_lat1 = Bytes2Number.int3(data[7], data[8], data[9]) / 1000.0;

      // octets 14-16 (longitude of first grid point)
      this.grid_lon1 = Bytes2Number.int3(data[10], data[11], data[12]) / 1000.0;

      // octet 17 (resolution and component flags -> 128 == increments given.)
      this.grid_mode = data[13];
      if (this.grid_mode != 128 && this.grid_mode != 0)
         throw new NoValidGribException("GribRecordGDS: No other component flag than 128 " +
               "(increments given) or 0 (not given) supported. " +
               "Current is: " + this.grid_mode);

      // octets 18-20 (latitude of last grid point)
      this.grid_lat2 = Bytes2Number.int3(data[14], data[15], data[16]) / 1000.0;

      // octets 21-23 (longitude of last grid point)
      this.grid_lon2 = Bytes2Number.int3(data[17], data[18], data[19]) / 1000.0;

      // increments given
      if (this.grid_mode == 128)
      {

         // octets 24-25 (x increment)
         this.grid_dx = Bytes2Number.uint2(data[20], data[21]) / 1000.0;

         // octets 26-27 (y increment)
         this.grid_dy = -Bytes2Number.uint2(data[22], data[23]) / 1000.0;

         // octet 28 (point scanning mode)
         this.grid_scan = data[24];
         if ((this.grid_scan & 63) != 0)
            throw new NoValidGribException("GribRecordGDS: This scanning mode (" + this.grid_scan +
                  ") is not supported.");
         if ((this.grid_scan & 128) != 0) this.grid_dx = -this.grid_dx;
// rdg - changed to != 64 here because table 8 shows -j if bit NOT set
         if ((this.grid_scan & 64) != 64) this.grid_dy = -this.grid_dy;
//         if ((this.grid_scan & 64) != 0) this.grid_dy = -this.grid_dy;
      }
      else
      {
         // calculate increments
         this.grid_dx = (this.grid_lon2 - this.grid_lon1) / (this.grid_nx - 1);
         this.grid_dy = (this.grid_lat2 - this.grid_lat1) / (this.grid_ny - 1);
      }

      // Original code
      //if (this.grid_type == 10)
      //{
      //   // octets 33-35 (lat of s.pole)
      //   this.grid_latsp = Bytes2Number.int3(data[29], data[30], data[31]) / 1000.0;
      //
      //   // octets 36-38 (lon of s.pole)
      //   this.grid_lonsp = Bytes2Number.int3(data[32], data[33], data[34]) / 1000.0;
      //
      //   // octets 39-42 (angle of rotation)
      //   this.grid_rotang = Bytes2Number.int4(data[35], data[36], data[37], data[38]) / 1000.0;
      //}

      // Code inserted by Peter Gylling - peg@fomfrv.dk, 2003-07-08
      //
      // This switch uses the grid_type to define how to handle the
      // southpole information.
      //
      switch (this.grid_type) {
      case 0:
          // Standard Lat/Lon grid, no rotation
          this.grid_latsp = -90.0;
          this.grid_lonsp = 0.0;
          this.grid_rotang = 0.0;
          break;

      case 10:
          // Rotated Lat/Lon grid, Lat (octets 33-35), Lon (octets 36-38), rotang (octets 39-42)
          this.grid_latsp = Bytes2Number.int3(data[29], data[30], data[31]) / 1000.0;
          this.grid_lonsp = Bytes2Number.int3(data[32], data[33], data[34]) / 1000.0;
          this.grid_rotang = Bytes2Number.int4(data[35], data[36], data[37], data[38]) / 1000.0;
          break;

      default:
          // No knowledge yet
          // NEED to fix this later, if supporting other grid types
          this.grid_latsp = Double.NaN;
          this.grid_lonsp = Double.NaN;
          this.grid_rotang = Double.NaN;
          break;
      }
      // Back on original code

   }


// *** public methods **************************************************************

   // rdg - the basic getters can remain here, but other functionality should
   //    be moved to the child GribGDSxxxx classes.  For now, overriding these
   //    methods will work just fine.

   // peg - turned all common methods into abstract methods, so it will become
   //       easier to subclass with a new GDS type class, this way it's much
   //       more clear which methods is standard for all GDS types

   /**
    * Get length in bytes of this section.
    *
    * @return length in bytes of this section
    */
   	public abstract int getLength();


   /**
    * Get type of grid. 
    *
    * @return type of grid
    */
   	public abstract int getGridType();

   /**
    * Get number of grid columns.
    *
    * @return number of grid columns
    */
   public abstract int getGridNX();


   /**
    * Get number of grid rows.
    *
    * @return number of grid rows.
    */
   public abstract int getGridNY();

   /**
    * Get y-coordinate/latitude of grid start point.
    *
    * @return y-coordinate/latitude of grid start point
    */
   public abstract double getGridLat1();

   /**
    * Get x-coordinate/longitude of grid start point.
    *
    * @return x-coordinate/longitude of grid start point
    */
   public abstract double getGridLon1();

   /**
    * Get grid mode. 
    * <i>Only 128 (increments given) supported so far.</i>
    *
    * @return grid mode
    */
   public abstract int getGridMode();

   /**
    * Get x-increment/distance between two grid points.
    *
    * @return x-increment
    */
   public abstract double getGridDX();


   /**
    * Get y-increment/distance between two grid points.
    *
    * @return y-increment
    */
   public abstract double getGridDY();


   /**
    * Get scan mode (sign of increments). 
    * <i>Only 64, 128 and 192 supported so far.</i>
    *
    * @return scan mode
    */
   public abstract int getGridScanmode();

   /**
    * Get all longitide coordinates
    * @return longtitude as double
    */
   public abstract double[] getXCoords();

   /**
    * Get all latitude coordinates
    * @return latitude as double
    */
   public abstract double[] getYCoords();

   /**
    * Get grid coordinates in longitude/latitude
    * @return longitide/latituide as doubles
    */
   public abstract double[] getGridCoords();
  
   /**
    * Table J.Resolution and Component Flags,
    * bit 5 (from left) = 2^(8-5) = 8 = 0x08 :
    *   false = u and v components are relative to east, north
    *   true = u and v components are relative to  grid x,y direction (i,j)
    *
    * @return true/false
 	*/
   public abstract boolean isUVEastNorth();

   /**
    * Overrides Object.hashCode() to be used in hashTables
    * 
    * @see java.lang.Object#hashCode()
    */   
   public abstract int hashCode();
   
   /**
    * Overrides Object.equals() - perfect for testing
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public abstract boolean equals(Object obj);

   /**
    * rdg - added this method to be used in a comparator for sorting while
    *       extracting records.
    * Not currently used in the JGrib library, but is used in a library I'm
    *    using that uses JGrib.
    * @param gds - GribRecordGDS
    * @return - -1 if gds is "less than" this, 0 if equal, 1 if gds is "greater than" this.
    * 
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   	public abstract int compare(GribRecordGDS gds);

   /**
    * Get a string representation of this GDS.
    *
    * @return string representation of this GDS
    * @see java.lang.Object#toString()
    */
   public abstract String toString();
      
   /**
    * NOTE: This method must remain here, so we don't 
    * break backward compability - thus this method
    * does not make any sence if the grid type isn't
    * standard lat/lon or rotated lat/lon
    * 
    * Get y-coordinate/latitude of grid end point.
    *
    * @return y-coordinate/latitude of grid end point
    */
   public double getGridLat2()
   {
      return this.grid_lat2;
   }


   /**
    * NOTE: This method must remain here, so we don't 
    * break backward compability - thus this method
    * does not make any sence if the grid type isn't
    * standard lat/lon or rotated lat/lon
    * 
    * Get x-coordinate/longitude of grid end point.
    *
    * @return x-coordinate/longitude of grid end point
    */
   public double getGridLon2()
   {
      return this.grid_lon2;
   }

   /**
    * NOTE: This method must remain here, so we don't 
    * break backward compability - thus this method
    * does not make any sence if the grid type isn't
    * standard lat/lon or rotated lat/lon
    * 
    * Get y-coordinate/latitude of south pole of a rotated latitude/longitude grid.
    *
    * @return latitude of south pole
    */
   public double getGridLatSP()
   {
      return this.grid_latsp;
   }


   /**
    * NOTE: This method must remain here, so we don't 
    * break backward compability - thus this method
    * does not make any sence if the grid type isn't
    * standard lat/lon or rotated lat/lon
    * 
    * Get x-coordinate/longitude of south pole of a rotated latitude/longitude grid.
    *
    * @return longitude of south pole
    */
   public double getGridLonSP()
   {
      return this.grid_lonsp;
   }


   /**
    * NOTE: This method must remain here, so we don't 
    * break backward compability - thus this method
    * does not make any sence if the grid type isn't
    * standard lat/lon or rotated lat/lon
    * 
    * Get grid rotation angle of a rotated latitude/longitude grid.
    *
    * @return rotation angle
    */
   public double getGridRotAngle()
   {
      return this.grid_rotang;
   }

}


