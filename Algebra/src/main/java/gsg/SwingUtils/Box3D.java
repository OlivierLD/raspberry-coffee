package gsg.SwingUtils;

import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

public class Box3D extends JPanel {

    // TODO Getters and setters
    private double xMin = -3d;
    private double xMax =  3d;
    private double yMin = -3d;
    private double yMax =  3d;
    private double zMin = -3d;
    private double zMax =  3d;

    // In degrees
    private double rotOnZ =  40d;
    private double rotOnY =   0d; // LEAVE IT TO 0 !!! (for now)
    private double rotOnX = -10d;

    // Default values
    private final static int WIDTH = 860;
    private final static int HEIGHT = 600;
    private Dimension dimension = new Dimension(WIDTH, HEIGHT);  // TODO Redundant with getSize() ?

    private Consumer<Graphics2D> DEFAULT_DRAWER = g2d -> {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, dimension.width, dimension.height);

        double ratioX = (xMax - xMin) / this.dimension.width;
        double ratioY = (yMax - yMin) / this.dimension.width;
        double ratioZ = (zMax - zMin) / this.dimension.height;

        double ratio = Collections.max(Arrays.asList(ratioX, ratioY, ratioZ)) * 1.5;

        // A Function<Vector3D, Point>. Not finalized yet...
        // See https://math.stackexchange.com/questions/164700/how-to-transform-a-set-of-3d-vectors-into-a-2d-plane-from-a-view-point-of-anoth
        Function<VectorUtils.Vector3D, Point> transformer = v3 -> {
            int xOnScreen = (this.dimension.width / 2) + (int)Math.round((v3.getX()) / ratio);
            int yOnScreen = (this.dimension.height / 2) - (int)Math.round((v3.getZ()) / ratio);
            return new Point(xOnScreen, yOnScreen);
        };

        // Find the center
        double centerX = this.xMin + ((this.xMax - this.xMin) / 2d);
        double centerY = this.yMin + ((this.yMax - this.yMin) / 2d);
        double centerZ = this.zMin + ((this.zMax - this.zMin) / 2d);
        VectorUtils.Vector3D center = new VectorUtils.Vector3D(centerX, centerY, centerZ);
        System.out.printf("Center: %s, ratio: %f%n", center.toString(), ratio);

