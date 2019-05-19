/**
 * ===============================================================================
 * $Id: GribGDSFactory.java,v 1.6 2006/07/31 11:55:20 frv_peg Exp $
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
 * GribGDSFactory.java  1.0  10/01/2002
 *
 */

package jgrib;

import java.io.IOException;


/**
 * GribGDSFactory determines the proper subclass of GribRecordGDS to create.
 * Extend GribRecordGDS to add a GDS type for your definition.
 * Add types to the switch statement to create an instance of your new type
 *
 * TODO - only a few types are supported so far
 *
 * @author  Capt Richard D. Gonzalez
 * @version 1.0
 */

public class GribGDSFactory
{
	/**
	 * Default contructor 
	 * 
	 * Needs to be private, so this
	 * class can become singleton, ie you 
	 * don't subclass it.
	 */
   private GribGDSFactory(){}

   /**
    * Determines the Grid type and calls the appropriate constructor (if it
    * exists)
    *
    * See: <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table6.html">table 6</a>
    * 
    * @param in bit input stream with GDS content
    * @return GribRecordGDS
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NoValidGribException  if stream contains no valid GRIB file
    * @throws NotSupportedException if GRIB type is not supported in JGRIB
    */
   public static GribRecordGDS getGDS(BitInputStream in) throws IOException,
          NoValidGribException,NotSupportedException
   {

      int[] data;
      // peg - length never used
      //int length;
      int grid_type;

      // octets 1-6 give the common GDS data - before the Table D unique data
      data = in.readUI8(6);

      // octet 6 (grid type - see table 6)
      grid_type = data[5];

      switch(grid_type){
         case(0):
            return new GribGDSLatLon(in,data);
         case(1):
            throw new NotSupportedException("GribGDSFactory: GRiB type "
                  +grid_type+": Mercator Projection is not supported yet");
         case(2):
            throw new NotSupportedException("GribGDSFactory: GRiB type "
                  +grid_type+": Gnomonic Projection is not supported yet");
         case(3):
            return new GribGDSLambert(in,data);
         case(4):
            throw new NotSupportedException("GribGDSFactory: GRiB type "
                  +grid_type+": Gaussian Lat/Lon Projection is not supported yet");
         case(5):
            return new GribGDSPolarStereo(in,data);
         case(6):
            throw new NotSupportedException("GribGDSFactory: GRiB type " +
                  grid_type+": Universal Transverse (UTM) Projection " +
                  "is not supported yet");
         case(7):
            throw new NotSupportedException("GribGDSFactory: GRiB type "
                  +grid_type+": Simple Polyconic Projection is not supported yet");
         case(8):
            throw new NotSupportedException("GribGDSFactory: GRiB type "
                  +grid_type+": Alber's equal-area Projection is not supported yet");
         case(9):
            throw new NotSupportedException("GribGDSFactory: GRiB type "
                  +grid_type+": Miller's Cylindrical Projection is not supported yet");
         case(10):
            return new GribGDSLatLon(in,data);
         default:
            throw new NotSupportedException("GribGDSFactory: GRiB type "
                  +grid_type+" not supported yet");
      }
   }
}


