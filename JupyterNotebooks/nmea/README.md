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
```
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
#### Data validation
- Does the string begin with `$`?
- Does it end with `\r\n`?
- Does it end with `*XX` (where `X` is in `[0, F]`)?
- Is the checksum valid?

#### Data parsing
- Drop the prefix (optional)
- Drop the Checksum
- Split at `,`
- Now get to the data!

#### Data Structure. Example: `RMC`
All NMEA Sentences are clearly documented.
RMC Structure is
```
                                                                   12
  0      1      2 3        4 5         6 7     8     9      10    11
  $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,A*6A
         |      | |        | |         | |     |     |      |     | |
         |      | |        | |         | |     |     |      |     | Type: A=autonomous,
         |      | |        | |         | |     |     |      |     |       D=differential,
         |      | |        | |         | |     |     |      |     |       E=Estimated,
         |      | |        | |         | |     |     |      |     |       N=not valid,
         |      | |        | |         | |     |     |      |     |       S=Simulator
         |      | |        | |         | |     |     |      |     Variation sign
         |      | |        | |         | |     |     |      Variation value
         |      | |        | |         | |     |     Date DDMMYY 
         |      | |        | |         | |     COG
         |      | |        | |         | SOG
         |      | |        | |         Longitude Sign
         |      | |        | Longitude Value
         |      | |        Latitude Sign
         |      | Latitude value
         |      Active or Void
         UTC
```

---
