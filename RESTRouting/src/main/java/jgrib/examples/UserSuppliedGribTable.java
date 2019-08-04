/**
 * ===============================================================================
 * $Id: UserSuppliedGribTable.java,v 1.2 2006/07/25 13:45:13 frv_peg Exp $
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jgrib.GribFile;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordGDS;
import jgrib.GribRecordIS;
import jgrib.GribRecordLight;
import jgrib.GribRecordPDS;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;

/****************************************************************************
 * Example class: UserSuppliedGribTable
 *
 * The purpose of this class is to demonstrate a way to
 * make the JGrib library read your own grib table before
 * processing the actual file you want to read.
 *
 * Be aware, that this subject has changed from beta 5 to beta 6,
 * so if you are upgrading from beta 5 - read on carefully.
 *
 * @author Peter Gylling<br>email: peg at frv dot dk
 *
 * @since 1.2
 ****************************************************************************/
public class UserSuppliedGribTable
{
  // Set variables to hold path to files.
  /**
   * The value to set as -D option or by System.setProperty
   */
  public static final String PROPERTY_GRIBFILEPATH = "GribFilePath";

  /**
   * The value to set as -D option or by System.setProperty
   */
  public static final String PROPERTY_GRIBTABPATH = "GribTabPath";

  /**
   * Standard generated main method
   * @param args - Not checked anyway
   */
  public static void main(String... args)
  {
    System.setProperty(PROPERTY_GRIBFILEPATH,
                       "C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\GRIBFiles\\2008\\GRIB_2008_02_26_09_06_49_PST.grb");

//  System.setProperty(PROPERTY_GRIBTABPATH, "jar:file:/C:/_myWork/_ForExport/dev-corner/olivsoft/GRIBApplet11g/public_html/apps/gribDisplay.jar!/tables");

    // This one works.
    System.setProperty("GribTabURL",
                       "jar:http://donpedro.lediouris.net/oliv-jnpl/apps/gribDisplay.jar!/tables");
//                     "jar:file:/C:/_myWork/_ForExport/dev-corner/olivsoft/GRIBApplet11g/public_html/apps/gribDisplay.jar!/tables");

    // Test if GRIB file is found on disk
    String gribFilePath = System.getProperty(PROPERTY_GRIBFILEPATH);
    System.out.println("File [" + gribFilePath + "]");
    File grib = new File(gribFilePath);
    if (!grib.exists())
    {
      System.out.println("File: [" + gribFilePath + "] is not found on disk");
      System.exit(0);
    }

    // Test if GRIBTAB file is found on disk
    String gribTabPath = System.getProperty(PROPERTY_GRIBTABPATH, "dummy.stuff");
    File gribTab = new File(gribTabPath);
    if (!gribTab.exists())
    {
      System.out.println("File: [" + gribTabPath + "] is not found on disk");
      //	System.exit(0);
    }
    // Reading of grib files must be inside a try-catch block
    try
    {
      // Set gribtab property to JGRIB library
      //	System.setProperty("GribTabURL", gribTab.toURL().toString());

      // Create GribFile instance
      GribFile gribFile = new GribFile(gribFilePath);

      // Get light grib reccord (used to get the meta data)
      GribRecordLight[] gribLight = gribFile.getLightRecords();
      System.out.println("Processing first record!");

      // determine how many GribRecords are stored
      int recordCount = gribFile.getRecordCount();
      System.out.println("gribFile reports " + recordCount + " records,");
      System.out.println("the first record's META data indicate:");

      // Get grib Information Section
      GribRecordIS gribIS = gribLight[0].getIS();
      System.out.println(gribIS.toString());

      // Get grib Grid Description Section
      GribRecordGDS gribGDS = gribLight[0].getGDS();
      System.out.println(gribGDS.toString());

      // Get grib Product Description Section
      GribRecordPDS gribPDS = gribLight[0].getPDS();
      System.out.println(gribPDS.toString());

      // Get grib scan mode to define reading direction
      int scan = gribGDS.getGridScanmode();
      System.out.println("\tScan order is " + scan + "\n");

      // Get data from first record remember that first record is number 1
      // and not number 0.
      GribRecord rec = gribFile.getRecord(1);
      GribRecordBDS gribBDS = rec.getBDS();
      System.out.println(gribBDS.toString());

      // Catch thrown errors from GribFile
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
  }
}