        /*
         * Print back(s) of the box.
         * TODO the grid
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

        // Panel centered on X: (minX, minY, minZ), (minX, minY, maxZ), (minX, maxY, maxZ), (minX, maxY, minZ)
        VectorUtils.Vector3D rotatedXBottomRight = VectorUtils.rotate(xBottomRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedXTopRight = VectorUtils.rotate(xTopRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedXTopLeft = VectorUtils.rotate(xTopLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedXBottomLeft = VectorUtils.rotate(xBottomLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));

        Point xBottomRight = transformer.apply(rotatedXBottomRight);
        Point xTopRight = transformer.apply(rotatedXTopRight);
        Point xTopLeft = transformer.apply(rotatedXTopLeft);
        Point xBottomLeft = transformer.apply(rotatedXBottomLeft);

        Polygon polygonX = new Polygon(new int[] {xBottomRight.x, xTopRight.x, xTopLeft.x, xBottomLeft.x},
                new int[] {xBottomRight.y, xTopRight.y, xTopLeft.y, xBottomLeft.y},
                4);
        g2d.setColor(new Color(230, 230, 230, 125));
        g2d.fillPolygon(polygonX);
        // Perimeter
        g2d.setColor(Color.GRAY);
        g2d.drawLine(xBottomRight.x, xBottomRight.y, xTopRight.x, xTopRight.y);
        g2d.drawLine(xTopRight.x, xTopRight.y, xTopLeft.x, xTopLeft.y);
        g2d.drawLine(xTopLeft.x, xTopLeft.y, xBottomLeft.x, xBottomLeft.y);
        g2d.drawLine(xBottomLeft.x, xBottomLeft.y, xBottomRight.x, xBottomRight.y);

        // Panel centered on Y
        VectorUtils.Vector3D rotatedYBottomLeft = VectorUtils.rotate(yBottomLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedYTopLeft = VectorUtils.rotate(yTopLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedYTopRight = VectorUtils.rotate(yTopRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedYBottomRight = VectorUtils.rotate(yBottomRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));

        Point yBottomRight = transformer.apply(rotatedYBottomRight);
        Point yTopRight = transformer.apply(rotatedYTopRight);
        Point yTopLeft = transformer.apply(rotatedYTopLeft);
        Point yBottomLeft = transformer.apply(rotatedYBottomLeft);

        Polygon polygonY = new Polygon(new int[] {yBottomRight.x, yTopRight.x, yTopLeft.x, yBottomLeft.x},
                new int[] {yBottomRight.y, yTopRight.y, yTopLeft.y, yBottomLeft.y},
                4);
        g2d.setColor(new Color(230, 230, 230, 125));
        g2d.fillPolygon(polygonY);
        // Perimeter
        g2d.setColor(Color.GRAY);
        g2d.drawLine(yBottomRight.x, yBottomRight.y, yTopRight.x, yTopRight.y);
        g2d.drawLine(yTopRight.x, yTopRight.y, yTopLeft.x, yTopLeft.y);
        g2d.drawLine(yTopLeft.x, yTopLeft.y, yBottomLeft.x, yBottomLeft.y);
        g2d.drawLine(yBottomLeft.x, yBottomLeft.y, yBottomRight.x, yBottomRight.y);

        // Panel centered on Z
        VectorUtils.Vector3D rotatedZBottomLeft = VectorUtils.rotate(zBottomLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedZTopLeft = VectorUtils.rotate(zTopLeftV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedZTopRight = VectorUtils.rotate(zTopRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D rotatedZBottomRight = VectorUtils.rotate(zBottomRightV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));

        Point zBottomRight = transformer.apply(rotatedZBottomRight);
        Point zTopRight = transformer.apply(rotatedZTopRight);
        Point zTopLeft = transformer.apply(rotatedZTopLeft);
        Point zBottomLeft = transformer.apply(rotatedZBottomLeft);

        Polygon polygonZ = new Polygon(new int[] {zBottomRight.x, zTopRight.x, zTopLeft.x, zBottomLeft.x},
                new int[] {zBottomRight.y, zTopRight.y, zTopLeft.y, zBottomLeft.y},
                4);
        g2d.setColor(new Color(230, 230, 230, 125));
        g2d.fillPolygon(polygonZ);
        // Perimeter
        g2d.setColor(Color.GRAY);
        g2d.drawLine(zBottomRight.x, zBottomRight.y, zTopRight.x, zTopRight.y);
        g2d.drawLine(zTopRight.x, zTopRight.y, zTopLeft.x, zTopLeft.y);
        g2d.drawLine(zTopLeft.x, zTopLeft.y, zBottomLeft.x, zBottomLeft.y);
        g2d.drawLine(zBottomLeft.x, zBottomLeft.y, zBottomRight.x, zBottomRight.y);

        // Plot center in the middle, (0, 0, 0)
        g2d.setColor(Color.BLACK);
        int circleDiam = 6;
        VectorUtils.Vector3D centerV3 = new VectorUtils.Vector3D(0, 0, 0);
        VectorUtils.Vector3D rotatedCenter = VectorUtils.rotate(centerV3, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        Point screenCenter = transformer.apply(rotatedCenter); // Could be simpler, but OK!
        g2d.fillOval(screenCenter.x - (circleDiam / 2),
                screenCenter.y - (circleDiam / 2),
                circleDiam, circleDiam);

        // Stroke for arrows
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2d.setStroke(dashed);

        // Y axis
        VectorUtils.Vector3D minYVector = new VectorUtils.Vector3D(0, yMin, 0);
        VectorUtils.Vector3D rotatedYMinVector =  VectorUtils.rotate(minYVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D maxYVector = new VectorUtils.Vector3D(0, yMax, 0);
        VectorUtils.Vector3D rotatedYMaxVector =  VectorUtils.rotate(maxYVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
//        System.out.printf("Rotated (%s) onX: %f, onY: %f, onZ: %f => (%s)%n", minYVector, rotOnX, rotOnY, rotOnZ, rotatedYMinVector);
        drawArrow(g2d,
                transformer.apply(rotatedYMinVector),
                transformer.apply(rotatedYMaxVector),
                Color.LIGHT_GRAY);
        g2d.setColor(Color.CYAN);
        g2d.drawString("Y", transformer.apply(rotatedYMaxVector).x, transformer.apply(rotatedYMaxVector).y);

        // X axis
        VectorUtils.Vector3D minXVector = new VectorUtils.Vector3D(xMin, 0, 0);
        VectorUtils.Vector3D rotatedXMinVector =  VectorUtils.rotate(minXVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D maxXVector = new VectorUtils.Vector3D(xMax, 0, 0);
        VectorUtils.Vector3D rotatedXMaxVector =  VectorUtils.rotate(maxXVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        drawArrow(g2d,
                transformer.apply(rotatedXMinVector),
                transformer.apply(rotatedXMaxVector),
                Color.LIGHT_GRAY);
        g2d.setColor(Color.CYAN);
        g2d.drawString("X", transformer.apply(rotatedXMaxVector).x, transformer.apply(rotatedXMaxVector).y);

        // Z axis
        VectorUtils.Vector3D minZVector = new VectorUtils.Vector3D(0, 0, zMin);
        VectorUtils.Vector3D rotatedZMinVector =  VectorUtils.rotate(minZVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        VectorUtils.Vector3D maxZVector = new VectorUtils.Vector3D(0, 0, zMax);
        VectorUtils.Vector3D rotatedZMaxVector =  VectorUtils.rotate(maxZVector, Math.toRadians(rotOnX), Math.toRadians(rotOnY), Math.toRadians(rotOnZ));
        drawArrow(g2d,
                transformer.apply(rotatedZMinVector),
                transformer.apply(rotatedZMaxVector),
                Color.LIGHT_GRAY);
        g2d.setColor(Color.CYAN);
        g2d.drawString("Z", transformer.apply(rotatedZMaxVector).x, transformer.apply(rotatedZMaxVector).y);

        g2d.setColor(Color.BLACK);
        g2d.drawString("More soon, ça rigole!", 10, 30);
    };

    private Consumer<Graphics2D> drawer = DEFAULT_DRAWER;

    public Box3D() {
        super();
        this.setSize(this.dimension);
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

    // TODO Change that to atan2
    public static double getDir(float x, float y) {
        double dir = 0.0D;
        if (y != 0) {
            dir = Math.toDegrees(Math.atan((double) x / (double) y));
        }
        if (x <= 0 || y <= 0) {
            if (x > 0 && y < 0) {
                dir += 180D;
            } else if (x < 0 && y > 0) {
                dir += 360D;
            } else if (x < 0 && y < 0) {
                dir += 180D;
            } else if (x == 0) {
                if (y > 0) {
                    dir = 0.0D;
                } else {
                    dir = 180D;
                }
            } else if (y == 0) {
                if (x > 0) {
                    dir = 90D;
                } else {
                    dir = 270D;
                }
            }
        }
        dir += 180D;
        while (dir >= 360D) {
            dir -= 360D;
        }
        return dir;
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

        double dir = getDir((float) (from.x - to.x), (float) (to.y - from.y));
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
