package tf.samples;

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Requires the model trained in Python, by create_and_save_model.py.
 */
public class UseTrainedModel {

	private final static String MODEL_LOCATION = "./model/saved_model.pb";

	public static void main(String... args) throws Exception {
//		Path modelPath = Paths.get(UseTrainedModel.class.getResource(MODEL_LOCATION).toURI());
		Path modelPath = Paths.get(MODEL_LOCATION);
		byte[] graph = Files.readAllBytes(modelPath);

		try (Graph g = new Graph()) {
			g.importGraphDef(graph);
			//open session using imported graph
			try (Session sess = new Session(g)) {
				float[][] inputData = {{4, 3, 2, 1}};
				// We have to create tensor to feed it to session,
				// unlike in Python where you just pass Numpy array
				Tensor inputTensor = Tensor.create(inputData, Float.class);
				float[][] output = predict(sess, inputTensor);
				for (int i = 0; i < output[0].length; i++) {
					System.out.println(output[0][i]);//should be 41. 51.5 62.
				}
			}
		}
	}

	private static float[][] predict(Session sess, Tensor inputTensor) {
		Tensor result = sess.runner()
				.feed("input", inputTensor)
				.fetch("not_activated_output").run().get(0);
		float[][] outputBuffer = new float[1][3];
		result.copyTo(outputBuffer);
		return outputBuffer;
	}
}
