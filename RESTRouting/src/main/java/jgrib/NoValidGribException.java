/**
 * ===============================================================================
 * $Id: NoValidGribException.java,v 1.3 2006/07/25 13:46:23 frv_peg Exp $
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
 * NoValidGribException.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 */

package jgrib;


/**
 * A class that represents an exception during GRIB file operations.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class NoValidGribException extends Exception
{

   /**
    * peg - Added default generated variable 
    * Default variable, that must be implemented, when
    * extending Exception
	*/
	private static final long serialVersionUID = 1L;

	/**
    * Construct a new Exception with message <tt>msg</tt>.
    *
    * @param msg error message
    */
   public NoValidGribException(String msg)
   {

      super(msg);
   }
}

