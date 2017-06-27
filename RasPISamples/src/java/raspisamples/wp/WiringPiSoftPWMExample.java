package raspisamples.wp;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

/*
 * PWM with WiringPi
 */
public class WiringPiSoftPWMExample {

	private static boolean go = true;

	public static void main(String[] args)
					throws InterruptedException {
		// initialize wiringPi library
		com.pi4j.wiringpi.Gpio.wiringPiSetup();
		int pinAddress = RaspiPin.GPIO_01.getAddress();
		// create soft-pwm pins (min=0 ; max=100)
//  SoftPwm.softPwmCreate(1, 0, 100); 
		SoftPwm.softPwmCreate(pinAddress, 0, 100);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			try { Thread.sleep(1_000L); } catch (Exception ignore) {}
		}));

		// continuous loop
	//while (go) {
			for (int idx = 0; idx < 5; idx++) {
				System.out.println(">> 0");
				// fade LED to fully ON
				for (int i = 0; i <= 100; i++) {
					SoftPwm.softPwmWrite(pinAddress, i);
					Thread.sleep(10);
				}
				System.out.println(">> 100");
				// fade LED to fully OFF
				for (int i = 100; i >= 0; i--) {
					SoftPwm.softPwmWrite(pinAddress, i);
					Thread.sleep(10);
				}
				System.out.println(">> 0");
			}
	//}
	}
}
