package oliv.opencv;

import cv.utils.Utils;
import oliv.opencv.swing.SwingFrame;
import oliv.opencv.swing.SwingFrameWithWidgets;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Oliv did it.
 * Path detection on frames returned by the Camera
 * Display images in a Swing JPanel
 */
public class OpenCVContinuousPathDetection {

	private ScheduledExecutorService timer;
	private VideoCapture camera = null;
	private boolean cameraActive = false;
	private static int cameraId = 0;


	private static SwingFrame swingFrame = null;

	private final static int DEFAULT_IMAGE_WIDTH =  600;
	private final static int DEFAULT_IMAGE_HEIGHT = 600;

	private static double getDir(double x, double y) {
		double direction = Math.toDegrees(Math.atan2(x, y));
		while (direction < 0) {
			direction += 360;
		}
		return direction;
	}

	public OpenCVContinuousPathDetection() {
		swingFrame = new SwingFrame();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - swingFrame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - swingFrame.getHeight()) / 2);
		swingFrame.setLocation(x, y);
		swingFrame.setVisible(true);

		swingFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				stopAcquisition();
				System.out.println("Closing!");
				System.exit(0);
			}
		});
		startCamera();
	}

	private final static double VIDEO_WIDTH = (double)DEFAULT_IMAGE_WIDTH;
	private final static double VIDEO_HEIGHT = (double)DEFAULT_IMAGE_HEIGHT;

	protected void startCamera() {
		this.camera = new VideoCapture(); // cameraId, Videoio.CAP_ANY); // With a cameraId: also opens the camera
		System.out.println(String.format("Camera opened: %s", this.camera.isOpened()));

		if (!this.cameraActive) {

			this.camera.open(cameraId);

			// Not able to set the frame size on the internal camera of the Mac (USB WebCam OK)... Works from Python though.
			boolean wSet = this.camera.set(Videoio.CAP_PROP_FRAME_WIDTH, VIDEO_WIDTH);
			boolean hSet = this.camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, VIDEO_HEIGHT);
			System.out.println(String.format("Setting video frame size to %.02f x %.02f => W set: %s, H set: %s", VIDEO_WIDTH, VIDEO_HEIGHT, wSet, hSet));
			System.out.println(String.format(">> Capture size WxH: %.02f x %.02f", this.camera.get(Videoio.CAP_PROP_FRAME_WIDTH), this.camera.get(Videoio.CAP_PROP_FRAME_HEIGHT)));

			if (this.camera.isOpened()) {
				this.cameraActive = true;
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = () -> {
					process(grabFrame());
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
			} else {
				System.err.println("Impossible to open the camera connection...");
			}
		} else {
			this.cameraActive = false;
			this.stopAcquisition();
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
	private Mat grabFrame() {
		Mat frame = new Mat();
		if (this.camera.isOpened()) {
			try {
				this.camera.read(frame);
			} catch (Exception e) {
				System.err.println("Exception during camera capture: " + e);
			}
		}
//		System.out.println(String.format("Read image from camera %d x %d", frame.width(), frame.height()));
		return frame;
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Exception when stopping the frame camera, will try to release the camera now... " + e);
			}
		}

		if (this.camera.isOpened()) {
			this.camera.release();
		}
	}

	public static void process(Mat frame) {

		Mat original = frame;
		Mat lastMat = frame;

		// convert the image in gray scale
		Mat gray = new Mat();
		Imgproc.cvtColor(lastMat, gray, Imgproc.COLOR_BGR2GRAY);

//		swingFrame.plot(Utils.mat2AWTImage(gray), "Gray");
		lastMat = gray;

		// Gaussian blur
		double sigmaX = 0d;
		final Size kSize = new Size(31, 31);
		Mat blurred = new Mat();
		Imgproc.GaussianBlur(lastMat, blurred, kSize, sigmaX);

//		swingFrame.plot(Utils.mat2AWTImage(blurred), "Blurred");
		lastMat = blurred;

		// threshold
		Mat threshed = new Mat();
		Imgproc.threshold(lastMat, threshed, 127, 255, 0);

//		swingFrame.plot(Utils.mat2AWTImage(threshed), "Threshed");
		lastMat = threshed;

		List<List<Integer>> tiles = new ArrayList<>();
		// Path detection?
		for (int h = lastMat.height() - 1; h >= 0; h -= 40) { // Start from the bottom of the image
			int firstBlack = -1;
			int lastBlack = -1;
			for (int w = 0; w < lastMat.width(); w++) {
				double[] pix = lastMat.get(h, w);
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
		String frameTitle = "";
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
				Imgproc.line(original, new Point(bottomLeft, bottom), new Point(bottomRight, bottom), green, 2);
				Imgproc.line(original, new Point(bottomRight, bottom), new Point(topRight, top), green, 2);
				Imgproc.line(original, new Point(topRight, top), new Point(topLeft, top), green, 2);
				Imgproc.line(original, new Point(topLeft, top), new Point(bottomLeft, bottom), green, 2);
				// a dot in the middle
				int centerY = (bottom + top) / 2;
				int centerX = (((bottomLeft + bottomRight) / 2) + ((topLeft + topRight) / 2)) / 2;
				Point center = new Point(centerX, centerY);
				Imgproc.circle(original, center, 10, red, -1);
				if (previousCenter != null) {
					// calculate course
					String course = String.format("Course %.02f\272    ", getDir(centerX - previousCenter.x, previousCenter.y - centerY));
					System.out.println(course);
					frameTitle = course;
//					Imgproc.putText(original, course, new Point(10, 30), Imgproc.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255, 0, 0));
				}
				previousCenter = center;
			}
			previousStep = step;
		}
//		Imgproc.putText(original, "Path Detected", new Point(10, 30), Imgproc.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255, 0, 0));

		swingFrame.plot(Utils.mat2AWTImage(original), frameTitle);
	}

	public static void main(String[] args) {
		// load the OpenCV native library
		System.out.println("Loading lib " + Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		new OpenCVContinuousPathDetection();

		System.out.println("On its way!");
	}
}
