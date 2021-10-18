package boatdesign.swingstuff;

import gsg.SwingUtils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;

/**
 *         minX, maxX, minY, maxY, minZ, maxZ, defaultLHT
 *         description, comments
 */
public class NewDataPanel extends JPanel {
    private double minX, maxX, minY, maxY, minZ, maxZ, defaultLHT;
    private String description, comments;

    private boolean withFileChooser = false;

    private JLabel minXLabel = new JLabel("Min X");
    private JLabel maxXLabel = new JLabel("Max X");
    private JLabel minYLabel = new JLabel("Min Y");
    private JLabel maxYLabel = new JLabel("Max Y");
    private JLabel minZLabel = new JLabel("Min Z");
    private JLabel maxZLabel = new JLabel("Max Z");
    private JLabel defaultLHTLabel = new JLabel("Default LHT");

    private JLabel descriptionLabel = new JLabel("Description");
    private JLabel commentsLabel = new JLabel("Comments");

    JFormattedTextField minXValue = new JFormattedTextField(new DecimalFormat("#0.00"));
    JFormattedTextField maxXValue = new JFormattedTextField(new DecimalFormat("#0.00"));
    JFormattedTextField minYValue = new JFormattedTextField(new DecimalFormat("#0.00"));
    JFormattedTextField maxYValue = new JFormattedTextField(new DecimalFormat("#0.00"));
    JFormattedTextField minZValue = new JFormattedTextField(new DecimalFormat("#0.00"));
    JFormattedTextField maxZValue = new JFormattedTextField(new DecimalFormat("#0.00"));

    JFormattedTextField defaultLhtValue = new JFormattedTextField(new DecimalFormat("#0.00"));

    JTextField descriptionField = new JTextField();
    JTextPane commentsTextArea = new JTextPane();
    JScrollPane commentsScrollPane = new JScrollPane(commentsTextArea);

    JFileChooser fileChooser = null;

    public NewDataPanel() {
        this(false);
    }
    public NewDataPanel(boolean withFileChooser) {
        super();
        this.withFileChooser = withFileChooser;
        initComponents();
    }

