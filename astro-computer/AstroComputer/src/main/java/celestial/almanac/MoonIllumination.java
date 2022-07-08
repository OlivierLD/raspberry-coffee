package celestial.almanac;

import calc.calculation.AstroComputer;
import utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Another quick example, using AstroComputer (deprecated)
 * Run the main.
 */
@SuppressWarnings("deprecation")
public class MoonIllumination {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
    private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        DURATION_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    private static final String HELP_PREFIX    = "--help";
    private static final String CSV_PREFIX     = "--csv:"; // true | false
    private static final String VERBOSE_PREFIX = "--verbose:"; // true | false
    private static final String FROM_PREFIX    = "--from:"; // Can be --from:now, or a date in duration format.
    private static final String LENGTH_PREFIX  = "--length:"; // XX:YEAR - See SupportedInterval
    private static final String STEP_PREFIX    = "--step:";   // YY:MINUTE - See SupportedInterval

    enum SupportedInterval {

        YEAR(Calendar.YEAR),
        MONTH(Calendar.DAY_OF_MONTH),
        DAY(Calendar.DAY_OF_MONTH),
        HOUR(Calendar.HOUR_OF_DAY),
        MINUTE(Calendar.MINUTE),
        SECOND(Calendar.SECOND);

        private final int calendarField;

        SupportedInterval(int field) {
            this.calendarField = field;
        }

        public int interval() {
            return this.calendarField;
        }
    }

    private static void displayHelp() {
        System.out.println("CLI Parameters:");
        System.out.println("--help \t Display this message and exit.");
        System.out.println("--verbose:true|false \tDefault is false.");
        System.out.println("--csv:true|false \tProduce the data as CSV (tab-separated). Default is false.");
        System.out.println("--from:now or --from:yyyy-mm-ddTHH:mi:ss \tStart date of computation. Default is now.");
        System.out.println("--length:xx:YEAR|MONTH|DAY|HOUR|MINUTE|SECOND, like --length:2:MONTH \tInterval covered by computation. Default is 1:MONTH.");
        System.out.println("--step:xx:YEAR|MONTH|DAY|HOUR|MINUTE|SECOND, like --step:10:MINUTE \tStep between dates for computation. Default is 1:HOUR.");
    }

