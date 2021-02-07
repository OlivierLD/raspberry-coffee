package celestial.almanac;

import calc.GeomUtil;
import calc.calculation.AstroComputer;
import utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
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
			formatted = String.format("%d'%05.02f\"", minutes, seconds);
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

	/**
	 *
	 * @param eot in minutes (of time)
	 * @return
	 */
	private static String renderEoT(double eot) {
		String formatted = "";
		double dEoT = Math.abs(eot);
		int eotMin = (int)Math.floor(dEoT);
		double eotSec = Math.round(600 * (dEoT - eotMin)) / 10d;
		if (eotMin == 0) {
			formatted = String.format("%s %04.01fs", eot > 0 ? "+" : "-", eotSec);
		} else {
			formatted = String.format("%s %02dm %04.01fs", eot > 0 ? "+" : "-", eotMin, eotSec);
		}
		return formatted;
	}

	public static void main(String... args) {

		boolean now = Arrays.stream(args).filter(arg -> arg.equals("--now")).findFirst().isPresent();

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
		if (!now) { // Hard coded date
			date.set(Calendar.YEAR, 2020);
			date.set(Calendar.MONTH, Calendar.MARCH); // March!
			date.set(Calendar.DAY_OF_MONTH, 28);
			date.set(Calendar.HOUR_OF_DAY, 16); // and not just HOUR !!!!
			date.set(Calendar.MINUTE, 50);
			date.set(Calendar.SECOND, 20);
		}
		System.out.println(String.format("Calculations for %s (%s)", SDF_UTC.format(date.getTime()), now ? "now" : "not now"));

		double defaultDeltaT = AstroComputer.getDeltaT();
		System.out.printf("Using deltaT: %f\n", defaultDeltaT);
		// Recalculate
		double deltaT = TimeUtil.getDeltaT(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1);
		System.out.printf("New deltaT: %f\n", deltaT);
		AstroComputer.setDeltaT(deltaT);

		// All calculations here
		AstroComputer.calculate(
				date.get(Calendar.YEAR),
				date.get(Calendar.MONTH) + 1, // Jan: 1, Dec: 12.
				date.get(Calendar.DAY_OF_MONTH),
				date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
				date.get(Calendar.MINUTE),
				date.get(Calendar.SECOND));

		// Done with calculations, now display
		System.out.println(String.format("Calculations done for %s", SDF_UTC.format(date.getTime())));

		System.out.println(String.format("Sun data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getSunDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(AstroComputer.getSunGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(AstroComputer.getSunRA()),
				lpad(renderSdHp(AstroComputer.getSunSd()), 9, " "),
				lpad(renderSdHp(AstroComputer.getSunHp()), 9, " ")));
		System.out.println(String.format("Moon data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getMoonDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(AstroComputer.getMoonGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(AstroComputer.getMoonRA()),
				lpad(renderSdHp(AstroComputer.getMoonSd()), 9, " "),
						lpad(renderSdHp(AstroComputer.getMoonHp()), 9, " ")));
		System.out.println(String.format("\tMoon phase: %s, %s", GeomUtil.decToSex(AstroComputer.getMoonPhase(), GeomUtil.SWING, GeomUtil.NONE), AstroComputer.getMoonPhaseStr()));
		System.out.println(String.format("Venus data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getVenusDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(AstroComputer.getVenusGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(AstroComputer.getVenusRA()),
				lpad(renderSdHp(AstroComputer.getVenusSd()), 9, " "),
				lpad(renderSdHp(AstroComputer.getVenusHp()), 9, " ")));
		System.out.println(String.format("Mars data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getMarsDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(AstroComputer.getMarsGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(AstroComputer.getMarsRA()),
				lpad(renderSdHp(AstroComputer.getMarsSd()), 9, " "),
				lpad(renderSdHp(AstroComputer.getMarsHp()), 9, " ")));
		System.out.println(String.format("Jupiter data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getJupiterDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(AstroComputer.getJupiterGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(AstroComputer.getJupiterRA()),
				lpad(renderSdHp(AstroComputer.getJupiterSd()), 9, " "),
				lpad(renderSdHp(AstroComputer.getJupiterHp()), 9, " ")));
		System.out.println(String.format("Saturn data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getSaturnDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(AstroComputer.getSaturnGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(AstroComputer.getSaturnRA()),
				lpad(renderSdHp(AstroComputer.getSaturnSd()), 9, " "),
				lpad(renderSdHp(AstroComputer.getSaturnHp()), 9, " ")));
		System.out.println();
		System.out.println(String.format("Polaris data:\tDecl.: %s, GHA: %s, RA: %s",
				lpad(GeomUtil.decToSex(AstroComputer.getPolarisDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(AstroComputer.getPolarisGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(AstroComputer.getPolarisRA())));

		System.out.println(String.format("Equation of time: %s", renderEoT(AstroComputer.getEoT())));
		System.out.println(String.format("Lunar Distance: %s", lpad(GeomUtil.decToSex(AstroComputer.getLDist(), GeomUtil.SWING, GeomUtil.NONE), 10, " ")));
		System.out.println(String.format("Day of Week: %s", AstroComputer.getWeekDay()));
	}
}
