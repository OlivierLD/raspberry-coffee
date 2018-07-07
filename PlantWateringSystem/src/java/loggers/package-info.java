package loggers;
/**
 * Contains what's needed to implement loggers.
 * Loggers are dynamically loaded, their name comes from a runtime parameter (--loggers:loggers.iot.AdafruitIOClient,loggers.text.FileLogger)
 *
 * Samples provided:
 * - File logger (loggers.text.FileLogger, generates a CSV file).
 * - IoT feeder (loggers.iot.AdafruitIOClient, feeds 2 Adafruit-IO feeds)
 *
 * To add, some time:
 * - TCP forwarder (following NMEA-Mux requirements?)
 * - WebSocket
 * - Display like a small oled screen (SSD1306)
 * - ...
 */
