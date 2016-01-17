"use strict";

var connection;

(function () 
{
  // if user is running mozilla then use it's built-in WebSocket
//  window.WebSocket = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back
  var ws = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back

  // if browser doesn't support WebSocket, just show some notification and exit
//  if (!window.WebSocket) 

  if (!ws) 
  {
    displayMessage('Sorry, but your browser does not support WebSockets.'); // TODO Fallback
    return;
  }

  // open connection
  var rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
                          (document.location.port === "" ? "8080" : document.location.port);
  console.log(rootUri);
  connection = new WebSocket(rootUri); // 'ws://localhost:9876');

  connection.onopen = function () 
  {
    displayMessage('Connected.')
  };

  connection.onerror = function (error) 
  {
    // just in there were some problems with connection...
    displayMessage('Sorry, but there is some problem with your connection or the server is down.');
  };

  connection.onmessage = function (message) 
  {
    console.log('onmessage:' + JSON.stringify(message.data));
    var data = JSON.parse(message.data);
    var value = data.data.text.replace(/&quot;/g, '"');
    var val   = JSON.parse(value);
    displayValue.setValue(val.value);
  };

  /**
   * This method is optional. If the server wasn't able to respond to the
   * in 3 seconds then show some error message to notify the user that
   * something is wrong.
   */
  setInterval(function() 
  {
    if (connection.readyState !== 1) 
    {
      displayMessage('Unable to communicate with the WebSocket server. Try again.');
    }
  }, 3000); // Ping

})();

var sendMessage = function(msg) 
{
  console.log("Sending:" + msg);
  if (!msg) 
  {
    return;
  }
  // send the message as an ordinary text
  connection.send(msg);
};
 
var displayMessage = function(mess)
{
  try
  {
    var messList = statusFld.innerHTML;
    messList = (((messList !== undefined && messList.length) > 0 ? messList + '<br>' : '') + mess);
    statusFld.innerHTML = messList;
  }
  catch (err)
  {
    console.log(mess);
  }
};

var resetStatus = function()
{
  statusFld.innerHTML = "";
};
