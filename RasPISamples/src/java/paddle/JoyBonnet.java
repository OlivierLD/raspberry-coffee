package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory;
import i2c.adc.ADS1x15;
import java.util.function.Consumer;

/**
 * Adafruit JoyBonnet for the Raspberry PI
 * https://www.adafruit.com/product/3464
 */
public class JoyBonnet {
	final static GpioController gpio = GpioFactory.getInstance();

	public enum AdcMode {
		SDL_MODE_INTERNAL_AD,
		SDL_MODE_I2C_ADS1015
	}

	// sample mode means return immediately.
	// The wind speed is averaged at sampleTime or when you ask, whichever is longer.
	// Delay mode means to wait for sampleTime and the average after that time.
	public enum SdlMode {
		SAMPLE,
		DELAY
	}

	private AdcMode ADMode = AdcMode.SDL_MODE_I2C_ADS1015;

	private int sampleTime = 5;
	private SdlMode selectedMode = SdlMode.SAMPLE;
	private long startSampleTime = 0L;

	private ADS1x15 ads1015 = null;
	private final static ADS1x15.ICType ADC_TYPE = ADS1x15.ICType.IC_ADS1015;
	private int gain = 6144;
	private int sps = 250;

	// TODO Listen to the buttons (8 buttons)

	private final static int BUTTON_A = 12;
	private final static int BUTTON_B = 6;
	private final static int BUTTON_X = 16;
	private final static int BUTTON_Y = 13;
	private final static int SELECT = 20;
	private final static int START = 26;
	private final static int PLAYER1 = 23;
	private final static int PLAYER2 = 22;
//BUTTONS = [BUTTON_A, BUTTON_B, BUTTON_X, BUTTON_Y, SELECT, START, PLAYER1, PLAYER2]

	private final static int ANALOG_THRESH_NEG = -600;
	private final static int ANALOG_THRESH_POS = 600;
//analog_states = [False, False, False, False]  # up down left right

//	KEYS= { # EDIT KEYCODES IN THIS TABLE TO YOUR PREFERENCES:
//	# See /usr/include/linux/input.h for keycode names
//	# Keyboard        Bonnet        EmulationStation
//		BUTTON_A: e.KEY_LEFTCTRL, # 'A' button
//		BUTTON_B: e.KEY_LEFTALT,  # 'B' button
//		BUTTON_X: e.KEY_Z,        # 'X' button
//		BUTTON_Y: e.KEY_X,        # 'Y' button
//		SELECT:   e.KEY_SPACE,    # 'Select' button
//		START:    e.KEY_ENTER,    # 'Start' button
//		PLAYER1:  e.KEY_1,        # '#1' button
//		PLAYER2:  e.KEY_2,        # '#2' button
//		1000:     e.KEY_UP,       # Analog up
//		1001:     e.KEY_DOWN,     # Analog down
//		1002:     e.KEY_LEFT,     # Analog left
//		1003:     e.KEY_RIGHT,    # Analog right
//	}

	public void init(AdcMode ADMode) {
		try {
			this.ads1015 = new ADS1x15(ADC_TYPE);
		} catch (I2CFactory.UnsupportedBusNumberException usbne) {
			throw new RuntimeException(usbne);
		}
		this.ADMode = ADMode;
	}

	public JoyBonnet() {
		init(AdcMode.SDL_MODE_I2C_ADS1015);
	}

	public void shutdown() {
		gpio.shutdown();
	}

	public double getChannel1Voltage() {
		double voltage = 0f;
		if (this.ADMode == AdcMode.SDL_MODE_I2C_ADS1015) {
			float value = ads1015.readADCSingleEnded(ADS1x15.Channels.CHANNEL_1,
							this.gain,
							this.sps);
//    System.out.println("Voltage Value:" + value);
			voltage = value / 1000f;
		} else {
			// user internal A/D converter
			voltage = 0.0f;
		}
		return voltage;
	}


	static class ButtonEvent {

	}

	private static boolean keepReading = true;
	private static void stopReading() {
		keepReading = false;
	}
	public static void main(String... args) {

		Consumer<ButtonEvent> buttonAConsumer = (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (A) " + event.toString());
		PushButtonInstance buttonA = new PushButtonInstance(gpio, RaspiPin.GPIO_12, buttonAConsumer);

		JoyBonnet joyBonnet = new JoyBonnet();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized(joyBonnet) {
				joyBonnet.shutdown();
				stopReading();
				try { Thread.sleep(20); } catch (Exception ex) {}
				System.out.println("Bye");
			}
		}));
		double voltage = 0d;
		double read = 0.0;
		while (keepReading) {
			try {
				read = joyBonnet.getChannel1Voltage();
				if (Math.abs(read - voltage) > 0.01) {
					System.out.println(String.format("%d: %f", System.currentTimeMillis(), read));
					voltage = read;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try { Thread.sleep(10); } catch (Exception ex) {}
		}
		System.out.println("Done reading.");
	}
}
