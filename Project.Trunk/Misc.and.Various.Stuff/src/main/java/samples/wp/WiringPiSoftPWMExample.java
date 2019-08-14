package samples.wp;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

import static utils.StaticUtil.userInput;

/*
 * PWM with WiringPi.
 * Works with a led, or with a servo.
 *
 * For a standard servo:
 * 0..25
 * 15 is the middle...
 *
 */
public class WiringPiSoftPWMExample {
	private static boolean go = true;

	public static void main(String... args)
					throws InterruptedException {
		// initialize wiringPi library
		int ret = Gpio.wiringPiSetup();
		System.out.println(String.format("Gpio.wiringPiSetup() returned %d", ret));
		int pinAddress = RaspiPin.GPIO_01.getAddress();
		System.out.println(String.format("RaspiPin.GPIO_01.getAddress()=%d", pinAddress));
		// create soft-pwm pins (min=0 ; max=100)
		int what = SoftPwm.softPwmCreate(pinAddress, 0, 100);
		System.out.println(String.format("SoftPwm.softPwmCreate() returned %d", what));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			try { Thread.sleep(1_000L); } catch (Exception ignore) {}
		}, "Shutdown Hook"));

		for (int idx = 0; idx < 3; idx++) {
			System.out.println(">> 0");
			// fade LED to fully ON
			for (int i = 0; i <= 100; i++) {
				SoftPwm.softPwmWrite(pinAddress, i);
				Thread.sleep(10);
			}
			Thread.sleep(1_000);
			System.out.println(">> 100");
			// fade LED to fully OFF
			for (int i = 100; i >= 0; i--) {
				SoftPwm.softPwmWrite(pinAddress, i);
				Thread.sleep(10);
			}
			System.out.println(">> 0");
			Thread.sleep(1_000);
		}

		// Interactive?
		int previousValue = 0;
		System.out.println("Enter [Q] at the prompt to quit.");
		go = true;
		while (go) {
			String s = userInput("PWM Value [0..100] > ");
			if ("Q".equalsIgnoreCase(s.trim())) {
				go = false;
			} else {
				try {
					int pwm = Integer.parseInt(s);
					System.out.println(String.format(">> Setting servo to %d", pwm));
					if (pwm > previousValue) {
						for (int x=previousValue; x<=pwm; x++) {
							SoftPwm.softPwmWrite(pinAddress, x);
							Thread.sleep(10);
						}
					} else if (pwm < previousValue) {
						for (int x=previousValue; x>=pwm; x--) {
							SoftPwm.softPwmWrite(pinAddress, x);
							Thread.sleep(10);
						}
					}
					previousValue = pwm;
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
		// Done!
	}
}
