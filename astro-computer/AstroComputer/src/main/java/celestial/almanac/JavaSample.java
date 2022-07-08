package celestial.almanac;

import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
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
		if (eotMin == 0) { // Less than 1 minute
			formatted = String.format("%s %04.01fs", eot > 0 ? "+" : "-", eotSec);
		} else {
			formatted = String.format("%s %02dm %04.01fs", eot > 0 ? "+" : "-", eotMin, eotSec);
		}
		return formatted;
	}

	/**
	 *
	 * @param args use --now to get current data. Otherwise, 2020-Mar-28 16:50:20 UTC will be used.
	 */
	public static void main(String... args) {

		boolean now = Arrays.stream(args).filter(arg -> "--now".equals(arg)).findFirst().isPresent();

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
		if (!now) { // Hard coded date
			date.set(Calendar.YEAR, 2020);
			date.set(Calendar.MONTH, Calendar.MARCH);
			date.set(Calendar.DAY_OF_MONTH, 28);
			date.set(Calendar.HOUR_OF_DAY, 16); // and not just Calendar.HOUR !!!!
			date.set(Calendar.MINUTE, 50);
			date.set(Calendar.SECOND, 20);
		}
		System.out.println(String.format("Calculations for %s (%s)", SDF_UTC.format(date.getTime()), now ? "now" : "not now"));

		AstroComputerV2 acv2 = new AstroComputerV2();
//		double defaultDeltaT = AstroComputer.getDeltaT();
//		System.out.printf("Using deltaT: %f\n", defaultDeltaT);
		for (int i=0; i<10; i++) { // Any variation across the time?
			long before = System.currentTimeMillis();
			// Recalculate DeltaT
			double deltaT = TimeUtil.getDeltaT(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1);
//		System.out.printf(">> deltaT: %f s\n", deltaT);
			acv2.setDeltaT(deltaT);

			// All calculations here
			acv2.calculate(
					date.get(Calendar.YEAR),
					date.get(Calendar.MONTH) + 1, // Jan: 1, Dec: 12.
					date.get(Calendar.DAY_OF_MONTH),
					date.get(Calendar.HOUR_OF_DAY), // and not just Calendar.HOUR !!!!
					date.get(Calendar.MINUTE),
					date.get(Calendar.SECOND));
			long after = System.currentTimeMillis();

			// Done with calculations, now display
			System.out.println(String.format(">>> Calculations done for %s, in %d ms <<<", SDF_UTC.format(date.getTime()), (after - before)));
		}

		System.out.println(String.format("Sun data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(acv2.getSunDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(acv2.getSunGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(acv2.getSunRA()),
				lpad(renderSdHp(acv2.getSunSd()), 9, " "),
				lpad(renderSdHp(acv2.getSunHp()), 9, " ")));
		System.out.println(String.format("Moon data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(acv2.getMoonDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(acv2.getMoonGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(acv2.getMoonRA()),
				lpad(renderSdHp(acv2.getMoonSd()), 9, " "),
						lpad(renderSdHp(acv2.getMoonHp()), 9, " ")));
		System.out.println(String.format("\tMoon phase: %s, %s",
				GeomUtil.decToSex(acv2.getMoonPhase(), GeomUtil.SWING, GeomUtil.NONE),
				acv2.getMoonPhaseStr()));
		System.out.println(String.format("\tMoon illumination %.04f%%", acv2.getMoonIllum()));
		System.out.println(String.format("Venus data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(acv2.getVenusDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(acv2.getVenusGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(acv2.getVenusRA()),
				lpad(renderSdHp(acv2.getVenusSd()), 9, " "),
				lpad(renderSdHp(acv2.getVenusHp()), 9, " ")));
		System.out.println(String.format("Mars data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(acv2.getMarsDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(acv2.getMarsGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(acv2.getMarsRA()),
				lpad(renderSdHp(acv2.getMarsSd()), 9, " "),
				lpad(renderSdHp(acv2.getMarsHp()), 9, " ")));
		System.out.println(String.format("Jupiter data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(acv2.getJupiterDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(acv2.getJupiterGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(acv2.getJupiterRA()),
				lpad(renderSdHp(acv2.getJupiterSd()), 9, " "),
				lpad(renderSdHp(acv2.getJupiterHp()), 9, " ")));
		System.out.println(String.format("Saturn data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
				lpad(GeomUtil.decToSex(acv2.getSaturnDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(acv2.getSaturnGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(acv2.getSaturnRA()),
				lpad(renderSdHp(acv2.getSaturnSd()), 9, " "),
				lpad(renderSdHp(acv2.getSaturnHp()), 9, " ")));
		System.out.println();
		System.out.println(String.format("Polaris data:\tDecl.: %s, GHA: %s, RA: %s",
				lpad(GeomUtil.decToSex(acv2.getPolarisDecl(), GeomUtil.SWING, GeomUtil.NS), 10, " "),
				lpad(GeomUtil.decToSex(acv2.getPolarisGHA(), GeomUtil.SWING, GeomUtil.NONE), 11, " "),
				renderRA(acv2.getPolarisRA())));

		System.out.println(String.format("Equation of time: %s", renderEoT(acv2.getEoT())));
		System.out.println(String.format("Lunar Distance: %s", lpad(GeomUtil.decToSex(acv2.getLDist(), GeomUtil.SWING, GeomUtil.NONE), 10, " ")));
		System.out.println(String.format("Day of Week: %s", acv2.getWeekDay()));

		System.out.println("Done with Java test run!");
	}
}
