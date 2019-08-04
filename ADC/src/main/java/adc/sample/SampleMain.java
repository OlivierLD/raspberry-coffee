package adc.sample;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

import adc.utils.EscapeSeq;

import org.fusesource.jansi.AnsiConsole;

import static utils.StringUtils.lpad;

public class SampleMain {
	private final static boolean DEBUG = false;
	private final static String STR100 = "                                                                                                    ";

	private final static int DIGITAL_OPTION = 0;
	private final static int ANALOG_OPTION = 1;

	private static int displayOption = ANALOG_OPTION;

	final String[] channelColors = new String[]{EscapeSeq.ANSI_RED, EscapeSeq.ANSI_BLUE, EscapeSeq.ANSI_YELLOW, EscapeSeq.ANSI_GREEN, EscapeSeq.ANSI_WHITE};
	private ADCObserver.MCP3008_input_channels channel = null;

	public SampleMain(int ch) throws Exception {
		channel = findChannel(ch);
		final ADCObserver obs = new ADCObserver(channel); // Note: We could instantiate more than one observer (on several channels).
		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) {
				if (inputChannel.equals(channel)) {
					int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
					if (DEBUG) {
						System.out.println("readAdc:" + Integer.toString(newValue) +
								" (0x" + lpad(Integer.toString(newValue, 16).toUpperCase(), 2, "0") +
								", 0&" + lpad(Integer.toString(newValue, 2), 8, "0") + ")");
					}
					if (displayOption == DIGITAL_OPTION) {
						System.out.println("Volume:" + volume + "% (" + newValue + ")");
					} else if (displayOption == ANALOG_OPTION) {
						String str = "";
						for (int i = 0; i < volume; i++)
							str += ".";
						try {
							str = EscapeSeq.superpose(str, "Ch " + Integer.toString(inputChannel.ch()) + ": " + Integer.toString(volume) + "%");
							AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
							AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, channelColors[inputChannel.ch()]) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT);
						} catch (Exception ex) {
							System.out.println(str);
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
		}));
	}

	public static void main(String... args) throws Exception {
		if (displayOption == ANALOG_OPTION) {
			AnsiConsole.systemInstall();
			AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
		}

		int channel = 0;
		if (args.length > 0) {
			channel = Integer.parseInt(args[0]);
		}
		new SampleMain(channel);
	}

	private static ADCObserver.MCP3008_input_channels findChannel(int ch) throws IllegalArgumentException {
		ADCObserver.MCP3008_input_channels channel = null;
		switch (ch) {
			case 0:
				channel = ADCObserver.MCP3008_input_channels.CH0;
				break;
			case 1:
				channel = ADCObserver.MCP3008_input_channels.CH1;
				break;
			case 2:
				channel = ADCObserver.MCP3008_input_channels.CH2;
				break;
			case 3:
				channel = ADCObserver.MCP3008_input_channels.CH3;
				break;
			case 4:
				channel = ADCObserver.MCP3008_input_channels.CH4;
				break;
			case 5:
				channel = ADCObserver.MCP3008_input_channels.CH5;
				break;
			case 6:
				channel = ADCObserver.MCP3008_input_channels.CH6;
				break;
			case 7:
				channel = ADCObserver.MCP3008_input_channels.CH7;
				break;
			default:
				throw new IllegalArgumentException("No channel " + Integer.toString(ch));
		}
		return channel;
	}
}
