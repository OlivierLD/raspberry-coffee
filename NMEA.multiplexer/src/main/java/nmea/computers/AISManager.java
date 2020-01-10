package nmea.computers;

import calc.GeomUtil;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.ais.AISParser;
import nmea.api.Multiplexer;
import nmea.api.NMEAParser;
import nmea.parser.GeoPos;
import nmea.parser.StringParsers;
import utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * AIS Manager
 * Uses current position and AIS data to detect possible collision threats
 */
public class AISManager extends Computer {

	private double minimumDistance = 20D;
	private final static double DEFAULT_HEADING_FORK = 10;
	private double headingFork = DEFAULT_HEADING_FORK;

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
					AISParser.AISRecord aisRecord = AISParser.parseAIS(sentence);
					if (aisRecord.getLatitude() != 0f && aisRecord.getLongitude() != 0f) {
						NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
						GeoPos position = (GeoPos) cache.get(NMEADataCache.POSITION);
						if (position != null) {

							double distToTarget = GeomUtil.haversineNm(position.lat, position.lng, aisRecord.getLatitude(), aisRecord.getLongitude());
							double bearingFromTarget = GeomUtil.bearingFromTo(aisRecord.getLatitude(), aisRecord.getLongitude(), position.lat, position.lng);

							if (distToTarget <= this.minimumDistance) {
								double diffHeading = GeomUtil.bearingDiff(bearingFromTarget, aisRecord.getCog());
								if (diffHeading < this.headingFork) { // Possible collision route
									// TODO Honk!
									System.out.println(String.format("!!! Possible collision with %s, at %s / %s\n\tdistance %.02f nm (min is %.02f)\n\tBearing from target to current pos. %.02f\272\n\tCOG Target: %.02f",
											aisRecord.getMMSI(),
											GeomUtil.decToSex(aisRecord.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
											GeomUtil.decToSex(aisRecord.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
											distToTarget,
											this.minimumDistance,
											bearingFromTarget,
											aisRecord.getCog()));
								}
							}
//							System.out.println(String.format("Comparing %s with %s / %s\n\tdistance %.02f nm (min is %.02f)\n\tBearing from target to current pos. %.02f\272\n\tCOG Target: %.02f",
//									position.toString(),
//									GeomUtil.decToSex(aisRecord.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
//									GeomUtil.decToSex(aisRecord.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
//									distToTarget,
//									this.minimumDistance,
//									bearingFromTarget,
//									aisRecord.getCog()));
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
		this.minimumDistance = Double.parseDouble(props.getProperty("minimum.distance", String.valueOf(this.minimumDistance)));
		this.headingFork = Double.parseDouble(props.getProperty("heading.fork.width", String.valueOf(DEFAULT_HEADING_FORK)));
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
