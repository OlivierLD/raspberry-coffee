package i2c.comm;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * I2C Communication with an Arduino
 * Raspberry is the Master, Arduino is the Slave
 *
 * See the Arduino sketch named RPi_I2C_2.ino
 *
 * Wiring:
 * RasPi    Arduino
 * ----------------
 * GND #9   GND
 * SDA #3   SDA (or A4, before Rev3)
 * SLC #5   SLC (or A5, before Rev3)
 *
 * This illustrates a more elaborated dialog between master and slave.
 *
 */
public class I2CArduino {
	public final static int ARDUINO_ADDRESS = 0x04; // See RPi_I2C_2.ino
	private final static boolean verbose = "true".equals(System.getProperty("arduino.verbose", "false"));

	private final static byte END_OF_MESSAGE = (byte) 0x00;
	private final static byte STRING_REQUEST = (byte) 0x10; // Master wants a string message
	private final static byte STRING_RECEIVE = (byte) 0x11; // Master will send a string message

	private final static byte PING = (byte) 0x20; // Master ping
	private final static byte PONG = (byte) 0x21; // Slave pong

	private I2CBus bus;
	private I2CDevice arduino;

	public I2CArduino() throws I2CFactory.UnsupportedBusNumberException {
		this(ARDUINO_ADDRESS);
	}

	public I2CArduino(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPi version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}
			// Get device itself
			arduino = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void close() {
		try {
			this.bus.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	 * methods readArduino, writeArduino
	 * This where the communication protocol would be implemented.
	 */
	public int readArduino()
					throws Exception {
		int r = arduino.read();
		return r;
	}

	public void writeArduino(byte b)
					throws Exception {
		arduino.write(b);
	}

	private static void delay(float d) // d in seconds.
	{
		try {
			Thread.sleep((long) (d * 1_000));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		final NumberFormat NF = new DecimalFormat("##00.00");
		I2CArduino sensor = new I2CArduino();
		int read = 0;
		System.out.println("Arduino Connected");

		try {
			System.out.println("First read");
			read = sensor.readArduino();
			System.out.println("Read: " + NF.format(read));
		} catch (Exception e) {
			System.err.println("First read:");
			e.printStackTrace();
		}
		delay(1);

		for (int i = 0; i < 5; i++) {
			try {
				System.out.println("Sending PING");
				sensor.writeArduino(PING);
				System.out.println("Expecting PONG");
				delay(1f);
				read = sensor.readArduino();
				if (((byte) read) == PONG) {
					System.out.println("Pong OK");
				} else {
					System.out.println("Unexpected reply to ping:" + Integer.toHexString(((byte) read) & 0xFF));
				}
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}
			delay(1f);
		}

		System.out.println("=== Receiving a message ===");
		try {
			sensor.writeArduino(STRING_REQUEST);
			System.out.println("STRING_REQUEST sent to Arduino.");
			delay(1f);
			StringBuffer sb = new StringBuffer();
			int r = -1;
			while (r != END_OF_MESSAGE) {
				r = sensor.readArduino();
				if (r != END_OF_MESSAGE) {
					delay(1f);
					sb.append((char) r);
				}
			}
			System.out.println("Received the following String message : [" + sb.toString() + "]");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		delay(1);
		System.out.println("=== Sending a message ===");
		try {
			sensor.writeArduino(STRING_RECEIVE);
			System.out.println("STRING_RECEIVE sent to Arduino.");
			delay(1f);
			String masterMessage = "From the Raspberry Pi"; // Hard coded (this is an example)
			byte[] ba = masterMessage.getBytes();
			for (byte b : ba) {
				sensor.writeArduino(b);
				delay(1f);
			}
			sensor.writeArduino(END_OF_MESSAGE);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Bye!");
	}
}
