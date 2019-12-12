package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Warning: This class involves artifacts from swing and awt.
 * It might not be suitable for all platforms (like Android...)
 */
public class StaticUtil {

	private static final InputStreamReader inputStream = new InputStreamReader(System.in);
	private static final BufferedReader stdin = new BufferedReader(inputStream);

	public static String userInput(String prompt) {
		String retString = "";
		System.err.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			System.out.println(e.toString());
			try {
				userInput("<Oooch/>");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return retString;
	}

	public static boolean thisClassVerbose(Class c) {
		return (System.getProperty(c.getName() + ".verbose", "false").equals("true") || System.getProperty("all.verbose", "false").equals("true"));
	}

	public static byte[] appendByteArrays(byte[] c, byte[] b, int n) {
		int newLength = c != null ? c.length + n : n;
		byte[] newContent = new byte[newLength];
		if (c != null) {
			for (int i = 0; i < c.length; i++) {
				newContent[i] = c[i];
			}
		}
		int offset = (c != null ? c.length : 0);
		for (int i = 0; i < n; i++) {
			newContent[offset + i] = b[i];
		}
		return newContent;
	}

	public static void copy(InputStream is, OutputStream os) throws IOException {
		synchronized (is) {
			synchronized (os) {
				byte[] buffer = new byte[256];
				while (true) {
					int bytesRead = is.read(buffer);
					if (bytesRead == -1) {
						break;
					}
					os.write(buffer, 0, bytesRead);
				}
			}
		}
	}

	public static File findFileName(String str) throws Exception {
		File file = null;
		boolean go = true;
		int i = 1;
		while (go) {
			String newName = str + "_" + Integer.toString(i);
			File f = new File(newName);
			if (f.exists()) {
				i++;
			} else {
				file = f;
				go = false;
			}
		}
		return file;
	}

	/**
	 * @param filename
	 * @param extension with the preceding ".", like ".ptrn"
	 * @return
	 */
	public static String makeSureExtensionIsOK(String filename, String extension) {
		if (!filename.toLowerCase().endsWith(extension)) {
			filename += extension;
		}
		return filename;
	}

	public static String makeSureExtensionIsOK(String filename, String[] extension, String defaultExtension) {
		boolean extensionExists = false;
		for (int i = 0; i < extension.length; i++) {
			if (filename.toLowerCase().endsWith(extension[i].toLowerCase())) {
				extensionExists = true;
				break;
			}
		}
		if (!extensionExists) {
			filename += defaultExtension;
		}
		return filename;
	}

	public static String getMacAddress() throws IOException {
		String macAddress = null;
		if (System.getProperty("os.name").contains("Windows")) {
			String command = "ipconfig /all";
			Process pid = Runtime.getRuntime().exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pid.getInputStream()));
			while (true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				Pattern p = Pattern.compile(".*Physical Address.*: (.*)");
				Matcher m = p.matcher(line);
				if (m.matches()) {
					macAddress = m.group(1);
					break;
				}
			}
			in.close();
		} else {
			macAddress = "Unknown";
		}
		return macAddress;
	}

	public static void addURLToClassPath(URL url) {
		try {
			URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
			Class<?> c = /*(Class<URLClassLoader>)*/Class.forName("java.net.URLClassLoader");
			Class<?>[] parameterTypes = new Class<?>[1];
			parameterTypes[0] = /*(Class<?>)*/Class.forName("java.net.URL");
			Method m = c.getDeclaredMethod("addURL", parameterTypes);
			m.setAccessible(true);
			Object[] args = new Object[1];
			args[0] = url;
			m.invoke(urlClassLoader, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void showFileSystem(String where) throws Exception {
		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			String cmd = "cmd /k start /D\"" + where + "\" .";
			//    System.out.println("Executing [" + cmd + "]");
			Runtime.getRuntime().exec(cmd); // Can contain blanks, need quotes around it...
		} else if (os.indexOf("Linux") > -1) {
			Runtime.getRuntime().exec("nautilus " + where);
		} else if (os.indexOf("Mac") > -1) {
			String[] applScriptCmd = {
							"osascript",
							"-e", "tell application \"Finder\"",
							"-e", "activate",
							"-e", "<open cmd>", // open cmd: index 6
							"-e", "end tell"
					};
			String pattern = File.separator;
			if (pattern.equals("\\")) {
				pattern = "\\\\";
			}
			String[] pathElem = where.split(pattern);
			String cmd = "open ";
			for (int i = pathElem.length - 1; i > 0; i--) {
				cmd += ("folder \"" + pathElem[i] + "\" of ");
			}
			cmd += "startup disk";
			applScriptCmd[6] = cmd;
			Runtime.getRuntime().exec(applScriptCmd);
		} else {
			throw new RuntimeException("showFileSystem method on OS [" + os + "] not implemented yet.\nFor now, you should open [" + where + "] by yourself.");
		}
	}

	public static int sign(double d) {
		int s = 0;
		if (d > 0.0D) {
			s = 1;
		} else if (d < 0.0D) {
			s = -1;
		}
		return s;
	}

	public static void makeSureTempExists() throws IOException {
		File dir = new File("temp");
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * remove leading and trailing blanks, CR, NL
	 *
	 * @param str
	 * @return
	 */
	public static String superTrim(String str) {
		String str2 = "";
		char[] strChar = str.toCharArray();
		// Leading
		int i = 0;
		while (strChar[i] == ' ' ||
				strChar[i] == '\n' ||
				strChar[i] == '\r') {
			i++;
		}
		str2 = str.substring(i);
		while (str2.endsWith("\n") || str2.endsWith("\r")) {
			str2 = str2.substring(0, str2.length() - 2);
		}
		return str2.trim();
	}

	public static String replaceString(String orig, String oldStr, String newStr) {
		String ret = orig;
		int indx = 0;
		for (boolean go = true; go; ) {
			indx = ret.indexOf(oldStr, indx);
			if (indx < 0) {
				go = false;
			} else {
				ret = ret.substring(0, indx) + newStr + ret.substring(indx + oldStr.length());
				indx += 1 + oldStr.length();
			}
		}
		return ret;
	}

	public static byte[] appendByte(byte[] c, byte b) {
		int newLength = c != null ? c.length + 1 : 1;
		byte newContent[] = new byte[newLength];
		for (int i = 0; i < newLength - 1; i++) {
			newContent[i] = c[i];
		}
		newContent[newLength - 1] = b;
		return newContent;
	}


	public static void shutdown() throws RuntimeException, IOException {
		String shutdownCommand;
		String operatingSystem = System.getProperty("os.name");

		if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem)) {
			shutdownCommand = "shutdown -h now";
		} else if ("Windows".equals(operatingSystem)) {
			shutdownCommand = "shutdown.exe -s -t 0";
		} else {
			throw new RuntimeException("Unsupported operating system.");
		}

		Runtime.getRuntime().exec(shutdownCommand); // Might require a sudo, hey...
		System.exit(0);
	}

	public static void reboot() throws RuntimeException, IOException {
		String shutdownCommand;
		String operatingSystem = System.getProperty("os.name");

		if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem)) {
			shutdownCommand = "sudo init 6";
		} else if ("Windows".equals(operatingSystem)) {
			// shutdownCommand = "shutdown.exe -s -t 0"; ???
			System.err.println("Oops! How to you do this on Windows?...");
			shutdownCommand = "echo 'Oops'";
		} else {
			throw new RuntimeException("Unsupported operating system.");
		}

		Runtime.getRuntime().exec(shutdownCommand); // Might require a sudo, hey...
		System.exit(0);
	}

	public static void main(String... args) throws Exception {
		String akeu = userInput("Tell me > ");
		System.out.println(akeu);
//  System.setProperty("os.name", "Mac OS X");
//  showFileSystem(System.getProperty("user.dir"));
	}
}
