package adc.gui;

import adc.ADCObserver;

import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AnalogDisplayApp {
	private ADCObserver.MCP3008_input_channels channel = null;
	private final ADCObserver obs;

	public AnalogDisplayApp(int ch) {
		channel = findChannel(ch);
		obs = new ADCObserver(channel); // Note: We could instantiate more than one observer (on several channels).

		JFrame frame = new AnalogDisplayFrame(channel, this);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
//  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
				System.exit(0);
			}
		});

		frame.setVisible(true);

		if (true) { // For Dev...
			try {
				obs.start();
			} catch (Exception ioe) {
				System.err.println("Oops");
				ioe.printStackTrace();
			}
		}
	}

	public static void main(String... args) {
		try {
			if (System.getProperty("swing.defaultlaf") == null)
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		int channel = 0;
		if (args.length > 0) {
			channel = Integer.parseInt(args[0]);
		}
		new AnalogDisplayApp(channel);
//  swingWait(1L);
	}

	public void close() {
		System.out.println("Exiting.");
		if (obs != null)
			obs.stop();
	}

	private static ADCObserver.MCP3008_input_channels findChannel(int ch) throws IllegalArgumentException {
		ADCObserver.MCP3008_input_channels channel = null;
		switch (ch) {
			case 0:
				channel = ADCObserver.MCP3008_input_channels.CH0;
				break;
			case 1:
				channel = ADCObserver.MCP3008_input_channels.CH1;
				break;
			case 2:
				channel = ADCObserver.MCP3008_input_channels.CH2;
				break;
			case 3:
				channel = ADCObserver.MCP3008_input_channels.CH3;
				break;
			case 4:
				channel = ADCObserver.MCP3008_input_channels.CH4;
				break;
			case 5:
				channel = ADCObserver.MCP3008_input_channels.CH5;
				break;
			case 6:
				channel = ADCObserver.MCP3008_input_channels.CH6;
				break;
			case 7:
				channel = ADCObserver.MCP3008_input_channels.CH7;
				break;
			default:
				throw new IllegalArgumentException("No channel " + Integer.toString(ch));
		}
		return channel;
	}

	private static void swingWait(final long w) {
		try {
			SwingUtilities.invokeAndWait(() -> {
				try {
					Thread.sleep(w);
				} catch (Exception ex) {
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
