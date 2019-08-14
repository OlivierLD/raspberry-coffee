package joystick.adc;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

/**
 * A two-channel listener. Uses an MCP3008 to get the values of the 2 joystick's channels.
 * An example, for inspiration.
 *
 * Wiring at http://raspberrypi.lediouris.net/joystick/readme.html
 */
public class TwoJoyStick {
	private static ADCObserver.MCP3008_input_channels channel[] = null;
	private final int[] channelValues = new int[]{0, 0, 0, 0}; // (0..100)

	private TwoJoyStickClient joyStickClient = null;
	private int prevUD1Value = 0, prevLR1Value = 0, prevUD2Value = 0, prevLR2Value = 0;

	public TwoJoyStick(TwoJoyStickClient jsc) throws Exception {
		this(jsc, true);
	}
	public TwoJoyStick(TwoJoyStickClient jsc, boolean withHook) throws Exception {
		System.out.println(">> Channel MCP3008 #0: Up-Down, 1");
		System.out.println(">> Channel MCP3008 #1: Left-Right, 1");
		System.out.println(">> Channel MCP3008 #2: Up-Down, 2");
		System.out.println(">> Channel MCP3008 #3: Left-Right, 2");

		joyStickClient = jsc;
		channel = new ADCObserver.MCP3008_input_channels[] {
										ADCObserver.MCP3008_input_channels.CH0, // UD, Joystick One
										ADCObserver.MCP3008_input_channels.CH1, // LR, Joystick One
										ADCObserver.MCP3008_input_channels.CH2, // UD, Joystick Two
										ADCObserver.MCP3008_input_channels.CH3  // LR, Joystick Two
						};
		final ADCObserver obs = new ADCObserver(channel);

		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) {
				int ch = getChannelIndex(channel, inputChannel.ch());
				int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				if ("true".equals(System.getProperty("joystick.verbose", "false"))) {
					System.out.println("\tServo channel:" + ch + ", value " + newValue + ", vol. " + volume + " %.");
					System.out.println(String.format("\tPrev values: %d, %d, %d, %d", prevUD1Value, prevLR1Value, prevUD2Value, prevLR2Value));
				}

				channelValues[ch] = volume;
				if (ch == 0 && volume != prevUD1Value) {
					joyStickClient.setUD1(volume);
					prevUD1Value = volume;
				}
				if (ch == 1 && volume != prevLR1Value) {
					joyStickClient.setLR1(volume);
					prevLR1Value = volume;
				}
				if (ch == 2 && volume != prevUD2Value) {
					joyStickClient.setUD2(volume);
					prevUD2Value = volume;
				}
				if (ch == 3 && volume != prevLR2Value) {
					joyStickClient.setLR2(volume);
					prevLR2Value = volume;
				}
			}
		});
		obs.start();
		if (withHook) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (obs != null)
					obs.stop();
			}, "Shutdown Hook"));
		}
	}

	private static int getChannelIndex(ADCObserver.MCP3008_input_channels[] channelArray, int channel) {
		int idx = -1;
		for (int i=0; i<channelArray.length; i++) {
			if (channelArray[i].ch() == channel) {
				idx = i;
				break;
			}
		}
		return idx;
	}
}
