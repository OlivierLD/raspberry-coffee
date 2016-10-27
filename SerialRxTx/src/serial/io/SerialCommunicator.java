package serial.io;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

public class SerialCommunicator
				implements SerialPortEventListener {
	private SerialIOCallbacks parent;
	private boolean verbose = false;

	private SerialPort serialPort = null;

	// Serial input and output
	private InputStream input = null;
	private OutputStream output = null;

	private boolean isConnected = false;

	private final static int TIMEOUT = 2000;

	private final static int DEFAULT_BAUD_RATE = 9600;
	private final static int DEFAULT_FLOW_CTRL_IN = SerialPort.FLOWCONTROL_NONE;
	private final static int DEFAULT_FLOW_CTRL_OUT = SerialPort.FLOWCONTROL_NONE;
	private final static int DEFAULT_DATABITS = SerialPort.DATABITS_8;
	private final static int DEFAULT_STOP_BITS = SerialPort.STOPBITS_1;
	private final static int DEFAULT_PARITY = SerialPort.PARITY_NONE;

	public SerialCommunicator(SerialIOCallbacks caller) {
		this.parent = caller;
	}

	public Map<String, CommPortIdentifier> getPortList() {
		Map<String, CommPortIdentifier> portMap = new HashMap<String, CommPortIdentifier>();
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements()) {
			CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
			// Serial ports only
			if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portMap.put(curPort.getName(), curPort);
			}
		}
		return portMap;
	}

	public void connect(CommPortIdentifier port) throws PortInUseException, Exception {
		connect(port, "");
	}

	public void connect(CommPortIdentifier port, String userPortName) throws PortInUseException, Exception {
		connect(port, userPortName, DEFAULT_BAUD_RATE);
	}

	public void connect(CommPortIdentifier port, int br) throws PortInUseException, Exception, UnsupportedCommOperationException {
		connect(port, "", br, DEFAULT_DATABITS, DEFAULT_STOP_BITS, DEFAULT_PARITY, DEFAULT_FLOW_CTRL_IN, DEFAULT_FLOW_CTRL_OUT);
	}

	public void connect(CommPortIdentifier port, String userPortName, int br) throws PortInUseException, Exception, UnsupportedCommOperationException {
		connect(port, userPortName, br, DEFAULT_DATABITS, DEFAULT_STOP_BITS, DEFAULT_PARITY, DEFAULT_FLOW_CTRL_IN, DEFAULT_FLOW_CTRL_OUT);
	}

	public void connect(CommPortIdentifier port, String userPortName, int br, int db, int sb, int par, int fIn, int fOut) throws PortInUseException, Exception, UnsupportedCommOperationException {
		try {
			serialPort = (SerialPort) port.open(userPortName, TIMEOUT);
			serialPort.setSerialPortParams(br, db, sb, par);
			try {
				serialPort.setFlowControlMode(fIn | fOut);
			} catch (UnsupportedCommOperationException e) {
				throw e;
			}
			setConnected(true);
			this.parent.connected(true);
		} catch (PortInUseException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	public boolean initIOStream() throws IOException {
		boolean success = false;
		try {
			this.input = serialPort.getInputStream();
			this.output = serialPort.getOutputStream();
			success = true;
		} catch (IOException e) {
			success = false;
			throw e;
		}
		return success;
	}

	public void initListener() throws TooManyListenersException {
		try {
			this.serialPort.addEventListener(this);
			this.serialPort.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			throw e;
		}
	}

	public void disconnect() throws IOException {
		System.exit(0);
		// TODO See what's wrong here, on disconnect (JVM crashes)
		try {
			if (this.input != null) {
				if (verbose) {
					System.out.println("Closing input");
				}
				this.input.close();
			}
			if (this.output != null) {
				if (verbose) {
					System.out.println("Closing output");
				}
				this.output.close();
			}
			if (verbose) {
				System.out.println("Removing event listener");
			}
			this.serialPort.removeEventListener(); // Causes a JVM crash?
			if (verbose) {
				System.out.println("Closing Serial port");
			}
			this.serialPort.close();               // Causes a JVM crash?
		} catch (IOException e) {
			throw e;
		}
		setConnected(false);
		this.parent.connected(false);
	}

	public final boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean b) {
		this.isConnected = b;
	}

	public void setVerbose(boolean b) {
		this.verbose = b;
	}

	@Override
	public void serialEvent(SerialPortEvent evt) {
//    if (verbose)
//      System.out.println("serialEvent");
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				if (false) // TODO an option
				{
					byte b = (byte) this.input.read();
					this.parent.onSerialData(b);
				} else {
					byte[] ba = new byte[512];
					int r = this.input.read(ba);
					if (verbose) {
						System.out.println(String.format(">>> Received %d byte(s), for onSerialData(byte[])", r));
					}
					this.parent.onSerialData(ba, r);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void writeData(String s) throws IOException {
		this.writeData(s.getBytes());
	}

	public void writeData(byte[] ba) throws IOException {
		for (byte b : ba)
			this.writeData(b);
	}

	public void writeData(byte b) throws IOException {
		if (verbose)
			System.out.println("Written to serial port => character [0x" + Integer.toHexString(b) + "]");

		try {
			this.output.write(b & 0xFF);
//    try { Thread.sleep(10L); } catch (InterruptedException ie) {} // QUESTION: ???
//    output.flush();
		} catch (IOException e) {
			throw e;
		}
	}

	public void flushSerial() throws IOException {
		output.flush();
	}
}
