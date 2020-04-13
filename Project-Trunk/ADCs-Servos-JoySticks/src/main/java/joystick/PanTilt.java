package joystick;

import java.io.InputStream;

import servo.StandardServo;

import static utils.TimeUtil.delay;

/*
 * Driven by keyboard entries.
 * 2 Servos (UP/LR)
 */
public class PanTilt {
	private static StandardServo ssUD = null,
					ssLR = null;

	public static void main(String... args) throws Exception {
		ssUD = new StandardServo(14); // 14 : Address on the board (1..15)
		ssLR = new StandardServo(15); // 15 : Address on the board (1..15)

		// Init/Reset
		ssUD.stop();
		ssLR.stop();
		ssUD.setAngle(0f);
		ssLR.setAngle(0f);

		delay(2_000);

		InputStream in = System.in;
		boolean go = true;
		float angleUD = 0f;
		float angleLR = 0f;
		System.out.println("Type [U]p, [D]own, [L]eft, [R]ight, or [Q]uit. (followed by [Return])");
		boolean unmanaged = false;
		while (go) {
			unmanaged = false;
			if (in.available() > 0) {
				int b = in.read();
				if (((char) b) == 'Q' || ((char) b) == 'q')
					go = false;
				else if (((char) b) == 'L' || ((char) b) == 'l') {
					angleLR -= (((char) b) == 'L' ? 10 : 1);
					ssLR.setAngle(angleLR); // -90..+90
				} else if (((char) b) == 'R' || ((char) b) == 'r') {
					angleLR += (((char) b) == 'R' ? 10 : 1);
					ssLR.setAngle(angleLR); // -90..+90
				} else if (((char) b) == 'U' || ((char) b) == 'u') // Inverted...
				{
					angleUD -= (((char) b) == 'U' ? 10 : 1);
					ssUD.setAngle(angleUD); // -90..+90
				} else if (((char) b) == 'D' || ((char) b) == 'd') // Inverted...
				{
					angleUD += (((char) b) == 'D' ? 10 : 1);
					ssUD.setAngle(angleUD); // -90..+90
				} else
					unmanaged = true;

				if (!unmanaged && go) {
					System.out.println("LR:" + angleLR + ", UD:" + angleUD);
				}
			}
		}
		// Reset to 0,0 before shutting down.
		ssUD.setAngle(0f);
		ssLR.setAngle(0f);
		delay(2_000);
		ssUD.stop();
		ssLR.stop();
		System.out.println("Bye");
	}
}
