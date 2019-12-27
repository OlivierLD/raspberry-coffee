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
import java.io.File;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created to isolate the Swing related utilities from the rest, so
 * the others can be imported from system that do not support Swing (like Android).
 */
public class SwingStaticUtil {

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
//		String os = System.getProperty("os.name");
//		if (os.indexOf("Windows") > -1) {
//			String cmd = "";
//			if (page.indexOf(" ") != -1) {
//				cmd = "cmd /k start \"" + page + "\"";
//			} else {
//				cmd = "cmd /k start " + page + "";
//			}
//			System.out.println("Command:" + cmd);
//			Runtime.getRuntime().exec(cmd); // Can contain blanks...
//		} else if (os.indexOf("Linux") > -1) { // Assuming htmlview
//			Runtime.getRuntime().exec("htmlview " + page);
//		} else {
//			throw new RuntimeException("OS [" + os + "] not supported yet");
//		}
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
				filters = new Hashtable<>(5);
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
						fullDescription += ("." + extensions.nextElement());
						while (extensions.hasMoreElements()) {
							fullDescription += (", " + extensions.nextElement());
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
}
