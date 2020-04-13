package nmea.forwarders;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.OutputStream;

import java.io.IOException;
import java.util.Properties;

public class SerialWriter implements Forwarder {
	private String comPort = "/dev/ttyUSB1"; // "COM1";
	private int br = 4800;
	private SerialPort serialPort;
	private Properties props = null;

	private OutputStream out = null;

	public SerialWriter(String port, int br) throws Exception {
		this.comPort = port;
		this.br = br;

		CommPortIdentifier com = null;
		try {
			com = CommPortIdentifier.getPortIdentifier(this.comPort);
		} catch (NoSuchPortException nspe) {
			System.err.println(this.comPort + ": No Such Port");
			nspe.printStackTrace();
			return;
		}
		CommPort thePort = null;
		try {
			thePort = com.open("NMEAPort", 10_000);
		} catch (PortInUseException piue) {
			System.err.println("Port In Use");
			return;
		}
		int portType = com.getPortType();
		if (portType == CommPortIdentifier.PORT_PARALLEL)
			System.out.println("This is a parallel port");
		else if (portType == CommPortIdentifier.PORT_SERIAL)
			System.out.println("This is a serial port");
		else
			System.out.println("This is an unknown port:" + portType);
		if (portType == CommPortIdentifier.PORT_SERIAL) {
			this.serialPort = (SerialPort) thePort;
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
				out = this.serialPort.getOutputStream();
				System.out.println("Ready to write on serial port...");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Reading on Serial Port
		System.out.println(String.format("%s:Port is open...", comPort));
	}

	@Override
	public void write(byte[] message) {
		if (this.out != null) {
			try {
				if ("true".equals(System.getProperty("serial.verbose", "false"))) {
					System.out.println(String.format("Writing to %s:%d [%s]", this.comPort, this.br, new String(message).trim()));
				}
				this.out.write((new String(message).trim() + "\r\n").getBytes());
				this.out.flush();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		} else if ("true".equals(System.getProperty("serial.verbose", "false"))) {
			System.err.println("Serial Output is not opened.");
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			if (this.out != null)
			  this.out.close();
			if (this.serialPort != null)
				this.serialPort.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getPort() {
		return this.comPort;
	}

	public static class SerialBean {
		private String cls;
		private String port;
		private int br;
		private String type = "serial";

		public SerialBean(SerialWriter instance) {
			cls = instance.getClass().getName();
			port = instance.comPort;
			br = instance.br;
		}

		public String getPort() {
			return port;
		}
		public int getBR() {
			return br;
		}
	}

	@Override
	public Object getBean() {
		return new SerialBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
