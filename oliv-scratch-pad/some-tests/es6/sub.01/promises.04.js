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

async function printAll(){
	await printString("A")
	await printString("B")
	await printString("C")
}

printAll()

