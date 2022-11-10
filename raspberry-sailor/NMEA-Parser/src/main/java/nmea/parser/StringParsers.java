package nmea.parser;

import calc.GeomUtil;

import java.io.PrintStream;
import java.text.NumberFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic form is
 * $<talker ID><sentence ID,>[parameter 1],[parameter 2],...[<*checksum>]<CR><LF> (\r\n)
 *
 * Available parsers:
 * - BAT (battery status, NOT standard)
 * - DBT (Depth Below Transducer)
 * - DPT (Depth)
 * - GGA (GPS Data)
 * - GLL (Geographical Latitude Longitude)
 * - GSA (GPS Satellites Data)
 * - GSV (GPS Detailed satellites data)
 * - HDM (Heading, Magnetic)
 * - HDT (Heading, True)
 * - MDA (Meteorological Composite)
 * - MMB (Atmospheric Pressure)
 * - MTA (Air Temperature)
 * - MTW (Water Temperature)
 * - MWD ((True) Wind Direction and Speed)
 * - MWV (Wind Speed and Angle)
 * - RMB (Recommended Minimum, version B)
 * - RMC (Recommended Minimum, version C)
 * - STD (Not standard, STarteD)
 * - TXT (Text)
 * - VDR (Current Speed and Direction)
 * - VHW (Water, Heading and Speed)
 * - VLW (Distance Travelled through Water)
 * - VTG (Track Made Good and Ground Speed)
 * - VWR (Relative Wind Speed and Angle)
 * - VWT (True Wind Speed and Angle - obsolete)
 * - XDR (Transducers Measurement, Various Sensors)
 * - ZDA (UTC DCate and Time)
 *
 * See {@link StringParsers.Dispatcher}, {@link #listDispatchers(PrintStream)}
 *
 * TASK Implement the following:
 *
 * MDW Surface Wind, direction and velocity
 * VPW Device measured velocity parallel true wind
 * ZLZ Time of Day
 *
 * Good source: http://www.catb.org/gpsd/NMEA.html
 */
public class StringParsers {

  public final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");
  static {
  	SDF_UTC.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
  }

	private static Map<Integer, SVData> gsvMap = null;

	public static List<StringGenerator.XDRElement> parseXDR(String data) {
		List<StringGenerator.XDRElement> lxdr = new ArrayList<>();
		String[] sa = data.substring(0, data.indexOf("*")).split(",");
		if ((sa.length - 1) % 4 != 0) { // Mismatch
			System.out.println("XDR String invalid (" + sa.length + " element(s) found, expected a multiple of 4)");
			return lxdr;
		}
		for (int i = 1; i < sa.length; i += 4) {
			String type = sa[i];
			String valStr = sa[i + 1];
			String unit = sa[i + 2];
			String tname = sa[i + 3];
			// Valid unit and type
			boolean foundType = false;
			boolean foundUnit = false;
			for (StringGenerator.XDRTypes xdrt : StringGenerator.XDRTypes.values()) {
				if (xdrt.type().equals(type)) {
					foundType = true;
					if (xdrt.unit().equals(unit)) {
						foundUnit = true;
						try {
							if (!valStr.trim().isEmpty()) {
								double value = Double.parseDouble(valStr);
								lxdr.add(new StringGenerator.XDRElement(xdrt, value, tname));
							}
						} catch (NumberFormatException nfe) {
							if (!valStr.trim().isEmpty()) {
								throw new RuntimeException(nfe);
							}
						}
						break;
					}
				}
			}
			if (!foundType) {
				System.out.println("Unknown XDR type [" + type + "], in [" + data + "]");
				return lxdr;
			}
			if (!foundUnit) {
				System.out.println("Invalid XDR unit [" + unit + "] for type [" + type + "], in [" + data + "]");
				return lxdr;
			}
		}

		return lxdr;
	}

	public static class MDA {
		public Double pressInch = null;
		public Double pressBar = null;
		public Double airT = null;
		public Double waterT = null;
		public Double relHum = null;
		public Double absHum = null;
		public Double dewC = null;
		public Double windDirT = null;
		public Double windDirM = null;
		public Double windSpeedK = null;
		public Double windSpeedMS = null;
	}

	// MDA Meteorological Composite
	public static MDA parseMDA(String data) {
		final int PRESS_INCH = 1;
		final int PRESS_BAR = 3;
		final int AIR_T = 5;
		final int WATER_T = 7;
		final int REL_HUM = 9;
		final int ABS_HUM = 10;
		final int DEW_P_C = 11;
		final int WD_T = 13;
		final int WD_M = 15;
		final int WS_KNOTS = 17;
		final int WS_MS = 19;

		/*
		 * $--MDA,x.x,I,x.x,B,x.x,C,x.x,C,x.x,x.x,x.x,C,x.x,T,x.x,M,x.x,N,x.x,M*hh<CR><LF>
		 *        |     |     |     |     |   |   |     |     |     |     |
		 *        |     |     |     |     |   |   |     |     |     |     19-Wind speed, m/s
		 *        |     |     |     |     |   |   |     |     |     17-Wind speed, knots
		 *        |     |     |     |     |   |   |     |     15-Wind dir Mag
		 *        |     |     |     |     |   |   |     13-Wind dir, True
		 *        |     |     |     |     |   |   11-Dew Point C
		 *        |     |     |     |     |   10-Absolute hum %
		 *        |     |     |     |     9-Relative hum %
		 *        |     |     |     7-Water temp in Celcius
		 *        |     |     5-Air Temp in Celcius  |
		 *        |     3-Pressure in Bars
		 *        1-Pressure in inches
		 *
		 * Example: $WIMDA,29.4473,I,0.9972,B,17.2,C,,,,,,,,,,,,,,*3E
		 */
		String[] sa = data.substring(0, data.indexOf("*")).split(",");
		MDA mda = new MDA();
		for (int i = 0; i < sa.length; i++) {
			//  System.out.println(sa[i]);
			if (/*i % 2 == 1 &&*/ !sa[i].trim().isEmpty()) {
				double d = 0;
				try {
					d = Double.parseDouble(sa[i]);
					switch (i) {
						case PRESS_INCH:
							mda.pressInch = d;
							break;
						case PRESS_BAR:
							mda.pressBar = d;
							break;
						case AIR_T:
							mda.airT = d;
							break;
						case WATER_T:
							mda.waterT = d;
							break;
						case REL_HUM:
							mda.relHum = d;
							break;
						case ABS_HUM:
							mda.absHum = d;
							break;
						case DEW_P_C:
							mda.dewC = d;
							break;
						case WD_T:
							mda.windDirT = d;
							break;
						case WD_M:
							mda.windDirM = d;
							break;
						case WS_KNOTS:
							mda.windSpeedK = d;
							break;
						case WS_MS:
							mda.windSpeedMS = d;
							break;
						default:
							break;
					}
				} catch (NumberFormatException nfe) {
					// Oops
				}
			}
		}
		return mda;
	}

	/**
	 * MMB Atmospheric pressure
	 * @param data the one to parse
	 * @return Pressure in Mb / hPa
	 */
	public static double parseMMB(String data) {
		final int PR_IN_HG = 1;
		final int PR_BARS = 3;
		/*
		 * Structure is $IIMMB,29.9350,I,1.0136,B*7A
		 *                     |       | |      |
		 *                     |       | |      Bars
		 *                     |       | Pressure in Bars
		 *                     |       Inches of Hg
		 *                     Pressure in inches of Hg
		 */
		double d = 0d;
		String[] sa = data.substring(0, data.indexOf("*")).split(",");
		try {
			d = Double.parseDouble(sa[PR_BARS]);
			d *= 1_000d;
		} catch (NumberFormatException nfe) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				nfe.printStackTrace();
			}
		}
		return d;
	}

	// MTA Air Temperature
	public static double parseMTA(String data) {
		final int TEMP_CELCIUS = 1;
		/*
		 * Structure is $IIMTA,020.5,C*30
		 *                     |     |
		 *                     |     Celcius
		 *                     Temperature in Celcius
		 */
		double d = 0d;
		String[] sa = data.substring(0, data.indexOf("*")).split(",");
		try {
			d = Double.parseDouble(sa[TEMP_CELCIUS]);
		} catch (NumberFormatException nfe) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				nfe.printStackTrace();
			}
		}
		return d;
	}

	// VDR Current Speed and Direction
	public static Current parseVDR(String data) {
		final int DIR = 1;
		final int SPEED = 5;
		/*
		 * Structure is $IIVDR,00.0,T,00.0,M,00.0,N*XX
		 *                     |    | |    | |    |
		 *                     |    | |    | |    Knots
		 *                     |    | |    | Speed
		 *                     |    | |    Mag.
		 *                     |    | Magnetic Dir
		 *                     |    True
		 *                     True Dir
		 */
		Current current = null;
		String[] sa = data.substring(0, data.indexOf("*")).split(",");
		try {
			double speed = Double.parseDouble(sa[SPEED]);
			float dir = Float.parseFloat(sa[DIR]);
			current = new Current(Math.round(dir), speed);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		return current;
	}

	public static float parseBAT(String data) {
		final int VOLTAGE = 1;
		/*
		 * NOT STANDARD !!!
		 * Structure is $XXBAT,14.82,V,1011,98*20
		 *                     |     | |    |
		 *                     |     | |    Volume [0..100]
		 *                     |     | ADC [0..1023]
		 *                     |     Volts
		 *                     Voltage
		 */
		float v = -1f;
		String[] sa = data.substring(0, data.indexOf("*")).split(",");
		try {
			v = Float.parseFloat(sa[VOLTAGE]);
		} catch (NumberFormatException nfe) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				nfe.printStackTrace();
			}
		}
		return v;
	}

	public static long parseSTD(String data) {
		final int VALUE = 1;
		/*
		 * NOT STANDARD !!!
		 * Structure is $XXSTD,77672*5C
		 *                     |
		 *                     Cache Age in ms
		 */
		long age = 0L;
		String[] sa = data.substring(0, data.indexOf("*")).split(",");
		try {
			age = Long.parseLong(sa[VALUE]);
		} catch (NumberFormatException nfe) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				nfe.printStackTrace();
			}
		}
		return age;
	}

	private static List<String> gsvData = new ArrayList<>();
	public static List<String> getGSVList() {
		return gsvData;
	}
	// GSV Detailed Satellite data
	public static Map<Integer, SVData> parseGSV(String data) {
		final int NB_MESS = 1;
		final int MESS_NUM = 2;

		String s = data.trim();
		if (s.length() < 6) {
			return gsvMap;
		}
//  	System.out.println("String [" + s + "]");
		/* Structure is $GPGSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00*74
		 *                     | | |  |  |  |   |  |            |            |
		 *                     | | |  |  |  |   |  |            |            10 - Fourth SV...
		 *                     | | |  |  |  |   |  |            9 - Third SV...
		 *                     | | |  |  |  |   |  8 - Second SV...
		 *                     | | |  |  |  |   7 - SNR (0-99 db)
		 *                     | | |  |  |  6 - Azimuth in degrees (0-359)
		 *                     | | |  |  5 - Elevation in degrees (0-90)
		 *                     | | |  4 - First SV PRN Number
		 *                     | | 3 - Total number of SVs in view, and other meanings.
		 *                     | 2 - Message Number
		 *                     1 - Number of messages in this cycle
		 */
		final int DATA_OFFSET = 3; // num of mess, mess num, Total num of SVs.
		final int NB_DATA = 4; // SV num, elev, Z, SNR

		int nbMess = -1;
		int messNum = -1;

		String[] sa = data.substring(0, data.indexOf("*")).split(",");
		try {
			nbMess = Integer.parseInt(sa[NB_MESS]);
			messNum = Integer.parseInt(sa[MESS_NUM]);
			int nbSVinView = Integer.parseInt(sa[DATA_OFFSET]);
			if (messNum == 1) { // Reset
				gsvMap = new HashMap<>(nbSVinView);
				gsvData = new ArrayList<>();
			}
			gsvData.add(s);

			for (int indexInSentence = 1; indexInSentence <= 4; indexInSentence++) {
				int rnkInView = ((messNum - 1) * NB_DATA) + (indexInSentence);
				if (rnkInView <= nbSVinView) {
					int svNum = 0;
					int elev = 0;
					int z = 0;
					int snr = 0;
					try {
						svNum = Integer.parseInt(sa[DATA_OFFSET + ((indexInSentence - 1) * NB_DATA) + 1]);
					} catch (Exception pex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							pex.printStackTrace();
						}
					}
					try {
						elev = Integer.parseInt(sa[DATA_OFFSET + ((indexInSentence - 1) * NB_DATA) + 2]);
					} catch (Exception pex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							pex.printStackTrace();
						}
					}
					try {
						z = Integer.parseInt(sa[DATA_OFFSET + ((indexInSentence - 1) * NB_DATA) + 3]);
					} catch (Exception pex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							pex.printStackTrace();
						}
					}
					try {
						snr = Integer.parseInt(sa[DATA_OFFSET + ((indexInSentence - 1) * NB_DATA) + 4]);
					} catch (Exception pex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							pex.printStackTrace();
						}
					}
					SVData svd = new SVData(svNum, elev, z, snr);
					if (gsvMap != null) {
						gsvMap.put(svNum, svd);
					}
