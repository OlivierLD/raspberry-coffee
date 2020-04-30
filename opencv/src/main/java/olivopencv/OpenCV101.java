package olivopencv;

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

/**
 * Oliv did it.
 * Basic OpenCV image manipulations, written to the disk.
 * No display, no Swing, or whatever.
 *
 * Uses assertions.
 */
public class OpenCV101 {

	private final static String IMAGE_SOURCE_PATH = "." + File.separator + "images";
	private final static String IMAGE_01 = IMAGE_SOURCE_PATH + File.separator + "path.jpg";
	private final static String IMAGE_02 = IMAGE_SOURCE_PATH + File.separator + "path.2.png";

	private static double getDir(double x, double y) {
		double direction = Math.toDegrees(Math.atan2(x, y));
		while (direction < 0) {
			direction += 360;
		}
		return direction;
	}

	public static void process(String imagePath, String outputSuffix) {
		Mat image = Imgcodecs.imread(imagePath);
		System.out.println(String.format("Original image: w %d, h %d, channels %d", image.width(), image.height(), image.channels()));
		// convert the image in gray scale
		Mat gray = new Mat();
		Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
		// write the new image on disk
		Imgcodecs.imwrite(IMAGE_SOURCE_PATH + File.separator + String.format("gray.%s.jpg", outputSuffix), gray);

		// Gaussian blur
		double sigmaX = 0d;
		final Size kSize = new Size(31, 31);
		Mat blurred = new Mat();
		Imgproc.GaussianBlur(gray, blurred, kSize, sigmaX);
		Imgcodecs.imwrite(IMAGE_SOURCE_PATH + File.separator + String.format("blurred.%s.jpg", outputSuffix), blurred);

		// threshold
		Mat threshed = new Mat();
		Imgproc.threshold(blurred, threshed, 127, 255, 0);
		Imgcodecs.imwrite(IMAGE_SOURCE_PATH + File.separator + String.format("threshed.%s.jpg", outputSuffix), threshed);

		// Contours
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(threshed, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(gray, contours, -1, new Scalar(0, 255, 0), 2);
		Imgcodecs.imwrite(IMAGE_SOURCE_PATH + File.separator + String.format("contours.%s.jpg", outputSuffix), gray);

		// Canny edges
		Mat canny = new Mat();
		Imgproc.Canny(image, canny, 10, 100);
		Imgcodecs.imwrite(IMAGE_SOURCE_PATH + File.separator + String.format("canny.%s.jpg", outputSuffix), canny);

		List<List<Integer>> tiles = new ArrayList<>();
		// Path detection?
		for (int h=threshed.height() - 1; h>=0; h-= 40) { // Start from the bottom of the image
			int firstBlack = -1;
			int lastBlack = -1;
			for (int w=0; w<threshed.width(); w++) {
				double[] pix = threshed.get(h, w);
//				System.out.println(String.format("Pixel: %d element(s), %f", pix.length, pix[0]));
				assert(pix.length == 1); // threshed is a B&W picture
				if (pix[0] == 0) { // black
					if (firstBlack == -1) {
						firstBlack = w;
					} else {
						lastBlack = w;
					}
				}
			}
			Integer[] array = new Integer[] { h, firstBlack, lastBlack };
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
				assert(previousStep.size() == 3);
				int bottom = previousStep.get(0);
				int bottomLeft = previousStep.get(1);
				int bottomRight = previousStep.get(2);
				assert(step.size() == 3);
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
		Imgcodecs.imwrite(IMAGE_SOURCE_PATH + File.separator + String.format("found.path.%s.jpg", outputSuffix), image);

		System.out.println("Done!");
	}

	public static void main(String[] args) {
		// load the OpenCV native library
		System.out.println("Loading " + Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		System.out.println(String.format("Using OpenCV version %s (%d.%d.%d)", Core.getVersionString(), Core.getVersionMajor(), Core.getVersionMinor(), Core.getVersionRevision()));

		System.out.println("Image 1...");
		long before = System.currentTimeMillis();
		try {
			process(IMAGE_01, "01");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		long after = System.currentTimeMillis();
		System.out.println(String.format("Done in %d ms", (after - before)));

		before = after;
		System.out.println("Image 2...");
		try {
			process(IMAGE_02, "02");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		after = System.currentTimeMillis();
		System.out.println(String.format("Done in %d ms", (after - before)));

		System.out.println("Bye!");
	}
}
