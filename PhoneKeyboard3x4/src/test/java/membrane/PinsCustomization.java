package membrane;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

public class PinsCustomization {

	MembraneKeyPad1x4 controller;

	@Before
	public void getStarted() {
		System.out.println(String.format("Getting started on %s", this.getClass().getName()));
	}

	@Test
	public void badOne() {

		System.setProperty("keypad.cols", "");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new MembraneKeyPad1x4();
			fail("Should have caught an exception");
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(ex instanceof InvalidParameterException);
			System.out.println(ex.getMessage());
			assertTrue(String.format("Unexpected error message [%s]", ex.getMessage()), ex.getMessage().startsWith("Please provide both "));
			System.out.println(String.format("As expected [%s]", ex.toString()));
		}
	}

	@Test
	public void badTwo() {

		System.setProperty("keypad.cols", "");
		System.setProperty("common.lead", "");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new MembraneKeyPad1x4();
			fail("Should have caught an exception");
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(ex instanceof InvalidParameterException);
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().equals("keypad.cols should contain 4 elements, comma-separated."));
			System.out.println(String.format("As expected [%s]", ex.toString()));
		}
	}

	@Test
	public void badThree() {

		System.setProperty("keypad.cols", "A,B,C,D");
		System.setProperty("common.lead", "A");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new MembraneKeyPad1x4();
			fail("Should have caught an exception");
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(ex instanceof InvalidParameterException);
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().contains("cannot appear more than once"));
			System.out.println(String.format("As expected [%s]", ex.toString()));
		}
	}

	@Test
	public void badFour() {

		System.setProperty("keypad.cols", "A,B,C,D");
		System.setProperty("common.lead", "E");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new MembraneKeyPad1x4();
			fail("Should have caught an exception");
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(ex instanceof InvalidParameterException);
			System.out.println(ex.getMessage());
			assertTrue(ex.getMessage().startsWith("Unknown row pin name "));
			System.out.println(String.format("As expected [%s]", ex.toString()));
		}
	}

	@Test
	public void goodFive() {

		System.setProperty("keypad.cols", "GPIO_1,GPIO_4,GPIO_21,GPIO_22");
		System.setProperty("common.lead", "GPIO_7");
		System.setProperty("keypad.verbose", "true");
		try {
			if (controller != null) {
				controller.shutdown();
				controller = null;
			}
			controller = new MembraneKeyPad1x4();
			System.out.println("Good config");
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
