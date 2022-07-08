package astro.sample;

import calc.GeoPoint;
import calc.GeomUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;

import java.text.DecimalFormat;

public class GeomUtilTest {

    public static void main(String... args) {
        DecimalFormat nf3 = new DecimalFormat("000.0000000");
        double d = GeomUtil.sexToDec("333", "22.07");
//  PolyAngle pa = new PolyAngle(d, PolyAngle.DEGREES);
//  System.out.println(nf3.format(d) + " degrees = " + GeomUtil.formatHMS(degrees2hours(ha2ra(pa.getAngleInDegrees()))) + " hours (hours turn the other way...)");

        System.out.println(GeomUtil.formatDMS(d));
        System.out.println(GeomUtil.decToSex(d, GeomUtil.SWING));

        double d2 = GeomUtil.sexToDec("333", "22", "04.20");
        System.out.println("Returned:" + nf3.format(d2));
        System.out.println(GeomUtil.formatDMS(d2));
        System.out.println(GeomUtil.decToSex(d2, GeomUtil.SWING));

        double l = GeomUtil.sexToDec("37", "29", "48.34");
        double g = GeomUtil.sexToDec("122", "15", "20.91");

        System.out.println("LogiSail headquarters - L:" + l + ", G:" + g);

        l = GeomUtil.sexToDec("37", "39", "51.26");
        g = GeomUtil.sexToDec("122", "22", "48.94");
        System.out.println("Don Pedro at Oyster Point - L:" + l + ", G:" + g);

        double z = 123.45;
        System.out.println(String.format("Z: %s", GeomUtil.decToSex(z, GeomUtil.NO_DEG, GeomUtil.NONE)));

        // Converting degrees to hours
        g = -GeomUtil.sexToDec("142", "01.9");
        double hours = GeomUtil.degrees2hours(g);
        System.out.println("142 01.9 W = " + GeomUtil.formatHMS(hours) + " hours");
        // At GMT 17:37:58
        double gmt = GeomUtil.sexToDec("17", "37", "58");
        double localSolar = gmt + hours;
        System.out.println("Local Solar:" + GeomUtil.formatHMS(localSolar));

        System.out.println("142 01.9 W, at 17:37:58 GMT, local solar:" + GeomUtil.formatHMS(GeomUtil.getLocalSolarTime(-GeomUtil.sexToDec("142", "01.9"), GeomUtil.sexToDec("17", "37", "58"))));
        System.out.println("142 28.1 W, at 20:44:23 GMT, local solar:" + GeomUtil.formatHMS(GeomUtil.getLocalSolarTime(-GeomUtil.sexToDec("142", "28.1"), GeomUtil.sexToDec("20", "44", "23"))));
        System.out.println("143 02.4 W, at 00:41:30 GMT, local solar:" + GeomUtil.formatHMS(GeomUtil.getLocalSolarTime(-GeomUtil.sexToDec("143", "02.4"), GeomUtil.sexToDec("00", "41", "30"))));

        System.out.println("  9 24 45 S =" + (-GeomUtil.sexToDec("9", "24", "45")));
        System.out.println("139 47 00 W =" + (-GeomUtil.sexToDec("139", "47", "00")));

        System.out.println("Nuku-Hiva");
        System.out.println("  8 55 12.45 S =" + (-GeomUtil.sexToDec("8", "55", "12.45")));
        System.out.println("140 05 18.39 W =" + (-GeomUtil.sexToDec("140", "05", "18.39")));
        System.out.println("Ua-Huka");
        System.out.println("  8 55 04.76 S =" + (-GeomUtil.sexToDec("8", "55", "04.76")));
        System.out.println("139 33 08.39 W =" + (-GeomUtil.sexToDec("139", "33", "08.39")));
        System.out.println("Ua-Pou");
        System.out.println("  9 24 00.00 S =" + (-GeomUtil.sexToDec("9", "24", "00.00")));
        System.out.println("140 04 37.45 W =" + (-GeomUtil.sexToDec("140", "04", "37.45")));

        System.out.println("Hiva-Oa");
        System.out.println("  9 49 37.33 S =" + (-GeomUtil.sexToDec("9", "49", "37.33")));
        System.out.println("139 03 37.84 W =" + (-GeomUtil.sexToDec("139", "03", "37.84")));
        System.out.println("Tahuata");
        System.out.println("  9 56 57.88 S =" + (-GeomUtil.sexToDec("9", "56", "57.88")));
        System.out.println("139 04 53.00 W =" + (-GeomUtil.sexToDec("139", "04", "53.00")));
        System.out.println("Mohotani");
        System.out.println("  9 59 21.66 S =" + (-GeomUtil.sexToDec("9", "59", "21.66")));
        System.out.println("138 46 54.94 W =" + (-GeomUtil.sexToDec("138", "46", "54.94")));

        System.out.println("Fatu-Hiva");
        System.out.println(" 10 28 32 S =" + (-GeomUtil.sexToDec("10", "28", "32")));
        System.out.println("138 39 59 W =" + (-GeomUtil.sexToDec("138", "39", "59")));

        System.out.println("37 48.22 N  =" + (GeomUtil.sexToDec("37", "48.22")));
        System.out.println("122 22.43 W =" + (-GeomUtil.sexToDec("122", "22.43")));

        System.out.println("Cook:");
        System.out.println("19 00.00 N  =" + (GeomUtil.sexToDec("19", "00.00")));
        System.out.println("160 00.00 W =" + (-GeomUtil.sexToDec("160", "00.00")));

        System.out.println("Niue:");
        System.out.println("19 00.00 N  =" + (GeomUtil.sexToDec("19", "00.00")));
        System.out.println("170 00.00 W =" + (-GeomUtil.sexToDec("170", "00.00")));

        System.out.println("Cook to Niue:");
        GreatCircle gc = new GreatCircle();
        gc.setStart(new GreatCirclePoint(Math.toRadians(GeomUtil.sexToDec("19", "00.00")),
                Math.toRadians((-GeomUtil.sexToDec("160", "00.00")))));
        gc.setArrival(new GreatCirclePoint(Math.toRadians(GeomUtil.sexToDec("19", "00.00")),
                Math.toRadians((-GeomUtil.sexToDec("170", "00.00")))));
        gc.calculateGreatCircle(20);
        //  Vector route = gc.getRoute();
        double distance = gc.getDistance();
        System.out.println("Dist:" + (Math.toDegrees(distance) * 60) + " nm");
        gc.getRoute().stream().forEach(pt -> {
            System.out.println(String.format("pt: %s/%s, z:%f", Math.toDegrees(pt.getPoint().getL()), Math.toDegrees(pt.getPoint().getG()), pt.getZ()));
        });


        System.out.println("Tonga:");
        System.out.println("21 10.00 N  =" + (GeomUtil.sexToDec("21", "10.00")));
        System.out.println("175 08.00 W =" + (-GeomUtil.sexToDec("175", "08.00")));

        gc = new GreatCircle();
        gc.setStart(new GreatCirclePoint(Math.toRadians(GeomUtil.sexToDec("19", "00.00")),
                Math.toRadians((-GeomUtil.sexToDec("170", "00.00")))));
        gc.setArrival(new GreatCirclePoint(Math.toRadians(GeomUtil.sexToDec("21", "10.00")),
                Math.toRadians((-GeomUtil.sexToDec("175", "08.00")))));
        gc.calculateGreatCircle(20);
        //  Vector route = gc.getRoute();
        distance = gc.getDistance();
        System.out.println("Dist:" + (Math.toDegrees(distance) * 60) + " nm");

        System.out.println("Fiji:");
        System.out.println("18 00.00 N  =" + (GeomUtil.sexToDec("18", "00.00")));
        System.out.println("180 00.00 W =" + (-GeomUtil.sexToDec("180", "00.00")));

        gc = new GreatCircle();
        gc.setStart(new GreatCirclePoint(Math.toRadians(GeomUtil.sexToDec("21", "10.00")),
                Math.toRadians((-GeomUtil.sexToDec("175", "08.00")))));
        gc.setArrival(new GreatCirclePoint(Math.toRadians(GeomUtil.sexToDec("18", "00.00")),
                Math.toRadians((-GeomUtil.sexToDec("180", "00.00")))));
        gc.calculateGreatCircle(20);
        //  Vector route = gc.getRoute();
        distance = gc.getDistance();
        System.out.println("Dist:" + (Math.toDegrees(distance) * 60) + " nm");

        double bug = -GeomUtil.sexToDec("10", "58", "20.8");
        System.out.println("Bug:" + bug + " " + GeomUtil.formatDMS(bug) + " (fixed)");

        System.out.println("Display (no deg):[" + GeomUtil.decToSex(37.123, GeomUtil.NO_DEG, GeomUtil.NS) + "]");

        String fromJPEG = String.format("N 37%s39'49.8\"", GeomUtil.DEGREE_SYMBOL);
        System.out.println(fromJPEG + " is [" + GeomUtil.sexToDec(fromJPEG) + "]");

        fromJPEG = String.format("W 112%s9'34.36\"", GeomUtil.DEGREE_SYMBOL);
        System.out.println(fromJPEG + " is [" + GeomUtil.sexToDec(fromJPEG) + "]");

        double lat = 37.750585;
        double lng = -122.507891;
        System.out.println("Grid Square: " + new GeoPoint(lat, lng).toString() + " => " + GeomUtil.gridSquare(lat, lng));
        // TODO reverse GRID Square
        // Also see this: https://www.karhukoti.com/maidenhead-grid-square-locator/?grid=CM87

        // Bearing from-to.
        double bearing = GeomUtil.bearingFromTo(39.099912, -94.581213, 38.627089, -90.200203);
        System.out.println(String.format("Kansas City to St Louis, Bearing: %.02f\272 ", bearing));
        System.out.println(String.format("W >> %.02f\272", GeomUtil.bearingFromTo(37, -122, 37, -123)));
        System.out.println(String.format("E >> %.02f\272", GeomUtil.bearingFromTo(37, -122, 37, -120)));

        // Bearing diffs
        System.out.println(String.format("15 >> %.02f\272", GeomUtil.bearingDiff(350, 5)));
        System.out.println(String.format("15 >> %.02f\272", GeomUtil.bearingDiff(5, 350)));
        System.out.println(String.format("15 >> %.02f\272", GeomUtil.bearingDiff(20, 5)));
        System.out.println(String.format("15 >> %.02f\272", GeomUtil.bearingDiff(350, 335)));
        System.out.println(String.format("15 >> %.02f\272", GeomUtil.bearingDiff(170, 185)));
    }
}
