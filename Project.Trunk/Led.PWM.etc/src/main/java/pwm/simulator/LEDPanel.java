package pwm.simulator;

import java.awt.Color;
import java.awt.Graphics;

public class LEDPanel
		extends javax.swing.JPanel {
	private Color ledColor = Color.red;
	private boolean withGrid = false;

	private static int NB_LINES = 1;
	private static int NB_COLS = 4;

	private boolean[][] ledOnOff; // = new boolean[NB_COLS][NB_LINES];

	public void setLedOnOff(boolean[][] ledOnOff) {
		this.ledOnOff = ledOnOff;
	}

	public boolean[][] getLedOnOff() {
		return ledOnOff;
	}

	/**
	 * Creates new form LEDPanel
	 */
	public LEDPanel() {
		this(NB_LINES, NB_COLS);
	}

	public LEDPanel(int nbLines, int nbCols) {
		NB_LINES = nbLines;
		NB_COLS = nbCols;
		initLeds();
		initComponents();
	}

	public void setLedColor(Color c) {
		this.ledColor = c;
	}

	public void setWithGrid(boolean withGrid) {
		this.withGrid = withGrid;
	}

	private void initLeds() {
		ledOnOff = new boolean[NB_COLS][NB_LINES];
		for (int r = 0; r < NB_LINES; r++) {
			for (int c = 0; c < NB_COLS; c++) {
				ledOnOff[c][r] = false;
			}
		}
	}

	public void clear() {
		initLeds();
		this.repaint();
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGap(0, 400, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGap(0, 300, Short.MAX_VALUE)
		);
	}

	@Override
	public void paintComponent(Graphics gr) {
		gr.setColor(Color.black);
		gr.fillRect(0, 0, this.getWidth(), this.getHeight());
		// Grid
		if (withGrid) {
			gr.setColor(Color.gray);
			for (int c = 0; c < NB_COLS; c++) {
				gr.drawLine(c * this.getWidth() / NB_COLS, 0, c * this.getWidth() / NB_COLS, this.getHeight());
			}
			for (int r = 0; r < NB_LINES; r++) {
				gr.drawLine(0, r * this.getHeight() / NB_LINES, this.getWidth(), r * this.getHeight() / NB_LINES);
			}
		}
		gr.setColor(ledColor);
		for (int r = 0; r < NB_LINES; r++) {
			for (int c = 0; c < NB_COLS; c++) {
				if (ledOnOff[c][r]) {
					// Change that, with gradient and this sort of stuff, if necessary
					gr.fillOval((c * this.getWidth() / NB_COLS) + 1,
							(r * this.getHeight() / NB_LINES) + 1,
							(this.getWidth() / NB_COLS) - 2,
							(this.getHeight() / NB_LINES) - 2);
				}
			}
		}
	}
}
