package phonekeyboard3x4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PinsCustomization {

	KeyboardController controller;

	@Before
	public void getStarted() {
		System.out.printf("Getting started on %s\n", this.getClass().getName());
	}

	@Test
	public void badOne() {

		System.setProperty("keypad.rows", "");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new KeyboardController();
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue("Exception should have been a InvalidParameterException", ex instanceof InvalidParameterException);
			System.out.println(ex.getMessage());
			assertTrue(String.format("Unexpected error message [%s]",
                    ex.getMessage()), ex.getMessage().startsWith("Please provide both ") || ex.getMessage().startsWith("keypad.rows should contain 4 elements, "));
			System.out.printf("As expected [%s]\n", ex.toString());
		}
	}

	@Test
	public void badTwo() {

		System.setProperty("keypad.rows", "");
		System.setProperty("keypad.cols", "");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new KeyboardController();
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(ex instanceof InvalidParameterException);
			System.out.println(ex.getMessage());
			assertEquals(String.format("Unexpected error message [%s]", ex.getMessage()), "keypad.rows should contain 4 elements, comma-separated.", ex.getMessage());
			System.out.printf("As expected [%s]\n", ex.toString());
		}
	}

	@Test
	public void badThree() {

		System.setProperty("keypad.rows", "A,B,C,D");
		System.setProperty("keypad.cols", "A,E,F");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new KeyboardController();
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(ex instanceof InvalidParameterException);
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().contains("cannot appear more than once"));
			System.out.printf("As expected [%s]\n", ex.toString());
		}
	}

	@Test
	public void badFour() {

		System.setProperty("keypad.rows", "A,B,C,D");
		System.setProperty("keypad.cols", "E,F,G");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new KeyboardController();
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(ex instanceof InvalidParameterException);
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().startsWith("Unknown row pin name "));
			System.out.printf("As expected [%s]\n", ex.toString());
		}
	}

	@Test
	public void goodFive() {

		System.setProperty("keypad.rows", "GPIO_1,GPIO_4,GPIO_21,GPIO_22");
		System.setProperty("keypad.cols", "GPIO_7,GPIO_23, GPIO_3");
		System.setProperty("keypad.verbose", "true");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new KeyboardController(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(String.format("This should have worked [%s]", ex.toString()));
		}
	}

	@After
	public void tearDown() {
		System.out.println("Tearing down");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
