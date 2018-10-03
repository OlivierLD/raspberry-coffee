package samples.wp;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

/*
 * PWM with WiringPi
 */
public class WiringPiSoftPWM3ColorLed {

	public static void main(String... args)
			throws InterruptedException {
		// initialize wiringPi library
		Gpio.wiringPiSetup();

		int pinAddress_00 = RaspiPin.GPIO_00.getAddress();
		int pinAddress_01 = RaspiPin.GPIO_01.getAddress();
		int pinAddress_02 = RaspiPin.GPIO_02.getAddress();
		// create soft-pwm pins (min=0 ; max=100)

		SoftPwm.softPwmCreate(pinAddress_00, 0, 100);
		SoftPwm.softPwmCreate(pinAddress_01, 0, 100);
		SoftPwm.softPwmCreate(pinAddress_02, 0, 100);

		// continuous loop
//  while (true)
		{
			System.out.println("One");
			// fade LED to fully ON
			for (int i = 0; i <= 100; i++) {
				SoftPwm.softPwmWrite(pinAddress_00, i);
				Thread.sleep(5);
			}
			System.out.println("Two");
			// fade LED to fully OFF
			for (int i = 100; i >= 0; i--) {
				SoftPwm.softPwmWrite(pinAddress_00, i);
				Thread.sleep(5);
			}
			System.out.println("Three");
			// fade LED to fully ON
			for (int i = 0; i <= 100; i++) {
				SoftPwm.softPwmWrite(pinAddress_01, i);
				Thread.sleep(5);
			}
			System.out.println("Four");
			// fade LED to fully OFF
			for (int i = 100; i >= 0; i--) {
				SoftPwm.softPwmWrite(pinAddress_01, i);
				Thread.sleep(5);
			}
			System.out.println("Five");
			// fade LED to fully ON
			for (int i = 0; i <= 100; i++) {
				SoftPwm.softPwmWrite(pinAddress_02, i);
				Thread.sleep(5);
			}
			System.out.println("Six");
			// fade LED to fully OFF
			for (int i = 100; i >= 0; i--) {
				SoftPwm.softPwmWrite(pinAddress_02, i);
				Thread.sleep(5);
			}
		}
		System.out.println("Seven");
		// All spectrum
		for (int a = 0; a <= 100; a++) {
			SoftPwm.softPwmWrite(pinAddress_00, a);
//    Thread.sleep(5);
			for (int b = 0; b <= 100; b++) {
				SoftPwm.softPwmWrite(pinAddress_01, b);
//      Thread.sleep(5);
				for (int c = 0; c <= 100; c++) {
					SoftPwm.softPwmWrite(pinAddress_02, c);
					Thread.sleep(1);
				}
				for (int c = 100; c >= 0; c--) {
					SoftPwm.softPwmWrite(pinAddress_02, c);
					Thread.sleep(1);
				}
			}
			for (int b = 100; b >= 0; b--) {
				SoftPwm.softPwmWrite(pinAddress_01, b);
//      Thread.sleep(5);
				for (int c = 0; c <= 100; c++) {
					SoftPwm.softPwmWrite(pinAddress_02, c);
					Thread.sleep(1);
				}
				for (int c = 100; c >= 0; c--) {
					SoftPwm.softPwmWrite(pinAddress_02, c);
					Thread.sleep(1);
				}
			}
		}
		System.out.println("Eight");
		for (int a = 100; a >= 0; a--) {
			SoftPwm.softPwmWrite(pinAddress_00, a);
//    Thread.sleep(5);
			for (int b = 0; b <= 100; b++) {
				SoftPwm.softPwmWrite(pinAddress_01, b);
//      Thread.sleep(5);
				for (int c = 0; c <= 100; c++) {
					SoftPwm.softPwmWrite(pinAddress_02, c);
					Thread.sleep(1);
				}
				for (int c = 100; c >= 0; c--) {
					SoftPwm.softPwmWrite(pinAddress_02, c);
					Thread.sleep(1);
				}
			}
			for (int b = 100; b >= 0; b--) {
				SoftPwm.softPwmWrite(pinAddress_01, b);
//      Thread.sleep(5);
				for (int c = 0; c <= 100; c++) {
					SoftPwm.softPwmWrite(pinAddress_02, c);
					Thread.sleep(1);
				}
				for (int c = 100; c >= 0; c--) {
					SoftPwm.softPwmWrite(pinAddress_02, c);
					Thread.sleep(1);
				}
			}
		}
		System.out.println("Done");
	}
}
