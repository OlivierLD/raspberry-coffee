package it.polito.elite.teaching.cv.dummy;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * A simple class that demonstrates/tests the usage of the OpenCV library in
 * Java. It prints a 3x3 identity matrix and then converts a given image in gray
 * scale.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @since 2013-10-20
 */
public class HelloCV {
	public static void main(String[] args) {
		// load the OpenCV native library
		System.out.println("Loading " + Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// create and print on screen a 3x3 identity matrix
		System.out.println("Create a 3x3 identity matrix...");
		Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		System.out.println("mat = \n" + mat.dump());

		// prepare to convert a RGB image in gray scale
//		String location = "resources/Poli.jpg";
		String location = "src/main/resources/Poli.jpg";
		System.out.print("Convert the image at " + location + " in gray scale... ");
		// get the jpeg image from the internal resource folder
		Mat image = Imgcodecs.imread(location);
		// convert the image in gray scale
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
		// write the new image on disk
		Imgcodecs.imwrite("src/main/resources/Poli-gray.jpg", image);
		System.out.println("Done!");
	}
}
