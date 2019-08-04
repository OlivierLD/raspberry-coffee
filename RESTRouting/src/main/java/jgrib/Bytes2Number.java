/**
 * ===============================================================================
 * $Id: Bytes2Number.java,v 1.4 2006/07/31 11:55:20 frv_peg Exp $
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
 * Bytes2Number.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 */

package jgrib;


/**
 * A class that contains several static methods for converting multiple bytes into
 * one float or integer.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class Bytes2Number
{
	
	
   /**
    * Convert two bytes into a signed integer.
    *
    * @param a higher byte
    * @param b lower byte
    *
    * @return integer value
    */
   public static int int2(int a, int b)
   {

      return (1 - ((a & 128) >> 6)) * ((a & 127) << 8 | b);
   }


   /**
    * Convert three bytes into a signed integer.
    *
    * @param a higher byte
    * @param b middle part byte
    * @param c lower byte
    *
    * @return integer value
    */
   public static int int3(int a, int b, int c)
   {

      return (1 - ((a & 128) >> 6)) * ((a & 127) << 16 | b << 8 | c);
   }


   /**
    * Convert four bytes into a signed integer.
    *
    * @param a highest byte
    * @param b higher middle byte
    * @param c lower middle byte
    * @param d lowest byte
    *
    * @return integer value
    */
   public static int int4(int a, int b, int c, int d)
   {

      return (1 - ((a & 128) >> 6)) * ((a & 127) << 24 | b << 16 | c << 8 | d);
   }


   /**
    * Convert two bytes into an unsigned integer.
    *
    * @param a higher byte
    * @param b lower byte
    *
    * @return integer value
    */
   public static int uint2(int a, int b)
   {

      return a << 8 | b;
   }


   /**
    * Convert three bytes into an unsigned integer.
    *
    * @param a higher byte
    * @param b middle byte
    * @param c lower byte
    *
    * @return integer value
    */
   public static int uint3(int a, int b, int c)
   {

      return a << 16 | b << 8 | c;
   }


   /**
    * Convert four bytes into a float value.
    *
    * @param a highest byte
    * @param b higher byte
    * @param c lower byte
    * @param d lowest byte
    *
    * @return float value
    */
   public static float float4(int a, int b, int c, int d)
   {

      int sgn, mant, exp;

      mant = b << 16 | c << 8 | d;
      if (mant == 0) return 0.0f;

      sgn = -(((a & 128) >> 6) - 1);
      exp = (a & 127) - 64;

      // TODO Validate, that this method works always
      return (float) (sgn * Math.pow(16.0, exp - 6) * mant);
   }

}

