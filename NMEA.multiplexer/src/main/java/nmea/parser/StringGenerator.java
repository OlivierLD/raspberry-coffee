package nmea.parser;

import java.util.TimeZone;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import utils.StringUtils;

public class StringGenerator {
	private static final SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
	static {
		SDF_DATETIME.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}
	private static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HHmmss");
	private static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("ddMMyy");

	private final static NumberFormat LAT_DEG_FMT = new DecimalFormat("00");
	private final static NumberFormat LONG_DEG_FMT = new DecimalFormat("000");
	private final static NumberFormat MIN_FMT = new DecimalFormat("00.000");
	private final static NumberFormat MIN_FMT_2 = new DecimalFormat("00.00");
	private final static NumberFormat OG_FMT = new DecimalFormat("000.0");
	private final static NumberFormat TEMP_FMT = new DecimalFormat("#0.0");
	private final static NumberFormat PRMSL_FMT = new DecimalFormat("##0.0000");
	private final static NumberFormat PRMSL_FMT_2 = new DecimalFormat("##0");
	private final static NumberFormat PERCENT_FMT = new DecimalFormat("##0.0");
	private final static NumberFormat DIR_FMT = new DecimalFormat("##0");
	private final static NumberFormat DIR_FMT_1 = new DecimalFormat("##0.0");
	private final static NumberFormat SPEED_FMT = new DecimalFormat("#0.0");
	private final static NumberFormat SPEED_FMT_2 = new DecimalFormat("#0.00");
	private final static NumberFormat ANGLE_FMT = new DecimalFormat("##0");

	private final static NumberFormat PRMSL_FMT_MDA = new DecimalFormat("##0.000");

	private final static NumberFormat GENERIC_1_FMT = new DecimalFormat("##0.00000");

	private final static double KNOTS_TO_KMH = 1.852;
	private final static double KNOTS_TO_MS  = 1.852 * 0.27777777;

/*
 * Common talker IDs
  |================================================================
  |GP    |  Global Positioning System receiver
  |LC    |  Loran-C receiver
  |II    |  Integrated Instrumentation
  |IN    |  Integrated Navigation
  |EC    |  Electronic Chart Display & Information System (ECDIS)
  |CD    |  Digital Selective Calling (DSC)
  |GL    |  GLONASS, according to IEIC 61162-1
  |GN    |  Mixed GPS and GLONASS data, according to IEIC 61162-1
  |================================================================
 */

  /*
   * XDR - Transducer Measurements
      $--XDR,a,x.x,a,c--c,...    ...a,x.x,a,c--c*hh<CR><LF>
             | |   | |    |        ||     |
             | |   | |    |        |+-----+-- Transducer 'n'
             | |   | |    +--------+- Data for variable # of transducers
             | |   | +- Transducer #1 ID
             | |   +- Units of measure, Transducer #1
             | +- Measurement data, Transducer #1
             +- Transducer type, Transducer #1
      Notes:
      1) Sets of the four fields 'Type-Data-Units-ID' are allowed for an undefined number of transducers.
      Up to 'n' transducers may be included within the limits of allowed sentence length, null fields are not
      required except where portions of the 'Type-Data-Units-ID' combination are not available.
      2) Allowed transducer types and their units of measure are:
      Transducer           Type Field  Units Field              Comments
      -------------------------------------------------------------------
      temperature            C           C = degrees Celsius
      angular displacement   A           D = degrees            "-" = anti-clockwise
      linear displacement    D           M = meters             "-" = compression
      frequency              F           H = Hertz
      force                  N           N = Newton             "-" = compression
      pressure               P           B = Bars, P = Pascal   "-" = vacuum
      flow rate              R           l = liters/second
      tachometer             T           R = RPM
      humidity               H           P = Percent
      volume                 V           M = cubic meters
      generic                G           none (null)            x.x = variable data
      current                I           A = Amperes
      voltage                U           V = Volts
      switch or valve        S           none (null)            1 = ON/ CLOSED, 0 = OFF/ OPEN
      salinity               L           S = ppt                ppt = parts per thousand
   */

