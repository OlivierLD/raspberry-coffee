/*
 * @author Olivier Le Diouris
 */
 // TODO This config in CSS
 // We wait for the var- custom properties to be implemented in CSS...
 // @see http://www.w3.org/TR/css-variables-1/
 
 /*
  * For now:
  * Themes are applied based on a css class:
  * .display-scheme
  * {
  *   color: black;
  * }
  * 
  * if color is black, analogDisplayColorConfigBlack is applied
  * if color is white, analogDisplayColorConfigWhite is applied, etc
  */
var analogDisplayColorConfigWhite = 
{
  bgColor:           'white',
  digitColor:        'black',
  withGradient:      true,
  displayBackgroundGradient: { from: 'LightGrey', to: 'white' },
  withDisplayShadow: true,
  shadowColor:       'rgba(0, 0, 0, 0.75)',
  outlineColor:      'DarkGrey',
  majorTickColor:    'black',
  minorTickColor:    'black',
  valueColor:        'grey',
  valueOutlineColor: 'black',
  valueNbDecimal:    1,
  handColor:         'rgba(0, 0, 100, 0.25)',
  handOutlineColor:  'black',
  withHandShadow:    true,
  knobColor:         'DarkGrey',
  knobOutlineColor:  'black',
  font:              'Arial' /* 'Source Code Pro' */
};

var analogDisplayColorConfigBlack = 
{
  bgColor:           'black',
  digitColor:        'cyan',
  withGradient:      true,
  displayBackgroundGradient: { from: 'black', to: 'LightGrey' },
  shadowColor:       'black',
  outlineColor:      'DarkGrey',
  majorTickColor:    'red',
  minorTickColor:    'red',
  valueColor:        'red',
  valueOutlineColor: 'black',
  valueNbDecimal:    1,
  handColor:         'rgba(0, 0, 100, 0.25)',
  handOutlineColor:  'blue',
  withHandShadow:    true,
  knobColor:         '#8ED6FF', // Kind of blue
  knobOutlineColor:  'blue',
  font:              'Arial'
};
var analogDisplayColorConfig = analogDisplayColorConfigWhite; // analogDisplayColorConfigBlack; // White is the default

