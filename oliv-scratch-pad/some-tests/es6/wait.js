// Bad
for (let i = 1; i < 10; i++) {
	setTimeout(function () {
		console.log(`Bam! (${i})`);
	}, 1000);
}
console.log("End of the story");


// Ok
(function theLoop (i) {
	setTimeout(() => {
		console.log("Bam!!");
		if (--i) {          // If i > 0, keep going
			theLoop(i);       // Call the loop again, and pass it the current value of i
		}
	}, 1000);
	console.log("End of the story");
})(5);

// Another one, ES6
function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

async function demo() {
	console.log('Taking a break...');
	await sleep(2000);
	console.log('Two seconds later');
}

demo();

// Wait for a condition to be fulfilled
function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

let bool = false;

setTimeout(() => {
	console.log(">> Setting bool to true");
	bool = true;
}, 10000);

async function waitForBool(ms) {
	console.log('Taking a break...');
	while (bool === false) {
		console.log("... Waiting a bit");
		await sleep(ms);
	}
	console.log('Bool now true');
}

async function demo2() {
	await waitForBool(1000);
	console.log('- Done waiting');
	return 'From demo2: Done!';
}

async function showMe() {
	let val = await demo2();
	// Display below appears only once the condition is fulfilled
	console.log('----------------------');
	console.log(`Finally got this: [${val}]`);
	console.log('----------------------');
}

showMe();

