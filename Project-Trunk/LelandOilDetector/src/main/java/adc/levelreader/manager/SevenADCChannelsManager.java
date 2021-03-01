package adc.levelreader.manager;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;
import adc.levelreader.ADCObserverSimulator;
import adc.levelreader.main.LelandPrototype;
import adc.utils.EscapeSeq;
import adc.utils.LowPassFilter;
import analogdigitalconverter.mcp.MCPReader;
import org.fusesource.jansi.AnsiConsole;
import utils.StringUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

public class SevenADCChannelsManager {
	// Thresholds are in %
	private final static int WATER_THRESHOLD = Integer.parseInt(LelandPrototype.getAppProperties().getProperty("water.threshold", "75"));
	//private final static int OIL_THRESHOLD   = Integer.parseInt(LelandPrototype.getAppProperties().getProperty("oil.threshold",          "60"));
	private final static double ALFA = Double.parseDouble(LelandPrototype.getAppProperties().getProperty("low.pass.filter.alfa", "0.5"));
	private final static int START_AFTER = Integer.parseInt(LelandPrototype.getAppProperties().getProperty("start.after", "30"));
	private final static long BETWEEN_LOOPS = Long.parseLong(LelandPrototype.getAppProperties().getProperty("between.loops", "100"));

	private final static Format DF4 = new DecimalFormat("#000");
	private final static Format DF32 = new DecimalFormat("#0.00");

	public enum Material {
		UNKNOWN,
		AIR,
		WATER /*,
      OIL */
	}

	/*
	 * Some samples:
	 * - Water : above 50%
	 * - Oil   : 30-40%
	 * - Air   : less than 30%
	 */

	protected static MCPReader.MCP3008InputChannels channel[] = new MCPReader.MCP3008InputChannels[] {
					MCPReader.MCP3008InputChannels.CH0,
					MCPReader.MCP3008InputChannels.CH1,
					MCPReader.MCP3008InputChannels.CH2,
					MCPReader.MCP3008InputChannels.CH3,
					MCPReader.MCP3008InputChannels.CH4,
					MCPReader.MCP3008InputChannels.CH5,
					MCPReader.MCP3008InputChannels.CH6
			};

	private final int[] channelValues = new int[]{0, 0, 0, 0, 0, 0, 0}; // [0..1023]
	private final int[] channelVolumes = new int[]{0, 0, 0, 0, 0, 0, 0}; // [0..100] %

	/* Used to smooth the values */
	private final float[] smoothedChannelVolumes = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f};
	private final List<Integer>[] smoothedChannel = new List[7];
	private final static int WINDOW_WIDTH = Integer.parseInt(LelandPrototype.getAppProperties().getProperty("smooth.width", "10")); // For smoothing

//private int currentLevel = 0;

	final ADCObserver obs;
	private long started = 0;

	/* Uses 7 channels among the 8 available */
	public SevenADCChannelsManager(final AirWaterInterface client) throws Exception {
		for (int i = 0; i < smoothedChannel.length; i++) {
			smoothedChannel[i] = new ArrayList<Integer>(WINDOW_WIDTH);
		}
		if ("true".equals(LelandPrototype.getAppProperties().getProperty("simulate.adc", "false"))) {
			obs = new ADCObserverSimulator(channel); // Simulator
		} else {
			obs = new ADCObserver(channel);
		}

		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(MCPReader.MCP3008InputChannels inputChannel, int newValue) {
				// new value [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
//         if (inputChannel.equals(channel))
				{
					int ch = inputChannel.ch();

					if ("true".equals(LelandPrototype.getAppProperties().getProperty("log.channels", "false"))) {
						try {
							String data = Integer.toString(newValue) + ";";
							LelandPrototype.getChannelLoggers()[ch].write(data);
							LelandPrototype.getChannelLoggers()[ch].flush();
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}

					int volume = (int) (newValue / 10.23); // volume in % [0..100].
					channelValues[ch] = newValue;
					channelVolumes[ch] = volume;

					smoothedChannel[ch].add(volume);
					while (smoothedChannel[ch].size() > WINDOW_WIDTH) {
						smoothedChannel[ch].remove(0);
					}
					// smoothedChannel[ch] = LowPassFilter.lowPass2(smoothedChannel[ch], 0.5);
					smoothedChannelVolumes[ch] = smooth(ch);

					Material material = Material.UNKNOWN;
					float val = smoothedChannelVolumes[ch];
					// Material set with the SMOOTHED value
					if (val > WATER_THRESHOLD) {
						material = Material.WATER;
						//    else if (val > OIL_THRESHOLD)
						//      material = Material.OIL;
					} else {
						material = Material.AIR;
					}
					//    System.out.println("Channel " + ch + ", Material:" + material + ", volume:" + volume + " (smmoth:" + val + ")");
					// Start after a while
					long now = System.currentTimeMillis();
					if ((now - started) > (START_AFTER * 1_000)) {
						client.setStarted((now - started) > ((START_AFTER + 5) * 1_000)); // After 5 more secs...
						client.setTypeOfChannel(ch, material, val); //volume); // send the smoothed value, and material.
					} else {
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1));
						AnsiConsole.out.println("Will start in " + (START_AFTER - ((int) (now - started) / 1_000)) + " s (" + WATER_THRESHOLD + /*", " + OIL_THRESHOLD + */ ")             ");
					}

//           int maxLevel = 0;
//           for (int chan=0; chan<channel.length; chan++)
//           {
//             if (channelVolumes[chan] > WATER_THRESHOLD)
//               maxLevel = Math.max(chan+1, maxLevel);
//           }

					// DEBUG
					if ("true".equals(System.getProperty("debug", "false"))) {
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 25 + ch));
						AnsiConsole.out.print("Channel " + ch + ": Value " + StringUtils.lpad(Integer.toString(newValue), 4) +
								", " + StringUtils.lpad(Integer.toString(volume), 3) + " (inst)" +
								", " + StringUtils.lpad(DF32.format(val), 6) + " (avg)" +
								", " + smoothedChannel[ch].size() + " val. : ");
						for (int vol : smoothedChannel[ch]) {
							AnsiConsole.out.print(DF4.format(vol) + " ");
						}
						AnsiConsole.out.println("    ");
					}
				}
			}
		});
		System.out.println("Start observing.");
		Thread observer = new Thread(() -> {
			started = System.currentTimeMillis();
			try {
				obs.start(-1, BETWEEN_LOOPS); // Tolerance -1: all values, pause
			} catch (ADCObserver.NotOnARaspberryException nore) {
				nore.printStackTrace();
			}
		});
		observer.start();
	}

	public static int getWaterThreshold() {
		return WATER_THRESHOLD;
	}
//public static int getOilThreshold() { return OIL_THRESHOLD; }

	public static MCPReader.MCP3008InputChannels[] getChannel() {
		return channel;
	}

	public void quit() {
		System.out.println("Stop observing.");
		if (obs != null) {
			obs.stop();
		}
	}


	private float smooth(int ch) {
		float size = smoothedChannel[ch].size();
		float sigma = 0;
		if (false) { // Average
			for (int v : smoothedChannel[ch]) {
				sigma += v;
			}
		} else {
			List<Integer> lpf = LowPassFilter.lowPass2(smoothedChannel[ch], ALFA);
			for (int v : lpf) {
				sigma += v;
			}
		}
		return sigma / size;
	}
}
