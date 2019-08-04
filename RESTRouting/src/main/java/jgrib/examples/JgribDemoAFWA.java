/**
 * ===============================================================================
 * $Id: JgribDemoAFWA.java,v 1.3 2006/07/27 14:08:37 frv_peg Exp $
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
//import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
//import java.util.List;
import java.util.TimeZone;

import jgrib.GribFile;
import jgrib.GribPDSLevel;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordGDS;
import jgrib.GribRecordIS;
import jgrib.GribRecordLight;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;

/***************************************************************************
 *
 * This class demonstrates several ways to use the Jgrib package.<br>
 * The purpose of this demo program is to help
 * new users of the Jgrib package get familiar with
 * some of the aspects of the package.
 *
 * @author Peter Gylling<br>email: Peg@frv.dk
 * @author Richard D. Gonzalez - heavily modified to work with new changes
 * (AFWA stands for Air Force Weather Agency - I modified JGrib specifically
 * to be able to read GRIB files produced by AFWA)
 *
 * This demo demonstrates three things:
 * 1.  Listing records in the order they appear in the file - useful for
 *        getting record number for extracting single records
 * 2.  Listing short names from the parameter table to see what types are in
 *        the GRIB file.
 * 3.  Sorting and extracting records using the methods in the GribFile class
 *
 * This dumps a lot of text to the screen - you may want to pipe the output to a
 * file, or set the properties of you window to have a large buffer size.
 *
 * @since 1.2
 *
 ****************************************************************************/
public class JgribDemoAFWA {

   /************************************************************************
    *
    * Dumps usage of the class, if called without arguments
    * @param className
    *
    ************************************************************************/
   public void usage(String className) {
      System.out.println();
      System.out.println("Usage of " + className + ":");
      System.out.println("Parameters is optional (Supplied by -D option)");
      System.out.println("GribTabURL=\"Location of gribtab file\"");
      System.out.println("E.g.   -D\"GribTabURL=file:///D:/JGrib/jgrib/tables\"");
      System.out.println();
      System.out.println("java [-D\"GribTabURL=<url>\"] " + className + " <GribFileToRead>");
      System.exit(0);
   }

   /***********************************************************************
    * Dump of meta data<br>
    *
    * @param  args Filename of gribfile to read
    *
    *************************************************************************/
   public static void main(String... args) {

      int recordCount;

      // Function References
      JgribDemoAFWA demo = new JgribDemoAFWA();

      // Test usage
      if (args.length != 1) {
         // Get class name as String
         Class cl = demo.getClass();
         demo.usage(cl.getName());
      }

      // Get UTC TimeZone
      // A list of available ID's show that UTC has ID = 127
      TimeZone tz = TimeZone.getTimeZone("127");
      TimeZone.setDefault(tz);

      // Say hello
      Date now = Calendar.getInstance().getTime();
      System.out.println(now.toString() + " ... Start of JgribDemoAFWA");

      // Reading of grib files must be inside a try-catch block
      try {
         // Create GribFile instance
         GribFile gribFile = new GribFile(args[0]);

         // Get light grib reccord (used to get the meta data)
         GribRecordLight[] gribLight = gribFile.getLightRecords();

         // determine how many GribRecords are stored
         recordCount = gribFile.getRecordCount();
         System.out.println("gribFile reports " + recordCount + " records,");
         System.out.println("the first record's IS and GDS indicate:");

         // Get grib Indicator Section
         GribRecordIS gribIS = gribLight[0].getIS();
         System.out.println(gribIS.toString());

         // Get grib Grid Description Section
         GribRecordGDS[] GDSs = gribFile.getGrids();
         System.out.println("found " + GDSs.length + " grids");
         for (int i=0;i<GDSs.length;i++){
//            GribRecordGDS gribGDS = gribLight[i].getGDS();
            System.out.println(GDSs[i].toString());
         }

         // Get grib Product Definition Section
         //String currentParam = null;
         //String oldParam = null;
         System.out.println(gribLight[0].getPDS().headerToString());

         // Get grib scan mode to define reading direction
//         int scan = gribGDS.getGridScanmode();
//         System.out.println("\n      Scan order is " + scan);

         // Get data from first record. remember that first record is number 1
         // and not number 0.
         GribRecord rec = gribFile.getRecord(1);
         GribRecordBDS gribBDS = rec.getBDS();
         System.out.println(gribBDS.toString());

         // dump out the parameters
         System.out.println();
         gribFile.listParameters(System.out);

         // compare the Lat/Lon values stored in the GRIB file (if there) with
         //    those computed by the projection
//         compareCoords(gribFile);

         // 2. A short test to see what parameter types the file contains
         for (int i = 0;i<gribFile.getTypeNames().length;i++){
            System.out.println((gribFile.getTypeNames())[i]);
         }
         System.out.println();

         tester(gribFile);

      // Catch thrown errors from GribFile
      } catch (FileNotFoundException noFileError) {
         System.err.println("FileNotFoundException : " + noFileError);
      } catch (IOException ioError) {
         System.err.println("IOException : " + ioError);
      } catch (NoValidGribException noGrib) {
         System.err.println("NoValidGribException : " + noGrib);
      } catch (NotSupportedException noSupport) {
         System.err.println("NotSupportedException : " + noSupport);
      }

      // Goodbye message
      now = Calendar.getInstance().getTime();
      System.out.println("\n" + now.toString() + " ... End of JgribDemoAFWA!");

   }

