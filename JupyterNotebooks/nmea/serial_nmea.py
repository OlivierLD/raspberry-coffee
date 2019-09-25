#!/usr/bin/env python3
#
# Main for an NMEA Parser. Read a serial port, and parse its output.
#
# May require:
# pip install pyserial
#
# Also see:
# - https://pythonhosted.org/pyserial/pyserial.html
# - https://pyserial.readthedocs.io/en/latest/pyserial.html
#
import nmea_parser as NMEAParser
import serial

DEBUG = False


def read_nmea_sentence(serial_port):
    """
    Reads the serial port until a '\n' is met.
    :param serial_port: the port to read, as returned by serial.Serial
    :return: the full NMEA String, with its EOS '\r\n'
    """
    rv = []
    while True:
        try:
            ch = serial_port.read()
        except KeyboardInterrupt as ki:
            raise ki
        if DEBUG:
            print("Read {} from Serial Port".format(ch))
        rv.append(ch)
        if ch == b'\n':
            # string = [x.decode('utf-8') for x in rv]
            string = "".join(map(bytes.decode, rv))
            if DEBUG:
                print("Returning {}".format(string))
            return string


# For tests
if __name__ == "__main__":
    # On mac, USB GPS on port /dev/tty.usbmodem14101,
    # Raspberry Pi, use /dev/ttyUSB0 or so.
    port_name = "/dev/tty.usbmodem14101"
    # port_name = "/dev/ttyS80"
    baud_rate = 4800
    port = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
    print("Let's go. Hit Ctrl+C to stop")
    while True:
        try:
            rcv = read_nmea_sentence(port)
            # print("\tReceived:" + repr(rcv))  # repr: displays also non printable characters between quotes.
            nmea_obj = NMEAParser.parse_nmea_sentence(rcv)
            try:
                if nmea_obj["type"] == 'rmc':
                    print("RMC => {}".format(rcv))
                    print("RMC => {}".format(nmea_obj))
                    if 'position' in nmea_obj['parsed']:
                        print("This is RMC: {} / {}".format(NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['latitude'], NMEAParser.NS),
                                                            NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['longitude'], NMEAParser.EW)))
                elif nmea_obj["type"] == 'gll':
                    print("GLL => {}".format(nmea_obj))
                    if 'position' in nmea_obj['parsed']:
                        print("This is GLL: {} / {}".format(NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['latitude'], NMEAParser.NS),
                                                            NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['longitude'], NMEAParser.EW)))
                else:
                    print("{} => {}".format(nmea_obj["type"], nmea_obj))
            except AttributeError as ae:
                print("AttributeError for {}".format(nmea_obj))
        except NMEAParser.NoParserException as npe:
            # absorb
            if DEBUG:
                print("- No parser, {}".format(npe))
        except KeyboardInterrupt:
            print("\n\t\tUser interrupted, exiting.")
            port.close()
            break
        except Exception as ex:
            print("\t\tOoops! {} {}".format(type(ex), ex))

    print("Bye.")
