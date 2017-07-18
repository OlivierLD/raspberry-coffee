package nmea.forwarders.substitute;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import lcd.ScreenBuffer;
import lcd.utils.CharacterMatrixes;
import lcd.utils.LEDPanel;
import utils.StringUtils;

@SuppressWarnings("oracle.jdeveloper.java.serialversionuid-field-missing")
public class SwingLedPanel
				extends java.awt.Frame {
	private SwingLedPanel instance = this;
	private LEDPanel ledPanel;
	private JPanel bottomPanel;
	private JCheckBox gridCheckBox;

	private transient ScreenBuffer sb;

	private static int nbCols = -1;

	// SSD1306
	private final static int NB_LINES = 32;
	private final static int NB_COLS = 128;
	// Nokia
//  private final static int NB_LINES = 48;
//  private final static int NB_COLS  = 84;

	private final static int BUFFER_SIZE = (NB_COLS * NB_LINES) / 8;

	private static int[] buffer = new int[BUFFER_SIZE];

	public SwingLedPanel() {
		initComponents();
		this.setSize(new Dimension(1_000, 300));
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		ledPanel = new LEDPanel(NB_LINES, NB_COLS);

		ledPanel.setWithGrid(false);

		setPreferredSize(new Dimension(1_000, 600));
		setTitle("LCD Screen Buffer");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});
		add(ledPanel, java.awt.BorderLayout.CENTER);

		bottomPanel = new JPanel();
		gridCheckBox = new JCheckBox("With Grid");
		gridCheckBox.setSelected(false);
		bottomPanel.add(gridCheckBox, null);
		gridCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				ledPanel.setWithGrid(gridCheckBox.isSelected());
				ledPanel.repaint();
			}
		});
		add(bottomPanel, java.awt.BorderLayout.SOUTH);
		pack();
	}

	/**
	 * Simulator. Takes the screenbuffer expected by the real device and displays it on
	 * a led array (2 dims).
	 *
	 * @param screenbuffer as expected by the device.
	 */
	public void setBuffer(int[] screenbuffer) {
		// This displays the buffer top to bottom, instead of left to right
		char[][] screenMatrix = new char[NB_LINES][NB_COLS];
		for (int i = 0; i < NB_COLS; i++) {
			// Line is a vertical line, its length is NB_LINES / 8
			String line = ""; /*lpad(Integer.toBinaryString(screenbuffer[i + (3 * NB_COLS)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " +
	                  lpad(Integer.toBinaryString(screenbuffer[i + (2 * NB_COLS)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " +
                    lpad(Integer.toBinaryString(screenbuffer[i + (1 * NB_COLS)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " + 
                    lpad(Integer.toBinaryString(screenbuffer[i + (0 * NB_COLS)]), "0", 8).replace('0', ' ').replace('1', 'X'); */
			for (int l = (NB_LINES / 8) - 1; l >= 0; l--)
				line += StringUtils.lpad(Integer.toBinaryString(screenbuffer[i + (l * NB_COLS)]), 8, "0").replace('0', ' ').replace('1', 'X');

//    System.out.println(line);
//    for (int c=0; c<Math.min(line.length(), NB_COLS); c++)
			for (int c = 0; c < line.length(); c++) {
				try {
					char mc = line.charAt(c);
					screenMatrix[c][i] = mc;
				} catch (Exception ex) {
					System.out.println("Line:" + line + " (" + line.length() + " character(s))");
					System.out.println("c:" + c + ", i=" + i + ", buffer length:" + buffer.length);
					ex.printStackTrace();
				}
			}
		}
		// Display the screen matrix, as it should be seen
		boolean[][] matrix = ledPanel.getLedOnOff();
		for (int i = 0; i < NB_LINES; i++)
		// for (int i=31; i>=0; i--)
		{
			for (int j = 0; j < NB_COLS; j++)
				matrix[j][NB_LINES - 1 - i] = (screenMatrix[i][j] == 'X' ? true : false);
		}
		ledPanel.setLedOnOff(matrix);
	}

	public void display() {
		ledPanel.repaint();
	}

	public void displayTest() {
		SwingLedPanel lcd = instance;
		if (sb == null) {
			sb = new ScreenBuffer(NB_COLS, NB_LINES);
			sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		}

		sb.text("ScreenBuffer", 2, 9, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text(NB_COLS + " x " + NB_LINES + " for LCD", 2, 19, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("I speak Java!", 2, 29, ScreenBuffer.Mode.BLACK_ON_WHITE);

		lcd.setBuffer(sb.getScreenBuffer());
		lcd.display();
		System.out.println("...Done!");
	}

	/**
	 * Exit the Application
	 */
	private void exitForm(@SuppressWarnings("oracle.jdeveloper.java.unused-parameter") java.awt.event.WindowEvent evt) {
		System.out.println("Bye");
		System.exit(0);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		// Available characters:
		Map<String, String[]> characters = CharacterMatrixes.characters;
		Set<String> keys = characters.keySet();
		List<String> kList = new ArrayList<String>(keys.size());
		for (String k : keys)
			kList.add(k);
		// Sort here
		Collections.sort(kList);
		for (String k : kList)
			System.out.print(k + " ");
		System.out.println();

		// Params
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if ("-col".equals(args[i]))
					nbCols = Integer.parseInt(args[i + 1]);
			}
		}

		SwingLedPanel lp = new SwingLedPanel();
		lp.setVisible(true);
		lp.displayTest();
	}
}
