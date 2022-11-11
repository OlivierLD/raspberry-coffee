package tests;

import org.junit.Test;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import static org.junit.Assert.assertEquals;

public class Jython_101 {

    @Test
    public void givenPythonInterpreter_whenNumbersAdded_thenOutputDisplayed() {
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec("x = 10 + 10");   // Warning: No Python3 syntax.
            PyObject x = pyInterp.get("x");
            assertEquals("x: ", 20, x.asInt());
        }
    }
}
