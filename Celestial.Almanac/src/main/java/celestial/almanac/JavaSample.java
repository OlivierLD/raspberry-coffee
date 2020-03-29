package celestial.almanac;

import calc.GeomUtil;
import calc.calculation.AstroComputer;
import calc.calculation.SightReductionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class JavaSample {

	private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
	static {
		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	public static void main(String... args) {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
		AstroComputer.calculate(
				date.get(Calendar.YEAR),
				date.get(Calendar.MONTH) + 1,
				date.get(Calendar.DAY_OF_MONTH),
				date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
				date.get(Calendar.MINUTE),
				date.get(Calendar.SECOND));

		System.out.println(String.format("Calculations done for %s", SDF_UTC.format(date.getTime())));

		System.out.println(String.format("Sun data:\tDecl.: %s, GHA: %s",
				GeomUtil.decToSex(AstroComputer.getSunDecl(), GeomUtil.SWING, GeomUtil.NS),
				GeomUtil.decToSex(AstroComputer.getSunGHA(), GeomUtil.SWING, GeomUtil.NONE)));
		System.out.println(String.format("Moon data:\tDecl.: %s, GHA: %s",
				GeomUtil.decToSex(AstroComputer.getMoonDecl(), GeomUtil.SWING, GeomUtil.NS),
				GeomUtil.decToSex(AstroComputer.getMoonGHA(), GeomUtil.SWING, GeomUtil.NONE)));
		System.out.println(String.format("Venus data:\tDecl.: %s, GHA: %s",
				GeomUtil.decToSex(AstroComputer.getVenusDecl(), GeomUtil.SWING, GeomUtil.NS),
				GeomUtil.decToSex(AstroComputer.getVenusGHA(), GeomUtil.SWING, GeomUtil.NONE)));
		System.out.println(String.format("Mars data:\tDecl.: %s, GHA: %s",
				GeomUtil.decToSex(AstroComputer.getMarsDecl(), GeomUtil.SWING, GeomUtil.NS),
				GeomUtil.decToSex(AstroComputer.getMarsGHA(), GeomUtil.SWING, GeomUtil.NONE)));
		System.out.println(String.format("Jupiter data:\tDecl.: %s, GHA: %s",
				GeomUtil.decToSex(AstroComputer.getJupiterDecl(), GeomUtil.SWING, GeomUtil.NS),
				GeomUtil.decToSex(AstroComputer.getJupiterGHA(), GeomUtil.SWING, GeomUtil.NONE)));
		System.out.println(String.format("Saturn data:\tDecl.: %s, GHA: %s",
				GeomUtil.decToSex(AstroComputer.getSaturnDecl(), GeomUtil.SWING, GeomUtil.NS),
				GeomUtil.decToSex(AstroComputer.getSaturnGHA(), GeomUtil.SWING, GeomUtil.NONE)));
	}
}
