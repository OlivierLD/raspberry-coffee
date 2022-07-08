package mindwave;

import utils.DumpUtil;
import utils.StringUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * See http://developer.neurosky.com/docs/doku.php?id=thinkgear_communications_protocol#data_payload
 */
public class MindWaveController {
	private final static float BETWEEN_SENT_CHAR = 0.001F;

	private final static byte CONNECT = (byte) 0xC0;
	private final static byte DISCONNECT = (byte) 0xC1;
	private final static byte AUTO_CONNECT = (byte) 0xC2;

	/*
	 * ThinkGear packet structure
	 * [SYNC] [SYNC] [PLEN] [PAYLOAD...] [CHKSUM]
	 * --------------------
	 * <-----(Header)----->
	 *
	 */
	public final static byte SYNC = (byte) 0xAA;
	private final static byte FOUND_AND_CONNECTED = (byte) 0xD0;
	private final static byte NOT_FOUND = (byte) 0xD1;
	private final static byte DISCONNECTED = (byte) 0xD2;
	private final static byte DENIED = (byte) 0xD3;
	private final static byte STBY = (byte) 0xD4;

	public final static byte STBY_STATUS_STBY = 0x00;
	public final static byte STBY_STATUS_TRYING = 0x01;

	// Single-byte codes
	private final static byte BATTERY_LEVEL = 0x01;
	private final static byte POOR_SIGNAL = 0x02;
	private final static byte HEART_RATE = 0x03;
	private final static byte ATTENTION = 0x04;
	private final static byte MEDITATION = 0x05;
	private final static byte _8BIT_RAW = 0x06;
	private final static byte RAW_MARKER = 0x07;

	// Multi-bytre codes
	private final static byte RAW_WAVE_VALUE = (byte) 0x80;
	private final static byte EEG_POWER = (byte) 0x81;
	private final static byte ASIC_EEG_POWER = (byte) 0x83;
	private final static byte RRINTERVAL = (byte) 0x86;

	public final static short EYE_BLINK_THRESHOLD = 150;

	private static boolean verbose = "true".equals(System.getProperty("mindwave.verbose", "false"));

	public static void setVerbose(boolean b) {
		verbose = b;
	}

	public static boolean getVerbose() {
		return verbose;
	}

	public final static void delay(float delay) {
		try { Thread.sleep(Math.round(delay * 1_000L)); } catch (InterruptedException ie) {}
	}

	private static boolean connectionEstablished = false;

	private MindWaveCallbacks parent = null;
	private SerialCommunicatorInterface sci = null;

	public MindWaveController(MindWaveCallbacks caller,
	                          SerialCommunicatorInterface serial) {
		this.parent = caller;
		this.sci = serial;
	}

	public static byte thinkGearChecksum(byte[] message) {
		byte cs = (byte) 0xFF;
		if (message.length < 4) {
			throw new RuntimeException("Payload too small");
		}
		if (message[0] != SYNC || message[1] != SYNC) {
			throw new RuntimeException("Payload does not start with SYNC-SYNC");
		}
		int payloadStart = 3;
		int payloadEnd = message.length - 1;
		int checkSum = 0;
		for (int i = payloadStart; i < payloadEnd; i++) {
			checkSum += message[i];
		}
		checkSum &= 0xFF;
//  cs = (byte)(0xFF - checkSum);
		cs = (byte) (~checkSum);
		return cs;
	}

	public boolean isConnected() {
		return connectionEstablished;
	}

	private void sendToMindWave(String payload) {
		sendToMindWave(payload.getBytes(), true);
	}

