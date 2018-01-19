package mindwave.samples.io.ws;

import gnu.io.CommPortIdentifier;
import mindwave.MindWaveCallbacks;
import mindwave.MindWaveController;
import mindwave.SerialCommunicatorInterface;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public class WebSocketFeeder
		implements SerialIOCallbacks,
		MindWaveCallbacks,
		SerialCommunicatorInterface {
	@Override
	public void connected(boolean b) {
		System.out.println("MindWave connected: " + b);
		JSONObject json = new JSONObject();
		json.put("connected", b);
		webSocketClient.send(json.toString());
	}

	private int lenToRead = 0;
	private int bufferIdx = 0;
	private byte[] serialBuffer = new byte[256];

	@Override
	public void onSerialData(byte b) {
		//  System.out.println("Received character [0x" + Integer.toHexString(b & 0xFF) + "]");
		serialBuffer[bufferIdx++] = (byte) (b & 0xFF);
		if (logSerial) {
			try {
				log.write(StringUtils.lpad(Integer.toHexString(b & 0xFF).toUpperCase(), 2, "0") + " ");
				log.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		if (bufferIdx == 1 && serialBuffer[0] != MindWaveController.SYNC)
			bufferIdx = 0;
		if (bufferIdx == 2 && (serialBuffer[0] != MindWaveController.SYNC || serialBuffer[1] != MindWaveController.SYNC))
			bufferIdx = 0;
		if (bufferIdx == 3)
			lenToRead = serialBuffer[2];
		if (bufferIdx > 3 && bufferIdx == (lenToRead + 3 + 1)) // 3: Payload start, 1: ChkSum
		{
			// Message completed
			byte[] mess = new byte[bufferIdx];
			for (int i = 0; i < bufferIdx; i++)
				mess[i] = serialBuffer[i];
			mwc.mwOutput(mess);
			// Reset
			lenToRead = 0;
			bufferIdx = 0;
		}
	}

	@Override
	public void onSerialData(byte[] b, int len) {
	}

	@Override
	public void mindWaveConnected(MindWaveController.DeviceID did) {
		System.out.println("Connected to Device ID: 0x" +
				StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0"));
		JSONObject json = new JSONObject();
		json.put("mindwave-connected", true);
		json.put("device-id", StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0"));
		webSocketClient.send(json.toString());
	}

	@Override
	public void mindWaveDisconnected(MindWaveController.DeviceID did) {
		System.out.println("Disconnected from Device ID: 0x" +
				StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0"));
		JSONObject json = new JSONObject();
		json.put("mindwave-connected", false);
		json.put("device-id", StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0"));
		webSocketClient.send(json.toString());
	}

	@Override
	public void mindWaveStandby(MindWaveController.StbyStatus ss) {
		System.out.println("Status:" +
				(ss.getStatus() == MindWaveController.STBY_STATUS_STBY ? "Stand By" :
						(ss.getStatus() == MindWaveController.STBY_STATUS_TRYING ? "Trying..." : "Unknown")));
	}

	@Override
	public void mindWaveAccessDenied() {
		System.out.println("Access denied");
	}

	@Override
	public void mindWaveNotFound() {
		System.out.println("Not found");
	}

	@Override
	public void mindWaveRawWave(MindWaveController.RawWave rw) {
//  System.out.println("Raw Wave value:" + rw.getValue());
		JSONObject json = new JSONObject();
		json.put("raw", rw.getValue());
		webSocketClient.send(json.toString());
	}

	@Override
	public void mindWavePoorSignal(MindWaveController.PoorSignal ps) {
//  System.out.println("Poor signal:" + ps.getVal() + "/255");
		JSONObject json = new JSONObject();
		json.put("noise", ps.getVal());
		webSocketClient.send(json.toString());
	}

	@Override
	public void mindWaveBatteryLevel(MindWaveController.BatteryLevel bl) {
		System.out.println("Battery Level:" + bl.getVal() + "/255");
	}

	@Override
	public void mindWaveHeartRate(MindWaveController.HeartRate hr) {
		System.out.println("Heart Rate:" + hr.getVal() + "/255");
	}

	@Override
	public void mindWave8BitRaw(MindWaveController.EightBitRaw ebr) {
		System.out.println("8-bit raw signal:" + ebr.getVal() + "/255");
	}

	@Override
	public void mindWaveRawMarker(MindWaveController.RawMarker rm) {
		System.out.println("Raw Marker:" + rm.getVal() + "/255");
	}

	@Override
	public void mindWaveAttention(MindWaveController.Attention att) {
		System.out.println("Attention:" + att.getVal() + "/255");
		JSONObject json = new JSONObject();
		json.put("attention", att.getVal());
		webSocketClient.send(json.toString());
	}

	@Override
	public void mindWaveMeditation(MindWaveController.Meditation med) {
		System.out.println("Meditation:" + med.getVal() + "/255");
		JSONObject json = new JSONObject();
		json.put("meditation", med.getVal());
		webSocketClient.send(json.toString());
	}

	@Override
	public void mindWaveAsicEegPower(MindWaveController.AsicEegPower aep) {
		System.out.print("AsicEegPower: ");
		int[] values = aep.getValues();
		for (int v : values)
			System.out.print(v + " ");
		System.out.println();
		JSONObject json = new JSONObject();
		JSONObject aeg = new JSONObject();
		json.put("aeg", aeg);
		int i = 0;
		for (int v : values)
			aeg.put(MindWaveController.AsicEegPower.WAVE_NAMES[i++], v);
		webSocketClient.send(json.toString());
	}

	@Override
	public void mindWaveUnknowType(byte t) {
		System.out.println("Unknown type [" + StringUtils.lpad(Integer.toHexString(t & 0xFF), 2, "0") + "]");
	}

	@Override
	public void mindWaveError(Throwable t) {
		t.printStackTrace();
	}

	@Override
	public boolean isSerialOpen() {
		return sc.isConnected();
	}

	@Override
	public void writeSerial(byte b) {
		try {
			sc.writeData(b);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void flushSerial() {
		try {
			sc.flushSerial();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static SerialCommunicator sc = null; // SerialIOCallbacks
	private static MindWaveController mwc = null;

	private static void parseParameters(String... args) {
		for (String prm : args) {
			if (LIST_PORTS_PRFX.equals(prm))
				listPort = true;
			if (prm.startsWith(PORT_NAME_PRFX))
				serialPort = prm.substring(PORT_NAME_PRFX.length());
			if (prm.startsWith(LOG_PRFX))
				logSerial = "true".equals(prm.substring(LOG_PRFX.length()));
			if (prm.startsWith(BAUD_RATE_PRFX)) {
				String brStr = prm.substring(BAUD_RATE_PRFX.length());
				try {
					baudRate = Integer.parseInt(brStr);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
	}

	private final static String LIST_PORTS_PRFX = "-list-ports";
	private final static String PORT_NAME_PRFX = "-port:";
	private final static String BAUD_RATE_PRFX = "-br:";
	private final static String LOG_PRFX = "-log:";

	private static String serialPort = "COM25";
	private static int baudRate = 115200;
	private static boolean listPort = false;
	private static boolean logSerial = false;

	private static BufferedWriter log = null;

	private static WebSocketClient webSocketClient = null;

	private static void initWebSocketConnection(String serverURI) {
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) // , (Draft) null)
			{
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("WS onOpen");
				}

				@Override
				public void onMessage(String string) {
					// TODO Implement this method
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					// TODO Implement this method
				}

				@Override
				public void onError(Exception exception) {
					// TODO Implement this method
				}
			};
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) {
		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
		System.out.println("Connecting on " + wsUri);
		initWebSocketConnection(wsUri);
		webSocketClient.connect();

		final String replay = System.getProperty("replay.serial");

		parseParameters(args);
		if (logSerial) {
			try {
				log = new BufferedWriter(new FileWriter("serial.log"));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		final WebSocketFeeder mwClient = new WebSocketFeeder();
		sc = new SerialCommunicator(mwClient);

		mwc = new MindWaveController(mwClient, mwClient);
		Map<String, CommPortIdentifier> pm = sc.getPortList();

		if (listPort) {
			Set<String> ports = pm.keySet();
			for (String port : ports)
				System.out.println("-> " + port);
			System.exit(0);
		}
		final Thread waiter = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (replay == null) {
					mwc.disconnectHeadSet();
					try {
						sc.disconnect();
						if (logSerial)
							log.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				webSocketClient.close();
				synchronized (waiter) {
					System.out.println("User Interrupted.");
					waiter.notify();
				}
			}
		});

		CommPortIdentifier mwPort = pm.get(serialPort);
		try {
			if (replay == null) {
				sc.connect(mwPort, "MindWave", baudRate);
				boolean b = sc.initIOStream();
				System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
				sc.initListener();
			} else {
				BufferedReader br = new BufferedReader(new FileReader(replay));
				StringBuffer fullData = new StringBuffer();
				String line = "";
				while (line != null) {
					line = br.readLine();
					if (line != null)
						fullData.append(line);
				}
				br.close();
				String bigLine = fullData.toString();
				final String[] bytes = bigLine.split(" ");
				Thread dataReplay = new Thread() {
					public void run() {
						for (String b : bytes) {
							byte bt = (byte) Integer.parseInt(b, 16);
							mwClient.onSerialData(bt);
							try {
								Thread.sleep(10L);
							} catch (InterruptedException ie) {
							}
						}
					}
				};
				dataReplay.start();
			}
			System.out.println("Connecting Headset");
			mwc.connectHeadSet();

			synchronized (waiter) {
				waiter.wait();
				System.out.println("Yo!");
			}

//      mwc.disconnectHeadSet();
//
//      sc.disconnect();
			System.out.println("Done.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
