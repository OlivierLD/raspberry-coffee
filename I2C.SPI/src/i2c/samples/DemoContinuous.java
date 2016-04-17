package i2c.samples;

import i2c.servo.pwm.PCA9685;

/*
 * Continuous, all the way, clockwise, counterclockwise
 * Note: This DOES NOT work as documented.
 */
public class DemoContinuous
{
  private static void waitfor(long howMuch)
  {
    try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
  }

  public static void main(String[] args)
  {
    PCA9685 servoBoard = new PCA9685();
    int freq = 60;
    servoBoard.setPWMFreq(freq); // Set frequency in Hz
    
    final int CONTINUOUS_SERVO_CHANNEL = 14;
//  final int STANDARD_SERVO_CHANNEL   = 15;
    
    int servo = CONTINUOUS_SERVO_CHANNEL;
    int servoMin = 340; 
    int servoMax = 410; 
    int servoStopsAt = 375;
    
    servoBoard.setPWM(servo, 0, 0);   // Stop the servo
    waitfor(2000);
    System.out.println("Let's go");
    
    for (int i=servoStopsAt; i<=servoMax; i++)
    {
      System.out.println("i=" + i);
      servoBoard.setPWM(servo, 0, i);
      waitfor(500);
    } 
    System.out.println("Servo Max");
    waitfor(1000);
    for (int i=servoMax; i>=servoMin; i--)
    {
      System.out.println("i=" + i);
      servoBoard.setPWM(servo, 0, i);
      waitfor(500);
    } 
    System.out.println("Servo Min");
    waitfor(1000);
    for (int i=servoMin; i<=servoStopsAt; i++)
    {
      System.out.println("i=" + i);
      servoBoard.setPWM(servo, 0, i);
      waitfor(500);
    } 
    waitfor(2000);
    servoBoard.setPWM(servo, 0, 0);   // Stop the servo
    System.out.println("Done.");
  }
}
