var clear = function(canvasName) {
  var canvas = document.getElementById(canvasName);
  var context = canvas.getContext('2d');

  // Cleanup
  context.fillStyle = "rgba(0, 0, 100, 10.0)";
  context.fillRect(0, 0, canvas.width, canvas.height);      
};

var fromPt, toPt;
var animationID;

var addCanvasListener = function(canvasName) {
  var canvas = document.getElementById(canvasName);
  canvas.addEventListener("click", // "click", "dblclick", "mousedown", "mouseup", "mousemove"
                          function(event) {
//                          console.log("Click on Canvas, event=" + (event == undefined?"undefined":("OK:" + event.clientX + ", " + event.clientY)));
                            var xClick;
                            var yClick;
                            
                            if (event.pageX || event.pageY) {
                              xClick = event.pageX;
                              yClick = event.pageY;
                            } else {
                              xClick = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft; 
                              yClick = event.clientY + document.body.scrollTop  + document.documentElement.scrollTop; 
                            } 
                            xClick -= canvas.offsetLeft;
                            yClick -= canvas.offsetTop;

                            if (toPt !== undefined) {
                              fromPt = toPt; // Swap
                              toPt = undefined;
                              clear(canvasName);
                              drawWorldMap(canvasName);
                              plotPoint(canvasName, fromPt, "red");
                            }
                            if (fromPt === undefined) {
                              fromPt = { "x":xClick, "y":yClick };
                              plotPoint(canvasName, fromPt, "red");
                            } else if (toPt === undefined) {
                              toPt = { "x":xClick, "y":yClick };
                              plotPoint(canvasName, toPt, "red");
                              currentStep = 0;
                              animationID = window.setInterval(function() { travel(canvasName, fromPt, toPt, 10); }, 100);
                            }
                          }, false);
};

var plotPoint = function(canvasName, pt, color) {
  var canvas = document.getElementById(canvasName);
  var context = canvas.getContext('2d');
  context.beginPath();
  context.fillStyle = color;
  context.arc(pt.x, pt.y, 2, 0, 2*Math.PI);
  context.stroke();
  context.fill();
};

var currentStep = 0;
var travel = function(canvasName, from, to, nbStep) {
  var newX = from.x + (currentStep * (to.x - from.x) / nbStep);
  var newY = from.y + (currentStep * (to.y - from.y) / nbStep);
  plotPoint(canvasName, {"x":newX, "y":newY}, "gray");
  currentStep++;
  if (currentStep > nbStep) {
    window.clearInterval(animationID);
  }
};

var drawWorldMap = function(canvasName) {
//var start = new Date().getTime();
  
  var canvas = document.getElementById(canvasName);
  var context = canvas.getContext('2d');

  try {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "data.xml", false);
    xhr.send();
    doc = xhr.responseXML; 
    
    var worldTop = doc.getElementsByTagName("top");
    var section  = worldTop[0].getElementsByTagName("section"); // We assume top has been found.
    
//  console.log("Found " + section.length + " section(s).")
    for (var i=0; i<section.length; i++) {
      var point = section[i].getElementsByTagName("point");
      var firstPt = null;
      var previousPt = null;
      context.beginPath();
      for (var p=0; p<point.length; p++) {
        var lat = parseFloat(point[p].getElementsByTagName("Lat")[0].childNodes[0].nodeValue);
        var lng = parseFloat(point[p].getElementsByTagName("Lng")[0].childNodes[0].nodeValue);
        if (lng < -180) lng += 360;
        if (lng > 180)  lng -= 360;
        var pt = posToCanvas(canvas, lat, lng);
        if (p === 0) {
          context.moveTo(pt.x, pt.y);
          firstPt = pt;
          previousPt = pt;
        } else {
          if (Math.abs(previousPt.x - pt.x) < (canvas.width / 2) && Math.abs(previousPt.y - pt.y) < (canvas.height / 2)) {
            context.lineTo(pt.x, pt.y);
            previousPt = pt;
          }
        }
      }
      if (firstPt !== null) {
        context.lineTo(firstPt.x, firstPt.y); // close the loop
      }
      context.lineWidth = 1;
      context.strokeStyle = 'black';
      context.stroke();
      context.fillStyle = "orange";
      context.fill();
      context.closePath();
    }
  } catch (ex) {
    alert("Oops:" + ex.toString());
  }
//var end = new Date().getTime();
//console.log("Operation completed in " + (end - start) + " ms.");
};

var plotPosToCanvas = function(canvasName, lat, lng, label) {
  var canvas = document.getElementById(canvasName); 
  var pt = posToCanvas(canvas, lat, lng);
  plotPoint(canvasName, pt, "red");
  if (label !== undefined) {
    try {
      var context = canvas.getContext('2d');
      context.fillStyle = "red";
      context.fillText(label, Math.round(pt.x) + 3, Math.round(pt.y) - 3);
    } catch (err) { // Firefox has some glitches here
      if (console.log !== undefined) {
        if (err.message !== undefined && err.name !== undefined) {
          console.log(err.message + " " + err.name);
        } else {
          console.log(err);
        }
      }
    }
  }
};

var posToCanvas = function(canvas, lat, lng) { // Anaximandre
  var x = (180 + lng) * (canvas.width / 360);
  var y = canvas.height - ((lat + 90) * canvas.height / 180);
  
  return { "x":x, "y":y };
};
