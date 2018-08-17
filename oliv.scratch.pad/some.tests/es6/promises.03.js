// Good resource at https://www.datchley.name/es6-promises/

// this function returns a Promise
function getSum(n1, n2) {

	function isAnyNegative() {
		return n1 < 0 || n2 < 0;
	}

	let promise = new Promise(function(resolve, reject) {
		if (isAnyNegative()) {
			reject(Error("Negative numbers not supported"));
		}
		setTimeout(() => {
					resolve(n1 + n2);
				}, Math.floor(Math.random() * 1000) + 1); // Random wait
	});
	return promise;
}

// Async
async function testIt(a, b) {
	// Those 3 lines produce the same. The default 'resolve' apparently returns its prm.
//let sum = await getSum(a, b).then((s) => { return s; }); // Wait for the promise to complete
//let sum = await getSum(a, b).then((s) => s); // Wait for the promise to complete
	let sum = await getSum(a, b); // Wait for the promise to complete
	console.log("Sum:", sum);
}

console.log("--- ASYNC ---");
testIt(5, 6);
testIt(5, -6).catch((err) => console.log("Argh!", err));
testIt(12, 34);
testIt(0, 1);
