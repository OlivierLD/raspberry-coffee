package utils.samples;

import utils.DampingService;

import java.util.List;

public class DampingSample {

	public static class DegreeAngle {

		private double cosinus;
		private double sinus;

		public DegreeAngle(double sin, double cos) {
			this.cosinus = cos;
			this.sinus = sin;
		}

		public double getAngleInDegrees() {
			double deg;
			if (this.cosinus != 0) {
				deg = Math.toDegrees(Math.acos(this.cosinus));
				if (this.sinus < 0) {
					deg = -deg;
				}
			} else if (this.sinus != 0) {
				deg = Math.toDegrees(Math.asin(this.sinus));
			} else {
				deg = 0;
			}
			return deg;
		}
	}

	public static class SmoothableDegreeAngle implements DampingService.Smoothable<DegreeAngle> {

		private final DegreeAngle degreeAngle;

		public SmoothableDegreeAngle() {
			this(new DegreeAngle(0, 0));
		}
		public SmoothableDegreeAngle(DegreeAngle deg) {
			this.degreeAngle = deg;
		}


		@Override
		public DegreeAngle get() {
			return this.degreeAngle;
		}

		@Override
		public void accumulate(DegreeAngle elmt) {
			degreeAngle.sinus += elmt.sinus;
			degreeAngle.cosinus += elmt.cosinus;
		}

		@Override
		public DegreeAngle smooth(List<DegreeAngle> buffer) {
			final SmoothableDegreeAngle smoothed = new SmoothableDegreeAngle(new DegreeAngle(0, 0));
			buffer.forEach(smoothed::accumulate);
			DegreeAngle degreeeAngle = smoothed.get();
			degreeeAngle.sinus /= buffer.size();
			degreeeAngle.cosinus /= buffer.size();
			return degreeeAngle;
		}
	}

	public static class SmoothableDouble implements DampingService.Smoothable<Double> {

		private Double value;

		public SmoothableDouble() {
			this(0d);
		}
		public SmoothableDouble(Double deg) {
			this.value = deg;
		}


		@Override
		public Double get() {
			return this.value;
		}

		@Override
		public void accumulate(Double elmt) {
			value += elmt;
		}

		@Override
		public Double smooth(List<Double> buffer) {
			final SmoothableDouble smoothed = new SmoothableDouble(0d);
			buffer.forEach(smoothed::accumulate);
			Double newVal = smoothed.get();
			newVal /= buffer.size();
			return newVal;
		}
	}

	// Example
	public static void main(String... args) {
		double[] degrees = { 0, 355, 10, 359, 2, 10, 8 };
		// Regular average, on the angle value
		{
			DampingService<Double> service = new DampingService<>(10);
			for (double deg : degrees) {
				service.append(new SmoothableDouble(deg));
			}
			Double smooth = service.smooth(new SmoothableDouble(0d));
			System.out.printf("Smoothed in doubles %f\n", smooth);
			System.out.println("If the values are degrees (from 0 to 360), then this value is wrong.");
		}
		// Improved average, that works.
		{
			DampingService<DegreeAngle> service = new DampingService<>(10);
			for (double deg : degrees) {
				DegreeAngle degreeAngle = new DegreeAngle(Math.sin(Math.toRadians(deg)), Math.cos(Math.toRadians(deg)));
				service.append(new SmoothableDegreeAngle(degreeAngle));
			}
			DegreeAngle smooth = service.smooth(new SmoothableDegreeAngle());
			System.out.printf("Smoothed in degrees %f\n", smooth.getAngleInDegrees());
			System.out.println("This one is correct.");
		}
	}
}
