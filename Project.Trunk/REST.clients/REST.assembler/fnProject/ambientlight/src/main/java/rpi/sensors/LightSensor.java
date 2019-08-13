package rpi.sensors;

public class LightSensor {

	public static class Input {
		public String name;
	}

	public static class Result {
		public String salutation;
	}

	public Result handleRequest(Input input) {
		Result result = new Result();
		result.salutation = String.format("Hello %s!", (input != null && input.name != null && !input.name.isEmpty() ? input.name : "World"));

		return result;
	}

}
