package oliv.annotations.server;

@RootPath(path = "/root")
public class SampleServerDefinition {

	@OperationDefinition(
			verb = OperationDefinition.Verb.GET,
			path = "/sample/stuff",
			description = "Very basic get sample"
	)
	public static ServerRunner.Response getSomeStuff(ServerRunner.Request request) {
		return new ServerRunner.Response();
	}

	@OperationDefinition(
			verb = OperationDefinition.Verb.PUT,
			path = "/sample/stuff",
			description = "Very basic put sample"
	)
	public static ServerRunner.Response setSomeStuff(ServerRunner.Request request) {
		return new ServerRunner.Response();
	}

	public static class Greeting {
		String name;
		String salutation = "Hello";
		public String toString() {
			return String.format("%s %s!", this.salutation, this.name);
		}
	}

	@OperationDefinition(
			verb = OperationDefinition.Verb.GET,
			path = "/sample/greeting/{greet}",
			description = "Greeting operation"
	)
	public static Greeting sayHello(@QueryParam(name = "who") String who, @PathParam(name = "greet") String greet) {
		Greeting greeting = new Greeting();
		greeting.name = (who != null) ? who : "World";
		if (greet != null) {
			greeting.salutation = greet;
		}
		return greeting;
	}
}
