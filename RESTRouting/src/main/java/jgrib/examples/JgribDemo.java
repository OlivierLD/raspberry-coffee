/**
 * ===============================================================================
 * $Id: JgribDemo.java,v 1.3 2006/07/27 14:08:37 frv_peg Exp $
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

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import jgrib.GribFile;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;

/***************************************************************************
 *
 * This class demonstrates the use of the Jgrib package.<br>
 * The demonstation shows how to exstract information about
 * the grib file targeted. Purpose of this demo program is to help
 * new users of the Jgrib package to get familiar whith
 * some of the aspects of the package.
 *
 * @author Peter Gylling<br>email: peg at frv.dk
 *
 * See: <a href="http://jgrib.sourceforge.net/">JGRIB homepage</a>
 *
 ****************************************************************************/
public class JgribDemo
{

  /************************************************************************
   *
   * Dumps usage of the class, if called without arguments
   *
   ************************************************************************/
  public static void usage()
  {
    System.out.println();
    System.out.println("Usage of " + JgribDemo.class.getName() + ":");
    System.out.println();
    System.out.println("Parameters is optional (Supplied by -D option)");
    System.out.println("GribTabURL=\"Location of gribtab file\"");
    System.out.println("E.g.   -D\"GribTabURL=file:///D:/gribtabs/default_gribtab\"");
    System.out.println();
    System.out.println("java [-D\"GribTabURL=<url>\"] " + JgribDemo.class.getName() + " <GribFileToRead>");
    System.exit(0);
  }

  /***********************************************************************
   * Dump of meta data<br>
   *
   * @param  args Filename of gribfile to read
   *
   *************************************************************************/
  public static void main(String... args)
  {

    // Test usage
    if (args.length != 1)
    {
      // Get class name as String
      JgribDemo.usage();
    }

    // Get UTC TimeZone
    TimeZone tz = TimeZone.getTimeZone("GMT + 0");
    TimeZone.setDefault(tz);

    // Say hello
    Date now = Calendar.getInstance().getTime();
    System.out.println(now.toString() + " ... Start of " + JgribDemo.class.getName());


    // Reading of grib files must be inside a try-catch block
    try
    {
      // Create GribFile instance
      GribFile gribFile = new GribFile(args[0]);

      // Dump verbose inventory for each record
      gribFile.listRecords(System.out);

    }
    catch (FileNotFoundException noFileError)
    {
      System.err.println("FileNotFoundException : " + noFileError);
    }
    catch (IOException ioError)
    {
      System.err.println("IOException : " + ioError);
    }
    catch (NoValidGribException noGrib)
    {
      System.err.println("NoValidGribException : " + noGrib);
    }
    catch (NotSupportedException noSupport)
    {
      System.err.println("NotSupportedException : " + noSupport);
    }


    // Goodbye message
    now = Calendar.getInstance().getTime();
    System.out.println(now.toString() + " ... End of " + JgribDemo.class.getName());

  }
}
