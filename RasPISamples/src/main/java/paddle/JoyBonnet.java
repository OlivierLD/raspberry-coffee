package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory;
import i2c.adc.ADS1x15;
import i2c.adc.ADS1x15.Channels;
import java.util.function.Consumer;
import paddle.buttons.PushButtonInstance;

/**
 * Adafruit JoyBonnet for the Raspberry Pi
 * https://www.adafruit.com/product/3464
 *
 * This is a work in progress... Far from completed.
 * Et ce truc m'emmerde.
 */
public class JoyBonnet {
	final static GpioController gpio = GpioFactory.getInstance();

	private ADS1x15 ads1015 = null;
	private final static ADS1x15.ICType ADC_TYPE = ADS1x15.ICType.IC_ADS1015;
	private int gain = ADS1x15.pgaADS1x15.ADS1015_REG_CONFIG_PGA_4_096V.meaning();
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

	public void init() {
		try {
			this.ads1015 = new ADS1x15(ADC_TYPE);
		} catch (I2CFactory.UnsupportedBusNumberException usbne) {
			throw new RuntimeException(usbne);
		}
	}

	public void shutdown() {
		gpio.shutdown();
	}

	private double getChannelVoltage(Channels channel) {
		double voltage = 0f;
		float value = ads1015.readADCSingleEnded(channel,
						this.gain,
						this.sps);
//  System.out.println("Voltage Value:" + value);
		voltage = value / 1_000f;
		return voltage;
	}

	public double getChannel0Voltage() {
		return getChannelVoltage(ADS1x15.Channels.CHANNEL_0);
	}
	public double getChannel1Voltage() {
		return getChannelVoltage(ADS1x15.Channels.CHANNEL_1);
	}
	public double getChannel2Voltage() {
		return getChannelVoltage(ADS1x15.Channels.CHANNEL_2);
	}
	public double getChannel3Voltage() {
		return getChannelVoltage(ADS1x15.Channels.CHANNEL_3);
	}


	public static class ButtonEvent {
		private String payload;
		public ButtonEvent(String payload) {
			this.payload = payload;
		}
		public String getPayload() {
			return this.payload;
		}
	}

	private static boolean keepReading = true;
	private static void stopReading() {
		keepReading = false;
	}
	public static void main(String... args) {

		PushButtonInstance buttonA = new PushButtonInstance(gpio, RaspiPin.GPIO_12, "Button A", (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (A) " + event.toString()));

		JoyBonnet joyBonnet = new JoyBonnet();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized(joyBonnet) {
				joyBonnet.shutdown();
				stopReading();
				try { Thread.sleep(20); } catch (Exception ex) {}
				System.out.println("Bye");
			}
		}, "Shutdown Hook"));
		double[] voltage = {0d, 0d, 0d, 0d};
		double[] read = {0.0, 0.0, 0.0, 0.0};
		while (keepReading) {
			for (int i=0; i<4; i++) {
				try {
					double value = 0.d;
					switch (i) {
						case 0:
							value = joyBonnet.getChannel0Voltage();
							break;
						case 1:
							value = joyBonnet.getChannel1Voltage();
							break;
						case 2:
							value = joyBonnet.getChannel2Voltage();
							break;
						case 3:
							value = joyBonnet.getChannel3Voltage();
							break;
						default:
							break;
					}
					read[i] = value;
					if (Math.abs(read[i] - voltage[i]) > 0.01) {
						System.out.println(String.format("Channel %d: %d: %f", i, System.currentTimeMillis(), read));
						voltage[i] = read[i];
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try { Thread.sleep(10); } catch (Exception ex) {}
		}
		System.out.println("\nDone reading.");
	}
}
