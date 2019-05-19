package joystick.adc;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;

/**
 * A two-channel listener. Uses an MCP3008 to get the values of the 2 joystick's channels.
 * An example, for inspiration.
 *
 * See joystick's wiring at http://raspberrypi.lediouris.net/joystick/readme.html
 */
public class JoyStick {
	// Default wiring for MCP3008
	private static Pin defaultMiso = PinUtil.GPIOPin.GPIO_13.pin();
	private static Pin defaultMosi = PinUtil.GPIOPin.GPIO_12.pin();
	private static Pin defaultClk  = PinUtil.GPIOPin.GPIO_14.pin();
	private static Pin defaultCs   = PinUtil.GPIOPin.GPIO_10.pin();

	private static ADCObserver.MCP3008_input_channels channel[] = null;
	private final int[] channelValues = new int[]{0, 0}; // (0..100)

	private JoyStickClient joyStickClient = null;
	private int prevUDValue = 0, prevLRValue = 0;

	public JoyStick(JoyStickClient jsc) throws Exception {
		this(jsc, true);
	}
	public JoyStick(JoyStickClient jsc, boolean withHook) throws Exception {
		this(jsc,
				ADCObserver.MCP3008_input_channels.CH0,
				ADCObserver.MCP3008_input_channels.CH1,
				defaultClk,
				defaultMiso,
				defaultMosi,
				defaultCs,
				withHook);
	}
	public JoyStick(JoyStickClient jsc, ADCObserver.MCP3008_input_channels ud, ADCObserver.MCP3008_input_channels lr) throws Exception {
		this(jsc,
				ud,
				lr,
				defaultClk,
				defaultMiso,
				defaultMosi,
				defaultCs,
				true);
	}
	public JoyStick(JoyStickClient jsc,
	                ADCObserver.MCP3008_input_channels ud,
	                ADCObserver.MCP3008_input_channels lr,
	                Pin clk,
	                Pin miso,
	                Pin mosi,
	                Pin cs,
	                boolean withHook) throws Exception {
		System.out.println(String.format(">> Channel MCP3008 #%s: Up-Down", ud.toString()));
		System.out.println(String.format(">> Channel MCP3008 #%s: Left-Right", lr.toString()));

		joyStickClient = jsc;
		channel = new ADCObserver.MCP3008_input_channels[] { ud, lr };
		final ADCObserver obs = new ADCObserver(channel, clk, miso, mosi, cs);

		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) {
				int ch = inputChannel.ch();
				int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				if ("true".equals(System.getProperty("joystick.verbose", "false")))
					System.out.println("\tServo channel:" + ch + ", value " + newValue + ", vol. " + volume + " %.");

				for (int i=0; i<channel.length; i++) {
					if (channel[i].ch() == ch) {
						channelValues[i] = volume;
						break;
					}
				}

				if (ch == channel[0].ch() && volume != prevUDValue)
					joyStickClient.setUD(volume);
				if (ch == channel[1].ch() && volume != prevLRValue)
					joyStickClient.setLR(volume);
			}
		});
		obs.start();
		if (withHook) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (obs != null)
					obs.stop();
			}));
		}
	}
}
