#!/usr/bin/env python3
#
# A Python NMEA Parser
#
DEBUG = False


def sex_to_dec(deg_str, min_str):
    try:
        degrees = float(deg_str)
        minutes = float(min_str)
        minutes *= (10.0 / 6.0)
        ret = degrees + minutes / 100.0
        return ret
    except ValueError:
        raise Exception("Bad numbers [{}] [{}]".format(deg_str, min_str))


def gll_parser(sentence, valid=False):
    parsed = {}
    if valid:
        # Validation (Checksum, etc) goes here
        print("Implement validation here")
    data = sentence[:-3].split(',')
    print("Parsing GLL, {} elements".format(len(data)))
    return {"type": "gll", "parsed": parsed}


def txt_parser(sentence, valid=False):
    parsed = {}
    if valid:
        # Validation (Checksum, etc) goes here
        print("Implement validation here")
    data = sentence[:-3].split(',')
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


def gsa_parser(sentence, valid=False):
    return {"type": "gsa", "parsed": sentence}


def rmc_parser(sentence, valid=False):
    parsed = {}
    if valid:
        # Validation (Checksum, etc) goes here
        print("Implement validation here")
    data = sentence[:-3].split(',')
    # RMC Structure is
    #  0      1      2 3        4 5         6 7     8     9      10    11
    #  $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A
    #         |      | |        | |         | |     |     |      |     |
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

    if len(data[3]) > 0 and len(data[5]) > 0:
        pos = {}
        lat_deg = data[3][0:2]
        lat_min = data[3][2:]
        lat = sex_to_dec(lat_deg, lat_min)
        if data[4] == 'S':
            lat *= -1
        pos["latitude"] = lat
        lng_deg = data[5][0:3]
        lng_min = data[3][3:]
        lng = sex_to_dec(lng_deg, lng_min)
        if data[6] == 'W':
            lng *= -1
        pos["longitude"] = lng

        parsed["position"] = pos

    return {"type": "rmc", "parsed": parsed}


NMEA_PARSER_DICT = {
    "TXT": txt_parser,
    "GLL": gll_parser,
    "GSA": gsa_parser,
    "RMC": rmc_parser
}


def calculate_check_sum(sentence):
    cs = 0
    char_array = list(sentence)
    for c in range(len(sentence)):
        cs = cs ^ ord(char_array[c])  # This is an XOR
    return cs


def valid_check_sum(sentence):
    try:
        _ = sentence.index('*')
    except Exception:
        if DEBUG:
            print("No star was found in the NMEA string")
        return False
    cs_key = sentence[-2:]
    # print("CS Key: {}".format(cs_key))
    try:
        csk = int(cs_key, 16)
    except Exception:
        if DEBUG:
            print("Invalid Hex CS Key {}".format(cs_key))
        return False

    string_to_validate = sentence[1:-3]  # no $, no *CS
    # print("Key in HEX is {}, validating {}".format(csk, string_to_validate))
    calculated = calculate_check_sum(string_to_validate)
    if calculated != csk:
        if DEBUG:
            print("Invalid checksum. Expected {}, calculated {}".format(csk, calculated))
        return False
    elif DEBUG:
        print("Valid Checksum 0x{:02x}".format(calculated))

    return True


def parse_nmea_sentence(sentence):
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
                        raise Exception("No parser exists (yet) for {}".format(sentence_id))
                    else:
                        # print("Proceeding... {}".format(sentence_id))
                        obj = parser(sentence)
                        # print("Parsed: {}".format(obj))
                        return obj
            else:
                raise Exception('Incorrect sentence prefix "{}". Should be 6 character long.'.format(sentence_prefix))
        else:
            raise Exception('Sentence should end with \\r\\n')
    else:
        raise Exception('Sentence should start with $')
    return nmea_dict


# For tests
if __name__ == "__main__":
    print("---------------------")
    print("{} running as main".format(__name__))
    print("---------------------")
    samples = [
        "$IIRMC,092551,A,1036.145,S,15621.845,W,04.8,317,,10,E,A*0D\r\n",
        "$IIMWV,088,T,14.34,N,A*27\r\n",
        "$IIVWR,148.,L,02.4,N,01.2,M,04.4,K*XX\r\n",
        "$IIVTG,054.7,T,034.4,M,005.5,N,010.2,K,A*XX\r\n",
        "$GPRMC,183333.000,A,4047.7034,N,07247.9938,W,0.66,196.21,150912,,,A*7C\r\n"
    ]
    # akeu = sex_to_dec("12", "34.XX")
    for sentence in samples:
        try:
            nmea_obj = parse_nmea_sentence(sentence)
            print("=> {}".format(nmea_obj))
        except Exception as ex:
            print("Ooops! {}".format(ex))
else:
    print("---------------------")
    print("{} NOT running as main, probably imported.".format(__name__))
    print("---------------------")
