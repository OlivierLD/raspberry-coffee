/**
 * ===============================================================================
 * $Id: GribFileGroup.java,v 1.4 2006/07/31 11:55:20 frv_peg Exp $
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A class to store multiple GribFile-s while allowing access to the GribRecords
 *    of those files as if they were one large file.
 * This came about because it seems most GRIB files are for a single forecast time,
 *    and this will allow an entire forecast period to be built.
 *    Therefore, the methods are biased towards the separate files being the same
 *      model run at different forecast times.  It can be used otherwise, but
 *      files will be sorted according to forecast time.
 *
 * TODO - This is not yet functional - the next item on my "todo" list
 * @author Capt Richard D. Gonzalez
 * @version 1.0
 */

public class GribFileGroup
{


  /**
   * Store the Grib files
   */
  private HashMap<Date, GribFile> files = new HashMap<Date, GribFile>();

  /**
   * Store the dates
   */
  private Date[] dates = null;
  //   HashSet files = new HashSet();

  /**
   * @param filenames
   * @throws FileNotFoundException
   * @throws IOException
   * @throws NotSupportedException
   * @throws NoValidGribException
   */
  public GribFileGroup(String[] filenames)
    throws FileNotFoundException, IOException, NotSupportedException, NoValidGribException
  {
    List<Date> dateList = new ArrayList<Date>();
    for (int i = 0; i < filenames.length; i++)
    {

      String filename = filenames[i];
      GribFile gribFile = new GribFile(filename);
      Date date = gribFile.getRecord(1).getPDS().getLocalForecastTime().getTime();
      dateList.add(date);
      files.put(date, gribFile);
    }
    dates = dateList.toArray(dates);
    Arrays.sort(dates);
  }

  // *** public methods *********************************************************
  // basically, reimplementations of the GribFile methods that adjust for
  //   multiple files.

  /**
   * Get type names
   * @return array with names
   */
  public String[] getTypeNames()
  {
    GribFile gribFile = null;
    String[] allTypeNames = null;
    HashSet<String> typeNames = new HashSet<String>();
    for (int i = 0; i < dates.length; i++)
    {
      Date date = dates[i];
      gribFile = files.get(date);
      String[] types = gribFile.getTypeNames();
      for (int j = 0; j < types.length; j++)
      {
        typeNames.add(types[j]);
      }
    }
    allTypeNames = typeNames.toArray(allTypeNames);
    return allTypeNames;
  }

  /**
   * Get Light GRIB records
   * @return Array with Light Grib Records
   */
  public GribRecordLight[] getLightRecords()
  {
    GribFile gribFile = null;
    GribRecordLight[] grls = null;
    List<GribRecordLight> grlList = new ArrayList<GribRecordLight>();
    for (int i = 0; i < dates.length; i++)
    {
      Date date = dates[i];
      gribFile = files.get(date);
      grls = gribFile.getLightRecords();
      for (int j = 0; j < grls.length; j++)
      {
        grlList.add(grls[j]);
      }
    }
    grls = grlList.toArray(grls);
    return grls;
  }

  /**
   * Get get grids
   * @return array with grids
   */
  public GribRecordGDS[] getGrids()
  {
    GribFile gribFile = null;
    GribRecordGDS[] gdss = null;
    List<GribRecordGDS> gdsList = new ArrayList<GribRecordGDS>();
    for (int i = 0; i < dates.length; i++)
    {
      Date date = dates[i];
      gribFile = files.get(date);
      gdss = gribFile.getGrids();
      for (int j = 0; j < gdss.length; j++)
      {
        gdsList.add(gdss[j]);
      }
    }
    gdss = gdsList.toArray(gdss);
    return gdss;
  }


  /**
   * Get the number of records this GRIB file contains.
   *
   * @return number of records in this GRIB file
   */
  public int getRecordCount()
  {
    GribFile gribFile = null;
    int recordCount = 0;
    for (int i = 0; i < dates.length; i++)
    {
      Date date = dates[i];
      gribFile = files.get(date);
      recordCount = recordCount + gribFile.getRecordCount();
    }
    return recordCount;
  }


  /**
   * Print out overview of GRIB file content.
   *
   * @param out print stream the output is written to
   *
   * @throws IOException            if a record can not be opened etc.
   * @throws NoValidGribException   if a record is no valid GRIB record
   * @throws NotSupportedException
   */
  public void listRecords(PrintStream out)
    throws IOException, NoValidGribException, NotSupportedException
  {
    GribFile gribFile = null;
    // peg - recordCount and record never used
    //int recordCount;
    //int record;
    for (int i = 0; i < dates.length; i++)
    {
      Date date = dates[i];
      gribFile = files.get(date);
      gribFile.listRecords(out);
    }
  }

  /**
   * Method added by Richard Gonzalez 23 Sep 02.
   *
   * Print out listing of parameters in GRIB file.
   *
   * @param out print stream the output is written to
   */
  public void listParameters(PrintStream out)
  {
    GribFile gribFile = null;
    for (int i = 0; i < dates.length; i++)
    {
      Date date = dates[i];
      gribFile = files.get(date);
      gribFile.listParameters(out);
    }
  }

  /**
   * Get a string representation of the GRIB file.
   *
   * @return NoValidGribException   if record is no valid GRIB record
   */
  public String toString()
  {
    GribFile gribFile = null;
    String theString = null;
    for (int i = 0; i < dates.length; i++)
    {
      Date date = dates[i];
      gribFile = files.get(date);
      theString = theString + gribFile.toString();
    }
    return theString;
  }

}
