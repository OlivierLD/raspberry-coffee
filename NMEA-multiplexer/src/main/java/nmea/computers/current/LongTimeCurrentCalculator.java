package nmea.computers.current;

import calc.GreatCircle;
import calc.GreatCirclePoint;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.parser.Angle360;
import nmea.parser.GeoPos;
import nmea.parser.Speed;
import nmea.parser.UTCDate;
import nmea.parser.UTCHolder;
import nmea.parser.UTCTime;
import util.MercatorUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Does the current - speed and direction - over a given period of time, rather than
 * on an instant triangulation.<br>
 * That turns out to be way more accurate.
 * <p>
 * It requires GPS Data, Apparent Wind Data, Heading, Deviation and Deviation to calculate an accurate CMG.<br>
 * Important: <b>The calculations are put in the NMEADataCache, under a member named "Current calculated with damping"</b>.
 * </p>
 * There is a section dedicated to those details at <a href="http://www.lediouris.net/RaspberryPI/_Articles/readme.html">http://www.lediouris.net/RaspberryPI/_Articles/readme.html</a>.
 */
public class LongTimeCurrentCalculator /* extends Computer */ {

	private final static long ONE_HOUR_MS = 3_600_000L;

	private boolean verbose = false;
	// buffer.length in milliseconds
	public final static long DEFAULT_BUFFER_LENGTH = 600_000L; // Milli Seconds
	private long bufferLength; // Long.parseLong(System.getProperty("buffer.length", String.valueOf(DEFAULT_BUFFER_LENGTH))); // Default 10 minutes

	private Thread watcher = null;
	private boolean keepWatching = true;
	private final long betweenLoops = 1_000L; // 1 sec

	// Time, Position, CMG, BSP.
	private List<TimeCurrent> timeCurrent = new ArrayList<>();
	private List<UTCHolder> timeBuffer = new ArrayList<>();
	private List<GeoPos> positionBuffer = new ArrayList<>();
	private List<Angle360> cmgBuffer = new ArrayList<>();
	private List<Angle360> hdgBuffer = new ArrayList<>();
	private List<Speed> bspBuffer = new ArrayList<>();

	private GreatCirclePoint[] groundData = null;
	private GreatCirclePoint[] drData = null;

	public LongTimeCurrentCalculator() {
		this(DEFAULT_BUFFER_LENGTH);
	}