	public enum XDRTypes { // Se above for more details
		TEMPERATURE("C", "C"), // in Celcius
		ANGULAR_DISPLACEMENT("A", "D"), // In degrees
		LINEAR_DISPLACEMENT("D", "M"), // In meters
		FREQUENCY("F", "H"), // In Hertz
		FORCE("N", "N"), // In Newtons
		PRESSURE_B("P", "B"), // In Bars
		PRESSURE_P("P", "P"), // In Pascals
		FLOW_RATE("R", "l"), // In liters
		TACHOMETER("T", "R"), // In RPM
		HUMIDITY("H", "P"), // In %
		VOLUME("V", "M"), // In Cubic meters
		GENERIC("G", ""),  // No unit
		CURRENT("I", "A"), // In Amperes
		VOLTAGE("U", "V"), // In Volts
		SWITCH_OR_VALVE("S", ""),  // No Unit
		SALINITY("L", "S"); // In Parts per Thousand

		private final String type;
		private final String unit;

		XDRTypes(String type, String unit) {
			this.type = type;
			this.unit = unit;
		}

		public String type() {
			return this.type;
		}

		public String unit() {
			return this.unit;
		}
	}

	public static class XDRElement {
		private XDRTypes typeNunit;
		private double value;
		private String transducerName;

		public XDRElement(XDRTypes tnu, double value, String tdName) {
			this.typeNunit = tnu;
			this.value = value;
			this.transducerName = tdName;
		}

		public StringGenerator.XDRTypes getTypeNunit() {
			return typeNunit;
		}

		public double getValue() {
			return value;
		}

		public String getTransducerName() {
			return this.transducerName;
		}

		public String toString() {
			return this.transducerName + ", " + this.getTypeNunit() + ", " + this.getTypeNunit().type() + ", " + Double.toString(this.getValue()) + " " + this.getTypeNunit().unit();
		}
	}

	public static String generateXDR(String devicePrefix, XDRElement first, XDRElement... next) {
		int nbDevice = 0;
		String xdr = devicePrefix + "XDR,";
		NumberFormat nf = null;
		xdr += (first.getTypeNunit().type() + ",");
		if (first.getTypeNunit().equals(XDRTypes.PRESSURE_B)) {
			nf = PRMSL_FMT;
		}
		if (first.getTypeNunit().equals(XDRTypes.PRESSURE_P)) {
			nf = PRMSL_FMT_2;
		}
		if (first.getTypeNunit().equals(XDRTypes.TEMPERATURE)) {
			nf = TEMP_FMT;
		}
		if (first.getTypeNunit().equals(XDRTypes.HUMIDITY)) {
			nf = PERCENT_FMT;
		}
		if (first.getTypeNunit().equals(XDRTypes.ANGULAR_DISPLACEMENT)) {
			nf = ANGLE_FMT;
		}
		if (first.getTypeNunit().equals(XDRTypes.GENERIC)) {
			nf = GENERIC_1_FMT;
		}
		// TODO More formats as needed...
//  System.out.println("XDR Format for [" + first.getTypeNunit() + "] is " + (nf == null?"":"not ") + "null");
		if (nf != null) {
			xdr += (nf.format(first.getValue()) + ",");
		} else {
			xdr += (Double.toString(first.getValue()) + ",");
		}
		xdr += (first.getTypeNunit().unit() + ",");
		xdr += (first.getTransducerName());
		// xdr += (Integer.toString(nbDevice++));

		for (XDRElement e : next) {
			nf = null;
			// TASK More formats
			if (e.getTypeNunit().equals(XDRTypes.PRESSURE_B)) {
				nf = PRMSL_FMT;
			}
			if (e.getTypeNunit().equals(XDRTypes.PRESSURE_P)) {
				nf = PRMSL_FMT_2;
			}
			if (e.getTypeNunit().equals(XDRTypes.TEMPERATURE)) {
				nf = TEMP_FMT;
			}
			if (e.getTypeNunit().equals(XDRTypes.HUMIDITY)) {
				nf = PERCENT_FMT;
			}
			if (e.getTypeNunit().equals(XDRTypes.ANGULAR_DISPLACEMENT)) {
				nf = ANGLE_FMT;
			}
			if (e.getTypeNunit().equals(XDRTypes.GENERIC)) {
				nf = GENERIC_1_FMT;
			}
			// TODO More formats...
			xdr += ("," + e.getTypeNunit().type() + ",");
//    System.out.println("XDR Format for [" + e.getTypeNunit() + "] is " + (nf == null?"":"not ") + "null");
			if (nf != null) {
				xdr += (nf.format(e.getValue()) + ",");
			} else {
				xdr += (Double.toString(e.getValue()) + ",");
			}
			xdr += (e.getTypeNunit().unit() + ",");
			xdr += (e.getTransducerName());
//    xdr += (Integer.toString(nbDevice++));
		}
		// Checksum
		int cs = StringParsers.calculateCheckSum(xdr);
		xdr += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + xdr;
	}

