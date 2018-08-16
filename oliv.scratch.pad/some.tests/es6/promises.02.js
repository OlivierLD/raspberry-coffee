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
						console.log("---- S U C C E S S ----");
						console.log("Success: Result is ", result);
						console.log("-----------------------");
					},
					// this is the reject function
					function (error) {
						console.log("--- R E J E C T E D ---");
						console.log("Oops:", error);
						console.log("-----------------------");
					});
}

testIt(5, 6);

testIt(5, -6);
