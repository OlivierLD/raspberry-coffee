package i2c.motor.adafruitmotorhat;

import com.pi4j.io.i2c.I2CFactory;
import i2c.pwm.PWM;

import java.io.IOException;

import static utils.TimeUtil.delay;

/**
 * For https://www.adafruit.com/product/2348
 * Adapted from the python code at https://github.com/adafruit/Adafruit-Motor-HAT-Python-Library.git
 * WIP.
 */
public class AdafruitMotorHAT {
	public enum Style {
		SINGLE, DOUBLE, INTERLEAVE, MICROSTEP
	}

	public enum Motor {
		M1, M2, M3, M4
	}

	public enum MotorCommand {
		FORWARD, BACKWARD, BRAKE, RELEASE
	}

	public final static int STEPPER_1 = 1;
	public final static int STEPPER_2 = 2;

	private final static int HAT_ADDR     = 0x60;
	private final static int DEFAULT_FREQ = 1_600;

	private AdafruitDCMotor motors[];
	private AdafruitStepperMotor steppers[];
	private PWM pwm;

	public AdafruitMotorHAT() throws I2CFactory.UnsupportedBusNumberException {
		this(HAT_ADDR, DEFAULT_FREQ);
	}

	public AdafruitMotorHAT(int nbSteps) throws I2CFactory.UnsupportedBusNumberException {
		this(HAT_ADDR, DEFAULT_FREQ, nbSteps);
	}

	public AdafruitMotorHAT(int addr, int freq) throws I2CFactory.UnsupportedBusNumberException {
		this(addr, freq, AdafruitStepperMotor.DEFAULT_NB_STEPS);
	}

