package tests;

import org.junit.Test;
import tideengine.BackEndTideComputer;
import tideengine.TideStation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.fail;

public class Basic01 {

    private static BackEndTideComputer backEndTideComputer;

    /**
     * Basic functional test.
     * fails if there is an exception.
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
                System.out.println(URLDecoder.decode(sd.getFullName(), StandardCharsets.UTF_8));
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

    @Test
    public void dateTest() {
        int year = 2022;

        final String TIME_ZONE = "America/Los_Angeles";
        final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z z");

        Calendar jan1st = null;
        Calendar now = null;
        if (false) {
            jan1st = new GregorianCalendar(year, Calendar.JANUARY, 1);
        } else {
            jan1st = new GregorianCalendar();
            jan1st.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
//          jan1st.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            jan1st.set(Calendar.YEAR, year);
            jan1st.set(Calendar.MONTH, Calendar.JANUARY);
            jan1st.set(Calendar.DAY_OF_MONTH, 1);
            jan1st.set(Calendar.HOUR_OF_DAY, 0);
            jan1st.set(Calendar.MINUTE, 0);
            jan1st.set(Calendar.SECOND, 0);
            jan1st.set(Calendar.MILLISECOND, 0);
        }

        now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));

        SDF.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));

        System.out.printf("Now: %d, Jan 1st: %d\n", now.getTime().getTime(), jan1st.getTime().getTime());
        System.out.printf("Now: %s, Jan 1st: %s\n",
                NumberFormat.getInstance().format(now.getTime().getTime()),
                NumberFormat.getInstance().format(jan1st.getTime().getTime()));
        System.out.printf("Now: %f h, Jan 1st: %f h\n", now.getTime().getTime() / 3_600_000d, jan1st.getTime().getTime() / 3_600_000d);
        System.out.println("--------------------");

        System.out.printf("Jan 1st: %s\n", SDF.format(jan1st.getTime()));
        System.out.printf("Now    : %s\n", SDF.format(now.getTime()));

        System.out.println("Hop!");
    }
}
