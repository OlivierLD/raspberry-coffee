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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static gsg.VectorUtils.Vector2D;

/**
 * A Reusable 2D WhiteBoard.
 * Designed to draw figures in a 2D coordinate system.
 * Suitable for Swing application, as Jupyter Notebooks (with iJava)
 */
public class WhiteBoardPanel extends JPanel {

    private final static boolean VERBOSE = "true".equals(System.getProperty("swing.verbose"));

    private List<DataSerie> allSeries = new ArrayList<>();

    public enum GraphicType {
        LINE,                   // Solid
        DOTTED_LINE,
        LINE_WITH_DOTS,
        CLOSED_LINE,            // aka Polygon...
        CLOSED_DOTTED_LINE,
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
        Color gradientTop = null;
        Color gradientBottom = null;
        Color bgColor = new Color(0.5f, 0.5f, 0.5f, 0.5f);

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

        public DataSerie areaGradient(Color top, Color bottom) { // Set both to null to use bgColor
            this.gradientTop = top;
            this.gradientBottom = bottom;
            return this;
        }

        public DataSerie bgColor(Color color) {
            this.bgColor = color;
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
        public Color getBGColor() {
            return bgColor;
        }

        public Color getGradientTop() {
            return this.gradientTop;
        }
        public Color getGradientBottom() {
            return this.gradientBottom;
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
    private final static int DEFAULT_WIDTH = 860;
    private final static int DEFAULT_HEIGHT = 600;
    private int graphicMargins = 20;
    private Color axisColor = Color.BLACK;
    private Color textColor = Color.GRAY;
    private Color bgColor = Color.LIGHT_GRAY;
    private String graphicTitle = "Graphic Title"; // Set to null to remove
    private Font titleFont = null;
    private boolean withGrid = false;
    private boolean xEqualsY = true;

    private Double forcedMinY = null;
    private Double forcedMaxY = null;

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

    public Double getForcedMinY() {
        return forcedMinY;
    }

    public void setForcedMinY(Double forcedMinY) {
        this.forcedMinY = forcedMinY;
    }

    public Double getForcedMaxY() {
        return forcedMaxY;
    }

    public void setForcedMaxY(Double forcedMaxY) {
        this.forcedMaxY = forcedMaxY;
    }

    private final Consumer<Graphics2D> DEFAULT_DASHBOARD_WRITER = g2d -> {
        List<List<Vector2D>> allData = new ArrayList<>();
        allSeries.forEach(serie -> { synchronized(serie) { allData.add(serie.getData()); } });

        VectorUtils.GraphicRange graphicRange = VectorUtils.findGraphicRanges(allData);
        double xAmplitude = graphicRange.getMaxX() - graphicRange.getMinX();
        // Warning: Not supported by Java 8
        double minDblY = Objects.requireNonNullElseGet(forcedMinY,
                () -> graphicRange.getMinY());
        double maxDblY = Objects.requireNonNullElseGet(forcedMaxY,
                () -> graphicRange.getMaxY());
        double yAmplitude = maxDblY - minDblY;

        int margins = graphicMargins;

        double oneUnitX = (this.getSize().width - (2 * margins)) / xAmplitude;
        double oneUnitY = (this.getSize().height - (2 * margins)) / yAmplitude;
        double oneUnit = Math.min(oneUnitX, oneUnitY);
        if (VERBOSE) {
            System.out.printf("One Unit: %f (from X:%f, Y:%f)%n", oneUnit, oneUnitX, oneUnitY);
        }

        // Find best tick amount for the grid
        // How Many units, in height, and width
        int horizontalTicks = (int)Math.round((double)this.getSize().width / (xEqualsY ? oneUnit : oneUnitX));
        int verticalTicks = (int)Math.round((double)this.getSize().height / (xEqualsY ? oneUnit : oneUnitY));
        if (VERBOSE) {
            System.out.printf("%d vertical ticks, %d horizontal ticks.%n", verticalTicks, horizontalTicks);
        }
        int biggestTick = Math.max(horizontalTicks, verticalTicks);

        double ratioX = (double)horizontalTicks / (double) MAX_TICK_PER_AXIS;
        double ratioY = (double)verticalTicks / (double) MAX_TICK_PER_AXIS;
        int tickIncrementX = Math.max((int)Math.round(ratioX), 1);
        int tickIncrementY = Math.max((int)Math.round(ratioY), 1);

        double ratio = (double)biggestTick / (double) MAX_TICK_PER_AXIS;
        int tickIncrement = Math.max((int)Math.round(ratio), 1);
        if (VERBOSE) {
            System.out.printf("tickIncrement: %d (from %d, %d)%n", tickIncrement, tickIncrementX, tickIncrementY);
        }

        // Transformers
        Function<Double, Integer> findCanvasXCoord = spaceXCoord -> (int)(margins + (Math.round((spaceXCoord - graphicRange.getMinX()) * (xEqualsY ? oneUnit : oneUnitX))));
        Function<Double, Integer> findCanvasYCoord = spaceYCoord -> (int)(margins + (Math.round((spaceYCoord - minDblY) * (xEqualsY ? oneUnit : oneUnitY))));

        double x0 = Math.floor(findCanvasXCoord.apply(graphicRange.getMinX() /*0d*/)); // Math.round(0 - graphicRange.getMinX()) * oneUnit;
        double y0 = Math.floor(findCanvasYCoord.apply(minDblY /*0d*/)); // Math.round(0 - graphicRange.getMinY()) * oneUnit;

        if (VERBOSE) {
            System.out.printf("x0: %f (minX: %f), y0: %f (minY: %f)%n", x0, graphicRange.getMinX(), y0, minDblY);
        }

        // Graphic scaffolding
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, this.getSize().width, this.getSize().height);

        // Actual working zone, from graphicRange
        g2d.setColor(Color.PINK); // GRAY);
        // graphicRange.getMaxX() graphicRange.getMinX() graphicRange.getMaxY() graphicRange.getMinY()
        int minX = findCanvasXCoord.apply(graphicRange.getMinX());
        int maxX = findCanvasXCoord.apply(graphicRange.getMaxX());
        int minY = findCanvasYCoord.apply(minDblY);
        int maxY = findCanvasYCoord.apply(maxDblY);
        if (VERBOSE) {
            System.out.println("-----------------------------------------------------");
        }
        int height = this.getSize().height;
        int width  = this.getSize().width;
        if (VERBOSE) {
            System.out.printf("HxW: %d x %d%n", height, width);
            System.out.printf(">> Working Rectangle: x:%d, y:%d, w:%d, h:%d%n", minX, height - maxY, (maxX - minX), (maxY - minY));
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
        int yTick = (int)Math.floor(minDblY); // 0;
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
            g2d.setFont(Objects.requireNonNullElseGet(titleFont,
                    () -> g2d.getFont().deriveFont(Font.BOLD | Font.ITALIC).deriveFont(24f)));
            g2d.drawString(graphicTitle, 10, 60);
        }

        // Now the data, Series
        allSeries.forEach(serie -> {
            if (serie.getGraphicType().equals(GraphicType.LINE) ||
                serie.getGraphicType().equals(GraphicType.CLOSED_LINE) ||
                serie.getGraphicType().equals(GraphicType.DOTTED_LINE) ||
                serie.getGraphicType().equals(GraphicType.CLOSED_DOTTED_LINE) ||
                serie.getGraphicType().equals(GraphicType.LINE_WITH_DOTS)  ||
                serie.getGraphicType().equals(GraphicType.CLOSED_LINE_WITH_DOTS)) {
                g2d.setColor(serie.getColor()); // Line Color
                Stroke stroke;
                if (serie.getGraphicType().equals(GraphicType.DOTTED_LINE) ||
                    serie.getGraphicType().equals(GraphicType.CLOSED_DOTTED_LINE)) {
                    stroke = new BasicStroke(serie.getLineThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0.0f, new float[] { 9f }, 0f);
                } else {
                    stroke = new BasicStroke(serie.getLineThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);             // Line Thickness
                }
                g2d.setStroke(stroke);
                boolean withPoints = serie.getGraphicType().equals(GraphicType.LINE_WITH_DOTS)  ||
                                     serie.getGraphicType().equals(GraphicType.CLOSED_LINE_WITH_DOTS);
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
                if (serie.getGraphicType().equals(GraphicType.CLOSED_LINE) ||
                    serie.getGraphicType().equals(GraphicType.CLOSED_DOTTED_LINE) ||
                    serie.getGraphicType().equals(GraphicType.CLOSED_LINE_WITH_DOTS)) {
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
            } else if (serie.getGraphicType().equals(GraphicType.AREA)) {
                g2d.setColor(serie.getColor());
                if (serie.getGradientTop() != null && serie.getGradientBottom() != null) {
                    g2d.setPaint(this.getGradientPaint(serie.getGradientTop(), serie.getGradientBottom()));
                } else {
                    g2d.setPaint(serie.getBGColor());
                }
                // Background
                final Polygon polygon = new Polygon();
                polygon.addPoint(graphicMargins, height - graphicMargins);
                serie.getData().forEach(v -> {
                    int pointX = findCanvasXCoord.apply(v.getX());
                    int pointY = findCanvasYCoord.apply(v.getY());
                    polygon.addPoint(pointX, height - pointY);
                });
                polygon.addPoint(width - graphicMargins, height - graphicMargins);
                g2d.fillPolygon(polygon);
                // The curve
                if (serie.getColor() != null) {
                    g2d.setColor(serie.getColor()); // Line Color
                    Stroke stroke = new BasicStroke(serie.getLineThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);             // Line Thickness
                    g2d.setStroke(stroke);
                    Point previous = null;
                    for (Vector2D v : serie.getData()) {
                        int pointX = findCanvasXCoord.apply(v.getX());
                        int pointY = findCanvasYCoord.apply(v.getY());
//                      System.out.println(String.format("x:%f, y:%f => X:%d, Y:%d", x[i], y[i], pointX, pointY));
                        Point here = new Point(pointX, pointY);
                        if (previous != null) {
                            g2d.drawLine(previous.x, height - previous.y, here.x, height - here.y);
                        }
                        previous = here;
                    }
                }
            } else {
//                case GraphicType.DONUT:
//                case GraphicType.PIE:
                System.out.printf("Type %s not managed yet%n", serie.getGraphicType());
            }
//            g2d.dispose();
        });
    };

    private GradientPaint getGradientPaint(Color top, Color bottom) {
        Dimension dimension = this.getSize();
        return new GradientPaint(0, 0, top, 0, dimension.height, /*dimension.height, dimension.width,*/ bottom);
    }

    // THE Renderer. The most important part here. Invoked from the paintComponent method.
    private Consumer<Graphics2D> whiteBoardWriter = DEFAULT_DASHBOARD_WRITER;

    // Graphic title, margins, axis color, fonts, etc, as setters.
    public WhiteBoardPanel() {
    }

    public WhiteBoardPanel(Consumer<Graphics2D> whiteBoardWriter) {
        this();
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
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
     * @param ext the image extension (jpg, png, etc), used by ImageIO
     * @param width image width
     * @param height image height
     */
    public void createImage(File f, String ext, int width, int height) {

        // Create a buffered image in which to draw
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final WhiteBoardPanel instance = this;
        Thread refreshThread = new Thread(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    // Create a graphics contents on the buffered image
                    Graphics2D g2d = bufferedImage.createGraphics();
                    instance.paintComponent(g2d);
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

    public static void drawArrow(Graphics2D g2d, Point from, Point to, Color c) {
        drawArrow(g2d, from, to, c, 30);
    }

    public static void drawArrow(Graphics2D g2d, Point from, Point to, Color c, int headLength) {
        Color orig = null;
        if (g2d != null) {
            orig = g2d.getColor();
        }
        double headHalfAngle = 15D;

        double dir = getDir((from.x - to.x), (to.y - from.y));
//      System.out.println("Dir:" + dir);

        g2d.setColor(c);
        Point left = new Point((int) (to.x - (headLength * Math.cos(Math.toRadians(dir - 90 + headHalfAngle)))),
                (int) (to.y - (headLength * Math.sin(Math.toRadians(dir - 90 + headHalfAngle)))));
        Point right = new Point((int) (to.x - (headLength * Math.cos(Math.toRadians(dir - 90 - headHalfAngle)))),
                (int) (to.y - (headLength * Math.sin(Math.toRadians(dir - 90 - headHalfAngle)))));

        g2d.drawLine(from.x, from.y, to.x, to.y);
        Polygon head = new Polygon(new int[]{to.x, left.x, right.x}, new int[]{to.y, left.y, right.y}, 3);
        g2d.fillPolygon(head);

        if (g2d != null) {
            g2d.setColor(orig);
        }
    }

    /**
     * Warning: this one is adding 180 to the direction.
     *
     * @param x deltaX
     * @param y deltaY
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
