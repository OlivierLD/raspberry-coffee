/**
 * ===============================================================================
 * $Id: SetupLog.java,v 1.1 2006/07/25 13:38:42 frv_peg Exp $
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
package jgrib.examples;

import java.net.URL;

/**
 * Demonstration class.
 *
 * Demonstrate how to get access to DEBUG information from the
 * JGRIB library. Logging is based upon Log4J, so we test before
 * using the library whether Log4J is allready configured.
 *
 * This way the standard Log4J warnings can be turned off, if
 * no logging is desired.
 *
 * If Log4J is configured the JGRIB library will not touch the
 * configuration.
 *
 * Together with this class you will find a config directory
 * holding:<br>
 *  - jgrib_log4j.xml<br>
 *  - log4j.dtd<br>
 *
 * They are used in the example method in this class, which will
 * turn debugging information from JGRIB on, and direct it to
 * a file named in the jgrib_log4j.xml file.
 *
 * @author frv_peg<br>E-mail: frv_peg at users.sourceforge.net
 */
public class SetupLog
{

  /**
   * Method: AddJGribDebugInfo()
   *
   * Call this if you want debug output written to a file.
   *
   * Make sure, that you have write access and isn't violating the
   * settings in the SecurityManager, if applied.
   *
   * Check settings in "config/jgrib_log4j.xml"
   */
  public static void addJGribDebugInfo()
  {

    // Check if log4j configuration file exist
    URL configUrl = SetupLog.class.getResource("config/jgrib_log4j.xml");
    if (configUrl == null)
    {
      System.err.println("Can't configure logging system!\nRequires file: jgrib_log4j.xml\n");
    }

  }

  /**
   * Method: main
   *
   * Use it for testing purposes.
   *
   * @param args - Totally ignored
   */
  public static void main(String... args)
  {

    // Define logging system
    SetupLog.addJGribDebugInfo();
    System.out.println("Testing info statements");
    System.out.println("Testing warning statements");
    // System.out.println("Testing debug statements");
  }

}
