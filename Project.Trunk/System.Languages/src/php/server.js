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

server.run();
