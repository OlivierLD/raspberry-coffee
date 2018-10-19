/*
 * @author Olivier Le Diouris
 */
function NumericDisplay(cName,     // Canvas Name
                        dSize,     // height         
                        nbDigits) {
	if (dSize === undefined) {
		dSize = 20;
	}
	if (nbDigits === undefined) {
		nbDigits = 1;
	}

/*
 * See custom properties in CSS.
 * =============================
 * @see https://developer.mozilla.org/en-US/docs/Web/CSS/
 * Relies on a rule named .numericdisplay, like that:
 *
 .numericdisplay {
		--bg-color: white;
		--with-gradient: true;
		--display-background-gradient-from: LightGrey;
		--display-background-gradient-to: white;
		--value-color: grey;
		--value-outline-color: black;
		--font: Arial;
	}
 */

	/**
	 * Recurse from the top down, on styleSheets and cssRules
	 *
	 * document.styleSheets[0].cssRules[2].selectorText returns ".numericdisplay"
	 * document.styleSheets[0].cssRules[2].cssText returns ".numericdisplay { --hand-color: red;  --face-color: white; }"
	 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
	 *
	 * spine-case to camelCase
	 */
	var getColorConfig = function () {
		var colorConfig = numericDisplayColorConfig;
		for (var s = 0; s < document.styleSheets.length; s++) {
//		console.log("Walking though ", document.styleSheets[s]);
			for (var r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
//			console.log(">>> ", document.styleSheets[s].cssRules[r].selectorText);
				if (document.styleSheets[s].cssRules[r].selectorText === '.numericdisplay') {
//				console.log("  >>> Found it!");
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
								case '--with-gradient':
									colorConfig.withGradient = (value === 'true');
									break;
								case '--display-background-gradient-from':
									colorConfig.displayBackgroundGradientFrom = value;
									break;
								case '--display-background-gradient-to':
									colorConfig.displayBackgroundGradientTo = value;
									break;
								case '--value-color':
									colorConfig.valueColor = value;
									break;
								case '--value-outline-color':
									colorConfig.valueOutlineColor = value;
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

	var defaultNumericDisplayColorConfig = {
		bgColor: 'white',
		withGradient: true,
		displayBackgroundGradientFrom: 'LightGrey',
		displayBackgroundGradientTo: 'white',
		valueColor: 'grey',
		valueOutlineColor: 'black',
		font: 'Arial'
	};
	var numericDisplayColorConfig = defaultNumericDisplayColorConfig; //

	if (events !== undefined) {
		events.subscribe('color-scheme-changed', function (val) {
//    console.log('Color scheme changed:', val);
			reloadColorConfig();
		});
	}
	numericDisplayColorConfig = getColorConfig();

	var scale = dSize / 100;
	var width = dSize;

	var canvasName = cName;
	var displaySize = dSize;

	var valueToDisplay = 0;

	var instance = this;

//try { console.log('in the AnalogDisplay constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

	(function () {
		drawDisplay(canvasName, displaySize, valueToDisplay);
	})(); // Invoked automatically

	var reloadColor = false;
	var reloadColorConfig = function() {
//  console.log('Color scheme has changed');
		reloadColor = true;
	};

	function drawDisplay(displayCanvasName, displayRadius, displayValue) {
		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			numericDisplayColorConfig = getColorConfig();
			console.log("Changed theme:", numericDisplayColorConfig);
		}
		reloadColor = false;

		var canvas = document.getElementById(displayCanvasName);
		var context = canvas.getContext('2d');

		var radius = displayRadius;

		if (numericDisplayColorConfig.withGradient) {
			var grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, numericDisplayColorConfig.displayBackgroundGradientFrom); // 0  Beginning
			grd.addColorStop(1, numericDisplayColorConfig.displayBackgroundGradientTo);   // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = numericDisplayColorConfig.displayBackgroundGradientTo;
		}

		// The rectangles around each digit
		var oneDigitWidth = canvas.width / nbDigits;
		for (var i = 0; i < nbDigits; i++) {
			context.beginPath();
			var x = i * oneDigitWidth;
			context.fillRect(x, 0, x + oneDigitWidth, canvas.height);
			context.lineWidth = 1;
			context.strokeStyle = 'black';
			context.rect(x, 0, x + oneDigitWidth, canvas.height);
			context.stroke();
			context.closePath();
		}

		// Value
		if (true) {
			text = displayValue.toFixed(0);
			while (text.length < nbDigits) {
				text = '0' + text;
			}
			for (var i = 0; i < nbDigits; i++) {
				len = 0;
				context.font = "bold " + Math.round(scale * 40) + "px Arial"; // "bold 40px Arial"
				var txt = text.substring(i, i + 1);
				var metrics = context.measureText(txt);
				len = metrics.width;
				var x = i * oneDigitWidth;
				context.beginPath();
				context.fillStyle = numericDisplayColorConfig.valueColor;
				context.fillText(txt, x + (oneDigitWidth / 2) - (len / 2), canvas.height - 10);
				context.lineWidth = 1;
				context.strokeStyle = numericDisplayColorConfig.valueOutlineColor;
				context.strokeText(txt, x + (oneDigitWidth / 2) - (len / 2), canvas.height - 10); // Outlined
				context.closePath();
			}
		}
	};

	this.setValue = function (val) {
		drawDisplay(canvasName, displaySize, val);
	};
}