package astro;

//import calc.calculation.AstroComputer;
import calc.calculation.AstroComputerV2;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals;

public class AstroTests {

	@Test
	public void ghaAndLongitude() {
		double longitude = -122;
		double gha = AstroComputerV2.longitudeToGHA(longitude);
		System.out.println(String.format("Longitude: %f => GHA: %f", longitude, gha));
		double backAgain = AstroComputerV2.ghaToLongitude(gha);
		assertEquals(String.format("Ooops: was %f, instead of %f", backAgain, longitude), longitude, backAgain);
	}
}
