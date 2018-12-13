(function webpackUniversalModuleDefinition(root, factory) {
	if(typeof exports === 'object' && typeof module === 'object')
		module.exports = factory();
	else if(typeof define === 'function' && define.amd)
		define("split-flap-display", [], factory);
	else if(typeof exports === 'object')
		exports["split-flap-display"] = factory();
	else
		root["split-flap-display"] = factory();
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
/******/ 	return __webpack_require__(__webpack_require__.s = "./SplitFlapDisplay.js");
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

/***/ "./SplitFlapDisplay.js":
/*!*****************************!*\
  !*** ./SplitFlapDisplay.js ***!
  \*****************************/
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

var spliFlapVerbose = false;
var SPLIT_FLAP_TAG_NAME = 'split-flap-display';
var SPLIT_FLAP_CHARACTERS = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", ":", ".", ",", "?", "!", "+", "=", "/", " " // Add more here if needed
];
var splitFlapDefaultColorConfig = {
  bgColor: 'transparent',
  displayBackgroundGradient: {
    from: 'black',
    to: 'gray'
  },
  displayColor: 'white',
  frameColor: 'silver',
  valueFont: 'Arial'
};

/* global HTMLElement */

var SplitFlapDisplay =
/*#__PURE__*/
function (_HTMLElement) {
  _inherits(SplitFlapDisplay, _HTMLElement);

  _createClass(SplitFlapDisplay, null, [{
    key: "observedAttributes",
    get: function get() {
      return ["font-size", // Integer. Font size in px. Default 30
      "nb-char", // Integer. Nb flaps. Default 1
      "value", // Initial value, default blank
      "justified" // LEFT (default), RIGHT or CENTER
      ];
    }
  }]);

  function SplitFlapDisplay() {
    var _this;

    _classCallCheck(this, SplitFlapDisplay);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(SplitFlapDisplay).call(this));
    _this._shadowRoot = _this.attachShadow({
      mode: 'open'
    }); // 'open' means it is accessible from external JavaScript.
    // create and append a <canvas>

    _this.canvas = document.createElement("canvas");
    var fallbackElemt = document.createElement("h1");
    var content = document.createTextNode("This is a Split-Flap Display, on an HTML5 canvas");
    fallbackElemt.appendChild(content);

    _this.canvas.appendChild(fallbackElemt);

    _this.shadowRoot.appendChild(_this.canvas);

    _this._connected = false; // Default values

    _this._value = "";
    _this._paddedValue = "";
    _this._font_size = 30;
    _this._nb_char = 1;
    _this._justified = "LEFT";
    _this._previousClassName = "";
    _this.splitFlapColorConfig = splitFlapDefaultColorConfig;

    if (spliFlapVerbose) {
      console.log("Data in Constructor:", _this._value);
    }

    return _this;
  } // Called whenever the custom element is inserted into the DOM.


  _createClass(SplitFlapDisplay, [{
    key: "connectedCallback",
    value: function connectedCallback() {
      this._connected = true;

      if (spliFlapVerbose) {
        console.log("connectedCallback invoked, 'value' is [", this._value, "]");
      }

      this.repaint();
    } // Called whenever the custom element is removed from the DOM.

  }, {
    key: "disconnectedCallback",
    value: function disconnectedCallback() {
      if (spliFlapVerbose) {
        console.log("disconnectedCallback invoked");
      }
    } // Called whenever an attribute is added, removed or updated.
    // Only attributes listed in the observedAttributes property are affected.

  }, {
    key: "attributeChangedCallback",
    value: function attributeChangedCallback(attrName, oldVal, newVal) {
      if (spliFlapVerbose) {
        console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
      }

      switch (attrName) {
        case "value":
          this._value = newVal;
          break;

        case "font-size":
          this._font_size = parseInt(newVal);
          break;

        case "nb-char":
          this._nb_char = parseInt(newVal);
          break;

        case "justified":
          this._justified = newVal === 'RIGHT' ? 'RIGHT' : 'LEFT';
          break;

        default:
          break;
      }

      this.repaint();
    } // Called whenever the custom element has been moved into a new document.

  }, {
    key: "adoptedCallback",
    value: function adoptedCallback() {
      if (spliFlapVerbose) {
        console.log("adoptedCallback invoked");
      }
    }
  }, {
    key: "getColorConfig",
    // Component methods
    value: function getColorConfig(classNames) {
      var colorConfig = splitFlapDefaultColorConfig;
      var classes = classNames.split(" ");

      for (var cls = 0; cls < classes.length; cls++) {
        var cssClassName = classes[cls];

        for (var s = 0; s < document.styleSheets.length; s++) {
          // console.log("Walking though ", document.styleSheets[s]);
          try {
            for (var r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
              var selector = document.styleSheets[s].cssRules[r].selectorText; //			console.log(">>> ", selector);

              if (selector !== undefined && (selector === '.' + cssClassName || selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(SPLIT_FLAP_TAG_NAME) > -1)) {
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
                      case '--bg-color':
                        colorConfig.bgColor = value;
                        break;

                      case '--display-background-gradient-from':
                        colorConfig.displayBackgroundGradient.from = value;
                        break;

                      case '--display-background-gradient-to':
                        colorConfig.displayBackgroundGradient.to = value;
                        break;

                      case '--display-color':
                        colorConfig.displayColor = value;
                        break;

                      case '--frame-color':
                        colorConfig.frameColor = value;
                        break;

                      case '--value-font':
                        colorConfig.valueFont = value;
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
      if (this._connected) {
        this.drawSplitFlap(this._value);
      }
    }
  }, {
    key: "getCharAt",
    value: function getCharAt(idx) {
      var char = null;

      if (idx < this._nb_char) {
        char = this._paddedValue.split('')[idx];
      }

      return char;
    }
  }, {
    key: "getNextChar",
    value: function getNextChar(char) {
      var idx = -1;

      for (var i = 0; i < SPLIT_FLAP_CHARACTERS.length; i++) {
        if (SPLIT_FLAP_CHARACTERS[i] === char.toUpperCase()) {
          idx = i;
          break;
        }
      }

      idx += 1;

      if (idx >= SPLIT_FLAP_CHARACTERS.length) {
        idx = 0;
      }

      return SPLIT_FLAP_CHARACTERS[idx];
    }
  }, {
    key: "setCharAt",
    value: function setCharAt(idx, char) {
      var newArray = this._paddedValue.split('');

      newArray[idx] = char;
      this._paddedValue = newArray.join(''); // join() keeps the ',' between characters.

      this.drawPaddedString();
    }
  }, {
    key: "cleanString",
    value: function cleanString(str) {
      var clean = str;
      var cleanArr = clean.split('');

      for (var i = 0; i < cleanArr.length; i++) {
        cleanArr[i] = cleanArr[i].toUpperCase();

        if (!SPLIT_FLAP_CHARACTERS.includes(cleanArr[i])) {
          cleanArr[i] = ' ';
        }
      }

      return cleanArr.join('');
    }
  }, {
    key: "drawOneFlap",
    value: function drawOneFlap(context, char, x, y, w, h, scale) {
      var grd = context.createLinearGradient(x, y, x + w, y + h);
      grd.addColorStop(0, this.splitFlapColorConfig.displayBackgroundGradient.from); // 0  Beginning

      grd.addColorStop(1, this.splitFlapColorConfig.displayBackgroundGradient.to); // 1  End

      context.fillStyle = grd;
      context.strokeStyle = this.splitFlapColorConfig.frameColor;
      context.lineWidth = 0.5; // Background

      this.roundRect(context, x, y, w, h, 1, true, false);
      context.beginPath();
      context.moveTo(x, y + h / 2);
      context.lineTo(x + w, y + h / 2);
      context.closePath();
      context.stroke();
      var str = char;

      if (!SPLIT_FLAP_CHARACTERS.includes(char)) {
        str = ' ';
      }

      context.fillStyle = this.splitFlapColorConfig.displayColor; // Value

      context.font = "bold " + Math.round(scale * this._font_size) + "px " + this.splitFlapColorConfig.valueFont;
      var strVal = str;
      var metrics = context.measureText(strVal);
      var len = metrics.width;
      var xOffset = x + w / 2 - len / 2;
      var yOffset = y + h / 2;
      context.textBaseline = "middle";
      context.fillText(strVal, xOffset, yOffset);
    }
  }, {
    key: "getPaddedValue",
    value: function getPaddedValue(val) {
      var paddedVal = val;

      if (val.length > this._nb_char) {
        switch (this._justified) {
          case "LEFT":
            paddedVal = val.toUpperCase().substring(0, this._nb_char);
            break;

          case "RIGHT":
            paddedVal = val.toUpperCase().substring(val.length - this._nb_char);
            break;

          case "CENTER": // TODO

          default:
            break;
        }
      }

      switch (this._justified) {
        case "LEFT":
          paddedVal = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["rpad"](val, this._nb_char);
          break;

        case "RIGHT":
          paddedVal = _utilities_Utilities_js__WEBPACK_IMPORTED_MODULE_0__["lpad"](val, this._nb_char);
          break;

        case "CENTER": // TODO

        default:
          break;
      }

      return paddedVal;
    }
  }, {
    key: "drawSplitFlap",
    value: function drawSplitFlap(textValue) {
      if (this._connected) {
        this._paddedValue = this.getPaddedValue(textValue);
      }

      this.drawPaddedString();
    }
  }, {
    key: "drawPaddedString",
    value: function drawPaddedString() {
      var upperCaseValue = this._paddedValue.toUpperCase().split(''); // Char array


      var currentStyle = this.className;

      if (this._previousClassName !== currentStyle || true) {
        // Reload
        //	console.log("Reloading CSS");
        try {
          this.splitFlapColorConfig = this.getColorConfig(currentStyle);
        } catch (err) {
          // Absorb?
          console.log(err);
        }

        this._previousClassName = currentStyle;
      }

      var context = this.canvas.getContext('2d');
      var scale = 1.0;
      var height = Math.round(this._font_size * 1.1); // cell height

      var oneWidth = Math.round(height * 0.9); // cell width

      var width = this._nb_char * oneWidth; // Set the canvas size from its container.

      this.canvas.width = width;
      this.canvas.height = height;
      context.fillStyle = this.splitFlapColorConfig.bgColor; // Background

      this.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 5, true, false);

      for (var i = 0; i < upperCaseValue.length; i++) {
        this.drawOneFlap(context, upperCaseValue[i], i * oneWidth, 0, oneWidth, height, scale);
      }
    }
  }, {
    key: "roundRect",
    value: function roundRect(ctx, x, y, width, height, radius, fill, stroke) {
      if (fill === undefined) {
        fill = true;
      }

      if (stroke === undefined) {
        stroke = true;
      }

      if (radius === undefined) {
        radius = 5;
      }

      ctx.beginPath();
      ctx.moveTo(x + radius, y);
      ctx.lineTo(x + width - radius, y);
      ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
      ctx.lineTo(x + width, y + height - radius);
      ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
      ctx.lineTo(x + radius, y + height);
      ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
      ctx.lineTo(x, y + radius);
      ctx.quadraticCurveTo(x, y, x + radius, y);
      ctx.closePath();

      if (stroke) {
        ctx.stroke();
      }

      if (fill) {
        ctx.fill();
      }
    }
  }, {
    key: "value",
    set: function set(val) {
      this.setAttribute("value", val);

      if (spliFlapVerbose) {
        console.log(">> Value:", val);
      }

      this.repaint();
    },
    get: function get() {
      return this._value;
    }
  }, {
    key: "fontSize",
    set: function set(val) {
      this.setAttribute("font-size", val);
      this.repaint();
    },
    get: function get() {
      return this._font_size;
    }
  }, {
    key: "nbChar",
    set: function set(val) {
      this.setAttribute("nb-char", val);
      this.repaint();
    },
    get: function get() {
      return this._nb_char;
    }
  }, {
    key: "justified",
    set: function set(val) {
      this.setAttribute("justified", val);
      this.repaint();
    },
    get: function get() {
      return this._justified;
    }
  }, {
    key: "shadowRoot",
    set: function set(val) {
      this._shadowRoot = val;
    },
    get: function get() {
      return this._shadowRoot;
    }
  }, {
    key: "paddedValue",
    get: function get() {
      return this._paddedValue;
    }
  }]);

  return SplitFlapDisplay;
}(_wrapNativeSuper(HTMLElement)); // Associate the tag and the class


window.customElements.define(SPLIT_FLAP_TAG_NAME, SplitFlapDisplay);

/***/ })

/******/ });
});
//# sourceMappingURL=split-flap-display.js.map