package nmea.consumers.reader;

import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.LSM303;
import java.io.IOException;
import java.util.List;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringGenerator;
import nmea.parser.StringGenerator.XDRTypes;
import utils.DampingService;

/**
 * Reads data from an LSM303 sensor.
 * Pitch and Roll, as XDR Strings.
 * Heading, as an HDM String.
 */
public class LSM303Reader extends NMEAReader {

	private LSM303 lsm303;
	private static final String DEFAULT_DEVICE_PREFIX = "RP";
	private String devicePrefix = DEFAULT_DEVICE_PREFIX;
	private static final long BETWEEN_LOOPS = 1_000L;
	/*
	 * Heading offset is the *read* bearing of the magnetic north.
	 * Point the LSM303 to the Magnetic North, and read the value returned by the lsm303.getHeading() methods.
	 * The value read by the device is the offset.
	 * Offset value is in [-180..180].
	 */
	private int headingOffset = 0;
	private Long readFrequency = BETWEEN_LOOPS; // Default
	private Integer damping = 200; // Default

	private DampingService<DegreeAngle> dampingService;

	public LSM303Reader(List<NMEAListener> al) {
		this(null, al);
	}
	public LSM303Reader(String threadName, List<NMEAListener> al) {
		super(threadName, al);
		this.setVerbose("true".equals(System.getProperty("lsm303.data.verbose", "false")));
		this.dampingService = new DampingService<>(damping);
		try {
			this.lsm303 = new LSM303(); // TODO Calibration parameters!!
		} catch (I2CFactory.UnsupportedBusNumberException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public String getDevicePrefix() {
		return this.devicePrefix;
	}

	public void setDevicePrefix(String devicePrefix) {
		this.devicePrefix = devicePrefix;
	}

	public int getHeadingOffset() {
		return this.headingOffset;
	}

	public void setHeadingOffset(int headingOffset) {
		this.headingOffset = headingOffset;
	}

	public Long getReadFrequency() {
		return this.readFrequency;
	}

	public void setReadFrequency(Long freq) {
		this.readFrequency = freq;
	}

	public Integer getDampingSize() {
		return this.damping;
	}

	public void setDampingSize(Integer dmp) {
		this.damping = dmp;
	}

	@Override
	public void startReader() {
		super.enableReading();
		if (this.lsm303 != null) {
			this.lsm303.startReading();
		} else {
			System.out.println("   >>> No LSM303 started, not found.");
		}
		System.out.println(String.format(">> Starting reader [%s] (prefix %s). Enabled:%s", this.getClass().getName(), this.devicePrefix, this.canRead()));

		// Start a thread that will broadcast the *damped* values, every second.
		final LSM303Reader instance = this;
		Thread feeder = new Thread("lsm303.nmea.feeder") {
			@Override
			public void run() {
				while (instance.canRead()) {
					DegreeAngle smooth = null;
					synchronized (dampingService) {
						smooth = dampingService.smooth(new SmoothableDegreeAngle());
						if (instance.verbose) {
							System.out.println(String.format(">>> Smoothed Heading in degrees %f, %d entries.", smooth.getAngleInDegrees(), dampingService.getBufferSize()));
						}
					}
					String nmeaHDM = StringGenerator.generateHDM(devicePrefix, (int) Math.round(smooth.getAngleInDegrees()));
					nmeaHDM += NMEAParser.NMEA_SENTENCE_SEPARATOR;
					fireDataRead(new NMEAEvent(instance, nmeaHDM));
					try {
						Thread.sleep(BETWEEN_LOOPS);
					} catch (InterruptedException ie) {
						// Bam!
						ie.printStackTrace();
					}
				}
				System.out.println(">>> Damping Heading Service stopped");
			}
		};
		feeder.start();

		while (this.canRead()) {
			// Read data every 1 second
			// Filter accordingly if needed, on XDR and HDM
			try {

				double pitch = 0;
				double roll  = 0;
				double heading = 0;
				if (lsm303 != null) {
					pitch = lsm303.getPitch();
					roll = lsm303.getRoll();
					heading = lsm303.getHeading();
				}
				if (this.headingOffset != 0) {
					heading += headingOffset;
					while (heading > 360) {
						heading -= 360;
					}
					while (heading < 0) {
						heading += 360;
					}
				}
				// Only for Heading for now...
				DegreeAngle degreeAngle = new DegreeAngle(Math.sin(Math.toRadians(heading)), Math.cos(Math.toRadians(heading)));
				synchronized (dampingService) {
					dampingService.append(new SmoothableDegreeAngle(degreeAngle));
				}

				if (this.verbose) {
					System.out.println(String.format("\t>>> From LSM303: Heading %f, Pitch: %f, Roll: %f", heading, pitch, roll));
				}

				// Generate NMEA String(s). OpenCPN recognizes those ones (Needs a 'II' prefix though).
				String nmeaXDR = StringGenerator.generateXDR(devicePrefix,
								new StringGenerator.XDRElement(XDRTypes.ANGULAR_DISPLACEMENT,
												pitch,
												"PTCH"), // No, it's not a typo, there is no 'I' in 'PTCH'.
								new StringGenerator.XDRElement(XDRTypes.ANGULAR_DISPLACEMENT,
												roll,
												"ROLL"));
				nmeaXDR += NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, nmeaXDR));

//				String nmeaHDM = StringGenerator.generateHDM(devicePrefix, (int)Math.round(heading));
//				nmeaHDM += NMEAParser.NMEA_SENTENCE_SEPARATOR;
//				fireDataRead(new NMEAEvent(this, nmeaHDM));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(readFrequency);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		System.out.println(String.format(">>> %s done reading. Bye.", this.getClass().getName()));
	}

	@Override
	public void closeReader() throws Exception {
		if (this.lsm303 != null) {
			this.lsm303.setKeepReading(false);
		}
	}

	// Classes used for the damping
	public static class DegreeAngle {

		private double cosinus;
		private double sinus;

		public DegreeAngle(double sin, double cos) {
			this.cosinus = cos;
			this.sinus = sin;
		}

		public double getAngleInDegrees() {
			double deg = 0;
			if (this.cosinus != 0) {
				deg = Math.toDegrees(Math.acos(this.cosinus));
				if (this.sinus < 0) {
					deg = -deg;
				}
			} else if (this.sinus != 0) {
				deg = Math.toDegrees(Math.asin(this.sinus));
			} else {
				deg = 0;
			}
			while (deg < 0) { // On 360, instead of [-180..180]
				deg += 360;
			}
			return deg;
		}
	}

	public static class SmoothableDegreeAngle implements DampingService.Smoothable<DegreeAngle> {

		private DegreeAngle degreeAngle;

		public SmoothableDegreeAngle() {
			this(new DegreeAngle(0, 0));
		}
		public SmoothableDegreeAngle(DegreeAngle deg) {
			this.degreeAngle = deg;
		}


		@Override
		public DegreeAngle get() {
			return this.degreeAngle;
		}

		@Override
		public void accumulate(DegreeAngle elmt) {
			degreeAngle.sinus += elmt.sinus;
			degreeAngle.cosinus += elmt.cosinus;
		}

		@Override
		public DegreeAngle smooth(List<DegreeAngle> buffer) {
			final SmoothableDegreeAngle smoothed = new SmoothableDegreeAngle(new DegreeAngle(0, 0));
			buffer.forEach(elmt -> {
				smoothed.accumulate(elmt);
			});
			DegreeAngle degreeeAngle = smoothed.get();
			degreeeAngle.sinus /= buffer.size();
			degreeeAngle.cosinus /= buffer.size();
			return degreeeAngle;
		}
	}

}
