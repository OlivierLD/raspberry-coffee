package calc.calculation.nauticalalmanac.sample;

import calc.calculation.nauticalalmanac.Anomalies;
import calc.calculation.nauticalalmanac.Context;
import calc.calculation.nauticalalmanac.Core;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Main4Tests {
	public static void main(String... args) {
		int year = 2017;
		int month = Calendar.JANUARY;
		int day = 1;

		Calendar cal = new GregorianCalendar();
		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		for (int yd = 0; yd < 365; yd++) { // 2017 has 365 days
			Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("Etc/UTC"));
			utc.setTimeInMillis(cal.getTimeInMillis());

			int _year = utc.get(Calendar.YEAR);
			int _month = utc.get(Calendar.MONTH) + 1;
			int _day = utc.get(Calendar.DAY_OF_MONTH);
			int _hour = utc.get(Calendar.HOUR_OF_DAY);
			int _minute = utc.get(Calendar.MINUTE);
			int _second = utc.get(Calendar.SECOND);

			double deltaT = 68.8033; // June 2017
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

			System.out.println("-- " + utc.getTime().toString() + ", Mean:" + meanOblOfEcl + ", True:" + trueOblOfEcl + ", Aries GHA:" + Context.GHAAtrue);
			System.out.println("   Polaris D:" + Context.DECpol + ", Z:" + Context.GHApol);
			System.out.println("   Sun Decl:" + Context.DECsun);

			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
	}
}
