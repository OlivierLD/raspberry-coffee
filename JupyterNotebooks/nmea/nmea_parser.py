#!/usr/bin/env python3
#
# A Python NMEA Parser. WIP.
#
__author__ = 'Olivier LeDiouris'
__version__ = '0.0.5'

import datetime
import math

DEBUG = False


class InvalidChecksumException(Exception):
    """Raised when checksum is invalid"""
    pass


class NoParserException(Exception):
    """Raised when parser is not implemented"""
    pass


def sex_to_dec(deg_str, min_str):
    """
    Sexagesimal to decimal
    :param deg_str: degrees value (as string containing an int) like '12'
    :param min_str: minutes value (as a string containing a float) like '45.00'
    :return: decimal value, like 12.75 here.
    """
    try:
        degrees = float(deg_str)
        minutes = float(min_str)
        minutes *= (10.0 / 6.0)
        ret = degrees + minutes / 100.0
        return ret
    except ValueError:
        raise Exception("Bad numbers [{}] [{}]".format(deg_str, min_str))


NS = 0
EW = 1


def dec_to_sex(value, type):
    abs_val = abs(value)  # (-value) if (value < 0) else value
    int_value = math.floor(abs_val)
    i = int(int_value)
    dec = abs_val - int_value
    dec *= 60
    sign = "N"
    if type == NS:
        if value < 0:
            sign = "S"
    else:
        if value < 0:
            sign = "W"
        else:
            sign = "E"
    formatted = "{} {}\272{:0.2f}'".format(sign, i, dec)
    return formatted


def gll_parser(nmea_sentence, valid=False):
    parsed = {}
    if valid:
        # Validation (Checksum, etc) goes here
        ok = valid_check_sum(nmea_sentence)
        if not ok:
            raise InvalidChecksumException('Invalid checksum for {}'.format(nmea_sentence))
    data = nmea_sentence[:-3].split(',')
    # Structure is
    #  0     1       2 3       4 5         6
    # $aaGLL,llll.ll,a,gggg.gg,a,hhmmss.ss,A*hh
    #        |       | |       | |         |
    #        |       | |       | |         A: data valid (Active), V: void
    #        |       | |       | UTC of position
    #        |       | |       Long sign: E / W
    #        |       | Longitude
    #        |       Lat sign: N / S
    #        Latitude

    if DEBUG:
        print("Parsing GLL, {} elements".format(len(data)))
    parsed["valid"] = "true" if (data[6] == 'A') else "false"
    # Position
    if len(data[1]) > 0 and len(data[3]) > 0:
        pos = {}
        lat_deg = data[1][0:2]
        lat_min = data[1][2:]
        lat = sex_to_dec(lat_deg, lat_min)
        if data[2] == 'S':
            lat *= -1
        pos["latitude"] = lat
        lng_deg = data[3][0:3]
        lng_min = data[3][3:]
        lng = sex_to_dec(lng_deg, lng_min)
        if data[4] == 'W':
            lng *= -1
        pos["longitude"] = lng

        parsed["position"] = pos

    # Time (UTC)
    if len(data[5]) > 0:
        utc = float(data[5])
        hours = int(utc / 10_000)
        mins = int((utc - (10_000 * hours)) / 100)
        secs = (utc % 100)
        microsecs = (secs - int(secs)) * 1_000_000
        time = datetime.time(hours, mins, int(secs), int(microsecs), tzinfo=datetime.timezone.utc)
        if DEBUG:
            print(time.strftime("%H:%M:%S %z %Z, also %c"))
        parsed["utc-time"] = time
    return {"type": "gll", "parsed": parsed}


def txt_parser(nmea_sentence, valid=False):
    """
    This is not an NMEA Standard, but used some times (by my small USB-GPS U-blox7
    :param nmea_sentence: The NMEA Sentence to parse, starting with '$', ending with '*CS' (CS is the CheckSum).
    :param valid: default False, will perform sentence validation if True
    :return: A dict, like { 'type': 'txt', 'parsed': { ... parsed object ... }}
    """
    parsed = {}
    if valid:
        # Validation (Checksum, etc) goes here
        print("Implement validation here")
    data = nmea_sentence[:-3].split(',')
    # TXT Structure (non NMEA standard):
    # 0      1  2  3  4
    # $GPTXT,01,01,02,ANTSTATUS=OK*3B
    #        |  |  |  |
    #        |  |  |  Content
    #        |  |  ?
    #        |  ?
    #        ?
    #
    if len(data) >= 4:
        parsed["content"] = data[4]
    return {"type": "txt", "parsed": parsed}


def gsa_parser(nmea_sentence, valid=False):
    if valid:
        # Validation (Checksum, etc) goes here
        print("Implement validation here")
    return {"type": "gsa", "parsed": nmea_sentence}


