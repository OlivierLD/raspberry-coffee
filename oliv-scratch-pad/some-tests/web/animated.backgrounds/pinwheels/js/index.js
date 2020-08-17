/*
* File Name / pinwheel.js
* Created Date / Aug 14, 2020
* Aurhor / Toshiya Marukubo
* Twitter / https://twitter.com/toshiyamarukubo
*/

(function () {
    'use strict';
    window.addEventListener('load', function () {
        var canvas = document.getElementById('canvas');

        if (!canvas || !canvas.getContext) {
            return false;
        }

        /********************
         Random Number
         ********************/

        function rand(min, max) {
            return Math.floor(Math.random() * (max - min + 1) + min);
        }

        /********************
         Var
         ********************/

        var ctx = canvas.getContext('2d');
        var X = canvas.width = window.innerWidth;
        var Y = canvas.height = window.innerHeight;
        var mouseX = null;
        var mouseY = null;
        var bubbles = [];
        var bubbleNum = 150;
        var shapes = [];
        var rad = Math.PI * 2 / 4;
        var style = {
            black: 'black',
            white: 'white',
            lineWidth: 4,
        };

        if (X < 768) {
            bubbleNum = 100;
        }

        /********************
         Animation
         ********************/

        window.requestAnimationFrame =
            window.requestAnimationFrame ||
            window.mozRequestAnimationFrame ||
            window.webkitRequestAnimationFrame ||
            window.msRequestAnimationFrame ||
            function(cb) {
                setTimeout(cb, 17);
            };

        /********************
         Bubble
         ********************/

        function Bubble(ctx, x, y) {
            this.ctx = ctx;
            this.init(x, y);
        }

        Bubble.prototype.init = function(x, y) {
            this.x = x;
            this.y = y;
            this.r = rand(20, 50);
            this.ga = Math.random() * Math.random() * Math.random();
            this.v = {
                x: rand(-1, 1) * Math.random() * Math.random(),
                y: Math.random()
            };
            this.random = Math.random();
            this.a = rand(0, 360);
            this.rad = this.a * Math.PI / 180;
            this.as = rand(0, 360) * Math.PI / 180;
        };

        Bubble.prototype.draw = function() {
            var ctx  = this.ctx;
            ctx.save();
            ctx.fillStyle = 'white';
            ctx.strokeStyle = 'white';
            ctx.lineCap = 'round';
            ctx.lineWidth = this.r / 4;
            ctx.globalAlpha = this.ga;
            ctx.translate(this.x, this.y);
            ctx.rotate(this.rad);
            ctx.translate(-this.x, -this.y);
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.r, 0, Math.PI * 2, false);
            ctx.fill();
            ctx.globalAlpha = this.ga * 1.1;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.r * 0.7, this.as, this.as + 1, false);
            ctx.stroke();
            ctx.restore();
        };

        Bubble.prototype.updatePosition = function() {
            if (this.y > Y - Y / 3) {
                this.v.x += 0.1;
                this.v.y -= 0.01;
            }
            this.x += this.v.x;
            this.y += this.v.y;
        };

        Bubble.prototype.updateParams = function(i) {
            this.a += this.random;
            this.rad = this.a * Math.PI / 180;
        };

        Bubble.prototype.wrapPosition = function() {
            if (this.x > X + this.r / 2) {
                this.init(rand(0, X), rand(0 - Y / 5, 0));
            }
        };

        Bubble.prototype.render = function(i) {
            this.updateParams(i);
            this.updatePosition();
            this.wrapPosition();
            this.draw();
        };

        for (var i = 0; i < bubbleNum; i++) {
            var b = new Bubble(ctx, rand(0, X), rand(0, Y / 2));
            bubbles.push(b);
        }

        /********************
         Shape
         ********************/

        function Shape(ctx, x, y) {
            this.ctx = ctx;
            this.init(x, y);
        }

        Shape.prototype.init = function(x, y) {
            this.x = x;
            this.y = y;
            this.r = rand(20, 30);
            this.a = 0;
            this.rad = this.a * Math.PI / 180;
            this.random = Math.random();
            this.c = rand(0, 360);
            this.a1 = rand(-10, 10);
            this.rad1 = this.a1 * Math.PI / 180;
        };

        Shape.prototype.draw = function() {
            var ctx  = this.ctx;
            ctx.save();
            ctx.fillStyle = 'hsl(' + this.c + ', 100%, 80%)';
            ctx.strokeStyle = 'hsl(' + this.c + ', 80%, 60%)';
            ctx.translate(this.x, this.y);
            ctx.rotate(this.rad1);
            ctx.translate(-this.x, -this.y);
            ctx.lineWidth = this.r / 10;
            ctx.beginPath();
            ctx.moveTo(this.x, this.y);
            ctx.lineTo(this.x, Y + 10);
            ctx.stroke();
            ctx.lineWidth = 1;
            ctx.translate(this.x, this.y);
            ctx.rotate(Math.tan(this.rad));
            ctx.translate(-this.x, -this.y);
            ctx.fillRect(this.x - this.r, this.y - this.r, this.r * 2, this.r * 2);
            ctx.fillStyle = 'hsl(' + this.c + ', 80%, 60%)';
            for (var j = 0; j < 4; j++) {
                ctx.translate(this.x, this.y);
                ctx.rotate(90 * Math.PI / 180);
                ctx.translate(-this.x, -this.y);
                ctx.beginPath();
                ctx.moveTo(Math.cos(rad * 0) * this.r + this.x, Math.sin(rad * 0) * this.r + this.y + this.r);
                for (var i = 1; i < 4; i++) {
                    if (i !== 2) {
                        ctx.lineTo(Math.cos(rad * i) * this.r + this.x, Math.sin(rad * i) * this.r + this.y + this.r);
                    }
                }
                ctx.closePath();
                ctx.fill();
            }
            ctx.restore();
        };

        Shape.prototype.updateParams = function() {
            this.a += this.random;
            this.rad = this.a * Math.PI / 180;
        };

        Shape.prototype.render = function(i) {
            this.updateParams();
            this.draw();
        };

        for (var i = 0; i < X;) {
            var dist = rand(1, 50);
            var s = new Shape(ctx, i += dist, rand(Y - Y / 3, Y - Y / 6));
            shapes.push(s);
        }

        /********************
         Render
         ********************/

        function render() {
            ctx.clearRect(0, 0, X, Y);
            for (var i = 0; i < shapes.length; i++) {
                shapes[i].render(i);
            }
            for (var i = 0; i < bubbles.length; i++) {
                bubbles[i].render(i);
            }
            requestAnimationFrame(render);
        }

        render();

        /********************
         Event
         ********************/

        function onResize() {
            X = canvas.width = window.innerWidth;
            Y = canvas.height = window.innerHeight;
            if (X < 768) {
                bubbleNum = 100;
            } else {
                bubbleNum = 200;
            }
            shapes = [];
            bubbles = [];
            for (var i = 0; i < X;) {
                var dist = rand(1, 50);
                var s = new Shape(ctx, i += dist, rand(Y - Y / 3, Y - Y / 6));
                shapes.push(s);
            }
            for (var i = 0; i < bubbleNum; i++) {
                var b = new Bubble(ctx, rand(0, X), rand(0, Y / 2));
                bubbles.push(b);
            }
        }

        window.addEventListener('resize', function() {
            onResize();
        });

        canvas.addEventListener('click', function(e) {
            mouseX = e.clientX;
            mouseY = e.clientY;
            var num = rand(1, 20);
            for (var i = 0; i < num; i++) {
                var b = new Bubble(ctx, rand(mouseX - 100, mouseX + 100), rand(mouseY - 100, mouseY + 100));
                bubbles.push(b);
            }
        }, false);

    });
})();
