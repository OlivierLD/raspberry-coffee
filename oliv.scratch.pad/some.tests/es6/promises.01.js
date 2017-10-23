setTimeout(function () {
	console.log("One");
	setTimeout(function () {
		console.log("Two");
		setTimeout(function () {
			console.log("Three");
		}, 1000);
	}, 1000);
}, 1000);
