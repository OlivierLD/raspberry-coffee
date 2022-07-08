package astro.sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import utils.TimeUtil;

public class TimeUtilTest {

    private static boolean verbose = false;
    /**
     * This is for tests
     *
     * @param args
     */
    public static void main(String... args) {

        System.setProperty("time.verbose", "true");
        verbose = true;

        TimeUtil.delay((1_000f / 60f) / 1_000f); // 16.66666f
        TimeUtil.delay(2.5f, TimeUnit.MILLISECONDS);
        TimeUtil.delay(1.5f, TimeUnit.MICROSECONDS);
        TimeUtil.delay(150f, TimeUnit.NANOSECONDS);

        boolean more = false;
        if (more) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String retString = "";
            String prompt = "?> ";
            System.err.print(prompt);
            try {
                retString = stdin.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("Your GMT Offset:" + Integer.toString(TimeUtil.getGMTOffset()) + " hours");
            System.out.println("Current Time is : " + (new Date()).toString());
            System.out.println("GMT is          : " + sdf.format(TimeUtil.getGMT()) + " GMT");
            System.out.println("");
            prompt = "Please enter a year [9999]       > ";
            int year = 0;
            int month = 0;
            int day = 0;
            int h = 0;
            System.err.print(prompt);
            try {
                retString = stdin.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
            try {
                year = Integer.parseInt(retString);
            } catch (NumberFormatException numberFormatException) {
                numberFormatException.printStackTrace();
            }
            prompt = "Please enter a month (1-12) [99] > ";
            System.err.print(prompt);
            try {
                retString = stdin.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
            try {
                month = Integer.parseInt(retString);
            } catch (NumberFormatException numberFormatException1) {
                numberFormatException1.printStackTrace();
            }
            prompt = "Please enter a day (1-31) [99]   > ";
            System.err.print(prompt);
            try {
                retString = stdin.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
            try {
                day = Integer.parseInt(retString);
            } catch (NumberFormatException numberFormatException2) {
                numberFormatException2.printStackTrace();
            }
            prompt = "Please enter an hour (0-23) [99] > ";
            System.err.print(prompt);
            try {
                retString = stdin.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
            try {
                h = Integer.parseInt(retString);
            } catch (NumberFormatException numberFormatException3) {
                numberFormatException3.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, day, h, 0, 0);
            System.out.println("You've entered:" + sdf.format(cal.getTime()));
            int gmtOffset = 0;
            prompt = "\nPlease enter the GMT offset for that date > ";
            System.err.print(prompt);
            try {
                retString = stdin.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
            try {
                gmtOffset = Integer.parseInt(retString);
            } catch (NumberFormatException numberFormatException4) {
                numberFormatException4.printStackTrace();
            }
            Date d = cal.getTime();
            long lTime = d.getTime();
            lTime -= 3_600_000L * gmtOffset;
            System.out.println("GMT for your date:" + sdf.format(new Date(lTime)));

            System.out.println();
            Date now = new Date();
            System.out.println("Date:" + now.toString());
            System.out.println("GTM :" + (new SimpleDateFormat("yyyy MMMMM dd HH:mm:ss 'GMT'").format(TimeUtil.getGMT(now))));

            System.out.println("To DMS:" + TimeUtil.decHoursToDMS(13.831260480533272));

            long _now = System.currentTimeMillis();
            System.out.println(String.format("Now: %s", TimeUtil.fmtDHMS(TimeUtil.msToHMS(_now))));

            long elapsed = 231_234_567_890L; // 123456L; //
            System.out.println("Readable time (" + elapsed + ") : " + TimeUtil.readableTime(elapsed));
        }
        String[] months = new String[] {"Jan", "Feb", "Mar","Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int year = 2020, month = 1;
        System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, TimeUtil.getDeltaT(year, month)));
        month = 12;
        System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, TimeUtil.getDeltaT(year, month)));
        year = 1955; month = 1;
        System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, TimeUtil.getDeltaT(year, month)));
        year = 1960; month = 1;
        System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, TimeUtil.getDeltaT(year, month)));
        year = 1965; month = 1;
        System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, TimeUtil.getDeltaT(year, month)));
        year = 2000; month = 1;
        System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, TimeUtil.getDeltaT(year, month)));
        year = 2005; month = 1;
        System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, TimeUtil.getDeltaT(year, month)));
        year = 2017; month = 6;
        System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, TimeUtil.getDeltaT(year, month)));

        if (true) {
            StringBuffer duh = new StringBuffer();
            for (int i = -1_999; i < 2_020; i++) {
                duh.append(String.format("%d;%f;\n", i, TimeUtil.getDeltaT(i, 1)));
            }
            System.out.println(duh.toString());
        }
    }
}
