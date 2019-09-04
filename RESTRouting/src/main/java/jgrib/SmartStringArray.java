/**
 * ===============================================================================
 * $Id: SmartStringArray.java,v 1.3 2006/07/25 13:46:23 frv_peg Exp $
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
package jgrib;

/**
* Class to handle addition of elements
* Kjell RXang, 18/03/2002
*/
public class SmartStringArray
{
	/**
	 * "stack pointer" to keep track of position in the array
	 */
   int sp = 0;

   /**
    * The array of strings
    */
   private String[] array;

   /**
    * How much to add
    */
   private int growthSize;

   /**
    * Start value
    */
   private static final int START = 64;

   /**
    * Constructor
    */
   public SmartStringArray()
   {
      this(START);
   }

   /**
    * Constructor
    * @param initialSize
    */
   public SmartStringArray( int initialSize )
   {
      this( initialSize, ( initialSize / 4 ) );
   }


   /**
    * Constructor
    * @param initialSize
    * @param growthSize
    */
   public SmartStringArray( int initialSize, int growthSize )
   {
      this.growthSize = growthSize;
      array = new String[ initialSize ];
   }

   /**
    * Constructor
    * @param anArr
    */
   public SmartStringArray(String[] anArr)
   {
      array = anArr;
      growthSize = START/4;
      sp = array.length;
   }

   /**
    * Reset stack pointer
    */
   public void reset()
   {
      sp = 0;
   }


   /**
   * Add one string
   * @param str
   */
   public void add(String str )
   {
      if( sp >= array.length ) // time to grow!
      {
         String[] tmpArray = new String[ array.length + growthSize ];
         System.arraycopy( array, 0, tmpArray, 0, array.length );
         array = tmpArray;
      }
      array[ sp ] = str;
      sp += 1;
   }

   /**
   * Return normal array
   * @return strings in an array
   */
   public String[] toArray()
   {
      String[] trimmedArray = new String[ sp ];
      System.arraycopy( array, 0, trimmedArray, 0, trimmedArray.length );
      return trimmedArray;
   }

   /**
    * @return size
    */
   public int size()
   {
      return sp;
   }

   /**
   * Split string in an array
   * @param token
   * @param string
   * @return String array
   */
   public static String[] split( String token, String string )
   {
      SmartStringArray ssa = new SmartStringArray();

      int previousLoc = 0;
      int loc = string.indexOf( token, previousLoc );
      if (loc == -1)
      {
         // No token in string
         ssa.add(string);
         return( ssa.toArray() );
      }

      do
      {
         //String sub = string.substring( previousLoc, loc );
         /*
         if (!sub.trim().isEmpty()) {
            ssa.add(sub);
         }
         */
         ssa.add( string.substring( previousLoc, loc ) );
         previousLoc = ( loc + token.length() );
         loc = string.indexOf( token, previousLoc );
      }
      while( ( loc != -1 ) && ( previousLoc < string.length() ) );

      //String sub = string.substring( previousLoc);
      /*
      if (!sub.trim().isEmpty()) {
         ssa.add(sub);
      }
      */
      ssa.add( string.substring( previousLoc ) );

      return( ssa.toArray() );
   }

   /**
   * Remove array elements with blanks and blanks inside a string
   * Alter number of elements
   * @param inArr
   * @return string array
   */
   public static String[] removeBlanks(String[] inArr)
   {
      // Count number of non blanks

      int nonBl = 0;
      for (int i=0; i<inArr.length; i++)
      {
         String inStr = inArr[i].trim();
         if (inStr.length() != 0)
         {
            nonBl++;
         }
         inArr[i] = inStr;
      }
      if (nonBl == inArr.length)
      {
         return inArr;
      }

      // Copy to new
      String[] outArr = new String[nonBl];
      nonBl = 0;
      for (int i=0; i<inArr.length; i++)
      {
         String inStr = inArr[i].trim();
         if (inStr.length() != 0)
         {
            outArr[nonBl] = inStr;
            nonBl++;
         }
      }
      return outArr;
   }

   /**
   * Join some elements with a token in between
   * @param token
   * @param strings
   * @return joined string
   */
   public static String join( String token, String[] strings )
   {
      StringBuffer sb = new StringBuffer();

      for( int x = 0; x < strings.length; x++ )
      {
         if (strings[x] != null)
         {
            sb.append( strings[x] );
         }

         if( x < ( strings.length - 1 ) )
         {
            sb.append( token );
         }
      }

      return( sb.toString() );
   }

   /**
    * Prints the array
    * @param arr
    */
   private static void printArr(String[] arr)
   {
      for (int i=0; i<arr.length; i++)
      {
         System.out.print(arr[i]);
         if (i != (arr.length-1))
         {
            System.out.print(" . ");
         }
      }
      System.out.println();
   }


   /**
    * @param argv
    */
   public static void main (String[] argv)
   {
      String s1 = "aaa.bbb.ccc";
      String s2 = "aaa..";
      String s3 = "...";

      String token = ".";

      // peg - variable never used
      //String[] res;

      printArr(split(token, s1));
      printArr(split(token, s2));
      printArr(split(token, s3));

   }



}
