package micronaut.sensors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class SensorsControllerTest {

	@Inject
	@Client("/")
	RxHttpClient client;

	@Test
	public void testSensors() {
		HttpRequest<String> request = HttpRequest.GET("/ambient-light");
		String body = client.toBlocking().retrieve(request);

		assertNotNull(body);
//		assertEquals("Hello World", body); // TODO Test value?
	}
}
