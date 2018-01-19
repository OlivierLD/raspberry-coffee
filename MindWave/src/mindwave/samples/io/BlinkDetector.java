package mindwave.samples.io;

import gnu.io.CommPortIdentifier;
import mindwave.MindWaveCallbacks;
import mindwave.MindWaveController;
import mindwave.SerialCommunicatorInterface;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlinkDetector
		implements SerialIOCallbacks,
		MindWaveCallbacks,
		SerialCommunicatorInterface {
	private List<Short> wave = new ArrayList<Short>();
	private final static int WIDTH = 1_000;
	private final static int SMOOTH_WIDTH = 50;

	@Override
	public void connected(boolean b) {
		System.out.println("MindWave connected: " + b);
	}

	private int lenToRead = 0;
	private int bufferIdx = 0;
	private byte[] serialBuffer = new byte[256];

	@Override
	public void onSerialData(byte b) {
		//  System.out.println("Received character [0x" + Integer.toHexString(b & 0xFF) + "]");
		serialBuffer[bufferIdx++] = (byte) (b & 0xFF);
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
	}

	@Override
	public void mindWaveDisconnected(MindWaveController.DeviceID did) {
		System.out.println("Disconnected from Device ID: 0x" +
				StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0"));
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

	private final static int BLINK_ABOVE = 150;
	private final static int NO_BLINK_BELOW = 100;
	private boolean blinked = false;
	private boolean started = false;

	@Override
	public void mindWaveRawWave(MindWaveController.RawWave rw) {
//  System.out.println("Raw Wave value:" + rw.getValue());
		wave.add(rw.getValue());
//  System.out.println("Size " + wave.size());
		while (wave.size() > WIDTH)
			wave.remove(0);

		// Smooth
		if (wave.size() > 500) {
			if (!started) {
				System.out.println("\n-- Detection started");
				started = true;
			}

			if (false) {
				int smoothWidth = SMOOTH_WIDTH;
				double[] smoothed = new double[wave.size()];
				for (int i = 0; i < wave.size(); i++) {
					double d = 0;
					int k = 0;
					for (int j = i - (smoothWidth / 2); j < (i + (smoothWidth / 2)); j++) {
						k = j;
						if (k < 0) k = 0;
						if (k >= wave.size()) k = wave.size() - 1;
						d += wave.get(k);
					}
					smoothed[i] = d / smoothWidth;
				}
			}
			double lastValue = wave.get(wave.size() - 1); // smoothed[smoothed.length - 1];
//    System.out.println("Last Smoothed value:" + lastValue);
			if (lastValue > BLINK_ABOVE && !blinked) {
				blinked = true;
				System.out.println("\t\t>>> Blink! (" + lastValue + ")");
			}
			if (blinked && lastValue < NO_BLINK_BELOW)
				blinked = false;
		} else
			System.out.print(".");
	}

	@Override
	public void mindWavePoorSignal(MindWaveController.PoorSignal ps) {
		System.out.println("Poor signal:" + ps.getVal() + "/255");
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
	}

	@Override
	public void mindWaveMeditation(MindWaveController.Meditation med) {
		System.out.println("Meditation:" + med.getVal() + "/255");
	}

	@Override
	public void mindWaveAsicEegPower(MindWaveController.AsicEegPower aep) {
		System.out.print("AsicEegPower: ");
		int[] values = aep.getValues();
		for (int v : values)
			System.out.print(v + " ");
		System.out.println();
	}

	@Override
	public void mindWaveUnknowType(byte t) {
		System.out.println("Unknown type [" + StringUtils.lpad(Integer.toHexString(t & 0xFF), 2, "0") + "]");
	}

	@Override
	public void mindWaveError(Throwable t) {
//  System.out.println(t.toString());
//  t.printStackTrace();
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

	private static SerialCommunicator sc = null;
	private static MindWaveController mwc = null;

	private static void parseParameters(String... args) {
		for (String prm : args) {
			if (LIST_PORTS_PRFX.equals(prm))
				listPort = true;
			if (prm.startsWith(PORT_NAME_PRFX))
				serialPort = prm.substring(PORT_NAME_PRFX.length());
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

	private static String serialPort = "COM25";
	private static int baudRate = 115200;
	private static boolean listPort = false;

	public static void main(String... args) {
		parseParameters(args);

		BlinkDetector mwClient = new BlinkDetector();
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
				mwc.disconnectHeadSet();
				try {
					sc.disconnect();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				synchronized (waiter) {
					System.out.println("User Interrupted.");
					waiter.notify();
				}
			}
		});

		CommPortIdentifier mwPort = pm.get(serialPort);
		try {
			sc.connect(mwPort, "MindWave", baudRate);
			boolean b = sc.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			sc.initListener();

			System.out.println("Connecting Headset");
			mwc.connectHeadSet();

			synchronized (waiter) {
				waiter.wait();
				System.out.println("Yo!");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
