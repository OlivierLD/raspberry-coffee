/*
 * How to do an import
 * See the export in worldmap.data.js
 * See the way the module figures in the html page.
 */
import fullWorldMap from "./worldmap.data.js";

let minLat = 100, maxLat = -100, minLng = 200, maxLng = -200;

for (let key in fullWorldMap) {
	console.log("Key [%s]", key);
	let top = fullWorldMap[key];
	for (let sectionK in top) {
		console.log(">> Key [%s]", sectionK);
		let section = top[sectionK];
		for (let key3 in section) {
			console.log(">> >> Key [%s], type %s", key3, typeof(section[key3]));
			if (section[key3].point !== undefined) {
				console.log("An array of %d points", section[key3].point.length);
				for (let p=0; p<section[key3].point.length; p++) {
					minLat = Math.min(minLat, parseFloat(section[key3].point[p].Lat));
					maxLat = Math.max(maxLat, parseFloat(section[key3].point[p].Lat));
					minLng = Math.min(minLng, parseFloat(section[key3].point[p].Lng));
					maxLng = Math.max(maxLng, parseFloat(section[key3].point[p].Lng));
				}
			}
		}
	}
}
console.log('Done. Lat in [%f, %f], Lng in [%f, %f]', minLat, maxLat, minLng, maxLng);

// export default fullWorldMap;
