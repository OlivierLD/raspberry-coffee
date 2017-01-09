/*
 * @author Olivier Le Diouris
 */
var awDisplayColorConfigWhite =
    {
        bgColor: 'white',
        digitColor: 'black',
        withGradient: true,
        displayBackgroundGradient: {from: 'LightGrey', to: 'white'},
        withDisplayShadow: true,
        shadowColor: 'rgba(0, 0, 0, 0.75)',
        outlineColor: 'DarkGrey',
        majorTickColor: 'black',
        minorTickColor: 'black',
        valueColor: 'grey',
        valueOutlineColor: 'black',
        valueNbDecimal: 1,
        handColor: 'red', // 'rgba(0, 0, 100, 0.25)',
        handOutlineColor: 'black',
        withHandShadow: true,
        knobColor: 'DarkGrey',
        knobOutlineColor: 'black',
        font: 'Arial' /* 'Source Code Pro' */
    };

var awDisplayColorConfigBlack =
    {
        bgColor: 'black',
        digitColor: 'white',
        withGradient: true,
        displayBackgroundGradient: {from: 'DarkGrey', to: 'black'},
        shadowColor: 'black',
        outlineColor: 'DarkGrey',
        majorTickColor: 'red',
        minorTickColor: 'red',
        valueColor: 'red',
        valueOutlineColor: 'black',
        valueNbDecimal: 1,
        handColor: 'rgba(255, 0, 0, 0.4)', // 'rgba(0, 0, 100, 0.25)',
        handOutlineColor: 'red', // 'blue',
        withHandShadow: true,
        knobColor: '#8ED6FF', // Kind of blue
        knobOutlineColor: 'blue',
        font: 'Arial'
    };
var awDisplayColorConfig = awDisplayColorConfigWhite; // 

