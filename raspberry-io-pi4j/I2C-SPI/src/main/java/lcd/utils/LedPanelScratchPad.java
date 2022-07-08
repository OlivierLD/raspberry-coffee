package lcd.utils;

import lcd.ScreenBuffer;
import utils.StringUtils;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LedPanelScratchPad
		extends java.awt.Frame {
	private LedPanelScratchPad instance = this;
	private LEDPanel ledPanel;
	private JPanel bottomPanel;
	private JCheckBox gridCheckBox;
	private JButton againButton;

	private transient ScreenBuffer sb;

	private static int nbCols = -1;

	// SSD1306 128x32
//	private final static int NB_LINES = 32;
//	private final static int NB_COLS = 128;
	// SSD1306 128x64
	private final static int NB_LINES = 64;
	private final static int NB_COLS = 128;
	// Nokia
//  private final static int NB_LINES = 48;
//  private final static int NB_COLS  = 84;

	private final static int BUFFER_SIZE = (NB_COLS * NB_LINES) / 32;

	private static int[] buffer = new int[BUFFER_SIZE];

	public LedPanelScratchPad() {
		initComponents();
		this.setSize(new Dimension(1_000, (300 * (NB_LINES / 32))));
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		ledPanel = new LEDPanel(NB_LINES, NB_COLS);
		ledPanel.setLedColor(Color.WHITE);
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
		againButton = new JButton("Play again");
		bottomPanel.add(againButton, null);
		againButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				Thread go = new Thread() {
					public void run() {
						instance.doYourJob();
					}
				};
				go.start();
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
	private void setBuffer(int[] screenbuffer) {
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

	private void display() {
		ledPanel.repaint();
	}

	public void doYourJob() {
		LedPanelScratchPad lcd = instance;
		againButton.setEnabled(false);
//  instance.repaint();
		if (true) {
			if (sb == null) {
				sb = new ScreenBuffer(NB_COLS, NB_LINES);
				sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
			}

			if (false) { // 128 x 32
				sb.text("<- A - Relay status", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("<- B - Relay ON", 2, 19, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("<- C - Relay OFF", 2, 29, ScreenBuffer.Mode.WHITE_ON_BLACK);

				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				//     sb.dumpScreen();
			}

			if (true) { // 128 x 64
				sb.text("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 1, 8, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("abcdfeghijklmnopqrstuvwxyz", 1, 16, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("012345678901234567890", 1, 25, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}", 1, 33, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text(":;<=>?@[\\]^_{|}", 1, 43, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("012345678901234567890", 1, 53, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("Hello SSD1306 world!..", 1, 62, ScreenBuffer.Mode.WHITE_ON_BLACK);

				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				//     sb.dumpScreen();
			}

			againButton.setEnabled(true);
			System.out.println("...Done!");
		}
	}

	/**
	 * Exit the Application
	 */
	private void exitForm(java.awt.event.WindowEvent evt) {
		System.out.println("Bye");
		System.exit(0);
	}

	/**
	 * @param args the command line arguments. Optional: -col XX
	 */
	public static void main(String... args) {
		// Available characters:
		Map<String, String[]> characters = CharacterMatrixes.characters;
		Set<String> keys = characters.keySet();
		List<String> kList = new ArrayList<>(keys.size());
		for (String k : keys) {
			kList.add(k);
		}
		// Sort here
		Collections.sort(kList);
		for (String k : kList) {
			System.out.print(k + " ");
		}
		System.out.println();

		// Params
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if ("-col".equals(args[i])) {
					nbCols = Integer.parseInt(args[i + 1]);
				}
			}
		}

		LedPanelScratchPad lp = new LedPanelScratchPad();
		lp.setVisible(true);
		lp.doYourJob();
	}
}
