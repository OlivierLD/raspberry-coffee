package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

/*
 * Standard, all the way, clockwise, counterclockwise
 */
public class MeArmDemo
{
  private static void waitfor(long howMuch)
  {
    try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
  }

  // Servo MG90S
  private static int servoMin = 130; // -90 degrees at 60 Hertz
  private static int servoMax = 675; //  90 degrees at 60 Hertz

  public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException
  {
    PCA9685 servoBoard = new PCA9685();
    int freq = 60;
    servoBoard.setPWMFreq(freq); // Set frequency in Hz
    
    final int LEFT_SERVO_CHANNEL   = 0; // Range 350 (all the way up) 135 (all the way down)
    final int CLAW_SERVO_CHANNEL   = 1; // Range 130 (open) 400 (closed)
    final int BOTTOM_SERVO_CHANNEL = 2; // 130 (all the way right) 675 (all the way left)
    final int RIGHT_SERVO_CHANNEL  = 4; // 130 (too far back, limit to 300) 675 (all the way ahead)

    // Test the 4 servos.
    try {
      // Stop the servos
      servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(CLAW_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
      waitfor(1000);

      // Open and close the claw
      // 130 Open, 400 closed
      move(servoBoard, CLAW_SERVO_CHANNEL, 400, 130, 10, 100);
      waitfor(1000);
      move(servoBoard, CLAW_SERVO_CHANNEL, 130, 400, 10, 100);

    } finally {
      // Stop the servos
      servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(CLAW_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
    }
    System.out.println("Done.");
  }

  private static void move(PCA9685 servoBoard, int channel, int from, int to, int step, int wait) {
    servoBoard.setPWM(channel, 0, 0);
    int inc = step * (from < to ? 1 : -1);
    for (int i = from; i != to; i+=inc) {
      servoBoard.setPWM(channel, 0, i);
      waitfor(wait);
    }
    servoBoard.setPWM(channel, 0, 0);
  }
}