function AnalogDisplay(cName,                     // Canvas Name
                       dSize,                     // Display radius
                       maxValue,                  // default 10
                       majorTicks,                // default 1
                       minorTicks,                // default 0
                       withDigits,                // default true, boolean
                       overlapOver180InDegree,    // default 0, beyond horizontal, in degrees, before 0, after 180
                       startValue,                // default 0, In case it is not 0
                       nbDecimal)                 // default 1, nb decimals in the value display
{
  if (maxValue === undefined)
    maxValue = 10;
  if (majorTicks === undefined)
    majorTicks = 1;
  if (minorTicks === undefined)
    minorTicks = 0;
  if (withDigits === undefined)
    withDigits = true;
  if (overlapOver180InDegree === undefined)
    overlapOver180InDegree = 0;
  if (startValue === undefined)
    startValue = 0;
  if (nbDecimal === undefined)
    nbDecimal = analogDisplayColorConfig.valueNbDecimal;

  var scale = dSize / 100;

  var canvasName = cName;
  var displaySize = dSize;

  var running = false;
  var previousValue = startValue;
  var intervalID;
  var valueToDisplay = 0;
  var incr = 1;
  var nbDec = nbDecimal;
  
  var instance = this;
  
//try { console.log('in the AnalogDisplay constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}
  
  (function(){ drawDisplay(canvasName, displaySize, previousValue); })(); // Invoked automatically
  
  this.setNbDec = function(nb) 
  {
    nbDec = nb;
  };

  this.repaint = function()
  {
    drawDisplay(canvasName, displaySize, previousValue);
  };
  
  this.setDisplaySize = function(ds)
  {
    scale = ds / 100;
    displaySize = ds;
    drawDisplay(canvasName, displaySize, previousValue);
  };
  
  this.startStop = function (buttonName) 
  {
//  console.log('StartStop requested on ' + buttonName);
    var button = document.getElementById(buttonName);
    running = !running;
    button.value = (running ? "Stop" : "Start");
    if (running)
      this.animate();
    else 
    {
      window.clearInterval(intervalID);
      previousValue = valueToDisplay;
    }
  };

  this.animate = function()
  {    
    var value;
    if (arguments.length === 1)
      value = arguments[0];
    else
    {
//    console.log("Generating random value");
      value = maxValue * Math.random();
    }
    value = Math.max(value, startValue);
    value = Math.min(value, maxValue);
    
  //console.log("Reaching Value :" + value + " from " + previousValue);
    diff = value - previousValue;
    valueToDisplay = previousValue;
    
//  console.log(canvasName + " going from " + previousValue + " to " + value);
    
//    if (diff > 0)
//      incr = 0.01 * maxValue;
//    else 
//      incr = -0.01 * maxValue;
    incr = diff / 10;
    if (intervalID)
      window.clearInterval(intervalID);      
    intervalID = window.setInterval(function () { displayAndIncrement(value); }, 10);
  };

  var displayAndIncrement = function(finalValue) 
  {
    //console.log('Tic ' + inc + ', ' + finalValue);
    drawDisplay(canvasName, displaySize, valueToDisplay);
    valueToDisplay += incr;
    if ((incr > 0 && valueToDisplay > finalValue) || (incr < 0 && valueToDisplay < finalValue))
    {
//    console.log('Stop, ' + finalValue + ' reached, steps were ' + incr);
      window.clearInterval(intervalID);
      previousValue = finalValue;
      if (running)
        instance.animate();
      else
        drawDisplay(canvasName, displaySize, finalValue); // Final display
    }
  };

  function getStyleRuleValue(style, selector, sheet) {
    var sheets = typeof sheet !== 'undefined' ? [sheet] : document.styleSheets;
    for (var i = 0, l = sheets.length; i < l; i++) {
      var sheet = sheets[i];
      if (!sheet.cssRules) { continue; }
      for (var j = 0, k = sheet.cssRules.length; j < k; j++) {
        var rule = sheet.cssRules[j];
        if (rule.selectorText && rule.selectorText.split(',').indexOf(selector) !== -1) {
          return rule.style[style];
        }
      }
    }
    return null;
  };
  
  // From http://www.html5canvastutorials.com/labs/html5-canvas-text-along-arc-path/
  function drawTextAlongArc(context, str, centerX, centerY, radius, angle) {
    var len = str.length, s;
    context.save();
    context.translate(centerX, centerY);
    context.rotate(-1 * angle / 2);
    context.rotate(-1 * (angle / len) / 2);
    for (var n = 0; n < len; n++) {
      context.rotate(angle / len);
      context.save();
      context.translate(0, -1 * radius);
      s = str[n];
      context.fillText(s, 0, 0);
      context.restore();
    }
    context.restore();
  };

  function drawDisplay(displayCanvasName, displayRadius, displayValue)
  {
    var schemeColor = getStyleRuleValue('color', '.display-scheme');
//  console.log(">>> DEBUG >>> color:" + schemeColor);
    if (schemeColor === 'black')
      analogDisplayColorConfig = analogDisplayColorConfigBlack;
    else if (schemeColor === 'white')
      analogDisplayColorConfig = analogDisplayColorConfigWhite;

    var digitColor = analogDisplayColorConfig.digitColor;
    
    var canvas = document.getElementById(displayCanvasName);
    var context = canvas.getContext('2d');

    var radius = displayRadius;
  
    // Cleanup
  //context.fillStyle = "#ffffff";
    context.fillStyle = analogDisplayColorConfig.bgColor;
  //context.fillStyle = "transparent";
    context.fillRect(0, 0, canvas.width, canvas.height);    
  //context.fillStyle = 'rgba(255, 255, 255, 0.0)';
  //context.fillRect(0, 0, canvas.width, canvas.height);    
  
    context.beginPath();
  //context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);      
//  context.arc(canvas.width / 2, radius + 10, radius, Math.PI - toRadians(overlapOver180InDegree), (2 * Math.PI) + toRadians(overlapOver180InDegree), false);
    context.arc(canvas.width / 2, radius + 10, radius, Math.PI - toRadians(overlapOver180InDegree > 0?90:0), (2 * Math.PI) + toRadians(overlapOver180InDegree > 0?90:0), false);
    context.lineWidth = 5;
  
    if (analogDisplayColorConfig.withGradient)
    {
      var grd = context.createLinearGradient(0, 5, 0, radius);
      grd.addColorStop(0, analogDisplayColorConfig.displayBackgroundGradient.from);// 0  Beginning
      grd.addColorStop(1, analogDisplayColorConfig.displayBackgroundGradient.to);  // 1  End
      context.fillStyle = grd;
    }
    else
      context.fillStyle = analogDisplayColorConfig.displayBackgroundGradient.to;
    
    if (analogDisplayColorConfig.withDisplayShadow)
    {
      context.shadowOffsetX = 3;
      context.shadowOffsetY = 3;
      context.shadowBlur  = 3;
      context.shadowColor = analogDisplayColorConfig.shadowColor;
    }
    context.lineJoin    = "round";
    context.fill();
    context.strokeStyle = analogDisplayColorConfig.outlineColor;
    context.stroke();
    context.closePath();
    
    var totalAngle = (Math.PI + (2 * (toRadians(overlapOver180InDegree))));
    // Major Ticks
    context.beginPath();
    for (i = 0;i <= (maxValue - startValue) ;i+=majorTicks)
    {
      var currentAngle = (totalAngle * (i / (maxValue - startValue))) - toRadians(overlapOver180InDegree);
      xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(currentAngle));
      yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(currentAngle));
      xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(currentAngle));
      yTo = (radius + 10) - ((radius * 0.85) * Math.sin(currentAngle));
      context.moveTo(xFrom, yFrom);
      context.lineTo(xTo, yTo);
    }
    context.lineWidth = 3;
    context.strokeStyle = analogDisplayColorConfig.majorTickColor;
    context.stroke();
    context.closePath();
  
    // Minor Ticks
    if (minorTicks > 0)
    {
      context.beginPath();
      for (i = 0;i <= (maxValue - startValue) ;i+=minorTicks)
      {
        var _currentAngle = (totalAngle * (i / (maxValue - startValue))) - toRadians(overlapOver180InDegree);

        xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(_currentAngle));
        yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(_currentAngle));
        xTo = (canvas.width / 2) - ((radius * 0.90) * Math.cos(_currentAngle));
        yTo = (radius + 10) - ((radius * 0.90) * Math.sin(_currentAngle));
        context.moveTo(xFrom, yFrom);
        context.lineTo(xTo, yTo);
      }
      context.lineWidth = 1;
      context.strokeStyle = analogDisplayColorConfig.minorTickColor;
      context.stroke();
      context.closePath();
    }
    
    // Numbers
    if (withDigits)
    {
      if (true) { // no "arced" text
        context.beginPath();
        for (i = 0; i <= (maxValue - startValue); i+=majorTicks)
        {
          context.save();
          context.translate(canvas.width/2, (radius + 10)); // canvas.height);
          var __currentAngle = (totalAngle * (i / (maxValue - startValue))) - toRadians(overlapOver180InDegree); // in radians
  //      context.rotate((Math.PI * (i / maxValue)) - (Math.PI / 2));
          context.rotate(__currentAngle - (Math.PI / 2));
          context.font = "bold " + Math.round(scale * 15) + "px " + analogDisplayColorConfig.font; // Like "bold 15px Arial"
          context.fillStyle = digitColor;
          str = (i + startValue).toString();
          len = context.measureText(str).width;
          context.fillText(str, - len / 2, (-(radius * .8) + 10));
          context.lineWidth = 1;
          context.strokeStyle = analogDisplayColorConfig.valueOutlineColor;
      //  context.strokeText(str, - len / 2, (-(radius * .8) + 10)); // Outlined          
          context.restore();
        }
        context.closePath();
      }

      if (false) { // text along arc
        for (i = 0; i <= (maxValue - startValue); i+=majorTicks)
        {
          var __currentAngle = (totalAngle * (i / (maxValue - startValue))) - toRadians(overlapOver180InDegree); // in radians
          __currentAngle -= (Math.PI / 2);
          context.font = "bold " + Math.round(scale * 15) + "px " + analogDisplayColorConfig.font; // Like "bold 15px Arial"
          context.fillStyle = digitColor;
          str = (i + startValue).toString();
          context.lineWidth = 1;
          context.strokeStyle = analogDisplayColorConfig.valueOutlineColor;
          console.log("Displaying " + str + " at " + toDegrees(__currentAngle));
          drawTextAlongArc(context, str, canvas.width/2, (radius + 10), (radius * 0.7), __currentAngle);
        }
      }
    }
    // Value
    text = displayValue.toFixed(nbDec);
