/*
 * @author Olivier Le Diouris
 */
var thermometerColorConfigWhite =
    {
        bgColor: 'white',
        digitColor: 'red',
        withGradient: true,
        displayBackgroundGradient: {from: 'black', to: 'LightGrey'},
        withDisplayShadow: true,
        shadowColor: 'rgba(0, 0, 0, 0.75)',
        majorTickColor: 'DarkGrey',
        minorTickColor: 'DarkGrey',
        valueColor: 'LightRed',
        valueOutlineColor: 'black',
        valueNbDecimal: 2,
        font: 'Arial' /* 'Source Code Pro' */
    };

var thermometerColorConfigBlack =
    {
        bgColor: 'black',
        digitColor: 'red',
        withGradient: true,
        displayBackgroundGradient: {from: 'black', to: 'LightGrey'},
        withDisplayShadow: true,
        shadowColor: 'rgba(0, 0, 0, 0.75)',
        majorTickColor: 'DarkGrey',
        minorTickColor: 'DarkGrey',
        valueColor: 'LightRed',
        valueOutlineColor: 'black',
        valueNbDecimal: 2,
        font: 'Arial' /* 'Source Code Pro' */
    };

var thermometerColorConfig = thermometerColorConfigWhite;

function Thermometer(cName, dSize, minValue, maxValue, majorTicks, minorTicks) {
    if (minValue === undefined)
        minValue = -20;
    if (maxValue === undefined)
        maxValue = 50;
    if (majorTicks === undefined)
        majorTicks = 10;
    if (minorTicks === undefined)
        minorTicks = 1;

    var canvasName = cName;
    var displaySize = dSize;

    var running = false;
    var previousValue = 0.0;
    var intervalID;
    var valueToDisplay = 0;

    var instance = this;

//try { console.log('in the Thermometer constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

    (function () {
        drawDisplay(canvasName, displaySize, previousValue);
    })(); // Invoked automatically

    this.repaint = function() {
        drawDisplay(canvasName, displaySize, previousValue);
    };

    this.setDisplaySize = function (ds) {
        displaySize = ds;
        drawDisplay(canvasName, displaySize, previousValue);
    };

    this.startStop = function (buttonName) {
//  console.log('StartStop requested on ' + buttonName);
        var button = document.getElementById(buttonName);
        running = !running;
        button.value = (running ? "Stop" : "Start");
        if (running)
            this.animate();
        else {
            window.clearInterval(intervalID);
            previousValue = valueToDisplay;
        }
    };

    this.animate = function () {
        var value;
        if (arguments.length === 1)
            value = arguments[0];
        else {
//    console.log("Generating random value");
            value = minValue + ((maxValue - minValue) * Math.random());
        }
//  console.log("Reaching Value :" + value + " from " + previousValue);
        diff = value - previousValue;
        valueToDisplay = previousValue;

//  console.log(canvasName + " going from " + previousValue + " to " + value);

        if (diff > 0)
            incr = 0.01 * maxValue;
        else
            incr = -0.01 * maxValue;
        intervalID = window.setInterval(function () {
            displayAndIncrement(incr, value);
        }, 50);
    };

    var displayAndIncrement = function (inc, finalValue) {
        //console.log('Tic ' + inc + ', ' + finalValue);
        drawDisplay(canvasName, displaySize, valueToDisplay);
        valueToDisplay += inc;
        if ((inc > 0 && valueToDisplay > finalValue) || (inc < 0 && valueToDisplay < finalValue)) {
            //  console.log('Stop!')
            window.clearInterval(intervalID);
            previousValue = finalValue;
            if (running)
                instance.animate();
            else
                drawDisplay(canvasName, displaySize, finalValue);
        }
    };

    function getStyleRuleValue(style, selector, sheet) {
        var sheets = typeof sheet !== 'undefined' ? [sheet] : document.styleSheets;
        for (var i = 0, l = sheets.length; i < l; i++) {
            var sheet = sheets[i];
            if (!sheet.cssRules) {
                continue;
            }
            for (var j = 0, k = sheet.cssRules.length; j < k; j++) {
                var rule = sheet.cssRules[j];
                if (rule.selectorText && rule.selectorText.split(',').indexOf(selector) !== -1) {
                    return rule.style[style];
                }
            }
        }
        return null;
    };

    function drawDisplay(displayCanvasName, displayRadius, displayValue) {
        var schemeColor = getStyleRuleValue('color', '.display-scheme');
//  console.log(">>> DEBUG >>> color:" + schemeColor);
        if (schemeColor === 'black')
            thermometerColorConfig = thermometerColorConfigBlack;
        else if (schemeColor === 'white')
            thermometerColorConfig = thermometerColorConfigWhite;

        var digitColor = thermometerColorConfig.digitColor;

        var canvas = document.getElementById(displayCanvasName);
        var context = canvas.getContext('2d');

        var radius = 10; // The ball at the bottom. The tube is (radius / 2) wide.

        // Cleanup
        //context.fillStyle = "#ffffff";
        context.fillStyle = thermometerColorConfig.bgColor;
        //context.fillStyle = "transparent";
        context.fillRect(0, 0, canvas.width, canvas.height);
        //context.fillStyle = 'rgba(255, 255, 255, 0.0)';
        //context.fillRect(0, 0, canvas.width, canvas.height);

        // Bottom of the tube at (canvas.height - 10 - radius)
        var bottomTube = (canvas.height - 10 - radius);
        var topTube = 40;// Top of the tube at y = 20

        var tubeLength = bottomTube - topTube;

        // Major Ticks
        context.beginPath();
        for (i = 0; i <= (maxValue - minValue); i += majorTicks) {
            xFrom = (canvas.width / 2) - 20;
            yFrom = bottomTube - ((tubeLength) * (i / (maxValue - minValue)));
            xTo = (canvas.width / 2) + 20;
            yTo = yFrom;
            context.moveTo(xFrom, yFrom);
            context.lineTo(xTo, yTo);
        }
        context.lineWidth = 1;
        context.strokeStyle = thermometerColorConfig.majorTickColor;
        context.stroke();
        context.closePath();

        // Minor Ticks
        if (minorTicks > 0) {
            context.beginPath();
            for (i = 0; i <= (maxValue - minValue); i += minorTicks) {
                xFrom = (canvas.width / 2) - 15;
                yFrom = bottomTube - ((tubeLength) * (i / (maxValue - minValue)));
                xTo = (canvas.width / 2) + 15;
                yTo = yFrom;
                context.moveTo(xFrom, yFrom);
                context.lineTo(xTo, yTo);
            }
            context.lineWidth = 1;
            context.strokeStyle = thermometerColorConfig.minorTickColor;
            context.stroke();
            context.closePath();
        }

        // Tube
        context.beginPath();
        //context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
        context.arc(canvas.width / 2, canvas.height - 10 - radius, radius, 5 * Math.PI / 4, 7 * Math.PI / 4, true);
        context.lineTo((canvas.width / 2) + (radius * Math.cos(Math.PI / 4)), topTube); // right side of the tube
        context.arc(canvas.width / 2, topTube, (radius / 2), 0, Math.PI, true);
        context.lineWidth = 1;

        if (thermometerColorConfig.withGradient) {
            var grd = context.createLinearGradient(0, 5, 0, radius);
            grd.addColorStop(0, thermometerColorConfig.displayBackgroundGradient.from);// 0  Beginning
            grd.addColorStop(1, thermometerColorConfig.displayBackgroundGradient.to);// 1  End
            context.fillStyle = grd;
        }
        if (thermometerColorConfig.withDisplayShadow) {
            context.shadowBlur = 0;
            context.shadowColor = thermometerColorConfig.shadowColor; // 'black';
        }
        context.lineJoin = "round";
        context.fill();
        context.strokeStyle = 'DarkGrey';
        context.stroke();
        context.closePath();

        // Numbers
        context.beginPath();
        for (i = minValue; i <= maxValue; i += majorTicks) {
            xTo = (canvas.width / 2) + 20;
            yTo = bottomTube - ((tubeLength) * ((i - minValue) / (maxValue - minValue)));
            ;
            context.font = "bold 10px Arial";
            context.fillStyle = digitColor;
            str = i.toString();
            len = context.measureText(str).width;
            context.fillText(str, xTo, yTo + 3); // 5: half font size
        }
        context.closePath();

        // Value
        text = displayValue.toFixed(thermometerColorConfig.valueNbDecimal);
        len = 0;
        context.font = "bold 20px Arial";
        var metrics = context.measureText(text);
        len = metrics.width;

        context.beginPath();
        context.fillStyle = thermometerColorConfig.valueColor;
        context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
        context.lineWidth = 1;
        context.strokeStyle = thermometerColorConfig.valueOutlineColor;
        context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
        context.closePath();

        // Liquid in the tube
        context.beginPath();
        //context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
        context.arc(canvas.width / 2, canvas.height - 10 - (radius * 0.75), (radius * 0.75), 5 * Math.PI / 4, 7 * Math.PI / 4, true);
        var y = bottomTube - ((tubeLength) * ((displayValue - minValue) / (maxValue - minValue)));

        context.lineTo((canvas.width / 2) + ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // right side of the tube
        context.lineTo((canvas.width / 2) - ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // top of the liquid

        context.lineWidth = 1;

        var _grd = context.createLinearGradient(0, topTube, 0, tubeLength);
        _grd.addColorStop(0, 'red');    // 0  Beginning
        _grd.addColorStop(0.6, 'red');
        _grd.addColorStop(0.8, 'blue');
        _grd.addColorStop(1, 'navy');   // 1  End
        context.fillStyle = _grd;

        context.shadowBlur = 20;
        context.shadowColor = 'black';

        context.lineJoin = "round";
        context.fill();
        context.strokeStyle = 'DarkGrey';
        context.stroke();
        context.closePath();
    };

    this.setValue = function (val) {
        drawDisplay(canvasName, displaySize, val);
    };
}