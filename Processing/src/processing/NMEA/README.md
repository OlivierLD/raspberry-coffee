# Some NMEA Samples on Processing

- `serial101` Basic reading of a Serial port, no display.
- `serial102` Display NMEA Sentences as they come fro the Serial port
- `nmea101` Parse RMC and GLL Sentence and display the formatted data

### A trick to find the Serial port to use
With the GPS **not** plugged in, type
```
 $ ls -lisah /dev/tty* > before.txt
```
Then connect the GPS on its USB socket
```
 $ ls -lisah /dev/tty* > after.txt
 $ diff before.txt after.txt
   3a4,5
   > 2589 0 crw-rw-rw-  1 root      wheel   18, 110 Nov 21 07:54 /dev/tty.usbmodem14101
   > 2593 0 crw-rw-rw-  1 root      wheel   18, 112 Nov 21 07:54 /dev/tty.usbmodeme2df64a32
 $
```

---
