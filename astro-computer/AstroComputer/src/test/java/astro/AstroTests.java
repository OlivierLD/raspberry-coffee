package astro;

//import calc.calculation.AstroComputer;

import calc.calculation.AstroComputer;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;
import calc.calculation.nauticalalmanac.Context;
import calc.calculation.nauticalalmanac.Core;
import calc.calculation.nauticalalmanac.Star;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

public class AstroTests {

	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	static {
		DURATION_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

	private final static DecimalFormat DF22 = new DecimalFormat("#0.00"); // ("##0'�'00'\''");

	@Test
	public void ghaAndLongitude() {
		double longitude = -122;
		double gha = AstroComputerV2.longitudeToGHA(longitude);
		System.out.printf("Longitude: %f => GHA: %f\n", longitude, gha);
		double backAgain = AstroComputerV2.ghaToLongitude(gha);
		assertEquals(String.format("Ooops: was %f, instead of %f", backAgain, longitude), longitude, backAgain);
	}

	public void sightReduction(String utcDate,
							   String bodyName,
							   double userLatitude,
							   double userLongitude,
							   boolean reverse,
							   double instrumentalAltitude,
							   int limb,
							   double eyeHeight) {
		try {
			Date from = DURATION_FMT.parse(utcDate);
			Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
			current.setTime(from);
			System.out.printf("Starting Sight Reduction calculation at %s (%s)\n", current.getTime(), utcDate);

			AstroComputerV2 acv2 = new AstroComputerV2();
			acv2.calculate(
					current.get(Calendar.YEAR),
					current.get(Calendar.MONTH) + 1,
					current.get(Calendar.DAY_OF_MONTH),
					current.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
					current.get(Calendar.MINUTE),
					current.get(Calendar.SECOND));

			double gha = 0, decl = 0;
			double hp = 0, sd = 0;

			double hDip = 0;
			double refr = 0;
			double parallax = 0;
			double obsAlt = 0;
			double totalCorrection = 0d;

			double lunar = -1;

			// Depends on the body
			switch (bodyName) {
				case "Sun":
					gha = acv2.getSunGHA();
					decl = acv2.getSunDecl();
					hp = acv2.getSunHp() /* Context.HPsun */ / 3_600d;
					sd = acv2.getSunSd() /* Context.SDsun */ / 3_600d;
					lunar = acv2.getLDist(); // Context.LDist;
					break;
				case "Moon":
					gha = acv2.getMoonGHA();
					decl = acv2.getMoonDecl();
					hp = acv2.getMoonHp() /* Context.HPmoon */ / 3_600d;
					sd = acv2.getMoonSd() /* Context.SDmoon */ / 3_600d;
					break;
				case "Venus":
					gha = acv2.getVenusGHA();
					decl = acv2.getVenusDecl();
					hp = acv2.getVenusHp() /* Context.HPvenus */ / 3_600d;
					sd = acv2.getVenusSd() /* Context.SDvenus */ / 3_600d;
					lunar = acv2.getVenusMoonDist(); // Context.moonVenusDist;
					break;
				case "Mars":
					gha = acv2.getMarsGHA();
					decl = acv2.getMarsDecl();
					hp = acv2.getMarsHp() /* Context.HPmars */ / 3_600d;
					sd = acv2.getMarsSd() /* Context.SDmars */ / 3_600d;
					lunar = acv2.getMarsMoonDist(); // Context.moonMarsDist;
					break;
				case "Jupiter":
					gha = acv2.getJupiterGHA();
					decl = acv2.getJupiterDecl();
					hp = acv2.getJupiterHp() /* Context.HPjupiter */ / 3_600d;
					sd = acv2.getJupiterSd() /* Context.SDjupiter */ / 3_600d;
					lunar = acv2.getJupiterMoonDist(); // Context.moonJupiterDist;
					break;
				case "Saturn":
					gha = acv2.getSaturnGHA();
					decl = acv2.getSaturnDecl();
					hp = acv2.getSaturnHp() /* Context.HPsaturn */ / 3_600d;
					sd = acv2.getSaturnSd() /* Context.SDsaturn */ / 3_600d;
					lunar = acv2.getSaturnMoonDist(); // Context.moonSaturnDist;
					break;
				default: // Stars
//					Core.starPos(bodyName);
//					gha = Context.GHAstar;
//					decl = Context.DECstar;
//					lunar = Context.starMoonDist;
					hp = 0d;
					sd = 0d;
					// New version.
					acv2.starPos(bodyName); //, acv2.context);
					gha = acv2.getStarGHA(bodyName);
					decl = acv2.getStarDec(bodyName);
					lunar = acv2.getStarMoonDist(bodyName);

					System.out.println("Break here!");

					break;
			}

			Map<String, Double> reduced = new HashMap<>();
			if (lunar != -1) { // i.e. not shooting the moon
				reduced.put("lunar-distance", lunar);
			}
			reduced.put("gha", gha);
			reduced.put("decl", decl);
			reduced.put("sd", sd);
			reduced.put("hp", hp);

			SightReductionUtil sru = new SightReductionUtil();

			sru.setL(userLatitude);
			sru.setG(userLongitude);

			sru.setAHG(gha);
			sru.setD(decl);
			sru.calculate();

			if (!reverse) {
				obsAlt = SightReductionUtil.observedAltitude(instrumentalAltitude,
						eyeHeight,
						hp,    // Returned in seconds, sent in degrees
						sd,    // Returned in seconds, sent in degrees
						limb,
						"true".equals(System.getProperty("astro.verbose", "false")));

				hDip = sru.getHorizonDip();
				refr = sru.getRefraction();
				parallax = sru.getPa();

				totalCorrection = 0d;
				totalCorrection -= (hDip / 60D);
				totalCorrection -= (refr / 60D);
				totalCorrection += (parallax);
				if (limb == SightReductionUtil.UPPER_LIMB) {
					sd = -sd;
				} else if (limb == SightReductionUtil.NO_LIMB) {
					sd = 0;
				}
				totalCorrection += sd;

				sru.calculate(userLatitude, userLongitude, gha, decl);

				double estimatedAltitude = sru.getHe();
				double z = sru.getZ();

				double intercept = obsAlt - estimatedAltitude;

				reduced.put("observed-altitude-degrees", obsAlt);
				reduced.put("estimated-altitude-degrees", estimatedAltitude);
				reduced.put("z", z);

				reduced.put("horizon-depression-minutes", hDip); // In minutes of arc
				reduced.put("total-correction-minutes", totalCorrection * 60); // In minutes of arc
				reduced.put("horizontal-parallax-minutes", hp * 60d); // In minutes of arc
				reduced.put("parallax-minutes", parallax * 60d); // In minutes of arc
				reduced.put("semi-diameter-minutes", sd * 60); // In minutes of arc
				reduced.put("refraction-minutes", refr); // In minutes of arc
				reduced.put("intercept-degrees", intercept); // In degrees
				reduced.put("delta-t", acv2.getDeltaT()); // In seconds

				if ("true".equals(System.getProperty("astro.verbose", "false"))) {
					System.out.println("For eye height " + DF22.format(eyeHeight) + " m, horizon dip = " + DF22.format(hDip) + "'");
					System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
					System.out.println("Refraction " + DF22.format(refr) + "'");
					System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
					System.out.println("For hp " + DF22.format(hp * 60d) + "', parallax " + DF22.format(parallax * 60d) + "'");
					System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
					System.out.println("Semi-diameter: " + DF22.format(sd * 60d) + "'");
					System.out.println("Intercept:" + DF22.format(Math.abs(intercept) * 60d) + "' " + (intercept < 0 ? "away from" : "towards") + " " + bodyName);
				}
			} else { // Reverse sight
				obsAlt = sru.getHe();
				hDip = SightReductionUtil.getHorizonDip(eyeHeight);
				// sd, we have already
				parallax = SightReductionUtil.getParallax(hp, obsAlt);
				refr = SightReductionUtil.getRefraction(obsAlt - parallax);
				double hi = obsAlt;
				if (limb == SightReductionUtil.UPPER_LIMB) {
					sd = -sd;
				} else if (limb == SightReductionUtil.NO_LIMB) {
					sd = 0;
				}
				totalCorrection = 0d;
				totalCorrection -= (hDip / 60D);
				totalCorrection -= (refr / 60D);
				totalCorrection += (parallax);
				totalCorrection += sd;
				hi -= sd;
				hi += (hDip / 60d);
				hi -= parallax;
				hi += (refr / 60d);

				if ("true".equals(System.getProperty("astro.verbose", "false"))) {
					System.out.println("For eye height " + DF22.format(eyeHeight) + " m, horizon dip = " + DF22.format(hDip) + "'");
					System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
					System.out.println("Refraction " + DF22.format(refr) + "'");
					System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
					System.out.println("For hp " + DF22.format(hp * 60d) + "', parallax " + DF22.format(parallax * 60d) + "'");
					System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
					System.out.println("Semi-diameter: " + DF22.format(sd * 60d) + "'");
					System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
					System.out.println("Hi " + hi);
				}

				reduced.put("instrumental-altitude", hi);
				reduced.put("observed-altitude-degrees", obsAlt); // To observe... (hi, with corrections)
				reduced.put("horizon-depression-minutes", hDip); // In minutes of arc
				reduced.put("total-correction-minutes", totalCorrection * 60); // In minutes of arc
				reduced.put("horizontal-parallax-minutes", hp * 60d); // In minutes of arc
				reduced.put("parallax-minutes", parallax * 60d); // In minutes of arc
				reduced.put("semi-diameter-minutes", sd * 60); // In minutes of arc
				reduced.put("refraction-minutes", refr); // In minutes of arc
				reduced.put("delta-t", acv2.getDeltaT()); // In seconds (of time)
			}

			// TODO Add ObjectMapper ?
//			String content = new Gson().toJson(reduced);
		} catch (ParseException pe) {
			fail(pe.getMessage());
		}
	}

	@Test
	public void testSRSun() {
		String utcDate = "2022-09-11T07:08:08";
		String bodyName = "Sun";
		double userLatitude = 47.677667;
		double userLongitude = -3.135667;
		boolean reverse = false;
		double instrumentalAltitude = 13.34188333;
		int limb = SightReductionUtil.LOWER_LIMB;
		double eyeHeight = 1.8;

		System.setProperty("astro.verbose", "true");

		sightReduction(utcDate, bodyName, userLatitude, userLongitude, reverse, instrumentalAltitude, limb, eyeHeight);
	}

	@Test
	public void testSRStar() {
		String utcDate = "2022-09-11T07:08:08";
		String bodyName = "Aldebaran";
		double userLatitude = 47.677667;
		double userLongitude = -3.135667;
		boolean reverse = false;
		double instrumentalAltitude = 13.34188333;
		int limb = SightReductionUtil.NO_LIMB;
		double eyeHeight = 1.8;

		System.setProperty("astro.verbose", "true");

		sightReduction(utcDate, bodyName, userLatitude, userLongitude, reverse, instrumentalAltitude, limb, eyeHeight);
	}

	@Test
	public void testNoCalculateInvoked() {
		Date from = new Date(); // now
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		current.setTime(from);
		System.out.printf("Starting calculation at %s (%s)\n", current.getTime(), DURATION_FMT.format(from));

		try {
			AstroComputerV2 acv2 = new AstroComputerV2();
			if (false) {
				acv2.calculate(
						current.get(Calendar.YEAR),
						current.get(Calendar.MONTH) + 1,
						current.get(Calendar.DAY_OF_MONTH),
						current.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
						current.get(Calendar.MINUTE),
						current.get(Calendar.SECOND));
			}
			final double ariesGHA = acv2.getAriesGHA();
			fail("Should have failed!");
		} catch (Exception ex) {
			assertTrue("Unexpected Exception type", ex instanceof RuntimeException && ex.getMessage().startsWith("Calculation was never invoked in this context"));
		}
	}

	@Test
	public void testGoodStar() {
		Date from = new Date(); // now
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		current.setTime(from);
		System.out.printf("Starting calculation at %s (%s)\n", current.getTime(), DURATION_FMT.format(from));

		try {
			AstroComputerV2 acv2 = new AstroComputerV2();
			if (true) {
				acv2.calculate(
						current.get(Calendar.YEAR),
						current.get(Calendar.MONTH) + 1,
						current.get(Calendar.DAY_OF_MONTH),
						current.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
						current.get(Calendar.MINUTE),
						current.get(Calendar.SECOND));
			}
			final String ALDEBARAN = "Aldebaran";
			final Star star = Star.getStar(ALDEBARAN);
			assertTrue(String.format("%s not found in Star Catalog", ALDEBARAN), star != null);

			acv2.starPos(ALDEBARAN);
			final double starGHA = acv2.getStarGHA(ALDEBARAN);

			assertTrue("Oops!", starGHA > 0);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testTwoStars() {
		Date from = new Date(); // now
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		current.setTime(from);
		System.out.printf("Starting Sight Reduction calculation at %s (%s)\n", current.getTime(), DURATION_FMT.format(from));

		try {
			AstroComputerV2 acv2 = new AstroComputerV2();
			if (true) {
				acv2.calculate(
						current.get(Calendar.YEAR),
						current.get(Calendar.MONTH) + 1,
						current.get(Calendar.DAY_OF_MONTH),
						current.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
						current.get(Calendar.MINUTE),
						current.get(Calendar.SECOND));
			}
			final String ALDEBARAN = "Aldebaran";
			final Star star = Star.getStar(ALDEBARAN);
			assertTrue(String.format("%s not found in Star Catalog", ALDEBARAN), star != null);

			acv2.starPos(ALDEBARAN);
			final double starGHA = acv2.getStarGHA(ALDEBARAN);
			final double starDEC = acv2.getStarDec("Capella");

			fail("Should have failed here!");
		} catch (Exception ex) {
			assertTrue(ex instanceof RuntimeException && ex.getMessage().startsWith("starPos was not invoked for Capella (Aldebaran)"));
		}
	}

	@Test
	public void compareV1V2() {
		Date from = new Date(); // now
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		current.setTime(from);
		System.out.printf("Starting calculation at %s (%s)\n", current.getTime(), DURATION_FMT.format(from));

		try {
			AstroComputerV2 acv2 = new AstroComputerV2();
			acv2.calculate(
					current.get(Calendar.YEAR),
					current.get(Calendar.MONTH) + 1,
					current.get(Calendar.DAY_OF_MONTH),
					current.get(Calendar.HOUR_OF_DAY),
					current.get(Calendar.MINUTE),
					current.get(Calendar.SECOND));

			AstroComputer.calculate(current.get(Calendar.YEAR),
					current.get(Calendar.MONTH) + 1,
					current.get(Calendar.DAY_OF_MONTH),
					current.get(Calendar.HOUR_OF_DAY),
					current.get(Calendar.MINUTE),
					current.get(Calendar.SECOND));

			// Now, compare.
			// Sun & Aries
			assertTrue("Sun GHA", acv2.getSunGHA() == AstroComputer.getSunGHA());
			assertTrue("Aries GHA", acv2.getAriesGHA() == AstroComputer.getAriesGHA());
			// Star
			final String ALDEBARAN = "Aldebaran";
			Core.starPos(ALDEBARAN);
			// New version.
			acv2.starPos(ALDEBARAN);

			assertTrue("Star GHA", acv2.getStarGHA(ALDEBARAN) == Context.GHAstar);
			assertTrue("Star Decl", acv2.getStarDec(ALDEBARAN) == Context.DECstar);
			assertTrue("Star Moon-Dist", acv2.getStarMoonDist(ALDEBARAN) == Context.starMoonDist);

			// TODO More !

		} catch (Exception ex) {

		}
	}
}