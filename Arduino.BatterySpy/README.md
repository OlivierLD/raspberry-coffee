### Warning
This one is not strictly related to the Raspberry PI, it only involves an Arduino, and a FONA. No Raspberry PI, no Java.

The goal of this project is to remotely monitor the state of a battery bank (on a boat, an RV, whatever).
This monitoring is done through SMS, for the request, and for the response. As such, it can be done from most
smart-phones.

But still, as it relates to the `adc.sample.BatteryMonitor.java`, this could be something to look at.
The hardware part is much simpler, as the Arduino has analog pins, it does not require an ADC.
