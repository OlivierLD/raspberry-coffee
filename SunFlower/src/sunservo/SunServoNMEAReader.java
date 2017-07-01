package sunservo;

import ansi.EscapeSeq;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import i2c.servo.pwm.PCA9685;

import calculation.AstroComputer;
import calculation.SightReductionUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.consumers.reader.SerialReader;
import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;
import org.fusesource.jansi.AnsiConsole;

/**
 * @Deprecated
 */
public class SunServoNMEAReader extends NMEAClient {
	private final static DecimalFormat DFH = new DecimalFormat("#0.00'\272'");
	private final static DecimalFormat DFZ = new DecimalFormat("##0.00'\272'");
	private final static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

	private static GeoPos prevPosition = null;
	private static long prevDateTime = -1L;
	private static int prevZ = -1;
	private static boolean parked = false;

	private static boolean calibrated = false;
	private static PCA9685 servoBoard = null;
	private static int servoMin = 130;   // was 150. Min pulse length out of 4096
	private static int servoMax = 615;   // was 600. Max pulse length out of 4096

	private final static int CONTINUOUS_SERVO_CHANNEL = 14;
	private final static int STANDARD_SERVO_CHANNEL = 15;
	private final static String STR100 = "                                                                                                    ";

	private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	public SunServoNMEAReader() {
		super();
	}

	public static String userInput(String prompt) {
		String retString = "";
		System.err.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			System.out.println(e);
			String s;
			try {
				s = userInput("<Oooch/>");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return retString;
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
//  System.out.println("Received:" + e.getContent());
		manageData(e.getContent().trim());
	}

	@Override
	public Object getBean() {
		return null;
	}

	private static SunServoNMEAReader customClient = null;

	private static void manageData(String sentence) {
		boolean valid = StringParsers.validCheckSum(sentence);
		Date now = new Date();
		if (valid) {
			String id = sentence.substring(3, 6);
			if ("RMC".equals(id)) {
				// System.out.println(line);
				RMC rmc = StringParsers.parseRMC(sentence);
				// System.out.println(rmc.toString());
				if (rmc != null && rmc.getRmcDate() != null && rmc.getGp() != null) {
					if ((prevDateTime == -1L || prevPosition == null) ||
									(prevDateTime != (rmc.getRmcDate().getTime() / 1_000) || !rmc.getGp().equals(prevPosition))) {
						Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
						current.setTime(rmc.getRmcDate());
						AstroComputer.setDateTime(current.get(Calendar.YEAR),
										current.get(Calendar.MONTH) + 1,
										current.get(Calendar.DAY_OF_MONTH),
										current.get(Calendar.HOUR_OF_DAY),
										current.get(Calendar.MINUTE),
										current.get(Calendar.SECOND));
						AstroComputer.calculate();
						SightReductionUtil sru = new SightReductionUtil(AstroComputer.getSunGHA(),
										AstroComputer.getSunDecl(),
										rmc.getGp().lat,
										rmc.getGp().lng);
						sru.calculate();
						Double he = sru.getHe();
						Double z = sru.getZ();
						// Orient the servo here
						if (!calibrated) {
							calibrate();
							calibrated = true;
						}

						try {
							if (he > 0) {
								if (parked) {
									String str = (SDF.format(now) + ":Resuming, sun is up.");
									AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
									AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
								}
								parked = false;
								int angle = 180 - (int) Math.round(z);
								if (angle < -90 || angle > 90) {
									String str = (SDF.format(now) + ":Between -90 and 90 only");
									AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
									AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
								} else {
									if (prevZ != angle) {
										String str = (SDF.format(now) + ":From [" + sentence + "]");
										AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
										AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
										str = (current.getTime().toString() + ", He:" + DFH.format(he) + ", Z:" + DFZ.format(z) + " (" + rmc.getGp().toString() + ") => " + angle);
										AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 3) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
										AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 3) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
										int on = 0;
										int off = (int) (servoMin + (((double) (angle + 90) / 180d) * (servoMax - servoMin)));
										//              System.out.println("setPWM(" + STANDARD_SERVO_CHANNEL + ", " + on + ", " + off + ");");
										servoBoard.setPWM(STANDARD_SERVO_CHANNEL, on, off);
										//              System.out.println("-------------------");
									}
									prevZ = angle;
								}
							} else {
								// Parking
								if (!parked) {
									String str = (SDF.format(now) + ":Parking, sun is down.");
									AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
									AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
								}
								parked = true;
								int on = 0;
								int angle = 0;
								int off = (int) (servoMin + (((double) (angle + 90) / 180d) * (servoMax - servoMin)));
//              System.out.println("setPWM(" + STANDARD_SERVO_CHANNEL + ", " + on + ", " + off + ");");
								servoBoard.setPWM(STANDARD_SERVO_CHANNEL, on, off);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					prevPosition = rmc.getGp();
					prevDateTime = (rmc.getRmcDate().getTime() / 1_000);
				} else {
					if (rmc == null) {
						String str = (SDF.format(now) + ": ... no RMC data in [" + sentence + "]");
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_RED, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
					} else {
						String errMess = "";
						if (rmc.getRmcDate() == null)
							errMess += ("no Date ");
						if (rmc.getGp() == null)
							errMess += ("no Pos ");
						String str = (SDF.format(now) + ":" + errMess + "in [" + sentence + "]");
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_RED, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
					}
				}
			}
		} else {
			String str = (SDF.format(now) + ":Invalid data [" + sentence + "]");
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_RED, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
		}
		String str = SDF.format(now);
		AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 4) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
		AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 4) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_CYAN, EscapeSeq.ANSI_BLACK) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL);
	}

	private static void calibrate() {
		try {
			int angle = 0;
			if (angle < -90 || angle > 90)
				System.err.println("Between -90 and 90 only");
			else {
				int on = 0;
				int off = (int) (servoMin + (((double) (angle + 90) / 180d) * (servoMax - servoMin)));
				System.out.println("setPWM(" + STANDARD_SERVO_CHANNEL + ", " + on + ", " + off + ");");
				servoBoard.setPWM(STANDARD_SERVO_CHANNEL, on, off);
				// TODO Vers le pole abaissé.
				String dummy = userInput("Orient the arrow SOUTH (true S, with no W), and hit return when ready.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) throws UnsupportedBusNumberException {
		System.setProperty("deltaT", System.getProperty("deltaT", "67.2810")); // 2014-Jan-01
	  /*
     * Serial port possibly overriden by -Dserial.port
     * Default is /dev/ttyAMA0
     */
		int br = 4_800;
		System.out.println("CustomNMEAReader invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("CustomNMEAReader prm:" + s);
			try {
				br = Integer.parseInt(s);
			} catch (NumberFormatException nfe) {
			}
		}

		customClient = new SunServoNMEAReader();

		servoBoard = new PCA9685();
		servoBoard.setPWMFreq(60); // Set frequency to 60 Hz
		if (!calibrated) {
			calibrate(); // Point the arrow South.
			calibrated = true;
		}

		AnsiConsole.systemInstall();
		AnsiConsole.out.println(EscapeSeq.ANSI_HOME + EscapeSeq.ANSI_CLS);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down nicely.");
			customClient.stopDataRead();
		}));
		customClient.initClient();
		customClient.setReader(new SerialReader(customClient.getListeners(), "<com port goes here>", br));
		customClient.startWorking(); // Feignasse!
	}

	public void stopDataRead() {
		if (customClient != null) {
			for (NMEAListener l : customClient.getListeners())
				l.stopReading(new NMEAEvent(this));
		}
	}
}