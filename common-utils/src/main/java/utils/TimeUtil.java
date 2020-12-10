package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

	private static boolean verbose = "true".equals(System.getProperty("time.verbose"));

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
	  delay(sec, 	TimeUnit.SECONDS);
	}
	public static void delay(float sec, String debugMess) {
		delay(sec, 	TimeUnit.SECONDS, debugMess);
	}
	public static void delay(float amount, TimeUnit unit) {
		delay(amount, unit, null);
	}
	public static void delay(float amount, TimeUnit unit, String debugMess) {
		double amountToMs = 1_000;
		long amountToNanoS = 1_000_000_000;
		final long msToNanoS = 1_000_000;
		long before = 0, after = 0;
		if (unit.equals(TimeUnit.SECONDS)) {
			// Default values above
		} else if (unit.equals(TimeUnit.MILLISECONDS)) {
			amountToMs = 1;
			amountToNanoS = 1_000_000;
		} else if (unit.equals(TimeUnit.MICROSECONDS)) {
			amountToMs = 0.001;
			amountToNanoS = 1_000;
		} else if (unit.equals(TimeUnit.NANOSECONDS)) {
			amountToMs = 0.000_001;
			amountToNanoS = 1;
		} else {
			throw new RuntimeException("Unsupported TimeUnit, only seconds, milliseconds, microseconds and nanoseconds are supported.");
		}
		long ms = (long)Math.floor(amount * amountToMs);
		int ns = (int)((amount * amountToNanoS) - (ms * msToNanoS));
		// rounding issues workaround
		if (ns >= 1_000_000) { // Would raise an IllegalArgumentException
			ms += 1;
			ns -= 1_000_000;
		}
		if (verbose) {
			// Micro: \u03bc, Nano: \u212b
			System.out.println(String.format("Wait requested%s: %f %s => Waiting %s ms and %s nano-s",
					debugMess != null ? String.format(" (%s)", debugMess) : "",
					amount,
					unit,
					NumberFormat.getInstance().format(ms),
					NumberFormat.getInstance().format(ns)));
			before = System.currentTimeMillis();
		}
		delay(ms, ns);
		if (verbose) {
			after = System.currentTimeMillis();
			System.out.println(String.format("\tMeasured diff: %s ms", NumberFormat.getInstance().format(after - before)));
		}
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

	private final static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

	public static String getTimeStamp() {
		String timeStamp = "--:--:--";
		try {
			timeStamp = SDF.format(new Date());
		} catch (Exception ex) {}
		return timeStamp;
	}

	/**
	 *
	 * @param year
	 * @param month in [1..12]
	 * @return
	 */
	private static double getY(int year, int month) {
		if (year < -1_999 || year > 3_000) {
			throw new RuntimeException("Year must be in [-1999, 3000]");
		} else {
			return (year + ((month - 0.5) / 12d));
		}
	}

	/**
	 * See https://astronomy.stackexchange.com/questions/19172/obtaining-deltat-for-use-in-software
	 * See values at https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab1 and
	 *               https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab2
	 *
	 * @param year
	 * @param month in [1..12]
	 * @return
	 */
	public static double getDeltaT(int year, int month) {
		if (year < -1_999 || year > 3_000) {
			throw new RuntimeException("Year must be in [-1999, 3000]");
		}
		if (month < 1 || month > 12) {
			throw new RuntimeException("Month must be in [1, 12]");
		}
		double deltaT = 0d;

		double y = getY(year, month);

		if (year < -500) {
			double u = (y - 1_820d) / 100d;
			deltaT = -20d + (32d * (u * u));
		} else if (year < 500) {
			double u = y / 100d;
			deltaT = 10_583.6
					+ (-1_014.41 * u)
					+ (33.78311 * Math.pow(u, 2))
					+ (-5.952053 * Math.pow(u, 3))
					+ (-0.1798452 * Math.pow(u, 4))
					+ (0.022174192 * Math.pow(u, 5))
					+ (0.0090316521 * Math.pow(u, 6));
		} else if (year < 1_600) {
			double u = (y - 1_000d) / 100d;
			deltaT = 1_574.2
					+ (-556.01 * u)
					+ (71.23472 * Math.pow(u, 2))
					+ (0.319781 * Math.pow(u, 3))
					+ (-0.8503463 * Math.pow(u, 4))
					+ (-0.005050998 * Math.pow(u, 5))
					+ (0.0083572073 * Math.pow(u, 6));
		} else if (year < 1_700) {
			double t = y - 1_600d;
			deltaT = 120
					+ (-0.9808 * t)
					+ (-0.01532 * Math.pow(t, 2))
					+ (Math.pow(t, 3) / 7_129);
		} else if (year < 1_800) {
			double t = y - 1_700d;
			deltaT = 8.83
					+ 0.1603 * t
					+ (-0.0059285 * Math.pow(t, 2))
					+ (0.00013336 * Math.pow(t, 3))
					+ (Math.pow(t, 4) / -1_174_000);
		} else if (year < 1_860) {
			double t = y - 1_800d;
			deltaT = 13.72
					+ (-0.332447 * t)
					+ (0.0068612 * Math.pow(t, 2))
					+ (0.0041116 * Math.pow(t, 3))
					+ (-0.00037436 * Math.pow(t, 4))
					+ (0.0000121272 * Math.pow(t, 5))
					+ (-0.0000001699 * Math.pow(t, 6))
					+ (0.000000000875 * Math.pow(t, 7));
		} else if (year < 1_900) {
			double t = y - 1_860d;
			deltaT = 7.62 +
					(0.5737 * t)
					+ (-0.251754 * Math.pow(t, 2))
					+ (0.01680668 * Math.pow(t, 3))
					+ (-0.0004473624 * Math.pow(t, 4))
					+ (Math.pow(t, 5) / 233_174);
		} else if (year < 1_920) {
			double t = y - 1_900;
			deltaT = -2.79
					+ (1.494119 * t)
					+ (-0.0598939 * Math.pow(t, 2))
					+ (0.0061966 * Math.pow(t, 3))
					+ (-0.000197 * Math.pow(t, 4));
		} else if (year < 1_941) {
			double t = y - 1_920;
			deltaT = 21.20
					+ (0.84493 * t)
					+ (-0.076100 * Math.pow(t, 2))
					+ (0.0020936 * Math.pow(t, 3));
		} else if (year < 1_961) {
			double t = y - 1_950;
			deltaT = 29.07
					+ (0.407 * t)
					+ (Math.pow(t, 2) / -233)
					+ (Math.pow(t, 3) / 2_547);
		} else if (year < 1_986) {
			double t = y - 1_975;
			deltaT = 45.45
					+ (1.067 * t)
					+ (Math.pow(t, 2) / -260)
					+ (Math.pow(t, 3) / -718);
		} else if (year < 2_005) {
			double t = y - 2_000;
			deltaT = 63.86
					+ (0.3345 * t)
					+ (-0.060374 * Math.pow(t, 2))
					+ (0.0017275 * Math.pow(t, 3))
					+ (0.000651814 * Math.pow(t, 4))
					+ (0.00002373599 * Math.pow(t, 5));
 	  	} else if (year < 2_050) {
			double t = y - 2_000;
			deltaT = 62.92
					+ (0.32217 * t)
					+ (0.005589 * Math.pow(t, 2));
		} else if (year < 2_150) {
			deltaT = -20
					+ (32 * Math.pow((y - 1_820) / 100, 2))
					+ (-0.5628 * (2_150 - y));
		} else {
			double u = (y - 1_820) / 100;
			deltaT = -20
					+ (32 * Math.pow(u, 2));
		}

		return deltaT;
	}

	public static void main(String... args) {

		System.setProperty("time.verbose", "true");
		verbose = true;

		delay((1_000f / 60f) / 1_000f); // 16.66666f
		delay(2.5f, TimeUnit.MILLISECONDS);
		delay(1.5f, TimeUnit.MICROSECONDS);
		delay(150f, TimeUnit.NANOSECONDS);

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
		String[] months = new String[] {"Jan", "Feb", "Mar","Apr", "May", "Jun",
		                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		int year = 2020, month = 1;
		System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, getDeltaT(year, month)));
		month = 12;
		System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, getDeltaT(year, month)));
		year = 1955; month = 1;
		System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, getDeltaT(year, month)));
		year = 1960; month = 1;
		System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, getDeltaT(year, month)));
		year = 1965; month = 1;
		System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, getDeltaT(year, month)));
		year = 2000; month = 1;
		System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, getDeltaT(year, month)));
		year = 2005; month = 1;
		System.out.println(String.format("DeltaT %s %d: %f", months[month - 1], year, getDeltaT(year, month)));

		if (false) {
			StringBuffer duh = new StringBuffer();
			for (int i = -1_999; i < 2_020; i++) {
				duh.append(String.format("%d;%f;\n", i, getDeltaT(i, 1)));
			}
			System.out.println(duh.toString());
		}
	}
}
