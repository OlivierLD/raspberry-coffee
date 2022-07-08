package astro.sample;

import calc.DeadReckoning;

public class DRTest {
    public static void main(String... args) {
//  getSunCorrectionTable();
//  getMoonCorrectionTable();
//  getPlanetStarsCorrectionTable();
        double corr = DeadReckoning.getAltitudeCorrection(7d,
                2d,
                0.1,
                16d,
                Double.MAX_VALUE,
                Double.MAX_VALUE,
                DeadReckoning.LOWER_LIMB,
                false,
                null,
                true);
        System.out.println("Correction:" + (corr * 60d) + "'");
    }

}
