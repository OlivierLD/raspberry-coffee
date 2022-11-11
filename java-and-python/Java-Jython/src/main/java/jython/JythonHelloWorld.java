package jython;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class JythonHelloWorld {

    /*
     * Warning; Python3 not supported
     */
    public static void main(String... args) {

        if (false) { // Not necessary with jython-standalone
            String home = System.getenv("HOME");
            System.setProperty("python.path", String.format("%s.gradle/caches/modules-2/files-2.1/org.python/jython/2.7.2/cd702d3b9e44b77302e2546562571f780d4c7e9a/jython-2.7.2.jar", home));
        }

        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec("print('Hello Python World!')");
        }

        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec("x = 10 + 10");
            PyObject x = pyInterp.get("x");
            System.out.printf("x is now %d\n", x.asInt());
        }

    }
}