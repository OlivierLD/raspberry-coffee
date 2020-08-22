# HTTP Clients
We are showing here how to get data from HTTP servers reading breakout boards, 
using drivers we do not need to care about.

Typically, this would concern drivers written in Python, see in the `Papirus` 
(look for `papirus_server.py`) and `I2C-SPI` (look for `lis3mdl_server.py`) modules.


Features: 
- Java
- Scala (install [Scala](https://sdkman.io/sdks#scala))
- Kotlin (install [Kotlin](https://sdkman.io/sdks#kotlin))
- Groovy (install [groovy](https://sdkman.io/sdks#groovy), or [here](https://groovy-lang.org/install.html))

> Note: To install extra languages, [SDKMAN](https://sdkman.io/install) is the easiest, whatever your system is.

### To run the samples

First, compile
```text
$ ../gradlew clean shadowJar
$ export CP=./build/libs/http-clients-1.0-all.jar
```
Then start the Python HTTP Server on a machine it is available with the board it reads:
```text
$ python3 lis3mdl_server.py --machine-name:$(hostname -I) --verbose:false
  Starting!
  Let's go. Hit Ctrl+C to stop
  Starting server on port 8080
  Try curl -X GET http://192.168.42.9:8080/lis3mdl/oplist
  or  curl -v -X VIEW http://192.168.42.9:8080/lis3mdl -H "Content-Length: 1" -d "1"
```

#### For Java
```text
$ java -cp ${CP} http.MagnetometerReader
Ctrl+C to stop
Heading: 143.892676 Pitch: 122.770906, Roll: -115.152288
Heading: 143.892676 Pitch: 122.770906, Roll: -115.152288
Heading: 143.892676 Pitch: 122.770906, Roll: -115.152288
Heading: 143.892676 Pitch: 122.770906, Roll: -115.152288
Heading: 143.892676 Pitch: 122.770906, Roll: -115.152288
Heading: 143.892676 Pitch: 122.770906, Roll: -115.152288
. . .
```

#### For Scala
```text
$ scala -cp ${CP} rest.LIS3MDLReader 
Ctrl+C to stop
Heading: 143.64324866174428 Pitch: 122.07185393670603, Roll: -114.76153087160264
Heading: 143.64324866174428 Pitch: 122.07185393670603, Roll: -114.76153087160264
Heading: 143.64324866174428 Pitch: 122.07185393670603, Roll: -114.76153087160264
Heading: 143.76748929250718 Pitch: 121.24913419262627, Roll: -113.97161468597878
Heading: 143.76748929250718 Pitch: 121.24913419262627, Roll: -113.97161468597878
Heading: 143.76748929250718 Pitch: 121.24913419262627, Roll: -113.97161468597878
Heading: 143.76748929250718 Pitch: 121.24913419262627, Roll: -113.97161468597878
. . .
```
> Note: you can also do a
```text
$ java -cp ${CP} rest.LIS3MDLReader 
. . .
```

#### For Kotlin
```text
$ kotlin -cp ${CP} restkt.KtMagReader
Heading:143.34438513662025 °, Pitch:122.7128245452906 °, Roll:-115.54712852747782 °
Heading:143.34438513662025 °, Pitch:122.7128245452906 °, Roll:-115.54712852747782 °
Heading:143.34438513662025 °, Pitch:122.7128245452906 °, Roll:-115.54712852747782 °
Heading:143.34438513662025 °, Pitch:122.7128245452906 °, Roll:-115.54712852747782 °
Heading:143.34438513662025 °, Pitch:122.7128245452906 °, Roll:-115.54712852747782 °
Heading:143.34438513662025 °, Pitch:122.7128245452906 °, Roll:-115.54712852747782 °
Heading:143.34438513662025 °, Pitch:122.7128245452906 °, Roll:-115.54712852747782 °
Heading:143.34438513662025 °, Pitch:122.7128245452906 °, Roll:-115.54712852747782 °
. . .
```
> Note: you can also do a 
```text
$ java -cp ${CP} restkt.KtMagReader
. . .
```

#### For Groovy
```text
$ groovy -cp ${CP} src/main/groovy/magreader.groovy 
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.codehaus.groovy.reflection.CachedClass$3$1 (file:/Users/olivierlediouris/.gradle/caches/modules-2/files-2.1/org.codehaus.groovy/groovy-all/2.4.6/478feadca929a946b2f1fb962bb2179264759821/groovy-all-2.4.6.jar) to method java.lang.Object.finalize()
WARNING: Please consider reporting this to the maintainers of org.codehaus.groovy.reflection.CachedClass$3$1
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
Heading: 143.737568 Pitch: 123.377093, Roll: -115.793348
Heading: 143.737568 Pitch: 123.377093, Roll: -115.793348
Heading: 143.737568 Pitch: 123.377093, Roll: -115.793348
Heading: 143.737568 Pitch: 123.377093, Roll: -115.793348
Heading: 143.737568 Pitch: 123.377093, Roll: -115.793348
Heading: 143.737568 Pitch: 123.377093, Roll: -115.793348
```

---
 