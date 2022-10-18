package sun;

import calc.calculation.AstroComputerV2;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class TestSolarDate {

    private final static SimpleDateFormat SDF_01 = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
    private final static SimpleDateFormat SDF_02 = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
    static {
        SDF_02.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
    }

    static AstroComputerV2 acv2 = new AstroComputerV2();
    static double LATITUDE = 37d;
    static double LONGITUDE = -122;

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

        Calendar current = GregorianCalendar.getInstance();
        current.setTime(utc);
        acv2.setDateTime(current.get(Calendar.YEAR),
                current.get(Calendar.MONTH) + 1,
                current.get(Calendar.DAY_OF_MONTH),
                current.get(Calendar.HOUR_OF_DAY),
                current.get(Calendar.MINUTE),
                current.get(Calendar.SECOND));
        acv2.calculate();

        Date one = getSolarDateFromEOT(utc, LATITUDE, LONGITUDE);
        Date two = acv2.getSolarDateAtPos(LATITUDE, LONGITUDE);

        System.out.println(String.format("UTC: %s, One: %s, Two:%s", utc, SDF_01.format(one), SDF_02.format(two))); // Returns UTC Dates... Look into that.

        assertEquals("Nope.", SDF_01.format(one), SDF_02.format(two));
    }

}
