package gsg.SwingUtils;

import gsg.VectorUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static gsg.VectorUtils.Vector2D;

/**
 * A Reusable 2D WhiteBoard.
 * Designed to draw figures in a 2D coordinate system.
 * Suitable for Swing application, as Jupyter Notebooks (with iJava)
 */
public class WhiteBoardPanel extends JPanel {

    private static boolean verbose = "true".equals(System.getProperty("swing.verbose"));

    private List<DataSerie> allSeries = new ArrayList<>();

    public enum GraphicType {
        LINE,
        LINE_WITH_DOTS,
        CLOSED_LINE,            // aka Polygon...
        CLOSED_LINE_WITH_DOTS,
        POINTS,
        AREA,
        DONUT,
        PIE // More to come
    }

    public static class DataSerie {
        List<Vector2D> data;
        GraphicType graphicType = GraphicType.LINE;
        Color color = Color.BLACK;
        int lineThickness = 1;
        int circleDiam = 10; // needs to be even

        public DataSerie() {
        }

        public DataSerie data(List<Vector2D> data) {
            this.data = data;
            return this;
        }

        public DataSerie graphicType(GraphicType graphicType) {
            this.graphicType = graphicType;
            return this;
        }

        public DataSerie color(Color color) {
            this.color = color;
            return this;
        }

        public DataSerie lineThickness(int lineThickness) {
            this.lineThickness = lineThickness;
            return this;
        }

        public DataSerie circleDiam(int circleDiam) {
            this.circleDiam = circleDiam;
            return this;
        }

        public List<Vector2D> getData() {
            return data;
        }

        public GraphicType getGraphicType() {
            return graphicType;
        }

        public Color getColor() {
            return color;
        }

        public int getLineThickness() {
            return lineThickness;
        }

        public int getCircleDiam() {
            return circleDiam;
        }
    }

    private final static int MAX_TICK_PER_AXIS = 30;

