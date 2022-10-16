# Some Python Client samples

In case the NMEA-multiplexer (and its extensions) is good enough for you, but if Java is
not 100% on your radar, you might want to use a language like Python to 
implement your own extensions or satellites... We want here to provide some samples of such ideas.

- `tcp_mux_client.py`
  - Requires a Mux to be forwarding NMEA data on TCP.
  - Acts as a TCP client, the NMEA Mux is acting as a TCP server, pushing data to all its TCP clients.
  - This example just echoes whatever is received through the TCP channel.
  - Can be used as a skeleton, for Python devices (like e-Ink or Papirus, samples available in this repo).

... More to come (REST, WebSockets, tkinter, guizero, etc).

---
