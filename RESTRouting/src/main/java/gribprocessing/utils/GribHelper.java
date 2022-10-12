package gribprocessing.utils;

import calc.GeoPoint;
import jgrib.GribFile;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordGDS;
import jgrib.GribRecordPDS;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;
import nmea.utils.NMEAUtils;
import utils.StaticUtil;

import javax.swing.JOptionPane;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

public class GribHelper {
	private static boolean alreadySaidTooOld;

	public static void setAlreadySaidTooOld(boolean b) {
		alreadySaidTooOld = b;
	}

	/**
	 * 2D smoothing (area, no time)
	 *
	 * @param gribData Original GRIB Data
	 * @param smooth   smooth factor.
	 * @return Smoothed data
	 */
	public static GribConditionData smoothGribData(GribConditionData gribData, int smooth) {
//  System.out.println("Smoothing..., factor " + smooth);
		GribConditionData newGribData;

		double _w = gribData.getWLng();
		double _e = gribData.getELng();
		double _n = gribData.getNLat();
		double _s = gribData.getSLat();

		double stepX = gribData.getStepX() / (double) smooth;
		double stepY = gribData.getStepY() / (double) smooth;

		newGribData = new GribConditionData();
		newGribData.setDate(gribData.getDate());
		newGribData.setELng(_e);
		newGribData.setWLng(_w);
		newGribData.setNLat(_n);
		newGribData.setSLat(_s);
		newGribData.setStepX(stepX);
		newGribData.setStepY(stepY);

		int newH = gribData.getGribPointData().length * smooth,
				newW = gribData.getGribPointData()[0].length * smooth;

		GribPointData[][] newGribPointData = new GribPointData[newH][newW];

		if (StaticUtil.sign(_w) != StaticUtil.sign(_e) && _w > 0D) // Around Anti-meridian
			_w -= 360D;

		int h = 0;
		for (double _Lat = _s; _Lat <= _n; _Lat += stepY) {
			int w = 0;
			for (double _Lng = _w; _Lng <= _e; _Lng += stepX) {
				List<Integer> ar = gribData.getDataPointsAround(new GeoPoint(_Lat, _Lng));
				if (ar != null) {
					try {
						int yIdx = ar.get(0);
						int xIdx = ar.get(1);
						DataPoint[] dp = new DataPoint[4];
						// Wind
						float u = gribData.getGribPointData()[yIdx][xIdx].getU();
						float v = -gribData.getGribPointData()[yIdx][xIdx].getV();
						double lat = gribData.getGribPointData()[yIdx][xIdx].getLat();
						double lng = gribData.getGribPointData()[yIdx][xIdx].getLng();
						if (_Lng < -180 && lng > 0)
							lng -= 360D;
						double speed = Math.sqrt(u * u + v * v);
						speed *= 3.60D;
						speed /= 1.852D;
						double dir = NMEAUtils.getDir(u, v);
						// Current (RTOFS Only)
						float uC = gribData.getGribPointData()[yIdx][xIdx].getUOgrd();
						float vC = -gribData.getGribPointData()[yIdx][xIdx].getVOgrd();
						double cSpeed = Math.sqrt(uC * uC + vC * vC);
						cSpeed *= 3.60D;
						cSpeed /= 1.852D;
						double cDir = NMEAUtils.getDir(uC, vC);
						cDir += 180;
						while (cDir > 360) cDir -= 360;

						double prmsl = gribData.getGribPointData()[yIdx][xIdx].getPrmsl();
						double hgt500 = gribData.getGribPointData()[yIdx][xIdx].getHgt();
						double temp = gribData.getGribPointData()[yIdx][xIdx].getAirtmp();
						double whgt = gribData.getGribPointData()[yIdx][xIdx].getWHgt();
						double rain = gribData.getGribPointData()[yIdx][xIdx].getRain();

						// Reset
						u = gribData.getGribPointData()[yIdx][xIdx].getU();
						v = gribData.getGribPointData()[yIdx][xIdx].getV();
						uC = gribData.getGribPointData()[yIdx][xIdx].getUOgrd();
						vC = gribData.getGribPointData()[yIdx][xIdx].getVOgrd();

						dp[0] = new DataPoint(lng, lat, u, v, dir, speed, prmsl, hgt500, temp, whgt, rain, uC, vC, cDir, cSpeed);

						u = gribData.getGribPointData()[yIdx][xIdx + 1].getU();
						v = -gribData.getGribPointData()[yIdx][xIdx + 1].getV();
						lat = gribData.getGribPointData()[yIdx][xIdx + 1].getLat();
						lng = gribData.getGribPointData()[yIdx][xIdx + 1].getLng();
						if (_Lng < -180 && lng > 0)
							lng -= 360D;
						speed = Math.sqrt(u * u + v * v);
						speed *= 3.60D;
						speed /= 1.852D;
						dir = NMEAUtils.getDir(u, v);

						uC = gribData.getGribPointData()[yIdx][xIdx + 1].getUOgrd();
						vC = -gribData.getGribPointData()[yIdx][xIdx + 1].getVOgrd();
						cSpeed = Math.sqrt(uC * uC + vC * vC);
						cSpeed *= 3.60D;
						cSpeed /= 1.852D;
						cDir = NMEAUtils.getDir(uC, vC);
						cDir += 180;
						while (cDir > 360) cDir -= 360;

						prmsl = gribData.getGribPointData()[yIdx][xIdx + 1].getPrmsl();
						hgt500 = gribData.getGribPointData()[yIdx][xIdx + 1].getHgt();
						temp = gribData.getGribPointData()[yIdx][xIdx + 1].getAirtmp();
						whgt = gribData.getGribPointData()[yIdx][xIdx + 1].getWHgt();
						rain = gribData.getGribPointData()[yIdx][xIdx + 1].getRain();

						u = gribData.getGribPointData()[yIdx][xIdx + 1].getU();
						v = gribData.getGribPointData()[yIdx][xIdx + 1].getV();
						uC = gribData.getGribPointData()[yIdx][xIdx + 1].getUOgrd();
						vC = gribData.getGribPointData()[yIdx][xIdx + 1].getVOgrd();

						dp[1] = new DataPoint(lng, lat, u, v, dir, speed, prmsl, hgt500, temp, whgt, rain, uC, vC, cDir, cSpeed);

						u = gribData.getGribPointData()[yIdx + 1][xIdx].getU();
						v = -gribData.getGribPointData()[yIdx + 1][xIdx].getV();
						lat = gribData.getGribPointData()[yIdx + 1][xIdx].getLat();
						lng = gribData.getGribPointData()[yIdx + 1][xIdx].getLng();
						if (_Lng < -180 && lng > 0)
							lng -= 360D;
						speed = Math.sqrt(u * u + v * v);
						speed *= 3.60D;
						speed /= 1.852D;
						dir = NMEAUtils.getDir(u, v);

						uC = gribData.getGribPointData()[yIdx + 1][xIdx].getUOgrd();
						vC = -gribData.getGribPointData()[yIdx + 1][xIdx].getVOgrd();
						cSpeed = Math.sqrt(uC * uC + vC * vC);
						cSpeed *= 3.60D;
						cSpeed /= 1.852D;
						cDir = NMEAUtils.getDir(uC, vC);
						cDir += 180;
						while (cDir > 360) cDir -= 360;

						prmsl = gribData.getGribPointData()[yIdx + 1][xIdx].getPrmsl();
						hgt500 = gribData.getGribPointData()[yIdx + 1][xIdx].getHgt();
						temp = gribData.getGribPointData()[yIdx + 1][xIdx].getAirtmp();
						whgt = gribData.getGribPointData()[yIdx + 1][xIdx].getWHgt();
						rain = gribData.getGribPointData()[yIdx + 1][xIdx].getRain();

						u = gribData.getGribPointData()[yIdx + 1][xIdx].getU();
						v = gribData.getGribPointData()[yIdx + 1][xIdx].getV();
						uC = gribData.getGribPointData()[yIdx + 1][xIdx].getUOgrd();
						vC = gribData.getGribPointData()[yIdx + 1][xIdx].getVOgrd();

						dp[2] = new DataPoint(lng, lat, u, v, dir, speed, prmsl, hgt500, temp, whgt, rain, uC, vC, cDir, cSpeed);

						u = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getU();
						v = -gribData.getGribPointData()[yIdx + 1][xIdx + 1].getV();
						lat = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getLat();
						lng = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getLng();
						if (_Lng < -180 && lng > 0)
							lng -= 360D;
						speed = Math.sqrt(u * u + v * v);
						speed *= 3.60D;
						speed /= 1.852D;
						dir = NMEAUtils.getDir(u, v);
						uC = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getUOgrd();
						vC = -gribData.getGribPointData()[yIdx + 1][xIdx + 1].getVOgrd();
						cSpeed = Math.sqrt(uC * uC + vC * vC);
						cSpeed *= 3.60D;
						cSpeed /= 1.852D;
						cDir = NMEAUtils.getDir(uC, vC);
						cDir += 180;
						while (cDir > 360) cDir -= 360;
						prmsl = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getPrmsl();
						hgt500 = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getHgt();
						temp = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getAirtmp();
						whgt = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getWHgt();
						rain = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getRain();

						u = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getU();
						v = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getV();
						uC = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getUOgrd();
						vC = gribData.getGribPointData()[yIdx + 1][xIdx + 1].getVOgrd();

						dp[3] = new DataPoint(lng, lat, u, v, dir, speed, prmsl, hgt500, temp, whgt, rain, uC, vC, cDir, cSpeed);

						boolean right = false;
						boolean left = false;
						boolean top = false;
						boolean bottom = false;
						for (int i = 0; i < dp.length; i++) {
							if (dp[i].d < 180) right = true;
							if (dp[i].d > 180) left = true;
							if (dp[i].d > 270 || dp[i].d < 90) top = true;
							if (dp[i].d < 270 && dp[i].d > 90) bottom = true;
						}
						if (right && left && top) {
							for (int i = 0; i < dp.length; i++) {
								if (dp[i].d < 180)
									dp[i].d += 360;
							}
						}
						// Smooth
						List<Double> _ar = Smoothing.calculate(dp, _Lng, _Lat);
						if (_ar != null) {
							double _dir = _ar.get(Smoothing.TWD_INDEX);
							double _speed = _ar.get(Smoothing.TWS_INDEX);
							double _prmsl = _ar.get(Smoothing.PRMSL_INDEX);
							double _500hgt = _ar.get(Smoothing.HGT500_INDEX);
							double _temp = _ar.get(Smoothing.TEMP_INDEX);
							double _whgt = _ar.get(Smoothing.TEMP_INDEX);
							double _rain = _ar.get(Smoothing.RAIN_INDEX);

							int _u = (int) (_ar.get(Smoothing.UWIND_INDEX).doubleValue());
							int _v = (int) (_ar.get(Smoothing.VWIND_INDEX).doubleValue());

							int _uC = (int) (_ar.get(Smoothing.UCURRENT_INDEX).doubleValue());
							int _vC = (int) (_ar.get(Smoothing.VCURRENT_INDEX).doubleValue());
							double _Cdir = _ar.get(Smoothing.CDR_INDEX);
							double _Cspeed = _ar.get(Smoothing.CSP_INDEX);

							GribPointData gpd = new GribPointData();
							gpd.setHgt((int) _500hgt);
							gpd.setLat(_Lat);
							double newLng = _Lng;
							if (Math.abs(newLng) > 180D) {
								if (newLng < 0D) newLng += 360D;
								else newLng -= 360D;
							}
							gpd.setLng(newLng);
							gpd.setPrmsl((int) _prmsl);
							gpd.setAirtmp((int) _temp);
							gpd.setRain((float) _rain);
							gpd.setWHgt((int) _whgt);
							gpd.setTwd(_dir);
							gpd.setTws(_speed);

							gpd.setU(_u);
							gpd.setV(_v);

							gpd.setUOgrd(_uC);
							gpd.setVOgrd(_vC);
							gpd.setCdr(_Cdir);
							gpd.setCsp(_Cspeed);

							newGribPointData[h][w] = gpd;
//            System.out.println("New GribPointData set at [" + h + ", " + w + "]");
						} // _ar != null
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						System.out.println(aioobe.toString());
						aioobe.printStackTrace();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} // ar != null (points around)
				w++;
			} // for _Lng
			h++;
		} // for _Lat
		newGribData.setGribPointData(newGribPointData);

		return newGribData;
	}

	public static GribConditionData[] smoothGRIBinTime(GribConditionData[] original, int nbsteps) {
		GribConditionData[] newData = new GribConditionData[((original.length - 1) * nbsteps) + 1];
		for (int i = 0; i < original.length - 1; i++) {
			for (int j = 0; j < nbsteps; j++) {
				int idx = (i * nbsteps) + j;
//      System.out.println("i=" + i + ", j=" + j + ", idx=" + idx);
				// Smoothing here
				if (j == 0)
					newData[idx] = original[i];
				else {
					newData[idx] = new GribConditionData();
					Date newDate = new Date((long) getIntermediateValue(original[i].getDate().getTime(), original[i + 1].getDate().getTime(), nbsteps, j));
//        System.out.println("New Date for " + idx + ":" + newDate.toString());
					newData[idx].setDate(newDate);
					newData[idx].setELng(original[i].getELng());
					newData[idx].setWLng(original[i].getWLng());
					newData[idx].setNLat(original[i].getNLat());
					newData[idx].setSLat(original[i].getSLat());
					newData[idx].setStepX(original[i].getStepX());
					newData[idx].setStepY(original[i].getStepY());
					newData[idx].hgt = original[i].hgt;
					newData[idx].prmsl = original[i].prmsl;
					newData[idx].temp = original[i].temp;
					newData[idx].rain = original[i].rain;
					newData[idx].wave = original[i].wave;
					newData[idx].wind = original[i].wind;
					newData[idx].current = original[i].current;
					GribPointData[][] gpd1 = original[i].getGribPointData();
					GribPointData[][] gpd2 = original[i + 1].getGribPointData();
					GribPointData[][] gpd = new GribPointData[gpd1.length][gpd1[0].length];
					for (int h = 0; h < gpd.length; h++) {
						for (int w = 0; w < gpd[h].length; w++) {
							try {
//              System.out.println("h=" + h + ", w=" + w + ", gpd[h].length=" + gpd[h].length);
								gpd[h][w] = new GribPointData();
								gpd[h][w].setLat(gpd1[h][w].getLat());
								gpd[h][w].setLng(gpd1[h][w].getLng());
								gpd[h][w].setHgt((float) getIntermediateValue(gpd1[h][w].getHgt(), gpd2[h][w].getHgt(), nbsteps, j));
								gpd[h][w].setPrmsl((float) getIntermediateValue(gpd1[h][w].getPrmsl(), gpd2[h][w].getPrmsl(), nbsteps, j));
								gpd[h][w].setRain((float) getIntermediateValue(gpd1[h][w].getRain(), gpd2[h][w].getRain(), nbsteps, j));
								gpd[h][w].setAirtmp((float) getIntermediateValue(gpd1[h][w].getAirtmp(), gpd2[h][w].getAirtmp(), nbsteps, j));
								gpd[h][w].setTwd(getIntermediateValue(gpd1[h][w].getTwd(), gpd2[h][w].getTwd(), nbsteps, j));
								gpd[h][w].setTws(getIntermediateValue(gpd1[h][w].getTws(), gpd2[h][w].getTws(), nbsteps, j));
								gpd[h][w].setU((float) getIntermediateValue(gpd1[h][w].getU(), gpd2[h][w].getU(), nbsteps, j));
								gpd[h][w].setV((float) getIntermediateValue(gpd1[h][w].getV(), gpd2[h][w].getV(), nbsteps, j));
								gpd[h][w].setWHgt((float) getIntermediateValue(gpd1[h][w].getWHgt(), gpd2[h][w].getWHgt(), nbsteps, j));
								gpd[h][w].setUOgrd((float) getIntermediateValue(gpd1[h][w].getUOgrd(), gpd2[h][w].getUOgrd(), nbsteps, j));
								gpd[h][w].setVOgrd((float) getIntermediateValue(gpd1[h][w].getVOgrd(), gpd2[h][w].getVOgrd(), nbsteps, j));
								gpd[h][w].setCdr(getIntermediateValue(gpd1[h][w].getCdr(), gpd2[h][w].getCdr(), nbsteps, j));
								gpd[h][w].setCsp(getIntermediateValue(gpd1[h][w].getCsp(), gpd2[h][w].getCsp(), nbsteps, j));
							} catch (ArrayIndexOutOfBoundsException aioobe) {
								System.out.println("GribHelper.smoothGRIBinTime:" + aioobe.toString());
//              aioobe.printStackTrace();
							}
						}
					}
					newData[idx].setGribPointData(gpd);
				}
			}
		}
		// Last one
		newData[(original.length - 1) * nbsteps] = original[original.length - 1];

		return newData;
	}

	private static double getIntermediateValue(double a, double b, int nbInterval, int currInterval) {
		return a + (currInterval * ((b - a) / nbInterval));
	}

	public static void displayGRIBDetails(String gribName) throws Exception {
		try {
			GribHelper.GribConditionData[] thisGRIB = GribHelper.getGribData(gribName, false);
			GribFile gf = new GribFile(gribName);
			String mess = "Contains " + thisGRIB.length + " frame(s).";
			displayGRIBDetails(gf, mess);
		} catch (NoValidGribException e) {
			e.printStackTrace();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// LOCALIZE
	public static void displayGRIBDetails(GribFile gf, String mess) {
		try {
			String[] types = gf.getTypeNames();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			gf.listRecords(ps);
			mess += ("\n" + baos.toString());

			//      if (false)
			//      {
			//        mess += ("\nCell: " + Double.toString(gf.getGrids()[0].getGridDX()) + " x " + Double.toString(gf.getGrids()[0].getGridDY()));
			//        mess += ("\n" + new GeoPoint(thisGRIB[0].getNLat(), thisGRIB[0].getWLng()).toString() + " to " + new GeoPoint(thisGRIB[0].getSLat(), thisGRIB[0].getELng()).toString());
			//        mess += "\nData:";
			//        for (int i=0; i<types.length; i++)
			//        {
			//          GribRecordGDS[]  grg = gf.getGridsForType(types[i]);
			//          int unit = gf.getZunitsForTypeGrid(types[i], grg[0])[0];
			//          String level = gf.getLevelsForTypeGridUnit(types[i], grg[0], unit)[0].getLevel();
			//          String description = gf.getDescriptionForType(types[i]);
			//          mess += ("\n " + Integer.toString(i+1) + ". " + types[i] + " (" + description + ", " + level + ")");
			//        }
			//        for (int i=0; i<thisGRIB.length; i++)
			//        {
			//          GribHelper.GribConditionData data = thisGRIB[i];
			//          mess += ("\nDate:" + data.getDate().toString());
			//        }
			//      }
			// TODO Better output
			System.out.println(mess);
		} catch (NoValidGribException e) {
			e.printStackTrace();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class GribCondition {
		public float windspeed;
		public int winddir;
		public float hgt500;
		public int horIdx;
		public int vertIdx;
		public float prmsl;
		public float waves;
		public float temp;
		public float rain;
		public float currentspeed;
		public int currentdir;

		public String comment = null;

		public GribCondition() {
		}

		public GribCondition(float windspeed,
		                     int winddir,
		                     float hgt500,
		                     int horIdx,
		                     int vertIdx,
		                     float prmsl,
		                     float waves,
		                     float temp,
		                     float rain,
		                     float currentspeed,
		                     int currentdir) {
			this.windspeed = windspeed;
			this.winddir = winddir;
			this.hgt500 = hgt500;
			this.horIdx = horIdx;
			this.vertIdx = vertIdx;
			this.prmsl = prmsl;
			this.waves = waves;
			this.temp = temp;
			this.rain = rain;
			this.currentspeed = currentspeed;
			this.currentdir = currentdir;
		}
	}

	public static class GribConditionData {
		public boolean wind = true; // true by default
		public boolean prmsl = false;
		public boolean temp = false;
		public boolean hgt = false;
		public boolean wave = false;
		public boolean rain = false;
		public boolean current = false;

		private Date date;
		private double wLng;
		private double eLng;
		private double nLat;
		private double sLat;
		private double stepX;
		private double stepY;
		private GribPointData[][] gribPointData;

		public GribConditionData() {
		}

		public void setDate(Date d) {
			date = d;
		}

		public void setWLng(double d) {
			wLng = d;
		}

		public void setELng(double d) {
			eLng = d;
		}

		public void setNLat(double d) {
			nLat = d;
		}

		public void setSLat(double d) {
			sLat = d;
		}

		public void setStepX(double d) {
			stepX = d;
		}

		public void setStepY(double d) {
			stepY = d;
		}

		public void setGribPointData(GribPointData[][] gpd) {
			gribPointData = gpd;
		}

		public Date getDate() {
			return date;
		}

		public double getWLng() {
			return wLng;
		}

		public double getELng() {
			return eLng;
		}

		public double getNLat() {
			return nLat;
		}

		public double getSLat() {
			return sLat;
		}

		public double getStepX() {
			return stepX;
		}

		public double getStepY() {
			return stepY;
		}

		public GribPointData[][] getGribPointData() {
			return gribPointData;
		}

		/**
		 * @param pt The position to find the points around for.
		 * @return indexes (2 integers) of the <b>bottom left</b> grib data point
		 * <p>
		 * get the two Integers from the returned ArrayList, Y, and X
		 * The 4 points we are interested in will eventually be:
		 * <p>
		 * bottom-left : gribPointData[Y][X]
		 * bottom-right: gribPointData[Y][X+1]
		 * top-right   : gribPointData[Y+1][X+1]
		 * top-left    : gribPointData[Y+1][X]
		 */
		public List<Integer> getDataPointsAround(GeoPoint pt) {
			List<Integer> array = null;
			double l = pt.getL();
			double g = pt.getG();
			// G same sign...
			double _wLng = gribPointData[0][0].getLng(), _eLng = eLng;
			double _sLat = gribPointData[0][0].getLat(), _nLat = nLat;
			if (StaticUtil.sign(_wLng) != StaticUtil.sign(_eLng) && _wLng > 0) // Around Ante meridian
			{
				_wLng -= 360D;
				if (g > 0) g -= 360D;
			}
			if (isBetween(l, _nLat, _sLat) && isBetween(g, _wLng, _eLng)) {
				double deltaX = g - _wLng;
				double deltaY = l - _sLat;
				int idxX = (int) Math.floor(deltaX / stepX);
				int idxY = (int) Math.floor(deltaY / stepY);
				// Warning/Reminder: [0][0] bottom left
//        System.out.println(pt.toString() + " is between:");
//        System.out.println( windPointData[idxY][idxX].toString() );
//        System.out.println( windPointData[idxY][idxX + 1].toString() );
//        System.out.println( windPointData[idxY + 1][idxX].toString() );
//        System.out.println( windPointData[idxY + 1][idxX + 1].toString() );
//        System.out.println("------------------");
				array = new ArrayList<>(2);
				// Indexes of the bottom left point.
				array.add(idxY);
				array.add(idxX);
			}
			return array;
		}
	}

	public static class GribPointData {
		private double lat;
		private double lng;
		private float u;
		private float v;
		private float hgt;
		private float prmsl;
		private float airtmp;
		private float seatmp;
		private float whgt;
		private float rain;

		private float uOgrd;
		private float vOgrd;

		private double tws = -1D;
		private double twd = -1D;

		private double csp = -1D;
		private double cdr = -1D;

		public GribPointData() {
		}


		public void setLat(double d) {
			lat = d;
		}

		public void setLng(double d) {
			lng = d;
		}

		public void setU(float i) {
			u = i;
		}

		public void setV(float i) {
			v = i;
		}

		public void setHgt(float i) {
			hgt = i;
		}

		public void setPrmsl(float i) {
			prmsl = i;
		}

		public void setAirtmp(float i) {
			airtmp = i;
		}

		public void setWHgt(float i) {
			whgt = i;
		}

		public void setRain(float f) {
			rain = f;
		}

		public double getLat() {
			return lat;
		}

		public double getLng() {
			return lng;
		}

		public float getU() {
			return u;
		}

		public float getV() {
			return v;
		}

		public float getHgt() {
			return hgt;
		}

		public float getPrmsl() {
			return prmsl;
		}

		public float getAirtmp() {
			return airtmp;
		}

		public float getWHgt() {
			return whgt;
		}

		public float getRain() {
			return rain;
		}

		public String toString() {
			return new GeoPoint(lat, lng).toString();
		}

		public void setTws(double tws) {
			this.tws = tws;
		}

		public double getTws() {
			return tws;
		}

		public void setTwd(double twd) {
			this.twd = twd;
		}

		public double getTwd() {
			return twd;
		}

		public void setSeatmp(float seatmp) {
			this.seatmp = seatmp;
		}

		public float getSeatmp() {
			return seatmp;
		}

		public void setUOgrd(float uOgrd) {
			this.uOgrd = uOgrd;
		}

		public float getUOgrd() {
			return uOgrd;
		}

		public void setVOgrd(float vOgrd) {
			this.vOgrd = vOgrd;
		}

		public float getVOgrd() {
			return vOgrd;
		}

		public void setCsp(double csp) {
			this.csp = csp;
		}

		public double getCsp() {
			return csp;
		}

		public void setCdr(double cdr) {
			this.cdr = cdr;
		}

		public double getCdr() {
			return cdr;
		}
	}

	private static class TempGribData {
		protected Date date;
		protected int width, height;
		protected double top, bottom, left, right;
		protected double stepX, stepY;
		protected String type;
		protected String description;
		protected float[][] data;
	}

	public GribHelper() {
	}

	public static GribConditionData[] getGribData(InputStream stream, String name) throws Exception {
		List<GribConditionData> wgd = null;
		try {
			TimeZone tz = TimeZone.getTimeZone("etc/UTC"); // "GMT + 0"
			TimeZone.setDefault(tz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
//    System.err.println("Managing [" + name +"]");
			GribFile gribFile = new GribFile(stream);
//			WWContext.getInstance().setGribFile(gribFile); TODO Check what this does
			stream.close();
			int recordCount = gribFile.getRecordCount();
//    System.out.println("Found " + recordCount + " GRIB record(s)");
			wgd = dumper(gribFile, name);
		} catch (IOException ioError) {
			System.err.println("For [" + name + "], IOException : " + ioError);
			JOptionPane.showMessageDialog(null, ioError.toString(), "For [" + name + "]", JOptionPane.ERROR_MESSAGE);
		} catch (NoValidGribException noGrib) {
			System.err.println("For [" + name + "], NoValidGribException : " + noGrib);
			JOptionPane.showMessageDialog(null, noGrib.toString(), "For [" + name + "]", JOptionPane.ERROR_MESSAGE);
		} catch (NotSupportedException noSupport) {
			noSupport.printStackTrace();
			System.err.println("For [" + name + "], NotSupportedException : " + noSupport);
			JOptionPane.showMessageDialog(null, noSupport.toString(), "For [" + name + "]", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			throw e;
		}
		GribConditionData[] gcd = null;
		if (wgd != null)
			gcd = new GribConditionData[wgd.size()];
		return (gcd != null ? wgd.toArray(gcd) : null);
	}

	public static GribConditionData[] getGribData(String fileName) throws Exception {
		return getGribData(fileName, false);
	}

	public static GribConditionData[] getGribData(String fileName, boolean verb) throws Exception {
		List<GribConditionData> wgd = null;
		try {
			TimeZone tz = TimeZone.getTimeZone("127"); // TODO replace with etc/UTC
			TimeZone.setDefault(tz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
//    System.err.println("Managing [" + fileName +"]");
			GribFile gribFile = new GribFile(fileName);
			// WWContext.getInstance().setGribFile(gribFile); TODO Check what this does
			int recordCount = gribFile.getRecordCount();
			wgd = dumper(gribFile, fileName, verb);
		} catch (FileNotFoundException noFileError) {
			System.err.println("For [" + fileName + "], " + "FileNotFoundException : " + noFileError);
			noFileError.printStackTrace();
		} catch (IOException ioError) {
			System.err.println("For [" + fileName + "], " + "IOException : " + ioError);
			JOptionPane.showMessageDialog(null, ioError.toString(), "For [" + fileName + "]", JOptionPane.ERROR_MESSAGE);
		} catch (NoValidGribException noGrib) {
			System.err.println("For [" + fileName + "], " + "NoValidGribException : " + noGrib);
			JOptionPane.showMessageDialog(null, noGrib.toString(), "For [" + fileName + "]", JOptionPane.ERROR_MESSAGE);
		} catch (NotSupportedException noSupport) {
			noSupport.printStackTrace();
			System.err.println("For [" + fileName + "], " + "NotSupportedException : " + noSupport);
			JOptionPane.showMessageDialog(null, noSupport.toString(), "For [" + fileName + "]", JOptionPane.ERROR_MESSAGE);
		}
		GribConditionData[] gcd = null;
		if (wgd != null)
			gcd = new GribConditionData[wgd.size()];
		return (gcd != null ? wgd.toArray(gcd) : null);
	}

	public static List<GribConditionData> dumper(GribFile gribFile, String fileName) throws Exception {
		return dumper(gribFile, fileName, false);
	}

	public static List<GribConditionData> dumper(GribFile gribFile, String fileName, boolean verb) throws Exception {
		Map<String, String> unrecognized = new Hashtable<>();

		List<GribConditionData> wgd = null;
		Map<String, Map<Date, TempGribData>> map = new HashMap<>();

		TimeZone tz = TimeZone.getTimeZone("etc/UTC"); // "GMT + 0"
//  TimeZone.setDefault(tz);
		GRIBUtils.SDF.setTimeZone(tz);


		for (int i = 0; i < gribFile.getLightRecords().length; i++) {
			try {
				GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
				GribRecordPDS grpds = gr.getPDS(); // Headers and Data

				GribRecordGDS grgds = gr.getGDS(); // Boundaries and Steps
				GribRecordBDS grbds = gr.getBDS(); // Min/Max TASK Use those ones

				TempGribData tgd = new TempGribData();
				tgd.date = grpds.getGMTForecastTime().getTime();
				tgd.width = grgds.getGridNX();
				tgd.height = grgds.getGridNY();
				tgd.stepX = grgds.getGridDX();
				tgd.stepY = grgds.getGridDY();
				tgd.top = Math.max(grgds.getGridLat1(), grgds.getGridLat2());
				tgd.bottom = Math.min(grgds.getGridLat1(), grgds.getGridLat2());
				tgd.left = Math.min(grgds.getGridLon1() > 180d ? grgds.getGridLon1() - 360 : grgds.getGridLon1(),
						grgds.getGridLon2() > 180 ? grgds.getGridLon2() - 360 : grgds.getGridLon2());
				tgd.right = Math.max(grgds.getGridLon1() > 180d ? grgds.getGridLon1() - 360 : grgds.getGridLon1(),
						grgds.getGridLon2() > 180 ? grgds.getGridLon2() - 360 : grgds.getGridLon2());

				tgd.type = grpds.getType();
				tgd.description = grpds.getDescription();

				float[][] airTmpData = null;
				float[][] seaTmpData = null;

				float val = 0F;
				for (int col = 0; col < tgd.width; col++) {
					for (int row = 0; row < tgd.height; row++) {
						try {
							val = gr.getValue(col, row);
							if (val > 200000F)
								val = 0.0F;
							if (tgd.type.equals("htsgw"))
								val *= 100F;
							if (tgd.type.equals("tmp")) {
								if (grpds.getLevel().equals("2.0m")) {
									if (airTmpData == null)
										airTmpData = new float[tgd.height][tgd.width];
									airTmpData[row][col] = val;
								} else if (grpds.getLevel().equals("surface")) {
									if (seaTmpData == null)
										seaTmpData = new float[tgd.height][tgd.width];
									seaTmpData[row][col] = val;
								}
							} else {
								if (tgd.data == null)
									tgd.data = new float[tgd.height][tgd.width];
								tgd.data[row][col] = val;
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
				String type = tgd.type;
				boolean typeLoop = true;
				int nbTmpChecked = 0;
				while (typeLoop) {
					if (tgd.type.equals("tmp")) {
						nbTmpChecked++;
						if (nbTmpChecked == 1 && airTmpData != null) {
							type = "airtmp";
							tgd.data = airTmpData;
							if (seaTmpData == null)
								typeLoop = false;
						} else if (seaTmpData != null) {
							type = "seatmp";
							tgd.data = seaTmpData;
							typeLoop = false;
						}
					} else
						typeLoop = false;
					Map<Date, TempGribData> mapForType = map.get(type);
					if (mapForType == null) {
						mapForType = new TreeMap<>();
						map.put(type, mapForType);
					}
					mapForType.put(tgd.date, tgd);
				}
			} catch (NoValidGribException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NotSupportedException e) {
				e.printStackTrace();
			}
		}
		if (false) {
			// Dump, for tests
			Set<String> keys = map.keySet();
			Iterator<String> itStr = keys.iterator();
			while (itStr.hasNext()) {
				String key = itStr.next();
				System.out.println("KEY: " + key);
				Map<Date, TempGribData> mapForType = map.get(key);
				Set<Date> dateKeys = mapForType.keySet();
				Iterator<Date> itDate = dateKeys.iterator();
				while (itDate.hasNext()) {
					Date date = itDate.next();
					TempGribData tgd2 = mapForType.get(date);
					System.out.println("\t " + tgd2.type + " " + date.toString() + ", " + tgd2.date.toString() + " " + tgd2.top + "/" + tgd2.bottom + " " + tgd2.left + "/" + tgd2.right + " " + tgd2.stepX + "-" + tgd2.stepY + " " + tgd2.width + "x" + tgd2.height);
				}
			}
		}
		// Sort them by date!!
		// All GRIB Records should have the same structure in term of dates and dimensions.
		// Data (GribConditionData) are added to the ArrayList, by date.
		Map<Date, GribConditionData> tMap = new TreeMap<>();
		Set<String> keys = map.keySet();
		Iterator<String> itStr = keys.iterator();
		while (itStr.hasNext()) {
			String key = itStr.next(); // That is the type
			Map<Date, TempGribData> mapForType = map.get(key);
			Set<Date> dateKeys = mapForType.keySet();
			Iterator<Date> itDate = dateKeys.iterator();
			while (itDate.hasNext()) {
				Date date = itDate.next();
				TempGribData tgd2 = mapForType.get(date);

				GribConditionData gcd = tMap.get(date);
				if (gcd == null) {
					gcd = new GribConditionData();
					tMap.put(date, gcd);
					gcd.date = date;
					gcd.setStepX(tgd2.stepX);
					gcd.setStepY(tgd2.stepY);
					double east = Math.max(tgd2.left, tgd2.right);
					double west = Math.min(tgd2.left, tgd2.right);
					if (Math.abs(west - east) > 180D) {
						double tmp = east;
						east = west;
						west = tmp;
					}
					gcd.setWLng(west);
					gcd.setELng(east);
					gcd.setNLat(tgd2.top);
					gcd.setSLat(tgd2.bottom);
				}
//        else
//          System.out.println("Found the GribConditionData from the map");

				// gcd.setStepX(tgd2.stepX);
				// gcd.setStepY(tgd2.stepY);

				boolean resizeNeeded = false;
				try {
					int arrayW = tgd2.width;
					int arrayH = tgd2.height;

					GribPointData[][] wpd = gcd.getGribPointData();
					if (wpd == null) {
						wpd = new GribPointData[arrayH][arrayW];
						gcd.setGribPointData(wpd);
					} else {
//          System.out.println("GribPointData array already exists.");
						if (wpd.length != arrayH) // was != instead of <
						{
//            String mess = "For" + key + ": DataArray (height) size mismatch in " + fileName + ", wpd.length=" + wpd.length + ", arrayH=" + arrayH;
////          throw new RuntimeException(mess);
//            System.out.println(mess);
							resizeNeeded = true;
						} else {
							if (wpd[0].length != arrayW) // was != instead of <
							{
//              String mess = "For " + key + ": DataArray (width) size mismatch in " + fileName + ", wpd[0].length=" + wpd[0].length + ", arrayW=" + arrayW;
////            throw new RuntimeException(mess);
//              System.out.println(mess);
								resizeNeeded = true;
							}
						}
					}
					if (resizeNeeded) {
//          System.out.println("Resize needed for [" + key + "], " + arrayH + "x" + arrayW + " to turn into " + wpd.length + "x" + wpd[0].length);
						float[][] newData = expandDataArray(tgd2.data, wpd.length, wpd[0].length);
						tgd2.data = newData;

						arrayH = wpd.length;
						arrayW = wpd[0].length;
					}
					// Good to go
					for (int i = 0; i < arrayH; i++) {
						for (int j = 0; j < arrayW; j++) {
							if (wpd[i][j] == null) {
								wpd[i][j] = new GribPointData();
								double l = gcd.getSLat() + (gcd.stepY / 2d + (double) i * gcd.stepY);
								double g = gcd.getWLng() + (gcd.stepX / 2d + (double) j * gcd.stepX);
								if (g > 180D)
									g -= 360;
								wpd[i][j].setLat(l);
								wpd[i][j].setLng(g);
							}
//            else
//              System.out.println("Point[" + i + "][" + j + "] already exists");

							try {
								if (key.equals("ugrd")) {
//                wpd[i][j].setX((int)Math.round(tgd2.data[i][j]));
									wpd[i][j].setU(tgd2.data[i][j]);
								} else if (key.equals("vgrd")) {
									wpd[i][j].setV(tgd2.data[i][j]);
								} else if (key.equals("uogrd")) {
									wpd[i][j].setUOgrd(tgd2.data[i][j]);
								} else if (key.equals("vogrd")) {
									wpd[i][j].setVOgrd(tgd2.data[i][j]);
								} else if (key.equals("prmsl")) {
									wpd[i][j].setPrmsl(tgd2.data[i][j]);
								} else if (key.equals("hgt")) {
									wpd[i][j].setHgt(tgd2.data[i][j]);
								} else if (key.equals("htsgw")) {
									wpd[i][j].setWHgt(tgd2.data[i][j]);
								} else if (key.equals("airtmp")) { // Air Temperature
									wpd[i][j].setAirtmp(tgd2.data[i][j]);
								} else if (key.equals("seatmp")) { // Sea Temperature
									wpd[i][j].setSeatmp(tgd2.data[i][j]);
								} else if (key.equals("prate")) {
									// Unit is Kg x m-2 x s-1, which is 1mm.s-1
									wpd[i][j].setRain(tgd2.data[i][j]);
								} else {
									if (!unrecognized.containsKey(key)) {
										String mess = "Type [" + key + "] not manadged : " + tgd2.description;
										unrecognized.put(key, mess);
										System.err.println("GribHelper:" + mess);
									}
//                System.out.println(key + " value: " + Float.toString(tgd2.data[i][j]));
								}
							} catch (Exception exx) {
								exx.printStackTrace();
							}
						}
					}
				} catch (RuntimeException rte) {
					String mess = rte.getMessage();
//        System.out.println("RuntimeException getMessage(): [" + mess + "]");
					if (mess.contains("DataArray (width) size mismatch") ||
							mess.contains("DataArray (height) size mismatch"))
						System.out.println(mess);
					else
						throw rte;
				} catch (Exception ex) {
					// ex.printStackTrace();
					throw ex;
				}
			}
		}
		// Now populate the output
		Set<Date> gribDates = tMap.keySet();
		Iterator<Date> dateIterator = gribDates.iterator();
		while (dateIterator.hasNext()) {
			GribConditionData cd = tMap.get(dateIterator.next());
			if (wgd == null) {
				wgd = new ArrayList<>(gribDates.size());
			}
			wgd.add(cd);
		}
		if (unrecognized.size() > 0 && verb) {
			String message = "<html>";
			for (String s : unrecognized.keySet()) {
				message += (unrecognized.get(s) + "<br>");
			}
			message += "</html>";
			JOptionPane.showMessageDialog(null, message, "GRIB", JOptionPane.WARNING_MESSAGE);
		}

		return wgd;
	}

	private static float[][] expandDataArray(float[][] original, int newHeight, int newWidth) {
		float[][] newArray = new float[newHeight][newWidth];
		int origHeight = original.length;
		int origWidth = original[0].length;
		for (int i = 0; i < newHeight; i++) {
			for (int j = 0; j < newWidth; j++) {
				float floatWidth = (float) origWidth * (float) j / (float) newWidth;
				float floatHeight = (float) origHeight * (float) i / (float) newHeight;
				int origI = (int) Math.floor(floatHeight);
				int origJ = (int) Math.floor(floatWidth);
				try {
					// C'est un petit peu gonflé...
					newArray[i][j] = original[origI][origJ];
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					System.err.println("newArray[" + newHeight + "][" + newWidth + "] for i=" + i + " j=" + j +
							", original[" + origHeight + "][" + origWidth + "] for i=" + origI + " j=" + origJ);
					System.err.println("GribHelper - 2:" + aioobe.toString());
				}
//      System.out.println("For i:" + i + ", j:" + j + " w:" + floatWidth + ", h:" + floatHeight);
			}
		}
		return newArray;
	}

	public static boolean isBetween(double value, double one, double two) {
		return (value <= Math.max(one, two) && value >= Math.min(one, two));
	}

	public static GribCondition gribLookup(GeoPoint gp, GribConditionData[] wgdArray, Date date) {
		GribCondition gribCond = null;
		GribConditionData wgd = null;
		long refDate = date.getTime();
		long interval = 0L;
		for (int i = 0; i < wgdArray.length; i++) {
			if (i < wgdArray.length - 1) {
				interval = Math.abs(wgdArray[i].getDate().getTime() - wgdArray[i + 1].getDate().getTime());
				if (refDate < wgdArray[i].getDate().getTime() || refDate >= wgdArray[i + 1].getDate().getTime()) {
					continue;
				}
				wgd = wgdArray[i];
//      System.out.println("Found date:" + wgdArray[i].getDate().toString());
				break;
			}
			wgd = wgdArray[i];
			if (Math.abs(wgdArray[i].getDate().getTime() - refDate) > interval) {
				if (!alreadySaidTooOld) {
//        System.out.println("Last GRIB record might be too old, using it anyway...");
					alreadySaidTooOld = true;
				}
			} else {
				alreadySaidTooOld = false;
			}
		}

		double pointLng = gp.getG();
		double pointLat = gp.getL();
		double stepx = wgd.getStepX();
		double stepy = wgd.getStepY();
		GribPointData wpd[][] = wgd.getGribPointData();
		// Use the isBetween function
		for (int l = 0; l < wpd.length; l++) {
			for (int g = 0; g < wpd[l].length; g++) {
				double diff_01 = Math.abs(pointLng - wpd[l][g].getLng());
				if (diff_01 > 180.0) {
					diff_01 = 360 - diff_01;
				}
				double diff_02 = Math.abs(pointLat - wpd[l][g].getLat());

				if (diff_01 <= (stepx / 2D) && diff_02 <= (stepy / 2D)) {
					gribCond = new GribCondition();
					float x = wpd[l][g].getU();
					float y = wpd[l][g].getV();
					double speed = Math.sqrt(x * x + y * y); // m/s
					speed *= 3.600D; // km/h
					speed /= 1.852D; // knots
					double dir = 0d;
					try {
						dir = NMEAUtils.getDir(x, y);
					} catch (NMEAUtils.AmbiguousException ae) {
						// Absorb. Leave dir to 0
						System.err.println(ae.getMessage());
					}
					gribCond.winddir = (int) Math.round(dir);
					gribCond.windspeed = adjustWindSpeed((float) speed);
					gribCond.hgt500 = wpd[l][g].getHgt();
					gribCond.horIdx = g;
					gribCond.vertIdx = l;
					gribCond.prmsl = wpd[l][g].getPrmsl();
					gribCond.waves = wpd[l][g].getWHgt();
					gribCond.temp = wpd[l][g].getAirtmp();
					gribCond.rain = wpd[l][g].getRain();

					float xC = wpd[l][g].getUOgrd();
					float yC = wpd[l][g].getVOgrd();
					double cSpeed = Math.sqrt(xC * xC + yC * yC); // m/s
					cSpeed *= 3.600D; // km/h
					cSpeed /= 1.852D; // knots
					double cDir = 0d;
					try {
						cDir = NMEAUtils.getDir(xC, yC);
					} catch (NMEAUtils.AmbiguousException ae) {
						// Aborb, leave dir to 0
						System.err.println(ae.getMessage());
					}
					cDir += 180;
					while (cDir > 360) {
						cDir -= 360;
					}
					gribCond.currentdir = (int) Math.round(cDir);
					gribCond.currentspeed = (float) cSpeed;
//        System.out.println("l:" + l + ",g:" + g + ", temp:" + (gribCond.temp - 273) + ", rain:" + (gribCond.rain * 3600f));
					if (alreadySaidTooOld) {
						gribCond.comment = "TOO_OLD";
					}
					return gribCond;
				}
			}
		}
//  System.out.println(GeomUtil.decToSex(gp.getL()) + "/" + GeomUtil.decToSex(gp.getG()) + " is not in that grid...");
		return gribCond;
	}

	public static float adjustWindSpeed(float speed) {
		return adjustWindSpeed(speed, 1f);
	}
	public static float adjustWindSpeed(float speed, float coeff) {
		return coeff * speed;
	}

	public static GribCondition gribLookup(GeoPoint gp, GribConditionData wgd) {
		GribCondition gribCond = null;

		double pointLng = gp.getG();
		double pointLat = gp.getL();
		double stepx = wgd.getStepX();
		double stepy = wgd.getStepY();
		GribPointData wpd[][] = wgd.getGribPointData();
		// Use the isBetween function
		for (int l = 0; l < wpd.length; l++) {
			for (int g = 0; g < wpd[l].length; g++) {
				if (wpd[l][g] != null) {
					double diff_01 = Math.abs(pointLng - wpd[l][g].getLng());
					if (diff_01 > 180.0) {
						diff_01 = 360 - diff_01;
					}
					double diff_02 = Math.abs(pointLat - wpd[l][g].getLat());

					if (diff_01 <= (stepx / 2D) && diff_02 <= (stepy / 2D)) {
						gribCond = new GribCondition();
						double speed = 0D, dir = 0D;
						if (wpd[l][g].getTwd() != -1D && wpd[l][g].getTws() != -1D) {
							speed = wpd[l][g].getTws();
							dir = wpd[l][g].getTwd();
						} else {
							float x = wpd[l][g].getU();
							float y = wpd[l][g].getV();
							speed = Math.sqrt(x * x + y * y); // m/s
							speed *= 3.600D; // km/h
							speed /= 1.852D; // knots
							dir = 0d;
							try {
								dir = NMEAUtils.getDir(x, y);
							} catch (NMEAUtils.AmbiguousException ae) {
								// Absorb. Leave dir to 0
								System.err.println(ae.getMessage());
							}
						}
						gribCond.winddir = (int) Math.round(dir);
						gribCond.windspeed = adjustWindSpeed((float) speed);
						gribCond.hgt500 = wpd[l][g].getHgt();
						gribCond.horIdx = g;
						gribCond.vertIdx = l;
						gribCond.prmsl = wpd[l][g].getPrmsl();
						gribCond.waves = wpd[l][g].getWHgt();
						gribCond.temp = wpd[l][g].getAirtmp();
						gribCond.rain = wpd[l][g].getRain();
						double cSpeed = 0D, cDir = 0D;
						if (wpd[l][g].getCdr() != -1D && wpd[l][g].getCsp() != -1D) {
							cSpeed = wpd[l][g].getCsp();
							cDir = wpd[l][g].getCdr();
						} else {
							float x = wpd[l][g].getUOgrd();
							float y = wpd[l][g].getVOgrd();
							speed = Math.sqrt(x * x + y * y); // m/s
							speed *= 3.600D; // km/h
							speed /= 1.852D; // knots
							dir = 0d;
							try {
								dir = NMEAUtils.getDir(x, y);
							} catch (NMEAUtils.AmbiguousException ae) {
								// Absorb. Leave dir to 0
								System.err.println(ae.getMessage());
							}
							dir += 180;
							while (dir > 360) dir -= 360;
						}
						gribCond.currentdir = (int) Math.round(dir);
						gribCond.currentspeed = (float) speed;

						return gribCond;
					}
				}
			}
		}
		//  System.out.println(GeomUtil.decToSex(gp.getL()) + "/" + GeomUtil.decToSex(gp.getG()) + " is not in that grid...");
		return gribCond;
	}

	public static void main1(String[] args) {
		System.out.println("123 is" + (isBetween(123, 0, 200) ? " " : " not") + " between " + 0 + " and " + 200);
		System.out.println("123 is" + (isBetween(123, 0, -200) ? " " : " not") + " between " + 0 + " and " + -200);
		System.out.println("123 is" + (isBetween(123, -100, 200) ? " " : " not") + " between " + -100 + " and " + 200);
	}

}
