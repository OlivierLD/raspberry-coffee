/**
 * @author Olivier LeDiouris
 *
 * This is a small and tiny Web server. Mostly serves static pages and files.
 *
 * To debug:
 *   To install node-inspector, to do once:
 *   $ export HTTP_PROXY=http://www-proxy.us.oracle.com:80 # if needed for the install
 *   $ export HTTPS_PROXY=http://www-proxy.us.oracle.com:80 # if needed for the install
 *   $ npm install -g node-inspector
 *
 * To run node-inspector:
 *   $ node-inspector
 *
 * Then, from another console:
 *   $ node --debug server.js
 */
"use strict";

process.title = 'TinyNodeServer';

// HTTP port
var port = 8080;

var http = require('http');
var fs = require('fs');

var verbose = false;

console.log("----------------------------------------------------");
console.log("Usage: node " + __filename + " --verbose:true|false --port:XXXX");
console.log("----------------------------------------------------");

for (var i=0; i<process.argv.length; i++) {
  console.log("arg #%d: %s", i, process.argv[i]);
}

if (typeof String.prototype.startsWith !== 'function') {
  String.prototype.startsWith = function (str) {
    return this.indexOf(str) === 0;
  };
}

if (typeof String.prototype.endsWith !== 'function') {
  String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
  };
}

if (process.argv.length > 2) { // Process user's parameters
  for (var argc=2; argc<process.argv.length; argc++) {
    if (process.argv[argc].startsWith("--verbose:")) {
      var value = process.argv[argc].substring("--verbose:".length);
      if (value !== 'true' && value !== 'false') {
        console.log("Invalid verbose value [%s]. Only 'true' and 'false' are supported.", value);
        process.exit(1); // BAM!
      }
      verbose = (value === 'true');
    } else if (process.argv[argc].startsWith("--port:")) {
      var value = process.argv[argc].substring("--port:".length);
      try {
        port = parseInt(value);
      } catch (err) {
        console.log("Invalid integer for port value %s.", value);
        process.exit(1);
      }
    } else {
      console.log("Unsupported parameter %s, ignored.", process.argv[argc]);
    }
  }
}

/**
 * Small Simple and Stupid little web server.
 * Very basic. Lighter than Express.
 */
var handler = function(req, res) {
  var respContent = "";
  if (verbose) {
    console.log("Speaking HTTP from " + __dirname);
    console.log("Server received an HTTP Request:\n" + req.method + "\n" + req.url + "\n-------------");
    console.log("ReqHeaders:" + JSON.stringify(req.headers, null, '\t'));
    console.log('Request:' + req.url);
    var prms = require('url').parse(req.url, true);
    console.log(prms);
    console.log("Search: [" + prms.search + "]");
    console.log("-------------------------------");
  }
  if (req.url.startsWith("/verbose=")) {
    if (req.method === "GET") {
      verbose = (req.url.substring("/verbose=".length) === 'on');
      res.end(JSON.stringify({verbose: verbose?'on':'off'}));
    }
  } else if (req.url.startsWith("/")) { // Assuming static resource
    if (req.method === "GET") {
      var resource = req.url.substring("/".length);
      if (resource.length === 0) {
        console.log('Defaulting to index.html');
        resource = 'index.html';
      }
	    var exist = fs.existsSync(__dirname + '/' + resource);

      if (exist === true && fs.lstatSync(__dirname + '/' + resource).isDirectory()) {
      	// try adding index.html
	      console.log('Defaulting to index.html...');
	      var resourceBackup = resource;
	      resource += ((resource.endsWith("/") ? "" : "/") + "index.html");
	      exist = fs.existsSync(__dirname + '/' + resource);
	      if (!exist) {
	      	resource = resourceBackup;
	      } else {
	      	console.log(" >> From " + resourceBackup + " to " + resource);
	      }
      } else {
      	if (verbose) {
		      console.log(__dirname + '/' + resource + " not found");
	      }
      }
      console.log((exist === true ? "Loading static " : ">> Warning: not found << ") + req.url + " (" + resource + ")");

      fs.readFile(__dirname + '/' + resource,
                  function (err, data) {
                    if (err) {
                      res.writeHead(400);
                      return res.end('Error loading ' + resource);
                    }
                    if (verbose) {
                      console.log("Read resource content:\n---------------\n" + data + "\n--------------");
                    }
                    var contentType = "text/plain"; // Default
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
                    } else if (resource.endsWith(".ttf")) {
	                    contentType = "application/x-font-ttf";
                    } else if (resource.endsWith(".woff")) {
                      contentType = "application/x-font-woff";
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
var htmlEntities = function(str) {
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
};

/**
 * HTTP server
 */
console.log((new Date()) + ": Starting server on port " + port);
var server = http.createServer(handler);

process.on('uncaughtException', function(err) {
	if (err.errno === 'EADDRINUSE') {
		console.log("Address in use.");
	} else {
		console.log(err);
	}
	process.exit(1);
});

try {
	server.listen(port, function () {
		console.log((new Date()) + ": Server is listening on port " + port);
	});
} catch (err) {
  console.log("There was an error:");
  console.log(err);
}
