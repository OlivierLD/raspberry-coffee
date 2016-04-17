package i2c.samples;

import i2c.servo.pwm.PCA9685;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
 * Two servos - one standard, one continous
 * Enter all the values from the command line, and see for yourself.
 */
public class InteractiveServo
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

  public static void main(String[] args)
  {
    PCA9685 servoBoard = new PCA9685();
    int freq = 60;
    String sFreq = userInput("freq (40-1000)  ? > ");
    try { freq = Integer.parseInt(sFreq); }
    catch (NumberFormatException nfe)
    {
      System.err.println("Defaulting freq to 60");
      nfe.printStackTrace();
    }
    if (freq < 40 || freq > 1000)
      throw new IllegalArgumentException("Freq only between 40 and 1000.");
    
    servoBoard.setPWMFreq(freq); // Set frequency in Hz
    
    final int CONTINUOUS_SERVO_CHANNEL = 14;
    final int STANDARD_SERVO_CHANNEL   = 15;
    
    int servo = STANDARD_SERVO_CHANNEL;
    
    String sServo = userInput("Servo: Continuous [C], Standard [S] > ");
    if ("C".equalsIgnoreCase(sServo))
      servo = CONTINUOUS_SERVO_CHANNEL;
    else if ("S".equalsIgnoreCase(sServo))
      servo = STANDARD_SERVO_CHANNEL;
    else
      System.out.println("Only C or S... Defaulting to Standard.");
    
    boolean keepGoing = true;
    System.out.println("Enter 'quit' to exit.");
    while (keepGoing)
    {
      String s1 = userInput("pulse width in ticks  (0..4095) ? > ");
      if ("QUIT".equalsIgnoreCase(s1))
        keepGoing = false;
      else
      {
        try
        {
          int on = Integer.parseInt(s1);
          if (on < 0 || on > 4095)
            System.out.println("Values between 0 and 4095.");
          else
          {
            System.out.println("setPWM(" + servo + ", 0, " + on + ");");
            servoBoard.setPWM(servo, 0, on);
            System.out.println("-------------------");
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    } 
    System.out.println("Done.");
  }
}
