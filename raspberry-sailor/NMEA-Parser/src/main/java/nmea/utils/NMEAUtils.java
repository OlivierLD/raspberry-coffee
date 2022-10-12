package nmea.utils;

import nmea.parser.Angle180EW;
import nmea.parser.HDG;
import nmea.parser.OverGround;
import nmea.parser.RMC;
import nmea.parser.StringParsers;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Merged with NMEA-multiplexer's nmea.utils.NMEAUtils, Oct 12, 2022.
public class NMEAUtils {
    public final static int ALL_IN_HEXA = 0;
    public final static int CR_NL = 1;

    public final static double DEFAULT_DECLINATION = 0d;

    public static String translateEscape(String str, int option) {
        String s = null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if (option == CR_NL) {
                if (str.charAt(i) == (char) 0x0A) // [NL], \n, [LF]
                    sb.append("[LF]");
                else if (str.charAt(i) == (char) 0x0D) // [CR], \r
                    sb.append("[CR]");
                else
                    sb.append(str.charAt(i));
            } else {
                String c = Integer.toHexString((int) str.charAt(i) & 0xFF).toUpperCase();
                sb.append(StringUtils.lpad(c, 2, "0") + " ");
            }
        }
        return sb.toString();
    }

    public static long longitudeToTime(double longitude) {
        long offset = (long) (longitude * 3_600_000L / 15L);
        return offset;
    }

    public static double[] calculateTWwithGPS(double aws, double awsCoeff,
                                              double awa, double awaOffset,
                                              double hdg, double hdgOffset,
                                              double sog,
                                              double cog) {
        double twa = 0d, tws = -1d, twd = 0d;
        try {
            // Warning, the MHU is carried by the boat, that has the HDG...
            // Only if the boat is moving (ie SOG > 0)
            double diffCogHdg = 0;
            if (sog > 0d) {
                diffCogHdg = (cog - (hdg + hdgOffset));
                while (diffCogHdg < 0) diffCogHdg += 360;
                if (diffCogHdg > 180) {
//        System.out.println("- diffCogHdg > 180:" + Double.toString(diffCogHdg));
                    diffCogHdg -= 360;
                }
            }
            double awaOnCOG = (awa + awaOffset) - diffCogHdg;
            double d = ((aws * awsCoeff) * Math.cos(Math.toRadians(awaOnCOG))) - (sog);
            double h = ((aws * awsCoeff) * Math.sin(Math.toRadians(awaOnCOG)));
            tws = Math.sqrt((d * d) + (h * h));
            double twaOnCOG = Math.toDegrees(Math.acos(d / tws));
            if (Double.compare(Double.NaN, twaOnCOG) == 0) {
                twaOnCOG = 0d;
            }
            if (Math.abs(awaOnCOG) > 180 || awaOnCOG < 0) {
                twaOnCOG = 360 - twaOnCOG;
            }
            if (sog > 0) {
                twd = (int) (cog) + (int) twaOnCOG;
            } else {
                twd = (int) (hdg) + (int) twaOnCOG;
            }
            while (twd > 360) {
                twd -= 360;
            }
            while (twd < 0) {
                twd += 360;
            }

            twa = twaOnCOG + diffCogHdg;
            if (twa > 180) {
                twa -= 360;
            }
            //    System.out.println("DiffCOG-HDG:" + diffCogHdg + ", AWA on COG:" + awaOnCOG + ", TWAonCOG:" + twaOnCOG);
        } catch (Exception oops) {
            oops.printStackTrace();
        }
        return new double[]{twa, tws, twd};
    }

    public static double[] calculateCurrent(double bsp, double bspCoeff,
                                            double hdg, double hdgOffset,
                                            double leeway,
                                            double sog, double cog) {
        double cdr = 0d, csp = 0d;

        //  double rvX = ((bsp * bspCoeff) * Math.sin(Math.toRadians(hdg + hdgOffset)));
        //  double rvY = -((bsp * bspCoeff) * Math.cos(Math.toRadians(hdg + hdgOffset)));

        double rsX = ((bsp * bspCoeff) * Math.sin(Math.toRadians((hdg + hdgOffset) + leeway)));
        double rsY = -((bsp * bspCoeff) * Math.cos(Math.toRadians((hdg + hdgOffset) + leeway)));

        double rfX = (sog * Math.sin(Math.toRadians(cog)));
        double rfY = -(sog * Math.cos(Math.toRadians(cog)));
        double a = (rsX - rfX);
        double b = (rfY - rsY);
        csp = Math.sqrt((a * a) + (b * b));
        cdr = getDir((float) a, (float) b);

        return new double[]{cdr, csp};
    }

    /**
     *
     * @param x deltaX
     * @param y deltaY
     * @return the dir, in degrees
     *
     * @deprecated Use getDir (below)
     */
    public static double getDirObsolete(float x, float y) {
        double dir = 0.0D;
        if (y != 0) {
            dir = Math.toDegrees(Math.atan((double) x / (double) y));
        }
        if (x <= 0 || y <= 0) {
            if (x > 0 && y < 0) {
                dir += 180D;
            } else if (x < 0 && y > 0) {
                dir += 360D;
            } else if (x < 0 && y < 0) {
                dir += 180D;
            } else if (x == 0) {
                if (y > 0) {
                    dir = 0.0D;
                } else {
                    dir = 180D;
                }
            } else if (y == 0) {
                if (x > 0) {
                    dir = 90D;
                } else {
                    dir = 270D;
                }
            }
        }
        dir += 180D;
        while (dir >= 360D) {
            dir -= 360D;
        }
        return dir;
    }

    /**
     *
     * @param x deltaX
     * @param y deltaY
     * @return the dir, in degrees [0..360[
     */
    public static double getDir(double x, double y) {
        if (x == 0d && y == 0d) {
            throw new RuntimeException("Ambiguous... deltaX and deltaY set to zero.");
        }
        double direction = 180 + Math.toDegrees(Math.atan2(x, y));
        while (direction < 0) {
            direction += 360;
        }
        direction %= 360;
        return direction;
    }

    public static double getLeeway(double awa, double maxLeeway) {
        double _awa = awa;
        if (_awa < 0) {
            _awa += 360;
        }
        double leeway = 0D;
        if (_awa < 90 || _awa > 270) {
            double leewayAngle = maxLeeway * Math.cos(Math.toRadians(awa));
            if (_awa < 90) {
                leewayAngle = -leewayAngle;
            }
            leeway = leewayAngle;
        }
//  System.out.println("For AWA:" + awa + ", leeway:" + leeway);
        return leeway;
    }

    public static Map<Double, Double> loadDeviationHashtable(InputStream is) {
        Map<Double, Double> data = null;
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            data = loadDeviationHashtable(br);
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public static Map<Double, Double> loadDeviationHashtable(String deviationFileName) {
        Map<Double, Double> data = null;
        try {
            FileReader fr = new FileReader(deviationFileName);
            BufferedReader br = new BufferedReader(fr);
            data = loadDeviationHashtable(br);
            br.close();
            fr.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Deviation curve data file [" + deviationFileName + "] does not exist.\n" +
                    "Please change your preferences accordingly.\n" +
                    "Using default [zero-deviation.csv] instead.");
            try {
                FileReader fr = new FileReader("zero-deviation.csv");
                BufferedReader br = new BufferedReader(fr);
                data = loadDeviationHashtable(br);
                br.close();
                fr.close();
            } catch (FileNotFoundException fnfe2) {
                System.err.println("Installation problem: file [zero-deviation.csv] not found.");
                System.err.println(">> Initializing the deviation map to all zeros");
                data = new HashMap<>();
                for (int i = 0; i <= 360; i += 10) {
                    data.put((double) i, 0d);
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public static Map<Double, Double> loadDeviationHashtable(BufferedReader br) {
        Map<Double, Double> data = new Hashtable<>();

        try {
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] sa = line.split(",");
                double cm = Double.parseDouble(sa[0]);
                double d = Double.parseDouble(sa[1]);
                data.put(cm, d);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public static List<double[]> loadDeviationCurve(Map<Double, Double> data) {
        List<double[]> ret = null;

        try {
            Set<Double> set = data.keySet();
            List<Double> list = new ArrayList<>(set.size());
            for (Double d : set) {
                list.add(d);
            }
            Collections.sort(list);

            ret = new ArrayList<>(list.size());
            for (Double d : list) {
                double deviation = data.get(d);
                double cm = d.doubleValue();
                ret.add(new double[]{cm, deviation});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public static List<double[]> loadDeviationCurve(String deviationFileName) {
        List<double[]> ret = null;
        try {
            Map<Double, Double> data = loadDeviationHashtable(deviationFileName);
            ret = loadDeviationCurve(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public static Hashtable<Double, Double> loadDeviationCurve(List<double[]> data) {
        Hashtable<Double, Double> ret = new Hashtable<>(data.size());
        try {
            for (double[] da : data) {
                ret.put(da[0], da[1]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public static double getDeviation(double cc, List<double[]> deviationAL) {
        double deviation = 0d;
        if (deviationAL != null) {
            double prevCm = 0d, prevDev = 0;
            for (double[] dd : deviationAL) {
                if (dd[0] == cc) {
                    deviation = dd[1];
                    break;
                } else if (cc > prevCm && cc < dd[0]) {
                    // Extrapolate
                    double factor = (cc - prevCm) / (dd[0] - prevCm);
                    deviation = prevDev + ((dd[1] - prevDev) * factor);
                    break;
                }
                prevCm = dd[0];
                prevDev = dd[1];
            }
        }
//  System.out.println("d for " + cc + "=" + deviation);
        return deviation;
    }

    public static List<double[]> getDataForDeviation(String dataFileName) {
        List<double[]> ret = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(dataFileName));
            /*
             * We need:
             *
             * HDG (possible mag decl), HDM, or VHW for Heading
             * RMC for COG, SOG, TimeStamp, and Mag Decl.
             * GLL for TimeStamp
             * VTG for COG & SOG
             */
            HashMap<String, Integer> counter = new HashMap<>(4);
            counter.put("HDG", 0);
            counter.put("HDM", 0);
            counter.put("VHW", 0);
            counter.put("RMC", 0);
            counter.put("GLL", 0);
            counter.put("VTG", 0);

            String line = "";
            boolean keepLooping = true;
            while (keepLooping) {
                line = br.readLine();
                if (line == null)
                    keepLooping = false;
                else {
                    if (line.startsWith("$") && line.length() > 7) { // then let's try
                        String key = line.substring(3, 6);
                        if ("HDG".equals(key) ||
                                "HDM".equals(key) ||
                                "VHW".equals(key) ||
                                "RMC".equals(key) ||
                                "GLL".equals(key) ||
                                "VTG".equals(key))
                            counter.put(key, counter.get(key).intValue() + 1);
                    }
                }
            }
            br.close();
            System.out.println("We have:");
            Set<String> keys = counter.keySet();
            for (String k : keys)
                System.out.println(k + " " + counter.get(k).intValue());
            if (counter.get("RMC").intValue() == 0 &&
                    counter.get("GLL").intValue() == 0 &&
                    counter.get("VTG").intValue() == 0) {
                System.err.println("No RMC, GLL, or VTG!");
            } else if (counter.get("HDG").intValue() == 0 &&
                    counter.get("HDM").intValue() == 0 &&
                    counter.get("VHW").intValue() == 0) {
                System.err.println("No HDM, HDG or VHW!");
            } else { // Proceed
                System.out.println("Proceeding...");
                // Ideal: RMC + HDG
                if (counter.get("RMC").intValue() > 0 &&
                        (counter.get("HDG").intValue() > 0 || counter.get("HDM").intValue() > 0)) {
                    System.out.println("RMC + HDG/HDM, Ideal.");
                    ret = new ArrayList<double[]>(counter.get("RMC").intValue());
                    // Is there a Declination?
                    double decl = -Double.MAX_VALUE;
                    double hdg = 0d; // (cc - D) when available
                    double cog = -Double.MAX_VALUE;
                    try {
                        br = new BufferedReader(new FileReader(dataFileName));
                        keepLooping = true;
                        while (keepLooping) {
                            line = br.readLine();
                            if (line == null) {
                                keepLooping = false;
                            } else {
                                if (line.startsWith("$") && line.length() > 7) { // then let's try
                                    String key = line.substring(3, 6);
                                    if ("HDG".equals(key)) {
                                        try {
                                            HDG val = StringParsers.parseHDG(line);
                                            if (val.getDeviation() != -Double.MAX_VALUE ||
                                                    val.getVariation() != -Double.MAX_VALUE) {
                                                decl = Math.max(val.getDeviation(), val.getVariation());
                                            }
                                            hdg = val.getHeading();
                                            if (decl != -Double.MAX_VALUE) {
                                                hdg += decl;
                                            } else {
                                                hdg += DEFAULT_DECLINATION;
                                            }
                                            // Write data here
                                            if (cog != -Double.MAX_VALUE) {
                                                ret.add(new double[]{hdg, cog});
                                            }
                                        } catch (Exception ex) {
                                        }
                                    } else if ("HDM".equals(key) && counter.get("HDG").intValue() == 0) {
                                        try {
                                            double hdm = StringParsers.parseHDM(line);
                                            if (decl != -Double.MAX_VALUE) {
                                                hdg = hdm + decl;
                                            } else {
                                                hdg = hdm;
                                            }
                                            // Write data here
                                            if (cog != -Double.MAX_VALUE) {
                                                ret.add(new double[]{ hdg, cog });
                                            }
                                        } catch (Exception ex) {
                                        }
                                    } else if ("RMC".equals(key)) {
                                        try {
                                            RMC rmc = StringParsers.parseRMC(line);
                                            if (rmc.getDeclination() != -Double.MAX_VALUE) {
                                                decl = rmc.getDeclination();
                                            }
                                            cog = rmc.getCog();
                                        } catch (Exception ex) {
                                        }
                                    }
                                }
                            }
                        }
                        br.close();
                        if (decl == -Double.MAX_VALUE) {
                            System.out.println("No declination found.");
                        } else {
                            System.out.println("Declination is :" + new Angle180EW(decl).toFormattedString());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (counter.get("VTG").intValue() > 0 &&
                        counter.get("GLL").intValue() > 0 &&
                        (counter.get("HDM").intValue() > 0 || counter.get("HDG").intValue() > 0)) {
                    ret = new ArrayList<double[]>(counter.get("GLL").intValue());
                    System.out.println("VTG, GLL, (HDG or HDM), good enough");
                    // Is there a Declination?
                    double decl = -Double.MAX_VALUE;
                    double hdg = 0d; // (cc - D) when available
                    double cog = -Double.MAX_VALUE;
                    try {
                        br = new BufferedReader(new FileReader(dataFileName));
                        keepLooping = true;
                        while (keepLooping) {
                            line = br.readLine();
                            if (line == null) {
                                keepLooping = false;
                            } else {
                                if (line.startsWith("$") && line.length() > 7) { // then let's try
                                    String key = line.substring(3, 6);
                                    if ("HDG".equals(key)) {
                                        try {
                                            HDG val = StringParsers.parseHDG(line);
                                            if (val.getDeviation() != -Double.MAX_VALUE ||
                                                    val.getVariation() != -Double.MAX_VALUE) {
                                                decl = Math.max(val.getDeviation(), val.getVariation());
                                            }
                                            hdg = val.getHeading();
                                        } catch (Exception ex) {
                                        }
                                    } else if (counter.get("HDM").intValue() == 0 && "HDG".equals(key)) {
                                        hdg = StringParsers.parseHDM(line);
                                    } else if ("GLL".equals(key)) {
                                        // Just for the rhythm. Write data here
                                        if (cog != -Double.MAX_VALUE) {
                                            double delta = cog - hdg;
//                    System.out.println("HDG:" + hdg + "\272, W:" + delta + "\272");
                                            ret.add(new double[]{hdg, cog});
                                        }
                                    } else if ("VTG".equals(key)) {
                                        OverGround og = StringParsers.parseVTG(line);
                                        try {
                                            cog = og.getCourse();
                                        } catch (Exception ex) {
                                        }
                                        if (og == null) {
                                            System.out.println("Null for VTG [" + line + "]");
                                        }
                                    }
                                }
                            }
                        }
                        br.close();
                        if (decl == -Double.MAX_VALUE) {
                            System.out.println("No declination found.");
                        } else {
                            System.out.println("Declination is :" + new Angle180EW(decl).toFormattedString());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("Later...");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    private static String generateCacheAge(String devicePrefix, long age) {
        String std = devicePrefix + "STD,"; // StarTeD
        std += Long.toString(age);
        // Checksum
        int cs = StringParsers.calculateCheckSum(std);
        std += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));
        return "$" + std;
    }

    public static void main(String... args) {
        String data = "Akeu CoucouA*FG\r\n";
        System.out.println(translateEscape(data, ALL_IN_HEXA));
        System.out.println(translateEscape(data, CR_NL));
    }
}
