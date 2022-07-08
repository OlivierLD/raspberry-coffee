package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import static utils.StaticUtil.userInput;
import static utils.TimeUtil.delay;

/**
 * Refers to a szc ript containing commands to execute to drive e MeArm robotic arm.
 *
 * Uses a PCA9685 (I2C) to drive a MeArm
 *
 * This class refers to a script to know what to do.
 * See the {@link #main} method.
 *
 * @deprecated, use {@link MeArmPilotDemo} instead.
 */
public class MeArmScriptDemo {
	// Servo MG90S
	private static int servoMin = 130; // -90 degrees at 60 Hertz
	private static int servoMax = 675; //  90 degrees at 60 Hertz

	/**
	 * Commands supported in the script.
	 * Constructor's parameters are:
	 * <ol>
	 * <li>Command Name (as used in the script)</li>
	 * <li>Nb of parameters</li>
	 * <li>Consumer&lt;CommandWithArgs&gt; to execute for this command</></li>
	 * </ol>
	 */
	enum Commands {
		SET_PMW("SET_PWM", 3, MeArmScriptDemo::servoSetPwm),
		PRINT("PRINT", 1, MeArmScriptDemo::servoPrint),
		MOVE("MOVE", 5, MeArmScriptDemo::servoMove),
		USER_INPUT("USER_INPUT", 1, MeArmScriptDemo::servoUserInput),
		WAIT("WAIT", 1, MeArmScriptDemo::servoWait);

		private final String command;
		private final int nbPrm;
		private final Consumer<CommandWithArgs> processor;

		Commands(String command, int nbPrm, Consumer<CommandWithArgs> processor) {
			this.command = command;
			this.nbPrm = nbPrm;
			this.processor = processor;
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
	}

	private final static int LEFT_SERVO_CHANNEL = 0; // Up and down. Range 350 (all the way up) 135 (all the way down), Centered at ~230
	private final static int CLAW_SERVO_CHANNEL = 1; // Open and close. Range 130 (open) 400 (closed)
	private final static int BOTTOM_SERVO_CHANNEL = 2; // Right and Left. 130 (all the way right) 675 (all the way left). Center at ~410
	private final static int RIGHT_SERVO_CHANNEL = 4; // Back and forth. 130 (too far back, limit to 300) 675 (all the way ahead), standing right at ~430

	private final int WAIT = 25; // Unused for now...

	private final static class CommandWithArgs {
		private final String command;
		private final String[] args;

		public CommandWithArgs(String command, String[] args) {
			this.command = command;
			this.args = args;
		}
	}

	/**
	 * Warning No comma "," in the message body!!
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
				System.out.println(">> PRINT >>> " + cmd.args[0]);
			}
		}
	}

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

	private static void servoUserInput(CommandWithArgs cmd) {
		if (!cmd.command.equals("USER_INPUT")) {
			System.err.println(String.format("Unexpected command [%s] in servoUserInput.", cmd.command));
		} else {
			if (cmd.args.length != 1) {
				System.err.println(String.format("Unexpected number of args [%d] in servoUserInput.", cmd.args.length));
			} else {
				String absorbed = userInput(cmd.args[0]);
			}
		}
	}

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

	private static PCA9685 servoBoard = null;

	private static void initContext()
					throws I2CFactory.UnsupportedBusNumberException {
		servoBoard = new PCA9685();
		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz
	}

	private static void validateCommand(String cmd, int lineNo) {
		String[] cmdAndPrms = cmd.split(":");
		Optional<Commands> commandOptional = Arrays.stream(Commands.values()).filter(verb -> verb.command().equals(cmdAndPrms[0])).findFirst();
		if (!commandOptional.isPresent()) {
			System.err.println(String.format("Line #%d, Command [%s] not found.", lineNo, cmdAndPrms[0]));
			System.exit(1);
		} else {
			Commands command = commandOptional.get();
			String[] prms = cmdAndPrms[1].split(",");
			if (command.nbPrm() != prms.length) {
				System.err.println(String.format("Command %s expects %d parameters. Found %d in [%s]", command.command(), command.nbPrm(), prms.length, cmd));
				System.exit(1);
			}
		}
	}

	private static void executeCommand(String cmd, int lineNo) {
		String[] cmdAndPrms = cmd.split(":");
		Optional<Commands> commandOptional = Arrays.stream(Commands.values()).filter(verb -> verb.command().equals(cmdAndPrms[0])).findFirst();
		if (!commandOptional.isPresent()) {
			System.err.println(String.format("Line #%d, Command [%s] not found.", lineNo, cmdAndPrms[0]));
			System.exit(1);
		} else {
			Commands command = commandOptional.get();
			String[] prms = cmdAndPrms[1].split(",");
			if (command.nbPrm() != prms.length) {
				System.err.println(String.format("Command %s expects %d parameters. Found %d in [%s]", command.command(), command.nbPrm(), prms.length, cmd));
				System.exit(1);
			} else {
				Consumer<CommandWithArgs> processor = command.processor();
				if (processor != null) {
					CommandWithArgs cna = new CommandWithArgs(command.command(), prms);
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
				servoNum = LEFT_SERVO_CHANNEL;
				break;
			case "RIGHT":
				servoNum = RIGHT_SERVO_CHANNEL;
				break;
			case "CLAW":
				servoNum = CLAW_SERVO_CHANNEL;
				break;
			case "BOTTOM":
				servoNum = BOTTOM_SERVO_CHANNEL;
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

	/**
	 * Execute a MeArm script. Provide the script name in a System variable, like in
	 * <pre>
	 *   -Dscript.name=script.01.mearm
	 * </pre>
	 *
	 * @param args None required.
	 * @throws I2CFactory.UnsupportedBusNumberException when I2C bus is not found (if you're not on a Raspberry Pi)
	 * @throws IOException                              when the script cannot be read, for example. File not found or so.
	 */
	public static void main(String... args)
					throws I2CFactory.UnsupportedBusNumberException,
					IOException {
		String scriptName = System.getProperty("script.name");
		if (scriptName == null) {
			throw new RuntimeException("Please provide the script name in -Dscript.name");
		}

		// 1 - Validate the script
		BufferedReader scriptReader = new BufferedReader(new FileReader(scriptName));
		String line = "";
		boolean keepReading = true;
		int nbLine = 0;
		while (keepReading) {
			line = scriptReader.readLine();
			if (line == null) {
				keepReading = false;
			} else {
				nbLine++;
				System.out.println("Read " + line);
				if (!line.startsWith("#")) {
					validateCommand(line, nbLine);
				}
			}
		}
		scriptReader.close();
		System.out.println("> Script is valid. <");

		// 2 - Execute the script
		System.out.println("Now running the commands");
		initContext();

		scriptReader = new BufferedReader(new FileReader(scriptName));
		line = "";
		keepReading = true;
		nbLine = 0;
		while (keepReading) {
			line = scriptReader.readLine();
			if (line == null) {
				keepReading = false;
			} else {
				nbLine++;
//      System.out.println("Executing " + line);
				if (!line.startsWith("#")) {
					System.out.println(String.format("\tExecuting [%s]", line));
					executeCommand(line, nbLine);
				}
			}
		}
		scriptReader.close();
		System.out.println("> Script completed. <");
	}
}
