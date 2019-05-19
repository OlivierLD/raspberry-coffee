/**
 * ===============================================================================
 * $Id: GribRecHeader.java,v 1.3 2006/07/25 13:44:57 frv_peg Exp $
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
package jgrib.util;


import java.util.Date;

import jgrib.GribPDSLevel;
import jgrib.GribPDSParameter;
import jgrib.GribRecord;
import jgrib.GribRecordGDS;
import jgrib.GribRecordLight;
import jgrib.GribRecordPDS;

import jgrib.util.GribPDSLevelComparator;


/**
 * GRIB Record Header<br>
 * Purpose: Organize GRIB records using their key fields.
 * Avoids memory overhead by not including body data, ie.
 * GribRecord's or GribRecordLight's.
 * torc
 */
public class GribRecHeader
  implements Comparable
{
  /**
   * Grid Definition Section
   */
  public static final int F_GDS = 0; // grid definition section

  /**
   * Level type (2D or 3D)
   */
  public static final int F_LEV_TYP = 1; // level type (2D or 3D data)

  /**
   * Parameter varName
   */
  public static final int F_PAR = 2; // parameter varName

  /**
   * Forecast date
   */
  public static final int F_DATE = 3; // forecast date

  /**
   * Actual level (Z level if 3D)
   */
  public static final int F_LEV_Z = 4; // actual level (numbered Z-level if 3D data)

  /**
   * Total number of fields
   */
  public static final int NUM_FIELDS = 5; // TOTAL NUM FIELDS

  /**
   * Default Field Order: F_GDS, F_LEV_TYP, F_PAR, F_DATE, F_LEV_Z
   */
  public static final int mDefaultFldOrder[] =
  { F_GDS, F_LEV_TYP, F_PAR, F_DATE, F_LEV_Z };


  /**
   * light record position in GribFile object or -1 if unknown
   */
  private int ix;

  /**
   * Local storage of GribPDSParameter object
   */
  private GribPDSParameter param;

  /**
   * Local storage of GribRecordGDS object
   */
  private GribRecordGDS gds;

  /**
   * Local storage of GribPDSLevel object
   */
  private GribPDSLevel plev;

  /**
   * The working date
   */
  private Date date;

  /**
   * @see net.sourceforge.jgrib.util.GribPDSLevelComparator
   */
  private static GribPDSLevelComparator mPDSlevComp;

  static {
    mPDSlevComp = new GribPDSLevelComparator();
  }

  /**
   *
   * @param index
   * @param rec
   */
  public GribRecHeader(int index, GribRecord rec)
  {
    init(index, rec.getPDS(), rec.getGDS());
  }

  /**
   *
   * @param index
   * @param rec
   */
  public GribRecHeader(int index, GribRecordLight rec)
  {
    init(index, rec.getPDS(), rec.getGDS());
  }

  /**
   *
   * @param index
   * @param p
   * @param g
   */
  private void init(int index, GribRecordPDS p, GribRecordGDS g)
  {
    ix = index;
    param = p.getParameter();
    gds = g;
    plev = p.getPDSLevel();
    date = p.getLocalForecastTime().getTime();
  }


  /**
   * F_GDS
   *
   * @return gds - GribRecordGDS
   */
  public GribRecordGDS getGDS()
  {
    return gds;
  }

  /**
   * F_LEV_TYP
   *
   * @return PDS level type
   */
  public int getPDSLevType()
  {
    return plev.getIndex();
  }


  /**
   * F_PAR
   *
   * @return parameter
   */
  public GribPDSParameter getParam()
  {
    return param;
  }

  /**
   * F_PAR
   *
   * @return parameter name
   */
  public String getParamName()
  {
    return param.getName();
  }

  /**
   *
   * @return parameter unit
   */
  public String getParamUnit()
  {
    return param.getUnit();
  }

  /**
   *
   * @return parameter description
   */
  public String getParamDescr()
  {
    return param.getDescription();
  }


  /**
   * F_DATE
   *
   * @return date
   */
  public Date getDate()
  {
    return date;
  }

  /**
   * F_LEV_Z
   *
   * @return pds level
   */
  public GribPDSLevel getPDSLev()
  {
    return plev;
  }

  /**
   * F_LEV_Z
   *
   * @return pds z level value
   */
  public float getPDSLevZvalue()
  {
    return plev.getValue1();
  }


  /**
   *
   * @param aFld
   * @param o2
   * @return result of comparation
   */
  public int compareField(int aFld, Object o2)
  {
    GribRecHeader r1 = this;
    if (o2 == null)
    {
      return 1;
    }
    GribRecHeader r2 = (GribRecHeader) o2;
    switch (aFld)
    {
      case F_GDS:
        return -r1.getGDS().compare(r2.getGDS());
      case F_LEV_TYP:
        return r1.getPDSLevType() - r2.getPDSLevType();
      case F_PAR:
        return r1.getParamName().compareTo(r2.getParamName());
        //return -r1.param.compare(r2.param);
      case F_DATE:
        return r1.getDate().compareTo(r2.getDate());
      case F_LEV_Z:
        //return -r1.getPDSLev().compare(r2.getPDSLev());
        return mPDSlevComp.compare(r1.getPDSLev(), r2.getPDSLev());
    }
    return 0;
  }


  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object obj)
  {
    /* this == obj : return 0
         this > obj : return i > 0
         this < obj : return i < 0
      */
    GribRecHeader o2 = (GribRecHeader) obj;
    if (this == o2)
    {
      return 0;
    }
    if (o2 == null)
    {
      return -1;
    }
    for (int fld = 0; fld < mDefaultFldOrder.length; ++fld)
    {
      int res = compareField(mDefaultFldOrder[fld], o2);
      if (res != 0)
      {
        return res > 0? (fld + 1): -(fld + 1);
      }
    }
    return 0;
  }

  /**
   *
   * @return ix
   */
  public int getIx()
  {
    return ix;
  }


  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer(200);
    //sb.append(varName());
    sb.append(" " + param);
    sb.append("\n gds" + gds);
    sb.append("\n lev    " + plev.getLevel());
    sb.append("\n date   " + date);
    return sb.toString();
  }
}
