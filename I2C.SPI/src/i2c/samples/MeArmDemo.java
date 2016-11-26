package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
 * Standard, all the way, clockwise, counterclockwise
 */
public class MeArmDemo
{
  private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
  public static String userInput(String prompt)
  {
    String retString = "";
    System.err.print(prompt);
    try
    {
      retString = stdin.readLine();
    }
    catch(Exception e)
    {
      System.out.println(e);
      String s;
      try
      {
        s = userInput("<Oooch/>");
      }
      catch(Exception exception)
      {
        exception.printStackTrace();
      }
    }
    return retString;
  }

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
    
    final int LEFT_SERVO_CHANNEL   = 0; // Up and down. Range 350 (all the way up) 135 (all the way down), Centered at ~230
    final int CLAW_SERVO_CHANNEL   = 1; // Open and close. Range 130 (open) 400 (closed)
    final int BOTTOM_SERVO_CHANNEL = 2; // Right and Left. 130 (all the way right) 675 (all the way left). Center at ~410
    final int RIGHT_SERVO_CHANNEL  = 4; // BAck and forth. 130 (too far back, limit to 300) 675 (all the way ahead), right at ~430

    // Test the 4 servos.
    try {
      // Stop the servos
      servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(CLAW_SERVO_CHANNEL, 0, 0);
      servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
      waitfor(1000);

      // Center the arm
      servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 410);
      servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
      // Stand up
      servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 430);
      servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
      // Middle
      servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 230);
      servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);

      // Open and close the claw
      // 130 Open, 400 closed
      System.out.println("Opening the claw");
      move(servoBoard, CLAW_SERVO_CHANNEL, 400, 130, 10, 50); // Open it
      System.out.println("Give me something to grab.");
      client.SpeechTools.speak("Hey, give me something to grab, hit return when I can catch it.");
      userInput("Hit return when I can catch it.");
      System.out.println("Closing the claw");
      move(servoBoard, CLAW_SERVO_CHANNEL, 130, 400, 10, 50); // Close it
      System.out.println("Thank you!");
      client.SpeechTools.speak("Thank you!");

      // Turn left and drop it.
      System.out.println("Turning left");
      move(servoBoard, BOTTOM_SERVO_CHANNEL, 410, 670, 10, 50); // Turn left
      System.out.println("Reaching ahead");
      move(servoBoard, RIGHT_SERVO_CHANNEL, 430, 600, 10, 50); // Move ahead
      System.out.println("Higher");
      move(servoBoard, LEFT_SERVO_CHANNEL, 230, 350, 10, 50); // Move up
      System.out.println("Dropping");
      move(servoBoard, CLAW_SERVO_CHANNEL, 400, 130, 10, 50); // Drop it
      System.out.println("Backwards");
      move(servoBoard, RIGHT_SERVO_CHANNEL, 600, 430, 10, 50); // Move back
      System.out.println("Re-centering");
      move(servoBoard, BOTTOM_SERVO_CHANNEL, 670, 410, 10, 50); // Come back
      System.out.println("Down");
      move(servoBoard, LEFT_SERVO_CHANNEL, 350, 230, 10, 50); // Move down
      System.out.println("Closing");
      move(servoBoard, CLAW_SERVO_CHANNEL, 130, 400, 10, 50); // Close it
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
