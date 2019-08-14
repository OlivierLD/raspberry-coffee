package rpi.sensors;

public class LightSensor {

	public static class Input {
		public String name;
	}

	public static class Result {
		public String requester;
		public String dataType;
		public float value;
	}

	public Result retrieveData(Input input) {
		Result result = new Result();
		result.requester = String.format("%s", (input != null && input.name != null && !input.name.isEmpty() ? input.name : "Nobody"));
		result.dataType = "ambient-light";
		result.value = (float)(100 * Math.random());

		return result;
	}

}
