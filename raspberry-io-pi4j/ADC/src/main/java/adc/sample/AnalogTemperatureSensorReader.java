package adc.sample;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;
import analogdigitalconverter.mcp.MCPReader;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Read a TMP36 Temperature Sensor (Analog)
 * See https://learn.adafruit.com/tmp36-temperature-sensor?view=all
 * with a MCP3008
 */
public class AnalogTemperatureSensorReader {
	private static boolean debug = false;
	private MCPReader.MCP3008InputChannels channel = null;
	private final static NumberFormat NF = new DecimalFormat("00.00");

	private static ADCObserver obs;

	public AnalogTemperatureSensorReader(int ch) throws Exception {
		channel = findChannel(ch);
		obs = new ADCObserver(channel);
		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(MCPReader.MCP3008InputChannels inputChannel, int newValue) {
				if (inputChannel.equals(channel)) {
					// Logic here
					// We use the 3V3 pin for the input.
					double analogOutputVoltage = newValue * (3_300d / 1_024d);
					double temp = (analogOutputVoltage - 500) / 10;
					System.out.println("Temp:" + NF.format(temp) + " C (" + newValue + ")");
				}
			}
		});
		obs.start(1); // Default is 5
	}

	private final static String DEBUG_PRM = "-debug=";
	private final static String CHANNEL_PRM = "-ch=";

	public static void main(String... args) throws Exception {
		System.out.println("Parameters are:");
		System.out.println("  -debug=y|n|yes|no|true|false - example -debug=y        (default is n)");
		System.out.println("  -ch=[0-7]                    - example -ch=0           (default is 0)");

		int channel = 0;
		for (String prm : args) {
			if (prm.startsWith(CHANNEL_PRM)) {
				channel = Integer.parseInt(prm.substring(CHANNEL_PRM.length()));
			} else if (!debug && prm.startsWith(DEBUG_PRM)) {
				debug = ("y".equals(prm.substring(DEBUG_PRM.length())) ||
						"yes".equals(prm.substring(DEBUG_PRM.length())) ||
						"true".equals(prm.substring(DEBUG_PRM.length())));
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
			public void run() {
				System.out.println("\nShutting down");
				if (obs != null) {
					long before = System.currentTimeMillis();
					obs.stop(this);
					synchronized (this) {
						try {
							this.wait(1_000L);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						long after = System.currentTimeMillis();
						System.out.println("Bye now. (" + Long.toString(after - before) + "ms)");
					}
				}
			}
		});
		new AnalogTemperatureSensorReader(channel);
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
