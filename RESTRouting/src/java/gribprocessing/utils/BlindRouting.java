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
	private boolean verbose = false;

	/*
	 * Mandatory prms:
	 * ---------------
	 * -fromL     Start latitude, decimal format
	 * -fromG     Start longitude, decimal format
	 * -toL       Arrival latitude, decimal format
	 * -toG       Arrival longitude, decimal format
	 * -startTime Start date, xsd:duration format, like 2012-03-13T12:00:00, UTC time
	 * -grib      [gribFileName]
	 * -polars    [polarFileName]
	 * -output    [outputFileName]. Supported extensions are gpx, csv, txt.
	 *
	 * Optional prms:
	 * --------------
	 * -verbose          default false (set to true, y, or yes)
	 * -timeInterval     default 24 (in hours)
	 * -routingForkWidth default 140 (in degrees)
	 * -routingStep      default 10 (in degrees)
	 * -limitTWS         default -1 (in knots. -1 means no limit)
	 * -limitTWA         default -1 (in degrees. -1 means no limit)
	 * -speedCoeff       default 1.0 used to multiply the speed computed with the polars.
	 */

	public static void main(String[] args) throws Exception {
		double fromL = 0, fromG = 0, toL = 0, toG = 0;
		String startTime = "";
		String gribName = "";
		String polarFile = "";
		String outputFile = "";

		double timeInterval = 24d;
		int routingForkWidth = 140;
		int routingStep = 10;
		int limitTWS = -1;
		int limitTWA = -1;
		double speedCoeff = 1.0;
		boolean verb = false;

		for (int i = 0; i < args.length; i++) {
			if ("-fromG".equals(args[i])) {
				fromG = Double.parseDouble(args[i + 1]);
			}
			if ("-fromL".equals(args[i])) {
				fromL = Double.parseDouble(args[i + 1]);
			}
			if ("-toG".equals(args[i])) {
				toG = Double.parseDouble(args[i + 1]);
			}
			if ("-toL".equals(args[i])) {
				toL = Double.parseDouble(args[i + 1]);
			}
			if ("-startTime".equals(args[i])) {
				startTime = args[i + 1];
			}
			if ("-grib".equals(args[i])) {
				gribName = args[i + 1];
			}
			if ("-polars".equals(args[i])) {
				polarFile = args[i + 1];
			}
			if ("-output".equals(args[i])) {
				outputFile = args[i + 1];
			}
			if ("-speedCoeff".equals(args[i])) {
				speedCoeff = Double.parseDouble(args[i + 1]);
			}
			if ("-timeInterval".equals(args[i])) {
				timeInterval = Double.parseDouble(args[i + 1]);
			}
			if ("-routingForkWidth".equals(args[i])) {
				routingForkWidth = Integer.parseInt(args[i + 1]);
			}
			if ("-routingStep".equals(args[i])) {
				routingStep = Integer.parseInt(args[i + 1]);
			}
			if ("-limitTWS".equals(args[i])) {
				limitTWS = Integer.parseInt(args[i + 1]);
			}
			if ("-limitTWA".equals(args[i])) {
				limitTWA = Integer.parseInt(args[i + 1]);
			}
			if ("-verbose".equals(args[i])) {
				verb = (args[i + 1].trim().toUpperCase().equals("Y") ||
						args[i + 1].trim().toUpperCase().equals("TRUE") ||
						args[i + 1].trim().toUpperCase().equals("YES"));
			}
			if ("-help".equals(args[i])) {
				BlindRouting br = new BlindRouting();
				System.out.println("Usage:");
				System.out.println("java " + br.getClass().getName() + " -option value -option value ...");
				System.out.println("Mandatory options:");
				System.out.println("-fromL -fromG -toL -toG -startTime -grib -polars -output");
				System.out.println("Optional:");
				System.out.println("-verbose default false");
				System.out.println("-timeInterval default 24");
				System.out.println("-routingForkWidth default 140");
				System.out.println("-routingStep default 10");
				System.out.println("-limitTWS default -1");
				System.out.println("-limitTWA default -1");
				System.out.println("-speedCoeff default 1.0");
				System.out.println("-----------------------");
				System.out.println("Example: java " + br.getClass().getName() +
						" -fromL 37.122 -fromG -122.5 -toL -9.75 -toG -139.10 -startTime \"2012-03-10T12:00:00\" -grib \"." +
						File.separator + "GRIBFiles" + File.separator + "2012" + File.separator + "03" + File.separator +
						"GRIB_2012_03_01_08_22_55_PST.grb\" -polars \"." + File.separator + "polars" + File.separator +
						"cheoy-lee-42.polar-coeff\" -output \"my.routing.gpx\" -speedCoeff 0.75");

				System.exit(0);
			}
		}


		BlindRouting br = new BlindRouting();
		br.calculate(
				fromL,
				fromG,
				toL,
				toG,
				startTime,
				gribName,
				polarFile,
				outputFile,
				timeInterval,
				routingForkWidth,
				routingStep,
				limitTWS,
				limitTWA,
				speedCoeff,
				verb);
	}

	private void calculate(double fromL, double fromG, double toL, double toG,
	                       String startTime,
	                       String gribName,
	                       String polarFile,
	                       String outputFile,
	                       double timeInterval,
	                       int routingForkWidth,
	                       int routingStep,
	                       int limitTWS,
	                       int limitTWA,
	                       double speedCoeff,
	                       boolean verbose) throws Exception {

		this.verbose = verbose;
		GeoPoint from = new GeoPoint(fromL, fromG);
		GeoPoint to = new GeoPoint(toL, toG);
		List<GeoPoint> intermediateRoutingWP = null;

		GribFile gf = null;
		try {
			File f = new File(gribName);
			if (!f.exists()) {
				System.out.println("GRIB file [" + gribName + "] not found, exiting.");
				System.exit(1);
			}
			FileInputStream fis = new FileInputStream(f);
			gf = new GribFile(fis);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		List<GribHelper.GribConditionData> agcd = GribHelper.dumper(gf, "");
		GribHelper.GribConditionData gribData[] = agcd.toArray(new GribHelper.GribConditionData[agcd.size()]);

		long _time = StringParsers.durationToDate(startTime, "Etc/UTC");
		Calendar startCal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		startCal.setTime(new Date(_time));
		Date startDate = startCal.getTime();

		boolean stopIfTooOld = false;

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
				stopIfTooOld,
				speedCoeff,
				"true".equals(System.getProperty("avoid.land", "false")),
				25.0);

		StringBuffer bestRoute = RoutingUtil.outputRouting(center.getPosition(), destination.getPosition(), routingResult.closest, routingResult.isochronals, RoutingUtil.OutputOption.JSON);
		// TODO Improve that
		System.out.println(bestRoute.toString());
	}
}
