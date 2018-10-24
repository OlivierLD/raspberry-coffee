/*
 * Weather station data parser
 * By OlivSoft
 * olivier@lediouris.net

 Sample data:
  [{
        "time": "2015-07-05 23:58:41",
        "wdir": 0,
        "gust": 0.00,
        "ws": 0.00,
        "rain": 0.000,
        "press": 1012.3800000,
        "atemp": 22.800,
        "hum": 72.499,
        "dew": 33.600
    }, {
        "time": "2015-07-06 00:08:40",
        "wdir": 270,
        "gust": 7.95,
        "ws": 4.74,
        "rain": 0.000,
        "press": 1012.4200000,
        "atemp": 22.700,
        "hum": 73.781,
        "dew": 32.000
    }, {...
      ]
 */

var JSONParser = {
  nmeaData : [],
  position : {},

  /*
    data look like
     [{
        "time": "2015-07-05 23:58:41",
        "wdir": 0,
        "gust": 0.00,
        "ws": 0.00,
        "rain": 0.000,
        "press": 1012.3800000,
        "atemp": 22.800,
        "hum": 72.499,
        "dew": 33.600
    }, {..} ]
  */

  parse : function(wsJSONContent, cb, cb2) {
    JSONParser.nmeaData  = [];
    var linkList = "";
    // For timestamps like 2015-07-05 23:58:41
    var regExp     = new RegExp("(\\d{4})-(\\d{2})-(\\d{2})\\s(\\d{2}):(\\d{2}):(\\d{2})");

    for (var i=0; i<wsJSONContent.length; i++) {
      var date  = wsJSONContent[i].time;
      var d = null;
      var matches = regExp.exec(date);
      if (matches !== null) {
        var y  = matches[1];
        var mo = matches[2];
        var d  = matches[3];
        var h  = matches[4];
        var mi = matches[5];
        var s  = matches[6];
        d = new Date(y, mo - 1, d, h, mi, s, 0);
      }
      var prmsl = wsJSONContent[i].press;
      var tws   = wsJSONContent[i].ws;
      var twd   = wsJSONContent[i].wdir;
      var rain  = wsJSONContent[i].rain;
      var temp  = wsJSONContent[i].atemp;
      var hum   = wsJSONContent[i].hum;
      var dew   = wsJSONContent[i].dew;

//      console.info("Line:" + date + ":" + tws);
      JSONParser.nmeaData.push(new NMEAData(d, prmsl, tws, twd, rain, temp, hum, dew));
    }
  }
};

var NMEAData = function(date, prmsl, tws, twd, rain, atemp, hum, dew) {
  var nmeaDate = date;
  var nmeaPrmsl = prmsl;
  var nmeaTws = tws;
  var nmeaTwd = twd;
  var nmeaRain = rain;
  var nmeaTemp = atemp;
  var nmeaHum = hum;
  var nmeaDew = dew;

  this.getNMEADate = function() { return nmeaDate; };

  this.getNMEAPrmsl = function() { return nmeaPrmsl; };

  this.getNMEATws = function() { return nmeaTws; };

  this.getNMEATwd = function() { return nmeaTwd; };

  this.getNMEARain = function() { return nmeaRain; };

  this.getNMEATemp = function() { return nmeaTemp; };

  this.getNMEAHum = function() { return nmeaHum; };

  this.getNMEADew = function() { return nmeaDew; };
};
