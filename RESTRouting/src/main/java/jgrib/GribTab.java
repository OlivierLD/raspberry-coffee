/**
 * ===============================================================================
 * $Id: GribTab.java,v 1.4 2006/07/25 13:46:23 frv_peg Exp $
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
 * @author Tor Christian Bekkvik
 */

package jgrib;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * A class containing methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.
 * @deprecated Implemetation now is supported by GribPDSParameter
 */

@Deprecated
public class GribTab implements Comparable
{
	/**
	 * 
	 */
   private String resource;
   /**
    * Identification of center e.g. 88 for Oslo
    */
   private int center_id;

   /**
    * Identification of sub center (-1 if irrelevant)
    */
   //protected int subcenter_id;

   /**
    * Table identification
    */
   private int paramter_table;

   /**
    * 
    * @param d
    * @return integer value
    */
   private static int cdif(int d){
      d = (d > 0) ? 1 : (d < 0 ? -1 : 0);
      return d;
   }

   /**
    * @param obj - Object to compare to "this"
    * @return integer value
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo (Object obj)
   {
      GribTab gt = (GribTab) obj;
      int res = cdif(this.paramter_table - gt.paramter_table);
      res <<= 1;
      res += cdif(this.center_id - gt.center_id);
      return res;
   }

   
   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (!(o instanceof GribTab)) return false;

      final GribTab tab = (GribTab) o;

      if (center_id != tab.center_id) return false;
      if (paramter_table != tab.paramter_table) return false;

      return true;
   }

   
   /**
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      int result;
      result = center_id;
      result = 29 * result + paramter_table;
      return result;
   }

   /**
    * Parameter table
    */
   private String[][] paramtable_opn;

   /**
    * Make one grib table
    * @param centId  center id
    * @param tabId   table id
    * @param parTable table
    */
   public GribTab (int centId, int tabId, String[][] parTable)
   {
      center_id = centId;
      paramter_table = tabId;
      paramtable_opn = parTable;
      resource = "table id: ("+centId+","+tabId+")";
   }

   /**
    * Load grid table from file
    * @param fileName  file name
    * @throws IOException
    */
   public GribTab (String fileName)
         throws IOException
   {
      init(new BufferedReader(new FileReader(fileName)));
      resource = fileName;
   }

   /**
    * @param url
    * @throws IOException
    */
   public GribTab (URL url) throws IOException
   {
      InputStream is = url.openStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      init(br);
      resource = url.toString();
   }

   /**
    * Add paramter table
    * @param br initialized BufferedReader
    * @throws IOException 
    */
   private void init (BufferedReader br)
         throws IOException
   {
      List<String> paraTable = new ArrayList<String>(300);
      String line = br.readLine();
      String[] tableDefArr = SmartStringArray.split(":", line);

      center_id = Integer.parseInt(tableDefArr[1].trim());
      paramter_table = Integer.parseInt(tableDefArr[3].trim());

      int maxInd = 0;
      while ((line = br.readLine()) != null)
      {
         int end = line.indexOf(':');
         String indStr = line.substring(0, end).trim();
         int index = Integer.parseInt(indStr);
         maxInd = Math.max(maxInd, index);
         paraTable.add(line);
      }
      paramtable_opn = decodeParamters(paraTable, maxInd);
   }

   /**
    * Decode paramters
    * @param aList 
    * @param aMaxInd 
    * @return decoded parameters 
    *
    */
   private static String[][] decodeParamters (List aList, int aMaxInd)
   {
      String[][] params = new String[aMaxInd + 1][3];
      for (int i = 0; i < aList.size(); i++)
      {
         String[] arr1 = SmartStringArray.split(":", (String) aList.get(i));
         int index = Integer.parseInt(arr1[0].trim());
         String type = arr1[1].trim().toLowerCase();
         String desc = null;
         String unit = null;
         // Check if "[" is used
         if (arr1[2].indexOf('[') == -1)
         {
            // Undefined variable
            unit = desc = arr1[2].trim();
         }
         else
         {
            String[] arr2 = SmartStringArray.split("[", arr1[2]);
            desc = arr2[0];
            // Remove "]"
            unit = arr2[1].substring(0, arr2[1].lastIndexOf(']')).trim();
         }

         // Set data
         String[] setArr = params[index];
         setArr[0] = type;
         setArr[1] = desc;
         setArr[2] = unit;
      }


      return params;
   }

