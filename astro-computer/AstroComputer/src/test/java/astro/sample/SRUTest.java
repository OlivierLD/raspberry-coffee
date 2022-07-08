package astro.sample;

import calc.calculation.SightReductionUtil;

public class SRUTest {

    public static void main(String... args) {
        double corr = SightReductionUtil.getAltitudeCorrection(7d,
                2d,
                0.1 / 60d,
                16d / 60d,
                SightReductionUtil.LOWER_LIMB,
                false,
                true);
        System.out.println("Correction: " + (corr * 60d) + "' (minutes)");

        SightReductionUtil sightReductionUtil = new SightReductionUtil(80d, 22d, 37.5, -122.3);
        sightReductionUtil.calculate();
        double he = sightReductionUtil.getHe();
        double z = sightReductionUtil.getZ();
        System.out.printf("Alt: %.02f\272, Z: %.01f\272 \n", he, z);
    }

}
