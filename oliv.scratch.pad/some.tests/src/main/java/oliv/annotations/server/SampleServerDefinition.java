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
}
