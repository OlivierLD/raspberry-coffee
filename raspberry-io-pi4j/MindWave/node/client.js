"use strict";

var connection;

(function () {
  // if user is running mozilla then use it's built-in WebSocket
//  window.WebSocket = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back
  var ws = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back

  // if browser doesn't support WebSocket, just show some notification and exit
//  if (!window.WebSocket) 

  if (!ws) {
    displayMessage('Sorry, but your browser does not support WebSockets.'); // TODO Fallback
    return;
  }

  // open connection
  var rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
                          (document.location.port === "" ? "8080" : document.location.port);
  console.log(rootUri);
  connection = new WebSocket(rootUri); // 'ws://localhost:9876');

  connection.onopen = function () {
    displayMessage('Connected.');
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
    if (json.type === 'message') { // TODO Get rid of the other types above
      // it's a single message
      var payload = JSON.parse((json.data.text).replace(/&quot;/g, '"'));
//    console.log("Received:", payload);
      if (payload.connected !== undefined) {
        console.log("Connected:" + payload.connected);
        displayMessage("Mindwave " + (payload.connected ? "connected" : "disconnected"));
      } else if (payload.raw !== undefined) {
        rawValue.setValue(payload.raw);
        graphdata.push(payload.raw);
        while (graphdata.length > 1000) {
          graphdata.splice(0, 1);          
        }
        redrawGraph();
      } else if (payload.attention !== undefined) {
        attValue.setValue(payload.attention);
      } else if (payload.meditation !== undefined) {
        medValue.setValue(payload.meditation);
      } else if (payload.noise !== undefined) {
        if (payload.noise !== 0) {
          displayMessage("Noise:" + payload.noise);
        }
      } else if (payload.aeg !== undefined) {
        console.log("AEG", payload.aeg); //  {"aeg":{"high-beta":16777104,"low-beta":3172,"mid-gamma":16777176,"low-gamma":16777088,"delta":16777109,"theta":16777109,"low-alpha":16777135,"high-alpha":16777124}}
      }
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
 
var displayMessage = function(mess) {
  var messList = statusFld.innerHTML;
  messList = (((messList !== undefined && messList.length) > 0 ? messList + '<br>' : '') + mess);
  statusFld.innerHTML = messList;
};

var resetStatus = function() {
  statusFld.innerHTML = "";
};

var redrawGraph = function() {
  var canvas = graph;
  var context = canvas.getContext('2d');
  var xScale, yScale;

  var data = graphdata;  
  var cWidth  = canvas.width;
  var cHeight = canvas.height;
  
  var maxi = Number.MIN_VALUE;
  var mini = 0;
  for (var i=0; i<data.length; i++) {
    maxi = Math.max(maxi, data[i]);
  }

  xScale = cWidth / data.length;
  yScale = cHeight / maxi;

  var gridXStep = Math.round((maxi - mini) / 3);
  var gridYStep = Math.round(data.length / 10);
  
  var smoothData = data;
  var _smoothData = [];
  var smoothWidth = 50;
  for (var i=0; i<smoothData.length; i++) {
    var yAccu = 0;
    for (var acc=i-(smoothWidth / 2); acc<i+(smoothWidth/2); acc++) {
      var y;
      if (acc < 0) {
        y = smoothData[0];
      } else if (acc > (smoothData.length - 1)){
        y = smoothData[smoothData.length - 1];
      } else {
        y = smoothData[acc];
      }
      yAccu += y;
    }
    yAccu = yAccu / smoothWidth;
    _smoothData.push(yAccu);
  }
  // Clear
  context.fillStyle = "white";
  context.fillRect(0, 0, canvas.width, canvas.height);    

  smoothData = _smoothData;
  if (false) {
    context.fillStyle = "LightGray";
    context.fillRect(0, 0, canvas.width, canvas.height);    
  } else {
    var grV = context.createLinearGradient(0, 0, 0, context.canvas.height);
    grV.addColorStop(0, 'rgba(0,0,0,0)');
    grV.addColorStop(1, 'cyan'); // "LightGray"); // '#000');

    context.fillStyle = grV;
    context.fillRect(0, 0, context.canvas.width, context.canvas.height);
  }
  // Horizontal grid
  for (var i=Math.round(mini); gridXStep>0 && i<maxi; i+=gridXStep) {
    context.beginPath();
    context.lineWidth = 1;
    context.strokeStyle = 'gray';
    context.moveTo(0, cHeight - (i - mini) * yScale);
    context.lineTo(cWidth, cHeight - (i - mini) * yScale);
    context.stroke();

    context.save();
    context.font = "bold 10px Arial"; 
    context.fillStyle = 'black';
    str = i.toString();
    len = context.measureText(str).width;
    context.fillText(str, cWidth - (len + 2), cHeight - ((i - mini) * yScale) - 2);
    context.restore();            
    context.closePath();
  }
  
  if (true) { // Raw data
    context.beginPath();
    context.lineWidth = 1;
    context.strokeStyle = 'green';

    context.moveTo(0 * xScale, cHeight - (data[0] - mini) * yScale);
    for (var i=1; i<data.length; i++) {
  //  context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
      context.lineTo(i * xScale, cHeight - (data[i] - mini) * yScale);
  //  context.stroke();
    }
    context.lineTo(context.canvas.width, context.canvas.height);
    context.lineTo(0, context.canvas.height);
    context.closePath();
    context.stroke(); 
    context.fillStyle = 'rgba(0, 255, 0, 0.35)';
    context.fill();
  }
  
  if (true) { // Smoothed data
    data = smoothData;
    
    context.beginPath();
    context.lineWidth = 3;
    context.strokeStyle = 'red';

    context.moveTo(0 * xScale, cHeight - (data[0] - mini) * yScale);
    for (var i=1; i<data.length; i++) {
//      context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
      context.lineTo(i * xScale, cHeight - (data[i] - mini) * yScale);
//      context.stroke();
    }
    // Close the shape, bottom
    context.lineTo(context.canvas.width, context.canvas.height);
    context.lineTo(0, context.canvas.height);

    context.closePath();
    context.stroke();
    context.fillStyle = 'rgba(255, 0, 0, 0.35)';
    context.fill();
  }
};
