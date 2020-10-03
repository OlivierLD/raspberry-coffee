/**
 * This is a small and tiny Web server. Mostly serves static pages and files.
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

/**
 * Small Simple and Stupid little web server.
 * Very basic. Lighter than Express.
 *
 * prms are req (Request) and res (Response)
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
		} else if (req.url.startsWith(REST_PREFIX + OPLIST_RESOURCE)) {
			if (req.method === "GET") {
				res.writeHead(200, {'Content-Type': 'application/json'});
				res.end(JSON.stringify([
					{
						name: REST_PREFIX + GEOPOS_RESOURCE,
						verb: "GET",
						description: "Transform decimal coordinates into 'X Deg Min.mm' format.",
						prms: {
							path: {
								name: "{coordinates}",
								description: "Like '37.75,-122.50"
							}
						}
					},
					{
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
