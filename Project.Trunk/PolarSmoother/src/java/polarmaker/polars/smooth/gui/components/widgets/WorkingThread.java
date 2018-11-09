package polarmaker.polars.smooth.gui.components.widgets;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;

public class WorkingThread extends Thread {
	public WorkingThread() {
	}

	public WorkingThread(String s) {
		this.label = s;
	}

	String label = "";
	WorkingFrame splash = new WorkingFrame();

	public void run() {
		splash.setText(label);
		splash.getProgressBar().setIndeterminate(true);
		splash.setSize(new Dimension(400, 128));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = splash.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		splash.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		splash.setVisible(true);
//  splash.show();
		splash.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		splash.repaint();
/*  try { Thread.sleep(3500); }
    catch (Exception ignore){}
    finally
    {
      splash.setVisible(false);
      splash.dispose();
    }  */
	}

	public void shutSplash() {
		splash.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		splash.setVisible(false);
		splash.dispose();
	}
}
