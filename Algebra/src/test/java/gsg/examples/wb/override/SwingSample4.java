package gsg.examples.wb.override;

import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static gsg.VectorUtils.Vector2D;

/**
 * Points, perimeter. Dino !
 */
public class SwingSample4 {

    private JFrame frame;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpAbout = new JMenuItem();

    // Default size
    private final static int WIDTH = 860;
    private final static int HEIGHT = 600;

    // The WhiteBoard
    private static WhiteBoardPanel whiteBoard = new WhiteBoardPanel();

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.println("Exit requested");
        System.exit(0);
    }
    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.println("Help requested");
        JOptionPane.showMessageDialog(whiteBoard, "Help would go here", "Help", JOptionPane.PLAIN_MESSAGE);
    }

    public SwingSample4() {
        // The JFrame
        frame = new JFrame("This is example #4");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
//        System.out.printf("Default frame width %d height %d %n", frameSize.width, frameSize.height);
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        if (frameSize.width == 0 || frameSize.height == 0) {
            frameSize = new Dimension(WIDTH, HEIGHT + 50); // 50: ... menu, title bar, etc.
            frame.setSize(frameSize);
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setJMenuBar(menuBar);
        frame.getContentPane().setLayout(new BorderLayout());
        menuFile.setText("File");
        menuFileExit.setText("Exit");
        menuFileExit.addActionListener(ae -> fileExit_ActionPerformed(ae));
        menuHelp.setText("Help");
        menuHelpAbout.setText("About");
        menuHelpAbout.addActionListener(ae -> helpAbout_ActionPerformed(ae));
        menuFile.add(menuFileExit);
        menuBar.add(menuFile);
        menuHelp.add(menuHelpAbout);
        menuBar.add(menuHelp);

        // Add the WitheBoard to the JFrame
        frame.getContentPane().add(whiteBoard, BorderLayout.CENTER);

        frame.setVisible(true); // Display
    }

    public static void main(String... args) {

        try {
            if (System.getProperty("swing.defaultlaf") == null) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        new SwingSample4();

        // Now, the Data
        List<Vector2D> dinoVectors = Arrays.asList(
                new Vector2D(6, 4),
                new Vector2D(3, 1),
                new Vector2D(1, 2),
                new Vector2D(-1, 5),
                new Vector2D(-2, 5),
                new Vector2D(-3, 4),
                new Vector2D(-4, 4),
                new Vector2D(-5, 3),
                new Vector2D(-5, 2),
                new Vector2D(-2, 2),
                new Vector2D(-5, 1),
                new Vector2D(-4, 0),
                new Vector2D(-2, 1),
                new Vector2D(-1, 0),
                new Vector2D(0, -3),
                new Vector2D(-1, -4),
                new Vector2D(1, -4),
                new Vector2D(2, -3),
                new Vector2D(1, -2),
                new Vector2D(3, -1),
                new Vector2D(5, 1));

        VectorUtils.GraphicRange graphicRange = VectorUtils.findGraphicRange(dinoVectors);
        double xAmplitude = graphicRange.getMaxX() - graphicRange.getMinX();
        double yAmplitude = graphicRange.getMaxY() - graphicRange.getMinY();

        int MARGINS = 20;

        double oneUnit = Math.min((WIDTH - (2 * MARGINS)) / xAmplitude, (HEIGHT - (2 * MARGINS)) / yAmplitude);
        System.out.println(String.format("One Unit: %f", oneUnit));

        // Transformers
        Function<Double, Integer> findCanvasXCoord = spaceXCoord -> (int)(MARGINS + (Math.round((spaceXCoord - graphicRange.getMinX()) * oneUnit)));
        Function<Double, Integer> findCanvasYCoord = spaceYCoord -> (int)(MARGINS + (Math.round((spaceYCoord - graphicRange.getMinY()) * oneUnit)));

        double x0 = findCanvasXCoord.apply(0d); // Math.round(0 - graphicRange.getMinX()) * oneUnit;
        double y0 = findCanvasYCoord.apply(0d); // Math.round(0 - graphicRange.getMinY()) * oneUnit;

//        System.out.println(String.format("y0: %f (minY: %f)", y0, graphicRange.getMinY()));

        int CIRCLE_DIAM = 2;
        Dimension dimension = new Dimension(WIDTH, HEIGHT);

        whiteBoard.setWhiteBoardWriter(g2d -> {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, dimension.width, dimension.height);

            // Actual working zone, from graphicRange
            g2d.setColor(Color.PINK); // GRAY);
            // graphicRange.getMaxX() graphicRange.getMinX() graphicRange.getMaxY() graphicRange.getMinY()
            int minX = findCanvasXCoord.apply(graphicRange.getMinX());
            int maxX = findCanvasXCoord.apply(graphicRange.getMaxX());
            int minY = findCanvasYCoord.apply(graphicRange.getMinY());
            int maxY = findCanvasYCoord.apply(graphicRange.getMaxY());
            System.out.println(String.format("Working Rectangle: x:%d, y:%d, w:%d, h:%d", minX, HEIGHT - maxY, (maxX - minX), (maxY - minY)));
            g2d.drawRect(minX, HEIGHT - maxY, (maxX - minX), (maxY - minY));

            // Label font
            int labelFontSize = 10;
            Font labelFont = new Font("Courier New", Font.PLAIN, labelFontSize);
            g2d.setFont(labelFont);

            // Vertical X (left) Arrow
            WhiteBoardPanel.drawArrow(g2d,
                    new Point((int)Math.round(x0), HEIGHT),
                    new Point((int)Math.round(x0), 0),
                    Color.BLACK);

            // X Notches, positive
            g2d.setColor(Color.BLACK);
            int xTick = 0;
            int canvasX = 0;
            while (canvasX <= WIDTH) {
                canvasX = findCanvasXCoord.apply((double)xTick);
                if (canvasX <= WIDTH) {
                    g2d.drawLine(canvasX, HEIGHT - (int) Math.round(y0 - 5),
                            canvasX, HEIGHT - (int) Math.round(y0 + 5));
                    String label = String.valueOf(xTick);
                    int strWidth = g2d.getFontMetrics(labelFont).stringWidth(label);
                    g2d.drawString(label, canvasX - (strWidth / 2),HEIGHT - (int) Math.round(y0 - 5 - (labelFont.getSize())));
                }
                xTick += 1;
            }
            // X Notches, negative
            xTick = 0;
            canvasX = WIDTH;
            while (canvasX >= 0) {
                canvasX = findCanvasXCoord.apply((double)xTick);
                if (canvasX >= 0) {
                    g2d.drawLine(canvasX, HEIGHT - (int) Math.round(y0 - 5),
                            canvasX, HEIGHT - (int) Math.round(y0 + 5));
                    String label = String.valueOf(xTick);
                    int strWidth = g2d.getFontMetrics(labelFont).stringWidth(label);
                    g2d.drawString(label, canvasX - (strWidth / 2),HEIGHT - (int) Math.round(y0 - 5 - (labelFont.getSize())));
                }
                xTick -= 1;
            }

            // Horizontal Y (bottom) Arrow
            WhiteBoardPanel.drawArrow(g2d,
                    new Point(0, HEIGHT - (int)Math.round(y0)),
                    new Point(WIDTH, HEIGHT - (int)Math.round(y0)),
                    Color.BLACK);

            // Y Notches, positive
            g2d.setColor(Color.BLACK);
            int yTick = 0;
            int canvasY = 0;
            while (canvasY <= HEIGHT) {
                canvasY = findCanvasYCoord.apply((double)yTick);
                if (canvasY <= HEIGHT) {
                    g2d.drawLine((int) Math.round(x0 - 5), HEIGHT - canvasY,
                            (int) Math.round(x0 + 5), HEIGHT - canvasY);
                    String label = String.valueOf(yTick);
                    int strWidth = g2d.getFontMetrics(labelFont).stringWidth(label);
                    g2d.drawString(label, (int) Math.round(x0 - 5) - strWidth - 2, HEIGHT - canvasY + (int)(labelFont.getSize() * 0.9 / 2));
                }
                yTick += 1;
            }
            // Y Notches, negative
            yTick = 0;
            canvasY = HEIGHT;
            while (canvasY >= 0) {
                canvasY = findCanvasYCoord.apply((double)yTick);
                if (canvasY >= 0) {
                    g2d.drawLine((int) Math.round(x0 - 5), HEIGHT - canvasY,
                            (int) Math.round(x0 + 5), HEIGHT - canvasY);
                    String label = String.valueOf(yTick);
                    int strWidth = g2d.getFontMetrics(labelFont).stringWidth(label);
                    g2d.drawString(label, (int) Math.round(x0 - 5) - strWidth - 2, HEIGHT - canvasY + (int)(labelFont.getSize() * 0.9 / 2));
                }
                yTick -= 1;
            }

            // For the text
            g2d.setColor(Color.GRAY);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD | Font.ITALIC).deriveFont(24f));
            g2d.drawString("Dinos!", 10, 60);

            /*
             *  THE DATA
             */
            g2d.setColor(new Color(0, 0, 255, 255)); // Line Color
            g2d.setStroke(new BasicStroke(3));             // Line Thickness
            boolean withPoints = true;
            Point previous = null;
            for (Vector2D v : dinoVectors) {
                int pointX = findCanvasXCoord.apply(v.getX());
                int pointY = findCanvasYCoord.apply(v.getY());
//              System.out.println(String.format("x:%f, y:%f => X:%d, Y:%d", x[i], y[i], pointX, pointY));
                Point here = new Point(pointX, pointY);
                if (withPoints) {
                    g2d.fillOval(pointX - (CIRCLE_DIAM / 2),
                            HEIGHT - pointY - (CIRCLE_DIAM / 2),
                            CIRCLE_DIAM, CIRCLE_DIAM);
                }
                if (previous != null) {
                    g2d.drawLine(previous.x, HEIGHT - previous.y, here.x, HEIGHT - here.y);
                }
                previous = here;
            }
            if (previous != null) { // Close the loop
                g2d.drawLine(
                        previous.x,
                        HEIGHT - previous.y,
                        findCanvasXCoord.apply(dinoVectors.get(0).getX()),
                        HEIGHT - findCanvasYCoord.apply(dinoVectors.get(0).getY()));
            }
            // Rotated ?
            g2d.setColor(new Color(255, 0, 255, 255)); // Line Color
            double rotation = -30d;
            previous = null;
            for (Vector2D v : dinoVectors) {
                int pointX = findCanvasXCoord.apply(VectorUtils.rotate(Math.toRadians(rotation) ,v).getX());
                int pointY = findCanvasYCoord.apply(VectorUtils.rotate(Math.toRadians(rotation) ,v).getY());
//              System.out.println(String.format("x:%f, y:%f => X:%d, Y:%d", x[i], y[i], pointX, pointY));
                Point here = new Point(pointX, pointY);
                if (withPoints) {
                    g2d.fillOval(pointX - (CIRCLE_DIAM / 2),
                            HEIGHT - pointY - (CIRCLE_DIAM / 2),
                            CIRCLE_DIAM, CIRCLE_DIAM);
                }
                if (previous != null) {
                    g2d.drawLine(previous.x, HEIGHT - previous.y, here.x, HEIGHT - here.y);
                }
                previous = here;
            }
            if (previous != null) { // Close the loop
                g2d.drawLine(
                        previous.x,
                        HEIGHT - previous.y,
                        findCanvasXCoord.apply(VectorUtils.rotate(Math.toRadians(rotation), dinoVectors.get(0)).getX()),
                        HEIGHT - findCanvasYCoord.apply(VectorUtils.rotate(Math.toRadians(rotation), dinoVectors.get(0)).getY()));
            }
            // Scaled ?
            g2d.setColor(new Color(0, 255, 0, 255)); // Line Color
            double scale = 0.25;
            previous = null;
            for (Vector2D v : dinoVectors) {
                int pointX = findCanvasXCoord.apply(VectorUtils.scale(scale ,v).getX());
                int pointY = findCanvasYCoord.apply(VectorUtils.scale(scale ,v).getY());
//              System.out.println(String.format("x:%f, y:%f => X:%d, Y:%d", x[i], y[i], pointX, pointY));
                Point here = new Point(pointX, pointY);
                if (withPoints) {
                    g2d.fillOval(pointX - (CIRCLE_DIAM / 2),
                            HEIGHT - pointY - (CIRCLE_DIAM / 2),
                            CIRCLE_DIAM, CIRCLE_DIAM);
                }
                if (previous != null) {
                    g2d.drawLine(previous.x, HEIGHT - previous.y, here.x, HEIGHT - here.y);
                }
                previous = here;
            }
            if (previous != null) { // Close the loop
                g2d.drawLine(
                        previous.x,
                        HEIGHT - previous.y,
                        findCanvasXCoord.apply(VectorUtils.scale(scale, dinoVectors.get(0)).getX()),
                        HEIGHT - findCanvasYCoord.apply(VectorUtils.scale(scale, dinoVectors.get(0)).getY()));
            }
        });
//      whiteBoard.getImage(); // This is for the Notebook
        whiteBoard.repaint();  // This is for a pure Swing context
    }
}
