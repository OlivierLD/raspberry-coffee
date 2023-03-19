# Java Clients

Contains some Swing components for display.

- TCP Clients
  - `clients.tcp.NMEATCPClient`
    - No GUI, console output.
    - Requires an NMEA-multiplexer to be running, with a TCP forwarder on port `7001`.
  - `clients.tcp.NMEATCPSwing101`
    - Same as above, with a (very basic) Swing GUI.  
      ![Raw Swing Display](./RawSwing.png)
  - `clients.tcp.NMEATCPSwingHeading`
    - Display the Heading, using a custom Swing Component.  
      ![Swing Heading](./SwingHeading.png)
  - `clients.tcp.NMEATCPSwingMultiDisplay`
    - WiP. Several widgets to scroll through, in the same frame (HDG, TWD, UTC, BSP, POS)

      | Boat Speed                 | True Wind Direction        | UTC Clock                  |
      |:--------------------------:|:--------------------------:|:--------------------------:|
      | ![BSP](./Multiple.BSP.png) | ![TWD](./Multiple.TWD.png) | ![UTC](./Multiple.UTC.png) | 

---
