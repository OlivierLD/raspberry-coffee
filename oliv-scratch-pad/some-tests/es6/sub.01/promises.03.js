// Good resource at https://www.datchley.name/es6-promises/
// https://medium.com/front-end-hacking/callbacks-promises-and-async-await-ad4756e01d90

function printString(string){
	return new Promise((resolve, reject) => {
		setTimeout(
				() => {
					console.log(string)
					resolve()
				},
				Math.floor(Math.random() * 100) + 1
		)
	})
}

function printAll(){
	printString("A")
			.then(() => {
				return printString("B")
			})
			.then(() => {
				return printString("C")
			})
}

// Same as
function printAll2(){
	printString("A")
			.then(() => printString("B"))
			.then(() => printString("C"))
}

printAll()

printAll2()