   /**
    * A method which extracts sorted records from the GRIB file.  This is the
    * intended method for a full extraction of all the records.
    * @param gribFile
    */
   private static void tester(GribFile gribFile){
      GribFile gf = gribFile;
      String[] types;
      GribRecordGDS[] gdsArray;
      int[] levelTypes;
      Date[] dates;
      GribPDSLevel[] levels;
      GribRecord[] sortedRecords = new GribRecord[gf.getRecordCount()];
      int count = 0;
      System.out.println("Output of GRIB File Information:");
      System.out.println("Type; Level id:Level name; Grid Size; Forecast Time [Counter]");
      // get the parameter types
      types = gf.getTypeNames();
      for (int count1=0;count1 < types.length;count1++){
         String type = types[count1];
         // get the grids for each type
         gdsArray = gf.getGridsForType(type);
         for (int count2=0;count2< gdsArray.length; count2++){
            GribRecordGDS gds = gdsArray[count2];
            // get the vertical coordinates for each type, grid
            levelTypes = gf.getZunitsForTypeGrid(type,gds);
            for (int count3=0;count3<levelTypes.length;count3++){
               int unit = levelTypes[count3];
               // get the levels for each type, grid, zUnit
               System.out.println("For type [" + type + "], unit=" + unit);
               levels = gf.getLevelsForTypeGridUnit(type,gds,unit);
               for (int count4=0;count4<levels.length;count4++){
                  GribPDSLevel level = levels[count4];
                  // get the dates for each type, grid, level
                  dates = gf.getDatesForTypeGridLevel(type,gds,level);
                  for (int count5=0;count5<dates.length;count5++){
                     Date date = dates[count5];
                     // get the record for each type, grid, level, date
                     // if this works properly, should get every record
//                     try{
                        System.out.println("Record: " + type + "; " +
                                            unit + ":" + level.getLevel() + "; " +
                                            gds.getGridNX() + "x" + gds.getGridNY() +
                                            "; " + date + "["+count+"]");
                        count++;
// rdg - uncommenting the next line (and the try and catch blocks) would store
//       the records in an array that could be passed to some other program.
//                        sortedRecords[count++] = gf.getRecord(type,gds,level,date);
//                     }catch (java.io.IOException ioe){
//                        System.err.println("Couldn't convert GribLightRecords to GribRecords - exiting");
//                        System.exit(-1);
//                     }catch (NoValidGribException nvge){
//                        System.err.println("Couldn't convert GribLightRecords to GribRecords - exiting");
//                        System.exit(-1);
//                     }catch (NotSupportedException nse){
//                        System.err.println("Couldn't convert GribLightRecords to GribRecords - exiting");
//                        System.exit(-1);
//                     }
                  }
               }
            }
         }
      }
      System.out.println("GribFile has " + gf.getRecordCount() + " records; " +
                         "Sorted Records Array length is " + sortedRecords.length +
                         " and loop count is " + count + " - if these don't all " +
                         "match, JGrib didn't properly process all the records");
   }

   /**
    * @param gribFile
    */
   public static void compareCoords(GribFile gribFile) {
      try {
         // compare computed latitude and longitude to records in file
         GribRecordLight[] latRecords = gribFile.getRecordForType("Lat");
         GribRecordLight[] lonRecords = gribFile.getRecordForType("Lon");
         GribRecord gribLat = null;
         GribRecord gribLon = null;
         double[] latCoords;
         double[] lonCoords;
         float[] latValues;
         float[] lonValues;

         double error, sumErrors = 0, rmse, maxError=0;

         //int latLength;
         int lonLength;
         for (int i=0;i<latRecords.length;i++){
            gribLat = new GribRecord(latRecords[i]);
            gribLon = new GribRecord(lonRecords[i]);
            lonLength = gribLat.getGDS().getGridNX();
            //latLength = gribLon.getGDS().getGridNY();
            latCoords = gribLat.getGridCoords();
            lonCoords = gribLon.getGridCoords();
            System.out.println("\nLats/Lons for level: " + gribLat.getLevel());
            latValues = gribLat.getBDS().getValues();
            lonValues = gribLon.getBDS().getValues();

            for (int n=0;n<latValues.length;n++){
               error = latValues[n] - latCoords[n*2+1];
               sumErrors += error*error;
               if (error > maxError) maxError = error;
            }
            rmse = Math.sqrt(sumErrors/latValues.length);
            System.out.println("lat RMSE = " + rmse + "Max error = " + maxError);

            sumErrors = 0;
            maxError = 0;
            for (int n=0;n<lonValues.length;n++){
               error = lonValues[n] - lonCoords[n*2];
               sumErrors += error*error;
               if (error > maxError) maxError = error;
            }
            rmse = Math.sqrt(sumErrors/latValues.length);
            System.out.println("lon RMSE = " + rmse + "Max error = " + maxError);


            for (int n=0;n<latValues.length;n=n+lonLength){
               System.out.println("lat[" + n + "]: " + latValues[n] +
                                  " latCoords[" + (n*2+1) +"]: " + latCoords[n*2+1]);
            }
            for (int n=0;n<lonLength;n++){
               System.out.println("lon[" + n + "]: " + lonValues[n] +
                                  " lonCoords[" + (2*n) +"]: " + lonCoords[2*n]);
            }
         }
      } catch (FileNotFoundException noFileError) {
         System.err.println("FileNotFoundException : " + noFileError);
      } catch (IOException ioError) {
         System.err.println("IOException : " + ioError);
      } catch (NoValidGribException noGrib) {
         System.err.println("NoValidGribException : " + noGrib);
      } catch (NotSupportedException noSupport) {
         System.err.println("NotSupportedException : " + noSupport);
      }
   }
}
