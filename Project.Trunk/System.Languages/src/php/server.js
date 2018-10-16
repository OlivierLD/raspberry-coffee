/*
   PHP local server
 */
const PHPServer = require('php-server-manager');

const server = new PHPServer({
	port: 3000,
	directives: {
		display_errors: 0,
		expose_php: 0
	}
});

console.log("-----------------------------------");
console.log("Starting PHP server...");
server.run();

console.log("Once started, reach http://localhost:3000/index.html from your browser.");
console.log("-----------------------------------");