//  text = displayValue.toFixed(nbDecimal); // analogDisplayColorConfig.valueNbDecimal);
    len = 0;
    context.font = "bold " + Math.round(scale * 40) + "px " + analogDisplayColorConfig.font; // "bold 40px Arial"
    var metrics = context.measureText(text);
    len = metrics.width;
  
    context.beginPath();
    context.fillStyle = analogDisplayColorConfig.valueColor;
    context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
    context.lineWidth = 1;
    context.strokeStyle = analogDisplayColorConfig.valueOutlineColor;
    context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined  
    context.closePath();
  
    // Hand
    context.beginPath();
    if (analogDisplayColorConfig.withHandShadow)
    {
      context.shadowColor = analogDisplayColorConfig.shadowColor;
      context.shadowOffsetX = 3;
      context.shadowOffsetY = 3;
      context.shadowBlur = 3;
    }
    // Center
    context.moveTo(canvas.width / 2, radius + 10);
    
    var ___currentAngle = (totalAngle * ((displayValue - startValue) / (maxValue - startValue))) - toRadians(overlapOver180InDegree);
    // Left
    x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((___currentAngle - (Math.PI / 2))));
    y = (radius + 10) - ((radius * 0.05) * Math.sin((___currentAngle - (Math.PI / 2))));
    context.lineTo(x, y);
    // Tip
    x = (canvas.width / 2) - ((radius * 0.90) * Math.cos(___currentAngle));
    y = (radius + 10) - ((radius * 0.90) * Math.sin(___currentAngle));
    context.lineTo(x, y);
    // Right
    x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((___currentAngle + (Math.PI / 2))));
    y = (radius + 10) - ((radius * 0.05) * Math.sin((___currentAngle + (Math.PI / 2))));
    context.lineTo(x, y);
  
    context.closePath();
    context.fillStyle = analogDisplayColorConfig.handColor;
    context.fill();
    context.lineWidth = 1;
    context.strokeStyle = analogDisplayColorConfig.handOutlineColor;
    context.stroke();
    // Knob
    context.beginPath();
    context.arc((canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
    context.closePath();
    context.fillStyle = analogDisplayColorConfig.knobColor;
    context.fill();
    context.strokeStyle = analogDisplayColorConfig.knobOutlineColor;
    context.stroke();
  };
  
  this.setValue = function(val)
  {
    drawDisplay(canvasName, displaySize, val);  
  };
  
  function toDegrees(rad)
  {
    return rad * (180 / Math.PI);
  }
  
  function toRadians(deg)
  {
    return deg * (Math.PI / 180);
  }
}