	private void sendToMindWave(byte[] payload, boolean withCR) {
		if (sci.isSerialOpen()) {
			try {
				if (getVerbose()) {
					System.out.println("Writing to MindWave (" + payload.length + " ch)");
					try {
						String[] sa = DumpUtil.dualDump(payload);
						if (sa != null) {
							System.out.println("\t>>> [Sent to MindWave]:");
							for (String s : sa) {
								System.out.println("\t\t" + s);
							}
						}
					} catch (Exception ex) {
						System.out.println(ex.toString());
					}
				}
        /* See below...
        if (withCR)
          serial.writeln(payload);
        else
          serial.write(payload);
        */
				for (int i = 0; i < payload.length; i++) {
					sci.writeSerial(payload[i]);
					delay(BETWEEN_SENT_CHAR);                     // << The MOST important trick here
				}
				if (withCR) {
					sci.writeSerial((byte) '\n');
					delay(BETWEEN_SENT_CHAR);
				}
				sci.flushSerial();
			} catch (IllegalStateException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void connectHeadSet() {
		delay(3f);
		sendToMindWave(new byte[]{AUTO_CONNECT}, false);
	}

	public void connectHeadSet(short headsetId) {
		byte b1 = (byte) ((headsetId & 0xFF00) >> 8);
		byte b2 = (byte) ((headsetId & 0xFF));
		delay(3f);
		sendToMindWave(new byte[]{CONNECT, b1, b2}, false);
	}

	public void disconnectHeadSet() {
		delay(3f);
		sendToMindWave(new byte[]{DISCONNECT}, false);
	}

	public static boolean isSingleByteCode(byte type) {
//    return (type == BATTERY_LEVEL ||
//            type == POOR_SIGNAL ||
//            type == HEART_RATE ||
//            type == ATTENTION ||
//            type == MEDITATION ||
//            type == _8BIT_RAW ||
//            type == RAW_MARKER);
		return (type >= 0x00 && type <= 0x7F);
	}

	/*
	 * Data received from MindWave, parse them
	 */
	public void mwOutput(byte[] mess) {
		if (getVerbose()) {
			try {
				String[] sa = DumpUtil.dualDump(mess);
				if (sa != null) {
					System.out.println("\t>>> [MindWave] Received:");
					for (String s : sa)
						System.out.println("\t\t" + s);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// Now parse
		try {
			byte cs = thinkGearChecksum(mess);
			byte lastByte = mess[mess.length - 1];
			if (cs == lastByte) {
				// Message is fine, parsing.
				List<Object> objLst = new ThinkGearParser(mess).getParsed();
				for (Object obj : objLst) {
					if (obj instanceof ConnectedDeviceID) {
						DeviceID id = (DeviceID) obj;
						this.parent.mindWaveConnected(id);
					} else if (obj instanceof DisconnectedDeviceID) {
						DeviceID id = (DeviceID) obj;
						this.parent.mindWaveDisconnected(id);
					} else if (obj instanceof StbyStatus) {
						StbyStatus ss = (StbyStatus) obj;
						this.parent.mindWaveStandby(ss);
					} else if (obj instanceof AccessDenied) {
						this.parent.mindWaveAccessDenied();
					} else if (obj instanceof NotFound) {
						this.parent.mindWaveNotFound();
					} else if (obj instanceof RawWave) {
						RawWave rw = (RawWave) obj;
						this.parent.mindWaveRawWave(rw);
					} else if (obj instanceof PoorSignal) {
						PoorSignal ps = (PoorSignal) obj;
						this.parent.mindWavePoorSignal(ps);
					} else if (obj instanceof BatteryLevel) {
						BatteryLevel bl = (BatteryLevel) obj;
						this.parent.mindWaveBatteryLevel(bl);
					} else if (obj instanceof HeartRate) {
						HeartRate hr = (HeartRate) obj;
						this.parent.mindWaveHeartRate(hr);
					} else if (obj instanceof EightBitRaw) {
						EightBitRaw ebr = (EightBitRaw) obj;
						this.parent.mindWave8BitRaw(ebr);
					} else if (obj instanceof RawMarker) {
						RawMarker rm = (RawMarker) obj;
						this.parent.mindWaveRawMarker(rm);
					} else if (obj instanceof Attention) {
						Attention att = (Attention) obj;
						this.parent.mindWaveAttention(att);
					} else if (obj instanceof Meditation) {
						Meditation med = (Meditation) obj;
						this.parent.mindWaveMeditation(med);
					} else if (obj instanceof AsicEegPower) {
						AsicEegPower aep = (AsicEegPower) obj;
						this.parent.mindWaveAsicEegPower(aep);
					} else if (obj instanceof UnknownType) {
						UnknownType ut = (UnknownType) obj;
						this.parent.mindWaveUnknowType(ut.getType());
					} else
						System.out.println("What?");
				}
			} else {
				this.parent.mindWaveError(new RuntimeException("Bad Checksum for [" + DumpUtil.dumpHexMess(mess) + "]"));
				if (getVerbose()) {
					System.out.println(">> Bad checksum for");
					try {
						String[] sa = DumpUtil.dualDump(mess);
						if (sa != null) {
							for (String s : sa)
								System.out.println("\t\t" + s);
						}
					} catch (Exception ex) {
						this.parent.mindWaveError(ex);
						//     ex.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
			this.parent.mindWaveError(ex);
//    ex.printStackTrace();
		}
	}

	public static class ThinkGearParser {
		private List<Object> parsed = null;

		public ThinkGearParser(byte[] ba) {
			parsed = parse(ba);
		}

		// Assume checksum has been checked
		public List<Object> parse(byte[] message) {
//    System.out.println("Parsing [" + DumpUtil.dumpHexMess(message) + "]");

			List<Object> returned = new ArrayList<Object>();
			byte len = message[2];
			byte[] payload = new byte[len];
			for (int i = 0; i < len; i++)
				payload[i] = message[3 + i];

//    System.out.println("Parsing payload [" + DumpUtil.dumpHexMess(payload) + "]");
			// Loop on all the codes in the payload here
			int subMessOffset = 0;
			boolean readCompleted = false;
			while (!readCompleted) {
				byte type = (byte) (payload[subMessOffset] & 0xFF);
				byte payloadLen = 0;
				int offset = 0;
				if (isSingleByteCode(type)) {
					payloadLen = 1;
					offset = -1;
				} else {
					payloadLen = payload[subMessOffset + 1];
				}
				byte[] rowPayload = null;
				if (payloadLen > 0) {
					rowPayload = new byte[payloadLen];
					for (int i = 0; i < payloadLen; i++)
						rowPayload[i] = payload[i + subMessOffset + 2 + offset];
				}
				if (false && rowPayload != null)
					System.out.println("Parsing rowPayload [" + DumpUtil.dumpHexMess(rowPayload) + "]");

//      System.out.println("Parsing payload type [" + DumpUtil.lpad(Integer.toHexString(type & 0xFF), 2, "0") + "]");
				switch (type) {
					case FOUND_AND_CONNECTED:
						//    System.out.println("Found and Connected");
						returned.add(new ConnectedDeviceID(rowPayload));
						connectionEstablished = true;
						break;
					case NOT_FOUND:
						//    System.out.println("Not Found");
						returned.add(new NotFound());
						break;
					case DISCONNECTED:
						//    System.out.println("Device disconnected");
						returned.add(new DisconnectedDeviceID(rowPayload));
						connectionEstablished = false;
						break;
					case DENIED:
						//    System.out.println("Device access denied");
						returned.add(new AccessDenied());
						break;
					case STBY:
						//    System.out.println("Stand by");
						returned.add(new StbyStatus(rowPayload));
						break;
					case RAW_WAVE_VALUE:
						returned.add(new RawWave(rowPayload));
						break;
					case BATTERY_LEVEL:
						returned.add(new BatteryLevel(rowPayload));
						break;
					case POOR_SIGNAL:
						returned.add(new PoorSignal(rowPayload));
						break;
					case HEART_RATE:
						returned.add(new HeartRate(rowPayload));
						break;
					case ATTENTION:
						returned.add(new Attention(rowPayload));
						break;
					case MEDITATION:
						returned.add(new Meditation(rowPayload));
						break;
					case _8BIT_RAW:
						returned.add(new EightBitRaw(rowPayload));
						break;
					case RAW_MARKER:
						returned.add(new RawMarker(rowPayload));
						break;
					case ASIC_EEG_POWER:
						returned.add(new AsicEegPower(rowPayload));
						break;
					default:
						returned.add(new UnknownType(type));
						break;
				}
				subMessOffset += ((offset + 1) + 1 + payloadLen);
				if (subMessOffset >= (payload.length - 1)) {
					//      System.out.println("...Done parsing.");
					readCompleted = true;
				} else {
					//      System.out.println("Now parsing mess offset " + subMessOffset);
				}
			}
			return returned;
		}

		public List<Object> getParsed() {
			return parsed;
		}
	}

	public static class DeviceID {
		private short id = 0;

		public DeviceID(byte[] ba) {
			id |= ((ba[0] & 0xFF) << 8);
			id |= (ba[1] & 0xFF);
			id &= 0xFFFF;
		}

		public short getID() {
			return id;
		}
	}

	public static class ConnectedDeviceID extends DeviceID {
		public ConnectedDeviceID(byte[] ba) {
			super(ba);
		}
	}

	public static class DisconnectedDeviceID extends DeviceID {
		public DisconnectedDeviceID(byte[] ba) {
			super(ba);
		}
	}

	public abstract static class OneByteValue {
		private byte val = 0;

		public OneByteValue(byte[] ba) {
			val = ba[0];
		}

		public short getVal() {
			return val;
		}
	}

	public static class BatteryLevel extends OneByteValue {
		public BatteryLevel(byte[] ba) {
			super(ba);
		}
	}

	public static class PoorSignal extends OneByteValue {
		public PoorSignal(byte[] ba) {
			super(ba);
		}
	}

	public static class HeartRate extends OneByteValue {
		public HeartRate(byte[] ba) {
			super(ba);
		}
	}

	public static class Attention extends OneByteValue {
		public Attention(byte[] ba) {
			super(ba);
		}
	}

	public static class Meditation extends OneByteValue {
		public Meditation(byte[] ba) {
			super(ba);
		}
	}

	public static class EightBitRaw extends OneByteValue {
		public EightBitRaw(byte[] ba) {
			super(ba);
		}
	}

	public static class RawMarker extends OneByteValue {
		public RawMarker(byte[] ba) {
			super(ba);
		}
	}

	public static class RawWave {
		private short value = 0;

		public RawWave(byte[] ba) {
			short high = (short) (ba[0] & 0xFF);
			short low = (short) (ba[1] & 0xFF);
			value = (short) ((high * 256) + low);
//    if (value > 0x8000) value -= 0x10000;
			if ((value & 0xF000) > 0)
				value = (short) (((~value) & 0xFFF) + 1);
			else
				value = (short) (value & 0xFFF);
		}

		public short getValue() {
			return value;
		}
	}

	public static class StbyStatus {
		private byte status;

		public StbyStatus(byte[] ba) {
			status = ba[0];
		}

		public byte getStatus() {
			return status;
		}
	}

	public static class UnknownType {
		private byte type;

		public UnknownType(byte b) {
			this.type = b;
		}

		public byte getType() {
			return this.type;
		}
	}

	public static class AsicEegPower {
		/**
		 * delta          0.5 -  2.75 Hz
		 * theta          3.5 -  6.75 Hz
		 * low-alpha      7.5 -  9.25 Hz
		 * high-alpha    10   - 11.75 Hz
		 * low-beta      13   - 16.75 Hz
		 * high-beta     18   - 29.75 Hz
		 * low-gamma     31   - 39.75 Hz
		 * and mid-gamma 41   - 49.75 Hz
		 * <p>
		 * The returned values have NO UNIT.
		 * Only meaningful when compared to each other.
		 * <p>
		 * Max value is 0x00FFFF (16,777,215 in decimal)
		 */
		public final static String[] WAVE_NAMES =
				{"delta", "theta", "low-alpha", "high-alpha", "low-beta", "high-beta", "low-gamma", "mid-gamma"};
		private int[] values = new int[8];

		public AsicEegPower(byte[] ba) {
			if (ba.length != 24)
				throw new RuntimeException("Bad length for ASIC_EEG_POWER. Expected 24, got " + ba.length);
			for (int i = 0; i < 8; i++) {
				int x = 0;
				x |= (ba[(i * 3) + 0] << 16);
				x |= (ba[(i * 3) + 1] << 8);
				x |= (ba[(i * 3) + 2]);
				x &= 0xFFFFFF;
				values[i] = x;
			}
		}

		public int[] getValues() {
			return values;
		}
	}

	public static class AccessDenied {
	}

	public static class NotFound {
	}

	/*
	 * For tests.
	 */
	public static void main(String... args) {
		byte[] REQUEST_DENIED = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0x02, (byte) 0xD3, (byte) 0x00, (byte) 0x2C};
		byte[] OK = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0xD0, (byte) 0x02, (byte) 0x05, (byte) 0x05, (byte) 0x23};
		byte[] TRYING = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0x03, (byte) 0xD4, (byte) 0x01, (byte) 0x01, (byte) 0x29};
		byte[] RAW_WAVE = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x80, (byte) 0x02, (byte) 0x00, (byte) 0x31, (byte) 0x4C};
		byte[] EEG = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0x20, (byte) 0x02, (byte) 0x00, (byte) 0x83, (byte) 0x18, (byte) 0x03, (byte) 0xEA, (byte) 0x2E, (byte) 0x02, (byte) 0xFA, (byte) 0x08, (byte) 0x01, (byte) 0x06, (byte) 0x7A, (byte) 0x00, (byte) 0x86, (byte) 0xA5, (byte) 0x00, (byte) 0x22, (byte) 0xCC, (byte) 0x00, (byte) 0x28, (byte) 0x04, (byte) 0x00, (byte) 0x11, (byte) 0xAB, (byte) 0x00, (byte) 0x10, (byte) 0xCC, (byte) 0x04, (byte) 0x26, (byte) 0x05, (byte) 0x42, (byte) 0x74};

		byte[] XXX = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x80, (byte) 0x02, (byte) 0x00, (byte) 0xAD, (byte) 0xD1};
		byte[] YYY = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x80, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0xE8};

		byte cs = 0;

		byte[][] cmd = new byte[][]
				{
						REQUEST_DENIED,
						OK,
						TRYING,
						RAW_WAVE,
						EEG,
						XXX,
						YYY
				};

		RawWave rawWave = new RawWave(new byte[]{(byte) 0xFF, (byte) 0xF0});
		System.out.println("RW:" + rawWave.getValue());

		short raw = 0;
		raw = (byte) 0xFF << 8 | (byte) 0xF0;
		System.out.println("Raw:" + raw);
		if ((raw & 0xF000) > 0)
			raw = (short) (((~raw) & 0xFFF) + 1);
		else
			raw = (short) (raw & 0xFFF);
		System.out.println("Raw:" + raw);

		for (int i = 0; i < cmd.length; i++) {
			String s = DumpUtil.dumpHexMess(cmd[i]);
			System.out.println("Parsing [" + s + "]");
			cs = thinkGearChecksum(cmd[i]);
			System.out.println("Checksum: 0x" + StringUtils.lpad(Integer.toHexString(cs & 0xFF).toUpperCase(), 2, "0"));

			if (cs == cmd[i][cmd[i].length - 1]) {
				List<Object> objLst = new ThinkGearParser(cmd[i]).getParsed();
				for (Object obj : objLst) {
					if (obj instanceof DeviceID) {
						DeviceID id = (DeviceID) obj;
						System.out.println("- Device ID: 0x" + StringUtils.lpad(Integer.toHexString(id.getID() & 0xFFFF), 4, "0"));
					} else if (obj instanceof StbyStatus) {
						StbyStatus ss = (StbyStatus) obj;
						System.out.println("- Status:" + (ss.getStatus() == STBY_STATUS_STBY ? "Stand By" : (ss.getStatus() == STBY_STATUS_TRYING ? "Trying..." : "Unknown")));
					} else if (obj instanceof AccessDenied) {
						System.out.println("- Access denied");
					} else if (obj instanceof RawWave) {
						RawWave rw = (RawWave) obj;
						System.out.println("- Raw Wave value:" + rw.getValue());
					} else if (obj instanceof PoorSignal) {
						PoorSignal ps = (PoorSignal) obj;
						System.out.println("- Poor signal:" + ps.getVal() + "/255");
					} else if (obj instanceof BatteryLevel) {
						BatteryLevel bl = (BatteryLevel) obj;
						System.out.println("- Battery Level:" + bl.getVal() + "/255");
					} else if (obj instanceof HeartRate) {
						HeartRate hr = (HeartRate) obj;
						System.out.println("- Heart Rate:" + hr.getVal() + "/255");
					} else if (obj instanceof EightBitRaw) {
						EightBitRaw ebr = (EightBitRaw) obj;
						System.out.println("- 8-bit raw signal:" + ebr.getVal() + "/255");
					} else if (obj instanceof RawMarker) {
						RawMarker rm = (RawMarker) obj;
						System.out.println("- Raw Marker:" + rm.getVal() + "/255");
					} else if (obj instanceof Attention) {
						Attention att = (Attention) obj;
						System.out.println("- Attention:" + att.getVal() + "/255");
					} else if (obj instanceof Meditation) {
						Meditation med = (Meditation) obj;
						System.out.println("- Meditation:" + med.getVal() + "/255");
					} else if (obj instanceof AsicEegPower) {
						AsicEegPower aep = (AsicEegPower) obj;
						System.out.print("- AsicEegPower: ");
						int[] values = aep.getValues();
						int idx = 0;
						for (int v : values)
							System.out.print(AsicEegPower.WAVE_NAMES[idx++] + ":" + NumberFormat.getInstance().format(v) + " ");
						System.out.println();
					} else if (obj instanceof UnknownType) {
						UnknownType ut = (UnknownType) obj;
						byte t = ut.getType();
						System.out.println("- Unknown type [" + StringUtils.lpad(Integer.toHexString(t & 0xFF), 2, "0") + "]");
					} else
						System.out.println("What?");
				}
			} else
				System.out.println("Oops! Bad checksum");
		}
	}
}
