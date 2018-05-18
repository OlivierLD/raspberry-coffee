package mearm;

import com.pi4j.io.i2c.I2CFactory;
import hanoitower.BackendAlgorithm;
import hanoitower.events.HanoiContext;
import hanoitower.events.HanoiEventListener;
import i2c.samples.mearm.MeArmPilot;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static i2c.samples.mearm.MeArmPilot.DEFAULT_BOTTOM_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_CLAW_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_LEFT_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_RIGHT_SERVO_CHANNEL;

public class HanoiPilot {

	private static int nbDisc = 4;
	private static int nbMove = 0;

	private static int
			left = DEFAULT_LEFT_SERVO_CHANNEL,      // up and down
			right = DEFAULT_RIGHT_SERVO_CHANNEL,    // back and forth
			bottom = DEFAULT_BOTTOM_SERVO_CHANNEL,  // left and right
			claw = DEFAULT_CLAW_SERVO_CHANNEL;      // open and close

	private static HanoiContext.Stand hanoiStand = null;
	private static Thread me;

	private static synchronized void startSolving() {
		System.out.println(String.format("Starting solving, anticipating %d moves.", (int)(Math.pow(2, nbDisc) - 1)));
		nbMove = 0;
		BackendAlgorithm.move(nbDisc, "A", "C", "B");
		System.out.println((new StringBuilder())
				.append("Finished in ")
				.append(nbMove)
				.append(" moves.").toString());
		HanoiContext.getInstance().fireComputationCompleted();
	}

	private final static String DISCS_PREFIX  = "--discs:";
	private final static String CLAW_PREFIX   = "--claw:";   // Used to open and close the claw
	private final static String LEFT_PREFIX   = "--left:";   // Used to move up and down
	private final static String RIGHT_PREFIX  = "--right:";  // Used to move back and forth
	private final static String BOTTOM_PREFIX = "--bottom:"; // Used to turn left and right

	private final static List<String> RESET = Arrays.asList(
			"SET_PWM:LEFT,   0, 0",
			"SET_PWM:RIGHT,  0, 0",
			"SET_PWM:CLAW,   0, 0",
			"SET_PWM:BOTTOM, 0, 0"
	);

	private static int postALeftRight = -30;
	private static int postBLeftRight =   0;
	private static int postCLeftRight =  30;

	private static int aboveThePosts  =  50;
	private static int postsLevelZero = -100;
	private static int discThickness  =  10;

	private static int clawOpen       = -100;
	private static int clawClosed     =  100;

	private static int getPostLeftRightValue(String post) {
		switch (post) {
			case "A":
				return postALeftRight;
			case "B":
				return postBLeftRight;
			case "C":
				return postCLeftRight;
			default:
				return 0;
		}
	}

	private static int getDiscZCoordinate(int discPos) {
		return postsLevelZero + (discThickness * (discPos - 1) + (discThickness / 2));
	}

	/**
	 *
	 * @param fromPost A, B, or C
	 * @param fromPosOnPost Position to move the disc from, on its current post. 1..nbDiscs. 1 is the bottom disc.
	 * @param toPost A, B, or C
	 * @param toPosOnPost Position to move the disc to, on its new post. 1 is the bottom disc.
	 * @return
	 */
	private static List<String> generateMove(String fromPost, int fromPosOnPost, String toPost, int toPosOnPost) {
		List<String> commands = new ArrayList<>();

		// Disc number on the original stack. 1 is the top (smallest).
		Integer d = ((HanoiContext.Post) hanoiStand.getPosts().get(fromPost)).getDiscAt(fromPosOnPost - 1);

		commands.add(String.format("PRINT: --- Now moving disc #%d from %s(%d) to %s(%d) ---", d, fromPost, fromPosOnPost, toPost, toPosOnPost));

		// TODO The actual move
//	commands.add(String.format("PRINT: Open the CLAW"));
		commands.add(String.format("SLIDE: CLAW, %d", clawOpen));
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Move up above the poles"));
		commands.add(String.format("SLIDE: LEFT, %d", aboveThePosts));
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Rotate above post %s (use SLIDE)", fromPost));
		commands.add(String.format("SLIDE: BOTTOM, %d", getPostLeftRightValue(fromPost)));
		commands.add("WAIT: 250"); // Simulate wait

		commands.add(String.format("PRINT: Come down to disc #%d in position %d", d, fromPosOnPost));
		commands.add(String.format("SLIDE: LEFT, %d", getDiscZCoordinate(fromPosOnPost)));
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Close the CLAW on disc #%d", d));
		commands.add(String.format("SLIDE: CLAW, %d", clawClosed)); // TODO Tweak this
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Move disc #%d UP%%2C above the post %s", d, fromPost));
		commands.add(String.format("SLIDE: LEFT, %d", aboveThePosts));
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Rotate above post %s", toPost));
		commands.add(String.format("SLIDE: BOTTOM, %d", getPostLeftRightValue(toPost)));
		commands.add("WAIT: 250"); // Simulate wait

		commands.add(String.format("PRINT: Bring disc #%d down to position %d on post %s", d, toPosOnPost, toPost));
		commands.add(String.format("SLIDE: LEFT, %d", getDiscZCoordinate(toPosOnPost)));
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Open the CLAW"));
		commands.add(String.format("SLIDE: CLAW, %d", clawOpen));
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Move UP%%2C above post %s", toPost));
		commands.add(String.format("SLIDE: LEFT, %d", aboveThePosts));
		commands.add("WAIT: 1000"); // Simulate wait

		return commands;
	}

