package astro;

import calc.GeomUtil;
import calc.calculation.AstroComputer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Compare two versions of the moon tilt calculation
 */
public class MoonTiltTest {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
//		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    // SF Home
    private final static double lat = 37.7489;
    private final static double lng = -122.5070;


    public static void main(String... args) {
        System.setProperty("deltaT", "AUTO");
//        System.setProperty("astro.verbose", "true");
        // Moon tilt
        Calendar date2 = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        System.out.println(String.format("Setting Cal Date to %d-%02d-%02d %02d:%02d:%02d",
                date2.get(Calendar.YEAR),
                date2.get(Calendar.MONTH) + 1,
                date2.get(Calendar.DAY_OF_MONTH),
                date2.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date2.get(Calendar.MINUTE),
                date2.get(Calendar.SECOND)));
        AstroComputer.setDateTime(date2.get(Calendar.YEAR),
                date2.get(Calendar.MONTH) + 1,
                date2.get(Calendar.DAY_OF_MONTH),
                date2.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date2.get(Calendar.MINUTE),
                date2.get(Calendar.SECOND));

        AstroComputer.calculate();
        Calendar calculationDateTime = AstroComputer.getCalculationDateTime();

        double moonTilt = AstroComputer.getMoonTilt(lat, lng);
        System.out.println(String.format("V1: At %s, from %s / %s, Moon Tilt: %.03f",
                SDF_UTC.format(calculationDateTime.getTime()),
                GeomUtil.decToSex(lat, GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(lng,  GeomUtil.SWING, GeomUtil.EW),
                moonTilt));
        double moonTiltV2 = AstroComputer.getMoonTiltV2(lat, lng);
        System.out.println(String.format("V2: At %s, from %s / %s, Moon Tilt: %.03f",
                SDF_UTC.format(calculationDateTime.getTime()),
                GeomUtil.decToSex(lat, GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(lng,  GeomUtil.SWING, GeomUtil.EW),
                moonTiltV2));

    }
}
