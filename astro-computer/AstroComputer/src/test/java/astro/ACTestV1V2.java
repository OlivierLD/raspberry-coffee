package astro;

import calc.GeomUtil;
import calc.calculation.AstroComputer;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ACTestV1V2 {

    // This is for tests. Compare V1 & V2
    public static void main(String... args) {

        SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
//		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        System.setProperty("deltaT", "AUTO");

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        AstroComputer.calculate(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND));

        AstroComputerV2 acV2 = new AstroComputerV2();
        acV2.calculate(
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
        System.out.println(String.format("Sun EoT V1: %f", sunMeridianPassageTime));
        sunMeridianPassageTime = acV2.getSunMeridianPassageTime(lat, lng);
        System.out.println(String.format("Sun EoT V2: %f", sunMeridianPassageTime));

        long sunTransit = AstroComputer.getSunTransitTime(lat, lng);
        Date tt = new Date(sunTransit);
        System.out.println("Transit Time V1:" + tt.toString());
        sunTransit = acV2.getSunTransitTime(lat, lng);
        tt = new Date(sunTransit);
        System.out.println("Transit Time V2:" + tt.toString());

        double[] riseAndSet = AstroComputer.sunRiseAndSet(lat, lng);
        System.out.println(String.format("V1: Time Rise: %f, Time Set: %f, ZRise: %f, ZSet: %f", riseAndSet[0], riseAndSet[1], riseAndSet[2], riseAndSet[3]));
        riseAndSet = acV2.sunRiseAndSet(lat, lng);
        System.out.println(String.format("V2: Time Rise: %f, Time Set: %f, ZRise: %f, ZSet: %f", riseAndSet[0], riseAndSet[1], riseAndSet[2], riseAndSet[3]));

        System.out.println(String.format("V1 Moon Phase (no specific date, current one) : %f", AstroComputer.getMoonPhase()));
        System.out.println(String.format("V2 Moon Phase (no specific date, current one) : %f", acV2.getMoonPhase()));

        System.out.println(String.format("V1 Sun data:\nDeclination: %s\nGHA: %s",
                GeomUtil.decToSex(AstroComputer.getSunDecl(), GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(AstroComputer.getSunGHA(), GeomUtil.SWING, GeomUtil.NONE)));
        System.out.println(String.format("V2 Sun data:\nDeclination: %s\nGHA: %s",
                GeomUtil.decToSex(acV2.getSunDecl(), GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(acV2.getSunGHA(), GeomUtil.SWING, GeomUtil.NONE)));

        SightReductionUtil sru = new SightReductionUtil();

        sru.setL(lat);
        sru.setG(lng);

        sru.setAHG(AstroComputer.getSunGHA());
        sru.setD(AstroComputer.getSunDecl());
        sru.calculate();
        double obsAlt = sru.getHe();
        double z = sru.getZ();
        System.out.println(String.format("V1 Now: Elev.: %s, Z: %.02f\272", GeomUtil.decToSex(obsAlt, GeomUtil.SWING, GeomUtil.NONE), z));

        sru.setAHG(acV2.getSunGHA());
        sru.setD(acV2.getSunDecl());
        sru.calculate();
        obsAlt = sru.getHe();
        z = sru.getZ();
        System.out.println(String.format("V2 Now: Elev.: %s, Z: %.02f\272", GeomUtil.decToSex(obsAlt, GeomUtil.SWING, GeomUtil.NONE), z));

        AstroComputer.EpochAndZ[] epochAndZs = AstroComputer.sunRiseAndSetEpoch(lat, lng);

        System.out.println("\nV1 With epochs");
        System.out.println(String.format("Rise Date: %s (Z:%.02f\272)\nSet Date: %s (Z:%.02f\272)",
                new Date(epochAndZs[0].getEpoch()).toString(),
                epochAndZs[0].getZ(),
                new Date(epochAndZs[1].getEpoch()).toString(),
                epochAndZs[1].getZ()));

        AstroComputerV2.EpochAndZ[] epochAndZsV2 = acV2.sunRiseAndSetEpoch(lat, lng);

        System.out.println("\nV2 With epochs");
        System.out.println(String.format("Rise Date: %s (Z:%.02f\272)\nSet Date: %s (Z:%.02f\272)",
                new Date(epochAndZsV2[0].getEpoch()).toString(),
                epochAndZsV2[0].getZ(),
                new Date(epochAndZsV2[1].getEpoch()).toString(),
                epochAndZsV2[1].getZ()));

        System.out.println();
        double moonTilt = AstroComputer.getMoonTilt(lat, lng);
        Calendar calculationDateTime = AstroComputer.getCalculationDateTime();
        System.out.println(String.format("At %s, Moon Tilt: %.03f", SDF_UTC.format(calculationDateTime.getTime()), moonTilt));

        moonTilt = acV2.getMoonTilt(lat, lng);
        calculationDateTime = AstroComputer.getCalculationDateTime();
        System.out.println(String.format("At %s, Moon Tilt: %.03f", SDF_UTC.format(calculationDateTime.getTime()), moonTilt));
    }
}
