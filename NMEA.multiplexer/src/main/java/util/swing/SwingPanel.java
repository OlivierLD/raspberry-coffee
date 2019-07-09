package util.swing;

import nmea.parser.GeoPos;
import util.LogAnalyzer;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SwingPanel
		extends javax.swing.JPanel {
	private Color pointColor = Color.red;
	private List<LogAnalyzer.DatedPosition> positions = null;
	private SwingPanel instance = this;

	public SwingPanel() {
		this.clear();
	}

	public void setPointColor(Color c) {
		this.pointColor = c;
	}

	public void clear() {
		this.positions = null;
		this.repaint();
	}

	public void plot(List<LogAnalyzer.DatedPosition> pos, boolean progressing) {

		int from = (progressing ? 0 : pos.size() - 1);
		Thread plotter = new Thread(() -> {
			try {
				for (int i = from; i < pos.size(); i++) {
					final List<LogAnalyzer.DatedPosition> toPlot = IntStream.range(0, i + 1)
							.mapToObj(x -> pos.get(x))
							.collect(Collectors.toList());
					SwingUtilities.invokeAndWait(() -> {
						instance.positions = toPlot;
						instance.repaint();
					});
				}
				System.out.println("Repaint, done!"); // TODO Enable plot button
			} catch (InterruptedException | InvocationTargetException ie) {
				ie.printStackTrace();
			}
		});
		plotter.start();
	}

	private final static boolean DEBUG = false;

	@Override
	public void paintComponent(Graphics gr) {
		gr.setColor(Color.white);
		gr.fillRect(0, 0, this.getWidth(), this.getHeight());

		if (this.positions != null) {
			gr.setColor(pointColor);
			// Draw point and / or path
			// 1 - get min and max
			double minLat = Double.MAX_VALUE,
					minLng = Double.MAX_VALUE,
					maxLat = -minLat,
					maxLng = -minLng;
			for (LogAnalyzer.DatedPosition pos : positions) {
				minLat = Math.min(minLat, pos.getPosition().lat);
				maxLat = Math.max(maxLat, pos.getPosition().lat);
				minLng = Math.min(minLng, pos.getPosition().lng);
				maxLng = Math.max(maxLng, pos.getPosition().lng);
			}

			double widthRatio = (double) this.getWidth() / ((maxLng - minLng) * 1.1);
			double heightRatio = (double) this.getHeight() / ((maxLat - minLat) * 1.1);
			final double ratio = Math.min(widthRatio, heightRatio);
			if (DEBUG) {
				System.out.println(String.format("TopLeft: %s, Bottom-Right: %s", new GeoPos(maxLat, minLng), new GeoPos(minLat, maxLng)));
				System.out.println(String.format("Ratio: W: %f, H: %f, r:%f, deltaLat: %f, deltaLng: %f", widthRatio, heightRatio, ratio, (maxLat - minLat), (maxLng - minLng)));
			}
			final double _minLng = minLng;
			final double _minLat = minLat;
			// Plot
			final AtomicInteger idx = new AtomicInteger(0);
			positions.stream().forEach(pos -> {
				int xCanvas = (this.getWidth() / 2) + (int) Math.round((pos.getPosition().lng - _minLng) * (ratio / 2));
				int yCanvas = (this.getHeight() / 2) - (int) Math.round((pos.getPosition().lat - _minLat) * (ratio / 2));
				if (DEBUG) {
					System.out.println(String.format("\tPlotting (%d) %s => x: %d, y=%d (canvas %d x %d)", idx.get(), pos.getPosition().toString(), xCanvas, yCanvas, this.getWidth(), this.getHeight()));
//					if (idx.get() < 10) {
//						gr.setColor(Color.blue);
//					} else {
//						gr.setColor(pointColor);
//					}
				}
				gr.fillOval(xCanvas - 1, yCanvas - 1, 3, 3);
				idx.set(idx.get() + 1);
			});
		} else {
			System.out.println(String.format("Size %d x %d", this.getWidth(), this.getHeight()));
		}
	}
}
