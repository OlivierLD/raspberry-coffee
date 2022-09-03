package lcd.utils;

import lcd.ScreenBuffer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.StringUtils;

public class LedPanelMain2 extends java.awt.Frame {
	private final LedPanelMain2 instance = this;
	private LEDPanel ledPanel;
	private JPanel bottomPanel;
	private JCheckBox gridCheckBox;
	private JButton againButton;

	private transient ScreenBuffer sb;

	private static int nbCols = -1;

	// SSD1306
//private final static int NB_LINES = 32;
	private final static int NB_LINES = 64;
	private final static int NB_COLS = 128;
	// Nokia
//  private final static int NB_LINES = 48;
//  private final static int NB_COLS  = 84;

	private final static int BUFFER_SIZE = (NB_COLS * NB_LINES) / 8;

	private final static int[] buffer = new int[BUFFER_SIZE];

	public LedPanelMain2() {
		initComponents();
		this.setSize(new Dimension(1_000, (NB_LINES == 32 ? 300 : 600))); // 300 * (NB_LINES / 32) ?
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		ledPanel = new LEDPanel(NB_LINES, NB_COLS);
		ledPanel.setWithGrid(false);
		ledPanel.setLedColor(Color.WHITE);

		setPreferredSize(new Dimension(1_000, (NB_LINES == 32 ? 300 : 600)));
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
		gridCheckBox.addActionListener(actionEvent -> {
			ledPanel.setWithGrid(gridCheckBox.isSelected());
			ledPanel.repaint();
		});
		againButton = new JButton("Play again");
		bottomPanel.add(againButton, null);
		againButton.addActionListener(actionEvent -> {
			Thread go = new Thread(() -> {
					instance.doYourJob();
				});
			go.start();
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
		for (int i = 0; i < NB_LINES; i++) {
			for (int j = 0; j < NB_COLS; j++) {
				matrix[j][NB_LINES - 1 - i] = (screenMatrix[i][j] == 'X');
			}
		}
		ledPanel.setLedOnOff(matrix);
	}

	private void display() {
		ledPanel.repaint();
	}

	public void doYourJob() {
		LedPanelMain2 lcd = instance;
		againButton.setEnabled(false);
//  instance.repaint();
		if (true) {
			if (sb == null) {
				sb = new ScreenBuffer(NB_COLS, NB_LINES);
				sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
			}

			if (true) {
				int centerX = 80, centerY = 16, radius = 15;

				int[] twd = new int[]{
						0, 10, 20, 30, 40, 50, 60, 70, 80, 90,
						100, 110, 120, 130, 140, 150, 160, 170, 180,
						190, 200, 210, 220, 230, 240, 250, 260, 270,
						280, 290, 300, 310, 320, 330, 340, 350, 360};

				for (int d : twd) {
					// Bigger
					sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

					sb.text("TWD ", 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
					sb.text(String.valueOf(d) + "\u00b0", 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK); // With a useless degree symbol (for tests).

					// Circle
					sb.circle(centerX, centerY, radius);

					// Hand
					int toX = centerX - (int) Math.round(radius * Math.sin(Math.toRadians(180 + d)));
					int toY = centerY + (int) Math.round(radius * Math.cos(Math.toRadians(180 + d)));
					sb.line(centerX, centerY, toX, toY);

					// Display
					lcd.setBuffer(sb.getScreenBuffer());
					lcd.display();
					try {
						Thread.sleep(1_000);
					} catch (Exception ex) {
					}
				}
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
	 * @param args the command line arguments
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

		LedPanelMain2 lp = new LedPanelMain2();
		lp.setVisible(true);
		lp.doYourJob();
	}
}
