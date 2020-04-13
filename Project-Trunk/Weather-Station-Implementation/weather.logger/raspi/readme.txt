Use from Postman:
=================

GET http://donpedro.lediouris.net/php/raspi?board=OlivsRasPI&temp=19

Returns a JSON Obj:
{
  { 'board': 'MyPI' },
  {
    'temperature': 12,
    'advice': 'Turn it on!'
  }
}

Reports:
========
http://donpedro.lediouris.net/php/raspi/reports/datatypes.php

Weather Station:
================
http://donpedro.lediouris.net/php/weather/reports.v2/weather.report.html

Cleanup:
========
http://donpedro.lediouris.net/php/weather/reports.v2/mysql.cleanup.html

Last Weather Data:
==================
GET http://donpedro.lediouris.net/php/weather/reports.v2/json.data.php?type=ALL&period=LAST




