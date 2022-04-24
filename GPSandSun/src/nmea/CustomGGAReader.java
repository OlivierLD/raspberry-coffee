package nmea;

import calc.calculation.AstroComputer;
import calc.calculation.SightReductionUtil;

import java.text.DecimalFormat;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.RMC;
import ocss.nmea.parser.StringParsers;
import ocss.nmea.parser.UTC;

/**
 * Reads the GPS Data, parse the GGA String
 */
public class CustomGGAReader extends NMEAClient {
    public CustomGGAReader() {
        super();
    }

    @Override
    public void dataDetectedEvent(NMEAEvent e) {
//  System.out.println("Received:" + e.getContent());
        manageData(e.getContent().trim());
    }

    private static CustomGGAReader customClient = null;

    private static void manageData(String sentence) {
        boolean valid = StringParsers.validCheckSum(sentence);
        if (valid) {
            String id = sentence.substring(3, 6);
            if ("GGA".equals(id)) {
                System.out.println(sentence);
                List<Object> al = StringParsers.parseGGA(sentence);
                UTC utc = (UTC) al.get(0);
                GeoPos pos = (GeoPos) al.get(1);
                Integer nbs = (Integer) al.get(2);
                Double alt = (Double) al.get(3);
                System.out.println("\tUTC:" + utc.toString() + "\tPos:" + pos.toString());
                System.out.println("\t" + nbs.intValue() + " Satellite(s) in use");
                System.out.println("\tAltitude:" + alt);
                System.out.println("------------------");
            }
            //  else
            //    System.out.println("Read [" + sentence + "]");
        } else {
          System.out.println("Invalid data [" + sentence + "]");
        }
    }

    public static void main(String... args) {
        int br = 9600;
        System.out.println("CustomNMEAReader invoked with " + args.length + " Parameter(s).");
        for (String s : args) {
            System.out.println("CustomGGAReader prm:" + s);
            try {
                br = Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
            }
        }

        customClient = new CustomGGAReader();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("\nShutting down nicely.");
                customClient.stopDataRead();
            }
        });
        customClient.initClient();
        customClient.setReader(new CustomNMEASerialReader(customClient.getListeners(), br));
        customClient.startWorking(); // Feignasse!
    }

    private void stopDataRead() {
        if (customClient != null) {
            for (NMEAListener l : customClient.getListeners()) {
              l.stopReading(new NMEAEvent(this));
            }
        }
    }
}
