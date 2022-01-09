"use strict";  // http://ejohn.org/blog/ecmascript-5-strict-mode-json-and-more/
/**
 * WebSocket server for NMEA
 * This one receives NMEA Strings, parses them (with NMEAParser.autoparse), and pushes the result into
 * a JSON cache (call fullContext, see below).
 *
 * CLI parameters are -verbose -port:XXXX
 *
 * Static requests must be prefixed with /data/, like in http://machine:9876/data/console.html
 *
 * When a string is received, it is re-broadcasted to all the connected WS clients.
 *
 * Moved to ES6
 */

// Optional. You will see this name in eg. 'ps' or 'top' command
process.title = 'node-nmea-parser';

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

console.log('To stop: Ctrl-C, or enter "quit" + [return] here in the console');
console.log("Usage: node " + __filename + " -verbose -port:XXXX");

// Port where we'll run the websocket server on
let port = 9876;
let verbose = false;

if (process.argv.length > 2) {
    for (let i=2; i<process.argv.length; i++) {
        if (process.argv[i] === "-verbose") {
            verbose = true;
        } else if (process.argv[i].startsWith("-port:")) {
            port = parseInt(process.argv[i].substr("-port:".length));
        }
    }
}

// websocket AND http servers
let webSocketServer = require('websocket').server;
let http = require('http');
let fs = require('fs');
let NMEAParser = require('./node/NMEAParser.js')

