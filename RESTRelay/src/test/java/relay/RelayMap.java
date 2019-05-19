package relay;

import httpserver.RelayRequestManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class RelayMap {

	@Test
	public void mapParsing00() {
		String map = "1:11";
		System.setProperty("relay.map", map);
		try {
			new RelayRequestManager();
		} catch (Throwable ex) {
			fail(String.format("Bad parsing for %s", map));
		}
	}

	@Test
	public void mapParsing01() {
		String map = "1:11,2:12";
		System.setProperty("relay.map", map);
		try {
			new RelayRequestManager();
		} catch (Throwable ex) {
			fail(String.format("Bad parsing for %s", map));
		}
	}

	@Test
	public void mapParsing02() {
		String map = "1:11,2:12,3";
		System.setProperty("relay.map", map);
		try {
			new RelayRequestManager();
			fail(String.format("Should have failed for %s", map));
		} catch (Throwable error) {
			System.out.println("Ah!");
		}
	}

	@Test
	public void mapParsing03() {
		String map = "1:11,2:12,3:0";
		System.setProperty("relay.map", map);
		try {
			new RelayRequestManager();
			fail(String.format("Should have failed for %s", map));
		} catch (Throwable error) {
			System.out.println("Ah!");
		}
	}

	@Test
	public void mapParsing04() {
		String map = "1:11,2:12,3:A";
		System.setProperty("relay.map", map);
		assertThrows(Throwable.class, () -> {
			new RelayRequestManager();
		});
	}

}
