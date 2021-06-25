/**
 * To debug:
 *   Prompt> set HTTP_PROXY=http://www-proxy.us.oracle.com:80
 *   Prompt> npm install -g node-inspector
 *   Prompt> node-inspector
 *
 * From another console:
 *   Prompt> node --debug server.js
 */
"use strict";

process.title = 'node-leap-motion';

// Port where we'll run the websocket server
let port = 9876;

// websocket and http servers
let webSocketServer = require('websocket').server;
let http = require('http');
let fs = require('fs');

let verbose = false;

if (typeof String.prototype.startsWith != 'function') {
  String.prototype.startsWith = function (str) {
    return this.indexOf(str) === 0;
  };
}

if (typeof String.prototype.endsWith != 'function') {
  String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
  };
}

let handler = function(req, res) {
  let respContent = "";
  if (verbose) {
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
    console.log('Loading static ' + req.url + " (" + resource + ")");
    fs.readFile(__dirname + '/' + resource,
                (err, data) => {
                  if (err) {
                    res.writeHead(500);
                    return res.end('Error loading ' + resource);
                  }
                  if (verbose) {
                    console.log("Read resource content:\n---------------\n" + data + "\n--------------");
                  }
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
                    } else if (resource.endsWith(".svg")) {
                      contentType = "image/svg+xml";
                    } else {
                      console.log("+-------------------------------------------")
                      console.log("| Un-managed content type for " + resource);
                      console.log("+-------------------------------------------")
                  }

                  res.writeHead(200, {'Content-Type': contentType});
              //  console.log('Data is ' + typeof(data));
                  if (resource.endsWith(".jpg") ||
                      resource.endsWith(".gif") ||
                      resource.endsWith(".svg") ||
                      resource.endsWith(".png")) {
                //  res.writeHead(200, {'Content-Type': 'image/gif' });
                    res.end(data, 'binary');
                  } else {
                    res.end(data.toString().replace('$PORT$', port.toString())); // Replace $PORT$ with the actual port value.
                  }
                });
  } else if (req.url.startsWith("/verbose=")) {
    if (req.method === "GET") {
      verbose = (req.url.substring("/verbose=".length) === 'on');
      res.end(JSON.stringify({verbose: verbose?'on':'off'}));
    }
  } else if (req.url == "/") {
    if (req.method === "POST") {
      let data = "";
      if (verbose) {
        console.log("---- Headers ----");
        for (let item in req.headers) {
          console.log(item + ": " + req.headers[item]);
        }
        console.log("-----------------");
      }
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
    respContent = "Response from " + req.url;
    res.writeHead(404, {'Content-Type': 'text/plain'});
    res.end(); // respContent);
  }
}; // HTTP Handler


/**
 * Global variables
 */
// list of currently connected clients (users)
let clients = [];

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
});

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
  let connection = request.accept(null, request.origin);
  clients.push(connection);
  console.log((new Date()) + ' Connection accepted.');

  // user sent some message
  connection.on('message', (message) => {
    if (message.type === 'utf8') { // accept only text
//    console.log((new Date()) + ' Received Message: ' + message.utf8Data);
      try {
        let mess = JSON.parse(message.utf8Data);
        console.log('P:' + mess.pitch + ', R:' + mess.roll + ', Y:' + mess.yaw);
      } catch (err) {
        // Probably a simple string
//      console.log(">>> ERR >>> " + err + ' in ' + message.utf8Data);
      }

      //text: htmlEntities(message.utf8Data)
      let obj = {
        time: (new Date()).getTime(),
        text: message.utf8Data
      };
      // broadcast message to all connected clients. That's what this app is doing.
      let json = JSON.stringify({ type: 'message', data: obj });
      for (let i=0; i<clients.length; i++) {
        clients[i].sendUTF(json);
      }
    }
  });

  // user disconnected
  connection.on('close', (code) => { // Close
    let nb = clients.length;
    for (let i=0; i<clients.length; i++) {
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
