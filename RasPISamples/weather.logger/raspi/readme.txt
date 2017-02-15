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