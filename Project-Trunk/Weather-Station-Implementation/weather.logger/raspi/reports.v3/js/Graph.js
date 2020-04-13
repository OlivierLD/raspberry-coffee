/**
 * Nice color picker at https://www.w3schools.com/colors/colors_names.asp
 *
 * That Graph sounds not to be generic enough to make a Web Component out of it...
 */

function Graph(cName,       // Canvas Name
               cWidth,      // Canvas width
               cHeight,     // Canvas height
               graphData,   // x,y tuple array
               graphData2,  // same as above, secondary data
               callback,    // Callback on mouse-click
               unit,
               withWindDir,
               dataType) {
  let instance = this;
  let xScale = 0, yScale = 0;
  let minx = 0, miny = 0, maxx = 0, maxy= 0;
  var context;

  this.unit = unit;
  var lastClicked;

  this.dType = dataType;

  let canvas = document.getElementById(cName);
  canvas.width = cWidth;
  canvas.height = cHeight;

  canvas.addEventListener('click', function(evt) {
      let x = evt.pageX - canvas.offsetLeft;
      let y = evt.pageY - canvas.offsetTop;

      let coords = relativeMouseCoords(evt, canvas);
      x = coords.x;
      y = coords.y;
//    console.log("Mouse: x=" + x + ", y=" + y);

      let idx = Math.round(x / xScale);
      if (idx < JSONParser.nmeaData.length) {
        if (callback !== undefined) {
          callback(idx);
        }
        lastClicked = idx;
      }
  }, 0);

	function repaint() {
		instance.drawGraph(cName, graphData, graphData2, lastClicked, instance.dType);
		if (withWindDir) {
			instance.drawWind(JSONParser.nmeaData);
		}
	}

	canvas.addEventListener('mouseout', function(evt) {
		if (document.getElementById("tooltip").checked) {
			repaint(); // To erase the tooltip
		}
	});

  canvas.addEventListener('mousemove', function(evt) {
    if (document.getElementById("tooltip").checked) {
      let x = evt.pageX - canvas.offsetLeft;
      let y = evt.pageY - canvas.offsetTop;

      let coords = relativeMouseCoords(evt, canvas);
      x = coords.x;
      y = coords.y;

//    console.log("Mouse: x=" + x + ", y=" + y);

      let idx = Math.round(x / xScale);
      if (idx < JSONParser.nmeaData.length) {
        let str = []; // Will contain the lines to display in the tooltip.
        try {
          str.push(JSONParser.nmeaData[idx].getNMEATws() + "kt @ " + JSONParser.nmeaData[idx].getNMEATwd() + "\272");
	        str.push("G: " + JSONParser.nmeaData[idx].getNMEAGust() + "kt");
          str.push("P:" + JSONParser.nmeaData[idx].getNMEAPrmsl() + " hPa");
          if (document.getElementById("utc-display").checked) {
	          str.push(new Date(JSONParser.nmeaData[idx].getNMEADate()).format("d-M-Y H:i") + " UT");
          } else {
	          str.push(reformatDate(JSONParser.nmeaData[idx].getNMEADate(), "d-M-Y H:i"));
          }
          str.push("Temp: " + JSONParser.nmeaData[idx].getNMEATemp() + "\272C");
          str.push("Hum: "  + JSONParser.nmeaData[idx].getNMEAHum() + " %");
          str.push("Rain: " + JSONParser.nmeaData[idx].getNMEARain() + " mm");
          str.push("DewPoint: "  + JSONParser.nmeaData[idx].getNMEADew() + "\272C");
  //      console.log("Bubble:" + str);
        } catch (err) { console.log(JSON.stringify(err)); }

  //    context.fillStyle = '#000';
  //    context.fillRect(0, 0, w, h);
	      repaint();

        // instance.drawGraph(cName, graphData, lastClicked, instance.dType);
        // if (withWindDir) {
        //   instance.drawWind(JSONParser.nmeaData);
        // }
        let tooltipW = 120, nblines = str.length;
        context.fillStyle = "rgba(250, 250, 210, .7)";
//      context.fillStyle = 'yellow';
        let fontSize = 10;
        let x_offset = 10, y_offset = 10;

        if (x > (cWidth / 2)) {
          x_offset = -(tooltipW + 10);
        }
        if (y > (cHeight / 2)) {
          y_offset = -(10 + 6 + (nblines * fontSize));
        }
        context.fillRect(x + x_offset, y + y_offset, tooltipW, 6 + (nblines * fontSize)); // Background
        context.fillStyle = 'black';
        context.font = /*'bold ' +*/ fontSize + 'px verdana';
        for (let i=0; i<str.length; i++) {
          context.fillText(str[i], x + x_offset + 5, y + y_offset + (3 + (fontSize * (i + 1)))); //, 60);
        }
      }
    }
  }, false);

  function relativeMouseCoords(event, element) {
	  let bcr = element.getBoundingClientRect();

	  let canvasX = event.clientX;
	  let canvasY = event.clientY;

	  canvasX -= bcr.left;
	  canvasY -= bcr.top;

    return {
    	x: canvasX,
	    y: canvasY
    };
  }

  this.minX = function(data) { // data = array of arrays
    let min = Number.MAX_VALUE;
    for (let i=0; i<data.length; i++) {
	    for (let j=0; j<data[i].length; j++) {
		    min = Math.min(min, data[i][j].getX());
	    }
    }
    return min;
  };

  this.minY = function(data) { // data = array of arrays
    let min = Number.MAX_VALUE;
    for (let i=0; i<data.length; i++) {
	    for (let j=0; j<data[i].length; j++) {
		    min = Math.min(min, data[i][j].getY());
	    }
    }
    return min;
  };

  this.maxX = function(data) { // data = array of arrays
    let max = -Number.MAX_VALUE;
    for (let i=0; i<data.length; i++) {
	    for (let j=0; j<data[i].length; j++) {
		    max = Math.max(max, data[i][j].getX());
	    }
    }
    return max;
  };

  this.maxY = function(data) { // data = array of arrays
    let max = -Number.MAX_VALUE;
    for (let i=0; i<data.length; i++) {
	    for (let j=0; j<data[i].length; j++) {
		    max = Math.max(max, data[i][j].getY());
	    }
    }
    return max;
  };

  this.drawGraph = function(displayCanvasName, data, data2, idx, dataType) {

  	var len, str;

    context = canvas.getContext('2d');

    this.dType = dataType;
    init([ data, data2 ]);
    switch (this.dType) {
      case "PRESS":
        miny =  970;
        maxy = 1040;
        break;
      case "HUM":
        miny =   0;
        maxy = 100;
        break;
	    case "DEW":
	    case "TEMP":
	    case "TEMP-DEW":
				miny -= 1;
				maxy += 1;
				break;
	    case "RAIN":
		    miny = 0;
		    maxy = Math.max(2.5,  Math.ceil(maxy)); // Math.ceil(this.maxY(data))); // at least 2.5mm
		    break;
        default:
          break;
    }
    scale();

    var _idxX;
    if (idx !== undefined) {
      _idxX = idx * xScale;
    }

    let mini = miny; // (this.dType === "PRESS" ?  970 : Math.floor(this.minY(data)));
    let maxi = maxy; // (this.dType === "PRESS" ? 1040 : Math.ceil(this.maxY(data)));
    let gridXStep = Math.round(JSONParser.nmeaData.length / 10);
    let gridYStep = (this.dType === "PRESS" ? 10 : Math.round((maxi - mini) / 5));

    // Sort the tuples (on X, time)
    data.sort(sortTupleX);
    let smoothData = data;
	  let smoothData2 = data2;
    let _smoothData = [];
    let smoothWidth = 20;
    for (let i=0; i<smoothData.length; i++) {
      let yAccu = 0;
      for (let acc=i-(smoothWidth / 2); acc<i+(smoothWidth/2); acc++) {
        var y;
        if (acc < 0) {
          y = smoothData[0].getY();
        } else if (acc > (smoothData.length - 1)){
          y = smoothData[smoothData.length - 1].getY();
        } else {
          y = smoothData[acc].getY();
        }
        yAccu += y;
      }
      yAccu = yAccu / smoothWidth;
      _smoothData.push(new Tuple(smoothData[i].getX(), yAccu));
//    console.log("I:" + smoothData[i].getX() + " y from " + smoothData[i].getY() + " becomes " + yAccu);
    }

	  let _smoothData2 = [];
	  if (data2 !== undefined && data2.length > 0) {
		  data2.sort(sortTupleX);
		  for (let i=0; i<smoothData2.length; i++) {
			  let yAccu = 0;
			  for (let acc=i-(smoothWidth / 2); acc<i+(smoothWidth/2); acc++) {
				  var y;
				  if (acc < 0) {
					  y = smoothData2[0].getY();
				  } else if (acc > (smoothData2.length - 1)){
					  y = smoothData2[smoothData2.length - 1].getY();
				  } else {
					  y = smoothData2[acc].getY();
				  }
				  yAccu += y;
			  }
			  yAccu = yAccu / smoothWidth;
			  _smoothData2.push(new Tuple(smoothData2[i].getX(), yAccu));
//    console.log("I:" + smoothData[i].getX() + " y from " + smoothData[i].getY() + " becomes " + yAccu);
		  }
	  }

	  // Clear
    context.fillStyle = "white";
    context.fillRect(0, 0, canvas.width, canvas.height);

    smoothData = _smoothData;
	  smoothData2 = _smoothData2;
    if (false) {
      context.fillStyle = "white";
      context.fillRect(0, 0, canvas.width, canvas.height);
    } else {
      let grV = context.createLinearGradient(0, 0, 0, context.canvas.height);
      grV.addColorStop(0, this.dType === "PRESS" ? 'white' : 'rgba(0,0,0,0)');
      grV.addColorStop(1, this.dType === "PRESS" ?  "GhostWhite" : 'cyan'); // "LightGray"); // '#000');

      context.fillStyle = grV;
      context.fillRect(0, 0, context.canvas.width, context.canvas.height);
    }
    // Horizontal grid (TWS, Hum, Press, Temp, or so)
    let gridColor = (this.dType === "PRESS" ? 'DarkOrange' : 'gray');
	  let letterColor = (this.dType === "PRESS" ? 'DarkOrange' : 'black');

    if (this.dType === "PRESS") { // intermediate grids.
        for (let i=Math.round(mini); i<maxi; i+=1) {
            context.beginPath();
            context.lineWidth = 0.1;
            context.strokeStyle = gridColor;
            context.moveTo(0, cHeight - (i - mini) * yScale);
            context.lineTo(cWidth, cHeight - (i - mini) * yScale);
            context.stroke();
            context.closePath();
        }
    }

    for (let i=Math.round(mini); gridYStep>0 && i<maxi; i+=gridYStep) {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = gridColor;
      context.moveTo(0, cHeight - (i - mini) * yScale);
      context.lineTo(cWidth, cHeight - (i - mini) * yScale);
      context.stroke();

      context.save();
      context.font = "bold 10px Arial";
      context.fillStyle = letterColor;
	    str = i.toString() + " " + unit;
	    len = context.measureText(str).width;
      context.fillText(str, cWidth - (len + 2), cHeight - ((i - mini) * yScale) - 2);
      context.restore();
      context.closePath();
    }

    // Vertical grid (Time)
    for (let i=gridXStep; i<data.length; i+=gridXStep) {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = gridColor;
      context.moveTo(i * xScale, 0);
      context.lineTo(i * xScale, cHeight);
      context.stroke();

      // Rotate the whole context, and then write on it (that's why we need the translate)
      context.save();
      context.translate(i * xScale, canvas.height);
      context.rotate(-Math.PI / 2);
      context.font = "bold 10px Arial";
      context.fillStyle = letterColor;
      if (document.getElementById("utc-display").checked) {
	      str = new Date(JSONParser.nmeaData[i].getNMEADate()).format("d-M H:i") + " UT";
      } else {
	      str = reformatDate(JSONParser.nmeaData[i].getNMEADate(), "d-M H:i");
      }
      len = context.measureText(str).width;
      context.fillText(str, 2, -1); //i * xScale, cHeight - (len));
      context.restore();
      context.closePath();
    }

    if (document.getElementById("raw-data").checked) { // Raw data
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'green';

      let previousPoint = data[0];
      context.moveTo((data[0].getX() - minx) * xScale, cHeight - (data[0].getY() - miny) * yScale);
      for (let i=1; i<data.length; i++) {
    //  context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
        context.lineTo((data[i].getX() - minx) * xScale, cHeight - (data[i].getY() - miny) * yScale);
    //  context.stroke();
        previousPoint = data[i];
      }
      context.lineTo(context.canvas.width, context.canvas.height);
      context.lineTo(0, context.canvas.height);
      context.closePath();
      context.stroke();
      context.fillStyle = 'rgba(0, 255, 0, 0.35)';
      context.fill();

      if (data2 !== undefined && data2.length > 0) {
	      context.beginPath();
	      context.lineWidth = 1;
	      context.strokeStyle = 'blue';

	      previousPoint = data2[0];
	      context.moveTo((data2[0].getX() - minx) * xScale, cHeight - (data2[0].getY() - miny) * yScale);
	      for (let i=1; i<data2.length; i++) {
		      //  context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
		      context.lineTo((data2[i].getX() - minx) * xScale, cHeight - (data2[i].getY() - miny) * yScale);
		      //  context.stroke();
		      previousPoint = data2[i];
	      }
	      context.lineTo(context.canvas.width, context.canvas.height);
	      context.lineTo(0, context.canvas.height);
	      context.closePath();
	      context.stroke();
	      // context.fillStyle = 'rgba(0, 255, 0, 0.35)';
	      // context.fill();
      }
    }

    if (document.getElementById("smooth-data").checked) { // Smoothed data
      data = smoothData;
			data2 = smoothData2;

      context.beginPath();
      context.lineWidth = 3;
      context.strokeStyle = (this.dType === "PRESS" ? 'indigo' : 'red');

      previousPoint = data[0];
      context.moveTo((data[0].getX() - minx) * xScale, cHeight - (data[0].getY() - miny) * yScale);
      for (let i=1; i<data.length; i++) {
//      context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - mini) * yScale);
        context.lineTo((data[i].getX() - minx) * xScale, cHeight - (data[i].getY() - miny) * yScale);
//      context.stroke();
        previousPoint = data[i];
      }
      if (this.dType !== "PRESS") {
          // Close the shape, bottom
          context.lineTo(context.canvas.width, context.canvas.height);
          context.lineTo(0, context.canvas.height);
          context.closePath();
      }
      context.stroke();
      if (this.dType !== "PRESS") {
          context.fillStyle = 'rgba(255, 0, 0, 0.35)';
          context.fill();
      }
	    if (data2 !== undefined && data2.length > 0) {
		    context.beginPath();
		    context.lineWidth = 3;
		    context.strokeStyle = 'blue';

		    previousPoint = data2[0];
		    context.moveTo((data2[0].getX() - minx) * xScale, cHeight - (data2[0].getY() - miny) * yScale);
		    for (let i=1; i<data2.length; i++) {
//      context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - mini) * yScale);
			    context.lineTo((data2[i].getX() - minx) * xScale, cHeight - (data2[i].getY() - miny) * yScale);
//      context.stroke();
			    previousPoint = data2[i];
		    }
		    // Close the shape, bottom
		    // context.lineTo(context.canvas.width, context.canvas.height);
		    // context.lineTo(0, context.canvas.height);
		    // context.closePath();

		    context.stroke();
	    }
	  }

	  // Vertical index (click)
    if (idx !== undefined) {
      context.beginPath();
      context.lineWidth = 1;
      context.strokeStyle = 'green';
      context.moveTo(_idxX, 0);
      context.lineTo(_idxX, cHeight);
      context.stroke();
      context.closePath();
    }

    if (withWindDir) {
      this.drawWind(JSONParser.nmeaData);
    }
  };

  const ARROW_LEN = 20;

  this.drawWind = function(nmea) {
    if (nmea !== undefined) {
      for (let i=0; i<nmea.length; i+=2)  {
        let wd = parseFloat(nmea[i].getNMEATwd() + 180);
        while (wd > 360) {
	        wd -= 360;
        }
        let twd = Math.toRadians(wd);
        context.beginPath();
        let x = i * (cWidth / nmea.length);
        let y = cHeight / 2;
        let dX = ARROW_LEN * Math.sin(twd);
        let dY = - ARROW_LEN * Math.cos(twd);
        // create a new line object
        let line = new Line(x, y, x + dX, y + dY);
        // draw the line
        line.draw(context);
//      line.drawWithArrowhead(context);
//      line.drawWithWindFeathers(context, parseFloat(nmea[i].getNMEATws()));
        context.closePath();
      }
    }
  };

  function init(gd) {
    minx = instance.minX(gd);
    miny = instance.minY(gd);
    maxx = instance.maxX(gd);
    maxy = instance.maxY(gd);
  }

  function scale() {
      xScale = cWidth / (maxx - minx);   // was Math.floor(canvas.getBoundingClientRect().width)
      yScale = cHeight / (maxy - miny);  // was canvas.getBoundingClientRect().height
  }

  (function() {
    if (graphData !== undefined && graphData.length > 0) {
        init([ graphData, graphData2 ]);
//   console.log("MinX:" + minx + ", MaxX:" + maxx + ", MinY:" + miny + ", MaxY:" + maxy);
        scale();
//  console.log("xScale:" + xScale + ", yScale:" + yScale);
    }
    instance.drawGraph(cName, graphData, graphData2);
   })(); // Invoked automatically when new is invoked.
}

function Tuple(_x, _y) {
  let x = _x;
  let y = _y;

  this.getX = function() { return x; };
  this.getY = function() { return y; };
}

function sortTupleX(t1, t2) {
  if (t1.getX() < t2.getX()) {
    return -1;
  }
  if (t1.getX() > t2.getX()){
    return 1;
  }
  return 0;
}
