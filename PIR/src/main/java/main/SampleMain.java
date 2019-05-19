package main;

import com.pi4j.io.gpio.RaspiPin;
import pir.MotionDetectionInterface;
import pir.MotionDetector;

public class SampleMain {
	public static void main(String... args) {
		final MotionDetector md = new MotionDetector(RaspiPin.GPIO_05, () -> System.out.println("Something is moving!!"));
		final Thread coreThread = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("\nUser interrupted.");
				synchronized (coreThread) {
					coreThread.notify();
				}
			}
		});

		System.out.println("...On watch.");
		try {
			synchronized (coreThread) {
				coreThread.wait();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		md.shutdown();
		System.out.println("Done.");
	}
}
