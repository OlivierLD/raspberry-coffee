## Run a PHP server locally
There is a `NodeJS` module that allows you to run a local `PHP` server, good for tests and development:
```
$ npm i php-server-manager
```
Create a `server.js` as follow:
```javascript
const PHPServer = require('php-server-manager');

const server = new PHPServer({
    port: 3000,
    directives: {
        display_errors: 0,
        expose_php: 0
    }
});

server.run();
```
This server can be run like this:
```
$ node server.js
```
Drop a `sample.php` in the directory you started the server from:
```
<!DOCTYPE html>
<html>
<body>

<?php
echo "My first PHP script!";
?>

</body>
</html>
```
... And reach http://localhost:3000/sample.php

## System Resolution
The core code is in `system.php`, runnable as explained above, using interactive web interface at http://localhost:3000/index.html

#### To run the System Resolution example
From the `php` directory (where the `server.js` is), run
```
 $ node server.js
```

Then from your browser, reach http://localhost:3000/index.html

---
