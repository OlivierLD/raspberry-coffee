package utils.swing.components;

import utils.swing.utils.SwingUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CircularDisplay extends JPanel {
    public static final NumberFormat SPEED_FMT = new DecimalFormat("00.00");
    public static final NumberFormat ANGLE_FMT = new DecimalFormat("000");

    private static CircularDisplay instance = null;
    private Font jumboFont = null;
    private Font bgJumboFont = null;
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel dataNameLabel = new JLabel();
    private JLabel dataValueLabel = new JLabel();
    private Color displayColor = Color.green;
    // Color for the background font.
    private final Color bgColor = new Color((Color.gray.getRed() / 255f), (Color.gray.getGreen() / 255f), (Color.gray.getBlue() / 255f), 0.5f);

    private String toolTipText = null;

    private String origName = "Value", origValue = "00.00";

    private double direction = 0d;
    private final static double EXTERNAL_RADIUS_COEFF = 1.050;
    private final static double INTERNAL_RADIUS_COEFF = 1.025;

    protected double radius = 0;
    protected Point center = null;

    public CircularDisplay(String name, String value) {
        instance = this;
        origName = name;
        origValue = value;
    }

    public CircularDisplay(String name, String value, String ttText) {
        this(name, value, ttText, 36);
    }

    public CircularDisplay(String name, String value, String ttText, int basicFontSize) {
        instance = this;
        origName = name;
        origValue = value;
        toolTipText = ttText;
        jumboFontSize = basicFontSize;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int jumboFontSize = 20;

    private void jbInit() {
        try {
            jumboFont = SwingUtils.tryToLoadFont("TRANA___.TTF", this);
            bgJumboFont = SwingUtils.tryToLoadFont("TRANGA__.TTF", this);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        this.setLayout(gridBagLayout1);
//  this.setBackground(Color.lightGray);

        resize(jumboFontSize);

//  this.setSize(new Dimension(120, 120));
//  this.setPreferredSize(new Dimension(120, 120));
        dataNameLabel.setText(origName);
        dataValueLabel.setText(origValue);
        if (toolTipText != null) {
            this.setToolTipText(toolTipText);
        }
        //  this.add(dataNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(dataValueLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

//  dataNameLabel.setFont(digiFont.deriveFont(Font.BOLD, 20));
        dataNameLabel.setForeground(displayColor);
        dataNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
//  dataValueLabel.setFont(digiFont.deriveFont(Font.BOLD, 24)); // Was 40
        dataValueLabel.setForeground(displayColor);
        dataValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dataValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
    }

    public void resize(int bigFontSize) {
        jumboFontSize = bigFontSize;
        int width = (int) (120d * (double) jumboFontSize / 36d);
        int height = (int) (120d * (double) jumboFontSize / 36d);
        this.setSize(new Dimension(width, height));
        this.setPreferredSize(new Dimension(width, height));
        int big = (int) (24d * jumboFontSize / 36d);
        int small = (int) (20d * jumboFontSize / 36d);
        dataNameLabel.setFont(jumboFont.deriveFont(Font.BOLD, small));
        dataValueLabel.setFont(jumboFont.deriveFont(Font.BOLD, big));
    }

    private static Font loadDigiFont() {
        Font f = null;
        try {
            f = SwingUtils.tryToLoadFont("ds-digi.ttf", instance);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        if (f == null) {
            f = new Font("Courier New", Font.BOLD, 20);
        } else {
            f = f.deriveFont(Font.BOLD, 20);
        }
        return f;
    }

    public void setName(String s) {
        dataNameLabel.setText(s);
    }

    public void setDirection(double d) {
        this.direction = d;
    }

    public void setSpeed(double speed) {
        dataValueLabel.setText(SPEED_FMT.format(speed));
    }

    public void setAngleValue(double angle) {
        dataValueLabel.setText(ANGLE_FMT.format(angle));
    }

    public void setDisplayColor(Color c) {
        displayColor = c;
        dataNameLabel.setForeground(displayColor);
        dataValueLabel.setForeground(displayColor);
    }

    public void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Background
        //  Dimension dim =  this.getSize();
        //  System.out.println("Dim:" + dim.getWidth() + "x" + dim.getHeight());
        Color startColor = new Color(0x94, 0x9c, 0x84); // new Color(0, 128, 128); // Color.black; // new Color(255, 255, 255);
        Color endColor = new Color(0, 64, 64); // Color.gray; // new Color(102, 102, 102);
        GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down

        if (false) {
            //  GradientPaint gradient = new GradientPaint(0, 0, startColor, this.getWidth(), this.getHeight(), endColor); // Diagonal, top-left to bottom-right
            //  GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, this.getWidth(), 0, endColor); // Horizontal
            //  GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, this.getHeight(), endColor); // vertical
            ((Graphics2D) g).setPaint(gradient);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        Dimension dim = this.getSize();
        radius = 0.9 * (Math.min(dim.width, dim.height) - 10d) / 2d;
        if (true) {
            center = new Point((dim.width / 2), (dim.height / 2));
            if (true) { // With shaded bevel
                RadialGradientPaint rgp = new RadialGradientPaint(center,
                        (int) (radius * 1.15),
                        new float[]{0f, 0.9f, 1f},
                        new Color[]{this.getBackground(), Color.gray, this.getBackground()});
                ((Graphics2D) g).setPaint(rgp);
                ((Graphics2D) g).fillRect(0, 0, dim.width, dim.height);
            }
            drawGlossyCircularDisplay((Graphics2D) g, center, (int) radius, Color.lightGray, Color.black, 1f);
        }
        // Boat

        // Rose
        g.setColor(Color.lightGray);
        for (int i = 0; i < 360; i += 10) {
            int x1 = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians(i)));
            int y1 = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians(i)));
            int x2 = (dim.width / 2) + (int) ((radius) * Math.cos(Math.toRadians(i)));
            int y2 = (dim.height / 2) + (int) ((radius) * Math.sin(Math.toRadians(i)));
            g.drawLine(x1, y1, x2, y2);
        }
//  g.setColor(Color.lightGray);
        g.setColor(Color.white);
        String n = "N";
        int fontSize = 14;
        g.setFont(g.getFont().deriveFont(Font.BOLD, fontSize));
        int strWidth = g.getFontMetrics(g.getFont()).stringWidth(n);
        g.drawString(n, (dim.width / 2) - strWidth / 2, (int) (fontSize * 1.5));

        // Hand
        drawHand((Graphics2D) g);
    }

    private void drawHand(Graphics2D g2d) {
//  String value = "";
//  try { value = DF.format(this.speed); } catch (NullPointerException npe) { npe.printStackTrace(); }
//  g2d.setFont(bgJumboFont.deriveFont((radius / 3f)));
//  int strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
//  g2d.setColor(bgColor);
//  g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 2));

//  g2d.setFont(jumboFont.deriveFont(Font.BOLD, (radius / 3f)));
//  strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
//  g2d.setColor(Color.green);
//  g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 2));

        // Unit
//  value = speedUnit.label();
//  g2d.setFont(bgJumboFont.deriveFont((radius / 6f)));
//  strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
//  g2d.setColor(bgColor);
//  g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 4));

//  g2d.setFont(jumboFont.deriveFont((radius / 6f)));
//  strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
//  g2d.setColor(Color.green);
//  g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 4));

        // Label
//    if (this.label.trim().length() > 0)
//    {
//      g2d.setFont(bgJumboFont.deriveFont((radius / 6f)));
//      strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(this.label);
//      g2d.setColor(bgColor);
//      g2d.drawString(this.label, center.x - (strWidth / 2), this.getHeight() - 2);
//          
//      g2d.setFont(jumboFont.deriveFont(Font.BOLD, (radius / 6f)));
//      strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(this.label);
//      g2d.setColor(Color.red);
//      g2d.drawString(this.label, center.x - (strWidth / 2), this.getHeight() - 2);      
//    }

        int longRadius = (int) (radius * INTERNAL_RADIUS_COEFF * 0.8);
        int shortRadius = (int) (radius * INTERNAL_RADIUS_COEFF * 0.15);

        double angle = direction - 90; // From the top, clockwise
        int toX = center.x + (int) ((radius) * Math.cos(Math.toRadians(angle)));
        int toY = center.y + (int) ((radius) * Math.sin(Math.toRadians(angle)));

        int fromXRight = center.x + (int) ((double) (shortRadius / 2d) * Math.cos(Math.toRadians(angle + 90)));
        int fromYRight = center.y + (int) ((double) (shortRadius / 2d) * Math.sin(Math.toRadians(angle + 90)));

        int fromXLeft = center.x + (int) ((double) (shortRadius / 2d) * Math.cos(Math.toRadians(angle - 90)));
        int fromYLeft = center.y + (int) ((double) (shortRadius / 2d) * Math.sin(Math.toRadians(angle - 90)));

        g2d.setColor(Color.black); // Outline
        Polygon hand = new Polygon(new int[]{toX, fromXRight, fromXLeft},
                new int[]{toY, fromYRight, fromYLeft}, 3);
        g2d.drawPolygon(hand);
        //  Point gradientOrigin = new Point(toX, toY);
//    GradientPaint gradient = new GradientPaint(toX, 
//                                               toY, 
//                                               Color.cyan, 
//                                               center.x, 
//                                               center.y, 
//                                               Color.blue); // vertical, light on top
        GradientPaint gradient = new GradientPaint(center.x,
                0,
                Color.cyan,
                center.x,
                (float) (2 * radius),
                Color.blue); // vertical, light on top
        g2d.setPaint(gradient);
        //  g2d.setColor(Color.red);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.fillPolygon(hand);

        drawGlossyCircularBall(g2d, center, (int) (shortRadius * 0.55), Color.lightGray, Color.darkGray, 1f);
    }

    private static void drawGlossyCircularDisplay(Graphics2D g2d, Point center, int radius, Color lightColor, Color darkColor, float transparency) {
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
}
