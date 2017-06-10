package i2c.samples.motorHAT;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.adafruitmotorhat.AdafruitMotorHAT;

import java.io.IOException;

public class OneMotorDemo
{
  private int addr = 0x60;    // The I2C address of the motor HAT, default is 0x60.
  private AdafruitMotorHAT.Motor motorID = AdafruitMotorHAT.Motor.M1; // The ID of the left motor, default is 1.
  private int trim = 0;  // Amount to offset the speed of the left motor, can be positive or negative and use useful for matching the speed of both motors.  Default is 0.

  private AdafruitMotorHAT mh;
  private AdafruitMotorHAT.AdafruitDCMotor motor;

  public OneMotorDemo() throws I2CFactory.UnsupportedBusNumberException
  {
    this.mh = new AdafruitMotorHAT();
    this.motor = mh.getMotor(motorID);
    try
    {
      this.motor.run(AdafruitMotorHAT.ServoCommand.RELEASE);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    // if stopOnExit...
  }

  public void stop()
  {
    try
    {
      this.motor.run(AdafruitMotorHAT.ServoCommand.RELEASE);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  public void setSpeed(int speed) throws IllegalArgumentException, IOException
  {
    if (speed < 0 || speed > 255)
    {
      throw new IllegalArgumentException("Speed must be an int belonging to [0, 255]");
    }
    int leftSpeed = speed + this.trim;
    leftSpeed = Math.max(0, Math.min(255, leftSpeed));
    this.motor.setSpeed(leftSpeed);
  }


  public void forward(int speed) throws IOException
  {
    forward(speed, 0);
  }
  public void forward(int speed, float seconds) throws IOException
  {
    this.motor.setSpeed(speed);
    this.motor.run(AdafruitMotorHAT.ServoCommand.FORWARD);
    if (seconds > 0)
    {
      delay(seconds);
      this.stop();
    }
  }

  public void backward(int speed) throws IOException
  {
    backward(speed, 0);
  }
  public void backward(int speed, float seconds) throws IOException
  {
    this.motor.setSpeed(speed);
    this.motor.run(AdafruitMotorHAT.ServoCommand.BACKWARD);
    if (seconds > 0)
    {
      delay(seconds);
      this.stop();
    }
  }

  public static void delay(float sec)
  {
    try
    {
      Thread.sleep((long)(sec * 1_000));
    }
    catch (InterruptedException ie)
    {
      // Absorb
    }
  }

  public static void main(String args[]) throws Exception
  {
    OneMotorDemo omd = new OneMotorDemo();
    System.out.println("Forward...");
    omd.forward(100, 10f);
    System.out.println("Done.");
  }
}
