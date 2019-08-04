package tf.samples;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * Expects a TensorFlow model (pb or pbtxt)
 */
public class Predictor {
	public static void main(String... args) throws IOException {
		String javaLibPath = System.getProperty("java.library.path");
		System.out.println(String.format("\n\t>> Running from %s, Java Lib Path: %s\n", System.getProperty("user.dir"), javaLibPath));
		// good idea to print the version number, 1.2.0 as of this writing
		System.out.println(TensorFlow.version());
		final int NUM_PREDICTIONS = 1;

		// load the model Bundle
//		String modelLocation = "/tmp/model";
		String modelLocation = "./model";
		try (SavedModelBundle b = SavedModelBundle.load(modelLocation, "serve")) { // Auto closeable

			// create the session from the Bundle
			Session sess = b.session();
			// create an input Tensor, value = 2.0f
			Tensor x = Tensor.create(
					new long[] { NUM_PREDICTIONS },
					FloatBuffer.wrap(new float[] { 2.0f })
			);

			// run the model and get the result, 4.0f.
			float[] y = sess.runner()
					.feed("x", x)
					.fetch("y")
					.run()
					.get(0)
					.copyTo(new float[NUM_PREDICTIONS]);

			// print out the result.
			System.out.println(y[0]);
		}
	}
}
