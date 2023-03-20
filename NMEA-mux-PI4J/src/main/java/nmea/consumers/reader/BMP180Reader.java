package nmea.consumers.reader;

import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.BMP180;
import java.util.List;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringGenerator;

/**
 * Reads data from an BMP180 sensor.
 * Pressure, Humidity and Temperature.
 */
public class BMP180Reader extends NMEAReader {

	private BMP180 bmp180;
	private static final String DEFAULT_DEVICE_PREFIX = "RP";
	private String devicePrefix = DEFAULT_DEVICE_PREFIX;

	private static final long BETWEEN_LOOPS = 1_000L; // TODO: Make it an external parameter?

	public BMP180Reader(List<NMEAListener> al) {
		this(null, al);
	}
	public BMP180Reader(String threadName, List<NMEAListener> al) {
		super(threadName, al);
		try {
			this.bmp180 = new BMP180();
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
				float temperature = bmp180.readTemperature();
				float pressure = bmp180.readPressure();
				// Generate NMEA String
				int deviceIdx = 0; // Instead of "BMP180"...
				String nmeaXDR = StringGenerator.generateXDR(devicePrefix,
								new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
												temperature,
												String.valueOf(deviceIdx++)), // Celcius, Temperature
								new StringGenerator.XDRElement(StringGenerator.XDRTypes.PRESSURE_P,
												pressure,
												String.valueOf(deviceIdx++))); // Pascal, pressure
				nmeaXDR += NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, nmeaXDR));

				String nmeaMTA = StringGenerator.generateMTA(devicePrefix, temperature);
				nmeaMTA += NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, nmeaMTA));

				String nmeaMMB = StringGenerator.generateMMB(devicePrefix, pressure / 100);
				nmeaMMB += NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, nmeaMMB));
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
