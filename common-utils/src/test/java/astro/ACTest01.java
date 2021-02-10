package astro;

import calc.GeomUtil;
import calc.calculation.AstroComputer;
import calc.calculation.SightReductionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ACTest01 {

    // This is for tests
    public static void main(String... args) {

        SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
//		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        System.setProperty("deltaT", "AUTO");

        System.out.println(String.format("Moon phase for date %d-%d-%d %d:%d:%d: ", 2011, 8, 22, 12, 00, 00) + AstroComputer.getMoonPhase(2011, 8, 22, 12, 00, 00));
        System.out.println("TimeOffset:" + AstroComputer.getTimeOffsetInHours("-09:30"));
        String[] tz = new String[]{"Pacific/Marquesas", "America/Los_Angeles", "GMT", "Europe/Paris", "Europe/Moscow", "Australia/Sydney", "Australia/Adelaide"};
        for (int i = 0; i < tz.length; i++) {
            System.out.println("TimeOffset for " + tz[i] + ":" + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz[i])));
        }
        System.out.println("TZ:" + TimeZone.getTimeZone(tz[0]).getDisplayName() + ", " + (TimeZone.getTimeZone(tz[0]).getOffset(new Date().getTime()) / (3_600_000d)));

        String timeZone = "America/Los_Angeles";
        Calendar cal = GregorianCalendar.getInstance();
        System.out.println("On " + cal.getTime() + ", TimeOffset for " + timeZone + ":" + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()));
        double d = TimeZone.getTimeZone(timeZone).getOffset(cal.getTime().getTime()) / (3_600_000d);
//  System.out.println("TimeOffset for " + timeZone + ":" +  d);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.getTime();
        System.out.println("On " + cal.getTime() + ", TimeOffset for " + timeZone + ":" + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()));
        d = TimeZone.getTimeZone(timeZone).getOffset(cal.getTime().getTime()) / (3_600_000d);
//  System.out.println("TimeOffset for " + timeZone + ":" +  d);

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        AstroComputer.calculate(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND));

        // SF Home
        double lat = 37.7489;
        double lng = -122.5070;

        System.out.println(String.format("\nFrom position %s / %s", GeomUtil.decToSex(lat, GeomUtil.SWING, GeomUtil.NS), GeomUtil.decToSex(lng, GeomUtil.SWING, GeomUtil.EW)));

        double sunMeridianPassageTime = AstroComputer.getSunMeridianPassageTime(lat, lng);
        System.out.println(String.format("Sun EoT: %f", sunMeridianPassageTime));

        long sunTransit = AstroComputer.getSunTransitTime(lat, lng);
        Date tt = new Date(sunTransit);
        System.out.println("Transit Time:" + tt.toString());

        double[] riseAndSet = AstroComputer.sunRiseAndSet(lat, lng);
        System.out.println(String.format("Time Rise: %f, Time Set: %f, ZRise: %f, ZSet: %f", riseAndSet[0], riseAndSet[1], riseAndSet[2], riseAndSet[3]));

        System.out.println(String.format("Moon Phase (no specific date, current one) : %f", AstroComputer.getMoonPhase()));

        System.out.println(String.format("Sun data:\nDeclination: %s\nGHA: %s",
                GeomUtil.decToSex(AstroComputer.getSunDecl(), GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(AstroComputer.getSunGHA(), GeomUtil.SWING, GeomUtil.NONE)));

        SightReductionUtil sru = new SightReductionUtil();

        sru.setL(lat);
        sru.setG(lng);

        sru.setAHG(AstroComputer.getSunGHA());
        sru.setD(AstroComputer.getSunDecl());
        sru.calculate();
        double obsAlt = sru.getHe();
        double z = sru.getZ();

        System.out.println(String.format("Elev.: %s, Z: %.02f\272", GeomUtil.decToSex(obsAlt, GeomUtil.SWING, GeomUtil.NONE), z));

        AstroComputer.EpochAndZ[] epochAndZs = AstroComputer.sunRiseAndSetEpoch(lat, lng);

        System.out.println("\nWith epochs");
        System.out.println(String.format("Rise Date: %s (Z:%.02f\272)\nSet Date: %s (Z:%.02f\272)",
                new Date(epochAndZs[0].getEpoch()).toString(),
                epochAndZs[0].getZ(),
                new Date(epochAndZs[1].getEpoch()).toString(),
                epochAndZs[1].getZ()));

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
        double moonTilt = AstroComputer.getMoonTilt(lat, lng);
        Calendar calculationDateTime = AstroComputer.getCalculationDateTime();
        System.out.println(String.format("At %s, Moon Tilt: %.03f", SDF_UTC.format(calculationDateTime.getTime()), moonTilt));
    }
}
