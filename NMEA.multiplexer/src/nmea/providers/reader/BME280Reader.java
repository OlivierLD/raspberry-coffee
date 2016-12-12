package nmea.providers.reader;

import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.BME280;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringGenerator;

import java.util.List;

/**
 * Reads data from an HTU21DF sensor.
 * Humidity and Temperature.
 */
public class BME280Reader extends NMEAReader {

	private BME280 bme280;

	public BME280Reader(List<NMEAListener> al) {
		super(al);
		try {
			this.bme280 = new BME280();
		} catch (I2CFactory.UnsupportedBusNumberException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void read() {
		super.enableReading();
		while (this.canRead()) {
			// Read data every 1 second
			try {
				float humidity = bme280.readHumidity();
				float temperature = bme280.readTemperature();
				float pressure = bme280.readPressure();
				// Generate NMEA String
				int deviceIdx = 0; // Instead of "BME280"...
				String nmeaXDR = StringGenerator.generateXDR("RP", // TODO Make this a external parameter
								new StringGenerator.XDRElement(StringGenerator.XDRTypes.HUMIDITY,
												humidity,
												String.valueOf(deviceIdx++)), // %, Humidity
								new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
												temperature,
												String.valueOf(deviceIdx++)), // Celcius, Temperature
								new StringGenerator.XDRElement(StringGenerator.XDRTypes.PRESSURE_P,
												pressure,
												String.valueOf(deviceIdx++))); // Pascal, pressure
				nmeaXDR += NMEAParser.getEOS();
				fireDataRead(new NMEAEvent(this, nmeaXDR));

				String nmeaMDA = StringGenerator.generateMDA("RP", // TODO Make this a external parameter
								pressure / 100,
								temperature,
								-Double.MAX_VALUE,
								humidity,
								-Double.MAX_VALUE,
								-Double.MAX_VALUE,
								-Double.MAX_VALUE,
								-Double.MAX_VALUE,
								-Double.MAX_VALUE);
				nmeaMDA += NMEAParser.getEOS();
				fireDataRead(new NMEAEvent(this, nmeaMDA));

				String nmeaMTA = StringGenerator.generateMTA("RP", temperature);
				nmeaMTA += NMEAParser.getEOS();
				fireDataRead(new NMEAEvent(this, nmeaMTA));

				String nmeaMMB = StringGenerator.generateMMB("RP", pressure / 100);
				nmeaMMB += NMEAParser.getEOS();
				fireDataRead(new NMEAEvent(this, nmeaMMB));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000L); // TODO Make this a parameter
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	@Override
	public void closeReader() throws Exception {
	}
}
