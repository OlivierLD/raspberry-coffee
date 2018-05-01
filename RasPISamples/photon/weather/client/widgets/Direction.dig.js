/*
 * @author Olivier Le Diouris
 */

var directionColorConfigWhite =
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
  valueNbDecimal:    0,
  handColor:         'red', // 'rgba(0, 0, 100, 0.25)',
  handOutlineColor:  'black',
  withHandShadow:    true,
  knobColor:         'DarkGrey',
  knobOutlineColor:  'black',
  font:              'Arial' /* 'Source Code Pro' */
};

var directionColorConfigBlack =
{
  bgColor:           'black',
  digitColor:        'cyan',
  withGradient:      true,
  displayBackgroundGradient: { from: 'DarkGrey', to: 'black' },
  shadowColor:       'black',
  outlineColor:      'DarkGrey',
  majorTickColor:    'red',
  minorTickColor:    'red',
  valueColor:        'red',
  valueOutlineColor: 'black',
  valueNbDecimal:    0,
  handColor:         'rgba(255, 0, 0, 0.4)', // 'rgba(0, 0, 100, 0.25)',
  handOutlineColor:  'red', //'blue',
  withHandShadow:    true,
  knobColor:         'darkGrey', // '#8ED6FF', // Kind of blue
  knobOutlineColor:  'black',
  font:              'Arial'
};
var directionColorConfig = directionColorConfigWhite;

