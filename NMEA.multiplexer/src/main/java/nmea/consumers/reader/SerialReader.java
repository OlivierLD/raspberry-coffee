package nmea.consumers.reader;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.CommPortOwnershipListener;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAReader;
import utils.TimeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

public class SerialReader
				extends NMEAReader
				implements SerialPortEventListener, CommPortOwnershipListener {
	private String comPort = "/dev/ttyUSB0"; // "COM1";
	private int br = 4_800;
	private SerialPort serialPort;

	private final static int TIMEOUT = 10_000;

	public SerialReader() {
	}
	public SerialReader(String com, int br) {
		this.comPort = com;
		this.br = br;
	}
	public SerialReader(List<NMEAListener> al) {
		super(al);
	}
	public SerialReader(List<NMEAListener> al, String com, int br) {
		this(null, al, com, br);
	}
	public SerialReader(String threadName, List<NMEAListener> al, String com, int br) {
		super(threadName, al);
		this.comPort = com;
		this.br = br;
	}

	public int getBr() {
		return this.br;
	}

	public String getPort() {
		return this.comPort;
	}

	private InputStream theInput = null;

	private static String readablePortType(int type) {
		switch (type) {
			case CommPortIdentifier.PORT_I2C:
				return "I2C";
			case CommPortIdentifier.PORT_PARALLEL:
				return "PARALLEL";
			case CommPortIdentifier.PORT_RAW:
				return "RAW";
			case CommPortIdentifier.PORT_RS485:
				return "RS485";
			case CommPortIdentifier.PORT_SERIAL:
				return "SERIAL";
			default:
				return "Unknown type";
		}
	}

	@Override
	public void startReader() {
		super.enableReading();
		// Opening Serial port
		if (verbose) {
			Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
			int nbp = 0;
			System.out.println("----- Serial Port List -----");
			while (enumeration.hasMoreElements()) {
				CommPortIdentifier cpi = (CommPortIdentifier) enumeration.nextElement();
				System.out.println(String.format("Port: %s, %s, %s.",
						cpi.getName(),
						readablePortType(cpi.getPortType()),
						(cpi.isCurrentlyOwned() ? String.format("(owned by %s)", cpi.getCurrentOwner()) : "free")));
				nbp++;
			}
			System.out.println("Found " + nbp + " port(s)");
			System.out.println("----------------------------");
		}
		CommPortIdentifier com = null;
		final int DEFAULT_MAX_OPEN_TRIES = 5;
		int maxOpenTries = Integer.parseInt(System.getProperty("serial.max.open.tries", String.valueOf(DEFAULT_MAX_OPEN_TRIES)));
		final long BETWEEN_TRIES = 1_000L;
		int nbOpenTries = 0;
		while (nbOpenTries < maxOpenTries && com == null) {
			try {
				com = CommPortIdentifier.getPortIdentifier(this.comPort);
				if (verbose) {
					System.out.println(String.format("\t>> Serial port %s opened after %d try(ies)", this.comPort, (nbOpenTries + 1)));
				}
			} catch (NoSuchPortException nspe) {
				nbOpenTries++;
				System.err.println(this.comPort + ": No Such Port");
				nspe.printStackTrace();
				if (nbOpenTries < maxOpenTries) {
					System.err.println(String.format("\t... Retrying to open %s in %d ms", this.comPort, BETWEEN_TRIES));
					TimeUtil.delay(BETWEEN_TRIES);
				} else {
					System.err.println(String.format(">>>>> %s: No Such Port after %d tries, giving up.", this.comPort, maxOpenTries));
					throw new RuntimeException(nspe);
//					return;
				}
			}
		}
		CommPort thePort = null;
		try {
			com.addPortOwnershipListener(this);
			thePort = com.open("NMEAPort", TIMEOUT);
		} catch (PortInUseException piue) {
			System.err.println("Port In Use");
			return;
		}
		int portType = com.getPortType();
		if (portType == CommPortIdentifier.PORT_PARALLEL) {
			System.out.println("This is a parallel port");
		} else if (portType == CommPortIdentifier.PORT_SERIAL) {
			System.out.println("This is a serial port");
		} else {
			System.out.println("This is an unknown port:" + portType);
		}
		if (portType == CommPortIdentifier.PORT_SERIAL) {
			this.serialPort = (SerialPort) thePort;
			try {
				this.serialPort.addEventListener(this);
			} catch (TooManyListenersException tmle) {
				this.serialPort.close();
				System.err.println(tmle.getMessage());
				return;
			}
			this.serialPort.notifyOnDataAvailable(true);

			try {
				this.serialPort.enableReceiveTimeout(50);
			} catch (UnsupportedCommOperationException ucoe) { // Do NOT stop on this error...
				System.err.println(String.format("enableReceiveTimeout: Unsupported Comm Operation, BR: %d", this.br));
				// this.serialPort.close();
				System.err.println(ucoe.getMessage());
				System.err.println("... Moving on anyway.");
				// return;
			}

			final int MAX_TRIES = 5;
			boolean allGood = false;
			int nbTries = 0;
			while (!allGood && nbTries < MAX_TRIES) {
				try {
					// Settings for B&G Hydra, TackTick, NKE, most of the NMEA Stations (BR 4800).
					this.serialPort.setSerialPortParams(this.br,
							SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					allGood = true;
				} catch (UnsupportedCommOperationException ucoe) {
					System.err.println(String.format("setSerialPortParams: Unsupported Comm Operation, BR: %d", this.br)); // Try again if it fails
					ucoe.printStackTrace();
					nbTries++;
					TimeUtil.delay(01f); // Wait 1 sec before trying again
				}
			}
			if (!allGood) {
				throw new RuntimeException(String.format("Too many tries (%d), cannot set SerialPortParam for %s", MAX_TRIES, this.comPort));
			}

			try {
				this.serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				this.theInput = this.serialPort.getInputStream();
				System.out.println("Reading serial port...");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Reading on Serial Port
		System.out.println(String.format("%s:Port is open...", comPort));
	}

	@Override
	public void closeReader() throws Exception {
		if (this.serialPort != null) {
			theInput.close();
			this.serialPort.close();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent serialPortEvent) {
		switch (serialPortEvent.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				if (canRead()) {
					try {
						StringBuffer inputBuffer = new StringBuffer();
						int newData = 0;
						while (newData != -1) {
							try {
								if (theInput != null) {
									newData = theInput.read();
									if (newData == -1) {
										break;
									}
									inputBuffer.append((char) newData);
								}
							} catch (IOException ex) {
								System.err.println(ex);
								return;
							}
						}
						String s = new String(inputBuffer);
						// Display the startReader string
						boolean justDump = false;
						if (justDump) {
							System.out.println(":: [" + s + "] ::");
						} else {
							super.fireDataRead(new NMEAEvent(this, s));
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
//			} else {
//        System.out.println("Stop Reading serial port.");
        }
        break;
			default:
				break;
		}
	}

	@Override
	public void ownershipChange(int type) {
		if (type == CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED) {
			System.out.println("PORT_OWNERSHIP_REQUESTED");
		} else
			System.out.println("ownership changed: type=" + type);
	}

	public static void main(String... args) {
		List<NMEAListener> al = new ArrayList<>();
		// Test the retry
		SerialReader serialReader = new SerialReader("SerialReader", al, "/dev/ttyUSB0", 4_800);
		serialReader.setVerbose(true);
		serialReader.startReader();
	}
}
