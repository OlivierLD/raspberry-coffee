package samples;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import i2c.sensor.HMC5883L;
import i2c.sensor.MPL115A2;

import lcd.oled.SSD1306;
import lcd.ScreenBuffer;

import arduino.raspberrypi.SerialReader;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.io.IOException;

import java.text.DecimalFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import phonekeyboard3x4.KeyboardController;

import static utils.TimeUtil.delay;

/*
 * A phone keypad, and a 128x32 oled screen
 *
 * Plus HMC5883L (triple-axis compass)
 *    & MPL115A2 (temp + pressure)
 *
 * and an Arduino Uno on a serial (USB) port (that reads an analog light sensor)
 *
 * and a relay
 *
 * Several threads are using the oled screen.
 * Synchronization (thread-safety) is important.
 */
public class OLEDKeypadAndMultiSensor {
	private KeyboardController kbc;
	private SSD1306 oled;
	private ScreenBuffer sb;

	private MPL115A2 ptSensor;
	private HMC5883L magnetometer;

	private OneRelayManager rm;

	private final DecimalFormat HDG_FMT = new DecimalFormat("000");
	private final DecimalFormat PR_FMT = new DecimalFormat("#000.0");
	private final DecimalFormat TEMP_FMT = new DecimalFormat("##00.0");

	private boolean keepReading = true;

	private static int relayThreshold = 50;

