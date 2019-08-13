package nmea.consumers.reader;

import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.HTU21DF;
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
public class HTU21DFReader extends NMEAReader {

  private HTU21DF htu21df;
  private static final String DEFAULT_DEVICE_PREFIX = "RP";
  private String devicePrefix = DEFAULT_DEVICE_PREFIX;
  private static final long BETWEEN_LOOPS = 1_000L; // TODO: Make it an external parameter.

  public HTU21DFReader(List<NMEAListener> al) {
    this(null, al);
  }
  public HTU21DFReader(String threadName, List<NMEAListener> al) {
    super(threadName, al);
    try {
      this.htu21df = new HTU21DF();
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
    while (this.canRead()) {
      // Read data every 1 second
      try {
        float humidity = htu21df.readHumidity();
        float temperature = htu21df.readTemperature();
        // Generate NMEA String
        String nmeaXDR = StringGenerator.generateXDR(devicePrefix,
                new StringGenerator.XDRElement(StringGenerator.XDRTypes.HUMIDITY,
                        humidity,
                        "HTU21DF"), // %, Humidity
                new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
                        temperature,
                        "HTU21DF")); // Celcius, temperature
        nmeaXDR += NMEAParser.NMEA_SENTENCE_SEPARATOR;
        fireDataRead(new NMEAEvent(this, nmeaXDR));
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        Thread.sleep(BETWEEN_LOOPS);
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    try {
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void closeReader() throws Exception {
    if (this.htu21df != null) {
      try {
        this.htu21df.close();
      } catch (Exception e) {
        // Absorb.
      }
    }
  }
}
