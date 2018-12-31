package orientation;

import static utils.StaticUtil.userInput;

/**
 * Show how to use the panel orienter (SunFlower)
 * Takes the Device (true) Heading from the Console
 */
public class InteractivePanelOrienter {

	public static void main(String... args) {

		int servoHeading = 14;
		int servoTilt = 15;

		// Supported parameters --heading:14 --tilt:15
		if (args.length > 0) {
			for (String prm : args) {
				if (prm.startsWith("--heading:")) {
					try {
						servoHeading = Integer.parseInt(prm.substring("--heading:".length()));
					} catch (Exception e) {
						throw e;
					}
				} else if (prm.startsWith("--tilt:")) {
					try {
						servoTilt = Integer.parseInt(prm.substring("--tilt:".length()));
					} catch (Exception e) {
						throw e;
					}
				}
			}
		}

		SunFlower instance = new SunFlower(new int[] { servoHeading }, new int[] { servoTilt });

		String strLat = System.getProperty("default.sf.latitude");
		if (strLat != null) {
			try {
				instance.setLatitude(Double.parseDouble(strLat));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strLong = System.getProperty("default.sf.longitude");
		if (strLong != null) {
			try {
				instance.setLongitude(Double.parseDouble(strLong));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}

		// Set to 0
		instance.servosZero();
		instance.setCalibrating(false);

		String mess = "Point the Device to the true South, hit [Return] when ready."; // Assuming we are in the Northern hemisphere.
		System.out.println(mess);

		instance.setCalibrating(true);
		userInput("");
		instance.setCalibrating(false);
		// Done calibrating
		double heading = 180D;
		instance.setDeviceHeading(heading);
		instance.startWorking();

		boolean demo = "true".equals(System.getProperty("auto.demo", "false"));
		if (demo) {
			for (int head = 30; head<330; head+=10) {
				instance.setDeviceHeading(head);
				try { Thread.sleep(1_100L); } catch (Exception ignore) {}
			}
		} else {
			boolean read = true;
			System.out.println("Enter 'Q' at the prompt to quit.");
			while (read) {
				String input = userInput(String.format("Enter Device's true heading [0..360], now %.02f > ", instance.getDeviceHeading()));
				if ("Q".equalsIgnoreCase(input.trim())) {
					read = false;
				} else {
					try {
						double h = Double.parseDouble(input);
						if (h < 0 || h > 360) {
							System.out.println("Bad range [0..360]");
						} else {
							instance.setDeviceHeading(h);
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
			}
		}
		System.out.println("Bye.");
		instance.stopWorking();
		instance.setHeadingServoAngle(0f);
		instance.setTiltServoAngle(0f);
		instance.stopHeadingServo();
		instance.stopTiltServo();
		try {
			Thread.sleep(1_000L);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}
}
