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
import sys
import nmea_parser
import serial

verbose = False
log_file_name = ""
log_file = None
serial_port_name = "/dev/tty.usbmodem14101"

LOG_PRM = '--log:'
SERIAL_PORT_PRM = '--serial:'
DEBUG_PRM = '--debug'
HELP_PRM = '--help'

for arg in sys.argv:
    if arg[:len(LOG_PRM)] == LOG_PRM:
        log_file_name = arg[len(LOG_PRM):]
    elif arg[:len(SERIAL_PORT_PRM)] == SERIAL_PORT_PRM:
        serial_port_name = arg[len(SERIAL_PORT_PRM):]
    elif arg[:len(DEBUG_PRM)] == DEBUG_PRM:
        verbose = True
    elif arg[:len(HELP_PRM)] == HELP_PRM:
        print("Usage is\n\t{} [{}] [{}filename] [{}] [{}/dev/tty.whatever]\n".format(sys.argv[0], HELP_PRM, LOG_PRM,
                                                                                     DEBUG_PRM,
                                                                                     SERIAL_PORT_PRM))
        sys.exit(0)


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
        if verbose:
            print("Read {} from Serial Port".format(ch))
        rv.append(ch)
        if ch == b'\n':
            # string = [x.decode('utf-8') for x in rv]
            string = "".join(map(bytes.decode, rv))
            if verbose:
                print("Returning {}".format(string))
            return string


# For tests, or logging.
if __name__ == "__main__":
    # On mac, USB GPS on port /dev/tty.usbmodem14101,
    # Raspberry Pi, use /dev/ttyUSB0 or so.
    port_name = serial_port_name
    # port_name = "/dev/ttyS80"
    baud_rate = 4800
    port = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
    print("Let's go. Hit Ctrl+C to stop")
    if len(log_file_name) > 0:
        log_file = open(log_file_name, "w")
        print("Logging in {}".format(log_file_name))

    while True:
        try:
            rcv = read_nmea_sentence(port)
            if log_file is not None:
                log_file.write(rcv)
                log_file.flush()
            # print("\tReceived:" + repr(rcv))  # repr: displays also non printable characters between quotes.
            nmea_obj = nmea_parser.parse_nmea_sentence(rcv)
            try:
                if nmea_obj["type"] == 'rmc':
                    print("RMC => {}".format(rcv))
                    print("RMC => {}".format(nmea_obj))
                    if 'position' in nmea_obj['parsed']:
                        print("This is RMC: {} / {}".format(
                            nmea_parser.dec_to_sex(nmea_obj['parsed']['position']['latitude'], nmea_parser.NS),
                            nmea_parser.dec_to_sex(nmea_obj['parsed']['position']['longitude'], nmea_parser.EW)))
                elif nmea_obj["type"] == 'gll':
                    print("GLL => {}".format(nmea_obj))
                    if 'position' in nmea_obj['parsed']:
                        print("This is GLL: {} / {}".format(
                            nmea_parser.dec_to_sex(nmea_obj['parsed']['position']['latitude'], nmea_parser.NS),
                            nmea_parser.dec_to_sex(nmea_obj['parsed']['position']['longitude'], nmea_parser.EW)))
                else:
                    print("{} => {}".format(nmea_obj["type"], nmea_obj))
            except AttributeError as ae:
                print("AttributeError for {}".format(nmea_obj))
        except nmea_parser.NoParserException as npe:
            # absorb
            if verbose:
                print("- No parser, {}".format(npe))
        except KeyboardInterrupt:
            print("\n\t\tUser interrupted, exiting.")
            port.close()
            if log_file is not None:
                log_file.close()
            break
        except Exception as ex:
            print("\t\tOoops! {} {}".format(type(ex), ex))

    print("Bye.")
