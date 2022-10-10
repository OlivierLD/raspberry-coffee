package joystick.adc.levelreader.samples;

import analogdigitalconverter.mcp.MCPReader;
import i2c.sensor.BMP180;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import joystick.adc.levelreader.ADCChannels_1_to_8;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

// import java.io.ByteArrayInputStream;
// import sun.misc.BASE64Decoder;

/**
 * Reads water level and temperature
 * Sends WS Messages (see level.server.js)
 */
public class LevelAndTemperature implements LevelListenerInterface {
	private final static NumberFormat NF = new DecimalFormat("#0.00");
	private final static Format SDF = new SimpleDateFormat("hh:mm:ss");

	private static WebSocketClient webSocketClient = null;
	private static String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
	private static String customerID = System.getProperty("customer.id", "Joe Shmow");

	@Override
	public void setLevel(int level) {
		if (level > 0) {
			JSONObject obj = new JSONObject();
			obj.put("customer-id", customerID);
			obj.put("water-level", level);
			if (webSocketClient != null) {
				System.out.println("Sending " + obj.toString());
				webSocketClient.send(obj.toString());
			} else {
				System.out.println("No websocket client (for level)");
			}
		}
	}

	private static boolean readTemperature = true;

	public static void setReadTemperature(boolean b) {
		readTemperature = b;
	}

	public static boolean keepReadingTemperature() {
		return readTemperature;
	}

	private static void initWebSocketConnection(String serverURI) {
		System.out.println("Connecting on WS " + serverURI);
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("WS Connection opened");
					Iterator<String> headers = serverHandshake.iterateHttpFields();
					while (headers.hasNext()) {
						String h = headers.next();
						System.out.println(h + " : " + serverHandshake.getFieldValue(h));
						/*
						if (h.equalsIgnoreCase("Sec-WebSocket-Accept"))
						{
						  try
						  {
							ByteArrayInputStream bais = new ByteArrayInputStream(serverHandshake.getFieldValue(h).getBytes(StandardCharsets.UTF_8));
							byte[] buf = new BASE64Decoder().decodeBuffer(bais);
							System.out.print("Server Key:"); //  + new String(buf));
							for (int i=0; i<buf.length; i++)
							  System.out.print("[" + lpad(Integer.toHexString(buf[i] & 0xFF), "0", 2) + "]");
							System.out.println();
						  }
						  catch (Exception ex)
						  {
							ex.printStackTrace();
						  }
						}
						*/
					}
				}

				@Override
				public void onMessage(String string) {
					try {
						JSONObject obj = new JSONObject(string);
						String ack = obj.getString("ack");
						String mess = obj.getString("mess");
						// TODO Display on the small oled screen
						System.out.println(ack + ":" + mess);
					} catch (Exception ex) {
						System.out.println("WS message:" + string);
					}
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					System.out.println("WS Connection closed");
				}

				@Override
				public void onError(Exception exception) {
					System.out.println("WS Error:" + exception.toString());
				}
			};
			webSocketClient.connect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) throws Exception {
		System.out.println(args.length + " parameter(s).");
		initWebSocketConnection(wsUri);
		LevelAndTemperature lat = new LevelAndTemperature();
		/**
		 * This is the list of the ADC channels to listen to.
		 */
		MCPReader.MCP3008InputChannels[] listening2 = new MCPReader.MCP3008InputChannels[]
				{
						MCPReader.MCP3008InputChannels.CH0,
						MCPReader.MCP3008InputChannels.CH1,
						MCPReader.MCP3008InputChannels.CH2,
						MCPReader.MCP3008InputChannels.CH3,
						MCPReader.MCP3008InputChannels.CH4,
						MCPReader.MCP3008InputChannels.CH5,
						MCPReader.MCP3008InputChannels.CH6
				};

		final ADCChannels_1_to_8 sac = new ADCChannels_1_to_8(listening2, lat);
		final BMP180 bmp180 = new BMP180();

		final Thread tempReader = new Thread() {
			public void run() {
				float temp = 0;
				float originalTemp = 0;
				try {
					originalTemp = bmp180.readTemperature();
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
					ex.printStackTrace();
				}
				System.out.println(">>> Original Temperature :" + NF.format(originalTemp) + "\272C");
				while (keepReadingTemperature()) {
					try {
						temp = bmp180.readTemperature();
//            System.out.println(">>> Temperature is now :" + NF.format(temp) + "\272C");
						if (Math.abs(temp - originalTemp) > 0.25f) {
							if (temp > originalTemp) {
								System.out.println(">>> Warning! >>> " + SDF.format(new Date()) + ", Temperature is rising, now " + NF.format(temp) + "\272C");
								JSONObject obj = new JSONObject();
								obj.put("temperature", temp);
								if (webSocketClient != null) {
									System.out.println("Sending " + obj.toString());
									webSocketClient.send(obj.toString());
								} else
									System.out.println("No websocket client (for temperature)");
							} else
								System.out.println("                 Setting base temperature to " + NF.format(temp) + "\272C");
							originalTemp = temp;
						}
					} catch (Exception ex) {
						System.err.println(ex.getMessage());
						ex.printStackTrace();
					}
				}
				System.out.println("Done with the temperature.");
			}
		};
		tempReader.start();

		final Thread me = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println();
			setReadTemperature(false); // Exit the temperature thread
			sac.quit();                // Stop observing the level
			if (webSocketClient != null)
				webSocketClient.close();
			else
				System.out.println("No websocket client to close");
			synchronized (me) {
				me.notify();
			}
			System.out.println("Program stopped by user's request.");
			try {
				Thread.sleep(1_000L);
			} catch (Exception ex) {
			} // Wait a bit for everything to shutdown cleanly...
		}, "Shutdown Hook"));
		synchronized (me) {
			System.out.println("Main thread waiting...");
			me.wait();
		}
		System.out.println("Done.");
	}
}
