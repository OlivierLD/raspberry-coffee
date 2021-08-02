package almanac;

import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import calc.calculation.nauticalalmanac.Anomalies;
import calc.calculation.nauticalalmanac.Context;
import calc.calculation.nauticalalmanac.Core;
import calc.GeomUtil;

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

			Core.julianDate(_year, _month, _day, _hour, _minute, _second, 68.5928);
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
		// Sun orientation, now.
		double lat = 37.7489;
		double lng = -122.5070;
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));

		AstroComputerV2 acv2 = new AstroComputerV2();
		int h = 0;
		while (h < 48) {

			acv2.setDateTime(current.get(Calendar.YEAR),
					current.get(Calendar.MONTH) + 1,
					current.get(Calendar.DAY_OF_MONTH),
					current.get(Calendar.HOUR_OF_DAY),
					current.get(Calendar.MINUTE),
					current.get(Calendar.SECOND));
			acv2.calculate();
			SightReductionUtil sru = new SightReductionUtil(acv2.getSunGHA(),
					acv2.getSunDecl(),
					lat,
					lng);
			sru.calculate();
			Double he = sru.getHe();
			Double z = sru.getZ();
			// Local time
			Calendar local = (Calendar) current.clone();
			local.setTimeZone(TimeZone.getDefault());
			System.out.println(String.format("From %s / %s, at %02d:%02d:%02d UTC (%02d:%02d:%02d Local), He:%.02f\272, Z:%.02f\272 (true)",
					GeomUtil.decToSex(lat, GeomUtil.SWING, GeomUtil.NS),
					GeomUtil.decToSex(lng, GeomUtil.SWING, GeomUtil.EW),
					current.get(Calendar.HOUR_OF_DAY),
					current.get(Calendar.MINUTE),
					current.get(Calendar.SECOND),
					local.get(Calendar.HOUR_OF_DAY),
					local.get(Calendar.MINUTE),
					local.get(Calendar.SECOND),
					he.doubleValue(),
					z.doubleValue()));

			current.add(Calendar.HOUR, 1);
			h += 1;
		}
	}
}
