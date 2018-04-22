package oliv.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A prototype for email command processing, without the email part.
 */
public class StreamConsumers {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	private static boolean verbose = "true".equals(System.getProperty("command.verbose", "false"));

	// The list of processors - Consumers
	List<CommandProcessor> processors = Arrays.asList(
			new CommandProcessor("execute", this::cmdProcessor),
			new CommandProcessor("file", this::newFileProcessor)
	);

	// The processing loop
	private boolean loopAndProcess(List<MessageContext> commandList) {
		boolean keepLooping = true;
		try {
			for (MessageContext mess : commandList) {
				if (verbose) {
					System.out.println("Received:\n" + mess.content);
				}
				String operation = mess.operation;
				if ("exit".equals(operation)) {                 // operation = 'exit'
					keepLooping = false;
					if (verbose) {
						System.out.println("Will exit next batch.");
					}
				} else {
					if (true || verbose) {
						System.out.println(String.format("---- Operation: [%s], sent for processing ----", operation));
					}
					final String finalOp = operation;
					Optional<CommandProcessor> processor = this.processors
							.stream()
							.filter(p -> finalOp.equals(p.getKey()))
							.findFirst();
					if (processor.isPresent()) {
						processor.get().getProcessor().accept(mess);
					} else {
						// Operation not found.
						System.out.println(String.format(">>> No processor registered for operation [%s] <<<", operation));
						try {
							Thread.sleep(1_000L);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return keepLooping;
	}

	public static void main(String... args) {

		StreamConsumers streamProcessor = new StreamConsumers();

		boolean keepLooping = true;
		while (keepLooping) {
			try {
				System.out.println("Starting.");
				// The list of messages to process.
				List<MessageContext> received = Arrays.asList(
						new MessageContext()
								.content("whoami\nuname -a") // ls -lisah
								.sender("Oliv")
								.operation("execute"),
						new MessageContext()
								.operation("pouet")
								.content("Uuuh what?"),
						new MessageContext()
								.operation("file")
								.content("uname -a\nwhoami"),
						new MessageContext()
								.operation("exit")
				);
				System.out.println("----------------------------------------------");
				System.out.println(SDF.format(new Date()) + " - Retrieved " + received.size() + " command(s).");
				System.out.println("----------------------------------------------");

				keepLooping = streamProcessor.loopAndProcess(received);

			} catch (Exception ex) {
				System.err.println("Receiver loop:");
				ex.printStackTrace();
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		System.out.println("Receiver. Done.");
	}

	// The user-provided message, to be processed.
	public static class MessageContext {
		String content;
		String sender;
		String operation;

		public MessageContext content(String content) {
			this.content = content;
			return this;
		}
		public MessageContext sender(String sender) {
			this.sender = sender;
			return this;
		}
		public MessageContext operation(String operation) {
			this.operation = operation;
			return this;
		}
	}

	// Association operation/Consumer
	public static class CommandProcessor {
		private String key;
		private Consumer<MessageContext> processor;

		public CommandProcessor(String key, Consumer<MessageContext> processor) {
			this.key = key;
			this.processor = processor;
		}
		public String getKey() {
			return this.key;
		}
		public Consumer<MessageContext> getProcessor() {
			return this.processor;
		}
	}

	// Consumer - operation = 'execute'
	private void cmdProcessor(MessageContext messContext) {
		try {
			String[] cmds = messContext.content.split("\n");
			for (String cmd : cmds) {
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream())); // stdout
				StringBuffer output = new StringBuffer();
				String line;
				while ((line = stdout.readLine()) != null) {
//				System.out.println(line);
					output.append(line + "\n");
				}
				int exitStatus = p.waitFor(); // Sync call
				System.out.println(String.format("cmd [%s] returned status %d.\n%s", cmd, exitStatus, output.toString()));
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// Consumer - operation = 'file'
	private void newFileProcessor(MessageContext messContext) {
		try {
			String[] cmds = messContext.content.split("\n");
			BufferedWriter bw = new BufferedWriter(new FileWriter("cmd.out"));
			for (String cmd : cmds) {
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream())); // stdout
				StringBuffer output = new StringBuffer();
				String line;
				while ((line = stdout.readLine()) != null) {
					bw.write(line + '\n');
//				System.out.println(line);
					output.append(line + "\n");
				}
				int exitStatus = p.waitFor(); // Sync call
				if (verbose) {
					System.out.println(String.format("cmd [%s] returned status %d.\n%s\nOutput in cmd.out", cmd, exitStatus, output.toString()));
				}
			}
			bw.close();
			System.out.println("Output in cmd.out");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
