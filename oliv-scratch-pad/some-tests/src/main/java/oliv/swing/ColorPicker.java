package oliv.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ColorPicker {
    public static void main(String... args) {
        JFrame frame = new JFrame("JColorChooser Sample");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JButton button = new JButton("Click to Change Background");
        button.setSize(200, 30);

        ActionListener actionListener = actionEvent -> {
            Color initialBackground = frame.getContentPane().getBackground();
            Color background = JColorChooser.showDialog(frame,
                    "JColorChooser Sample", initialBackground);
            if (background != null) {
                System.out.println("Color:" + background.toString());
                frame.getContentPane().setBackground(background);
            } else {
                System.out.println("No color chosen.");
            }
        };
        button.addActionListener(actionListener);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(button, BorderLayout.NORTH);
        frame.setSize(300, 200);
        frame.setVisible(true);
    }
}
