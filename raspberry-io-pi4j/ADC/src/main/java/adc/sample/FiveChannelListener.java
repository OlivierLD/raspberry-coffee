package adc.sample;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

import adc.utils.EscapeSeq;

import analogdigitalconverter.mcp.MCPReader;
import org.fusesource.jansi.AnsiConsole;

import static utils.StringUtils.lpad;

public class FiveChannelListener {
	private final static boolean DEBUG = false;
	private final static String STR100 = "                                                                                                    ";

	private final static int DIGITAL_OPTION = 0;
	private final static int ANALOG_OPTION = 1;

	private static int displayOption = ANALOG_OPTION;

	private static MCPReader.MCP3008InputChannels channel[] = null;
	private final int[] channelValues = new int[]{0, 0, 0, 0, 0};

	public FiveChannelListener() throws Exception {
		channel = new MCPReader.MCP3008InputChannels[]
				{
						MCPReader.MCP3008InputChannels.CH0,
						MCPReader.MCP3008InputChannels.CH1,
						MCPReader.MCP3008InputChannels.CH2,
						MCPReader.MCP3008InputChannels.CH3,
						MCPReader.MCP3008InputChannels.CH4
				};
		final ADCObserver obs = new ADCObserver(channel);

		final String[] channelColors = new String[]{EscapeSeq.ANSI_RED, EscapeSeq.ANSI_WHITE, EscapeSeq.ANSI_YELLOW, EscapeSeq.ANSI_GREEN, EscapeSeq.ANSI_BLUE};

		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(MCPReader.MCP3008InputChannels inputChannel, int newValue) {
//         if (inputChannel.equals(channel))
				{
					int ch = inputChannel.ch();
					int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
					channelValues[ch] = volume;
					if (DEBUG)
						System.out.println("readAdc:" + Integer.toString(newValue) +
								" (0x" + lpad(Integer.toString(newValue, 16).toUpperCase(), 2, "0") +
								", 0&" + lpad(Integer.toString(newValue, 2), 8, "0") + ")");
					if (displayOption == DIGITAL_OPTION) {
						String output = "";
						for (int chan = 0; chan < channel.length; chan++)
							output += "Ch " + Integer.toString(chan) + ", Volume:" + channelValues[chan] + "%    ";
						System.out.println(output.trim());
					} else if (displayOption == ANALOG_OPTION) {
						for (int chan = 0; chan < channel.length; chan++) {
							String str = "";
							for (int i = 0; i < channelValues[chan]; i++)
								str += ".";
							try {
								str = EscapeSeq.superpose(str, "Ch " + Integer.toString(chan) + ": " + Integer.toString(channelValues[chan]) + "%");
								AnsiConsole.out.println(EscapeSeq.ansiLocate(2, 2 + chan + 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
								AnsiConsole.out.println(EscapeSeq.ansiLocate(2, 2 + chan + 1) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, channelColors[chan]) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT);
							} catch (Exception ex) {
								System.out.println(str);
							}
						}
					}
				}
			}
		});
		obs.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (obs != null) {
					obs.stop();
				}
			}, "Shutdown Hook"));
	}

	public static void main(String... args) throws Exception {
		if (displayOption == ANALOG_OPTION) {
			AnsiConsole.systemInstall();
			AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + "Displaying channels' volume");
		}
		// Channels are hard-coded
		new FiveChannelListener();
	}
}