	/*
	 * -Double.MAX_VALUE means NO VALUE
	 *
	$--MDA,x.x,I,x.x,B,x.x,C,x.x,C,x.x,x.x,x.x,C,x.x,T,x.x,M,x.x,N,x.x,M*hh<CR><LF>
				 |   | |   | |   | |   | |   |   |   | |   | |   | |   | |   |
				 |   | |   | |   | |   | |   |   |   | |   | |   | |   | +---+- Wind Speed, m/s
				 |   | |   | |   | |   | |   |   |   | |   | |   | +---+- Wind Speed, Knots
				 |   | |   | |   | |   | |   |   |   | |   | +---+- Wind Dir, Magnetic
				 |   | |   | |   | |   | |   |   |   | +---+- Wind Dir, True
				 |   | |   | |   | |   | |   |   +---+- Dew point, degrees C
				 |   | |   | |   | |   | |   +- Absolute humidity, percent
				 |   | |   | |   | |   | +- Relative humidity, percent
				 |   | |   | |   | +---+- Water temperature, degrees C
				 |   | |   | +---+- Air temperature, degrees C
				 |   | +---+- Barometric pressure, bars
				 +---+- Barometric pressure, inches of mercury
	*/
	public static String generateMDA(String devicePrefix, double pressureInhPa, // ~ mb
	                                 double airTempInDegrees,
	                                 double waterTempInDegrees,
	                                 double relHumidity,
	                                 double absHumidity,
	                                 double dewPointInCelcius,
	                                 double windDirTrue,
	                                 double windDirMag,
	                                 double windSpeedInKnots) {
		String mda = devicePrefix + "MDA,";
		if (pressureInhPa != -Double.MAX_VALUE) {
			mda += (PRMSL_FMT_MDA.format(pressureInhPa / Pressure.HPA_TO_INHG) + ",I,");
			mda += (PRMSL_FMT_MDA.format(pressureInhPa / 1_000) + ",B,");
		} else {
			mda += ",,,,";
		}
		if (airTempInDegrees != -Double.MAX_VALUE) {
			mda += (TEMP_FMT.format(airTempInDegrees) + ",C,");
		} else {
			mda += ",,";
		}
		if (waterTempInDegrees != -Double.MAX_VALUE) {
			mda += (TEMP_FMT.format(waterTempInDegrees) + ",C,");
		} else {
			mda += ",,";
		}
		if (relHumidity != -Double.MAX_VALUE) {
			mda += (PERCENT_FMT.format(relHumidity) + ",");
		} else {
			mda += ",";
		}
		if (absHumidity != -Double.MAX_VALUE) {
			mda += (PERCENT_FMT.format(absHumidity) + ",");
		} else {
			mda += ",";
		}
		if (dewPointInCelcius != -Double.MAX_VALUE) {
			mda += (TEMP_FMT.format(dewPointInCelcius) + ",C,");
		} else {
			mda += ",,";
		}
		if (windDirTrue != -Double.MAX_VALUE) {
			mda += (DIR_FMT.format(windDirTrue) + ",T,");
		} else {
			mda += ",,";
		}
		if (windDirMag != -Double.MAX_VALUE) {
			mda += (DIR_FMT.format(windDirMag) + ",M,");
		} else {
			mda += ",,";
		}
		if (windSpeedInKnots != -Double.MAX_VALUE) {
			mda += (SPEED_FMT.format(windSpeedInKnots) + ",N,");
			mda += (SPEED_FMT.format(windSpeedInKnots * 1.852 / 3.6) + ",M");
		} else
			mda += ",,,";

		int cs = StringParsers.calculateCheckSum(mda);
		mda += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + mda;
	}

	/*
	 * Barometric pressure
	 */
	public static String generateMMB(String devicePrefix, double mbPressure) // pressure in mb
	{
		String mmb = devicePrefix + "MMB,";
		mmb += (PRMSL_FMT.format(mbPressure / 33.8600) + ",I,"); // Inches of Hg
		mmb += (PRMSL_FMT.format(mbPressure / 1_000) + ",B");     // Bars. 1 mb = 1 hPa
		// Checksum
		int cs = StringParsers.calculateCheckSum(mmb);
		mmb += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + mmb;
	}

