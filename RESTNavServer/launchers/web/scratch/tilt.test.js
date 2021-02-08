if (Math.toDegrees === undefined) {
	Math.toDegrees = (rad) => {
		return rad * (180 / Math.PI);
	};
}

if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	};
}

let zSun = 121;
let zMoon = 161;
let altSun = 14;
let altMoon = 25;

let alpha = 0;

// Take the first triangle, from the Moon.
let deltaZ = zMoon - zSun;
if (deltaZ > 180) { // like 358 - 2, should be 358 - 362.
    deltaZ -= 360;
}
let deltaElev = altMoon - altSun;
alpha = Math.toDegrees(Math.atan2(deltaElev, deltaZ)); // atan2 from -Pi to Pi
if (deltaElev > 0) {
    if (deltaZ > 0) { // positive angle, like 52
        alpha *= -1;
    } else { // Angle > 90, like 116
        if (alpha < 90) {
            alpha -= 90;
        } else {
            alpha = 180 - alpha;
        }
    }
} else {
    if (deltaZ > 0) { // negative angle, like -52
        alpha *= -1;
    } else { // Negative, < -90, like -116
        // alpha += 90; // TODO Tweak that like above too
        if (alpha > -90) {
            alpha += 90;
        } else {
            alpha = - 180 - alpha;
        }
    }
}

console.log(`Tilt: ${alpha}`);
