/**
 * ===============================================================================
 * $Id: GribRecordBMS.java,v 1.2 2006/07/25 13:46:23 frv_peg Exp $
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
 * GribRecordBMS.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 */

package jgrib;

import java.io.IOException;


/**
 * A class that represents the bitmap section (BMS) of a GRIB record. It
 * indicates grid points where no parameter value is defined.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class GribRecordBMS
{

   /**
    * Length in bytes of this section.
    */
   protected int length;

   /**
    * The bit map.
    */
   protected boolean[] bitmap;


   // *** constructors *******************************************************

   /**
    * Constructs a <tt>GribRecordBMS</tt> object from a bit input stream.
    *
    * @param in bit input stream with BMS content
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NoValidGribException  if stream contains no valid GRIB file
    */
   public GribRecordBMS(BitInputStream in) throws IOException, NoValidGribException
   {

      int[] data;
      int[] bitmask = {128, 64, 32, 16, 8, 4, 2, 1};

      data = in.readUI8(3);

      // octets 1-3 (length of section)
      this.length = Bytes2Number.uint3(data[0], data[1], data[2]);

      // read rest of section
      data = in.readUI8(this.length - 3);

      // octets 5-6
      if (data[1] != 0 || data[2] != 0)
         throw new NoValidGribException("GribRecordBMS: No bit map defined here.");

      // create new bit map, octet 4 contains number of unused bits at the end
      this.bitmap = new boolean[(this.length - 6) * 8 - data[0]];

      // fill bit map
      for (int i = 0; i < this.bitmap.length; i++)
         this.bitmap[i] = (data[i / 8 + 3] & bitmask[i % 8]) != 0;
   }


   // *** public methods *********************************************************

   /**
    * Get length in bytes of this section.
    *
    * @return length in bytes
    */
   public int getLength()
   {

      return this.length;
   }


   /**
    * Get bit map.
    *
    * @return bit map as array of boolean values
    */
   public boolean[] getBitmap()
   {

      return this.bitmap;
   }


   /**
    * Get a string representation of this BMS.
    *
    * @return string representation of this BMS
    */
   public String toString()
   {

      return "    BMS section:" + '\n' +
            "        bitmap length: " + this.bitmap.length;
   }

}