	public LongTimeCurrentCalculator(long bufferLength) {
		this.bufferLength = bufferLength;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public long getBufferLength() {
		return bufferLength;
	}

	public int getBufferSize() {
		return timeCurrent.size();
	}

	/**
	 * et the time buffer length, in ms.
	 * @param bufferLength in ms.
	 */
	public void setBufferLength(long bufferLength) {
		this.bufferLength = bufferLength;
	}

	public void resetBuffers() {
		this.timeCurrent = new ArrayList<>();
		this.timeBuffer = new ArrayList<>();
		this.positionBuffer = new ArrayList<>();
		this.cmgBuffer = new ArrayList<>();
		this.hdgBuffer = new ArrayList<>();
		this.bspBuffer = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public void start() {
		System.out.println(this.getClass().getName() + " is starting...");
		final long _betweenLoops = betweenLoops;
		watcher = new Thread("CurrentCalculatorWatcher") {
			private final long BETWEEN_LOOPS = _betweenLoops;
			private long waitTime = BETWEEN_LOOPS;

			public void run() {
				this.setPriority(Thread.MIN_PRIORITY);
				while (keepWatching) {
					waitTime = BETWEEN_LOOPS;
					NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
					if (cache != null) {
						if (verbose) {
							System.out.println("There is a cache...");
						}
						try {
				      //    synchronized (cache)
							{
								Object ot = /*(UTCDate)*/cache.get(NMEADataCache.GPS_DATE_TIME);
								if (ot == null) {
									ot = /*(UTCTime)*/cache.get(NMEADataCache.GPS_TIME);
									if (verbose) {
										System.out.println("Time from NMEADataCache.GPS_TIME");
									}
								} else if (verbose) {
									System.out.println("Time from NMEADataCache.GPS_DATE_TIME");
								}
								UTCHolder utcDate = null;
								if (ot instanceof UTCDate) {
									utcDate = new UTCHolder((UTCDate) ot);
								} else {
									utcDate = new UTCHolder((UTCTime) ot);
								}
								Angle360 cmg = null;
								try { cmg = (Angle360) cache.get(NMEADataCache.CMG); } catch (Exception ex) {}
								GeoPos position = null;
								try { position = (GeoPos) cache.get(NMEADataCache.POSITION); } catch (Exception ex) {}
								Speed bsp = null;
								try { bsp = (Speed) cache.get(NMEADataCache.BSP); } catch (Exception ex) {}
								Angle360 hdg = null;
								try { hdg = (Angle360) cache.get(NMEADataCache.HDG_TRUE); } catch (Exception ex) {}
								// From a file: reset?
								//            if (timeBuffer.size() > 1 && ((timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() > utcDate.getValue().getTime())))
								if (timeBuffer != null &&
										timeBuffer.size() > 1 &&
										timeBuffer.get(timeBuffer.size() - 1) != null &&
										!timeBuffer.get(timeBuffer.size() - 1).isNull() &&
										utcDate != null &&
										!utcDate.isNull() &&
										utcDate.getValue() != null &&
										((timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() - utcDate.getValue().getTime()) > 1_000)) {
									// Buffer Reset
						//    System.out.println("== Resetting data buffers: last date in buffer=[" + SDF2.format(timeBuffer.get(timeBuffer.size() - 1).getValue()) + "] > current Date=[" + SDF2.format(utcDate.getValue()) + "]");
									resetDataBuffers();
								}

								if (timeBuffer != null &&
										utcDate != null &&
										!utcDate.isNull() &&
										utcDate.getValue() != null &&
										(timeBuffer.size() == 0 ||
												(timeBuffer.size() > 0 &&
														timeBuffer.get(timeBuffer.size() - 1).getValue() != null &&
														(timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime())))) {
									if (utcDate != null && cmg != null && position != null && bsp != null && hdg != null) {
										if (timeBuffer.size() > 0) {
											UTCHolder oldest = timeBuffer.get(0);
											boolean keepGoing = true;

											while (keepGoing && oldest.getValue().getTime() < (utcDate.getValue().getTime() - bufferLength)) {
												timeBuffer.remove(0);
												positionBuffer.remove(0);
												cmgBuffer.remove(0);
												bspBuffer.remove(0);
												hdgBuffer.remove(0);

												if (timeBuffer.size() > 0) {
													oldest = timeBuffer.get(0);
												} else {
													keepGoing = false;
												}
											}
										}

										timeBuffer.add(utcDate);
										positionBuffer.add(position);
										//                System.out.println("Adding position:" + position.toString());
										cmgBuffer.add(cmg);
										bspBuffer.add(bsp);
										hdgBuffer.add(hdg);
										groundData = new GreatCirclePoint[positionBuffer.size()];
										int index = 0;
										for (GeoPos gp : positionBuffer) {
											groundData[index++] = new GreatCirclePoint(gp.lat, gp.lng); // GPS
										}
										index = 0;
										drData = new GreatCirclePoint[positionBuffer.size()]; // Positions from DoW and CMG (aka on water)
										GeoPos drPos = positionBuffer.get(0);
										int size = positionBuffer.size();
										// DoW: Distance on Water, CMG: Course Made Good
										// From point to point (with EACH DoW and CMG) calculate the point we should have reached at the end of the buffer.
										// The difference is the vector of the current.
										for (int i = 0; i < size; i++) {
											if (i > 0) {
												long timeInterval = timeBuffer.get(i).getValue().getTime() - timeBuffer.get(i - 1).getValue().getTime();
												double bSpeed = bspBuffer.get(i).getDoubleValue();
												//                    System.out.println("-- TimeInterval:" + timeInterval + ", bsp:" + bSpeed);
												if (bSpeed > 0) { // Then calculate estimated pos, with DoW and CMG
													double dist = bSpeed * ((double) timeInterval / (double) ONE_HOUR_MS); // in minutes (miles)
													double rv = cmgBuffer.get(i - 1).getValue(); // rv: Route Vraie (aka Surface): CMG in French
													//                      System.out.println("** In " + timeInterval + " ms, at " + bSpeed + " kts, from " + drPos.toString() + " dist:" + dist + ", hdg:" + hdg + "... ");
													if (dist > 0) {
														GreatCirclePoint pt = MercatorUtil.deadReckoning(drPos.lat, drPos.lng, dist, rv);
														//                        System.out.println("In " + timeInterval + " ms, from " + drPos.toString() + " dist:" + dist + ", hdg:" + hdg + ", ends up " + pt.toString());
														drPos = new GeoPos(pt.getL(), pt.getG()); // We should be here if no current
													}
												}
											} // else we set the starting point
											drData[i] = new GreatCirclePoint(drPos.lat, drPos.lng);
										}
										// final DR (on water...)
										GreatCirclePoint geoFrom = new GreatCirclePoint(
												Math.toRadians(drData[drData.length - 1].getL()),
												Math.toRadians(drData[drData.length - 1].getG()));
										// final GPS (on the ground)
										GreatCirclePoint geoTo = new GreatCirclePoint(
												Math.toRadians(groundData[groundData.length - 1].getL()),
												Math.toRadians(groundData[groundData.length - 1].getG()));

										// Between the 2 above: the current
										double dist = GreatCircle.calculateRhumLineDistance(geoFrom, geoTo); // Dist between DR & GPS
										double currentDir = Math.toDegrees(GreatCircle.calculateRhumbLineRoute(geoFrom, geoTo));
										double hourRatio = (double) (timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() - timeBuffer.get(0).getValue().getTime()) / (double) ONE_HOUR_MS;
										double currentSpeed = dist / hourRatio;
										timeCurrent.add(new TimeCurrent(
												timeBuffer.get(timeBuffer.size() - 1).getValue().getTime(),
												currentSpeed,
												currentDir));
										// Trim current buffer to the time-length.
										// Remove point older than the buffer length (which is a time interval, not a cardinality)
										long oldest = timeCurrent.get(0).getTime();
										boolean keepGoing = true;
										while (keepGoing && oldest < (timeCurrent.get(timeCurrent.size() - 1).getTime() - bufferLength)) {
											timeCurrent.remove(0);
											if (timeBuffer.size() > 0) {
												oldest = timeCurrent.get(0).getTime();
											} else {
												keepGoing = false;
											}
										}
										if (verbose) {
											System.out.println("Inserting Current: on:" + NumberFormat.getInstance().format(bufferLength) + " ms, " + currentSpeed + " kts, dir:" + currentDir);
										}
										((Map<Long, NMEADataCache.CurrentDefinition>) ApplicationContext.getInstance()
												.getDataCache().get(NMEADataCache.CALCULATED_CURRENT)).put(bufferLength,
														new NMEADataCache.CurrentDefinition(
																		bufferLength,
																		new Speed(currentSpeed),
																		new Angle360(currentDir),
																		timeBuffer.size(),
																		timeBuffer.get(0).toString(),
																		timeBuffer.get(timeBuffer.size() - 1).toString(),
																		timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() - timeBuffer.get(0).getValue().getTime()));
										if (verbose) {
											@SuppressWarnings("unchecked")
											Map<Long, NMEADataCache.CurrentDefinition> map = (Map<Long, NMEADataCache.CurrentDefinition>) ApplicationContext.getInstance().getDataCache().get(NMEADataCache.CALCULATED_CURRENT);
											System.out.println("Calculated Current Map:" + map.size() + " entry(ies)");
										}
									}
								} else if (verbose) {
									//  if (!utcDate.isNull() && (timeBuffer.size() == 0 || (timeBuffer.size() > 0 && (timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime()))))
									System.out.println("utcDate is " + (utcDate == null || utcDate.isNull() ? "" : "not ") + "null");
									System.out.println("timeBuffer.size() = " + timeBuffer.size());
									System.out.println("utcDate        :" + (utcDate.isNull() ? "" : new Date(utcDate.getValue().getTime()).toString()));
									System.out.println("last timeBuffer:" + (timeBuffer.size() > 0 ? new Date(timeBuffer.get(timeBuffer.size() - 1).getValue().getTime()).toString() : "none"));
									try {
										if (timeBuffer.size() > 0) {
											System.out.println("-> " + ((timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime()) ? "true" : "false"));
										}
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else {
						System.out.println("... No cache yet");
					}
					synchronized (this) {
						if (verbose) {
							System.out.println("  ...LongTimeCurrentCalculator going to wait, at " + new Date().toString() + " (will wait for " + (waitTime / 1_000) + " s)");
						}
						try {
							wait(waitTime);
						} catch (InterruptedException ie) {
							System.out.println("Told to stop!");
							keepWatching = false;
						}
					}
				}
				System.out.println("Stop waiting.");
			}
		};
		keepWatching = true;
		watcher.start();
	}

	private void resetDataBuffers() {
		timeBuffer = new ArrayList<>();
		positionBuffer = new ArrayList<>();
		cmgBuffer = new ArrayList<>();
		hdgBuffer = new ArrayList<>();
		bspBuffer = new ArrayList<>();
		timeCurrent = new ArrayList<>();
	}

	public void stop() {
		System.out.println("    " + this.getClass().getName() + "(" + NumberFormat.getInstance().format(this.bufferLength) + " ms) is terminating (at epoch " + System.currentTimeMillis() + ")");
		keepWatching = false;
		synchronized (watcher) {
			watcher.notify();
		}
	}

	private static class TimeCurrent {
		private final long time;
		private final double speed;
		private final double dir;

		public TimeCurrent(long time, double speed, double dir) {
			this.time = time;
			this.speed = speed;
			this.dir = dir;
		}

		public long getTime() {
			return time;
		}

		public double getSpeed() {
			return speed;
		}

		public double getDir() {
			return dir;
		}
	}
}