	public AdafruitMotorHAT(int addr, int freq, int nbSteps) throws I2CFactory.UnsupportedBusNumberException {
		motors = new AdafruitDCMotor[4];
		int i = 0;
		for (Motor motor : Motor.values()) {
			motors[i++] = new AdafruitDCMotor(this, motor);
		}
		steppers = new AdafruitStepperMotor[2];
		steppers[0] = new AdafruitStepperMotor(this, STEPPER_1, nbSteps);
		steppers[1] = new AdafruitStepperMotor(this, STEPPER_2, nbSteps);
		pwm = new PWM(addr);
		try {
			pwm.setPWMFreq(freq);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void setPin(int pin, int value) throws IOException {
		if (pin < 0 || pin > 15) {
			throw new RuntimeException("PWM pin must be in [0..15], found " + pin);
		}
		if (value != 0 && value != 1) {
			throw new RuntimeException("Pin value must be 0 or 1, found " + value);
		}
		if (value == 0) {
			this.pwm.setPWM(pin, (short) 0, (short) 4_096);
		}
		if (value == 1) {
			this.pwm.setPWM(pin, (short) 4_096, (short) 0);
		}
	}

	public AdafruitStepperMotor getStepper(int num) {
		if (num != STEPPER_1 && num != STEPPER_2) {
			throw new RuntimeException("MotorHAT Stepper must be 1 or 2.");
		}
		return steppers[num - 1];
	}

	public AdafruitDCMotor getMotor(Motor mn) {
		AdafruitDCMotor motor = null;
		for (AdafruitDCMotor m : motors) {
			if (m.motorNum == mn) {
				motor = m;
				if ("true".equals(System.getProperty("hat.debug", "false"))) {
					System.out.println("getMotor (DC):" + mn);
				}
				break;
			}
		}
		return motor;
	}

	public static class AdafruitDCMotor {
		private AdafruitMotorHAT mh;
		private Motor motorNum;
		private int pwm = 0, in1 = 0, in2 = 0;
		private int PWMpin = 0, IN1pin = 0, IN2pin = 0;

		public AdafruitDCMotor(AdafruitMotorHAT controller, Motor mn) {
			this.mh = controller;
			this.motorNum = mn;
			if (mn == Motor.M1) {
				pwm = 8;
				in2 = 9;
				in1 = 10;
			} else if (mn == Motor.M2) {
				pwm = 13;
				in2 = 12;
				in1 = 11;
			} else if (mn == Motor.M3) {
				pwm = 2;
				in2 = 3;
				in1 = 4;
			} else if (mn == Motor.M4) {
				pwm = 7;
				in2 = 6;
				in1 = 5;
			} else {
				throw new RuntimeException("Bad MotorHAT Motor # " + mn);
			}
			this.PWMpin = pwm;
			this.IN1pin = in1;
			this.IN2pin = in2;
			if ("true".equals(System.getProperty("hat.debug", "false"))) {
				System.out.println("DCMotor:" + mn +
								" PWM pin:" + this.PWMpin +
								", IN1 pin:" + this.IN1pin +
								", IN2 pin:" + this.IN2pin);
			}
		}

		public void run(MotorCommand command) throws IOException {
			if (this.mh == null) {
				return;
			}
			if (command == MotorCommand.FORWARD) {
				this.mh.setPin(this.IN2pin, 0);
				this.mh.setPin(this.IN1pin, 1);
			} else if (command == MotorCommand.BACKWARD) {
				this.mh.setPin(this.IN1pin, 0);
				this.mh.setPin(this.IN2pin, 1);
			} else if (command == MotorCommand.RELEASE) {
				this.mh.setPin(this.IN1pin, 0);
				this.mh.setPin(this.IN2pin, 0);
			}
		}

		public void setSpeed(int speed) throws IOException {
			if (speed < 0) {
				speed = 0;
			}
			if (speed > 255) {
				speed = 255;
			}
			this.mh.pwm.setPWM(this.PWMpin, (short) 0, (short) (speed * 16));
		}
	}

	public static class AdafruitStepperMotor {
		public final static int PORT_M1_M2 = 1; // Port #1
		public final static int PORT_M3_M4 = 2; // Port #2

		private double rpm = 30; // Default

		private AdafruitMotorHAT mc;
		private int MICROSTEPS = 8;
		private int[] MICROSTEP_CURVE = new int[] {
				0, 50, 98, 142, 180, 212, 236, 250, 255
		};
//		private int MICROSTEPS = 16;
//		// a sinusoidal curve NOT LINEAR!
//		private int[] MICROSTEP_CURVE = new int[] {
//				0, 25, 50, 74, 98, 120, 141, 162, 180, 197, 212, 225, 236, 244, 250, 253, 255
//		};

		public final static int DEFAULT_NB_STEPS = 200; // between 35 & 200. Nb steps per revolution.

		private int PWMA = 8;
		private int AIN2 = 9;
		private int AIN1 = 10;
		private int PWMB = 13;
		private int BIN2 = 12;
		private int BIN1 = 11;

		private int revSteps;
		private int motorNum;
		private double secPerStep = 0.1;
		private int steppingCounter = 0;
		private int currentStep = 0;

		public AdafruitStepperMotor(AdafruitMotorHAT controller, int num) {
			this(controller, num, DEFAULT_NB_STEPS);
		}

		public AdafruitStepperMotor(AdafruitMotorHAT controller, int num, int steps) {
			this.mc = controller;
			if (steps < 35 || steps > 200) {
				throw new RuntimeException(String.format("StepsPerRevolution must be in [35..200], found %d", steps));
			}
			this.revSteps = steps;
			if (num != PORT_M1_M2 && num != PORT_M3_M4) {
				throw new RuntimeException(String.format("Motor Num can only be 1 or 2, found %d.", num));
			}
			this.motorNum = num;
			this.secPerStep = 0.1;
			this.steppingCounter = 0;
			this.currentStep = 0;

			switch (num - 1) {
				case 0: // num == STEPPER_1
					this.PWMA = 8;
					this.AIN2 = 9;
					this.AIN1 = 10;
					this.PWMB = 13;
					this.BIN2 = 12;
					this.BIN1 = 11;
					break;
				case 1: // num == STEPPER_2
					this.PWMA = 2;
					this.AIN2 = 3;
					this.AIN1 = 4;
					this.PWMB = 7;
					this.BIN2 = 6;
					this.BIN1 = 5;
					break;
				default:
					throw new RuntimeException("MotorHAT Stepper must be 1 or 2");
			}
		}

		public void setSpeed(double rpm) {
			this.rpm = rpm;
			this.secPerStep = 60.0 / (this.revSteps * rpm);
			this.steppingCounter = 0;
		}

		public int getStepPerRev() {
			return this.revSteps;
		}

		public int getMotorNum() {
			return this.motorNum;
		}

		public double getSecPerStep() {
			return this.secPerStep;
		}

		public double getRPM() {
			return this.rpm;
		}

		public int oneStep(MotorCommand dir, Style style) throws IOException {
			int pwmA = 255,
					pwmB = 255;

			// first determine what sort of stepping procedure we're up to
			if (style == Style.SINGLE) {
				if ((int)(this.currentStep / (int)(this.MICROSTEPS / 2)) % 2 == 1) {
					// we're at an odd step, weird
					if (dir == MotorCommand.FORWARD) {
						this.currentStep += ((int)(this.MICROSTEPS / 2));
					} else {
						this.currentStep -= ((int)(this.MICROSTEPS / 2));
					}
				} else {
					// go to next even step
					if (dir == MotorCommand.FORWARD) {
						this.currentStep += this.MICROSTEPS;
					} else {
						this.currentStep -= this.MICROSTEPS;
					}
				}
			} else if (style == Style.DOUBLE) {
				if ((int)(this.currentStep / (int)(this.MICROSTEPS / 2)) % 2 == 0) {
					// we're at an even step, weird
					if (dir == MotorCommand.FORWARD) {
						this.currentStep += ((int)(this.MICROSTEPS / 2));
					} else {
						this.currentStep -= ((int)(this.MICROSTEPS / 2));
					}
				} else {
					// go to next odd step
					if (dir == MotorCommand.FORWARD) {
						this.currentStep += this.MICROSTEPS;
					} else {
						this.currentStep -= this.MICROSTEPS;
					}
				}
			} else if (style == Style.INTERLEAVE) {
				if (dir == MotorCommand.FORWARD) {
					this.currentStep += ((int)(this.MICROSTEPS / 2));
				} else {
					this.currentStep -= ((int)(this.MICROSTEPS / 2));
				}
			} else if (style == Style.MICROSTEP) {
				if (dir == MotorCommand.FORWARD) {
					this.currentStep += 1;
				} else {
					this.currentStep -= 1;
					// go to next 'step' and wrap around
					this.currentStep += this.MICROSTEPS * 4;
					this.currentStep %= this.MICROSTEPS * 4;
				}
				pwmA = 0;
				pwmB = 0;
				if (this.currentStep >= 0 && this.currentStep < this.MICROSTEPS) {
					pwmA = this.MICROSTEP_CURVE[this.MICROSTEPS - this.currentStep];
					pwmB = this.MICROSTEP_CURVE[this.currentStep];
				} else if (this.currentStep >= this.MICROSTEPS && this.currentStep < this.MICROSTEPS * 2) {
					pwmA = this.MICROSTEP_CURVE[this.currentStep - this.MICROSTEPS];
					pwmB = this.MICROSTEP_CURVE[this.MICROSTEPS * 2 - this.currentStep];
				} else if (this.currentStep >= this.MICROSTEPS * 2 && this.currentStep < this.MICROSTEPS * 3) {
					pwmA = this.MICROSTEP_CURVE[this.MICROSTEPS * 3 - this.currentStep];
					pwmB = this.MICROSTEP_CURVE[this.currentStep - this.MICROSTEPS * 2];
				} else if (this.currentStep >= this.MICROSTEPS * 3 && this.currentStep < this.MICROSTEPS * 4) {
					pwmA = this.MICROSTEP_CURVE[this.currentStep - this.MICROSTEPS * 3];
					pwmB = this.MICROSTEP_CURVE[this.MICROSTEPS * 4 - this.currentStep];
				}
			}

			// go to next 'step' and wrap around
			this.currentStep += this.MICROSTEPS * 4;
			this.currentStep %= this.MICROSTEPS * 4;

			// only really used for microstepping, otherwise always on.
			this.mc.pwm.setPWM(this.PWMA, (short) 0, (short) (pwmA * 16));
			this.mc.pwm.setPWM(this.PWMB, (short) 0, (short) (pwmB * 16));

			// set up coil energizing.
			int coils[] = new int[]{ 0, 0, 0, 0 };

			if (style == Style.MICROSTEP) {
				if (this.currentStep >= 0 && this.currentStep < this.MICROSTEPS) {
					coils = new int[] { 1, 1, 0, 0 };
				} else if (this.currentStep >= this.MICROSTEPS && this.currentStep < this.MICROSTEPS * 2) {
					coils = new int[] { 0, 1, 1, 0 };
				} else if (this.currentStep >= this.MICROSTEPS * 2 && this.currentStep < this.MICROSTEPS * 3) {
					coils = new int[] { 0, 0, 1, 1 };
				} else if (this.currentStep >= this.MICROSTEPS * 3 && this.currentStep < this.MICROSTEPS * 4) {
					coils = new int[] { 1, 0, 0, 1 };
				}
			} else {
				int[][] step2coils = new int[][] {
						{ 1, 0, 0, 0 },
						{ 1, 1, 0, 0 },
						{ 0, 1, 0, 0 },
						{ 0, 1, 1, 0 },
						{ 0, 0, 1, 0 },
						{ 0, 0, 1, 1 },
						{ 0, 0, 0, 1 },
						{ 1, 0, 0, 1 }
				};
				coils = step2coils[(int)(this.currentStep / (int)(this.MICROSTEPS / 2))];
			}
			// print "coils state = " + str(coils)
			this.mc.setPin(this.AIN2, coils[0]);
			this.mc.setPin(this.BIN1, coils[1]);
			this.mc.setPin(this.AIN1, coils[2]);
			this.mc.setPin(this.BIN2, coils[3]);

			return this.currentStep;
		}

		public void step(int steps, MotorCommand direction, Style stepStyle) throws IOException {
			double sPerS = this.secPerStep;
			int latestStep = 0;

			if (stepStyle == Style.INTERLEAVE) {
				sPerS = sPerS / 2.0;
			} else if (stepStyle == Style.MICROSTEP) {
				sPerS /= this.MICROSTEPS;
				steps *= this.MICROSTEPS;
			}
			System.out.println(sPerS + " sec per step");

			for (int s = 0; s < steps; s++) {
				latestStep = this.oneStep(direction, stepStyle);
				delay((long) (sPerS * 1_000));
			}
			if (stepStyle == Style.MICROSTEP) {
				// this is an edge case, if we are in between full steps, lets just keep going
				// so we end on a full step
				while (latestStep != 0 && latestStep != this.MICROSTEPS) {
					latestStep = this.oneStep(direction, stepStyle);
					delay((long) (sPerS * 1_000));
				}
			}
		}
	}
}
