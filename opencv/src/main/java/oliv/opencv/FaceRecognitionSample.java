package oliv.opencv;

import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import static org.opencv.imgproc.Imgproc.rectangle;

//
// Detects faces in an image, draws boxes around them, and writes the results
// to "faceDetection.png".
//
class FaceDetectDemo {

	public void run() {
		System.out.println("\nRunning FaceDetectDemo");

		// Create a face detector from the cascade file in the resources directory.
		String cascadeResource = getClass().getResource("/lbpcascade_frontalface.xml").getPath(); // It comes in the opencv repo.
		System.out.println(String.format("Looking for the resource file %s", cascadeResource));
		CascadeClassifier faceDetector = new CascadeClassifier(cascadeResource);
		Mat image = Imgcodecs.imread(getClass().getResource("/meuf.png").getPath());

		// Detect faces in the image.
		// MatOfRect is a special container class for Rect.
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);

		System.out.println(String.format(">> Detected %s face(s)", faceDetections.toArray().length));

		// Draw a bounding box around each face.
		for (Rect rect : faceDetections.toArray()) {
			rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		}

		// Save the visualized detection.
		String filename = "faceDetection.png";
		System.out.println(String.format("Writing %s, open it.", filename));
		Imgcodecs.imwrite(filename, image);
	}

}

public class FaceRecognitionSample {
	public static void main(String[] args) {
		System.out.println("Face Detect, OpenCV");

		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new FaceDetectDemo().run();
	}
}
