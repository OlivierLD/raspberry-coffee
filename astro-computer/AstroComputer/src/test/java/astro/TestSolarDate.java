package astro;

import calc.calculation.AstroComputerV2;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class TestSolarDate {

    private final static SimpleDateFormat SDF_01 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //                                                                           |    |  |  |  |  |
    //                                                                           |    |  |  |  |  17
    //                                                                           |    |  |  |  14,16
    //                                                                           |    |  |  11,13
    //                                                                           |    |  8,10
    //                                                                           |    5,7
    //                                                                           0,4
    static {
        SDF_01.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
    }

    static AstroComputerV2 acv2 = new AstroComputerV2();
    // SF~ish
//    static double LATITUDE = 37d;
//    static double LONGITUDE = -122d;

    // Chair Mn drive
//    static double LATITUDE = 39.167398801021655;
//    static double LONGITUDE = -107.24753700739706;

    // Belz
    static double LATITUDE = 47.67766666666667;
    static double LONGITUDE = -3.135666666666667;

    private static Date getSolarDateFromEOT(Date utc, double latitude, double longitude) {
        // Get Equation of time, used to calculate solar time.
        double eot = acv2.getSunMeridianPassageTime(latitude, longitude); // in decimal hours

        long ms = utc.getTime();
        Date solar = new Date(ms + Math.round((12 - eot) * 3_600_000));
        return solar;
    }

    @Test
    public void testSolarDateCalculation() {
        Date utc = new Date();

//        System.out.println(String.format("- UTC 0: %s", utc));
//        System.out.println(String.format("- UTC 1: %s", SDF_01.format(utc)));

        Calendar current = GregorianCalendar.getInstance();
        current.setTime(utc);
        acv2.setDateTime(current.get(Calendar.YEAR),
                current.get(Calendar.MONTH) + 1,
                current.get(Calendar.DAY_OF_MONTH),
                current.get(Calendar.HOUR_OF_DAY),
                current.get(Calendar.MINUTE),
                current.get(Calendar.SECOND));
        acv2.calculate(); // Set the timestamp.

        Date one = getSolarDateFromEOT(utc, LATITUDE, LONGITUDE);
        AstroComputerV2.YMDHMSs two = acv2.getSolarDateAtPos(LATITUDE, LONGITUDE);

        String formatted = SDF_01.format(one);
        System.out.println(String.format(">> Test >> UTC: %s, Solar: %s", SDF_01.format(utc), formatted ));

        assertEquals("Wrong year.",    Integer.parseInt(formatted.substring( 0,  4)), two.getYear());
        assertEquals("Wrong month.",   Integer.parseInt(formatted.substring( 5,  7)), two.getMonth());
        assertEquals("Wrong day.",     Integer.parseInt(formatted.substring( 8, 10)), two.getDay());
        assertEquals("Wrong hours.",   Integer.parseInt(formatted.substring(11, 13)), two.getH24());
        assertEquals("Wrong minutes.", Integer.parseInt(formatted.substring(14, 16)), two.getMinutes());
        assertEquals("Wrong seconds.", Integer.parseInt(formatted.substring(17)), Math.round(two.getSeconds()));

        System.out.println("<< End of test");
    }

}