function AWDisplay(cName, dSize, majorTicks, minorTicks, withDigits) {
    if (majorTicks === undefined)
        majorTicks = 45;
    if (minorTicks === undefined)
        minorTicks = 0;
    if (withDigits === undefined)
        withDigits = false;

    var canvasName = cName;
    var displaySize = dSize;

    var scale = dSize / 100;

    var running = false;
    var previousValue = 0.0;
    var intervalID;
    var angleToDisplay = 0;
    var aws = 0;
    var incr = 1;
    var withBorder = true;

    var instance = this;

//try { console.log('in the AWDisplay constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

    (function () {
        drawDisplay(canvasName, displaySize, previousValue);
    })(); // Invoked automatically

    this.setDisplaySize = function (ds) {
        scale = ds / 100;
        displaySize = ds;
        drawDisplay(canvasName, displaySize, previousValue);
    };

    this.setBorder = function (b) {
        withBorder = b;
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
            previousValue = angleToDisplay;
        }
    };

    var on360 = function (angle) {
        var num = angle;
        while (num < 0)
            num += 360;
        return num;
    };

    this.setAWS = function (ws) {
        aws = ws;
    };

    this.animate = function () {
        var value;
        if (arguments.length === 1)
            value = arguments[0];
        else {
//    console.log("Generating random value");
            value = 360 * Math.random();
        }
//  console.log("Reaching Value :" + value + " from " + previousValue);
        diff = value - on360(previousValue);
        if (Math.abs(diff) > 180) // && sign(Math.cos(toRadians(value))))
        {
//    console.log("Diff > 180: new:" + value + ", prev:" + previousValue);
            if (value > on360(previousValue))
                value -= 360;
            else
                value += 360;
            diff = value - on360(previousValue);
        }
        angleToDisplay = on360(previousValue);

//  console.log(canvasName + " going from " + previousValue + " to " + value);

        incr = diff / 10;
//    if (diff < 0)
//      incr *= -1;
        if (intervalID)
            window.clearInterval(intervalID);
        intervalID = window.setInterval(function () {
            displayAndIncrement(value);
        }, 50);
    };

    function sign(x) {
        return x > 0 ? 1 : x < 0 ? -1 : 0;
    };
    function toRadians(d) {
        return Math.PI * d / 180;
    };

    function toDegrees(d) {
        return d * 180 / Math.PI;
    };

    var displayAndIncrement = function (finalValue) {
        //console.log('Tic ' + inc + ', ' + finalValue);
        drawDisplay(canvasName, displaySize, angleToDisplay);
        angleToDisplay += incr;
        if ((incr > 0 && angleToDisplay > finalValue) || (incr < 0 && angleToDisplay < finalValue)) {
            //  console.log('Stop!')
            window.clearInterval(intervalID);
            previousValue = finalValue;
            if (running)
                instance.animate();
            else
                drawDisplay(canvasName, displaySize, finalValue);
        }
    };

    this.setValue = function (val) {
        drawDisplay(canvasName, displaySize, val);
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
            awDisplayColorConfig = awDisplayColorConfigBlack;
        else if (schemeColor === 'white')
            awDisplayColorConfig = awDisplayColorConfigWhite;

        var digitColor = awDisplayColorConfig.digitColor;

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
        if (withBorder === true) {
            //  context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
            context.arc(canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
            context.lineWidth = 5;
        }
        if (analogDisplayColorConfig.withGradient) {
            var grd = context.createLinearGradient(0, 5, 0, radius);
            grd.addColorStop(0, awDisplayColorConfig.displayBackgroundGradient.from);// 0  Beginning
            grd.addColorStop(1, awDisplayColorConfig.displayBackgroundGradient.to);// 1  End
            context.fillStyle = grd;
        }
        else
            context.fillStyle = awDisplayColorConfig.displayBackgroundGradient.to;

        if (awDisplayColorConfig.withDisplayShadow) {
            context.shadowOffsetX = 3;
            context.shadowOffsetY = 3;
            context.shadowBlur = 3;
            context.shadowColor = awDisplayColorConfig.shadowColor;
        }

        context.lineJoin = "round";
        context.fill();
        context.strokeStyle = awDisplayColorConfig.outlineColor;
        context.stroke();
        context.closePath();

        // Major Ticks
        context.beginPath();
        for (i = 0; i < 360; i += majorTicks) {
            xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 360)));
            yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 360)));
            xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (i / 360)));
            yTo = (radius + 10) - ((radius * 0.85) * Math.sin(2 * Math.PI * (i / 360)));
            context.moveTo(xFrom, yFrom);
            context.lineTo(xTo, yTo);
        }
        context.lineWidth = 3;
        context.strokeStyle = awDisplayColorConfig.majorTickColor;
        context.stroke();
        context.closePath();

        // Minor Ticks
        if (minorTicks > 0) {
            context.beginPath();
            for (i = 0; i <= 360; i += minorTicks) {
                xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 360)));
                yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 360)));
                xTo = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (i / 360)));
                yTo = (radius + 10) - ((radius * 0.90) * Math.sin(2 * Math.PI * (i / 360)));
                context.moveTo(xFrom, yFrom);
                context.lineTo(xTo, yTo);
            }
            context.lineWidth = 1;
            context.strokeStyle = awDisplayColorConfig.minorTickColor;
            context.stroke();
            context.closePath();
        }

        // Numbers
        context.beginPath();
        for (i = 0; i < 360 && withDigits; i += majorTicks) {
            context.save();
            context.translate(canvas.width / 2, (radius + 10)); // canvas.height);
            context.rotate((2 * Math.PI * (i / 360)));
            context.font = "bold " + Math.round(scale * 15) + "px " + awDisplayColorConfig.font; // Like "bold 15px Arial"
            context.fillStyle = digitColor;
            str = i.toString();
            len = context.measureText(str).width;
            context.fillText(str, -len / 2, (-(radius * .8) + 10));
            context.restore();
        }
        context.closePath();

        // Arcs
        context.beginPath();
        x = canvas.width / 2;
        y = canvas.height / 2;
        context.lineWidth = 20;
        var top = 1.5 * Math.PI;
        var arcWidth = toRadians(120);

        // Starboard
        context.beginPath();
        context.strokeStyle = 'rgba(0, 255, 0, 0.25)';
        context.arc(x, y, radius * .75, 1.5 * Math.PI, top + arcWidth, false);
        context.stroke();
        context.closePath();

        // Port
        context.beginPath();
        context.strokeStyle = 'rgba(255, 0, 0, 0.25)';
        context.arc(x, y, radius * .75, 1.5 * Math.PI, top - arcWidth, true);
        context.stroke();
        context.closePath();

        // Value
//    var dv = displayValue;
//    while (dv > 360) dv -= 360;
//    while (dv < 0) dv += 360;
        text = aws.toFixed(1);
        len = 0;
        context.font = "bold " + Math.round(scale * 40) + "px " + awDisplayColorConfig.font; // "bold 40px Arial"
        var metrics = context.measureText(text);
        len = metrics.width;

        context.beginPath();
        context.fillStyle = awDisplayColorConfig.valueColor;
        context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
        context.lineWidth = 1;
        context.strokeStyle = awDisplayColorConfig.valueOutlineColor;
        context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
        context.closePath();

        // Hand
        context.beginPath();
        context.beginPath();
        if (awDisplayColorConfig.withHandShadow) {
            context.shadowColor = awDisplayColorConfig.shadowColor;
            context.shadowOffsetX = 3;
            context.shadowOffsetY = 3;
            context.shadowBlur = 3;
        }
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
        context.fillStyle = awDisplayColorConfig.handColor;
        context.fill();
        context.lineWidth = 1;
        context.strokeStyle = awDisplayColorConfig.handOutlineColor;
        context.stroke();
        // Knob
        context.beginPath();
        context.arc((canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
        context.closePath();
        context.fillStyle = awDisplayColorConfig.knobColor;
        context.fill();
        context.strokeStyle = awDisplayColorConfig.knobOutlineColor;
        context.stroke();
    };
}