package util;

import nmea.utils.NMEAUtils;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviationCurve {

    @Test
    public void testDevCurve() {
        final URL resource = this.getClass().getResource("dp_2011_04_15.csv");
        final List<double[]> dcData = NMEAUtils.loadDeviationCurve(resource.getFile());
        assertTrue("Dev Curve data not found.", dcData != null);
        assertEquals("Expected 73 entries", 73, dcData.size());

        final double dev225 = NMEAUtils.getDeviation(225d, dcData);
        // -2.21511
        final long round = Math.round(dev225 * 10e4);
        assertEquals("Bad Value", -221512, round);
    }
}
