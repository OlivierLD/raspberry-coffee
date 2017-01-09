/*
 * @author Olivier Le Diouris
 */
var timeDisplayColorConfigBlack =
    {
        bgColor: 'black',
        withGradient: true,
        displayBackgroundGradient: {from: 'black', to: 'LightGrey'},
        valueColor: 'red',
        valueOutlineColor: 'black',
        font: 'Arial'
    };
var timeDisplayColorConfigWhite =
    {
        bgColor: 'white',
        withGradient: true,
        displayBackgroundGradient: {from: 'LightGrey', to: 'white'},
        valueColor: 'grey',
        valueOutlineColor: 'black',
        font: 'Arial'
    };
var timeDisplayColorConfig = timeDisplayColorConfigBlack;

function TimeDisplay(cName,     // Canvas Name
                     dSize)     // height         
{
    if (dSize === undefined)
        dSize = 20;

    var scale = dSize / 100;
    var width = dSize;

    var canvasName = cName;
    var displaySize = dSize;

    // Default
    var hours = 0;
    var minutes = 0;
    var seconds = 0;

    var instance = this;

//try { console.log('in the AnalogDisplay constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

    (function () {
        drawDisplay(canvasName, displaySize, hours, minutes, seconds);
    })(); // Invoked automatically

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

    function drawDisplay(displayCanvasName, displayRadius, h, m, s) {
        var schemeColor = getStyleRuleValue('color', '.display-scheme');
//  console.log(">>> DEBUG >>> color:" + schemeColor);
        if (schemeColor === 'black')
            timeDisplayColorConfig = timeDisplayColorConfigBlack;
        else if (schemeColor === 'white')
            timeDisplayColorConfig = timeDisplayColorConfigWhite;

        var canvas = document.getElementById(displayCanvasName);
        var context = canvas.getContext('2d');

        var radius = displayRadius;

        if (timeDisplayColorConfig.withGradient) {
            var grd = context.createLinearGradient(0, 5, 0, radius);
            grd.addColorStop(0, timeDisplayColorConfig.displayBackgroundGradient.from);// 0  Beginning
            grd.addColorStop(1, timeDisplayColorConfig.displayBackgroundGradient.to);// 1  End
            context.fillStyle = grd;
        }

        // The rectangles around each digit
        var nbDigits = 8; // DD-MMM-YY
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
            textHour = h.toFixed(0);
            while (textHour.length < 2) {
                textHour = '0' + textHour;
            }
            textMinute = m.toFixed(0);
            while (textMinute.length < 2) {
                textMinute = '0' + textMinute;
            }
            textSecond = s.toFixed(0);
            while (textSecond.length < 2) {
                textSecond = '0' + textSecond;
            }


            var text = textHour + ":" + textMinute + ":" + textSecond;

            for (var i = 0; i < nbDigits; i++) {
                len = 0;
                context.font = "bold " + Math.round(scale * 40) + "px Arial"; // "bold 40px Arial"
                var txt = text.substring(i, i + 1);
                var metrics = context.measureText(txt);
                len = metrics.width;
                var x = i * oneDigitWidth;
                context.beginPath();
                context.fillStyle = timeDisplayColorConfig.valueColor;
                context.fillText(txt, x + (oneDigitWidth / 2) - (len / 2), canvas.height - 10);
                context.lineWidth = 1;
                context.strokeStyle = timeDisplayColorConfig.valueOutlineColor;
                context.strokeText(txt, x + (oneDigitWidth / 2) - (len / 2), canvas.height - 10); // Outlined
                context.closePath();
            }
        }
    };

    this.setValue = function (val) {
        var time = new Date(val);
        drawDisplay(canvasName, displaySize, time.getHours(), time.getMinutes() + 1, time.getSeconds());
    };
}