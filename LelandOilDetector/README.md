# Oil Detector prototyping

The MCP3008 has its channel 0 to 7 connected.
The relay uses the GPIO pin #00 (relay 1, 2 is unused)
The FONA is connected to the Arduino, conected to the RPi with its serial cable

To run:
Start the node server on the RasPI:
```
Prompt> node server.js
```
(Ctrl+C to stop it)
It runs by default on port 9876

Then run the WebSocket feeder (that also reads the sensor and drives the relay):
```
Prompt> ./run.ws
```
(Ctrl+C to stop it)

All properties are driven by props.properties.
WebSocket URI can be customized with ws.uri=ws://localhost:9876/
Default value is ws://localhost:9876/

From any device connected on the RasPI network, reach 
http://192.168.1.1:9876/data/display.html.

The relay will turn off when the value read from the ADC means 'Oil', just before the poil reaches the pump.

![Wiring](doc/bold.wiring.png "Wiring")

## Wiring
### Arduino - FONA
| Arduino | FONA |
|:-------:|:----:|
| GND | GND |
|     | Key |
| ~3  | TX |
| 2   | RX |
| 5V  | Vin |

The Arduino is connected to the Raspberry PI with its serial cable. Usually on port `/dev/ttyACM0`.

### MCP3008 - Raspberry PI
The `MCP3008` is connect on the Raspberry PI:

| MCP3008 | Raspberry PI |
|:-------:|:-------------|
| VDD     | 3.3 VDC (#1) |
| VREF    | 3.3 VDC (#1) |
| AGND    | GND (#6)     |
| CLK     | GPIO_01 (#12) |
| DOUT    | GPIO_04 (#16) |
| DIN     | GPIO_05 (#18) |
| CS/SHDN | GPIO_06 (#22) |
| DGND    | GND (#6) |

For the connection with the probe, see the diagram. Pins `CH0` to `CH7` are available.

The momentary switch uses the 5V and the Raspberry PI's' `CE0` (#24, GPIO_10).

The relay uses the 5V and the GND, the IN connects to the Raspberry PI's GPIO_00 (#11).

![Prototype](doc/prototype.png "Prototype")
