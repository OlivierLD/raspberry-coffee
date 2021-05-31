package dnd.gui.utils;

import dnd.gui.ctx.AppContext;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

class FSTransfer
		extends TransferHandler {
	private static final long serialVersionUID = 6535784439057561617L;
	private ImagePanel parent = null;

	public FSTransfer(ImagePanel caller)
	{
		this.parent = caller;
	}

	public boolean importData(JComponent comp, Transferable t) {
		if (!(comp instanceof ImagePanel)) {
			return false;
		}
		if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return false;
		}

		try {
			final List data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);

			Thread loader = new Thread() {
				public void run() {
					try {
						Iterator i = data.iterator();
						int max = data.size();
						int current = 0;
						AppContext.getInstance().fireActivateProgressBar(true);
						AppContext.getInstance().fireSetProgressBar(current, max);
						while (i.hasNext()) {
							File f = (File) i.next();
							String str = "Loading " + f.getName() + "...";
							if (System.getProperty("verbose", "false").equals("true")) System.out.println(str);
							AppContext.getInstance().fireSetStatusLabel(str);

							try {
								parent.setFileToLoad(f.getAbsolutePath());
								AppContext.getInstance().fireSetProgressBar(++current, max);
							} catch (RuntimeException rte) {
								System.out.println("Exception:" + rte.getLocalizedMessage());
								break;
							}
						}
						if (System.getProperty("verbose", "false").equals("true")) System.out.println("Loader is done.");
						AppContext.getInstance().fireActivateProgressBar(false);
					} catch (Exception e) {
						System.err.println("From loader thread:");
						e.printStackTrace();
					}
				}
			};
			loader.start();
			return true;
		} catch (UnsupportedFlavorException ufe) {
			System.err.println("Ack! we should not be here.\nBad Flavor.");
		} catch (IOException ioe) {
			System.out.println("Something failed during import:\n" + ioe);
		}
		return false;
	}

	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		if ((comp instanceof ImagePanel)) {
			for (int i = 0; i < transferFlavors.length; i++) {
				if (!transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
