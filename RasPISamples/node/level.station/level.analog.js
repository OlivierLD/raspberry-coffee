/*
 * @author Olivier Le Diouris
 */
var displayLevel;
    
var init = function() 
{
  displayLevel = new AnalogDisplay('levelCanvas', 200, 8, 1, 0.1, true, 40);
  
  var connection;

  // if user is running mozilla then use it's built-in WebSocket
  //  window.WebSocket = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back
  var ws = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back

  // if browser doesn't support WebSocket, just show some notification and exit
  //  if (!window.WebSocket) 

  if (!ws) 
  {
    alert('Sorry, but your browser does not support WebSockets.'); // TODO Fallback
    return;
  }

  // open connection
  var rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
                          (document.location.port === "" ? "9876" : document.location.port);
  console.log(rootUri);
  connection = new WebSocket(rootUri); // 'ws://localhost:9876');

  connection.onopen = function () 
  {
    console.log('Connected.')
  };

  connection.onerror = function (error) 
  {
    // just in there were some problems with connection...
    alert('Sorry, but there is some problem with your connection or the server is down.');
  };

  connection.onmessage = function (message) 
  {
//  console.log('onmessage:' + JSON.stringify(message.data));
    var data = JSON.parse(message.data);
    setValues(data);
  };

};

var changeBorder = function(b) 
{
  displayLevel.setBorder(b);
};

var TOTAL_WIDTH = 1200;

var resizeDisplays = function(width)
{
  displayLevel.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH)); 
};
  
var setValues = function(doc)
{
  try
  {
    var errMess = "";
    
    var json = JSON.parse(doc.data.text);

    // Displays
    try
    {
      var level = parseInt(json["water-level"]);
      displayLevel.animate(level);
//    displayTWD.setValue(twd);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?"\n":"") + "Problem with Level...");
    }

    if (errMess !== undefined)
      document.getElementById("err-mess").innerHTML = errMess;
  }
  catch (err)
  {
    document.getElementById("err-mess").innerHTML = err;
  }
};

var lpad = function(str, pad, len)
{
  while (str.length < len)
    str = pad + str;
  return str;
};
