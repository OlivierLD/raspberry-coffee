<?php

header('Content-type: application/json;charset=UTF-8');

// phpinfo();
/*
 * Delete weather data from the WEATHER_DATA table.
 * http://machine:port/php/raspi/cleanup.wd.php?YEAR=2013&MONTH=02&DAY=01
 */

function getPrm($name) { // Returns -1 if not found
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

$pswd  = getPrm('PSWD'); // Password provided by the client.

// App logic

$username="oliv";
$password=$pswd;
$database="sensors";

if ($year == -1) { $year = 1970; }
if ($month == -1) { $month = 01; }
if ($day == -1) { $day = 01; }
if ($hour == -1) { $hour = 00; }
if ($min == -1) { $min = 00; }
if ($sec == -1) { $sec = 00; }
// $sql = "DELETE FROM `weather_data` WHERE `log_time` < STR_TO_DATE('$day-$month-$year $hour:$min:$sec', '%d-%m-%Y %h:%i:%s')";
// $sql = "DELETE FROM `weather_data` WHERE `log_time` < '2018-03-01 00:00:00'";
$sql = "DELETE FROM `weather_data` WHERE `log_time` < '$year-$month-$day $hour:$min:$sec'";

$mess = "Deleting with \n[$sql]";

$link = mysql_connect("mysql", $username, $password);
//$link = mysql_connect("localhost", $username, $password);
@mysql_select_db($database) or die("Unable to reach database $database with username $username and provided paswword.");

if (!mysql_query("BEGIN WORK")) {
    $mess .= ("\n" . mysql_error());
    die('BEGIN WORK problem: ' . $mess . '<br><a href="#" onclick="back();">Back</a>');
} else {
    $mess .= "\nBEGIN WORK Done.";
}

if (!mysql_query($sql)) {
  $mess .= ("\n" . mysql_error());
  die('Delete from DB problem: ' . $mess . '<br><a href="#" onclick="back();">Back</a>');
} else {
  $mess .= ("\nDELETE Done, record(s) deleted: " . mysql_affected_rows());
}

if (!mysql_query("COMMIT")) {
    $mess .= ("\n" . mysql_error());
    die('COMMIT problem: ' . $mess . '<br><a href="#" onclick="back();">Back</a>');
} else {
    $mess .= "\nCOMMIT Done.";
}

//mysql_close();
mysql_close($link);

// A REST app would return the payload after insert...
$response = $mess; // "{ 'message': '" . $mess . "' }";
echo $response;

?>
