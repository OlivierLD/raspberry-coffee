package oliv.misc;

public class RunnablesAndThreads {

	public static void main(String... args) {
		Runnable runnable = () -> {
			System.out.println("Akeu coucou!");
			try {
				Thread.sleep(1_000L);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println("Runnable done");
		};

		System.out.println("==================");
		System.out.println("Result should be:");
		System.out.println("- Runnable done");
		System.out.println("- Main done");
		System.out.println("==================");

		runnable.run();
		System.out.println("Main done - 1");

		Thread thread = new Thread(runnable);

		System.out.println("==================");
		System.out.println("Result should be:");
		System.out.println("- Main done");
		System.out.println("- Runnable done");
		System.out.println("==================");

		thread.start();
		System.out.println("Main done - 2");

		System.out.println("== Program completed ==");

	}

}