	/*
	 * Air temperature
	 */
	public static String generateMTA(String devicePrefix, double temperature) // in Celcius
	{
		String mta = devicePrefix + "MTA,";
		mta += (TEMP_FMT.format(temperature) + ",C");
		// Checksum
		int cs = StringParsers.calculateCheckSum(mta);
		mta += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + mta;
	}

	/*
	 * Set and Drift (current speed and direction)
	 */
	public static String generateVDR(String devicePrefix, double speed, double dirT, double dirM) {
		String vdr = devicePrefix + "VDR,";
		vdr += (DIR_FMT_1.format((dirT == Double.MAX_VALUE) ? 0 : dirT) + ",T,");
		vdr += (DIR_FMT_1.format((dirM == Double.MAX_VALUE) ? 0 : dirM) + ",M,");
		vdr += ((Double.isNaN(speed) ? "" : SPEED_FMT_2.format(speed)) + ",N"); // TODO Make sure that is OK (2 decimals).
		// Checksum
		int cs = StringParsers.calculateCheckSum(vdr);
		vdr += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + vdr;
	}

	/* $WIMWD,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>*hh
	+     *
	+     * NMEA 0183 standard Wind Direction and Speed, with respect to north.
	+     *
	+     * <1> Wind direction, 0.0 to 359.9 degrees True, to the nearest 0.1 degree
	+     * <2> T = True
	+     * <3> Wind direction, 0.0 to 359.9 degrees Magnetic, to the nearest 0.1 degree
	+     * <4> M = Magnetic
	+     * <5> Wind speed, knots, to the nearest 0.1 knot.
	+     * <6> N = Knots
	+     * <7> Wind speed, meters/second, to the nearest 0.1 m/s.
	+     * <8> M = Meters/second
	+     */
	public static String generateMWD(String devicePrefix, double tdir, double knts, double dec) {
		String mwd = devicePrefix + "MWD,";
		mwd += (OG_FMT.format(tdir) + ",T,");
		double mDir = tdir - dec;
		if (Double.MAX_VALUE != Math.abs(tdir)) {
			if (mDir < 0) mDir += 360;
			if (mDir > 360) mDir -= 360;
		}
		mwd += ((Double.MAX_VALUE == Math.abs(mDir) ? "" : OG_FMT.format(mDir)) + ",M,");
		mwd += ((Double.MAX_VALUE == Math.abs(knts) ? "" : SPEED_FMT.format(knts)) + ",N,");
		mwd += ((Double.MAX_VALUE == Math.abs(knts) ? "" : SPEED_FMT.format(knts * KNOTS_TO_MS)) + ",M");
		// Checksum
		int cs = StringParsers.calculateCheckSum(mwd);
		mwd += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + mwd;
	}

	/* GLL Structure is
	 *  $aaGLL,llll.ll,a,gggg.gg,a,hhmmss.ss,A*hh
	 *         |       | |       | |         |
	 *         |       | |       | |         A:data valid
	 *         |       | |       | UTC of position
	 *         |       | |       Long sign :E/W
	 *         |       | Longitude
	 *         |       Lat sign :N/S
	 *         Latitude
	 */
	public static String generateGLL(String devicePrefix, double lat, double lng) {
		return generateGLL(devicePrefix, lat, lng, -1L);
	}
	public static String generateGLL(String devicePrefix, double lat, double lng, long epoch) {
		String gll = devicePrefix + "GLL,";
		double absLat = Math.abs(lat);
		String latStr = LAT_DEG_FMT.format((int)absLat) +
										MIN_FMT_2.format(60 * (absLat - ((int)absLat))) +
										"," +
										(lat < 0 ? "S" : "N");
		gll += latStr;
		gll += ",";
		double absLng = Math.abs(lng);
		String lngStr = LONG_DEG_FMT.format((int)absLng) +
										MIN_FMT_2.format(60 * (absLng - ((int)absLng))) +
										"," +
										(lng < 0 ? "W" : "E");
		gll += lngStr;
		gll += ",";
		if (epoch != -1) {
			Date utc = new Date(epoch);
			String strUTC = SDF_DATETIME.format(utc); // "20170623194037.845"
			//                                            Y   M D H M S
			//                                            012345678901234567
			//                                            0         1
			gll += strUTC.substring(8, 17); // Time
		}

		gll += ",A";

		int cs = StringParsers.calculateCheckSum(gll);
		gll += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + gll;
	}

