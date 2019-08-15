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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

public class SerialReader
				extends NMEAReader
				implements SerialPortEventListener, CommPortOwnershipListener {
	private String comPort = "/dev/ttyUSB0"; // "COM1";
	private int br = 4800;
	private SerialPort serialPort;

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
			while (enumeration.hasMoreElements()) {
				CommPortIdentifier cpi = (CommPortIdentifier) enumeration.nextElement();
				System.out.println(String.format("Port: %s, %s", cpi.getName(), readablePortType(cpi.getPortType())));
				nbp++;
			}
			System.out.println("Found " + nbp + " port(s)");
		}
		CommPortIdentifier com = null;
		try {
			com = CommPortIdentifier.getPortIdentifier(comPort);
		} catch (NoSuchPortException nspe) {
			System.err.println(comPort + ": No Such Port");
			nspe.printStackTrace();
			return;
		}
		CommPort thePort = null;
		try {
			com.addPortOwnershipListener(this);
			thePort = com.open("NMEAPort", 10_000);
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
				this.serialPort.enableReceiveTimeout(30);
			} catch (UnsupportedCommOperationException ucoe) {
				this.serialPort.close();
				System.err.println(ucoe.getMessage());
				return;
			}
			try {
				// Settings for B&G Hydra, TackTick, NKE, most of the NMEA Stations (BR 4800).
				this.serialPort.setSerialPortParams(this.br,
								SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
			} catch (UnsupportedCommOperationException ucoe) {
				System.err.println("Unsupported Comm Operation");
				return;
			}
			try {
				this.serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				theInput = this.serialPort.getInputStream();
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
		new SerialReader().startReader();
	}
}
