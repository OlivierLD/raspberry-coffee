package i2c.servo.adafruitmotorhat;

import java.io.IOException;

public class AdafruitMotorHAT
{
  public enum Style
  {  
    SINGLE, DOUBLE, INTERLEAVE, MICROSTEP
  }
  
  public enum Motor
  {
    M1, M2, M3, M4
  }
  
  public enum ServoCommand
  {
    FORWARD, BACKWARD, BRAKE, RELEASE
  }

  private final static int HAT_ADDR = 0x60;
  private final static int DEFAULT_FREQ = 1600;
  private int freq = 1600;
  private int i2caddr = HAT_ADDR;
  
  private AdafruitDCMotor      motors[];
  private AdafruitStepperMotor steppers[];
  private PWM pwm;
  
  public AdafruitMotorHAT()
  {
    this(HAT_ADDR, DEFAULT_FREQ);
  }
            
  public AdafruitMotorHAT(int addr, int freq)
  {
    this.i2caddr = addr;
    this.freq    = freq;
    motors = new AdafruitDCMotor[4];
    int i = 0;
    for (Motor motor : Motor.values())
      motors[i++] = new AdafruitDCMotor(this, motor);
    steppers = new AdafruitStepperMotor[2];
    steppers[0] = new AdafruitStepperMotor(this, 1);
    steppers[0] = new AdafruitStepperMotor(this, 2);
    pwm = new PWM(addr);
    try
    {
      pwm.setPWMFreq(freq);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  public void setPin(int pin, int value) throws IOException
  {
    if (pin < 0 || pin > 15)
      throw new RuntimeException("PWM pin must be between 0 and 15 inclusive : " + pin);
    if (value != 0 && value != 1)
      throw new RuntimeException("Pin value must be 0 or 1! " + value);
    if (value == 0)
      this.pwm.setPWM(pin, (short)0, (short)4096);
    if (value == 1)
      this.pwm.setPWM(pin, (short)4096, (short)0);
  }
  
  public AdafruitStepperMotor getStepper(int num)
  {
    if (num < 1 || num > 2)
      throw new RuntimeException("MotorHAT Stepper must be between 1 and 2 inclusive");
    return steppers[num-1];
  }

  public AdafruitDCMotor getMotor(Motor num)
  {
    AdafruitDCMotor motor = null;
    for (AdafruitDCMotor m : motors)
    {
      if (m.motorNum == num)
      {
        motor = m;
        if ("true".equals(System.getProperty("hat.debug", "false")))
        {
          System.out.println("getMotor (DC):" + num);
        }
        break;
      }
    }
    return motor;
  }
  
  public static class AdafruitDCMotor
  {
    private AdafruitMotorHAT mh;
    private Motor motorNum;
    private int pwm = 0, in1 = 0, in2 = 0;
    private int PWMpin = 0, IN1pin = 0, IN2pin =0;
    
    public AdafruitDCMotor(AdafruitMotorHAT controller, Motor num)
    {
      this.mh = controller;
      this.motorNum = num;
      if (num == Motor.M1)
      {
        pwm = 8;
        in2 = 9;
        in1 = 10;
      } else if (num == Motor.M2)
      {
        pwm = 13;
        in2 = 12;
        in1 = 11;
      } else if (num == Motor.M3)
      {
        pwm = 2;
        in2 = 3;
        in1 = 4;
      } else if (num == Motor.M4)
      {
        pwm = 7;
        in2 = 6;
        in1 = 5;
      } else
      {
        throw new RuntimeException("Bad MotorHAT Motor # " + num);
      }
      this.PWMpin = pwm;
      this.IN1pin = in1;
      this.IN2pin = in2;
      if ("true".equals(System.getProperty("hat.debug", "false")))
      {
        System.out.println("DCMotor:" + num +
                           " PWM pin:" + this.PWMpin +
                           ", IN1 pin:" + this.IN1pin +
                           ", IN2 pin:" + this.IN2pin);
      }
    }
                      
    public void run(ServoCommand command) throws IOException
    {
      if (this.mh == null)
        return;
      
      if (command == ServoCommand.FORWARD)
      {
        this.mh.setPin(this.IN2pin, 0);
        this.mh.setPin(this.IN1pin, 1);
      }
      else if (command == ServoCommand.BACKWARD)
      {
        this.mh.setPin(this.IN1pin, 0);
        this.mh.setPin(this.IN2pin, 1);
      }
      else if (command == ServoCommand.RELEASE)
      {
        this.mh.setPin(this.IN1pin, 0);
        this.mh.setPin(this.IN2pin, 0);
      }
    }
    
    public void setSpeed(int speed) throws IOException
    {
      if (speed < 0)
        speed = 0;
      if (speed > 255)
        speed = 255;
      this.mh.pwm.setPWM(this.PWMpin, (short)0, (short)(speed*16));
    }
  }

  public static class AdafruitStepperMotor
  {
    private AdafruitMotorHAT mc;
    private int MICROSTEPS = 8;
    private int[] MICROSTEP_CURVE = new int[] {0, 50, 98, 142, 180, 212, 236, 250, 255};

    private int PWMA = 8;
    private int AIN2 = 9;
    private int AIN1 = 10;
    private int PWMB = 13;
    private int BIN2 = 12;
    private int BIN1 = 11;

    private int revsteps;
    private int motornum;
    private double sec_per_step = 0.1;
    private int steppingcounter = 0;
    private int currentstep = 0;

    // MICROSTEPS = 16
    // a sinusoidal curve NOT LINEAR!
    // MICROSTEP_CURVE = [0, 25, 50, 74, 98, 120, 141, 162, 180, 197, 212, 225, 236, 244, 250, 253, 255]
  
    public AdafruitStepperMotor(AdafruitMotorHAT controller, int num)
    {
      this(controller, num, 200);
    }
    
    public AdafruitStepperMotor(AdafruitMotorHAT controller, int num, int steps)
    {
      this.mc = controller;
      this.revsteps = steps;
      this.motornum = num;
      this.sec_per_step = 0.1;
      this.steppingcounter = 0;
      this.currentstep = 0;

      if ((num - 1) == 0)
      {
        this.PWMA = 8;
        this.AIN2 = 9;
        this.AIN1 = 10;
        this.PWMB = 13;
        this.BIN2 = 12;
        this.BIN1 = 11;
      }
      else if ((num - 1) == 1)
      {
        this.PWMA = 2;
        this.AIN2 = 3;
        this.AIN1 = 4;
        this.PWMB = 7;
        this.BIN2 = 6;
        this.BIN1 = 5;
      }
      else
      {
        throw new RuntimeException("MotorHAT Stepper must be between 1 and 2 inclusive");
      }
    }   
    
    public void setSpeed(double rpm)
    {
      this.sec_per_step = 60.0 / (this.revsteps * rpm);
      this.steppingcounter = 0;
    }
                  
    public int oneStep(ServoCommand dir, Style style) throws IOException
    {
      int pwm_a = 255,
          pwm_b = 255;

      // first determine what sort of stepping procedure we're up to
      if (style == Style.SINGLE)
      {
        if ((this.currentstep/(this.MICROSTEPS/2)) % 2 == 1)
        {
          // we're at an odd step, weird
          if (dir == ServoCommand.FORWARD)
            this.currentstep += this.MICROSTEPS / 2;
          else
            this.currentstep -= this.MICROSTEPS / 2;
        }
      }
      else
      {
        // go to next even step
        if (dir == ServoCommand.FORWARD)
          this.currentstep += this.MICROSTEPS;
        else
          this.currentstep -= this.MICROSTEPS;
      }
      if (style == Style.DOUBLE)
      {
        if (this.currentstep/(this.MICROSTEPS/2) % 2 == 0)
        {
          // we're at an even step, weird
          if (dir == ServoCommand.FORWARD)
            this.currentstep += this.MICROSTEPS/2;
          else
            this.currentstep -= this.MICROSTEPS/2;
        }
        else
        {
          // go to next odd step
          if (dir == ServoCommand.FORWARD)
            this.currentstep += this.MICROSTEPS;
          else
            this.currentstep -= this.MICROSTEPS;
        }
      }
      if (style == Style.INTERLEAVE)
      {
        if (dir == ServoCommand.FORWARD)
          this.currentstep += this.MICROSTEPS/2;
        else
          this.currentstep -= this.MICROSTEPS/2;
      }
      if (style == Style.MICROSTEP)
      {
        if (dir == ServoCommand.FORWARD)
          this.currentstep += 1;
        else
          this.currentstep -= 1;
      }
      // go to next 'step' and wrap around
      this.currentstep += this.MICROSTEPS * 4;
      this.currentstep %= this.MICROSTEPS * 4;
  
      pwm_a = 0;
      pwm_b = 0;
      if (this.currentstep >= 0 && this.currentstep < this.MICROSTEPS)
      {
        pwm_a = this.MICROSTEP_CURVE[this.MICROSTEPS - this.currentstep];
        pwm_b = this.MICROSTEP_CURVE[this.currentstep];
      }
      else if (this.currentstep >= this.MICROSTEPS && this.currentstep < this.MICROSTEPS*2)
      {
        pwm_a = this.MICROSTEP_CURVE[this.currentstep - this.MICROSTEPS];
        pwm_b = this.MICROSTEP_CURVE[this.MICROSTEPS*2 - this.currentstep];
      }
      else if (this.currentstep >= this.MICROSTEPS*2 && this.currentstep < this.MICROSTEPS*3)
      {
        pwm_a = this.MICROSTEP_CURVE[this.MICROSTEPS*3 - this.currentstep];
        pwm_b = this.MICROSTEP_CURVE[this.currentstep - this.MICROSTEPS*2];
      }
      else if (this.currentstep >= this.MICROSTEPS*3 && this.currentstep < this.MICROSTEPS*4)
      {
        pwm_a = this.MICROSTEP_CURVE[this.currentstep - this.MICROSTEPS*3];
        pwm_b = this.MICROSTEP_CURVE[this.MICROSTEPS*4 - this.currentstep];
      }
  
      // go to next 'step' and wrap around
      this.currentstep += this.MICROSTEPS * 4;
      this.currentstep %= this.MICROSTEPS * 4;
  
      // only really used for microstepping, otherwise always on!
      this.mc.pwm.setPWM(this.PWMA, (short)0, (short)(pwm_a*16));
      this.mc.pwm.setPWM(this.PWMB, (short)0, (short)(pwm_b*16));
  
      // set up coil energizing!
      int coils[] = new int[] {0, 0, 0, 0};
  
      if (style == Style.MICROSTEP)
      {
        if (this.currentstep >= 0 && this.currentstep < this.MICROSTEPS)
          coils = new int[] {1, 1, 0, 0};
        else if (this.currentstep >= this.MICROSTEPS && this.currentstep < this.MICROSTEPS*2)
          coils = new int[] {0, 1, 1, 0};
        else if (this.currentstep >= this.MICROSTEPS*2 && this.currentstep < this.MICROSTEPS*3)
          coils = new int[] {0, 0, 1, 1};
        else if (this.currentstep >= this.MICROSTEPS*3 && this.currentstep < this.MICROSTEPS*4)
          coils = new int[] {1, 0, 0, 1};
      }
      else
      {
        int[][] step2coils = new int[][] {   
          {1, 0, 0, 0}, 
          {1, 1, 0, 0},
          {0, 1, 0, 0},
          {0, 1, 1, 0},
          {0, 0, 1, 0},
          {0, 0, 1, 1},
          {0, 0, 0, 1},
          {1, 0, 0, 1} };
        coils = step2coils[this.currentstep/(this.MICROSTEPS/2)];
      } 
      // print "coils state = " + str(coils)
      this.mc.setPin(this.AIN2, coils[0]);
      this.mc.setPin(this.BIN1, coils[1]);
      this.mc.setPin(this.AIN1, coils[2]);
      this.mc.setPin(this.BIN2, coils[3]);
  
      return this.currentstep;
    }
                      
    public void step(int steps, ServoCommand direction, Style stepstyle) throws IOException
    {
      double s_per_s = this.sec_per_step;
      int lateststep = 0;
      
      if (stepstyle == Style.INTERLEAVE)
        s_per_s = s_per_s / 2.0;
      if (stepstyle == Style.MICROSTEP)
      {
        s_per_s /= this.MICROSTEPS;
        steps *= this.MICROSTEPS;
      }
      System.out.println(s_per_s + " sec per step");
  
      for (int s=0; s<steps; s++)
      {
        lateststep = this.oneStep(direction, stepstyle);
        delay((long)(s_per_s * 1000));
      }
      if (stepstyle == Style.MICROSTEP)
      {
        // this is an edge case, if we are in between full steps, lets just keep going
        // so we end on a full step
        while (lateststep != 0 && lateststep != this.MICROSTEPS)
        {
          lateststep = this.oneStep(direction, stepstyle);
          delay((long)(s_per_s * 1000));
        }
      }
    }
  }
            
  private static void delay(long t)
  {
    try { Thread.sleep(t); } catch (InterruptedException ie) {}  
  }
}
