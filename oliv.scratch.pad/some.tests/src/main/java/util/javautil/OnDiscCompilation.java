package util.javautil;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.net.URL;

public class OnDiscCompilation {

	private final static String SRC_DIR = "/Users/olediouris/repos/api-definitions/build/swagger-generated/src/gen/java";
	private final static String CLASS_DIR = "./classes";
	private final static String FILE_TO_COMPILE = SRC_DIR + "/com/medallia/v4tests/empdept/model/DeptsModel.java";
	private final static String CLASS_NAME = "com.medallia.v4tests.empdept.model.DeptsModel";
	private final static String CLASSPATH = "/Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home/lib/tools.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/io.swagger/swagger-codegen-cli/2.1.6/1b67736fdc00d4484b46656ed9f7f9ca8bca2fd0/swagger-codegen-cli-2.1.6.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/javax.ws.rs/javax.ws.rs-api/2.0.1/104e9c2b5583cfcfeac0402316221648d6d8ea6b/javax.ws.rs-api-2.0.1.jar:/Users/olediouris/repos/api-definitions/common-definitions/build/libs/common-definitions-1.0.0.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/junit/junit/4.11/4e031bb61df09069aeb2bffb4019e7a5034a4ee0/junit-4.11.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/com.bmsi/gnudiff/1.7/a46aad15cad3de6815c33c14e02f3f5f9fe70880/gnudiff-1.7.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/joda-time/joda-time/2.9.3/9e46be514a4ed60bcfbaaba88a3c668cf30476ab/joda-time-2.9.3.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-annotations/2.7.4/84b2f8e53bd8a077d402bc99d9bce816c2b2d0f9/jackson-annotations-2.7.4.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-databind/2.7.4/1e9c6f3659644aeac84872c3b62d8e363bf4c96d/jackson-databind-2.7.4.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar:/Users/olediouris/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-core/2.7.4/b8f38a249116b66d804a5ca2b14a3459b7913a94/jackson-core-2.7.4.jar";

	private static JavaCompiler compiler = null;
	private static DiagnosticCollector<JavaFileObject> diagnostics = null;
	private static CustomClassLoader customClassLoader = null;

	public static void main(String[] args) throws Exception {
		if (compiler == null)
			compiler = ToolProvider.getSystemJavaCompiler();
		if (diagnostics == null)
			diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

		// Call compiler task
		List<String> options = new ArrayList<String>();
		options.add("-s");
		options.add(SRC_DIR);

		String classDir = CLASS_DIR;
		if (classDir != null) {
			options.add("-d");
			options.add(classDir);
			File clsDir = new File(classDir);
			if (!clsDir.exists())
				clsDir.mkdirs();
		}
		options.add("-cp");
		options.add(CLASSPATH);

		File[] startFrom = null;
		File javaRoot = new File(SRC_DIR + "/com/medallia/v4tests/empdept/model/");
		startFrom = javaRoot.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".java");
			}
		});

//      Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(FILE_TO_COMPILE));
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(startFrom);

		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
		boolean success = task.call();
		fileManager.close();
		System.out.println("Success:" + success);
		for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
			System.err.println("Code    :" + diagnostic.getCode());
			System.err.println("Kind    :" + diagnostic.getKind());
			System.err.println("Position:" + diagnostic.getPosition());
			System.err.println("Start   :" + diagnostic.getStartPosition());
			System.err.println("End     :" + diagnostic.getEndPosition());
			System.err.println("Source  :" + diagnostic.getSource());
			System.err.println("Message :" + diagnostic.getMessage(null));
			System.err.println("=======================================");
		}

		if (!success) {
			String compileError = "";
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
				ps.format("Error on line %d in:\n[%s]\n", diagnostic.getLineNumber(), diagnostic.getMessage(Locale.ENGLISH));
				compileError += baos.toString();
				baos.reset();
			}
			throw new RuntimeException(compileError);
		} else {
			// Loading the generated classes?

			// Add the CLASS_DIR to the classpath
			URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Class sysclass = URLClassLoader.class;
			try {
				Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
				method.setAccessible(true);
				method.invoke(sysloader, new Object[]{new File(CLASS_DIR).toURI().toURL()});
			} catch (Throwable t) {
				t.printStackTrace();
				throw new IOException("Error, could not add URL to system classloader");
			}

			if (customClassLoader == null)
				customClassLoader = new CustomClassLoader(sysloader);

			File classFile = null;
			if (classDir != null)
				classFile = new File(classDir + File.separator + CLASS_NAME.replaceAll("\\.", File.separator) + ".class");
			if (classDir != null && classFile.exists()) {
				try {
					long length = classFile.length();
					InputStream cis = new FileInputStream(classFile);
					if (length > Integer.MAX_VALUE)
						throw new RuntimeException("Class file too large...");
					else {
						// Read bytes from the class
						byte[] bytes = new byte[(int) length];
						int offset = 0, numread = 0;
						while (offset < bytes.length && (numread = cis.read(bytes, offset, bytes.length - offset)) >= 0)
							offset += numread;
						cis.close();
						Class newClass = customClassLoader.getClassFromBytes(CLASS_NAME, bytes);
						System.out.println();
						Object obj = newClass.newInstance();
						// Etc...
						System.out.println(String.format("Object is a [%s]", obj.getClass().getName()));
						for (Field field : newClass.getDeclaredFields()) {
							System.out.println(String.format("%s: type %s", field.getName(), field.getType()));
						}
					}
				} catch (Exception ex) {
					throw ex;
				}
			}

		}

	}

	public static class CustomClassLoader extends ClassLoader {
		public CustomClassLoader() {
			super();
		}

		public CustomClassLoader(ClassLoader parent) {
			super(parent);
		}

		public Class getClassFromBytes(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}
	}
}
