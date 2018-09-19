<?php

header('Content-type: application/json;charset=UTF-8');

$username="oliv";
$password="xxxx";
$database="sensors";

$link = mysql_connect("mysql", $username, $password);
@mysql_select_db($database) or die("Unable to select database $database");

$sql = 'SELECT data_name, unit, min_value, max_value'
        . ' FROM units';

$result = mysql_query($sql);
$num = mysql_numrows($result);
$i=0;

$json = 'var units = [';
$first = true;
try
{
  while ($i < $num) 
  {
    $dataName = mysql_result($result, $i, "data_name");
    $unit     = mysql_result($result, $i, "unit");
    $min      = mysql_result($result, $i, "min_value");
    $max      = mysql_result($result, $i, "max_value");
    
    $json .= (($first ? '' : ', ') . '{ \'name\': \'' . $dataName . '\', \'unit\': \'' . $unit . '\'' );
    if ($min != null)
    {
      $json .= (', \'mini\': ' . $min);
    }
    if ($max != null)
    {
      $json .= (', \'maxi\': ' . $max);
    }
    $json .=  ' }';
  //echo $i. ' = ' . $json;
    $first = false;
    $i++;
  }
}
catch (Exception $e)
{
  $json .= $e->getMessage();
}
$json .= '];';
mysql_close($link);        
// Return the result
echo $json;

?>        