    public static void main(String... args) {

        AtomicBoolean now = new AtomicBoolean(true);
        AtomicLong start = new AtomicLong(0L);
        AtomicInteger lengthValue = new AtomicInteger(1);    // Default
        AtomicInteger lengthType = new AtomicInteger(Calendar.MONTH);  // Default

        AtomicInteger stepValue = new AtomicInteger(1);          // Default
        AtomicInteger stepType = new AtomicInteger(Calendar.HOUR_OF_DAY);  // Default

        boolean help = Arrays.stream(args).anyMatch(HELP_PREFIX::equals);
        if (help) {
            displayHelp();
            System.exit(0);
        }

        boolean csv = Arrays.stream(args).anyMatch((CSV_PREFIX + "true")::equals);
        boolean verbose = Arrays.stream(args).anyMatch((VERBOSE_PREFIX + "true")::equals);
        Arrays.asList(args).forEach(arg -> {
           if (arg.startsWith(FROM_PREFIX)) {
               String value = arg.substring(FROM_PREFIX.length()).trim();
               if ("now".equals(value)) {
                   now.set(true);
               } else {
                   now.set(false);
                   try {
                       Date from = DURATION_FMT.parse(value);
                       start.set(from.getTime());
                   } catch (ParseException e) {
                       System.err.println("What is that date???");
                       throw new RuntimeException(e);
                   }

               }
           } else if (arg.startsWith(LENGTH_PREFIX)) {
               String value = arg.substring(LENGTH_PREFIX.length()).trim();
               String[] nvPair = value.split(":");
               if (nvPair.length != 2) {
                   throw new RuntimeException(String.format("Invalid Length [%s]", value));
               }
               try {
                   lengthValue.set(Integer.parseInt(nvPair[0].trim()));
               } catch (NumberFormatException nfe) {
                   throw new RuntimeException(String.format("Bad value [%s]", nvPair[0]), nfe);
               }
               Optional<SupportedInterval> first = Arrays.stream(SupportedInterval.values())
                       .filter(val -> nvPair[1].equals(val.toString()))
                       .findFirst();
               if (!first.isPresent()) {
                   throw new RuntimeException(String.format("Bad value [%s]", nvPair[1]));
               } else {
                   lengthType.set(first.get().interval());
               }
           } else if (arg.startsWith(STEP_PREFIX)) {
               String value = arg.substring(STEP_PREFIX.length()).trim();
               String[] nvPair = value.split(":");
               if (nvPair.length != 2) {
                   throw new RuntimeException(String.format("Invalid Step [%s]", value));
               }
               try {
                   stepValue.set(Integer.parseInt(nvPair[0].trim()));
               } catch (NumberFormatException nfe) {
                   throw new RuntimeException(String.format("Bad value [%s]", nvPair[0]), nfe);
               }
               Optional<SupportedInterval> first = Arrays.stream(SupportedInterval.values())
                       .filter(val -> nvPair[1].equals(val.toString()))
                       .findFirst();
               if (!first.isPresent()) {
                   throw new RuntimeException(String.format("Bad value [%s]", nvPair[1]));
               } else {
                   stepType.set(first.get().interval());
               }
           }
        });

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        if (!now.get()) { // User provided date
            Date startingDate = new Date(start.get());
            date.setTime(startingDate);
            date.set(Calendar.MILLISECOND, 0);
        }
        // Recalculate
        double deltaT = TimeUtil.getDeltaT(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1);
        AstroComputer.setDeltaT(deltaT);
        if (verbose) {
            System.out.printf(">> deltaT: %f s\n", deltaT);
        }

        Calendar until = (Calendar)date.clone(); // End date.
        until.add(lengthType.get(), lengthValue.get());

        if (verbose) {
            System.out.printf("Calculations from %s to %s\n",
                    SDF_UTC.format(date.getTime()),
                    SDF_UTC.format(until.getTime()));
        }

        if (csv) {
            System.out.println("Date\tMoon Illumination (%)\tMoon Phase (\272)");
        }

        boolean firstLine = true,
                newMoon = false,
                firstQuarter = false,
                fullMoon = false,
                lastQuarter = false;

        String exactPhase = "";
        while (!date.after(until)) {
            // All calculations here
            // TODO Set/Reset DeltaT here ?
            AstroComputer.calculate(
                    date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH) + 1, // Jan: 1, Dec: 12.
                    date.get(Calendar.DAY_OF_MONTH),
                    date.get(Calendar.HOUR_OF_DAY), // and not just Calendar.HOUR !!!!
                    date.get(Calendar.MINUTE),
                    date.get(Calendar.SECOND));
            double moonIllum = AstroComputer.getMoonIllum();
            double moonPhase = AstroComputer.getMoonPhase();
            if (moonPhase > 270) {
                if (!lastQuarter) {
                    exactPhase = firstLine ? "" : "Last Quarter";
                    newMoon = false;
                    firstQuarter = false;
                    fullMoon = false;
                    lastQuarter = true;
                } else {
                    exactPhase = "";
                }
            } else if (moonPhase > 180) {
                if (!fullMoon) {
                    exactPhase = firstLine ? "" : "Full Moon";
                    newMoon = false;
                    firstQuarter = false;
                    fullMoon = true;
                    lastQuarter = false;
                } else {
                    exactPhase = "";
                }
            } else if (moonPhase > 90) {
                if (!firstQuarter) {
                    exactPhase = firstLine ? "" : "First Quarter";
                    newMoon = false;
                    firstQuarter = true;
                    fullMoon = false;
                    lastQuarter = false;
                } else {
                    exactPhase = "";
                }
            } else if (moonPhase > 0) {
                if (!newMoon) {
                    exactPhase = firstLine ? "" : "New Moon";
                    newMoon = true;
                    firstQuarter = false;
                    fullMoon = false;
                    lastQuarter = false;
                } else {
                    exactPhase = "";
                }
            }
            if (csv) {
                System.out.printf("%s\t%07.04f\t%06.02f\t%s\n",
                        SDF_UTC.format(date.getTime()),
                        moonIllum,
                        moonPhase,
                        exactPhase);
            } else {
                System.out.printf("%s - Moon Illumination: %07.04f%% - Phase %06.02f\272 %s\n",
                        SDF_UTC.format(date.getTime()),
                        moonIllum,
                        moonPhase,
                        exactPhase);
            }
            // Increment by 1 hour, or another amount
//            date.add(Calendar.HOUR_OF_DAY, 1);
            date.add(stepType.get(), stepValue.get());
            firstLine = false;
        }
    }
}
