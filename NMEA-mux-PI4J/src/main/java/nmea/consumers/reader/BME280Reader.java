package nmea.consumers.reader;

import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.BME280;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringGenerator;

import java.util.List;

/**
 * Reads data from an BME280 sensor.
 * Pressure, Humidity and Temperature.
 */
public class BME280Reader extends NMEAReader {

	private BME280 bme280;
	private static final String DEFAULT_DEVICE_PREFIX = "RP";
	private String devicePrefix = DEFAULT_DEVICE_PREFIX;

	private boolean alreadySaidDeviceMissing = false;

	private static final long BETWEEN_LOOPS = 1_000L; // TODO: Make it an external parameter?

	public BME280Reader(List<NMEAListener> al) {
		this(null, al);
	}
	public BME280Reader(String threadName, List<NMEAListener> al) {
		super(threadName, al);
		try {
			this.bme280 = new BME280();
		} catch (I2CFactory.UnsupportedBusNumberException e) {
			e.printStackTrace();
		}
	}

	public String getDevicePrefix() {
		return this.devicePrefix;
	}

	public void setDevicePrefix(String devicePrefix) {
		this.devicePrefix = devicePrefix;
	}

	@Override
	public void startReader() {
		super.enableReading();
		System.out.println(String.format(">> Starting reader [%s] (%s). Enabled:%s", this.getClass().getName(), this.devicePrefix, this.canRead()));
		while (this.canRead()) {
			// Read data every 1 second
			try {
				if (bme280 != null) {
					float humidity = bme280.readHumidity();
					float temperature = bme280.readTemperature();
					float pressure = bme280.readPressure();
					// Generate NMEA String
					int deviceIdx = 0; // Instead of "BME280"...
					String nmeaXDR = StringGenerator.generateXDR(devicePrefix,
							new StringGenerator.XDRElement(StringGenerator.XDRTypes.HUMIDITY,
									humidity,
									String.valueOf(deviceIdx++)), // %, Humidity
							new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
									temperature,
									String.valueOf(deviceIdx++)), // Celsius, Temperature
							new StringGenerator.XDRElement(StringGenerator.XDRTypes.PRESSURE_P,
									pressure,
									String.valueOf(deviceIdx++))); // Pascal, pressure
					nmeaXDR += NMEAParser.NMEA_SENTENCE_SEPARATOR;
					fireDataRead(new NMEAEvent(this, nmeaXDR));

					String nmeaMDA = StringGenerator.generateMDA(devicePrefix,
							pressure / 100,
							temperature,
							-Double.MAX_VALUE,
							humidity,
							-Double.MAX_VALUE,
							-Double.MAX_VALUE,
							-Double.MAX_VALUE,
							-Double.MAX_VALUE,
							-Double.MAX_VALUE);
					nmeaMDA += NMEAParser.NMEA_SENTENCE_SEPARATOR;
					fireDataRead(new NMEAEvent(this, nmeaMDA));

					String nmeaMTA = StringGenerator.generateMTA(devicePrefix, temperature);
					nmeaMTA += NMEAParser.NMEA_SENTENCE_SEPARATOR;
					fireDataRead(new NMEAEvent(this, nmeaMTA));

					String nmeaMMB = StringGenerator.generateMMB(devicePrefix, pressure / 100);
					nmeaMMB += NMEAParser.NMEA_SENTENCE_SEPARATOR;
					fireDataRead(new NMEAEvent(this, nmeaMMB));
				} else {
					if (!alreadySaidDeviceMissing) {
						System.out.println(">> No BME280 device found.");
						alreadySaidDeviceMissing = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(BETWEEN_LOOPS);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		System.out.println(String.format(">>> %s done reading. Bye.", this.getClass().getName()));
	}

	@Override
	public void closeReader() throws Exception {
	}
}
