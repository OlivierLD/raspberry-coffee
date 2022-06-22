/**
 * ===============================================================================
 * $Id: GribFile.java,v 1.8 2006/07/27 13:44:29 frv_peg Exp $
 * ===============================================================================
 * JGRIB library
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * <p>
 * Authors:
 * See AUTHORS file
 * ===============================================================================
 */

/*
 * GribFile.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 * Updated Kjell RXang, 18/03/2002
 * Updated Richard D. Gonzalez 7 Dec 02
 */

package jgrib;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import jgrib.util.GribPDSLevelComparator;

//import jgrib.util.GribPDSLevelComparator;


/**
 * A class that represents a GRIB file. It consists of a number of records which
 * are represented by <tt>GribRecord</tt> objects.
 *
 * To retrieve a specific record or records, the standard sequence of methods is:
 *    getGridsForType
 *    getZunitsForTypeGrid
 *    getLevelsForTypeGridUnit
 *    getDatesForTypeGridLevel
 *    get2dRecord
 *       or
 *    get3dRecord
 *
 * @author Benjamin Stark
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class GribFile {

    /**
     * Defines version of JGRIB
     */
    private static String VERSION = "jgrib_beta7";

    /**
     * Array with light records
     */
    private GribRecordLight[] lightRecords;

    /**
     * Array with grids
     */
    private GribRecordGDS[] grids;

    /**
     * Array with type names
     */
    private String[] typeNames;

    /**
     * Array with descriptions
     */
    private String[] descriptions;

    // *** constructors *******************************************************

    /**
     * Constructs a <tt>GribFile</tt> object from a file.
     *
     * @param filename name of the GRIB file
     *
     * @throws FileNotFoundException if file can not be found
     * @throws IOException           if file can not be opened etc.
     * @throws NotSupportedException if file contains features not yet in jgrib
     * @throws NoValidGribException  if file is no valid GRIB file
     */
    public GribFile(String filename)
            throws FileNotFoundException, IOException, NotSupportedException, NoValidGribException {
        this(new FileInputStream(filename));
    }


    /**
     * Constructs a <tt>GribFile</tt> object from an input stream
     *
     * @param in input stream with GRIB content
     *
     * @throws IOException           if stream can not be opened etc.
     * @throws NotSupportedException if file contains features not yet in jgrib
     * @throws NoValidGribException  if stream does not contain a valid GRIB file
     */
    public GribFile(InputStream in)
            throws IOException, NotSupportedException, NoValidGribException {
        this(new BitInputStream(new BufferedInputStream(in)));
    }


    /**
     * Constructs a <tt>GribFile</tt> object from a bit input stream
     *
     * @param in bit input stream with GRIB content
     *
     * @throws IOException           if stream can not be opened etc.
     * @throws NotSupportedException if file contains features not yet in jgrib
     * @throws NoValidGribException  if stream does not contain a valid GRIB file
     */
    public GribFile(BitInputStream in)
            throws IOException, NotSupportedException, NoValidGribException {
        //long start = System.currentTimeMillis();
        Map<GribRecordGDS, GribRecordGDS> gridMap = new HashMap<GribRecordGDS, GribRecordGDS>();
        List<String> typeList = new ArrayList<String>();
        List<String> descList = new ArrayList<String>();
        List<GribRecordLight> lightRecList = new ArrayList<GribRecordLight>();

        /**
         * Initialize the Parameter Tables with the information in the parameter
         * table lookup file.  See GribPDSParamTable for details
         */
        //GribPDSParamTable.readParameterTableLookup(); done in static initializer

        GribRecordIS is = null; // the indicator section of a record

        while (this.seekHeader(in)) {
            // Read IS
            is = new GribRecordIS(in);
            int totalBytes = is.getGribLength();

            // Remove IS length
            totalBytes -= is.getISLength();
            byte[] buf = null;

            // Read other records
            GribRecordPDS pds = new GribRecordPDS(in); // read Product Definition Section
            // Remove PDS length
            totalBytes -= pds.getLength();

            GribRecordGDS gds = null;

            if (pds.gdsExists()) {
                //          rdg - changed to use GribGDSFactory class
                //            gds = new GribRecordGDS(in);     // read Grid Description Section
                gds = GribGDSFactory.getGDS(in);
                // Remove GDS length
                totalBytes -= gds.getLength();

                // Read rest of bytes
                buf = new byte[totalBytes];
                in.read(buf);
                if (gridMap.containsValue(gds)) {
                    // Get ref alredy in
                    gds = gridMap.get(gds);
                } else {
                    // Put in new
                    gridMap.put(gds, gds);
                }
                if (!typeList.contains(pds.getType())) {
                    typeList.add(pds.getType());
                    descList.add(pds.getDescription());
                }

                GribRecordLight grl = new GribRecordLight(is, pds, gds, buf);
                lightRecList.add(grl);
                //this.records.addElement(buf);               // store record buffer
            } else {
                System.err.println(this.getClass().toString() + ": No GDS included.");
                // Skip
                in.skip(totalBytes);
            }
            //       System.out.println("READ binary description section: IS,PDS,GDS");
        }

        // Convert to arrays
        this.grids = gridMap.values().toArray(new GribRecordGDS[gridMap.size()]);
        this.typeNames = typeList.toArray(new String[typeList.size()]);
        this.descriptions = descList.toArray(new String[descList.size()]);
        this.lightRecords = lightRecList.toArray(new GribRecordLight[lightRecList.size()]);

        if (grids.length == 0) {
            System.err.println("GribFile: No GRIB file or no records found.");
        }
    }

    /**
     * Buildin version, so you can get it from within a jar file.
     * @return String - Current version of JGRIB
     */
    public static String getVersion() {
        return GribFile.VERSION;
    }

    /**
     * Get description for type name
     * @param aType type name
     * @return description of type
     */
    public String getDescriptionForType(String aType) {
        for (int i = 0; i < typeNames.length; i++) {
            if (typeNames[i].equalsIgnoreCase(aType)) {
                return descriptions[i];
            }
        }
        return null;
    }

    /**
     * Added by Richard Gonzalez 23 Sep 02
     * Get Records whose description matches (aDesc)
     * @param aDesc String representing type name
     * @return array of GribRecords
     */
    public GribRecordLight[] getRecordForDescription(String aDesc) {
        Set<GribRecordLight> recordSet = new HashSet<GribRecordLight>();

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getDescription().equalsIgnoreCase(aDesc)) {
                recordSet.add(lightRecords[i]);
            }
        }
        return recordSet.toArray(new GribRecordLight[recordSet.size()]);
    }


    /**
     * Added by Richard Gonzalez 23 Sep 02
     * Get Records for type (variable)
     * @param aType String representing type name
     * @return array of GribRecords
     */
    public GribRecordLight[] getRecordForType(String aType) {
        Set<GribRecordLight> recordSet = new HashSet<GribRecordLight>();

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType)) {
                recordSet.add(lightRecords[i]);
            }
        }
        return recordSet.toArray(new GribRecordLight[recordSet.size()]);
    }

    /**
     * Get Grids used for type (variable)
     * @param aType type name
     * @return array of grids
     * @deprecated - use getGridsForType - it better reflects the functionality
     */
    @Deprecated
    public GribRecordGDS[] getGridForType(String aType) {
        return getGridsForType(aType);
    }

    /**
     * Get Grids used for type (variable)
     * @param aType type name
     * @return array of grids
     * rdg - renamed to better represent functionality
     */
    public GribRecordGDS[] getGridsForType(String aType) {
        Set<GribRecordGDS> gdSet = new HashSet<GribRecordGDS>();

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType)) {
                gdSet.add(lightRecords[i].getGDS());
            }
        }
        return gdSet.toArray(new GribRecordGDS[gdSet.size()]);
    }

    /**
     * rdg - added this method to distinguish between vertical coordinate types.
     *         e.g. hPa vs. meters vs. layer between hPa surfaces
     *
     * Get level indices for type and grid
     * Can't use the unit string because it may be blank for many types
     * @param aType type name
     * @param aGDS grid
     * @return array of level indices from table 3
     */
    public int[] getZunitsForTypeGrid(String aType, GribRecordGDS aGDS) {
        Set<Integer> unitSet = new HashSet<Integer>();

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType) && lightRecords[i].getGDS().equals(aGDS)) {
                unitSet.add(new Integer(lightRecords[i].getPDS().getPDSLevel().getIndex()));
            }
        }
        // parse out the unit indices
        Integer[] ints = unitSet.toArray(new Integer[unitSet.size()]);
        int[] units = new int[unitSet.size()];
        for (int i = 0; i < ints.length; i++) {
            units[i] = ints[i].intValue();
        }
        return units;
    }

    /**
     * rdg - added this method to distinguish between different vertical
     *       coordinate units (e.g. hPa vs. meters).
     * Get levels (sorted) for type and grid and vertical coordinate unit index
     * Using index guards against blank unit strings matching each other
     * @param aType type name
     * @param aGDS grid
     * @param aUnit index of vertical axis units from table 3
     * @return array of GribPDSLevel-s
     */
    public GribPDSLevel[] getLevelsForTypeGridUnit(String aType, GribRecordGDS aGDS, int aUnit) {
        Set<GribPDSLevel> levelSet = new HashSet<GribPDSLevel>();
        List<GribPDSLevel> levelValues = new ArrayList<GribPDSLevel>(); // used for tracking levels (e.g. 1000mb)
        List<String> levelNames = new ArrayList<String>(); // used for tracking names (e.g. surface, tropopause level)
        Float levelValue;
        String levelName;
        GribPDSLevel level;

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType) && lightRecords[i].getGDS().equals(aGDS) &&
                    (lightRecords[i].getPDS().getPDSLevel().getIndex() == aUnit)) {
                // have to use the level alot, get a reference to it
                level = lightRecords[i].getPDS().getPDSLevel();

                // get the numeric value for this level
                levelValue = new Float(level.getValue1());

                // if no value for level (e.g. surface), then check if this type has been added
                if (levelValue.isNaN()) {
                    levelName = level.getName();
                    if (!(levelNames.contains(levelName))) { // if not stored yet, store it
                        levelNames.add(levelName);
                        levelSet.add(lightRecords[i].getPDS().getPDSLevel());
                    }
                } else { // has a numeric value, check if it has been stored
                    if (!(levelValues.contains(level))) { // if not stored yet, store it
                        levelValues.add(level);
                        levelSet.add(lightRecords[i].getPDS().getPDSLevel());
                    }
                }
            }
        }
        // Sort
        GribPDSLevel[] levels = levelSet.toArray(new GribPDSLevel[levelSet.size()]);
        Arrays.sort(levels, new GribPDSLevelComparator());

        return levels;
    }


    /**
     * rdg - this is superceded by getLevelsForTypeGridUnit - should be deleted
     * Get levels (sorted) for type and grid
     *
     * @param aType type name
     * @param aGDS grid
     * @return array of forecast time
     * @deprecated - allows ambiguity - use getLevelsForTypeGridUnit instead
     */
    @Deprecated
    public String[] getLevelsForTypeGrid(String aType, GribRecordGDS aGDS) {
        Set<String> gdSet = new HashSet<String>();

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType) && lightRecords[i].getGDS().equals(aGDS)) {
                gdSet.add(lightRecords[i].getPDS().getLevel());
            }
        }
        // Sort
        String[] levels = gdSet.toArray(new String[gdSet.size()]);
        Arrays.sort(levels);

        return levels;
    }


    /**
     * Get forecast times (sorted) for type and grid.
     * @param aType type name
     * @param aGDS grid
     * @param aLevel
     * @return array of forecast time
     * @deprecated - use same method name that uses a GribPDSLevel vice a string
     */
    @Deprecated
    public Date[] getDatesForTypeGridLevel(String aType, GribRecordGDS aGDS, String aLevel) {
        List<Date> gdList = new ArrayList<Date>();

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType) && lightRecords[i].getGDS().equals(aGDS) &&
                    lightRecords[i].getPDS().getLevel().equals(aLevel)) {
                gdList.add(lightRecords[i].getPDS().getLocalForecastTime().getTime());
            }
        }
        // Sort
        Date[] dates = gdList.toArray(new Date[gdList.size()]);
        Arrays.sort(dates);

        return dates;
    }

    /**
     * rdg - an override of the method passing a GribPDSLevel, vice a string for a level
     * Get forecast times (sorted) for type and grid
     * @param aType type name
     * @param aGDS grid
     * @param aLevel
     * @return array of forecast time
     */
    public Date[] getDatesForTypeGridLevel(String aType, GribRecordGDS aGDS, GribPDSLevel aLevel) {
        List<Date> dateList = new ArrayList<Date>();

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType) && lightRecords[i].getGDS().equals(aGDS) &&
                    lightRecords[i].getPDS().getPDSLevel().equals(aLevel)) {
                dateList.add(lightRecords[i].getPDS().getLocalForecastTime().getTime());
            }
        }
        // Sort
        Date[] dates = dateList.toArray(new Date[dateList.size()]);
        Arrays.sort(dates);

        return dates;
    }

    /**
     * Get a specified grid record
     * @param aType type name
     * @param aGDS  grid
     * @param aLevel
     * @param aDate forecast date
     * @return GribRecord
     * @throws IOException
     * @throws NoValidGribException
     * @throws NotSupportedException
     */
    public GribRecord getRecord(String aType, GribRecordGDS aGDS, String aLevel, Date aDate)
            throws IOException, NoValidGribException, NotSupportedException {
        GribRecordLight grl = null;

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType) && lightRecords[i].getGDS().equals(aGDS) &&
                    lightRecords[i].getPDS().getLevel().equals(aLevel) &&
                    lightRecords[i].getPDS().getLocalForecastTime().getTime().equals(aDate)) {
                grl = lightRecords[i];
                break;
            }
        }
        if (grl != null) {
            return new GribRecord(grl);
        }
        return null;
    }

    /**
     * rdg - added this method to work with refined methods.  Also made name more
     *       specific to what you are getting.
     * Get a specified grid record
     * @param aType parameter type name
     * @param aGDS  grid
     * @param aLevel level
     * @param aDate forecast date
     * @return - a GribRecord for a single level of a parameter
     * @throws IOException
     * @throws NoValidGribException
     * @throws NotSupportedException
     */
    public GribRecord get2dRecord(String aType, GribRecordGDS aGDS, GribPDSLevel aLevel, Date aDate)
            throws IOException, NoValidGribException, NotSupportedException {
        GribRecordLight grl = null;

        for (int i = 0; i < lightRecords.length; i++) {
            if (lightRecords[i].getPDS().getType().equalsIgnoreCase(aType) && lightRecords[i].getGDS().equals(aGDS) &&
                    lightRecords[i].getPDS().getPDSLevel().equals(aLevel) &&
                    lightRecords[i].getPDS().getLocalForecastTime().getTime().equals(aDate)) {
                grl = lightRecords[i];
                break;
            }
        }
        if (grl != null) {
            return new GribRecord(grl);
        }
        return null;
    }

    /**
     * rdg - added this method to work with refined methods.  Also made name more
     *       specific to what you are getting.
     * Get a specified grid record
     * @param aType parameter type name
     * @param aGDS  grid
     * @param levels  levels to be retrieved - should already be sorted
     * @param aDate forecast date
     * @return - an array of GribRecord-s representing a volume of a parameter
     * @throws IOException
     * @throws NoValidGribException
     * @throws NotSupportedException
     */
    public GribRecord[] get3dRecord(String aType, GribRecordGDS aGDS, GribPDSLevel[] levels, Date aDate)
            throws IOException, NoValidGribException, NotSupportedException {
        GribRecord[] records = new GribRecord[levels.length];
        GribRecordLight grl = null;

        for (int z = 0; z < levels.length; z++) {

            for (int i = 0; i < lightRecords.length; i++) {
                if (lightRecords[i].getGDS().equals(aGDS) && lightRecords[i].getPDS().getType().equalsIgnoreCase(aType) &&
                        lightRecords[i].getPDS().getPDSLevel().equals(levels[z]) &&
                        lightRecords[i].getPDS().getLocalForecastTime().getTime().equals(aDate)) {
                    grl = lightRecords[i];
                    break;
                }
            }
            if (grl != null) {
                records[z] = new GribRecord(grl);
            } else {
                records = null;
            }
        }
        return records;
    }

    /**
     * Seeks for the magic word GRIB in the binary stream
     * @param in
     * @return true/false
     */
    private boolean seekHeader(BitInputStream in) {

        int pat[] =
                {'G', 'R', 'I', 'B'};
        int ui8 = 0;
        // seek header
        try {
            //if(in.available()>0) ui8 = in.readUI8();

            while (in.available() > 2) {
                //This code has been commented by Antonio S. Cofi�o, because if
                // you have this sequence of characters GGRIB you will skip the message
                //
                // code must be "G" "R" "I" "B"
                //            int ui8 = in.readUI8();
                //            if (ui8 == (int) 'G' && in.readUI8() == (int) 'R'
                //                  && in.readUI8() == (int) 'I' && in.readUI8() == (int) 'B')
                //               return true;

                if (ui8 == pat[0]) {
                    if ((ui8 = in.readUI8()) == pat[1] &&
                            (ui8 = in.readUI8()) == pat[2] &&
                            (ui8 = in.readUI8()) == pat[3])
                        return true;
                } else {
                    ui8 = in.readUI8();
                }
            }
        } catch (IOException ioe) {
            System.err.println(this.getClass().toString() + ": IOException: " + ioe.getMessage());
            // do nothing
        }
        return false;
    }


    /**
     * Get a specific GRIB record of this GRIB file as <tt>GribRecord</tt> object.
     *
     * @param i number of GRIB record, first record is number 1
     *
     * @return GRIB record object
     *
     * @throws NoSuchElementException if record number does not exist
     * @throws IOException            if record can not be opened etc.
     * @throws NoValidGribException   if record is no valid GRIB record
     * @throws NotSupportedException  if JGrib doesn't yet support the operation
     */
    public GribRecord getRecord(int i)
            throws NoSuchElementException, IOException, NoValidGribException, NotSupportedException {

        if (i <= 0 || i > this.lightRecords.length)
            throw new NoSuchElementException("GribFile: No such record (" + i + ").");

        // Changed by Peter Gylling Peg@fomfrv.dk 2002-04-16
        // from this line
        // return new GribRecord(lightRecords[i]);
        return new GribRecord(lightRecords[i - 1]);
        //return new GribRecord((byte[]) this.records.elementAt(i - 1));
    }

    /**
     * Get type names
     * @return array with names
     */
    public String[] getTypeNames() {
        return typeNames;
    }

    /**
     * Get Light GRIB records
     * @return Array with Light Grib Records
     */
    public GribRecordLight[] getLightRecords() {
        return lightRecords;
    }

    /**
     * Get get grids
     * @return array with grids
     */
    public GribRecordGDS[] getGrids() {
        return grids;
    }


    /**
     * Get the number of records this GRIB file contains.
     *
     * @return number of records in this GRIB file
     */
    public int getRecordCount() {
        return this.lightRecords.length;
    }


    /**
     * Print out overview of GRIB file content.
     *
     * @param out print stream the output is written to
     *
     * @throws IOException            if a record can not be opened etc.
     * @throws NoValidGribException   if a record is no valid GRIB record
     * @throws NotSupportedException  if JGrib doesn't support something yet
     */
    public void listRecords(PrintStream out)
            throws IOException, NoValidGribException, NotSupportedException {
        for (int i = 0; i < this.lightRecords.length; i++)
            out.println("(" + (i + 1) + ") " + new GribRecord(lightRecords[i]));
    }

    /**
     * Method added by Richard Gonzalez 23 Sep 02.
     *
     * Print out listing of parameters in GRIB file.
     *
     * @param out print stream the output is written to
     *
     */
    public void listParameters(PrintStream out) {
        String currentParam = null;
        String oldParam = null;
        int recordCount;

        // determine how many GribRecords are stored
        recordCount = this.getRecordCount();
        out.println("GribFile reports " + recordCount + " records,");

        // Get light grib reccords
        GribRecordLight[] gribLight = this.getLightRecords();

        out.print("   PDS records [record number] are:");
        for (int i = 0; i < recordCount; i++) {
            GribRecordPDS gribPDS = gribLight[i].getPDS();
            currentParam = gribPDS.getDescription();
            if (currentParam == oldParam) { // put same parameter on the same line
                // one added because records start at 1
                out.print(";" + gribPDS.getLevel() + "[" + (i + 1) + "]");
            } else { // start a new line
                out.println();
                out.print("       " + currentParam + " at level(s): ");
                oldParam = currentParam;
                out.print(gribPDS.getLevel() + "[" + (i + 1) + "]");
            }
        }
    }

    /**
     * Get a string representation of the GRIB file.
     *
     * @return NoValidGribException   if record is no valid GRIB record
     */
    public String toString() {
        return "GRIB file (" + this.lightRecords.length + " records)";
    }

}


