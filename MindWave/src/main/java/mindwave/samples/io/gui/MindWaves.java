package mindwave.samples.io.gui;

import gnu.io.CommPortIdentifier;
import mindwave.MindWaveCallbacks;
import mindwave.MindWaveController;
import mindwave.SerialCommunicatorInterface;
import mindwave.samples.io.gui.ctx.MindWaveContext;
import mindwave.samples.io.gui.ctx.MindWaveListener;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.StringUtils;

import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class MindWaves
		implements SerialIOCallbacks, MindWaveCallbacks, SerialCommunicatorInterface {
	private final static String replay = System.getProperty("replay.serial");
	private MindWaveFrame frame;

	public MindWaves() {
		frame = new MindWaveFrame(this);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		MindWaveContext.getInstance().addListener(new MindWaveListener() {
			@Override
			public void connect(String port, int br) {
				serialConnect(port, br);
			}

			@Override
			public void disconnect() {
				serialDisconnect();
			}
		});
	}

	public void setPortList(Map<String, CommPortIdentifier> pm) {
		frame.setPortList(pm);
	}

	@Override
	public void connected(boolean b) {
		System.out.println("MindWave connected: " + b);
	}

	private int lenToRead = 0;
	private int bufferIdx = 0;
	private byte[] serialBuffer = new byte[256];

	@Override
	public void onSerialData(byte b) {
		if (((byte) (b & 0xFF)) == MindWaveController.SYNC && bufferIdx > 2)
			bufferIdx = 0;

		//  System.out.println("Received character [0x" + Integer.toHexString(b & 0xFF) + "]");
		serialBuffer[bufferIdx++] = (byte) (b & 0xFF);
		// Log
		if (bufferIdx > 0) {
			String s = "";
			for (int i = 0; i < bufferIdx; i++) {
				s += (StringUtils.lpad(Integer.toHexString(serialBuffer[i] & 0xFF).toUpperCase(), 2, "0") + " ");
			}
			MindWaveContext.getInstance().fireSerialData(s);
		}
		// Log - end

		if (bufferIdx == 1 && serialBuffer[0] != MindWaveController.SYNC) {
			bufferIdx = 0;
		} else if (bufferIdx == 2 && (serialBuffer[0] != MindWaveController.SYNC || serialBuffer[1] != MindWaveController.SYNC)) {
			bufferIdx = 0;
		} else if (bufferIdx == 3) {
			lenToRead = serialBuffer[2];
		} else if (bufferIdx > 3 && bufferIdx == (lenToRead + 3 + 1)) { // 3: Payload start (2 + 1), 1: ChkSum
			// Message completed
			final byte[] mess = new byte[bufferIdx];
			System.arraycopy(mess, 0, serialBuffer, 0, mess.length);
			//  for (int i=0; i<bufferIdx; i++)
			//    mess[i] = serialBuffer[i];
			MindWaveContext.getInstance().fireParsing(mess);
			Thread messProcessor = new Thread(() -> mwc.mwOutput(mess));
			messProcessor.start();
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
		String mess = "Connected to Device ID: 0x" +
				StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0");
		System.out.println(mess);
		MindWaveContext.getInstance().fireMindWaveStatus(mess);
	}

	@Override
	public void mindWaveDisconnected(MindWaveController.DeviceID did) {
		String mess = "Disconnected from Device ID: 0x" +
				StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0");
		System.out.println(mess);
		MindWaveContext.getInstance().fireMindWaveStatus(mess);
	}

	@Override
	public void mindWaveStandby(MindWaveController.StbyStatus ss) {
		String mess = (ss.getStatus() == MindWaveController.STBY_STATUS_STBY ? "Stand By" :
				(ss.getStatus() == MindWaveController.STBY_STATUS_TRYING ? "Trying..." : "Unknown"));
		System.out.println("Status:" + mess);
		MindWaveContext.getInstance().fireMindWaveStatus(mess);
	}

	@Override
	public void mindWaveAccessDenied() {
		System.out.println("Access denied");
		MindWaveContext.getInstance().fireMindWaveStatus("Access denied");
	}

	@Override
	public void mindWaveNotFound() {
		System.out.println("Not found");
		MindWaveContext.getInstance().fireMindWaveStatus("Not found");
	}

	@Override
	public void mindWaveRawWave(MindWaveController.RawWave rw) {
//  System.out.println("Raw Wave value:" + rw.getValue());
		MindWaveContext.getInstance().fireAddRawData(rw.getValue());
	}

	@Override
	public void mindWavePoorSignal(MindWaveController.PoorSignal ps) {
		if (ps.getVal() != 0)
			System.out.println("Poor signal: noise=" + ps.getVal() + "/255");
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
//  System.out.println("Attention:" + att.getVal() + "/100");
		MindWaveContext.getInstance().fireAttention(att.getVal());
	}

	@Override
	public void mindWaveMeditation(MindWaveController.Meditation med) {
//  System.out.println("Meditation:" + med.getVal() + "/100");
		MindWaveContext.getInstance().fireRelaxation(med.getVal());
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
		System.err.println(t.toString());
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
	private static Map<String, CommPortIdentifier> pm = null;

	private void serialConnect(String port, int br) {
		try {
			if (replay == null) {
				System.out.println("(Serial) Connecting...");
				sc.connect(pm.get(port), "MindWave", br);
				boolean b = sc.initIOStream();
				System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
				sc.initListener();
			} else {
				BufferedReader reader = new BufferedReader(new FileReader(replay));
				StringBuffer fullData = new StringBuffer();
				String line = "";
				while (line != null) {
					line = reader.readLine();
					if (line != null)
						fullData.append(line);
				}
				reader.close();
				String bigLine = fullData.toString();
				final String[] bytes = bigLine.split(" ");
				Thread dataReplay = new Thread() {
					public void run() {
						for (String b : bytes) {
							byte bt = (byte) Integer.parseInt(b, 16);
							onSerialData(bt);
							try {
								Thread.sleep(10L);
							} catch (InterruptedException ie) {
							}
						}
						System.out.println("Data Replay completed.");
					}
				};
				dataReplay.start();
			}
			MindWaveContext.getInstance().fireSerialConnected();
			// Connect headset...
			mwc.connectHeadSet();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("(Serial) ...NOT connected");
			MindWaveContext.getInstance().fireSerialDisconnected();
		}
	}

	private void serialDisconnect() {
		try {
			sc.disconnect();
			mwc.disconnectHeadSet();
			MindWaveContext.getInstance().fireSerialDisconnected();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			MindWaveContext.getInstance().fireSerialConnected();
		}
	}

	public static void main(String... args) {
		try {
			if (System.getProperty("swing.defaultlaf") == null)
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		MindWaves mwClient = new MindWaves();
		sc = new SerialCommunicator(mwClient);

		mwc = new MindWaveController(mwClient, mwClient);
		MindWaveController.setVerbose("true".equals(System.getProperty("mindwave.verbose", "false")));

		pm = sc.getPortList();
		mwClient.setPortList(pm);
	}
}
