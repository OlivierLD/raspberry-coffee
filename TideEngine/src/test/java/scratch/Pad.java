package scratch;

import calc.calculation.AstroComputer;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Pad {
	public static void main(String... args) {
//	long epoch = 1_504_821_600_841L;
		long epoch = 1_235_700_000_000L;
		String tzName = "Europe/Paris";
		SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z z");

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(tzName));
		cal.setTimeInMillis(epoch);

		SDF.setTimeZone(TimeZone.getTimeZone(tzName));

		System.out.println(String.format("%s, %s", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis())));

		cal.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		System.out.println(String.format("%s, %s", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis())));

		System.out.println(String.format("%04d-%02d-%02d %02d:%02d:%02d, epoch %s, tz %s",
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DATE),
				cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND),
				NumberFormat.getInstance().format(cal.getTimeInMillis()),
				cal.getTimeZone().getID()));

		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		epoch = 1_505_312_100_000L;
		cal.setTimeInMillis(epoch);
		SDF.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		System.out.println(String.format("SunSet: %s, %s", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis())));
		System.out.println(String.format("%s, %s", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis())));

		System.out.println(String.format("%04d-%02d-%02d %02d:%02d:%02d, epoch %s, tz %s",
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DATE),
				cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND),
				NumberFormat.getInstance().format(cal.getTimeInMillis()),
				cal.getTimeZone().getID()));

		System.out.println(">> Adding 1 hour");
		cal.add(Calendar.HOUR, 1);
		System.out.println(String.format("%s, %s", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis())));

		System.out.println(String.format("%04d-%02d-%02d %02d:%02d:%02d, epoch %s, tz %s",
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DATE),
				cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND),
				NumberFormat.getInstance().format(cal.getTimeInMillis()),
				cal.getTimeZone().getID()));

		double[] astroData = AstroComputer.getSunMoonAltDecl(
				2017,
				9,
				13,
				18,
				58,
				25,
				38,
				-122);
		System.out.println(String.format("hSun: %.02f", astroData[AstroComputer.HE_SUN_IDX]));
	}
}
