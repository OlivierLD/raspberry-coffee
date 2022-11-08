#!/usr/bin/env python3

import checksum  # local script
import utils     # local script
import json
from typing import Dict  # , List, Set, Tuple, Optional

NMEA_EOS: str = '\r\n'
DEBUG: bool = False


def rmc_parser(sentence: str) -> Dict[str, Dict]:
    """
    RMC Structure is
                                                                         12
              1      2 3        4 5         6 7     8     9      10    11
       $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,T*6A
              |      | |        | |         | |     |     |      |     | |
              |      | |        | |         | |     |     |      |     | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator. Not mandatory
              |      | |        | |         | |     |     |      |     Variation sign
              |      | |        | |         | |     |     |      Variation value
              |      | |        | |         | |     |     Date DDMMYY (see rmc.date.offset property)
              |      | |        | |         | |     COG
              |      | |        | |         | SOG
              |      | |        | |         Longitude Sign
              |      | |        | Longitude Value
              |      | |        Latitude Sign
              |      | Latitude value
              |      Active or Void
              UTC
    
    """
    RMC_UTC: int = 1
    RMC_ACTIVE_VOID: int = 2
    RMC_LATITUDE_VALUE: int = 3
    RMC_LATITUDE_SIGN: int = 4
    RMC_LONGITUDE_VALUE: int = 5
    RMC_LONGITUDE_SIGN: int = 6
    RMC_SOG: int = 7
    RMC_COG: int = 8
    RMC_DDMMYY: int = 9
    RMC_VARIATION_VALUE: int = 10
    RMC_VARIATION_SIGN: int = 11
    RMC_TYPE: int = 12

    rmc_dict = {}
    sentence = sentence.strip()  # drops the \r\n
    members: list = sentence.split(',')

    if len(members[RMC_ACTIVE_VOID]) > 0 and members[RMC_ACTIVE_VOID] == 'A':
        if len(members[RMC_UTC]) > 0 and len(members[RMC_DDMMYY]) > 0:
            year: int = int(members[RMC_DDMMYY][4:])
            if year < 50:
                year = 2000 + year
            else:
                year = 1900 + year
            month: int = int(members[RMC_DDMMYY][2:4])
            day: int = int(members[RMC_DDMMYY][0:2])
            hour: int = int(members[RMC_UTC][0:2])
            minute: int = int(members[RMC_UTC][2:4])
            second: float = float(members[RMC_UTC][4:])
            rmc_dict["utc"] = {
                "year": year,
                "month": month,
                "day": day,
                "hour": hour,
                "minute": minute,
                "second": second
            }
        if len(members[RMC_LATITUDE_VALUE]) > 0 and len(members[RMC_LATITUDE_SIGN]) > 0 and len(members[RMC_LONGITUDE_VALUE]) > 0 and len(members[RMC_LONGITUDE_SIGN]) > 0:
            deg: str = members[RMC_LATITUDE_VALUE][0:2]
            min: str = members[RMC_LATITUDE_VALUE][2:]
            lat: float = utils.sex_to_dec(deg, min)
            if members[RMC_LATITUDE_SIGN] == 'S':
                lat = -lat
            deg = members[RMC_LONGITUDE_VALUE][0:3]
            min = members[RMC_LONGITUDE_VALUE][3:]
            lng: float = utils.sex_to_dec(deg, min)
            if members[RMC_LONGITUDE_SIGN] == 'W':
                lng = -lng
            rmc_dict["pos"] = {
                "latitude": lat,
                "longitude": lng
            }
        if len(members[RMC_SOG]) > 0:
            sog: float = float(members[RMC_SOG])
            rmc_dict["sog"] = sog
        if len(members[RMC_COG]) > 0:
            cog: float = float(members[RMC_COG])
            rmc_dict["cog"] = cog
        if len(members[RMC_VARIATION_VALUE]) > 0:
            variation: float = float(members[RMC_VARIATION_VALUE])
            if members[RMC_VARIATION_SIGN] == 'W':
                variation = -variation
            rmc_dict["variation"] = variation

    return {"rmc": rmc_dict}


def gsa_parser(sentence: str) -> Dict[str, str]:
    return {"gsa": sentence}


def gll_parser(sentence: str) -> Dict[str, str]:
    return {"gll": sentence}


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


def parse_nmea_sentence(sentence: str) -> Dict:
    nmea_dict = {}
    if sentence.startswith('$'):
        if sentence.endswith(NMEA_EOS):
            sentence = sentence.strip()  # drops the \r\n
            members: list  = sentence.split(',')
            # print(f"members is a {type(members)}")
            if DEBUG:
                print("Split: {}".format(members))
            sentence_prefix: str = members[0]  # $TTIII
            if len(sentence_prefix) == 6:
                # print("Sentence ID: {}".format(sentence_prefix))
                valid: bool = checksum.valid_check_sum(sentence)
                if not valid:
                    raise Exception('Invalid checksum')
                else:
                    sentence_id: str = sentence_prefix[3:]
                    parser = None
                    for key in NMEA_PARSER_DICT:
                        if key == sentence_id:
                            parser = NMEA_PARSER_DICT[key]  # TODO type 'function'
                            # print(f"parser is a {type(parser)}")
                            break
                    if parser is None:
                        raise Exception("No parser exists for {}".format(sentence_id))
                    else:
                        if DEBUG:
                            print("Proceeding... {}".format(sentence_id))
                        nmea_dict = parser(sentence)
                        if DEBUG:
                            print("Parsed: {}".format(nmea_dict))
            else:
                raise Exception('Incorrect sentence prefix "{}". Should be 6 character long.'.format(sentence_prefix))
        else:
            raise Exception('Sentence should end with \\r\\n')
    else:
        raise Exception('Sentence should start with $')
    return nmea_dict


# This is for tests
if __name__ == '__main__':
    nmea: str = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A"
    # nmea: str = "$GPRMC,170000.00,A,3744.79693,N,12223.30420,W,0.052,,200621,,,D*62"
    parsed: Dict = parse_nmea_sentence(nmea + NMEA_EOS)
    print(f"Parsed RMC: {parsed}")
    print(f"Beautified:\n{json.dumps(parsed, sort_keys=False, indent=2)}")
