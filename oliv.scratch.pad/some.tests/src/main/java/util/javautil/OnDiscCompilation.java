package util.javautil;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.net.URL;

public class OnDiscCompilation {

	private final static String SRC_DIR = "./src/main/java";
	private final static String CLASS_DIR = "./classes";
	private final static String FILE_TO_COMPILE = SRC_DIR + "/scripting/Executor.java";
	private final static String CLASS_NAME = "scripting.Executor";
	private final static String CLASSPATH = ""; // Add required resources here

	private static JavaCompiler compiler = null;
	private static DiagnosticCollector<JavaFileObject> diagnostics = null;
	private static CustomClassLoader customClassLoader = null;

	public static void main(String... args) throws Exception {
		if (compiler == null) {
			compiler = ToolProvider.getSystemJavaCompiler();
		}
		if (diagnostics == null) {
			diagnostics = new DiagnosticCollector<JavaFileObject>();
		}
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
			if (!clsDir.exists()) {
				clsDir.mkdirs();
			}
		}
		options.add("-cp");
		options.add(CLASSPATH);

		File[] startFrom = null;
		File javaRoot = new File(SRC_DIR + "/scripting/");
		startFrom = javaRoot.listFiles(f -> f.getAbsolutePath().endsWith(".java"));

//  Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(FILE_TO_COMPILE));
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

			if (customClassLoader == null) {
				customClassLoader = new CustomClassLoader(sysloader);
			}

			File classFile = null;
			if (classDir != null) {
				classFile = new File(classDir + File.separator + CLASS_NAME.replaceAll("\\.", File.separator) + ".class");
			}
			if (classDir != null && classFile.exists()) {
				try {
					long length = classFile.length();
					InputStream cis = new FileInputStream(classFile);
					if (length > Integer.MAX_VALUE) {
						throw new RuntimeException("Class file too large...");
					} else {
						// Read bytes from the class
						byte[] bytes = new byte[(int) length];
						int offset = 0, numread = 0;
						while (offset < bytes.length && (numread = cis.read(bytes, offset, bytes.length - offset)) >= 0) {
							offset += numread;
						}
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
