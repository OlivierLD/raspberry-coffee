/*
 * @author Olivier Le Diouris
 */
var dateDisplayColorConfigBlack =
    {
        bgColor: 'black',
        withGradient: true,
        displayBackgroundGradient: {from: 'black', to: 'LightGrey'},
        valueColor: 'red',
        valueOutlineColor: 'black',
        font: 'Arial'
    };
var dateDisplayColorConfigWhite =
    {
        bgColor: 'white',
        withGradient: true,
        displayBackgroundGradient: {from: 'LightGrey', to: 'white'},
        valueColor: 'grey',
        valueOutlineColor: 'black',
        font: 'Arial'
    };
var dateDisplayColorConfig = dateDisplayColorConfigBlack;

function DateDisplay(cName,     // Canvas Name
                     dSize)     // height         
{
    if (dSize === undefined)
        dSize = 20;

    var scale = dSize / 100;
    var width = dSize;

    var canvasName = cName;
    var displaySize = dSize;

    // Default
    var day = 1;
    var month = 1;
    var year = 1970;

    var displayMonth = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];

    var instance = this;

//try { console.log('in the AnalogDisplay constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

    (function () {
        drawDisplay(canvasName, displaySize, day, month, year);
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

    function drawDisplay(displayCanvasName, displayRadius, d, m, y) {
        var schemeColor = getStyleRuleValue('color', '.display-scheme');
//  console.log(">>> DEBUG >>> color:" + schemeColor);
        if (schemeColor === 'black')
            dateDisplayColorConfig = dateDisplayColorConfigBlack;
        else if (schemeColor === 'white')
            dateDisplayColorConfig = dateDisplayColorConfigWhite;

        var canvas = document.getElementById(displayCanvasName);
        var context = canvas.getContext('2d');

        var radius = displayRadius;

        if (dateDisplayColorConfig.withGradient) {
            var grd = context.createLinearGradient(0, 5, 0, radius);
            grd.addColorStop(0, dateDisplayColorConfig.displayBackgroundGradient.from);// 0  Beginning
            grd.addColorStop(1, dateDisplayColorConfig.displayBackgroundGradient.to);// 1  End
            context.fillStyle = grd;
        }

        // The rectangles around each digit
        var nbDigits = 9; // DD-MMM-YY
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
            textDay = d.toFixed(0);
            while (textDay.length < 2) {
                textDay = '0' + textDay;
            }

            textMonth = displayMonth[m - 1];

            textYear = (y % 100).toFixed(0);
            while (textYear.length < 2) {
                textYear = '0' + textYear;
            }
            var text = textDay + "-" + textMonth + "-" + textYear;

            for (var i = 0; i < nbDigits; i++) {
                len = 0;
                context.font = "bold " + Math.round(scale * 40) + "px Arial"; // "bold 40px Arial"
                var txt = text.substring(i, i + 1);
                var metrics = context.measureText(txt);
                len = metrics.width;
                var x = i * oneDigitWidth;
                context.beginPath();
                context.fillStyle = dateDisplayColorConfig.valueColor;
                context.fillText(txt, x + (oneDigitWidth / 2) - (len / 2), canvas.height - 10);
                context.lineWidth = 1;
                context.strokeStyle = dateDisplayColorConfig.valueOutlineColor;
                context.strokeText(txt, x + (oneDigitWidth / 2) - (len / 2), canvas.height - 10); // Outlined
                context.closePath();
            }
        }
    };

    this.setValue = function (val) {
        var date = new Date(val);
        drawDisplay(canvasName, displaySize, date.getDate(), date.getMonth() + 1, date.getFullYear());
    };
}