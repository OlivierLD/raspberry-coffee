package polarmaker.polars.smooth.gui.components.dotmatrix;

public class ThreeDPoint {
	float x;
	float y;
	float z;

	public ThreeDPoint() {
	}

	public ThreeDPoint(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX() {
		return x;
	}

	public void setX(float newX) {
		x = newX;
	}

	public float getY() {
		return y;
	}

	public void setY(float newY) {
		y = newY;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float newZ) {
		z = newZ;
	}
}
