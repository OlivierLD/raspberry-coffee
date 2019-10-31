package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

	public static class DMS {
		int hours;
		int minutes;
		double seconds;

		public int getHours() {
			return hours;
		}

		public int getMinutes() {
			return minutes;
		}

		public double getSeconds() {
			return seconds;
		}

		public DMS hours(int hours) {
			this.hours = hours;
			return this;
		}
		public DMS minutes(int minutes) {
			this.minutes = minutes;
			return this;
		}
		public DMS seconds(double seconds) {
			this.seconds = seconds;
			return this;
		}
	}

	public static DMS decimalToDMS(double decimalHours) {
		int hours = (int)Math.floor(decimalHours);
		double min = (decimalHours - hours) * 60D;
		double sec = (min - Math.floor(min)) * 60D;
		return new DMS()
				.hours(hours)
				.minutes((int)Math.floor(min))
				.seconds(sec);
	}

	public static String decHoursToDMS(double decimalHours) {
		return decHoursToDMS(decimalHours, "%02d:%02d:%02f");
	}

	public static String decHoursToDMS(double decimalHours, String format) {
		DMS dms = decimalToDMS(decimalHours);
		return String.format(format, dms.hours, dms.minutes, dms.seconds);
	}

	public static Date getGMT() {
		Date now = new Date();
		return getGMT(now);
	}

	public static Date getGMT(Date d) {
		Date now = d;
		Date gmt = null;
		String tzOffset = (new SimpleDateFormat("Z")).format(now);
//  System.out.println("tz:" + tzOffset);
		int offset = 0;
		try {
			if (tzOffset.startsWith("+")) {
				tzOffset = tzOffset.substring(1);
			}
			offset = Integer.parseInt(tzOffset);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		if (offset != 0) {
			long value = offset / 100;
			long longDate = now.getTime();
			longDate -= value * 3_600_000L;
			gmt = new Date(longDate);
		} else {
			gmt = now;
		}
		return gmt;
	}

	public static int getGMTOffset() {
		Date now = new Date();
		String tzOffset = (new SimpleDateFormat("Z")).format(now);
		int offset = 0;
		try {
			if (tzOffset.startsWith("+"))
				tzOffset = tzOffset.substring(1);
			offset = Integer.parseInt(tzOffset);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return offset / 100;
	}

	public static int getLocalGMTOffset() {
		TimeZone tz = Calendar.getInstance().getTimeZone();
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("Z");
		sdf.setTimeZone(TimeZone.getDefault());
		String tzOffset = sdf.format(now);
		int offset = 0;
		try {
			if (tzOffset.startsWith("+"))
				tzOffset = tzOffset.substring(1);
			offset = Integer.parseInt(tzOffset);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		Calendar.getInstance().setTimeZone(tz);
		return offset / 100;
	}

	public static String getTimeZoneLabel() {
		return (new SimpleDateFormat("z")).format(new Date());
	}

	/**
	 * @param howManyMs in ms.
	 */
	public static void delay(long howManyMs) {
		try {
			Thread.sleep(howManyMs);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}
	public static void delay(long ms, int nano) {
		try {
			Thread.sleep(ms, nano);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 *
	 * @param sec in seconds
	 */
	public static void delay(float sec) {
		long ms = (long)Math.floor(sec * 1_000);
		int ns = (int)((sec * 1_000_000_000) - (ms * 1_000_000));
		// Micro: \u03bc
		// System.out.println(String.format("Waiting %s ms and %s \u212bs", NumberFormat.getInstance().format(ms), NumberFormat.getInstance().format(ns)));
		delay(ms, ns);
	}


	private final static long SEC  = 1_000L;
	private final static long MIN  = 60 * SEC;
	private final static long HOUR = 60 * MIN;
	private final static long DAY  = 24 * HOUR;

	public static int[] msToHMS(long ms) {
		long remainder = ms;
		int days = (int) (remainder / DAY);
		remainder -= (days * DAY);
		int hours = (int) (remainder / HOUR);
		remainder -= (hours * HOUR);
		int minutes = (int) (remainder / MIN);
		remainder -= (minutes * MIN);
		int seconds = (int) (remainder / SEC);
		remainder -= (seconds * SEC);
		int millis = (int)remainder;

		return new int[] { days, hours, minutes, seconds, millis };
	}

	public static String fmtDHMS(int[] date) {
		String str = "";
		if (date[0] > 0)
			str = String.format("%d day%s ", date[0], (date[0] > 1 ? "s" : ""));
		if (date[1] > 0 || !str.trim().isEmpty())
			str += String.format("%d hour%s ", date[1], (date[1] > 1 ? "s" : ""));
		if (date[2] > 0 || !str.trim().isEmpty())
			str += (String.format("%d minute%s", date[2], (date[2] > 1 ? "s" : "")));
		if (date[3] > 0 || date[4] > 0) {
			str += (String.format("%s%d.%03d sec%s", (!str.trim().isEmpty() ? " " : ""), date[3], date[4], (date[3] > 1 ? "s" : "")));
		}
		return str;
	}

	public static String readableTime(long elapsed) {
		return readableTime(elapsed, false);
	}

	public static String readableTime(long elapsed, boolean small) {
		long amount = elapsed;
		String str = "";
		final long SECOND = 1_000L;
		final long MINUTE = 60 * SECOND;
		final long HOUR = 60 * MINUTE;
		final long DAY = 24 * HOUR;
		final long WEEK = 7 * DAY;

		if (amount >= WEEK) {
			int week = (int) (amount / WEEK);
			str += (week + (small ? " w " : " week(s) "));
			amount -= (week * WEEK);
		}
		if (amount >= DAY || str.length() > 0) {
			int day = (int) (amount / DAY);
			str += (day + (small ? " d " : " day(s) "));
			amount -= (day * DAY);
		}
		if (amount >= HOUR || str.length() > 0) {
			int hour = (int) (amount / HOUR);
			str += (hour + (small ? " h " : " hour(s) "));
			amount -= (hour * HOUR);
		}
		if (amount >= MINUTE || str.length() > 0) {
			int minute = (int) (amount / MINUTE);
			str += (minute + (small ? " m " : " minute(s) "));
			amount -= (minute * MINUTE);
		}
//  if (amount > SECOND || str.length() > 0)
		{
			int second = (int) (amount / SECOND);
			str += (second + ((amount % 1_000) != 0 ? "." + (amount % 1_000) : "") + (small ? " s " : " second(s) "));
			amount -= (second * SECOND);
		}
		return str;
	}

	public static void main(String args[]) {

		delay((1_000f / 60f) / 1_000f); // 16.66666f

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
		System.out.println("Your GMT Offset:" + Integer.toString(getGMTOffset()) + " hours");
		System.out.println("Current Time is : " + (new Date()).toString());
		System.out.println("GMT is          : " + sdf.format(getGMT()) + " GMT");
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
		}
		Date d = cal.getTime();
		long lTime = d.getTime();
		lTime -= 3_600_000L * gmtOffset;
		System.out.println("GMT for your date:" + sdf.format(new Date(lTime)));

		System.out.println();
		Date now = new Date();
		System.out.println("Date:" + now.toString());
		System.out.println("GTM :" + (new SimpleDateFormat("yyyy MMMMM dd HH:mm:ss 'GMT'").format(getGMT(now))));

		System.out.println("To DMS:" + decHoursToDMS(13.831260480533272));

		long _now = System.currentTimeMillis();
		System.out.println(String.format("Now: %s", fmtDHMS(msToHMS(_now))));

		long elapsed = 231_234_567_890L; // 123456L; //
		System.out.println("Readable time (" + elapsed + ") : " + readableTime(elapsed));
	}
}
