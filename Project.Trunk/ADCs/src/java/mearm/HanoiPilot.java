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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static i2c.samples.mearm.MeArmPilot.DEFAULT_BOTTOM_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_CLAW_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_LEFT_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_RIGHT_SERVO_CHANNEL;

public class HanoiPilot {

	private static int nbDisc = 4;
	private static int nbMove = 0;

	private final static String FROM_DISC  = "A";
	private final static String USING_DISC = "B";
	private final static String TO_DISC    = "C";

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
		BackendAlgorithm.move(nbDisc, FROM_DISC, TO_DISC, USING_DISC);
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

	private static final String UP_AND_DOWN    = "LEFT";
	private static final String LEFT_AND_RIGHT = "BOTTOM";
	private static final String BACK_AND_FORTH = "RIGHT";
	private static final String OPEN_AND_CLOSE = "CLAW";

	private static int postALeftRight =  -30;
	private static int postBLeftRight =    0;
	private static int postCLeftRight =   30;

	private static int aboveThePosts  =   50;
	private static int postsLevelZero = -100;
	private static int discThickness  =   30;

	private static int clawOpen       = -100;
	private static int clawClosed     =  100;

	private static int minDiscDiameter = 30;

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

	/**
	 *
	 * @param discPos [1..nbDisc]. 1 is the bottom one.
	 * @return
	 */
	private static int getDiscZCoordinate(int discPos) {
		return postsLevelZero + (discThickness * (discPos - 1) + (discThickness / 2));
	}

	private static List<String> slideServoToValue(String servo, int value) {
		return slideServoToValue(servo, value, 1);
	}
	private static List<String> slideServoToValue(String servo, int value, int step) {
		List<String> commands = new ArrayList<>();
		double from = MeArmPilot.getServoSliderValue(servo);
//	System.out.println(String.format("Starting from %f", from));
		if (value > from) { // ascending
			int _from = (int)Math.round(Math.floor(from));
			for (int i=_from; i<=value; i+=step) {
				String command = String.format("SLIDE: %s, %d", servo, i);
				commands.add(command);
				commands.add("WAIT: 5");
			}
		} else { // Descending
			int _from = (int)Math.round(Math.ceil(from));
			for (int i=_from; i>=value; i-=step) {
				String command = String.format("SLIDE: %s, %d", servo, i);
				commands.add(command);
				commands.add("WAIT: 5");
			}
		}
		return commands;
	}

