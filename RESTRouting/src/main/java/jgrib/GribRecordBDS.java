/**
 * ===============================================================================
 * $Id: GribRecordBDS.java,v 1.8 2006/07/25 20:03:28 frv_peg Exp $
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
 * GribRecordBDS.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 */

package jgrib;

import java.io.IOException;


/**
 * A class representing the binary data section (BDS) of a GRIB record.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class GribRecordBDS
{

   /**
    * Constant value for an undefined grid value.
    */
   public static final float UNDEFINED = 99999e20f;

   /**
    * Length in bytes of this BDS.
    */
   protected int length;

   /**
    * Binary scale factor.
    */
   protected int binscale;

   /**
    * Reference value, the base for all parameter values.
    */
   protected float refvalue;

   /**
    * Number of bits per value.
    */
   protected int numbits;

   /**
    * Array of parameter values.
    */
   protected float[] values;

   /**
    * Minimal parameter value in grid.
    */
   protected float minvalue = Float.MAX_VALUE;

   /**
    * Maximal parameter value in grid.
    */
   protected float maxvalue = -Float.MAX_VALUE;

   /**
    * rdg - added this to prevent a divide by zero error if variable data empty
    *
    * Indicates whether the BMS is represented by a single value
    *   -  Octet 12 is empty, and the data is represented by the reference value.
    */
    protected boolean isConstant = false;


   // *** constructors *******************************************************

   /**
    * Constructs a <tt>GribRecordBDS</tt> object from a bit input stream.
    * A bit map which indicates grid points where no parameter value is
    * defined is not available.
    *
    * @param in - bit input stream with BDS content
    * @param decimalscale - the exponent of the decimal scale
    *
    * @throws IOException - if stream can not be opened etc.
    * @throws NotSupportedException 
    */
   public GribRecordBDS(BitInputStream in, int decimalscale)
         throws IOException, NotSupportedException
   {
      this(in, decimalscale, null);
   }


   /**
    * Constructs a <tt>GribRecordBDS</tt> object from a bit input stream.
    * A bit map indicates the grid points where no parameter value is defined.
    *
    * @param in - bit input stream with BDS content
    * @param decimalscale - the exponent of the decimal scale
    * @param bms - bit map section of GRIB record
    *
    * @throws IOException - if stream can not be opened etc.
    * @throws NotSupportedException 
    */
   public GribRecordBDS(BitInputStream in, int decimalscale, GribRecordBMS bms)
         throws IOException, NotSupportedException
   {
      int[] data = in.readUI8(11);
      int unusedbits;

      // octets 1-3 (section length)
      this.length = Bytes2Number.uint3(data[0], data[1], data[2]);

      // octet 4, 1st half (packing flag)
      if ((data[3] & 240) != 0)
         throw new NotSupportedException("GribRecordBDS: No other flag " +
                   "(octet 4, 1st half) than 0 (= simple packed floats as " +
                   "grid point data) supported yet in BDS section.");

      // octet 4, 2nd half (number of unused bits at end of this section)
      unusedbits = data[3] & 15;

      // octets 5-6 (binary scale factor)
      this.binscale = Bytes2Number.int2(data[4], data[5]);

      // octets 7-10 (reference point = minimum value)
      this.refvalue = Bytes2Number.float4(data[6], data[7], data[8], data[9]);

      // octet 11 (number of bits per value)
      this.numbits = data[10];
      if (this.numbits == 0)
         isConstant = true;

      // *** read values ************************************************************

      float ref = (float) (Math.pow(10.0, -decimalscale) * this.refvalue);
      float scale = (float) (Math.pow(10.0, -decimalscale) * Math.pow(2.0, this.binscale));

      if (bms != null)
      {
         boolean[] bitmap = bms.getBitmap();

         this.values = new float[bitmap.length];
         for (int i = 0; i < bitmap.length; i++)
         {
            if (bitmap[i])
            {
               if (!isConstant){
                  this.values[i] = ref + scale * in.readUBits(this.numbits);
                  if (this.values[i] > this.maxvalue)
                     this.maxvalue = this.values[i];
                  if (this.values[i] < this.minvalue)
                     this.minvalue = this.values[i];
               }else{// rdg - added this to handle a constant valued parameter
                  this.values[i] = ref;
               }
            }
            else
               this.values[i] = GribRecordBDS.UNDEFINED;
         }
      }
      else
      {
         if (!isConstant){
            this.values = new float[((this.length - 11) * 8 - unusedbits) / this.numbits];

            for (int i = 0; i < values.length; i++)
            {
               this.values[i] = ref + scale * in.readUBits(this.numbits);

               if (this.values[i] > this.maxvalue)
                  this.maxvalue = this.values[i];
               if (this.values[i] < this.minvalue)
                  this.minvalue = this.values[i];
            }
         }else{ // constant valued - same min and max
            this.maxvalue = ref;
            this.minvalue = ref;
         }
      }
   }


   // *** public methods *********************************************************

   /**
    * Get the length in bytes of this section.
    *
    * @return length in bytes of this section
    */
   public int getLength()
   {

      return this.length;
   }


   /**
    * Get the binary scale factor.
    *
    * @return binary scale factor
    */
   public int getBinaryScale()
   {

      return this.binscale;
   }

   /**
    * Get whether this BDS is single valued
    *
    * @return isConstant
    */
   public boolean getIsConstant()
   {
      return this.isConstant;
   }

   /**
    * Get the reference value all data values are based on.
    *
    * @return reference value
    */
   public float getReferenceValue()
   {
      return this.refvalue;
   }


   /**
    * Get number of bits used per parameter value.
    *
    * @return number of bits used per parameter value
    */
   public int getNumBits()
   {
      return this.numbits;
   }


   /**
    * Get data/parameter values as an array of float.
    *
    * @return  array of parameter values
    */
   public float[] getValues()
   {
      return this.values;
   }

   /**
    * Get data/parameter value as a float.
    * @param index 
    *
    * @return  array of parameter values
    * @throws NoValidGribException 
    */
   public float getValue(int index) throws NoValidGribException
   {
      if (index >=0 && index < values.length){
         return this.values[index];
      }
      throw new NoValidGribException("GribRecordBDS: Array index out of bounds");
   }


   /**
    * Get minimum value
    * @return mimimum value
    */
   public float getMinValue()
   {
      return minvalue;
   }

   /**
    * Get maximum value
    * @return maximum value
    */
   public float getMaxValue()
   {
      return maxvalue;
   }


   /**
    * Get a string representation of this BDS.
    *
    * @return string representation of this BDS
    */
   public String toString()
   {
      return "    BDS section:" + '\n' +
            "        min/max value: " + this.minvalue + " / " + this.maxvalue + "\n" +
            "        ref. value: " + this.refvalue + "\n" +
            "        is a constant: " + this.isConstant + "\n" +
            "        bin. scale: " + this.binscale + "\n" +
            "        num bits: " + this.numbits;
   }

}

