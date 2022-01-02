package tests;

import org.junit.Test;
import tideengine.BackEndTideComputer;
import tideengine.TideStation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.fail;

public class Basic01 {

    private static BackEndTideComputer backEndTideComputer;

    /**
     * Basic functional test
     */
    @Test
    public void basic() {

        backEndTideComputer = new BackEndTideComputer();

        try {
            backEndTideComputer.connect();
            backEndTideComputer.setVerbose(true); // "true".equals(System.getProperty("tide.verbose", "false")));

            List<TideStation> stationData = BackEndTideComputer.getStationData();
            System.out.printf("Got %d station data.\n", stationData.size());
            stationData.forEach(sd -> {
                try {
                    System.out.println(URLDecoder.decode(sd.getFullName(), "UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                    System.err.printf("Bad Encoding for %s\n", sd.getFullName());
                }
            });

            backEndTideComputer.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void listTimeZones() {
        String[] availableIDs = TimeZone.getAvailableIDs();
        System.out.printf("We have %d time zones.", availableIDs.length);
        Arrays.asList(availableIDs).stream()
                .forEach(tz -> System.out.printf("<option value=\"%s\">%s</option>\n", tz, tz));
    }
}