    public void initComponents() {
        minXValue.setPreferredSize(new Dimension(60, 20));
        minXValue.setHorizontalAlignment(SwingConstants.RIGHT);
        maxXValue.setPreferredSize(new Dimension(60, 20));
        maxXValue.setHorizontalAlignment(SwingConstants.RIGHT);
        minYValue.setPreferredSize(new Dimension(60, 20));
        minYValue.setHorizontalAlignment(SwingConstants.RIGHT);
        maxYValue.setPreferredSize(new Dimension(60, 20));
        maxYValue.setHorizontalAlignment(SwingConstants.RIGHT);
        minZValue.setPreferredSize(new Dimension(60, 20));
        minZValue.setHorizontalAlignment(SwingConstants.RIGHT);
        maxZValue.setPreferredSize(new Dimension(60, 20));
        maxZValue.setHorizontalAlignment(SwingConstants.RIGHT);

        defaultLhtValue.setPreferredSize(new Dimension(60, 20));
        defaultLhtValue.setHorizontalAlignment(SwingConstants.RIGHT);

        commentsTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        commentsTextArea.setText("Your comments here...");

        this.setLayout(new GridBagLayout());

        // Min-max labels
        this.add(minXLabel,
                new GridBagConstraints(0,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.CENTER, // BOTH
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(maxXLabel,
                new GridBagConstraints(1,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.CENTER,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(minYLabel,
                new GridBagConstraints(2,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.CENTER,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(maxYLabel,
                new GridBagConstraints(3,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.CENTER,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(minZLabel,
                new GridBagConstraints(4,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.CENTER,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(maxZLabel,
                new GridBagConstraints(5,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.CENTER,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(defaultLHTLabel,
                new GridBagConstraints(6,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.CENTER,
                        new Insets(0, 0, 0, 0), 0, 0));
        // Min-max fields
        this.add(minXValue,
                new GridBagConstraints(0,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(maxXValue,
                new GridBagConstraints(1,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(minYValue,
                new GridBagConstraints(2,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(maxYValue,
                new GridBagConstraints(3,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(minZValue,
                new GridBagConstraints(4,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(maxZValue,
                new GridBagConstraints(5,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(defaultLhtValue,
                new GridBagConstraints(6,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        // Description
        this.add(descriptionLabel,
                new GridBagConstraints(0,
                        2,
                        2,
                        1,
                        1.0,
                        1.0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.CENTER, // EAST,
                        new Insets(0, 0, 0, 5), 0, 0));
        this.add(descriptionField,
                new GridBagConstraints(2,
                        2,
                        5,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        // Comments
        this.add(commentsLabel,
                new GridBagConstraints(0,
                        3,
                        2,
                        1,
                        1.0,
                        1.0,
                        GridBagConstraints.NORTHEAST,
                        GridBagConstraints.CENTER, // NORTHEAST,
                        new Insets(0, 0, 0, 5), 0, 0));
        commentsScrollPane.setPreferredSize(new Dimension(300, 200));
        this.add(commentsScrollPane,
                new GridBagConstraints(2,
                        3,
                        5,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        // File Chooser?
        if (this.withFileChooser) {
            this.fileChooser = new JFileChooser();

            SwingUtils.ToolFileFilter filter = new SwingUtils.ToolFileFilter("json", "Boat Data");
            this.fileChooser.setCurrentDirectory(new File("."));
            this.fileChooser.addChoosableFileFilter(filter);
            this.fileChooser.setFileFilter(filter);
            this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            this.fileChooser.setDialogType(JFileChooser.SAVE_DIALOG); // Important. Show the filed name on Mac
            this.fileChooser.setBorder(BorderFactory.createTitledBorder("Save as..."));
            this.fileChooser.setApproveButtonText("Save as...");
            this.fileChooser.revalidate();
            this.add(this.fileChooser,
                    new GridBagConstraints(0,
                            4,
                            7,
                            1,
                            1.0,
                            0.0,
                            GridBagConstraints.CENTER,
                            GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
        }
    }

    public static class PanelData {
        double minX;
        double maxX;
        double minY;
        double maxY;
        double minZ;
        double maxZ;
        double defaultLHT;
        String description;
        String comments;
        String fileName;
    }

    public void setValues(double minX,
                          double maxX,
                          double minY,
                          double maxY,
                          double minZ,
                          double maxZ,
                          double defaultLHT,
                          String description,
                          String comments) {
        this.minXValue.setValue(minX);
        this.maxXValue.setValue(maxX);
        this.minYValue.setValue(minY);
        this.maxYValue.setValue(maxY);
        this.minZValue.setValue(minZ);
        this.maxZValue.setValue(maxZ);

        this.defaultLhtValue.setValue(defaultLHT);

        this.descriptionField.setText(description);
        this.commentsTextArea.setText(comments);
    }

    public PanelData getPanelData() {
        PanelData panelData = new PanelData();

        panelData.minX = (double)this.minXValue.getValue();
        panelData.maxX = (double)this.maxXValue.getValue();
        panelData.minY = (double)this.minYValue.getValue();
        panelData.maxY = (double)this.maxYValue.getValue();
        panelData.minZ = (double)this.minZValue.getValue();
        panelData.maxZ = (double)this.maxZValue.getValue();
        panelData.defaultLHT = (double)this.defaultLhtValue.getValue();
        panelData.description = this.descriptionField.getText();
        panelData.comments = this.commentsTextArea.getText();

        if (this.withFileChooser) {
            // String fName = this.fileChooser.
            System.out.println("File name to come...");
            File selectedFile = this.fileChooser.getSelectedFile();
            if (selectedFile != null) {
                System.out.println("Selected:" + selectedFile.getAbsolutePath());
                panelData.fileName = selectedFile.getAbsolutePath();
            } else {
                System.out.println("No file selected.");
                panelData.fileName = null;
            }
        }

        return panelData;
    }
}
