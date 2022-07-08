package astro;

import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ACV2Test01 {

    // This is for tests
    public static void main(String... args) {

        SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
//		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        System.setProperty("deltaT", "AUTO");

        AstroComputerV2 astroComputerV2 = new AstroComputerV2();

        System.out.println(String.format("Moon phase for date %d-%d-%d %d:%d:%d: ", 2011, 8, 22, 12, 00, 00) + astroComputerV2.getMoonPhase(2011, 8, 22, 12, 00, 00));
        System.out.println("TimeOffset:" + AstroComputerV2.getTimeOffsetInHours("-09:30"));
        String[] tz = new String[]{"Pacific/Marquesas", "America/Los_Angeles", "GMT", "Europe/Paris", "Europe/Moscow", "Australia/Sydney", "Australia/Adelaide"};
        for (int i = 0; i < tz.length; i++) {
            System.out.println("TimeOffset for " + tz[i] + ":" + AstroComputerV2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz[i])));
        }
        System.out.println("TZ:" + TimeZone.getTimeZone(tz[0]).getDisplayName() + ", " + (TimeZone.getTimeZone(tz[0]).getOffset(new Date().getTime()) / (3_600_000d)));

        String timeZone = "America/Los_Angeles";
        Calendar cal = GregorianCalendar.getInstance();
        System.out.println("On " + cal.getTime() + ", TimeOffset for " + timeZone + ":" + AstroComputerV2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()));
        double d = TimeZone.getTimeZone(timeZone).getOffset(cal.getTime().getTime()) / (3_600_000d);
//  System.out.println("TimeOffset for " + timeZone + ":" +  d);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.getTime();
        System.out.println("On " + cal.getTime() + ", TimeOffset for " + timeZone + ":" + AstroComputerV2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()));
        d = TimeZone.getTimeZone(timeZone).getOffset(cal.getTime().getTime()) / (3_600_000d);
//  System.out.println("TimeOffset for " + timeZone + ":" +  d);

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        astroComputerV2.calculate(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND));

//        astroComputerV2.calculate(  // At TPass
//                2021,
//                7,
//                26,
//                20,
//                16,
//                33);

        // SF Home
        double lat = 37.7489;
        double lng = -122.5070;

        System.out.println(String.format("\nFrom position %s / %s", GeomUtil.decToSex(lat, GeomUtil.SWING, GeomUtil.NS), GeomUtil.decToSex(lng, GeomUtil.SWING, GeomUtil.EW)));

        double sunMeridianPassageTime = astroComputerV2.getSunMeridianPassageTime(lat, lng);
        System.out.println(String.format("Sun EoT: %f", sunMeridianPassageTime));

        long sunTransit = astroComputerV2.getSunTransitTime(lat, lng);
        Date tt = new Date(sunTransit);
        System.out.println("Transit Time:" + tt);

        double[] riseAndSet = astroComputerV2.sunRiseAndSet(lat, lng);
        System.out.println(String.format("Time Rise: %f, Time Set: %f, ZRise: %f, ZSet: %f", riseAndSet[0], riseAndSet[1], riseAndSet[2], riseAndSet[3]));

        System.out.println(String.format("Moon Phase (no specific date, current one) : %f", astroComputerV2.getMoonPhase()));

        System.out.println(String.format("Sun data:\nDeclination: %s\nGHA: %s",
                GeomUtil.decToSex(astroComputerV2.getSunDecl(), GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(astroComputerV2.getSunGHA(), GeomUtil.SWING, GeomUtil.NONE)));

        SightReductionUtil sru = new SightReductionUtil();

        sru.setL(lat);
        sru.setG(lng);

        sru.setAHG(astroComputerV2.getSunGHA());
        sru.setD(astroComputerV2.getSunDecl());
        sru.calculate();
        double obsAlt = sru.getHe();
        double z = sru.getZ();

        System.out.println(String.format("Now: Elev.: %s, Z: %.02f\272", GeomUtil.decToSex(obsAlt, GeomUtil.SWING, GeomUtil.NONE), z));

        System.out.printf(">> BEFORE: Now is %s, Sun GHA: %f \n", astroComputerV2.getCalculationDateTime().getTime(), astroComputerV2.getSunGHA());
        double sunElevAtTransit = astroComputerV2.getSunElevAtTransit(lat, lng); // TODO Needs attention...
        System.out.printf(">> AFTER:  And now is (still) %s, Sun GHA: %f \n", astroComputerV2.getCalculationDateTime().getTime(), astroComputerV2.getSunGHA());

        AstroComputerV2.EpochAndZ[] epochAndZs = astroComputerV2.sunRiseAndSetEpoch(lat, lng);

        System.out.println("\nWith epochs");
        System.out.println(String.format("Rise Date: %s (Z:%.02f\272)\nSet Date: %s (Z:%.02f\272)",
                new Date(epochAndZs[0].getEpoch()),
                epochAndZs[0].getZ(),
                new Date(epochAndZs[1].getEpoch()),
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
        astroComputerV2.setDateTime(date2.get(Calendar.YEAR),
                date2.get(Calendar.MONTH) + 1,
                date2.get(Calendar.DAY_OF_MONTH),
                date2.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date2.get(Calendar.MINUTE),
                date2.get(Calendar.SECOND));

        astroComputerV2.calculate();
        double moonTilt = astroComputerV2.getMoonTilt(lat, lng);
        Calendar calculationDateTime = astroComputerV2.getCalculationDateTime();
        System.out.println(String.format("At %s, Moon Tilt: %.03f", SDF_UTC.format(calculationDateTime.getTime()), moonTilt));
    }
}
