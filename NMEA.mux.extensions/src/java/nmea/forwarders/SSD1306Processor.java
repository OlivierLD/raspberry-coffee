package nmea.forwarders;

import com.pi4j.io.gpio.RaspiPin;
import context.ApplicationContext;
import context.NMEADataCache;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import nmea.parser.Angle180;
import nmea.parser.Angle180EW;
import nmea.parser.Angle180LR;
import nmea.parser.Angle360;
import nmea.parser.Current;
import nmea.parser.Depth;
import nmea.parser.Distance;
import nmea.parser.GeoPos;
import nmea.parser.Pressure;
import nmea.parser.SolarDate;
import nmea.parser.Speed;
import nmea.parser.Temperature;
import nmea.parser.UTCDate;
import nmea.parser.UTCTime;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import nmea.forwarders.pushbutton.PushButtonObserver;
import nmea.forwarders.pushbutton.PushButtonMaster;

import java.util.Properties;
import calc.GeomUtil;

/**
 * This is an example of a <b>transformer</b>.
 * <br>
 * To be used with other apps.
 * This transformer displays the TWD on an OLED display (SSD1306)
 * <br>
 * See http://raspberrypi.lediouris.net/SSD1306/readme.html
 *
 * <br>
 * This is JUST an example. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Web UI.
 * The REST api is not aware of it.
 */
public class SSD1306Processor implements Forwarder, PushButtonObserver {
	private boolean keepWorking = true;

	private static class CacheBean {
		private long gpstime;
//	private String gpstimefmt;
		private long gpsdatetime;
//	private String gpsdatetimefmt;

		private String wp;
		private double d2wp;
		private int b2wp;
		private double xte;

		private double lat;
		private double lng;
		private String pos;

		private long gpssolardate;

		private double log;
		private double daylog;
		private int cog;
		private double sog;

		private int awa;
		private double aws;
		private double dbt;
		private int hdg;

		private double bsp;

		private double wtemp;
		private double atemp;

		private double D;
		private double d;
		private double W;

		private double leeway;
		private int cmg;

		private double tws;
		private int twa;
		private int twd;

		private int cdr;
		private double csp;

		private double prmsl;
		private double hum;
	}

	private int WIDTH = 128;
	private int HEIGHT = 32;

	private SSD1306 oled;
	private ScreenBuffer sb;

	private boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

	private static PushButtonMaster pbm = null;

	private final static int TWD_OPTION =  0;
	private final static int BSP_OPTION =  1;
	private final static int TWS_OPTION =  2;
	private final static int TWA_OPTION =  3;
	private final static int AWA_OPTION =  4;
	private final static int AWS_OPTION =  5;
	private final static int ATP_OPTION =  6;
	private final static int WTP_OPTION =  7;
	private final static int COG_OPTION =  8;
	private final static int SOG_OPTION =  9;
	private final static int HDG_OPTION = 10;
	private final static int POS_OPTION = 11;
	private final static int DBT_OPTION = 12;
	private final static int HUM_OPTION = 13;
	private final static int CUR_OPTION = 14;
	private final static int PRS_OPTION = 15;

	private final static int[] OPTION_ARRAY = {
					TWD_OPTION, // True Wind Direction
					BSP_OPTION, // Boat Speed
					TWS_OPTION, // True Wind Speed
					TWA_OPTION, // True Wind Angle
					AWA_OPTION, // Apparent Wind Angle
					AWS_OPTION, // Apparent Wind Speed
					ATP_OPTION, // Air Temperature
					WTP_OPTION, // Water Temperature
					COG_OPTION, // Course Over Ground
					SOG_OPTION, // Speed Over Ground
					HDG_OPTION, // Heading
					POS_OPTION, // Position
					DBT_OPTION, // Depth Below Transducer
					HUM_OPTION, // Relative Humidity
					CUR_OPTION, // Current. Speed and Direction
					PRS_OPTION  // Atmospheric Pressure (PRMSL). // TODO Add other options, like in the I2C version
	};

	private int currentOption = TWD_OPTION;

	@Override
	public void onButtonPressed() {
		currentOption++;
		if (currentOption >= OPTION_ARRAY.length) {
			currentOption = 0;
		}
	}

