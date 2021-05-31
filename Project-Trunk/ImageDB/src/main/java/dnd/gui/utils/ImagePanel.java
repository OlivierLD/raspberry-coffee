package dnd.gui.utils;

import dnd.gui.ctx.AppContext;
import dnd.gui.ctx.ImageAppListener;
import dnd.sqlite.Populate;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.datatransfer.Transferable;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

public class ImagePanel
		extends JPanel {
	private static final long serialVersionUID = -2116609065971018541L;
	private BorderLayout borderLayout1 = new BorderLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JPanel bottomPanel = new JPanel();
	private JRadioButton scaleRadioButton = new JRadioButton("Re-scale");
	private JRadioButton noScaleRadioButton = new JRadioButton("As-is");
	private ButtonGroup group = new ButtonGroup();

	private DropTarget dropTarget;

	public ImagePanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		dropTarget = new DropTarget(this,new DropTargetImplementation(this));
	}

	private void jbInit() throws Exception {
		AppContext.getInstance().addApplicationListener(new ImageAppListener() {
			public void displayImage(String imgName) {
				display(imgName);
			}
		});
		setTransferHandler(new FSTransfer(this));

		setLayout(this.borderLayout1);
		this.jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(this.jScrollPane1, BorderLayout.CENTER);
		this.group.add(this.scaleRadioButton);
		this.group.add(this.noScaleRadioButton);
		this.bottomPanel.add(this.scaleRadioButton, null);
		this.bottomPanel.add(this.noScaleRadioButton, null);
		this.scaleRadioButton.setSelected(true);
		add(this.bottomPanel, BorderLayout.SOUTH);
		this.scaleRadioButton.addActionListener(e -> repaint());
		this.noScaleRadioButton.addActionListener(e -> repaint());
		this.jScrollPane1.getViewport().add(new JPanel() {
			public void paintComponent(Graphics gr) {
				gr.setColor(Color.white);
				gr.drawString("Drag & drop images here to populate the database", 12, 22);
				gr.setColor(Color.black);
				gr.drawString("Drag & drop images here to populate the database", 10, 20);
			}
		}, null);
	}

	public void display(String in) {
		BufferedImage bi = ImageDBUtils.getImage(in, AppContext.getInstance().getConn());
		display(bi);
	}

	public void setFileToLoad(String imageLocation) {
		if ((imageLocation.toUpperCase().endsWith(".TIF")) || (imageLocation.toUpperCase().endsWith(".TIFF"))) {
			JOptionPane.showMessageDialog(this, "TIFF Format not supported yet...", "Upload", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			File imageFile = new File(imageLocation);
			BufferedImage bi = ImageIO.read(imageFile);
			long lastModified = imageFile.lastModified();

			if (bi == null) {
				JOptionPane.showMessageDialog(this, "[" + imageLocation + "]\nis not a supported image...\nCanceling.", "New image", JOptionPane.WARNING_MESSAGE);
				return;
			}
			String imageName = imageLocation.substring(imageLocation.lastIndexOf(File.separator) + 1);
			String imgType = imageLocation.substring(imageLocation.lastIndexOf(".") + 1).toLowerCase();
			int w = bi.getWidth();
			int h = bi.getHeight();
			Date d = new Date(lastModified);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, imgType, baos);
			byte[] data = baos.toByteArray();
			Populate.insertNewImage(AppContext.getInstance().getConn(), imageName, imgType, w, h, d, data, new String[0]);
			AppContext.getInstance().fireRefreshFromDB();
			display(bi);
		} catch (RuntimeException rte) {
			throw rte;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void display(final BufferedImage bi) {
		this.jScrollPane1.getViewport().removeAll();
		JPanel imgPanel = new JPanel() {
			private static final long serialVersionUID = -2116609065971018541L;

			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				if (noScaleRadioButton.isSelected()) {
					Dimension dim = new Dimension(bi.getWidth(), bi.getHeight());
					setPreferredSize(dim);
					g2d.drawImage(bi, null, 0, 0);
				} else { // Re-size
					AffineTransform tx = new AffineTransform();
					int imgW = bi.getWidth();
					int imgH = bi.getHeight();
					double w = (double)this.getWidth() / (double)imgW;
					double h = (double)this.getHeight() / (double)imgH;
					double ratio = Math.min(w, h);
			//	System.out.println(String.format("Ratio %f", ratio));
					tx.scale(ratio, ratio);
					g2d.drawImage(bi, tx, this);
				}

			}
		};
		this.jScrollPane1.getViewport().add(imgPanel, null);
		imgPanel.repaint();
		this.jScrollPane1.repaint();
	}

	private class DropTargetImplementation extends DropTargetAdapter {
		JPanel panel;

		public DropTargetImplementation(JPanel panel) {
			//		this.metaFrame = metaFrame;
			this.panel = panel;
		}

		public void drop(DropTargetDropEvent e) {
			System.err.println("The DropPanel received the DropEvent");

			// Called when the user finishes or cancels the drag operation.
			Transferable transferable = e.getTransferable();
			try {
				if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					final List data = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
					e.getDropTargetContext().dropComplete(true);
					Thread loader = new Thread(() -> {
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
										setFileToLoad(f.getAbsolutePath());
										AppContext.getInstance().fireSetProgressBar(++current, max);
									} catch (RuntimeException rte) {
										System.out.println("Exception:" + rte.getLocalizedMessage());
										break;
									}
								}
								if (System.getProperty("verbose", "false").equals("true")) System.out.println("Loader is done.");
								AppContext.getInstance().fireActivateProgressBar(false);
							} catch (Exception ex) {
								System.err.println("From loader thread:");
								ex.printStackTrace();
							}
						});
					loader.start();
				} else {
					System.err.println("DROP::That wasn't an image!");
					e.rejectDrop();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				e.rejectDrop();
			} catch (UnsupportedFlavorException ufe) {
				ufe.printStackTrace();
				e.rejectDrop();
			}
		}

		public void dragEnter(DropTargetDragEvent e) {
			// called when the user is dragging and enters our target
			panel.setBackground(Color.GREEN);
		}

		public void dragExit(DropTargetEvent e) {
			// called when the user is dragging and leaves our target
			panel.setBackground(Color.WHITE);
		}

		public void dragOver(DropTargetDragEvent e) {
			// called when the user is dragging and moves over our target
			panel.setBackground(Color.GREEN);
		}
	}
}
