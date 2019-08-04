package http;

import http.client.HTTPClient;
import org.junit.Test;

import java.net.ProtocolException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.fail;

public class HTTPServerTests {

	private static int PORT_TO_USE = 1024;

	@Test
	public void detectBadRequestManager() {
		List<HTTPServer.Operation> opList1 = Arrays.asList(
				new HTTPServer.Operation(
						"GET",
						"/oplist",
						this::emptyOperation,
						"List of all available operations."),
				new HTTPServer.Operation(
						"POST",
						"/create/{it}",
						this::emptyOperation,
						"Blah."),
				new HTTPServer.Operation(
						"POST",
						"/terminate",
						this::emptyOperation,
						"Hard stop, shutdown. VERY unusual REST resource..."));


		List<HTTPServer.Operation> opList2 = Arrays.asList(
				new HTTPServer.Operation(
						"GET",
						"/oplist",
						this::emptyOperation,
						"List of all available operations."),
				new HTTPServer.Operation(
						"POST",
						"/create/{this}",
						this::emptyOperation,
						"Blah."),
				new HTTPServer.Operation(
						"POST",
						"/finish",
						this::emptyOperation,
						"Hard stop, shutdown. VERY unusual REST resource..."));

		 RESTRequestManager restServerImplOne = new RESTRequestManager() {

			 @Override
			 public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
				 return null;
			 }

			 @Override
			 public List<HTTPServer.Operation> getRESTOperationList() {
				 return opList1;
			 }
		 };


		RESTRequestManager restServerImplTwo = new RESTRequestManager() {

			@Override
			public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
				return null;
			}

			@Override
			public List<HTTPServer.Operation> getRESTOperationList() {
				return opList2;
			}
		};
		HTTPServer httpServer = null;
		PORT_TO_USE += 1;
		try {
			httpServer = new HTTPServer(PORT_TO_USE, restServerImplOne);
			httpServer.startServer();
		} catch (Exception ex) {
			fail(ex.toString());
		}
		assertNotNull(httpServer);
		try {
			httpServer.addRequestManager(restServerImplTwo);
			fail("We should not be there");
		} catch (IllegalArgumentException ex) {
			System.out.println(String.format("As expected [%s]", ex.toString()));
		}
	}

	@Test
	public void detectDuplicateOperations() {
		List<HTTPServer.Operation> opList = Arrays.asList(
				new HTTPServer.Operation(
						"GET",
						"/oplist",
						this::emptyOperation,
						"List of all available operations."),
				new HTTPServer.Operation(
						"POST",
						"/create/{it}",
						this::emptyOperation,
						"Blah."),
				new HTTPServer.Operation(
						"POST",
						"/create/{stuff}",
						this::emptyOperation,
						"Blah."),
				new HTTPServer.Operation(
						"POST",
						"/terminate",
						this::emptyOperation,
						"Hard stop, shutdown. VERY unusual REST resource..."));
		try {
			RESTProcessorUtil.checkDuplicateOperations(opList);
			fail("Should have detected duplicate");
		} catch (Exception ex) {
			System.out.println(String.format("As expected: %s", ex.toString()));
		}
	}

	@Test
	public void useCustomVerb() {
		List<HTTPServer.Operation> opList = Arrays.asList(
				new HTTPServer.Operation(
						"GET",
						"/oplist",
						this::emptyOperation,
						"List of all available operations."),
				new HTTPServer.Operation(
						"CUST",
						"/customverb/{it}",
						this::emptyOperation,
						"Blah."));

		RESTRequestManager restServerImpl = new RESTRequestManager() {

			@Override
			public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
				return null;
			}

			@Override
			public List<HTTPServer.Operation> getRESTOperationList() {
				return opList;
			}
		};
		HTTPServer httpServer = null;
		PORT_TO_USE += 1;
		try {
			httpServer = new HTTPServer(PORT_TO_USE, restServerImpl, true);
		} catch (Exception ex) {
			fail(ex.toString());
		}
		assertNotNull(httpServer);
		PORT_TO_USE += 1;
		try {
			int ret = HTTPClient.doCustomVerb("CUST", "http://localhost:" + String.valueOf(PORT_TO_USE) + "/cutomverb/Yo", null, null);
			fail("Custom protocol should not be supported.");
		} catch (ProtocolException pe) {
			// Expected
			System.out.println("As expected");
		} catch (Exception ex) {
			fail(ex.toString());
		}
	}

	private HTTPServer.Response emptyOperation(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		return response;
	}
}
