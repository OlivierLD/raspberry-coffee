package nmea.forwarders;

import calc.GeomUtil;
import context.ApplicationContext;
import context.NMEADataCache;
import lcd.ScreenBuffer;
import nmea.mux.context.Context;
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
import spi.lcd.nokia.Nokia5110;
import lcd.substitute.SwingLedPanel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

/**
 * This is an example of a <b>transformer</b>.
 * <br>
 * To be used with other apps.
 * This dynamic transformer displays the TWD on a Nokia5110 small screen
 *
 * <br>
 * This is JUST an example. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Web UI.
 * The REST api is not aware of it.
 *
 * It auto-scrolls across available values.
 */
public class Nokia5110Processor implements Forwarder {
	private boolean keepWorking = true;

	private final static SimpleDateFormat SDF_DATE = new SimpleDateFormat("E dd MMM yyyy");
	private final static SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss Z");
	private final static SimpleDateFormat SDF_TIME_NO_Z = new SimpleDateFormat("HH:mm:ss");
	static {
		SDF_DATE.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
		SDF_TIME.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
		SDF_TIME_NO_Z.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

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

	private int WIDTH  = 84; // 128;
	private int HEIGHT = 48; //  64;

	private Nokia5110 nokiaScreen;
	private ScreenBuffer sb;
	private SwingLedPanel substitute;

	private boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.
	private boolean verbose = "true".equals(System.getProperty("screen.verbose", "false"));

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
	private final static int GPS_OPTION = 16;
	private final static int SOL_OPTION = 17;

	private static List<Integer> optionList = new ArrayList<>();
//	{
//					TWD_OPTION, // True Wind Direction
//					BSP_OPTION, // Boat Speed
//					TWS_OPTION, // True Wind Speed
//					TWA_OPTION, // True Wind Angle
//					AWA_OPTION, // Apparent Wind Angle
//					AWS_OPTION, // Apparent Wind Speed
//					ATP_OPTION, // Air Temperature
//					WTP_OPTION, // Water Temperature
//					COG_OPTION, // Course Over Ground
//					SOG_OPTION, // Speed Over Ground
//					HDG_OPTION, // Heading
//					POS_OPTION, // Position
//					DBT_OPTION, // Depth Below Transducer
//					HUM_OPTION, // Relative Humidity
//					CUR_OPTION, // Current. Speed and Direction
//					PRS_OPTION  // Atmospheric Pressure (PRMSL).
//          GPS_OPTION  // GPS Date & Time
//          SOL_OPTION  // SOLAR Date & Time
//	};

	private int currentOption = 0;

	private long scrollWait = 5_000L;

	enum SpeedUnit {
		KNOTS, KMH, MPH, MS
	};

	SpeedUnit speedUnit = SpeedUnit.KNOTS;

	// Use it to scroll across data
	public void onButtonPressed() {
		currentOption++;
		if (currentOption >= optionList.size()) {
			currentOption = 0;
		}
	}

	/*
	 * @throws Exception
	 */
	public Nokia5110Processor() throws Exception {

		int nbTry = 0;
		boolean ok = false;
		while (!ok) {
			// Make sure the cache has been initialized.
			if (ApplicationContext.getInstance().getDataCache() == null) {
				if (nbTry < 10) {
					try { Thread.sleep(1000L); } catch (Exception ex) {}
					nbTry++;
				} else {
					throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
				}
			} else {
				ok = true;
			}
		}

		final String REST_CLIENT_EVENT_NAME = "change-speed-unit";
		final String SPEED_UNIT = "speed-unit";

		Context.getInstance().addTopicListener(new Context.TopicListener(REST_CLIENT_EVENT_NAME) {
			/**
			 * Speed Unit can be changed with a REST call: POST /mux/events/change-speed-unit with a payload like
			 * { "speed-unit": "kmh" }
			 * @param topic <code>change-speed-unit</code> for this to work, or a regex matching it.
			 * @param payload one of { "speed-unit": "kmh" }, { "speed-unit": "mph" }, { "speed-unit": "ms" }, or { "speed-unit": "kts" }
			 */
			@Override
			public void topicBroadcast(String topic, Object payload){
//			System.out.println("Topic:" + topic + ", payload:" + payload);
				if (payload instanceof Map) {
					Map<String, Object> map = (Map) payload;
					Object unit = map.get(SPEED_UNIT);
					if (unit != null) {
						if (verbose) {
						  System.out.println("Changing Speed Unit to " + unit.toString());
						}
						switch (unit.toString()) {
							case "kmh":
								speedUnit = SpeedUnit.KMH;
								break;
							case "ms":
								speedUnit = SpeedUnit.MS;
								break;
							case "mph":
								speedUnit = SpeedUnit.MPH;
								break;
							case "kts":
								speedUnit = SpeedUnit.KNOTS;
								break;
							default:
								System.err.println(String.format("Un-managed speed unit [%s]", unit.toString()));
								break;
						}
					} else {
						System.err.println("Expected member [speed-unit] not found in the payload.");
					}
				} else {
					System.err.println(String.format("Un-expected payload type [%s]", payload.getClass().getName()));
				}
			}
		});

		try {
			nokiaScreen = new Nokia5110();
			nokiaScreen.begin();
			nokiaScreen.clear();
		} catch (Throwable error) {
			// Not on a RPi? Try JPanel.
			nokiaScreen = null;
			System.out.println("Displaying substitute Swing Led Panel");
			substitute = new SwingLedPanel(SwingLedPanel.ScreenDefinition.NOKIA5110);
			substitute.setVisible(true);
		}
		sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

		Thread scrollThread = new Thread("ScrollThread") {
			public void run() {
				while (keepWorking) {
					try { Thread.sleep(scrollWait); } catch (Exception ignore) {}
					onButtonPressed();
				}
			}
		};
		scrollThread.start();

		Thread cacheThread = new Thread("Nokia5110Processor CacheThread") {
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
					if (!optionList.isEmpty()) {
						int toDisplay = optionList.get(currentOption);
						switch (toDisplay) {
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
								displayAngleAndValue("TWA ", bean.twa, 1);
								break;
							case AWA_OPTION:
								displayAngleAndValue("AWA ", bean.awa, 1);
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
							case GPS_OPTION:
								displayDateTime(bean.gpsdatetime);
								break;
							case SOL_OPTION:
								displaySolarDateTime(bean.gpssolardate);
								break;
							default:
								break;
						}
					}
					try { Thread.sleep(1_000L); } catch (Exception ex) {}
				}
				System.out.println("Cache thread completed.");
			}
		};
		cacheThread.start();
	}

	private void displayAngleAndValue(String label, int value) {
		displayAngleAndValue(label, value, 2);
	}
	private void displayAngleAndValue(String label, int value, int fontFact) {
		int centerX = 65, centerY = 21, radius = 15;
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text(label, 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(String.valueOf(value) + "\u00b0", 2, 19, fontFact, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Circle
			sb.circle(centerX, centerY, radius);

			// Hand
			int toX = centerX - (int) Math.round(radius * Math.sin(Math.toRadians(180 + value)));
			int toY = centerY + (int) Math.round(radius * Math.cos(Math.toRadians(180 + value)));
			sb.line(centerX, centerY, toX, toY);

			// Display
			display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private final static NumberFormat _22 = new DecimalFormat("00.00");
	private final static NumberFormat _X1 = new DecimalFormat("#0.0");

	private void displayValue(String label, String unit, double value) {
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text(label + "in" + unit, 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(_22.format(value), 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			display();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void displaySpeed(String label, double value) {
		String unit = " kts";
		double speedFactor = 1D;
		switch (speedUnit) {
			case KMH:
				unit = " km/h";
				speedFactor = 1.852;
				break;
			case MPH:
				unit = " mph";
				speedFactor = 1.151;
				break;
			case MS:
				unit = " m/s";
				speedFactor = 0.514444;
				break;
			case KNOTS:
			default:
				break;
		}
		displayValue(label, unit, value * speedFactor);
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
			display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void displayCurrent(int dir, double speed) {
		String direction = "CUR. DIR in \u00b0";
		String dirValue = String.valueOf(dir);
		String speedStr = "CUR. SPD in kts";
		String speedValue = _22.format(speed);
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text(direction, 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(dirValue, 2, 19, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(speedStr, 2, 29, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(speedValue, 2, 39, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void displayPRMSL(double value) {
		try {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text("PRMSL in mb", 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(_X1.format(value), 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void displayDateTime(long gpsDateTime) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		cal.setTimeInMillis(gpsDateTime);
		Date gps = cal.getTime();
		try {

			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text("GPS Date and Time", 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(SDF_DATE.format(gps), 2, 19, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(SDF_TIME.format(gps), 2, 29, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			display();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// Make sure the cache is fed using EoT, see System property calculate.solar.with.eot
	private void displaySolarDateTime(long solarTime) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		cal.setTimeInMillis(solarTime);
		Date solar = cal.getTime();
		try {

			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text("SOLAR Date and Time", 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(SDF_DATE.format(solar), 2, 19, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(SDF_TIME_NO_Z.format(solar), 2, 29, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Display
			display();

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
			if (nokiaScreen != null) {
				nokiaScreen.clear(); // Blank screen
				nokiaScreen.setScreenBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
				nokiaScreen.display(); // Display blank screen
				nokiaScreen.shutdown();
			} else {
				substitute.setVisible(false);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void display() throws Exception {
		if (nokiaScreen != null) {
			nokiaScreen.setScreenBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			nokiaScreen.display();
		} else {
			substitute.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			substitute.display();
		}
	}

	public static class NokiaBean {
		private String cls;
		private String type = "nokia";

		public NokiaBean(Nokia5110Processor instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new NokiaBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		String betweenLoops = props.getProperty("display.time", "5");
		try {
			scrollWait = Long.parseLong(betweenLoops) * 1_000L;
		} catch (NumberFormatException nfe) {
			System.err.println("Using default value for display wait time");
		}
		// Data to display on the small screen
		String csv = props.getProperty("to.display", "");
		if (!csv.trim().isEmpty()) {
			Arrays.stream(csv.trim().split(",")).forEach(id -> {
				switch (id) {
					case "POS": // Position
						optionList.add(POS_OPTION);
						break;
					case "GPS": // Position
						optionList.add(GPS_OPTION);
						break;
					case "SOL": // Position
						optionList.add(SOL_OPTION);
						break;
					case "BSP":
						optionList.add(BSP_OPTION);
						speedUnit = SpeedUnit.KNOTS;
						break;
					case "SOG": // KMH, MPH Speed in knots, km/h or mph
						optionList.add(SOG_OPTION);
						speedUnit = SpeedUnit.KNOTS;
						break;
					case "KMH": // KMH, MPH Speed in knots, km/h or mph
						optionList.add(SOG_OPTION);
						speedUnit = SpeedUnit.KMH;
						break;
					case "MPH": // KMH, MPH Speed in knots, km/h or mph
						optionList.add(SOG_OPTION);
						speedUnit = SpeedUnit.MPH;
						break;
					case "MS": // KMH, MPH Speed in knots, km/h or mph
						optionList.add(SOG_OPTION);
						speedUnit = SpeedUnit.MS;
						break;
					case "COG": // Course Over Ground
						optionList.add(COG_OPTION);
						break;
					case "HDG": // Heading
						optionList.add(HDG_OPTION);
						break;
					case "TWD": // True Wind Direction
						optionList.add(TWD_OPTION);
						break;
					case "TWS": // - True Wind Speed
						optionList.add(TWS_OPTION);
						break;
					case "TWA": // - True Wind Angle
						optionList.add(TWA_OPTION);
						break;
					case "AWS": // - Apparent Wind Speed
						optionList.add(AWS_OPTION);
						break;
					case "AWA": // - Apparent Wind Angle
						optionList.add(AWA_OPTION);
						break;
					case "WTP": // - Water Temperature
						optionList.add(WTP_OPTION);
						break;
					case "ATP": // - Air Temperature
						optionList.add(ATP_OPTION);
						break;
					case "PML": // - Pressure at Mean Sea Level
						optionList.add(PRS_OPTION);
						break;
					case "HUM": // - Humidity
						optionList.add(HUM_OPTION);
						break;
					case "DBT": // - Depth
						optionList.add(DBT_OPTION);
						break;
					case "CUR": // - Current. Speed and Direction
						optionList.add(CUR_OPTION);
						break;
					case "PCH": // Pitch
					case "ROL": // Roll
					default:
						System.out.println(String.format("[%s] not implemented yet.", id));
						break;
				}
			});
		}
	}
}
