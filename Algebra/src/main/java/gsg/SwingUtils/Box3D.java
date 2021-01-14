package gsg.SwingUtils;

import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Designed to draw figures in a 3D coordinate system.
 * Suitable for Swing application, as Jupyter Notebooks (with iJava)
 */
public class Box3D extends JPanel {

    private double xMin = -3d;
    private double xMax = 3d;
    private double yMin = -3d;
    private double yMax = 3d;
    private double zMin = -3d;
    private double zMax = 3d;

    // In degrees
    private double rotOnZ = 40d;
    private double rotOnY = 0d;
    private double rotOnX = -10d;

    // Zoom
    private double zoom = 1.0;
    private boolean withBoxFaces = true;

    private Color backgroundColor = Color.WHITE;
    private Color perimeterColor = Color.GRAY;
    private Color gridColor = new Color(0, 125, 125, 80); // Color.GRAY;
    private Color boxFacesColor = new Color(230, 230, 230, 125);
    private Color axisColor = Color.LIGHT_GRAY;

    private final static int MAX_TICK_PER_AXIS = 16;

    private Stroke axisStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);

    public void setPerimeterColor(Color perimeterColor) {
        this.perimeterColor = perimeterColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public void setBoxFacesColor(Color boxFacesColor) {
        this.boxFacesColor = boxFacesColor;
    }

    public void setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
    }

    public void setAxisStroke(Stroke axisStroke) {
        this.axisStroke = axisStroke;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getPerimeterColor() {
        return perimeterColor;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public Color getBoxFacesColor() {
        return boxFacesColor;
    }

    public Color getAxisColor() {
        return axisColor;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public double getyMax() {
        return yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    public double getzMin() {
        return zMin;
    }

    public void setzMin(double zMin) {
        this.zMin = zMin;
    }

    public double getzMax() {
        return zMax;
    }

    public void setzMax(double zMax) {
        this.zMax = zMax;
    }

    public double getRotOnZ() {
        return rotOnZ;
    }

    public void setRotOnZ(double rotOnZ) {
        this.rotOnZ = rotOnZ;
    }

    public double getRotOnY() {
        return rotOnY;
    }

    public void setRotOnY(double rotOnY) {
        this.rotOnY = rotOnY;
    }

    public double getRotOnX() {
        return rotOnX;
    }

    public void setRotOnX(double rotOnX) {
        this.rotOnX = rotOnX;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public boolean isWithBoxFaces() {
        return withBoxFaces;
    }

    public void setWithBoxFaces(boolean withBoxFaces) {
        this.withBoxFaces = withBoxFaces;
    }

    // Default values
    private final static int WIDTH = 860;
    private final static int HEIGHT = 600;
    private Dimension dimension = new Dimension(WIDTH, HEIGHT);  // TODO Redundant with getSize() ?

    private double ratio = 1d;
    /**
     * A Function<Vector3D, Point>.
     * Externalized to be reached from the beforeDrawer and afterDrawer.
     * See https://math.stackexchange.com/questions/164700/how-to-transform-a-set-of-3d-vectors-into-a-2d-plane-from-a-view-point-of-anoth
     * <p>
     * This is the function that takes a space point into the point to display in the canvas.
     * It takes in account the position of the eye/camera, managed during the VectorUtils.rotate invocation,
     * which has to happen before.
     */
    private final Function<VectorUtils.Vector3D, Point> transformer = v3 -> {
        int xOnScreen = (this.dimension.width / 2) + (int) Math.round((v3.getX()) / (ratio / zoom));
        int yOnScreen = (this.dimension.height / 2) - (int) Math.round((v3.getZ()) / (ratio / zoom));
        return new Point(xOnScreen, yOnScreen);
    };

    public Function<VectorUtils.Vector3D, Point> getTransformer() {
        return transformer;
    }

    private Consumer<Graphics2D> beforeDrawer = null;
    private Consumer<Graphics2D> afterDrawer = null;

    private final Consumer<Graphics2D> DEFAULT_DRAWER = g2d -> {

        // Call before
        if (beforeDrawer != null) {
            beforeDrawer.accept(g2d);
        }

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, dimension.width, dimension.height);

        double ratioX = (xMax - xMin) / this.dimension.width;
        double ratioY = (yMax - yMin) / this.dimension.width;
        double ratioZ = (zMax - zMin) / this.dimension.height;

        ratio = Collections.max(Arrays.asList(ratioX, ratioY, ratioZ)) * 1.5;

        // Find the center, point (0, 0, 0)
        double centerX = this.xMin + ((this.xMax - this.xMin) / 2d);
        double centerY = this.yMin + ((this.yMax - this.yMin) / 2d);
        double centerZ = this.zMin + ((this.zMax - this.zMin) / 2d);
        VectorUtils.Vector3D center = new VectorUtils.Vector3D(centerX, centerY, centerZ);
//        System.out.printf("Center: %s, ratio: %f%n", center.toString(), ratio);

        /*
         * Print the "back sides" of the box.
         * And the grid
         */

        // Centered on X
        VectorUtils.Vector3D xBottomRightV3 = new VectorUtils.Vector3D(xMin, yMin, zMin);
        VectorUtils.Vector3D xTopRightV3 = new VectorUtils.Vector3D(xMin, yMin, zMax);
        VectorUtils.Vector3D xTopLeftV3 = new VectorUtils.Vector3D(xMin, yMax, zMax);
        VectorUtils.Vector3D xBottomLeftV3 = new VectorUtils.Vector3D(xMin, yMax, zMin);
        // Centered on Y
        VectorUtils.Vector3D yBottomLeftV3 = xBottomRightV3;
        VectorUtils.Vector3D yTopLeftV3 = xTopRightV3;
        VectorUtils.Vector3D yTopRightV3 = new VectorUtils.Vector3D(xMax, yMin, zMax);
        VectorUtils.Vector3D yBottomRightV3 = new VectorUtils.Vector3D(xMax, yMin, zMin);
        // Centered on Z
        VectorUtils.Vector3D zTopLeftV3 = yBottomLeftV3;
        VectorUtils.Vector3D zBottomLeftV3 = xBottomLeftV3;
        VectorUtils.Vector3D zTopRightV3 = yBottomRightV3;
        VectorUtils.Vector3D zBottomRightV3 = new VectorUtils.Vector3D(xMax, yMax, zMin);

        if (withBoxFaces) {
            // Panel centered on X: (minX, minY, minZ), (minX, minY, maxZ), (minX, maxY, maxZ), (minX, maxY, minZ)
            VectorUtils.Vector3D rotatedXBottomRight = VectorUtils.rotate(xBottomRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedXTopRight = VectorUtils.rotate(xTopRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedXTopLeft = VectorUtils.rotate(xTopLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedXBottomLeft = VectorUtils.rotate(xBottomLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));

            Point xBottomRight = transformer.apply(rotatedXBottomRight);
            Point xTopRight = transformer.apply(rotatedXTopRight);
            Point xTopLeft = transformer.apply(rotatedXTopLeft);
            Point xBottomLeft = transformer.apply(rotatedXBottomLeft);

            Polygon polygonX = new Polygon(new int[]{xBottomRight.x, xTopRight.x, xTopLeft.x, xBottomLeft.x},
                    new int[]{xBottomRight.y, xTopRight.y, xTopLeft.y, xBottomLeft.y},
                    4);
            g2d.setColor(boxFacesColor);
            g2d.fillPolygon(polygonX);
            // Perimeter
            g2d.setColor(perimeterColor);
            g2d.drawLine(xBottomRight.x, xBottomRight.y, xTopRight.x, xTopRight.y);
            g2d.drawLine(xTopRight.x, xTopRight.y, xTopLeft.x, xTopLeft.y);
            g2d.drawLine(xTopLeft.x, xTopLeft.y, xBottomLeft.x, xBottomLeft.y);
            g2d.drawLine(xBottomLeft.x, xBottomLeft.y, xBottomRight.x, xBottomRight.y);

            // Grid on the panel centered on X axis, y as abscissa, z as ordinate
            g2d.setColor(gridColor);
            Stroke originalStroke = g2d.getStroke();
            // Leave Stroke as it is (for now)
            g2d.setStroke(new BasicStroke(1));
            // TODO Ticks on axis (option)
            // Parallel to Z, vertical grid
            double yWidth = this.getyMax() - this.getyMin();
            int yUnit = (int)Math.round(Math.max(yWidth / MAX_TICK_PER_AXIS, 1));
            int startY = (int) Math.round(Math.ceil(this.getyMin()));
            while (startY % yUnit != 0) {
                startY += 1;
            }
//            System.out.println("yUnit " + yUnit + ", Y Starts at " + this.getyMin() +", start graph at " + startY);
            for (int y = startY; y <= this.getyMax(); y += yUnit) {
                // Define space points
                VectorUtils.Vector3D bottomSpacePoint = new VectorUtils.Vector3D(this.getxMin(), y, this.getzMin());
                VectorUtils.Vector3D topSpacePoint = new VectorUtils.Vector3D(this.getxMin(), y, this.getzMax());
                // Rotate them
                VectorUtils.Vector3D rotatedBottomSpacePoint = VectorUtils.rotate(bottomSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                VectorUtils.Vector3D rotatedTopSpacePoint = VectorUtils.rotate(topSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                // Draw line between them
                Point bottom = transformer.apply(rotatedBottomSpacePoint);
                Point top = transformer.apply(rotatedTopSpacePoint);
                g2d.drawLine(bottom.x, bottom.y, top.x, top.y);
            }
            // Parallel to Y, horizontal grid
            double zWidth = this.getzMax() - this.getzMin();
            int zUnit = (int)Math.round(Math.max(zWidth / MAX_TICK_PER_AXIS, 1));
            int startZ = (int) Math.round(Math.ceil(this.getzMin()));
            while (startZ % zUnit != 0) {
                startZ += 1;
            }
//            System.out.println("zUnit " + zUnit + ", Z Starts at " + this.getzMin() +", start graph at " + startZ);
            for (int z = startZ; z <= this.getzMax(); z += zUnit) {
                // Define space points
                VectorUtils.Vector3D leftSpacePoint = new VectorUtils.Vector3D(this.getxMin(), this.getyMax(), z);
                VectorUtils.Vector3D rightSpacePoint = new VectorUtils.Vector3D(this.getxMin(), this.getyMin(), z);
                // Rotate them
                VectorUtils.Vector3D rotatedLeftSpacePoint = VectorUtils.rotate(leftSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                VectorUtils.Vector3D rotatedRightSpacePoint = VectorUtils.rotate(rightSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                // Draw line between them
                Point left = transformer.apply(rotatedLeftSpacePoint);
                Point right = transformer.apply(rotatedRightSpacePoint);
                g2d.drawLine(left.x, left.y, right.x, right.y);
            }
            g2d.setStroke(originalStroke);

            // Panel centered on Y
            VectorUtils.Vector3D rotatedYBottomLeft = VectorUtils.rotate(yBottomLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedYTopLeft = VectorUtils.rotate(yTopLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedYTopRight = VectorUtils.rotate(yTopRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedYBottomRight = VectorUtils.rotate(yBottomRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));

            Point yBottomRight = transformer.apply(rotatedYBottomRight);
            Point yTopRight = transformer.apply(rotatedYTopRight);
            Point yTopLeft = transformer.apply(rotatedYTopLeft);
            Point yBottomLeft = transformer.apply(rotatedYBottomLeft);

            Polygon polygonY = new Polygon(new int[]{yBottomRight.x, yTopRight.x, yTopLeft.x, yBottomLeft.x},
                    new int[]{yBottomRight.y, yTopRight.y, yTopLeft.y, yBottomLeft.y},
                    4);
            g2d.setColor(boxFacesColor);
            g2d.fillPolygon(polygonY);
            // Perimeter
            g2d.setColor(perimeterColor);
            g2d.drawLine(yBottomRight.x, yBottomRight.y, yTopRight.x, yTopRight.y);
            g2d.drawLine(yTopRight.x, yTopRight.y, yTopLeft.x, yTopLeft.y);
            g2d.drawLine(yTopLeft.x, yTopLeft.y, yBottomLeft.x, yBottomLeft.y);
            g2d.drawLine(yBottomLeft.x, yBottomLeft.y, yBottomRight.x, yBottomRight.y);
            // Grid on the panel centered on Y axis, x as abscissa, z as ordinate
            g2d.setColor(gridColor);
            originalStroke = g2d.getStroke();
            // Leave Stroke as it is (for now)
            g2d.setStroke(new BasicStroke(1));
            // Parallel to Z, vertical grid
            double xWidth = this.getxMax() - this.getxMin();
            int xUnit = (int)Math.round(Math.max(xWidth / MAX_TICK_PER_AXIS, 1));
            int startX = (int) Math.round(Math.ceil(this.getxMin()));
            while (startX % xUnit != 0) {
                startX += 1;
            }
//            System.out.println("xUnit " + xUnit + ", X Starts at " + this.getxMin() +", start graph at " + startX);
            for (int x = startX; x <= this.getxMax(); x += xUnit) {
                // Define space points
                VectorUtils.Vector3D bottomSpacePoint = new VectorUtils.Vector3D(x, this.getyMin(), this.getzMin());
                VectorUtils.Vector3D topSpacePoint = new VectorUtils.Vector3D(x, this.getyMin(), this.getzMax());
                // Rotate them
                VectorUtils.Vector3D rotatedBottomSpacePoint = VectorUtils.rotate(bottomSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                VectorUtils.Vector3D rotatedTopSpacePoint = VectorUtils.rotate(topSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                // Draw line between them
                Point bottom = transformer.apply(rotatedBottomSpacePoint);
                Point top = transformer.apply(rotatedTopSpacePoint);
                g2d.drawLine(bottom.x, bottom.y, top.x, top.y);
            }
            // Parallel to Y, horizontal grid
//            startZ = (int) Math.round(Math.ceil(this.getzMin()));
            for (int z = startZ; z <= this.getzMax(); z += zUnit) {
                // Define space points
                VectorUtils.Vector3D leftSpacePoint = new VectorUtils.Vector3D(this.getxMin(), this.getyMin(), z);
                VectorUtils.Vector3D rightSpacePoint = new VectorUtils.Vector3D(this.getxMax(), this.getyMin(), z);
                // Rotate them
                VectorUtils.Vector3D rotatedLeftSpacePoint = VectorUtils.rotate(leftSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                VectorUtils.Vector3D rotatedRightSpacePoint = VectorUtils.rotate(rightSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                // Draw line between them
                Point left = transformer.apply(rotatedLeftSpacePoint);
                Point right = transformer.apply(rotatedRightSpacePoint);
                g2d.drawLine(left.x, left.y, right.x, right.y);
                // Label on the right
                String label = String.valueOf(z);
//            int strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(label);
                g2d.drawString(label, right.x + 2, right.y + 5); // 5: half font size
            }
            g2d.setStroke(originalStroke);

            // Panel centered on Z
            VectorUtils.Vector3D rotatedZBottomLeft = VectorUtils.rotate(zBottomLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedZTopLeft = VectorUtils.rotate(zTopLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedZTopRight = VectorUtils.rotate(zTopRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
            VectorUtils.Vector3D rotatedZBottomRight = VectorUtils.rotate(zBottomRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));

            Point zBottomRight = transformer.apply(rotatedZBottomRight);
            Point zTopRight = transformer.apply(rotatedZTopRight);
            Point zTopLeft = transformer.apply(rotatedZTopLeft);
            Point zBottomLeft = transformer.apply(rotatedZBottomLeft);

            Polygon polygonZ = new Polygon(new int[]{zBottomRight.x, zTopRight.x, zTopLeft.x, zBottomLeft.x},
                    new int[]{zBottomRight.y, zTopRight.y, zTopLeft.y, zBottomLeft.y},
                    4);
            g2d.setColor(boxFacesColor);
            g2d.fillPolygon(polygonZ);
            // Perimeter
            g2d.setColor(perimeterColor);
            g2d.drawLine(zBottomRight.x, zBottomRight.y, zTopRight.x, zTopRight.y);
            g2d.drawLine(zTopRight.x, zTopRight.y, zTopLeft.x, zTopLeft.y);
            g2d.drawLine(zTopLeft.x, zTopLeft.y, zBottomLeft.x, zBottomLeft.y);
            g2d.drawLine(zBottomLeft.x, zBottomLeft.y, zBottomRight.x, zBottomRight.y);
            // Grid on the panel centered on Z axis, x as abscissa, y as ordinate
            g2d.setColor(gridColor);
            originalStroke = g2d.getStroke();
            // Leave Stroke as it is (for now)
            g2d.setStroke(new BasicStroke(1));
            // Parallel to X, left to right
//            startY = (int) Math.round(Math.ceil(this.getyMin()));
            for (int y = startY; y <= this.getyMax(); y += yUnit) {
                // Define space points
                VectorUtils.Vector3D leftSpacePoint = new VectorUtils.Vector3D(this.getxMin(), y, this.getzMin());
                VectorUtils.Vector3D rightSpacePoint = new VectorUtils.Vector3D(this.getxMax(), y, this.getzMin());
                // Rotate them
                VectorUtils.Vector3D rotatedLeftSpacePoint = VectorUtils.rotate(leftSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                VectorUtils.Vector3D rotatedRightSpacePoint = VectorUtils.rotate(rightSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                // Draw line between them
                Point left = transformer.apply(rotatedLeftSpacePoint);
                Point right = transformer.apply(rotatedRightSpacePoint);
                g2d.drawLine(left.x, left.y, right.x, right.y);
                // Label on the right
                String label = String.valueOf(y);
                int strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(label);
                g2d.drawString(label, right.x - (strWidth / 2), right.y + 10);
            }
            // Parallel to Y, back to forth
//            startX = (int) Math.round(Math.ceil(this.getxMin()));
            for (int x = startX; x <= this.getxMax(); x += xUnit) {
                // Define space points
                VectorUtils.Vector3D backSpacePoint = new VectorUtils.Vector3D(x, this.getyMin(), this.getzMin());
                VectorUtils.Vector3D frontSpacePoint = new VectorUtils.Vector3D(x, this.getyMax(), this.getzMin());
                // Rotate them
                VectorUtils.Vector3D rotatedBackSpacePoint = VectorUtils.rotate(backSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                VectorUtils.Vector3D rotatedFrontSpacePoint = VectorUtils.rotate(frontSpacePoint, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
                // Draw line between them
                Point back = transformer.apply(rotatedBackSpacePoint);
                Point front = transformer.apply(rotatedFrontSpacePoint);
                g2d.drawLine(back.x, back.y, front.x, front.y);
                // Label in Front
                String label = String.valueOf(x);
                int strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(label);
                g2d.drawString(label, front.x - (strWidth / 2), front.y + 10);
            }
            g2d.setStroke(originalStroke);
        }

        // Plot center in the middle, (0, 0, 0)
        g2d.setColor(Color.BLACK);
        int circleDiam = 6;
//        VectorUtils.Vector3D centerV3 = new VectorUtils.Vector3D(0, 0, 0);
//        VectorUtils.Vector3D rotatedCenter = VectorUtils.rotate(centerV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedCenter = VectorUtils.rotate(center, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        Point screenCenter = transformer.apply(rotatedCenter); // Could be simpler, but OK!
        g2d.fillOval(screenCenter.x - (circleDiam / 2),
                screenCenter.y - (circleDiam / 2),
                circleDiam, circleDiam);

        // Stroke for arrows
        g2d.setStroke(axisStroke);

        // Y axis
        // TODO Switch yMin and yMax to match the book ?
        VectorUtils.Vector3D minYVector = new VectorUtils.Vector3D(0, yMin, 0);
        VectorUtils.Vector3D rotatedYMinVector = VectorUtils.rotate(minYVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D maxYVector = new VectorUtils.Vector3D(0, yMax, 0);
        VectorUtils.Vector3D rotatedYMaxVector = VectorUtils.rotate(maxYVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
//        System.out.printf("Rotated (%s) onX: %f, onY: %f, onZ: %f => (%s)%n", minYVector, rotOnX, rotOnY, rotOnZ, rotatedYMinVector);
        drawArrow(g2d,
                transformer.apply(rotatedYMinVector),
                transformer.apply(rotatedYMaxVector),
                axisColor);
        g2d.setColor(this.axisColor);
        String label = "Y";
        int strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(label);
        int fontSize = g2d.getFont().getSize();
        // Push it a bit further
        Point labelLocation = transformer.apply(VectorUtils.rotate(VectorUtils.translate(
                new VectorUtils.Vector3D(0, 0.2, 0), maxYVector),
                Math.toRadians(rotOnX),
                Math.toRadians(rotOnY),
                Math.toRadians(rotOnZ)));
        g2d.drawString(label, labelLocation.x - (strWidth / 2), labelLocation.y + ((fontSize - 2) / 2));

        // X axis
        VectorUtils.Vector3D minXVector = new VectorUtils.Vector3D(xMin, 0, 0);
        VectorUtils.Vector3D rotatedXMinVector = VectorUtils.rotate(minXVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D maxXVector = new VectorUtils.Vector3D(xMax, 0, 0);
        VectorUtils.Vector3D rotatedXMaxVector = VectorUtils.rotate(maxXVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        drawArrow(g2d,
                transformer.apply(rotatedXMinVector),
                transformer.apply(rotatedXMaxVector),
                axisColor);
        g2d.setColor(this.axisColor);
        label = "X";
        strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(label);
        fontSize = g2d.getFont().getSize();
        labelLocation = transformer.apply(VectorUtils.rotate(VectorUtils.translate(
                new VectorUtils.Vector3D(0.2, 0, 0), maxXVector),
                Math.toRadians(rotOnX),
                Math.toRadians(rotOnY),
                Math.toRadians(rotOnZ)));
        g2d.drawString(label, labelLocation.x - (strWidth / 2), labelLocation.y + ((fontSize - 2) / 2));

        // Z axis
        VectorUtils.Vector3D minZVector = new VectorUtils.Vector3D(0, 0, zMin);
        VectorUtils.Vector3D rotatedZMinVector = VectorUtils.rotate(minZVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D maxZVector = new VectorUtils.Vector3D(0, 0, zMax);
        VectorUtils.Vector3D rotatedZMaxVector = VectorUtils.rotate(maxZVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        drawArrow(g2d,
                transformer.apply(rotatedZMinVector),
                transformer.apply(rotatedZMaxVector),
                axisColor);
        g2d.setColor(this.axisColor);
        label = "Z";
        strWidth = g2d.getFontMetrics(g2d.getFont()).stringWidth(label);
        fontSize = g2d.getFont().getSize();
        labelLocation = transformer.apply(VectorUtils.rotate(VectorUtils.translate(
                new VectorUtils.Vector3D(0, 0, 0.2), maxZVector),
                Math.toRadians(rotOnX),
                Math.toRadians(rotOnY),
                Math.toRadians(rotOnZ)));
        g2d.drawString(label, labelLocation.x - (strWidth / 2), labelLocation.y + ((fontSize - 2) / 2));

        // TODO Maybe remove this...
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Courier New", Font.BOLD, 16));
        g2d.drawString(String.format("Rotations on Z:%.02f\272, on X:%.02f\272, on Y:%.02f\272",
                this.rotOnZ, this.rotOnX, this.rotOnY), 10, 18);

        // Call after
        if (afterDrawer != null) {
            afterDrawer.accept(g2d);
        }

    };

    // This can be overridden. Voids the warranty!
    private Consumer<Graphics2D> drawer = DEFAULT_DRAWER;

    public void setDrawer(Consumer<Graphics2D> drawer) {
        this.drawer = drawer;
    }

    public void setBeforeDrawer(Consumer<Graphics2D> beforeDrawer) {
        this.beforeDrawer = beforeDrawer;
    }

    public void setAfterDrawer(Consumer<Graphics2D> afterDrawer) {
        this.afterDrawer = afterDrawer;
    }

    public Box3D() {
        super();
        this.setSize(this.dimension);
    }

    public Box3D(int width, int height) {
        super();
        this.setSize(width, height);
    }

    @Override
    public void setSize(Dimension dimension) {
        super.setSize(dimension);
        this.dimension = dimension;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
//      System.out.println("paintComponent invoked on JPanel");
        Graphics2D g2d = (Graphics2D) g;
        drawer.accept(g2d);     // Invoke the drawer
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
     * Uses ABSOLUTE Spatial coordinates. Rotation and
     * transformation space-to-screen are taken care of.
     *
     * @param g2d  the graphic env
     * @param from The Spatial ABSOLUTE coordinate of the from vector
     * @param to   The Spatial ABSOLUTE coordinate of the to vector
     */
    public void drawArrow(Graphics2D g2d, VectorUtils.Vector3D from, VectorUtils.Vector3D to) {
        VectorUtils.Vector3D rotatedFrom = VectorUtils.rotate(from,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedTo = VectorUtils.rotate(to,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        Point fromPt = transformer.apply(rotatedFrom);
        Point toPt = transformer.apply(rotatedTo);
        drawArrow(g2d, fromPt, toPt, g2d.getColor());
    }

    /**
     * Use triplets (double[] of 3 elements) to describe the ABSOLUTE
     * coordinates of the from and to space points.
     *
     * @param g2d  Graphical env
     * @param from 3 doubles (x, y, z)
     * @param to   3 doubles (x, y, z)
     */
    public void drawArrow(Graphics2D g2d, double[] from, double[] to) {
        drawArrow(g2d,
                new VectorUtils.Vector3D(from[0], from[1], from[2]),
                new VectorUtils.Vector3D(to[0], to[1], to[2]));
    }

    public void drawSegment(Graphics2D g2d, VectorUtils.Vector3D from, VectorUtils.Vector3D to) {
        VectorUtils.Vector3D rotatedFrom = VectorUtils.rotate(from,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedTo = VectorUtils.rotate(to,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        Point fromPt = transformer.apply(rotatedFrom);
        Point toPt = transformer.apply(rotatedTo);
        g2d.drawLine(fromPt.x, fromPt.y, toPt.x, toPt.y);
    }

    public void drawSegment(Graphics2D g2d, double[] from, double[] to) {
        drawSegment(g2d,
                new VectorUtils.Vector3D(from[0], from[1], from[2]),
                new VectorUtils.Vector3D(to[0], to[1], to[2]));
    }

    public void drawSurroundingBox(Graphics2D g2d, VectorUtils.Vector3D from, VectorUtils.Vector3D to) {
        double[] topFrontLeft = new double[3];
        double[] topFrontRight = new double[3];
        double[] topBackLeft = new double[3];
        double[] topBackRight = new double[3];
        double[] bottomFrontLeft = new double[3];
        double[] bottomFrontRight = new double[3];
        double[] bottomBackLeft = new double[3];
        double[] bottomBackRight = new double[3];

        // Top face
        topFrontLeft[0] = Math.min(from.getX(), to.getX());
        topFrontLeft[1] = Math.min(from.getY(), to.getY());
        topFrontLeft[2] = Math.max(from.getZ(), to.getZ());

        topFrontRight[0] = Math.max(from.getX(), to.getX());
        topFrontRight[1] = Math.min(from.getY(), to.getY());
        topFrontRight[2] = Math.max(from.getZ(), to.getZ());

        topBackLeft[0] = Math.min(from.getX(), to.getX());
        topBackLeft[1] = Math.max(from.getY(), to.getY());
        topBackLeft[2] = Math.max(from.getZ(), to.getZ());

        topBackRight[0] = Math.max(from.getX(), to.getX());
        topBackRight[1] = Math.max(from.getY(), to.getY());
        topBackRight[2] = Math.max(from.getZ(), to.getZ());

        // Bottom face
        bottomFrontLeft[0] = Math.min(from.getX(), to.getX());
        bottomFrontLeft[1] = Math.min(from.getY(), to.getY());
        bottomFrontLeft[2] = Math.min(from.getZ(), to.getZ());

        bottomFrontRight[0] = Math.max(from.getX(), to.getX());
        bottomFrontRight[1] = Math.min(from.getY(), to.getY());
        bottomFrontRight[2] = Math.min(from.getZ(), to.getZ());

        bottomBackLeft[0] = Math.min(from.getX(), to.getX());
        bottomBackLeft[1] = Math.max(from.getY(), to.getY());
        bottomBackLeft[2] = Math.min(from.getZ(), to.getZ());

        bottomBackRight[0] = Math.max(from.getX(), to.getX());
        bottomBackRight[1] = Math.max(from.getY(), to.getY());
        bottomBackRight[2] = Math.min(from.getZ(), to.getZ());

        this.drawBox.accept(g2d, new double[][]{
                topFrontLeft,
                topBackLeft,
                topBackRight,
                topFrontRight,
                bottomFrontLeft,
                bottomBackLeft,
                bottomBackRight,
                bottomFrontRight
        });
    }

    public void drawSurroundingBox(Graphics2D g2d, double[] from, double[] to) {
        drawSurroundingBox(g2d,
                new VectorUtils.Vector3D(from[0], from[1], from[2]),
                new VectorUtils.Vector3D(to[0], to[1], to[2]));
    }

    public void plotStringAt(Graphics2D g2d, String text, VectorUtils.Vector3D where, boolean centered) {
        VectorUtils.Vector3D rotated = VectorUtils.rotate(where,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        Point textPoint = this.getTransformer().apply(rotated);
        if (centered) {
            Font font = g2d.getFont();
            int strWidth = g2d.getFontMetrics(font).stringWidth(text);
            int strHeight = font.getSize(); // TODO Get this right
            textPoint.setLocation(textPoint.x - (strWidth / 2),
                    textPoint.y + (strHeight / 2));
        }
        g2d.drawString(text, textPoint.x, textPoint.y);
    }

    /**
     * Set Stroke and Color before calling.
     * <p>
     * Warning: Points (vectors) order is important:
     * <pre>
     *    1 +----------+ 2
     *     /|         /|
     *    / |        / |
     * 0 +--+-------+ 3|
     *   |  |       |  |
     *   |5 +-------|--+ 6
     *   | /        | /
     *   |/         |/
     * 4 +----------+ 7
     * </pre>
     */
    public final BiConsumer<Graphics2D, double[][]> drawBox = (g2d, tuples) -> {
        // Requires 8 vertices
        assert (tuples.length == 8);
        // 3 coordinates per vertex
        assert (tuples[0].length == 3);
        assert (tuples[1].length == 3);
        assert (tuples[2].length == 3);
        assert (tuples[3].length == 3);
        assert (tuples[4].length == 3);
        assert (tuples[5].length == 3);
        assert (tuples[6].length == 3);
        assert (tuples[7].length == 3);

        // Define vertices as Vector3D
        VectorUtils.Vector3D topFrontLeft3D = new VectorUtils.Vector3D(tuples[0][0], tuples[0][1], tuples[0][2]);
        VectorUtils.Vector3D topBackLeft3D = new VectorUtils.Vector3D(tuples[1][0], tuples[1][1], tuples[1][2]);
        VectorUtils.Vector3D topBackRight3D = new VectorUtils.Vector3D(tuples[2][0], tuples[2][1], tuples[2][2]);
        VectorUtils.Vector3D topFrontRight3D = new VectorUtils.Vector3D(tuples[3][0], tuples[3][1], tuples[3][2]);
        VectorUtils.Vector3D bottomFrontLeft3D = new VectorUtils.Vector3D(tuples[4][0], tuples[4][1], tuples[4][2]);
        VectorUtils.Vector3D bottomBackLeft3D = new VectorUtils.Vector3D(tuples[5][0], tuples[5][1], tuples[5][2]);
        VectorUtils.Vector3D bottomBackRight3D = new VectorUtils.Vector3D(tuples[6][0], tuples[6][1], tuples[6][2]);
        VectorUtils.Vector3D bottomFrontRight3D = new VectorUtils.Vector3D(tuples[7][0], tuples[7][1], tuples[7][2]);

        // Rotate vertices
        VectorUtils.Vector3D rotatedTopFrontLeft = VectorUtils.rotate(topFrontLeft3D,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedTopBackLeft = VectorUtils.rotate(topBackLeft3D,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedTopBackRight = VectorUtils.rotate(topBackRight3D,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedTopFrontRight = VectorUtils.rotate(topFrontRight3D,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedBottomFrontLeft = VectorUtils.rotate(bottomFrontLeft3D,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedBottomBackLeft = VectorUtils.rotate(bottomBackLeft3D,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedBottomBackRight = VectorUtils.rotate(bottomBackRight3D,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        VectorUtils.Vector3D rotatedBottomFrontRight = VectorUtils.rotate(bottomFrontRight3D,
                Math.toRadians(this.getRotOnX()),
                Math.toRadians(this.getRotOnY()),
                Math.toRadians(this.getRotOnZ()));
        // Plot
        Point topFrontLeft = this.getTransformer().apply(rotatedTopFrontLeft);
        Point topBackLeft = this.getTransformer().apply(rotatedTopBackLeft);
        Point topBackRight = this.getTransformer().apply(rotatedTopBackRight);
        Point topFrontRight = this.getTransformer().apply(rotatedTopFrontRight);

        Point bottomFrontLeft = this.getTransformer().apply(rotatedBottomFrontLeft);
        Point bottomBackLeft = this.getTransformer().apply(rotatedBottomBackLeft);
        Point bottomBackRight = this.getTransformer().apply(rotatedBottomBackRight);
        Point bottomFrontRight = this.getTransformer().apply(rotatedBottomFrontRight);

        // Upper face
        g2d.drawLine(topFrontLeft.x, topFrontLeft.y, topBackLeft.x, topBackLeft.y);
        g2d.drawLine(topBackLeft.x, topBackLeft.y, topBackRight.x, topBackRight.y);
        g2d.drawLine(topBackRight.x, topBackRight.y, topFrontRight.x, topFrontRight.y);
        g2d.drawLine(topFrontRight.x, topFrontRight.y, topFrontLeft.x, topFrontLeft.y);
        // Bottom face
        g2d.drawLine(bottomFrontLeft.x, bottomFrontLeft.y, bottomBackLeft.x, bottomBackLeft.y);
        g2d.drawLine(bottomBackLeft.x, bottomBackLeft.y, bottomBackRight.x, bottomBackRight.y);
        g2d.drawLine(bottomBackRight.x, bottomBackRight.y, bottomFrontRight.x, bottomFrontRight.y);
        g2d.drawLine(bottomFrontRight.x, bottomFrontRight.y, bottomFrontLeft.x, bottomFrontLeft.y);
        // Vertical edges
        g2d.drawLine(topFrontLeft.x, topFrontLeft.y, bottomFrontLeft.x, bottomFrontLeft.y);
        g2d.drawLine(topBackLeft.x, topBackLeft.y, bottomBackLeft.x, bottomBackLeft.y);
        g2d.drawLine(topBackRight.x, topBackRight.y, bottomBackRight.x, bottomBackRight.y);
        g2d.drawLine(topFrontRight.x, topFrontRight.y, bottomFrontRight.x, bottomFrontRight.y);
    };

    /**
     * Call this from a Jupyter Notebook (iJava) !
     *
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
