package tests;

import tideengine.BackEndTideComputer;
import tideengine.TideStation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class Basic01 {

    private static BackEndTideComputer backEndTideComputer;

    public static void main(String... args) {

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
        }

    }
}
