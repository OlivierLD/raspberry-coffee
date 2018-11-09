package gribprocessing.utils;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


import java.util.TimeZone;

import calc.GeoPoint;
import jgrib.GribFile;


public class BlindRouting implements RoutingClientInterface
{
	private RoutingPoint closestPoint = null;
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

	public static void main(String[] args) throws Exception
	{
		BlindRouting br = new BlindRouting();
		br.calculate(args);
	}

	private void calculate(String[] args) throws Exception
	{
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

		for (int i=0; i<args.length; i++)
		{
			if ("-fromG".equals(args[i]))
				fromG = Double.parseDouble(args[i+1]);
			if ("-fromL".equals(args[i]))
				fromL = Double.parseDouble(args[i+1]);
			if ("-toG".equals(args[i]))
				toG = Double.parseDouble(args[i+1]);
			if ("-toL".equals(args[i]))
				toL = Double.parseDouble(args[i+1]);
			if ("-startTime".equals(args[i]))
				startTime = args[i+1];
			if ("-grib".equals(args[i]))
				gribName = args[i+1];
			if ("-polars".equals(args[i]))
				polarFile = args[i+1];
			if ("-output".equals(args[i]))
				outputFile = args[i+1];
			if ("-speedCoeff".equals(args[i]))
				speedCoeff = Double.parseDouble(args[i+1]);
			if ("-timeInterval".equals(args[i]))
				timeInterval = Double.parseDouble(args[i+1]);
			if ("-routingForkWidth".equals(args[i]))
				routingForkWidth = Integer.parseInt(args[i+1]);
			if ("-routingStep".equals(args[i]))
				routingStep = Integer.parseInt(args[i+1]);
			if ("-limitTWS".equals(args[i]))
				limitTWS = Integer.parseInt(args[i+1]);
			if ("-limitTWA".equals(args[i]))
				limitTWA = Integer.parseInt(args[i+1]);
			if ("-verbose".equals(args[i]))
				verbose = (args[i+1].trim().toUpperCase().equals("Y") ||
						args[i+1].trim().toUpperCase().equals("TRUE") ||
						args[i+1].trim().toUpperCase().equals("YES"));
			if ("-help".equals(args[i]))
			{
				System.out.println("Usage:");
				System.out.println("java " + this.getClass().getName() + " -option value -option value ...");
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
				System.out.println("Example: java " + this.getClass().getName() +
						" -fromL 37.122 -fromG -122.5 -toL -9.75 -toG -139.10 -startTime \"2012-03-10T12:00:00\" -grib \"." +
						File.separator + "GRIBFiles" + File.separator + "2012" + File.separator + "03" + File.separator +
						"GRIB_2012_03_01_08_22_55_PST.grb\" -polars \"." + File.separator + "polars" + File.separator +
						"cheoy-lee-42-polar-coeff.xml\" -output \"my.routing.gpx\" -speedCoeff 0.75");

				System.exit(0);
			}
		}
		GeoPoint from = new GeoPoint(fromL, fromG);
		GeoPoint to   = new GeoPoint(toL,   toG);
		List<GeoPoint> intermediateRoutingWP = null;
		List<List<RoutingPoint>> allCalculatedIsochrons = new ArrayList<List<RoutingPoint>>();

		GribFile gf = null;
		try
		{
			File f = new File(gribName);
			if (!f.exists())
			{
				System.out.println("GRIB file [" + gribName + "] not found, exiting.");
				System.exit(1);
			}
			FileInputStream fis = new FileInputStream(f);
			gf = new GribFile(fis);
		}
		catch (Exception ex)
		{
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
		GeoPoint isoTo   = to;
		if (verbose)
			System.out.println("Routing from " + isoFrom.toString() + "\nto " + isoTo.toString());
		int i = 0;
		Point point = new Point((int)Math.round(isoFrom.getG() * 1000), (int)Math.round(isoFrom.getL() * 1000)); //chartPanel.getPanelPoint(isoFrom);
		RoutingPoint center = new RoutingPoint(point);
		center.setPosition(from);
		point = new Point((int)Math.round(to.getG() * 1000), (int)Math.round(to.getL() * 1000)); // chartPanel.getPanelPoint(to);
		RoutingPoint destination = new RoutingPoint(point);
		destination.setPosition(to);

		List<RoutingPoint> interWP = null;
		if (intermediateRoutingWP != null && intermediateRoutingWP.size() > 0)
		{
			interWP = new ArrayList<RoutingPoint>(intermediateRoutingWP.size());
			for (GeoPoint gp : intermediateRoutingWP)
			{
				RoutingPoint rp = new RoutingPoint(new Point((int)Math.round(gp.getG() * 1000), (int)Math.round(gp.getL() * 1000)));
				rp.setPosition(gp);
				interWP.add(rp);
			}
		}
		ParamPanel.setUserValues();
		ParamPanel.data[ParamData.POLAR_FILE_LOC][ParamData.VALUE_INDEX] = new ParamPanel.DataFile(new String[] {"xml"}, WWGnlUtilities.buildMessage("polars"), polarFile);
		String fName = ((ParamPanel.DataFile) ParamPanel.data[ParamData.POLAR_FILE_LOC][ParamData.VALUE_INDEX]).toString();
		if (verbose)
		{
			System.out.println("Using polar file " + fName);
			System.out.println("Starting " + startDate.toString());
		}
		WWContext.getInstance().addApplicationListener(new ApplicationEventListener()
		{
			public String toString()
			{
				return "from blindRouting.";
			}
			public void log(String str)
			{
				if (verbose)
					System.out.print("Routing-> " + str + (str.endsWith("\n")?"":"\n"));
			}
			public void log(String str, int i)
			{
				if (verbose)
					System.out.print("Routing-> " + str + (str.endsWith("\n")?"":"\n"));
			}
		});

		allCalculatedIsochrons = RoutingUtil.calculateIsochrons(this,
				null, // chartPanel,
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

		int clipboardOption = -1;
		String fileOutput = outputFile;
		String clipboardContent = "";
		if (fileOutput.toUpperCase().trim().endsWith(".CSV"))
			clipboardOption = ParamPanel.RoutingOutputList.CSV;
		else if (fileOutput.toUpperCase().trim().endsWith(".GPX"))
			clipboardOption = ParamPanel.RoutingOutputList.GPX;
		else if (fileOutput.toUpperCase().trim().endsWith(".TXT"))
			clipboardOption = ParamPanel.RoutingOutputList.TXT;
		else
		{
			String ext = "";
			if (fileOutput.indexOf(".") > -1)
				ext = fileOutput.substring(fileOutput.lastIndexOf(".") + 1);
			System.out.println("Supported output extensions are csv, gpx, or txt");
			System.out.println("Unknown output file type [" + ext + "], setting to GPX");
			clipboardOption = ParamPanel.RoutingOutputList.GPX;
		}

		i = allCalculatedIsochrons.size();

		// Reverse, for the output
		boolean generateGPXRoute = true;

		if (clipboardOption == ParamPanel.RoutingOutputList.CSV)
			clipboardContent = "L;(dec L);G;(dec G);Date;UTC;TWS;TWD;BSP;HDG\n";
		else if (clipboardOption == ParamPanel.RoutingOutputList.GPX)
		{
			clipboardContent =
					"<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
							"<gpx version=\"1.1\" \n" +
							"     creator=\"OpenCPN\" \n" +
							"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
							"  xmlns=\"http://www.topografix.com/GPX/1/1\" \n" +
							"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" \n" +
							"  xmlns:opencpn=\"http://www.opencpn.org\">\n";
			if (generateGPXRoute)
			{
				Date d = new Date();
				clipboardContent += ("  <rte>\n" +
						"    <name>Weather Wizard route (" + WWGnlUtilities.SDF_DMY.format(d) + ")</name>\n" +
						"    <type>Routing</type>\n" +
						"    <desc>Routing from Weather Wizard (generated " + d.toString() + ")</desc>\n" +
						"    <number>" + (d.getTime()) + "</number>\n");
			}
		}
		else if (clipboardOption == ParamPanel.RoutingOutputList.TXT)
		{
			Date d = new Date();
			clipboardContent += ("Weather Wizard route (" + WWGnlUtilities.SDF_DMY.format(d) + ") generated " + d.toString() + ")\n");
		}

		if (closestPoint != null && allCalculatedIsochrons != null)
		{
			Calendar cal = new GregorianCalendar();
			cal.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
			List<RoutingPoint> bestRoute = new ArrayList<RoutingPoint>(allCalculatedIsochrons.size());
			boolean go = true;
			RoutingPoint start = closestPoint;
			bestRoute.add(start);
			while (go)
			{
				RoutingPoint next = start.getAncestor();
				if (next == null)
					go = false;
				else
				{
					bestRoute.add(next);
					start = next;
				}
			}
			int routesize = bestRoute.size();
			String date = "", time = "";
			RoutingPoint rp = null;
			RoutingPoint ic = null; // Isochron Center
			//  for (int r=0; r<routesize; r++) // 0 is the closest point, the last calculated
			for (int r=routesize - 1; r>=0; r--) // 0 is the closest point, the last calculated
			{
				rp = bestRoute.get(r);
				if (r == 0) // Last one
					ic = rp;
				else
					ic = bestRoute.get(r-1);

				if (rp.getDate() == null)
					date = time = "";
				else
				{
					cal.setTime(rp.getDate());

					int year    = cal.get(Calendar.YEAR);
					int month   = cal.get(Calendar.MONTH);
					int day     = cal.get(Calendar.DAY_OF_MONTH);
					int hours   = cal.get(Calendar.HOUR_OF_DAY);
					int minutes = cal.get(Calendar.MINUTE);
					int seconds = cal.get(Calendar.SECOND);
					if (clipboardOption == ParamPanel.RoutingOutputList.CSV)
					{
						date = WWGnlUtilities.DF2.format(month + 1) + "/" + WWGnlUtilities.DF2.format(day) + "/" + Integer.toString(year);
						time = WWGnlUtilities.DF2.format(hours) + ":" + WWGnlUtilities.DF2.format(minutes);
					}
					else if (clipboardOption == ParamPanel.RoutingOutputList.GPX)
					{
						date = Integer.toString(year) + "-" +
								WWGnlUtilities.DF2.format(month + 1) + "-" +
								WWGnlUtilities.DF2.format(day) + "T" +
								WWGnlUtilities.DF2.format(hours) + ":" +
								WWGnlUtilities.DF2.format(minutes) + ":" +
								WWGnlUtilities.DF2.format(seconds) + "Z";
					}
					else if (clipboardOption == ParamPanel.RoutingOutputList.TXT)
					{
						date = rp.getDate().toString();
					}
				}
				if (clipboardOption == ParamPanel.RoutingOutputList.CSV)
				{
					String lat = GeomUtil.decToSex(rp.getPosition().getL(), GeomUtil.SWING, GeomUtil.NS);
					String lng = GeomUtil.decToSex(rp.getPosition().getG(), GeomUtil.SWING, GeomUtil.EW);
					String tws = WWGnlUtilities.XX22.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = WWGnlUtilities.XX22.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());

					clipboardContent += (lat + ";" +
							Double.toString(rp.getPosition().getL()) + ";" +
							lng + ";" +
							Double.toString(rp.getPosition().getG()) + ";" +
							date + ";" +
							time + ";" +
							tws + ";" +
							twd + ";" +
							bsp + ";" +
							hdg + "\n");
				}
				else if (clipboardOption == ParamPanel.RoutingOutputList.GPX)
				{
					if (generateGPXRoute)
					{
						NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
						nf.setMaximumFractionDigits(2);
						clipboardContent +=
								("       <rtept lat=\"" + rp.getPosition().getL() + "\" lon=\"" + rp.getPosition().getG() + "\">\n" +
										"            <name>" + WWGnlUtilities.DF3.format(routesize - r) + "_WW</name>\n" +
										"            <desc>Waypoint " + Integer.toString(routesize - r) + ";VMG=" + nf.format(ic.getBsp()) + ";</desc>\n" +
										//  "            <sym>triangle</sym>\n" +
										"            <sym>empty</sym>\n" +
										"            <type>WPT</type>\n" +
										"            <extensions>\n" +
										"                <opencpn:prop>A,0,1,1,1</opencpn:prop>\n" +
										"                <opencpn:viz>1</opencpn:viz>\n" +
										"                <opencpn:viz_name>0</opencpn:viz_name>\n" +
										"            </extensions>\n" +
										"        </rtept>\n");
					}
					else
					{
						clipboardContent +=
								("  <wpt lat=\"" + rp.getPosition().getL() + "\" lon=\"" + rp.getPosition().getG() + "\">\n" +
										"    <time>" + date + "</time>\n" +
										"    <name>" + WWGnlUtilities.DF3.format(r) + "_WW</name>\n" +
										"    <sym>triangle</sym>\n" +
										"    <type>WPT</type>\n" +
										"    <extensions>\n" +
										"            <opencpn:guid>142646-1706866-1264115693</opencpn:guid>\n" +
										"            <opencpn:viz>1</opencpn:viz>\n" +
										"            <opencpn:viz_name>1</opencpn:viz_name>\n" +
										"            <opencpn:shared>1</opencpn:shared>\n" +
										"    </extensions>\n" +
										"  </wpt>\n");
					}
				}
				else if (clipboardOption == ParamPanel.RoutingOutputList.TXT)
				{
					String tws = WWGnlUtilities.XX22.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = WWGnlUtilities.XX22.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());
					clipboardContent +=
							(rp.getPosition().toString() + " : " + date + ", tws:" + tws + ", twd:" + twd + ", bsp:" + bsp + ", hdg:" + hdg + "\n");
				}
			}
			if (clipboardOption == ParamPanel.RoutingOutputList.GPX)
			{
				if (generateGPXRoute)
					clipboardContent += "  </rte>\n";
				clipboardContent +=
						("</gpx>");
			}

			if (fileOutput != null && fileOutput.trim().length() > 0)
			{
				try
				{
					File f = new File(fileOutput);
					BufferedWriter bw = new BufferedWriter(new FileWriter(f));
					bw.write(clipboardContent + "\n");
					bw.close();
					System.out.println("");
					System.out.println("Generated " + allCalculatedIsochrons.size() + " isochrones.");
					System.out.println("Output file [" + f.getAbsolutePath() + "] is ready.");
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection stringSelection = new StringSelection(clipboardContent);
				clipboard.setContents(stringSelection, null);
				System.out.println("Routing is in the clipboard\n(Ctrl+V in any editor...)");
			}
		}
	}

	public void routingNotification(List<List<RoutingPoint>> all, RoutingPoint closest)
	{
		closestPoint = closest;
	}
}
