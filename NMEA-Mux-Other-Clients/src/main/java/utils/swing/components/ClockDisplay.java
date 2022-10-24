package utils.swing.components;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ClockDisplay extends JPanel {
    private static ClockDisplay instance = null;
    private Font digiFont = null;
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel dataNameLabel = new JLabel();
    private JLabel dataValueLabel = new JLabel();
    private Color displayColor = Color.green;
    private Color gridColor = Color.lightGray;
    private boolean withGlossyBG = true;

    private long value = 0L;
    private final static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    public void setWithGlossyBG(boolean withGlossyBG) {
        this.withGlossyBG = withGlossyBG;
    }

    private Color customBGColor = null;

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public void setCustomBGColor(Color customBGColor) {
        this.customBGColor = customBGColor;
    }

    private String toolTipText = null;

    private String origName = "Time",
            origValue = "00:00";

    public ClockDisplay(String name, String value) {
        instance = this;
        origName = name;
        origValue = value;
    }

    public ClockDisplay(String name, String value, String ttText, Color bgColor) {
        this(name, value, ttText, 36, bgColor);
    }

    public ClockDisplay(String name, String value, String ttText) {
        this(name, value, ttText, 36, Color.black);
    }

    public ClockDisplay(String name, String value, String ttText, int basicSize, Color bgColor) {
        instance = this;
        origName = name;
        origValue = value;
        toolTipText = ttText;
        this.fontSize = basicSize;
        try {
            jbInit(bgColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int fontSize = 36;

    private void jbInit() {
        jbInit(Color.black);
    }
    private void jbInit(Color bg) {
        try {
            digiFont = tryToLoadFont("ds-digi.ttf", this);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        if (digiFont == null) {
            digiFont = new Font("Courier New", Font.BOLD, fontSize);
        } else {
            digiFont = digiFont.deriveFont(Font.BOLD, fontSize);
        }
        digiFont = loadDigiFont();
        this.setLayout(gridBagLayout1);
        this.setBackground(bg);

        this.setSize(new Dimension(240, 240));
        this.setPreferredSize(new Dimension(100, 100));
        resize(fontSize);

//  this.setSize(new Dimension(120, 65));
//  this.setPreferredSize(new Dimension(120, 65));
        dataNameLabel.setText(origName);
        dataValueLabel.setText(origValue);
        if (toolTipText != null) {
            this.setToolTipText(toolTipText);
        }
        this.add(dataNameLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 20, 0), 0, 0));
        this.add(dataValueLabel,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(20, 0, 0, 0), 0, 0));

//  dataNameLabel.setFont(digiFont.deriveFont(Font.BOLD, 20));
        dataNameLabel.setForeground(displayColor);
        dataNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
//  dataValueLabel.setFont(digiFont.deriveFont(Font.BOLD, 40));
        dataValueLabel.setForeground(displayColor);
        dataValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dataValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
    }

    public void resize(int bigFontSize) {
        fontSize = bigFontSize;
        int width = (int) (120d * (double) fontSize / 36d);
        int height = (int) (120d * (double) fontSize / 36d);
        this.setSize(new Dimension(width, height));
        this.setPreferredSize(new Dimension(width, height));
        int big = (int) (40d * fontSize / 36d);
        int small = (int) (20d * fontSize / 36d);
        dataNameLabel.setFont(digiFont.deriveFont(Font.BOLD, small));
        dataValueLabel.setFont(digiFont.deriveFont(Font.BOLD, big));
    }

    private Font loadDigiFont() {
        Font f = null;
        try {
            f = tryToLoadFont("ds-digi.ttf", instance);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        if (f == null) {
            f = new Font("Courier New", Font.BOLD, fontSize);
        } else {
            f = f.deriveFont(Font.BOLD, fontSize);
        }
        return f;
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

    public void setName(String s) {
        dataNameLabel.setText(s);
    }

    public void setValue(long time) {
        this.value = time;
        String s = SDF.format(new Date(time));
        dataValueLabel.setText(s);
    }

    public void setDisplayColor(Color c) {
        displayColor = c;
        dataNameLabel.setForeground(displayColor);
        dataValueLabel.setForeground(displayColor);
    }

    public void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // resize(jumboFontSize);

//    Dimension dim =  this.getSize();
//    System.out.println("Dim:" + dim.getWidth() + "x" + dim.getHeight());
        if (false) {
            Color startColor = Color.black; // new Color(255, 255, 255);
            Color endColor = Color.gray; // new Color(102, 102, 102);
            //  GradientPaint gradient = new GradientPaint(0, 0, startColor, this.getWidth(), this.getHeight(), endColor); // Diagonal, top-left to bottom-right
            //  GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, this.getWidth(), 0, endColor); // Horizontal
            //  GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, this.getHeight(), endColor); // vertical

            GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
            ((Graphics2D) g).setPaint(gradient);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        if (withGlossyBG) {
            drawGlossyDisplay((Graphics2D) g,
                    new Point(0, 0),
                    new Point(this.getWidth(), this.getHeight()),
                    Color.gray,
                    Color.black,
                    1f);
        } else {
            // Use customBGColor
            drawFlatDisplay((Graphics2D) g,
                    new Point(0, 0),
                    new Point(this.getWidth(), this.getHeight()),
                    customBGColor,
                    1f);
        }
    }

    private void drawGlossyDisplay(Graphics2D g2d, Point topLeft, Point bottomRight, Color lightColor, Color darkColor, float transparency) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g2d.setPaint(null);

        g2d.setColor(darkColor);

        int width = bottomRight.x - topLeft.x;
        int height = bottomRight.y - topLeft.y;

        g2d.fillOval(topLeft.x, topLeft.y, width, height);

        Point gradientOrigin = new Point(0, //topLeft.x + (width) / 2,
                0);
        GradientPaint gradient = new GradientPaint(gradientOrigin.x,
                gradientOrigin.y,
                lightColor,
                gradientOrigin.x,
                gradientOrigin.y + (height / 3),
                darkColor); // vertical, light on top
        g2d.setPaint(gradient);
        int offset = (int) (width * 0.025);
        g2d.fillOval(topLeft.x + offset, topLeft.y + offset, (width - (2 * offset)), (height - (2 * offset)));

        g2d.setColor(gridColor);
        Composite comp = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        // Ticks
        Dimension dim = this.getSize();
        double radius = 0.9 * (Math.min(dim.width, dim.height) - 10d) / 2d;
        for (int i = 0; i < 360; i += 30) {
            int x1 = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians(i)));
            int y1 = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians(i)));
            int x2 = (dim.width / 2) + (int) ((radius) * Math.cos(Math.toRadians(i)));
            int y2 = (dim.height / 2) + (int) ((radius) * Math.sin(Math.toRadians(i)));
            g2d.drawLine(x1, y1, x2, y2);
        }
        // Hands
        if (this.value != 0L) {
            Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
            cal.setTimeInMillis(this.value);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int m = cal.get(Calendar.MINUTE);
            int s = cal.get(Calendar.SECOND);
            float decHour = h + (m / 60f) + (s / 3600f);
            if (decHour > 12) {
                decHour -= 12;
            }
            //  System.out.println("H:" + decHour + ", M:" + m + ", S:" + s);

            int xCenter = (dim.width / 2);
            int yCenter = (dim.height / 2);
            Stroke originalStroke = g2d.getStroke();
            //  Font origFont = g2d.getFont();
            //  g2d.setFont(new Font("Arial", 8, Font.PLAIN));
            // Hours
            Stroke stroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
            g2d.setStroke(stroke);
            radius = 0.7 * (Math.min(dim.width, dim.height) - 10d) / 2d;
            int x = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians((decHour * 30) - 90)));
            int y = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians((decHour * 30) - 90)));
            g2d.drawLine(xCenter, yCenter, x, y);
            //  g2d.drawString(Float.toString(decHour * 30), x, y);
            // Minutes
            stroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
            g2d.setStroke(stroke);
            radius = 0.9 * (Math.min(dim.width, dim.height) - 10d) / 2d;
            x = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians((m * 6) - 90)));
            y = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians((m * 6) - 90)));
            g2d.drawLine(xCenter, yCenter, x, y);
            //  g2d.drawString(Integer.toString(m * 6), x, y);
            // Seconds
            stroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
            g2d.setStroke(stroke);
            radius = 1.0 * (Math.min(dim.width, dim.height) - 10d) / 2d;
            x = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians((s * 6) - 90)));
            y = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians((s * 6) - 90)));
            g2d.drawLine(xCenter, yCenter, x, y);
            //  g2d.drawString(Integer.toString(s * 6), x, y);

            g2d.setStroke(originalStroke);
            //  g2d.setFont(origFont);
        }
        g2d.setComposite(comp);
    }

    private void drawFlatDisplay(Graphics2D g2d, Point topLeft, Point bottomRight, Color bgColor, float transparency) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g2d.setPaint(null);

        g2d.setColor(bgColor);

        int width = bottomRight.x - topLeft.x;
        int height = bottomRight.y - topLeft.y;

        g2d.fillOval(topLeft.x, topLeft.y, width, height);

        g2d.setColor(gridColor);
        Composite comp = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        // Ticks
        Dimension dim = this.getSize();
        double radius = 0.9 * (Math.min(dim.width, dim.height) - 10d) / 2d;
        for (int i = 0; i < 360; i += 30) {
            int x1 = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians(i)));
            int y1 = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians(i)));
            int x2 = (dim.width / 2) + (int) ((radius) * Math.cos(Math.toRadians(i)));
            int y2 = (dim.height / 2) + (int) ((radius) * Math.sin(Math.toRadians(i)));
            g2d.drawLine(x1, y1, x2, y2);
        }
        // Hands
        if (this.value != 0L) {
            Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
            cal.setTimeInMillis(this.value);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int m = cal.get(Calendar.MINUTE);
            int s = cal.get(Calendar.SECOND);
            float decHour = h + (m / 60f) + (s / 3600f);
            if (decHour > 12) {
                decHour -= 12;
            }
            //  System.out.println("H:" + decHour + ", M:" + m + ", S:" + s);

            int xCenter = (dim.width / 2);
            int yCenter = (dim.height / 2);
            Stroke originalStroke = g2d.getStroke();
            //  Font origFont = g2d.getFont();
            //  g2d.setFont(new Font("Arial", 8, Font.PLAIN));
            // Hours
            Stroke stroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
            g2d.setStroke(stroke);
            radius = 0.7 * (Math.min(dim.width, dim.height) - 10d) / 2d;
            int x = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians((decHour * 30) - 90)));
            int y = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians((decHour * 30) - 90)));
            g2d.drawLine(xCenter, yCenter, x, y);
            //  g2d.drawString(Float.toString(decHour * 30), x, y);
            // Minutes
            stroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
            g2d.setStroke(stroke);
            radius = 0.9 * (Math.min(dim.width, dim.height) - 10d) / 2d;
            x = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians((m * 6) - 90)));
            y = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians((m * 6) - 90)));
            g2d.drawLine(xCenter, yCenter, x, y);
            //  g2d.drawString(Integer.toString(m * 6), x, y);
            // Seconds
            stroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
            g2d.setStroke(stroke);
            radius = 1.0 * (Math.min(dim.width, dim.height) - 10d) / 2d;
            x = (dim.width / 2) + (int) ((radius - 10) * Math.cos(Math.toRadians((s * 6) - 90)));
            y = (dim.height / 2) + (int) ((radius - 10) * Math.sin(Math.toRadians((s * 6) - 90)));
            g2d.drawLine(xCenter, yCenter, x, y);
            //  g2d.drawString(Integer.toString(s * 6), x, y);

            g2d.setStroke(originalStroke);
            //  g2d.setFont(origFont);
        }

        g2d.setComposite(comp);
    }
}
