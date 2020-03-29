package celestial.almanac;

import calc.GeomUtil;
import calc.calculation.AstroComputer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import static utils.StringUtils.lpad;

public class JavaSample {

	private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
	static {
		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	/**
	 *
	 * @param value in seconds of arc
	 * @return
	 */
	private static String renderSdHp(double value) {
		String formatted = "";
		int minutes = (int)Math.floor(value / 60d);
		double seconds = value - (minutes * 60);
		if (minutes > 0) {
			formatted = String.format("%d' %05.02f\"", minutes, seconds);
		} else {
			formatted = String.format("%05.02f\"", seconds);
		}
		return formatted;
	}

	private static String renderRA(double ra) {
		String formatted = "";
		double t = ra / 15d;
		int raH = (int)Math.floor(t);
		int raMin = (int)Math.floor(60 * (t - raH));
		float raSec = (float)Math.round(10d * (3_600d * ((t - raH) - (raMin / 60d)))) / 10;
		formatted = String.format("%02dh %02dm %05.02fs", raH, raMin, raSec);
		return formatted;
	}

	public static void main(String... args) {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
		// All calculations here
		AstroComputer.calculate(
				date.get(Calendar.YEAR),
				date.get(Calendar.MONTH) + 1,
				date.get(Calendar.DAY_OF_MONTH),
				date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
				date.get(Calendar.MINUTE),
				date.get(Calendar.SECOND));

		// Done with calculations, now display
		System.out.println(String.format("Calculations done for %s", SDF_UTC.format(date.getTime())));

		System.out.println(String.format("Sun data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getSunDecl(), GeomUtil.SWING, GeomUtil.NS), 10, "0"),
				GeomUtil.decToSex(AstroComputer.getSunGHA(), GeomUtil.SWING, GeomUtil.NONE),
				renderRA(AstroComputer.getSunRA()),
				lpad(renderSdHp(AstroComputer.getSunSd()), 10, " "),
				lpad(renderSdHp(AstroComputer.getSunHp()), 10, " ")));
		System.out.println(String.format("Moon data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getMoonDecl(), GeomUtil.SWING, GeomUtil.NS), 10, "0"),
				GeomUtil.decToSex(AstroComputer.getMoonGHA(), GeomUtil.SWING, GeomUtil.NONE),
				renderRA(AstroComputer.getMoonRA()),
				lpad(renderSdHp(AstroComputer.getMoonSd()), 10, " "),
						lpad(renderSdHp(AstroComputer.getMoonHp()), 10, " ")));
		System.out.println(String.format("Venus data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getVenusDecl(), GeomUtil.SWING, GeomUtil.NS), 10, "0"),
				GeomUtil.decToSex(AstroComputer.getVenusGHA(), GeomUtil.SWING, GeomUtil.NONE),
				renderRA(AstroComputer.getVenusRA()),
				lpad(renderSdHp(AstroComputer.getVenusSd()), 10, " "),
				lpad(renderSdHp(AstroComputer.getVenusHp()), 10, " ")));
		System.out.println(String.format("Mars data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getMarsDecl(), GeomUtil.SWING, GeomUtil.NS), 10, "0"),
				GeomUtil.decToSex(AstroComputer.getMarsGHA(), GeomUtil.SWING, GeomUtil.NONE),
				renderRA(AstroComputer.getMarsRA()),
				lpad(renderSdHp(AstroComputer.getMarsSd()), 10, " "),
				lpad(renderSdHp(AstroComputer.getMarsHp()), 10, " ")));
		System.out.println(String.format("Jupiter data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getJupiterDecl(), GeomUtil.SWING, GeomUtil.NS), 10, "0"),
				GeomUtil.decToSex(AstroComputer.getJupiterGHA(), GeomUtil.SWING, GeomUtil.NONE),
				renderRA(AstroComputer.getJupiterRA()),
				lpad(renderSdHp(AstroComputer.getJupiterSd()), 10, " "),
				lpad(renderSdHp(AstroComputer.getJupiterHp()), 10, " ")));
		System.out.println(String.format("Saturn data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getSaturnDecl(), GeomUtil.SWING, GeomUtil.NS), 10, "0"),
				GeomUtil.decToSex(AstroComputer.getSaturnGHA(), GeomUtil.SWING, GeomUtil.NONE),
				renderRA(AstroComputer.getSaturnRA()),
				lpad(renderSdHp(AstroComputer.getSaturnSd()), 10, " "),
				lpad(renderSdHp(AstroComputer.getSaturnHp()), 10, " ")));
	}
}
