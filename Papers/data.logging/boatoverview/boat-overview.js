(function webpackUniversalModuleDefinition(root, factory) {
	if(typeof exports === 'object' && typeof module === 'object')
		module.exports = factory();
	else if(typeof define === 'function' && define.amd)
		define("boat-overview", [], factory);
	else if(typeof exports === 'object')
		exports["boat-overview"] = factory();
	else
		root["boat-overview"] = factory();
})(window, function() {
return /******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = "./BoatOverview.js");
/******/ })
/************************************************************************/
/******/ ({

/***/ "../utilities/Utilities.js":
/*!*********************************!*\
  !*** ../utilities/Utilities.js ***!
  \*********************************/
/*! exports provided: lpad, rpad, toRadians, toDegrees, deadReckoning, calculateGreatCircle, calculateGreatCircleInDegrees, decToSex, getDir */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "lpad", function() { return lpad; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "rpad", function() { return rpad; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "toRadians", function() { return toRadians; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "toDegrees", function() { return toDegrees; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "deadReckoning", function() { return deadReckoning; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "calculateGreatCircle", function() { return calculateGreatCircle; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "calculateGreatCircleInDegrees", function() { return calculateGreatCircleInDegrees; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "decToSex", function() { return decToSex; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getDir", function() { return getDir; });
/**
 *
 * Misc utilities used all over the place.
 */
function lpad(str, len, pad) {
  let s = str;

  while (s.length < len) {
    s = (pad === undefined ? ' ' : pad) + s;
  }

  return s;
}
function rpad(str, len, pad) {
  let s = str;

  while (s.length < len) {
    s += pad === undefined ? ' ' : pad;
  }

  return s;
}
function toRadians(deg) {
  return deg * (Math.PI / 180);
}
function toDegrees(rad) {
  return rad * (180 / Math.PI);
}
/**
 *
 * @param start { lat: xx, lng: xx }, L & G in Degrees
 * @param dist distance in nm
 * @param bearing route in Degrees
 * @return DR Position, L & G in Degrees
 */

function deadReckoning(start, dist, bearing) {
  let radianDistance = toRadians(dist / 60);
  let finalLat = Math.asin(Math.sin(toRadians(start.lat)) * Math.cos(radianDistance) + Math.cos(toRadians(start.lat)) * Math.sin(radianDistance) * Math.cos(toRadians(bearing)));
  let finalLng = toRadians(start.lng) + Math.atan2(Math.sin(toRadians(bearing)) * Math.sin(radianDistance) * Math.cos(toRadians(start.lat)), Math.cos(radianDistance) - Math.sin(toRadians(start.lat)) * Math.sin(finalLat));
  finalLat = toDegrees(finalLat);
  finalLng = toDegrees(finalLng);
  return {
    lat: finalLat,
    lng: finalLng
  };
}
const TO_NORTH = 0;
const TO_SOUTH = 1;
const TO_EAST = 2;
const TO_WEST = 3;
/**
 * All in Radians
 *
 * @param start
 * @param arrival
 * @param nbPoints
 * @returns {Array}
 */

function calculateGreatCircle(start, arrival, nbPoints) {
  var ewDir;
  var nsDir;

  if (arrival.lat > start.lat) {
    nsDir = TO_NORTH;
  } else {
    nsDir = TO_SOUTH;
  }

  if (arrival.lng > start.lng) {
    ewDir = TO_EAST;
  } else {
    ewDir = TO_WEST;
  }

  if (Math.abs(arrival.lng - start.lng) > Math.PI) {
    if (ewDir == TO_EAST) {
      ewDir = TO_WEST;
      arrival.lng = arrival.lng - 2 * Math.PI;
    } else {
      ewDir = TO_EAST;
      arrival.lng = 2 * Math.PI + arrival.lng;
    }
  }

  let deltaG = arrival.lng - start.lng;
  let route = [];
  let interval = deltaG / nbPoints;
  let smallStart = {
    lat: start.lat,
    lng: start.lng
  };

  for (let g = start.lng; route.length <= nbPoints; g += interval) {
    let deltag = arrival.lng - g;
    let tanStartAngle = Math.sin(deltag) / (Math.cos(smallStart.lat) * Math.tan(arrival.lat) - Math.sin(smallStart.lat) * Math.cos(deltag));
    let smallL = Math.atan(Math.tan(smallStart.lat) * Math.cos(interval) + Math.sin(interval) / (tanStartAngle * Math.cos(smallStart.lat)));
    let rpG = g + interval;

    if (rpG > Math.PI) {
      rpG -= 2 * Math.PI;
    }

    if (rpG < -Math.PI) {
      rpG = 2 * Math.PI + rpG;
    }

    let routePoint = {
      lat: smallL,
      lng: rpG
    };
    let ari = toDegrees(Math.atan(tanStartAngle));

    if (ari < 0.0) {
      ari = Math.abs(ari);
    }

    var _nsDir;

    if (routePoint.lat > smallStart.lat) {
      _nsDir = TO_NORTH;
    } else {
      _nsDir = TO_SOUTH;
    }

    let arrG = routePoint.lng;
    let staG = smallStart.lng;

    if (Math.sign(arrG) != Math.sign(staG)) {
      if (Math.sign(arrG) > 0) {
        arrG -= 2 * Math.PI;
      } else {
        arrG = Math.PI - arrG;
      }
    }

    var _ewDir;

    if (arrG > staG) {
      _ewDir = TO_EAST;
    } else {
      _ewDir = TO_WEST;
    }

    let _start = 0.0;

    if (_nsDir == TO_SOUTH) {
      _start = 180;

      if (_ewDir == TO_EAST) {
        ari = _start - ari;
      } else {
        ari = _start + ari;
      }
    } else {
      if (_ewDir == TO_EAST) {
        ari = _start + ari;
      } else {
        ari = _start - ari;
      }
    }

    while (ari < 0.0) {
      ari += 360;
    }

    route.push({
      pos: smallStart,
      z: arrival === smallStart ? null : ari
    });
    smallStart = routePoint;
  }

  return route;
}
function calculateGreatCircleInDegrees(start, arrival, nbPoints) {
  let radRoute = calculateGreatCircle({
    lat: toRadians(start.lat),
    lng: toRadians(start.lng)
  }, {
    lat: toRadians(arrival.lat),
    lng: toRadians(arrival.lng)
  }, nbPoints);
  let degRoute = [];
  radRoute.forEach(pt => {
    degRoute.push({
      pos: {
        lat: toDegrees(pt.pos.lat),
        lng: toDegrees(pt.pos.lng)
      },
      z: pt.z
    });
  });
  return degRoute;
}
function decToSex(val, ns_ew) {
  let absVal = Math.abs(val);
  let intValue = Math.floor(absVal);
  let dec = absVal - intValue;
  let i = intValue;
  dec *= 60; //    let s = i + "°" + dec.toFixed(2) + "'";
  //    let s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";

  let s = "";

  if (ns_ew !== undefined) {
    if (val < 0) {
      s += ns_ew === 'NS' ? 'S' : 'W';
    } else {
      s += ns_ew === 'NS' ? 'N' : 'E';
    }

    s += " ";
  } else {
    if (val < 0) {
      s += '-';
    }
  }

  s += i + "°" + dec.toFixed(2) + "'";
  return s;
}
function getDir(x, y) {
  let dir = 0.0;

  if (y != 0) {
    dir = toDegrees(Math.atan(x / y));
  }

  if (x <= 0 || y <= 0) {
    if (x > 0 && y < 0) {
      dir += 180;
    } else if (x < 0 && y > 0) {
      dir += 360;
    } else if (x < 0 && y < 0) {
      dir += 180;
    } else if (x == 0) {
      if (y > 0) {
        dir = 0.0;
      } else {
        dir = 180;
      }
    } else if (y == 0) {
      if (x > 0) {
        dir = 90;
      } else {
        dir = 270;
      }
    }
  }

  dir += 180;

  while (dir >= 360) {
    dir -= 360;
  }

  return dir;
}

/***/ }),

/***/ "./BoatOverview.js":
/*!*************************!*\
  !*** ./BoatOverview.js ***!
  \*************************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../utilities/Utilities.js */ "../utilities/Utilities.js");
function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _wrapNativeSuper(Class) { var _cache = typeof Map === "function" ? new Map() : undefined; _wrapNativeSuper = function _wrapNativeSuper(Class) { if (Class === null || !_isNativeFunction(Class)) return Class; if (typeof Class !== "function") { throw new TypeError("Super expression must either be null or a function"); } if (typeof _cache !== "undefined") { if (_cache.has(Class)) return _cache.get(Class); _cache.set(Class, Wrapper); } function Wrapper() { return _construct(Class, arguments, _getPrototypeOf(this).constructor); } Wrapper.prototype = Object.create(Class.prototype, { constructor: { value: Wrapper, enumerable: false, writable: true, configurable: true } }); return _setPrototypeOf(Wrapper, Class); }; return _wrapNativeSuper(Class); }

function isNativeReflectConstruct() { if (typeof Reflect === "undefined" || !Reflect.construct) return false; if (Reflect.construct.sham) return false; if (typeof Proxy === "function") return true; try { Date.prototype.toString.call(Reflect.construct(Date, [], function () {})); return true; } catch (e) { return false; } }

function _construct(Parent, args, Class) { if (isNativeReflectConstruct()) { _construct = Reflect.construct; } else { _construct = function _construct(Parent, args, Class) { var a = [null]; a.push.apply(a, args); var Constructor = Function.bind.apply(Parent, a); var instance = new Constructor(); if (Class) _setPrototypeOf(instance, Class.prototype); return instance; }; } return _construct.apply(null, arguments); }

function _isNativeFunction(fn) { return Function.toString.call(fn).indexOf("[native code]") !== -1; }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

/*
 * HTML Color names: https://www.w3schools.com/colors/colors_names.asp
 *
 * Attributes:
 * Values: width, height, BSP, HDG, AWS, AWA, TWS, TWA, TWD, CDR, CSP, Leeway, CMG, VMG, B2WP, Decl, dev
 * Booleans: displayCurrent, displayLabels, displayVMG, vmgOnWind or vmgOnWP, with D, d, W
 * Options: BoatShape (mono, cata, tri, plane), WPName
 *
 * Designed to represent the data coming from the NavServer.
 * NOTE: The consistency of the data is not maintained by the Component!!
 * Example:
{
  "Damping": 30,
  "HDG Offset": 0,
  "To Waypoint": "RANGI   ",
  "CDR": {
    "angle": 115.39230004857473
  },
  "Daily": {
    "distance": 12.2
  },
  "Max Leeway": 10,
  "VMG on Wind": -3.8584310339481522,
  "HDG c.": {
    "angle": 213
  },
  "CMG": {
    "angle": 222.01692220976113
  },
  "BSP": {
    "speed": 7.1
  },
  "TWA": {
    "angle": -130.38922223613739
  },
  "TWD": {
    "angle": 91
  },
  "Current calculated with damping": {
    "30000": {
      "bufferLength": 30000,
      "speed": {
        "speed": 0.32396646536173773
      },
      "direction": {
        "angle": 48.891255868064114
      },
      "nbPoints": 11,
      "oldest": "Sun, 2010 Nov 21 22:07:44 UTC",
      "latest": "Sun, 2010 Nov 21 22:07:44 UTC",
      "len": 30000
    },
    "60000": {
      "bufferLength": 60000,
      "speed": {
        "speed": 0.32396646536173773
      },
      "direction": {
        "angle": 48.891255868064114
      },
      "nbPoints": 11,
      "oldest": "Sun, 2010 Nov 21 22:07:14 UTC",
      "latest": "Sun, 2010 Nov 21 22:07:44 UTC",
      "len": 30000
    },
    "600000": {
      "bufferLength": 600000,
      "speed": {
        "speed": 0.32396646536173773
      },
      "direction": {
        "angle": 48.891255868064114
      },
      "nbPoints": 11,
      "oldest": "Sun, 2010 Nov 21 22:07:14 UTC",
      "latest": "Sun, 2010 Nov 21 22:07:44 UTC",
      "len": 30000
    }
  },
  "Position": {
    "lat": -9.108366666666667,
    "lng": -140.20933333333332
  },
  "Log": {
    "distance": 3013
  },
  "Solar Time": {
    "date": "Nov 21, 2010, 4:46:56 AM",
    "fmtDate": {
      "epoch": 0,
      "year": 0,
      "month": 0,
      "day": 0,
      "hour": 12,
      "min": 46,
      "sec": 56
    }
  },
  "BSP Factor": 1,
  "Set and Drift": {
    "speed": 0.32,
    "angle": 49
  },
  "From Waypoint": "",
  "TWS": {
    "speed": 18.5
  },
  "GPS Time": {
    "date": "Nov 21, 2010, 2:07:47 PM",
    "fmtDate": {
      "epoch": 0,
      "year": 0,
      "month": 0,
      "day": 0,
      "hour": 22,
      "min": 7,
      "sec": 47
    }
  },
  "Distance to WP": {
    "distance": 561.7
  },
  "AWS Factor": 1,
  "Water Temperature": {
    "temperature": 26.5
  },
  "Small Distance": 0.06425312432932662,
  "NMEA": "$CCVDR,49.0,T,39.0,M,0.32,N*0F\r\n",
  "NMEA_AS_IS": {
    "VLW": "$IIVLW,03013,N,012.2,N*53\r",
    "VHW": "$IIVHW,,,213,M,07.1,N,,*62\r",
    "VDR": "$CCVDR,49.0,T,39.0,M,0.32,N*0F\r\n",
    "GLL": "$IIGLL,0906.498,S,14012.558,W,220745,A,A*5D\r",
    "RMB": "$IIRMB,A,3.00,R,,RANGI   ,,,,,561.70,230,06.7,V,A*02\r",
    "RMC": "$IIRMC,220747,A,0906.502,S,14012.560,W,06.9,215,211110,10,E,A*00\r",
    "DPT": "$IIDPT,001.3,+0.7,*42\r",
    "HDG": "$IIHDG,211,,,10,E*11\r",
    "MWV": "$CCMWV,230.0,T,018.5,N,A*36\r\n",
    "VWR": "$IIVWR,109,L,15.8,N,,,,*7B\r",
    "MTW": "$IIMTW,+26.5,C*39\r",
    "VWT": "$CCVWT,130.4,L,18.5,N,9.5,M,34.3,K*53\r\n",
    "MWD": "$CCMWD,091.0,T,081.0,M,18.5,N,9.5,M*75\r\n"
  },
  "D": {
    "angle": 10
  },
  "XTE": {
    "distance": 3
  },
  "AWA": {
    "angle": -109
  },
  "Depth": {
    "depthInMeters": 2
  },
  "Bearing to WP": {
    "angle": 230
  },
  "W": {
    "angle": 9.01692220976113
  },
  "Speed to WP": {
    "speed": 6.7
  },
  "COG": {
    "angle": 215
  },
  "AWS": {
    "speed": 15.8
  },
  "HDG true": {
    "angle": 222.01692220976113
  },
  "AWA Offset": 0,
  "CSP": {
    "speed": 0.8796925520240182
  },
  "d": {
    "angle": -0.9830777902388692
  },
  "Default Declination": {
    "angle": 14
  },
  "Deviation file name": "dp_2011_04_15.csv",
  "HDG mag.": {
    "angle": 212.01692220976113
  },
  "SOG": {
    "speed": 6.9
  },
  "Leeway": {
    "angle": 0
  },
  "GPS Date & Time": {
    "date": "Nov 21, 2010, 2:07:47 PM",
    "epoch": 1290377267000,
    "fmtDate": {
      "epoch": 1290377267000,
      "year": 2010,
      "month": 11,
      "day": 21,
      "hour": 22,
      "min": 7,
      "sec": 47
    }
  },
  "WayPoint pos": {
    "lat": 0,
    "lng": 0
  },
  "VMG to Waypoint": 6.664888201394572,
  "Steer": "R"
}
 *
 */
var boatOverviewVerbose = false;
var BOAT_OVERVIEW_TAG_NAME = 'boat-overview';
var boatOverviewDefaultColorConfig = {
  displayBackgroundGradient: {
    from: 'silver',
    to: 'lightgray'
  },
  gridColor: 'gray',
  twArrowColor: 'black',
  bspArrowColor: 'red',
  cmgArrowColor: 'cyan',
  awArrowColor: 'blue',
  gpsWsArrowColor: 'coral',
  vmgArrowColor: 'red',
  dDWArrowColor: 'yellow',
  currentArrowColor: 'royalblue',
  boatFillColor: 'silver',
  boatOutlineColor: 'blue',
  nmeaDataDisplayColor: 'royalblue',
  calculatedDataDisplayColor: 'darkcyan',
  vmgDataDisplayColor: 'red',
  dDWDataDisplayColor: 'yellow'
};

/* global HTMLElement */

var BoatOverview =
/*#__PURE__*/
function (_HTMLElement) {
  _inherits(BoatOverview, _HTMLElement);

  _createClass(BoatOverview, null, [{
    key: "observedAttributes",
    get: function get() {
      // That's a big one...
      return ["width", // Integer. Canvas width
      "height", // Integer. Canvas height
      "bsp", // Float. Boat Speed Numeric value, knots
      "hdg", // Integer, True Heading [0..360]
      "aws", // Float. App Wind Speed Numeric value, knots
      "awa", // Integer. App Wind Angle [-180..180] Numeric value
      "tws", // Float. True Wind Speed Numeric value, knots
      "twa", // Integer. True Wind Angle [-180..180] Numeric value
      "twd", // Integer. True Wind Direction [0..360] Numeric value
      "vmg", // Float. Velocity Made Good value, knots
      "sog", // Float. Speed Over Ground Numeric value, knots
      "cog", // Integer. Course Over Ground [0..360] Numeric value
      "cdr", // Integer. Current Direction [0..360] Numeric value
      "csp", // Float. Current Speed Numeric value, knots
      "lwy", // Float. Leeway [-180..180] Numeric value
      "cmg", // Integer. Course Made Good [0..360] Numeric value
      "b2wp", // Integer. Bearing to Next Way Point [0..360] Numeric value
      "decl", // Float. Magnetic Declination Numeric value +/-
      "dev", // Float. Magnetic deviation Numeric value +/-
      "with-current", // Boolean. Draw current
      "with-gps", // Boolean. Draw SOG & COG
      "with-wind", // Boolean. Draw wind (app & true)
      "with-true-wind", // Boolean. Draw true wind
      "with-labels", // Boolean. Draw Labels on graphic
      "with-vmg", // Boolean. Draw VMG
      "vmg-on-wind", // Boolean. true: on Wind, false: on WayPoint
      "with-w", // Boolean. Draw D & d
      "boat-shape", // String. MONO, CATA, TRI, PLANE
      "zoom-on-boat", // Float, enforce zoom in (>1) or out (<1). Default 1
      "wp-name" // String. Next WayPoint name
      ];
    }
  }]);

  function BoatOverview() {
    var _this;

    _classCallCheck(this, BoatOverview);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(BoatOverview).call(this));
    _this._shadowRoot = _this.attachShadow({
      mode: 'open'
    }); // 'open' means it is accessible from external JavaScript.
    // create and append a <canvas>

    _this.canvas = document.createElement("canvas");
    var fallbackElemt = document.createElement("h1");
    var content = document.createTextNode("This is a Boat Overview Web Component, on an HTML5 canvas");
    fallbackElemt.appendChild(content);

    _this.canvas.appendChild(fallbackElemt);

    _this.shadowRoot.appendChild(_this.canvas); // Default values


    _this._width = 600;
    _this._height = 500;
    _this._bsp = 0; // Boat Speed

    _this._hdg = 0; // True Heading

    _this._aws = 0; // App Wind Speed

    _this._awa = 0; // App Wind Angle

    _this._tws = 0; // True Wind Speed

    _this._twa = 0; // True Wind Angle

    _this._twd = 0; // True Wind Direction

    _this._vmg = 0; // Velocity Made Good

    _this._cog = 0; // Course Over Ground

    _this._sog = 0; // Speed Over Ground

    _this._cdr = 0; // Current Direction

    _this._csp = 0; // Current Speed

    _this._lwy = 0; // Leeway

    _this._cmg = 0; // Course Made Good

    _this._b2wp = 0; // Bearing to WP

    _this._Decl = 0; // Declination

    _this._dev = 0; // deviation

    _this._zoom = 1.0;
    _this._withCurrent = false;
    _this._withLabels = true;
    _this._withGPS = true;
    _this._withWind = true;
    _this._withTrueWind = true;
    _this._withVMG = true;
    _this._vmgOnWind = true; // False means vmg on WP

    _this._withW = false; // Requires D and d

    _this._boatShape = "MONO"; // "CATA"; //"MONO"; // "TRI"; // "PLANE";

    _this._wpName = "";
    _this._previousClassName = "";
    _this.boatOverviewColorConfig = boatOverviewDefaultColorConfig;
    _this.speedScale = 10; // Default value

    _this.WL_RATIO_COEFF = 0.75; // Ratio to apply to (3.5 * Width / Length)

    _this.BOAT_LENGTH = 100; // 50;

    return _this;
  } // Called whenever the custom element is inserted into the DOM.


  _createClass(BoatOverview, [{
    key: "connectedCallback",
    value: function connectedCallback() {
      if (boatOverviewVerbose) {
        console.log("connectedCallback invoked.");
      }

      this.repaint();
    } // Called whenever the custom element is removed from the DOM.

  }, {
    key: "disconnectedCallback",
    value: function disconnectedCallback() {
      if (boatOverviewVerbose) {
        console.log("disconnectedCallback invoked");
      }
    } // Called whenever an attribute is added, removed or updated.
    // Only attributes listed in the observedAttributes property are affected.

  }, {
    key: "attributeChangedCallback",
    value: function attributeChangedCallback(attrName, oldVal, newVal) {
      if (boatOverviewVerbose) {
        console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
      }

      switch (attrName) {
        case "width":
          this._width = parseInt(newVal);
          break;

        case "height":
          this._height = parseInt(newVal);
          break;

        case "bsp":
          this._bsp = parseFloat(newVal);
          break;

        case "hdg":
          this._hdg = parseInt(newVal);
          break;

        case "aws":
          this._aws = parseFloat(newVal);
          break;

        case "awa":
          this._awa = parseInt(newVal);
          break;

        case "tws":
          this._tws = parseFloat(newVal);
          break;

        case "twa":
          this._twa = parseInt(newVal);
          break;

        case "twd":
          this._twd = parseInt(newVal);
          break;

        case "vmg":
          this._vmg = parseFloat(newVal);
          break;

        case "sog":
          this._sog = parseFloat(newVal);
          break;

        case "cog":
          this._cog = parseInt(newVal);
          break;

        case "cdr":
          this._cdr = parseInt(newVal);
          break;

        case "csp":
          this._csp = parseFloat(newVal);
          break;

        case "lwy":
          this._lwy = parseFloat(newVal);
          break;

        case "cmg":
          this._cmg = parseInt(newVal);
          break;

        case "b2wp":
          this._b2wp = parseFloat(newVal);
          break;

        case "decl":
          this._Decl = parseFloat(newVal);
          break;

        case "dev":
          this._dev = parseFloat(newVal);
          break;

        case "zoom-on-boat":
          this._zoom = parseFloat(newVal);
          break;

        case "with-current":
          this._withCurrent = newVal === 'true';
          break;

        case "with-gps":
          this._withGPS = newVal === 'true';
          break;

        case "with-true-wind":
          this._withTrueWind = newVal === 'true';
          break;

        case "with-wind":
          this._withWind = newVal === 'true';
          break;

        case "with-labels":
          this._withLabels = newVal === 'true';
          break;

        case "with-vmg":
          this._withVMG = newVal === 'true';
          break;

        case "vmg-on-wind":
          this._vmgOnWind = newVal === 'true';
          break;

        case "with-w":
          this._withW = newVal === 'true';
          break;

        case "boat-shape":
          console.log('BoatShape changing');

          if (newVal === 'MONO' || newVal === 'CATA' || newVal === 'TRI' || newVal === 'PLANE') {
            this._boatShape = newVal;
          }

          break;

        case "wp-name":
          this._wpName = newVal;
          break;

        default:
          break;
      }

      this.repaint();
    } // Called whenever the custom element has been moved into a new document.

  }, {
    key: "adoptedCallback",
    value: function adoptedCallback() {
      if (boatOverviewVerbose) {
        console.log("adoptedCallback invoked");
      }
    } // Setters

  }, {
    key: "getColorConfig",
    // Component methods
    value: function getColorConfig(classNames) {
      var colorConfig = boatOverviewDefaultColorConfig;
      var classes = classNames.split(" ");

      for (var cls = 0; cls < classes.length; cls++) {
        var cssClassName = classes[cls];

        for (var s = 0; s < document.styleSheets.length; s++) {
          // console.log("Walking though ", document.styleSheets[s]);
          try {
            for (var r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
              var selector = document.styleSheets[s].cssRules[r].selectorText; //			console.log(">>> ", selector);

              if (selector !== undefined && (selector === '.' + cssClassName || selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(BOAT_OVERVIEW_TAG_NAME) > -1)) {
                // Cases like "tag-name .className"
                //				console.log("  >>> Found it! [%s]", selector);
                var cssText = document.styleSheets[s].cssRules[r].style.cssText;
                var cssTextElems = cssText.split(";");
                cssTextElems.forEach(function (elem) {
                  if (elem.trim().length > 0) {
                    var keyValPair = elem.split(":");
                    var key = keyValPair[0].trim();
                    var value = keyValPair[1].trim();

                    switch (key) {
                      case '--display-background-gradient-from':
                        colorConfig.displayBackgroundGradient.from = value;
                        break;

                      case '--display-background-gradient-to':
                        colorConfig.displayBackgroundGradient.to = value;
                        break;

                      case '--grid-color':
                        colorConfig.gridColor = value;
                        break;

                      case '--tw-arrow-color':
                        colorConfig.twArrowColor = value;
                        break;

                      case '--bsp-arrow-color':
                        colorConfig.bspArrowColor = value;
                        break;

                      case '--cmg-arrow-color':
                        colorConfig.cmgArrowColor = value;
                        break;

                      case '--aw-arrow-color':
                        colorConfig.awArrowColor = value;
                        break;

                      case '--gps-ws-arrow-color':
                        colorConfig.gpsWsArrowColor = value;
                        break;

                      case '--vmg-arrow-color':
                        colorConfig.vmgArrowColor = value;
                        break;

                      case '--variation-arrow-color':
                        colorConfig.dDWArrowColor = value;
                        break;

                      case '--current-arrow-color':
                        colorConfig.currentArrowColor = value;
                        break;

                      case '--boat-fill-color':
                        colorConfig.boatFillColor = value;
                        break;

                      case '--boat-outline-color':
                        colorConfig.boatOutlineColor = value;
                        break;

                      case '--nmea-display-color':
                        colorConfig.nmeaDataDisplayColor = value;
                        break;

                      case '--calculated-color':
                        colorConfig.calculatedDataDisplayColor = value;
                        break;

                      case '--variation-display-color':
                        colorConfig.dDWDataDisplayColor = value;
                        break;

                      case '--vmg-display-color':
                        colorConfig.vmgDataDisplayColor = value;
                        break;

                      default:
                        break;
                    }
                  }
                });
              }
            }
          } catch (err) {// Absorb
          }
        }
      }

      return colorConfig;
    }
  }, {
    key: "repaint",
    value: function repaint() {
      this.drawBoatOverview();
    }
  }, {
    key: "getCanvasCenter",
    value: function getCanvasCenter() {
      var cw = this.width;
      var ch = this.height;
      var distFromRight = Math.min(cw, ch) / 2;
      return {
        x: cw - distFromRight,
        y: ch / 2
      };
    }
  }, {
    key: "drawTrueWind",
    value: function drawTrueWind(context) {
      var cWidth = this.width;
      var cHeight = this.height;

      var _twd = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this.twd);

      context.beginPath();
      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;
      var windLength = this._zoom * this.tws * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dX = windLength * Math.sin(_twd);
      var dY = -windLength * Math.cos(_twd); // create a new line object

      var line = new Line(x + dX, y + dY, x, y); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.twArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.twArrowColor;
      context.lineWidth = 5;
      line.drawWithAnemoArrowheads(context);
      context.closePath();

      if (this._withLabels) {
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.twArrowColor;
        context.fillText("TWS:" + this.tws.toFixed(2) + " kts", x + dX, y + dY);
        context.fillText("TWA:" + this.twa + "°", x + dX, y + dY + 14);
      }
    }
  }, {
    key: "drawAppWind",
    value: function drawAppWind(context) {
      var cWidth = this.width;
      var cHeight = this.height;
      var wd = this._hdg + this.awa; // Direction the wind is blowing TO

      while (wd > 360) {
        wd -= 360;
      }

      var _awd = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](wd);

      context.beginPath();
      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;
      var windLength = this._zoom * this.aws * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dX = windLength * Math.sin(_awd);
      var dY = -windLength * Math.cos(_awd); // create a new line object

      var line = new Line(x + dX, y + dY, x, y); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.awArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.awArrowColor;
      context.lineWidth = 5;
      line.drawWithAnemoArrowheads(context);
      context.closePath();

      if (this._withLabels) {
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.awArrowColor;
        context.fillText("AWS:" + this.aws + " kts", x + dX, y + dY);
        context.fillText("AWA:" + this.awa + "°", x + dX, y + dY + 14);
      }
    }
  }, {
    key: "drawBSP",
    value: function drawBSP(context) {
      if (this._bsp === 0) {
        return;
      }

      var cWidth = this.width;
      var cHeight = this.height;

      var _hdg = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this._hdg);

      context.beginPath();
      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;
      var bspLength = this._zoom * this._bsp * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dX = bspLength * Math.sin(_hdg);
      var dY = -bspLength * Math.cos(_hdg); // create a new line object

      var line = new Line(x, y, x + dX, y + dY); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.bspArrowColor;
      context.lineWidth = 3;
      line.drawHollowArrow(context);
      context.closePath(); //    let metrics = context.measureText(valueToDisplay);
      //    len = metrics.width;

      if (this._withLabels) {
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.bspArrowColor;
        context.fillText("BSP:" + this._bsp.toFixed(2) + " kts", x + dX, y + dY);
        context.fillText("HDG:" + this._hdg.toFixed(0) + "°", x + dX, y + dY + 14);
      }
    }
  }, {
    key: "drawCMG",
    value: function drawCMG(context) {
      if (this._bsp === 0 || this.lwy === 0) {
        return;
      }

      var cWidth = this.width;
      var cHeight = this.height;

      var _hdg = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this._cmg);

      context.beginPath();
      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;
      var bspLength = this._zoom * this._bsp * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dX = bspLength * Math.sin(_hdg);
      var dY = -bspLength * Math.cos(_hdg); // create a new line object

      var line = new Line(x, y, x + dX, y + dY); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.cmgArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.cmgArrowColor;
      context.lineWidth = 5;
      line.drawWithArrowhead(context);
      context.closePath();

      if (this._withLabels) {
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.calculatedDataDisplayColor;
        context.fillText("CMG:" + this._cmg.toFixed(0) + "°", x + dX, y + dY);
      }
    }
  }, {
    key: "drawNorths",
    value: function drawNorths(context) {
      if (this._bsp === 0 || this._Decl === 0 && this._dev === 0) {
        return;
      }

      var cWidth = this.width;
      var cHeight = this.height; // Warning: Represent the Norths, not the headings!!!

      var magNorth = this._Decl;
      var compassNorth = magNorth + this._dev;

      while (magNorth < 0) {
        magNorth += 360;
      }

      while (compassNorth < 0) {
        compassNorth += 360;
      }

      var _magNorth = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](magNorth);

      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;
      var bspLength = this._zoom * this._bsp * (Math.min(cHeight, cWidth) / 2 / this.speedScale); // True North first

      var line = new Line(x, y, x, y - bspLength * 1.1);
      context.beginPath();
      context.strokeStyle = this.boatOverviewColorConfig.dDWArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.dDWArrowColor;
      context.lineWidth = 1;
      line.drawWithArrowhead(context);
      context.closePath();

      if (this._withLabels) {
        context.font = "12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.dDWDataDisplayColor;
        context.fillText("N", x, y - bspLength * 1.1);
      }

      context.beginPath();
      var dX = bspLength * Math.sin(_magNorth);
      var dY = -bspLength * Math.cos(_magNorth); // create a new line object

      line = new Line(x, y, x + dX, y + dY); // draw the line

      context.lineWidth = 5;
      line.drawWithArrowhead(context);
      context.closePath();

      if (this._withLabels) {
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.dDWDataDisplayColor;
        context.fillText("Mag N:" + magNorth.toFixed(0) + "°", x + dX, y + dY);
      }

      var _compassNorth = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](compassNorth);

      context.beginPath();
      dX = bspLength * 0.8 * Math.sin(_compassNorth);
      dY = -bspLength * 0.8 * Math.cos(_compassNorth); // create a new line object

      line = new Line(x, y, x + dX, y + dY); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.dDWArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.dDWArrowColor;
      context.lineWidth = 5;
      line.drawWithArrowhead(context);
      context.closePath();

      if (this._withLabels) {
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.dDWDataDisplayColor;
        context.fillText("Compass N:" + compassNorth.toFixed(0) + "°", x + dX, y + dY);
      }
    }
  }, {
    key: "drawSOG",
    value: function drawSOG(context) {
      if (this.sog === 0) {
        return;
      }

      var cWidth = this.width;
      var cHeight = this.height;

      var _hdg = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this.cog);

      context.beginPath();
      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;
      var bspLength = this._zoom * this.sog * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dX = bspLength * Math.sin(_hdg);
      var dY = -bspLength * Math.cos(_hdg); // create a new line object

      var line = new Line(x, y, x + dX, y + dY); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.gpsWsArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.gpsWsArrowColor;
      context.lineWidth = 5;
      line.drawWithArrowhead(context);
      context.closePath();

      if (this._withLabels) {
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.gpsWsArrowColor;
        context.fillText("SOG:" + this.sog + " kts", x + dX, y + dY);
        context.fillText("COG:" + this.cog + "°", x + dX, y + dY + 14);
        context.lineWidth = 1;
      }
    }
  }, {
    key: "drawVMG",
    value: function drawVMG(context) {
      var cWidth = this.width;
      var cHeight = this.height;
      var _hdg = 0;
      context.beginPath();
      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;

      if (this.vmgOnWind) {
        _hdg = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this.twd);
      } else {
        _hdg = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this.b2wp); // Display WP direction

        context.strokeStyle = this.boatOverviewColorConfig.vmgArrowColor;
        context.fillStyle = this.boatOverviewColorConfig.vmgArrowColor;
        context.lineWidth = 1;
        var len = 0.75 * Math.min(cHeight, cWidth) / 2;

        var _dX = len * Math.sin(_hdg);

        var _dY = -len * Math.cos(_hdg);

        var wpLine = new Line(x, y, x + _dX, y + _dY);
        wpLine.drawWithArrowhead(context);
        context.fillText(this._wpName, x + _dX, y + _dY);
      }

      var bspLength = this._zoom * this.vmg * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dX = bspLength * Math.sin(_hdg);
      var dY = -bspLength * Math.cos(_hdg); // create a new line object

      var line = new Line(x, y, x + dX, y + dY); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.vmgArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.vmgArrowColor;
      context.lineWidth = 5;
      line.drawWithArrowhead(context);
      context.closePath();

      if (this._withLabels) {
        context.save();
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.vmgArrowColor;
        context.fillText("VMG:" + this.vmg.toFixed(2) + " kts", x + dX, y + dY);
        context.restore();
      }

      if (context.setLineDash !== undefined) {
        context.setLineDash([5]);
        context.moveTo(x + dX, y + dY);

        var _cog = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this.cog);

        var sogLength = this._zoom * this.sog * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
        dX = sogLength * Math.sin(_cog);
        dY = -sogLength * Math.cos(_cog);
        context.lineTo(x + dX, y + dY);
        context.lineWidth = 1;
        context.stroke(); // Reset

        context.setLineDash([]);
      }
    }
  }, {
    key: "drawCurrent",
    value: function drawCurrent(context) {
      if (this.csp === 0) {
        return;
      }

      var cWidth = this.width;
      var cHeight = this.height;
      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;

      var _cmg = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this._cmg);

      var bspLength = this._zoom * this._bsp * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dXcmg = bspLength * Math.sin(_cmg);
      var dYcmg = -bspLength * Math.cos(_cmg);

      var _cog = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this.cog);

      var sogLength = this._zoom * this.sog * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dXcog = sogLength * Math.sin(_cog);
      var dYcog = -sogLength * Math.cos(_cog);
      context.beginPath(); // create a new line object

      var line = new Line(x + dXcmg, y + dYcmg, x + dXcog, y + dYcog); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.currentArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.currentArrowColor;
      context.lineWidth = 5;
      line.drawWithArrowhead(context);
      context.closePath();

      if (this._withLabels) {
        context.font = "bold 12px Arial";
        context.fillStyle = this.boatOverviewColorConfig.currentArrowColor;
        context.fillText("CSP:" + this.csp.toFixed(2) + " kts", x + dXcog, y + dYcog + 28); // + 14 not to overlap the SOG/COG

        context.fillText("CDR:" + this.cdr.toFixed(0) + "°", x + dXcog, y + dYcog + 42);
      }
    }
  }, {
    key: "drawVW",
    value: function drawVW(context) {
      // Velocity Wind
      if (this.sog === 0) {
        return;
      }

      var cWidth = this.width;
      var cHeight = this.height;
      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;
      var wd = this._hdg + this.awa; // Direction the wind is blowing TO

      while (wd > 360) {
        wd -= 360;
      }

      var _awd = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](wd);

      context.beginPath();
      var awLength = this._zoom * this.aws * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dXaw = awLength * Math.sin(_awd);
      var dYaw = -awLength * Math.cos(_awd);

      var _twd = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](this.twd);

      var twLength = this._zoom * this.tws * (Math.min(cHeight, cWidth) / 2 / this.speedScale);
      var dXtw = twLength * Math.sin(_twd);
      var dYtw = -twLength * Math.cos(_twd);
      context.beginPath(); // create a new line object

      var line = new Line(x + dXaw, y + dYaw, x + dXtw, y + dYtw); // draw the line

      context.strokeStyle = this.boatOverviewColorConfig.gpsWsArrowColor;
      context.fillStyle = this.boatOverviewColorConfig.gpsWsArrowColor;
      context.lineWidth = 5;
      line.drawWithAnemoArrowheads(context);
      context.closePath();
    }
  }, {
    key: "drawBoat",
    value: function drawBoat(context, trueHeading) {
      var x = [];
      var y = []; // Half, length

      var boatLength = this.BOAT_LENGTH * this._zoom;

      if (this.boatShape === 'MONO') {
        // Width
        x.push(this.WL_RATIO_COEFF * 0); // Bow
        //     Starboard

        x.push(this.WL_RATIO_COEFF * (1 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (1.5 * boatLength) / 7); // Transom, starboard
        //     Port

        x.push(this.WL_RATIO_COEFF * (-1.5 * boatLength) / 7); // Transom, port

        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1 * boatLength) / 7); // Length

        y.push(-4 * boatLength / 7); // Bow
        //      Starboard

        y.push(-3 * boatLength / 7);
        y.push(-1 * boatLength / 7);
        y.push(1 * boatLength / 7);
        y.push(3 * boatLength / 7); //     Port

        y.push(3 * boatLength / 7);
        y.push(1 * boatLength / 7);
        y.push(-1 * boatLength / 7);
        y.push(-3 * boatLength / 7);
      } else if (this.boatShape === 'CATA') {
        x.push(this.WL_RATIO_COEFF * 0); // Arm, front, center
        // Starboard

        x.push(this.WL_RATIO_COEFF * (1 * boatLength) / 7); // Arm starboard, hull side

        x.push(this.WL_RATIO_COEFF * (1.5 * boatLength) / 7); // Starboard bow

        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (1.8 * boatLength) / 7); // Starboard transform, ext

        x.push(this.WL_RATIO_COEFF * (1.2 * boatLength) / 7); // Starboard transform, int

        x.push(this.WL_RATIO_COEFF * (1 * boatLength) / 7); // Arm, back, starboard, hull side

        x.push(this.WL_RATIO_COEFF * (0 * boatLength) / 7); // Arm, back, starboard, center
        // Port

        x.push(this.WL_RATIO_COEFF * (-0 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1.2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1.8 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1.5 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1 * boatLength) / 7); // Length

        y.push(-1 * boatLength / 7); //   Starboard

        y.push(-1 * boatLength / 7);
        y.push(-4 * boatLength / 7); // Bow

        y.push(-1 * boatLength / 7);
        y.push(0 * boatLength / 7);
        y.push(1 * boatLength / 7);
        y.push(3 * boatLength / 7);
        y.push(3 * boatLength / 7);
        y.push(1 * boatLength / 7);
        y.push(1 * boatLength / 7); //    Port

        y.push(1 * boatLength / 7);
        y.push(1 * boatLength / 7); // Bow

        y.push(3 * boatLength / 7);
        y.push(3 * boatLength / 7);
        y.push(1 * boatLength / 7);
        y.push(0 * boatLength / 7);
        y.push(-1 * boatLength / 7);
        y.push(-4 * boatLength / 7);
        y.push(-1 * boatLength / 7);
      } else if (this.boatShape === 'TRI') {
        // Width
        x.push(this.WL_RATIO_COEFF * 0); // Bow, center hull
        // Starboard

        x.push(this.WL_RATIO_COEFF * (0.3 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (0.6 * boatLength) / 7); // Arm, front, starboard, inside

        x.push(this.WL_RATIO_COEFF * (1.6 * boatLength) / 7); // Arm, front, starboard, outside

        x.push(this.WL_RATIO_COEFF * (1.8 * boatLength) / 7); // Outrigger bow

        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (1.9 * boatLength) / 7); // Outrigger transom, ext

        x.push(this.WL_RATIO_COEFF * (1.7 * boatLength) / 7); // Outrigger transom, int

        x.push(this.WL_RATIO_COEFF * (1.6 * boatLength) / 7); // Arm, back, starboard, outside

        x.push(this.WL_RATIO_COEFF * (0.6 * boatLength) / 7); // Arm, back, starboard, inside

        x.push(this.WL_RATIO_COEFF * (0.3 * boatLength) / 7); // Main hull, transom starboard,
        // Port

        x.push(this.WL_RATIO_COEFF * (-0.3 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-0.6 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1.6 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1.7 * boatLength) / 7); // Outrigger transom, int

        x.push(this.WL_RATIO_COEFF * (-1.9 * boatLength) / 7); // Outrigger transom, ext

        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (-1.8 * boatLength) / 7); // Outrigger bow

        x.push(this.WL_RATIO_COEFF * (-1.6 * boatLength) / 7); // Arm, front, starboard, outside

        x.push(this.WL_RATIO_COEFF * (-0.6 * boatLength) / 7); // Arm, front, starboard, inside

        x.push(this.WL_RATIO_COEFF * (-0.3 * boatLength) / 7); // Length

        y.push(-4 * boatLength / 7); // Bow
        // Starboard

        y.push(-3 * boatLength / 7);
        y.push(-1 * boatLength / 7); // Starboard arm, front

        y.push(-1 * boatLength / 7); // Starboard arm, front, outrigger

        y.push(-2.6 * boatLength / 7); // Starboard outrigger bow

        y.push(-1.5 * boatLength / 7);
        y.push(1.5 * boatLength / 7);
        y.push(2.5 * boatLength / 7); // Starboard transom, ext

        y.push(2.5 * boatLength / 7); // Starboard transom, ext

        y.push(1 * boatLength / 7); // Starboard arm, back, outrigger

        y.push(1 * boatLength / 7); // Starboard arm, hull

        y.push(3 * boatLength / 7); // Port

        y.push(3 * boatLength / 7);
        y.push(1 * boatLength / 7);
        y.push(1 * boatLength / 7);
        y.push(2.5 * boatLength / 7);
        y.push(2.5 * boatLength / 7);
        y.push(1.5 * boatLength / 7);
        y.push(-1.5 * boatLength / 7);
        y.push(-2.6 * boatLength / 7);
        y.push(-1 * boatLength / 7);
        y.push(-1 * boatLength / 7);
        y.push(-3 * boatLength / 7);
      } else if (this.boatShape === 'PLANE') {
        // Width
        x.push(this.WL_RATIO_COEFF * 0); // Nose
        // Starboard

        x.push(this.WL_RATIO_COEFF * (0.3 * boatLength) / 7);
        x.push(this.WL_RATIO_COEFF * (0.6 * boatLength) / 7); // Wing, front, starboard, inside

        x.push(this.WL_RATIO_COEFF * (4 * boatLength) / 7); // Wing, front, starboard, outside

        x.push(this.WL_RATIO_COEFF * (4 * boatLength) / 7); // Wing, outside, back

        x.push(this.WL_RATIO_COEFF * (0.6 * boatLength) / 7); // Wing, back, starboard, inside

        x.push(this.WL_RATIO_COEFF * (0.3 * boatLength) / 7); // Main hull, transom starboard,

        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7); // Main hull, back wing, front

        x.push(this.WL_RATIO_COEFF * (2 * boatLength) / 7); // Main hull, back wing, back, ext

        x.push(this.WL_RATIO_COEFF * (0.1 * boatLength) / 7); // Main hull, back wing, back, int
        // Port

        x.push(this.WL_RATIO_COEFF * (-0.1 * boatLength) / 7); // Main hull, back wing, back, int

        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7); // Main hull, back wing, back, ext

        x.push(this.WL_RATIO_COEFF * (-2 * boatLength) / 7); // Main hull, back wing, front

        x.push(this.WL_RATIO_COEFF * (-0.3 * boatLength) / 7); // Main hull, transom starboard,

        x.push(this.WL_RATIO_COEFF * (-0.6 * boatLength) / 7); // Wing, back, starboard, inside

        x.push(this.WL_RATIO_COEFF * (-4 * boatLength) / 7); // Outrigger bow

        x.push(this.WL_RATIO_COEFF * (-4 * boatLength) / 7); // Arm, front, starboard, outside

        x.push(this.WL_RATIO_COEFF * (-0.6 * boatLength) / 7); // Arm, front, starboard, inside

        x.push(this.WL_RATIO_COEFF * (-0.3 * boatLength) / 7); // Length

        y.push(-4 * boatLength / 7); // Nose
        // Starboard

        y.push(-3 * boatLength / 7);
        y.push(-2 * boatLength / 7);
        y.push(-1 * boatLength / 7);
        y.push(0 * boatLength / 7);
        y.push(-0.5 * boatLength / 7);
        y.push(1.8 * boatLength / 7);
        y.push(2.5 * boatLength / 7);
        y.push(3 * boatLength / 7);
        y.push(2.8 * boatLength / 7); // Port

        y.push(2.8 * boatLength / 7);
        y.push(3 * boatLength / 7);
        y.push(2.5 * boatLength / 7);
        y.push(1.8 * boatLength / 7);
        y.push(-0.5 * boatLength / 7);
        y.push(0 * boatLength / 7);
        y.push(-1 * boatLength / 7);
        y.push(-2 * boatLength / 7);
        y.push(-3 * boatLength / 7);
      }

      var xPoints = [];
      var yPoints = []; // Rotation matrix:
      // | cos(alpha)  -sin(alpha) |
      // | sin(alpha)   cos(alpha) |
      // The center happens to be the middle of the boat.

      var center = this.getCanvasCenter();
      var ptX = center.x;
      var ptY = center.y;

      for (var i = 0; i < x.length; i++) {
        // Rotation
        var dx = x[i] * Math.cos(_utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](trueHeading)) + y[i] * -Math.sin(_utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](trueHeading));
        var dy = x[i] * Math.sin(_utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](trueHeading)) + y[i] * Math.cos(_utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](trueHeading));
        xPoints.push(Math.round(ptX + dx));
        yPoints.push(Math.round(ptY + dy));
      }

      context.fillStyle = this.boatOverviewColorConfig.boatFillColor;
      context.beginPath();
      context.moveTo(xPoints[0], yPoints[0]);

      for (var _i = 1; _i < xPoints.length; _i++) {
        context.lineTo(xPoints[_i], yPoints[_i]);
      }

      context.closePath();
      context.fill();
      context.strokeStyle = this.boatOverviewColorConfig.boatOutlineColor;
      context.lineWidth = 2;
      context.stroke();
    }
  }, {
    key: "drawBoatOverview",
    value: function drawBoatOverview() {
      var currentStyle = this.className;

      if (this._previousClassName !== currentStyle || true) {
        // Reload
        //	console.log("Reloading CSS");
        try {
          this.boatOverviewColorConfig = this.getColorConfig(currentStyle);
        } catch (err) {
          // Absorb?
          console.log(err);
        }

        this._previousClassName = currentStyle;
      }

      var context = this.canvas.getContext('2d');

      if (this.width === 0 || this.height === 0) {
        // Not visible
        return;
      } // Set the canvas size from its container.


      this.canvas.width = this._width;
      this.canvas.height = this._height; // Background

      var grd = context.createLinearGradient(0, 5, 0, this.height);
      grd.addColorStop(0, this.boatOverviewColorConfig.displayBackgroundGradient.from); // 0  Beginning

      grd.addColorStop(1, this.boatOverviewColorConfig.displayBackgroundGradient.to); // 1  End

      context.fillStyle = grd;
      context.fillRect(0, 0, this.width, this.height); // The actual Graph:

      var maxSpeed = 5;

      if (this._withGPS) {
        maxSpeed = Math.max(maxSpeed, this.sog);
      }

      maxSpeed = Math.max(maxSpeed, this._bsp);

      if (this._withGPS && this._withWind && this._withTrueWind) {
        maxSpeed = Math.max(maxSpeed, this.tws);
      }

      if (this._withWind) {
        maxSpeed = Math.max(maxSpeed, this.aws);
      }

      this.speedScale = 5 * Math.ceil(maxSpeed / 5);
      var cWidth = this._width;
      var cHeight = this._height; // Circles

      var center = this.getCanvasCenter();
      var x = center.x;
      var y = center.y;
      context.strokeStyle = this.boatOverviewColorConfig.gridColor;

      for (var circ = 1; circ <= this.speedScale; circ++) {
        var radius = this._zoom * Math.round(circ * (Math.min(cHeight, cWidth) / 2 / this.speedScale));
        context.beginPath();

        if (circ % 5 == 0) {
          context.lineWidth = 3;
        } else {
          context.lineWidth = 1;
        }

        context.arc(x, y, radius, 0, 2 * Math.PI);
        context.closePath();
        context.stroke();
      }

      this.drawBoat(context, this._hdg);

      if (this._withWind && this._withTrueWind) {
        if (this._withGPS) {
          this.drawTrueWind(context);
          this.drawVW(context); // Speed Wind (Velocity)
        }
      }

      if (this._withWind) {
        this.drawAppWind(context);
      }

      this.drawBSP(context);

      if (this._withW) {
        this.drawNorths(context);
      }

      this.drawCMG(context);

      if (this._withGPS) {
        this.drawSOG(context);
      }

      if (this._withCurrent && this._withGPS) {
        this.drawCurrent(context);
      }

      if (this._withVMG && this._withGPS) {
        this.drawVMG(context);
      } // Display values
      // See http://www.w3schools.com/tags/ref_entities.asp, &deg; = &#176;


      context.fillStyle = this.boatOverviewColorConfig.nmeaDataDisplayColor;
      context.font = "bold 16px Courier New";
      var txtY = 20;
      var space = 18;
      var col1 = 10,
          col2 = 90;
      context.fillText("BSP", col1, txtY);
      context.fillText(this._bsp + " kts", col2, txtY);
      txtY += space;
      context.fillText("HDG", col1, txtY);
      context.fillText(this._hdg.toFixed(0) + "° True", col2, txtY);

      if (this._withWind) {
        txtY += space;
        context.fillText("AWS", col1, txtY);
        context.fillText(this.aws + " kts", col2, txtY);
        txtY += space;
        context.fillText("AWA", col1, txtY);
        context.fillText(this.awa + "°", col2, txtY);
      }

      if (this._withGPS) {
        context.fillStyle = this.boatOverviewColorConfig.gpsWsArrowColor;
        txtY += space;
        context.fillText("COG", col1, txtY);
        context.fillText(this.cog.toFixed(0) + "°", col2, txtY);
        txtY += space;
        context.fillText("SOG", col1, txtY);
        context.fillText(this.sog.toFixed(2) + " kts", col2, txtY);
      }

      context.fillStyle = this.boatOverviewColorConfig.calculatedDataDisplayColor;

      if (this._withWind && this._withTrueWind && this._withGPS) {
        txtY += space;
        context.fillText("TWS", col1, txtY);
        context.fillText(this.tws.toFixed(2) + " kts", col2, txtY);
        txtY += space;
        context.fillText("TWA", col1, txtY);
        context.fillText(this.twa + "°", col2, txtY);
        txtY += space;
        context.fillText("TWD", col1, txtY);
        context.fillText(this.twd + "°", col2, txtY);
      }

      if (this._withCurrent && this._withGPS) {
        txtY += space;
        context.fillText("CDR", col1, txtY);
        context.fillText(this.cdr.toFixed(0) + "°", col2, txtY);
        txtY += space;
        context.fillText("CSP", col1, txtY);
        context.fillText(this.csp.toFixed(2) + " kts", col2, txtY);
      }

      txtY += space;
      context.fillText("leeway", col1, txtY);
      context.fillText(this.lwy.toFixed(2) + "°", col2, txtY);
      txtY += space;
      context.fillText("CMG", col1, txtY);
      context.fillText(this._cmg.toFixed(0) + "°", col2, txtY);

      if (this._withVMG && this._withGPS) {
        var mess = ", ";

        if (this.vmgOnWind) {
          mess += "on wind";
        } else {
          mess += "on WP [" + this._wpName + "]";
        }

        context.fillStyle = this.boatOverviewColorConfig.vmgDataDisplayColor;
        txtY += space;
        context.fillText("VMG", col1, txtY);
        context.fillText(this.vmg.toFixed(2) + " kts" + mess, col2, txtY);
      }

      if (this._withW) {
        var hdm = this._hdg - this._Decl;
        var hdc = hdm - this._dev;

        while (hdm < 0) {
          hdm += 360;
        }

        while (hdc < 0) {
          hdc += 360;
        }

        context.fillStyle = this.boatOverviewColorConfig.dDWDataDisplayColor;
        txtY += space;
        context.fillText("D", col1, txtY);
        context.fillText(this.Decl.toFixed(1) + "°", col2, txtY);
        txtY += space;
        context.fillText("d", col1, txtY);
        context.fillText(this.dev.toFixed(1) + "°", col2, txtY);
        txtY += space;
        context.fillText("W", col1, txtY);
        context.fillText((this.Decl + this.dev).toFixed(1) + "°", col2, txtY);
        txtY += space;
        context.fillText("HDM", col1, txtY);
        context.fillText(hdm.toFixed(1) + "°", col2, txtY);
        txtY += space;
        context.fillText("HDC", col1, txtY);
        context.fillText(hdc.toFixed(1) + "°", col2, txtY);
      }
    }
  }, {
    key: "width",
    set: function set(val) {
      this.setAttribute("width", val);
    },
    // Getters
    get: function get() {
      return this._width;
    }
  }, {
    key: "height",
    set: function set(val) {
      this.setAttribute("height", val);
    },
    get: function get() {
      return this._height;
    }
  }, {
    key: "bsp",
    set: function set(val) {
      this.setAttribute("bsp", val);
    },
    get: function get() {
      return this._bsp;
    }
  }, {
    key: "hdg",
    set: function set(val) {
      this.setAttribute("hdg", val);
    },
    get: function get() {
      return this._hdg;
    }
  }, {
    key: "aws",
    set: function set(val) {
      this.setAttribute("aws", val);
    },
    get: function get() {
      return this._aws;
    }
  }, {
    key: "awa",
    set: function set(val) {
      this.setAttribute("awa", val);
    },
    get: function get() {
      return this._awa;
    }
  }, {
    key: "tws",
    set: function set(val) {
      this.setAttribute("tws", val);
    },
    get: function get() {
      return this._tws;
    }
  }, {
    key: "twa",
    set: function set(val) {
      this.setAttribute("twa", val);
    },
    get: function get() {
      return this._twa;
    }
  }, {
    key: "twd",
    set: function set(val) {
      this.setAttribute("twd", val);
    },
    get: function get() {
      return this._twd;
    }
  }, {
    key: "vmg",
    set: function set(val) {
      this.setAttribute("vmg", val);
    },
    get: function get() {
      return this._vmg;
    }
  }, {
    key: "cog",
    set: function set(val) {
      this.setAttribute("cog", val);
    },
    get: function get() {
      return this._cog;
    }
  }, {
    key: "sog",
    set: function set(val) {
      this.setAttribute("sog", val);
    },
    get: function get() {
      return this._sog;
    }
  }, {
    key: "cdr",
    set: function set(val) {
      this.setAttribute("cdr", val);
    },
    get: function get() {
      return this._cdr;
    }
  }, {
    key: "csp",
    set: function set(val) {
      this.setAttribute("csp", val);
    },
    get: function get() {
      return this._csp;
    }
  }, {
    key: "lwy",
    set: function set(val) {
      this.setAttribute("lwy", val);
    },
    get: function get() {
      return this._lwy;
    }
  }, {
    key: "cmg",
    set: function set(val) {
      this.setAttribute("cmg", val);
    },
    get: function get() {
      return this._cmg;
    }
  }, {
    key: "b2wp",
    set: function set(val) {
      this.setAttribute("b2wp", val);
    },
    get: function get() {
      return this._b2wp;
    }
  }, {
    key: "Decl",
    set: function set(val) {
      this.setAttribute("decl", val);
    },
    get: function get() {
      return this._Decl;
    }
  }, {
    key: "dev",
    set: function set(val) {
      this.setAttribute("dev", val);
    },
    get: function get() {
      return this._dev;
    }
  }, {
    key: "zoomOnBoat",
    set: function set(val) {
      this.setAttribute("zoom-on-boat", val);
    },
    get: function get() {
      return this._zoom;
    }
  }, {
    key: "withGPS",
    set: function set(val) {
      this.setAttribute("with-gps", val);
    },
    get: function get() {
      return this._withGPS;
    }
  }, {
    key: "withCurrent",
    set: function set(val) {
      this.setAttribute("with-current", val);
    },
    get: function get() {
      return this._withCurrent;
    }
  }, {
    key: "withTrueWind",
    set: function set(val) {
      this.setAttribute("with-true-wind", val);
    },
    get: function get() {
      return this._withTrueWind;
    }
  }, {
    key: "withWind",
    set: function set(val) {
      this.setAttribute("with-wind", val);
    },
    get: function get() {
      return this._withWind;
    }
  }, {
    key: "withLabels",
    set: function set(val) {
      this.setAttribute("with-labels", val);
    },
    get: function get() {
      return this._withLabels;
    }
  }, {
    key: "withVMG",
    set: function set(val) {
      this.setAttribute("with-vmg", val);
    },
    get: function get() {
      return this._withVMG;
    }
  }, {
    key: "vmgOnWind",
    set: function set(val) {
      this.setAttribute("vmg-on-wind", val);
    },
    get: function get() {
      return this._vmgOnWind;
    }
  }, {
    key: "withW",
    set: function set(val) {
      this.setAttribute("with-w", val);
    },
    get: function get() {
      return this._withW;
    }
  }, {
    key: "wpName",
    set: function set(val) {
      this.setAttribute("wp-name", val);
    },
    get: function get() {
      return this._wpName;
    }
  }, {
    key: "boatShape",
    set: function set(val) {
      this.setAttribute("boat-shape", val);
    },
    get: function get() {
      return this._boatShape;
    }
  }, {
    key: "shadowRoot",
    set: function set(val) {
      this._shadowRoot = val;
    },
    get: function get() {
      return this._shadowRoot;
    }
  }]);

  return BoatOverview;
}(_wrapNativeSuper(HTMLElement));

