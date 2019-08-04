/**
 * ===============================================================================
 * $Id: GribRecord.java,v 1.4 2006/07/25 13:46:23 frv_peg Exp $
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
 * GribRecord.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 * Updated Kjell RXang, 18/03/2002
 */

package jgrib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;


/**
 * A class representing a single GRIB record. A record consists of five sections:
 * indicator section (IS), product definition section (PDS), grid definition section
 * (GDS), bitmap section (BMS) and binary data section (BDS). The sections can be
 * obtained using the getIS, getPDS, ... methods.<p>
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class GribRecord
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
    * The bitmap section.
    */
   protected GribRecordBMS bms;

   /**
    * The binary data section.
    */
   protected GribRecordBDS bds;


   // *** constructors ************************************************************


   /**
    * Constructs a <tt>GribRecord</tt> object from a bit input stream.
    *
    * @param in bit input stream with GRIB record content
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NotSupportedException 
    * @throws NoValidGribException  if stream contains no valid GRIB file
    */
   public GribRecord(BitInputStream in) throws IOException,
          NotSupportedException, NoValidGribException
   {

      this.is = new GribRecordIS(in);          // read Indicator Section

      this.pds = new GribRecordPDS(in);         // read Product Definition Section

      if (this.pds.gdsExists())
      {
// rdg - changed to use GribGDSFactory
//         this.gds = new GribRecordGDS(in);     // read Grid Description Section
         this.gds = GribGDSFactory.getGDS(in);     // read Grid Description Section
      }
      else
      {
         throw new NoValidGribException("GribRecord: No GDS included.");
      }

      if (this.pds.bmsExists())
      {
         this.bms = new GribRecordBMS(in);     // read Bitmap Section
      }

      // read Binary Data Section
      this.bds = new GribRecordBDS(in, this.pds.getDecimalScale(), this.bms);

      // number of values
      // rdg - added the check for a constant field - otherwise this fails
      if (!(this.bds.getIsConstant()) &&
          this.bds.getValues().length != this.gds.getGridNX() * this.gds.getGridNY())
      {
         System.err.println("Grid should contain " +
               this.gds.getGridNX() + " * " + this.gds.getGridNY() + " = " +
               this.gds.getGridNX() * this.gds.getGridNY() + " values.");
         System.err.println("But BDS section delivers only " +
               this.bds.getValues().length + ".");
      }

      in.close();
   }

   /**
    * Constructs a <tt>GribRecord</tt> object from a bit input stream.
    *
    * @param grl - a light grib record
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NoValidGribException  if stream contains no valid GRIB file
    * @throws NotSupportedException 
    */
   public GribRecord(GribRecordLight grl) throws IOException,
          NoValidGribException, NotSupportedException
   {

      this.is = grl.getIS();          // get Indicator Section

      this.pds = grl.getPDS();         // read Product Definition Section

      if (this.pds.gdsExists())
      {
         this.gds = grl.getGDS();     // read Grid Description Section
      }
      else
      {
         throw new NoValidGribException("GribRecord: No GDS included.");
      }

      // Make stream
      BitInputStream in = new BitInputStream(new ByteArrayInputStream(grl.getBuf()));

      if (this.pds.bmsExists())
      {
         this.bms = new GribRecordBMS(in);     // read Bitmap Section
      }

      // read Binary Data Section
      this.bds = new GribRecordBDS(in, this.pds.getDecimalScale(), this.bms);

      // number of values
      // rdg - added the check for a constant field - otherwise this fails
      if (!(this.bds.getIsConstant()) &&
          this.bds.getValues().length != this.gds.getGridNX() * this.gds.getGridNY())
      {
         System.err.println("Grid should contain " +
               this.gds.getGridNX() + " * " + this.gds.getGridNY() + " = " +
               this.gds.getGridNX() * this.gds.getGridNY() + " values.");
         System.err.println("But BDS section delivers only " +
               this.bds.getValues().length + ".");
      }

      in.close();
   }


   // *** public methods *********************************************************

   /**
    * Get the byte length of this GRIB record.
    *
    * @return length in bytes of GRIB record
    */
   public int getLength()
   {
      return this.is.getGribLength();
   }


   /**
    * Get the indicator section of this GRIB record.
    *
    * @return indicator section object
    */
   public GribRecordIS getIS()
   {
      return this.is;
   }


   /**
    * Get the product definition section of this GRIB record.
    *
    * @return product definition section object
    */
   public GribRecordPDS getPDS()
   {
      return this.pds;
   }


   /**
    * Get the grid definition section of this GRIB record.
    *
    * @return grid definition section object
    */
   public GribRecordGDS getGDS()
   {
      return this.gds;
   }


   /**
    * Get the bitmap section of this GRIB record.
    *
    * @return bitmap section object
    */
   public GribRecordBMS getBMS()
   {
      return this.bms;
   }


   /**
    * Get the binary data section of this GRIB record.
    *
    * @return binary data section object
    */
   public GribRecordBDS getBDS()
   {
      return this.bds;
   }

   /**
    * Get grid coordinates in longitude/latitude
    * @return longitide/latituide as doubles
    */
   public double[] getGridCoords()
   {
      return gds.getGridCoords();
   }


   /**
    * Get data/parameter values as an array of float.
    *
    * @return  array of parameter values
    */
   public float[] getValues()
   {
      if (!(bds.getIsConstant())){
         return bds.getValues();
      }
      int gridSize = gds.getGridNX()*gds.getGridNY();
      float[] values = new float[gridSize];
      float ref = bds.getReferenceValue();
      for (int i = 0; i < gridSize;i++){
	    values[i] = ref;
      }
      return values;
   }

   /**
    * Get a single value from the BDS using i/x, j/y index.
    *
    * Retrieves using a row major indexing.
    * @param i 
    * @param j 
    *
    * @return  array of parameter values
    * @throws NoValidGribException 
    */
   public float getValue(int i, int j) throws NoValidGribException
   {
      if (i >= 0 && i < gds.getGridNX() && j >= 0 && j < gds.getGridNY()){
         return bds.getValue(gds.getGridNX()*j + i);
      }
      throw new NoValidGribException ("GribRecord:  Array index out of bounds");
   }


   /**
    * Get the parameter type of this GRIB record.
    *
    * @return name of parameter
    */
   public String getType()
   {
      return this.pds.getType();
   }


   /**
    * Get a more detailed description of the parameter.
    *
    * @return description of parameter
    */
   public String getDescription()
   {
      return this.pds.getDescription();
   }


   /**
    * Get the unit for the parameter.
    *
    * @return name of unit
    */
   public String getUnit()
   {
      return this.pds.getUnit();
   }


   /**
    * Get the level (height or pressure).
    *
    * @return description of level
    */
   public String getLevel()
   {
      return this.pds.getLevel();
   }


   /**
    * Get the analysis or forecast time of this GRIB record.
    *
    * @return analysis or forecast time
    */
   public Calendar getTime()
   {
      return this.pds.getLocalForecastTime();
   }


   /**
    * Get a string representation of this GRIB record.
    *
    * @return string representation of this GRIB record
    */
   public String toString()
   {
      // combine string representations of subsections
      return "GRIB record:\n" +
            this.is + "\n" +
            this.pds + "\n" + 
            (this.pds.gdsExists() ? this.gds.toString() + "\n" : "") +
            (this.pds.bmsExists() ? this.bms.toString() + "\n" : "") +
            this.bds;
   }
}


