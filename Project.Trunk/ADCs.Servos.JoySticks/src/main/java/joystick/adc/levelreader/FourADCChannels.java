package joystick.adc.levelreader;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;
import utils.StringUtils;

public class FourADCChannels {
	private final static boolean DEBUG = false;

	private static ADCObserver.MCP3008_input_channels channel[] = null;
	private final int[] channelValues = new int[]{-1, -1, -1, -1};
	private final int[] calibrationValues = new int[]{-1, -1, -1, -1};

	public FourADCChannels() throws Exception {
		channel = new ADCObserver.MCP3008_input_channels[]
				{
						ADCObserver.MCP3008_input_channels.CH0,
						ADCObserver.MCP3008_input_channels.CH1,
						ADCObserver.MCP3008_input_channels.CH2,
						ADCObserver.MCP3008_input_channels.CH3
				};
		final ADCObserver obs = new ADCObserver(channel);

		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) {
//         if (inputChannel.equals(channel))
				{
					int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
					int ch = inputChannel.ch();
					if (calibrationValues[ch] == -1)
						calibrationValues[ch] = volume;
					else {
						//           channelValues[ch] = newValue;
						channelValues[ch] = volume;
						if (DEBUG)
							System.out.println("readAdc:" + Integer.toString(newValue) +
									" (0x" + StringUtils.lpad(Integer.toString(newValue, 16).toUpperCase(), 2, "0") +
									", 0&" + StringUtils.lpad(Integer.toString(newValue, 2), 8, "0") + ")");
						String output = "";
						for (int chan = 0; chan < channel.length; chan++)
							output += (channelValues[chan] != calibrationValues[chan] ? "*" : " ");
						output += " || ";
						for (int chan = 0; chan < channel.length; chan++)
							//           output += "Ch " + Integer.toString(chan) + ":" + lpad(Integer.toString(channelValues[chan]), " ", 3) + "%" + (chan != (channel.length - 1)?", ":"");
							output += (Integer.toString(chan) + ":" + StringUtils.lpad(Integer.toString(channelValues[chan]), 4, " ") + (chan != (channel.length - 1) ? " | " : " |"));
						System.out.println(output);
					}
				}
			}
		});
		obs.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (obs != null)
					obs.stop();
			}, "Shutdown Hook"));
	}

	public static void main(String... args) throws Exception {
		System.out.println(args.length + " parameter(s).");
		// Channels are hard-coded
		new FourADCChannels();
	}
}
