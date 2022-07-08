package lcd.substitute;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import lcd.ScreenBuffer;
import lcd.utils.CharacterMatrixes;
import lcd.utils.LEDPanel;
import utils.StringUtils;

public class SwingLedPanel
				extends Frame {
	private SwingLedPanel instance = this;
	private LEDPanel ledPanel;
	private JPanel bottomPanel;
	private JCheckBox gridCheckBox;

	private transient ScreenBuffer sb;

	// private static int nbCols = -1;

	 public enum ScreenDefinition {

		SSD1306_128x32(128, 32),
		SSD1306_128x64(128, 64),
		NOKIA5110(84, 48);

		private int width;
		private int height;
		ScreenDefinition(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public int width() {
			return this.width;
		}
		public int height() {
			return this.height;
		}
	}

	Consumer<KeyEvent> onKeyPressed = (keyEvent) -> {};   // Empty, NoOp
	Consumer<KeyEvent> onKeyReleased = (keyEvent) -> {};  // Empty, NoOp
	Consumer<KeyEvent> onKeyTyped = (keyEvent) -> {};     // Empty, NoOp

	private ScreenDefinition config;
	// Default for SSD1306
	private int nbLines = 32;
	private int nbCols = 128;

	private int bufferSize = (nbCols * nbLines) / 8;

	private int[] buffer = null; // new int[bufferSize];

	public SwingLedPanel() {
		this(ScreenDefinition.SSD1306_128x32); // Default
	}

	public SwingLedPanel(ScreenDefinition config) {
		try {
			this.config = config;
			this.nbLines = this.config.height();
			this.nbCols = this.config.width();
			this.bufferSize = (this.nbCols * this.nbLines) / 8;
			this.buffer = new int[this.bufferSize];

			initComponents();
			int panelWidth = Math.round(1_000f * (this.nbCols / 128f));
			int panelHeight = Math.round(300f * (this.nbLines / 32f));

			this.setSize(new Dimension(panelWidth, panelHeight));
		} catch (HeadlessException he) {
			System.err.println(">> No Graphical Environment available <<");
		}
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		ledPanel = new LEDPanel(nbLines, nbCols);

		ledPanel.setWithGrid(false);

		int panelWidth = Math.round(1_000f * (this.nbCols / 128f));
		int panelHeight = Math.round(300f * (this.nbLines / 32f));

		setPreferredSize(new Dimension(panelWidth, panelHeight));
		setTitle("LCD Screen Buffer");
		addWindowListener(new WindowAdapter() {
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
		add(bottomPanel, java.awt.BorderLayout.SOUTH);
		pack();

		// Key listener
		this.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
//				System.out.println("Key typed," + e);
				onKeyTyped.accept(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
//				System.out.println("Key pressed," + e);
				onKeyPressed.accept(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
//				System.out.println("Key released," + e);
				onKeyReleased.accept(e);
			}
		});
	}

	public void setKeyTypedConsumer(Consumer<KeyEvent> consumer) {
		this.onKeyTyped = consumer;
	}
	public void setKeyPressedConsumer(Consumer<KeyEvent> consumer) {
		this.onKeyPressed = consumer;
	}
	public void setKeyReleasedConsumer(Consumer<KeyEvent> consumer) {
		this.onKeyReleased = consumer;
	}

	public void setLedColor(Color color) {
		ledPanel.setLedColor(color);
	}
	/**
	 * Simulator. Takes the screenbuffer expected by the real device and displays it on
	 * a led array (2 dims).
	 *
	 * @param screenbuffer as expected by the device.
	 */
	public void setBuffer(int[] screenbuffer) {
		// This displays the buffer top to bottom, instead of left to right
		char[][] screenMatrix = new char[nbLines][nbCols];
		for (int i = 0; i < nbCols; i++) {
			// Line is a vertical line, its length is nbLines / 8
			String line = ""; /*lpad(Integer.toBinaryString(screenbuffer[i + (3 * nbCols)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " +
	                  lpad(Integer.toBinaryString(screenbuffer[i + (2 * nbCols)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " +
                    lpad(Integer.toBinaryString(screenbuffer[i + (1 * nbCols)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " +
                    lpad(Integer.toBinaryString(screenbuffer[i + (0 * nbCols)]), "0", 8).replace('0', ' ').replace('1', 'X'); */
			for (int l = (nbLines / 8) - 1; l >= 0; l--)
				line += StringUtils.lpad(Integer.toBinaryString(screenbuffer[i + (l * nbCols)]), 8, "0").replace('0', ' ').replace('1', 'X');

//    System.out.println(line);
//    for (int c=0; c<Math.min(line.length(), nbCols); c++)
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
		for (int i = 0; i < nbLines; i++)
		// for (int i=31; i>=0; i--)
		{
			for (int j = 0; j < nbCols; j++)
				matrix[j][nbLines - 1 - i] = (screenMatrix[i][j] == 'X' ? true : false);
		}
		ledPanel.setLedOnOff(matrix);
	}

	public void display() {
		ledPanel.repaint();
	}

	public void displayTest() {
		SwingLedPanel lcd = instance;
		if (sb == null) {
			sb = new ScreenBuffer(nbCols, nbLines);
			sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		}

		sb.text("ScreenBuffer", 2, 9, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text(nbCols + " x " + nbLines + " for LCD", 2, 19, ScreenBuffer.Mode.BLACK_ON_WHITE);
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
	 * @param args the command line arguments (unused)
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

		SwingLedPanel lp = new SwingLedPanel(ScreenDefinition.NOKIA5110);
		lp.setVisible(true);
		lp.displayTest();
	}
}