//        			System.out.println("SV #" + rnkInView + ", SV:" + svNum + " H:"+ elev + ", Z:" + z + ", snr:" + snr);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (messNum != -1 && nbMess != -1 && messNum == nbMess) {
			return gsvMap;
		}
		return null;
	}

	public static String GSVtoString(Map<Integer, SVData> hm) {
		String str = "";
		if (hm != null) {
			str += (hm.size() + " Satellites in view:");
			for (Integer sn : hm.keySet()) {
				SVData svd = hm.get(sn);
				str += ("Satellite #" + svd.getSvID() + " Elev:" + svd.getElevation() + ", Z:" + svd.getAzimuth() + ", SNR:" + svd.getSnr() + "db. ");
			}
		}
		return str.trim();
	}

	public static final int GGA_UTC_IDX = 0;
	public static final int GGA_POS_IDX = 1;
	public static final int GGA_NBSAT_IDX = 2;
	public static final int GGA_ALT_IDX = 3;

	// GGA Global Positioning System Fix Data. Time, Position and fix related data for a GPS receiver
	public static List<Object> parseGGA(String data) {
		final int KEY_POS = 0;
		final int UTC_POS = 1;
		final int LAT_POS = 2;
		final int LAT_SGN_POS = 3;
		final int LONG_POS = 4;
		final int LONG_SGN_POS = 5;
		final int GPS_Q_POS = 6;
		final int NBSAT_POS = 7;
		final int ANTENNA_ALT = 9;

		ArrayList<Object> al = null;
		String s = data.trim();
		if (s.length() < 6) {
			return al;
		}
		/* Structure is
		 *  $GPGGA,014457,3739.853,N,12222.821,W,1,03,5.4,1.1,M,-28.2,M,,*7E
		 *  $aaGGA,hhmmss.ss,llll.ll,a,gggg.gg,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh(CR)(LF)
		 *         |         |         |         | |  |   |   | |   | |   |
		 *         |         |         |         | |  |   |   | |   | |   Differential reference station ID
		 *         |         |         |         | |  |   |   | |   | Age of differential GPS data (seconds)
		 *         |         |         |         | |  |   |   | |   Unit of geodial separation, meters
		 *         |         |         |         | |  |   |   | Geodial separation
		 *         |         |         |         | |  |   |   Unit of antenna altitude, meters
		 *         |         |         |         | |  |   Antenna altitude above sea level
		 *         |         |         |         | |  Horizontal dilution of precision
		 *         |         |         |         | number of satellites in use 00-12 (in use, not in view!)
		 *         |         |         |         GPS quality indicator (0:invalid, 1:GPS fix, 2:DGPS fix)
		 *         |         |         Longitude
		 *         |         Latitude
		 *         UTC of position
		 */
		String[] sa = s.substring(0, s.indexOf("*")).split(",");
		double utc = 0L, lat = 0L, lng = 0L;
		int nbsat = 0;
		try {
			utc = parseNMEADouble(sa[UTC_POS]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}

		try {
			double l = parseNMEADouble(sa[LAT_POS]);
			int intL = (int) l / 100;
			double m = ((l / 100.0) - intL) * 100.0;
			m *= (100.0 / 60.0);
			lat = intL + (m / 100.0);
			if ("S".equals(sa[LAT_SGN_POS])) {
				lat = -lat;
			}
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		try {
			double g = parseNMEADouble(sa[LONG_POS]);
			int intG = (int) g / 100;
			double m = ((g / 100.0) - intG) * 100.0;
			m *= (100.0 / 60.0);
			lng = intG + (m / 100.0);
			if ("W".equals(sa[LONG_SGN_POS])) {
				lng = -lng;
			}
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		try {
			nbsat = Integer.parseInt(sa[NBSAT_POS]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}

//  System.out.println("UTC:" + utc + ", lat:" + lat + ", lng:" + lng + ", nbsat:" + nbsat);
		int h = (int) (utc / 10_000);
		int m = (int) ((utc - (h * 10_000)) / 100);
		float sec = (float) (utc - ((h * 10_000) + (m * 100)));
//  System.out.println(h + ":" + m + ":" + sec);

//  System.out.println(new GeoPos(lat, lng).toString());
//  System.out.println("Done.");

		double alt = 0;
		try {
			alt = parseNMEADouble(sa[ANTENNA_ALT]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}

		al = new ArrayList<Object>(4);
		al.add(new UTC(h, m, sec));
		al.add(new GeoPos(lat, lng));
		al.add(nbsat);
		al.add(alt);

		return al;
	}

	// GSA GPS DOP and active satellites
	public static GSA parseGSA(String data) {
		final int MODE_1 = 1;
		final int MODE_2 = 2;
		final int PDOP = 15;
		final int HDOP = 16;
		final int VDOP = 17;
		/*
		 * $GPGSA,A,3,19,28,14,18,27,22,31,39,,,,,1.7,1.0,1.3*35
		 *        | | |                           |   |   |
		 *        | | |                           |   |   VDOP
		 *        | | |                           |   HDOP
		 *        | | |                           PDOP (dilution of precision). No unit; the smaller the better.
		 *        | | IDs of the SVs used in fix (up to 12)
		 *        | Mode: 1=Fix not available, 2=2D, 3=3D
		 *        Mode: M=Manual, forced to operate in 2D or 3D
		 *              A=Automatic, 3D/2D
		 */
		GSA gsa = new GSA();
		String[] elements = data.substring(0, data.indexOf("*")).split(",");
		if (elements.length >= 2) {
			if ("M".equals(elements[MODE_1])) {
				gsa.setMode1(GSA.ModeOne.Manual);
			}
			if ("A".equals(elements[MODE_1])) {
				gsa.setMode1(GSA.ModeOne.Auto);
			}
		}
		if (elements.length >= 3) {
			if ("1".equals(elements[MODE_2])) {
				gsa.setMode2(GSA.ModeTwo.NoFix);
			}
			if ("2".equals(elements[MODE_2])) {
				gsa.setMode2(GSA.ModeTwo.TwoD);
			}
			if ("3".equals(elements[MODE_2])) {
				gsa.setMode2(GSA.ModeTwo.ThreeD);
			}
		}
		for (int i = 3; i < 15; i++) {
			if (!elements[i].trim().isEmpty()) {
				int sv = Integer.parseInt(elements[i]);
				gsa.getSvArray().add(sv);
			}
		}
		if (elements.length >= 16) {
			gsa.setPDOP(Float.parseFloat(elements[PDOP]));
		}
		if (elements.length >= 17) {
			gsa.setHDOP(Float.parseFloat(elements[HDOP]));
		}
		if (elements.length >= 18) {
			gsa.setVDOP(Float.parseFloat(elements[VDOP]));
		}
		return gsa;
	}

	// VHW Water speed and heading
	public static VHW parseVHW(String data) {
		return parseVHW(data, 0d);
	}

	public static VHW parseVHW(String data, double defaultBSP) {
		final int HDG_IN_DEG_TRUE = 1;
		final int HDG_IN_DEG_MAG = 3;
		final int SPEED_IN_KN = 5;

		String s = data.trim();
		if (s.length() < 6) {
			return null;
		}
		/* Structure is
		 *         1   2 3   4 5   6 7   8
		 *  $aaVHW,x.x,T,x.x,M,x.x,N,x.x,K*hh(CR)(LF)
		 *         |     |     |     |
		 *         |     |     |     Speed in km/h
		 *         |     |     Speed in knots
		 *         |     Heading in degrees, Magnetic
		 *         Heading in degrees, True
		 */
		// We're interested only in Speed in knots.
		double speed = defaultBSP;
		double hdm = -1d; // set to -1.Means not found.
		double hdg = -1d;

		try {
			String[] nmeaElements = data.substring(0, data.indexOf("*")).split(",");
			try {
				speed = parseNMEADouble(nmeaElements[SPEED_IN_KN]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			try {
				hdm = parseNMEADouble(nmeaElements[HDG_IN_DEG_MAG]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			try {
				hdg = parseNMEADouble(nmeaElements[HDG_IN_DEG_TRUE]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		return new VHW().bsp(speed).hdm(hdm).hdg(hdg);
	}

	// VLW Distance Traveled through Water
	public static VLW parseVLW(String data) {
		final int CUM_DIST = 1;
		final int SINCE_RESET = 3;

		String s = data.trim();
		if (s.length() < 6) {
			return (VLW) null;
		}

		double cumulative = 0d;
		double sinceReset = 0d;
		/* Structure is
		 * $aaVLW,x.x,N,x.x,N*hh<CR><LF>
		 *        |   | |   |
		 *        |   | |   Nautical miles
		 *        |   | Distance since reset
		 *        |   Nautical miles
		 *        Total cumulative distance
		 */
		try {
			String[] nmeaElements = data.substring(0, data.indexOf("*")).split(",");
			cumulative = parseNMEADouble(nmeaElements[CUM_DIST]);
			sinceReset = parseNMEADouble(nmeaElements[SINCE_RESET]);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return new VLW().log(cumulative).daily(sinceReset);
	}

	// MTW Water Temperature
	public static double parseMTW(String data) {
		final int TEMP_CELCIUS = 1;
		/* Structure
		 * $xxMTW,+18.0,C*hh
		 *
		 */
		String s = data.trim();
		if (s.length() < 6) {
			return 0d;
		}

		double temp = 0d;
		try {
			String[] nmeaElements = data.substring(0, data.indexOf("*")).split(",");
			String _s = nmeaElements[TEMP_CELCIUS];
			if (_s.startsWith("+")) {
				_s = _s.substring(1);
			}
			temp = parseNMEADouble(_s);
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0d;
		}
		return temp;
	}

	public static final int TRUE_WIND = 0;
	public static final int APPARENT_WIND = 1;

	// MWV Wind Speed and Angle
	// AWA, AWS (R), possibly TWA, TWS (T)
	public static Wind parseMWV(String data) {
		int flavor = -1;

		String s = data.trim();
		if (s.length() < 6) {
			return null;
		}
		/* Structure is
		 *  $aaMWV,x.x,a,x.x,a,A*hh
		 *         |   | |   | |
		 *         |   | |   | status : A=data valid
		 *         |   | |   Wind Speed unit (K/M/N)
		 *         |   | Wind Speed
		 *         |   reference R=relative, T=true
		 *         Wind angle 0 to 360 degrees
		 */
		// We're interested only in Speed in knots.
		Wind aw = null;
		try {
			if (!s.contains("A*")) { // Data invalid
				return aw;
			} else {
				String speed = "", angle = "";
				if (s.contains("MWV,") && s.contains(",R,")) { // Apparent
					flavor = APPARENT_WIND;
					angle = s.substring(s.indexOf("MWV,") + "MWV,".length(), s.indexOf(",R,"));
				}
				if (s.contains(",R,") && s.contains(",N,")) {
					speed = s.substring(s.indexOf(",R,") + ",R,".length(), s.indexOf(",N,"));
				}
				if (speed.trim().isEmpty() && angle.trim().isEmpty()) {
					if (s.contains("MWV,") && s.contains(",T,")) {
						flavor = TRUE_WIND;
						angle = s.substring(s.indexOf("MWV,") + "MWV,".length(), s.indexOf(",T,"));    // True
					}
					if (s.contains(",T,") && s.contains(",N,")) {
						speed = s.substring(s.indexOf(",T,") + ",T,".length(), s.indexOf(",N,"));
					}
				}
				float awa = 0f;
				double aws = 0d;
				try {
					awa = parseNMEAFloat(angle);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				try {
					aws = parseNMEADouble(speed);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				if (flavor == APPARENT_WIND) {
					aw = new ApparentWind(Math.round(awa), aws);
				} else if (flavor == TRUE_WIND) {
					aw = new TrueWind(Math.round(awa), aws);
				} else {
					System.out.println("UNKNOWN wind type!");
				}
			}
		} catch (Exception e) {
			System.err.println("parseMWV for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return aw;
	}

	// MWD Wind Direction & Speed
	public static TrueWind parseMWD(String data) {
			/* $WIMWD,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>*hh
	     *
	     * NMEA 0183 standard Wind Direction and Speed, with respect to north.
	     *
	     * <1> Wind direction, 0.0 to 359.9 degrees True, to the nearest 0.1 degree
	     * <2> T = True
	     * <3> Wind direction, 0.0 to 359.9 degrees Magnetic, to the nearest 0.1 degree
	     * <4> M = Magnetic
	     * <5> Wind speed, knots, to the nearest 0.1 knot.
	     * <6> N = Knots
	     * <7> Wind speed, meters/second, to the nearest 0.1 m/s.
	     * <8> M = Meters/second
	     */
		TrueWind tw = null;
		if (validCheckSum(data)) {
			String[] part = data.split(",");
			double dir = 0;
			double speed = 0;
			if ("T".equals(part[2])) {
				dir = Double.parseDouble(part[1]);
			}
			if ("N".equals(part[6])) {
				speed = Double.parseDouble(part[5]);
			}
			tw = new TrueWind((int)Math.round(dir), speed);
		}
		return tw;
	}

	/*
	 * VWT True Windspeed and Angle (obsolete)
	 * $--VWT,x.x,a,x.x,N,x.x,M,x.x,K*hh<CR><LF>
	 *        |     |     |     |
	 *        |     |     |     Wind speed, Km/Hr
	 *        |     |     Wind speed, meters/second
	 *        |     Calculated wind Speed, knots
	 *        Calculated wind angle relative to the vessel, 0 to 180, left/right L/R of vessel heading
	 */
	public static TrueWind parseVWT(String data) {
		TrueWind wind = null;
		String s = data.trim();
		if (s.length() < 6) {
			return null;
		}
		try {
			// TODO Implement
		} catch (Exception e) {
			System.err.println("parseVWT for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return wind;
	}

	// VWR Relative Wind Speed and Angle
	// AWA, AWS
	// Example: VWR,148.,L,02.4,N,01.2,M,04.4,K*XX
	public static ApparentWind parseVWR(String data) {
		String s = data.trim();
		if (s.length() < 6) {
			return null;
		}
		/* Structure is
		 *  $aaVWR,x.x,a,x.x,N,x.x,M,x.x,K*hh
		 *         |   | |     |     |
		 *         |   | |     |     Wind Speed, in km/h
		 *         |   | |     Wind Speed, in m/s
		 *         |   | Wind Speed, in knots
		 *         |   L=port, R=starboard
		 *         Wind angle 0 to 180 degrees
		 */
		// We're interested only in Speed in knots.
		ApparentWind aw = null;
		try {
			if (false && !s.contains("K*")) { // Data invalid // Watafok???
				return aw;
			} else {
				String speed = "", angle = "", side = "";
				int firstCommaIndex = s.indexOf(",");
				int secondCommaIndex = s.indexOf(",", firstCommaIndex + 1);
				int thirdCommaIndex = s.indexOf(",", secondCommaIndex + 1);
				int fourthCommaIndex = s.indexOf(",", thirdCommaIndex + 1);
				if (firstCommaIndex > -1 && secondCommaIndex > -1) {
					angle = s.substring(firstCommaIndex + 1, secondCommaIndex);
				}
				while (angle.endsWith(".")) {
					angle = angle.substring(0, angle.length() - 1);
				}
				if (secondCommaIndex > -1 && thirdCommaIndex > -1) {
					side = s.substring(secondCommaIndex + 1, thirdCommaIndex);
				}
				if (thirdCommaIndex > -1 && fourthCommaIndex > -1) {
					speed = s.substring(thirdCommaIndex + 1, fourthCommaIndex);
				}
				double ws = 0d;
				try {
					ws = parseNMEADouble(speed);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				int wa = 0;
				try {
					wa = Integer.parseInt(angle);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				if (side.equals("L")) {
					wa = 360 - wa;
				}
				aw = new ApparentWind(wa, ws);
			}
		} catch (Exception e) {
			System.err.println("parseMWV for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return aw;
	}

	// VTG Track made good and Ground speed
	public static OverGround parseVTG(String data) {
		String s = data.trim();
		OverGround og = null;
		if (s.length() < 6) {
			return null;
		}
		/* Structure is
		 * $IIVTG,x.x,T,x.x,M,x.x,N,x.x,K,A*hh
				  |   | |  |  |   | |___|SOG, km/h
				  |   | |  |  |___|SOG, knots
				  |   | |__|COG, mag
				  |___|COG, true

		   $IIVTG,17.,T,M,7.9,N,,*36 // B&G does this...
		   $IIVTG,,T,338.,M,N,,*28   // or this...
		   $IIVTG,054.7,T,034.4,M,005.5,N,010.2,K,A*XX
				  054.7,T      True track made good
				  034.4,M      Magnetic track made good
				  005.5,N      Ground speed, knots
				  010.2,K      Ground speed, Kilometers per hour
		 */
		// We're interested only in Speed in knots.
		try {
			if (false && !s.contains("A*")) { // Data invalid, only for NMEA 2.3 and later
				return og;
			} else {
				String speed = "", angle = "";
				String[] sa = s.split(",");

				int tIndex = -1;
				for (int i = 0; i < sa.length; i++) {
					if ("T".equals(sa[i])) {
						tIndex = i;
						break;
					}
				}
				int nIndex = -1;
				for (int i = 0; i < sa.length; i++) {
					if ("N".equals(sa[i])) {
						nIndex = i;
						break;
					}
				}
				angle = sa[tIndex - 1];
				speed = sa[nIndex - 1];
				if (speed.endsWith(".")) {
					speed += "0";
				}
				double sog = parseNMEADouble(speed);
				if (angle.endsWith(".")) {
					angle += "0";
				}
				int cog = (int) Math.round(parseNMEADouble(angle));
				og = new OverGround(sog, cog);
			}
		} catch (Exception e) {
			if ("true".equals(System.getProperty("nmea.parser.verbose", "false"))) {
				System.err.println("parseVTG for " + s + ", " + e.toString());
			}
//    e.printStackTrace();
		}
		return og;
	}

	// GLL Geographical Latitude & Longitude
	public static GLL parseGLL(String data) {
		String s = data.trim();
		if (s.length() < 6) {
			return null;
		}
		if (!validCheckSum(s)) {
			return null;
		}
		/* Structure is
		 *  $aaGLL,llll.ll,a,gggg.gg,a,hhmmss.ss,A,D*hh
		 *         |       | |       | |         | |
		 *         |       | |       | |         | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator (not always there)
		 *         |       | |       | |         A:data valid (Active), V: void
		 *         |       | |       | UTC of position
		 *         |       | |       Long sign :E/W
		 *         |       | Longitude
		 *         |       Lat sign :N/S
		 *         Latitude
		 */
		GeoPos ll = null;
		Date date = null;
		try {
			if (!s.contains("A*")) { // Not Active, Data invalid (void)
				return null;
			} else {
				int i = s.indexOf(",");
				if (i > -1) {
					String lat = "";
					int j = s.indexOf(",", i + 1);
					lat = s.substring(i + 1, j);
					double l = parseNMEADouble(lat);
					int intL = (int) l / 100;
					double m = ((l / 100.0) - intL) * 100.0;
					m *= (100.0 / 60.0);
					l = intL + (m / 100.0);
					String latSgn = s.substring(j + 1, j + 2);
					if (latSgn.equals("S")) {
						l *= -1.0;
					}
					int k = s.indexOf(",", j + 3);
					String lng = s.substring(j + 3, k);
					double g = parseNMEADouble(lng);
					int intG = (int) g / 100;
					m = ((g / 100.0) - intG) * 100.0;
					m *= (100.0 / 60.0);
					g = intG + (m / 100.0);
					String lngSgn = s.substring(k + 1, k + 2);
					if (lngSgn.equals("W")) {
						g *= -1.0;
					}
					ll = new GeoPos(l, g);
					k = s.indexOf(",", k + 2);
					String dateStr = s.substring(k + 1);
					if (dateStr.indexOf(",") > 0) {
						dateStr = dateStr.substring(0, dateStr.indexOf(","));
					}
					double utc = 0D;
					try {
						utc = parseNMEADouble(dateStr);
					} catch (Exception ex) { /*System.out.println("dateStr in StringParsers.parseGLL"); */ }
					int h = (int) (utc / 10_000);
					int mn = (int) ((utc - (10_000 * h)) / 100);
					float sec = (float) (utc % 100f);
					Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // new GregorianCalendar();
//					local.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
					local.set(Calendar.YEAR, 1_970);
					local.set(Calendar.MONTH, Calendar.JANUARY);
					local.set(Calendar.DAY_OF_MONTH, 1);
					local.set(Calendar.HOUR_OF_DAY, h);
					local.set(Calendar.MINUTE, mn);
					local.set(Calendar.SECOND, Math.round(sec));
					local.set(Calendar.MILLISECOND, 0);
					try {
						date = local.getTime();
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("parseGLL for [" + s + "] " + e.toString());
		}
		return new GLL().gllPos(ll).gllDate(date);
	}

	// HDT Heading - True
	public static int parseHDT(String data) {
		final int KEY_POS = 0;
		final int HDG_POS = 1;
		final int MT_POS = 2;
		String s = data.trim();
		if (s.length() < 6) {
			return -1;
		}
    /* Structure is
     *  $aaHDT,xxx,M*hh(CR)(LF)
     *         |   |
     *         |   Magnetic, True
     *         Heading in degrees
     */
		int hdg = 0;

		String[] elmts = data.substring(0, data.indexOf("*")).split(",");
		try {
			if (elmts[KEY_POS].contains("HDT")) {
				if ("T".equals(elmts[MT_POS])) {
					hdg = Math.round(parseNMEAFloat(elmts[HDG_POS]));
				} else {
					throw new RuntimeException("Wrong type [" + elmts[HDG_POS] + "] in parseHDT.");
				}
			} else {
				System.err.println("Wrong chain in parseHDT [" + data + "]");
			}
		} catch (Exception e) {
			System.err.println("parseHDT for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return hdg;
	}

	// HDM Heading (Mag.)
	public static int parseHDM(String data) {
		final int KEY_POS = 0;
		final int HDG_POS = 1;
		final int MT_POS = 2;
		String s = data.trim();
		if (s.length() < 6) {
			return -1;
		}
		/* Structure is
		 *  $aaHDM,xxx,M*hh(CR)(LF)
		 *         |   |
		 *         |   Magnetic, True
		 *         Heading in degrees
		 */
		int hdg = 0;

		String[] elmts = data.substring(0, data.indexOf("*")).split(",");
		try {
			if (elmts[KEY_POS].contains("HDM")) {
				if ("M".equals(elmts[MT_POS])) {
					hdg = Math.round(parseNMEAFloat(elmts[HDG_POS]));
				} else {
					throw new RuntimeException("Wrong type [" + elmts[HDG_POS] + "] in parseHDM.");
				}
			} else {
				System.err.println("Wrong chain in parseHDM [" + data + "]");
			}
		} catch (Exception e) {
			System.err.println("parseHDM for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return hdg;
	}

	public static String parseHDMtoString(String s) {
		String ret = "";
		try {
			ret = Integer.toString(parseHDM(s));
		} catch (Exception ignore) {
		}
		return ret;
	}

	// HDG - Magnetic heading, deviation, variation
	public static HDG parseHDG(String data) {
		HDG ret = null;
		String s = data.trim();
		if (s.length() < 6) {
			return ret;
		}
		double hdg = 0d;
		double dev = 0d; // -Double.MAX_VALUE;
		double var = 0d; // -Double.MAX_VALUE;
		/* Structure is
		 * $xxHDG,x.x,x.x,a,x.x,a*hh<CR><LF>
		 *        |   |   | |   | |
		 *        |   |   | |   | Checksum
		 *        |   |   | |   Magnetic Variation direction, E = Easterly, W = Westerly
		 *        |   |   | Magnetic Variation degrees
		 *        |   |   Magnetic Deviation direction, E = Easterly, W = Westerly
		 *        |   Magnetic Deviation, degrees
		 *        Magnetic Sensor heading in degrees
		 */
		try {
			String[] nmeaElements = data.substring(0, data.indexOf("*")).split(",");
			try {
				hdg = parseNMEADouble(nmeaElements[1]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			try {
				dev = parseNMEADouble(nmeaElements[2]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			if (nmeaElements.length > 3 && nmeaElements[3] != null && "W".equals(nmeaElements[3]))
				dev = -dev;
			try {
				var = parseNMEADouble(nmeaElements[4]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			if (nmeaElements.length > 5 && nmeaElements[5] != null && "W".equals(nmeaElements[5])) {
				var = -var;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		ret = new HDG().heading(hdg).deviation(dev).variation(var);

		return ret;
	}

	// RMB Recommended Minimum Navigation Information
	public static RMB parseRMB(String str) {
		final int RMB_STATUS = 1;
		final int RMB_XTE = 2;
		final int RMB_STEER = 3;
		final int RMB_ORIGIN_WP = 4;
		final int RMB_DEST_WP = 5;
		final int RMB_DEST_WP_LAT = 6;
		final int RMB_DEST_WP_LAT_SIGN = 7;
		final int RMB_DEST_WP_LNG = 8;
		final int RMB_DEST_WP_LNG_SIGN = 9;
		final int RMB_RANGE_TO_DEST = 10;
		final int RMB_BEARING_TO_DEST = 11;
		final int RMB_DEST_CLOSING = 12;
		final int RMB_INFO = 13;

		/*        1 2   3 4    5    6       7 8        9 10  11  12  13
		 * $GPRMB,A,x.x,a,c--c,d--d,llll.ll,e,yyyyy.yy,f,g.g,h.h,i.i,j*kk
		 *        | |   | |    |    |       | |        | |   |   |   |
		 *        | |   | |    |    |       | |        | |   |   |   A=Entered or perpendicular passed, V:not there yet
		 *        | |   | |    |    |       | |        | |   |   Destination closing velocity in knots
		 *        | |   | |    |    |       | |        | |   Bearing to destination, degrees, True
		 *        | |   | |    |    |       | |        | Range to destination, nm
		 *        | |   | |    |    |       | |        E or W
		 *        | |   | |    |    |       | Destination Waypoint longitude
		 *        | |   | |    |    |       N or S
		 *        | |   | |    |    Destination Waypoint latitude
		 *        | |   | |    Destination Waypoint ID
		 *        | |   | Origin Waypoint ID
		 *        | |   Direction to steer (L or R) to correct error
		 *        | Crosstrack error in nm
		 *        Data Status (Active or Void)
		 */
		RMB rmb = null;
		String s = str.trim();
		if (s.length() < 6) {
			return null;
		}
		try {
			if (s.contains("RMB,")) {
				rmb = new RMB();
				String[] data = str.substring(0, str.indexOf("*")).split(",");
				if (data[RMB_STATUS].equals("V")) { // Void
					return null;
				}
				double xte = 0d;
				try {
					xte = parseNMEADouble(data[RMB_XTE]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				rmb.setXte(xte);
				rmb.setDts(data[RMB_STEER]);
				rmb.setOwpid(data[RMB_ORIGIN_WP]);
				rmb.setDwpid(data[RMB_DEST_WP]);

				double _lat = 0d;
				try {
					_lat = parseNMEADouble(data[RMB_DEST_WP_LAT]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				double lat = (int) (_lat / 100d) + ((_lat % 100d) / 60d);
				if ("S".equals(data[RMB_DEST_WP_LAT_SIGN])) {
					lat = -lat;
				}
				double _lng = 0d;
				try {
					_lng = parseNMEADouble(data[RMB_DEST_WP_LNG]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				double lng = (int) (_lng / 100d) + ((_lng % 100d) / 60d);
				if ("W".equals(data[RMB_DEST_WP_LNG_SIGN])) {
					lng = -lng;
				}
				rmb.setDest(new GeoPos(lat, lng));
				double rtd = 0d;
				try {
					rtd = parseNMEADouble(data[RMB_RANGE_TO_DEST]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				rmb.setRtd(rtd);
				double btd = 0d;
				try {
					btd = parseNMEADouble(data[RMB_BEARING_TO_DEST]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				rmb.setBtd(btd);
				double dcv = 0d;
				try {
					dcv = parseNMEADouble(data[RMB_DEST_CLOSING]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				rmb.setDcv(dcv);
				rmb.setAs(data[RMB_INFO]);
			}
		} catch (Exception e) {
			System.err.println("parseRMB for " + s + ", " + e.toString());
		}
		return rmb;
	}

	// RMC Recommended minimum specific GPS/Transit data
	public static RMC parseRMC(String str) {
		final int RMC_UTC = 1;
		final int RMC_ACTIVE_VOID = 2;
		final int RMC_LATITUDE_VALUE = 3;
		final int RMC_LATITUDE_SIGN = 4;
		final int RMC_LONGITUDE_VALUE = 5;
		final int RMC_LONGITUDE_SIGN = 6;
		final int RMC_SOG = 7;
		final int RMC_COG = 8;
		final int RMC_DDMMYY = 9;
		final int RMC_VARIATION_VALUE = 10;
		final int RMC_VARIATION_SIGN = 11;
		final int RMC_TYPE = 12;

		RMC rmc = null;
//		String str = StringUtils.removeNullsFromString(strOne.trim()); // TODO Do it at the consumer level
		if (str.length() < 6 || !str.contains("*")) {
			return null;
		}
		if (!validCheckSum(str)) {
			return null;
		}
		String s = str.substring(0, str.indexOf("*"));
		/* RMC Structure is
		 *                                                                    12
		 *         1      2 3        4 5         6 7     8     9      10    11
		 *  $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,T*6A
		 *         |      | |        | |         | |     |     |      |     | |
		 *         |      | |        | |         | |     |     |      |     | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator
		 *         |      | |        | |         | |     |     |      |     Variation sign
		 *         |      | |        | |         | |     |     |      Variation value
		 *         |      | |        | |         | |     |     Date DDMMYY (see rmc.date.offset property)
		 *         |      | |        | |         | |     COG
		 *         |      | |        | |         | SOG
		 *         |      | |        | |         Longitude Sign
		 *         |      | |        | Longitude Value
		 *         |      | |        Latitude Sign
		 *         |      | Latitude value
		 *         |      Active or Void
		 *         UTC
		 */
		try {
			if (s.contains("RMC,")) {
				rmc = new RMC();

				String[] data = s.split(",");
				rmc = rmc.setValid(data[RMC_ACTIVE_VOID].equals("A")); // Active. Does not prevent the date and time from being available.
				if (data[RMC_UTC].length() > 0) { // Time and Date
					double utc = 0D;
					try {
						utc = parseNMEADouble(data[RMC_UTC]);
					} catch (Exception ex) {
						System.out.println("data[1] in StringParsers.parseRMC");
					}
					int h = (int) (utc / 10_000);
					int m = (int) ((utc - (10_000 * h)) / 100);
					float sec = (float) (utc % 100f);

//        			System.out.println("Data[1]:" + data[1] + ", h:" + h + ", m:" + m + ", s:" + sec);

					Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // new GregorianCalendar();
//					local.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
					local.set(Calendar.HOUR_OF_DAY, h);
					local.set(Calendar.MINUTE, m);
					local.set(Calendar.SECOND, (int) Math.round(sec));
					local.set(Calendar.MILLISECOND, 0);
					if (data[RMC_DDMMYY].length() > 0) {
						int d = 1;
						try {
							d = Integer.parseInt(data[RMC_DDMMYY].substring(0, 2));
						} catch (Exception ex) {
							if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
								ex.printStackTrace();
							}
						}
						int mo = 0;
						try {
							mo = Integer.parseInt(data[RMC_DDMMYY].substring(2, 4)) - 1;
						} catch (Exception ex) {
							if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
								ex.printStackTrace();
							}
						}
						int y = 0;
						try {
							y = Integer.parseInt(data[RMC_DDMMYY].substring(4));
						} catch (Exception ex) {
							if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
								ex.printStackTrace();
							}
						}
						if (y > 50) {
							y += 1900;
						} else {
							y += 2_000;
						}
						local.set(Calendar.DATE, d);
						local.set(Calendar.MONTH, mo);
						local.set(Calendar.YEAR, y);
						// In case the GPS date is wrong (it happens):
						String gpsOffset = System.getProperty("rmc.date.offset");
						// Offset in DAYS to add to the RMC Date.
						// One of mines has an offset of 7168 (0x1C00) days.
						if (gpsOffset != null) {
							try {
								int offset = Integer.parseInt(gpsOffset);
								if ("true".equals(System.getProperty("rmc.date.offset.verbose"))) {
									System.out.printf(">>> Adding %d days to %s\n", offset, local.getTime().toString());
								}
								local.add(Calendar.DATE, offset); // Add in Days
								if ("true".equals(System.getProperty("rmc.date.offset.verbose"))) {
									System.out.printf(">>>   that becomes %s\n", local.getTime().toString());
								}
							} catch (NumberFormatException nfe) {
								nfe.printStackTrace();
							}
						}
						Date rmcDate = local.getTime();
						rmc = rmc.setRmcDate(rmcDate);
					}
					Date rmcTime = local.getTime();
					rmc = rmc.setRmcTime(rmcTime);
					if ("true".equals(System.getProperty("RMC.verbose"))) {
						System.out.printf("RMC: From [%s], GPS date: %s, GPS Time: %s\n", str, SDF_UTC.format(rmc.getRmcDate()), SDF_UTC.format(rmcTime));
					}
				}
				if (data[RMC_LATITUDE_VALUE].length() > 0 && data[RMC_LONGITUDE_VALUE].length() > 0) {
					String deg = data[RMC_LATITUDE_VALUE].substring(0, 2);
					String min = data[RMC_LATITUDE_VALUE].substring(2);
					double l = GeomUtil.sexToDec(deg, min);
					if ("S".equals(data[RMC_LATITUDE_SIGN])) {
						l = -l;
					}
					deg = data[RMC_LONGITUDE_VALUE].substring(0, 3);
					min = data[RMC_LONGITUDE_VALUE].substring(3);
					double g = GeomUtil.sexToDec(deg, min);
					if ("W".equals(data[RMC_LONGITUDE_SIGN])) {
						g = -g;
					}
					rmc = rmc.setGp(new GeoPos(l, g));
				}
				if (data[RMC_SOG].length() > 0) {
					double speed = 0;
					try {
						speed = parseNMEADouble(data[RMC_SOG]);
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					rmc.setSog(speed);
				}
				if (data[RMC_COG].length() > 0) {
					double cog = 0;
					try {
						cog = parseNMEADouble(data[RMC_COG]);
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					rmc.setCog(cog);
				}
				if (data[RMC_VARIATION_VALUE].length() > 0 && data[RMC_VARIATION_SIGN].length() > 0) {
					double d = -Double.MAX_VALUE;
					try {
						d = parseNMEADouble(data[RMC_VARIATION_VALUE]);
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					if ("W".equals(data[RMC_VARIATION_SIGN]))
						d = -d;
					rmc = rmc.setDeclination(d);
				}
				if (data.length > 12) { // Can be missing
					switch (data[RMC_TYPE]) {
						case "A":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.AUTONOMOUS);
							break;
						case "D":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.DIFFERENTIAL);
							break;
						case "E":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.ESTIMATED);
							break;
						case "N":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.NOT_VALID);
							break;
						case "S":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.SIMULATOR);
							break;
						default:
							rmc = rmc.setRmcType(null);
							break;
					}
				}
			}
		} catch (Exception e) {
			System.err.println("In parseRMC for " + str.trim() + ", " + e.toString());
			e.printStackTrace();
		}
		return rmc;
	}

	public static String parseRMCtoString(String data) {
		String ret = "";
		try {
			ret = parseRMC(data).toString();
		} catch (Exception ignore) {
		}
		return ret;
	}

	public static String getLatFromRMC(String s) {
		String result = "";
		try {
			RMC rmc = parseRMC(s);
			result = Double.toString(rmc.getGp().lat);
		} catch (Exception ex) {
			result = "-";
		}
		return result;
	}

	public static String getLongFromRMC(String s) {
		String result = "";
		try {
			RMC rmc = parseRMC(s);
			result = Double.toString(rmc.getGp().lng);
		} catch (Exception ex) {
			result = "-";
		}
		return result;
	}

	public static String getCOGFromRMC(String s) {
		String result = "";
		try {
			RMC rmc = parseRMC(s);
			result = Double.toString(rmc.getCog());
		} catch (Exception ex) {
			result = "-";
		}
		return result;
	}

	public static String getSOGFromRMC(String s) {
		String result = "";
		try {
			RMC rmc = parseRMC(s);
			result = Double.toString(rmc.getSog());
		} catch (Exception ex) {
			result = "-";
		}
		return result;
	}

	public final static int MESS_NUM = 0;
	public final static int NB_MESS = 1;

	/**
	 * For GSV, returns the message number, and the total number of messages to expect.
	 *
	 * @param gsvString the string to parse
	 * @return teh expected int array
	 */
	public static int[] getMessNum(String gsvString) {
		int mn = -1;
		int nbm = -1;
		if (validCheckSum(gsvString)) {
			String[] elmt = gsvString.trim().split(",");
			try {
				nbm = Integer.parseInt(elmt[1]);
				mn = Integer.parseInt(elmt[2]);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return new int[]{mn, nbm};
	}

	// ZDA Time & Date - UTC, day, month, year and local time zone
	public static UTCDate parseZDA(String str) {
		final int ZDA_UTC = 1;
		final int ZDA_DAY = 2;
		final int ZDA_MONTH = 3;
		final int ZDA_YEAR = 4;
		final int ZDA_LOCAL_ZONE_HOURS = 5;
		final int ZDA_LOCAL_ZONE_MINUTES = 6;

		/* Structure is
		 * $GPZDA,hhmmss.ss,dd,mm,yyyy,xx,yy*CC
		 *        1         2  3  4
		 * $GPZDA,201530.00,04,07,2002,00,00*60
		 *        |         |  |  |    |  |
		 *        |         |  |  |    |  local zone minutes 0..59
		 *        |         |  |  |    local zone hours -13..13
		 *        |         |  |  year
		 *        |         |  month
		 *        |         day
		 *        HrMinSec(UTC)
		 */
		String[] data = str.substring(0, str.indexOf("*")).split(",");

		Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // new GregorianCalendar();
//		local.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		local.set(Calendar.HOUR_OF_DAY, Integer.parseInt(data[ZDA_UTC].substring(0, 2)));
		local.set(Calendar.MINUTE, Integer.parseInt(data[ZDA_UTC].substring(2, 4)));
		local.set(Calendar.SECOND, (int) Math.round(Float.parseFloat(data[ZDA_UTC].substring(4))));
		local.set(Calendar.MILLISECOND, 0); // TODO Something nicer
		int d = 1;
		try {
			d = Integer.parseInt(data[ZDA_DAY]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		int mo = 0;
		try {
			mo = Integer.parseInt(data[ZDA_MONTH]) - 1;
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		int y = 0;
		try {
			y = Integer.parseInt(data[ZDA_YEAR]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		local.set(Calendar.DATE, d);
		local.set(Calendar.MONTH, mo);
		local.set(Calendar.YEAR, y);

		Date utc = local.getTime();
		return new UTCDate(utc);
	}

	public static final short DEPTH_IN_FEET = 0;
	public static final short DEPTH_IN_METERS = 1;
	public static final short DEPTH_IN_FATHOMS = 2;

	public static String parseDBTinMetersToString(String data) {
		String s = data.trim();
		String sr = "";
		try {
			float f = parseDBT(s, DEPTH_IN_METERS);
			sr = Float.toString(f);
		} catch (Exception ex) {
			sr = "-";
		}
		return sr;
	}

	private final static double METERS_TO_FEET = 3.28083;

	// DBT Depth of Water
	public static float parseDPT(String data) {
		return parseDPT(data, DEPTH_IN_METERS);
	}
	// Depth
	public static float parseDPT(String data, short unit) {
		final int IN_METERS = 1;
		final int OFFSET = 2;
		String s = data.trim();
		if (s.length() < 6) {
			return -1F;
		}
		/* Structure is
		 *  $xxDPT,XX.XX,XX.XX,XX.XX*hh<0D><0A>
		 *         |     |     |
		 *         |     |     Max depth in meters
		 *         |     offset
		 *         Depth in meters
		 */
		float feet = 0.0F;
		float meters = 0.0F;
		float fathoms = 0.0F;
		String[] array = data.substring(0, data.indexOf("*")).split(",");
		try {
			meters = parseNMEAFloat(array[IN_METERS]);
			try {
				String strOffset = array[OFFSET].trim();
				if (strOffset.startsWith("+")) {
					strOffset = strOffset.substring(1);
				}
				float offset = parseNMEAFloat(strOffset);
				meters += offset;
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			feet = meters * (float) METERS_TO_FEET;
			fathoms = feet / 6F;
		} catch (Exception e) {
			System.err.println("parseDPT For " + s + ", " + e.toString());
			//  e.printStackTrace();
		}

		if (unit == DEPTH_IN_FEET) {
			return feet;
		} else if (unit == DEPTH_IN_METERS) {
			return meters;
		} else if (unit == DEPTH_IN_FATHOMS) {
			return fathoms;
		} else {
			return meters;
		}
	}

	// Depth Below Transducer
	public static float parseDBT(String data) {
		return parseDBT(data, DEPTH_IN_METERS);
	}
	// Depth Below Transducer
	public static float parseDBT(String data, short unit) {
		String s = data.trim();
		if (s.length() < 6) {
			return -1F;
		}
		/* Structure is
		 *  $aaDBT,011.0,f,03.3,M,01.8,F*18(CR)(LF)
		 *         |     | |    | |    |
		 *         |     | |    | |    F for fathoms
		 *         |     | |    | Depth in fathoms
		 *         |     | |    M for meters
		 *         |     | Depth in meters
		 *         |     f for feet
		 *         Depth in feet
		 */
		float feet = 0.0F;
		float meters = 0.0F;
		float fathoms = 0.0F;
		String str = "";
		String first = "", last = "";
		try {
			first = "DBT,";
			last = ",f,";
			if (s.contains(first) && s.contains(last)) {
				if (s.indexOf(first) < s.indexOf(last)) {
					str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));
				}
			}
			feet = parseNMEAFloat(str);
			first = ",f,";
			last = ",M,";
			if (s.contains(first) && s.contains(last)) {
				if (s.indexOf(first) < s.indexOf(last)) {
					str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));
				}
			}
			meters = parseNMEAFloat(str);
			first = ",M,";
			last = ",F";
			if (s.contains(first) && s.contains(last)) {
				if (s.indexOf(first) < s.indexOf(last)) {
					str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));
				}
			}
			fathoms = parseNMEAFloat(str);
		} catch (Exception e) {
			System.err.println("parseDBT For " + s + ", " + e.toString());
//    e.printStackTrace();
		}

		if (unit == DEPTH_IN_FEET) {
			return feet;
		} else if (unit == DEPTH_IN_METERS) {
			return meters;
		} else if (unit == DEPTH_IN_FATHOMS) {
			return fathoms;
		} else {
			return feet;
		}
	}

	public static String parseTXT(String sentence) {
		/*
		 * WIP
		 * Structure:
		 * $AITXT,01,01,91,FREQ,2087,2088*57
		 * $GPTXT,01,01,02,u-blox ag - www.u-blox.com*50
		 * $GPTXT,01,01,02,HW  UBX-G70xx   00070000 FF7FFFFFo*69
		 *
		 *        1  2  3  4
		 * $GPTXT,01,01,02,ROM CORE 1.00 (59842) Jun 27 2012 17:43:52*59
		 *        |  |  |  |                                          |
		 *        |  |  |  |                                          Checksum
		 *        |  |  |  Content
		 *        |  |  ?
		 *        |  ?
		 *        ?
		 *
		 * $GPTXT,01,01,02,PROTVER 14.00*1E
		 * $GPTXT,01,01,02,ANTSUPERV=AC SD PDoS SR*20
		 * $GPTXT,01,01,02,ANTSTATUS=OK*3B
		 * $GPTXT,01,01,02,LLC FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFD*2C
		 *
		 * Pending questions: what are 01,01,02 ?
		 */
		String s = sentence.trim();
		if (s.length() < 6 || !s.contains("*")) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			return null;
		}
		String[] elmts = sentence.substring(0, sentence.indexOf("*")).split(",");

		return elmts.length > 4 ? elmts[4] : null;
	}

	public static boolean validCheckSum(String sentence) {
		return validCheckSum(sentence, false);
	}

	public static boolean validCheckSum(String data, boolean verb) {
		String sentence = data.trim();
		boolean b = false;
		try {
			int starIndex = sentence.indexOf("*");
			if (starIndex < 0) {
				return false;
			}
			String csKey = sentence.substring(starIndex + 1);
			int csk = Integer.parseInt(csKey, 16);
//    System.out.println("Checksum  : 0x" + csKey + " (" + csk + ")");
			String str2validate = sentence.substring(1, sentence.indexOf("*"));
//    System.out.println("To validate:[" + str2validate + "]");
//    char[] ca = str2validate.toCharArray();
//    int calcCheckSum = ca[0];
//    for (int i=1; i<ca.length; i++)
//      calcCheckSum = calcCheckSum ^ ca[i]; // XOR

			int calcCheckSum = calculateCheckSum(str2validate);
			b = (calcCheckSum == csk);
//    System.out.println("Calculated: 0x" + lpad(Integer.toString(calcCheckSum, 16).toUpperCase(), 2, "0"));
		} catch (Exception ex) {
			if (verb) System.err.println("Oops:" + ex.getMessage());
		}
		return b;
	}

	public static int calculateCheckSum(String str) {
		if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
			System.out.printf("Calculating checksum for %s\n", str);
		}
		int cs = 0;
		char[] ca = str.toCharArray();
		cs = ca[0];
		for (int i = 1; i < ca.length; i++) {
			cs = cs ^ ca[i]; // XOR
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				System.out.printf("Checksum is now 0x%02X \n", cs);
			}
		}
		if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
			System.out.printf("Final Checksum %02X \n", cs);
		}
		return cs;
	}

	/**
	 * @param sentence a VALID NMEA Sentence
	 * @return the device ID
	 */
	public static String getDeviceID(String sentence) {
		String id = "";
		if (sentence == null || sentence.length() < 7) {
			throw new RuntimeException(String.format("Invalid NMEA Sentence", sentence));
		}
		id = sentence.substring(1, 3);
		return id;
	}

	/**
	 * @param sentence a VALID NMEA Semtence
	 * @return the sentence ID
	 */
	public static String getSentenceID(String sentence) {
		String id = "";
		if (sentence == null || sentence.length() < 7) {
			throw new RuntimeException(String.format("Invalid NMEA Sentence [%s]", sentence));
		}
		id = sentence.substring(3, 6);
		return id;
	}

	/**
	 * Enforce the parsing using the Locale.ENGLISH
	 *
	 * @param str the string to parse
	 * @return the double value
	 * @throws Exception, in case it fails
	 */
	private static double parseNMEADouble(String str) throws Exception {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		Number number = nf.parse(str);
		double d = number.doubleValue();
//  System.out.println("Number is " + Double.toString(d));
		return d;
	}

	private static float parseNMEAFloat(String str) throws Exception {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		Number number = nf.parse(str);
		float f = number.floatValue();
		//  System.out.println("Number is " + Double.toString(d));
		return f;
	}

	/*
	 * Parses strings like "2006-05-05T17:35:48.000" + "Z" or UTC Offset like "-10:00"
	 * 01234567890123456789012
	 * 1         2         3
	 * <p>
	 * Return a UTC date
	 */
	public static long durationToDate(String duration) {
		return durationToDate(duration, null);
	}

	/*
	 * Sample: "2006-05-05T17:35:48.000Z"
	 *          |    |  |  |  |  |  |
	 *          |    |  |  |  |  |  20
	 *          |    |  |  |  |  17
	 *          |    |  |  |  14
	 *          |    |  |  11
	 *          |    |  8
	 *          |    5
	 *          0
	 */
	public static long durationToDate(String duration, String tz)
					throws RuntimeException {
		// A RegEx
		final String regex = // "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})(.)$";
				             "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(\\.(\\d{3}))?(.*)$"; // ms, TZ are optional.
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(duration);
		if (!matcher.find()) {
			// TODO Oops, raise ?
			System.out.printf("Oops, no duration match for [%s] %s.\n", duration, tz);
		}

		String yyyy = duration.substring(0, 4);
		String mm = duration.substring(5, 7);
		String dd = duration.substring(8, 10);
		String hh = duration.substring(11, 13);
		String mi = duration.substring(14, 16);
		String ss = duration.substring(17, 19);
		String ms = "0";
		try {
			ms = duration.substring(20, 23);
		} catch (IndexOutOfBoundsException iobe) {
			// Absorb;
		}

		float utcOffset = 0F;

		String trailer = duration.substring(19);
		if (trailer.contains("+") || trailer.contains("-")) {
//    System.out.println(trailer);
			if (trailer.contains("+")) {
				trailer = trailer.substring(trailer.indexOf("+") + 1);
			}
			if (trailer.contains("-")) {
				trailer = trailer.substring(trailer.indexOf("-"));
			}
			if (trailer.contains(":")) {
				String hours = trailer.substring(0, trailer.indexOf(":"));
				String mins = trailer.substring(trailer.indexOf(":") + 1);
				utcOffset = (float) Integer.parseInt(hours) + (float) (Integer.parseInt(mins) / 60f);
			} else {
				utcOffset = Float.parseFloat(trailer);
			}
		}
//  System.out.println("UTC Offset:" + utcOffset);

		Calendar calendar = Calendar.getInstance();
		if (utcOffset == 0f && tz != null) {
			calendar.setTimeZone(TimeZone.getTimeZone(tz));
		} else {
			calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		}
		int milliSec = 0;
		try {
			milliSec = Integer.parseInt(ms); // OK for 000,would fail if like '10:', where it is the TZ Offset.
		} catch (NumberFormatException nfe) {
			// Absorb
			milliSec = 0;
		}
		try {
			calendar.set(Integer.parseInt(yyyy),
							Integer.parseInt(mm) - 1,
							Integer.parseInt(dd),
							Integer.parseInt(hh),
							Integer.parseInt(mi),
							Integer.parseInt(ss));
			calendar.set(Calendar.MILLISECOND, milliSec);
			if (false) {
				Date date = calendar.getTime();
				System.out.printf(">> Date: %s\n", SDF_UTC.format(date));
			}
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("durationToDate, for [" + duration + "] : " + nfe.getMessage());
		}
		return calendar.getTimeInMillis() - (long) (utcOffset * (3_600_000));
	}

	public static String durationToExcel(String duration)
					throws RuntimeException {
		String yyyy = duration.substring(0, 4);
		String mm = duration.substring(5, 7);
		String dd = duration.substring(8, 10);
		String hh = duration.substring(11, 13);
		String mi = duration.substring(14, 16);
		String ss = duration.substring(17, 19);
		String result = "";
		try {
			result = yyyy + "/" + mm + "/" + dd + " " + hh + ":" + mi + ":" + ss;
		} catch (Exception ex) {
			throw new RuntimeException("durationToDate, for [" + duration + "] : " + ex.getMessage());
		}
		return result;
	}

	// Used by autoParse
	public enum Dispatcher {

		RMC("RMC", "Recommended Minimum Navigation Information, C", StringParsers::parseRMC, RMC.class),
		GLL("GLL", "Geographical Lat & Long", StringParsers::parseGLL, GLL.class),
		DBT("DBT", "Depth Below Transducer", StringParsers::parseDBT, Float.class),
		DPT("DPT", "Depth of Water", StringParsers::parseDPT, Object.class),
		GGA("GGA", "Global Positioning System Fix Data", StringParsers::parseGGA, List.class),
		GSA("GSA", "GPS DOP and active satellites", StringParsers::parseGSA, GSA.class),
		GSV("GSV", "Satellites in view", StringParsers::parseGSV, Map.class),
		HDG("HDG", "Heading - Deviation & Variation", StringParsers::parseHDG, HDG.class),
		HDM("HDM", "Heading - Magnetic", StringParsers::parseHDM, Integer.class),
		HDT("HDT", "Heading - True", StringParsers::parseHDT, Integer.class),
		MDA("MDA", "Meteo Composite", StringParsers::parseMDA, MDA.class),
		MMB("MMB", "Atm Pressure", StringParsers::parseMMB, Double.class),
		MTA("MTA", "Air Temperature, Celcius", StringParsers::parseMTA, Double.class),
		MTW("MTW", "Mean Temperature of Water", StringParsers::parseMTW, Double.class),
		MWD("MWD", "Wind Direction & Speed", StringParsers::parseMWD, TrueWind.class),
		MWV("MWV", "Wind Speed and Angle", StringParsers::parseMWV, ApparentWind.class), // Careful, actually returns Wind.class
		RMB("RMB", "Recommended Minimum Navigation Information, B", StringParsers::parseRMB, RMB.class),
		TXT("TXT", "Text Transmission", StringParsers::parseTXT, String.class),
		VDR("VDR", "Set and Drift", StringParsers::parseVDR, Current.class),
		VHW("VHW", "Water speed and heading", StringParsers::parseVHW, VHW.class),
		VLW("VLW", "Distance Traveled through Water", StringParsers::parseVLW, VLW.class),
		VTG("VTG", "Track made good and Ground speed", StringParsers::parseVTG, OverGround.class),
		VWR("VWR", "Relative Wind Speed and Angle", StringParsers::parseVWR, ApparentWind.class),
		VWT("VWT", "Wind Data", StringParsers::parseVWT, TrueWind.class),                              // Obsolete
		XDR("XDR", "Transducer Measurement", StringParsers::parseXDR, List.class),
		ZDA("ZDA", "Time & Date - UTC, day, month, year and local time zone", StringParsers::parseZDA, UTCDate.class);

		private final String key;
		private final String description;
		private final Function<String, Object> parser;
		private final Class returnedType;

		Dispatcher(String key, String description, Function<String, Object> parser, Class returned) {
			this.key = key;
			this.description = description;
			this.parser = parser;
			returnedType = returned;
		}

		public String key() {
			return this.key;
		}
		public String description() {
			return this.description;
		}
		public Function<String, Object> parser() {
			return this.parser;
		}
		public Class returnedType() { return this.returnedType; }
	}

	public static String getSentenceDescription(String id) {
		return Arrays.asList(StringParsers.Dispatcher.values()).stream()
				.filter(disp -> findDispatcherByKey(id) != null)
				.map(disp -> findDispatcherByKey(id).description())
				.findFirst()
				.orElse(null);
	}

	public static class ParsedData {
		private String deviceID;
		private String sentenceId;
		private String fullSentence;
		private Object parsedData;

		public ParsedData deviceID(String deviceID) {
			this.deviceID = deviceID;
			return this;
		}
		public ParsedData sentenceId(String sentenceId) {
			this.sentenceId = sentenceId;
			return this;
		}
		public ParsedData fullSentence(String fullSentence) {
			this.fullSentence = fullSentence;
			return this;
		}
		public ParsedData parsedData(Object parsedData) {
			this.parsedData = parsedData;
			return this;
		}
		public String getDeviceId() {
			return this.deviceID;
		}
		public String getSentenceId() {
			return this.sentenceId;
		}
		public String getFullSentence() {
			return this.fullSentence;
		}
		public Object getParsedData() {
			return this.parsedData;
		}
	}

	public static Dispatcher findDispatcherByKey(String key) {
		Optional<Dispatcher> first = Arrays.stream(Dispatcher.values())
				.filter(disp -> key.equals(disp.key()))
				.findFirst();
		return first.orElse(null);
	}
	/**
	 * Lists available parsers, key and description.
	 */
	public static void listDispatcher() {
		listDispatchers(System.out);
	}
	public static void listDispatchers(PrintStream out) {
		Arrays.stream(Dispatcher.values())
				.forEach(dispatcher -> out.printf("%s: %s\n", dispatcher.key(), dispatcher.description()));
	}

	public static ParsedData autoParse(String data) {
		if (!validCheckSum(data)) {
			throw new RuntimeException(String.format("Invalid NMEA Sentence CheckSum [%s]", data));
		}
		ParsedData parsedData = new ParsedData().fullSentence(data);
		String key = getSentenceID(data);
		parsedData.sentenceId(key).deviceID(getDeviceID(data));
		for (Dispatcher dispatcher : Dispatcher.values()) {
			if (key.equals(dispatcher.key)) {
				Object parsed = dispatcher.parser().apply(data);
				parsedData.parsedData(parsed);
				break;
			}
		}
		return parsedData;
	}
}
