package utils.swing.components;

// import coreutilities.ctx.CoreContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class HeadingPanel
        extends JPanel
        implements MouseListener, MouseMotionListener {
    @SuppressWarnings("compatibility:8059530136393454267")
    private final static long serialVersionUID = 1L;

    public final static int ROSE = 0;
    public final static int ZERO_TO_360 = 1;
    public final static int MINUS_180_TO_PLUS_180 = 2;

    private int roseType = ROSE;

    private int hdg = 0;
    private int prevHdg = 0;
    private boolean whiteOnBlack = true;
    private boolean draggable = false;
    private float roseWidth = 60;
    private boolean withNumber = true;
    private boolean withCardinalPoints = true;

    private boolean glossy = false;
    private boolean smooth = false;

    private String ttPrefix = "";

    public void setTtPrefix(String ttPrefix) {
        this.ttPrefix = ttPrefix;
    }

    public HeadingPanel() {
        this(ROSE);
    }

    public HeadingPanel(boolean b) {
        this(ROSE, b);
    }

    public HeadingPanel(int roseOption) {
        this(roseOption, false);
    }

    public HeadingPanel(int roseOption, boolean b) {
        this.roseType = roseOption;
        this.glossy = b;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(null);
        this.setSize(new Dimension(200, 50));
        this.setMinimumSize(new Dimension(200, 50));
        this.setPreferredSize(new Dimension(200, 50));
    }

    public void setValue(double d) {
        hdg = (int) d;
        repaint();
    }

    private boolean withColorGradient = true;
    private boolean withCustomColors = false;
    private int labelStep = 15;
    private int digitFontSize = 12;

    public void setDigitFontSize(int digitFontSize) {
        this.digitFontSize = digitFontSize;
    }

    public void setLabelStep(int labelStep) {
        this.labelStep = labelStep;
    }

    private Color bgColor = null,
            tickColor = null;

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public void setTickColor(Color tickColor) {
        this.tickColor = tickColor;
    }

    public void setWithCustomColors(boolean withCustomColors) {
        this.withCustomColors = withCustomColors;
    }

    public void setWithColorGradient(boolean withColorGradient) {
        this.withColorGradient = withColorGradient;
    }

    public void paintComponent(Graphics gr) {
        Graphics2D g2d = (Graphics2D) gr;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = this.getWidth();
        int h = this.getHeight();

        Color startColor = Color.black; // new Color(255, 255, 255);
        Color endColor = Color.gray; // new Color(102, 102, 102);
        if (!whiteOnBlack) {
            startColor = Color.lightGray;
            endColor = Color.white;
        }

        if (glossy) {
            drawGlossyRectangularDisplay(g2d,
                    new Point(0, 0),
                    new Point(this.getWidth(), this.getHeight()),
                    endColor,
                    startColor,
                    1f);
        } else {
            boolean doFill = true;
            if (withColorGradient) {
                //    GradientPaint gradient = new GradientPaint(0, 0, startColor, this.getWidth(), this.getHeight(), endColor);
                //    GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, this.getWidth(), 0, endColor); // Horizontal
                //    GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, this.getHeight(), endColor); // vertical
                GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
                g2d.setPaint(gradient);
            } else {
                if (!withCustomColors)
                    g2d.setColor(whiteOnBlack ? Color.black : Color.white);
                else if (bgColor != null)
                    g2d.setColor(bgColor);
                else if (bgColor == null)
                    doFill = false;
            }
            if (doFill)

                g2d.fillRect(0, 0, w, h);
        }
        // Width: 30 on each side = 60 (default)
        if (!withCustomColors)
            g2d.setColor(whiteOnBlack ? Color.white : Color.black);
        else
            g2d.setColor(tickColor);
        float oneDegree = (float) w / roseWidth; // 30 degrees each side
        // One graduation every 1 & 5, one label every 'labelStep'
        for (int rose = hdg - (int) (roseWidth / 2f); rose <= hdg + (int) (roseWidth / 2f); rose++) {
            int roseToDisplay = rose;
            while (roseToDisplay >= 360) roseToDisplay -= 360;
            while (roseToDisplay < 0) roseToDisplay += 360;
            int abscisse = Math.round((float) (rose + (roseWidth / 2f) - hdg) * oneDegree);
//    System.out.println("(w=" + w + ") Abscisse for " + rose + "=" + abscisse);
            g2d.drawLine(abscisse, 0, abscisse, 2);
            g2d.drawLine(abscisse, h - 2, abscisse, h);
            if (rose % 5 == 0) {
                g2d.drawLine(abscisse, 0, abscisse, 5);
                g2d.drawLine(abscisse, h - 5, abscisse, h);
            }
            if (rose % labelStep == 0) {
                Font f = g2d.getFont();
                g2d.setFont(new Font("Arial", Font.BOLD, digitFontSize));
                String roseStr = Integer.toString(Math.round(roseToDisplay));
                boolean cardinal = false;
                if (withCardinalPoints) {
                    roseStr = getRoseStr(roseToDisplay);
                    if (roseStr.trim().length() > 0)
                        cardinal = true;
                }
//      System.out.println("String:" + roseStr);
//      try { int x = Integer.parseInt(roseStr); } catch (NumberFormatException nfe) { cardinal = true; } // Deprecated, see above
                if (withNumber || (cardinal && withCardinalPoints)) {
                    int strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(roseStr);
                    g2d.drawString(roseStr, abscisse - strWidth / 2, (h / 2) + (digitFontSize / 2));
                }
                g2d.setFont(f);
            }
        }
        g2d.setColor(Color.red);
        g2d.drawLine(w / 2, 0, w / 2, h);
        //
        while (hdg < 0) hdg += 360;
//  this.setToolTipText(ttPrefix + Integer.toString(hdg % 360) + "\272");
    }

    private String getRoseStr(int rtd) {
        String roseStr = Integer.toString(rtd);
        if (rtd == 0) {
            if (roseType == ROSE)
                roseStr = "N";
            else if (roseType == MINUS_180_TO_PLUS_180)
                roseStr = "0";
            else
                roseStr = "0";
        } else if (rtd == 180) {
            if (roseType == ROSE)
                roseStr = "S";
            else if (roseType == MINUS_180_TO_PLUS_180)
                roseStr = "180";
            else
                roseStr = "180";
        } else if (rtd == 90) {
            if (roseType == ROSE)
                roseStr = "E";
            else if (roseType == MINUS_180_TO_PLUS_180)
                roseStr = "90";
            else
                roseStr = "90";
        } else if (rtd == 270) {
            if (roseType == ROSE)
                roseStr = "W";
            else if (roseType == MINUS_180_TO_PLUS_180)
                roseStr = "90";
            else
                roseStr = "270";
        } else if (rtd == 45) {
            if (roseType == ROSE)
                roseStr = "NE";
            else if (roseType == MINUS_180_TO_PLUS_180)
                roseStr = "45";
            else
                roseStr = "45";
        } else if (rtd == 135) {
            if (roseType == ROSE)
                roseStr = "SE";
            else if (roseType == MINUS_180_TO_PLUS_180)
                roseStr = "135";
            else
                roseStr = "135";
        } else if (rtd == 225) {
            if (roseType == ROSE)
                roseStr = "SW";
            else if (roseType == MINUS_180_TO_PLUS_180)
                roseStr = "135";
            else
                roseStr = "225";
        } else if (rtd == 315) {
            if (roseType == ROSE)
                roseStr = "NW";
            else if (roseType == MINUS_180_TO_PLUS_180)
                roseStr = "45";
            else
                roseStr = "315";
        }

        return roseStr;
    }

    public void setHdg(int heading) {
        if (!smooth) {
            this.hdg = heading;
//    repaint();
            final HeadingPanel instance = this;
            Thread thread = new Thread() {
                public void run() {
                    try {
//            SwingUtilities.invokeAndWait(new Runnable()
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                instance.repaint();
                            }
                        });
                    }
//            catch (InvocationTargetException ite)
//            {
//              ite.printStackTrace();
//            }
//            catch (InterruptedException ie)
//            {
//              ie.printStackTrace();
//            }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            try {
                thread.start();
                thread.join();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        } else {
            this.hdg = heading;
            int from = prevHdg;
            int to = hdg;

            // Manage the case 350-10
            if (Math.abs(prevHdg - hdg) > 180) {
                if (Math.signum(Math.cos(Math.toRadians(prevHdg))) == Math.signum(Math.cos(Math.toRadians(hdg)))) {
                    if (from > to)
                        to += 360;
                    else
                        to -= 360;
                }
            }

            int sign = (from > to) ? -1 : 1;
            prevHdg = hdg;
            // Smooth rotation
            for (int h = from; (sign == 1 && h <= to) || (sign == -1 && h >= to); h += (1 * sign)) {
                final int _h = h;
                try {
                    // For a smooth move of the hand
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            int _heading = _h % 360;
                            while (_heading < 0) _heading += 360;
                            hdg = _heading;
                            repaint();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    public int getHdg() {
        return hdg;
    }

    public void setWhiteOnBlack(boolean whiteOnBlack) {
        this.whiteOnBlack = whiteOnBlack;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
        if (draggable) {
            addMouseMotionListener(this);
            addMouseListener(this);
        }
    }

    public void setRoseWidth(float roseWidth) {
        this.roseWidth = roseWidth;
    }

    public float getRoseWidth() {
        return roseWidth;
    }

    public void setWithNumber(boolean withNumber) {
        this.withNumber = withNumber;
    }

    public void setWithCardinalPoints(boolean withCardinalPoints) {
        this.withCardinalPoints = withCardinalPoints;
    }

    public boolean isWithCardinalPoints() {
        return withCardinalPoints;
    }

    private static void drawGlossyRectangularDisplay(Graphics2D g2d, Point topLeft, Point bottomRight, Color lightColor, Color darkColor, float transparency) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g2d.setPaint(null);

        g2d.setColor(darkColor);

        int width = bottomRight.x - topLeft.x;
        int height = bottomRight.y - topLeft.y;

        g2d.fillRoundRect(topLeft.x, topLeft.y, width, height, 10, 10);

        Point gradientOrigin = new Point(0, //topLeft.x + (width) / 2,
                0);
        GradientPaint gradient = new GradientPaint(gradientOrigin.x,
                gradientOrigin.y,
                lightColor,
                gradientOrigin.x,
                gradientOrigin.y + (height / 3),
                darkColor); // vertical, light on top
        g2d.setPaint(gradient);
        int offset = 1; //(int)(width * 0.025);
        int arcRadius = 5;
        g2d.fillRoundRect(topLeft.x + offset, topLeft.y + offset, (width - (2 * offset)), (height - (2 * offset)), 2 * arcRadius, 2 * arcRadius);
    }

    public void mouseDragged(MouseEvent e) {
        int hdgOffset = (int) ((mousePressedFrom.x - e.getPoint().x) * (60D / (double) this.getWidth()));
        hdg = headingFrom + hdgOffset;
        this.repaint();
        // CoreContext.getInstance().fireHeadingHasChanged(hdg);
    }

    public void mouseMoved(MouseEvent e) {
        int hdgOffset = (int) (((this.getWidth() / 2) - e.getPoint().x) * (60D / (double) this.getWidth()));
        int mouseHdg = hdg - hdgOffset;
        while (mouseHdg < 0) mouseHdg += 360;
        this.setToolTipText(Integer.toString(mouseHdg % 360) + "\272");
    }

    public void mouseClicked(MouseEvent e) {
    }

    Point mousePressedFrom = null;
    int headingFrom = 0;

    public void mousePressed(MouseEvent e) {
        mousePressedFrom = e.getPoint();
        headingFrom = hdg;
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }
}
