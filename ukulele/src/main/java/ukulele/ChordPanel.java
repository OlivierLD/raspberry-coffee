package ukulele;

import chords.ChordList;
import chords.ChordUtil;
import ctx.AppContext;
import ctx.AppListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class ChordPanel
				extends JPanel {
	private static final String[] ENGLISH_NOTE_NAMES = {"A", "B", "C", "D", "E", "F", "G"};
	private static final String[] FRENCH_NOTE_NAMES = {"La", "Si", "Do", "Ré", "Mi", "Fa", "Sol"};

	public static final int A_B_C_OPTION = 0;
	public static final int DO_RE_MI_OPTION = 1;
	private static int userLangOption = A_B_C_OPTION;

	private static Font musicFont = null;

	private static final int BETWEEN_STRINGS = 10;

	private static final int NUMBER_OF_STRINGS = 4;

	private static final int FRET_WIDTH = 14;
	private static final int TOP_THICKNESS = 3;
	private static final int FINGER_RADIUS = 8;
	private int nbFrets = 5;

	private transient Chord chord;
	private int[] chordToIdentify = {0, 0, 0, 0};

	public static final int DISPLAY_MODE = 1;
	public static final int IDENTIFIER_MODE = 2;

	private int chordMode = DISPLAY_MODE;

	public ChordPanel() {
		this(null);
	}

	public ChordPanel(Chord chord) {
		this.chord = chord;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setChord(Chord chord) {
		this.chord = chord;
	}

	public Chord getChord() {
		return this.chord;
	}

	private void init() {
		musicFont = getMusicFont();

		addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (ChordPanel.this.chordMode == IDENTIFIER_MODE) {

					int h = ChordPanel.this.getHeight();
					int w = ChordPanel.this.getWidth();

					int totalWidth = (NUMBER_OF_STRINGS - 1) * BETWEEN_STRINGS;
					int totalHeight = ChordPanel.this.nbFrets * FRET_WIDTH + TOP_THICKNESS;

					int xOrig = w / 2 - totalWidth / 2;
					int yOrig = h / 2 - totalHeight / 2;

					int stringNum = 0;
					for (int i = 0; i < NUMBER_OF_STRINGS; i++) {
						int x = xOrig + i * BETWEEN_STRINGS;
						if (Math.abs(x - e.getX()) < 3) {
							stringNum = i + 1;
							break;
						}
					}
					int fretNum = 0;
					for (int i = 0; (stringNum > 0) && (i < ChordPanel.this.nbFrets + 1); i++) {
						int yUp = yOrig + i * FRET_WIDTH;
						int yDown = yOrig + (i + 1) * FRET_WIDTH;
						if ((e.getY() > yUp) && (e.getY() < yDown)) {
							fretNum = i + 1;
							System.out.println("String # " + stringNum + ", fret " + fretNum);
							break;
						}
					}
					if (stringNum > 0) {
						if (ChordPanel.this.chordToIdentify[(stringNum - 1)] == fretNum)
							fretNum = 0;
						ChordPanel.this.chordToIdentify[(stringNum - 1)] = fretNum;

						boolean found = false;
						for (Chord c : ChordList.getChords()) {
							int[] fingers = c.getFinger();
							if ((fingers[0] == ChordPanel.this.chordToIdentify[0]) && (fingers[1] == ChordPanel.this.chordToIdentify[1]) && (fingers[2] == ChordPanel.this.chordToIdentify[2]) && (fingers[3] == ChordPanel.this.chordToIdentify[3])) {


								found = true;
								ChordPanel.this.setChord(c);
								break;
							}
						}
						if (!found)
							ChordPanel.this.setChord(new Chord("-unknown-", ChordPanel.this.chordToIdentify));
						ChordPanel.this.repaint();
					}
				}

				if (ChordPanel.this.getChord() != null) {
					ChordUtil.playChord(ChordPanel.this.getChord());
				} else
					ChordUtil.playChord(new Chord("", new int[]{0, 0, 0, 0}));
			}
		});
		AppContext.getInstance().addAppListener(new AppListener() {

			public void setUserLanguage(String lng) {
				setUserLangOption("FR".equals(lng) ? DO_RE_MI_OPTION : A_B_C_OPTION);
				repaint();
			}
		});

		String lang = System.getProperty("lang", "EN");
		AppContext.getInstance().fireUserLanguage(lang);

	}

	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		musicFont = musicFont.deriveFont(Font.BOLD, 30.0F);

		int h = getHeight();
		int w = getWidth();

		int totalWidth = 30;
		int totalHeight = this.nbFrets * FRET_WIDTH + TOP_THICKNESS;

		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.black);

		int xOrig = w / 2 - totalWidth / 2;
		int yOrig = h / 2 - totalHeight / 2;


		Font f = g.getFont();
		Font f2 = new Font(f.getName(), 1, 18);
		Font f3 = new Font(f.getName(), 1, 12);
		g.setFont(f2);


		if (this.chord != null) {
			String title = this.chord.getTitle();

			if (userLangOption == DO_RE_MI_OPTION) {
				String noteName = title.substring(0, 1);

				for (int i = 0; i < ENGLISH_NOTE_NAMES.length; i++) {
					if (ENGLISH_NOTE_NAMES[i].equals(noteName)) {
						title = FRENCH_NOTE_NAMES[i] + title.substring(1);
						break;
					}
				}
			}

			boolean sharp = title.indexOf("#") > -1;
			if (sharp) {
				title = title.replace('#', 'B');
			}
			if ((title != null) && (title.trim().length() > 0)) {
				AttributedString astr = new AttributedString(title);
				astr.addAttribute(TextAttribute.FONT, f2);
				Map<TextAttribute, Object> map = new HashMap<>();
				map.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER);
				map.put(TextAttribute.FONT, musicFont);

				Pattern pattern = null;
				Matcher matcher = null;
				boolean found = false;

				pattern = Pattern.compile("\\d");
				matcher = pattern.matcher(title);
				found = matcher.find();


				while (found) {

					int start = matcher.start();
					int end = matcher.end();

					astr.addAttribute(TextAttribute.FONT, f3.deriveFont(0, f3.getSize()), start, end);
					astr.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, start, end);
					found = matcher.find();
				}

				pattern = Pattern.compile("[a-z\\(\\)\\+\\-]");
				matcher = pattern.matcher(title);
				found = matcher.find();


				while (found) {

					int start = matcher.start();
					int end = matcher.end();

					astr.addAttribute(TextAttribute.FONT, f2.deriveFont(0, f3.getSize()), start, end);
					found = matcher.find();
				}

				pattern = Pattern.compile("b");
				matcher = pattern.matcher(title);
				found = matcher.find();


				while (found) {

					int start = matcher.start();
					int end = matcher.end();

					astr.addAttribute(TextAttribute.FONT, musicFont, start, end);

					found = matcher.find();
				}

				if (sharp) {
					pattern = Pattern.compile("\\u0042");
					matcher = pattern.matcher(title);
					found = matcher.find();


					while (found) {

						int start = matcher.start();
						int end = matcher.end();

						astr.addAttribute(TextAttribute.FONT, musicFont, start, end);

						found = matcher.find();
					}
				}

				pattern = Pattern.compile("Do|Ré|Mi|Fa|Sol|La|Si");
				matcher = pattern.matcher(title);
				found = matcher.find();
				while (found) {
					int start = matcher.start();
					int end = matcher.end();
					astr.addAttribute(TextAttribute.FONT, f2, start, end);
					found = matcher.find();
				}


				FontRenderContext frc = g2d.getFontRenderContext();
				LineBreakMeasurer lbm = new LineBreakMeasurer(astr.getIterator(), frc);
				int len = (int) lbm.nextLayout(getWidth()).getAdvance();

				g2d.drawString(astr.getIterator(), w / 2 - len / 2, yOrig - (FRET_WIDTH / 2));
			}
		}

		for (int i = 0; i < NUMBER_OF_STRINGS; i++) {
			int x = xOrig + i * BETWEEN_STRINGS;
			g.drawLine(x, yOrig, x, yOrig + totalHeight);
		}

		int fretBase = 0;
		if (this.chord != null) {
			int lowestFinger = ChordUtil.findLowestFinger(this.chord)[0];
			int highestFinger = ChordUtil.findLowestFinger(this.chord)[1];
			if ((lowestFinger > 1) && ((lowestFinger > TOP_THICKNESS) || (highestFinger - lowestFinger >= TOP_THICKNESS))) {
				fretBase = lowestFinger;
			}
		}

		for (int i = 0; i < this.nbFrets + 1; i++) {
			int y = yOrig + i * FRET_WIDTH;
			if ((i == 0) && (fretBase == 0)) {
				g.fillRect(xOrig, y - TOP_THICKNESS, totalWidth + 1, TOP_THICKNESS);
			} else
				g.drawLine(xOrig, y, xOrig + totalWidth, y);
		}
		if (this.chord != null) {

			if (fretBase != 0) {
				g.setFont(f);
				g.drawString(Integer.toString(fretBase), xOrig + 40, yOrig + f.getSize() / 2 + (FRET_WIDTH / 2));
			}

			for (int i = 0; i < this.chord.getFinger().length; i++) {
				if (this.chord.getFinger()[i] == -1) {
					int x = xOrig + i * BETWEEN_STRINGS;
					int y = yOrig + (FRET_WIDTH / 2);

					String nope = "X";

					Font currFont = g.getFont();
					g.setFont(f);
					int l = g.getFontMetrics(g.getFont()).stringWidth(nope);
					g.drawString(nope, x - l / 2 + 1, y + g.getFont().getSize() / 2);
					g.setFont(currFont);
				}
				if (this.chord.getFinger()[i] > 0) {
					int x = xOrig + i * BETWEEN_STRINGS;
					int y = yOrig + (this.chord.getFinger()[i] - (fretBase == 0 ? 0 : fretBase - 1) - 1) * FRET_WIDTH + (FRET_WIDTH / 2);
					g.fillOval(x - (FINGER_RADIUS / 2), y - (FINGER_RADIUS / 2), FINGER_RADIUS, FINGER_RADIUS);
				}
			}
		}
	}

	public BufferedImage makeImage(int w, int h) {
		int width = w;
		int height = h;


		BufferedImage bufferedImage = new BufferedImage(width, height, 1);


		Graphics2D g2d = bufferedImage.createGraphics();

		paintComponent(g2d);

		g2d.dispose();


		return bufferedImage;
	}

	public void saveImage(BufferedImage bi, String imageType, String fileName) throws Exception {
		ImageIO.write(bi, imageType, new File(fileName));
	}

	public void setClipboard(Image image) {
		ImageSelection imgSel = new ImageSelection(image);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
	}

	public Font getMusicFont() {
		if (musicFont == null)
			musicFont = loadMusicFont();
		return musicFont;
	}

	private static Font loadMusicFont() {
		Font f = null;
		try {
			f = tryToLoadFont("MusiSync.ttf");
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		if (f == null) {
			f = new Font("Courier New", 1, 20);
		} else
			f = f.deriveFont(1, 20.0F);
		return f;
	}

	private static Font tryToLoadFont(String fontName) {
		String RESOURCE_PATH = "resources/";
		try {
			String fontRes = RESOURCE_PATH + fontName;
			InputStream fontDef = ChordPanel.class.getResourceAsStream(fontRes);
			if (fontDef == null) {
				throw new NullPointerException("Could not find font resource \"" + fontName + "\"\n\t\tin \"" + fontRes + "\"\n\t\tfor \"" + ChordPanel.class.getName() + "\"\n\t\ttry: " + ChordPanel.class.getResource(fontRes));
			}


			return Font.createFont(0, fontDef);
		} catch (FontFormatException e) {
			System.err.println("getting font " + fontName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("getting font " + fontName);
			e.printStackTrace();
		}
		return null;
	}

	private void jbInit()
					throws Exception {
		setSize(new Dimension(100, 165));
		setPreferredSize(new Dimension(100, 165));
		setMinimumSize(new Dimension(100, 165));
		init();
	}

	public static void setUserLangOption(int ulo) {
		userLangOption = ulo;
	}

	public void setChordMode(int chordMode) {
		this.chordMode = chordMode;
	}

	public int getChordMode() {
		return this.chordMode;
	}

	public static class ImageSelection
					implements Transferable {
		private Image image;

		public ImageSelection(Image image) {
			this.image = image;
		}


		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}


		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}


		public Object getTransferData(DataFlavor flavor)
						throws UnsupportedFlavorException, IOException {
			if (!DataFlavor.imageFlavor.equals(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return this.image;
		}
	}
}
