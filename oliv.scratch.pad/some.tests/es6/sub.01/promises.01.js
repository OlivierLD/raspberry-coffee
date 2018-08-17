// Good resource at https://www.datchley.name/es6-promises/
// https://medium.com/front-end-hacking/callbacks-promises-and-async-await-ad4756e01d90

function printString(string){
	setTimeout(
			() => {
				console.log(string)
			},
			Math.floor(Math.random() * 100) + 1
	)
}

function printAll(){
	printString("A")
	printString("B")
	printString("C")
}
printAll()

