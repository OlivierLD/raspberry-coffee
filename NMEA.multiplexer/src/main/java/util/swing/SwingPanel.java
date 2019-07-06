package util.swing;

import nmea.parser.GeoPos;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class SwingPanel
		extends javax.swing.JPanel {
	private Color pointColor = Color.red;
	private List<GeoPos> positions = null;
	/**
	 * Creates new form SwingPanel
	 */
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

	public void plot(List<GeoPos> positions) {
		this.positions = positions;
		this.repaint();
	}

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
			for (GeoPos pos : positions) {
				minLat = Math.min(minLat, pos.lat);
				maxLat = Math.max(maxLat, pos.lat);
				minLng = Math.min(minLng, pos.lng);
				maxLng = Math.max(maxLng, pos.lng);
			}
			double widthRatio = (double)this.getWidth() / ((maxLng - minLng) * 1.1);
			double heightRatio = (double)this.getWidth() / ((maxLng - minLng) * 1.1);
			final double ratio = Math.min(widthRatio, heightRatio);
			final double _minLng = minLng;
			final double _minLat = minLat;
			// Plot
			positions.stream().forEach(pos -> {
				int xCanvas = (int)Math.round((pos.lng - _minLng) * ratio);
				int yCanvas = this.getHeight() - (int)Math.round((pos.lat - _minLat) * ratio);
				gr.fillOval(xCanvas, yCanvas, 1, 1);
			});
		} else {
			System.out.println(String.format("Size %d x %d", this.getWidth(), this.getHeight()));
		}
	}
}
