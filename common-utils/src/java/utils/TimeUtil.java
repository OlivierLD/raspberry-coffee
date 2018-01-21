package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

	public static String decHoursToDMS(double decimalHours) {
		String s = "";
		int hours = (int)Math.floor(decimalHours);
		double min = (decimalHours - hours) * 60D;
		double sec = (min - Math.floor(min)) * 60D;
		s = String.format("%02d:%02d:%02d", hours, (int)Math.floor(min), (int)Math.round(sec));
		return s;
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
	 * @param howMuch in ms.
	 */
	public static void delay(long howMuch) {
		try {
			Thread.sleep(howMuch);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}


	public static void main(String args[]) {
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
		} catch (NumberFormatException numberformatexception) {
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
		} catch (NumberFormatException numberformatexception1) {
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
		} catch (NumberFormatException numberformatexception2) {
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
		} catch (NumberFormatException numberformatexception3) {
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
		} catch (NumberFormatException numberformatexception4) {
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
	}
}
