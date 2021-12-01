import utils.SwingStaticUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Test the file chooser and the drawPanelTable
 */
public class TestFileChooserAndMore {

    // Screen dimensions
    private final static int WIDTH = 600;
    private final static int HEIGHT = 400;

    private static JFrame frame;
    private final static JButton button = new JButton("Click here to choose a file");

    private final static String TITLE = "This is a test";

    JPanel panel = new JPanel() {
        public void paintComponent(Graphics gr) {
            gr.setColor(Color.BLUE);
            gr.setFont(gr.getFont().deriveFont(Font.BOLD, 16f));
            String[][] data = new String[][]
                    {
                            {"ah!-", "123.00", "cou"},
                            {"ah!", "1.10", "couc"},
                            {"ah-gh!", "65465546", "couco"},
                            {"ah!127542", "-123.00", "coucou"},
                            {"ah!adasada", "0.00", "coucouxxx"},
                            {"oh!", "12.34", "coucoux"}
                    };
            int y = SwingStaticUtil.drawPanelTable(data, gr, new Point(20, 20), 10, 2,
                    new int[]{SwingStaticUtil.LEFT_ALIGNED, SwingStaticUtil.RIGHT_ALIGNED, SwingStaticUtil.CENTER_ALIGNED});
            gr.setColor(Color.RED);
            // Use the returned y for the position
            y = SwingStaticUtil.drawPanelTable(data, gr, new Point(50, y + 10), 30, 2,
                    new int[]{SwingStaticUtil.LEFT_ALIGNED, SwingStaticUtil.RIGHT_ALIGNED, SwingStaticUtil.CENTER_ALIGNED},
                    true, Color.RED, Color.ORANGE, 0.5f, 0.5f);
            System.out.println("Returned y:" + y);
        }
    };

    public TestFileChooserAndMore() {
        frame = new JFrame(TITLE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
//        System.out.printf("Default frame width %d height %d %n", frameSize.width, frameSize.height);
        frameSize.height = Math.min(frameSize.height, screenSize.height);
        frameSize.width = Math.min(frameSize.width, screenSize.width);
        if (frameSize.width == 0 || frameSize.height == 0) {
            frameSize = new Dimension(WIDTH, HEIGHT);
            frame.setSize(frameSize);
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        button.addActionListener(e -> buttonChooseFile(e));

        frame.getContentPane().add(panel, BorderLayout.CENTER);

        frame.getContentPane().add(button, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void buttonChooseFile(ActionEvent ae) {
        String fName = SwingStaticUtil.chooseFile(
                JFileChooser.FILES_ONLY,
                new String[]{"json", "txt"},
                "Data Files",
                "Choose Data File",
                "That one");
        // And then...
        System.out.println("You choose " + fName);
    }

    public static void main(String... args) {
        new TestFileChooserAndMore();
    }
}
