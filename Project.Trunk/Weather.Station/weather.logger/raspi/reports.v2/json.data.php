<?php
/*
 * Query the WEATHER_DATA table, according to the received parameters.
 */

header('Content-type: application/json;charset=UTF-8');

$type   = null;
$period = null;

// type prm can be one of WIND, RAIN, PRESS, TEMP, HUM, CPU
if (!empty($_GET['type'])) {
  try {
    $type = $_GET['type'];
  } catch (Exception $e) {
    $type = "";
  }
} else {
  $type = "ALL";
}


// period prm can be one of ALL, YEAR, MONTH, WEEK, DAY
if (!empty($_GET['period'])) {
  try {
    $period = $_GET['period'];
  } catch (Exception $e) {
    $period = "";
  }
} else {
  $period = "";
}

$username = "oliv";
$password = "xxxxxx";
$database = "sensors";

$link = mysql_connect("mysql", $username, $password);
@mysql_select_db($database) or die("Unable to select database $database");

$sql = 'SELECT log_time, ';

if ($type == 'WIND') {
  $sql .= ('CONCAT(FORMAT(wdir, 0), \',\', FORMAT(wgust, 2), \',\', FORMAT(wspeed, 2)) AS data_value');
//$sql .= ('wspeed AS data_value');
} else if ($type == 'RAIN') {
  $sql .= ('rain AS data_value');
} else if ($type == 'PRESS') {
  $sql .= ('press / 100.0 AS data_value');
} else if ($type == 'TEMP') {
  $sql .= ('atemp AS data_value');
} else if ($type == 'HUM') {
  $sql .= ('hum AS data_value');
} else if ($type == 'DEW') {
  $sql .= ('dew AS data_value');
} else if ($type == 'ALL') {
  $sql .= ('dew, FORMAT(wdir, 0) as wdir, FORMAT(wgust, 2) as wgust, FORMAT(wspeed, 2) as wspeed, rain, press / 100 as press, atemp, hum');
}
// All the data we are interested in are in WEATHER_DATA.
$sql .=   ' FROM weather_data';
if ($period != '' && $period == 'LAST') {
  $sql .= (' WHERE log_time = (SELECT MAX(log_time) FROM weather_data)');
} else if ($period != '' && $period != 'ALL') {
  $nbs = 3600 * 24;
  if ($period == 'DAY') {
    $nbs = 3600 * 24; // DAY, with time offset ??
  } else if ($period == '2DAYS') {
    $nbs *= 2;
  } else if ($period == '3DAYS') {
      $nbs *= 3;
  } else if ($period == '4DAYS') {
      $nbs *= 4;
  } else if ($period == '5DAYS') {
      $nbs *= 5;
  } else if ($period == 'WEEK') {
    $nbs *= 7;
  } else if ($period == 'MONTH') {
    $nbs *= 30;
  } else if ($period == 'YEAR') {
    $nbs *= 365;
  }
//$sql .= (' WHERE (UNIX_TIMESTAMP(current_date()) - UNIX_TIMESTAMP(log_time)) < ' . $nbs);
  $sql .= (' WHERE (UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(log_time)) < ' . $nbs);
}
$sql .=   ' ORDER BY log_time'
        . ' ASC '; // Oldest on top

$result = mysql_query($sql);
$num = mysql_numrows($result);
$i = 0;

// Build the json object here...
$json = '{ "type":"' . $type . '", "period":"' . $period . '", ';
$json .= '"query":"' . $sql . '", ';
$json .= '"data":[';
$first = true;
while ($i < $num) {
  $time  = mysql_result($result, $i, "log_time");
  $value = mysql_result($result, $i, "data_value"); // Alias

  if ($type == 'WIND') {
    $array = explode(',', $value);
    $json .= (($first ? '' : ', ') . '{ "time": "' . $time
                                   . '", "wdir": ' . $array[0]
                                   .  ', "gust":'  . $array[1]
                                   .  ', "ws":'    . $array[2]
                                   . ' }');
  } else if ($type == 'ALL') {
    $dew   = mysql_result($result, $i, "dew");
    $wdir  = mysql_result($result, $i, "wdir");
    $wgust = mysql_result($result, $i, "wgust");
    $ws    = mysql_result($result, $i, "wspeed");
    $rain  = mysql_result($result, $i, "rain");
    $press = mysql_result($result, $i, "press");
    $atemp = mysql_result($result, $i, "atemp");
    $hum   = mysql_result($result, $i, "hum");
    $json .= (($first ? '' : ', ')
          . '{ "time": "' . $time
          . '", "wdir": ' . $wdir
          .  ', "gust":'  . $wgust
          .  ', "ws":'    . $ws
          .  ', "rain":'  . $rain
          .  ', "press":' . $press
          .  ', "atemp":' . $atemp
          .  ', "hum":'   . $hum
          .  ', "dew":'   . $dew
          . ' }');
  } else {
    $json .= (($first ? '' : ', ') . '{ "time": "' . $time . '", "value": ' . $value . ' }');
  }
//echo $i. ' = ' . $json;
  $first = false;
  $i++;
}
$json .= ']}';

mysql_close($link);
// Return the result. Yes, echo.
echo $json;

?>
