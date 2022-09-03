package main.gui.acc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccelerometerDisplayFrame
        extends JFrame {
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();

    private AccelerometerDisplayPanel displayPanel = null;
    private transient AccelerometerUI caller;

    public AccelerometerDisplayFrame(AccelerometerUI parent) {
        this.caller = parent;
        displayPanel = new AccelerometerDisplayPanel();
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
        this.setJMenuBar(menuBar);
        this.getContentPane().setLayout(new BorderLayout());
        this.setSize(new Dimension(800, 400));
        this.setTitle("Accelerometer UI");
        menuFile.setText("File");
        menuFileExit.setText("Exit");
        menuFileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                fileExit_ActionPerformed(ae);
            }
        });
        menuFile.add(menuFileExit);
        menuBar.add(menuFile);

        this.getContentPane().add(displayPanel, BorderLayout.CENTER);
    }

    void fileExit_ActionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        this.caller.close();
        System.exit(0);
    }
}
