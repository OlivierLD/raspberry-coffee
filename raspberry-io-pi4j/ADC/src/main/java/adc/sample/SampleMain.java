package adc.sample;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

import adc.utils.EscapeSeq;

import analogdigitalconverter.mcp.MCPReader;
import org.fusesource.jansi.AnsiConsole;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.StringUtils.lpad;

/**
 * See the {@link #main(String...)} method for runtime CLI parameters.
 */
public class SampleMain {
	private static boolean DEBUG = false;
	// 100 character string
	private final static String STR100 = "                                                                                                    ";

	private final static int DIGITAL_OPTION = 0;
	private final static int ANALOG_OPTION = 1;

	private static int displayOption = ANALOG_OPTION;

	final String[] channelColors = new String[]{EscapeSeq.ANSI_RED, EscapeSeq.ANSI_BLUE, EscapeSeq.ANSI_YELLOW, EscapeSeq.ANSI_GREEN, EscapeSeq.ANSI_WHITE};
	private MCPReader.MCP3008InputChannels channel = null;

	public SampleMain(int ch) throws Exception {
		channel = findChannel(ch);
		final ADCObserver obs = new ADCObserver(channel); // Note: We could instantiate more than one observer (on several channels).
		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(MCPReader.MCP3008InputChannels inputChannel, int newValue) {
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

		final Thread currentThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (obs != null) {
				obs.stop();
			}
			synchronized (currentThread) {
//                currentThread.notify(); // No thread is waiting...
				try {
					currentThread.join();
					System.out.println("... Joining");
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}, "Shutdown Hook"));
	}

	private final static String DISPLAY_PREFIX = "--display:";
	private final static String ANALOG_VALUE = "ANALOG";
	private final static String DIGITAL_VALUE = "DIGITAL";
	private final static String CHANNEL_PREFIX = "--channel:";
	private final static String DEBUG_PREFIX = "--debug:";
	private final static String HELP_PREFIX = "--help";

	/**
	 * Main for MCP3008 test. Reads one channel of the MCP3008. Ctrl-C to stop.
	 *
	 * @param args Optional --display:ANALOG|DIGITAL --channel:X, where X in [0..7] --help
	 * @throws Exception when failure
	 */
	public static void main(String... args) throws Exception {
		AtomicInteger channel = new AtomicInteger(0);

		Arrays.stream(args).forEach(arg -> {
			if (arg.startsWith(DISPLAY_PREFIX)) {
				String displayValue = arg.substring(DISPLAY_PREFIX.length());
				switch (displayValue) {
					case ANALOG_VALUE:
						displayOption = ANALOG_OPTION;
						break;
					case DIGITAL_VALUE:
						displayOption = DIGITAL_OPTION;
						break;
					default:
						System.err.printf("Un-managed display value: %s%n", displayValue);
						break;
				}
			} else if (arg.startsWith(CHANNEL_PREFIX)) {
				channel.set(Integer.parseInt(arg.substring(CHANNEL_PREFIX.length())));
			} else if (arg.equals(DEBUG_PREFIX)) {
				DEBUG = "true".equals(arg.substring(DEBUG_PREFIX.length()));
			} else if (arg.equals(HELP_PREFIX)) {
				System.out.println("Usage is:");
				System.out.printf("java %s %sOPTION %sX %strue|false %s\n", SampleMain.class.getName(), DISPLAY_PREFIX, CHANNEL_PREFIX, DEBUG_PREFIX, HELP_PREFIX);
				System.out.println("Where:");
				System.out.println("OPTION is DIGITAL or ANALOG (default ANALOG)");
				System.out.println("X is in [0..7] (default 0)");
				System.exit(0);
			} else {
				System.err.printf("Un-managed CLI parameter %s%n", arg);
			}
		});

		if (displayOption == ANALOG_OPTION) {
			AnsiConsole.systemInstall();
			AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
		}

		new SampleMain(channel.get());
	}

	private static MCPReader.MCP3008InputChannels findChannel(int ch) throws IllegalArgumentException {
		MCPReader.MCP3008InputChannels channel = null;
		switch (ch) {
			case 0:
				channel = MCPReader.MCP3008InputChannels.CH0;
				break;
			case 1:
				channel = MCPReader.MCP3008InputChannels.CH1;
				break;
			case 2:
				channel = MCPReader.MCP3008InputChannels.CH2;
				break;
			case 3:
				channel = MCPReader.MCP3008InputChannels.CH3;
				break;
			case 4:
				channel = MCPReader.MCP3008InputChannels.CH4;
				break;
			case 5:
				channel = MCPReader.MCP3008InputChannels.CH5;
				break;
			case 6:
				channel = MCPReader.MCP3008InputChannels.CH6;
				break;
			case 7:
				channel = MCPReader.MCP3008InputChannels.CH7;
				break;
			default:
				throw new IllegalArgumentException("No channel " + Integer.toString(ch));
		}
		return channel;
	}
}
