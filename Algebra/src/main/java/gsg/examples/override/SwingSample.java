package gsg.examples.override;

import gsg.SwingUtils.WhiteBoardPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class SwingSample {

    private JFrame frame;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileCustomAction = new JMenuItem();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpAbout = new JMenuItem();

    private WhiteBoardPanel whiteBoard = new WhiteBoardPanel(g2d -> {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 800, 600); // Hard coded dimensions for that one.
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD).deriveFont(30f));
        g2d.setColor(Color.ORANGE);
        g2d.drawString("This is your white board!", 10, 40);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD).deriveFont(16f));
        String message = "Use the WhiteBoardPanel.setWhiteBoardWriter method.";
        g2d.drawString(message, 40, 80);
    });

    private void customAction_ActionPerformed(ActionEvent ae) {
        System.out.println("Custom Action requested - Change repaint, take a snapshot");
        File snap = new File("snap.jpg");
        Dimension dimension = whiteBoard.getSize();
        whiteBoard.setWhiteBoardWriter(g2d -> {
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, dimension.width, dimension.height);
            g2d.setFont(new Font("Courier New", Font.BOLD | Font.ITALIC, 48));
//            g2d.setFont(new Font("source code pro", Font.BOLD | Font.ITALIC, 48));
            g2d.setColor(Color.PINK);
            g2d.drawString("This is your white board!", 10, 60);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD).deriveFont(16f));
            String message = "Use the WhiteBoardPanel.setWhiteBoardWriter method.";
            g2d.drawString(message, 40, 100);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(50, 110, dimension.width - 100, dimension.height - 200);
            // Arrow
            WhiteBoardPanel.drawArrow(g2d,
                    new Point(400, 300),
                    new Point(300, 200),
                    Color.WHITE);
        });
        whiteBoard.repaint();
        whiteBoard.createImage(snap, "jpg", dimension.width, dimension.height);
    }
    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.println("Exit requested");
    }
    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.println("Help requested");
        JOptionPane.showMessageDialog(whiteBoard, "Help would go here", "Help", JOptionPane.PLAIN_MESSAGE);
    }

    private void jbInit() {
        frame.setJMenuBar(menuBar);
        frame.getContentPane().setLayout(new BorderLayout());
        menuFile.setText("File");
        menuFileCustomAction.setText("Custom Action");
        menuFileCustomAction.addActionListener(ae -> customAction_ActionPerformed(ae));
        menuFile.add(menuFileCustomAction);
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

    public SwingSample() {
        frame = new JFrame("This is an example");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        System.out.printf("Default frame width %d height %d %n", frameSize.width, frameSize.height);
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        if (frameSize.width == 0 || frameSize.height == 0) {
            frameSize = new Dimension(800, 600);
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

     /* SwingSample swingSample = */ new SwingSample();
    }
}
