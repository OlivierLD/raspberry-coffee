"use strict";

var connection;

(function () {
  var ws = window.WebSocket || window.MozWebSocket;

  // if browser doesn't support WebSocket, just show some notification and exit
  if (!ws) {
    displayMessage('Sorry, but your browser does not support WebSockets.');
    return;
  }

  // open connection
  var rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
                          (document.location.port === "" ? "8080" : document.location.port);
  console.log(rootUri);
  connection = new WebSocket(rootUri); // 'ws://localhost:9876');

  connection.onopen = function () {
    displayMessage('Connected.')
  };

  connection.onerror = function (error) {
    // just in there were some problems with connection...
    displayMessage('Sorry, but there is some problem with your connection or the server is down.');
  };

  var h = 0, p = 0, r = 0;

  connection.onmessage = function (message) {
    console.log('onmessage:', message.data);
    var mess = JSON.parse(message.data);
    var payload = JSON.parse(mess.data.text.replace(/&quot;/g, '"'));
    document.getElementById('X').innerHTML = payload.pitch.toFixed(0);
    document.getElementById('Y').innerHTML = payload.roll.toFixed(0);
    document.getElementById('Z').innerHTML = payload.heading.toFixed(0);

    sendToCube(payload.pitch.toFixed(0), payload.heading.toFixed(0), payload.roll.toFixed(0));
//  sendToCube(payload.heading.toFixed(0) - h, payload.pitch.toFixed(0) - p, payload.roll.toFixed(0) - r);

    h = payload.heading.toFixed(0);
    p = payload.pitch.toFixed(0);
    r = payload.roll.toFixed(0);
  };

  /**
   * This method is optional. If the server wasn't able to respond to the
   * in 3 seconds then show some error message to notify the user that
   * something is wrong.
   */
  setInterval(function() {
    if (connection.readyState !== 1) {
      displayMessage('Unable to communicate with the WebSocket server. Trying again.');
    }
  }, 3000); // Ping

})();

var displayMessage = function(mess) {
  try {
    console.log(mess);
  } catch (err) {
    console.log(mess);
  }
};
