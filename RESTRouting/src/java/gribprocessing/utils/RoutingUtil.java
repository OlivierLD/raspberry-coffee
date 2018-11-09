package gribprocessing.utils;

import calc.GeoPoint;
import calc.GreatCircle;
import calc.GreatCirclePoint;

import javax.swing.JOptionPane;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoutingUtil {
	public static final int REAL_ROUTING = 0;
	public static final int WHAT_IF_ROUTING = 1;

	private static RoutingPoint finalDestination = null;
	private static GribHelper.GribConditionData[] wgd = null;
	private static double timeStep = 0D;

	private static GreatCircle gc = new GreatCircle();
	private static RoutingPoint closest = null;
	private static RoutingPoint finalClosest = null;

	private static int brg = 0;

	private static double smallestDist = Double.MAX_VALUE;

	private static boolean interruptRouting = false;

	private static int getBearing(RoutingPoint center) {
		int brg = 0;
		GreatCircle.setStart(new GreatCirclePoint(Math.toRadians(center.getPosition().getL()),
				Math.toRadians(center.getPosition().getG())));
		GreatCircle.setArrival(new GreatCirclePoint(Math.toRadians(finalDestination.getPosition().getL()),
				Math.toRadians(finalDestination.getPosition().getG())));
//  gc.calculateGreatCircle(10);
//  double gcDistance = Math.toDegrees(gc.getDistance() * 60D);
		GreatCircle.calculateRhumLine();
		double rlZ = gc.getRhumbLineRoute();
		brg = (int) Math.round(Math.toDegrees(rlZ));
		return brg;
	}

	private static int getBearingTo(RoutingPoint center, RoutingPoint dest) {
		int brg = 0;
		GreatCircle.setStart(new GreatCirclePoint(Math.toRadians(center.getPosition().getL()),
				Math.toRadians(center.getPosition().getG())));
		GreatCircle.setArrival(new GreatCirclePoint(Math.toRadians(dest.getPosition().getL()),
				Math.toRadians(dest.getPosition().getG())));
		//  gc.calculateGreatCircle(10);
		//  double gcDistance = Math.toDegrees(gc.getDistance() * 60D);
		GreatCircle.calculateRhumLine();
		double rlZ = gc.getRhumbLineRoute();
		brg = (int) Math.round(Math.toDegrees(rlZ));
		return brg;
	}

	//

	public static List<List<RoutingPoint>> calculateIsochrons(RoutingClientInterface caller,
	                                                          RoutingPoint startFrom,
	                                                          RoutingPoint destination,
	                                                          List<RoutingPoint> intermediateWP,
	                                                          Date fromDate,
	                                                          GribHelper.GribConditionData[] gribData,
	                                                          double timeInterval,
	                                                          int routingForkWidth,
	                                                          int routingStep,
	                                                          int maxTWS,
	                                                          int minTWA,
	                                                          boolean stopIfGRIB2old,
	                                                          double speedCoeff,
	                                                          boolean avoidLand,
	                                                          double proximity) {
		smallestDist = Double.MAX_VALUE; // Reset, for the next leg
		return calculateIsochrons(caller,
				startFrom,
				destination,
				intermediateWP,
				null,
				fromDate,
				gribData,
				timeInterval,
				routingForkWidth,
				routingStep,
				maxTWS,
				minTWA,
				stopIfGRIB2old,
				speedCoeff,
				avoidLand,
				proximity);
	}

	private static List<List<RoutingPoint>> calculateIsochrons(RoutingClientInterface caller,
	                                                           RoutingPoint startFrom,
	                                                           RoutingPoint destination,
	                                                           List<RoutingPoint> intermediateWP,
	                                                           List<RoutingPoint> bestRoute,
	                                                           Date fromDate,
	                                                           GribHelper.GribConditionData[] gribData,
	                                                           double timeInterval,
	                                                           int routingForkWidth,
	                                                           int routingStep,
	                                                           int maxTWS,
	                                                           int minTWA,
	                                                           boolean stopIfGRIB2old,
	                                                           double speedCoeff,
	                                                           boolean avoidLand,
	                                                           double proximity) {
		wgd = gribData;
		finalDestination = destination; // By default
		timeStep = timeInterval;
		closest = null;
		finalClosest = null;

		RoutingPoint center = startFrom;

		int nbIntermediateIndex = 0;
		if (intermediateWP != null)
			finalDestination = intermediateWP.get(nbIntermediateIndex++);

		double gcDistance = 0D;
		RoutingPoint aimFor = null;
		int bestRouteIndex = 0;
		if (bestRoute != null && bestRoute.size() > 0) {
			aimFor = bestRoute.get(++bestRouteIndex);
			System.out.println("Aiming for [" + aimFor.getPosition() + "]");
		}

//  System.out.println("Starting routing from " + center.getPosition().toString() + " to " + destination.getPosition().toString());

		// Calcutate bearing to detination (from start)
		if (aimFor == null)
			brg = getBearing(center);
		else
			brg = getBearingTo(center, aimFor);

		List<List<RoutingPoint>> allIsochrons = new ArrayList<List<RoutingPoint>>();

		// Initialization
		interruptRouting = false;
		timer = System.currentTimeMillis();

		smallestDist = Double.MAX_VALUE;
		List<List<RoutingPoint>> data = new ArrayList<List<RoutingPoint>>(1);
		ArrayList<RoutingPoint> one = new ArrayList<RoutingPoint>(1);
		center.setDate(fromDate);
		GribHelper.GribCondition wind = GribHelper.gribLookup(center.getPosition(), wgd, fromDate);
		boolean keepLooping = true;
		boolean interruptedBecauseTooOld = false;
		if (wind != null && wind.comment != null && wind.comment.equals("TOO_OLD")) {
			center.setGribTooOld(true);
//    System.out.println("Stop if GRIB too old:" + stopIfGRIB2old);
			if (stopIfGRIB2old) {
				keepLooping = false;
				interruptedBecauseTooOld = true;
				System.out.println("Routing aborted. GRIB exhausted (preference)."); // TODO Better logging
			}
		}
		one.add(center); // Initialize data with the center. One point only.
		data.add(one);

		Date currentDate = fromDate; // new Date(fromDate.getTime() + (long)(timeStep * 3600D * 1000D));
		Date arrivalDate = null;
//  synchronized (allIsochrons)
		{
			// Start from "center"
			while (keepLooping && !interruptRouting) {
//      timer = logDiffTime(timer, "Milestone 1");
				double localSmallOne = Double.MAX_VALUE;
				List<List<RoutingPoint>> temp = new ArrayList<List<RoutingPoint>>();
				Iterator<List<RoutingPoint>> dimOne = data.iterator();
				int nbNonZeroSpeed = 0;
				boolean metLand = false;
				boolean allowOtherRoute = false;
				long before = System.currentTimeMillis();
				while (!interruptRouting && dimOne.hasNext() && keepLooping) {
//        timer = logDiffTime(timer, "Milestone 2");
					List<RoutingPoint> curve = dimOne.next();
					Iterator<RoutingPoint> dimTwo = curve.iterator();
					nbNonZeroSpeed = 0;
					metLand = false;
					while (!interruptRouting && keepLooping && dimTwo.hasNext()) {
//          timer = logDiffTime(timer, "Milestone 3");
						RoutingPoint newCurveCenter = dimTwo.next();
						List<RoutingPoint> oneCurve = new ArrayList<RoutingPoint>(10);

						wind = GribHelper.gribLookup(newCurveCenter.getPosition(), wgd, currentDate);
						if (wind != null && wind.comment != null && wind.comment.equals("TOO_OLD")) {
							center.setGribTooOld(true);
//            System.out.println("Stop if GRIB too old:" + stopIfGRIB2old);
							if (stopIfGRIB2old) {
								keepLooping = false;
								interruptedBecauseTooOld = true;
								System.out.println("Routing aborted. GRIB exhausted (preference).");
							}
						}
//          timer = logDiffTime(timer, "Milestone 4");

//          brg = getBearing(newCurveCenter); // 7-apr-2010.
						if (aimFor == null)
							brg = getBearing(newCurveCenter);
						else // Finer Routing
							brg = getBearingTo(newCurveCenter, aimFor);

//          nbNonZeroSpeed = 0;
						// Calculate isochron from center
						for (int bearing = brg - routingForkWidth / 2;
						     keepLooping && !interruptRouting && bearing <= brg + routingForkWidth / 2;
						     bearing += routingStep) {
//            timer = logDiffTime(timer, "Milestone 5");
							int windDir = 0;
							if (wind != null)
								windDir = wind.winddir;
							else {
//              Context.getInstance().fireLogging("Wind is null..., aborting (out of the GRIB)\n");
//              System.out.println("Aborting routing from " + center.getPosition().toString() + " to " + destination.getPosition().toString()+ ", wind is null.");
								//      keepLooping = false;
								continue;
							}
							int twa;
							for (twa = bearing - windDir; twa < 0; twa += 360) ;
							double wSpeed = 0.0D;
							if (wind != null) // Should be granted already...
								wSpeed = wind.windspeed;
							// In case user said to avoid TWS > xxx
							if (maxTWS > -1) {
								if (wSpeed > maxTWS) {
//                Context.getInstance().fireLogging("Avoiding too much wind (" + GnlUtilities.XXXX12.format(wSpeed) + " over " + Integer.toString(maxTWS) + ")\n");
//                System.out.println(".", LoggingPanel.RED_STYLE); // Takes a long time!
									wSpeed = 0;
									allowOtherRoute = true;
									continue;
								}
							}
							double speed = 0D;
							if (minTWA > -1 && twa < minTWA || twa > (360 - minTWA)) {
//              Context.getInstance().fireLogging("Avoiding too close wind (" + Integer.toString(twa) + " below " + Integer.toString(minTWA) + ")\n");
//              System.out.println(".", LoggingPanel.RED_STYLE); // Takes a long time!
								speed = 0D;
								allowOtherRoute = true;
								continue; // Added 22-Jun-2009
							} else {
//              if (minTWA > -1)
//                System.out.println(".", LoggingPanel.GREEN_STYLE); // Takes a long time!
								speed = PolarHelper.getSpeed(wSpeed, twa, speedCoeff);
							}

							if (speed < 0.0D)
								speed = 0.0D;

							if (speed > 0D) {
								nbNonZeroSpeed++;
								double dist = timeInterval * speed;
								arrivalDate = new Date(currentDate.getTime() + (long) (timeStep * 3600D * 1000D));
								GreatCirclePoint dr = GreatCircle.dr(new GreatCirclePoint(Math.toRadians(newCurveCenter.getPosition().getL()),
												Math.toRadians(newCurveCenter.getPosition().getG())),
										dist,
										bearing);
								GeoPoint forecast = new GeoPoint(Math.toDegrees(dr.getL()), Math.toDegrees(dr.getG()));
//              System.out.println("Routing point [" + forecast.toString() + "] in " + (World.isInLand(forecast)?"land <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<":"the water"));
								// Avoid the land
								// if (avoidLand && (World.isInLand(forecast) || World.isRouteCrossingLand(newCurveCenter.getPosition(), forecast) != null))
								if (avoidLand && World.isInLand(forecast)) {
//                System.out.println("..........................Avoiding land...");
									metLand = true;
									speed = 0D;
									allowOtherRoute = true;
									nbNonZeroSpeed--;
									continue;
								}

								Point forecastPoint = new Point((int) Math.round(forecast.getG() * 1000), (int) Math.round(forecast.getL() * 1000));
								RoutingPoint ip = new RoutingPoint(forecastPoint);

								// Add to Data
								ip.setPosition(forecast);
								ip.setAncestor(newCurveCenter);
								ip.setBsp(speed);        // Speed from the center
								ip.setHdg(bearing);      // Bearing from the center
								ip.setTwa(twa);          // twa from center
								ip.setTws(wSpeed);       // tws from center
								ip.setTwd(windDir);      // twd from center
								ip.setDate(arrivalDate); // arrival date at this point
								if (wind != null && wind.comment != null && wind.comment.equals("TOO_OLD")) {
									ip.setGribTooOld(true);
								}
								oneCurve.add(ip);
							}
//            timer = logDiffTime(timer, "Milestone 6");

						}
//          timer = logDiffTime(timer, "Milestone 7");
						if (!interruptRouting)
							temp.add(oneCurve);
					}
				}
				long after = System.currentTimeMillis();
//      System.out.println("Isochron calculated in " + Long.toString(after - before) + " ms.");
				// Start from the finalCurve, the previous enveloppe, for the next calculation
				// Flip data
//      timer = logDiffTime(timer, "Milestone 8");
				data = temp;
				List<RoutingPoint> finalCurve = null;
				if (!interruptRouting) {
//        timer = logDiffTime(timer, "Milestone 8-bis");
//        System.out.println("Reducing...");
//        System.out.print("Reducing...");
//        before = System.currentTimeMillis();
					finalCurve = calculateEnveloppe(data, center);
					if (aimFor != null) {
						if (isPointIn(aimFor, finalCurve, center)) {
							try {
								aimFor = bestRoute.get(++bestRouteIndex);
							} catch (IndexOutOfBoundsException ioobe) {
								aimFor = finalDestination;
							}
							System.out.println("Aiming for [" + aimFor.getPosition() + "]");
							//localSmallOne = Double.MAX_VALUE;
							smallestDist = Double.MAX_VALUE; // Reset, for the next leg
						}
					}
//        System.out.println("Reducing completed in " + Long.toString(System.currentTimeMillis() - before) + " ms\n");
//        System.out.println(" completed in " + Long.toString(System.currentTimeMillis() - before) + " ms\n");
				}
				// Calculate distance to destination, from the final curve
				Iterator<RoutingPoint> finalIterator = null;
//      timer = logDiffTime(timer, "Milestone 9");
				if (finalCurve != null) {
					try {
						finalIterator = finalCurve.iterator();
					} catch (Exception ex) {
						if (!interruptRouting) {
							ex.printStackTrace();
						}
					}
				}
//      System.out.println("finalIterator.hasNext() : [" + finalIterator.hasNext() + "]");
				while (!interruptRouting && finalIterator != null && finalIterator.hasNext()) {
//        timer = logDiffTime(timer, "Milestone 10");
					RoutingPoint forecast = finalIterator.next();
					GreatCircle.setStart(new GreatCirclePoint(Math.toRadians(forecast.getPosition().getL()),
							Math.toRadians(forecast.getPosition().getG())));
					if (aimFor == null) {
						GreatCircle.setArrival(new GreatCirclePoint(Math.toRadians(finalDestination.getPosition().getL()),
								Math.toRadians(finalDestination.getPosition().getG())));
					} else {
						GreatCircle.setArrival(new GreatCirclePoint(Math.toRadians(aimFor.getPosition().getL()),
								Math.toRadians(aimFor.getPosition().getG())));
					}
					try {
						gc.calculateGreatCircle(10);
						gcDistance = Math.toDegrees(GreatCircle.getDistance() * 60D);
						if (gcDistance < localSmallOne) {
							localSmallOne = gcDistance;
							closest = forecast;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
//      timer = logDiffTime(timer, "Milestone 11");
//      System.out.println("Local:" + localSmallOne + ", Smallest:" + smallestDist);
				if (localSmallOne < smallestDist) {
					smallestDist = localSmallOne;
					finalClosest = closest;
//        System.out.println("Still progressing...\n");
				} else if (localSmallOne == smallestDist) {
					// Not progressing
					keepLooping = false;
					System.out.println("Not progressing (stuck at " + smallestDist + " nm), aborting.");
				} else if (Math.abs(localSmallOne - smallestDist) < (smallestDist * 0.9)) {
					// When tacking for example... TODO Explore that one.
					System.out.println("Corner case... localSmallOne:" + localSmallOne + ", smallesrDist:" + smallestDist);
				} else {
					keepLooping = false;
					System.out.println("Destination reached? aiming WP [" + (aimFor != null ? aimFor.getPosition().toString() : "none") + "] finalDestination [" + finalDestination.getPosition().toString() + "]");
					System.out.println("LocalSmallOne:" + localSmallOne);
					System.out.println("SmallestDistance:" + smallestDist);
					if ((allowOtherRoute && nbNonZeroSpeed == 0) || metLand) {
						keepLooping = true; // Try again, even if the distance was not shrinking
//          smallestDist = localSmallOne;
						if (metLand) {
							System.out.println("--------------- Try again, maybe met land. (smallest:" + smallestDist + ", local:" + localSmallOne + ", prox:" + proximity + ") --------------");
//            JOptionPane.showMessageDialog(null, "Met Land?", "Bing", JOptionPane.PLAIN_MESSAGE);
							if (smallestDist < proximity) {
								keepLooping = false;
								System.out.println("Close enough.");
							} else
								smallestDist *= 1.5; // Boo... Should do some backtracking
						}
						smallestDist = localSmallOne;
					}
					if (localSmallOne != Double.MAX_VALUE) {
						if (intermediateWP != null) {
							smallestDist = Double.MAX_VALUE; // Reset, for the next leg
							keepLooping = true;
							finalCurve = new ArrayList<RoutingPoint>();
							finalCurve.add(closest);
							center = closest;
							center.setDate(currentDate);

							if (nbIntermediateIndex < intermediateWP.size())
								finalDestination = intermediateWP.get(nbIntermediateIndex++);
							else {
								if (!finalDestination.getPosition().equals(destination.getPosition()))
									finalDestination = destination;
								else {
									keepLooping = false;
									System.out.println("Destination reached, aiming (inter-WP) [" + (aimFor != null ? aimFor.getPosition().toString() : "none") + "] finalDestination [" + finalDestination.getPosition().toString() + "]");
								}
							}
						}
						if (!keepLooping) {// End of Routing
							System.out.println("Finished (" + smallestDist + " vs " + localSmallOne + ").\n(Non Zero Speed:" + nbNonZeroSpeed + ")");
						}
						if (nbNonZeroSpeed == 0) {
							if (interruptedBecauseTooOld) {
								JOptionPane.showMessageDialog(null, WWGnlUtilities.buildMessage("grib-exhausted", new String[]{Integer.toString(allIsochrons.size())}), WWGnlUtilities.buildMessage("routing"), JOptionPane.WARNING_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(null, "(2) " + WWGnlUtilities.buildMessage("routing-aborted", new String[]{Integer.toString(allIsochrons.size())}), WWGnlUtilities.buildMessage("routing"), JOptionPane.WARNING_MESSAGE);
							}
						}
					} else {
						if (interruptedBecauseTooOld) {
							JOptionPane.showMessageDialog(null, WWGnlUtilities.buildMessage("grib-exhausted", new String[]{Integer.toString(allIsochrons.size())}), WWGnlUtilities.buildMessage("routing"), JOptionPane.WARNING_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(null, "(3) " + WWGnlUtilities.buildMessage("routing-aborted", new String[]{Integer.toString(allIsochrons.size())}), WWGnlUtilities.buildMessage("routing"), JOptionPane.WARNING_MESSAGE);
						}
					}
				}
				allowOtherRoute = false;

//      timer = logDiffTime(timer, "Milestone 12");
				if (keepLooping) {
					allIsochrons.add(finalCurve);
					data = new ArrayList<List<RoutingPoint>>();
					data.add(finalCurve);
					currentDate = arrivalDate;
				}
				System.out.println("Isochrone # " + Integer.toString(allIsochrons.size()) + ", smallest distance to arrival:" + smallestDist + " nm. Still processing:" + keepLooping);
				System.out.println("Isochrone # " + Integer.toString(allIsochrons.size()) + "...");

				if (caller != null)
					caller.routingNotification(allIsochrons, finalClosest);
//      timer = logDiffTime(timer, "Milestone 13");
			}
			if (interruptRouting) {
				logDiffTime(timer, "Routing interrupted.");
//      System.out.println("Routing interrupted.");
				System.out.println("Routing aborted on user's request.");
			}
		}
//  timer = logDiffTime(timer, "Milestone 14");
		return allIsochrons;
	}

	public static List<List<RoutingPoint>> refineRouting(RoutingClientInterface caller,
	                                                     RoutingPoint startFrom,
	                                                     RoutingPoint destination,
	                                                     List<List<RoutingPoint>> previousIsochrons,
	                                                     RoutingPoint closestPoint,
	                                                     List<RoutingPoint> intermediateWP,
	                                                     Date fromDate,
	                                                     GribHelper.GribConditionData[] gribData,
	                                                     double timeInterval,
	                                                     int routingForkWidth,
	                                                     int routingStep,
	                                                     int maxTWS,
	                                                     int minTWA,
	                                                     boolean stopIfGRIB2old,
	                                                     double speedCoeff) {
		smallestDist = Double.MAX_VALUE; // Reset, for the next leg
		List<RoutingPoint> bestRoute = RoutingUtil.getBestRoute(closestPoint, previousIsochrons);
		// The route goes from destination to origin. Revert it.
		bestRoute = revertList(bestRoute);

//    for (RoutingPoint rp : bestRoute)
//      System.out.println("Best : " + rp.getPosition().toString());

		return calculateIsochrons(caller,
				startFrom,
				destination,
				intermediateWP,
				bestRoute,
				fromDate,
				gribData,
				timeInterval,
				routingForkWidth,
				routingStep,
				maxTWS,
				minTWA,
				stopIfGRIB2old,
				speedCoeff,
				false,                     // TASK Tossion.
				25.0);
	}

	public static <T> List<T> revertList(List<T> list) {
		List<T> inverted = new ArrayList<T>(list.size());
		int listSize = list.size();
		for (int i = listSize - 1; i >= 0; i--) {
			inverted.add(list.get(i));
		}
		return inverted;
	}

	public static List<RoutingPoint> getBestRoute(RoutingPoint closestPoint, List<List<RoutingPoint>> allIsochrons) {
		List<RoutingPoint> bestRoute = new ArrayList<RoutingPoint>(allIsochrons.size());
		boolean go = true;
		RoutingPoint start = closestPoint;
		bestRoute.add(start);
		while (go) {
			RoutingPoint next = start.getAncestor();
			if (next == null)
				go = false;
			else {
				bestRoute.add(next);
				start = next;
			}
		}
		return bestRoute;
	}

	public static RoutingPoint getClosestRoutingPoint(List<List<RoutingPoint>> isochrons,
	                                                  int draggedIsochronIdx,
	                                                  GeoPoint mousePosition) {
		RoutingPoint rp = null;
		List<RoutingPoint> isochron = isochrons.get(isochrons.size() - draggedIsochronIdx - 1);
//  System.out.println("Dragged isochron has " + isochron.size() + " point(s)");
		double minDist = Double.MAX_VALUE;
		for (RoutingPoint lrp : isochron) {
			double dist = GreatCircle.getDistanceInNM(new GreatCirclePoint(lrp.getPosition()), new GreatCirclePoint(mousePosition));
			if (dist < minDist) {
				minDist = dist;
				rp = lrp;
			}
		}
//  System.out.println("Smallest distance is " + minDist);
		return rp;
	}

	private static long timer = 0L;

	private static long logDiffTime(long before, String mess) {
		long after = System.currentTimeMillis();
		System.out.println(mess + " (" + Long.toString(after - before) + " ms)");
		return after;
	}

	// Possible optimization ?
	private static List<RoutingPoint> calculateEnveloppe(List<List<RoutingPoint>> bulkPoints, RoutingPoint center) {
		List<RoutingPoint> returnCurve = new ArrayList<RoutingPoint>();
		long before = System.currentTimeMillis();
		// Put ALL the points in the finalCurve
		Iterator<List<RoutingPoint>> dimOne = bulkPoints.iterator();
		while (!interruptRouting && dimOne.hasNext()) {
			List<RoutingPoint> curve = dimOne.next();
			Iterator<RoutingPoint> dimTwo = curve.iterator();
			while (!interruptRouting && dimTwo.hasNext()) {
				RoutingPoint newPoint = dimTwo.next();
				returnCurve.add(newPoint);
			}
		}
		String mess = "Reducing from " + returnCurve.size() + " ";
		int origNum = returnCurve.size();
		// Calculate final curve - Here is the skill
		dimOne = bulkPoints.iterator();
		while (!interruptRouting && dimOne.hasNext()) {
			Polygon currentPolygon = new Polygon();
			currentPolygon.addPoint(center.getPoint().x, center.getPoint().y); // center
			List<RoutingPoint> curve = dimOne.next();
			Iterator<RoutingPoint> dimTwo = curve.iterator();
			while (!interruptRouting && dimTwo.hasNext()) {
				RoutingPoint newPoint = dimTwo.next();
				currentPolygon.addPoint(newPoint.getPoint().x,
						newPoint.getPoint().y);
			}
			currentPolygon.addPoint(center.getPoint().x, center.getPoint().y); // close
			Iterator<List<RoutingPoint>> dimOneBis = bulkPoints.iterator();
			while (!interruptRouting && dimOneBis.hasNext()) {
				List<RoutingPoint> curveBis = dimOneBis.next();
				if (curveBis.equals(curve)) continue;
				Iterator<RoutingPoint> dimTwoBis = curveBis.iterator();
				while (!interruptRouting && dimTwoBis.hasNext()) {
					RoutingPoint isop = dimTwoBis.next();
					if (currentPolygon.contains(isop.getPoint())) {
						// Remove from the final Curve if it's inside (and not removed already)
//          if (returnCurve.contains(isop.getPoint())) // Demanding...
						{
							returnCurve.remove(isop.getPoint());
							//          System.out.println("Removing point, len now " + returnCurve.size());
						}
					}
				}
			}
		}
		long after = System.currentTimeMillis();
		int finalNum = returnCurve.size();
		float ratio = 100f * (float) (origNum - finalNum) / (float) origNum;
		System.out.println(mess + "to " + returnCurve.size() + " point(s) (gained " + ratio + "%), curve reduction calculated in " + Long.toString(after - before) + " ms");
		System.out.println(mess + "to " + returnCurve.size() + " point(s) (gained " + ratio + "%), curve reduction calculated in " + Long.toString(after - before) + " ms");

		return returnCurve;
	}

	private static boolean isPointIn(RoutingPoint rp, List<RoutingPoint> lrp, RoutingPoint center) {
		Polygon currentPolygon = new Polygon();
		currentPolygon.addPoint(center.getPoint().x, center.getPoint().y); // center
		for (RoutingPoint p : lrp)
			currentPolygon.addPoint(p.getPoint().x, p.getPoint().y);
		currentPolygon.addPoint(center.getPoint().x, center.getPoint().y); // close
		return currentPolygon.contains(rp.getPoint());
	}

	public static void interruptRoutingCalculation() {
		System.out.println("Interrupting the routing.");
		timer = System.currentTimeMillis();
		interruptRouting = true;
	}

	private static Pattern pattern = null;
	private static Matcher matcher = null;

	// TODO What-If routing

	public static void outputRouting(CommandPanel instance, GeoPoint from, GeoPoint to, RoutingPoint closestPoint, List<List<RoutingPoint>> allCalculatedIsochrons) {
		outputRouting(instance, from, to, closestPoint, allCalculatedIsochrons, false);
	}

	public static void outputRouting(CommandPanel instance, GeoPoint from, GeoPoint to, RoutingPoint closestPoint, List<List<RoutingPoint>> allCalculatedIsochrons, boolean doAsk) {
		int clipboardOption = Integer.parseInt(((ParamPanel.RoutingOutputList) (ParamPanel.data[ParamData.ROUTING_OUTPUT_FLAVOR][ParamData.VALUE_INDEX])).getStringIndex());
		String fileOutput = null;
		if (doAsk || clipboardOption == ParamPanel.RoutingOutputList.ASK) {
			try {
				Thread.sleep(500L);
			} catch (InterruptedException ie) {
			} // Pas joli...
			RoutingOutputFlavorPanel rofp = new RoutingOutputFlavorPanel();
			JOptionPane.showMessageDialog(instance, rofp, "Routing output", JOptionPane.QUESTION_MESSAGE);
			clipboardOption = rofp.getSelectedOption();
			fileOutput = rofp.getFileOutput();
		}

		String kmlPlaces = "";
		String kmlRoute = "";
		int firstKMLHeading = -1;

		// Reverse, for the clipboard
		boolean generateGPXRoute = true;
		String clipboardContent = "";
		// Opening tags
		if (clipboardOption == ParamPanel.RoutingOutputList.CSV)
			clipboardContent = "L;(dec L);G;(dec G);Date;UTC;TWS;TWD;BSP;HDG\n";
		else if (clipboardOption == ParamPanel.RoutingOutputList.GPX) {
			clipboardContent =
					"<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
							"<gpx version=\"1.1\" \n" +
							"     creator=\"OpenCPN\" \n" +
							"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
							"  xmlns=\"http://www.topografix.com/GPX/1/1\" \n" +
							"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" \n" +
							"  xmlns:opencpn=\"http://www.opencpn.org\">\n";
			if (generateGPXRoute) {
				Date d = new Date();
				clipboardContent += ("  <rte>\n" +
						"    <name>Weather Wizard route (" + WWGnlUtilities.SDF_DMY.format(d) + ")</name>\n" +
						"    <extensions>\n" +
						"      <opencpn:start>" + from.toString() + "</opencpn:start>\n" +
						"      <opencpn:end>" + to.toString() + "</opencpn:end>\n" +
						"      <opencpn:viz>1</opencpn:viz>\n" +
						"      <opencpn:guid>" + UUID.randomUUID().toString() + "</opencpn:guid>\n" +
						"    </extensions>\n" +
						"    <type>Routing</type>\n" +
						"    <desc>Routing from Weather Wizard (generated " + d.toString() + ")</desc>\n" +
						"    <number>" + (d.getTime()) + "</number>\n");
			}
		} else if (clipboardOption == ParamPanel.RoutingOutputList.TXT) {
			Date d = new Date();
			clipboardContent += ("Weather Wizard route (" + WWGnlUtilities.SDF_DMY.format(d) + ") generated " + d.toString() + ")\n");
		} else if (clipboardOption == ParamPanel.RoutingOutputList.KML) {
			Date d = new Date();
			clipboardContent +=
					"<?xml version = '1.0' encoding = 'UTF-8'?>\n" +
							"<kml xmlns=\"http://earth.google.com/kml/2.0\" \n" +
							"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
							"     xsi:schemaLocation=\"http://earth.google.com/kml/2.0 ../xsd/kml21.xsd\">\n" +
							"   <Document>\n" +
							"      <name>Weather Wizard route (" + WWGnlUtilities.SDF_DMY.format(d) + ")</name>\n"; // TASK Add from/to
		} else if (clipboardOption == ParamPanel.RoutingOutputList.JSON) {
			clipboardContent +=
					("{\n" +
							"  \"waypoints\": [\n");
		}

		if (closestPoint != null && allCalculatedIsochrons != null) {
			Calendar cal = new GregorianCalendar();
			List<RoutingPoint> bestRoute = RoutingUtil.getBestRoute(closestPoint, allCalculatedIsochrons);
			int routesize = bestRoute.size();
			String date = "", time = "";
			RoutingPoint rp = null;
			RoutingPoint ic = null; // Isochron Center
//    for (int r=0; r<routesize; r++) // 0 is the closest point, the last calculated
			for (int r = routesize - 1; r >= 0; r--) // 0 is the closest point, the last calculated
			{
				rp = bestRoute.get(r);
				if (r == 0) // Last one
					ic = rp;
				else
					ic = bestRoute.get(r - 1);

				if (rp.getDate() == null)
					date = time = "";
				else {
					cal.setTime(rp.getDate());

					int year = cal.get(Calendar.YEAR);
					int month = cal.get(Calendar.MONTH);
					int day = cal.get(Calendar.DAY_OF_MONTH);
					int hours = cal.get(Calendar.HOUR_OF_DAY);
					int minutes = cal.get(Calendar.MINUTE);
					int seconds = cal.get(Calendar.SECOND);
					if (clipboardOption == ParamPanel.RoutingOutputList.CSV) {
						date = WWGnlUtilities.DF2.format(month + 1) + "/" + WWGnlUtilities.DF2.format(day) + "/" + Integer.toString(year);
						time = WWGnlUtilities.DF2.format(hours) + ":" + WWGnlUtilities.DF2.format(minutes);
					} else if (clipboardOption == ParamPanel.RoutingOutputList.GPX) {
						date = Integer.toString(year) + "-" +
								WWGnlUtilities.DF2.format(month + 1) + "-" +
								WWGnlUtilities.DF2.format(day) + "T" +
								WWGnlUtilities.DF2.format(hours) + ":" +
								WWGnlUtilities.DF2.format(minutes) + ":" +
								WWGnlUtilities.DF2.format(seconds) + "Z";
					} else if (clipboardOption == ParamPanel.RoutingOutputList.TXT || clipboardOption == ParamPanel.RoutingOutputList.KML) {
						date = rp.getDate().toString();
					} else if (clipboardOption == ParamPanel.RoutingOutputList.JSON) {
						date = Integer.toString(year) + "-" +
								WWGnlUtilities.DF2.format(month + 1) + "-" +
								WWGnlUtilities.DF2.format(day) + "T" +
								WWGnlUtilities.DF2.format(hours) + ":" +
								WWGnlUtilities.DF2.format(minutes) + ":" +
								WWGnlUtilities.DF2.format(seconds) + "Z";
					}
				}
				// Route points
				if (clipboardOption == ParamPanel.RoutingOutputList.CSV) {
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
				} else if (clipboardOption == ParamPanel.RoutingOutputList.GPX) {
					if (generateGPXRoute) {
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
					} else {
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
				} else if (clipboardOption == ParamPanel.RoutingOutputList.TXT) {
					String tws = WWGnlUtilities.XX22.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = WWGnlUtilities.XX22.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());
					clipboardContent +=
							(rp.getPosition().toString() + " : " + date + ", tws:" + tws + ", twd:" + twd + ", bsp:" + bsp + ", hdg:" + hdg + "\n");
				} else if (clipboardOption == ParamPanel.RoutingOutputList.KML) {
					if (firstKMLHeading == -1)
						firstKMLHeading = ic.getHdg();
					kmlRoute += (rp.getPosition().getG() + "," + rp.getPosition().getL() + ",0\n");
					String tws = WWGnlUtilities.XX22.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = WWGnlUtilities.XX22.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());
					kmlPlaces +=
							("         <Placemark>\n" +
									"           <name>WayPoint " + Integer.toString(routesize - r) + "</name>\n" +
									"           <description>\n" +
									"            <![CDATA[\n" +
									"              <b>" + date + "</b>\n" +
									"              <table>\n" +
									"                <tr><td>TWS</td><td>" + tws + " knots</td></tr>\n" +
									"                <tr><td>TWD</td><td>" + twd + "&deg;</td></tr>\n" +
									"                <tr><td>BSP</td><td>" + bsp + " knots</td></tr>\n" +
									"                <tr><td>HDG</td><td>" + hdg + "&deg;</td></tr>\n" +
									"              </table>\n" +
									"            ]]>\n" +
									"           </description>\n" +
									"           <LookAt>\n" +
									"             <longitude>" + rp.getPosition().getG() + "</longitude>\n" +
									"             <latitude>" + rp.getPosition().getL() + "</latitude>\n" +
									"             <range>50000</range>\n" +
									"             <tilt>45</tilt>\n" +
									"             <heading>" + Integer.toString(ic.getHdg()) + "</heading>\n" +
									"           </LookAt>\n" +
									"           <Point>\n" +
									"             <coordinates>" + rp.getPosition().getG() + "," + rp.getPosition().getL() + ",0 </coordinates>\n" +
									"           </Point>\n" +
									"         </Placemark>\n");
				} else if (clipboardOption == ParamPanel.RoutingOutputList.JSON) {
					String tws = WWGnlUtilities.XXX12.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = WWGnlUtilities.XXX12.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());
					clipboardContent +=
							("    {\n" +
									"      \"datetime\":\"" + date + "\",\n" +
									"      \"position\": {\n" +
									"                  \"latitude\":\"" + rp.getPosition().getL() + "\",\n" +
									"                  \"longitude\":\"" + rp.getPosition().getG() + "\"\n" +
									"                },\n" +
									"      \"tws\":" + tws + ",\n" +
									"      \"twd\":" + twd + ",\n" +
									"      \"bsp\":" + bsp + ",\n" +
									"      \"hdg\":" + hdg + "\n" +
									"    }" + (r == 0 ? "" : ",") + "\n");
				}
			}
			// Closing tags
			if (clipboardOption == ParamPanel.RoutingOutputList.GPX) {
				if (generateGPXRoute)
					clipboardContent += "  </rte>\n";
				clipboardContent +=
						("</gpx>");
			} else if (clipboardOption == ParamPanel.RoutingOutputList.KML) {
				clipboardContent +=
						("      <Folder>\n" +
								"         <name>Waypoints</name>\n" +
								kmlPlaces +
								"      </Folder>\n");
				clipboardContent +=
						("      <Placemark>\n" +
								"          <name>Suggested route</name>\n" +
								"          <LookAt>\n" +
								"             <longitude>" + from.getG() + "</longitude>\n" +
								"             <latitude>" + from.getL() + "</latitude>\n" +
								"             <range>100000</range>\n" +
								"             <tilt>45</tilt>\n" +
								"             <heading>" + Integer.toString(firstKMLHeading) + "</heading>\n" +
								"          </LookAt>\n" +
								"          <visibility>1</visibility>\n" +
								"          <open>0</open>\n" +
								"          <Style>\n" +
								"             <LineStyle>\n" +
								"                <width>3</width>\n" +
								"                <color>ff00ffff</color>\n" +
								"             </LineStyle>\n" +
								"             <PolyStyle>\n" +
								"                <color>7f00ff00</color>\n" +
								"             </PolyStyle>\n" +
								"          </Style>\n" +
								"          <LineString>\n" +
								"             <extrude>1</extrude>\n" +
								"             <tessellate>1</tessellate>\n" +
								"             <altitudeMode>clampToGround</altitudeMode>\n" +
								"             <coordinates>\n" +
								kmlRoute +
								"             </coordinates>\n" +
								"          </LineString>\n" +
								"       </Placemark>\n");
				clipboardContent +=
						("       <Snippet><![CDATA[created by <a href=\"http://code.google.com/p/weatherwizard/\">The Weather Wizard</a>]]></Snippet>\n" +
								"   </Document>\n" +
								"</kml>");
			} else if (clipboardOption == ParamPanel.RoutingOutputList.JSON) {
				clipboardContent +=
						("  ]\n" +
								"}\n");
			}

			if (fileOutput != null && fileOutput.trim().length() > 0) {
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(fileOutput));
					bw.write(clipboardContent + "\n");
					bw.close();
					if (clipboardOption == ParamPanel.RoutingOutputList.JSON) // Suggest view in Google maps
					{
						int resp = JOptionPane.showConfirmDialog(instance, "See in Google maps?", "Routing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (resp == JOptionPane.YES_OPTION) {
							File f = new File("routing/googlemaprouting.html");
							if (f.exists()) {
								BufferedWriter bwjs = new BufferedWriter(new FileWriter("routing" + File.separator + "routing.js"));
								bwjs.write("var routing = " + clipboardContent + "\n");
								bwjs.close();

//              String whatToOpen = f.toURI().toURL().toString() + "?data=" + new File(fileOutput).toURI().toURL().toString();
								String whatToOpen = f.toURI().toURL().toString();
								System.out.println("Opening:" + whatToOpen);
								try {
									Utilities.openInBrowser(whatToOpen);
									Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
									StringSelection stringSelection = new StringSelection(whatToOpen);
									clipboard.setContents(stringSelection, null);
									JOptionPane.showMessageDialog(instance, "In case there is a problem,\nthe URL to open is in the clipboard.\nCtrl+V in your browser...", "Google Routing", JOptionPane.INFORMATION_MESSAGE);
								} catch (Exception ex) {
									String message = "Running in " + System.getProperty("user.dir") + "\n" + ex.getLocalizedMessage();
									JOptionPane.showMessageDialog(instance, message, "Routing in GoogleMaps", JOptionPane.ERROR_MESSAGE);
								}
							} else
								JOptionPane.showMessageDialog(instance, "File routing/googlemaprouting.html not found on your system...", "Google Routing", JOptionPane.WARNING_MESSAGE);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				WWContext.getInstance().fireSetStatus(WWGnlUtilities.buildMessage("routing-in-file", new String[]{fileOutput}));
			} else {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection stringSelection = new StringSelection(clipboardContent);
				clipboard.setContents(stringSelection, null);
				//          JOptionPane.showMessageDialog(null, "Routing is in the clipboard\n(Ctrl+V in any editor...)", "Routing completed", JOptionPane.INFORMATION_MESSAGE);
				WWContext.getInstance().fireSetStatus(WWGnlUtilities.buildMessage("routing-in-clip"));
			}
			WWContext.getInstance().fireRoutingAvailable(true, bestRoute);
			// End of the Routing.
		}
	}
}
