package nmea.forwarders;

import com.google.gson.Gson;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.parser.Speed;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * To be used with other apps, like the pebble one.
 *
 * See https://github.com/OlivierLD/pebble/tree/master/NMEA.app
 */
public class WebSocketProcessor implements Forwarder {
	private WebSocketClient wsClient = null;
	private boolean isConnected = false;
	private String wsUri;

	private boolean keepWorking = true;

	private static class CacheBean {
		private double wtemp;
		private long gpstime;
		private String gpstimefmt;
		private String d2wp;
		private int cog;
		private double leeway;
		private double bsp;
		private double lat;
		private double lng;
		private String pos;
		private int b2wp;
		private double xte;
		private long gpsdatetime;
		private String gpsdatetimefmt;
		private double D;
		private double aws;
		private int cdr;
		private String towp;
		private double tws;
		private double dbt;
		private double log;
		private int awa;
		private int hdg;
		private int cmg;
		private int twd;
		private double prmsl;
		private double d;
		private double atemp;
		private int twa;
		private double daylog;
		private double sog;
		private double gpssolardate;
		private double vmgwind;
		private double vmgwp;
		// Extra
		private double hum;
		private double bat;
	}

	/**
	 * @param serverURL like ws://hostname:port/
	 * @throws Exception
	 */
	public WebSocketProcessor(String serverURL) throws Exception {
		// Make sure the cache has been initialized.
		if (ApplicationContext.getInstance().getDataCache() == null) {
			throw new RuntimeException("Init the Cache first. See the properties file used at started."); // Oops
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

		Thread cacheThread = new Thread() {
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
						// TODO: All the others...
					}

					String content = new Gson().toJson(bean);
					broadcast(content.getBytes());

					try { Thread.sleep(1000L); } catch (Exception ex) {}
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
			try { Thread.sleep(2000L); } catch (Exception ex) {}
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
}
