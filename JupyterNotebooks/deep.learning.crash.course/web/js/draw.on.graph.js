/*
 * @author Olivier Le Diouris
 */

/**
 *
 * @param cName
 * @param graphData, for all inputs (3 inputs by default) : { Xi value, Wi value }, and Bias
 * @constructor
 */
function Graph(cName, graphData) {

	let instance = this;
	let context;

	let canvas = document.getElementById(cName);
	let minY = graphData.minY,
			maxY = graphData.maxY,
			minX = graphData.minX,
			maxX = graphData.maxX;
	let gridXStep = 1,
			gridYStep = 1;
	let xScale = 1,
			yScale = 1;

	this.drawGraph = function(displayCanvasName, data) {

		context = canvas.getContext('2d');

		let width = context.canvas.clientWidth;
		let height = context.canvas.clientHeight;

		if (width === 0 || height === 0) { // Not visible
			return;
		}
		this.init(data);

		// Set the canvas size from its container.
		canvas.width = width;
		canvas.height = height;

		gridXStep = 1;
		gridYStep = 1;

		// Horizontal grid (x)
		for (let i=minY; i<maxY; i+=gridYStep) {
			context.beginPath();
			context.lineWidth = 1;
			context.strokeStyle = 'rgba(0, 0, 128, 0.5)';
			let h = ((i - minY) / (maxY - minY)) * height;
			context.moveTo(0, height - h);
			context.lineTo(width, height - h);
			context.stroke();

			context.save();
			context.font = "bold 10px Arial";
			context.fillStyle = 'rgba(0, 0, 128, 0.5)';
			let str = i.toString();
			let len = context.measureText(str).width;
			context.fillText(str, width - (len + 2), height - h - 2);
			context.restore();
			context.closePath();
		}

		// Vertical grid (y)
		for (let i=minX; i<maxX; i+=gridXStep) {
			context.beginPath();
			context.lineWidth = 1;
			context.strokeStyle = 'rgba(0, 0, 128, 0.5)';
			let w = ((i - minX) / (maxX - minX)) * width;
			context.moveTo(w, 0);
			context.lineTo(w, height);
			context.stroke();

			// Rotate the whole context, and then write on it (that's why we need the translate)
			context.save();
			context.translate(w, height);
			context.rotate(-Math.PI / 2);
			context.font = "bold 10px Arial";
			context.fillStyle = 'rgba(0, 0, 128, 0.5)';
			let str = i.toString();
			let len = context.measureText(str).width;
			context.fillText(str, 2, -1); //i * xScale, cHeight - (len));
			context.restore();
			context.closePath();
		}

		colors = [ 'blue', 'green', 'red' ];
		// Drawing the functions
		for (neuron in data.neurons) {
			// console.log(data.neurons[neuron], data.bias);
			context.beginPath();
			context.lineWidth = 2;
			context.strokeStyle = colors[neuron % colors.length];
			context.fillStyle = context.strokeStyle;
			let fX = (minX * data.neurons[neuron].w) + data.bias;
			let y = ((fX - minY) / (maxY - minY)) * height;
			let w = 0;
			context.moveTo(w, height - y);
			fX = (maxX * data.neurons[neuron].w) + data.bias;
			y = ((fX - minY) / (maxY - minY)) * height;
			w = width;
			context.lineTo(w, height - y);
			context.stroke();
			context.closePath();
			// The point
			// console.log(data);
			fX = (data.neurons[neuron].x * data.neurons[neuron].w) + data.bias;
			y = ((fX - minY) / (maxY - minY)) * height;
			let x = width * ((data.neurons[neuron].x - minX) / (maxX - minX));
			context.arc(x, height - y, 4, 0, 2 * Math.PI, false); // 60 degrees
			context.fill();
		}
	};

	this.init = function(data) {
	};

	(function() {
		instance.init(graphData);
		instance.drawGraph(cName, graphData);
	})(); // Invoked automatically when new is invoked.
}
