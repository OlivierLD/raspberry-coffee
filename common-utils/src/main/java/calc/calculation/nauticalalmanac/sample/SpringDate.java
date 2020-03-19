package calc.calculation.nauticalalmanac.sample;

import calc.calculation.nauticalalmanac.Anomalies;
import calc.calculation.nauticalalmanac.Context;
import calc.calculation.nauticalalmanac.Core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class SpringDate {

	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z z");
	static {
		SDF.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	public static void main(String... args) {
		int year = 2020;
		int month = Calendar.MARCH;
		int day = 19;


		double sunD = -10;

		Calendar cal = new GregorianCalendar();
		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		Calendar march22 = new GregorianCalendar();
		march22.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		march22.set(Calendar.YEAR, year);
		march22.set(Calendar.MONTH, month);
		march22.set(Calendar.DAY_OF_MONTH, 22);
		march22.set(Calendar.HOUR_OF_DAY, 0);
		march22.set(Calendar.MINUTE, 0);
		march22.set(Calendar.SECOND, 0);

		while (cal.before(march22) && sunD < 0) {
			Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("Etc/UTC"));
			utc.setTimeInMillis(cal.getTimeInMillis());

			int _year = utc.get(Calendar.YEAR);
			int _month = utc.get(Calendar.MONTH) + 1;
			int _day = utc.get(Calendar.DAY_OF_MONTH);
			int _hour = utc.get(Calendar.HOUR_OF_DAY);
			int _minute = utc.get(Calendar.MINUTE);
			int _second = utc.get(Calendar.SECOND);

			double deltaT = 69.2201; // 2020...
			Core.julianDate(_year, _month, _day, _hour, _minute, _second, deltaT);
			Anomalies.nutation();
			Anomalies.aberration();

			Core.aries();
			Core.sun();

//    Moon.compute(); // Important! Moon is used for lunar distances, by planets and stars.

//    Venus.compute();
//    Mars.compute();
//    Jupiter.compute();
//    Saturn.compute();

			Core.polaris();
//    Core.moonPhase();
//    Core.weekDay();

			double meanOblOfEcl = Context.eps0;
			double trueOblOfEcl = Context.eps;

			// System.out.println("-- " + utc.getTime().toString() + ", Mean:" + meanOblOfEcl + ", True:" + trueOblOfEcl + ", Aries GHA:" + Context.GHAAtrue);
			// System.out.println("   Polaris D:" + Context.DECpol + ", Z:" + Context.GHApol);
			sunD = Context.DECsun;
			if (sunD >= 0) {
				Date spring = utc.getTime();
				System.out.println(String.format("Spring at %s, Sun Decl: %f", SDF.format(spring), sunD));
			}

			cal.add(Calendar.SECOND, 1);
		}
	}
}
