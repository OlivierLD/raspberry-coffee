package tf.samples;

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

public class HelloTF {
	public static void main(String[] args) throws Exception {

		String javaLibPath = System.getProperty("java.library.path");
		System.out.println(String.format("\n\t>> Running from %s, Java Lib Path: %s\n", System.getProperty("user.dir"), javaLibPath));

		try (Graph g = new Graph()) {
			final String value = "Hello from TF " + TensorFlow.version();

			// Construct the computation graph with a single operation, a constant
			// named "MyConst" with a value "value".
			try (Tensor t = Tensor.create(value.getBytes("UTF-8"))) {
				// The Java API doesn't yet include convenience functions for adding operations.
				g.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
			}

			// Execute the "MyConst" operation in a Session.
			try (Session s = new Session(g);
			     Tensor output = s.runner().fetch("MyConst").run().get(0)) { // Auto-closable
				System.out.println("Session:");
				System.out.println(new String(output.bytesValue(), "UTF-8"));
			}
		}
		System.out.println("Done.");
	}
}