	// This one overrides the default pins for the OLED
	public OLEDKeypadAndMultiSensor() throws Exception {
		// Relay
		try {
			rm = new OneRelayManager();
			rm.set("off"); // off by default
			System.out.println("Takatak, pin is " + rm.getStatus());
			// To make sure it works:
			delay(250);
			rm.set("on");
			delay(250);
			rm.set("off");
			delay(250);
			rm.set("on");
			delay(250);
			rm.set("off");
			System.out.println("Takatak - Off");
		} catch (Exception ex) {
			System.err.println("You're not on the PI, hey?");
			ex.printStackTrace();
		}

		// Keypad and OLED Screen
		kbc = new KeyboardController();
		//                                          Override the default pins
		oled = new SSD1306(RaspiPin.GPIO_12, // Clock
						RaspiPin.GPIO_13, // MOSI (data)
						RaspiPin.GPIO_14, // CS
						RaspiPin.GPIO_15, // RST
						RaspiPin.GPIO_16);// DC
		oled.begin();

		sb = new ScreenBuffer(128, 32);
//  sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		clear();

		// Sensors
		ptSensor = new MPL115A2();
		try {
			ptSensor.begin();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		magnetometer = new HMC5883L();

		// First sensor readings
		try {
			double hdg = magnetometer.getHeading();
			displayHdg(hdg);
			float[] data = ptSensor.measure();
			displayPT(data[MPL115A2.PRESSURE_IDX],
							data[MPL115A2.TEMPERATURE_IDX]);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		// Wait...
		delay(2_000);

		// Start sensor threads
		Thread ptThread = new Thread(() -> {
			while (keepReading) {
				try {
					float[] data = ptSensor.measure();
					displayPT(data[MPL115A2.PRESSURE_IDX], data[MPL115A2.TEMPERATURE_IDX]);
					try {
						Thread.sleep(500L);
					} catch (Exception ex) {
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			System.out.println("ptThread completed");
			ptSensor.close();
		});
		Thread hdgThread = new Thread(() -> {
			while (keepReading) {
				try {
					double hdg = magnetometer.getHeading();
					displayHdg(hdg);
					try {
						Thread.sleep(500L);
					} catch (Exception ex) {
					}
				} catch (Exception ioe) {
					ioe.printStackTrace();
				}
			}
			System.out.println("hdgThread completed");
//			magnetometer.close();
		});
		ptThread.start();
		hdgThread.start();

		reset();

		// Serial part (for the Arduino)
		String port = System.getProperty("serial.port", Serial.DEFAULT_COM_PORT);
		int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));

		System.out.println("Initializing serial Communication.");
		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(event -> {
			// print out the data received to the oled display
			String payload;
			try {
				payload = event.getAsciiString();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			if (SerialReader.validCheckSum(payload, false)) {
//        System.out.print("Arduino said:" + payload);
				// Payload like $OSMSG,LR,178*65
				String content = payload.substring(7, payload.indexOf("*"));
				String[] sa = content.split(",");
				String strVal = sa[1];
//        System.out.println("Val:" + strVal);
				try {
					displayLR(strVal + "   "); // On the oled
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				int volume = relayThreshold; // Default
				try {
					volume = Integer.parseInt(sa[1]);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
				System.out.println("Volume:" + volume);
				// Turn relay on below the threshold
				if (volume < relayThreshold) {
					String status = rm.getStatus();
					//  System.out.println("Relay is:" + status);
					if ("off".equals(status)) {
						System.out.println("Turning relay on!");
						try {
							rm.set("on");
						} catch (Exception ex) {
							System.err.println(ex.toString());
						}
					}
				} else {
					String status = rm.getStatus();
					//  System.out.println("Relay is:" + status);
					if ("on".equals(status)) {
						System.out.println("Turning relay off!");
						try {
							rm.set("off");
						} catch (Exception ex) {
							System.err.println(ex.toString());
						}
					}
				}
			}
//      else
//        System.out.println("\tOops! Invalid String [" + payload + "]");
		});

		try {
			// open the default serial port provided on the GPIO header
			System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
			serial.open(port, br);
			System.out.println(" ... connected using settings: " + Integer.toString(br) + ", N, 8, 1.");
		} catch (SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		} catch (IOException ioe) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ioe.getMessage());
			return;
		}
	}

	public synchronized void display(String txt) throws Exception {
		synchronized (sb) {
			sb.text(txt, 2, 10);
			oled.setBuffer(sb.getScreenBuffer());
			oled.display();
		}
	}

	public synchronized void displayHdg(double hdg) throws Exception {
//  System.out.println("HDG:" + Math.toDegrees(hdg) + " deg");
		synchronized (sb) {
			String txt = "HDG:" + HDG_FMT.format(Math.toDegrees(hdg));
			sb.text(txt, 2, 20);
			oled.setBuffer(sb.getScreenBuffer());
			oled.display();
		}
	}

	public synchronized void displayLR(String s) throws Exception {
		//  System.out.println("HDG:" + Math.toDegrees(hdg) + " deg");
		synchronized (sb) {
			String txt = "LR:" + s;
			sb.text(txt, 64, 20);
			oled.setBuffer(sb.getScreenBuffer());
			oled.display();
		}
	}

	public synchronized void displayPT(double press, double temp) throws Exception {
//  System.out.println("P:" + press + ", T:" + temp);
		synchronized (sb) {
			String txt = "Baro:" + PR_FMT.format(press * 10) + " hPa, T:" + TEMP_FMT.format(temp) + " C";
			sb.text(txt, 2, 30);
			oled.setBuffer(sb.getScreenBuffer());
			oled.display();
		}
	}

	private Pattern p = Pattern.compile("^(\\d+)#"); // Pattern for the threshold 123#

	/*
	 * Reads user input from the keypad
	 */
	public void userInput() throws Exception {
		StringBuffer charBuff = new StringBuffer();
		boolean go = true;
		while (go) {
			char c = kbc.getKey();
//    System.out.println("At " + System.currentTimeMillis() + ", Char: " + c);

			if (c == '*') // key '*' means reset
			{
				charBuff = new StringBuffer();
				reset();
			} else
				charBuff.append(c);
			String user = charBuff.toString();
			display(user);
			// Semantic...
			if ("##".equals(user)) // ## : Exit
				go = false;
			else {
				Matcher m = p.matcher(user); // New threshold
				if (m.matches()) {
					String newThresholdStr = m.group(1);
					try {
						int newThreshold = Integer.parseInt(newThresholdStr);
						System.out.println("*** Setting new Threshold to " + newThreshold);
						relayThreshold = newThreshold;
						charBuff = new StringBuffer();
						reset();
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
			}
			delay(200); // 200ms between keys
		}
		reset();
		display("Bye-bye");
		System.out.println("...Bye");
		kbc.shutdown();
		keepReading = false;
		delay(1_000); // Wait for the threads to end properly

		// Final cleanup.
		clear();
		oled.shutdown();
		rm.set("off");
		rm.shutdown();
		System.exit(0);
	}

	public void reset() throws Exception {
		synchronized (sb) {
			synchronized (oled) {
				sb.clear();
				oled.clear();
				sb.text("## = Exit, * = Reset.", 2, 10);
				oled.setBuffer(sb.getScreenBuffer());
				oled.display();
				//  clear();
			}
		}
	}

	public void clear() throws Exception {
		synchronized (sb) {
			sb.clear();
			oled.clear();
			oled.setBuffer(sb.getScreenBuffer());
			oled.display();
		}
	}

	public static void main(String... args) throws Exception {
		if (args.length > 0) {
			try {
				relayThreshold = Integer.parseInt(args[0]);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.err.println("Using default value for threshold:" + relayThreshold);
			}
		}
		System.out.println("Relay threshold is " + relayThreshold);
		System.out.println("Hit ## on the keypad to exit");
		OLEDKeypadAndMultiSensor ui = new OLEDKeypadAndMultiSensor();
		ui.userInput();
	}

	private class OneRelayManager {
		private final GpioController gpio = GpioFactory.getInstance();
		private final GpioPinDigitalOutput pin;

		public OneRelayManager() {
			System.out.println("GPIO Control - pin 13/#27... started.");

			// For a relay it seems that HIGH means NC (Normally Closed)...
			// GPIO_27, pin #13, Wiring/PI4J:GPIO_02
			pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Relay", PinState.HIGH);
		}

		public void set(String status) {
			if ("on".equals(status))
				pin.low();
			else
				pin.high();
		}

		public String getStatus() {
			String status = "unknown";

			status = pin.isHigh() ? "off" : "on";
			return status;
		}

		public void shutdown() {
			gpio.shutdown();
		}
	}

}
