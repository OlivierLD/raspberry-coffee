package joystick.adc.levelreader;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;
import adc.utils.EscapeSeq;
import org.fusesource.jansi.AnsiConsole;
import utils.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class SevenADCChannels {
	private final static boolean DEBUG = "true".equals(System.getProperty("verbose", "false"));
	private final static int WATER_THRESHOLD = Integer.parseInt(System.getProperty("water.threshold", "50"));
	private final static int OIL_THRESHOLD = Integer.parseInt(System.getProperty("oil.threshold", "30"));

	/*
	 * Some samples:
	 * - Water : above 50%
	 * - Oil   : 30-40%
	 * - Air   : less than 30%
	 */

	private final static NumberFormat DF3 = new DecimalFormat("##0");
	private final static NumberFormat DF33 = new DecimalFormat("##0.000");
	private final static NumberFormat DF4 = new DecimalFormat("###0");

	private static ADCObserver.MCP3008_input_channels channel[] = null;
	private final int[] channelValues = new int[]{0, 0, 0, 0, 0, 0, 0};
	private final int[] channelVolumes = new int[]{0, 0, 0, 0, 0, 0, 0};

	/* Used to smooth the values */
	private final float[] smoothedChannelVolumes = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f};
	private final List<Integer>[] smoothedChannel = new List[7];
	private final static int WINDOW_WIDTH = Integer.parseInt(System.getProperty("smooth.width", "100"));

	private int currentLevel = 0;

	final ADCObserver obs;

	/* Uses 7 channels among the 8 available */
	public SevenADCChannels() throws Exception {
		for (int i = 0; i < smoothedChannel.length; i++)
			smoothedChannel[i] = new ArrayList<Integer>(WINDOW_WIDTH);

		channel = new ADCObserver.MCP3008_input_channels[]
				{
						ADCObserver.MCP3008_input_channels.CH0,
						ADCObserver.MCP3008_input_channels.CH1,
						ADCObserver.MCP3008_input_channels.CH2,
						ADCObserver.MCP3008_input_channels.CH3,
						ADCObserver.MCP3008_input_channels.CH4,
						ADCObserver.MCP3008_input_channels.CH5,
						ADCObserver.MCP3008_input_channels.CH6
				};
		obs = new ADCObserver(channel);

		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) {
//         if (inputChannel.equals(channel))
				{
					int ch = inputChannel.ch();
					int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
					channelValues[ch] = newValue;
					channelVolumes[ch] = volume;

					smoothedChannel[ch].add(volume);
					while (smoothedChannel[ch].size() > WINDOW_WIDTH) smoothedChannel[ch].remove(0);
					smoothedChannelVolumes[ch] = smooth(ch);

					if (DEBUG) // A table, with ansi box-drawing characters. Channel, volume, value.
					{
						if (false) {
							System.out.println("readAdc:" + Integer.toString(newValue) +
									" (0x" + StringUtils.lpad(Integer.toString(newValue, 16).toUpperCase(), 2, "0") +
									", 0&" + StringUtils.lpad(Integer.toString(newValue, 2), 8, "0") + ")");
							String output = "";
							for (int chan = 0; chan < channel.length; chan++)
								output += (channelVolumes[chan] > WATER_THRESHOLD ? "*" : " ");
							output += " || ";
							for (int chan = 0; chan < channel.length; chan++)
								//           output += "Ch " + Integer.toString(chan) + ":" + lpad(Integer.toString(channelValues[chan]), " ", 3) + "%" + (chan != (channel.length - 1)?", ":"");
								output += (Integer.toString(chan) + ":" + StringUtils.lpad(Integer.toString(channelVolumes[chan]), 4, " ") + (chan != (channel.length - 1) ? " | " : " |"));
							System.out.println(output);
						}
						// Clear the screen, cursor on top left.
//             AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1));
						boolean ansiBox = false;
						// See http://en.wikipedia.org/wiki/Box-drawing_character
						String str = (ansiBox ? "\u2554\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2557" :
								"+---+-------+------+");
						AnsiConsole.out.println(str);
						str = (ansiBox ? "\u2551 " : "| ") +
								"C" + (ansiBox ? " \u2503 " : " | ") +
								"Vol" + (ansiBox ? " % \u2503 " : " % | ") +
								" Val" + (ansiBox ? " \u2551" : " |");
						AnsiConsole.out.println(str);

						str = (ansiBox ? "\u2554\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2557" :
								"+---+-------+------+");
						AnsiConsole.out.println(str);
						for (int chan = channel.length - 1; chan >= 0; chan--) {
							str = (ansiBox ? "\u2551 " : "| ") +
									Integer.toString(chan) + (ansiBox ? " \u2503 " : " | ") +
									StringUtils.lpad(DF3.format(channelVolumes[chan]), 3, " ") + (ansiBox ? " % \u2503 " : " % | ") +
									StringUtils.lpad(DF4.format(channelValues[chan]), 4, " ") + (ansiBox ? " \u2551" : " |");

							if (smoothedChannelVolumes[chan] > WATER_THRESHOLD)
								str += " Water (~ " + DF33.format(smoothedChannelVolumes[chan]) + ")                 ";
							else if (smoothedChannelVolumes[chan] > OIL_THRESHOLD)
								str += " Oil   (~ " + DF33.format(smoothedChannelVolumes[chan]) + ")                 ";
							else
								str += " Air   (~ " + DF33.format(smoothedChannelVolumes[chan]) + ")                 ";
							AnsiConsole.out.println(str);
						}
						str = (ansiBox ? "\u255a\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2550\u2550\u2550\u255d" :
								"+---+-------+------+");
						AnsiConsole.out.println(str);
					}

					int maxLevel = 0;
					for (int chan = 0; chan < channel.length; chan++) {
						if (channelVolumes[chan] > WATER_THRESHOLD)
							maxLevel = Math.max(chan + 1, maxLevel);
					}
					if (maxLevel != currentLevel) {
						System.out.print("Level : " + maxLevel + " ");
						for (int i = 0; i < channel.length; i++) {
							if (i < maxLevel)
								System.out.print(">>");
							else
								System.out.print("  ");
						}
						System.out.println();
						currentLevel = maxLevel;
					}
				}
			}
		});
		AnsiConsole.out.println(EscapeSeq.ANSI_CLS);

		System.out.println("Start observing.");
		Thread observer = new Thread(() -> {
				try {
					obs.start(0L);
				} catch (ADCObserver.NotOnARaspberryException nore) {
					nore.printStackTrace();
				}
			});
		observer.start();
	}

	private void quit() {
		System.out.println("Stop observing.");
		if (obs != null)
			obs.stop();
	}

	private float smooth(int ch) {
		float size = smoothedChannel[ch].size();
		float sigma = 0;
		for (int v : smoothedChannel[ch])
			sigma += v;

		return sigma / size;
	}

	public static void main(String... args) throws Exception {
		System.out.println(args.length + " parameter(s).");
		// Channels are hard-coded
		final SevenADCChannels sac = new SevenADCChannels();
		final Thread me = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println();
				sac.quit();
				synchronized (me) {
					me.notify();
				}
				System.out.println("Program stopped by user's request.");
			}
		});
		synchronized (me) {
			System.out.println("Main thread waiting...");
			me.wait();
		}
		System.out.println("Done.");
	}
}
