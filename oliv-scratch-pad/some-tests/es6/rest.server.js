/**
 * This is a small and tiny Web server running on NodeJS. Mostly serves static pages and files.
 *
 * To debug:
 *   To install node-inspector, to do once:
 *   $ set HTTP_PROXY=http://www-proxy.us.oracle.com:80 # if needed for the install
 *   $ npm install -g node-inspector
 *
 *   To run node-inspector:
 *   $ node-inspector
 *
 * From another console:
 *   $ node --debug server.js
 */
"use strict";

process.title = 'TinyNodeServer';

// HTTP port
let port = 8080;

let http = require('http');
let fs = require('fs');

let verbose = false;

let workDir = process.cwd();

console.log("----------------------------------------------------");
console.log("Usage: node " + __filename + " --verbose:true|false --port:XXXX --wdir:path/to/working/dir");
console.log("----------------------------------------------------");

for (let i=0; i<process.argv.length; i++) {
	console.log("arg #%d: %s", i, process.argv[i]);
}

if (typeof String.prototype.startsWith !== 'function') {
	String.prototype.startsWith = (str) => {
		return this.indexOf(str) === 0;
	};
}

if (typeof String.prototype.endsWith !== 'function') {
	String.prototype.endsWith = (suffix) => {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};
}

if (process.argv.length > 2) {
	for (let argc=2; argc<process.argv.length; argc++) {
		if (process.argv[argc].startsWith("--verbose:")) {
			let value = process.argv[argc].substring("--verbose:".length);
			if (value !== 'true' && value !== 'false') {
				console.log("Invalid verbose value [%s]. Only 'true' and 'false' are supported.", value);
				process.exit(1);
			}
			verbose = (value === 'true');
		} else if (process.argv[argc].startsWith("--port:")) {
			let value = process.argv[argc].substring("--port:".length);
			try {
				port = parseInt(value);
			} catch (err) {
				console.log("Invalid integer for port value %s.", value);
				process.exit(1);
			}
		} else if (process.argv[argc].startsWith("--wdir:")) {
			let value = process.argv[argc].substring("--wdir:".length);
			try {
				process.chdir(value);
				workDir = process.cwd();
			} catch (err) {
				console.log("Invalid new working directory %s.", value);
				process.exit(1);
			}
		} else {
			console.log("Unsupported parameter %s, ignored.", process.argv[argc]);
		}
	}
}

console.log("----------------------------------------------------");
console.log("Your working directory:", workDir);
console.log("----------------------------------------------------");

let decToSex = (val, ns_ew, withDeg) => {
	let absVal = Math.abs(val);
	let intValue = Math.floor(absVal);
	let dec = absVal - intValue;
	let i = intValue;
	dec *= 60;
//    let s = i + "°" + dec.toFixed(2) + "'";
//    let s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
	let s = "";
	if (val < 0) {
		s += (ns_ew === 'NS' ? 'S' : 'W');
	} else {
		s += (ns_ew === 'NS' ? 'N' : 'E');
	}
	s += " ";
	let sep = " ";
	if (withDeg === true) {
		sep = "°";
	}
//    s += i + "\"" + dec.toFixed(2) + "'";
	s += i + sep + dec.toFixed(2) + "'";
	return s;
};

const REST_PREFIX = "/rest";
const OPLIST_RESOURCE = "/oplist";
const GEOPOS_RESOURCE = "/geopos/";
const POINTS_RESOURCE = "/points/";
// Match "/rest/gridpoints/MTR/85,126/forecast"; 3 Digits: \d{3}
const FORECAST_RESOURCE = new RegExp("\\/gridpoints\\/[A-Z]{3}\\/[0-9]*,[0-9]*\\/forecast");

/**
 * Small Simple and Stupid little web server.
 * Very basic. Lighter than Express.
 *
 * handler function takes 2 parameters:
 * - req (Request)
 * - res (Response)
 */
