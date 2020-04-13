package oliv.misc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RuntimeExec {

	private static String cmdProcessor(String script) {
		try {
			// Loop on lines of cmd
			String[] cmds = script.split("\n");
			StringBuffer output = new StringBuffer();
			for (String cmd : cmds) {
				if (!cmd.trim().isEmpty()) {
					Process p = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c",  cmd});
					BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream())); // stdout
					BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream())); // stderr
					String line;
					while ((line = stdout.readLine()) != null) {
						System.out.println(line);
						output.append(line + "\n");
					}
					while ((line = stderr.readLine()) != null) {
						System.out.println(line);
						output.append(line + "\n");
					}
					int exitStatus = p.waitFor(); // Sync call
					output.append(String.format(">> %s returned status %d\n", cmd.trim(), exitStatus));
				}
			}
			return	String.format("cmd [%s] returned: \n%s", script, output.toString());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void main(String... args) {
		String result = cmdProcessor("whoami\nps -ef | grep java");
		System.out.println(result);
	}
}
