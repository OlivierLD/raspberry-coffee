package oliv.moon;

import calc.GeomUtil;
import calc.calculation.AstroComputer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

// http://master.grad.hr/hdgg/kog_stranica/kog18/06myers-KoG18.pdf
public class MoonTilt {

	public static void main(String... args) {
		SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
//		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

		// SF Home
		double lat = 37.7489;
		double lng = -122.5070;

//		System.setProperty("astro.verbose", "true");

//		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
//		System.out.println(String.format("Setting Cal Date to %d-%02d-%02d %02d:%02d:%02d",
//				date.get(Calendar.YEAR),
//				date.get(Calendar.MONTH) + 1,
//				date.get(Calendar.DAY_OF_MONTH),
//				date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
//				date.get(Calendar.MINUTE),
//				date.get(Calendar.SECOND)));
//		AstroComputer.setDateTime(date.get(Calendar.YEAR),
//				date.get(Calendar.MONTH) + 1,
//				date.get(Calendar.DAY_OF_MONTH),
//				date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
//				date.get(Calendar.MINUTE),
//				date.get(Calendar.SECOND));

		AstroComputer.calculate();
		double moonTilt = AstroComputer.getMoonTilt(lat, lng);

		double moonTiltV2 = AstroComputer.getMoonTiltV2(lat, lng);

		Calendar calculationDateTime = AstroComputer.getCalculationDateTime();
		System.out.println(String.format("At %s, Moon Tilt V1: %.03f\272 (%s), V2: %.03f\272 (%s)",
				SDF_UTC.format(calculationDateTime.getTime()),
				moonTilt,
				GeomUtil.decToSex(moonTilt, GeomUtil.SHELL, GeomUtil.NONE).trim(),
				moonTiltV2,
				GeomUtil.decToSex(moonTiltV2, GeomUtil.SHELL, GeomUtil.NONE).trim()));
	}

}
