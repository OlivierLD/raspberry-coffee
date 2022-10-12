package nmea.utils;

import context.NMEADataCache;
import nmea.mux.context.Context;
import nmea.parser.Angle180;
import nmea.parser.Angle180EW;
import nmea.parser.Angle180LR;
import nmea.parser.Angle360;
import nmea.parser.Speed;
import nmea.parser.TrueWindDirection;
import nmea.parser.TrueWindSpeed;
import java.util.List;
import java.util.logging.Level;

public class MuxNMEAUtils {

    /*
     * Calculated Data
     * <p>
     * TWS, TWA, TWD
     * HDG, true
     * CSP, CDR
     * Leeway
     */
    public static void computeAndSendValuesToCache(NMEADataCache cache) {
        computeAndSendValuesToCache(cache, false);
    }

    /**
     * Compute values for the cache
     * @param cache The full thing
     * @param isHDTPresent Heading True is present
     */
    @SuppressWarnings("unchecked")
    public static void computeAndSendValuesToCache(NMEADataCache cache, boolean isHDTPresent) {
        double heading = 0d;
//        System.out.println("HDT is here:" + isHDTPresent);
        if (!isHDTPresent) {
            double hdc = 0d;
            double dec = 0d;
            //  System.out.println("========================");
            try {
                hdc = ((Angle360) cache.get(NMEADataCache.HDG_COMPASS)).getValue() + ((Double) cache.get(NMEADataCache.HDG_OFFSET)).doubleValue();
            } catch (Exception ex) {
            }
            //  System.out.println("HDG Compass:" + hdc);
            try {
                dec = ((Angle180EW) cache.get(NMEADataCache.DECLINATION)).getValue();
            } catch (Exception ex) {
            }
            if (dec == -Double.MAX_VALUE) {
                dec = ((Angle180EW) cache.get(NMEADataCache.DEFAULT_DECLINATION)).getValue();
            }
            //  System.out.println("Declination:" + dec);

            @SuppressWarnings("unchecked")
            double dev = NMEAUtils.getDeviation(hdc, (List<double[]>) cache.get(NMEADataCache.DEVIATION_DATA)); // From the curve
            cache.put(NMEADataCache.DEVIATION, new Angle180EW(dev));

            heading = hdc + dev; // Magnetic
            cache.put(NMEADataCache.HDG_MAG, new Angle360(heading));
            //  System.out.println("HDG Mag: " + heading);

            double w = dec + dev;
            cache.put(NMEADataCache.VARIATION, new Angle180EW(w));
            heading = hdc + w; // true
            cache.put(NMEADataCache.HDG_TRUE, new Angle360(heading));
            if (false) {
                System.out.println("=== Computed Values ===");
                  System.out.println("HDG Compass:" + hdc);
                  System.out.println("Declination:" + dec);
                  System.out.println("Deviation  :" + dev);
                  System.out.println("Variation  :" + w);
                  System.out.println("HDG True   :" + heading);
                  System.out.println("==========================");
            }
        } else
            try {
                heading = ((Angle360) cache.get(NMEADataCache.HDG_TRUE)).getValue() + ((Double) cache.get(NMEADataCache.HDG_OFFSET)).doubleValue();
            } catch (Exception ex) {
                // Absorb
            }

        double twa = 0d, tws = 0d;
        int twd = 0;

        double sog = -1d,
               cog = -1d,
               aws = -1d;
        int awa = 0;
        try {
            sog = ((Speed) cache.get(NMEADataCache.SOG)).getValue();
        } catch (Exception ex) {
        }
        try {
            cog = ((Angle360) cache.get(NMEADataCache.COG)).getValue();
        } catch (Exception ex) {
        }
        try {
            aws = ((Speed) cache.get(NMEADataCache.AWS)).getValue() * ((Double) cache.get(NMEADataCache.AWS_FACTOR)).doubleValue();
        } catch (Exception ex) {
        }
        try {
            awa = (int) (((Angle180) cache.get(NMEADataCache.AWA)).getValue() + ((Double) cache.get(NMEADataCache.AWA_OFFSET)).doubleValue());
        } catch (Exception ex) {
        }

        double awsCoeff = 1d;
        try {
            awsCoeff = ((Double) cache.get(NMEADataCache.AWS_FACTOR)).doubleValue();
        } catch (Exception ex) {
        }
        double awaOffset = 0d;
        try {
            awaOffset = ((Double) cache.get(NMEADataCache.AWA_OFFSET)).doubleValue();
        } catch (Exception ex) {
        }
        double bspCoeff = 1d;
        try {
            bspCoeff = ((Double) cache.get(NMEADataCache.BSP_FACTOR)).doubleValue();
        } catch (Exception ex) {
        }
        double hdgOffset = 0d;
        try {
            hdgOffset = ((Double) cache.get(NMEADataCache.HDG_OFFSET)).doubleValue();
        } catch (Exception ex) {
        }

        if (aws != -Double.MAX_VALUE) {
            //    System.out.println("Using the GOOD method");
            double[] tw = NMEAUtils.calculateTWwithGPS(
                    aws,
                    awsCoeff,
                    awa,
                    awaOffset,
                    heading,
                    hdgOffset,
                    sog,
                    cog);
            twa = tw[0];
            tws = tw[1];
            twd = (int) tw[2];
            cache.put(NMEADataCache.TWA, new Angle180(twa));
            cache.put(NMEADataCache.TWS, new TrueWindSpeed(tws));
            cache.put(NMEADataCache.TWD, new TrueWindDirection(twd));
        }
//  else
//    System.out.println(" NO AW !!!");
//  System.out.println("AWS:" + aws + ", TWS:" + tws + ", AWA:" + awa + ", TWA:" + twa);

        double bsp = 0d;
        double maxLeeway = 0d;
        try {
            maxLeeway = ((Double) cache.get(NMEADataCache.MAX_LEEWAY)).doubleValue();
        } catch (Exception ex) {
        }
        double leeway = NMEAUtils.getLeeway(awa, maxLeeway);
        cache.put(NMEADataCache.LEEWAY, new Angle180LR(leeway));
        double cmg = heading + leeway;
        cache.put(NMEADataCache.CMG, new Angle360(cmg));

        try {
            bsp = ((Speed) cache.get(NMEADataCache.BSP)).getValue();
        } catch (Exception ex) {
        }
        if (cog != -1 && sog != -1) {
            double[] cr = NMEAUtils.calculateCurrent(bsp,
                    bspCoeff,
                    heading,
                    hdgOffset,
                    leeway,
                    sog,
                    cog);
            cache.put(NMEADataCache.CDR, new Angle360(cr[0]));
            cache.put(NMEADataCache.CSP, new Speed(cr[1]));
        }
    }