// HTTP Handler
let handler = (req, res) => {
    var respContent = "";
    if (verbose === true) {
        console.log("Speaking HTTP from " + __dirname);
        console.log("Server received an HTTP Request:\n" + req.method + "\n" + req.url + "\n-------------");
        console.log("ReqHeaders:" + JSON.stringify(req.headers, null, '\t'));
        console.log('Request:' + req.url);
        let prms = require('url').parse(req.url, true);
        console.log(prms);
        console.log("Search: [" + prms.search + "]");
        console.log("-------------------------------");
    }
    if (req.url.startsWith("/data/")) { // Static resource
        let resource = req.url.substring("/data/".length);
        if (resource.indexOf("?") > -1) {
            resource = resource.substring(0, resource.indexOf("?"));
        }
        console.log('Loading static ' + req.url + " (" + resource + ")");
        fs.readFile(__dirname + '/' + resource, (err, data) => {
                if (err) {
                    res.writeHead(500);
                    return res.end('Error loading ' + resource);
                }
                // if (verbose)
                //   console.log("Read resource content:\n---------------\n" + data + "\n--------------");
                let contentType = "text/html";
                if (resource.endsWith(".css")) {
                    contentType = "text/css";
                } else if (resource.endsWith(".html")) {
                    contentType = "text/html";
                } else if (resource.endsWith(".xml")) {
                    contentType = "text/xml";
                } else if (resource.endsWith(".js")) {
                    contentType = "text/javascript";
                } else if (resource.endsWith(".jpg")) {
                    contentType = "image/jpg";
                } else if (resource.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (resource.endsWith(".png")) {
                    contentType = "image/png";
                } else if (resource.endsWith(".ico")) {
                    contentType = "image/ico";
                }

                res.writeHead(200, {'Content-Type': contentType});
                //  console.log('Data is ' + typeof(data));
                if (resource.endsWith(".jpg") ||
                    resource.endsWith(".gif") ||
                    resource.endsWith(".ico") ||
                    resource.endsWith(".png")) {  // TODO More types here if needed
                    //  res.writeHead(200, {'Content-Type': 'image/gif' });
                    res.end(data, 'binary');
                } else {
                    res.end(data.toString().replace('$PORT$', port.toString())); // Replace $PORT$ with the actual port value.
                }
            });
    } else if (req.url === "/") {
        if (req.method === "POST") {
            let data = "";
            console.log("---- Headers ----");
            for (let item in req.headers) {
                console.log(item + ": " + req.headers[item]);
            }
            console.log("-----------------");

            req.on("data", (chunk) => {
                data += chunk;
            });

            req.on("end", () => {
                console.log("POST request: [" + data + "]");
                res.writeHead(200, {'Content-Type': 'application/json'});
                let status = {'status':'OK'};
                res.end(JSON.stringify(status));
            });
        }
    } else {
        console.log("Unmanaged request: [" + req.url + "]");
        //console.log(">>> " + JSON.stringify(req, null, 2));
        respContent = "Response from " + req.url;
        res.writeHead(404, {'Content-Type': 'text/plain'});
        res.end(); // respContent);
    }
}; // HTTP Handler


/**
 * Global variables
 */
// list of currently connected clients (users)
let clients = [ ];

/**
 * Helper function for escaping input strings
 */
let htmlEntities = (str) => {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
};

/**
 * HTTP server
 */
let server = http.createServer(handler);

server.listen(port, () => {
    console.log((new Date()) + " Server is listening on port " + port);
    console.log("Connect to [http://localhost:" + port + "/data/web/wsconsole.html]");
});

let fullContext = {};

let parseSentence = (data) => {
    try {
        let id = NMEAParser.validate(data); // Validation
        console.log(`ID: ${JSON.stringify(id)}`);
        if (id !== undefined) {
            try {
                let auto = NMEAParser.autoparse(data);
                console.log(`AUTO: ${JSON.stringify(auto)}`);
                try {
                    if (auto !== undefined && auto.type !== undefined) {
                        //  console.log(">> Autoparsed:" + auto.type);
                        fullContext.lastID = auto.type;
                        switch (auto.type.trim()) {
                            case "GSV":
                                if (auto.satData !== undefined) {
                                    fullContext.nbSat = auto.satData.length;
                                    fullContext.satellites = auto.satData;
                                }
                                break;
                            case "RMC":
                                fullContext.date = new Date(auto.epoch);
                                fullContext.gpstime = auto.epoch;
                                fullContext.gpsdatetime = auto.epoch;
                                fullContext.lat = auto.pos.lat;
                                fullContext.lng = auto.pos.lon;
                                fullContext.cog = auto.cog;
                                fullContext.sog = auto.sog;
                                break;
                            case "GGA":
                                fullContext.date = new Date(auto.epoch);
                                fullContext.latitude = auto.position.latitude;
                                fullContext.longitude = auto.position.longitude;
                                fullContext.altitude = auto.antenna.altitude;
                                break;
                            case "DPT":
                                fullContext.dbt = auto.depth;
                                break;
                            case "GLL":
                                fullContext.lat = auto.latitude;
                                fullContext.lng = auto.longitude;
                                break;
                            case "VWT":
                                fullContext.twa = auto.wind.dir
                                fullContext.tws = auto.wind.speed.knots;
                                break;
                            case "VHW":
                                fullContext.thdg = auto.heading.true;
                                fullContext.bsp = auto.speed.knots;
                                break;
                            case "MTW":
                                fullContext.wtemp = auto.temp;
                                break;
                            case "VDR":
                                fullContext.cdr = auto.current.dir.true;
                                fullContext.csp = auto.current.speed.knots;
                                break;
                            case "HDG":
                                fullContext.hdg = auto.hdg;
                                fullContext.D = auto.dec;
                                break;
                            case "MWV":
                                fullContext.awa = auto.wind.dir;
                                fullContext.aws = auto.wind.speed;
                                break;
                            case "MWD":
                                fullContext.twd = auto.wind.dir.true;
                                fullContext.tws = auto.wind.speed.knots;
                                break;
                            case "RMB":
                                fullContext.xte = auto.crosstack.error;
                                fullContext.wp = auto.waypoints.destination.id;
                                fullContext.d2wp = auto.range;
                                fullContext.b2wp = auto.bearing;
                                break;
                            case "VWR":
                                fullContext.awa = auto.wind.dir;
                                fullContext.aws = auto.wind.speed.knots;
                                break;
                            case "VLW":
                                fullContext.log = auto.total;
                                fullContext.daylog = auto.sincereset;
                                break;
                            default:
                                console.log("[" + auto.type.trim() + "] to be managed");
                                break;
                        }
                        if (verbose) {
                            console.log(">>> NMEA Cache:\n", JSON.stringify(fullContext, null, 2));
                        }
                    } else {
                        console.log(`>>> AutoParse failed for ${data} (${auto})`);
                        console.log(">>> NMEA:", JSON.stringify(fullContext));
                    }
                } catch (err) {
                    console.log(err);
                    console.log("AutoParsed (1):", auto, data);
                }
            } catch (err) {
                console.log(err);
                console.log("AutoParsed (2):", auto, data);
            }
        }
    } catch (err) {
        console.log(err);
    }
};

/**
 * WebSocket server
 */
let wsServer = new webSocketServer({
    // WebSocket server is tied to a HTTP server. WebSocket request is just
    // an enhanced HTTP request. For more info http://tools.ietf.org/html/rfc6455#page-6
    httpServer: server
});

// This callback function is called every time someone
// tries to connect to the WebSocket server
wsServer.on('request', (request) => {
    console.log((new Date()) + ' Connection from origin ' + request.origin + '.');

    // accept connection - you should check 'request.origin' to make sure that
    // client is connecting from your website
    // (http://en.wikipedia.org/wiki/Same_origin_policy)
    var connection = request.accept(null, request.origin);
    clients.push(connection);
    console.log((new Date()) + ' Connection accepted.');

    // user sent some message
    connection.on('message', (message) => {
        if (verbose) {
        //  console.log("On Message:" + JSON.stringify(message));
            console.log("On Message:", message);
        }
        if (message.type === 'utf8') {
            // accept only text
      //    console.log((new Date()) + ' Received Message: ' + message.utf8Data);
      //    console.log("Rebroadcasting: " + message.utf8Data);
            // Parse the NMEA Content here
            let nmeaData = message.utf8Data;
            let sentenceArray = nmeaData.split("\r\n");

            console.log(`\t>>> Will manage ${sentenceArray.length} sentences`);

            for (let n=0; n<sentenceArray.length; n++) {
                if (sentenceArray[n].trim().length > 0) {
                    parseSentence(sentenceArray[n]); // Populate the fullContext
                    // Broadcast the FULL context to the connected clients
                    // The client (wsReader) needs to be aware of that shape...

                    console.log(`\tBroadcasting the full cache ${JSON.stringify(fullContext)} to ${clients.length} clients.`);

                    for (let i = 0; i < clients.length; i++) {
                        clients[i].sendUTF(JSON.stringify(fullContext)); // Just re-broadcast.
                    }
                } else {
                  console.log("\tNothing!!");
                }
            }
        } else {
          if (true || verbose) {
            console.log(`Unsupported message type ${message.type}`);
          }
        }
    });

    // user disconnected
    connection.on('close', (code) => {
        console.log((new Date()) + ' Connection closed.');
        let nb = clients.length;
        for (var i=0; i<clients.length; i++) {
            if (clients[i] === connection) {
                clients.splice(i, 1);
                break;
            }
        }
        if (verbose) {
            console.log("We have (" + nb + "->) " + clients.length + " client(s) connected.");
        }
    });
});

process.on('SIGINT', done); // Ctrl C
process.stdin.resume();
process.stdin.setEncoding('utf8');

process.stdin.on('data', (text) => {
    // console.log('received data:', util.inspect(text));
    if (text.startsWith('quit')) {
        done();
    }
});

function done() {
    console.log("\nBye now!");
    process.exit();
};
