package astro;

import calc.calculation.AstroComputerV2;
import utils.TimeUtil;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Calculate exact date of Spring, Summer, Fall, and Winter.
 * <br/>
 * See the 'narrow' methods, to get more precision.
 * <br/>
 * Initial calculation is done with a step of one day, and when a milestone is found (Equinox or Solstice),
 * we drill down to HOURS, MINUTES, SECONDS.
 * <br/>
 * See {@link SpringSummerFallWinter#narrowSpringFall(Calendar, int, boolean)} and {@link SpringSummerFallWinter#narrowSummerWinter(Calendar, int, boolean)}.
 */
public class SpringSummerFallWinter {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("EEE yyyy-MMM-dd HH:mm:ss 'UTC'");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    private final static AstroComputerV2 astroComputerV2 = new AstroComputerV2();

    private final static int[] STEPS = { Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
    private final static int STEP_BACK_FOR_NARROWING = 2;

    private static Calendar narrowSpringFall(Calendar from, int stepIndex, boolean goingUp) {
        Calendar cal = (Calendar)from.clone();
        astroComputerV2.calculate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
        double decl = astroComputerV2.getSunDecl();
        while ((goingUp && decl < 0) || (!goingUp && decl > 0)) {
            cal.add(STEPS[stepIndex], 1);
            astroComputerV2.calculate(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND));
            decl = astroComputerV2.getSunDecl();
        }
        if (stepIndex < STEPS.length - 1) {
            cal.add(STEPS[stepIndex], -STEP_BACK_FOR_NARROWING);
            return narrowSpringFall(cal, stepIndex + 1, goingUp);
        } else {
            return cal;
        }
    }

    private static Calendar narrowSummerWinter(Calendar from, int stepIndex, boolean goingUp) {
        Calendar cal = (Calendar)from.clone();
        astroComputerV2.calculate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
        double prevDecl = astroComputerV2.getSunDecl();
        double deltaDecl = goingUp ? 1 : -1;
        while ((goingUp && deltaDecl > 0) || (!goingUp && deltaDecl < 0)) {
            cal.add(STEPS[stepIndex], 1);
            astroComputerV2.calculate(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND));
            double decl = astroComputerV2.getSunDecl();
            deltaDecl = decl - prevDecl;
            prevDecl = decl;
        }
        if (stepIndex < STEPS.length - 1) {
            cal.add(STEPS[stepIndex], -STEP_BACK_FOR_NARROWING);
            return narrowSummerWinter(cal, stepIndex + 1, goingUp);
        } else {
            return cal;
        }
    }

    private final static boolean VERBOSE = false;

    public static void main(String... args) {
        System.setProperty("deltaT", "AUTO");
//        System.setProperty("astro.verbose", "true");

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        int currentYear = date.get(Calendar.YEAR); // Or hard-code the date you want here
        System.out.printf("Year %d\n", currentYear);

        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("Etc/UTC" /*"America/Los_Angeles"*/));
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        System.out.println("Setting date to " + SDF_UTC.format(new Date(cal.getTime().getTime())) + ", let's go.");
        // For info...
        double deltaT = TimeUtil.getDeltaT(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
        System.out.printf("Will use DeltaT for [%04d-%02d]: %f\n", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, deltaT);

        double prevDecl = -Double.MAX_VALUE;
        boolean goingUp = true; // Assuming it is not spring yet, and winter behind us.
        boolean crossed = false;
        long iterations = 0L;
        int stepIndex = 0;
        long before = System.currentTimeMillis();
        while (cal.get(Calendar.YEAR) == currentYear) {
            iterations++;
            astroComputerV2.calculate(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND));

            double sunDecl = astroComputerV2.getSunDecl();
            if (goingUp && sunDecl > 0 && !crossed) { // Equinox
                Calendar savedCal = (Calendar)cal.clone();
                cal.add(STEPS[stepIndex], -STEP_BACK_FOR_NARROWING);
                cal = narrowSpringFall(cal, 1, goingUp);
                System.out.println("Northern Spring / Southern Fall:\t" + SDF_UTC.format(new Date(cal.getTime().getTime())));
                crossed = true;
                cal = savedCal; // To re-start when we left
            }
            if (!goingUp && sunDecl < 0 && !crossed) { // Equinox
                Calendar savedCal = (Calendar)cal.clone();
                cal.add(STEPS[stepIndex], -STEP_BACK_FOR_NARROWING);
                cal = narrowSpringFall(cal, 1, goingUp);
                System.out.println("Northern Fall / Southern Spring:\t" + SDF_UTC.format(new Date(cal.getTime().getTime())));
                crossed = true;
                cal = savedCal; // To re-start when we left
            }
            if (goingUp && sunDecl < prevDecl) { // Solstice
                Calendar savedCal = (Calendar)cal.clone();
                cal.add(STEPS[stepIndex], -STEP_BACK_FOR_NARROWING);
                cal = narrowSummerWinter(cal, 1, goingUp);
                // Northern Summer
                System.out.println("Northern Summer / Southern Winter:\t" + SDF_UTC.format(new Date(cal.getTime().getTime())));
                cal = savedCal; // To re-start when we left
                goingUp = false;
                crossed = false;
            }
            if (!goingUp && sunDecl > prevDecl) { // Solstice
                Calendar savedCal = (Calendar)cal.clone();
                cal.add(STEPS[stepIndex], -STEP_BACK_FOR_NARROWING);
                cal = narrowSummerWinter(cal, 1, goingUp);
                // Southern Summer
                System.out.println("Northern Winter / Southern Summer:\t" + SDF_UTC.format(new Date(cal.getTime().getTime())));
                cal = savedCal; // To re-start when we left
                goingUp = true;
                crossed = false;
            }
            if (VERBOSE) {
                System.out.println("\tDate is now " + SDF_UTC.format(new Date(cal.getTime().getTime())));
            }
            prevDecl = sunDecl;
            cal.add(STEPS[stepIndex], 1);
        }
        long after = System.currentTimeMillis();
        System.out.printf("Used %s iterations, in %s ms.\n",
                NumberFormat.getInstance().format(iterations),
                NumberFormat.getInstance().format(after - before));
    }
}
