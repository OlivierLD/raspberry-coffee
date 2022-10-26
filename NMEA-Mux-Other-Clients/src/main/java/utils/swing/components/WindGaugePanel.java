package utils.swing.components;


import utils.WindUtils;

import javax.swing.*;
import java.awt.*;

public class WindGaugePanel
        extends JPanel {
    float tws = 0f;
    float max = -1f;

    private boolean glossy = true;

    private final static int FIVE_KNOTS_STEP = 0;
    private final static int BEAUFORT_SCALE = 1;

    private final static int DISPLAY_OPTION = BEAUFORT_SCALE;

    public void setGlossy(boolean glossy) {
        this.glossy = glossy;
    }

    public void setCustomBG(Color customBG) {
        this.customBG = customBG;
    }

    private Color customBG = null;

    public void setMax(float max) {
        this.max = max;
    }

    public WindGaugePanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(null);
        this.setSize(new Dimension(30, 200));
    }

    final private static Color[] colorfield = new Color[]{
            Color.white,             // 0-5
            new Color(21, 200, 232), // Blue 5-10
            new Color(19, 234, 186), // Lighter blue 10-15
            new Color(48, 232, 21),  // Green 15-20
            new Color(211, 239, 14), // Yellow 20-25
            new Color(232, 180, 21), // Orange 25-30
            new Color(232, 100, 21), // Darker Orange 30-35
            new Color(180, 8, 0),    // Red 35-40
            new Color(147, 4, 0),    // Dark red 40-45
            new Color(148, 4, 161)   // Purple 45-
    };

    public static Color getWindColor(float w) {
        int i = (int) w;
        int colorIdx = (int) (i / 5d);
        if (colorIdx > colorfield.length - 1) {
            colorIdx = colorfield.length - 1;
        }
        return colorfield[colorIdx];
    }

    public void paintComponent(Graphics gr) {
        ((Graphics2D) gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D) gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Graphics2D g2d = (Graphics2D) gr;
        // Gauge background
//  g2d.setColor(Color.black); 
        int gaugeHeight = this.getHeight();
        if (false) {
            Color startColor = Color.black; // new Color(255, 255, 255);
            Color endColor = Color.gray; // new Color(102, 102, 102);
            GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
            (g2d).setPaint(gradient);
            g2d.fillRect(0,
                    0,
                    this.getWidth(),
                    gaugeHeight);
        }
        if (glossy) {
            Point topLeft = new Point(0, 0);
            Point bottomRight = new Point(this.getWidth(), this.getHeight());
            drawGlossyRectangularDisplay((Graphics2D) gr,
                    topLeft,
                    bottomRight,
                    Color.lightGray,
                    Color.black,
                    1f);
            g2d.setColor(Color.white);
            g2d.drawRoundRect(topLeft.x, topLeft.y, this.getWidth(), this.getHeight(), 10, 10);
        } else if (customBG != null) {
            g2d.setColor(customBG);
            g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        // Data
        final int MAX_RANGE = 60;
        final int STEP = 5;

        int w = this.getWidth();
        if (DISPLAY_OPTION == BEAUFORT_SCALE) {
            int beaufort = WindUtils.getBeaufort(tws);
            int h = Math.round((float) gaugeHeight / 12f);
            for (int b = 1; b <= beaufort; b++) {
                int colorIdx = b - 1;
                if (colorIdx > colorfield.length - 1) colorIdx = colorfield.length - 1;
                Color c = colorfield[colorIdx];
                gr.setColor(c);
                gr.fillRoundRect(2,
                        gaugeHeight - (b * h) - b - 1,
                        w - 3,
                        h,
                        3, 3);
            }
        } else if (DISPLAY_OPTION == FIVE_KNOTS_STEP) {
            int h = (int) ((STEP - 2) * ((float) gaugeHeight / (float) MAX_RANGE));
            int i = 0;
            int y = 0;
            boolean last = false;
            boolean go = true;
            while (tws > 0f && !Double.isInfinite(tws) && go) {
                int colorIdx = (int) (i / 5d);
                if (colorIdx > colorfield.length - 1) colorIdx = colorfield.length - 1;
                Color c = colorfield[colorIdx];
                //      if (i > 5)
                //        c = Color.yellow;
                //      if (i > 25)
                //        c = Color.orange;
                //      if (i > 40)
                //        c = Color.red;
                //      if (i > 50)
                //        c = new Color(125, 2, 15); // Dark red
                gr.setColor(c);
                gr.fillRoundRect(1,
                        gaugeHeight - y - STEP - 1,
                        w - 2,
                        h,
                        2, 2);
                i += STEP;
                y += (h + 2);
                if (i > tws) {
                    if (!last) {
                        last = true;
//                    } else {
//                        go = false;
                    }
                    go = false;
                }
            }
        }
        if (max != -1) {
            gr.setColor(Color.red);
            int hMax = gaugeHeight - (int) (gaugeHeight * (max / (float) MAX_RANGE));
            gr.drawLine(0, hMax, this.getWidth(), hMax);
        }
    }

    public void setTws(float tws) {
        this.tws = tws;
    }

    @Override
    public void setToolTipText(String string) {
        super.setToolTipText("One notch is " + (DISPLAY_OPTION == BEAUFORT_SCALE ? "1 Beaufort" : "5 knots"));
    }

    public float getTws() {
        return tws;
    }

    private static void drawGlossyRectangularDisplay(Graphics2D g2d, Point topLeft, Point bottomRight, Color lightColor, Color darkColor, float transparency) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g2d.setPaint(null);

        g2d.setColor(darkColor);

        int width = bottomRight.x - topLeft.x;
        int height = bottomRight.y - topLeft.y;

        g2d.fillRoundRect(topLeft.x, topLeft.y, width, height, 10, 10);

        Point gradientOrigin = new Point(topLeft.x + (width) / 2, topLeft.y);
        GradientPaint gradient = new GradientPaint(gradientOrigin.x,
                gradientOrigin.y,
                lightColor,
                gradientOrigin.x,
                gradientOrigin.y + (height / 3),
                darkColor); // vertical, light on top
        g2d.setPaint(gradient);
        int offset = 1;
        int arcRadius = 5;
        g2d.fillRoundRect(topLeft.x + offset, topLeft.y + offset, (width - (2 * offset)), (height - (2 * offset)), 2 * arcRadius, 2 * arcRadius);
    }
}
