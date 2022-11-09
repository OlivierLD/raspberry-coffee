package utils;

import nmea.parser.StringGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Generators {

    @Test
    public void generateXDR() {
        float humidity = 56.78f;
        float temperature = 12.34f;
        float pressure = 101325f;
        String devicePrefix = "--";
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

        System.out.printf("XDR: %s\n", nmeaXDR);
        String expected = "$--XDR,H,56.8,P,0,C,12.3,C,1,P,101325,P,2*6A";
        assertEquals("Oops", expected, nmeaXDR);
    }
}
