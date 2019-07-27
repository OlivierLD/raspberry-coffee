package oliv.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/*
 * Detects faces in an image, draws boxes around them, and writes the results
 * to "faceDetection.png".
*/
class FaceDetectDemo {

	public void run() {
		System.out.println("\nRunning FaceDetectDemo");

		// Create a face detector from the cascade file in the resources directory.
		String cascadeResource = getClass().getResource("/lbpcascade_frontalface.xml").getPath(); // It comes in the opencv repo.
		System.out.println(String.format("Looking for the resource file %s", cascadeResource));
		if (cascadeResource == null) {
			throw new RuntimeException("lbpcascade_frontalface.xml not found where expected");
		}
		CascadeClassifier faceDetector = new CascadeClassifier(cascadeResource);
		String imageToProcess =
//														getClass().getResource("/mec.jpeg").getPath();
//														getClass().getResource("/meuf.png").getPath();
														getClass().getResource("/several.jpeg").getPath();
		if (imageToProcess == null) {
			throw new RuntimeException("Image to process not found where expected");
		}
		Mat image = Imgcodecs.imread(imageToProcess);

		// Detect faces in the image.
		// MatOfRect is a special container class for Rect.
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		faceDetections.toList().stream().forEach(rect -> {
			System.out.println(String.format("Rect WxH, (x,y) %d x %d, (%d, %d)", rect.width, rect.height, rect.x, rect.y));
		});

		System.out.println(String.format(">> Detected %s face(s)", faceDetections.toArray().length));

		// Draw a bounding box around each face.
		for (Rect rect : faceDetections.toArray()) {
			Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 3);
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
