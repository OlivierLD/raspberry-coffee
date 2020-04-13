/*
 * @author Olivier Le Diouris
 */
function BoatOverview(cName) {   // Canvas name
  var instance = this;
  var cWidth, cHeight;
  
  var canvas;
  var context;
  
  // NMEA Data
  var  bsp = 0, hdg = 0, tws = 0, twa = 0, twd = 0, aws = 0, awa = 0,
       leeway = 0, vmg = 0, cog = 0, sog = 0, cmg = 0, csp = 0, cdr = 0, b2wp = 0;
  
  var VMW_ON_WIND     = 0;
  var VMW_ON_WAYPOINT = 1;
  
  var vmgOption = VMW_ON_WIND;
  var toWayPoint = "";
      
  this.setVMGonWind = function() {
    vmgOption = VMW_ON_WIND;
  };
  
  this.setVMGto = function(waypoint) {
    vmgOption = VMW_ON_WAYPOINT;
    toWayPoint = waypoint;
  };
  
  this.setB2WP = function(d) {
    b2wp = d;
    instance.drawGraph();
  };
  this.setBSP = function(d) {
    bsp = d;
    instance.drawGraph();
  };
  this.setVMG = function(d) {
    vmg = d;
    instance.drawGraph();
  };
  this.setTWA = function(d) {
    twa = d;
    instance.drawGraph();
  };
  this.setTWS = function(d) {
    tws = d;
    instance.drawGraph();
  };
  this.setTWD = function(d) {
    twd = d;
    instance.drawGraph();
  };
  this.setHDG = function(d) {
    hdg = d;
    instance.drawGraph();
  };
  this.setAWA = function(d) {
    awa = d;
    instance.drawGraph();
  };
  this.setAWS = function(d) {
    aws = d;
    instance.drawGraph();
  };
  this.setCOG = function(d) {
    cog = d;
    instance.drawGraph();
  };
  this.setSOG = function(d) {
    sog = d;
    instance.drawGraph();
  };
  this.setCSP = function(d) {
    csp = d;
    instance.drawGraph();
  };
  this.setCDR = function(d) {
    cdr = d;
    instance.drawGraph();
  };
  this.setCMG = function(d) {
    cmg = d;
    instance.drawGraph();
  };
  this.setLeeway = function(d) {
    leeway = d;
    instance.drawGraph();
  };

  var rotate = function(p, angle) {
    var r = new Point(Math.round((p.x * Math.cos(toRadians(angle))) + (p.y * Math.sin(toRadians(angle)))), 
                      Math.round((p.x * -Math.sin(toRadians(angle))) + (p.y * Math.cos(toRadians(angle)))));
    return r;
  };
    
  var getDir = function(x, y) {
    var dir = 0.0;
    if (y != 0)
      dir = toDegrees(Math.atan(x / y));
    if (x <= 0 || y <= 0) {
      if (x > 0 && y < 0)
        dir += 180;
      else if (x < 0 && y > 0)
        dir += 360;
      else if (x < 0 && y < 0)
        dir += 180;
      else if (x == 0) {
        if (y > 0)
          dir = 0.0;
        else
          dir = 180;
      } else if (y == 0) {
        if (x > 0)
          dir = 90;
        else
          dir = 270;
      }
    }
    dir += 180;
    while (dir >= 360)
      dir -= 360;
    return dir;
  };
    
  // Line with arrow head
  function Line(x1, y1, x2, y2) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  };
  Line.prototype.drawWithArrowhead = function(ctx) {
    this.drawWithArrowheads(ctx, false);
  };
  
  Line.prototype.drawWithArrowheads = function(ctx, both) {
    if (both === undefined)
      both = true;
    // arbitrary styling
//  ctx.strokeStyle = "blue";
//  ctx.fillStyle   = "blue";
//  ctx.lineWidth   = 1;
  
    // draw the line
    ctx.beginPath();
    ctx.moveTo(this.x1, this.y1);
    ctx.lineTo(this.x2, this.y2);
    ctx.stroke();
  
    if (both) {
      // draw the starting arrowhead
      var startRadians = Math.atan((this.y2 - this.y1) / (this.x2 - this.x1));
      startRadians += ((this.x2>this.x1)?-90:90) * Math.PI/180;
      this.drawArrowhead(ctx, this.x1, this.y1, startRadians);
    }
    // draw the ending arrowhead
    var endRadians=Math.atan((this.y2 - this.y1) / (this.x2 - this.x1));
    endRadians += ((this.x2>this.x1)?90:-90) * Math.PI/180;
    this.drawArrowhead(ctx, this.x2, this.y2, endRadians);
  };
  
  Line.prototype.drawWithAnemoArrowheads = function(ctx) {
    // draw the line
    ctx.beginPath();
    ctx.moveTo(this.x1, this.y1);
    ctx.lineTo(this.x2, this.y2);
    ctx.stroke();
  
    var endRadians = Math.atan((this.y2 - this.y1) / (this.x2 - this.x1));
    endRadians += ((this.x2>this.x1)?90:-90) * Math.PI/180;
    this.drawArrowhead(ctx, this.x2 - (this.x2 - this.x1) / 2, this.y2 - (this.y2 - this.y1) / 2, endRadians);
  };
  
  Line.prototype.drawHollowArrow = function(ctx) {
    var headLength = 30;
    var arrowWidth = 10; // Ondulation width
    var headWidth  = 20;
    
    var dir = getDir((this.x1 - this.x2), (this.y2 - this.y1));
    var len = Math.sqrt(((this.x1 - this.x2) * (this.x1 - this.x2)) + ((this.y2 - this.y1) * (this.y2 - this.y1)));
    
    var one, two, three, four, five, six, seven, eight;
    one   = new Point(0, 0);
    two   = new Point(-arrowWidth / 2,
                      0);
    three = new Point(-arrowWidth / 2,
                      -(Math.round(len - headLength)));
    four  = new Point(-headWidth / 2,
                      -(Math.round(len - headLength)));
    five  = new Point(0, -Math.round(len)); // to
    six   = new Point(headWidth / 2,
                      -(Math.round(len - headLength)));
    seven = new Point(arrowWidth / 2,
                      -(Math.round(len - headLength)));
    eight = new Point(arrowWidth / 2,
                      0);
    one   = rotate(one, -dir);
    two   = rotate(two, -dir);
    three = rotate(three, -dir);
    four  = rotate(four, -dir);
    five  = rotate(five, -dir);
    six   = rotate(six, -dir);
    seven = rotate(seven, -dir);
    eight = rotate(eight, -dir);    
    
    var x = new Array();
    var y = new Array();
    
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

    context.beginPath();
    context.moveTo(x[0], y[0]);
    for (var i=1; i<x.length; i++) {
      context.lineTo(x[i], y[i]);
    }
    context.closePath();
    context.stroke();
  };
  
  var HEAD_LENGTH = 20;
  var HEAD_WIDTH  = 6;
  Line.prototype.drawArrowhead = function(ctx, x, y, radians) {
    ctx.save();
    ctx.beginPath();
    ctx.translate(x, y);
    ctx.rotate(radians);
    ctx.moveTo(0, 0);
    ctx.lineTo( HEAD_WIDTH, HEAD_LENGTH);
    ctx.lineTo(-HEAD_WIDTH, HEAD_LENGTH);
    ctx.closePath();
    ctx.restore();
    ctx.fill();
  };
  
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
  
  var speedScale = 10;
  
  var getCanvasCenter = function() {
    var cw = document.getElementById(cName).width;
    var ch = document.getElementById(cName).height;
    var distFromRight = Math.min(cw, ch) / 2;
    
    return { x: cw - distFromRight, y: ch / 2};
  };
  
  this.drawGraph = function() {
    var maxSpeed = 5;
    maxSpeed = Math.max(maxSpeed, sog);
    maxSpeed = Math.max(maxSpeed, bsp);
    maxSpeed = Math.max(maxSpeed, tws);
    maxSpeed = Math.max(maxSpeed, aws);
    speedScale = 5 * (Math.ceil(maxSpeed / 5));
    
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    context = canvas.getContext('2d');
    
 // context.fillStyle = "LightGray";
    var grd = context.createLinearGradient(0, 5, 0, document.getElementById(cName).height);
    grd.addColorStop(0, 'LightGray') ; // 'gray');    // 0  Beginning
    grd.addColorStop(1, 'black'); // 'LightGray');// 1  End
    context.fillStyle = grd;

    context.fillRect(0, 0, canvas.width, canvas.height);    

//  context.beginPath();
//  context.lineWidth = 1;
//  context.strokeStyle = 'black';
//  context.strokeText("Overview", 10, 20); // Outlined  
//  context.closePath();
    // Circles
    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;
    
    context.strokeStyle = 'gray';
    for (var circ=1; circ<=speedScale; circ++) {
      var radius = Math.round(circ * ((Math.min(cHeight, cWidth) / 2) / speedScale));
      context.beginPath();
      if (circ % 5 == 0)
        context.lineWidth = 3;
      else
        context.lineWidth = 1;
      context.arc(x, y, radius, 0, 2 * Math.PI);        
      context.closePath();
      context.stroke();
    }
    
    instance.drawBoat(hdg);
    instance.drawTrueWind();
    instance.drawAppWind();
    instance.drawVW();
    instance.drawBSP();
    instance.drawCMG();
    instance.drawSOG();
    if (document.getElementById("display.current").checked)
      instance.drawCurrent();
    if (document.getElementById("display.vmg").checked)
      instance.drawVMG();
    
    // Display values
    // See http://www.w3schools.com/tags/ref_entities.asp, &deg; = &#176;
    context.fillStyle = 'green';
    context.font="bold 16px Courier New";
    var txtY = 20;
    var space = 18;
    var col1 = 10, col2 = 90;
    context.fillText("BSP", col1, txtY);
    context.fillText(bsp + " kts", col2, txtY);
    txtY += space;    
    context.fillText("HDG", col1, txtY);
    context.fillText(hdg.toFixed(0) + "" /* "�" */, col2, txtY);
    txtY += space;    
    context.fillText("AWS", col1, txtY);
    context.fillText(aws + " kts", col2, txtY);
    txtY += space;    
    context.fillText("AWA", col1, txtY);
    context.fillText(awa + "" /* "�" */, col2, txtY);
    context.fillStyle = 'blue';
    txtY += space;    
    context.fillText("TWS", col1, txtY);
    context.fillText(tws.toFixed(2) + " kts", col2, txtY);
    txtY += space;    
    context.fillText("TWA", col1, txtY);
    context.fillText(twa + "" /* "�" */, col2, txtY);
    txtY += space;    
    context.fillText("TWD", col1, txtY);
    context.fillText(twd + "" /* "�" */, col2, txtY);
    txtY += space;    
    context.fillText("CDR", col1, txtY);
    context.fillText(cdr.toFixed(0) + "" /* "�" */, col2, txtY);
    txtY += space;    
    context.fillText("CSP", col1, txtY);
    context.fillText(csp.toFixed(2) + " kts", col2, txtY);

    txtY += space;    
    context.fillText("leeway", col1, txtY);
    context.fillText(leeway.toFixed(2) + "" /* "�" */, col2, txtY);
    txtY += space;    
    context.fillText("CMG", col1, txtY);
    context.fillText(cmg.toFixed(0) + "" /* "�" */, col2, txtY);
  };
  
  this.drawTrueWind = function() {
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var _twd = toRadians(twd); 
    context.beginPath();
    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;
    
    var windLength = tws * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dX = windLength * Math.sin(_twd);
    var dY = - windLength * Math.cos(_twd);
    // create a new line object
    var line = new Line(x + dX, y + dY, x, y);
    // draw the line
    context.strokeStyle = "black";
    context.fillStyle   = "black";
    context.lineWidth = 5;
    line.drawWithAnemoArrowheads(context);
    context.closePath();
    if (document.getElementById("display.labels").checked)
    {
      context.font= "bold 12px Arial";
      context.fillStyle = "black";
      context.fillText("TWS:" + tws.toFixed(2) + " kts, TWA:" + twa + "" /* "�" */, x + dX, y + dY);
    }
  };
  
  this.drawAppWind = function() {
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var wd = hdg + awa; // Direction the wind is blowing TO
    while (wd > 360)
      wd -= 360;
    var _awd = toRadians(wd); 
    context.beginPath();
    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;
    
    var windLength = aws * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dX = windLength * Math.sin(_awd);
    var dY = - windLength * Math.cos(_awd);
    // create a new line object
    var line = new Line(x + dX, y + dY, x, y);
    // draw the line
    context.strokeStyle = "blue";
    context.fillStyle   = "blue";
    context.lineWidth = 5;
    line.drawWithAnemoArrowheads(context);
    context.closePath();
    if (document.getElementById("display.labels").checked) {
      context.font= "bold 12px Arial";
      context.fillStyle = "blue";
      context.fillText("AWS:" + aws + " kts, AWA:" + awa + "" /* "�" */, x + dX, y + dY);
    }
  };
  
  this.drawBSP = function() {
    if (bsp === 0) return;

    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var _hdg = toRadians(hdg); 
    context.beginPath();
    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;
    
    var bspLength = bsp * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dX = bspLength * Math.sin(_hdg);
    var dY = - bspLength * Math.cos(_hdg);
    // create a new line object
    var line = new Line(x, y, x + dX, y + dY);
    // draw the line
    context.strokeStyle = "red";
    context.lineWidth = 3;
    line.drawHollowArrow(context);
    context.closePath();
//    var metrics = context.measureText(valueToDisplay);
//    len = metrics.width;
    if (document.getElementById("display.labels").checked) {
      context.font= "bold 12px Arial";
      context.fillStyle = "red";
      context.fillText("BSP:" + bsp.toFixed(2) + " kts, HDG:" + hdg.toFixed(0) + "" /* "�" */, x + dX, y + dY);
    }
  };
  
  this.drawCMG = function() {
    if (bsp === 0 || leeway === 0) return;

    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var _hdg = toRadians(cmg); 
    context.beginPath();
    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;
    
    var bspLength = bsp * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dX = bspLength * Math.sin(_hdg);
    var dY = - bspLength * Math.cos(_hdg);
    // create a new line object
    var line = new Line(x, y, x + dX, y + dY);
    // draw the line
    context.strokeStyle = "cyan";
    context.fillStyle   = "cyan";
    context.lineWidth = 5;
    line.drawWithArrowhead(context);
    context.closePath();
    if (document.getElementById("display.labels").checked) {
      context.font= "bold 12px Arial";
      context.fillStyle = "cyan";
      context.fillText("CMG:" + cmg.toFixed(0) + "" /* "�" */, x + dX, y + dY);
    }
  };

  this.drawSOG = function() {
    if (sog === 0) return;

    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var _hdg = toRadians(cog); 
    context.beginPath();
    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;
    
    var bspLength = sog * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dX = bspLength * Math.sin(_hdg);
    var dY = - bspLength * Math.cos(_hdg);
    // create a new line object
    var line = new Line(x, y, x + dX, y + dY);
    // draw the line
    context.strokeStyle = "pink";
    context.fillStyle   = "pink";
    context.lineWidth = 5;
    line.drawWithArrowhead(context);
    context.closePath();
    if (document.getElementById("display.labels").checked) {
      context.font= "bold 12px Arial";
      context.fillStyle = "pink";
      context.fillText("SOG:" + sog + " kts, COG:" + cog + "" /* "�" */, x + dX, y + dY);
      context.strokeStyle = "black";
      context.lineWidth = 1;
//    context.strokeText("SOG:" + sog.toFixed(2) + " kts, COG:" + cog + "" /* "�" */, x + dX, y + dY);
    }
  };
  
  this.drawVMG = function() {
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;
    
    var _hdg = 0;
    context.beginPath();
    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;
    
    var wpName = document.getElementById("display.vmg.waypoint").value;
    if (document.getElementById("display.vmg.wind").checked) {
        _hdg = toRadians(twd);
    } else {
      _hdg = toRadians(b2wp);       
      // Display WP direction
      context.strokeStyle = "orange";
      context.fillStyle   = "orange";
      context.lineWidth = 1;
      var len = 0.75 * Math.min(cHeight, cWidth) / 2;
      var _dX = len * Math.sin(_hdg);
      var _dY = - len * Math.cos(_hdg);
      var wpLine = new Line(x, y, x + _dX, y + _dY);
      wpLine.drawWithArrowhead(context);
      context.fillText(wpName, x + _dX, y + _dY);
    }

    var bspLength = vmg * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dX = bspLength * Math.sin(_hdg);
    var dY = - bspLength * Math.cos(_hdg);
    // create a new line object
    var line = new Line(x, y, x + dX, y + dY);
    // draw the line
    context.strokeStyle = "yellow";
    context.fillStyle   = "yellow";
    context.lineWidth = 5;
    line.drawWithArrowhead(context);
    context.closePath();
    if (false && document.getElementById("display.labels").checked) {
      context.font= "bold 12px Arial";
      context.fillStyle = "yellow";
      context.fillText("VMG:" + vmg.toFixed(2) + " kts", x + dX, y + dY);
      context.strokeStyle = "black";
      context.lineWidth = 1;
//    context.strokeText("SOG:" + sog.toFixed(2) + " kts, COG:" + cog + "" /* "�" */, x + dX, y + dY);
    }
    if (context.setLineDash !== undefined) {
      context.setLineDash([5]);
      context.moveTo(x + dX, y + dY);
      var _cog = toRadians(cog); 
      var sogLength = sog * ((Math.min(cHeight, cWidth) / 2) / speedScale);
      dX = sogLength * Math.sin(_cog);
      dY = - sogLength * Math.cos(_cog);
      context.lineTo(x + dX, y + dY);
      context.lineWidth = 1;
      context.stroke();
      // Reset
      context.setLineDash([]);
    }
  };
  
  this.drawCurrent = function() {
    if (csp === 0) return;
    
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;

    var _cmg = toRadians(cmg); 
    var bspLength = bsp * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dXcmg = bspLength * Math.sin(_cmg);
    var dYcmg = - bspLength * Math.cos(_cmg);

    var _cog = toRadians(cog); 
    var sogLength = sog * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dXcog = sogLength * Math.sin(_cog);
    var dYcog = - sogLength * Math.cos(_cog);

    context.beginPath();
    // create a new line object
    var line = new Line(x + dXcmg, y + dYcmg, x + dXcog, y + dYcog);
    // draw the line
    context.strokeStyle = "LightGreen";
    context.fillStyle   = "LightGreen";
    context.lineWidth = 5;
    line.drawWithArrowhead(context);
    context.closePath();
    if (document.getElementById("display.labels").checked) {
      context.font= "bold 12px Arial";
      context.fillStyle = "LightGreen";
      context.fillText("CSP:" + csp.toFixed(2) + " kts, CDR:" + cdr.toFixed(0) + "" /* "�" */, x + dXcog, y + dYcog + 14); // + 14 not to overlap the SOG/COG
    }
  };
  
  this.drawVW = function() {
    if (sog === 0) return;

    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var center = getCanvasCenter();
    var x = center.x;
    var y = center.y;

    var wd = hdg + awa; // Direction the wind is blowing TO
    while (wd > 360)
      wd -= 360;
    var _awd = toRadians(wd); 
    context.beginPath();
    var awLength = aws * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dXaw = awLength * Math.sin(_awd);
    var dYaw = - awLength * Math.cos(_awd);

    var _twd = toRadians(twd); 
    var twLength = tws * ((Math.min(cHeight, cWidth) / 2) / speedScale);
    var dXtw = twLength * Math.sin(_twd);
    var dYtw = - twLength * Math.cos(_twd);

    context.beginPath();
    // create a new line object
    var line = new Line(x + dXaw, y + dYaw, x + dXtw, y + dYtw);
    // draw the line
    context.strokeStyle = "pink";
    context.fillStyle   = "pink";
    context.lineWidth = 5;
    line.drawWithAnemoArrowheads(context);
    context.closePath();
  };
  
  var WL_RATIO_COEFF = 0.75; // Ratio to apply to (3.5 * Width / Length)
  var BOAT_LENGTH = 50;
  this.drawBoat = function(trueHeading) {
    cWidth  = document.getElementById(cName).width;
    cHeight = document.getElementById(cName).height;

    var x = new Array();
    x.push(WL_RATIO_COEFF * 0); 
    x.push(WL_RATIO_COEFF * BOAT_LENGTH / 7);
    x.push(WL_RATIO_COEFF * (2 * BOAT_LENGTH) / 7);
    x.push(WL_RATIO_COEFF * (2 * BOAT_LENGTH) / 7);
    x.push(WL_RATIO_COEFF * (1.5 * BOAT_LENGTH) / 7); 
    x.push(WL_RATIO_COEFF * -(1.5 * BOAT_LENGTH) / 7);
    x.push(WL_RATIO_COEFF * -(2 * BOAT_LENGTH) / 7);
    x.push( WL_RATIO_COEFF * -(2 * BOAT_LENGTH) / 7); 
    x.push(WL_RATIO_COEFF * -BOAT_LENGTH / 7);
    var y = new Array();// Half, length
    y.push(-(4 * BOAT_LENGTH) / 7);
    y.push(-(3 * BOAT_LENGTH) / 7);
    y.push(-(BOAT_LENGTH) / 7);
    y.push(BOAT_LENGTH / 7);
    y.push((3 * BOAT_LENGTH) / 7);
    y.push((3 * BOAT_LENGTH) / 7);
    y.push(BOAT_LENGTH / 7);
    y.push(-(BOAT_LENGTH) / 7);
    y.push(-(3 * BOAT_LENGTH) / 7);
    
    var xpoints = new Array();
    var ypoints = new Array();

    // Rotation matrix:
    // | cos(alpha)  -sin(alpha) |
    // | sin(alpha)   cos(alpha) |
    
    var center = getCanvasCenter();
    var ptX = center.x;
    var ptY = center.y;

    for (var i=0; i<x.length; i++) {
      var dx = x[i] * Math.cos(toRadians(trueHeading)) + (y[i] * (-Math.sin(toRadians(trueHeading))));
      var dy = x[i] * Math.sin(toRadians(trueHeading)) + (y[i] * Math.cos(toRadians(trueHeading)));
      xpoints.push(Math.round(ptX + dx));
      ypoints.push(Math.round(ptY + dy));
    }
    context.fillStyle = 'gray';
    context.beginPath();
    context.moveTo(xpoints[0], ypoints[0]);
    for (var i=1; i<xpoints.length; i++) {
      context.lineTo(xpoints[i], ypoints[i]);
    }
    context.closePath();
    context.fill();
    context.strokeStyle = 'blue';
    context.lineWidth = 2;
    context.stroke();
  };

  var toRadians = function(deg) {
    return deg * (Math.PI / 180);
  };

  function toDegrees(rad) {
    return rad * (180 / Math.PI);
  };
  
  (function() {
     canvas = document.getElementById(cName);
     instance.drawGraph();
   })(); // Invoked automatically when new is invoked.  
};

function Point(_x, _y) {
  this.x = _x;
  this.y = _y;
};
