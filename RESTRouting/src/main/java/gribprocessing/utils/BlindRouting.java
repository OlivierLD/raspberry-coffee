package gribprocessing.utils;

import calc.GeoPoint;
import jgrib.GribFile;
import nmea.parser.StringParsers;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class BlindRouting {
	private static boolean verbose = "true".equals(System.getProperty("rouring.verbose", "false"));

	/*
	 * Mandatory prms:
	 * ---------------
	 * --from-lat      Start latitude, decimal format
	 * --from-lng      Start longitude, decimal format
	 * --to-lat        Arrival latitude, decimal format
	 * --to-lng        Arrival longitude, decimal format
	 * --start-time    Start date, xsd:duration format, like 2012-03-13T12:00:00, UTC time
	 * --grib-file     [gribFileName]
	 * --polar-file    [polarFileName]
	 * --output-type   [typeName]. JSON, CSV, KML, GPX, TXT
	 *
	 * Optional prms:
	 * --------------
	 * --verbose            default false (set to true, y, or yes)
	 * --time-interval      default 24 (in hours)
	 * --fork-width         default 140 (in degrees)
	 * --routing-angle-step default 10 (in degrees)
	 * --limit-tws          default -1 (in knots. -1 means no limit)
	 * --limit-twa          default -1 (in degrees. -1 means no limit)
	 * --speedC-Coeff       default 1.0 used to multiply the speed computed with the polars.
	 * --avoid-land         default false (set to true, y, or yes)
	 */

	private static final String FROM_L = "--from-lat";
	private static final String FROM_G = "--from-lng";
	private static final String TO_L = "--to-lat";
	private static final String TO_G = "--to-lat";
	private static final String START_TIME = "--start-time";
	private static final String POLAR_FILE = "--polar-file";
	private static final String GRIB_FILE = "--grib-file";
	private static final String OUTPUT_TYPE = "--output-type";
	private static final String TIME_INTERVAL = "--time-interval";
	private static final String FORK_WIDTH = "--fork-width";
	private static final String ROUTING_STEP = "--routing-angle-step";
	private static final String LIMIT_TWS = "--limit-tws";
	private static final String LIMIT_TWA = "--limit-twa";
	private static final String SPEED_COEFF = "--speed-coeff";
	private static final String AVOID_LAND = "--avoid-land";
	private static final String VERBOSE = "--verbose";
	private static final String HELP = "--help";

	public static void main(String... args) throws Exception {
		double fromL = 0, fromG = 0, toL = 0, toG = 0;
		String startTime = "";
		String gribName = "";
		String polarFile = "";
		String outputType = "";
		double timeInterval = 24d;
		int routingForkWidth = 140;
		int routingStep = 10;
		int limitTWS = -1;
		int limitTWA = -1;
		double speedCoeff = 1.0;
		boolean avoidLand = false;
		boolean verb = verbose;

		for (int i = 0; i < args.length; i++) {
			if (FROM_G.equals(args[i])) {
				fromG = Double.parseDouble(args[i + 1]);
			}
			if (FROM_L.equals(args[i])) {
				fromL = Double.parseDouble(args[i + 1]);
			}
			if (TO_G.equals(args[i])) {
				toG = Double.parseDouble(args[i + 1]);
			}
			if (TO_L.equals(args[i])) {
				toL = Double.parseDouble(args[i + 1]);
			}
			if (START_TIME.equals(args[i])) {
				startTime = args[i + 1];
			}
			if (GRIB_FILE.equals(args[i])) {
				gribName = args[i + 1];
			}
			if (POLAR_FILE.equals(args[i])) {
				polarFile = args[i + 1];
			}
			if (OUTPUT_TYPE.equals(args[i])) {
				outputType = args[i + 1];
			}
			if (SPEED_COEFF.equals(args[i])) {
				speedCoeff = Double.parseDouble(args[i + 1]);
			}
			if (TIME_INTERVAL.equals(args[i])) {
				timeInterval = Double.parseDouble(args[i + 1]);
			}
			if (FORK_WIDTH.equals(args[i])) {
				routingForkWidth = Integer.parseInt(args[i + 1]);
			}
			if (ROUTING_STEP.equals(args[i])) {
				routingStep = Integer.parseInt(args[i + 1]);
			}
			if (LIMIT_TWS.equals(args[i])) {
				limitTWS = Integer.parseInt(args[i + 1]);
			}
			if (LIMIT_TWA.equals(args[i])) {
				limitTWA = Integer.parseInt(args[i + 1]);
			}
			if (VERBOSE.equals(args[i])) {
				verb = (args[i + 1].trim().toUpperCase().equals("Y") ||
						args[i + 1].trim().toUpperCase().equals("TRUE") ||
						args[i + 1].trim().toUpperCase().equals("YES"));
			}
			if (AVOID_LAND.equals(args[i])) {
				avoidLand = (args[i + 1].trim().toUpperCase().equals("Y") ||
						args[i + 1].trim().toUpperCase().equals("TRUE") ||
						args[i + 1].trim().toUpperCase().equals("YES"));
			}
			if (HELP.equals(args[i])) {
				BlindRouting br = new BlindRouting();
				System.out.println("Usage:");
				System.out.println("java " + br.getClass().getName() + " --option value --option value ...");
				System.out.println("Mandatory options:");
				System.out.println(String.format("%s %s %s %s %s %s %s %s", FROM_L, FROM_G, TO_L, TO_G, START_TIME, GRIB_FILE, POLAR_FILE, OUTPUT_TYPE));
				System.out.println("Optional:");
				System.out.println(String.format("%s default false", VERBOSE));
				System.out.println(String.format("%s default 24", TIME_INTERVAL));
				System.out.println(String.format("%s default 140", FORK_WIDTH));
				System.out.println(String.format("%s default 10", ROUTING_STEP));
				System.out.println(String.format("%s default -1", LIMIT_TWS));
				System.out.println(String.format("%s default -1", LIMIT_TWA));
				System.out.println(String.format("%s default 1.0", SPEED_COEFF));
				System.out.println(String.format("%s default false", AVOID_LAND));
				System.out.println("-----------------------");
				System.out.println("Example: java " + br.getClass().getName() +
						FROM_L + " 37.122 " + FROM_G + " -122.5 " + TO_L + " -9.75 " + TO_G + " -139.10 " + START_TIME + " \"2012-03-10T12:00:00\" " + GRIB_FILE + " \"." +
						File.separator + "GRIBFiles" + File.separator + "2012" + File.separator + "03" + File.separator +
						"GRIB_2012_03_01_08_22_55_PST.grb\" " + POLAR_FILE + " \"." + File.separator + "polars" + File.separator +
						"cheoy-lee-42.polar-coeff\" " + OUTPUT_TYPE + " \"GPX\" " + SPEED_COEFF + " 0.75");

				System.exit(0);
			}
		}

		BlindRouting br = new BlindRouting();
		RoutingUtil.RoutingResult content = br.calculate(
				fromL,
				fromG,
				toL,
				toG,
				startTime,
				gribName,
				polarFile,
				outputType,
				timeInterval,
				routingForkWidth,
				routingStep,
				limitTWS,
				limitTWA,
				speedCoeff,
				25.0,
				avoidLand,
				verb);
		System.out.println(content.bestRoute);
		System.out.println("Done!");
	}

	public RoutingUtil.RoutingResult calculate(double fromL,
	                                           double fromG,
	                                           double toL,
	                                           double toG,
	                                           String startTime,
	                                           String gribName,
	                                           String polarFile,
	                                           String outputType,
	                                           double timeInterval,
	                                           int routingForkWidth,
	                                           int routingStep,
	                                           int limitTWS,
	                                           int limitTWA,
	                                           double speedCoeff,
	                                           double proximity,
	                                           boolean avoidLand,
	                                           boolean verbose) throws Exception {

		this.verbose = verbose;
		GeoPoint from = new GeoPoint(fromL, fromG);
		GeoPoint to = new GeoPoint(toL, toG);
		List<GeoPoint> intermediateRoutingWP = null;

		GribFile gf = null;
		try {
			File f = new File(gribName);
			if (!f.exists()) {
				throw new RuntimeException(String.format("GRIB file [%s] not found in [%s]", gribName, System.getProperty("user.dir")));
			}
			FileInputStream fis = new FileInputStream(f);
			gf = new GribFile(fis);
		} catch (Exception ex) {
			throw ex;
		}

		if (verbose) {
			System.out.println("-- Starting Routing computation --");
			System.out.println(String.format("From %f/%f to %f/%f, at %s, grib %s, with polars %s, output %s\n" +
							"every %f hours, width %d, limitTWS %s, limitTWA %s, speedCoeff %f, prox: %f, avoid land: %s",
					fromL, fromG,
					toL, toG,
					startTime,
					gribName,
					polarFile,
					outputType,
					timeInterval,
					routingForkWidth,
					(limitTWS == -1 ? "none" : String.valueOf(limitTWS)),
					(limitTWA == -1 ? "none" : String.valueOf(limitTWA)),
					speedCoeff,
					proximity,
					avoidLand ? "yes" : "no"
			));
		}
		List<GribHelper.GribConditionData> agcd = GribHelper.dumper(gf, "");
		GribHelper.GribConditionData gribData[] = agcd.toArray(new GribHelper.GribConditionData[agcd.size()]);

		long _time = StringParsers.durationToDate(startTime, "Etc/UTC");
		Calendar startCal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		startCal.setTime(new Date(_time));
		Date startDate = startCal.getTime();

		boolean stopIfTooOld = false; // TODO A Parameter

		GeoPoint isoFrom = from;
		GeoPoint isoTo = to;
		if (verbose)
			System.out.println("Routing from " + isoFrom.toString() + "\nto " + isoTo.toString());
		int i = 0;
		Point point = new Point((int) Math.round(isoFrom.getG() * 1_000), (int) Math.round(isoFrom.getL() * 1_000)); //chartPanel.getPanelPoint(isoFrom);
		RoutingPoint center = new RoutingPoint(point);
		center.setPosition(from);
		point = new Point((int) Math.round(to.getG() * 1_000), (int) Math.round(to.getL() * 1_000)); // chartPanel.getPanelPoint(to);
		RoutingPoint destination = new RoutingPoint(point);
		destination.setPosition(to);

		List<RoutingPoint> interWP = null;
		if (intermediateRoutingWP != null && intermediateRoutingWP.size() > 0) {
			interWP = new ArrayList<>(intermediateRoutingWP.size());
			for (GeoPoint gp : intermediateRoutingWP) {
				RoutingPoint rp = new RoutingPoint(new Point((int) Math.round(gp.getG() * 1_000), (int) Math.round(gp.getL() * 1_000)));
				rp.setPosition(gp);
				interWP.add(rp);
			}
		}

		String fName = polarFile;
		if (verbose) {
			System.out.println("Using polar file " + fName);
			System.out.println("Starting " + startDate.toString());
		}
		RoutingUtil.RoutingResult routingResult = RoutingUtil.calculateIsochrons(
				fName,
				center,
				destination,
				interWP,
				startDate,
				gribData,
				timeInterval,
				routingForkWidth,
				routingStep,
				limitTWS,
				limitTWA,
				stopIfTooOld, // hard-coded to false
				speedCoeff,
				avoidLand,
				proximity,
				verbose);

		RoutingUtil.OutputOption outputFmt = RoutingUtil.OutputOption.JSON;
		for (RoutingUtil.OutputOption fmt : RoutingUtil.OutputOption.values()) {
			if (fmt.name().equals(outputType)) {
				outputFmt = fmt;
				break;
			}
		}

		StringBuffer bestRoute = RoutingUtil.outputRouting(center.getPosition(), destination.getPosition(), routingResult.closest, routingResult.isochronals, outputFmt);
		routingResult.bestRoute(bestRoute.toString());
		return routingResult;
	}
}
