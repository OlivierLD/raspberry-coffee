package joystick.adc.levelreader.manager;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;
import adc.utils.EscapeSeq;
import analogdigitalconverter.mcp.MCPReader;
import org.fusesource.jansi.AnsiConsole;
import utils.StringUtils;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

public class SevenADCChannelsManager {
	private final static int WATER_THRESHOLD = Integer.parseInt(System.getProperty("water.threshold", "50"));
	private final static int OIL_THRESHOLD = Integer.parseInt(System.getProperty("oil.threshold", "30"));

	private final static Format DF4 = new DecimalFormat("#000");
	private final static Format DF32 = new DecimalFormat("#0.00");

	public enum Material {
		UNKNOWN,
		AIR,
		WATER,
		OIL
	}

	/*
	 * Some samples:
	 * - Water : above 50%
	 * - Oil   : 30-40%
	 * - Air   : less than 30%
	 */

	private static MCPReader.MCP3008InputChannels channel[] = null;
	private final int[] channelValues = new int[]{0, 0, 0, 0, 0, 0, 0};
	private final int[] channelVolumes = new int[]{0, 0, 0, 0, 0, 0, 0};

	/* Used to smooth the values */
	private final float[] smoothedChannelVolumes = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f};
	@SuppressWarnings("unchecked")
	private final List<Integer>[] smoothedChannel = new List[7];
	private final static int WINDOW_WIDTH = Integer.parseInt(System.getProperty("smooth.width", "100")); // For smoothing

//private int currentLevel = 0;

	final ADCObserver obs;

	/* Uses 7 channels among the 8 available */
	public SevenADCChannelsManager(final AirWaterOilInterface client) throws Exception {
		for (int i = 0; i < smoothedChannel.length; i++)
			smoothedChannel[i] = new ArrayList<>(WINDOW_WIDTH);

		channel = new MCPReader.MCP3008InputChannels[]
				{
						MCPReader.MCP3008InputChannels.CH0,
						MCPReader.MCP3008InputChannels.CH1,
						MCPReader.MCP3008InputChannels.CH2,
						MCPReader.MCP3008InputChannels.CH3,
						MCPReader.MCP3008InputChannels.CH4,
						MCPReader.MCP3008InputChannels.CH5,
						MCPReader.MCP3008InputChannels.CH6
				};
		obs = new ADCObserver(channel);

		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(MCPReader.MCP3008InputChannels inputChannel, int newValue) {
//         if (inputChannel.equals(channel))
				{
					int ch = inputChannel.ch();
					int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
					channelValues[ch] = newValue;
					channelVolumes[ch] = volume;

					smoothedChannel[ch].add(volume);
					while (smoothedChannel[ch].size() > WINDOW_WIDTH) smoothedChannel[ch].remove(0);
					smoothedChannelVolumes[ch] = smooth(ch);

					Material material = Material.UNKNOWN;
					float val = smoothedChannelVolumes[ch];
					if (val > WATER_THRESHOLD)
						material = Material.WATER;
					else if (val > OIL_THRESHOLD)
						material = Material.OIL;
					else
						material = Material.AIR;
					client.setTypeOfChannel(ch, material, volume); // val);

//           int maxLevel = 0;
//           for (int chan=0; chan<channel.length; chan++)
//           {
//             if (channelVolumes[chan] > WATER_THRESHOLD)
//               maxLevel = Math.max(chan+1, maxLevel);
//           }

					// DEBUG
					{
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 24 + ch));
						AnsiConsole.out.print("Channel " + ch + ": Value " + StringUtils.lpad(Integer.toString(newValue), 4, " ") +
								", " + StringUtils.lpad(Integer.toString(volume), 3, " ") + " (inst)" +
								", " + StringUtils.lpad(DF32.format(val), 6, " ") + " (avg)" +
								", " + smoothedChannel[ch].size() + " val. : ");
						for (int vol : smoothedChannel[ch])
							AnsiConsole.out.print(DF4.format(vol) + " ");
						AnsiConsole.out.println("    ");
					}
				}
			}
		});
		System.out.println("Start observing.");
		Thread observer = new Thread(() -> {
			try {
				obs.start(-1, 0L); // Tolerance -1: all values
			} catch (ADCObserver.NotOnARaspberryException nore) {
				nore.printStackTrace();
			}
		});
		observer.start();
	}

	public void quit() {
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
}