def rmc_parser(nmea_sentence, valid=False):
    """
    NMEA Standard
    :param nmea_sentence: The NMEA Sentence to parse, starting with '$', ending with '*CS' (CS is the CheckSum).
    :param valid: default False, will perform sentence validation if True
    :return: A dict, like { 'type': 'rmc', 'parsed': { ... parsed object ... }}
    """
    if DEBUG:
        print("Parsing {}".format(nmea_sentence))
    parsed = {}
    if valid:
        # Validation (Checksum, etc) goes here
        ok = valid_check_sum(nmea_sentence)
        if not ok:
            raise InvalidChecksumException('Invalid checksum for {}'.format(nmea_sentence))
    data = nmea_sentence[:-3].split(',')  # Drop the CheckSum
    # RMC Structure is
    #                                                                   12
    #  0      1      2 3        4 5         6 7     8     9      10    11
    #  $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,A*6A
    #         |      | |        | |         | |     |     |      |     | |
    #         |      | |        | |         | |     |     |      |     | Type: A=autonomous,
    #         |      | |        | |         | |     |     |      |     |       D=differential,
    #         |      | |        | |         | |     |     |      |     |       E=Estimated,
    #         |      | |        | |         | |     |     |      |     |       N=not valid,
    #         |      | |        | |         | |     |     |      |     |       S=Simulator
    #         |      | |        | |         | |     |     |      |     Variation sign
    #         |      | |        | |         | |     |     |      Variation value
    #         |      | |        | |         | |     |     Date DDMMYY (see rmc.date.offset property)
    #         |      | |        | |         | |     COG
    #         |      | |        | |         | SOG
    #         |      | |        | |         Longitude Sign
    #         |      | |        | Longitude Value
    #         |      | |        Latitude Sign
    #         |      | Latitude value
    #         |      Active or Void
    #         UTC
    #
    if DEBUG:
        print("Parsing RMC, {} items".format(len(data)))
    parsed["valid"] = "true" if (data[2] == 'A') else "false"
    # Time and Date
    if len(data[1]) > 0:
        utc = float(data[1])
        hours = int(utc / 10_000)
        mins = int((utc - (10_000 * hours)) / 100)
        secs = (utc % 100)
        if len(data[9]) > 0:
            day = int(data[9][:2])
            month = int(data[9][2:4])
            year = int(data[9][4:6])
            if year > 50:
                year += 1_900
            else:
                year += 2_000
            date = datetime.datetime(year, month, day, hours, mins, int(secs), 0, tzinfo=datetime.timezone.utc)
            if DEBUG:
                print(date.strftime("%A %d %B %Y %H:%M:%S %z %Z, also %c"))
            parsed["utc-date"] = date

    # Position
    if len(data[3]) > 0 and len(data[5]) > 0:
        pos = {}
        lat_deg = data[3][0:2]
        lat_min = data[3][2:]
        lat = sex_to_dec(lat_deg, lat_min)
        if data[4] == 'S':
            lat *= -1
        pos["latitude"] = lat
        lng_deg = data[5][0:3]
        lng_min = data[5][3:]
        lng = sex_to_dec(lng_deg, lng_min)
        if data[6] == 'W':
            lng *= -1
        pos["longitude"] = lng

        parsed["position"] = pos

    # SOG
    if len(data[7]) > 0:
        sog = float(data[7])
        parsed["sog"] = sog

    # COG
    if len(data[8]) > 0:
        cog = float(data[8])
        parsed["cog"] = cog

    # Mag Decl. (variation, actually)
    if len(data[10]) > 0 and len(data[11]) > 0:
        decl = float(data[10])
        if "W" == data[11]:
            decl = -decl
        parsed["declination"] = decl

    # Extra field, recently added to the spec
    if data[12] is not None:
        # The value can be A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator.
        rmc_type = "None"
        if data[12] == 'A':
            rmc_type = "autonomous"
        elif data[12] == 'D':
            rmc_type = "differential"
        elif data[12] == 'E':
            rmc_type = "estimated"
        elif data[12] == 'N':
            rmc_type = "not valid"
        elif data[12] == 'S':
            rmc_type = "simulator"
        parsed["type"] = rmc_type

    return {"type": "rmc", "parsed": parsed}


# Populate this dict as parsers are available
NMEA_PARSER_DICT = {
    "TXT": txt_parser,
    "GLL": gll_parser,
    "GSA": gsa_parser,
    "RMC": rmc_parser
}


def calculate_check_sum(nmea_sentence):
    """
    Calculates the NMEA CheckSum
    :param nmea_sentence: NMEA Sentence to calculate the checksum of,
                          with NO *CS at the end, and no '$' at the beginning
    :return: the expected checksum, as in int
    """
    cs = 0
    char_array = list(nmea_sentence)
    for c in range(len(nmea_sentence)):
        cs = cs ^ ord(char_array[c])  # This is an XOR
    return cs


