package util;

public class MercatorUtilTest {

    public static void main(String... args) {
        double d = MercatorUtil.getIncLat(45D);
        System.out.println("IncLat(45)=" + d);
        System.out.println("Rad(45)=" + Math.toRadians(45D));

        System.out.println("IncLat(60)=" + MercatorUtil.getIncLat(60D));
        System.out.println("Ratio at L=60:" + MercatorUtil.getIncLatRatio(60D));

        System.out.println("-----------------------");
        for (int i = 0; i <= 90; i += 10) {
            System.out.println("Ratio at " + i + "=" + MercatorUtil.getIncLatRatio(i));
        }
        System.out.println("IncLat(90)=" + MercatorUtil.getIncLat(90D));
    }
}
