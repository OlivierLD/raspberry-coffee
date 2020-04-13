package servo;

import static utils.StaticUtil.userInput;

/*
 * Standard, using I2C and the PCA9685 servo board
 * User interface (CLI).
 */
public class InteractiveServo {

	/**
	 * To test the servo - namely, the min & max values.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		int channel = 14;
		if (args.length > 0) {
			try {
				channel = Integer.parseInt(args[0]);
			} catch (Exception e) {
				throw e;
			}
		}
		System.out.println("Driving Servo on Channel " + channel);
		StandardServo ss = new StandardServo(channel);

		boolean loop = true;
		System.out.println("Enter the angle at the prompt (-90..90), and q to quit.");

		try {
			ss.stop();
			while (loop) {
				String userInput = userInput("Angle (or Q) > ");
				if ("Q".equals(userInput.toUpperCase())) {
					loop = false;
				} else {
					try {
						float angle = Float.parseFloat(userInput);
						ss.setAngle(angle);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		} finally {
			ss.stop();
		}
		System.out.println("Done.");
	}
}
