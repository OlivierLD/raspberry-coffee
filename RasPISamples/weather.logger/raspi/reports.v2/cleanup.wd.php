<?php

header('Content-type: application/json;charset=UTF-8');

// phpinfo();
/*
 * Delete weather data from the WEATHER_DATA table.
 * http://machine:port/php/raspi/cleanup.wd.php?YEAR=2013&MONTH=02&DAY=01
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

$year  = getPrm('YEAR');
$month = getPrm('MONTH');
$day   = getPrm('DAY');
$hour  = getPrm('HOUR');
$min   = getPrm('MIN');
$sec   = getPrm('SEC');

$pswd  = getPrm('PSWD');

// App logic

$username="oliv";
$password=$pswd;
$database="sensors";

$link = mysql_connect("mysql", $username, $password);
//$link = mysql_connect("localhost", $username, $password);
@mysql_select_db($database) or die("Unable to select database $database");

$sql = "DELETE FROM `weather_data` WHERE `log_time` < STR_TO_DATE('$day-$month-$year $hour:$min:$sec', '%d-%m-%Y %h:%i:%s')";

$mess = "Deleted with [$sql]";
if (!mysql_query($sql)) {
  $mess = mysql_error();
  die('Delete from DB problem: ' . $mess . '<br><a href="idform.html">Back</a>');
}

//mysql_close();
mysql_close($link);

// A REST app would return the payload after insert...
$response = $mess; // "{ 'message': '" . $mess . "' }";
echo $response;

?>
