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

  private static int servoMin = 122;
  private static int servoMax = 615;

  public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException
  {
    PCA9685 servoBoard = new PCA9685();
    int freq = 60;
    servoBoard.setPWMFreq(freq); // Set frequency in Hz
    
    final int LEFT_SERVO_CHANNEL   = 0;
    final int CLAW_SERVO_CHANNEL   = 1;
    final int BOTTOM_SERVO_CHANNEL = 2;
    final int RIGHT_SERVO_CHANNEL  = 3;


    int servoChannel = BOTTOM_SERVO_CHANNEL;
    int diff = servoMax - servoMin;
    System.out.println("Min:" + servoMin + ", Max:" + servoMax + ", diff:" + diff);

    if (false) {
      try {
        servoBoard.setPWM(servoChannel, 0, 0);   // Stop the standard one
        waitfor(2000);
        System.out.println("Let's go, 1 by 1");
        for (int i = servoMin; i <= servoMax; i++) {
          System.out.println("i=" + i + ", " + (-90f + (((float) (i - servoMin) / (float) diff) * 180f)));
          servoBoard.setPWM(servoChannel, 0, i);
          waitfor(10);
        }
        for (int i = servoMax; i >= servoMin; i--) {
          System.out.println("i=" + i + ", " + (-90f + (((float) (i - servoMin) / (float) diff) * 180f)));
          servoBoard.setPWM(servoChannel, 0, i);
          waitfor(10);
        }
        servoBoard.setPWM(servoChannel, 0, 0);   // Stop the standard one
        waitfor(2000);
        System.out.println("Let's go, 1 deg by 1 deg");
        for (int i = servoMin; i <= servoMax; i += (diff / 180)) {
          System.out.println("i=" + i + ", " + Math.round(-90f + (((float) (i - servoMin) / (float) diff) * 180f)));
          servoBoard.setPWM(servoChannel, 0, i);
          waitfor(10);
        }
        for (int i = servoMax; i >= servoMin; i -= (diff / 180)) {
          System.out.println("i=" + i + ", " + Math.round(-90f + (((float) (i - servoMin) / (float) diff) * 180f)));
          servoBoard.setPWM(servoChannel, 0, i);
          waitfor(10);
        }
        servoBoard.setPWM(servoChannel, 0, 0);   // Stop the standard one
        waitfor(2000);

        float[] degValues = {-10, 0, -90, 45, -30, 90, 10, 20, 30, 40, 50, 60, 70, 80, 90, 0};
        for (float f : degValues) {
          int pwm = degreeToPWM(servoMin, servoMax, f);
          System.out.println(f + " degrees (" + pwm + ")");
          servoBoard.setPWM(servoChannel, 0, pwm);
          waitfor(1500);
        }
      } finally {
        servoBoard.setPWM(servoChannel, 0, 0);   // Stop the standard one
      }

      System.out.println("Done.");
    }

    // Test the 4 servos.
    try {
      // Stop the servos
      servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(CLAW_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
      waitfor(1000);

      move(servoBoard, "LEFT", -10, 10, LEFT_SERVO_CHANNEL, 100);
      move(servoBoard, "RIGHT", -10, 10, RIGHT_SERVO_CHANNEL, 100);
      move(servoBoard, "BOTTOM", -10, 10, BOTTOM_SERVO_CHANNEL, 100);
      move(servoBoard, "CLAW", -10, 10, CLAW_SERVO_CHANNEL, 100);

    } finally {
      // Stop the servos
      servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(CLAW_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
    }
    System.out.println("Done.");
  }

  private static void move(PCA9685 servoBoard, String name, int min, int max, int channel, int wait) {
    System.out.println(String.format("%s: Let's go, 1 by 1", name));
    for (int i = 0; i <= max; i++) {
      int pwm = degreeToPWM(servoMin, servoMax, i);
      System.out.println("i=" + i + ", pwm=" + pwm);
      servoBoard.setPWM(channel, 0, pwm);
      waitfor(wait);
    }
    for (int i = max; i >= min; i--) {
      int pwm = degreeToPWM(servoMin, servoMax, i);
      System.out.println("i=" + i + ", pwm=" + pwm);
      servoBoard.setPWM(channel, 0, pwm);
      waitfor(wait);
    }
    for (int i = min; i <= 0; i++) {
      int pwm = degreeToPWM(servoMin, servoMax, i);
      System.out.println("i=" + i + ", pwm=" + pwm);
      servoBoard.setPWM(channel, 0, pwm);
      waitfor(wait);
    }
  }
  /*
   * deg in [-90..90]
   */
  private static int degreeToPWM(int min, int max, float deg)
  {
    int diff = max - min;
    float oneDeg = diff / 180f;
    return Math.round(min + ((deg + 90) * oneDeg));
  }    
}
