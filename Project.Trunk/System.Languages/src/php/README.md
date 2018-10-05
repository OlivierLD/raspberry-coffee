## Run a PHP server locally
The is a `NodeJS` module that allows you to run a `PHP` server:
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
Drop an `index.php` in the directory you started the server from:
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
... And reach http://localhost:3000/index.php

## System Resolution
The code is in `AlgebraUtils.php`, runnable as explained above, using http://localhost:3000/AlgebraUtils.php
