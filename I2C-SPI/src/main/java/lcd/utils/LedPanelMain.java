package lcd.utils;

import lcd.ScreenBuffer;
import lcd.utils.img.ImgInterface;
import lcd.utils.img.Java32x32;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;

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

import utils.StringUtils;

public class LedPanelMain
		extends java.awt.Frame {
	private LedPanelMain instance = this;
	private LEDPanel ledPanel;
	private JPanel bottomPanel;
	private JCheckBox gridCheckBox;
	private JButton againButton;

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

	public LedPanelMain() {
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

		setPreferredSize(new java.awt.Dimension(1_000, 600));
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
		LedPanelMain lcd = instance;
		againButton.setEnabled(false);
//  instance.repaint();
		if (true) {
			if (sb == null) {
				sb = new ScreenBuffer(NB_COLS, NB_LINES);
				sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
			}

			if (true) {
				sb.text("ScreenBuffer", 2, 9, ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text(NB_COLS + " x " + NB_LINES + " for LCD", 2, 19, ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text("I speak Java!", 2, 29, ScreenBuffer.Mode.BLACK_ON_WHITE);

				lcd.setBuffer(sb.getScreenBuffer());

				lcd.display();
				//     sb.dumpScreen();
				try {
					Thread.sleep(5_000);
				} catch (Exception ex) {
				}

				int[] mirror = ScreenBuffer.mirror(sb.getScreenBuffer(), NB_COLS, NB_LINES);

				lcd.setBuffer(mirror);

				lcd.display();
				//     sb.dumpScreen();
				try {
					Thread.sleep(5_000);
				} catch (Exception ex) {
				}

				// Bigger
				sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("Pi = ", 2, 9, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("3.1415926\u00b0", 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK); // With a useless degree symbol (for tests).
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				//   sb.dumpScreen();
				try {
					Thread.sleep(5_000);
				} catch (Exception ex) {
				}

				// Blinking
				sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("ScreenBuffer", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text(NB_COLS + " x " + NB_LINES + " for LCD", 2, 19, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("I speak Java!", 2, 29, ScreenBuffer.Mode.WHITE_ON_BLACK);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(50);
				} catch (Exception ex) {
				}

				sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text("ScreenBuffer", 2, 9, ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text(NB_COLS + " x " + NB_LINES + " for LCD", 2, 19, ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text("I speak Java!", 2, 29, ScreenBuffer.Mode.BLACK_ON_WHITE);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(50);
				} catch (Exception ex) {
				}

				sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("ScreenBuffer", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text(NB_COLS + " x " + NB_LINES + " for LCD", 2, 19, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("I speak Java!", 2, 29, ScreenBuffer.Mode.WHITE_ON_BLACK);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(50);
				} catch (Exception ex) {
				}

				sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text("ScreenBuffer", 2, 9, ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text(NB_COLS + " x " + NB_LINES + " for LCD", 2, 19, ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text("I speak Java!", 2, 29, ScreenBuffer.Mode.BLACK_ON_WHITE);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(50);
				} catch (Exception ex) {
				}

				sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("ScreenBuffer", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text(NB_COLS + " x " + NB_LINES + " for LCD", 2, 19, ScreenBuffer.Mode.WHITE_ON_BLACK);
				sb.text("I speak Java!", 2, 29, ScreenBuffer.Mode.WHITE_ON_BLACK);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(2_000);
				} catch (Exception ex) {
				}

				// End blinking
			}

			if (false) {
				String[] txt1 = new String[]{
						"!\":#$%&'()*+,-./01234",
						"56789;<=>?@ABCDEFGHI",
						"JKLMNOPQRSTUVWXYZ[\\]"
				};
				String[] txt2 = new String[]{
						"^_abcdefghijklmnopqr",
						"stuvwxyz{|}"
				};

				boolean one = false;

				for (int t = 0; t < 4; t++) {
					sb.clear();
					String[] sa = one ? txt1 : txt2;
					for (int i = 0; i < sa.length; i++)
						sb.text(sa[i], 0, 10 + (i * 10));
					lcd.setBuffer(sb.getScreenBuffer());
					lcd.display();
					one = !one;
					try {
						Thread.sleep(2_000);
					} catch (Exception ex) {
					}
				}
			}

			// Image + text, marquee
			if (false) {
				sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
				ImgInterface img = new Java32x32();
				sb.image(img, 0, 0, ScreenBuffer.Mode.BLACK_ON_WHITE);
				sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);

				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(2_000);
				} catch (Exception ex) {
				}

				sb.clear();
				for (int x = 0; x < 128; x++) {
					sb.image(img, 0 - x, 0);
					sb.text("I speak Java!.....", 36 - x, 20);

					lcd.setBuffer(sb.getScreenBuffer());
					lcd.display();
					long s = (long) (150 - (1.5 * x));
					try {
						Thread.sleep(s > 0 ? s : 0);
					} catch (Exception ex) {
					}
				}
			}

			// Circles
			if (false) {
				sb.clear();
				sb.circle(64, 16, 15);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}

				sb.circle(74, 16, 10);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}

				sb.circle(80, 16, 5);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Lines
			if (false) {
				sb.clear();
				sb.line(1, 1, 126, 30);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}

				sb.line(126, 1, 1, 30);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}

				sb.line(1, 25, 120, 10);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}

				sb.line(10, 5, 10, 30);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}

				sb.line(1, 5, 120, 5);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Rectangle
			if (false) {
				sb.clear();
				sb.rectangle(5, 10, 100, 25);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}

				sb.rectangle(15, 3, 50, 30);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Nested rectangles
			if (true) {
				sb.clear();
				for (int i = 0; i < 8; i++) {
					sb.rectangle(1 + (i * 2), 1 + (i * 2), 127 - (i * 2), 31 - (i * 2));
					lcd.setBuffer(sb.getScreenBuffer());
					lcd.display();
					try {
						Thread.sleep(100);
					} catch (Exception ex) {
					}
				}
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Arc
			if (false) {
				sb.clear();
				sb.arc(64, 16, 10, 20, 90);
				sb.plot(64, 16);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Shape
			if (true) {
				sb.clear();
				int[] x = new int[]{64, 73, 50, 78, 55};
				int[] y = new int[]{1, 30, 12, 12, 30};
				Polygon p = new Polygon(x, y, 5);
				sb.shape(p, true);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Centered text
			if (true) {
				sb.clear();
				String txt = "Centered";
				int len = sb.strlen(txt);
				sb.text(txt, 64 - (len / 2), 16);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
				// sb.clear();
				txt = "A much longer string.";
				len = sb.strlen(txt);
				sb.text(txt, 64 - (len / 2), 26);
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Vertical marquee
			if (true) {
				String[] txt = new String[]{
						"Centered",
						"This is line one",
						"More text goes here",
						"Some crap follows: ...",
						"We're reaching the end",
						"* The End *"
				};
				int len = 0;
				for (int t = 0; t < 80; t++) {
					sb.clear();
					for (int i = 0; i < txt.length; i++) {
						len = sb.strlen(txt[i]);
						sb.text(txt[i], 64 - (len / 2), (10 * (i + 1)) - t);
						lcd.setBuffer(sb.getScreenBuffer());
						lcd.display();
					}
					try {
						Thread.sleep(100);
					} catch (Exception ex) {
					}
				}
//      sb.dumpScreen();

				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			if (true) {
				// Text Snake...
				String snake = "This text is displayed like a snake, waving across the screen...";
				char[] ca = snake.toCharArray();
				int strlen = sb.strlen(snake);
				// int i = 0;
				for (int i = 0; i < strlen + 2; i++) {
					sb.clear();
					for (int c = 0; c < ca.length; c++) {
						int strOffset = 0;
						if (c > 0) {
							String tmp = new String(ca, 0, c);
							//    System.out.println(tmp);
							strOffset = sb.strlen(tmp) + 2;
						}
						double virtualAngle = Math.PI * (((c - i) % 32) / 32d);
						int x = strOffset - i,
								y = 26 + (int) (16 * Math.sin(virtualAngle));
//          System.out.println("Displaying " + ca[c] + " at " + x + ", " + y + ", i=" + i + ", strOffset=" + strOffset);
						sb.text(new String(new char[]{ca[c]}), x, y);
					}
					lcd.setBuffer(sb.getScreenBuffer());
					lcd.display();
					try {
						Thread.sleep(75);
					} catch (Exception ex) {
					}
				}
			}

			// Curve
			if (true) {
				sb.clear();
				// Axis
				sb.line(0, 16, 128, 16);
				sb.line(2, 0, 2, 32);

				Point prev = null;
				for (int x = 0; x < 130; x++) {
					double amplitude = 6 * Math.exp((double) (130 - x) / (13d * 7.5d));
					//    System.out.println("X:" + x + ", ampl: " + (amplitude));
					int y = 16 - (int) (amplitude * Math.cos(Math.toRadians(360 * x / 16d)));
					sb.plot(x + 2, y);
					if (prev != null)
						sb.line(prev.x, prev.y, x + 2, y);
					prev = new Point(x + 2, y);
				}
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Progressing Curve
			if (true) {
				sb.clear();
				// Axis
				sb.line(0, 16, 128, 16);
				sb.line(2, 0, 2, 32);

				Point prev = null;
				for (int x = 0; x < 130; x++) {
					double amplitude = 6 * Math.exp((double) (130 - x) / (13d * 7.5d));
					//  System.out.println("X:" + x + ", ampl: " + (amplitude));
					int y = 16 - (int) (amplitude * Math.cos(Math.toRadians(360 * x / 16d)));
					sb.plot(x + 2, y);
					if (prev != null)
						sb.line(prev.x, prev.y, x + 2, y);
					prev = new Point(x + 2, y);
					lcd.setBuffer(sb.getScreenBuffer());
					lcd.display();
					try {
						Thread.sleep(75);
					} catch (Exception ex) {
					}
				}
				lcd.setBuffer(sb.getScreenBuffer());
				lcd.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}

			// Bouncing
			if (true) {
				sb.clear();
				for (int x = 0; x < 130; x++) {
					sb.clear();
					double amplitude = 6 * Math.exp((double) (130 - x) / (13d * 7.5d));
					//  System.out.println("X:" + x + ", ampl: " + (amplitude));
					int y = 32 - (int) (amplitude * Math.abs(Math.cos(Math.toRadians(180 * x / 10d))));
					// 4 dots
					sb.plot(x, y);
					sb.plot(x + 1, y);
					sb.plot(x + 1, y + 1);
					sb.plot(x, y + 1);

					lcd.setBuffer(sb.getScreenBuffer());
					lcd.display();
					try {
						Thread.sleep(75);
					} catch (Exception ex) {
					}
				}
				//  oled.setBuffer(sb.getScreenBuffer());
				//  oled.display();
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
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

		LedPanelMain lp = new LedPanelMain();
		lp.setVisible(true);
		lp.doYourJob();
	}
}
