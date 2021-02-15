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
        System.out.println("Correction:" + (corr * 60d) + "' (minutes)");
    }

}
