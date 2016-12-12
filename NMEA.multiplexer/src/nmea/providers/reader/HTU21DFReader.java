package nmea.providers.reader;

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

  public HTU21DFReader(List<NMEAListener> al) {
    super(al);
    try {
      this.htu21df = new HTU21DF();
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
        float humidity = htu21df.readHumidity();
        float temperature = htu21df.readTemperature();
        // Generate NMEA String
        String nmeaXDR = StringGenerator.generateXDR("RP", // TODO Make this a external parameter
                new StringGenerator.XDRElement(StringGenerator.XDRTypes.HUMIDITY,
                        humidity,
                        "HTU21DF"), // %, Humidity
                new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
                        temperature,
                        "HTU21DF")); // Celcius, temperature
        nmeaXDR += NMEAParser.getEOS();
        fireDataRead(new NMEAEvent(this, nmeaXDR));
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        Thread.sleep(1000L); // TODO Make this a parameter
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