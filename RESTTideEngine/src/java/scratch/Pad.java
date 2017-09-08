package scratch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Pad {
	public static void main(String... args) {
		long epoch = 1_504_821_600_841L;
		String tzName = "Europe/Paris";
		SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z z");

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(tzName));
		cal.setTimeInMillis(epoch);

		SDF.setTimeZone(TimeZone.getTimeZone(tzName));
		System.out.println(SDF.format(cal.getTime()));
	}
}
