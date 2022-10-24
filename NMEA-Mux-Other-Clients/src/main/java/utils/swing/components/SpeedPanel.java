package utils.swing.components;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class SpeedPanel
        extends JPanel
        implements MouseMotionListener {

    private final static NumberFormat DF_2 = new DecimalFormat("##0.00");

    private final static boolean GLOSSY_DISPLAY = true;
    private final static boolean WITH_TEXTURE = false;
    private final static boolean WITH_BEVEL_SHADE = true;

    private final static double EXTERNAL_RADIUS_COEFF = 1.050;
    private final static double INTERNAL_RADIUS_COEFF = 1.025;
    //                                                   F: 0  1  2  3   4   5   6   7   8   9  10  11  12
    private final static int[] BEAUFORT_SCALE = new int[]{0, 1, 4, 7, 11, 16, 22, 28, 34, 41, 48, 56, 64};

    private double speed = 0D;
    private double prevSpeed = 0;

    private boolean withMinMax = true;

    private double minimumSpeed = Double.MAX_VALUE;
    private double maximumSpeed = -minimumSpeed;

    private final static int DISPLAY_OFFSET = 5;

    private final static double INCREMENT_DEFAULT_VALUE = 0.25;
    private final static int TICK_DEFAULT_VALUE = 5;

    private double maxSpeed = 50D;
    private double increment = 0.25;
    private int bigTick = 5;

    private double speedUnitRatio = (180D - (2 * DISPLAY_OFFSET)) / maxSpeed;

    // The background font is transparent
    private final Color bgColor = new Color((Color.gray.getRed() / 255f), (Color.gray.getGreen() / 255f), (Color.gray.getBlue() / 255f), 0.5f);

    public static int getBeaufort(double d) {
        int b = 12;
        for (int i = 0; i < BEAUFORT_SCALE.length; i++) {
            if (d < BEAUFORT_SCALE[i]) {
                b = i - 1;
                break;
            }
        }
        return b;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setWithMinMax(boolean withMinMax) {
        this.withMinMax = withMinMax;
    }

    public boolean isWithMinMax() {
        return withMinMax;
    }

    public void resetMinMax() {
        minimumSpeed = Double.MAX_VALUE;
        maximumSpeed = -minimumSpeed;
    }

    public void setMinMax(double min, double max) {
        minimumSpeed = min;
        maximumSpeed = max;
    }

    public void mouseDragged(MouseEvent e) {
    }

    /* Display the current value at the mouse */
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

//  System.out.println("X:" + x + ", Y:" + y);

        int centerX = this.getWidth() / 2;
        int centerY = this.getHeight() - 20;

        double deltaX = x - centerX;
        double deltaY = centerY - y;
//  System.out.println("X:" + x + ", Y:" + y + ", deltaX:" + deltaX + ", deltaY:" + deltaY);
        double angle = 90d;
        String tt = "Yo!";
        if (deltaX != 0) {
            double tan = deltaY / (double) Math.abs(deltaX);
//    System.out.println("Tan:" + tan);
            angle = Math.toDegrees(Math.atan(tan));
            if (deltaX > 0)
                angle = 180d - angle;
//    tt = "Mouse moved-> " + Integer.toString((int)angle) + "\272";
        }
        if (angle > DISPLAY_OFFSET && angle < (180 - DISPLAY_OFFSET)) {
            // DISPLAY_OFFSET = angle
            double actualAngle = angle - DISPLAY_OFFSET;
            double speed = (actualAngle / (180 - (2 * DISPLAY_OFFSET))) * maxSpeed;
            tt = DF_2.format(speed) + " " + speedUnit.label();
            if (withBeaufortScale) {
                tt = "<html>" + tt + "<br>F " + Integer.toString(getBeaufort(speed)) + "</html>";
            }
            this.setToolTipText(tt);
        }
    }

    public enum SpeedUnit {
        KNOT("knots"),
        KMH("km/h"),
        MPH("mph"),
        MS("m/s");

        private final String label;

        SpeedUnit(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }

    private SpeedUnit speedUnit = SpeedUnit.KNOT;

    private String label = "";

    private SpeedPanel instance = this;
    private boolean smooth = true;
    private boolean withBeaufortScale = false;

    private Font jumboFont = null;
    private Font bgJumboFont = null;

    protected transient Stroke thick = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    protected transient Stroke dotted = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{2f}, 0f);
    protected transient Stroke origStroke = null;

    protected int radius = 0;
    protected Point center = null;

    public SpeedPanel(double s) {
        this(s, INCREMENT_DEFAULT_VALUE, TICK_DEFAULT_VALUE);
    }

    public SpeedPanel(double s, boolean smooth) {
        this(s, INCREMENT_DEFAULT_VALUE, TICK_DEFAULT_VALUE, smooth);
    }

    public SpeedPanel(double s, double inc, boolean smooth) {
        this(s, inc, TICK_DEFAULT_VALUE, smooth);
    }

    public SpeedPanel(double s, double inc, int tick) {
        this(s, inc, tick, true);
    }

    public SpeedPanel(double s, double inc, int tick, boolean smooth) {
        this.maxSpeed = s;
        this.increment = inc;
        this.bigTick = tick;
        this.smooth = smooth;

        speedUnitRatio = (180D - (2 * DISPLAY_OFFSET)) / maxSpeed;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Font tryToLoadFont(String fontName, Object parent) {
        // final String RESOURCE_PATH = "resources" + "/"; // A slash! Not File.Separator, it is a URL.
        try {
            String fontRes = /*RESOURCE_PATH +*/ fontName;
            InputStream fontDef = null;
            if (parent != null) {
                fontDef = parent.getClass().getClassLoader().getResourceAsStream(fontRes);
            } else {
                fontDef = ClockDisplay.class.getClassLoader().getResourceAsStream(fontRes);
            }
            if (fontDef == null) {
                throw new NullPointerException("Could not find font resource \"" + fontName +
                        "\"\n\t\tin \"" + fontRes +
                        "\"\n\t\tfor \"" + parent.getClass().getName() +
                        "\"\n\t\ttry: " + parent.getClass().getResource(fontRes));
            } else {
                return Font.createFont(Font.TRUETYPE_FONT, fontDef);
            }
        } catch (FontFormatException e) {
            System.err.println("getting font " + fontName);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("getting font " + fontName);
            e.printStackTrace();
        }
        return null;
    }

    private void jbInit() {
        this.addMouseMotionListener(this);
        this.setLayout(null);
        if (!WITH_TEXTURE) {
            this.setOpaque(false);
            this.setBackground(new Color(0, 0, 0, 0));
        }
//      jumboFont = tryToLoadFont("ds-digi.ttf", null);
      jumboFont = tryToLoadFont("TRANA___.TTF", this);
      bgJumboFont = tryToLoadFont("TRANGA__.TTF", this);
    }

    public void setSpeedUnit(SpeedUnit speedUnit) {
        this.speedUnit = speedUnit;
    }

    private double damping = 0.5;

    public void setSpeed(final double d) {
        this.speed = d;
        minimumSpeed = Math.min(minimumSpeed, d);
        maximumSpeed = Math.max(maximumSpeed, d);
        double from = prevSpeed;
        double to = d;

        damping = Math.abs(prevSpeed - speed) / 100;
        if (damping == 0)
            damping = 0.5;
//  System.out.println("Damping:" + DAMPING);

        // Manage the case 350-10
        if (Math.abs(prevSpeed - speed) > 180) {
            if (Math.signum(Math.cos(Math.toRadians(prevSpeed))) == Math.signum(Math.cos(Math.toRadians(speed)))) {
                if (from > to) {
                    to += 360;
                } else {
                    to -= 360;
                }
            }
        }
//  final double _to = to;
        if (from != to) {
            int sign = (from > to) ? -1 : 1;
            prevSpeed = d;
            if (smooth) {
                // Smooth rotation
                for (double h = from; (sign == 1 && h <= to) || (sign == -1 && h >= to); h += (damping * sign)) {
//        System.out.println("Setting speed from " + from + " to " + to + ", at " + h);
                    final double _h = h;
                    try {
                        // For a smooth move of the hand
                        SwingUtilities.invokeAndWait(new Runnable()
                                //        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run() {
                                double _s = _h % 360;
                                while (_s < 0) _s += 360;
                                instance.speed = _s;
                                //            System.out.println("-> Speed:" + speed + " (" + _to + ")");
                                instance.repaint();
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
//      instance.speed = to;
                instance.repaint();
            }
        }
    }

    public void withBeaufortScale(boolean b) {
        this.withBeaufortScale = b;
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

        Point gradientOrigin = new Point(center.x - radius,
                center.y - radius);
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
            if (WITH_TEXTURE) {
                bgColor = new Color(bgColor.getRed(),
                        bgColor.getGreen(),
                        bgColor.getBlue(),
                        10); // transparent
            }
            // Shaded bevel
            if (WITH_BEVEL_SHADE) {
                RadialGradientPaint rgp = new RadialGradientPaint(center,
                        (int) (radius * 1.15) + 15,
                        new float[]{0f, 0.85f, 1f},
                        new Color[]{bgColor, Color.black, bgColor});
                g2d.setPaint(rgp);
                g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
            // White disc scale, outside
//    g2d.setColor(Color.white);
//    int extRadius = (int)(radius * EXTERNAL_RADIUS_COEFF) + 15; // 10 is the font size, for the months (in case of map)
//    g2d.fillOval(center.x - extRadius, center.y - extRadius, 2 * extRadius, 2 * extRadius);      
            // Glossy Display
            drawGlossyHalfCircularDisplay(g2d, center, radius, Color.lightGray, Color.black, 1f);
        }

        g2d.setStroke(origStroke);
        drawGrid(g2d);

        if (this.withBeaufortScale) {
//    System.out.println("With Beaufort scale");
            float alpha = 0.3f;
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            Stroke origStroke = ((Graphics2D) g).getStroke();
            Stroke stroke = new BasicStroke(10,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL);
            ((Graphics2D) g).setStroke(stroke);
            int externalScaleRadius = (int) (radius * EXTERNAL_RADIUS_COEFF);
            int internalScaleRadius = (int) (radius * INTERNAL_RADIUS_COEFF);
//    Color[] bColor = new Color[] { Color.white, Color.black };
            Color[] bColor = new Color[]{Color.orange, Color.cyan};
            double fromSpeed = speedToAngle(BEAUFORT_SCALE[0]);
            for (int b = 1; b < BEAUFORT_SCALE.length; b++) {
                double toSpeed = speedToAngle(BEAUFORT_SCALE[b] > maxSpeed ? maxSpeed : BEAUFORT_SCALE[b]);
                g2d.setColor(bColor[b % 2]);
                // The origin of the angles is on the right (East). They turn counter-clockwise.
                float extent = (float) (fromSpeed - toSpeed);
                Shape oneBeaufortDegree = new Arc2D.Float((center.x - externalScaleRadius), // The X coordinate of the upper-left corner of the arc's framing rectangle.
                        (center.y - externalScaleRadius), // The Y coordinate of the upper-left corner of the arc's framing rectangle.
                        (float) (2 * externalScaleRadius), // The overall width of the full ellipse of which this arc is a partial section.
                        (float) (2 * externalScaleRadius), // The overall height of the full ellipse of which this arc is a partial section.
                        (float) (180f - fromSpeed),        // The starting angle of the arc in degrees.
                        extent,                           // The angular extent of the arc in degrees.
                        Arc2D.OPEN);                      // The closure type for the arc: Arc2D.OPEN, Arc2D.CHORD, or Arc2D.PIE.
                ((Graphics2D) g).draw(oneBeaufortDegree);
                if (BEAUFORT_SCALE[b] > maxSpeed)
                    break;
                fromSpeed = toSpeed;
            }
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            ((Graphics2D) g).setStroke(origStroke);
        }
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
        for (double s = 0; s <= maxSpeed; s += increment) {
            double d = speedToAngle(s) - 90;
            int scaleRadius = internalScaleRadius;
            if (s % 1 == 0) {
                scaleRadius = externalScaleRadius;
            }
            int fromX = center.x + (int) (radius * Math.sin(Math.toRadians(d - 180)));
            int fromY = center.y + (int) (radius * Math.cos(Math.toRadians(d - 180)));
            int toX = center.x + (int) (scaleRadius * Math.sin(Math.toRadians(d - 180)));
            int toY = center.y + (int) (scaleRadius * Math.cos(Math.toRadians(d - 180)));
            g2d.drawLine(fromX, fromY, toX, toY); // Ticks
            if (s % bigTick == 0) {
                Font f = g2d.getFont();
                float fontFactor = Math.min(this.getWidth(), this.getHeight()) / 20f;
                g2d.setFont(f.deriveFont(Font.BOLD, 2 * fontFactor));
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
        if (withMinMax) {
            g2d.setColor(Color.red);
            // Min
            double angle = 270 - speedToAngle(this.minimumSpeed);
            // Tip of the triangle
            int toX = center.x + (int) ((radius * INTERNAL_RADIUS_COEFF * 0.8) * Math.sin(Math.toRadians(angle)));
            int toY = center.y + (int) ((radius * INTERNAL_RADIUS_COEFF * 0.8) * Math.cos(Math.toRadians(angle)));

            int toX_1 = center.x + (int) (externalScaleRadius * Math.sin(Math.toRadians(angle + 2)));
            int toY_1 = center.y + (int) (internalScaleRadius * Math.cos(Math.toRadians(angle + 2)));

            int toX_2 = center.x + (int) (externalScaleRadius * Math.sin(Math.toRadians(angle - 2)));
            int toY_2 = center.y + (int) (internalScaleRadius * Math.cos(Math.toRadians(angle - 2)));

//    g2d.drawLine(center.x, center.y, toX, toY);
            g2d.fillPolygon(new int[]{toX, toX_1, toX_2}, new int[]{toY, toY_1, toY_2}, 3);

            // Max
            angle = 270 - speedToAngle(this.maximumSpeed);
            toX = center.x + (int) ((radius * INTERNAL_RADIUS_COEFF * 0.8) * Math.sin(Math.toRadians(angle)));
            toY = center.y + (int) ((radius * INTERNAL_RADIUS_COEFF * 0.8) * Math.cos(Math.toRadians(angle)));

            toX_1 = center.x + (int) (externalScaleRadius * Math.sin(Math.toRadians(angle + 2)));
            toY_1 = center.y + (int) (internalScaleRadius * Math.cos(Math.toRadians(angle + 2)));

            toX_2 = center.x + (int) (externalScaleRadius * Math.sin(Math.toRadians(angle - 2)));
            toY_2 = center.y + (int) (internalScaleRadius * Math.cos(Math.toRadians(angle - 2)));

            //    g2d.drawLine(center.x, center.y, toX, toY);
            g2d.fillPolygon(new int[]{toX, toX_1, toX_2}, new int[]{toY, toY_1, toY_2}, 3);
        }
    }

    private double speedToAngle(double s) {
        double angle = s * speedUnitRatio;
        angle += DISPLAY_OFFSET;
        return angle;
    }

    private final static DecimalFormat DF = new DecimalFormat("00.0");

    private void drawHand(Graphics2D g2d) {
        String value = "";
        try {
            value = DF.format(this.speed);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        g2d.setFont(bgJumboFont.deriveFont((radius / 3f)));
        int strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
        g2d.setColor(bgColor);
        g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 2));

        g2d.setFont(jumboFont.deriveFont(Font.BOLD, (radius / 3f)));
        strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
        g2d.setColor(Color.green);
        g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 2));

        // Unit
        value = speedUnit.label();
        g2d.setFont(bgJumboFont.deriveFont((radius / 6f)));
        strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
        g2d.setColor(bgColor);
        g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 4));

        g2d.setFont(jumboFont.deriveFont((radius / 6f)));
        strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
        g2d.setColor(Color.green);
        g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 4));

        // Label
        if (this.label.trim().length() > 0) {
            g2d.setFont(bgJumboFont.deriveFont((radius / 6f)));
            strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(this.label);
            g2d.setColor(bgColor);
            g2d.drawString(this.label, center.x - (strWidth / 2), this.getHeight() - 2);

            g2d.setFont(jumboFont.deriveFont(Font.BOLD, (radius / 6f)));
            strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(this.label);
            g2d.setColor(Color.red);
            g2d.drawString(this.label, center.x - (strWidth / 2), this.getHeight() - 2);
        }

        int longRadius = (int) (radius * INTERNAL_RADIUS_COEFF * 0.8);
        int shortRadius = (int) (radius * INTERNAL_RADIUS_COEFF * 0.1);

        if (this.speed > this.maxSpeed)
            this.speed = this.maxSpeed;

        double angle = 270 - speedToAngle(this.speed);

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
