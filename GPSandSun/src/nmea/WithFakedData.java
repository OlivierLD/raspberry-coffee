package nmea;

import calc.calculation.AstroComputer;
import calc.calculation.SightReductionUtil;

import java.io.BufferedReader;
import java.io.FileReader;

import java.text.DecimalFormat;

import java.util.Calendar;
import java.util.TimeZone;

import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.RMC;
import ocss.nmea.parser.StringParsers;

/**
 * Parse the RMC string from a log file.
 * No serial port involved
 */
public class WithFakedData {
    private final static DecimalFormat DFH = new DecimalFormat("#0.00'\272'");
    private final static DecimalFormat DFZ = new DecimalFormat("#0.00'\272'");

    private static GeoPos prevPosition = null;
    private static long prevDateTime = -1L;

    /*
     * deltaT, system variable.
     * See http://maia.usno.navy.mil/ser7/deltat.data
     */
    public static void main(String... args) throws Exception {
        System.setProperty("deltaT", "67.2810"); // 2014-Jan-01

        BufferedReader br = new BufferedReader(new FileReader("raspPiLog.nmea"));
        String line = "";
        boolean go = true;
        long nbRec = 0L, nbDisplay = 0L;
        while (go) {
            line = br.readLine();
            if (line == null) {
              go = false;
            } else {
                nbRec++;
                boolean valid = StringParsers.validCheckSum(line);
                if (valid) {
                    String id = line.substring(3, 6);
                    if ("RMC".equals(id)) {
                        // System.out.println(line);
                        RMC rmc = StringParsers.parseRMC(line);
                        // System.out.println(rmc.toString());
                        if (rmc.getRmcDate() != null && rmc.getGp() != null) {
                            if ((prevDateTime == -1L || prevPosition == null) ||
                                    (prevDateTime != (rmc.getRmcDate().getTime() / 1_000) || !rmc.getGp().equals(prevPosition))) {
                                nbDisplay++;
                                Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
                                current.setTime(rmc.getRmcDate());
                                AstroComputer.setDateTime(current.get(Calendar.YEAR),
                                        current.get(Calendar.MONTH) + 1,
                                        current.get(Calendar.DAY_OF_MONTH),
                                        current.get(Calendar.HOUR_OF_DAY), // 12 - (int)Math.round(AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(ts.getTimeZone()))),
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
                                System.out.println(current.getTime().toString() + ", He:" + DFH.format(he) + ", Z:" + DFZ.format(z));
                            }
                            prevPosition = rmc.getGp();
                            prevDateTime = (rmc.getRmcDate().getTime() / 1_000);
                        }
                    }
                }
            }
        }
        br.close();
        System.out.println(nbRec + " record(s).");
        System.out.println(nbDisplay + " displayed.");
    }
}
