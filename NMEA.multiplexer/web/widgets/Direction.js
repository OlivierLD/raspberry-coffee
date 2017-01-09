/*
 * @author Olivier Le Diouris
 */
function Direction(cName, dSize, majorTicks, minorTicks)
{
  if (majorTicks === undefined)
    majorTicks = 45;
  if (minorTicks === undefined)
    minorTicks = 0;

  var canvasName = cName;
  var displaySize = dSize;

  var scale = dSize / 100;

  var running = false;
  this.previousValue = 0.0;
  this.intervalID = 0;
  this.valueToDisplay = 0;
  this.incr = 1;
  this.busy = false;
  
  var instance = this;
  
//try { console.log('in the Direction constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}
  
  this.setDisplaySize = function(ds)
  {
    scale = ds / 100;
    displaySize = ds;
    this.drawDisplay(canvasName, displaySize, instance.previousValue);
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
      if (canvasName === 'twdCanvas')    
        console.log('Starting animation between ' + this.previousValue + ' and ' + value + ', step ' + this.incr);
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
    if (canvasName === 'twdCanvas')
      console.log('       displayAndIncrement curr:' + this.valueToDisplay.toFixed(2) + ', final:' + finalValue + ', step ' + this.incr);
    if ((this.incr > 0 && this.valueToDisplay.toFixed(2) >= finalValue) || (this.incr < 0 && this.valueToDisplay.toFixed(2) <= finalValue))
    {
      if (canvasName === 'twdCanvas')
        console.log('Stop, ' + finalValue + ' reached, steps were ' + this.incr);
      //  console.log('Stop!')
      window.clearInterval(this.intervalID);
      this.intervalID = 0;
      this.previousValue = finalValue;
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

  this.drawDisplay = function(displayCanvasName, displayRadius, displayValue)
  {
    var digitColor = 'LightBlue';
    
    var canvas = document.getElementById(displayCanvasName);
    var context = canvas.getContext('2d');

    var radius = displayRadius;
  
    // Cleanup
  //context.fillStyle = "#ffffff";
  //context.fillStyle = "LightBlue";
    context.fillStyle = "transparent";
    context.fillRect(0, 0, canvas.width, canvas.height);    
  //context.fillStyle = 'rgba(255, 255, 255, 0.0)';
  //context.fillRect(0, 0, canvas.width, canvas.height);    
  
    context.beginPath();
  //context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);      
    context.arc(canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
    context.lineWidth = 5;
  
    var grd = context.createLinearGradient(0, 5, 0, radius);
    grd.addColorStop(0, 'black');// 0  Beginning
    grd.addColorStop(1, 'LightGrey');// 1  End
    context.fillStyle = grd;
    
    context.shadowBlur  = 10;
    context.shadowColor = 'black';
  
    context.lineJoin    = "round";
    context.fill();
    context.strokeStyle = 'DarkGrey';
    context.stroke();
    context.closePath();
    
    // Major Ticks
    context.beginPath();
    for (i = 0;i < 360 ;i+=majorTicks)
    {
      xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 360)));
      yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 360)));
      xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (i / 360)));
      yTo = (radius + 10) - ((radius * 0.85) * Math.sin(2 * Math.PI * (i / 360)));
      context.moveTo(xFrom, yFrom);
      context.lineTo(xTo, yTo);
    }
    context.lineWidth = 3;
    context.strokeStyle = 'LightGreen';
    context.stroke();
    context.closePath();
  
    // Minor Ticks
    if (minorTicks > 0)
    {
      context.beginPath();
      for (i = 0;i <= 360 ;i+=minorTicks)
      {
        xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 360)));
        yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 360)));
        xTo = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (i / 360)));
        yTo = (radius + 10) - ((radius * 0.90) * Math.sin(2 * Math.PI * (i / 360)));
        context.moveTo(xFrom, yFrom);
        context.lineTo(xTo, yTo);
      }
      context.lineWidth = 1;
      context.strokeStyle = 'LightGreen';
      context.stroke();
      context.closePath();
    }
    
    // Numbers
    context.beginPath();
    for (i = 0;i < 360 ;i+=majorTicks)
    {
      context.save();
      context.translate(canvas.width/2, (radius + 10)); // canvas.height);
      context.rotate((2 * Math.PI * (i / 360)));
      context.font = "bold " + Math.round(scale * 15) + "px Arial"; // Like "bold 15px Arial"
      context.fillStyle = digitColor;
      str = i.toString();
      len = context.measureText(str).width;
      context.fillText(str, - len / 2, (-(radius * .8) + 10));
      context.strokeStyle = 'black';
      context.strokeText(str, - len / 2, (-(radius * .8) + 10)); // Outlined  
      context.restore();
    }
    context.closePath();
    // Value
    var dv = displayValue;
    while (dv > 360) dv -= 360;
    while (dv < 0) dv += 360;
    text = dv.toFixed(0);
    len = 0;
    context.font = "bold " + Math.round(scale * 40) + "px Arial"; // "bold 40px Arial"
    var metrics = context.measureText(text);
    len = metrics.width;
  
    context.beginPath();
    context.fillStyle = 'LightGreen';
    context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
    context.lineWidth = 1;
    context.strokeStyle = 'black';
    context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined  
    context.closePath();
  
    // Hand
    context.beginPath();
    // Center
    context.moveTo(canvas.width / 2, radius + 10);
    // Left
    x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (displayValue / 360)))); //  - (Math.PI / 2))));
    y = (radius + 10) - ((radius * 0.05) * Math.sin((2 * Math.PI * (displayValue / 360)))); // - (Math.PI / 2))));
    context.lineTo(x, y);
    // Tip
    x = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (displayValue / 360) + (Math.PI / 2)));
    y = (radius + 10) - ((radius * 0.90) * Math.sin(2 * Math.PI * (displayValue / 360) + (Math.PI / 2)));
    context.lineTo(x, y);
    // Right
    x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (displayValue / 360) + (2 * Math.PI / 2))));
    y = (radius + 10) - ((radius * 0.05) * Math.sin((2 * Math.PI * (displayValue / 360) + (2 * Math.PI / 2))));
    context.lineTo(x, y);
  
    context.closePath();
    context.fillStyle = 'rgba(100, 0, 0, 0.25)';
    context.fill();
    context.lineWidth = 1;
    context.strokeStyle = 'red';
    context.stroke();
    // Knob
    context.beginPath();
    context.arc((canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
    context.closePath();
    context.fillStyle = '#fa5858';
    context.fill();
    context.strokeStyle = 'red';
    context.stroke();
  };
  
  this.setValue = function(val)
  {
    instance.drawDisplay(canvasName, displaySize, val);
  };

  (function(){ instance.drawDisplay(canvasName, displaySize, instance.previousValue); })(); // Invoked automatically
}