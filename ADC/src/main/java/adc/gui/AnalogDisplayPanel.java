package adc.gui;


import adc.ADCContext;

import adc.ADCListener;

import adc.ADCObserver;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.io.IOException;
import java.io.InputStream;

import java.text.DecimalFormat;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AnalogDisplayPanel
		extends JPanel {
	@SuppressWarnings("compatibility:-6774783565143675115")
	public final static long serialVersionUID = 1L;

	private final static boolean GLOSSY_DISPLAY = true;

	private final static double EXTERNAL_RADIUS_COEFF = 1.050;
	private final static double INTERNAL_RADIUS_COEFF = 1.025;

	private double value = 0D;
	private double prevValue = 0;

	private final static int DISPLAY_OFFSET = 5;

	private final static double INCREMENT_DEFAULT_VALUE = 0.25;
	private final static int TICK_DEFAULT_VALUE = 5;

	private double maxValue = 50D;
	private double increment = 0.25;
	private int bigTick = 5;

	private double valueUnitRatio = (180D - (2 * DISPLAY_OFFSET)) / maxValue;

	public enum AnalogUnit {
		PC("%"),
		KNOT("knots"),
		KMH("km/h"),
		MPH("mph"),
		MS("m/s");

		@SuppressWarnings("compatibility:-2776162247172094666")
		public final static long serialVersionUID = 1L;

		private final String label;

		AnalogUnit(String label) {
			this.label = label;
		}

		public String label() {
			return this.label;
		}
	}

	private AnalogUnit displayUnit = AnalogUnit.PC;

	private AnalogDisplayPanel instance = this;

	private Font jumboFont = null;
	private Font bgJumboFont = null;

	protected transient Stroke thick = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	protected transient Stroke dotted = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{2f}, 0f);
	protected transient Stroke origStroke = null;

	protected int radius = 0;
	protected Point center = null;
	final ADCObserver.MCP3008_input_channels channel;

	public AnalogDisplayPanel(ADCObserver.MCP3008_input_channels channel, double s) {
		this(channel, s, INCREMENT_DEFAULT_VALUE, TICK_DEFAULT_VALUE);
	}

	public AnalogDisplayPanel(ADCObserver.MCP3008_input_channels channel, double s, double inc, int tick) {
		this.channel = channel;
		this.maxValue = s;
		this.increment = inc;
		this.bigTick = tick;

		valueUnitRatio = (180D - (2 * DISPLAY_OFFSET)) / maxValue;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
			throws Exception {
		this.setLayout(null);

		this.setOpaque(false);
		this.setBackground(new Color(0, 0, 0, 0));

		ADCContext.getInstance().addListener(new ADCListener() {
			@Override
			public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) {
				if (inputChannel.equals(channel)) {
					int volume = (int) (newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
					instance.setValue(volume);
				}
			}
		});
//  jumboFont = JumboDisplay.tryToLoadFont("ds-digi.ttf", null);
//  jumboFont = JumboDisplay.tryToLoadFont("CodenameCoderFree4F-Bold.ttf", this);
//  jumboFont = JumboDisplay.tryToLoadFont("AUDIMSCB.TTF", this);
		jumboFont = /*new Font("Source Code Pro", 18, Font.PLAIN);*/  tryToLoadFont("TRANA___.TTF", this);
		bgJumboFont = /* new Font("Source Code Pro", 18, Font.PLAIN);*/ tryToLoadFont("TRANGA__.TTF", this);
	}

	public static Font tryToLoadFont(String fontName, Object parent) {
		final String RESOURCE_PATH = "resources" + "/"; // A slash! Not File.Separator, it is a URL.
		try {
			String fontRes = RESOURCE_PATH + fontName;
			InputStream fontDef = null;
			if (parent != null)
				fontDef = parent.getClass().getResourceAsStream(fontRes);
			else
				fontDef = AnalogDisplayPanel.class.getResourceAsStream(fontRes);
			if (fontDef == null) {
				throw new NullPointerException("Could not find font resource \"" + fontName +
						"\"\n\t\tin \"" + fontRes +
						"\"\n\t\tfor \"" + parent.getClass().getName() +
						"\"\n\t\ttry: " + parent.getClass().getResource(fontRes));
			} else
				return Font.createFont(Font.TRUETYPE_FONT, fontDef);
		} catch (FontFormatException | IOException e) {
			System.err.println("getting font " + fontName);
			Logger.getGlobal().finest("Error getting font " + fontName + ", " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public void setSpeedUnit(AnalogUnit displayUnit) {
		this.displayUnit = displayUnit;
	}

	private double damping = 0.5;

	public void setValue(final double d) {
		System.out.println("Setting value to " + d);
		this.value = d;
		double from = prevValue;
		double to = d;

		damping = Math.abs(prevValue - value) / 100;
		if (damping == 0) {
			damping = 0.5;
		}
//  System.out.println("Damping:" + DAMPING);

		// Manage the case 350-10
		if (Math.abs(prevValue - value) > 180) {
			if (Math.signum(Math.cos(Math.toRadians(prevValue))) == Math.signum(Math.cos(Math.toRadians(value)))) {
				if (from > to) {
					to += 360;
				} else {
					to -= 360;
				}
			}
		}
//  final double _to = to;
		int sign = (from > to) ? -1 : 1;
		prevValue = d;
		// Smooth rotation
		for (double h = from; (sign == 1 && h <= to) || (sign == -1 && h >= to); h += (damping * sign)) {
			final double _h = h;
			try {
				// For a smooth move of the hand
				SwingUtilities.invokeAndWait(() -> {
					double _heading = _h % 360;
					while (_heading < 0) _heading += 360;
					value = _heading;
					repaint();
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static void drawGlossyHalfCircularDisplay(Graphics2D g2d,
	                                                  Point center,
	                                                  int radius,
	                                                  Color lightColor,
	                                                  Color darkColor,
	                                                  float transparency) {
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
		g2d.setPaint(null);

		g2d.setColor(darkColor);
//  g2d.fillOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);
		g2d.fillArc(center.x - radius, center.y - radius, 2 * radius, 2 * radius, 0, 180);

		Point gradientOrigin = new Point(center.x - radius,center.y - radius);
		GradientPaint gradient = new GradientPaint(gradientOrigin.x,
				gradientOrigin.y,
				lightColor,
				gradientOrigin.x,
				gradientOrigin.y + (2 * radius / 3),
				darkColor); // vertical, light on top
		g2d.setPaint(gradient);
//  g2d.fillOval((int)(center.x - (radius * 0.90)), 
//               (int)(center.y - (radius * 0.95)), 
//               (int)(2 * radius * 0.9), 
//               (int)(2 * radius * 0.95));
		g2d.fillArc((int) (center.x - (radius * 0.90)),
				(int) (center.y - (radius * 0.95)),
				(int) (2 * radius * 0.9),
				(int) (2 * radius * 0.95),
				0,
				180);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		radius = Math.min(this.getWidth() / 2, this.getHeight());
		center = new Point(this.getWidth() / 2, this.getHeight() - 20);

		// For the scale and shadow:
		radius *= 0.8; // 0.9;

//  g.setColor(Color.lightGray);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		origStroke = g2d.getStroke();

		if (GLOSSY_DISPLAY) {
			Color bgColor = this.getBackground();
			// Shaded bevel
			RadialGradientPaint rgp = new RadialGradientPaint(center,
					(int) (radius * 1.15) + 15,
					new float[]{0f, 0.85f, 1f},
					new Color[]{bgColor, Color.black, bgColor});
			g2d.setPaint(rgp);
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			// White disc scale, outside
//    g2d.setColor(Color.white);
//    int extRadius = (int)(radius * EXTERNAL_RADIUS_COEFF) + 15; // 10 is the font size, for the months (in case of map)
//    g2d.fillOval(center.x - extRadius, center.y - extRadius, 2 * extRadius, 2 * extRadius);      
			// Glossy Display
			drawGlossyHalfCircularDisplay(g2d, center, radius, Color.lightGray, Color.darkGray, 1f);
		}

		g2d.setStroke(origStroke);
		drawGrid(g2d);
		drawHand(g2d);
	}

	private static void drawGlossyCircularBall(Graphics2D g2d, Point center, int radius, Color lightColor, Color darkColor, float transparency) {
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
		g2d.setPaint(null);

		g2d.setColor(darkColor);
		g2d.fillOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);

		Point gradientOrigin = new Point(center.x - radius,
				center.y - radius);
		GradientPaint gradient = new GradientPaint(gradientOrigin.x,
				gradientOrigin.y,
				lightColor,
				gradientOrigin.x,
				gradientOrigin.y + (2 * radius / 3),
				darkColor); // vertical, light on top
		g2d.setPaint(gradient);
		g2d.fillOval((int) (center.x - (radius * 0.90)),
				(int) (center.y - (radius * 0.95)),
				(int) (2 * radius * 0.9),
				(int) (2 * radius * 0.95));
	}

	private void drawGrid(Graphics2D g2d) {
		g2d.setColor(Color.gray);
		g2d.setStroke(thick);
		// Center
		g2d.fillOval(center.x - 2, center.y - 2, 4, 4);
		// Equateur celeste
//  g2d.drawOval(center.x - (radius / 2), center.y - (radius / 2), radius, radius);
		// Pole abaiss�
//  g2d.drawOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);
		// Horizontal axis
//  g2d.drawLine(0, center.y, this.getWidth(), center.y);

		// Scale, on the edge
		g2d.setStroke(origStroke);
		int externalScaleRadius = (int) (radius * EXTERNAL_RADIUS_COEFF);
		int internalScaleRadius = (int) (radius * INTERNAL_RADIUS_COEFF);
//  g2d.drawOval(center.x - externalScaleRadius, 
//               center.y - externalScaleRadius, 
//               2 * externalScaleRadius, 
//               2 * externalScaleRadius);
		g2d.drawArc(center.x - externalScaleRadius,
				center.y - externalScaleRadius,
				2 * externalScaleRadius,
				2 * externalScaleRadius,
				0,
				180);
//  for (int d=-90; d<=90; d+=1)
		for (double s = 0; s <= maxValue; s += increment) {
			double d = valueToAngle(s) - 90;
			int scaleRadius = internalScaleRadius;
			if (s % 1 == 0) {
				scaleRadius = externalScaleRadius;
			}
			int fromX = center.x + (int) (radius * Math.sin(Math.toRadians(d - 180)));
			int fromY = center.y + (int) (radius * Math.cos(Math.toRadians(d - 180)));
			int toX = center.x + (int) (scaleRadius * Math.sin(Math.toRadians(d - 180)));
			int toY = center.y + (int) (scaleRadius * Math.cos(Math.toRadians(d - 180)));
			g2d.drawLine(fromX, fromY, toX, toY);
			if (s % bigTick == 0) {
				Font f = g2d.getFont();
				g2d.setFont(f.deriveFont(Font.BOLD, f.getSize())); // * 2));
				String str = Integer.toString((int) s);
				int strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(str);
				Color c = g2d.getColor();
				g2d.setColor(Color.white);
				g2d.rotate(Math.toRadians(d), center.x, center.y);
				g2d.drawString(str, center.x - (strWidth / 2), center.y - (int) (0.95 * internalScaleRadius) + (g2d.getFont().getSize() / 2));
				g2d.rotate(Math.toRadians(d) * -1, center.x, center.y);
				g2d.setColor(c);
				g2d.setFont(f);
			}
		}
	}

	private double valueToAngle(double s) {
		double angle = s * valueUnitRatio;
		return angle + DISPLAY_OFFSET;
	}

	private final static DecimalFormat DF = new DecimalFormat("000");

	private void drawHand(Graphics2D g2d) {
		// Display value and unit in a Jumbo
		String value = DF.format(this.value);
		g2d.setFont(bgJumboFont.deriveFont((radius / 3f)));
		int strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
		g2d.setColor(Color.gray);
		g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 2));

		g2d.setFont(jumboFont.deriveFont((radius / 3f)));
		strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
		g2d.setColor(Color.green);
		g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 2));

		// Unit
		value = displayUnit.label();
		g2d.setFont(bgJumboFont.deriveFont((radius / 6f)));
		strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
		g2d.setColor(Color.gray);
		g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 4));

		g2d.setFont(jumboFont.deriveFont((radius / 6f)));
		strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
		g2d.setColor(Color.green);
		g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 4));

		int longRadius = (int) (radius * INTERNAL_RADIUS_COEFF * 0.8);
		int shortRadius = (int) (radius * INTERNAL_RADIUS_COEFF * 0.1);

		if (this.value > this.maxValue) {
			this.value = this.maxValue;
		}
		double angle = 270 - valueToAngle(this.value);

		int toX = center.x + (int) (longRadius * Math.sin(Math.toRadians(angle)));
		int toY = center.y + (int) (longRadius * Math.cos(Math.toRadians(angle)));

		int fromXRight = center.x + (int) ((shortRadius / 2d) * Math.sin(Math.toRadians(angle + 90)));
		int fromYRight = center.y + (int) ((shortRadius / 2d) * Math.cos(Math.toRadians(angle + 90)));

		int fromXLeft = center.x + (int) ((shortRadius / 2d) * Math.sin(Math.toRadians(angle - 90)));
		int fromYLeft = center.y + (int) ((shortRadius / 2d) * Math.cos(Math.toRadians(angle - 90)));

		g2d.setColor(Color.black);
		Polygon north = new Polygon(new int[]{toX, fromXRight, fromXLeft},
				new int[]{toY, fromYRight, fromYLeft}, 3);
		g2d.drawPolygon(north);
//  Point gradientOrigin = new Point(toX, toY);
		GradientPaint gradient = new GradientPaint(toX,
				toY,
				Color.cyan,
				center.x,
				center.y,
				Color.blue); // vertical, light on top
		g2d.setPaint(gradient);
//  g2d.setColor(Color.red);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g2d.fillPolygon(north);

		drawGlossyCircularBall(g2d, center, (int) (shortRadius * 0.55), Color.lightGray, Color.darkGray, 1f);
	}
}