	public static String generateRMC(String devicePrefix, Date date, double lat, double lng, double sog, double cog, double d) {
		String rmc = devicePrefix + "RMC,";
		rmc += (SDF_TIME.format(date) + ",");
		rmc += "A,";
		int deg = (int) Math.abs(lat);
		double min = 0.6 * ((Math.abs(lat) - deg) * 100d);
		rmc += (LAT_DEG_FMT.format(deg) + MIN_FMT.format(min));
		if (lat < 0) rmc += ",S,";
		else rmc += ",N,";

		deg = (int) Math.abs(lng);
		min = 0.6 * ((Math.abs(lng) - deg) * 100d);
		rmc += (LONG_DEG_FMT.format(deg) + MIN_FMT.format(min));
		if (lng < 0) rmc += ",W,";
		else rmc += ",E,";

		rmc += (OG_FMT.format(sog) + ",");
		rmc += (OG_FMT.format(cog) + ",");

		rmc += (SDF_DATE.format(date) + ",");

		rmc += (OG_FMT.format(Math.abs(d)) + ",");
		if (d < 0) rmc += "W";
		else rmc += "E";
		// Checksum
		int cs = StringParsers.calculateCheckSum(rmc);
		rmc += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + rmc;
	}

	public static String generateMWV(String devicePrefix, double aws, int awa) {
		return generateMWV(devicePrefix, aws, awa, StringParsers.APPARENT_WIND);
	}

	public static String generateMWV(String devicePrefix, double ws, int wa, int flavor) {
		if (wa < 0)
			wa = 360 + wa;
		String mwv = devicePrefix + "MWV,";
		mwv += (OG_FMT.format(wa) + (flavor == StringParsers.APPARENT_WIND ? ",R," : ",T,"));
		mwv += ((Double.MAX_VALUE == Math.abs(ws) ? "" : OG_FMT.format(ws)) + ",N,A");
		// Checksum
		int cs = StringParsers.calculateCheckSum(mwv);
		mwv += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + mwv;
	}

	public static String gerenateVWT(String devicePrefix, double tws, double twa) {
		String vwt = devicePrefix + "VWT,";
		vwt += (SPEED_FMT.format(Math.abs(twa)) + "," + (twa > 0 ? "R" : "L") + ",");
		vwt += ((Double.MAX_VALUE == Math.abs(tws) ? "" : SPEED_FMT.format(tws)) + ",N,");
		vwt += ((Double.MAX_VALUE == Math.abs(tws) ? "" : SPEED_FMT.format(tws * KNOTS_TO_MS)) + ",M,");
		vwt += ((Double.MAX_VALUE == Math.abs(tws) ? "" : SPEED_FMT.format(tws * KNOTS_TO_KMH)) + ",K");
		// Checksum
		int cs = StringParsers.calculateCheckSum(vwt);
		vwt += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + vwt;
	}

	public static String generateVHW(String devicePrefix, double bsp, int cc) {
		String vhw = devicePrefix + "VHW,,,";
		vhw += (LONG_DEG_FMT.format(cc) + ",M,");
		vhw += (MIN_FMT.format(bsp) + ",N,,");
		// Checksum
		int cs = StringParsers.calculateCheckSum(vhw);
		vhw += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + vhw;
	}

