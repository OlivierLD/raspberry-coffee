/*
 * @author Olivier Le Diouris
 */
var initWS = function() 
{
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

var setValues = function(doc)
{
  try
  {
    var errMess = "";
    
    var json = doc;

    var displayWT    = (json.displayWT !== undefined ? json.displayWT : true);
    var displayAT    = (json.displayAT !== undefined ? json.displayAT : true);
    var displayGDT   = (json.displayGDT !== undefined ? json.displayGDT : false);
    var displayPRMSL = (json.displayPRMSL !== undefined ? json.displayPRMSL : false);
    var displayHUM   = (json.displayHUM !== undefined ? json.displayHUM : false);
    var displayVOLT  = (json.displayVOLT !== undefined ? json.displayVOLT : false);

    events.publish('show-hide-wt', displayWT);
    events.publish('show-hide-at', displayAT);
    events.publish('show-hide-gdt', displayGDT);
    events.publish('show-hide-prmsl', displayPRMSL);
    events.publish('show-hide-hum', displayHUM);
    events.publish('show-hide-volt', displayVOLT);

    try
    {
      var latitude  = parseFloat(json.lat);
//    console.log("latitude:" + latitude)
      var longitude = parseFloat(json.lng);
//    console.log("Pt:" + latitude + ", " + longitude);
      events.publish('pos', { 'lat': latitude,
                              'lng': longitude });
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "position");
    }
    // Displays
    try
    {
      var bsp = parseFloat(json.bsp);
      events.publish('bsp', bsp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "boat speed");
    }
     try
    {
      var log = parseFloat(json.log);
      events.publish('log', log);
    }
    catch (err)
    {
      console.log("Log problem...", err);
      errMess += ((errMess.length > 0?", ":"Problem with ") + "log (" + err + ")");
    }
    try
    {
      var gpsDate = parseFloat(json.gpstime);
      events.publish('gps-time', gpsDate);
    }
    catch (err)
    {
      console.log("GPS Date problem...", err);
      errMess += ((errMess.length > 0?", ":"Problem with ") + "GPS Date (" + err + ")");
    }    
    try
    {
      var hdg = parseFloat(json.hdg) % 360;
      events.publish('hdg', hdg);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "heading");
    }
    try
    {
      var twd = parseFloat(json.twd) % 360;
      events.publish('twd', twd);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "TWD");
    }
    try
    {
      var twa = parseFloat(json.twa);
      events.publish('twa', twa);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "TWA");
    }
    try
    {
      var tws = parseFloat(json.tws);
      events.publish('tws', tws);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "TWS");
    }
    try
    {
      var waterTemp = parseFloat(json.wtemp);
      events.publish('wt', waterTemp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "water temperature");
    }
    try
    {
      var airTemp = parseFloat(json.atemp);
      events.publish('at', airTemp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "air temperature");
    }
    try
    {
      var voltage = parseFloat(json.bat);
      if (voltage > 0) {
        events.publish('volt', voltage);
      }
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "Battery_Voltage");
    }
    try
    {
      var baro = parseFloat(json.prmsl);
      if (baro != 0) {
        events.publish('prmsl', baro);
      }
    }
    catch (err)
    {
    }
    try
    {
      var hum = parseFloat(json.hum);
      if (hum > 0) {
        events.publish('hum', hum);
      }
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "Relative_Humidity");
    }
    try
    {
      var aws = parseFloat(json.aws);
      events.publish('aws', aws);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "AWS");
    }    
    try
    {
      var awa = parseFloat(json.awa);
      events.publish('awa', awa);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "AWA");
    }    
    try
    {
      var cdr = parseFloat(json.cdr);
      events.publish('cdr', cdr);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "CDR");
    }
      
    try
    {
      var cog = parseFloat(json.cog);
      events.publish('cog', cog);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "COG");
    }
    try
    {
      var cmg = parseFloat(json.cmg);
      events.publish('cmg', cmg);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "CMG");
    }      
    try
    {
      var leeway = parseFloat(json.leeway);
      events.publish('leeway', leeway);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "Leeway");
    }      
    try
    {
      var csp = parseFloat(json.csp);
      events.publish('csp', csp);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "CSP");
    }    
    try
    {
      var sog = parseFloat(json.sog);
      events.publish('sog', sog);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "SOG");
    }
    // towp, vmgwind, vmgwp, b2wp
    try
    {
      var to_wp = json.towp;
      var b2wp = parseFloat(json.b2wp);
      events.publish('wp', { 'to_wp': to_wp,
                             'b2wp': b2wp });
    }
    catch (err)
    {
    }
    
    try
    {
      events.publish('vmg', { 'onwind': parseFloat(json.vmgwind),
                              'onwp':   parseFloat(json.vmgwp) });
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "VMG");
    }
    
    // perf
    try
    {
      var perf = parseFloat(json.perf);
      perf *= 100;
      events.publish('perf', perf);
    }
    catch (err)
    {
      errMess += ((errMess.length > 0?", ":"Problem with ") + "Perf");
    }
    
    if (errMess !== undefined)
      displayErr(errMess);
  }
  catch (err)
  {
    displayErr(err);
  }
};