    // Default values
    private final static int WIDTH = 860;
    private final static int HEIGHT = 600;
    private Dimension dimension = new Dimension(WIDTH, HEIGHT); // TODO Redundant with getSize() ?
    private int graphicMargins = 20;
    private Color axisColor = Color.BLACK;
    private Color textColor = Color.GRAY;
    private Color bgColor = Color.LIGHT_GRAY;
    private String graphicTitle = "Graphic Title"; // Set to null to remove
    private Font titleFont = null;
    private boolean withGrid = false;
    private boolean xEqualsY = true;

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
        this.setSize(dimension);
    }

    public void setGraphicMargins(int graphicMargins) {
        this.graphicMargins = graphicMargins;
    }

    public void setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public void setGraphicTitle(String graphicTitle) {
        this.graphicTitle = graphicTitle;
    }

    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    public void setWithGrid(boolean withGrid) {
        this.withGrid = withGrid;
    }

    public void setXEqualsY(boolean xEqualsY) {
        this.xEqualsY = xEqualsY;
    }

    private Consumer<Graphics2D> DEFAULT_DASHBOARD_WRITER = g2d -> {
        List<List<Vector2D>> allData = new ArrayList<>();
        allSeries.forEach(serie -> allData.add(serie.getData()));

        VectorUtils.GraphicRange graphicRange = VectorUtils.findGraphicRanges(allData);
        double xAmplitude = graphicRange.getMaxX() - graphicRange.getMinX();
        double yAmplitude = graphicRange.getMaxY() - graphicRange.getMinY();

        int margins = graphicMargins;

        double oneUnitX = (dimension.width - (2 * margins)) / xAmplitude;
        double oneUnitY = (dimension.height - (2 * margins)) / yAmplitude;
        double oneUnit = Math.min(oneUnitX, oneUnitY);
        if (verbose) {
            System.out.println(String.format("One Unit: %f (from X:%f, Y:%f)", oneUnit, oneUnitX, oneUnitY));
        }

        // Find best tick amount for the grid
        // How Many units, in height, and width
        int horizontalTicks = (int)Math.round((double)dimension.width / (xEqualsY ? oneUnit : oneUnitX));
        int verticalTicks = (int)Math.round((double)dimension.height / (xEqualsY ? oneUnit : oneUnitY));
        if (verbose) {
            System.out.println(String.format("%d vertical ticks, %d horizontal ticks.", verticalTicks, horizontalTicks));
        }
        int biggestTick = Math.max(horizontalTicks, verticalTicks);

        double ratioX = (double)horizontalTicks / (double) MAX_TICK_PER_AXIS;
        double ratioY = (double)verticalTicks / (double) MAX_TICK_PER_AXIS;
        int tickIncrementX = Math.max((int)Math.round(ratioX), 1);
        int tickIncrementY = Math.max((int)Math.round(ratioY), 1);

        double ratio = (double)biggestTick / (double) MAX_TICK_PER_AXIS;
        int tickIncrement = Math.max((int)Math.round(ratio), 1);
        if (verbose) {
            System.out.printf("tickIncrement: %d (from %d, %d)%n", tickIncrement, tickIncrementX, tickIncrementY);
        }

        // Transformers
        Function<Double, Integer> findCanvasXCoord = spaceXCoord -> (int)(margins + (Math.round((spaceXCoord - graphicRange.getMinX()) * (xEqualsY ? oneUnit : oneUnitX))));
        Function<Double, Integer> findCanvasYCoord = spaceYCoord -> (int)(margins + (Math.round((spaceYCoord - graphicRange.getMinY()) * (xEqualsY ? oneUnit : oneUnitY))));

        double x0 = Math.floor(findCanvasXCoord.apply(graphicRange.getMinX() /*0d*/)); // Math.round(0 - graphicRange.getMinX()) * oneUnit;
        double y0 = Math.floor(findCanvasYCoord.apply(graphicRange.getMinY() /*0d*/)); // Math.round(0 - graphicRange.getMinY()) * oneUnit;

        if (verbose) {
            System.out.println(String.format("x0: %f (minX: %f), y0: %f (minY: %f)", x0, graphicRange.getMinY(), y0, graphicRange.getMinY()));
        }

        // Graphic scaffolding
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, dimension.width, dimension.height);

        // Actual working zone, from graphicRange
        g2d.setColor(Color.PINK); // GRAY);
        // graphicRange.getMaxX() graphicRange.getMinX() graphicRange.getMaxY() graphicRange.getMinY()
        int minX = findCanvasXCoord.apply(graphicRange.getMinX());
        int maxX = findCanvasXCoord.apply(graphicRange.getMaxX());
        int minY = findCanvasYCoord.apply(graphicRange.getMinY());
        int maxY = findCanvasYCoord.apply(graphicRange.getMaxY());
        if (verbose) {
            System.out.println("-----------------------------------------------------");
        }
        int height = this.getSize().height;
        int width  = this.getSize().width;
        if (verbose) {
            System.out.println(String.format("HxW: %d x %d", height, width));
            System.out.println(String.format(">> Working Rectangle: x:%d, y:%d, w:%d, h:%d", minX, height - maxY, (maxX - minX), (maxY - minY)));
            System.out.println("-----------------------------------------------------");
        }
        g2d.drawRect(minX, height - maxY, (maxX - minX), (maxY - minY));

        // Label font
        int labelFontSize = 10;
        Font labelFont = new Font("Courier New", Font.PLAIN, labelFontSize);
        g2d.setFont(labelFont);

        // Vertical X (left) Arrow
        g2d.setStroke(new BasicStroke(2));             // Line Thickness
        WhiteBoardPanel.drawArrow(g2d,
                new Point((int)Math.round(x0), height),
                new Point((int)Math.round(x0), 0),
                axisColor);
        g2d.setStroke(new BasicStroke(1));             // Line Thickness

        // X Notches or grid
        g2d.setColor(axisColor);
        int xTick = (int)Math.floor(graphicRange.getMinX()); // 0;
        int canvasX = 0;
        while (canvasX <= width) {
            canvasX = findCanvasXCoord.apply((double)xTick);
//            System.out.printf("X notch %d%n", xTick);
            g2d.setStroke(new BasicStroke(xTick == 0 ? 2 : 1));
            if (canvasX <= width) {
                if (withGrid) {
                    g2d.drawLine(canvasX, height, canvasX, 0);
                } else {
                    g2d.drawLine(canvasX, height - (int) Math.round(y0 - 5),
                            canvasX, height - (int) Math.round(y0 + 5));
                }
                String label = String.valueOf(xTick);
                int strWidth = g2d.getFontMetrics(labelFont).stringWidth(label);
                g2d.drawString(label, canvasX - (strWidth / 2),height - (int) Math.round(y0 - 5 - (labelFont.getSize())));
            }
            xTick += (xEqualsY ? tickIncrement : tickIncrementX);
        }

        // Horizontal Y (bottom) Arrow
        g2d.setStroke(new BasicStroke(2));             // Line Thickness
        WhiteBoardPanel.drawArrow(g2d,
                new Point(0, height - (int)Math.round(y0)),
                new Point(width, height - (int)Math.round(y0)),
                axisColor);

        g2d.setStroke(new BasicStroke(1));             // Line Thickness
        // Y Notches
        g2d.setColor(axisColor);
        int yTick = (int)Math.floor(graphicRange.getMinY()); // 0;
        int canvasY = 0;
        while (canvasY <= height) {
            canvasY = findCanvasYCoord.apply((double)yTick);
//            System.out.printf("Y notch %d%n", yTick);
            g2d.setStroke(new BasicStroke(yTick == 0 ? 2 : 1));
            if (canvasY <= height) {
                if (withGrid) {
                    g2d.drawLine(0, height - canvasY,
                            width, height - canvasY);
                } else {
                    g2d.drawLine((int) Math.round(x0 - 5), height - canvasY,
                            (int) Math.round(x0 + 5), height - canvasY);
                }
                String label = String.valueOf(yTick);
                int strWidth = g2d.getFontMetrics(labelFont).stringWidth(label);
                g2d.drawString(label, (int) Math.round(x0 - 5) - strWidth - 2, height - canvasY + (int)(labelFont.getSize() * 0.9 / 2));
            }
            yTick += (xEqualsY ? tickIncrement : tickIncrementY);
        }

        // For the text
        if (graphicTitle != null) {
            g2d.setColor(textColor);
            if (titleFont == null) {
                g2d.setFont(g2d.getFont().deriveFont(Font.BOLD | Font.ITALIC).deriveFont(24f));
            } else {
                g2d.setFont(titleFont);
            }
            g2d.drawString(graphicTitle, 10, 60);
        }

        // Now the data, Series
        allSeries.forEach(serie -> {
            if (serie.getGraphicType().equals(GraphicType.LINE) || serie.getGraphicType().equals(GraphicType.CLOSED_LINE) ||
                serie.getGraphicType().equals(GraphicType.LINE_WITH_DOTS)  || serie.getGraphicType().equals(GraphicType.CLOSED_LINE_WITH_DOTS)) {
                g2d.setColor(serie.getColor()); // Line Color
                g2d.setStroke(new BasicStroke(serie.getLineThickness()));             // Line Thickness
                boolean withPoints = serie.getGraphicType().equals(GraphicType.LINE_WITH_DOTS)  || serie.getGraphicType().equals(GraphicType.CLOSED_LINE_WITH_DOTS);
                Point previous = null;
                for (Vector2D v : serie.getData()) {
                    int pointX = findCanvasXCoord.apply(v.getX());
                    int pointY = findCanvasYCoord.apply(v.getY());
//              System.out.println(String.format("x:%f, y:%f => X:%d, Y:%d", x[i], y[i], pointX, pointY));
                    Point here = new Point(pointX, pointY);
                    if (withPoints) {
                        g2d.fillOval(pointX - (serie.circleDiam / 2),
                                height - pointY - (serie.circleDiam / 2),
                                serie.circleDiam, serie.circleDiam);
                    }
                    if (previous != null) {
                        g2d.drawLine(previous.x, height - previous.y, here.x, height - here.y);
                    }
                    previous = here;
                }
                if (serie.getGraphicType().equals(GraphicType.CLOSED_LINE) || serie.getGraphicType().equals(GraphicType.CLOSED_LINE_WITH_DOTS)) {
                    if (previous != null) { // Close the loop
                        g2d.drawLine(
                                previous.x,
                                height - previous.y,
                                findCanvasXCoord.apply(serie.getData().get(0).getX()),
                                height - findCanvasYCoord.apply(serie.getData().get(0).getY()));
                    }
                }
            } else if (serie.getGraphicType().equals(GraphicType.POINTS)) {
                g2d.setColor(serie.getColor()); // Plot Color
                int circleDiam = serie.getCircleDiam();
                serie.getData().forEach(v -> {
                    int pointX = findCanvasXCoord.apply(v.getX());
                    int pointY = findCanvasYCoord.apply(v.getY());
//              System.out.println(String.format("x:%f, y:%f => X:%d, Y:%d", x[i], y[i], pointX, pointY));
                    g2d.fillOval(pointX - (circleDiam / 2),
                            height - pointY - (circleDiam / 2),
                            circleDiam, circleDiam);

                });
            } else {
//                case GraphicType.AREA:
//                case GraphicType.PIE:
                System.out.println(String.format("Type %s not managed yet", serie.getGraphicType()));
            }
        });
    };

    // THE Renderer. The most important part here. Invoked from the paintComponent method.
    private Consumer<Graphics2D> whiteBoardWriter = DEFAULT_DASHBOARD_WRITER;

    // Graphic title, margins, axis color, fonts, etc, as setters.
    public WhiteBoardPanel() {
    }

    public WhiteBoardPanel(Consumer<Graphics2D> whiteBoardWriter) {
        this();
        this.whiteBoardWriter = whiteBoardWriter;
    }

    public void setWhiteBoardWriter(Consumer<Graphics2D> whiteBoardWriter) {
        this.whiteBoardWriter = whiteBoardWriter;
    }

    public void resetDefaultWhiteBoardWriter() {
        this.whiteBoardWriter = DEFAULT_DASHBOARD_WRITER;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
//      System.out.println("paintComponent invoked on JPanel");
        Graphics2D g2d = (Graphics2D) g;
        whiteBoardWriter.accept(g2d);     // Invoke the whiteBoardWriter
    }

    /**
     * Save the current view to a file
     * @param f the file to create
     * @param ext the iage extension (jpg, png, etc)
     * @param w image width
     * @param h image height
     */
    public void createImage(File f, String ext, int w, int h) {
        int width = w;
        int height = h;

        // Create a buffered image in which to draw
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final WhiteBoardPanel instance = this;
        Thread refreshThread = new Thread(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    // Create a graphics contents on the buffered image
                    Graphics2D g2d = bufferedImage.createGraphics();
                    instance.paintComponent((Graphics) g2d);
                    // Write generated image to a file
                    try {
                        OutputStream os = new FileOutputStream(f);
                        ImageIO.write(bufferedImage, ext, os);
                        os.flush();
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Graphics context no longer needed so dispose it
                    g2d.dispose();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        refreshThread.start();
    }

    public static void drawArrow(Graphics2D g, Point from, Point to, Color c) {
        drawArrow(g, from, to, c, 30);
    }

    public static void drawArrow(Graphics2D g, Point from, Point to, Color c, int hl) {
        Color orig = null;
        if (g != null) {
            orig = g.getColor();
        }
        int headLength = hl;
        double headHalfAngle = 15D;

        double dir = getDir((from.x - to.x), (to.y - from.y));
//      System.out.println("Dir:" + dir);

        g.setColor(c);
        Point left = new Point((int) (to.x - (headLength * Math.cos(Math.toRadians(dir - 90 + headHalfAngle)))),
                (int) (to.y - (headLength * Math.sin(Math.toRadians(dir - 90 + headHalfAngle)))));
        Point right = new Point((int) (to.x - (headLength * Math.cos(Math.toRadians(dir - 90 - headHalfAngle)))),
                (int) (to.y - (headLength * Math.sin(Math.toRadians(dir - 90 - headHalfAngle)))));

        g.drawLine(from.x, from.y, to.x, to.y);
        Polygon head = new Polygon(new int[]{to.x, left.x, right.x}, new int[]{to.y, left.y, right.y}, 3);
        g.fillPolygon(head);

        if (g != null) {
            g.setColor(orig);
        }
    }

    /**
     * Warning: this one is adding 180 to the direction.
     * @param x
     * @param y
     * @return direction, [0..360[
     */
    private static double getDir(double x, double y) {
        double direction = 180 + Math.toDegrees(Math.atan2(x, y));
        while (direction < 0) {
            direction += 360;
        }
        direction %= 360;
        return direction;
    }

    public void addSerie(DataSerie serie) {
        this.allSeries.add(serie);
    }

    public void removeSerie(DataSerie serie) {
        if (this.allSeries.contains(serie)) {
            this.allSeries.remove(serie);
        }
    }

    public void resetAllData() {
        this.allSeries.clear();
    }

    /**
     * Call this from a Jupyter Notebook (iJava) !
     * @return The expected BufferedImage
     */
    public BufferedImage getImage() {
        Dimension size = this.getSize();
        final BufferedImage bufferedImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        this.paintComponent(g2d);
        return bufferedImage;
    }
}