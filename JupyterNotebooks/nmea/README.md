## To install the dependencies
Just run
```
 $ [sudo] python3 setup.py install
```

Good doc on `setup.py` at <https://python-packaging.readthedocs.io/en/latest/dependencies.html>

### NMEA Parser
What's that?

Turn this
```
$GNRMC,132857.00,A,3744.93332,N,12230.41996,W,0.097,,250919,,,D*76
```
into that
```json
{
  'type': 'rmc',
  'parsed': {
    'valid': 'true',
    'utc-date': datetime.datetime(2019, 9, 25, 13, 28, 57, tzinfo=datetime.timezone.utc),
    'utc-date-itemized': {
      'year': 2019,
      'month': 9,
      'day': 25,
      'hours': 13,
      'minutes': 28,
      'seconds': 57
    },
    'position': {
      'latitude': 37.748888666666666,
      'longitude': -122.50699933333334
    },
    'sog': 0.097,
    'type': 'differential'
  }
}
```