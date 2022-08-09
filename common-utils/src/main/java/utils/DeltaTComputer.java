package utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * To run in standalone from the command line
 */
public class DeltaTComputer {

    private final static String YEAR_PREFIX = "--year:";
    private final static String MONTH_PREFIX = "--month:";
    private final static String NOW_PREFIX = "--now";

    private final static String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * @param args --year:2020 --month:3, or --now
     */
    public static void main(String... args) {

        System.out.printf("This is class %s%n", DeltaTComputer.class.getName());

        if (args.length == 0) {
            System.out.println("Usage is: java utils.DeltaTComputer --year:XXXX --month:YY");
            System.out.println("      or: java utils.DeltaTComputer --now");
            System.out.println("- year between -1999 and 3000");
            System.out.println("- month between 01 (jan) and 12 (dec)");
            System.exit(0);
        }
        boolean now = false;
        int year = Integer.MIN_VALUE;
        int month = -1;

        for (String arg : args) {
            if (NOW_PREFIX.equals(arg)) {
                now = true;
                break;
            } else if (arg.startsWith(YEAR_PREFIX)) {
                year = Integer.parseInt(arg.substring(YEAR_PREFIX.length()));
            } else if (arg.startsWith(MONTH_PREFIX)) {
                month = Integer.parseInt(arg.substring(MONTH_PREFIX.length()));
            }
        }
        if (now) {
            Calendar timeNow = GregorianCalendar.getInstance();
            year = timeNow.get(Calendar.YEAR);
            month = timeNow.get(Calendar.MONTH) + 1;
        } else {
            if (year == Integer.MIN_VALUE || month == -1) {
                System.out.println("If --now is not there, you need to provide both --year: and --month:");
                System.out.printf("\tYou provided: %s%n", String.join(" ", Arrays.asList(args)));
                System.exit(1);
            }
        }
        double deltaT = TimeUtil.getDeltaT(year, month);
        System.out.printf("For %s %d => \u0394T %f s\n", MONTHS[month - 1], year, deltaT);
    }
}
