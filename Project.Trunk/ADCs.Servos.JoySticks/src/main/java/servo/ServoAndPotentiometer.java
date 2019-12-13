package servo;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;

/**
 * Standard servo, using I2C and the PCA9685 servo board
 *
 * Uses a linear potentiometer to drive a servo.
 * <br/>
 * Servos like <a href="https://www.adafruit.com/product/169">https://www.adafruit.com/product/169</a> or <a href="https://www.adafruit.com/product/155">https://www.adafruit.com/product/155</a>
 * <br/>
 * Pot like <a href="https://www.adafruit.com/product/3391">https://www.adafruit.com/product/3391</a>
 */
public class ServoAndPotentiometer {

	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;

	private PCA9685 servoBoard = null;

	public ServoAndPotentiometer(int channel) throws I2CFactory.UnsupportedBusNumberException {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX);
	}

	public ServoAndPotentiometer(int channel, int servoMin, int servoMax) throws I2CFactory.UnsupportedBusNumberException {
		this.servoBoard = new PCA9685();

		this.servoMin = servoMin;
		this.servoMax = servoMax;
		this.diff = servoMax - servoMin;

		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz

		this.servo = channel;
		System.out.println("Channel " + channel + " all set. Min:" + servoMin + ", Max:" + servoMax + ", diff:" + diff);
	}

	public void setAngle(float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		// System.out.println(f + " degrees (" + pwm + ")");
		servoBoard.setPWM(servo, 0, pwm);
	}

	public void setPWM(int pwm) {
		servoBoard.setPWM(servo, 0, pwm);
	}

	public void stop() { // Set to 0
		servoBoard.setPWM(servo, 0, 0);
	}

	/*
	 * deg in [-90..90]
	 */
	private static int degreeToPWM(int min, int max, float deg) {
		int diff = max - min;
		float oneDeg = diff / 180f;
		return Math.round(min + ((deg + 90) * oneDeg));
	}

	private final static String ADC_CHANNEL = "--adc-channel:";
	private final static String PCA9685_SERVO_PORT = "--servo-port:";

	private static boolean loop = true;
	public static void main(String... args) throws Exception {
		int adcChannel = 0;
		int servoPort  = 0;

		// User prms
		for (String str : args) {
			if (str.startsWith(ADC_CHANNEL)) {
				String s = str.substring(ADC_CHANNEL.length());
				adcChannel = Integer.parseInt(s);
				boolean validChannel = false;
				for (MCPReader.MCP3008InputChannels channel : MCPReader.MCP3008InputChannels.values()) {
					if (channel.ch() == adcChannel) {
						validChannel = true;
						break;
					}
				}
				if (!validChannel) {
					throw new IllegalArgumentException(String.format("Invalid MCP3008 Channel: %d", adcChannel));
				}
			} else if (str.startsWith(PCA9685_SERVO_PORT)) {
				String s = str.substring(PCA9685_SERVO_PORT.length());
				servoPort = Integer.parseInt(s);
			}
		}

		System.out.println("Driving Servo on Channel " + servoPort);
		System.out.println("Reading MCP300 on Channel " + adcChannel);

		ServoAndPotentiometer ss = new ServoAndPotentiometer(servoPort);
		MCPReader.initMCP(MCPReader.MCPFlavor.MCP3008); // with default wiring

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			loop = false;
		}, "Shutdown Hook"));

		int lastRead = 0;
		int tolerance = 1; // This is used for damping...

		try {
			ss.stop(); // init
			while (loop) {
				int adc = MCPReader.readMCP(adcChannel); // [0..1023]
				int potAdjust = Math.abs(adc - lastRead);
				if (potAdjust > tolerance) {
					try {
						float angle = (adc * (180f / 1_023f)) - 90f;
						ss.setAngle(angle);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		} finally {
			ss.stop();
			MCPReader.shutdownMCP();
		}
		System.out.println("Done.");
	}
}