let handler = (req, res) => {
	let respContent = "";
	if (verbose) {
		console.log("Speaking HTTP from " + workDir); // was __dirname
		console.log("Server received an HTTP Request:\n" + req.method + "\n" + req.url + "\n-------------");
		console.log("ReqHeaders:" + JSON.stringify(req.headers, null, '\t'));
		console.log('Request:' + req.url);
		let prms = require('url').parse(req.url, true);
		console.log(prms);
		console.log("Search: [" + prms.search + "]");
		console.log("-------------------------------");
	}
	if (req.url.startsWith("/verbose=")) { // Not very usual syntax, I know...
		if (req.method === "GET") {
			verbose = (req.url.substring("/verbose=".length) === 'on');
			res.end(JSON.stringify({verbose: verbose ? 'on' : 'off'}));
		}
	} else if (req.url.startsWith(REST_PREFIX)) { // REST Operations!
		if (req.url.startsWith(REST_PREFIX + GEOPOS_RESOURCE)) {
			if (req.method === "GET") {
				let pathPrm = req.url.substring((REST_PREFIX + GEOPOS_RESOURCE).length); // Expects Path prm like "37.75,-122.50"
				console.log(`Received ${pathPrm}`);
				try {
					let latLong = pathPrm.split(",");
					let lat = parseFloat(latLong[0]);
					let lng = parseFloat(latLong[1]);
					let latDeg = decToSex(lat, "NS", true);
					let lngDeg = decToSex(lng, "EW", true);
					res.writeHead(200, {'Content-Type': 'application/json'});
					res.end(JSON.stringify({from: {lat: lat, lng: lng}, to: {lat: latDeg, lng: lngDeg}}));
				} catch (error) {
					res.writeHead(404, {'Content-Type': 'application/json'});
					res.end(JSON.stringify(error));
				}
			} else {
				res.writeHead(404, {'Content-Type': 'text/plain'});
				res.end(`${req.method} ${req.url} : Not Implemented`);
			}
		} else if (req.url.startsWith(REST_PREFIX + POINTS_RESOURCE)) {
			// console.log("HOST: [" + req.headers.host + "]");
			if (req.method === "GET") {
				let pathPrm = req.url.substring((REST_PREFIX + POINTS_RESOURCE).length); // Expects Path prm like "37.75,-122.50"
				try {
					let latLong = pathPrm.split(",");
					let lat = parseFloat(latLong[0]);
					let lng = parseFloat(latLong[1]);
					res.writeHead(200, {'Content-Type': 'application/json'});
					let rootUrl = `${req.protocol}${req.host}:${req.port}/rest`; // Requires node 14.5.+. See https://nodejs.org/api/http.html#http_request_host
					if (req.host === undefined) {
						rootUrl = `http://${req.headers.host}/rest`;
					}
					let responsePayload = { // Partly hard-coded
						"properties": {
							"@id": `${rootUrl}/points/${pathPrm}`,
							"@type": "wx:Point",
							"cwa": "MTR",
							"forecastOffice": "http://api.weather.gov/offices/MTR",
							"gridId": "MTR",
							"gridX": 85,
							"gridY": 126,
							"forecast": `${rootUrl}/gridpoints/MTR/85,126/forecast`,
							"forecastHourly": `${rootUrl}/gridpoints/MTR/85,126/forecast/hourly`,
							"forecastGridData": `${rootUrl}/gridpoints/MTR/85,126`,
							"observationStations": `${rootUrl}/gridpoints/MTR/85,126/stations`,
							"relativeLocation": {
								"type": "Feature",
								"geometry": {
									"type": "Point",
									"coordinates": [
										lng,
										lat
									]
								},
								"properties": {
									"city": "Daly City",
									"state": "CA",
									"distance": {
										"value": 6264.6077562384999,
										"unitCode": "unit:m"
									},
									"bearing": {
										"value": 330,
										"unitCode": "unit:degrees_true"
									}
								}
							},
							"forecastZone": "https://api.weather.gov/zones/forecast/CAZ006",
							"county": "https://api.weather.gov/zones/county/CAC075",
							"fireWeatherZone": "https://api.weather.gov/zones/fire/CAZ006",
							"timeZone": "America/Los_Angeles",
							"radarStation": "KMUX"
						}
					};
					res.end(JSON.stringify(responsePayload));
				} catch (error) {
					res.writeHead(404, {'Content-Type': 'application/json'});
					res.end(JSON.stringify(error));
				}
			}
		} else if (FORECAST_RESOURCE.test(req.url)) { // RegExp
			if (req.method === "GET") {
				let responsePayload = { // All hard-coded for this test.
					"@context": [
						"https://geojson.org/geojson-ld/geojson-context.jsonld",
						{
							"@version": "1.1",
							"wx": "https://api.weather.gov/ontology#",
							"geo": "http://www.opengis.net/ont/geosparql#",
							"unit": "http://codes.wmo.int/common/unit/",
							"@vocab": "https://api.weather.gov/ontology#"
						}
					],
					"type": "Feature",
					"geometry": {
						"type": "Polygon",
						"coordinates": [
							[
								[
									-122.5090833,
									37.767808899999999
								],
								[
									-122.50340989999999,
									37.746005099999998
								],
								[
									-122.47585049999999,
									37.750485300000001
								],
								[
									-122.48151849999999,
									37.772289499999999
								],
								[
									-122.5090833,
									37.767808899999999
								]
							]
						]
					},
					"properties": {
						"updated": "2020-09-21T19:56:34+00:00",
						"units": "us",
						"forecastGenerator": "BaselineForecastGenerator",
						"generatedAt": "2020-09-21T20:03:33+00:00",
						"updateTime": "2020-09-21T19:56:34+00:00",
						"validTimes": "2020-09-21T13:00:00+00:00/P7DT12H",
						"elevation": {
							"value": 45.110399999999998,
							"unitCode": "unit:m"
						},
						"periods": [
							{
								"number": 1,
								"name": "This Afternoon",
								"startTime": "2020-09-21T13:00:00-07:00",
								"endTime": "2020-09-21T18:00:00-07:00",
								"isDaytime": true,
								"temperature": 63,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "9 to 13 mph",
								"windDirection": "WSW",
								"icon": "https://api.weather.gov/icons/land/day/sct?size=medium",
								"shortForecast": "Mostly Sunny",
								"detailedForecast": "Mostly sunny, with a high near 63. West southwest wind 9 to 13 mph."
							},
							{
								"number": 2,
								"name": "Tonight",
								"startTime": "2020-09-21T18:00:00-07:00",
								"endTime": "2020-09-22T06:00:00-07:00",
								"isDaytime": false,
								"temperature": 57,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "9 to 14 mph",
								"windDirection": "WSW",
								"icon": "https://api.weather.gov/icons/land/night/bkn?size=medium",
								"shortForecast": "Mostly Cloudy",
								"detailedForecast": "Mostly cloudy, with a low around 57. West southwest wind 9 to 14 mph."
							},
							{
								"number": 3,
								"name": "Tuesday",
								"startTime": "2020-09-22T06:00:00-07:00",
								"endTime": "2020-09-22T18:00:00-07:00",
								"isDaytime": true,
								"temperature": 62,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "13 mph",
								"windDirection": "WSW",
								"icon": "https://api.weather.gov/icons/land/day/bkn?size=medium",
								"shortForecast": "Partly Sunny",
								"detailedForecast": "Partly sunny, with a high near 62. West southwest wind around 13 mph."
							},
							{
								"number": 4,
								"name": "Tuesday Night",
								"startTime": "2020-09-22T18:00:00-07:00",
								"endTime": "2020-09-23T06:00:00-07:00",
								"isDaytime": false,
								"temperature": 56,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "8 to 13 mph",
								"windDirection": "W",
								"icon": "https://api.weather.gov/icons/land/night/bkn?size=medium",
								"shortForecast": "Mostly Cloudy",
								"detailedForecast": "Mostly cloudy, with a low around 56. West wind 8 to 13 mph."
							},
							{
								"number": 5,
								"name": "Wednesday",
								"startTime": "2020-09-23T06:00:00-07:00",
								"endTime": "2020-09-23T18:00:00-07:00",
								"isDaytime": true,
								"temperature": 63,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "8 to 12 mph",
								"windDirection": "W",
								"icon": "https://api.weather.gov/icons/land/day/sct?size=medium",
								"shortForecast": "Mostly Sunny",
								"detailedForecast": "Mostly sunny, with a high near 63. West wind 8 to 12 mph."
							},
							{
								"number": 6,
								"name": "Wednesday Night",
								"startTime": "2020-09-23T18:00:00-07:00",
								"endTime": "2020-09-24T06:00:00-07:00",
								"isDaytime": false,
								"temperature": 58,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "6 to 12 mph",
								"windDirection": "W",
								"icon": "https://api.weather.gov/icons/land/night/sct?size=medium",
								"shortForecast": "Partly Cloudy",
								"detailedForecast": "Partly cloudy, with a low around 58."
							},
							{
								"number": 7,
								"name": "Thursday",
								"startTime": "2020-09-24T06:00:00-07:00",
								"endTime": "2020-09-24T18:00:00-07:00",
								"isDaytime": true,
								"temperature": 63,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "6 to 16 mph",
								"windDirection": "W",
								"icon": "https://api.weather.gov/icons/land/day/sct?size=medium",
								"shortForecast": "Mostly Sunny",
								"detailedForecast": "Mostly sunny, with a high near 63."
							},
							{
								"number": 8,
								"name": "Thursday Night",
								"startTime": "2020-09-24T18:00:00-07:00",
								"endTime": "2020-09-25T06:00:00-07:00",
								"isDaytime": false,
								"temperature": 56,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "9 to 18 mph",
								"windDirection": "WNW",
								"icon": "https://api.weather.gov/icons/land/night/few?size=medium",
								"shortForecast": "Mostly Clear",
								"detailedForecast": "Mostly clear, with a low around 56."
							},
							{
								"number": 9,
								"name": "Friday",
								"startTime": "2020-09-25T06:00:00-07:00",
								"endTime": "2020-09-25T18:00:00-07:00",
								"isDaytime": true,
								"temperature": 64,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "8 to 16 mph",
								"windDirection": "NW",
								"icon": "https://api.weather.gov/icons/land/day/few?size=medium",
								"shortForecast": "Sunny",
								"detailedForecast": "Sunny, with a high near 64."
							},
							{
								"number": 10,
								"name": "Friday Night",
								"startTime": "2020-09-25T18:00:00-07:00",
								"endTime": "2020-09-26T06:00:00-07:00",
								"isDaytime": false,
								"temperature": 56,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "8 to 16 mph",
								"windDirection": "NW",
								"icon": "https://api.weather.gov/icons/land/night/few?size=medium",
								"shortForecast": "Mostly Clear",
								"detailedForecast": "Mostly clear, with a low around 56."
							},
							{
								"number": 11,
								"name": "Saturday",
								"startTime": "2020-09-26T06:00:00-07:00",
								"endTime": "2020-09-26T18:00:00-07:00",
								"isDaytime": true,
								"temperature": 67,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "9 mph",
								"windDirection": "NW",
								"icon": "https://api.weather.gov/icons/land/day/few?size=medium",
								"shortForecast": "Sunny",
								"detailedForecast": "Sunny, with a high near 67."
							},
							{
								"number": 12,
								"name": "Saturday Night",
								"startTime": "2020-09-26T18:00:00-07:00",
								"endTime": "2020-09-27T06:00:00-07:00",
								"isDaytime": false,
								"temperature": 58,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "5 to 9 mph",
								"windDirection": "WNW",
								"icon": "https://api.weather.gov/icons/land/night/few?size=medium",
								"shortForecast": "Mostly Clear",
								"detailedForecast": "Mostly clear, with a low around 58."
							},
							{
								"number": 13,
								"name": "Sunday",
								"startTime": "2020-09-27T06:00:00-07:00",
								"endTime": "2020-09-27T18:00:00-07:00",
								"isDaytime": true,
								"temperature": 84,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "5 to 9 mph",
								"windDirection": "N",
								"icon": "https://api.weather.gov/icons/land/day/skc?size=medium",
								"shortForecast": "Sunny",
								"detailedForecast": "Sunny, with a high near 84."
							},
							{
								"number": 14,
								"name": "Sunday Night",
								"startTime": "2020-09-27T18:00:00-07:00",
								"endTime": "2020-09-28T06:00:00-07:00",
								"isDaytime": false,
								"temperature": 60,
								"temperatureUnit": "F",
								"temperatureTrend": null,
								"windSpeed": "5 to 8 mph",
								"windDirection": "WNW",
								"icon": "https://api.weather.gov/icons/land/night/skc?size=medium",
								"shortForecast": "Clear",
								"detailedForecast": "Clear, with a low around 60."
							}
						]
					}
				};
				res.writeHead(200, {'Content-Type': 'application/json'});
				res.end(JSON.stringify(responsePayload));
			}
		} else if (req.url.startsWith(REST_PREFIX + OPLIST_RESOURCE)) {
			if (req.method === "GET") {
				res.writeHead(200, {'Content-Type': 'application/json'});
				res.end(JSON.stringify([
					{
						name: REST_PREFIX + GEOPOS_RESOURCE + "{coordinates}",
						verb: "GET",
						description: "Transform decimal coordinates into 'X Deg Min.mm' format.",
						prms: {
							path: [{
								name: "{coordinates}",
								description: "Like '37.75,-122.50'"
							}]
						}
					}, {
						name: REST_PREFIX + POINTS_RESOURCE + "{coordinates}",
						verb: "GET",
						description: "Mimicks the weather.gov service.",
						prms: {
							path: [{
								name: "{coordinates}",
								description: "Like '37.75,-122.50'"
							}]
						}
					}, {
						// "/rest/gridpoints/MTR/85,126/forecast"
						name: REST_PREFIX + "/gridpoints/{station}/{grid-coordinates}/forecast",
						verb: "GET",
						description: "Mimicks the weather.gov service.",
						prms: {
							path: [{
								name: "{station}",
								description: "Like 'MTR'"
							}, {
								name: "{grid-coordinates}",
								description: "Like '85,126'"
							}]
						}
					}, {
						name: REST_PREFIX + OPLIST_RESOURCE,
						verb: "GET",
						description: "Returns REST operations list.",
						prms: null
					}
				]));
			}
		} else {
			// TODO Implement other methods (verb and resources) here
			res.writeHead(404, {'Content-Type': 'text/plain'});
			res.end(`${req.method} ${req.url} : Not Implemented`);
		}

	} else if (req.url.startsWith("/")) { // All the rest, assuming static resource, GET only for now.
		if (req.method === "GET") {
			let resource = req.url.substring("/".length);
			if (resource.length === 0) {
				console.log('Defaulting to index.html');
				resource = 'index.html';
			}
			if (resource.indexOf("?") > -1) {
				resource = resource.substring(0, resource.indexOf("?"));
			}

			let exist = fs.existsSync(workDir + '/' + resource);

			if (exist === true && fs.lstatSync(workDir + '/' + resource).isDirectory()) {
				// try adding index.html
				console.log('Defaulting to index.html...');
				let resourceBackup = resource;
				resource += ((resource.endsWith("/") ? "" : "/") + "index.html");
				exist = fs.existsSync(workDir + '/' + resource);
				if (!exist) {
					resource = resourceBackup;
				} else {
					console.log(" >> From " + resourceBackup + " to " + resource);
				}
			} else {
				if (verbose) {
					console.log(workDir + '/' + resource + " not found");
				}
			}
			console.log((exist === true ? "Loading static " : ">> Warning: not found << ") + req.url + " (" + resource + ")");

			fs.readFile(workDir + '/' + resource,
					(err, data) => {
						if (err) {
							res.writeHead(400);
							return res.end('Error loading ' + resource);
						}
						if (verbose) {
							console.log("Read resource content:\n---------------\n" + data + "\n--------------");
						}
						let contentType = "text/plain"; // Default
						if (resource.endsWith(".css") || resource.endsWith(".css.map")) {
							contentType = "text/css";
						} else if (resource.endsWith(".html")) {
							contentType = "text/html";
						} else if (resource.endsWith(".xml")) {
							contentType = "text/xml";
						} else if (resource.endsWith(".js") || resource.endsWith(".js.map") || resource.endsWith(".map")) {
							contentType = "text/javascript";
						} else if (resource.endsWith(".jpg")) {
							contentType = "image/jpg";
						} else if (resource.endsWith(".jpeg")) {
							contentType = "image/jpeg";
						} else if (resource.endsWith(".gif")) {
							contentType = "image/gif";
						} else if (resource.endsWith(".png")) {
							contentType = "image/png";
						} else if (resource.endsWith(".ico")) {
							contentType = "image/x-icon";
						} else if (resource.endsWith(".svg")) {
							contentType = "image/svg+xml";
						} else if (resource.endsWith(".woff")) {
							contentType = "application/x-font-woff";
						} else if (resource.endsWith(".ttf")) {
							contentType = "application/octet-stream";
						} else {
							console.log("+-------------------------------------------")
							console.log("| Un-managed content type for " + resource);
							console.log("| You should add it in '%s'", __filename);
							console.log("+-------------------------------------------")
						}

						res.writeHead(200, {'Content-Type': contentType});
						//  console.log('Data is ' + typeof(data));
						if (resource.endsWith(".jpg") ||
								resource.endsWith(".jpeg") ||
								resource.endsWith(".gif") ||
								resource.endsWith(".ico") ||
								resource.endsWith(".svg") ||
								resource.endsWith(".woff") ||
								resource.endsWith(".ttf") ||
								resource.endsWith(".png")) {
							//  res.writeHead(200, {'Content-Type': 'image/gif' });
							res.end(data, 'binary');
						} else {
							res.end(data.toString().replace('$PORT$', port.toString())); // Replace $PORT$ with the actual port value.
						}
					});
		}
	} else {
		console.log("Unmanaged request: [" + req.url + "]");
		respContent = "Response from " + req.url;
		res.writeHead(404, {'Content-Type': 'text/plain'});
		res.end(); // respContent);
	}
}; // HTTP Handler

/**
 * Helper function for escaping input strings
 */
function htmlEntities(str) {
	return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;')
			.replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/**
 * HTTP server
 */
console.log((new Date()) + ": Starting server on port " + port);
let server = http.createServer(handler);

process.on('uncaughtException', (err) => {
	if (err.errno === 'EADDRINUSE') {
		console.log("Address in use.");
	} else {
		console.log(err);
	}
	process.exit(1);
});

try {
	server.listen(port, () => {
		console.log((new Date()) + ": Server is listening on port " + port);
	});
} catch (err) {
	console.log("There was an error:");
	console.log(err);
}
