package polarmaker.smooth;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PolarsResourceBundle {
	private static String baseName = "polarmaker.smooth.polars";
	private static ResourceBundle resourceBundle;

	private PolarsResourceBundle() {
	}

	public static synchronized ResourceBundle getPolarsResourceBundle() {
		if (resourceBundle == null) {
			try {
				resourceBundle = ResourceBundle.getBundle(baseName);
//      System.out.println("ResourceBundle created");
			} catch (MissingResourceException mre) {
				System.out.println("Missing Resource:" + mre.getMessage());
			}
		}
//  else
//    System.out.println("ResourceBundle reused");
		return resourceBundle;
	}
}
