package i2c.samples.motorHAT;

import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

/**
 * See also {@link Robot}, a 2-wheel robot moved by DC motors.
 *
 */
public class RobotDemo {
	public static void main(String... args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		Robot robot = new Robot();

    /* Now move the robot around!
     * Each call below takes two parameters:
     *  - speed: The speed of the movement, a value from 0-255.  The higher the value
     *           the faster the movement.  You need to start with a value around 100
     *           to get enough torque to move the robot.
     *  - time (seconds):  Amount of time to perform the movement.  After moving for
     *                     this amount of seconds the robot will stop.  This parameter
     *                     is optional and if not specified the robot will start moving
     *                     forever.
     */
		System.out.println("Forward");
		robot.forward(150, 1.0f);   // Move forward at speed 150 for 1 second.
		System.out.println("Left");
		robot.left(200, 0.5f);      // Spin left at speed 200 for 0.5 seconds.

		System.out.println("Forward");
		robot.forward(150, 1.0f);   // Repeat the same movement 3 times below...
		System.out.println("Left");
		robot.left(200, 0.5f);

		System.out.println("Forward");
		robot.forward(150, 1.0f);
		System.out.println("Left");
		robot.left(200, 0.5f);

		System.out.println("Forward");
		robot.forward(150, 1.0f);
		System.out.println("Right");
		robot.right(200, 0.5f);

		// Spin in place slowly for a few seconds.
		System.out.println("Right...");
		robot.right(100);  // No time is specified so the robot will start spinning forever.
		Robot.delay(2.0f);   // Pause for a few seconds while the robot spins (you could do other processing here though).
		robot.stop();             // Stop the robot.

		// Now move backwards and spin right a few times.
		System.out.println("Backward");
		robot.backward(150, 1.0f);
		System.out.println("Right");
		robot.right(200, 0.5f);

		System.out.println("Backward");
		robot.backward(150, 1.0f);
		System.out.println("Right");
		robot.right(200, 0.5f);

		System.out.println("Backward");
		robot.backward(150, 1.0f);
		System.out.println("Right");
		robot.right(200, 0.5f);

		System.out.println("Backward");
		robot.backward(150, 1.0f);

		System.out.println("That's it!");
	}
}
