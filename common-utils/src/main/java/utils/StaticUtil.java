package utils;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticUtil {

	private static final InputStreamReader inputStream = new InputStreamReader(System.in);
	private static final BufferedReader stdin = new BufferedReader(inputStream);

	public static String userInput(String prompt) {
		String retString = "";
		System.err.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			System.out.println(e);
			try {
				userInput("<Oooch/>");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return retString;
	}

	public static byte[] appendByteArrays(byte c[], byte b[], int n) {
		int newLength = c != null ? c.length + n : n;
		byte newContent[] = new byte[newLength];
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
		if (System.getProperty("os.name").indexOf("Windows") > -1) {
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
		} else
			macAddress = "Unknown";
		return macAddress;
	}

	//@SuppressWarnings("unchecked")
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

	public static void openInBrowser(String page) throws Exception {
		URI uri = new URI(page);
		try {
			//  System.out.println("Opening in browser:[" + uri.toString() + "]");
			Desktop.getDesktop().browse(uri);
		} catch (Exception ex) { // UnsupportedOperationException ex)
			String mess = ex.getMessage();
			mess += ("\n\nUnsupported operation on your system. URL [" + uri.toString() + "] is in the clipboard.\nOpen your browser manually, and paste it in there (Ctrl+V).");
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			String path = uri.toString();
			try {
				File f = new File(page);
				if (f.exists()) {
					path = f.getAbsolutePath();
					if (File.separatorChar != '/') {
						path = path.replace(File.separatorChar, '/');
					}
					if (!path.startsWith("/")) {
						path = "/" + path;
					}
					path = "file:" + path;
				}
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
			StringSelection stringSelection = new StringSelection(path);
			clipboard.setContents(stringSelection, null);
			JOptionPane.showMessageDialog(null, mess, "Showing in Browser", JOptionPane.ERROR_MESSAGE);
		}
//    String os = System.getProperty("os.name");
//    if (os.indexOf("Windows") > -1)
//    {
//      String cmd = "";
//      if (page.indexOf(" ") != -1)
//        cmd = "cmd /k start \"" + page + "\"";
//      else
//        cmd = "cmd /k start " + page + "";
//      System.out.println("Command:" + cmd);
//      Runtime.getRuntime().exec(cmd); // Can contain blanks...
//    }
//    else if (os.indexOf("Linux") > -1) // Assuming htmlview
//      Runtime.getRuntime().exec("htmlview " + page);
//    else
//    {
//      throw new RuntimeException("OS [" + os + "] not supported yet");
//    }
	}

	public static void showFileSystem(String where) throws Exception {
		String os = System.getProperty("os.name");
		if (os.indexOf("Windows") > -1) {
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

	public static byte[] appendByte(byte c[], byte b) {
		int newLength = c != null ? c.length + 1 : 1;
		byte newContent[] = new byte[newLength];
		for (int i = 0; i < newLength - 1; i++) {
			newContent[i] = c[i];
		}
		newContent[newLength - 1] = b;
		return newContent;
	}

	public static String chooseFile(int mode,
	                                String flt,
	                                String desc,
	                                String title,
	                                String buttonLabel) {
		String fileName = "";
		JFileChooser chooser = new JFileChooser();
		if (title != null) {
			chooser.setDialogTitle(title);
		}
		if (buttonLabel != null) {
			chooser.setApproveButtonText(buttonLabel);
		}
		if (flt != null) {
			ToolFileFilter filter = new ToolFileFilter(flt, desc);
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
		}
		chooser.setFileSelectionMode(mode);
		// Set current directory
		File f = new File(".");
		String currPath = f.getAbsolutePath();
		f = new File(currPath.substring(0, currPath.lastIndexOf(File.separator)));
		chooser.setCurrentDirectory(f);

		int retval = chooser.showOpenDialog(null);
		switch (retval) {
			case JFileChooser.APPROVE_OPTION:
				fileName = chooser.getSelectedFile().toString();
				break;
			case JFileChooser.CANCEL_OPTION:
				break;
			case JFileChooser.ERROR_OPTION:
				break;
		}
		return fileName;
	}

	public static int drawPanelTable(String[][] data, Graphics gr, Point topLeft, int betweenCols, int betweenRows) {
		return drawPanelTable(data, gr, topLeft, betweenCols, betweenRows, null);
	}

	public final static int LEFT_ALIGNED = 0;
	public final static int RIGHT_ALIGNED = 1;
	public final static int CENTER_ALIGNED = 2;

	public static int drawPanelTable(String[][] data, Graphics gr, Point topLeft, int betweenCols, int betweenRows, int[] colAlignment) {
		return drawPanelTable(data, gr, topLeft, betweenCols, betweenRows, colAlignment, false, null, 0f);
	}

	public static int drawPanelTable(String[][] data, Graphics gr, Point topLeft, int betweenCols, int betweenRows, int[] colAlignment, boolean paintBackground, Color bgColor, float bgTransparency) {
		return drawPanelTable(data, gr, topLeft, betweenCols, betweenRows, colAlignment, paintBackground, bgColor, null, bgTransparency, 1f);
	}

	public static int drawPanelTable(String[][] data,
	                                 Graphics gr,
	                                 Point topLeft,
	                                 int betweenCols,
	                                 int betweenRows,
	                                 int[] colAlignment,
	                                 boolean paintBackground,
	                                 Color bgLightColor,
	                                 Color bgDarkColor,
	                                 float bgTransparency,
	                                 float textTransparency) {
		int w = 0, h = 0;

		Font f = gr.getFont();
		int[] maxLength = new int[data[0].length]; // Max length for each column
		for (int i = 0; i < maxLength.length; i++) { // init. All to 0
			maxLength[i] = 0;
		}
		// Identify the max length for each column
		for (int row = 0; row < data.length; row++) {
			for (int col = 0; col < data[row].length; col++) {
				int strWidth = gr.getFontMetrics(f).stringWidth(data[row][col]);
				maxLength[col] = Math.max(maxLength[col], strWidth);
			}
		}
		int x = topLeft.x;
		int y = topLeft.y;

		w = betweenCols;
		for (int i = 0; i < maxLength.length; i++) {
			w += (maxLength[i] + betweenCols);
		}
		h = betweenRows + (data.length * (f.getSize() + betweenRows)) + betweenRows;

		if (paintBackground) { // Glossy
			boolean glossy = (bgLightColor != null && bgDarkColor != null);
			Color c = gr.getColor();
			if (glossy) {
				drawGlossyRectangularDisplay((Graphics2D) gr,
						new Point(x - betweenCols, y - f.getSize() - betweenRows),
						new Point(x - betweenCols + w, y - f.getSize() - betweenRows + h),
						bgLightColor,
						bgDarkColor,
						bgTransparency);
			} else {
				gr.setColor(bgLightColor);
				gr.fillRoundRect(x - betweenCols, y - f.getSize() - betweenRows, w, h, 10, 10);
			}
			gr.setColor(c);
		}
		((Graphics2D) gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textTransparency));
		// Now display
		for (int row = 0; row < data.length; row++) {
			for (int col = 0; col < data[row].length; col++) {
				int _x = x;
				for (int c = 1; c <= col; c++) {
					_x += (betweenCols + maxLength[c - 1]);
				}
				if (colAlignment != null && colAlignment[col] != LEFT_ALIGNED) {
					int strWidth = gr.getFontMetrics(f).stringWidth(data[row][col]);
					switch (colAlignment[col]) {
						case RIGHT_ALIGNED:
							_x += (maxLength[col] - strWidth);
							break;
						case CENTER_ALIGNED:
							_x += ((maxLength[col] - strWidth) / 2);
							break;
						default:
							break;
					}
				}
				gr.drawString(data[row][col], _x, y);
			}
			y += (f.getSize() + betweenRows);
		}
		return y;
	}

	private static void drawGlossyRectangularDisplay(Graphics2D g2d, Point topLeft, Point bottomRight, Color lightColor, Color darkColor, float transparency) {
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
		g2d.setPaint(null);

		g2d.setColor(darkColor);

		int width = bottomRight.x - topLeft.x;
		int height = bottomRight.y - topLeft.y;

		g2d.fillRoundRect(topLeft.x, topLeft.y, width, height, 10, 10);

		Point gradientOrigin = new Point(topLeft.x + (width) / 2, topLeft.y);
		GradientPaint gradient = new GradientPaint(gradientOrigin.x,
				gradientOrigin.y,
				lightColor,
				gradientOrigin.x,
				gradientOrigin.y + (height / 3),
				darkColor); // vertical, light on top
		g2d.setPaint(gradient);
		int offset = 3;
		int arcRadius = 5;
		g2d.fillRoundRect(topLeft.x + offset, topLeft.y + offset, (width - (2 * offset)), (height - (2 * offset)), 2 * arcRadius, 2 * arcRadius);
	}

	public static boolean thisClassVerbose(Class c) {
		return (System.getProperty(c.getName() + ".verbose", "false").equals("true") || System.getProperty("all.verbose", "false").equals("true"));
	}

	static class ToolFileFilter extends FileFilter {
		private Hashtable<String, FileFilter> filters = null;
		private String description = null;
		private String fullDescription = null;
		private boolean useExtensionsInDescription = true;

		public ToolFileFilter() {
			this((String) null, (String) null);
		}

		public ToolFileFilter(String extension) {
			this(extension, null);
		}

		public ToolFileFilter(String extension, String description) {
			this(new String[]{extension}, description);
		}

		public ToolFileFilter(String[] filters) {
			this(filters, null);
		}

		public ToolFileFilter(String[] filters, String description) {
			this.filters = new Hashtable<String, FileFilter>(filters.length);
			for (int i = 0; i < filters.length; i++) {
				// add filters one by one
				addExtension(filters[i]);
			}
			setDescription(description);
		}

		public boolean accept(File f) {
			if (f != null) {
				if (f.isDirectory()) {
					return true;
				}
				String extension = getExtension(f);
				if (extension != null && filters.get(getExtension(f)) != null) {
					return true;
				}
			}
			return false;
		}

		public String getExtension(File f) {
			if (f != null) {
				String filename = f.getName();
				int i = filename.lastIndexOf('.');
				if (i > 0 && i < filename.length() - 1) {
					return filename.substring(i + 1).toLowerCase();
				}
			}
			return null;
		}

		public void addExtension(String extension) {
			if (filters == null) {
				filters = new Hashtable<String, FileFilter>(5);
			}
			filters.put(extension.toLowerCase(), this);
			fullDescription = null;
		}

		public String getDescription() {
			if (fullDescription == null) {
				if (description == null || isExtensionListInDescription()) {
					if (description != null) {
						fullDescription = description;
					}
					fullDescription += " (";
					// build the description from the extension list
					Enumeration extensions = filters.keys();
					if (extensions != null) {
						fullDescription += "." + (String) extensions.nextElement();
						while (extensions.hasMoreElements()) {
							fullDescription += ", " + (String) extensions.nextElement();
						}
					}
					fullDescription += ")";
				} else {
					fullDescription = description;
				}
			}
			return fullDescription;
		}

		public void setDescription(String description) {
			this.description = description;
			fullDescription = null;
		}

		public void setExtensionListInDescription(boolean b) {
			useExtensionsInDescription = b;
			fullDescription = null;
		}

		public boolean isExtensionListInDescription() {
			return useExtensionsInDescription;
		}
	}

	public static void shutdown() throws RuntimeException, IOException {
		String shutdownCommand;
		String operatingSystem = System.getProperty("os.name");

		if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem)) {
			shutdownCommand = "shutdown -h now";
		}
		else if ("Windows".equals(operatingSystem)) {
			shutdownCommand = "shutdown.exe -s -t 0";
		}
		else {
			throw new RuntimeException("Unsupported operating system.");
		}

		Runtime.getRuntime().exec(shutdownCommand);
		System.exit(0);
	}

	public static void main(String... args) throws Exception {
		String akeu = userInput("Tell me > ");
		System.out.println(akeu);
//  System.setProperty("os.name", "Mac OS X");
//  showFileSystem(System.getProperty("user.dir"));
	}
}