function DirectionDig(cName, dSize, ticks)
{
  if (ticks === undefined)
    ticks = 22.5;

  var canvasName = cName;
  var displaySize = dSize;

  var scale = dSize / 100;

  var running = false;
  this.previousValue = 0.0;
  this.intervalID = 0;
  this.valueToDisplay = 0;
  this.incr = 1;
  this.busy = false;
  var withBorder = true;

  var instance = this;

//try { console.log('in the Direction constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

  this.setDisplaySize = function(ds)
  {
    scale = ds / 100;
    displaySize = ds;
    this.drawDisplay(canvasName, displaySize, instance.previousValue);
  };

  this.setBorder = function(b)
  {
    withBorder = b;
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
      window.clearInterval(this.intervalID);
      this.intervalID = 0;
      this.previousValue = this.valueToDisplay;
    }
  };

  var on360 = function(angle)
  {
    var num = angle;
    while (num < 0)
      num += 360;
    while (num > 360)
      num -= 360;
    return num;
  };

  this.animate = function()
  {
    var value;
    if (arguments.length === 1)
      value = arguments[0];
    else
    {
//    console.log("Generating random value");
      value = 360 * Math.random();
    }
//  console.log("Reaching Value :" + value + " from " + this.previousValue);
    diff = value - on360(this.previousValue);
    if (Math.abs(diff) > 180) // && sign(Math.cos(toRadians(value))))
    {
//    console.log("Diff > 180: new:" + value + ", prev:" + this.previousValue);
      if (value > on360(this.previousValue))
        value -= 360;
      else
        value += 360;
      diff = value - on360(this.previousValue);
    }
    this.valueToDisplay = on360(this.previousValue);

//  console.log(canvasName + " going from " + this.previousValue + " to " + value);

    this.incr = diff / 10;
//    if (diff < 0)
//      incr *= -1;
    if (this.intervalID && this.intervalID !== 0)
      window.clearInterval(this.intervalID);
    if (this.incr !== 0 && !this.busy)
    {
//    if (false && canvasName === 'twdCanvas')
//      console.log('Starting animation between ' + this.previousValue + ' and ' + value + ', step ' + this.incr);
      this.busy = true;
      this.intervalID = window.setTimeout(function () { instance.displayAndIncrement(value); }, 50);
    }
  };

  var sign = function(x) { return x > 0 ? 1 : x < 0 ? -1 : 0; };
  var toRadians = function(d)
  {
    return Math.PI * d / 180;
  };

  var toDegrees = function(d)
  {
    return d * 180 / Math.PI;
  };

  this.displayAndIncrement = function(finalValue)
  {
    //console.log('Tic ' + inc + ', ' + finalValue);
    this.drawDisplay(canvasName, displaySize, this.valueToDisplay);
    this.valueToDisplay += this.incr;
//  if (canvasName === 'twdCanvas')
//    console.log('       displayAndIncrement curr:' + this.valueToDisplay.toFixed(2) + ', final:' + finalValue + ', step ' + this.incr);
    if ((this.incr > 0 && this.valueToDisplay.toFixed(2) >= finalValue) || (this.incr < 0 && this.valueToDisplay.toFixed(2) <= finalValue))
    {
//    if (canvasName === 'twdCanvas')
//      console.log('Stop, ' + finalValue + ' reached, steps were ' + this.incr);
      //  console.log('Stop!')
      window.clearInterval(this.intervalID);
      this.intervalID = 0;
      this.previousValue = on360(finalValue);
      if (running)
        instance.animate();
      else
        this.drawDisplay(canvasName, displaySize, finalValue); // Final state
      this.busy = false; // Free!
    }
    else
    {
      window.setTimeout(function () { instance.displayAndIncrement(finalValue); }, 50);
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

  this.drawDisplay = function(displayCanvasName, displayRadius, displayValue)
  {
    var schemeColor = getStyleRuleValue('color', '.display-scheme');
//  console.log(">>> DEBUG >>> color:" + schemeColor);
    if (schemeColor === 'black')
      directionColorConfig = directionColorConfigBlack;
    else if (schemeColor === 'white')
      directionColorConfig = directionColorConfigWhite;

    var digitColor = directionColorConfig.digitColor;

    var canvas = document.getElementById(displayCanvasName);
    var context = canvas.getContext('2d');

    var radius = displayRadius;

    // Cleanup
  //context.fillStyle = "#ffffff";
    context.fillStyle = directionColorConfig.bgColor;
//  context.fillStyle = "transparent";
    context.fillRect(0, 0, canvas.width, canvas.height);
  //context.fillStyle = 'rgba(255, 255, 255, 0.0)';
  //context.fillRect(0, 0, canvas.width, canvas.height);

    context.beginPath();
    if (withBorder === true)
    {
    //context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
      context.arc(canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
      context.lineWidth = 5;
    }
    if (directionColorConfig.withGradient)
    {
      var grd = context.createLinearGradient(0, 5, 0, radius);
      grd.addColorStop(0, directionColorConfig.displayBackgroundGradient.from);// 0  Beginning
      grd.addColorStop(1, directionColorConfig.displayBackgroundGradient.to);  // 1  End
      context.fillStyle = grd;
    }
    else
      context.fillStyle = directionColorConfig.displayBackgroundGradient.to;

    if (directionColorConfig.withDisplayShadow)
    {
      context.shadowOffsetX = 3;
      context.shadowOffsetY = 3;
      context.shadowBlur  = 3;
      context.shadowColor = directionColorConfig.shadowColor;
    }
    context.lineJoin    = "round";
    context.fill();
    context.strokeStyle = directionColorConfig.outlineColor;
    context.stroke();
    context.closePath();

    // Major Ticks
    context.beginPath();
    context.lineWidth = 1;
    for (i = 0;i < 360 ;i+=ticks)
    {
      xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (i / 360)));
      yTo = (radius + 10) - ((radius * 0.85) * Math.sin(2 * Math.PI * (i / 360)));

      context.beginPath();
      context.arc(xTo, yTo, 5, 0, 2 * Math.PI, false);
      context.closePath();
      context.fillStyle = 'gray'; // raw16pointsColorConfig.knobColor;
      context.fill();
      context.strokeStyle = 'black'; // raw16pointsColorConfig.knobOutlineColor;
      context.stroke();
    }

    // Value
    var dv = displayValue;
    while (dv > 360) dv -= 360;
    while (dv < 0) dv += 360;
    text = dv.toFixed(1); // raw16pointsColorConfig.valueNbDecimal);
    len = 0;
    context.font = "bold " + Math.round(scale * 30) + "px " + directionColorConfig.font; // "bold 40px Arial"
    var metrics = context.measureText(text);
    len = metrics.width;

    context.beginPath();
    context.fillStyle = directionColorConfig.valueColor;
    context.fillText(text, (canvas.width / 2) - (len / 2), radius + 10);
    context.lineWidth = 1;
    context.strokeStyle = directionColorConfig.valueOutlineColor;
    context.strokeText(text, (canvas.width / 2) - (len / 2), radius + 10); // Outlined
    context.closePath();

    // Hand
    context.beginPath();
    context.lineWidth = 1;
    for (i = 90;i < 450 ;i+=ticks) // 0 is East. Turns clockwise
    {
      xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (i / 360)));
      yTo = (radius + 10) - ((radius * 0.85) * Math.sin(2 * Math.PI * (i / 360)));

      context.beginPath();
      context.arc(xTo, yTo, 5, 0, 2 * Math.PI, false);
      context.closePath();
      context.fillStyle = (displayValue === (i - 90)) ? 'red' : 'gray'; // raw16pointsColorConfig.knobColor;
      context.fill();
      context.strokeStyle = 'black'; // raw16pointsColorConfig.knobOutlineColor;
      context.stroke();
    }
  };

  this.drawSpike = function(canvas, radius, outsideRadius, insideRadius, angle, context) {
      var xFrom = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (angle / 360)));
      var yFrom = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (angle / 360)));
      //
      var xTo = (canvas.width / 2) - (insideRadius * Math.cos(2 * Math.PI * ((angle - 90)/ 360)));
      var yTo = (radius + 10) - (insideRadius * Math.sin(2 * Math.PI * ((angle - 90) / 360)));
      context.moveTo(xFrom, yFrom);
      context.lineTo(xTo, yTo);
      //
      xTo = (canvas.width / 2) - (insideRadius * Math.cos(2 * Math.PI * ((angle + 90)/ 360)));
      yTo = (radius + 10) - (insideRadius * Math.sin(2 * Math.PI * ((angle + 90) / 360)));
      context.moveTo(xFrom, yFrom);
      context.lineTo(xTo, yTo);
  };

  this.setValue = function(val)
  {
    instance.drawDisplay(canvasName, displaySize, val);
  };

  (function(){ instance.drawDisplay(canvasName, displaySize, instance.previousValue); })(); // Invoked automatically
}
