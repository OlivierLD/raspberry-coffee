<?php

header('Content-type: application/json;charset=UTF-8');

// phpinfo();
/*
 * Insert weather data in a REST-like way, with a GET (POST & PUT are not always supported).
 * http://machine:port/php/raspi/insert.wd.php?WDIR=350.0&WSPEED=12.345&WGUST=13.456&RAIN=0.1&PRMSL=101300.00&ATEMP=18.34&HUM=58.5&CPU=34.56
 *
 * URL QS:
 *  like WDIR=350.0&WSPEED=12.345&WGUST=13.456&RAIN=0.1&PRMSL=101300.00&ATEMP=18.34&HUM=58.5&CPU=34.56
 */

function getPrm($name) {
  $val = -1;
  if (!empty($_GET[$name])) {
    try {
      $val = $_GET[$name];
    } catch (Exception $e) {
      $val = "-1";
    }
  } else {
    $val = "-1";
  }
  return $val;
}

$wdir   = getPrm('WDIR');
$wspeed = getPrm('WSPEED');
$wgust  = getPrm('WGUST');
$rain   = getPrm('RAIN');
$press  = getPrm('PRMSL');
$atemp  = getPrm('ATEMP');
$hum    = getPrm('HUM');
$cpu    = getPrm('CPU');

// App logic

$username="oliv";
$password="xxxx";
$database="sensors";

$link = mysql_connect("mysql", $username, $password);
//$link = mysql_connect("localhost", $username, $password);
@mysql_select_db($database) or die("Unable to select database $database");

$sql = "INSERT INTO `weather_data` (`wdir`, `wgust`, `wspeed`, `rain`, `press`, `atemp`, `hum`, `cputemp`) "
                         ."VALUES ('$wdir', '$wgust', '$wspeed', '$rain', '$press', '$atemp', '$hum', '$cpu')";  

$mess = "Record created";
if (!mysql_query($sql)) {
  $mess = mysql_error();
  die('Insert in DB problem: ' . $mess . '<br><a href="idform.html">Back</a>');
}

//mysql_close();
mysql_close($link);        

$response = "{ 'message': '" . $mess . "' }";
echo $response;

?>
