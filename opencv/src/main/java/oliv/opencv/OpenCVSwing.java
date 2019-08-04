package oliv.opencv;

import cv.utils.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oliv.opencv.swing.SwingFrame;

/**
 * Oliv did it.
 * Basic OpenCV image manipulations, with path detection.
 * Display images in Swing
 *
 * Uses assertions.
 */
public class OpenCVSwing {

	private final static String IMAGE_SOURCE_PATH = "." + File.separator + "images";
	private final static String IMAGE_01 = IMAGE_SOURCE_PATH + File.separator + "path.jpg";
	private final static String IMAGE_02 = IMAGE_SOURCE_PATH + File.separator + "path.2.png";

	private final static long WAIT = 10_000L;

	private static SwingFrame swingFrame = null;

	private static double getDir(double x, double y) {
		double direction = Math.toDegrees(Math.atan2(x, y));
		while (direction < 0) {
			direction += 360;
		}
		return direction;
	}

	public static void process(String imagePath) {

		swingFrame = new SwingFrame();
		swingFrame.setVisible(true);

		while (true) {
			Mat image = Imgcodecs.imread(imagePath);
			System.out.println(String.format("Original image: w %d, h %d, channels %d", image.width(), image.height(), image.channels()));

			swingFrame.plot(Utils.mat2AWTImage(image), "Original");

			try {
				Thread.sleep(WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			// convert the image in gray scale
			Mat gray = new Mat();
			Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

			swingFrame.plot(Utils.mat2AWTImage(gray), "Gray");

			try {
				Thread.sleep(WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			// Gaussian blur
			double sigmaX = 0d;
			final Size kSize = new Size(31, 31);
			Mat blurred = new Mat();
			Imgproc.GaussianBlur(gray, blurred, kSize, sigmaX);

			swingFrame.plot(Utils.mat2AWTImage(blurred), "Blurred");

			try {
				Thread.sleep(WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			// threshold
			Mat threshed = new Mat();
			Imgproc.threshold(blurred, threshed, 127, 255, 0);

			swingFrame.plot(Utils.mat2AWTImage(threshed), "Threshed");

			try {
				Thread.sleep(WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			// Contours
			List<MatOfPoint> contours = new ArrayList<>();
			Imgproc.findContours(threshed, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
			Imgproc.drawContours(gray, contours, -1, new Scalar(0, 255, 0), 2);

			swingFrame.plot(Utils.mat2AWTImage(gray), "Contours");

			try {
				Thread.sleep(WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			// Canny edges
			Mat canny = new Mat();
			Imgproc.Canny(image, canny, 10, 100);

			swingFrame.plot(Utils.mat2AWTImage(canny), "Canny Edges");

			try {
				Thread.sleep(WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			List<List<Integer>> tiles = new ArrayList<>();
			// Path detection?
			for (int h = threshed.height() - 1; h >= 0; h -= 40) { // Start from the bottom of the image
				int firstBlack = -1;
				int lastBlack = -1;
				for (int w = 0; w < threshed.width(); w++) {
					double[] pix = threshed.get(h, w);
//				System.out.println(String.format("Pixel: %d element(s), %f", pix.length, pix[0]));
					assert (pix.length == 1); // threshed is a B&W picture
					if (pix[0] == 0) { // black
						if (firstBlack == -1) {
							firstBlack = w;
						} else {
							lastBlack = w;
						}
					}
				}
				Integer[] array = new Integer[]{h, firstBlack, lastBlack};
				List<Integer> tuple = Arrays.asList(array);
				tiles.add(tuple);
			}
			System.out.println(String.format("%d tiles", tiles.size()));
			Scalar green = new Scalar(0, 255, 0);
			Scalar red = new Scalar(0, 0, 255);
			List<Integer> previousStep = null;
			Point previousCenter = null;
			for (List<Integer> step : tiles) {
				if (previousStep != null) {
					assert (previousStep.size() == 3);
					int bottom = previousStep.get(0);
					int bottomLeft = previousStep.get(1);
					int bottomRight = previousStep.get(2);
					assert (step.size() == 3);
					int top = step.get(0);
					int topLeft = step.get(1);
					int topRight = step.get(2);
					// draw a box
					Imgproc.line(image, new Point(bottomLeft, bottom), new Point(bottomRight, bottom), green, 2);
					Imgproc.line(image, new Point(bottomRight, bottom), new Point(topRight, top), green, 2);
					Imgproc.line(image, new Point(topRight, top), new Point(topLeft, top), green, 2);
					Imgproc.line(image, new Point(topLeft, top), new Point(bottomLeft, bottom), green, 2);
					// a dot in the middle
					int centerY = (bottom + top) / 2;
					int centerX = (((bottomLeft + bottomRight) / 2) + ((topLeft + topRight) / 2)) / 2;
					Point center = new Point(centerX, centerY);
					Imgproc.circle(image, center, 10, red, -1);
					if (previousCenter != null) {
						// calculate course
						System.out.println(String.format("Course %.02f\272", getDir(centerX - previousCenter.x, previousCenter.y - centerY)));
					}
					previousCenter = center;
				}
				previousStep = step;
			}
			Imgproc.putText(image, "Path Detected", new Point(10, 30), Imgproc.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255, 0, 0));

			swingFrame.plot(Utils.mat2AWTImage(image), "Path Detection");

			try {
				Thread.sleep(WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

//		System.out.println("Done!");
	}

	public static void main(String[] args) {
		// load the OpenCV native library
		System.out.println("Loading " + Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

//		process(IMAGE_01);
		process(IMAGE_02);

		System.out.println("Bye!");
	}
}
