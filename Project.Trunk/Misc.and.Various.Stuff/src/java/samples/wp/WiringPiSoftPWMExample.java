package samples.wp;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

import static utils.StaticUtil.userInput;

/*
 * PWM with WiringPi. Works with a led, or with a standard servo.
 */
public class WiringPiSoftPWMExample {
	private static boolean go = true;

	public static void main(String... args)
					throws InterruptedException {
		// initialize wiringPi library
		int ret = Gpio.wiringPiSetup();
		int pinAddress = RaspiPin.GPIO_01.getAddress();
		// create soft-pwm pins (min=0 ; max=100)
		SoftPwm.softPwmCreate(pinAddress, 0, 100);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			try { Thread.sleep(1_000L); } catch (Exception ignore) {}
		}));

		// continuous loop
	//while (go) {
			for (int idx = 0; idx < 3; idx++) {
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
		// Interactive?
		System.out.println("Enter [Q] at the prompt to quit.");
		go = true;
		while (go) {
			String s = userInput("PWM Value [0..100] > ");
			if ("Q".equalsIgnoreCase(s.trim())) {
				go = false;
			} else {
				try {
					int pwm = Integer.parseInt(s);
					SoftPwm.softPwmWrite(pinAddress, pwm);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
		// Done!
	}
}
