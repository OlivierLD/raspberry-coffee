package astro.sample;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Is there a bug in 2038 for Java?
 * Run the main.
 */
public class Bug2038 {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");

    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    public static void main(String... args) {

        System.out.println(String.format("Long Max value: %s", NumberFormat.getInstance().format(Long.MAX_VALUE)));
        System.out.println(String.format("Int Max value: %s", NumberFormat.getInstance().format(Integer.MAX_VALUE)));

        Date minDate = new Date(0);
        System.out.println(String.format("Min Date: %s", SDF_UTC.format(minDate)));

        Date maxDate = new Date(Long.MAX_VALUE);
        System.out.println(String.format("Max Date (long): %s", SDF_UTC.format(maxDate)));

//        maxDate = new Date(Integer.MAX_VALUE);
//        System.out.println(String.format("Max Date (integer): %s", SDF_UTC.format(maxDate)));

        maxDate = new Date(1_000L * Integer.MAX_VALUE);
        System.out.printf("%s seconds after 1970-01-01 00:00:00:\n", NumberFormat.getInstance().format(Integer.MAX_VALUE));
        System.out.println(String.format("Max Date (integer * 1000): %s <- This is where Unix might have problems...", SDF_UTC.format(maxDate)));

    }
}