    public static void calculateVMGs(NMEADataCache cache) {
        double vmg = 0d;
        try {
            if (cache.get(NMEADataCache.SOG) != null &&
                    cache.get(NMEADataCache.COG) != null &&
                    cache.get(NMEADataCache.TWD) != null) {
                double sog = (((Speed) cache.get(NMEADataCache.SOG)).getValue());
                double cog = ((Angle360) cache.get(NMEADataCache.COG)).getValue();
                double twd = (((Angle360) cache.get(NMEADataCache.TWD)).getValue());
                double twa = twd - cog;
                if (sog > 0) { // Try with GPS Data first
                    vmg = sog * Math.cos(Math.toRadians(twa));
                } else {
                    try {
                        twa = ((Angle180) cache.get(NMEADataCache.TWA)).getValue();
                        double bsp = ((Speed) cache.get(NMEADataCache.BSP)).getValue();
                        if (bsp > 0) {
                            vmg = bsp * Math.cos(Math.toRadians(twa));
                        }
                    } catch (Exception e) {
                        vmg = 0;
                    }
                }
                cache.put(NMEADataCache.VMG_ON_WIND, vmg);

                if (cache.get(NMEADataCache.TO_WP) != null && !cache.get(NMEADataCache.TO_WP).toString().trim().isEmpty()) {
                    double b2wp = ((Angle360) cache.get(NMEADataCache.B2WP)).getValue();
                    sog = (((Speed) cache.get(NMEADataCache.SOG)).getValue());
                    cog = ((Angle360) cache.get(NMEADataCache.COG)).getValue();
                    if (sog > 0) {
                        double angle = b2wp - cog;
                        vmg = sog * Math.cos(Math.toRadians(angle));
                    } else {
                        double angle = b2wp - ((Angle360) cache.get(NMEADataCache.HDG_TRUE)).getValue();
                        double bsp = ((Speed) cache.get(NMEADataCache.BSP)).getValue();
                        vmg = bsp * Math.cos(Math.toRadians(angle));
                    }
                    cache.put(NMEADataCache.VMG_ON_WP, vmg);
                }
            }
        } catch (Exception ex) {
            Context.getInstance().getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
    }

}
