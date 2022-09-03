package main.gui.gyro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GyroDisplayFrame
        extends JFrame {
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();

    private GyroDisplayPanel displayPanel = null;
    private transient GyroscopeUI caller;

    public GyroDisplayFrame(GyroscopeUI parent) {
        this.caller = parent;
        displayPanel = new GyroDisplayPanel();
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
        this.setSize(new Dimension(400, 400));
        this.setTitle("Gyroscope UI");
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
