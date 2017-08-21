package i2c.samples.mearm;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Standard, all the way, clockwise, counterclockwise.
 *
 * Uses a PCA9685 (I2C) to drive a MeArm
 *
 * Execute commands sent by a main or another program.
 */
public class MeArmPilot {

	private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Prompt the user for input, from stdin. Completed on [Return]
	 *
	 * @param prompt The prompt
	 * @return the user's input.
	 */
	private static String userInput(String prompt) {
		String retString = "";
		System.out.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			System.out.println(e);
			String s;
			try {
				s = userInput("<Oooch/>");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return retString;
	}

	/**
	 * @param howMuch in ms.
	 */
	private static void delay(long howMuch) {
		try {
			Thread.sleep(howMuch);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	// Servo MG90S
	private static int servoMin = 130; // -90 degrees at 60 Hertz
	private static int servoMax = 675; //  90 degrees at 60 Hertz

	private final static String DUMMY_HELP = "Duh...";

	private final static String HELP_HELP = "You're on it...";
	private final static String PRINT_HELP =
	    "PRINT: \"Your message here\"\n" +
			"        |\n" +
			"        Encode commas and columns with UTF-8: ','=%2C, ':'=%3A):\n" +
			"       \"Your message here%2C please.\"";
	private final static String WAIT_HELP =
			"WAIT: 1000\n" +
			"      |\n" +
			"      In ms";
	private final static String SET_PWM_HELP =
			"SET_PWM: BOTTOM, 0, 0\n" +
			"         |       |  |\n" +
			"         |       |  Off\n" +
			"         |       On\n" +
			"         Servo ID";
	private final static String MOVE_HELP =
			"MOVE: LEFT, 350, 230, 10, 25\n" +
			"      |     |    |    |   |\n" +
			"      |     |    |    |   Wait (between each step)\n" +
			"      |     |    |    Nb steps from 'from' to 'to'\n" +
			"      |     |    To\n" +
			"      |     From\n" +
			"      Servo ID";
	private final static String DIRECT_MOVE_HELP =
			"DIRECT: LEFT, 350\n" +
			"        |     |\n" +
			"        |     To\n" +
			"        Servo ID";
	private final static String SLIDE_HELP =
			"SLIDE: BOTTOM, 0\n" +
			"       |       |\n" +
			"       |       Value [-100..+100]\n" +
			"       Servo ID";
	private final static String USER_INPUT_HELP =
			"USER_INPUT: \"Prompt\"\n" +
			"  Resumes after the user hits [Return]\n" +
			"  Encode commas and columns with UTF-8: ','=%2C, ':'=%3A):\n" +
			"  Typically used to wait for the user to be ready:\n" +
			"USER_INPUT: \"Hit [Return] when ready \"";
	/**
	 * Commands supported in the script.
	 * Constructor's parameters are:
	 * <ol>
	 * <li>Command Name (as used in the script)</li>
	 * <li>Nb of parameters</li>
	 * <li>Consumer&lt;CommandWithArgs&gt; to execute for this command</></li>
	 * </ol>
	 */
	public enum Commands {
		HELP("HELP", 0, MeArmPilot::displayHelp, HELP_HELP),
		SET_PMW("SET_PWM", 3, MeArmPilot::servoSetPwm, SET_PWM_HELP),
		PRINT("PRINT", 1, MeArmPilot::servoPrint, PRINT_HELP),
		MOVE("MOVE", 5, MeArmPilot::servoMove, MOVE_HELP),

		DIRECT("DIRECT", 2, MeArmPilot::servoDirectMove, DIRECT_MOVE_HELP),
		SLIDE("SLIDE", 2, MeArmPilot::servoSlide, SLIDE_HELP),
		BOUNDARIES("BOUNDARIES", 0, MeArmPilot::showBoundaries, "Just type it"),

		USER_INPUT("USER_INPUT", 1, MeArmPilot::servoUserInput, USER_INPUT_HELP),
		WAIT("WAIT", 1, MeArmPilot::servoWait, WAIT_HELP);

		private final String command;
		private final int nbPrm;
		private final Consumer<CommandWithArgs> processor;
		private final String help;

		Commands(String command, int nbPrm, Consumer<CommandWithArgs> processor, String help) {
			this.command = command;
			this.nbPrm = nbPrm;
			this.processor = processor;
			this.help = help;
		}

		public String command() {
			return this.command;
		}

		public int nbPrm() {
			return this.nbPrm;
		}

		public Consumer<CommandWithArgs> processor() {
			return this.processor;
		}

		public String help() { return this.help; }
	}

	public final static int DEFAULT_LEFT_SERVO_CHANNEL = 0; // Up and down. Range 350 (all the way up) 135 (all the way down), Centered at ~230
	public final static int DEFAULT_CLAW_SERVO_CHANNEL = 1; // Open and close. Range 130 (open) 400 (closed)
	public final static int DEFAULT_BOTTOM_SERVO_CHANNEL = 2; // Right and Left. 130 (all the way right) 675 (all the way left). Center at ~410
	public final static int DEFAULT_RIGHT_SERVO_CHANNEL = 4; // Back and forth. 130 (too far back, limit to 300) 675 (all the way ahead), standing right at ~430

	enum ServoBoundaries {
		LEFT(135, 350, 230),
		RIGHT(130, 675, 410),
		BOTTOM(130, 675, 430),
		CLAW(130, 400, 265);

		private final int min;
		private final int max;
		private final int center;

		ServoBoundaries(int min, int max, int center) {
			this.min = min;
			this.max = max;
			this.center = center;
		}

		public int min() { return this.min; }
		public int max() { return this.max; }
		public int center() { return this.center; }
	}

	private static int leftServoChannel = DEFAULT_LEFT_SERVO_CHANNEL;
	private static int clawServoChannel = DEFAULT_CLAW_SERVO_CHANNEL;
	private static int bottomServoChannel = DEFAULT_BOTTOM_SERVO_CHANNEL;
	private static int rightServoChannel = DEFAULT_RIGHT_SERVO_CHANNEL;

	private final static class CommandWithArgs {
		private final String full;
		private final String command;
		private final String[] args;

		public CommandWithArgs(String input, String command, String... args) {
			this.full = input;
			this.command = command;
			this.args = args;
		}
	}

	private static void displayHelp(CommandWithArgs cmd) {
		System.out.println("Available commands with their syntax:");
		System.out.println("-------------------------------------");
		for (Commands command : Commands.values()) {
			System.out.println(String.format("Command '%s', %d parameters.\nUsage is:\n%s", command.command(), command.nbPrm(), command.help()));
			System.out.println("-------------------------------------");
		}
	}

	/**
	 * Warning No comma ",", to columns ":" in the message!!
	 * Encode comma with %2C, column with %3A
	 *
	 * @param cmd
	 */
	private static void servoPrint(CommandWithArgs cmd) {
		if (!cmd.command.equals("PRINT")) {
			System.err.println(String.format("Unexpected command [%s] in servoPrint.", cmd.command));
		} else {
			if (cmd.args.length != 1) {
				System.err.println(String.format("Unexpected number of args [%d] in servoPrint.", cmd.args.length));
			} else {
				try {
					System.out.println(">> PRINT >>> " + URLDecoder.decode(cmd.args[0], "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Syntax WAIT:1000
	 *             |
	 *             In ms
	 * @param cmd
	 */
	private static void servoWait(CommandWithArgs cmd) {
		if (!cmd.command.equals("WAIT")) {
			System.err.println(String.format("Unexpected command [%s] in servoWait.", cmd.command));
		} else {
			if (cmd.args.length != 1) {
				System.err.println(String.format("Unexpected number of args [%d] in servoWait.", cmd.args.length));
			} else {
				try {
					delay(Long.parseLong(cmd.args[0].trim()));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
	}

	/**
	 * Syntax USER_INPUT: "Prompt"
	 * Warning No comma ",", to columns ":" in the prompt!!
	 * Encode comma with %2C, column with %3A
	 */
	private static void servoUserInput(CommandWithArgs cmd) {
		if (!cmd.command.equals("USER_INPUT")) {
			System.err.println(String.format("Unexpected command [%s] in servoUserInput.", cmd.command));
		} else {
			if (cmd.args.length != 1) {
				System.err.println(String.format("Unexpected number of args [%d] in servoUserInput.", cmd.args.length));
			} else {
				try {
					String absorbed = userInput(URLDecoder.decode(cmd.args[0], "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Syntax SET_PWM:BOTTOM, 0, 0
	 *                |       |  |
	 *                |       |  Off
	 *                |       On
	 *                Servo ID
	 * @param cmd
	 */
	private static void servoSetPwm(CommandWithArgs cmd) {
		if (!cmd.command.equals("SET_PWM")) {
			System.err.println(String.format("Unexpected command [%s] in servoSetPwm.", cmd.command));
		} else {
			if (cmd.args.length != 3) {
				System.err.println(String.format("Unexpected number of args [%d] in servoSetPwm.", cmd.args.length));
			} else {
				int servoNum = getServoNum(cmd.args[0].trim());
				if (servoNum != -1) {
					try {
						int on = Integer.parseInt(cmd.args[1].trim());
						int off = Integer.parseInt(cmd.args[2].trim());
						if (servoBoard != null) {
							servoBoard.setPWM(servoNum, on, off);
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.err.println(String.format("Unknown servo: [%s]", cmd.args[0].trim()));
				}
			}
		}
	}

	/**
	 * Syntax MOVE: LEFT, 350, 230, 10, 25
	 *              |     |    |    |   |
	 *              |     |    |    |   Wait (between each step)
	 *              |     |    |    Nb steps from 'from' to 'to'
	 *              |     |    To
	 *              |     From
	 *              Servo ID
	 * @param cmd
	 */
	private static void servoMove(CommandWithArgs cmd) {
		if (!cmd.command.equals("MOVE")) {
			System.err.println(String.format("Unexpected command [%s] in servoMove.", cmd.command));
		} else {
			if (cmd.args.length != 5) {
				System.err.println(String.format("Unexpected number of args [%d] in servoMove.", cmd.args.length));
			} else {
				int servoNum = getServoNum(cmd.args[0].trim());
				if (servoNum != -1) {
					try {
						int from = Integer.parseInt(cmd.args[1].trim());
						int to = Integer.parseInt(cmd.args[2].trim());
						int step = Integer.parseInt(cmd.args[3].trim());
						int wait = Integer.parseInt(cmd.args[4].trim());
						if (servoBoard != null) {
							move(servoNum, from, to, step, wait);
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.err.println(String.format("Unknown servo: [%s]", cmd.args[0].trim()));
				}
			}
		}
	}

	/**
	 * Syntax DIRECT: LEFT, 350
	 *                |     |
	 *                |     To
	 *                Servo ID
	 * @param cmd
	 */
	private static void servoDirectMove(CommandWithArgs cmd) {
		if (!cmd.command.equals("DIRECT")) {
			System.err.println(String.format("Unexpected command [%s] in servoDirectMove.", cmd.command));
		} else {
			if (cmd.args.length != 2) {
				System.err.println(String.format("Unexpected number of args [%d] in servoDirectMove.", cmd.args.length));
			} else {
				int servoNum = getServoNum(cmd.args[0].trim());
				if (servoNum != -1) {
					try {
						int to = Integer.parseInt(cmd.args[1].trim());
						if (!validateValue(servoNum, to)) {
							System.out.println(String.format("Servo value [%s] out of bounds for %s.", to, cmd.args[0].trim()));
						} else {
							if (servoBoard != null) {
								servoBoard.setPWM(servoNum, 0, to);
							}
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.err.println(String.format("Unknown servo: [%s]", cmd.args[0].trim()));
				}
			}
		}
	}

	/**
	 * Syntax SLIDE:BOTTOM, 0
	 *              |       |
	 *              |       Value [-100..+100]
	 *              Servo ID
	 * @param cmd
	 */
	private static void servoSlide(CommandWithArgs cmd) {
		if (!cmd.command.equals("SLIDE")) {
			System.err.println(String.format("Unexpected command [%s] in servoSlide.", cmd.command));
		} else {
			if (cmd.args.length != 2) {
				System.err.println(String.format("Unexpected number of args [%d] in servoSlide.", cmd.args.length));
			} else {
				int servoNum = getServoNum(cmd.args[0].trim());
				if (servoNum != -1) {
					try {
						int to = Integer.parseInt(cmd.args[1].trim());
						if (servoBoard != null) {
							setFromSlider(cmd.args[0].trim(), to);
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.err.println(String.format("Unknown servo: [%s]", cmd.args[0].trim()));
				}
			}
		}
	}

	private static boolean validateValue(int servo, int value) {
		boolean ok = true;
		if (servo == leftServoChannel) {
			ok = !(value < ServoBoundaries.LEFT.min() || value > ServoBoundaries.LEFT.max());
		} else if (servo == rightServoChannel) {
			ok = !(value < ServoBoundaries.RIGHT.min() || value > ServoBoundaries.RIGHT.max());
		} else if (servo == bottomServoChannel) {
			ok = !(value < ServoBoundaries.BOTTOM.min() || value > ServoBoundaries.BOTTOM.max());
		} else if (servo == clawServoChannel) {
			ok = !(value < ServoBoundaries.CLAW.min() || value > ServoBoundaries.CLAW.max());
		} else {
				ok = false;
		}
		return ok;
	}

	private static void showBoundaries(CommandWithArgs cmd) {
		if (!cmd.command.equals("BOUNDARIES")) {
			System.err.println(String.format("Unexpected command [%s] in showBoundaries.", cmd.command));
		} else {
			if (cmd.args.length != 0) {
				System.err.println(String.format("Unexpected number of args [%d] in showBoundaries.", cmd.args.length));
			} else {
				// ServoBoundaries
				for (ServoBoundaries boundary : ServoBoundaries.values()) {
					System.out.println(String.format("Servo %s, min: %d, max: %d, center: %d", boundary.toString(), boundary.min(), boundary.max(), boundary.center()));
				}
			}
		}
	}

	/**
	 * Execute an array of commands
	 * @param command
	 */
	public static void runMacro(String... command) {
		for (String cmd : command) {
			MeArmPilot.executeCommand(cmd, -1);
		}
	}

	public static String[] closeClaw() {
		return new String[] {
				String.format("DIRECT: CLAW, %d", ServoBoundaries.CLAW.max())
		};
	}

	public static String[] openClaw() {
		return new String[] {
				String.format("DIRECT: CLAW, %d", ServoBoundaries.CLAW.min())
		};
	}

	/**
	 * Set the value from a slider. The value MUST be in [-100.0..+100.0]
	 *
	 * @param servoName
	 * @param value
	 */
	public static void setFromSlider(String servoName, double value) {
		if (value < -100 || value > 100) {
			throw new IllegalArgumentException(String.format("Invalid value [-100..100] %f", value));
		}
		int servoValue = -1;
		switch (servoName) {
			case "LEFT":
				int deltaL = (value < 0 ? ServoBoundaries.LEFT.center() - ServoBoundaries.LEFT.min() : ServoBoundaries.LEFT.max() - ServoBoundaries.LEFT.center());
				servoValue = (int)Math.round(ServoBoundaries.LEFT.center() + (deltaL * (value / 100d)));
				break;
			case "RIGHT":
				int deltaR = (value < 0 ? ServoBoundaries.RIGHT.center() - ServoBoundaries.RIGHT.min() : ServoBoundaries.RIGHT.max() - ServoBoundaries.RIGHT.center());
				servoValue = (int)Math.round(ServoBoundaries.RIGHT.center() + (deltaR * (value / 100d)));
				break;
			case "CLAW":
				int deltaC = (value < 0 ? ServoBoundaries.CLAW.center() - ServoBoundaries.CLAW.min() : ServoBoundaries.CLAW.max() - ServoBoundaries.CLAW.center());
				servoValue = (int)Math.round(ServoBoundaries.CLAW.center() + (deltaC * (value / 100d)));
				break;
			case "BOTTOM":
				int deltaB = (value < 0 ? ServoBoundaries.BOTTOM.center() - ServoBoundaries.BOTTOM.min() : ServoBoundaries.BOTTOM.max() - ServoBoundaries.BOTTOM.center());
				servoValue = (int)Math.round(ServoBoundaries.BOTTOM.center() + (deltaB * (value / 100d)));
				break;
			default:
				System.out.println(String.format("Unknown servo %s", servoName));
				break;
		}
		if (servoValue != -1) {
			String macro = String.format("DIRECT: %s, %d", servoName, servoValue);
			System.out.println(String.format("Executing %s", macro));
			runMacro(macro);
		}
	}

	public static String[] initStop() {
		return new String[]{
				"SET_PWM:LEFT,   0, 0",
				"SET_PWM:RIGHT,  0, 0",
				"SET_PWM:CLAW,   0, 0",
				"SET_PWM:BOTTOM, 0, 0"
		};
	}

	public static String[] initialPosition() {
		List<String> cmds = new ArrayList<>();
		for (ServoBoundaries boundary : ServoBoundaries.values()) {
			cmds.add(String.format("DIRECT: %s, %d", boundary.toString(), boundary.center()));
		}
    return cmds.toArray(new String[cmds.size()]);
	}

	private static PCA9685 servoBoard = null;

	public static void initContext()
			throws I2CFactory.UnsupportedBusNumberException {
		initContext(DEFAULT_LEFT_SERVO_CHANNEL, DEFAULT_CLAW_SERVO_CHANNEL, DEFAULT_BOTTOM_SERVO_CHANNEL, DEFAULT_RIGHT_SERVO_CHANNEL);
	}
	public static void initContext(int leftServo, int clawServo, int bottomServo, int rightServo)
					throws I2CFactory.UnsupportedBusNumberException {
		leftServoChannel = leftServo;
		clawServoChannel = clawServo;
		bottomServoChannel = bottomServo;
		rightServoChannel = rightServo;

		System.out.println("InitContext:");
		System.out.println(String.format("Left Servo (Up and Down), #%d", leftServoChannel));
		System.out.println(String.format("Claw Servo (Open and Close), #%d", clawServoChannel));
		System.out.println(String.format("Bottom Servo (Right and Left), #%d", bottomServoChannel));
		System.out.println(String.format("Right Servo (Back and Forth), #%d", rightServoChannel));
		System.out.println("----------------------------------");

		servoBoard = new PCA9685();
		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz
	}

	public static void validateCommand(String cmd, int lineNo) {
		String[] cmdAndPrms = cmd.split(":");
		Optional<Commands> commandOptional = Arrays.stream(Commands.values()).filter(verb -> verb.command().equals(cmdAndPrms[0])).findFirst();
		if (!commandOptional.isPresent()) {
			throw new RuntimeException(String.format("Line #%d, Command [%s] not found.", lineNo, cmdAndPrms[0]));
		} else {
			Commands command = commandOptional.get();
			if (command.nbPrm() > 0) {
				String[] prms = cmdAndPrms[1].split(",");
				if (command.nbPrm() != prms.length) {
					throw new RuntimeException(String.format("Command %s expects %d parameters. Found %d in [%s]", command.command(), command.nbPrm(), prms.length, cmd));
				}
			}
		}
	}

	public static void executeCommand(String cmd, int lineNo) {
		String[] cmdAndPrms = cmd.split(":");
		Optional<Commands> commandOptional = Arrays.stream(Commands.values()).filter(verb -> verb.command().equals(cmdAndPrms[0])).findFirst();
		if (!commandOptional.isPresent()) {
			System.err.println(String.format("Line #%d, Command [%s] not found.", lineNo, cmdAndPrms[0]));
			System.exit(1);
		} else {
			Commands command = commandOptional.get();
			String[] prms = new String[] {};
			if (command.nbPrm() > 0) {
				prms = cmdAndPrms[1].split(",");
			}
			if (command.nbPrm() != prms.length) {
				System.err.println(String.format("Command %s expects %d parameters. Found %d in [%s]", command.command(), command.nbPrm(), prms.length, cmd));
				System.exit(1);
			} else {
				Consumer<CommandWithArgs> processor = command.processor();
				if (processor != null) {
					CommandWithArgs cna = new CommandWithArgs(cmd, command.command(), prms);
					processor.accept(cna);
				} else {
					System.out.println(String.format(">>> %s >>> null.", command.command()));
				}
			}
		}
	}

	private static int getServoNum(String servoId) {
		int servoNum = -1;
		switch (servoId) {
			case "LEFT":
				servoNum = leftServoChannel;
				break;
			case "RIGHT":
				servoNum = rightServoChannel;
				break;
			case "CLAW":
				servoNum = clawServoChannel;
				break;
			case "BOTTOM":
				servoNum = bottomServoChannel;
				break;
			default:
				break;
		}
		return servoNum;
	}

	/**
	 * @param channel Channel #
	 * @param from    From position
	 * @param to      To position
	 * @param step    nb steps from From to To
	 * @param wait    nb ms between each step.
	 */
	private static void move(int channel, int from, int to, int step, int wait) {
		servoBoard.setPWM(channel, 0, 0);
		int inc = step * (from < to ? 1 : -1);
		for (int i = from; (from < to && i <= to) || (to < from && i >= to); i += inc) {
			servoBoard.setPWM(channel, 0, i);
			delay(wait);
		}
		servoBoard.setPWM(channel, 0, 0);
	}
}
