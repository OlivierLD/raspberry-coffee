/**
 * ===============================================================================
 * $Id: GribPDSLevelComparator.java,v 1.3 2006/07/25 13:44:57 frv_peg Exp $
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

/**
  * A comparator class used with HashSet-s when storing/sorting Records as
  * they are read.
  *
  * Compares numerous features from the record information to sort according
  * to a time, level, level-type, y-axis, x-axis order
  * @author Capt Richard D. Gonzalez
  * @version 1.0
  */
import java.util.Comparator;

import jgrib.GribPDSLevel;


/**
 * @author rdg
 *
 */
public class GribPDSLevelComparator
  implements Comparator<GribPDSLevel>
{
  /**
   * Method required to implement Comparator.
   * If obj1 is less than obj2, return -1, if equal, return 0, else return 1
   * @param obj1
   * @param obj2
   * @return integer value
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(GribPDSLevel obj1, GribPDSLevel obj2)
  {

    // quick check to see if they're the same PDSLevel
    if (obj1 == obj2)
      return 0;

    float z1;
    float z2;
    //String levelType1;  // Variable never used
    //String levelType2;  // Variable never used
    GribPDSLevel level1;
    GribPDSLevel level2;
    //int check;          // Variable never used

    // get the levels
    level1 = obj1;
    level2 = obj2;

    // compare the levels
    if (level1.getIndex() < level2.getIndex())
      return -1;
    if (level1.getIndex() > level2.getIndex())
      return 1; // if neither, then equal; continue

    // compare the z levels
    z1 = level1.getValue1();
    z2 = level2.getValue1();
    // if the levels are supposed to decrease with height, reverse comparator
    if (!(level1.getIsIncreasingUp()))
    {
      z1 = -z1;
      z2 = -z2;
    }
    if (z1 < z2)
      return -1;
    if (z1 > z2)
      return 1; //if neither, then equal; continue

    z1 = level1.getValue2();
    z2 = level2.getValue2();
    // if the levels are supposed to decrease with height, reverse comparator
    if (!(level1.getIsIncreasingUp()))
    {
      z1 = -z1;
      z2 = -z2;
    }
    if (z1 < z2)
      return -1;
    if (z1 > z2)
      return 1; // last check, if neither, then equal

    return 0;
  } // end of method compare

}