	/*
	 * @throws Exception
	 */
	public SSD1306Processor() throws Exception {
		// Make sure the cache has been initialized.
		if (ApplicationContext.getInstance().getDataCache() == null) {
			throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
		}

		oled = new SSD1306(); // Default pins (look in the SSD1306 code)
		// Override the default pin:  Clock              MOSI                CS               RST                DC
//  oled = new SSD1306(RaspiPin.GPIO_12, RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16);

		oled.begin();
		oled.clear();

		sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

		PushButtonObserver instance = this;
		pbm = new PushButtonMaster(instance);
		pbm.initCtx(RaspiPin.GPIO_02); // (); Initialize Push button. Possibly takes the pushbutton pin as parameter.

		Thread cacheThread = new Thread("SSD1306Processor CacheThread") {
			public void run() {
				while (keepWorking) {
					NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
					// Populate bean
					CacheBean bean = new CacheBean();
					if (cache != null) {
						Object bsp = cache.get(NMEADataCache.BSP);
						if (bsp != null) {
							bean.bsp = ((Speed)bsp).getValue();
						}
						Object wtemp = cache.get(NMEADataCache.WATER_TEMP);
						if (wtemp != null) {
							bean.wtemp = ((Temperature)wtemp).getValue();
						}
						Object atemp = cache.get(NMEADataCache.AIR_TEMP);
						if (atemp != null) {
							bean.atemp = ((Temperature)atemp).getValue();
						}
						Object gpstime = cache.get(NMEADataCache.GPS_TIME);
						if (gpstime != null) {
							bean.gpstime = ((UTCTime)gpstime).getValue().getTime();
						}
						Object gpsdatetime = cache.get(NMEADataCache.GPS_DATE_TIME);
						if (gpsdatetime != null) {
							bean.gpsdatetime = ((UTCDate)gpsdatetime).getValue().getTime();
						}
						Object nextwp = cache.get(NMEADataCache.TO_WP);
						if (nextwp != null) {
							bean.wp = (String)nextwp;
						}
						Object d2wp = cache.get(NMEADataCache.D2WP);
						if (d2wp != null) {
							bean.d2wp = ((Distance)d2wp).getValue();
						}
						Object cog = cache.get(NMEADataCache.COG);
						if (cog != null) {
							bean.cog = (int)Math.round(((Angle360)cog).getValue());
						}
						Object sog = cache.get(NMEADataCache.SOG);
						if (sog != null) {
							bean.sog = ((Speed)sog).getValue();
						}
						Object leeway = cache.get(NMEADataCache.LEEWAY);
						if (leeway != null) {
							bean.leeway = ((Angle180LR)leeway).getValue();
						}
						Object aws = cache.get(NMEADataCache.AWS);
						if (aws != null) {
							bean.aws = ((Speed)aws).getValue();
						}
						Object tws = cache.get(NMEADataCache.TWS);
						if (tws != null) {
							bean.tws = ((Speed)tws).getValue();
						}
						Object awa = cache.get(NMEADataCache.AWA);
						if (awa != null) {
							bean.awa = (int)Math.round(((Angle180)awa).getValue());
						}
						Object twa = cache.get(NMEADataCache.TWA);
						if (twa != null) {
							bean.twa = (int)Math.round(((Angle180)twa).getValue());
						}
						Object twd = cache.get(NMEADataCache.TWD);
						if (twd != null) {
							bean.twd = (int)Math.round(((Angle360)twd).getValue());
						}
						Object pos = cache.get(NMEADataCache.POSITION);
						if (pos != null) {
							GeoPos geopos = (GeoPos)pos;
							bean.lat = geopos.lat;
							bean.lng = geopos.lng;
						}
						Object decl = cache.get(NMEADataCache.DECLINATION);
						if (decl != null) {
							bean.D = ((Angle180EW)decl).getValue();
						}
						Object dev = cache.get(NMEADataCache.DEVIATION);
						if (dev != null) {
							bean.d = ((Angle180EW)dev).getValue();
						}
						Object w = cache.get(NMEADataCache.VARIATION);
						if (w != null) {
							bean.W = ((Angle180EW)w).getValue();
						}
						Object hdg = cache.get(NMEADataCache.HDG_COMPASS);
						if (hdg != null) {
							bean.hdg = (int)Math.round(((Angle360)hdg).getValue());
						}
						Object cmg = cache.get(NMEADataCache.CMG);
						if (cmg != null) {
							bean.cmg = (int)Math.round(((Angle360)cmg).getValue());
						}
						Object vdr = cache.get(NMEADataCache.VDR_CURRENT);
						if (vdr != null) {
							bean.cdr = ((Current)vdr).angle;
							bean.csp = ((Current)vdr).speed;
						}
						Object xte = cache.get(NMEADataCache.XTE);
						if (xte != null) {
							bean.xte = ((Distance)xte).getValue();
						}
						Object b2wp = cache.get(NMEADataCache.B2WP);
						if (b2wp != null) {
							bean.b2wp = (int)Math.round(((Angle360)b2wp).getValue());
						}
						Object dbt = cache.get(NMEADataCache.DBT);
						if (dbt != null) {
							bean.dbt = ((Depth)dbt).getValue();
						}
						Object solarDate = cache.get(NMEADataCache.GPS_SOLAR_TIME);
						if (solarDate != null) {
							bean.gpssolardate = ((SolarDate)solarDate).getValue().getTime();
						}
						Object log = cache.get(NMEADataCache.LOG);
						if (log != null) {
							bean.log = ((Distance)log).getValue();
						}
						Object dayLog = cache.get(NMEADataCache.DAILY_LOG);
						if (dayLog != null) {
							bean.daylog = ((Distance)dayLog).getValue();
						}
						Object prmsl = cache.get(NMEADataCache.BARO_PRESS);
						if (prmsl != null) {
							bean.prmsl = ((Pressure)prmsl).getValue();
						}
						Object hum = cache.get(NMEADataCache.RELATIVE_HUMIDITY);
						if (hum != null) {
							bean.hum = (Double)hum;
						}
					}
					// Transformer's specific job.
					switch (currentOption) {
						case TWD_OPTION:
							displayAngleAndValue("TWD ", bean.twd);
							break;
						case BSP_OPTION:
							displaySpeed("BSP ", bean.bsp);
							break;
						case TWS_OPTION:
							displaySpeed("TWS ", bean.tws);
							break;
						case TWA_OPTION:
							displayAngleAndValue("TWA ", bean.twa);
							break;
						case AWA_OPTION:
							displayAngleAndValue("AWA ", bean.awa);
							break;
						case AWS_OPTION:
							displaySpeed("AWS ", bean.aws);
							break;
						case ATP_OPTION:
							displayTemp("AIR ", bean.atemp);
							break;
						case WTP_OPTION:
							displayTemp("WATER ", bean.wtemp);
							break;
						case COG_OPTION:
							displayAngleAndValue("COG ", bean.cog);
							break;
						case SOG_OPTION:
							displaySpeed("SOG ", bean.sog);
							break;
						case HDG_OPTION:
							displayAngleAndValue("HDG ", bean.hdg);
							break;
						case DBT_OPTION:
							displayValue("DBT ", " m", bean.dbt);
							break;
						case HUM_OPTION:
							displayValue("HUM ", " %", bean.hum);
							break;
						case CUR_OPTION:
							displayCurrent(bean.cdr, bean.csp);
							break;
						case POS_OPTION:
							displayPos(bean.lat, bean.lng);
							break;
						case PRS_OPTION:
							displayPRMSL(bean.prmsl);
							break;
						default:
							break;
					}

					try { Thread.sleep(1_000L); } catch (Exception ex) {}
				}
				System.out.println("Cache thread completed.");
			}
		};
		cacheThread.start();
	}

