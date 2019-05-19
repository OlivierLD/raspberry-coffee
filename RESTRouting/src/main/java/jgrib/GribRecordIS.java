/**
 * ===============================================================================
 * $Id: GribRecordIS.java,v 1.4 2006/07/25 13:46:23 frv_peg Exp $
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
 * GribRecordIS.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 *
 * Updated by Capt Richard D. Gonzalez 16 Sep 02
 */

package jgrib;

import java.io.IOException;


/**
 * A class that represents the indicator section (IS) of a GRIB record.
 *
 * @author  Benjamin Stark
 * @author Richard D. Gonzalez - modified to indicate support of GRIB edition 1 only
 * @version 1.1
 *
 */

public class GribRecordIS
{

   /**
    * Length in bytes of GRIB record.
    */
   protected int length;

   /**
    * Length in bytes of IS section.
    * Section length differs between GRIB editions 1 and 2
    * Currently only GRIB edition 1 supported - length is 8 octets/bytes.
    */
   protected int isLength;

   /**
    * Edition of GRIB specification used.
    */
   protected int edition;


   // *** constructors *******************************************************

   /**
    * Constructs a <tt>GribRecordIS</tt> object from a bit input stream.
    *
    * @param in bit input stream with IS content
    * @throws NotSupportedException 
    * @throws IOException           if stream can not be opened etc.
    */
   public GribRecordIS(BitInputStream in) throws NotSupportedException, IOException
   {

      int[] data = in.readUI8(4);
      // length of GRIB record
      this.length = Bytes2Number.uint3(data[0], data[1], data[2]);

      // edition of GRIB specification
      this.edition = data[3];
      if (edition == 1) {
         this.isLength = 8;
      }
      else{
         throw new NotSupportedException("GRIB edition " + edition +
               " is not yet supported");
      }
   }


   /**
    * Get the byte length of this GRIB record.
    *
    * @return length in bytes of GRIB record
    */
   public int getGribLength()
   {
      return this.length;
   }

   /**
    * Get the byte length of the IS section.
    *
    * @return length in bytes of IS section
    */
   public int getISLength()
   {
      return this.isLength;
   }

   /**
    * Get the edition of the GRIB specification used.
    *
    * @return edition number of GRIB specification
    */
   public int getGribEdition()
   {
      return this.edition;
   }


   /**
    * Get a string representation of this IS.
    *
    * @return string representation of this IS
    */
   public String toString()
   {

      return "    IS section:" + '\n' +
            "        Grib Edition " + this.edition + '\n' +
            "        length: " + this.length + " bytes";
   }

}


