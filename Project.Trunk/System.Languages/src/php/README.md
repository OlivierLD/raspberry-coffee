### Install php server
To know if a php server is installed, type
```
 $ which php
```
or
```
 $ php -v
   PHP 7.1.16 (cli) (built: Mar 31 2018 02:59:59) ( NTS )
   Copyright (c) 1997-2018 The PHP Group
   Zend Engine v3.1.0, Copyright (c) 1998-2018 Zend Technologies
```
If no php server is available, you can install one.
#### On Raspberry Pi
```
 $ sudo apt-get install php libapache2-mod-php
```

## Run a PHP server locally

```
$ php -S localhost:3000
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
The core code is in `SystemSolver.php` and `SquareMatrix.php`, runnable as explained above, using interactive web interface at http://localhost:3000/index.html

#### To run the System Resolution example
From the `php` directory (where the `index.html` is), run
```
 $ php -S localhost:3000
```

Then from your browser, reach http://localhost:3000/index.html

---
