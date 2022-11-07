
from typing import Dict  # , List, Set, Tuple, Optional

DEBUG: bool = False


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

