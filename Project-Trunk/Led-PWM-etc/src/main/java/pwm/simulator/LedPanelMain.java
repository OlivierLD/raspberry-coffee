package pwm.simulator;

import javax.swing.*;
import java.awt.*;

/**
 * A PWM simulation, in Swing.
 */
public class LedPanelMain extends JFrame {
	private LedPanelMain instance = this;
	private LEDPanel ledPanel;

	private SoftPin[] ledPins;
	private PWMSoftPin[] pwm;

	public LedPanelMain() {
		initComponents();
		this.setSize(new Dimension(200, 65));
		this.setPreferredSize(new Dimension(200, 65));
		this.setTitle("PWM Simulator");

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		this.setVisible(true);

		final LedPanelMain instance = this;
		int NB_PINS = 4;
		ledPins = new SoftPin[NB_PINS];
		for (int i=0; i<NB_PINS; i++) {
			final int idx = i;
			ledPins[i] = new SoftPin() {
				public void high() {
					instance.setLed(idx, true);
				}
				public void low() {
					instance.setLed(idx, false);
				}
			};
		}
		pwm = new PWMSoftPin[NB_PINS];
		int cycleWidth = 50; // in ms
		for (int i=0; i<NB_PINS; i++) {
			pwm[i] = new PWMSoftPin(ledPins[i], String.valueOf(i), cycleWidth);
		}
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		ledPanel = new LEDPanel();
		ledPanel.setWithGrid(false);
		ledPanel.setLedColor(Color.RED);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});
		this.add(ledPanel, BorderLayout.CENTER);

		this.pack();
	}

	public void setLed(int ledIdx, boolean status) {
		boolean[][] buffer = ledPanel.getLedOnOff();
		buffer[ledIdx][0] = status;
		ledPanel.setLedOnOff(buffer);
		display();
	}

	private void display() {
		ledPanel.repaint();
	}

	public void doYourJob() {
		LedPanelMain lcd = instance;
//  instance.repaint();

		pwm[0].emitPWM( 25);
		pwm[1].emitPWM( 50);
		pwm[2].emitPWM( 75);
		pwm[3].emitPWM(100);
	}

	/**
	 * Exit the Application
	 */
	private void exitForm(java.awt.event.WindowEvent evt) {
		System.out.println("Bye");
		System.exit(0);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		LedPanelMain lp = new LedPanelMain();
		lp.setVisible(true);

		for (int i=0; i<4; i++) {
			lp.setLed(i, true);
		}

		lp.doYourJob();
	}
}