	private static void moveDisc(String fromPost, int fromPos, String toPost, int toPos) {
		try {
			MeArmPilot.runMacro(generateMove(fromPost, fromPos, toPost, toPos + 1));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// Simulate wait
		// try { Thread.sleep(1000); } catch (Exception ex) {}
	}

	public static void main(String... args) {

		if (args.length > 0) {
			for (String prm : args) {
				if (prm.startsWith(DISCS_PREFIX)) {
					try {
						nbDisc = Integer.parseInt(prm.substring(DISCS_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (prm.startsWith(CLAW_PREFIX)) {
					try {
						claw = Integer.parseInt(prm.substring(CLAW_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (prm.startsWith(LEFT_PREFIX)) {
					try {
						left = Integer.parseInt(prm.substring(LEFT_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (prm.startsWith(RIGHT_PREFIX)) {
					try {
						right = Integer.parseInt(prm.substring(RIGHT_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (prm.startsWith(BOTTOM_PREFIX)) {
					try {
						bottom = Integer.parseInt(prm.substring(BOTTOM_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.out.println("Duh?");
				}
			}
		}

		MeArmPilot.executeCommand("BOUNDARIES", 0);
		MeArmPilot.executeCommand("HELP", 0);

		System.out.println(String.format("With %d discs, from A to C", nbDisc));

		hanoiStand = new HanoiContext.Stand("A", "B", "C");
		String initialPost = "A";
		hanoiStand.initStand(nbDisc, initialPost);

		try {
			MeArmPilot.initContext(left, claw, bottom, right);
		} catch (I2CFactory.UnsupportedBusNumberException ex) {
			System.out.println("Ooops, no I2C bus...");
		} catch (Exception ioe) {
			System.out.println("Is the PCA9685 connected?");
		}

		HanoiContext.getInstance().fireSetNbDisc(nbDisc);

		int nbCommand = 0;

		MeArmPilot.runMacro(RESET);
		// TODO calibrate here
		try {
			++nbCommand;
			MeArmPilot.executeCommand("PRINT: Will calibrate the MeArm's position here.", nbCommand);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String cmd = "USER_INPUT: Hit [return] when ready to begin. ";

		try {
			++nbCommand;
			MeArmPilot.validateCommand(cmd, nbCommand);
			MeArmPilot.executeCommand(cmd, nbCommand);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {

			public void computationCompleted() {
				System.out.println("Completed!");
				synchronized (me) {
					me.notify();
				}
			}
		});

		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {

			public void moveRequired(String from, String to) {
				nbMove++;

				HanoiContext.Post fromPost = hanoiStand.getPost(from);
				HanoiContext.Post toPost = hanoiStand.getPost(to);
				Integer discToMove = fromPost.getTopDisc();
				Integer otherDisc = toPost.getTopDisc();
				if (otherDisc != null && otherDisc.intValue() != 0 && otherDisc.intValue() < discToMove.intValue()) {
					JOptionPane.showMessageDialog(null, (new StringBuilder()).append("Un-authorized move!!!\n").append(discToMove.toString()).append(" cannot go on top of ").append(otherDisc.toString()).toString(), "Error", 0);
					if ("true".equals(System.getProperty("fail.on.forbidden.move", "false"))) {
						throw new RuntimeException((new StringBuilder()).append("Un-authorized move, ").append(discToMove.toString()).append(" cannot go on top of ").append(otherDisc.toString()).toString());
					}
				}

				System.out.println((new StringBuilder())
						.append("Moving from ")
						.append(String.format("%s pos %d", from, fromPost.getDiscCount()))
						.append(" to ")
						.append(String.format("%s, currently %d disc(s)", to, toPost.getDiscCount())).toString());

				moveDisc(from, fromPost.getDiscCount(), to, toPost.getDiscCount());

				fromPost.removeTopDisc();
				toPost.add(discToMove);
			}

			public void startComputation() {
				System.out.println("Starting resolution");
				Thread solver = new Thread(() -> startSolving());
				solver.start();
			}
		});
		// Process
		HanoiContext.getInstance().fireStartComputation();

		me = Thread.currentThread();
		synchronized (me) {
			try {
				me.wait(); // Released by HanoiEventListener.computationCompleted
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// Test ;)
		MeArmPilot.executeCommand("FORK_SLIDE: BOTTOM, 0, LEFT, 0, RIGHT, 0, CLAW, 0");

		MeArmPilot.runMacro(RESET);
		System.out.println("Done.");
	}
}
