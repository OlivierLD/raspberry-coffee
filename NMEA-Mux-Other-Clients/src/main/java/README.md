# Java Clients

Contains some Swing components for display.

- TCP Clients
  - `clients.tcp.NMEATCPClient`
    - No GUI, console output.
    - Requires an NMEA-multiplexer to be running, with a TCP forwarder on port `7001`.
  - `clients.tcp.NMEATCPSwing101`
    - Same as above, with a (very basic) Swing GUI.  
      ![Raw Swing Display](./RawSwing.png)

---
