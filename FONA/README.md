## FONA in Java

### An important detail
Using the PI4J `com.pi4j.io.Serial` package, I was not able to write more than 16 characters to the Serial output.
A code like this one:
```
String payload = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
serial.writeln(payload);
```
will result in an output like `"ABCDEFGHIJKLMNOP"` on the receiver's end.
I've not been able to find why, but this is a fact.

Waiting 1 millisecond between each character sent to the Serial port seems to address the issue:
```
private final static float BETWEEN_SENT_CHAR = 0.001F; // 1 ms
...
String payload = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
for (int i=0; i<payload.length(); i++)
{
  serial.write(payload.charAt(i));
  delay(BETWEEN_SENT_CHAR);
}
```
The code above works as expected. The `delay` method is defined in the code, it is a wrapper around `Thread.sleep`.

### Two approaches
This project contains two parts, each of them illustrating a way to access the FONA:
* One is using the Serial connection between the Raspberry PI and the Arduino where the FONA is connected
* One is using the Serial connection directly to the FONA.

For the first approach, see the package named `fona.arduino`, for the second one, see the package named `fona.manager`.

#### First approach: Arduino
The first approach is straightforward. It requires the sketch `FONA_for_RPi.ino` to be uploaded on the Arduino.

#### Second approach: direct
The second approach does not require an Arduino, I find it more interesting, as none of the real-time capabilities
provided by an Arduino are required in the FONA context.

See an example of a client in `fona.manager.sample.InteractiveFona.java`. It requires the client
to implement the `fona.manager.FONAClient` interface, mostly for the callbacks.



