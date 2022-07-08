package astro

import "math"

func ToRadians(deg float64) float64 {
	return deg * math.Pi / 180
}

func ToDegrees(rad float64) float64 {
	return rad * 180 / math.Pi
}

// Sine of angles in degrees
func SinD(x float64) float64 {
	return math.Sin(ToRadians(x))
}

// Cosine of angles in degrees
func CosD(x float64) float64 {
	return math.Cos(ToRadians(x))
}

// Tangent of angles in degrees
func TanD(x float64) float64 {
	return math.Tan(ToRadians(x))
}

// Normalize large angles
// Degrees
func Norm360Deg(x float64) float64 {
	for ok := true; ok; ok = x < 0 {
		x += 360
	}
	for ok := true; ok; ok = x > 360 {
		x -= 360
	}
	return x
}

// Radians
func Norm2PiRad(x float64) float64 {
	for ok := true; ok; ok = x < 0 {
	    x += (2 * math.Pi)
    }
	for ok := true; ok; ok = x > (2 * math.Pi) {
		x -= (2 * math.Pi)
 	}
	return x
}

// Cosine of normalized angle (in radians)
func CosT(x float64) float64 {
  return math.Cos(Norm2PiRad(x))
}
