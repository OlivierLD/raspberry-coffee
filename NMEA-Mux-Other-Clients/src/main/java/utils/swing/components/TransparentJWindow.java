package utils.swing.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public abstract class TransparentJWindow extends JWindow {
    private Robot robot = null; // new Robot();
    private BufferedImage screenImg;
    private Rectangle screenRect;
    private TransparentPanel contentPanel = new TransparentPanel();
    boolean userActivate = false;

    private TransparentJWindow instance = this;

    private Point dragOrigin = null;
    private int xPosOffset = 0, yPosOffset = 0;
    private Point currentPosition = new Point(100, 100);
    private Dimension winDim = new Dimension(200, 200);
    private Point draggedPos = null;

    protected void transparentWindowPaintComponent(Graphics g) {
    }

    protected abstract void onClick();

    public TransparentJWindow() {
        super();
        createScreenImage(); // As it was when launched...
        this.setContentPane(contentPanel);

        this.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
                resetUnderImg();
                repaint();
            }

            public void componentResized(ComponentEvent e) {
                resetUnderImg();
                repaint();
            }

            public void componentShown(ComponentEvent e) {
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            }

            public void windowClosed(WindowEvent e) {
            }

            public void windowOpened(WindowEvent e) {
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
                if (userActivate) {
                    userActivate = false;
                    setVisible(false);
                    createScreenImage();
                    resetUnderImg();
                    setVisible(true);
                } else {
                    userActivate = true;
                }
            }

            public void windowDeactivated(WindowEvent e) {
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                onClick();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
//        super.mousePressed(e);
                dragOrigin = e.getPoint();
//        System.out.println("DragPoint: x=" + dragOrigin.x + ", y=" + dragOrigin.y);
                xPosOffset = dragOrigin.x;
                yPosOffset = dragOrigin.y;
                draggedPos = e.getPoint();
//        System.out.println("XOffset:" + xPosOffset);
//        System.out.println("YOffset:" + yPosOffset);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
//        super.mouseReleased(e);
                int x = currentPosition.x + e.getPoint().x - xPosOffset;
                int y = currentPosition.y + e.getPoint().y - yPosOffset;
                currentPosition = new Point(x, y);
                draggedPos = null;
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
//        super.mouseDragged(e);
                if (draggedPos != null && !draggedPos.equals(e.getPoint())) {
                    int x = currentPosition.x + e.getPoint().x - xPosOffset;
                    int y = currentPosition.y + e.getPoint().y - yPosOffset;
//            System.out.println("Moving to: x=" + x + ", y=" + y);
                    currentPosition.x = x;
                    currentPosition.y = y;
                    instance.setBounds(x, y, winDim.width, winDim.height);
                    draggedPos = e.getPoint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
            }
        });
    }

    protected void createScreenImage() {
        try {
            if (robot == null) {
                robot = new Robot();
            }
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenRect = new Rectangle(0, 0, screenSize.width, screenSize.height);
        screenImg = robot.createScreenCapture(screenRect);
    }

    public void resetUnderImg() {
        if (robot != null && screenImg != null) {
            if (false) { // Quite demanding...
                // Re-snapshot the background
                setVisible(false);
                createScreenImage();
                setVisible(true);
            }
            Rectangle frameRect = getBounds();
            int x = frameRect.x; // + 4;
            contentPanel.paintX = 0;
            contentPanel.paintY = 0;
            if (x < 0) {
                contentPanel.paintX = -x;
                x = 0;
            }
            int y = frameRect.y; // + 23;
            if (y < 0) {
                contentPanel.paintY = -y;
                y = 0;
            }
            int w = frameRect.width; // - 10;
            if (x + w > screenImg.getWidth()) {
                w = screenImg.getWidth() - x;
            }
            int h = frameRect.height; // - 23 - 5;
            if (y + h > screenImg.getHeight()) {
                h = screenImg.getHeight() - y;
            }
            contentPanel.underFrameImg = screenImg.getSubimage(x, y, w, h);
        }
    }

    public void setCurrentPosition(Point currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Point getCurrentPosition() {
        return currentPosition;
    }

    public void setWinDim(Dimension winDim) {
        this.winDim = winDim;
    }

    public Dimension getWinDim() {
        return winDim;
    }

    class TransparentPanel extends JPanel {
        private BufferedImage underFrameImg;
        private int paintX = 0;
        private int paintY = 0;

        public TransparentPanel() {
            super();
            setOpaque(true);
        }

        public void paint(Graphics g) {
            super.paint(g);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(underFrameImg, paintX, paintY, null);
            transparentWindowPaintComponent(g);
        }
    }
}


