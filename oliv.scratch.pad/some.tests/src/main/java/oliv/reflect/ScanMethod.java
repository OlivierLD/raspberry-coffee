package oliv.reflect;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ScanMethod {

	private static interface StuffInterface {
		String getStuff();
	}

	private static class StuffImplementation implements StuffInterface {

		@Override
		public String getStuff() {
			return "Akeu pouet";
		}

		public String getMoreStuff() {
			return "More Stuff";
		}

		public void otherStuff() {

		}
	}

	private StuffImplementation stuff;

	public ScanMethod() {
	    stuff = new StuffImplementation();
	}

	public StuffImplementation getStuffImplementation() {
		return this.stuff;
	}


	public static void main(String... args) {
		ScanMethod scanMethod = new ScanMethod();
		StuffImplementation stuffImplementation = scanMethod.getStuffImplementation();
		// On instance
		System.out.println(String.format("%s, %s", stuffImplementation.getStuff(), stuffImplementation.getMoreStuff()));
		// Reflection
		Object object = stuffImplementation;
		Arrays.stream(object.getClass()/*.getMethods()*/.getDeclaredMethods()).forEach(decMethod -> {
			System.out.println(String.format("Declared method: %s", decMethod.getName()));
			if (decMethod.getName().startsWith("get")) {
				System.out.println(String.format("\tGetter %s", decMethod.getName()));
				try {
					Object got = decMethod.invoke(object); // No arg for the getters
					System.out.println(String.format("\tmember [%s=%s]", decMethod.getName().substring("get".length()), (got != null ? got.toString() : "null")));

				} catch (IllegalAccessException iae) {
					iae.printStackTrace();
				} catch (InvocationTargetException ite) {
					ite.printStackTrace();
				}
			}
		});
	}
}
