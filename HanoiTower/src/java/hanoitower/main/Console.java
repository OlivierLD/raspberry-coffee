package hanoitower.main;

import hanoitower.BackendAlgorithm;
import hanoitower.events.HanoiContext;
import hanoitower.events.HanoiEventListener;

/**
 * Use -Dbackend.verbose=true for verbose.
 */
public class Console {

	public Console() {
		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {
      public void moveRequired(String from, String to) {
        System.out.println((new StringBuilder()).append("Moving from ").append(from).append(" to ").append(to).toString());
      }
    });
	}

	public static void main(String... args) {
		int nb = 5;
		if (args.length > 0) {
			try {
				nb = Integer.parseInt(args[0]);
			} catch (Exception ex) {
				System.err.println(ex.toString());
			}
		}
		new Console();
		System.out.println(String.format("Anticipating %d moves...", (int)(Math.pow(2, nb) - 1)));
		System.out.println("Moving the tower from B to A");
		BackendAlgorithm.move(nb, "B", "A", "C");
	}
}
