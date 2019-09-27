## To install the dependencies
Just run
```
 $ [sudo] python3 setup.py install
```
Good doc on `setup.py` at <https://python-packaging.readthedocs.io/en/latest/dependencies.html>

### NMEA Parser
[NMEA](http://nmea.org) (National Marine Electronics Association) is one of the oldest IT standards, defining how sensor data should be conveyed.

A parser is the tool that takes data flows (user input, data streams, etc) and turns them in a format that can be processed by a computer program.

In our NMEA case, it would turn things like this
```
$GNRMC,132857.00,A,3744.93332,N,12230.41996,W,0.097,,250919,,,D*76
```
into something like that
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
> Note: The above represents a JSON Objet (JavaScript Object Notation). This is far from being the only format a parser can produce. A given parser
> is tied to a language. There are parsers for JSON (for JavaScript), for Java, for C, for Python, etc. If you want to work in `Language A`, then you
> need a parser for `Language A`.

#### Data validation
To make sure the data we want to parse are valid, based on the NMEA spec, we need to check the following points:
- Does the string begin with `$`?
- Does it end with `\r\n`?
- Does it end with `*XX` (where `X` is in `[0, F]`)?
- Is the checksum valid?
    - Checksum is a logical `XOR` (aka eXclusive OR) on all the characters of the sentence, without the first `$`, and ending before the `*` preceding the checksum

#### Data parsing
Once the data validity has been determine, we can get to the data. To do so, we
- Drop the Checksum
- Split at `,`

Now we can get to the data, they are at this stage represented as an array of strings.
The cardinality of this array depends on the Sentence ID (this will be `RMC` in the example below).
Also keep in mind that some members of such an array can very well be empty.

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
> _Note_: In `GPRMC`, `GP` is the _device prefix_, `RMC` is called the _sentence ID_

Fo more details on the process, there is [a Jupyter Notebook](./python.nmea.ipynb) illustrating the full process, from reading the serial flow byte by byte to displaying the data carried over.

<!-- Ideas for Christophe
    - GPS: History (Loran, Decca, Hyperbolic Navigation System), 24 satellites on 6 orbits
    - Pair programming
    - github
    - Write tests!!!
    - Python, NodeJS, Java (and Co)
    - Jupyter Notebooks for
        - Checksum validation - Ok
        - NMEA Sentence parsing (like RMC) - Ok
        - Decimal to Sexagesimal and vice-versa - Ok
    - Explain knots and nautical miles
    - Record and replay
    - Google Maps APIs -> Geolocalisation API: https://olivierld.github.io/web.stuff/gps/GPS.api.html
-->

### Links
- [NMEA Multiplexer](https://github.com/OlivierLD/raspberry-coffee/blob/master/NMEA.multiplexer/README.md)
- There is a good NMEA documentation [here](https://gpsd.gitlab.io/gpsd/NMEA.html)

---
