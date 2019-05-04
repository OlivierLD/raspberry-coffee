// Bad
for (var i = 1; i < 10; i++) {
	setTimeout(function () {
		console.log("Bam!");
	}, 1000);
}
console.log("End of the story");


// Ok
(function theLoop (i) {
	setTimeout(function () {
		console.log("Bam!");
		if (--i) {          // If i > 0, keep going
			theLoop(i);       // Call the loop again, and pass it the current value of i
		}
	}, 1000);
	console.log("End of the story");
})(5);
