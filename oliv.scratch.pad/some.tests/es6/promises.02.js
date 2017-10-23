function getSum(n1, n2) {
	var isAnyNegative = function() {
		return n1 < 0 || n2 < 0;
	};

	var promise = new Promise(function(resolve, reject) {
		if (isAnyNegative()) {
			reject(Error("Negative numbers not supported"));
		}
		resolve(n1 + n2);
	});
	return promise;
};

function testIt(a, b) {
	getSum(a, b).then(function (result) { // this is the resolve function
		console.log("Result is ", result);
	}, function (error) {                 // this is the reject function
		console.log("Oops:", error);
	});
};

testIt(5, 6);

testIt(5, -6);
