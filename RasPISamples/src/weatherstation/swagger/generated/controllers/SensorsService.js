'use strict';

exports.getSensors = function(args, res, next) {
  /**
   * parameters expected in the args:
  **/
    var examples = {};
  examples['application/json'] = "";
  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }
  
}

exports.readBme280 = function(args, res, next) {
  /**
   * parameters expected in the args:
  **/
    var examples = {};
  examples['application/json'] = {
  "altitude" : 1.3579000000000001069366817318950779736042022705078125,
  "temperature" : 1.3579000000000001069366817318950779736042022705078125,
  "humidity" : 1.3579000000000001069366817318950779736042022705078125,
  "pressure" : 1.3579000000000001069366817318950779736042022705078125
};
  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }
  
}

exports.readWind = function(args, res, next) {
  /**
   * parameters expected in the args:
  **/
    var examples = {};
  examples['application/json'] = {
  "rain" : "",
  "temperature" : 1.3579000000000001069366817318950779736042022705078125,
  "humidity" : 1.3579000000000001069366817318950779736042022705078125,
  "windspeed" : 1.3579000000000001069366817318950779736042022705078125,
  "winddir" : "",
  "pressure" : 1.3579000000000001069366817318950779736042022705078125
};
  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }
  
}

