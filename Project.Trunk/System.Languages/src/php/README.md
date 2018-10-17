## Run a PHP server locally

`$ php -S localhost:3000`

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
From the `php` directory (where the `index.html` is), run
```
 $ php -S localhost:3000
```

Then from your browser, reach http://localhost:3000/index.html

---
