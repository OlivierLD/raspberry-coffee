package raspisamples.wp;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

/*
 * PWM with WiringPi
 */
public class WiringPiSoftPWMExample {

	private static boolean go = true;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])


	public static void main(String[] args)
					throws InterruptedException {
		// initialize wiringPi library
		com.pi4j.wiringpi.Gpio.wiringPiSetup();
		int pinAddress = RaspiPin.GPIO_01.getAddress();
		// create soft-pwm pins (min=0 ; max=100)
//  SoftPwm.softPwmCreate(1, 0, 100); 
		SoftPwm.softPwmCreate(pinAddress, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX); // was 0, 100

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			try { Thread.sleep(1_000L); } catch (Exception ignore) {}
		}));

		// continuous loop
		while (go) {
	//	for (int idx = 0; idx < 5; idx++) {
				// fade LED to fully ON
				for (int i = DEFAULT_SERVO_MIN; i <= DEFAULT_SERVO_MAX; i++) {
					SoftPwm.softPwmWrite(pinAddress, i);
					Thread.sleep(10);
				}
				// fade LED to fully OFF
				for (int i = DEFAULT_SERVO_MAX; i >= DEFAULT_SERVO_MIN; i--) {
					SoftPwm.softPwmWrite(pinAddress, i);
					Thread.sleep(10);
				}
	//	}
		}
	}
}
