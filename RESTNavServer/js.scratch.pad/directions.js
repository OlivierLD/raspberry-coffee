// Run in nodeJS
//
if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	};
}

if (Math.toDegrees === undefined) {
	Math.toDegrees = (rad) => {
		return rad * (180 / Math.PI);
	};
}

let directionFinder = (x, y) => {

    // Prefer getDir (atan2)
    
    let dir = 0.0;
    if (y !== 0) {
        dir = Math.toDegrees(Math.atan(x / y));
    }
    if (x <= 0 || y <= 0) {
        if (x > 0 && y < 0) {
            dir += 180;
        } else if (x < 0 && y > 0) {
            dir += 360;
        } else if (x < 0 && y < 0) {
            dir += 180;
        } else if (x === 0) {
            if (y > 0) {
                dir = 0.0;
            } else {
                dir = 180;
            }
        } else if (y === 0) {
            if (x > 0) {
                dir = 90;
            } else {
                dir = 270;
            }
        }
    }
    dir += 180;
    while (dir >= 360) {
        dir -= 360;
    }
    return dir;
};

let getDir = (x, y) => {
	let direction = 180 + Math.toDegrees(Math.atan2(x, y));
	while (direction < 0) {
		direction += 360;
	}
	direction %= 360;
	return direction;
};

let xArray = [-10, 0, 10];
let yArray = [-10, 0, 10];

const BAD_ONLY = true;
const OK_DELTA = 1e-10; // Set to 0 to get all the diffs.

console.log("-----------------------------");
if (false) {
    xArray.forEach(x => {
        yArray.forEach(y => {
            let dir1 = directionFinder(x, y);
            let dir2 = getDir(x, y);
            let diff = Math.abs(dir2 - dir1);
            if (!BAD_ONLY || (BAD_ONLY && dir2 !== dir1 && diff > OK_DELTA)) {
                console.log(`x:${x}, y:${y}: dir1:${dir1}, dir2:${dir2}. ${ (dir1 === dir2) ? "Good" : "BAAD!! (diff:" + diff + ")" }`);
            }
        });
    });
    console.log("-----------------------------");
}
if (true) {
    for (let x=-10; x<=10; x++) {
        for (let y=-10; y<=10; y++) {
            let dir1 = directionFinder(x, y);
            let dir2 = getDir(x, y);
            let diff = Math.abs(dir2 - dir1);
            if (!BAD_ONLY || (BAD_ONLY && dir2 !== dir1 && diff > OK_DELTA)) {
                console.log(`x:${x}, y:${y}: dir1:${dir1}, dir2:${dir2}. ${ (dir1 === dir2) ? "Good" : "BAAD!! (diff:" + diff + ")" }`);
            }
        }
    }
    console.log("-----------------------------");
}
