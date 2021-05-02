package oliv.time;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

/**
 * Use legacy classes
 */
public class TimeZones {

    private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public final static String decHoursToHM(float decHours) {
        int intHours = (int)decHours;
        float decimalPart = Math.abs(decHours - intHours);
        decimalPart *= 60f;
        return String.format("%+d:%02d", intHours, (int)decimalPart);
    }

    public static void main(String... args) {

        Date now = new Date();
        DURATION_FMT.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        System.out.printf("Now, here: %s\n", DURATION_FMT.format(now));

        Arrays.stream(TimeZone.getAvailableIDs())
                .forEach(tzId -> {
                    TimeZone tz = TimeZone.getTimeZone(tzId);
                    DURATION_FMT.setTimeZone(tz);
                    int offset = tz.getOffset(now.getTime()); // in ms
                    float offsetInHours = (float) offset / (3_600f * 1_000f);
                    System.out.printf("At %s (%s), it is %s (UTC%s)\n", tzId, tz.getDisplayName(), DURATION_FMT.format(now), decHoursToHM(offsetInHours));
                });
    }
}
