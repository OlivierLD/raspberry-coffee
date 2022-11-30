package scratch;

import calc.calculation.AstroComputer;
import calc.calculation.AstroComputerV2;

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

		System.out.printf("%s, %s\n", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis()));

		cal.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		System.out.printf("%s, %s\n", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis()));

		System.out.printf("%04d-%02d-%02d %02d:%02d:%02d, epoch %s, tz %s\n",
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DATE),
				cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND),
				NumberFormat.getInstance().format(cal.getTimeInMillis()),
				cal.getTimeZone().getID());

		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		epoch = 1_505_312_100_000L;
		cal.setTimeInMillis(epoch);
		SDF.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		System.out.printf("SunSet: %s, %s\n", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis()));
		System.out.printf("%s, %s\n", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis()));

		System.out.printf("%04d-%02d-%02d %02d:%02d:%02d, epoch %s, tz %s\n",
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DATE),
				cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND),
				NumberFormat.getInstance().format(cal.getTimeInMillis()),
				cal.getTimeZone().getID());

		System.out.println(">> Adding 1 hour");
		cal.add(Calendar.HOUR, 1);
		System.out.printf("%s, %s\n", SDF.format(cal.getTime()), NumberFormat.getInstance().format(cal.getTimeInMillis()));

		System.out.printf("%04d-%02d-%02d %02d:%02d:%02d, epoch %s, tz %s\n",
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DATE),
				cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND),
				NumberFormat.getInstance().format(cal.getTimeInMillis()),
				cal.getTimeZone().getID());

		double[] astroData = AstroComputer.getSunMoonAltDecl(
				2017, 9, 13, 18, 58, 25,
				38,	-122);
		System.out.printf("hSun (V1): %.02f\n", astroData[AstroComputer.HE_SUN_IDX]);

		AstroComputerV2 acV2 = new AstroComputerV2();
		double[] astroDataV2 = acV2.getSunMoonAltDecl(
				2017, 9, 13, 18, 58, 25,
				38,	-122);
		System.out.printf("hSun (V2): %.02f\n", astroDataV2[AstroComputerV2.HE_SUN_IDX]);
	}
}
