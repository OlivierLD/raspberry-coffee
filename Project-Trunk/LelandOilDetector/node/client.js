"use strict";

var connection;

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

  connection.onopen = function() {
    displayMessage('Connected.')
  };

  connection.onerror = function (error) {
    // just in there were some problems with connection...
    displayMessage('Sorry, but there is some problem with your connection or the server is down.');
  };

  // most important part - incoming messages
  connection.onmessage = function (message) {
 // console.log('onmessage:' + message);
    // try to parse JSON message. 
    try {
      var json = JSON.parse(message.data);
    } catch (e) {
      displayMessage('This doesn\'t look like a valid JSON: ' + message.data);
      return;
    }

    // NOTE: if you're not sure about the JSON structure
    // check the server source code above
    if (json.type === 'message') { 
      // it's a single message
      var value = json.data.text;
      while (value.indexOf("&quot;") > -1) {
        value = value.replace("&quot;", "\"");
      }
      var payload = JSON.parse(value);

   // console.log('Setting value to ' + value);
      waterDisplayValue.setValue(payload.water);
      oilDisplayValue.setValue(Math.max(0, payload.oil - payload.water));
    } else {
      displayMessage('Hmm..., I\'ve never seen JSON like this: ' + json);
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

var sendMessage = function(msg) {
  if (!msg) {
    return;
  }
  // send the message as an ordinary text
  connection.send(msg);
};
 
var displayMessage = function(mess){
  var messList = statusFld.innerHTML;
  messList = (((messList !== undefined && messList.length) > 0 ? messList + '<br>' : '') + mess);
  statusFld.innerHTML = messList;
};

var resetStatus = function() {
  statusFld.innerHTML = "";
};
