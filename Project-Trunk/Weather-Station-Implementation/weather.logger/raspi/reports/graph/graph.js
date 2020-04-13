function Graph(cName,       // Canvas Name
               cWidth,      // Canvas width
               cHeight,     // Canvas height
               graphData,   // x,y tuple array
               unitLabel,   
               minValue,    
               maxValue)
{
  var instance = this;
  var xScale, yScale;
  var minx, miny, maxx, maxy;
  var context;

  var canvasWidth = cWidth;

  var rawData = graphData;

  var smoothWidth;
  
  this.getSmoothWidth = function() { 
    return this.smoothWidth;
  };

  this.setSmoothWidth = function(sw) {
    this.smoothWidth = sw;
  };

  this.getCanvasWidth = function() {
    return canvasWidth;
  };

  this.setCanvasWidth = function(cw) {
    canvasWidth = cw;
  };

  var canvas = document.getElementById(cName);
  canvas.addEventListener('mousemove', function(evt)
  {
    if (document.getElementById("tooltip").checked)
    {
      var x = evt.pageX - canvas.offsetLeft;
      var y = evt.pageY - canvas.offsetTop;
      
      var coords = relativeMouseCoords(evt, canvas);
      x = coords.x;
      y = coords.y;
//    console.log("Mouse: x=" + x + ", y=" + y);
      
      var idx = Math.round(rawData.length * x / canvas.width)
      if (idx < rawData.length)
      {
        var str1; // = 'X : ' + x + ', ' + 'Y :' + y;
        var str2;
        try 
        { 
          str1 = rawData[idx].value;
          if (unitLabel) {
            str1 += (' ' + unitLabel);
          }
          str2 = parseSQLDate(rawData[idx].time).format("d-M H:i");
  //      console.log("Bubble:" + str);
        }
        catch (err) { console.log(JSON.stringify(err)); }
        
  //    context.fillStyle = '#000';
  //    context.fillRect(0, 0, w, h);
        instance.drawGraph(cName, rawData); // Demanding...
        context.fillStyle = "rgba(250, 250, 210, .7)"; 
        context.fillRect(x + 10, y + 10, 70, 30); // Background
        context.fillStyle = 'black';
        context.font = 'bold 12px verdana';
        context.fillText(str1, x + 15, y + 25, 60); 
        context.fillText(str2, x + 15, y + 37, 60); 
      }
    }
  }, 0);
  
  var relativeMouseCoords = function (event, element) {
    var totalOffsetX = 0;
    var totalOffsetY = 0;
    var canvasX = 0;
    var canvasY = 0;
    var currentElement = element;

    do {
      totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
      totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
    } while (currentElement = currentElement.offsetParent)

    canvasX = event.pageX - totalOffsetX;
    canvasY = event.pageY - totalOffsetY;

    return {x:canvasX, y:canvasY};
  };
  
  this.repaint = function() {
    this.drawGraph(cName, rawData);
  };

  this.minX = function(data) {
    var min = Number.MAX_VALUE;
    for (var i=0; i<data.length; i++) {
      min = Math.min(min, parseSQLDate(data[i].time).getTime());
    }
    return min;
  };
  
  this.minY = function(data) {
    var min = Number.MAX_VALUE;
    for (var i=0; i<data.length; i++) {
      if (data[i].value !== undefined) {
        min = Math.min(min, parseFloat(data[i].value));
      } else { // Assume Wind
        min = Math.min(min, parseFloat(data[i].ws));
      }
    }
    return min;
  };
  
  this.maxX = function(data) {
    var max = Number.MIN_VALUE;
    for (var i=0; i<data.length; i++) {
      max = Math.max(max, parseSQLDate(data[i].time).getTime());
    }
    return max;
  };
  
  this.maxY = function(data) {
    var max = Number.MIN_VALUE;
    for (var i=0; i<data.length; i++) {
      if (data[i].value !== undefined) {
        max = Math.max(max, parseFloat(data[i].value));
      } else { // Assume Wind
        max = Math.max(max, parseFloat(data[i].ws));
      }
    }
    return max;
  };
  
  this.getMinY = function() {
    return miny;
  };

  this.getMaxY = function() {
    return maxy;
  };

  this.drawGraph = function(displayCanvasName, data, idx) {
    context = canvas.getContext('2d');

    var _data = data;
    
    var _idxX;
    if (idx !== undefined)
      _idxX = idx * xScale;
    
    var mini = this.getMinY(); // Math.floor(this.minY(_data));
    var maxi = this.getMaxY(); // Math.ceil(this.maxY(_data));
    var gridXStep = Math.round((maxi - mini) / 3);
    var gridYStep = Math.round(_data.length / 10);
    
    // Sort the tuples (on X)
//  data.sort(sortTupleX);
    
    console.log("Smoothing...");
    var smoothData = [];
    // 1 - More data (X times more)
    var X = 1; // was 10
    for (var i=0; i<_data.length - 1; i++) {
      for (var j=0; j<X; j++) {
        var _x = parseSQLDate(_data[i].time).getTime() + (j * (parseSQLDate(_data[i + 1].time).getTime() - parseSQLDate(_data[i].time).getTime()) / 10);
        var _y = 
          (_data[i].value !== undefined) ? 
          (parseFloat(_data[i].value) + (j * (parseFloat(_data[i + 1].value) - parseFloat(_data[i].value)) / 10)) :
          (parseFloat(_data[i].ws) + (j * (parseFloat(_data[i + 1].ws) - parseFloat(_data[i].ws)) / 10));
        smoothData.push(new Tuple(_x, _y));
      }
    }
    // 2 - Smooth
    var _smoothData = [];
    if (this.smoothWidth === undefined) {
      this.smoothWidth = Math.round((smoothData.length / 2) / 10) * 2; // Must be even
    }
    for (var i=0; i<smoothData.length; i++) {
      var yAccu = 0;
      for (var acc=i-(this.smoothWidth / 2); acc<i+(this.smoothWidth/2); acc++) {
        var y;
        if (acc < 0)
          y = smoothData[0].getY();
        else if (acc > (smoothData.length - 1))
          y = smoothData[smoothData.length - 1].getY();
        else
          y = smoothData[acc].getY();
        yAccu += y;
      }
      yAccu = yAccu / this.smoothWidth;
      _smoothData.push(new Tuple(smoothData[i].getX(), yAccu));
//    console.log("I:" + smoothData[i].getX() + " y from " + smoothData[i].getY() + " becomes " + yAccu);
    }
    smoothData = _smoothData;
    
    console.log("Drawing...");
    context.fillStyle = "LightGray";
    context.fillRect(0, 0, canvas.width, canvas.height);    

    // Horizontal grid (value)
    for (var i=Math.round(mini); i<maxi; i+=gridXStep) {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'gray';
      context.moveTo(0, cHeight - (i - mini) * yScale);
      context.lineTo(canvasWidth, cHeight - (i - mini) * yScale);
      context.stroke();

      context.save();
      context.font = "bold 10px Arial"; 
      context.fillStyle = 'black';
      str = i.toString() + ' ' + unitLabel; // + " kt";
      len = context.measureText(str).width;
      context.fillText(str, canvasWidth - (len + 2), cHeight - ((i - mini) * yScale) - 2);
      context.restore();            
      context.closePath();
    }
    
    // Vertical grid (Time)
    for (var i=gridYStep; i<_data.length; i+=gridYStep) {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'gray';
      var x = parseSQLDate(_data[i].time).getTime() - minx;
  //  console.log(">>> i: " + i + ", x:" + x);
      context.moveTo(x * xScale, 0);
      context.lineTo(x * xScale, cHeight);
      context.stroke();

      // Rotate the whole context, and then write on it (that's why we need the translate)
      context.save(); 
      context.translate(x * xScale, canvas.height);
      context.rotate(-Math.PI / 2);
      context.font = "bold 10px Arial"; 
      context.fillStyle = 'black';
      str = parseSQLDate(_data[i].time).format("D d-M H:i");
      len = context.measureText(str).width;
      context.fillText(str, 2, -1); //i * xScale, cHeight - (len));
      context.restore();            
      context.closePath();
    }

    if (document.getElementById("raw-data").checked) { // Raw data
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'blue';
  
      var previousPoint = _data[0];
      for (var i=1; i<_data.length; i++) {
        if (_data[i].value !== undefined) {
          context.moveTo((parseSQLDate(previousPoint.time).getTime() - minx) * xScale, cHeight - (parseFloat(previousPoint.value) - miny) * yScale);
          context.lineTo((parseSQLDate(_data[i].time).getTime() - minx) * xScale, cHeight - (parseFloat(_data[i].value) - miny) * yScale);
        } else {
          context.moveTo((parseSQLDate(previousPoint.time).getTime() - minx) * xScale, cHeight - (parseFloat(previousPoint.ws) - miny) * yScale);
          context.lineTo((parseSQLDate(_data[i].time).getTime() - minx) * xScale, cHeight - (parseFloat(_data[i].ws) - miny) * yScale);
        }
        context.stroke();
        previousPoint = _data[i];
      }
      context.closePath();
    }
    
    if (document.getElementById("smooth-data").checked) { // Smoothed data
      _data = smoothData;
      
      context.beginPath();
      context.lineWidth = 3;
      context.strokeStyle = 'red';
  
      previousPoint = _data[0];
      for (var i=1; i<_data.length; i++) {
        context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
        context.lineTo((_data[i].getX() - minx) * xScale, cHeight - (_data[i].getY() - miny) * yScale);
        context.stroke();
        previousPoint = _data[i];
      }
      // TODO Wind Direction

      context.closePath();
    }
    
    if (idx !== undefined) {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'green';
      context.moveTo(_idxX, 0);
      context.lineTo(_idxX, cHeight);
      context.stroke();
      context.closePath();
    }
  };

  this.rescale = function() {
     minx = instance.minX(rawData);
     miny = (minValue ? minValue : instance.minY(rawData));
     maxx = instance.maxX(rawData);
     maxy = (maxValue ? maxValue : instance.maxY(rawData));
     
//   console.log("MinX:" + minx + ", MaxX:" + maxx + ", MinY:" + miny + ", MaxY:" + maxy);
     
     xScale = canvasWidth / (maxx - minx);
     yScale = cHeight / (maxy - miny);
   };
  
  (function() { 
     instance.rescale();
//   console.log("xScale:" + xScale + ", yScale:" + yScale);
     
     instance.drawGraph(cName, rawData);
   })(); // Invoked automatically when new is invoked.  
};

function Tuple(_x, _y) {
  var x = _x;
  var y = _y;
  
  this.getX = function() { return x; };
  this.getY = function() { return y; };
};

function sortTupleX(t1, t2) {
  if (t1.getX() < t2.getX())
    return -1;
  if (t1.getX() > t2.getX())
    return 1;
  return 0;  
};
