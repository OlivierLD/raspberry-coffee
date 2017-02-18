"use strict";

var connection;
var GRAPH_MAX_LEN = 600;

(function () {
  var ws = window.WebSocket || window.MozWebSocket;
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

  // most important part - incoming messages
  connection.onmessage = function (message) {
//  console.log('onmessage:' + message);
    // try to parse JSON message. 
    try {
      var str = JSON.parse(message.data);
      var volt = parseFloat(str);
      displayValue.animate(volt);
      graphBatteryData.push(new Tuple(graphBatteryData.length, volt));
      if (GRAPH_MAX_LEN !== undefined && graphBatteryData.length > GRAPH_MAX_LEN) {
        while (graphBatteryData.length > GRAPH_MAX_LEN) {
            graphBatteryData.splice(0, 1);
        }
  //    displayMessage('Trimming graph data (' + graphBatteryData.length + ') ' + arrayToString(graphBatteryData));
      }
      graph.drawGraph("graphCanvas", graphBatteryData); //, graphBatteryData.length);
    } catch (e) {
      displayMessage('This doesn\'t look like a valid value: ' + message.data);
      return;
    }
  };

  /**
   * This method is optional. If the server wasn't able to respond to the
   * in 3 seconds then show some error message to notify the user that
   * something is wrong.
   */
  setInterval(function() {
    if (connection.readyState !== 1) {
      displayMessage('Unable to communicate with the WebSocket server. Try again.');
    }
  }, 3000); // Ping
})();

var arrayToString = function(ar) {
  var str = "";
  for (var i=0; i<ar.length; i++) {
    str += ((str.length > 0 ? ", " : "") + ar[i].getY());
  }
  return str;
};

var displayMessage = function(mess) {
  if (statusFld !== undefined) {
      var messList = statusFld.innerHTML;
      messList = (((messList !== undefined && messList.length) > 0 ? messList + '<br>' : '') + mess);
      statusFld.innerHTML = messList;
      statusFld.scrollTop = statusFld.scrollHeight; // Scroll down
  }
};

var resetStatus = function() {
  statusFld.innerHTML = "";
};
