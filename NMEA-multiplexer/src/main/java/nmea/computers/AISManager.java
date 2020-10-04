package nmea.computers;

import calc.GeomUtil;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.ais.AISParser;
import nmea.api.Multiplexer;
import nmea.api.NMEAParser;
import nmea.parser.GeoPos;
import nmea.parser.StringParsers;
import util.TextToSpeech;
import utils.StringUtils;
import utils.TimeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * AIS Manager. WIP.
 * Uses current position and AIS data to detect possible collision threats.
 * Does NOT put anything in the cache.
 */
public class AISManager extends Computer {

	private final static double DEFAULT_MINIMUM_DISTANCE = 20D;
	private double minimumDistance = DEFAULT_MINIMUM_DISTANCE;
	private final static double DEFAULT_HEADING_FORK = 10;
	private double headingFork = DEFAULT_HEADING_FORK;

	private AISParser aisParser = new AISParser();

	public AISManager(Multiplexer mux) {
		super(mux);
	}

	/**
	 * Wait for AIS data (not cached), get the position from the cache,
	 * and computes threat.
	 *
	 * @param mess Received message
	 */
	@Override
	public void write(byte[] mess) {
		String sentence = new String(mess);
//	System.out.println(String.format("In AIS Computer, write method: %s", sentence));

		if (StringParsers.validCheckSum(sentence)) {
			if (sentence.startsWith(AISParser.AIS_PREFIX)) {
				try {
					AISParser.AISRecord aisRecord = aisParser.parseAIS(sentence);
					if (aisRecord != null) {
						if (aisRecord.getLatitude() != 0f && aisRecord.getLongitude() != 0f) {
							NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
							GeoPos position = (GeoPos) cache.get(NMEADataCache.POSITION);
							if (position != null) {
								double distToTarget = GeomUtil.haversineNm(position.lat, position.lng, aisRecord.getLatitude(), aisRecord.getLongitude());
								double bearingFromTarget = GeomUtil.bearingFromTo(aisRecord.getLatitude(), aisRecord.getLongitude(), position.lat, position.lng);
								// TODO Use the two speeds and headings (here and target). First degree equation solving.
								if (distToTarget <= this.minimumDistance) {
									double diffHeading = GeomUtil.bearingDiff(bearingFromTarget, aisRecord.getCog());
									String inRangeMessage = String.format("(%s) AISManager >> In range (%.02f/%.02f nm), diff heading: %.02f", TimeUtil.getTimeStamp(), distToTarget, this.minimumDistance, diffHeading);
									System.out.println(inRangeMessage);
									// A test
									if (false) {
										String messToSpeak = String.format("Boat in range %.02f miles! %s", distToTarget, (aisRecord.getVesselName() != null ? aisRecord.getVesselName() : ""));
										TextToSpeech.speak(messToSpeak);
									}
									if (diffHeading < this.headingFork) { // Possible collision route (if you don't move)
										String warningText = String.format("!!! Possible collision threat with %s (%s), at %s / %s\n\tdistance %.02f nm (min is %.02f)\n\tBearing from target to current pos. %.02f\272\n\tCOG Target: %.02f",
												aisRecord.getMMSI(),
												aisRecord.getVesselName(),
												GeomUtil.decToSex(aisRecord.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
												GeomUtil.decToSex(aisRecord.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
												distToTarget,
												this.minimumDistance,
												bearingFromTarget,
												aisRecord.getCog());
										System.out.println(warningText);
										// TODO Honk! Define a callback Consumer
										if ("true".equals(System.getProperty("try.to.speak"))) {
											// A test
											int bearingToTarget = (int)(180 + Math.round(bearingFromTarget));
											bearingToTarget %= 360;
											String messageToSpeak = String.format("Possible collision threat, %.02f miles in the %d.",
													distToTarget,
													bearingToTarget);
											TextToSpeech.speak(messageToSpeak);
										}
									}
								} else {
									if (this.verbose) {
										System.out.println(String.format("For %s (%s): Comparing %s with %s / %s (%.04f / %.04f)\n\tdistance %.02f nm (min is %.02f)\n\tBearing from target to current pos. %.02f\272\n\tCOG Target: %.02f\n\t-> No threat found",
												aisRecord.getMMSI(),
												aisRecord.getVesselName(),
												position.toString(),
												GeomUtil.decToSex(aisRecord.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
												GeomUtil.decToSex(aisRecord.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
												aisRecord.getLatitude(),
												aisRecord.getLongitude(),
												distToTarget,
												this.minimumDistance,
												bearingFromTarget,
												aisRecord.getCog()));
									}
								}
							}
						}
					}
				} catch (AISParser.AISException aisException) { // un-managed AIS type
					// Absorb
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing AIS data, " + this.getClass().getName());
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
		this.minimumDistance = Double.parseDouble(props.getProperty("minimum.distance", String.valueOf(DEFAULT_MINIMUM_DISTANCE)));
		this.headingFork = Double.parseDouble(props.getProperty("heading.fork.width", String.valueOf(DEFAULT_HEADING_FORK)));
		this.verbose = "true".equals(props.getProperty("verbose"));
		if (this.verbose) {
			System.out.println(String.format("Computer %s:\nVerbose: %s\nMinimum Distance: %.02f\nHeading Fork: %.01f",
					this.getClass().getName(),
					this.verbose,
					this.minimumDistance,
					this.headingFork));
		}
	}

	public static class AISComputerBean {
		private String cls;
		private String type = "ais-computer";
		private boolean verbose;

		public AISComputerBean(AISManager instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
		}
	}

	@Override
	public Object getBean() {
		return new AISComputerBean(this);
	}
}
