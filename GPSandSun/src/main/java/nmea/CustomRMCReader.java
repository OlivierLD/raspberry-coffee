package nmea;

import calc.calculation.AstroComputer;
import calc.calculation.SightReductionUtil;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.text.DecimalFormat;

import java.util.Calendar;
import java.util.TimeZone;


/**
 * Reads the GPS Data, parse the RMC String
 * Display astronomical data
 */
public class CustomRMCReader extends NMEAClient {
    private final static DecimalFormat DFH = new DecimalFormat("#0.00'\272'");
    private final static DecimalFormat DFZ = new DecimalFormat("##0.00'\272'");

    private static GeoPos prevPosition = null;
    private static long prevDateTime = -1L;

    public CustomRMCReader() {
        super();
    }

    @Override
    public void dataDetectedEvent(NMEAEvent e) {
//  System.out.println("Received:" + e.getContent());
        manageData(e.getContent().trim());
    }

    @Override
    public Object getBean() {
        return null;
    }

    private static CustomRMCReader customClient = null;

    private static void manageData(String sentence) {
        boolean valid = StringParsers.validCheckSum(sentence);
        if (valid) {
            String id = sentence.substring(3, 6);
            if ("RMC".equals(id)) {
                System.out.println(sentence);
                RMC rmc = StringParsers.parseRMC(sentence);
                // System.out.println(rmc.toString());
                if (rmc != null && rmc.getRmcDate() != null && rmc.getGp() != null) {
                    if ((prevDateTime == -1L || prevPosition == null) ||
                            (prevDateTime != (rmc.getRmcDate().getTime() / 1_000) || !rmc.getGp().equals(prevPosition))) {
                        Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
                        current.setTime(rmc.getRmcDate());
                        AstroComputer.setDateTime(current.get(Calendar.YEAR),
                                current.get(Calendar.MONTH) + 1,
                                current.get(Calendar.DAY_OF_MONTH),
                                current.get(Calendar.HOUR_OF_DAY),
                                current.get(Calendar.MINUTE),
                                current.get(Calendar.SECOND));
                        AstroComputer.calculate();
                        SightReductionUtil sru = new SightReductionUtil(AstroComputer.getSunGHA(),
                                AstroComputer.getSunDecl(),
                                rmc.getGp().lat,
                                rmc.getGp().lng);
                        sru.calculate();
                        Double he = sru.getHe();
                        Double z = sru.getZ();
                        System.out.println(current.getTime().toString() + ", He:" + DFH.format(he) + ", Z:" + DFZ.format(z) + " (" + rmc.getGp().toString() + ")");
                    }
                    prevPosition = rmc.getGp();
                    prevDateTime = (rmc.getRmcDate().getTime() / 1_000);
                } else {
                    if (rmc == null) {
                      System.out.println("... no RMC data in [" + sentence + "]");
                    } else {
                        String errMess = "";
                        if (rmc.getRmcDate() == null) {
                          errMess += ("no Date ");
                        }
                        if (rmc.getGp() == null) {
                          errMess += ("no Pos ");
                        }
                        System.out.println(errMess + "in [" + sentence + "]");
                    }
                }
            } else {
              System.out.println("Read [" + sentence + "]");
            }
        } else {
          System.out.println("Invalid data [" + sentence + "]");
        }
    }

    public static void main(String... args) {
        System.setProperty("deltaT", System.getProperty("deltaT", "67.2810")); // 2014-Jan-01

        int br = 9600;
        System.out.println("CustomNMEAReader invoked with " + args.length + " Parameter(s).");
        for (String s : args) {
            System.out.println("CustomNMEAReader prm:" + s);
            try {
                br = Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
            }
        }

        customClient = new CustomRMCReader();

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

    public void stopDataRead() {
        if (customClient != null) {
            for (NMEAListener l : customClient.getListeners()) {
              l.stopReading(new NMEAEvent(this));
            }
        }
    }
}
