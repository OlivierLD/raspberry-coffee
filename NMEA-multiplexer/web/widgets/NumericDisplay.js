/*
 * @author Olivier Le Diouris
 */
var numericDisplayColorConfigBlack =
    {
        bgColor: 'black',
        withGradient: true,
        displayBackgroundGradient: {from: 'black', to: 'LightGrey'},
        valueColor: 'red',
        valueOutlineColor: 'black',
        font: 'Arial'
    };
var numericDisplayColorConfigWhite =
    {
        bgColor: 'white',
        withGradient: true,
        displayBackgroundGradient: {from: 'LightGrey', to: 'white'},
        valueColor: 'grey',
        valueOutlineColor: 'black',
        font: 'Arial'
    };
var numericDisplayColorConfig = numericDisplayColorConfigBlack;

function NumericDisplay(cName,     // Canvas Name
                        dSize,     // height         
                        nbDigits)  //
{
    if (dSize === undefined)
        dSize = 20;
    if (nbDigits === undefined)
        nbDigits = 1;

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
            numericDisplayColorConfig = numericDisplayColorConfigBlack;
        else if (schemeColor === 'white')
            numericDisplayColorConfig = numericDisplayColorConfigWhite;

        var canvas = document.getElementById(displayCanvasName);
        var context = canvas.getContext('2d');

        var radius = displayRadius;

        if (numericDisplayColorConfig.withGradient) {
            var grd = context.createLinearGradient(0, 5, 0, radius);
            grd.addColorStop(0, numericDisplayColorConfig.displayBackgroundGradient.from);// 0  Beginning
            grd.addColorStop(1, numericDisplayColorConfig.displayBackgroundGradient.to);// 1  End
            context.fillStyle = grd;
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