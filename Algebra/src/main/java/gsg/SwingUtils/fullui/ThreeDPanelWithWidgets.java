package gsg.SwingUtils.fullui;

import gsg.SwingUtils.Box3D;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.function.BiConsumer;

/**
 * A Box3D on a JPanel, in Swing.
 * With all kinds of widgets to interact with the Box3D.
 */
public class ThreeDPanelWithWidgets
		extends JPanel {

	private int prevX, prevY;

	private final Box3D box3D;

	private final JLabel xLabel = new JLabel("X");
	private final JLabel yLabel = new JLabel("Y");
	private final JLabel zLabel = new JLabel("Z");

	private final JLabel minX = new JLabel("min");
	private final JLabel maxX = new JLabel("max");
	private final JLabel minY = new JLabel("min");
	private final JLabel maxY = new JLabel("max");
	private final JLabel minZ = new JLabel("min");
	private final JLabel maxZ = new JLabel("max");

	private final JFormattedTextField minXValue = new JFormattedTextField(new DecimalFormat("#0.00"));
	private final JFormattedTextField maxXValue = new JFormattedTextField(new DecimalFormat("#0.00"));
	private final JFormattedTextField minYValue = new JFormattedTextField(new DecimalFormat("#0.00"));
	private final JFormattedTextField maxYValue = new JFormattedTextField(new DecimalFormat("#0.00"));
	private final JFormattedTextField minZValue = new JFormattedTextField(new DecimalFormat("#0.00"));
	private final JFormattedTextField maxZValue = new JFormattedTextField(new DecimalFormat("#0.00"));

	private final JLabel zoomLabel = new JLabel("Zoom:");
	private final JFormattedTextField zoomValue = new JFormattedTextField(new DecimalFormat("#0.00"));

	private JCheckBox withBoxFaces = null;
	private JCheckBox withAxis = null;

	private final JLabel rotXLabel = new JLabel("Rotation on X");
	private final JLabel rotYLabel = new JLabel("Rotation on Y");
	private final JLabel rotZLabel = new JLabel("Rotation on Z");

	private JSlider rotOnZSlider = null;
	private JSlider rotOnYSlider = null;
	private JSlider rotOnXSlider = null;

	private final JLabel rotOnZValue = new JLabel("0\u00b0");
	private final JLabel rotOnYValue = new JLabel("0\u00b0");
	private final JLabel rotOnXValue = new JLabel("0\u00b0");

	private final static int COLOR_PANEL_DIMENSION = 20;
	private final JLabel colorsLabel = new JLabel("Colors");
	private final JButton perimeterButton = new JButton("Perimeter");
	BiConsumer<Graphics2D, Color> colorSampler = (g2d, color) -> {
		g2d.setColor(color);
		g2d.fillOval(0, 0, COLOR_PANEL_DIMENSION / 2, COLOR_PANEL_DIMENSION / 2);
		g2d.setColor(Color.BLACK);
		g2d.drawOval(0, 0, COLOR_PANEL_DIMENSION / 2, COLOR_PANEL_DIMENSION / 2);
	};
	private final JPanel perimeterColorPanel = new JPanel() {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			colorSampler.accept(g2d, box3D.getPerimeterColor());
		}
	};
	private final JButton boxFacesButton = new JButton("Box Faces");
	private final JPanel boxFacesColorPanel = new JPanel() {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			colorSampler.accept(g2d, box3D.getBoxFacesColor());
		}
	};
	private final JButton gridButton = new JButton("Grid");
	private final JPanel gridColorPanel = new JPanel() {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			colorSampler.accept(g2d, box3D.getGridColor());
		}
	};
	private final JButton axisButton = new JButton("Axis");
	private final JPanel axisColorPanel = new JPanel() {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			colorSampler.accept(g2d, box3D.getAxisColor());
		}
	};
	private final JButton backgroundButton = new JButton("Background");
	private final JPanel backgroundColorPanel = new JPanel() {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			colorSampler.accept(g2d, box3D.getBackgroundColor());
		}
	};

	public final static int DEFAULT_WIDTH = 800;
	public final static int DEFAULT_HEIGHT = 800;

	private final static int MIN_SLIDER_VALUE = -180;
	private final static int MAX_SLIDER_VALUE = 180;

	public ThreeDPanelWithWidgets(Box3D box3D) {
		this(box3D, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
	}

	public ThreeDPanelWithWidgets(Box3D box3D, String title) {
		this(box3D, DEFAULT_WIDTH, DEFAULT_HEIGHT, title);
	}

	public ThreeDPanelWithWidgets(Box3D box3D, int width, int height) {
		this(box3D, width, height, null);
	}
	public ThreeDPanelWithWidgets(Box3D box3D, int width, int height, String title) {
		this.box3D = box3D;
		initComponents(width, height);
		this.setSize(new Dimension(width, height));  // Maybe conflicting...
		this.setPreferredSize(new Dimension(width, height));
//		this.setTitle(title == null ? "Box3D demo - Figure is draggable" : title); // TODO Move this at the frame level

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
//		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO Move this at the frame level
		this.setVisible(true);
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents(int width, int height) {

		// this.getContentPane().setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane(box3D, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(width, height));
		scrollPane.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
//				System.out.println("Mouse pressed.");
				scrollPane.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				prevX = e.getX();
				prevY = e.getY();
				e.consume();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
//				System.out.println("Mouse released.");
				scrollPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		scrollPane.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
//				System.out.println("Mouse being dragged.");
				int x = e.getX();
				int y = e.getY();

				double xTheta = (prevY - y) * 360.0 / getSize().width;
				double yTheta = (x - prevX) * 360.0 / getSize().height;

				double newX = box3D.getRotOnX() + xTheta;
				double newZ = box3D.getRotOnZ() - yTheta;
				box3D.setRotOnX(newX);
				box3D.setRotOnZ(newZ);

				prevX = x;
				prevY = y;
				e.consume();
//				System.out.println("rotOnX:" + newX + ", rotOnZ:" + newZ);
				box3D.repaint();
				rotOnXSlider.setValue((int)Math.round(newX));
				rotOnZSlider.setValue((int)Math.round(newZ));
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
		});

//		this.getContentPane().add(scrollPane, BorderLayout.CENTER);
		this.add(scrollPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridBagLayout());
		bottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "3D Box config"));

		JPanel xMinMaxPanel = new JPanel();
		xMinMaxPanel.setLayout(new GridBagLayout());

		JPanel yMinMaxPanel = new JPanel();
		yMinMaxPanel.setLayout(new GridBagLayout());

		JPanel zMinMaxPanel = new JPanel();
		zMinMaxPanel.setLayout(new GridBagLayout());

		JPanel cBPanel = new JPanel();
		cBPanel.setLayout(new GridBagLayout());

		JPanel colorsPanel = new JPanel();
		colorsPanel.setLayout(new GridBagLayout());

		JPanel rotXPanel = new JPanel();
		rotXPanel.setLayout(new GridBagLayout());
		JPanel rotYPanel = new JPanel();
		rotYPanel.setLayout(new GridBagLayout());
		JPanel rotZPanel = new JPanel();
		rotZPanel.setLayout(new GridBagLayout());

		withBoxFaces = new JCheckBox("With box faces");
		withBoxFaces.setSelected(box3D.isWithBoxFaces());
		withBoxFaces.addChangeListener(e -> {
			box3D.setWithBoxFaces(withBoxFaces.isSelected());
			box3D.repaint();
		});

		withAxis = new JCheckBox("With axis");
		withAxis.setSelected(box3D.isWithAxis());
		withAxis.addChangeListener(e -> {
			box3D.setWithAxis(withAxis.isSelected());
			box3D.repaint();
		});

		rotOnZSlider = new JSlider(JSlider.HORIZONTAL, MIN_SLIDER_VALUE, MAX_SLIDER_VALUE, (int)Math.round(box3D.getRotOnZ()));
		rotOnZSlider.setPreferredSize(new Dimension(300, 30));
		rotOnZSlider.setEnabled(true);

		rotOnZSlider.addChangeListener(changeEvent -> {
			int sliderValue = rotOnZSlider.getValue();
//			System.out.println(String.format("Zoom: %d => %.02f", zoomSliderValue, sliderToZoom(zoomSliderValue)));
			rotOnZValue.setText(String.format("%d\u00b0", sliderValue));
			box3D.setRotOnZ(sliderValue);
			box3D.repaint();
		});
		rotOnZSlider.setToolTipText("Rotation on Z");

		rotOnYSlider = new JSlider(JSlider.HORIZONTAL, MIN_SLIDER_VALUE, MAX_SLIDER_VALUE, (int)Math.round(box3D.getRotOnY()));
		rotOnYSlider.setPreferredSize(new Dimension(300, 30));
		rotOnYSlider.setEnabled(true);
		rotOnYSlider.addChangeListener(changeEvent -> {
			int sliderValue = rotOnYSlider.getValue();
			rotOnYValue.setText(String.format("%d\u00b0", sliderValue));
			box3D.setRotOnY(sliderValue);
			box3D.repaint();
		});
		rotOnYSlider.setToolTipText("Rotation on Y");

		rotOnXSlider = new JSlider(JSlider.HORIZONTAL, MIN_SLIDER_VALUE, MAX_SLIDER_VALUE, (int)Math.round(box3D.getRotOnX()));
		rotOnXSlider.setPreferredSize(new Dimension(300, 30));
		rotOnXSlider.setEnabled(true);
		rotOnXSlider.addChangeListener(changeEvent -> {
			int sliderValue = rotOnXSlider.getValue();
			rotOnXValue.setText(String.format("%d\u00b0", sliderValue));
			box3D.setRotOnX(sliderValue);
			box3D.repaint();
		});
		rotOnXSlider.setToolTipText("Rotation on X");

		int nbLinesInBottomPanel = 0;

		// Min and Max values
		// X
		xLabel.setFont(xLabel.getFont().deriveFont(Font.ITALIC | Font.BOLD));
		xMinMaxPanel.add(xLabel, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 10), 0, 0));
		xMinMaxPanel.add(minX, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		minXValue.setHorizontalAlignment(SwingConstants.RIGHT);
		minXValue.setPreferredSize(new Dimension(80, 20));
		minXValue.setText(String.valueOf(box3D.getxMin()));
		minXValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkValue();
			}
			public void removeUpdate(DocumentEvent e) {
				checkValue();
			}
			public void insertUpdate(DocumentEvent e) {
				checkValue();
			}
			public void checkValue() {
				if (!minXValue.getText().trim().isEmpty()) {
					try {
						double val = Double.parseDouble(minXValue.getText());
						if (val >= box3D.getxMax()) {
							JOptionPane.showMessageDialog(minXValue,
									"Error: Please enter a number smaller than max X", "Error Message",
									JOptionPane.ERROR_MESSAGE);
						} else {
							box3D.setxMin(val);
							box3D.repaint();
						}
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
					}
				}
			}
		});
		xMinMaxPanel.add(minXValue, new GridBagConstraints(2,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		xMinMaxPanel.add(maxX, new GridBagConstraints(3,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		maxXValue.setHorizontalAlignment(SwingConstants.RIGHT);
		maxXValue.setPreferredSize(new Dimension(80, 20));
		maxXValue.setText(String.valueOf(box3D.getxMax()));
		maxXValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkValue();
			}
			public void removeUpdate(DocumentEvent e) {
				checkValue();
			}
			public void insertUpdate(DocumentEvent e) {
				checkValue();
			}
			public void checkValue() {
				if (!maxXValue.getText().trim().isEmpty()) {
					try {
						double val = Double.parseDouble(maxXValue.getText());
						if (val <= box3D.getxMin()) {
							JOptionPane.showMessageDialog(maxXValue,
									"Error: Please enter a number bigger than min X", "Error Message",
									JOptionPane.ERROR_MESSAGE);
						} else {
							box3D.setxMax(val);
							box3D.repaint();
						}
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
					}
				}
			}
		});
		xMinMaxPanel.add(maxXValue, new GridBagConstraints(4,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(xMinMaxPanel, new GridBagConstraints(0,
				nbLinesInBottomPanel++,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		// Y
		yLabel.setFont(yLabel.getFont().deriveFont(Font.ITALIC | Font.BOLD));
		yMinMaxPanel.add(yLabel, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 10), 0, 0));
		yMinMaxPanel.add(minY, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		minYValue.setHorizontalAlignment(SwingConstants.RIGHT);
		minYValue.setPreferredSize(new Dimension(80, 20));
		minYValue.setText(String.valueOf(box3D.getyMin()));
		minYValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkValue();
			}
			public void removeUpdate(DocumentEvent e) {
				checkValue();
			}
			public void insertUpdate(DocumentEvent e) {
				checkValue();
			}
			public void checkValue() {
				if (!minYValue.getText().trim().isEmpty()) {
					try {
						double val = Double.parseDouble(minYValue.getText());
						if (val >= box3D.getyMax()) {
							JOptionPane.showMessageDialog(minYValue,
									"Error: Please enter a number smaller than max Y", "Error Message",
									JOptionPane.ERROR_MESSAGE);
						} else {
							box3D.setyMin(val);
							box3D.repaint();
						}
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
					}
				}
			}
		});
		yMinMaxPanel.add(minYValue, new GridBagConstraints(2,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		yMinMaxPanel.add(maxY, new GridBagConstraints(3,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		maxYValue.setHorizontalAlignment(SwingConstants.RIGHT);
		maxYValue.setPreferredSize(new Dimension(80, 20));
		maxYValue.setText(String.valueOf(box3D.getyMax()));
		maxYValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkValue();
			}
			public void removeUpdate(DocumentEvent e) {
				checkValue();
			}
			public void insertUpdate(DocumentEvent e) {
				checkValue();
			}
			public void checkValue() {
				if (!maxYValue.getText().trim().isEmpty()) {
					try {
						double val = Double.parseDouble(maxYValue.getText());
						if (val <= box3D.getyMin()) {
							JOptionPane.showMessageDialog(maxYValue,
									"Error: Please enter a number bigger than min Y", "Error Message",
									JOptionPane.ERROR_MESSAGE);
						} else {
							box3D.setyMax(val);
							box3D.repaint();
						}
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
					}
				}
			}
		});
		yMinMaxPanel.add(maxYValue, new GridBagConstraints(4,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(yMinMaxPanel, new GridBagConstraints(0,
				nbLinesInBottomPanel++,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		// Z
		zLabel.setFont(zLabel.getFont().deriveFont(Font.ITALIC | Font.BOLD));
		zMinMaxPanel.add(zLabel, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 10), 0, 0));
		zMinMaxPanel.add(minZ, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		minZValue.setHorizontalAlignment(SwingConstants.RIGHT);
		minZValue.setPreferredSize(new Dimension(80, 20));
		minZValue.setText(String.valueOf(box3D.getzMin()));
		minZValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkValue();
			}
			public void removeUpdate(DocumentEvent e) {
				checkValue();
			}
			public void insertUpdate(DocumentEvent e) {
				checkValue();
			}
			public void checkValue() {
				if (!minZValue.getText().trim().isEmpty()) {
					try {
						double val = Double.parseDouble(minZValue.getText());
						if (val >= box3D.getzMax()) {
							JOptionPane.showMessageDialog(minZValue,
									"Error: Please enter a number smaller than max Z", "Error Message",
									JOptionPane.ERROR_MESSAGE);
						} else {
							box3D.setzMin(val);
							box3D.repaint();
						}
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
					}
				}
			}
		});
		zMinMaxPanel.add(minZValue, new GridBagConstraints(2,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		zMinMaxPanel.add(maxZ, new GridBagConstraints(3,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		maxZValue.setHorizontalAlignment(SwingConstants.RIGHT);
		maxZValue.setPreferredSize(new Dimension(80, 20));
		maxZValue.setText(String.valueOf(box3D.getzMax()));
		maxZValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkValue();
			}
			public void removeUpdate(DocumentEvent e) {
				checkValue();
			}
			public void insertUpdate(DocumentEvent e) {
				checkValue();
			}
			public void checkValue() {
				if (!maxZValue.getText().trim().isEmpty()) {
					try {
						double val = Double.parseDouble(maxZValue.getText());
						if (val <= box3D.getzMin()) {
							JOptionPane.showMessageDialog(maxZValue,
									"Error: Please enter a number bigger than min Z", "Error Message",
									JOptionPane.ERROR_MESSAGE);
						} else {
							box3D.setzMax(val);
							box3D.repaint();
						}
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
					}
				}
			}
		});
		zMinMaxPanel.add(maxZValue, new GridBagConstraints(4,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(zMinMaxPanel, new GridBagConstraints(0,
				nbLinesInBottomPanel++,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// Check boxes
		cBPanel.add(withBoxFaces, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		cBPanel.add(withAxis, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		cBPanel.add(zoomLabel, new GridBagConstraints(2,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		zoomValue.setHorizontalAlignment(SwingConstants.RIGHT);
		zoomValue.setPreferredSize(new Dimension(80, 20));
		zoomValue.setText(String.valueOf(box3D.getZoom()));
		zoomValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkValue();
			}
			public void removeUpdate(DocumentEvent e) {
				checkValue();
			}
			public void insertUpdate(DocumentEvent e) {
				checkValue();
			}
			public void checkValue() {
				if (!zoomValue.getText().trim().isEmpty()) {
					try {
						double val = Double.parseDouble(zoomValue.getText());
						box3D.setZoom(val);
						box3D.repaint();
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
					}
				}
			}
		});
		cBPanel.add(zoomValue, new GridBagConstraints(3,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(cBPanel, new GridBagConstraints(0,
				nbLinesInBottomPanel++,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// Colors
		colorsLabel.setFont(colorsLabel.getFont().deriveFont(Font.ITALIC | Font.BOLD));
		colorsPanel.add(colorsLabel, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 10), 0, 0));
		// Perimeter
		perimeterButton.addActionListener(e -> {
			Color color = JColorChooser.showDialog(perimeterButton,
					"Perimeter Color", Color.CYAN);
			if (color != null) {
				box3D.setPerimeterColor(color);
				box3D.repaint();
				perimeterColorPanel.repaint();
			} else {
				System.out.println("No color chosen.");
			}
		});
		colorsPanel.add(perimeterButton, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		perimeterColorPanel.setSize(COLOR_PANEL_DIMENSION, COLOR_PANEL_DIMENSION);
		colorsPanel.add(perimeterColorPanel, new GridBagConstraints(2,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		// Box Faces
		boxFacesButton.addActionListener(e -> {
			Color color = JColorChooser.showDialog(boxFacesButton,
					"Box Faces Color", box3D.getBoxFacesColor());
			if (color != null) {
				box3D.setBoxFacesColor(color);
				box3D.repaint();
				boxFacesColorPanel.repaint();
			} else {
				System.out.println("No color chosen.");
			}
		});
		colorsPanel.add(boxFacesButton, new GridBagConstraints(3,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		boxFacesColorPanel.setSize(COLOR_PANEL_DIMENSION, COLOR_PANEL_DIMENSION);
		colorsPanel.add(boxFacesColorPanel, new GridBagConstraints(4,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		// Grid
		gridButton.addActionListener(e -> {
			Color color = JColorChooser.showDialog(gridButton,
					"Grid Color", box3D.getGridColor());
			if (color != null) {
				box3D.setGridColor(color);
				box3D.repaint();
				gridColorPanel.repaint();
			} else {
				System.out.println("No color chosen.");
			}
		});
		colorsPanel.add(gridButton, new GridBagConstraints(5,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		gridColorPanel.setSize(COLOR_PANEL_DIMENSION, COLOR_PANEL_DIMENSION);
		colorsPanel.add(gridColorPanel, new GridBagConstraints(6,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		// Axis
		axisButton.addActionListener(e -> {
			Color color = JColorChooser.showDialog(axisButton,
					"Axis Color", box3D.getAxisColor());
			if (color != null) {
				box3D.setAxisColor(color);
				box3D.repaint();
				axisColorPanel.repaint();
			} else {
				System.out.println("No color chosen.");
			}
		});
		colorsPanel.add(axisButton, new GridBagConstraints(7,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		axisColorPanel.setSize(COLOR_PANEL_DIMENSION, COLOR_PANEL_DIMENSION);
		colorsPanel.add(axisColorPanel, new GridBagConstraints(8,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		// Background
		backgroundButton.addActionListener(e -> {
			Color color = JColorChooser.showDialog(backgroundButton,
					"Background Color", box3D.getAxisColor());
			if (color != null) {
				box3D.setBackgroundColor(color);
				box3D.repaint();
				backgroundColorPanel.repaint();
			} else {
				System.out.println("No color chosen.");
			}
		});
		colorsPanel.add(backgroundButton, new GridBagConstraints(9,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));
		backgroundColorPanel.setSize(COLOR_PANEL_DIMENSION, COLOR_PANEL_DIMENSION);
		colorsPanel.add(backgroundColorPanel, new GridBagConstraints(10,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 1), 0, 0));

		bottomPanel.add(colorsPanel, new GridBagConstraints(0,
				nbLinesInBottomPanel++,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// Rotations, on X
		rotXPanel.add(rotXLabel, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		rotXPanel.add(rotOnXSlider, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		rotOnXValue.setText(String.valueOf((int)Math.round(box3D.getRotOnX())) + "\u00b0");
		rotXPanel.add(rotOnXValue, new GridBagConstraints(2,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(rotXPanel, new GridBagConstraints(0,
				nbLinesInBottomPanel++,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// Rotations, on Y
		rotYPanel.add(rotYLabel, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		rotYPanel.add(rotOnYSlider, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		rotOnYValue.setText(String.valueOf((int)Math.round(box3D.getRotOnY())) + "\u00b0");
		rotYPanel.add(rotOnYValue, new GridBagConstraints(2,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(rotYPanel, new GridBagConstraints(0,
				nbLinesInBottomPanel++,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));


		// Rotations, on Z
		rotZPanel.add(rotZLabel, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		rotZPanel.add(rotOnZSlider, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		rotOnZValue.setText(String.valueOf((int)Math.round(box3D.getRotOnZ())) + "\u00b0");
		rotZPanel.add(rotOnZValue, new GridBagConstraints(2,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(rotZPanel, new GridBagConstraints(0,
				nbLinesInBottomPanel++,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		this.add(bottomPanel, BorderLayout.SOUTH);

//		this.pack();
	}

	public boolean isWithFacesChecked() {
		return this.withBoxFaces.isSelected();
	}
}
