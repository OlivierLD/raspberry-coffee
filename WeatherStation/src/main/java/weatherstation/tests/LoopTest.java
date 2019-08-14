package weatherstation.tests;

public class LoopTest {
	private static boolean go = true;

	public static void main(String... args) {
		final Thread coreThread = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nUser interrupted.");
			go = false;
			synchronized (coreThread) {
				coreThread.notify();
			}
			System.out.println("Unleashed");
		}, "Shutdown Hook"));

		while (go) {
			System.out.println("Blah");
			try {
				synchronized (coreThread) {
					coreThread.wait(5_000L);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Done.\n\n");
	}
}
