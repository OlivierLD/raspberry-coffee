package oliv.annotations.server;

public class SampleServerDefinition {

	@OperationDefinition(
			verb = OperationDefinition.Verb.GET,
			path = "/root/stuff",
			description = "Very basic sample"
	)
	public static ServerRunner.Response getSomeStuff(ServerRunner.Request request) {
		return new ServerRunner.Response();
	}
}
