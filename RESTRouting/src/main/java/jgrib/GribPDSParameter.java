/**
 * ===============================================================================
 * $Id: GribPDSParameter.java,v 1.4 2006/07/25 13:46:23 frv_peg Exp $
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
 * Title:        JGrib
 * Description:  Class which represents a parameter from a PDS parameter table
 * Copyright:    Copyright (c) 2002
 * Company:      U.S. Air Force
 * @author Capt Richard D. Gonzalez
 * @version 1.0
 */

public class GribPDSParameter {

	/**
	 * Parameter number [0 - 255]
	 */
   protected int number;
   
   /**
    * Parameter name
    */
   protected String name;
   
   /**
    * Parameter description
    */
   protected String description;

   /**
    * Parameter unit
    */
   protected String unit;

   /**
    * Constuctor - Default
    */
   public GribPDSParameter() {
      this.number=0;
      this.name="UNDEF";
      this.description="undefined";
      this.unit="undefined";
   }

   /**
    * Constructor
    * @param aNum - Parameter number
 	* @param aName - Parameter name
 	* @param aDesc - Parameter description
 	* @param aUnit - Parameter unit
 	*/
   public GribPDSParameter(int aNum, String aName, String aDesc, String aUnit){
      this.number=aNum;
      this.name=aName;
      this.description=aDesc;
      this.unit=aUnit;
   }

   /**
    * @return Parameter number
    */
   public int getNumber(){
      return number;
   }

   /**
    * @return Parameter name
    */
   public String getName(){
      return name;
   }

   /**
    * @return Parameter description
    */
   public String getDescription(){
      return description;
   }

   /**
    * @return Parameter unit
    */
   public String getUnit(){
      return unit;
   }

   
   /**
    * Overrides Object.toString()
    * 
    * @see java.lang.Object#toString()
    * @return String representation of the parameter
    */
   public String toString(){
      return number + ":" + name + ":" + description + " [" + unit +"]";
   }

   
   /**
    * Overrides Object.equals()
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    * @return true/false
    */
   public boolean equals(Object obj){
      if (!(obj instanceof GribPDSParameter))
         return false;

      if (this == obj)
         return true;

      GribPDSParameter param = (GribPDSParameter)obj;

      if (name != param.name) return false;
      if (number != param.number) return false;
      if (description != param.description) return false;
      if (unit != param.unit) return false;

      return true;
   }

   /**
    * rdg - added this method to be used in a comparator for sorting while
    *       extracting records.
    * Not currently used in the JGrib library, but is used in a library I'm
    *    using that uses JGrib.
    * @param param to compare
    * @return - -1 if level is "less than" this, 0 if equal, 1 if level is "greater than" this.
    *
    */
   public int compare(GribPDSParameter param){
      if (this.equals(param))
         return 0;

      // check if param is less than this
      // really only one thing to compare because parameter table sets info
      // compare tables in GribRecordPDS
      if (number > param.number) return -1;

      return 1;
   }

}