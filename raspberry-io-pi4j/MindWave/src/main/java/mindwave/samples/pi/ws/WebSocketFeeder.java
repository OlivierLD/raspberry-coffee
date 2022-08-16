package mindwave.samples.pi.ws;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import mindwave.MindWaveCallbacks;
import mindwave.MindWaveController;
import mindwave.SerialCommunicatorInterface;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import utils.StringUtils;

import java.io.IOException;
import java.net.URI;

public class WebSocketFeeder implements MindWaveCallbacks,
		SerialCommunicatorInterface {
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
		System.out.println("Attention:" + att.getVal() + "/100");
		JSONObject json = new JSONObject();
		json.put("attention", att.getVal());
		webSocketClient.send(json.toString());
	}

	@Override
	public void mindWaveMeditation(MindWaveController.Meditation med) {
		System.out.println("Meditation:" + med.getVal() + "/100");
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
		if (verbose)
			t.printStackTrace();
		else
			System.err.println(t.toString());
	}

	@Override
	public boolean isSerialOpen() {
		return serial.isOpen();
	}

	@Override
	public void writeSerial(byte b) {
		try {
			serial.write(b);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public void flushSerial() {
		try {
			serial.flush();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void closeAll(MindWaveController mwc) throws IOException {
		mwc.disconnectHeadSet();
		while (mwc.isConnected())
			MindWaveController.delay(1f);
		System.out.println("Disconnected. Done");

		stopReading();
		closeSerial();
	}

	private final static Serial serial = SerialFactory.createInstance(); // PI4J Serial manager

	private static boolean verbose = "true".equals(System.getProperty("mindwave.verbose", "false"));

	public static void setVerbose(boolean b) {
		verbose = b;
	}

	public static boolean getVerbose() {
		return verbose;
	}

	private static boolean readSerial = true;

	public static boolean keepReading() {
		return readSerial;
	}

	public void stopReading() {
		readSerial = false;
	}

	public void closeSerial() throws IOException {
		serial.close();
	}

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

	public static void main(String... args) throws IOException {
		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
		System.out.println("Connecting on " + wsUri);
		initWebSocketConnection(wsUri);
		webSocketClient.connect();

		final WebSocketFeeder c1 = new WebSocketFeeder();
		final MindWaveController mwc = new MindWaveController(c1, c1);
		System.out.println("Connection...");

		serial.open("/dev/ttyUSB0", 115_200);

		Thread serialReader = new Thread() {
			private byte[] serialBuffer = new byte[256];

			public void run() {
				int lenToRead = 0;
				int bufferIdx = 0;
				while (keepReading()) {
					try {
						while (serial.available() > 0) {
							char c = (char) 0; // TODO Fix that serial.read();
							c &= 0xFF;
							serialBuffer[bufferIdx++] = (byte) c;
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
					} catch (IllegalStateException ise) {
						ise.printStackTrace();
					} catch (Exception ex) {
						c1.mindWaveError(ex);
						//      System.err.println("Serial Thread:" + ex.toString());
						lenToRead = 0;
						bufferIdx = 0;
					}
				}
			}
		};
		serialReader.start();

		final Thread waiter = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (waiter) {
				// Hanging up.
				try {
					c1.closeAll(mwc);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				webSocketClient.close();
				waiter.notify();
			}
			System.out.println("Released Waiter...");
		}, "Shutdown Hook"));

//  short hID = (short)0x9228;
//  mwc.connectHeadSet(hID);
		mwc.connectHeadSet();

		while (!mwc.isConnected())
			MindWaveController.delay(1f);
		System.out.println("Connected!");

		// Some time to live here
		synchronized (waiter) {
			try {
				waiter.wait();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			System.out.println("Waiter released.");
		}
	}
}
