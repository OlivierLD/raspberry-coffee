/**
 * ===============================================================================
 * $Id: GribRecordLight.java,v 1.4 2006/07/25 13:46:23 frv_peg Exp $
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
 * Created by IntelliJ IDEA.
 * User: Kjell RXang
 * Date: 15.mar.02
 */
package jgrib;

/**
 * @author Benjamin Stark
 *
 */
public class GribRecordLight
{
   /**
     * The indicator section.
     */
    protected GribRecordIS is;

    /**
     * The product definition section.
     */
    protected GribRecordPDS pds;

    /**
     * The grid definition section.
     */
    protected GribRecordGDS gds;

   /**
    * Array with bytes
    */
   protected byte[] buf;

   /**
    *
    * @param aIs IS section
    * @param aPds PDS section
    * @param aGds GDS section
    * @param aBuf buffer with rest of data
    */
   public GribRecordLight(GribRecordIS aIs, GribRecordPDS aPds, GribRecordGDS aGds, byte[] aBuf)
   {
      is = aIs;
      pds = aPds;
      gds = aGds;
      buf = aBuf;
   }

   /**
    *  Get Information record
    * @return an IS record
    */
   public GribRecordIS getIS()
   {
      return is;
   }

   /**
    * Get Product Definition record
    * @return a PDS record
    */
   public GribRecordPDS getPDS()
   {
      return pds;
   }

   /**
    * Get Grid Definition record
    * @return a gds
    */
   public GribRecordGDS getGDS()
   {
      return gds;
   }

   /**
    * Get buffer with bds and bms
    * @return a buffer of BDS and BMS
    */
   public byte[] getBuf()
   {
      return buf;
   }
}
