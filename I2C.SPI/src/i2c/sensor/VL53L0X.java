package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

/*
 * I2C Time of Flight distance sensor, 30 to 1000 mm.
 * https://www.adafruit.com/products/3317
 */
public class VL53L0X {
	public final static int VL53L0X_I2CADDR = 0x29;

	private final static int VL53L0X_GOOD_ACCURACY_MODE      = 0;   // Good Accuracy mode
	private final static int VL53L0X_BETTER_ACCURACY_MODE    = 1;   // Better Accuracy mode
	private final static int VL53L0X_BEST_ACCURACY_MODE      = 2;   // Best Accuracy mode
	private final static int VL53L0X_LONG_RANGE_MODE         = 3;   // Longe Range mode
	private final static int VL53L0X_HIGH_SPEED_MODE         = 4;   // High Speed mode

	private final static int  VERSION_REQUIRED_MAJOR = 1;
	private final static int  VERSION_REQUIRED_MINOR = 0;
	private final static int  VERSION_REQUIRED_BUILD = 2;

	private final static int  MAX_DEVICES = 16;

	private static boolean verbose = "true".equals(System.getProperty("vl53l0x.debug", "false"));

	private I2CBus bus;
	private I2CDevice vl53l0x;

	public VL53L0X() throws I2CFactory.UnsupportedBusNumberException {
		this(VL53L0X_I2CADDR);
	}

	public VL53L0X(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI version
			if (verbose)
				System.out.println("Connected to bus. OK.");

			// Get device itself
			vl53l0x = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}

		} catch (Exception ex) {

		}
	}

	public void startRanging(int objectNumber, int mode, int i2cAddress, int tca9548ADevice, int tca9548AAddress) {

	}

}