	private void displayAngleAndValue(String label, int value) {
		int centerX = 80, centerY = 16, radius = 15;
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text(label, 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(String.valueOf(value) + "\u00b0", 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Circle
			sb.circle(centerX, centerY, radius);

			// Hand
			int toX = centerX - (int) Math.round(radius * Math.sin(Math.toRadians(180 + value)));
			int toY = centerY + (int) Math.round(radius * Math.cos(Math.toRadians(180 + value)));
			sb.line(centerX, centerY, toX, toY);

			// Display
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private final static NumberFormat _22 = new DecimalFormat("00.00");
	private final static NumberFormat _X1 = new DecimalFormat("#0.0");

	private void displayValue(String label, String unit, double value) {
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text(label, 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(_22.format(value) + unit, 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void displaySpeed(String label, double value) {
		displayValue(label, " kts", value);
	}

	private void displayTemp(String label, double value) {
		displayValue(label, "\u00b0C", value);
	}

	private void displayPos(double lat, double lng) {
		String latitude = GeomUtil.decToSex(lat, GeomUtil.NO_DEG, GeomUtil.NS, GeomUtil.TRAILING_SIGN).replaceFirst(" ", "\u00b0");
		String longitude = GeomUtil.decToSex(lng, GeomUtil.NO_DEG, GeomUtil.EW, GeomUtil.TRAILING_SIGN).replaceFirst(" ", "\u00b0");
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text("POSITION", 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(latitude, 2, 19, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(longitude, 2, 29, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void displayCurrent(int dir, double speed) {
		String direction = "CURRENT DIR " + String.valueOf(dir) + "\u00b0";
		String speedStr = "CURRENT SPEED " + _22.format(speed) + " kts";
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text(direction, 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(speedStr, 2, 19, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void displayPRMSL(double value) {
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text("PRMSL ", 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(_X1.format(value) + " mb", 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void write(byte[] message) {
		// Nothing is done here. It is replaced by the Thread in the constructor.
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			// Stop Cache thread
			keepWorking = false;
			try { Thread.sleep(2_000L); } catch (Exception ex) {}
			sb.clear();
			oled.clear(); // Blank screen
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

			oled.shutdown();

			pbm.freeResources();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class OLEDBean {
		private String cls;
		private String type = "oled";

		public OLEDBean(SSD1306Processor instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new OLEDBean(this);
	}

	@Override
	public void setProperties(Properties props) {
	}
}
