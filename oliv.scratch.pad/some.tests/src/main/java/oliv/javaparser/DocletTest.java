package oliv.javaparser;

import com.sun.javadoc.*;
import com.sun.tools.javadoc.Main;

public class DocletTest {
	private final static String FILE_PATH = "./src/main/java/oliv/tutorial/TestOne.java";
	// Use javadoc -h for a list of the available arguments.
	private final static String PRIVATE_FLAG = "-private";


	public static void main(String... args) {
		System.out.println(String.format("Looking for %s from %s", FILE_PATH, new java.io.File(".").getAbsolutePath()));
		Main.execute("", Analyzer.class.getName(), new String[]{FILE_PATH, PRIVATE_FLAG});
	}

	public static class Analyzer extends Doclet {

		public static boolean start(RootDoc root) {
			for (ClassDoc classDoc : root.classes()) {
				System.out.println("Class: " + classDoc.qualifiedName());

				for (MethodDoc methodDoc : classDoc.methods()) {
					System.out.println("  Method: " + methodDoc.returnType() + " " + methodDoc.name() + methodDoc.signature());
				}

				for (ClassDoc cd : classDoc.interfaces()) {
					System.out.println("  Interface: " + cd.toString());
				}

				for (FieldDoc fd : classDoc.fields()) {
					System.out.println(String.format("  Field: %s (%s)", fd.toString(), (fd.isPublic() ? "public" : (fd.isProtected() ? "protected" : "private"))));
				}
			}
			return false;
		}
	}
}