	public static String generateHDM(String devicePrefix, int cc) {
		String hdm = devicePrefix + "HDM,";
		hdm += (LONG_DEG_FMT.format(cc) + ",M");
		// Checksum
		int cs = StringParsers.calculateCheckSum(hdm);
		hdm += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));

		return "$" + hdm;
	}

	public static String generateZDA(String devicePrefix, long epoch) {
    /* Structure is
     * $GPZDA,hhmmss.ss,dd,mm,yyyy,xx,yy*CC
     * $GPZDA,201530.00,04,07,2002,00,00*60
     *        |         |  |  |    |  |
     *        |         |  |  |    |  local zone minutes 0..59
     *        |         |  |  |    local zone hours -13..13
     *        |         |  |  year
     *        |         |  month
     *        |         day
     *        HrMinSec(UTC)
     */
		String zda = devicePrefix + "ZDA,";
		Date utc = new Date(epoch);
		String strUTC = SDF_DATETIME.format(utc); // "20170623194037.845"
		//                                            012345678901234567
		//                                            0         1
		zda += strUTC.substring(8, 17); // Time
		zda += ",";
		zda += strUTC.substring(6, 8);  // day
		zda += ",";
		zda += strUTC.substring(4, 6);  // month
		zda += ",";
		zda += strUTC.substring(0, 4);  // year
		zda += ",00,00";
		// Checksum
		int cs = StringParsers.calculateCheckSum(zda);
		zda += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));
		return "$" + zda;
	}

	public static void main(String... args) {
		String rmc = generateRMC("II", new Date(), 38.2500, -122.5, 6.7, 210, 3d);
		System.out.println("Generated RMC:" + rmc);

		if (StringParsers.validCheckSum(rmc))
			System.out.println("Valid!");
		else
			System.out.println("Invalid...");

		String mwv = generateMWV("II", 23.45, 110);
		System.out.println("Generated MWV:" + mwv);

		if (StringParsers.validCheckSum(mwv))
			System.out.println("Valid!");
		else
			System.out.println("Invalid...");

		String vhw = generateVHW("II", 8.5, 110);
		System.out.println("Generated VHW:" + vhw);

		if (StringParsers.validCheckSum(vhw))
			System.out.println("Valid!");
		else
			System.out.println("Invalid...");

		String mmb = generateMMB("II", 1013.6);
		System.out.println("Generated MMB:" + mmb);

		String mta = generateMTA("II", 20.5);
		System.out.println("Generated MTA:" + mta);

		String xdr = generateXDR("II", new XDRElement(XDRTypes.PRESSURE_B, 1.0136, "BMP180"));
		System.out.println("Generated XDR:" + xdr);
		xdr = generateXDR("II",
						new XDRElement(XDRTypes.PRESSURE_B, 1.0136, "BMP180"),
						new XDRElement(XDRTypes.TEMPERATURE, 15.5, "BMP180"),
						new XDRElement(XDRTypes.HUMIDITY, 65.5, "HTU21DF"),
						new XDRElement(XDRTypes.GENERIC, 0.014270, "PRATE"));
		System.out.println("Generated XDR:" + xdr);

		xdr = generateXDR("XX", new XDRElement(XDRTypes.VOLTAGE, 12.34, "TRINKET"));
		System.out.println("Generated XDR:" + xdr);

		System.out.println("Generating MDA...");
		String mda = generateMDA("II", 1013.25, // PRMSL
						25,    // AIR TEMP
						12,    // WATER TEMP
						75,    // REL HUMIDITY
						50,    // ABS HUMIDITY
						9,    // DEW POINT (CELCIUS)
						270,    // TWD
						255,    // WIND DIR (MAG)
						12.0); // TWS
		if (StringParsers.validCheckSum(mda))
			System.out.println("Valid!");
		else
			System.out.println("Invalid...");
		System.out.println("Generated MDA:" + mda);
		mda = generateMDA("II", 1013.25, // PRMSL
						25,    // AIR TEMP
						12,    // WATER TEMP
						75,    // REL HUMIDITY
						50,    // ABS HUMIDITY
						-Double.MAX_VALUE,    // DEW POINT (CELCIUS)
						270,    // TWD
						255,    // WIND DIR (MAG)
						12.0); // TWS
		if (StringParsers.validCheckSum(mda))
			System.out.println("Valid!");
		else
			System.out.println("Invalid...");
		System.out.println("Generated MDA:" + mda);

		double noValue = -Double.MAX_VALUE;
		mda = generateMDA("WI", 1009, 31.7, noValue, noValue, noValue, noValue, 82.3, 72.3, 7.4);
		if (StringParsers.validCheckSum(mda))
			System.out.println("Valid!");
		else
			System.out.println("Invalid...");
		System.out.println("Generated MDA:" + mda);

		System.out.println("Another one...");
		mda = "$WIMDA,29.796,I,1.009,B,31.7,C,,,,,,,82.3,T,72.3,M,7.4,N,3.8,M*23";
		System.out.println("Copied MDA   :" + mda);
		if (StringParsers.validCheckSum(mda))
			System.out.println("Valid!");
		else
			System.out.println("Invalid...");

		String vwt = gerenateVWT("II", 16, 96);
		System.out.println(vwt);

		String mwd = generateMWD("II", 289, 20.9, 15.0);
		System.out.println(mwd);

		String zda = generateZDA("GP", System.currentTimeMillis());
		System.out.println(zda);
		UTCDate utc = StringParsers.parseZDA(zda);
		System.out.println("ZDA:" + utc.toString());

		String gll = generateGLL("XX", 37.7489, -122.5070, System.currentTimeMillis());
		System.out.println(gll);
		GLL parsedGLL = StringParsers.parseGLL(gll);
		GeoPos ll = parsedGLL.getGllPos();
		System.out.println(String.format(">> Pos %s", ll.toString()));
	}
}
