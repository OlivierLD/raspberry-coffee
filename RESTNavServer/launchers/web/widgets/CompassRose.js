/*
 * @author Olivier Le Diouris
 */

function CompassRose(cName,                     // Canvas Name
                     title,
                     width,                     // Display width
                     height,                    // Display height
                     value, 
                     textColor) {

	/*
 * See custom properties in CSS.
 * =============================
 * @see https://developer.mozilla.org/en-US/docs/Web/CSS/
 * Relies on a rule named .graphdisplay, like that:
 *
 .analogdisplay {
		--bg-color: rgba(0, 0, 0, 0);
		--digit-color: black;
		--with-gradient: true;
		--display-background-gradient-from: LightGrey;
		--display-background-gradient-to: white;
		--with-display-shadow: false;
		--shadow-color: rgba(0, 0, 0, 0.75);
		--outline-color: DarkGrey;
		--major-tick-color: black;
		--minor-tick-color: black;
		--value-color: grey;
		--value-outline-color: black;
		--value-nb-decimal: 1;
		--hand-color: red;
		--hand-outline-color: black;
		--with-hand-shadow: true;
		--knob-color: DarkGrey;
		--knob-outline-color: black;
		--font: Arial;
	}
 */

	/**
	 * Recurse from the top down, on styleSheets and cssRules
	 *
	 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
	 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
	 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
	 *
	 * spine-case to camelCase
	 */
	var getColorConfig = function() {
		var colorConfig = defaultRoseColorConfig;
		for (var s=0; s<document.styleSheets.length; s++) {
			console.log("Walking though ", document.styleSheets[s]);
			for (var r=0; document.styleSheets[s].cssRules !== null && r<document.styleSheets[s].cssRules.length; r++) {
				console.log(">>> ", document.styleSheets[s].cssRules[r].selectorText);
				if (document.styleSheets[s].cssRules[r].selectorText === '.analogdisplay') {
					console.log("  >>> Found it!");
					var cssText = document.styleSheets[s].cssRules[r].style.cssText;
					var cssTextElems = cssText.split(";");
					cssTextElems.forEach(function(elem) {
						if (elem.trim().length > 0) {
							var keyValPair = elem.split(":");
							var key = keyValPair[0].trim();
							var value = keyValPair[1].trim();
							switch (key) {
								case '--bg-color':
									colorConfig.bgColor = value;
									break;
								case '--digit-color':
									colorConfig.digitColor = value;
									break;
								case '--with-gradient':
									colorConfig.withGradient = (value === 'true');
									break;
								case '--display-background-gradient-from':
									colorConfig.displayBackgroundGradientFrom = value;
									break;
								case '--display-background-gradient-to':
									colorConfig.displayBackgroundGradientTo = value;
									break;
								case '--with-display-shadow':
									colorConfig.withDisplayShadow = (value === 'true');
									break;
								case '--shadow-color':
									colorConfig.shadowColor = value;
									break;
								case '--outline-color':
									colorConfig.outlineColor = value;
									break;
								case '--major-tick-color':
									colorConfig.majorTickColor = value;
									break;
								case '--minor-tick-color':
									colorConfig.minorTickColor = value;
									break;
								case '--value-color':
									colorConfig.valueColor = value;
									break;
								case '--index-color':
									colorConfig.indexColor = value;
									break;
								case '--value-outline-color':
									colorConfig.valueOutlineColor = value;
									break;
								case '--value-nb-decimal':
									colorConfig.valueNbDecimal = value;
									break;
								case '--hand-color':
									colorConfig.handColor = value;
									break;
								case '--hand-outline-color':
									colorConfig.handOutlineColor = value;
									break;
								case '--with-hand-shadow':
									colorConfig.withHandShadow = (value === 'true');
									break;
								case '--knob-color':
									colorConfig.knobColor = value;
									break;
								case '--knob-outline-color':
									colorConfig.knobOutlineColor = value;
									break;
								case '--font':
									colorConfig.font = value;
									break;
								default:
									break;
							}
						}
					});
				}
			}
		}
		return colorConfig;
	};

	var defaultRoseColorConfig = {
		bgColor:           'white',
		digitColor:        '#404040',
		withGradient:      true,
		displayBackgroundGradientFrom: 'gray',
		displayBackgroundGradientTo: 'white',
		tickColor:         'darkGray',
		valueColor:        'blue',
		indexColor:        'red',
		font:              'Arial'
	};

	var roseColorConfig = defaultRoseColorConfig; // analogDisplayColorConfigBlack; // White is the default

	if (events !== undefined) {
		events.subscribe('color-scheme-changed', function(val) {
//    console.log('Color scheme changed:', val);
			reloadColorConfig();
		});
	}
	roseColorConfig = getColorConfig();

  // base = w 200 h 50
  var scale = 1;   

  var canvasName = cName;
  
  var displayWidth  = width;
  var displayHeight = height;

  var valueToDisplay = 0;
  var totalViewAngle = 60; // must be even...
  
  if (value !== undefined)
    valueToDisplay = value;
  if (textColor === undefined)
    textColor = roseColorConfig.digitColor;
  
  var instance = this;
  
  (function(){ drawDisplay(canvasName, displayWidth, displayHeight); })(); // Invoked automatically
  
  this.repaint = function() {
    drawDisplay(canvasName, displayWidth, displayHeight);
  };

  this.setValue = function(val) {
    valueToDisplay = val;
    drawDisplay(canvasName, displayWidth, displayHeight);
  };
  
  this.setDisplaySize = function(dw, dh) {
 // scale = ds / 100;
    displayWidth  = dw;
    displayHeight = dh;
    drawDisplay(canvasName, displayWidth, displayHeight);
  };
  
	var reloadColor = false;
	var reloadColorConfig = function() {
//  console.log('Color scheme has changed');
		reloadColor = true;
	};

	function drawDisplay(displayCanvasName, displayW, displayH) {
		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			roseColorConfig = getColorConfig();
			console.log("Changed theme:", roseColorConfig);
		}
		reloadColor = false;

    if (displayW !== undefined && displayH !== undefined) {
      scale = Math.min(displayW / 200, displayH / 50);
    }
    var canvas = document.getElementById(displayCanvasName);
    var context = canvas.getContext('2d');

    if (roseColorConfig.withGradient === true) {
	    var grd = context.createLinearGradient(0, 5, 0, document.getElementById(cName).height);
	    grd.addColorStop(0, roseColorConfig.displayBackgroundGradientFrom); // 0  Beginning
	    grd.addColorStop(1, roseColorConfig.displayBackgroundGradientTo);   // 1  End
	    context.fillStyle = grd;
    } else {
	    context.fillStyle = roseColorConfig.displayBackgroundGradientTo;
    }
  
    // Background
    roundRect(context, 0, 0, canvas.width, canvas.height, 10, true, false);    
    // Ticks
    context.strokeStyle = roseColorConfig.tickColor;
    context.lineWidth   = 0.5;
    
    var startValue = valueToDisplay - (totalViewAngle / 2);
    var endValue   = valueToDisplay + (totalViewAngle / 2);
    for (var tick=startValue; tick<=endValue; tick++) {
      var tickHeight = canvas.height / 4;
      if (tick % 5 === 0)
        tickHeight = canvas.height / 2;
      var x = (tick - startValue) * (canvas.width / totalViewAngle);
      context.strokeStyle = roseColorConfig.tickColor; // 'rgba(255, 255, 255, 0.7)';
      context.beginPath();
      context.moveTo(x, 0);
      context.lineTo(x, tickHeight);
      context.closePath();
      context.stroke();    
      if (tick % 15 === 0) {
        var tk = tick;
        while (tk < 0) tk += 360;
        var txt = tk.toString();
        if (tick % 45 === 0) {
          if (tick === 0)   txt = "N";
          if (tick === 45)  txt = "NE";
          if (tick === 90)  txt = "E";
          if (tick === 135) txt = "SE";
          if (tick === 180) txt = "S";
          if (tick === 225) txt = "SW";
          if (tick === 270) txt = "W";
          if (tick === 315) txt = "NW";
          if (tick === 360) txt = "N";
        }
        context.font = "bold " + Math.round(scale * 20) + "px " + roseColorConfig.font; // "bold 16px Arial"
        var metrics = context.measureText(txt);
        len = metrics.width;    
        context.fillStyle = roseColorConfig.digitColor;
        context.fillText(txt, x - (len / 2), canvas.height - 10);
      }
    }

    context.fillStyle = roseColorConfig.valueColor;
    // Value, top left corner
    context.font = "bold " + Math.round(scale * 16) + "px Courier New"; // "bold 16px Arial"
    context.fillText(valueToDisplay.toString() + "\272", 5, 14);
    
    context.strokeStyle = roseColorConfig.indexColor; // The index
    context.beginPath();
    context.moveTo(canvas.width / 2, 0);
    context.lineTo(canvas.width / 2, canvas.height);
    context.closePath();
    context.stroke();    
  };

  function roundRect(ctx, x, y, width, height, radius, fill, stroke)  {
    if (fill === undefined)  {
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
  };
};