def valid_check_sum(nmea_sentence):
    """
    Validates an NMEA Sentence
    :param nmea_sentence: Full one, with '$' at the start, and checksumn at the end
    :return: True if valid, False if not
    """
    try:
        _ = nmea_sentence.index('*')
    except Exception as exception:
        if DEBUG:
            print("No star was found in the NMEA string, {}".format(exception))
        return False
    cs_key = nmea_sentence[-2:]  # the 2 last characters, should be an hexadecimal value
    # print("CS Key: {}".format(cs_key))
    try:
        csk = int(cs_key, 16)  # int value
    except Exception as exception:
        if DEBUG:
            print("Invalid Hex CS Key {}, {}".format(cs_key, exception))
        return False

    string_to_validate = nmea_sentence[1:-3]  # no $, no *CS
    # print("Key in HEX is {}, validating {}".format(csk, string_to_validate))
    calculated = calculate_check_sum(string_to_validate)
    if calculated != csk:
        if DEBUG:
            print("Invalid checksum. Expected {}, calculated {}".format(csk, calculated))
        return False
    elif DEBUG:
        print("Valid Checksum 0x{:02x}".format(calculated))

    return True


def parse_nmea_sentence(nmea_sentence):
    if nmea_sentence.startswith('$'):
        if nmea_sentence.endswith('\r\n'):
            nmea_sentence = nmea_sentence.strip()  # drops the \r\n
            members = nmea_sentence.split(',')
            # print("Split: {}".format(members))
            sentence_prefix = members[0]
            if len(sentence_prefix) == 6:
                # print("Sentence ID: {}".format(sentence_prefix))
                valid = valid_check_sum(nmea_sentence)
                if not valid:
                    raise InvalidChecksumException('Invalid checksum for {}'.format(nmea_sentence))
                else:
                    sentence_id = sentence_prefix[3:]
                    parser = None
                    for key in NMEA_PARSER_DICT:
                        if key == sentence_id:
                            parser = NMEA_PARSER_DICT[key]
                            break
                    if parser is None:
                        raise NoParserException("No parser exists (yet) for {}".format(sentence_id))
                    else:
                        # print("Proceeding... {}".format(sentence_id))
                        obj = parser(nmea_sentence)
                        # print("Parsed: {}".format(obj))
                        return obj
            else:
                raise Exception('Incorrect sentence prefix "{}". Should be 6 character long.'.format(sentence_prefix))
        else:
            raise Exception('Sentence should end with \\r\\n')
    else:
        raise Exception('Sentence should start with $')


# For tests
if __name__ == "__main__":

    print("Lat: {} => {}".format(37.748911666666665, dec_to_sex(37.748911666666665, NS)))
    print("Lng: {} => {}".format(-122.5071295, dec_to_sex(-122.5071295, EW)))

    print("------------------------------")
    print("{} running as main (for tests)".format(__name__))
    print("------------------------------")
    samples = [
        "$IIRMC,092551,A,1036.145,S,15621.845,W,04.8,317,,10,E,A*0D\r\n",
        "$IIMWV,088,T,14.34,N,A*27\r\n",
        "$IIVWR,148.,L,02.4,N,01.2,M,04.4,K*XX\r\n",
        "$IIVTG,054.7,T,034.4,M,005.5,N,010.2,K,A*XX\r\n",
        "$GPRMC,183333.000,A,4047.7034,N,07247.9938,W,0.66,196.21,150912,,,A*7C\r\n",
        "$GPTXT,01,01,02,u-blox ag - www.u-blox.com*50\r\n",
        "$IIGLL,3739.854,N,12222.812,W,014003,A,A*49\r\n",
        "$GPRMC,012047.00,A,3744.93470,N,12230.42777,W,0.035,,030519,,,D*61\r\n"  # Returned by the U-blox7
    ]
    # akeu = sex_to_dec("12", "34.XX")
    for sentence in samples:
        try:
            nmea_obj = parse_nmea_sentence(sentence)
            print("=> {}".format(nmea_obj))
            if nmea_obj["type"] == 'rmc':
                print("This is RMC: {} / {}".format(dec_to_sex(nmea_obj['parsed']['position']['latitude'], NS),
                                                    dec_to_sex(nmea_obj['parsed']['position']['longitude'], EW)))
            elif nmea_obj["type"] == 'gll':
                print("This is GLL: {} / {}".format(dec_to_sex(nmea_obj['parsed']['position']['latitude'], NS),
                                                    dec_to_sex(nmea_obj['parsed']['position']['longitude'], EW)))
        except Exception as ex:
            print("Ooops! {}".format(ex))
else:
    print("---------------------")
    print("{} NOT running as main, probably imported.".format(__name__))
    print("---------------------")
