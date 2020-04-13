package rest.oas.impl;

public class SensorData {

	public static class RelayStatus {
		boolean status = false;

		public RelayStatus() {
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}
	}

	public static class AmbientLight {
		float light;

		public AmbientLight() {
		}

		public float getLight() {
			return light;
		}

		public void setLight(float light) {
			this.light = light;
		}
	}
}
