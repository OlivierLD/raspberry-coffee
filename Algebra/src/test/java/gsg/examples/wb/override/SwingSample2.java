package gsg.examples.wb.override;

import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Function;

public class SwingSample2 {

    private JFrame frame;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpAbout = new JMenuItem();

    private final static int WIDTH = 860;
    private final static int HEIGHT = 600;

    private static WhiteBoardPanel whiteBoard = new WhiteBoardPanel(g2d -> {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT); // Hard coded dimensions for that one.
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD).deriveFont(30f));
        g2d.setColor(Color.ORANGE);
        g2d.drawString("This is your white board!", 10, 40);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD).deriveFont(16f));
        String message = "Use the WhiteBoardPanel.setWhiteBoardWriter method.";
        g2d.drawString(message, 40, 80);
    });

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.println("Exit requested");
        System.exit(0);
    }
    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.println("Help requested");
        JOptionPane.showMessageDialog(whiteBoard, "Help would go here", "Help", JOptionPane.PLAIN_MESSAGE);
    }

    private void jbInit() {
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

        frame.getContentPane().add(whiteBoard, BorderLayout.CENTER);
    }

    public SwingSample2() {
        frame = new JFrame("This is example #2");
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
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frame.setVisible(true);

    }

    public static void main(String... args) {

        try {
            if (System.getProperty("swing.defaultlaf") == null) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

     /* SwingSample swingSample = */ new SwingSample2();

        // Get the range here
        VectorUtils.Vector2D one = new VectorUtils.Vector2D(-1, -2);
        VectorUtils.Vector2D two = new VectorUtils.Vector2D(3, 4);
        VectorUtils.Vector2D three = VectorUtils.toPolar(one);
        VectorUtils.Vector2D four = new VectorUtils.Vector2D(5, 6);
        VectorUtils.Vector2D five = new VectorUtils.Vector2D(7, 2);

        VectorUtils.GraphicRange graphicRange = VectorUtils.findGraphicRange(one, two, three, four, five);
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

        System.out.println(String.format("y0: %f (minY: %f)", y0, graphicRange.getMinY()));

        int CIRCLE_DIAM = 30;
        Dimension dimension = new Dimension(WIDTH, HEIGHT);

        whiteBoard.setWhiteBoardWriter(g2d -> {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, dimension.width, dimension.height);

            // Actual working zone, from graphicRange
            g2d.setColor(Color.GRAY);
            // graphicRange.getMaxX() graphicRange.getMinX() graphicRange.getMaxY() graphicRange.getMinY()
            int minX = findCanvasXCoord.apply(graphicRange.getMinX());
            int maxX = findCanvasXCoord.apply(graphicRange.getMaxX());
            int minY = findCanvasYCoord.apply(graphicRange.getMinY());
            int maxY = findCanvasYCoord.apply(graphicRange.getMaxY());
            g2d.drawRect(minX, minY, (maxX - minX), (maxY - minY));

            // Vertical (left) Arrow
            WhiteBoardPanel.drawArrow(g2d,
                    new Point((int)Math.round(x0), HEIGHT),
                    new Point((int)Math.round(x0), 0),
                    Color.BLACK);

            // X Notches, positive
            g2d.setColor(Color.BLACK);
            int xTick = 0;
            int canvasX = 0;
            while (canvasX < WIDTH) {
                canvasX = findCanvasXCoord.apply((double)xTick);
                g2d.drawLine(canvasX, HEIGHT - (int)Math.round(y0 - 5),
                        canvasX, HEIGHT - (int)Math.round(y0 + 5));
                xTick += 1;
            }
            // X Notches, negative
            xTick = 0;
            canvasX = WIDTH;
            while (canvasX > 0) {
                canvasX = findCanvasXCoord.apply((double)xTick);
                g2d.drawLine(canvasX, HEIGHT - (int)Math.round(y0 - 5),
                        canvasX, HEIGHT - (int)Math.round(y0 + 5));
                xTick -= 1;
            }

            // Horizontal (bottom) Arrow
            WhiteBoardPanel.drawArrow(g2d,
                    new Point(0, HEIGHT - (int)Math.round(y0)),
                    new Point(WIDTH, HEIGHT - (int)Math.round(y0)),
                    Color.BLACK);

            // Y Notches, positive
            g2d.setColor(Color.BLACK);
            int yTick = 0;
            int canvasY = 0;
            while (canvasY < HEIGHT) {
                canvasY = findCanvasYCoord.apply((double)yTick);
                g2d.drawLine((int)Math.round(x0 - 5), HEIGHT - canvasY,
                             (int)Math.round(x0 + 5), HEIGHT - canvasY);
                yTick += 1;
            }
            // Y Notches, negative
            yTick = 0;
            canvasY = HEIGHT;
            while (canvasY > 0) {
                canvasY = findCanvasYCoord.apply((double)yTick);
                g2d.drawLine((int)Math.round(x0 - 5), HEIGHT - canvasY,
                        (int)Math.round(x0 + 5), HEIGHT - canvasY);
                yTick -= 1;
            }

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD | Font.ITALIC).deriveFont(30f));

            // Points, Vectors
            g2d.setColor(new Color(0, 0, 255, 125)); // blue
            int pointX = findCanvasXCoord.apply(one.getX());
            int pointY = findCanvasYCoord.apply(one.getY());
            g2d.fillOval(pointX - (CIRCLE_DIAM / 2),
                    HEIGHT - pointY - (CIRCLE_DIAM / 2),
                    CIRCLE_DIAM, CIRCLE_DIAM);
            WhiteBoardPanel.drawArrow(g2d,
                    new Point((int)Math.round(x0), HEIGHT - (int)Math.round(y0)),
                    new Point(pointX, HEIGHT - pointY),
                    new Color(0, 0, 255, 125));

            g2d.setColor(new Color(255, 0, 0, 125)); // red
            pointX = findCanvasXCoord.apply(two.getX());
            pointY = findCanvasYCoord.apply(two.getY());
            g2d.fillOval(pointX - (CIRCLE_DIAM / 2),
                    HEIGHT - pointY - (CIRCLE_DIAM / 2),
                    CIRCLE_DIAM, CIRCLE_DIAM);
            WhiteBoardPanel.drawArrow(g2d,
                    new Point((int)Math.round(x0), HEIGHT - (int)Math.round(y0)),
                    new Point(pointX, HEIGHT - pointY),
                    new Color(255, 0, 0, 125));

            g2d.setColor(new Color(0, 255, 0, 125)); // green
            pointX = findCanvasXCoord.apply(three.getX());
            pointY = findCanvasYCoord.apply(three.getY());
            g2d.fillOval(pointX - (CIRCLE_DIAM / 2),
                    HEIGHT - pointY - (CIRCLE_DIAM / 2),
                    CIRCLE_DIAM, CIRCLE_DIAM);
            WhiteBoardPanel.drawArrow(g2d,
                    new Point((int)Math.round(x0), HEIGHT - (int)Math.round(y0)),
                    new Point(pointX, HEIGHT - pointY),
                    new Color(0, 255, 0, 125));

            g2d.setColor(new Color(0, 255, 255, 125)); // cyan
            pointX = findCanvasXCoord.apply(four.getX());
            pointY = findCanvasYCoord.apply(four.getY());
            g2d.fillOval(pointX - (CIRCLE_DIAM / 2),
                    HEIGHT - pointY - (CIRCLE_DIAM / 2),
                    CIRCLE_DIAM, CIRCLE_DIAM);
            WhiteBoardPanel.drawArrow(g2d,
                    new Point((int)Math.round(x0), HEIGHT - (int)Math.round(y0)),
                    new Point(pointX, HEIGHT - pointY),
                    new Color(0, 255, 255, 125));

            g2d.setColor(new Color(255, 255, 0, 125)); // yellow
            pointX = findCanvasXCoord.apply(five.getX());
            pointY = findCanvasYCoord.apply(five.getY());
            g2d.fillOval(pointX - (CIRCLE_DIAM / 2),
                    HEIGHT - pointY - (CIRCLE_DIAM / 2),
                    CIRCLE_DIAM, CIRCLE_DIAM);
            WhiteBoardPanel.drawArrow(g2d,
                    new Point((int)Math.round(x0), HEIGHT - (int)Math.round(y0)),
                    new Point(pointX, HEIGHT - pointY),
                    new Color(255, 255, 0, 125));
        });
//        whiteBoard.getImage(); // This is for the Notebook
        whiteBoard.repaint(); /// This is for a pure Swing context

//        double perimeter = VectorUtils.perimeter(Arrays.asList(one, two, three, four));

    }
}
