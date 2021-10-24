package gsg.SwingUtils;

import gsg.VectorUtils;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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

    private List<DataSerie> dataSeries = new ArrayList<>();
    private List<TextSerie> textSeries = new ArrayList<>();

    public enum TitleJustification {
        LEFT,
        RIGHT,
        CENTER
    }

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

    public static class TextSerie {
        public enum Justification {
            LEFT,
            RIGHT,
            CENTER
        }

        private VectorUtils.Vector2D at;
        private Font font = null;
        private String str = "";
        private int xOffset = 0;
        private int yOffset = 0;
        private Justification justification = Justification.LEFT;
        private Color textColor = null;

        public TextSerie(VectorUtils.Vector2D at, String text) {
            this.at = at;
            this.str = text;
        }
        public TextSerie(VectorUtils.Vector2D at, String text, int xOffset, int yOffset, Justification justification) {
            this.at = at;
            this.str = text;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.justification = justification;
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public Color getTextColor() {
            return textColor;
        }

        public void setTextColor(Color textColor) {
            this.textColor = textColor;
        }

        public Vector2D getAt() {
            return at;
        }

        public String getStr() {
            return str;
        }

        public int getxOffset() {
            return xOffset;
        }

        public int getyOffset() {
            return yOffset;
        }

        public Justification getJustification() {
            return justification;
        }
    }

    private final static int MAX_TICK_PER_AXIS = 30;

    // Default values
    private final static int DEFAULT_WIDTH = 860;
    private final static int DEFAULT_HEIGHT = 600;
    private int graphicMargins = 20;
    private Color axisColor = Color.BLACK;
    private Color gridColor = Color.BLACK;
    private Color textColor = Color.GRAY;
    private Color bgColor = Color.LIGHT_GRAY;
    private boolean frameGraphic = true;
    private String graphicTitle = "Graphic Title"; // Set to null to remove
    private TitleJustification titleJustification = TitleJustification.LEFT;
    private Font titleFont = null;
    private boolean withGrid = false;
    private boolean xEqualsY = true;

    private Double forcedMinY = null;
    private Double forcedMaxY = null;

    private Double enforceXAxisAt = null;
    private Double enforceYAxisAt = null;

    private Integer forceTickIncrement = null;
    private Integer forceTickIncrementX = null;
    private Integer forceTickIncrementY = null;

    public void setForceTickIncrement(Integer inc) {
        this.forceTickIncrement = inc;
    }
    public void setForceTickIncrementX(Integer inc) {
        this.forceTickIncrementX = inc;
    }
    public void setForceTickIncrementY(Integer inc) {
        this.forceTickIncrementY = inc;
    }

    public void setGraphicMargins(int graphicMargins) {
        this.graphicMargins = graphicMargins;
    }

    public void setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
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

    public void setEnforceXAxisAt(Double enforceXAxisAt) {
        this.enforceXAxisAt = enforceXAxisAt;
    }
    public void setEnforceYAxisAt(Double enforceYAxisAt) {
        this.enforceYAxisAt = enforceYAxisAt;
    }

    public void setTitleJustification(TitleJustification justification) {
        this.titleJustification = justification;
    }

    public void setFrameGraphic(boolean b) {
        this.frameGraphic = b;
    }

    private Function<Integer, String> xLabelGenerator = x -> String.valueOf(x); // Default

    public void setXLabelGenerator(Function<Integer, String> xLabelGenerator) {
        this.xLabelGenerator = xLabelGenerator;
    }

    private Function<Double, Integer> getCanvasXCoord;
    private Function<Double, Integer> getCanvasYCoord;

    private Function<Integer, Double> canvasX2SpaceX;
    private Function<Integer, Double> canvasY2SpaceY;

    public Function<Double, Integer> getSpaceToCanvasXTransformer() {
        return this.getCanvasXCoord;
    }
    public Function<Double, Integer> getSpaceToCanvasYTransformer() {
        return this.getCanvasYCoord;
    }
    public Function<Integer, Double> getCanvasToSpaceXTransformer() {
        return this.canvasX2SpaceX;
    }
    public Function<Integer, Double> getCanvasToSpaceYTransformer() {
        return this.canvasY2SpaceY;
    }

    private final Consumer<Graphics2D> DEFAULT_DASHBOARD_WRITER = g2d -> {

        if (dataSeries.size() == 0) {
            throw new RuntimeException("No data");
        }
        List<List<Vector2D>> allData = new ArrayList<>();
        dataSeries.forEach(serie -> { synchronized(serie) { allData.add(serie.getData()); } });

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

//        Function<Double, Integer> findCanvasXCoord = spaceXCoord -> {
//            int value = (int)(margins + (Math.round((spaceXCoord - graphicRange.getMinX()) * (xEqualsY ? oneUnit : oneUnitX))));
//            System.out.println(String.format("Margin: %d, SpaceX: %f, minX: %f, unit: %f => %d",
//                    margins,
//                    spaceXCoord,
//                    graphicRange.getMinX(),
//                    (xEqualsY ? oneUnit : oneUnitX),
//                    value));
//            return value;
//        };
//        Function<Double, Integer> findCanvasYCoord = spaceYCoord -> {
//            int value = (int)(margins + (Math.round((spaceYCoord - minDblY) * (xEqualsY ? oneUnit : oneUnitY))));
//            System.out.println(String.format("Margin: %d, SpaceY: %f, minYX: %f, unit: %f => %d",
//                    margins,
//                    spaceYCoord,
//                    minDblY,
//                    (xEqualsY ? oneUnit : oneUnitY),
//                    value));
//            return value;
//        };

        // canvasToSpace
        Function<Integer, Double> canvasToSpaceX = canvasX -> ((canvasX - margins) / (xEqualsY ? oneUnit : oneUnitX)) + graphicRange.getMinX();
        Function<Integer, Double> canvasToSpaceY = canvasY -> ((this.getSize().height - margins - canvasY) / (xEqualsY ? oneUnit : oneUnitY)) + minDblY;
//        Function<Integer, Double> canvasToSpaceY = canvasY -> {
//            System.out.println(String.format("Margin:%d, MinY:%.02f", margins, minDblY));
//            return ((this.getSize().height - margins - canvasY) / (xEqualsY ? oneUnit : oneUnitY)) + minDblY;
//        }; // + graphicRange.getMinY();

        // For external access
        getCanvasXCoord = findCanvasXCoord;
        getCanvasYCoord = findCanvasYCoord;
        canvasX2SpaceX = canvasToSpaceX;
        canvasY2SpaceY = canvasToSpaceY;

        // Axis coordinates
        double x0 = Math.floor(findCanvasXCoord.apply(this.enforceXAxisAt == null ? graphicRange.getMinX() : this.enforceXAxisAt /*0d*/)); // Math.round(0 - graphicRange.getMinX()) * oneUnit;
        double y0 = Math.floor(findCanvasYCoord.apply(this.enforceYAxisAt == null ? minDblY : this.enforceYAxisAt /*0d*/)); // Math.round(0 - graphicRange.getMinY()) * oneUnit;

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
        if (this.frameGraphic) {
            g2d.drawRect(minX, height - maxY, (maxX - minX), (maxY - minY));
        }

        // Label font
        int labelFontSize = 10;
        Font labelFont = new Font("Courier New", Font.PLAIN, labelFontSize);
        g2d.setFont(labelFont);

        // Vertical X (left) Arrow. Vertical: orientation of the notches.
        g2d.setStroke(new BasicStroke(2));             // Line Thickness
        WhiteBoardPanel.drawArrow(g2d,
                new Point((int)Math.round(x0), height),
                new Point((int)Math.round(x0), 0),
                axisColor);
        g2d.setStroke(new BasicStroke(1));             // Line Thickness

        // X Notches or grid, left to right
        g2d.setColor(gridColor);
        int xTick = (int)Math.floor(graphicRange.getMinX()); // 0;
        // Re-locate if (forceTickIncrement != null or forceTickIncrementX != null) and enforceXAxisAt != null
        if ((this.forceTickIncrement != null || this.forceTickIncrementX != null) && this.enforceXAxisAt != null) {
            int _inc = 0;
            if (this.forceTickIncrementX != null) {
                _inc = this.forceTickIncrementX;
            } else {
                _inc = this.forceTickIncrement;
            }
            int _x = (int)Math.floor(this.enforceXAxisAt);
            xTick = _x;
            int _canvasX = findCanvasXCoord.apply((double)_x);
            while (_canvasX >= 0) {
                _x -= _inc;
                _canvasX = findCanvasXCoord.apply((double)_x);
                if (_canvasX >= 0) {
                    xTick = _x;
                }
            }
        }
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
                String label = xLabelGenerator.apply(xTick);
                int strWidth = g2d.getFontMetrics(labelFont).stringWidth(label);
                g2d.drawString(label, canvasX - (strWidth / 2),height - (int) Math.round(y0 - 5 - (labelFont.getSize())));
            }
            xTick += (xEqualsY ?
                    (forceTickIncrement != null ? forceTickIncrement : tickIncrement) :
                    (forceTickIncrementX != null ? forceTickIncrementX : tickIncrementX));
        }

        // Horizontal Y (bottom) Arrow. Horizontal: orientation of the notches.
        g2d.setStroke(new BasicStroke(2));             // Line Thickness
        WhiteBoardPanel.drawArrow(g2d,
                new Point(0, height - (int)Math.round(y0)),
                new Point(width, height - (int)Math.round(y0)),
                axisColor);

        g2d.setStroke(new BasicStroke(1));             // Line Thickness
        // Y Notches, top to bottom
        g2d.setColor(gridColor);
        int yTick = (int)Math.floor(minDblY); // 0;
        // Re-locate if (forceTickIncrement != null or forceTickIncrementY != null) and enforceYAxisAt != null
        if ((this.forceTickIncrement != null || this.forceTickIncrementY != null) && this.enforceYAxisAt != null) {
            int _inc = 0;
            if (this.forceTickIncrementY != null) {
                _inc = this.forceTickIncrementY;
            } else {
                _inc = this.forceTickIncrement;
            }
            int _y = (int)Math.floor(this.enforceYAxisAt);
            yTick = _y;
            int _canvasY = findCanvasYCoord.apply((double)_y);
            while (_canvasY >= 0) {
                _y -= _inc;
                _canvasY = findCanvasYCoord.apply((double)_y);
                if (_canvasY >= 0) {
                    yTick = _y;
                }
            }
        }
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
            yTick += (xEqualsY ?
                    (forceTickIncrement != null ? forceTickIncrement : tickIncrement) :
                    (forceTickIncrementY != null ? forceTickIncrementY : tickIncrementY));
        }

        // For the text
        if (this.graphicTitle != null) {
            g2d.setColor(textColor);
            g2d.setFont(Objects.requireNonNullElseGet(titleFont,
                    () -> g2d.getFont().deriveFont(Font.BOLD | Font.ITALIC).deriveFont(24f)));
            int x = 10, y = 60;
            if (this.titleJustification.equals(TitleJustification.LEFT)) {
                x = 10;
            } else if (this.titleJustification.equals(TitleJustification.RIGHT) || this.titleJustification.equals(TitleJustification.CENTER)) {
                FontMetrics fm = g2d.getFontMetrics();
                int stringWidth = fm.stringWidth(this.graphicTitle);
                if (this.titleJustification.equals(TitleJustification.RIGHT)) {
                    x = width - 10 - stringWidth;
                } else {
                    x = (width / 2) - (stringWidth / 2);
                }
            }
            g2d.drawString(this.graphicTitle, x, y);
        }

        // Now the data, Series
        dataSeries.forEach(serie -> {
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

        // Any text?
        textSeries.forEach(txt -> {
            int pointX = findCanvasXCoord.apply(txt.getAt().getX());
            int pointY = findCanvasYCoord.apply(txt.getAt().getY());
            Font font = txt.getFont();
            Font previousFont = null;
            Color color = txt.getTextColor();
            Color previousColor = null;
            if (font != null) {
                previousFont = g2d.getFont();
                g2d.setFont(font);
            }
            if (color != null) {
                previousColor = g2d.getColor();
                g2d.setColor(color);
            }
            int justOffset = 0;
            if (txt.getJustification() != TextSerie.Justification.LEFT) {
                int sl = g2d.getFontMetrics().stringWidth(txt.getStr());
                justOffset = txt.getJustification() == TextSerie.Justification.RIGHT ? sl : (sl / 2);
            }
            g2d.drawString(txt.getStr(), pointX + txt.getxOffset() - justOffset, height - (pointY + txt.getyOffset()));
            if (previousFont != null) {
                g2d.setFont(previousFont);
            }
            if (previousColor != null) {
                g2d.setColor(previousColor);
            }
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
        this.dataSeries.add(serie);
    }

    public void removeSerie(DataSerie serie) {
        if (this.dataSeries.contains(serie)) {
            this.dataSeries.remove(serie);
        }
    }

    public void resetAllData() {
        this.dataSeries.clear();
    }

    public void addTextSerie(TextSerie serie) {
        this.textSeries.add(serie);
    }

    public void removeTextSerie(TextSerie serie) {
        if (this.textSeries.contains(serie)) {
            this.textSeries.remove(serie);
        }
    }

    public void resetAllText() {
        this.textSeries.clear();
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