   /**
    * Get a description for the level code.
    *
    * @param pds9  part 1 of leve code
    * @param pds10 part 2 of level code
    * @param pds11 part 3 of level code
    *
    * @return description of the level
    */
   public static String getLevel(int pds9, int pds10, int pds11)
   {

      int pds1011 = pds10 << 8 | pds11;

      switch (pds9)
      {

         case 1:
            return "surface";
         case 2:
            return "cloud base level";
         case 3:
            return "cloud top level";
         case 4:
            return "0 degree isotherm level";
         case 5:
            return "condensation level";
         case 6:
            return "maximum wind speed level";
         case 7:
            return "tropopause level";
         case 8:
            return "nominal atmosphere top";
         case 9:
            return "sea bottom";

         case 100:
            return pds1011 + " mb";
         case 101:
            return pds10 + "-" + pds11 + " mb";
         case 102:
            return "mean sea level";
         case 103:
            return pds1011 + " m above mean sea level";
         case 104:
            return (pds10 * 100) + "-" + (pds11 * 100) + " m above mean sea level";
         case 105:
            return pds1011 + " m above ground";
         case 106:
            return (pds10 * 100) + "-" + (pds11 * 100) + " m above ground";
         case 107:
            return "sigma=" + (pds1011 / 10000.0);
         case 108:
            return "sigma " + (pds10 / 100.0) + "-" + (pds11 / 100.0);
         case 109:
            return "hybrid level " + pds1011;
         case 110:
            return "hybrid " + pds10 + "-" + pds11;
         case 111:
            return pds1011 + " cm below ground";
         case 112:
            return pds10 + "-" + pds11 + " cm down";
         case 113:
            return pds1011 + " K";
         case 125:
            return pds1011 + " cm above ground";
         case 160:
            return pds1011 + " m below sea level";

         case 200:
            return "entire atmoshere layer";
         case 201:
            return "entire ocean layer";

         default:
            return "";
      }

   }



   /**
    * @param id
    * @param pos
    * @return Parameter string
    */
   private String getPString (int id, int pos)
   {
      if (paramtable_opn == null || id < 0
            || id > paramtable_opn.length) return null;
      return paramtable_opn[id][pos];
   }

   /**
    * Get the tag/name of the parameter with id <tt>id</tt>.
    *
    * @param id 
    * @return tag/name of the parameter
    */
   public String getParameterTag (int id)
   {
      return getPString(id, 0);
   }

   /**
    * Get a description for the parameter with id <tt>id</tt>.
    * @param id index
    * @return description for the parameter
    */
   public String getParameterDescription (int id)
   {
      return getPString(id, 1);
   }

   /**
    * Get a description for the unit with id <tt>id</tt>.
    * @param id index
    * @return description of the unit for the parameter
    */
   public String getParameterUnit (int id)
   {
      return getPString(id, 2);
   }


   /**
    * Get center identifiewr
    * @return id
    */
   public int getCenterId ()
   {
      return center_id;
   }

   /**
    * Get table identifier
    * @return id
    */
   public int getTableId ()
   {
      return paramter_table;
   }

   /**
    * Prints parameter table 
    */
   public void printTable ()
   {
      String str = "PARAMETER TABLE: "
            + "\t center: " + center_id
            + "\t table: " + paramter_table
            + "\n\t resource: " + resource;
      System.out.println(str);
      if (paramtable_opn == null)
      {
         System.out.println("\t NO TABLE");
      } else
      {
         for (int id = 0; id < paramtable_opn.length; ++id)
         {
            str = "\t " + id + "\t "
                  + paramtable_opn[id][0] + "\t ,"
                  + paramtable_opn[id][1] + "\t , ["
                  + paramtable_opn[id][2] + "]";
            System.out.println(str);
         }
      }
      System.out.println();
   }
}
