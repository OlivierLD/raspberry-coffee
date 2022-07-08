package sevensegdisplay;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

/*
 * I2C Required for this one
 */
public class LEDBackPack {
	/*
	Prompt> sudo i2cdetect -y 1
		0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
	00:          -- -- -- -- -- -- -- -- -- -- -- -- --
	10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
	20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
	30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
	40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
	50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
	60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
	70: 70 -- -- -- -- -- -- --
	 */
	// This next addresses is returned by "sudo i2cdetect -y 1", see above.
	public final static int LEDBACKPACK_ADDRESS = 0x70;

	private boolean verbose = false;

	private I2CBus bus;
	private I2CDevice ledBackpack;

	// Registers
	public final static int HT16K33_REGISTER_DISPLAY_SETUP = 0x80;
	public final static int HT16K33_REGISTER_SYSTEM_SETUP = 0x20;
	public final static int HT16K33_REGISTER_DIMMING = 0xE0;

	// Blink rate
	public final static int HT16K33_BLINKRATE_OFF = 0x00;
	public final static int HT16K33_BLINKRATE_2HZ = 0x01;
	public final static int HT16K33_BLINKRATE_1HZ = 0x02;
	public final static int HT16K33_BLINKRATE_HALFHZ = 0x03;

	// Display buffer (8x16-bits).
	//                       1st digit, 2nd digit, column, 3rd digit, 4th digit, ?,      ?,      ?     Probably for the 8x8 led matrix
	private int[] buffer = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};

	public LEDBackPack() throws I2CFactory.UnsupportedBusNumberException {
		this(LEDBACKPACK_ADDRESS);
	}

	public LEDBackPack(int address) throws I2CFactory.UnsupportedBusNumberException {
		this(address, false);
	}

	public LEDBackPack(int address, boolean v) throws I2CFactory.UnsupportedBusNumberException {
		this.verbose = v;
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPi version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}
			// Get device itself
			ledBackpack = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
			//Turn the oscillator on
			ledBackpack.write(HT16K33_REGISTER_SYSTEM_SETUP | 0x01, (byte) 0x00);
			// Turn blink off
			this.setBlinkRate(HT16K33_BLINKRATE_OFF);
			// Set maximum brightness
			this.setBrightness(15);
			// Clear the screen
			this.clear();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	 * Sets the brightness level from 0..15
	 */
	private void setBrightness(int brightness) throws IOException {
		if (brightness > 15) {
			brightness = 15;
		}
		ledBackpack.write(HT16K33_REGISTER_DIMMING | brightness, (byte) 0x00);
	}

	/*
	 * Sets the blink rate
	 */
	private void setBlinkRate(int blinkRate) throws IOException {
		if (blinkRate > HT16K33_BLINKRATE_HALFHZ) {
			blinkRate = HT16K33_BLINKRATE_OFF;
		}
		ledBackpack.write(HT16K33_REGISTER_DISPLAY_SETUP | 0x01 | (blinkRate << 1), (byte) 0x00);
	}

	/*
	 * Updates a single 16-bit entry in the 8*16-bit buffer
	 */
	public void setBufferRow(int row, int value) throws IOException {
		setBufferRow(row, value, true);
	}

	public void setBufferRow(int row, int value, boolean update) throws IOException {
		if (row > 7) {
			return;                    // Prevent buffer overflow
		}
		this.buffer[row] = value;    // value # & 0xFFFF
		if (update) {
			this.writeDisplay();       // Update the display
		}
	}

	/*
	 * Returns a copy of the raw buffer contents
	 */
	public int[] getBuffer() {
		int[] bufferCopy = buffer.clone();
		return bufferCopy;
	}

	/*
	 * Updates the display memory
	 */
	private void writeDisplay() throws IOException {
		byte[] bytes = new byte[2 * buffer.length];
		for (int i = 0; i < buffer.length; i++) {
			int item = buffer[i];
			bytes[2 * i] = (byte) (item & 0xFF);
			bytes[(2 * i) + 1] = (byte) ((item >> 8) & 0xFF);
		}
		ledBackpack.write(0x00, bytes, 0, bytes.length);
	}

	/*
	 * Clears the display memory
	 */
	public void clear() throws IOException {
		clear(true);
	}

	public void clear(boolean update) throws IOException {
		this.buffer = new int[]{0, 0, 0, 0, 0, 0, 0, 0}; // Reset. Bam!
		if (update) {
			this.writeDisplay();
		}
	}
}
