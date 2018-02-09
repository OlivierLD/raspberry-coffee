/*
 * How to do an import
 * See the export
 * See the way the module figures in the html page.
 */
import fullWorldMap from "./worldmap.data.js";

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
			}
		}
	}
}
console.log('Done');

export let worldMap = fullWorldMap;
