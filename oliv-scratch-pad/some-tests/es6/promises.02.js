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
		resolve(n1 + n2);
	});
	return promise;
}

function testIt(a, b) {
	getSum(a, b)
			.then(
					// this is the resolve function
					function (result) {
						console.log("---- 1 - S U C C E S S ----");
						console.log("Success: Result is ", result);
						console.log("-----------------------");
					},
					// this is the reject function
					function (error) {
						console.log("--- 1 - R E J E C T E D ---");
						console.log("Oops:", error);
						console.log("-----------------------");
					});
}

function testIt_2(a, b) {
	getSum(a, b)
			.then(
					// this is the resolve function
					function (result) {
						console.log("---- 2 - S U C C E S S ----");
						console.log("Success: Result is ", result);
						console.log("-----------------------");
					})
			.catch (
					// this is the reject function
					function (error) {
						console.log("--- 2 - R E J E C T E D ---");
						console.log("Oops:", error);
						console.log("-----------------------");
					});
}

console.log("--- RESOLVE / REJECT ---");
testIt(5, 6);
testIt(5, -6);

console.log("--- THEN.CATCH ---");
testIt_2(5, 6);
testIt_2(5, -6);
