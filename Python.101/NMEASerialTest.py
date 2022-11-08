#!/usr/bin/env python3
#
# http://www.elinux.org/Serial_port_programming
# sudo apt-get install python-serial
# or
# pip3 install pyserial
#
# Also see:
# - https://pythonhosted.org/pyserial/pyserial.html
# - https://pyserial.readthedocs.io/en/latest/pyserial.html
#
# Read Serial port, parse NMEA Data.
#
import serial
from typing import Dict  # , List, Set, Tuple, Optional

DEBUG: bool = False

#
# Basic parsers, like placeholders.
#


def gll_parser(sentence: str) -> Dict[str, str]:
    return {"gll": sentence}


def gsa_parser(sentence: str) -> Dict[str, str]:
    return {"gsa": sentence}


def rmc_parser(sentence: str) -> Dict[str, str]:
    return {"rmc": sentence}


def gga_parser(sentence: str) -> Dict[str, str]:
    return {"gga": sentence}


def vtg_parser(sentence: str) -> Dict[str, str]:
    return {"vtg": sentence}


def gsv_parser(sentence: str) -> Dict[str, str]:
    return {"gsv": sentence}


NMEA_PARSER_DICT: Dict = {
    "GLL": gll_parser,
    "GSA": gsa_parser,
    "RMC": rmc_parser,
    "GGA": gga_parser,
    "VTG": vtg_parser,
    "GSV": gsv_parser
}


def read_nmea_sentence(serial_port: serial.serialposix.Serial) -> str:
    """
    Reads the serial port until a '\n' is met.
    :param serial_port: the port, as returned by serial.Serial
    :return: the full NMEA String, with its EOL '\r\n'
    """
    rv = []
    while True:
        ch = serial_port.read()
        if DEBUG:
            print("Read {} from Serial Port".format(ch))
        rv.append(ch)
        if ch == b'\n':
            # string = [x.decode('utf-8') for x in rv]
            string = "".join(map(bytes.decode, rv))
            if DEBUG:
                print("Returning {}".format(string))
            return string


def calculate_check_sum(sentence: str) -> int:
    cs = 0
    char_array = list(sentence)
    for c in range(len(sentence)):
        cs = cs ^ ord(char_array[c])  # This is an XOR
    return cs


def valid_check_sum(sentence: str) -> bool:
    star_index = -1
    try:
        star_index = sentence.index('*')
    except Exception:
        if DEBUG:
            print("No star was found")
        return False
    cs_key = sentence[-2:]
    # print("CS Key: {}".format(cs_key))
    try:
        csk = int(cs_key, 16)
    except Exception:
        print("Invalid Hex CS Key {}".format(cs_key))
        return False

    string_to_validate = sentence[1:-3]  # drop both ends, no $, no *CS
    # print("Key in HEX is {}, validating {}".format(csk, string_to_validate))
    calculated = calculate_check_sum(string_to_validate)
    if calculated != csk:
        if DEBUG:
            print("Invalid checksum. Expected {}, calculated {}".format(csk, calculated))
        return False
    elif DEBUG:
        print("Valid Checksum 0x{:02x}".format(calculated))

    return True


def parse_nmea_sentence(sentence: str) -> Dict:
    nmea_dict = {}
    if sentence.startswith('$'):
        if sentence.endswith('\r\n'):
            sentence = sentence.strip()  # drops the \r\n
            members = sentence.split(',')
            # print("Split: {}".format(members))
            sentence_prefix = members[0]
            if len(sentence_prefix) == 6:
                # print("Sentence ID: {}".format(sentence_prefix))
                valid = valid_check_sum(sentence)
                if not valid:
                    raise Exception('Invalid checksum')
                else:
                    sentence_id = sentence_prefix[3:]
                    parser = None
                    for key in NMEA_PARSER_DICT:
                        if key == sentence_id:
                            parser = NMEA_PARSER_DICT[key]
                            break
                    if parser is None:
                        raise Exception("No parser exists for {}".format(sentence_id))
                    else:
                        print("Proceeding... {}".format(sentence_id))
                        obj = parser(sentence)
                        print("Parsed: {}".format(obj))
            else:
                raise Exception('Incorrect sentence prefix "{}". Should be 6 character long.'.format(sentence_prefix))
        else:
            raise Exception('Sentence should end with \\r\\n')
    else:
        raise Exception('Sentence should start with $')
    return nmea_dict


# On mac, USB GPS on port /dev/tty.usbmodem14101,
# Raspberry Pi, use /dev/ttyUSB0 or so.
port_name: str = "/dev/tty.usbmodem141101"
baud_rate: int = 4800
# port_name = "/dev/ttyACM0"
# baud_rate = 115200
port: int = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
print("Let's go. Hit Ctrl+C to stop")
keep_looping: bool = True
while keep_looping:
    rcv: str = read_nmea_sentence(port)
    print("\tReceived:" + repr(rcv))  # repr: displays also non-printable characters between quotes.
    try:
        nmea_obj = parse_nmea_sentence(rcv)
    except KeyboardInterrupt:
        keep_looping = False
        print("Exiting at user's request")
    except Exception as ex:
        print("Oops! {}".format(ex))

print("\nBye!")
