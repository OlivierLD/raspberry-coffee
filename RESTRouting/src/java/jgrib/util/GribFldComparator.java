/**
 * ===============================================================================
 * $Id: GribFldComparator.java,v 1.3 2006/07/25 13:44:57 frv_peg Exp $
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
package jgrib.util;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: torc
 * Date: Jun 19, 2003
 * Time: 11:07:15 AM
 * To change this template use Options | File Templates.
 */

/**
 * GRIB field comparator
 * Compares all primary fields of a Grib Record
 */
public class GribFldComparator implements Comparator<GribRecHeader>
{
   
	/**
	 * field order when sorting
	 */
   private int mOrder[] = GribRecHeader.mDefaultFldOrder;

   /**
    * Default constructor
    */
   public GribFldComparator(){}

   /**
    * Constructor
    * @param aFieldOrder
    */
   public GribFldComparator(int[] aFieldOrder)
   {
      if (aFieldOrder.length < 1 || 
          aFieldOrder.length > GribRecHeader.mDefaultFldOrder.length)
      {
         throw new RuntimeException("Bad Field Order");
      }
      mOrder = aFieldOrder;
   }

   /**
    * 
    * @param aFldIndex
    * @return - Field id
    */
   public int getFieldId(int aFldIndex)
   {
      if (aFldIndex < 0 || aFldIndex >= mOrder.length)
      {
         return -1;
      }
      return mOrder[aFldIndex];
   }

   /**
    * Compare all record fields
    *
    * @param o1
    * @param o2
    * @return 0:All fields equal ,    +-(F+1): Fields >=F not equal
    */
   public int compare(GribRecHeader o1, GribRecHeader o2)
   {
      if (o1 == o2)
      {
         return 0;
      }
      if (o1 == null)
      {
         return 1;
      }
      if (o2 == null)
      {
         return -1;
      }
      GribRecHeader hd = o1;
      for (int fld = 0; fld < mOrder.length; ++fld)
      {
         int res = hd.compareField(mOrder[fld], o2);
         if (res != 0)
         {
            return res > 0 ? (fld + 1) : -(fld + 1);
         }
      }
      return 0;
   }
}