	/**
	 * May deserve some polishing...
	 *
	 * @param disc [1..nbDisc]. 1 is on top (smallest)
	 * @return
	 */
	private static int getClosedClawPosOnDisc(int disc) {
		int pos = clawClosed - minDiscDiameter;

		int availableInterval = Math.abs(clawOpen - clawClosed) - (2 * minDiscDiameter);
		pos -= ((disc - 1) * (availableInterval / nbDisc));

//	System.out.println(String.format("Disc #%d, claw pos: %d", disc, pos));
		return pos;
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

		commands.add(String.format("PRINT: --- Move #%d. Now moving disc #%d from %s(%d) to %s(%d) ---", nbMove, d, fromPost, fromPosOnPost, toPost, toPosOnPost));

		// The actual move
//	commands.add(String.format("PRINT: Open the CLAW"));
//	commands.add(String.format("SLIDE: CLAW, %d", clawOpen));
		commands = Stream.concat(commands.stream(), slideServoToValue(OPEN_AND_CLOSE, clawOpen).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Move up above the poles"));
//	commands.add(String.format("SLIDE: LEFT, %d", aboveThePosts));
		commands = Stream.concat(commands.stream(), slideServoToValue(UP_AND_DOWN, aboveThePosts).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Rotate above post %s (use SLIDE)", fromPost));
//	commands.add(String.format("SLIDE: BOTTOM, %d", getPostLeftRightValue(fromPost)));
		commands = Stream.concat(commands.stream(), slideServoToValue(LEFT_AND_RIGHT, getPostLeftRightValue(fromPost)).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Come down to disc #%d in position %d", d, fromPosOnPost));
//	commands.add(String.format("SLIDE: LEFT, %d", getDiscZCoordinate(fromPosOnPost)));
		commands = Stream.concat(commands.stream(), slideServoToValue(UP_AND_DOWN, getDiscZCoordinate(fromPosOnPost)).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Close the CLAW on disc #%d", d));
//	commands.add(String.format("SLIDE: CLAW, %d", clawClosed));
		commands = Stream.concat(commands.stream(), slideServoToValue(OPEN_AND_CLOSE, getClosedClawPosOnDisc(d)).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Move disc #%d UP%%2C above the post %s", d, fromPost));
//	commands.add(String.format("SLIDE: LEFT, %d", aboveThePosts));
		commands = Stream.concat(commands.stream(), slideServoToValue(UP_AND_DOWN, aboveThePosts).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Rotate above post %s", toPost));
//	commands.add(String.format("SLIDE: BOTTOM, %d", getPostLeftRightValue(toPost)));
		commands = Stream.concat(commands.stream(), slideServoToValue(LEFT_AND_RIGHT, getPostLeftRightValue(toPost)).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Bring disc #%d down to position %d on post %s", d, toPosOnPost, toPost));
//	commands.add(String.format("SLIDE: LEFT, %d", getDiscZCoordinate(toPosOnPost)));
		commands = Stream.concat(commands.stream(), slideServoToValue(UP_AND_DOWN, getDiscZCoordinate(toPosOnPost)).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Open the CLAW"));
//	commands.add(String.format("SLIDE: CLAW, %d", clawOpen));
		commands = Stream.concat(commands.stream(), slideServoToValue(OPEN_AND_CLOSE, clawOpen).stream()).collect(Collectors.toList());
		commands.add("WAIT: 250"); // Simulate wait

//	commands.add(String.format("PRINT: Move UP%%2C above post %s", toPost));
//	commands.add(String.format("SLIDE: LEFT, %d", aboveThePosts));
		commands = Stream.concat(commands.stream(), slideServoToValue(UP_AND_DOWN, aboveThePosts).stream()).collect(Collectors.toList());
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
//	MeArmPilot.executeCommand("HELP", 0);

		System.out.println(String.format("With %d discs, from A to C", nbDisc));

		hanoiStand = new HanoiContext.Stand("A", "B", "C");
		String initialPost = FROM_DISC;
		hanoiStand.initStand(nbDisc, initialPost);

		try {
			MeArmPilot.initContext(left, claw, bottom, right);
		} catch (I2CFactory.UnsupportedBusNumberException ex) {
			System.out.println("Ooops, no I2C bus...");
		} catch (Exception ioe) {
			System.out.println("Is the PCA9685 connected?");
		}

		HanoiContext.getInstance().fireSetNbDisc(nbDisc);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Park everyone ;)
			MeArmPilot.executeCommand("FORK_SLIDE: BOTTOM, 0, LEFT, 0, RIGHT, 0, CLAW, 0");
			try {
				Thread.sleep(1_000L); // Wait for the  command above to be completed;
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			MeArmPilot.runMacro(RESET);
			System.out.println("\nInterrupted.");
		}));

		MeArmPilot.runMacro(RESET);
		// Initial position
		MeArmPilot.executeCommand("FORK_SLIDE: BOTTOM, 0, LEFT, 0, RIGHT, 0, CLAW, 0");

		// Calibrate here
    String response = utils.StaticUtil.userInput(">> Calibrate MeArm y|[n] ? > ");
    if ("y".equalsIgnoreCase(response)) {
	    try {
//	    MeArmPilot.executeCommand("PRINT: Calibrating the MeArm's position here.");
		    MeArmPilot.executeCommand("USER_INPUT: Hit [return] to start calibration. ");

		    MeArmPilot.executeCommand("PRINT: All the way down%2C post A.");
		    List<String> commands = new ArrayList<>();
		    commands = Stream.concat(commands.stream(), slideServoToValue(UP_AND_DOWN, postsLevelZero).stream()).collect(Collectors.toList());
		    commands.add("WAIT: 250"); // Simulate wait
		    MeArmPilot.runMacro(commands);

		    commands = new ArrayList<>();
		    commands = Stream.concat(commands.stream(), slideServoToValue(LEFT_AND_RIGHT, getPostLeftRightValue("A")).stream()).collect(Collectors.toList());
		    commands.add("WAIT: 250"); // Simulate wait
		    MeArmPilot.runMacro(commands);
		    MeArmPilot.executeCommand("USER_INPUT: Hit [return] when ready for next step. ");

		    MeArmPilot.executeCommand("PRINT: All the way down%2C post B.");
		    commands = new ArrayList<>();
		    commands = Stream.concat(commands.stream(), slideServoToValue(LEFT_AND_RIGHT, getPostLeftRightValue("B")).stream()).collect(Collectors.toList());
		    commands.add("WAIT: 250"); // Simulate wait
		    MeArmPilot.runMacro(commands);
		    MeArmPilot.executeCommand("USER_INPUT: Hit [return] when ready for next step. ");

		    MeArmPilot.executeCommand("PRINT: All the way down%2C post C.");
		    commands = new ArrayList<>();
		    commands = Stream.concat(commands.stream(), slideServoToValue(LEFT_AND_RIGHT, getPostLeftRightValue("C")).stream()).collect(Collectors.toList());
		    commands.add("WAIT: 250"); // Simulate wait
		    MeArmPilot.runMacro(commands);
		    MeArmPilot.executeCommand("USER_INPUT: Hit [return] when ready for next step. ");

		    MeArmPilot.executeCommand("PRINT: Moving to Post B.");
		    commands = Stream.concat(commands.stream(), slideServoToValue(LEFT_AND_RIGHT, getPostLeftRightValue("B")).stream()).collect(Collectors.toList());
		    commands.add("WAIT: 250"); // Simulate wait
		    MeArmPilot.runMacro(commands);


		    commands = new ArrayList<>();
		    // All the way up
		    commands = Stream.concat(commands.stream(), slideServoToValue(UP_AND_DOWN, aboveThePosts).stream()).collect(Collectors.toList());
		    commands.add("WAIT: 250"); // Simulate wait

		    for (int disc = 0; disc < nbDisc; disc++) {
			    commands = new ArrayList<>();
			    // Open
			    commands = Stream.concat(commands.stream(), slideServoToValue(OPEN_AND_CLOSE, clawOpen).stream()).collect(Collectors.toList());
			    commands.add("WAIT: 250"); // Simulate wait
			    // Z
			    commands = Stream.concat(commands.stream(), slideServoToValue(UP_AND_DOWN, getDiscZCoordinate(disc + 1)).stream()).collect(Collectors.toList());
			    commands.add("WAIT: 250"); // Simulate wait
			    // Close on disc
			    commands = Stream.concat(commands.stream(), slideServoToValue(OPEN_AND_CLOSE, getClosedClawPosOnDisc(nbDisc - disc)).stream()).collect(Collectors.toList());
			    commands.add("WAIT: 250"); // Simulate wait

			    MeArmPilot.runMacro(commands);
			    MeArmPilot.executeCommand(String.format("PRINT: Post B%%2C disc #%d.", (nbDisc - disc)));

			    MeArmPilot.executeCommand("USER_INPUT: Hit [return] when ready for next step. ");
		    }

		    MeArmPilot.executeCommand("FORK_SLIDE: BOTTOM, 0, LEFT, 0, RIGHT, 0, CLAW, 0");
		    MeArmPilot.executeCommand("PRINT: Calibrating Completed.");

	    } catch (Exception ex) {
		    ex.printStackTrace();
	    }
    }

		String cmd = "USER_INPUT: Hit [return] when ready to begin. ";

		try {
			MeArmPilot.validateCommand(cmd);
			MeArmPilot.executeCommand(cmd);
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

//				System.out.println((new StringBuilder())
//						.append("Moving from ")
//						.append(String.format("%s pos %d", from, fromPost.getDiscCount()))
//						.append(" to ")
//						.append(String.format("%s, currently %d disc(s)", to, toPost.getDiscCount())).toString());

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

		// Park everyone ;)
		MeArmPilot.executeCommand("FORK_SLIDE: BOTTOM, 0, LEFT, 0, RIGHT, 0, CLAW, 0");

		try {
			Thread.sleep(1_000L); // Wait for the  command above to be completed;
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
		MeArmPilot.runMacro(RESET);
		System.out.println("Done.");
	}
}
