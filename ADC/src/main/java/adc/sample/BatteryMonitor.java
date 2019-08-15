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

import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
import utils.StringUtils;

public class BatteryMonitor {
	private static boolean debug = false;
	private static boolean calib = false;
	private static boolean simulate = false;
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
			if (bw != null) {
				bw.write(line + "\n");
				bw.flush();
			} else {
				System.out.println(line);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setProcessor(Consumer<ADCData> processor) {
		this.processor = processor;
	}
	public Consumer<ADCData> getProcessor() {
		return this.processor;
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
		System.out.println("Value range: ADC=0 => V=" + b + ", ADC=1023 => V=" + ((a * 1_023) + b));
		obs = new ADCObserver(channel, clk, miso, mosi, cs); // Note: We could instantiate more than one observer (on several channels).
		bw = new BufferedWriter(new FileWriter(logFileName));
		if (debug) {
			System.out.println(String.format("Created log-file [%s]", logFileName));
		}

		if (processor != null) {
			this.setProcessor(processor);
		} else {
			this.setProcessor(this::defaultProcessor);
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

							if (getProcessor() != null) {
								getProcessor().accept(new ADCData(volume, newValue, voltage));
							}

							lastLogTimeStamp = now;
							lastVolumeLogged = volume;
						}
					}
				}
			}
		});
		if (!simulate) {
			try {
				obs.start();
			} catch (ADCObserver.NotOnARaspberryException error) {
				System.out.println("Not on a RPi, simulating.");
				simulate = true;
			}
		} else {
			System.out.println("Simulating data...");
		}
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
	private final static String SIMULATE_PRM = "-simulate=";

	private final static String MISO_PRM = "-miso=";
	private final static String MOSI_PRM = "-mosi=";
	private final static String CLK_PRM = "-clk=";
	private final static String CS_PRM = "-cs=";

	private static int minADC = 0;
	private static int maxADC = 1_023;
	private static float minVolt = 0f;
	private static float maxVolt = 15f;
	private static float tuningVolt = 15f;
	private static int tuningADC = 1_023;
	private static boolean scale = false;
	private static boolean tuning = false;

	// Defaults
	private static Pin miso = PinUtil.GPIOPin.GPIO_13.pin();
	private static Pin mosi = PinUtil.GPIOPin.GPIO_12.pin();
	private static Pin clk  = PinUtil.GPIOPin.GPIO_14.pin();
	private static Pin cs   = PinUtil.GPIOPin.GPIO_10.pin();

	public static void main(String... args) throws Exception {
		System.out.println("Parameters are:");
		System.out.println("  -calibration or -cal");
		System.out.println("  -debug=y|n|yes|no|true|false - example -debug=y        (default is n)");
		System.out.println("  -ch=[0-7]                    - example -ch=0           (default is 0)");
		System.out.println("  -min=minADC:minVolt          - example -min=280:3.75   (default is    0:0.0)");
		System.out.println("  -max=maxADC:maxVolt          - example -min=879:11.25  (default is 1023:15.0)");
		System.out.println("  -tune=ADC:volt               - example -tune=973:12.6  (default is 1023:15.0)");
		System.out.println("  -scale=y|n                   - example -scale=y        (default is n)");
		System.out.println("  -simulate=y|n                - example -simulate=y     (default is n)");
		System.out.println("  -log=[log-file-name]         - example -log=[batt.csv] (default is battery.log)");
		System.out.println("");
		System.out.println("  -miso=XX                     - example -miso=9         (default is BCM  9 => GPIO_13)");
		System.out.println("  -mosi=XX                     - example -mosi=10        (default is BCM 10 => GPIO_12)");
		System.out.println("  -clk=XX                      - example -clk=11         (default is BCM 11 => GPIO_14)");
		System.out.println("  -cs=XX                       - example -cs=8           (default is BCM  8 => GPIO_10)");
		System.out.println("");
		System.out.println(" -min & -max are required if -tune is not here, and vice versa.");
		int channel = 0;
		for (String prm : args) {
			if (prm.startsWith(CHANNEL_PRM)) {
				channel = Integer.parseInt(prm.substring(CHANNEL_PRM.length()));
			} else if (prm.startsWith(CALIBRATION_PRM) || prm.startsWith(CAL_PRM)) {
				debug = true;
				calib = true;
			} else if (!debug && prm.startsWith(DEBUG_PRM)) {
				debug = ("y".equals(prm.substring(DEBUG_PRM.length())) ||
						"yes".equals(prm.substring(DEBUG_PRM.length())) ||
						"true".equals(prm.substring(DEBUG_PRM.length())));
			} else if (prm.startsWith(SCALE_PRM)) {
				scale = ("y".equals(prm.substring(SCALE_PRM.length())));
			} else if (prm.startsWith(SIMULATE_PRM)) {
				simulate = ("y".equals(prm.substring(SIMULATE_PRM.length())));
			} else if (prm.startsWith(LOG_PRM)) {
				logFileName = prm.substring(LOG_PRM.length());
			} else if (prm.startsWith(TUNE_VALUE)) {
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
			} else if (prm.startsWith(MISO_PRM)) {
				String pinValue = prm.substring(MISO_PRM.length());
				try {
					int pin = Integer.parseInt(pinValue);
					miso = PinUtil.getPinByGPIONumber(pin);
				} catch (NumberFormatException nfe) {
					System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
				}
			} else if (prm.startsWith(MOSI_PRM)) {
				String pinValue = prm.substring(MOSI_PRM.length());
				try {
					int pin = Integer.parseInt(pinValue);
					mosi = PinUtil.getPinByGPIONumber(pin);
				} catch (NumberFormatException nfe) {
					System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
				}
			} else if (prm.startsWith(CLK_PRM)) {
				String pinValue = prm.substring(CLK_PRM.length());
				try {
					int pin = Integer.parseInt(pinValue);
					clk = PinUtil.getPinByGPIONumber(pin);
				} catch (NumberFormatException nfe) {
					System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
				}
			} else if (prm.startsWith(CS_PRM)) {
				String pinValue = prm.substring(CS_PRM.length());
				try {
					int pin = Integer.parseInt(pinValue);
					cs = PinUtil.getPinByGPIONumber(pin);
				} catch (NumberFormatException nfe) {
					System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
				}
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
			System.out.println("Value range: ADC:0 => V:" + b + ", ADC:1023 => V:" + ((a * 1_023) + b));
			System.out.println("Coeff A:" + a + ", coeff B:" + b);
			for (int i = 0; i < 1_024; i++) {
				System.out.println(i + ";" + ((a * i) + b));
			}
			System.out.println("=============");
			System.exit(0);
		}

		System.out.println(String.format("Reading MCP3008 on channel %d", channel));
		System.out.println(
				" Wiring of the MCP3008-SPI (without power supply):\n" +
						" +---------++-------------------------------------------------+\n" +
						" | MCP3008 || Raspberry Pi                                    |\n" +
						" +---------++------+--------------+------+---------+----------+\n" +
						" |         || Pin# | Name         | Role | GPIO    | wiringPI |\n" +
						" |         ||      |              |      | /BCM    | /PI4J    |\n" +
						" +---------++------+--------------+------+---------+----------+");
		System.out.println(String.format(" | CLK (13)|| #%02d  | %s | CLK  | GPIO_%02d | %02d       |",
				PinUtil.findByPin(clk).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(clk).pinName(), 12, " "),
				PinUtil.findByPin(clk).gpio(),
				PinUtil.findByPin(clk).wiringPi()));
		System.out.println(String.format(" | Din (11)|| #%02d  | %s | MOSI | GPIO_%02d | %02d       |",
				PinUtil.findByPin(mosi).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(mosi).pinName(), 12, " "),
				PinUtil.findByPin(mosi).gpio(),
				PinUtil.findByPin(mosi).wiringPi()));
		System.out.println(String.format(" | Dout(12)|| #%02d  | %s | MISO | GPIO_%02d | %02d       |",
				PinUtil.findByPin(miso).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(miso).pinName(), 12, " "),
				PinUtil.findByPin(miso).gpio(),
				PinUtil.findByPin(miso).wiringPi()));
		System.out.println(String.format(" | CS  (10)|| #%02d  | %s | CS   | GPIO_%02d | %02d       |",
				PinUtil.findByPin(cs).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(cs).pinName(), 12, " "),
				PinUtil.findByPin(cs).gpio(),
				PinUtil.findByPin(cs).wiringPi()));
		System.out.println(" +---------++------+--------------+-----+----------+----------+");
		System.out.println("Raspberry Pi is the Master, MCP3008 is the Slave:");
		System.out.println("- Dout on the MCP3008 goes to MISO on the RPi");
		System.out.println("- Din on the MCP3008 goes to MOSI on the RPi");
		System.out.println("Pins on the MCP3008 are numbered from 1 to 16, beginning top left, counter-clockwise.");
		System.out.println("       +--------+ ");
		System.out.println(String.format("%s CH0 -+  1  16 +- Vdd ",  (channel == 0 ? "*" : " ")));
		System.out.println(String.format("%s CH1 -+  2  15 +- Vref ", (channel == 1 ? "*" : " ")));
		System.out.println(String.format("%s CH2 -+  3  14 +- aGnd ", (channel == 2 ? "*" : " ")));
		System.out.println(String.format("%s CH3 -+  4  13 +- CLK ",  (channel == 3 ? "*" : " ")));
		System.out.println(String.format("%s CH4 -+  5  12 +- Dout ", (channel == 4 ? "*" : " ")));
		System.out.println(String.format("%s CH5 -+  6  11 +- Din ",  (channel == 5 ? "*" : " ")));
		System.out.println(String.format("%s CH6 -+  7  10 +- CS ",   (channel == 6 ? "*" : " ")));
		System.out.println(String.format("%s CH7 -+  8   9 +- dGnd ", (channel == 7 ? "*" : " ")));
		System.out.println("       +--------+ ");

		// Compose mapping for PinUtil
		String[] map = new String[4];
		map[0] = String.valueOf(PinUtil.findByPin(clk).pinNumber()) + ":" + "CLK";
		map[1] = String.valueOf(PinUtil.findByPin(miso).pinNumber()) + ":" + "Dout";
		map[2] = String.valueOf(PinUtil.findByPin(mosi).pinNumber()) + ":" + "Din";
		map[3] = String.valueOf(PinUtil.findByPin(cs).pinNumber()) + ":" + "CS";

		PinUtil.print(map);

		Thread me = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nShutting down");
			if (debug) {
				System.out.println("Interrupted from here:");
				// Who called me
				Throwable stack = new Throwable();
				stack.printStackTrace(System.out);
			}
			if (bw != null) {
				System.out.println("Closing log file");
				try {
					bw.flush();
					bw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (obs != null) {
				obs.stop();
			}
			synchronized (me) {
				try {
					me.notify();
				} catch (Exception ex) {
					// absorb.
					ex.printStackTrace();
				}
			}
		}, "Shutdown Hook"));

		new BatteryMonitor(channel);

		if (simulate) {
			System.out.println("Waiting...");

			final int ch = channel;
			Thread dataSimulator = new Thread(() -> {
				while (true) {
					int adc = (int) Math.round(Math.random() * 1_024);
					ADCContext.getInstance().fireValueChanged(findChannel(ch), adc);
					try { Thread.sleep(500L); } catch (InterruptedException ie) {}
				}
			});
			dataSimulator.start();

			synchronized (me) {
				me.wait();
				System.out.println("Waking up, exiting.");
			}
		}
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
