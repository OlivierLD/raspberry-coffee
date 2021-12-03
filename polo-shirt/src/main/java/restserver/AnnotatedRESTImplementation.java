package restserver;

import http.HTTPServer;
import restserver.annotations.BodyParam;
import restserver.annotations.OperationDefinition;
import restserver.annotations.PathParam;
import restserver.annotations.QueryParam;
import restserver.annotations.RootPath;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * Unlike in the non-annotated case, Operations need to be non-private,
 * as they will be dynamically invoked by the {@link PoloRESTRequestManager}
 *
 * This is an EXAMPLE of implementation.
 *
 */
@RootPath("/top-root")
public class AnnotatedRESTImplementation {

	private boolean verbose = "true".equals(System.getProperty("server.rest.verbose"));

	private PoloRESTRequestManager restRequestManager;

	public AnnotatedRESTImplementation(PoloRESTRequestManager restRequestManager) {
		this.restRequestManager = restRequestManager;
	}

	@OperationDefinition(
			verb = OperationDefinition.Verbs.GET,
			path = "/oplist",
			absolutePath = true,
			description = "List of all available operations."
	)
	protected List<HTTPServer.Operation> getOperationList() {
		List<HTTPServer.Operation> opList = this.restRequestManager.getRestOperationList()
				.stream()
				.map(rop -> new HTTPServer.Operation(rop.getVerb(), rop.getPath(), null, rop.getDescription()))
				.collect(Collectors.toList());
		return opList;
	}

	@OperationDefinition(
			verb = OperationDefinition.Verbs.GET,
			path = "/greeting",
			description = "A simple example, with different signatures."
	)
	protected static String greet(@QueryParam(name = "name") String who) {
		return greet(null, who);
	}

	@OperationDefinition(
			verb = OperationDefinition.Verbs.GET,
			path = "/greeting/{greet}",
			description = "A simple example, returns a String. QueryParam 'name'"
	)
	protected static String greet(@PathParam(name = "greet") String salutation, @QueryParam(name = "name") String who) {
		String greeting = String.format("%s %s!", (salutation == null ? "Hello" : salutation), (who != null ? who : "world"));
		return greeting;
	}

	public static class Message {
		String message;
	}

	@OperationDefinition(
			verb = OperationDefinition.Verbs.GET,
			path = "/greeting/v2/{greet}",
			description = "A simple example, returning a Bean. QueryParam 'name'"
	)
	protected static Message greetV2(@PathParam(name = "greet") String salutation, @QueryParam(name = "name") String who) {
		String greeting = String.format("%s %s!", (salutation == null ? "Hello" : salutation), (who != null ? who : "world"));
		Message message = new Message();
		message.message = greeting;
		return message;
	}

	public class GreetingObject {
		private String salutation;
		private String name;

		public GreetingObject() {
		}

		public String getSalutation() {
			return salutation;
		}

		public void setSalutation(String salutation) {
			this.salutation = salutation;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@OperationDefinition(
			verb = OperationDefinition.Verbs.POST,
			path = "/greeting/v3",
			description = "A simple example, taking a BodyParam, returning a Bean. Try curl -X POST http://localhost:2345/top-root/greeting/v3 -d '{ \"name\": \"Ducon\", \"salutation\": \"Salut\" }' | jq"
	)
	protected static Message greetV3(@BodyParam() GreetingObject greetingObj) {
		String greeting = String.format("%s %s!",
				(greetingObj.getSalutation() == null ? "Hello" : greetingObj.getSalutation()),
				(greetingObj.getName() != null ? greetingObj.getName() : "world"));
		Message message = new Message();
		message.message = greeting;
		return message;
	}

}
