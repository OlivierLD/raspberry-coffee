package adc.sample;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.function.Consumer;

import utils.StringUtils;

public class BatteryMonitor {
	private static boolean debug = false;
	private static boolean calib = false;
	private ADCObserver.MCP3008_input_channels channel = null;
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final static TimeZone HERE = TimeZone.getTimeZone("America/Los_Angeles");

	static {
		SDF.setTimeZone(HERE);
	}

	private final static NumberFormat VF = new DecimalFormat("00.00");

	private static BufferedWriter bw = null;
	private static ADCObserver obs;

	private long lastLogTimeStamp = 0;
	private int lastVolumeLogged = 0;

	private static String logFileName = "battery.log";

	public void stop() {
		if (obs != null) {
			obs.stop();
		}
	}

	public static class ADCData {
		int volume;
		int newValue;
		float voltage;

		public int getVolume() {
			return volume;
		}

		public int getNewValue() {
			return newValue;
		}

		public float getVoltage() {
			return voltage;
		}

		public ADCData(int volume, int newValue, float voltage) {
			this.volume = volume;
			this.newValue = newValue;
			this.voltage = voltage;
		}
	}

	private Consumer<ADCData> processor = this::defaultProcessor;

	private void defaultProcessor(ADCData adcData) {
		// Log the voltage, along with the date and ADC val.
		String line = SDF.format(GregorianCalendar.getInstance(HERE).getTime()) + ";" + adcData.newValue + ";" + adcData.volume + ";" + VF.format(adcData.voltage);
		try {
			bw.write(line + "\n");
			bw.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setProcessor(Consumer<ADCData> processor) {
		this.processor = processor;
	}

	public BatteryMonitor(int ch) throws Exception {
		this(ch, null);
	}

	public BatteryMonitor(int ch, Consumer<ADCData> processor) throws Exception {
		channel = findChannel(ch);

		if (tuning) {
			minADC = 0;
			minVolt = 0f;
			maxADC = tuningADC;
			maxVolt = tuningVolt;
		}
		final int deltaADC = maxADC - minADC;
		final float deltaVolt = maxVolt - minVolt;
		final float b = ((maxVolt * minADC) - (minVolt * maxADC)) / deltaADC;
		final float a = (maxVolt - b) / maxADC;
		if (debug) {
			System.out.println("Volt [" + minVolt + ", " + maxVolt + "]");
			System.out.println("ADC  [" + minADC + ", " + maxADC + "]");
			System.out.println("a=" + a + ", b=" + b);
		}
		System.out.println("Value range: ADC=0 => V=" + b + ", ADC=1023 => V=" + ((a * 1023) + b));
		obs = new ADCObserver(channel); // Note: We could instantiate more than one observer (on several channels).
		bw = new BufferedWriter(new FileWriter(logFileName));

		if (processor != null) {
			this.setProcessor(processor);
		}
		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) {
				if (inputChannel.equals(channel)) {
					long now = System.currentTimeMillis();
					if (calib || Math.abs(now - lastLogTimeStamp) > 1_000) {
						int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
						if (Math.abs(volume - lastVolumeLogged) > 1) // 1 %
						{
							float voltage = 0;
							if (false) {
								if (newValue < minADC) {
									voltage = /* 0 + */ minVolt * ((float) newValue / (float) minADC);
								} else if (newValue >= minADC && newValue <= maxADC) {
									voltage = minVolt + (deltaVolt * (float) (newValue - minADC) / (float) deltaADC);
								} else { // value > maxADC
									voltage = maxVolt + ((15 - maxVolt) * (float) (newValue - maxADC) / (float) (1023 - maxADC));
								}
							} else {
								voltage = (a * newValue) + b;
							}
							if (debug) {
								System.out.print("readAdc:" + Integer.toString(newValue) +
										" (0x" + StringUtils.lpad(Integer.toString(newValue, 16).toUpperCase(), 2, "0") +
										", 0&" + StringUtils.lpad(Integer.toString(newValue, 2), 8, "0") + ") ");
								System.out.println("Volume:" + volume + "% (" + newValue + ") Volt:" + VF.format(voltage));
							}

							processor.accept(new ADCData(volume, newValue, voltage));

							lastLogTimeStamp = now;
							lastVolumeLogged = volume;
						}
					}
				}
			}
		});
		obs.start();
	}

	private final static String DEBUG_PRM = "-debug=";
	private final static String CALIBRATION_PRM = "-calibration";
	private final static String CAL_PRM = "-cal";
	private final static String CHANNEL_PRM = "-ch=";
	private final static String MIN_VALUE = "-min=";
	private final static String MAX_VALUE = "-max=";
	private final static String TUNE_VALUE = "-tune=";
	private final static String SCALE_PRM = "-scale=";
	private final static String LOG_PRM = "-log=";

	private static int minADC = 0;
	private static int maxADC = 1023;
	private static float minVolt = 0f;
	private static float maxVolt = 15f;
	private static float tuningVolt = 15f;
	private static int tuningADC = 1023;
	private static boolean scale = false;
	private static boolean tuning = false;

	public static void main(String[] args) throws Exception {
		System.out.println("Parameters are:");
		System.out.println("  -calibration or -cal");
		System.out.println("  -debug=y|n|yes|no|true|false - example -debug=y        (default is n)");
		System.out.println("  -ch=[0-7]                    - example -ch=0           (default is 0)");
		System.out.println("  -min=minADC:minVolt          - example -min=280:3.75   (default is    0:0.0)");
		System.out.println("  -max=maxADC:maxVolt          - example -min=879:11.25  (default is 1023:15.0)");
		System.out.println("  -tune=ADC:volt               - example -tune=973:12.6  (default is 1023:15.0)");
		System.out.println("  -scale=y|n                   - example -scale=y        (default is n)");
		System.out.println("  -log=[log-file-name]         - example -log=[batt.csv] (default is battery.log)");
		System.out.println("");
		System.out.println(" -min & -max are required if -tune is not here, and vice versa.");
		int channel = 0;
		for (String prm : args) {
			if (prm.startsWith(CHANNEL_PRM))
				channel = Integer.parseInt(prm.substring(CHANNEL_PRM.length()));
			else if (prm.startsWith(CALIBRATION_PRM) || prm.startsWith(CAL_PRM)) {
				debug = true;
				calib = true;
			} else if (!debug && prm.startsWith(DEBUG_PRM))
				debug = ("y".equals(prm.substring(DEBUG_PRM.length())) ||
						"yes".equals(prm.substring(DEBUG_PRM.length())) ||
						"true".equals(prm.substring(DEBUG_PRM.length())));
			else if (prm.startsWith(SCALE_PRM))
				scale = ("y".equals(prm.substring(SCALE_PRM.length())));
			else if (prm.startsWith(LOG_PRM))
				logFileName = prm.substring(LOG_PRM.length());
			else if (prm.startsWith(TUNE_VALUE)) {
				tuning = true;
				String val = prm.substring(TUNE_VALUE.length());
				tuningADC = Integer.parseInt(val.substring(0, val.indexOf(":")));
				tuningVolt = Float.parseFloat(val.substring(val.indexOf(":") + 1));
			} else if (prm.startsWith(MIN_VALUE)) {
				String val = prm.substring(MIN_VALUE.length());
				minADC = Integer.parseInt(val.substring(0, val.indexOf(":")));
				minVolt = Float.parseFloat(val.substring(val.indexOf(":") + 1));
			} else if (prm.startsWith(MAX_VALUE)) {
				String val = prm.substring(MAX_VALUE.length());
				maxADC = Integer.parseInt(val.substring(0, val.indexOf(":")));
				maxVolt = Float.parseFloat(val.substring(val.indexOf(":") + 1));
			}
		}
		String prms = "Prms: ADC Channel:" + channel;
		if (tuning) {
			prms += ", tuningADC:" + tuningADC + ", tuningVolt:" + tuningVolt;
		} else {
			prms += ", MinADC:" + minADC + ", MinVolt:" + minVolt + ", MaxADC:" + maxADC + ", maxVolt:" + maxVolt;
		}
		System.out.println(prms);
		if (scale) {
			if (tuning) {
				minADC = 0;
				minVolt = 0f;
				maxADC = tuningADC;
				maxVolt = tuningVolt;
			}
			final int deltaADC = maxADC - minADC;
			final float deltaVolt = maxVolt - minVolt;

			float b = ((maxVolt * minADC) - (minVolt * maxADC)) / deltaADC;
			float a = (maxVolt - b) / maxADC;

			//  System.out.println("a=" + a + "(" + ((maxVolt - b) / maxADC) + "), b=" + b);

			System.out.println("=== Scale ===");
			System.out.println("Value range: ADC:0 => V:" + b + ", ADC:1023 => V:" + ((a * 1023) + b));
			System.out.println("Coeff A:" + a + ", coeff B:" + b);
			for (int i = 0; i < 1024; i++) {
				System.out.println(i + ";" + ((a * i) + b));
			}
			System.out.println("=============");
			System.exit(0);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nShutting down");
			if (bw != null) {
				System.out.println("Closing log file");
				try {
					bw.flush();
					bw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (obs != null)
				obs.stop();
		}));
		new BatteryMonitor(channel);
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
