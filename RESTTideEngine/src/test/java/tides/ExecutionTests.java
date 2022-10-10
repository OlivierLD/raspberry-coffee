package tides;

import calc.GeoPoint;
import org.junit.Test;
import tideengine.publisher.TideForOneMonth;
import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.publisher.TidePublisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;

import static junit.framework.TestCase.fail;

public class ExecutionTests {

    /**
     * Basic execution test. Fails in case of failure...
     */
    @Test
    public void testTideForOneMonth() {
        try {
            mainTide("-year", "2022", "-month", "1");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * Basic execution test
     */
    @Test
    public void testSunForOneMonth() {
        try {
            mainSun("-year", "2022", "-month", "1");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    /**
     * @param args -month MM -year YYYY. For month, 1=Jan, 2=Feb,..., 12=Dec.
     * @throws Exception
     */
    public static void mainTide(String... args) throws Exception {
        String yearStr = null;
        String monthStr = null;

        int year = -1;
        int month = -1;

        if (args.length != 4) {
            throw new RuntimeException("Wrong number of arguments: -year 2011 -month 2, for Feb 2011.");
        } else {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-year")) {
                    yearStr = args[i + 1];
                } else if (args[i].equals("-month")) {
                    monthStr = args[i + 1];
                }
            }
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException nfe) {
                throw (nfe);
            }
            try {
                month = Integer.parseInt(monthStr);
            } catch (NumberFormatException nfe) {
                throw (nfe);
            }
        }
        //  long before = System.currentTimeMillis();
        //  BackEndTideComputer.setVerbose(verbose);
        //  XMLDocument constituents = BackEndXMLTideComputer.loadDOM(CONSTITUENT_FILE);
        //  long after = System.currentTimeMillis();
        //  if (verbose) System.out.println("DOM loading took " + Long.toString(after - before) + " ms");

        BackEndTideComputer backEndTideComputer = new BackEndTideComputer();
        backEndTideComputer.connect();
        List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();

        String location = "Oyster Point Marina, San Francisco Bay, California";
        String encoded = URLEncoder.encode(location, StandardCharsets.UTF_8).replace("+", "%20");
    //  String location = "Adelaide";
        System.out.println("-- " + location + " --");
        System.out.println("Date and time zone:" + "Etc/UTC");
        TideForOneMonth.tideForOneMonth(System.out, "Etc/UTC", year, month, encoded, "meters", constSpeed);
    }

    /**
     * @param args -month MM -year YYYY. For month, 1=Jan, 2=Feb,..., 12=Dec.
     * @throws Exception
     */
    public static void mainSun(String... args) throws Exception {
        String yearStr = null;
        String monthStr = null;

        int year = -1;
        int month = -1;

        if (args.length != 4) {
            throw new RuntimeException("Wrong number of arguments: -year 2011 -month 2, for Feb 2011.");
        } else {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-year")) {
                    yearStr = args[i + 1];
                } else if (args[i].equals("-month")) {
                    monthStr = args[i + 1];
                }
            }
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException nfe) {
                throw (nfe);
            }
            try {
                month = Integer.parseInt(monthStr);
            } catch (NumberFormatException nfe) {
                throw (nfe);
            }
        }
        GeoPoint geoPoint = new GeoPoint(47.0, -3.0);
        String timeZone = "Europe/Paris"; // "Etc/UTC"; //

        String location = geoPoint.toString();
        System.out.println("-- " + location + " --");
        System.out.println("Date and time zone:" + timeZone);

        PrintStream out = System.out;
        String prefix = "test";
        String radical = "";
        try {
            File tempFile = File.createTempFile(prefix + ".data.", ".xml");
            out = new PrintStream(new FileOutputStream(tempFile));
            radical = tempFile.getAbsolutePath();
            radical = radical.substring(0, radical.lastIndexOf(".xml"));
            System.out.println("Writing data in " + tempFile.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Error creating temp file");
            ex.printStackTrace();
            throw ex;
        }
        out.println("<root>");
        TideForOneMonth.sunForOneMonth(out, timeZone, year, month, geoPoint, TideForOneMonth.XML_FLAVOR);
        out.println("</root>");
        out.close();

        // TODO Check the XML Content

    }

    @Test
    public void mainSunDoc() {
        try {
            String f = TidePublisher.publishFromPos(new GeoPoint(47.661667, -2.758167), // Vannes
                    "Vannes, France",
                    "Europe/Paris",
                    Calendar.JANUARY,
                    2022,
                    1,
                    Calendar.YEAR,
                    "publishagenda.sh");
            System.out.println(String.format("%s generated", f));
            System.out.println("Done!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