var Point = function Point(x, y) {
  _classCallCheck(this, Point);

  this.x = x;
  this.y = y;
};

var Line =
/*#__PURE__*/
function () {
  // Line with arrow head
  function Line(x1, y1, x2, y2) {
    _classCallCheck(this, Line);

    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.HEAD_LENGTH = 20;
    this.HEAD_WIDTH = 6;
  }

  _createClass(Line, [{
    key: "rotate",
    value: function rotate(p, angle) {
      var r = new Point(Math.round(p.x * Math.cos(_utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](angle)) + p.y * Math.sin(_utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](angle))), Math.round(p.x * -Math.sin(_utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](angle)) + p.y * Math.cos(_utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["toRadians"](angle))));
      return r;
    }
  }, {
    key: "drawWithArrowhead",
    value: function drawWithArrowhead(ctx) {
      this.drawWithArrowheads(ctx, false);
    }
  }, {
    key: "drawWithArrowheads",
    value: function drawWithArrowheads(ctx, both) {
      if (both === undefined) {
        both = true;
      } // draw the line


      ctx.beginPath();
      ctx.moveTo(this.x1, this.y1);
      ctx.lineTo(this.x2, this.y2);
      ctx.stroke();

      if (both) {
        // draw the starting arrowhead
        var startRadians = Math.atan((this.y2 - this.y1) / (this.x2 - this.x1));
        startRadians += (this.x2 > this.x1 ? -90 : 90) * Math.PI / 180;
        this.drawArrowhead(ctx, this.x1, this.y1, startRadians);
      } // draw the ending arrowhead


      var endRadians = Math.atan((this.y2 - this.y1) / (this.x2 - this.x1));
      endRadians += (this.x2 > this.x1 ? 90 : -90) * Math.PI / 180;
      this.drawArrowhead(ctx, this.x2, this.y2, endRadians);
    }
  }, {
    key: "drawWithAnemoArrowheads",
    value: function drawWithAnemoArrowheads(ctx) {
      // draw the line
      ctx.beginPath();
      ctx.moveTo(this.x1, this.y1);
      ctx.lineTo(this.x2, this.y2);
      ctx.stroke();
      var endRadians = Math.atan((this.y2 - this.y1) / (this.x2 - this.x1));
      endRadians += (this.x2 > this.x1 ? 90 : -90) * Math.PI / 180;
      this.drawArrowhead(ctx, this.x2 - (this.x2 - this.x1) / 2, this.y2 - (this.y2 - this.y1) / 2, endRadians);
    }
  }, {
    key: "drawHollowArrow",
    value: function drawHollowArrow(ctx) {
      var headLength = 30;
      var arrowWidth = 10;
      var headWidth = 20;
      var dir = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["getDir"](this.x1 - this.x2, this.y2 - this.y1);
      var len = Math.sqrt((this.x1 - this.x2) * (this.x1 - this.x2) + (this.y2 - this.y1) * (this.y2 - this.y1));
      var one, two, three, four, five, six, seven, eight;
      one = new Point(0, 0);
      two = new Point(-arrowWidth / 2, 0);
      three = new Point(-arrowWidth / 2, -Math.round(len - headLength));
      four = new Point(-headWidth / 2, -Math.round(len - headLength));
      five = new Point(0, -Math.round(len)); // to

      six = new Point(headWidth / 2, -Math.round(len - headLength));
      seven = new Point(arrowWidth / 2, -Math.round(len - headLength));
      eight = new Point(arrowWidth / 2, 0);
      one = this.rotate(one, -dir);
      two = this.rotate(two, -dir);
      three = this.rotate(three, -dir);
      four = this.rotate(four, -dir);
      five = this.rotate(five, -dir);
      six = this.rotate(six, -dir);
      seven = this.rotate(seven, -dir);
      eight = this.rotate(eight, -dir);
      var x = [];
      var y = [];
      x.push(this.x1 + one.x);
      x.push(this.x1 + two.x);
      x.push(this.x1 + three.x);
      x.push(this.x1 + four.x);
      x.push(this.x1 + five.x);
      x.push(this.x1 + six.x);
      x.push(this.x1 + seven.x);
      x.push(this.x1 + eight.x);
      y.push(this.y1 + one.y);
      y.push(this.y1 + two.y);
      y.push(this.y1 + three.y);
      y.push(this.y1 + four.y);
      y.push(this.y1 + five.y);
      y.push(this.y1 + six.y);
      y.push(this.y1 + seven.y);
      y.push(this.y1 + eight.y);
      ctx.beginPath();
      ctx.moveTo(x[0], y[0]);

      for (var i = 1; i < x.length; i++) {
        ctx.lineTo(x[i], y[i]);
      }

      ctx.closePath();
      ctx.stroke();
    }
  }, {
    key: "drawArrowhead",
    value: function drawArrowhead(ctx, x, y, radians) {
      ctx.save();
      ctx.beginPath();
      ctx.translate(x, y);
      ctx.rotate(radians);
      ctx.moveTo(0, 0);
      ctx.lineTo(this.HEAD_WIDTH, this.HEAD_LENGTH);
      ctx.lineTo(-this.HEAD_WIDTH, this.HEAD_LENGTH);
      ctx.closePath();
      ctx.restore();
      ctx.fill();
    }
  }]);

  return Line;
}(); // Associate the tag and the class


window.customElements.define(BOAT_OVERVIEW_TAG_NAME, BoatOverview);

/***/ })

/******/ });
});
//# sourceMappingURL=boat-overview.js.map