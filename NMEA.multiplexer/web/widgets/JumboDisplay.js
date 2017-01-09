/*
 * @author Olivier Le Diouris
 */
function JumboDisplay(cName,                     // Canvas Name
                      title,
                      width,                     // Display width
                      height,                    // Display height
                      value,
                      textColor) {
    // base = w 160 h 80
    var scale = 1;

    var canvasName = cName;

    var displayTitle = title;
    var displayWidth = width;
    var displayHeight = height;

    var valueToDisplay = "0.00";

    if (value !== undefined)
        valueToDisplay = value;
    if (textColor === undefined)
        textColor = 'LightGreen';

    var instance = this;

    (function () {
        drawDisplay(canvasName, displayTitle, displayWidth, displayHeight);
    })(); // Invoked automatically

    this.setValue = function (val) {
        valueToDisplay = val;
        drawDisplay(canvasName, displayTitle, displayWidth, displayHeight);
    };

    this.setTitle = function (val) {
        displayTitle = val;
        drawDisplay(canvasName, displayTitle, displayWidth, displayHeight);
    };

    this.setDisplaySize = function (dw, dh) {
        // scale = ds / 100;
        displayWidth = dw;
        displayHeight = dh;
        drawDisplay(canvasName, displayTitle, displayWidth, displayHeight);
    };

    function drawDisplay(displayCanvasName, displayT, displayW, displayH) {
        if (displayW !== undefined && displayH !== undefined) {
            scale = Math.min(displayW / 160, displayH / 80);
        }
        var canvas = document.getElementById(displayCanvasName);
        var context = canvas.getContext('2d');

        var grd = context.createLinearGradient(0, 5, 0, document.getElementById(cName).height);
        grd.addColorStop(0, 'black'); // 0  Beginning
        grd.addColorStop(1, 'gray');  // 1  End
        context.fillStyle = grd;

        // Background
        roundRect(context, 0, 0, canvas.width, canvas.height, 10, true, false);
        // Grid
        //  1 - vertical
        var nbVert = 5;
        context.strokeStyle = 'rgba(255, 255, 255, 0.7)';
        context.lineWidth = 0.5;
        for (var i = 1; i < nbVert; i++) {
            var x = i * (canvas.width / nbVert);
            context.beginPath();
            context.moveTo(x, 0);
            context.lineTo(x, canvas.height);
            context.closePath();
            context.stroke();
        }
        // 2 - Horizontal
        var nbHor = 3;
        for (var i = 1; i < nbHor; i++) {
            var y = i * (canvas.height / nbHor);
            context.beginPath();
            context.moveTo(0, y);
            context.lineTo(canvas.width, y);
            context.closePath();
            context.stroke();
        }

        context.fillStyle = textColor;
        // Title
        context.font = "bold " + Math.round(scale * 16) + "px Courier New"; // "bold 16px Arial"
        context.fillText(displayTitle, 5, 18);
        // Value
        context.font = "bold " + Math.round(scale * 60) + "px Arial";
        var metrics = context.measureText(valueToDisplay);
        len = metrics.width;

        context.fillText(valueToDisplay, canvas.width - len - 5, canvas.height - 5);
    };

    function roundRect(ctx, x, y, width, height, radius, fill, stroke) {
        if (fill === undefined) {
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
}