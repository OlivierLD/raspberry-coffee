package nmea.forwarders;

import com.google.gson.Gson;
import context.ApplicationContext;
import context.NMEADataCache;
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
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Properties;

/**
 * This is an example of a <b>transformer</b>.
 * <br>
 * To be used with other apps, like the pebble one.
 * The transformer turns the content of the NMEA Cache into the expected format.
 * <br>
 * See https://github.com/OlivierLD/pebble/tree/master/NMEA.app
 */
public class WebSocketProcessor implements Forwarder {
	private WebSocketClient wsClient = null;
	private boolean isConnected = false;
	private String wsUri;
	private Properties props = null;

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

	/*
	 * @param serverURL like ws://hostname:port/
	 * @throws Exception
	 */
	public WebSocketProcessor(String serverURL) throws Exception {
		// Make sure the cache has been initialized.
		if (ApplicationContext.getInstance().getDataCache() == null) {
			throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
		}

		this.wsUri = serverURL;
		try {
			wsClient = new WebSocketClient(new URI(serverURL)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("WS On Open");
					isConnected = true;
				}

				@Override
				public void onMessage(String string) {
//        System.out.println("WS On Message");
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					System.out.println("WS On Close");
					isConnected = false;
				}

				@Override
				public void onError(Exception exception) {
					System.out.println("WS On Error");
					exception.printStackTrace();
				}
			};
			wsClient.connect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Thread cacheThread = new Thread("WebSocketProcessor CacheThread") {
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
					String content = new Gson().toJson(bean);
					broadcast(content.getBytes());

					try { Thread.sleep(1_000L); } catch (Exception ex) {}
				}
				System.out.println("Cache thread completed.");
			}
		};
		cacheThread.start();
	}

	public String getWsUri() {
		return this.wsUri;
	}

	public void broadcast(byte[] message) {
		try {
			String mess = new String(message);
			if (!mess.isEmpty() && isConnected) {
				this.wsClient.send(mess);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void write(byte[] message) {
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			// Stop Cache thread
			keepWorking = false;
			try { Thread.sleep(2_000L); } catch (Exception ex) {}
			// Close WS Client
			this.wsClient.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class WSBean {
		private String cls;
		private String wsUri;
		private String type = "wsp";

		public WSBean(WebSocketProcessor instance) {
			cls = instance.getClass().getName();
			wsUri = instance.wsUri;
		}

		public String getWsUri() {
			return wsUri;
		}
	}

	@Override
	public Object getBean() {
		return new WSBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
