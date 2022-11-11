"""
Checksum operations
"""

DEBUG: bool = False


def calculate_check_sum(sentence: str) -> int:
    if DEBUG:
        print(f"Calculating CheckSum for {sentence}")
    cs: int = 0
    char_array = list(sentence)
    for c in range(len(sentence)):
        cs = cs ^ ord(char_array[c])  # This is an XOR
        if DEBUG:
            print("Checksum is now 0x{:02x}".format(cs))
    if DEBUG:
        print("Final Checksum 0x{:02x}".format(cs))
    return cs


def valid_check_sum(sentence: str) -> bool:
    star_index: int = -1
    try:
        star_index = sentence.index('*')
    except Exception:
        if DEBUG:
            print("No star was found")
        return False
    cs_key: str = sentence[-2:]
    if DEBUG:
        print("CS Key: {}, from {}".format(cs_key, sentence))
    try:
        csk: int = int(cs_key, 16)
        if DEBUG:
            print("Calculated CheckSum {:02x}".format(csk))
    except Exception:
        print("Invalid Hex CS Key {}".format(cs_key))
        return False

    string_to_validate: str = sentence[1:-3]  # drop both ends, no $, no *CS
    # print("Key in HEX is {}, validating {}".format(csk, string_to_validate))
    calculated: int = calculate_check_sum(string_to_validate)
    if calculated != csk:
        if DEBUG:
            print("Invalid checksum. Expected {}, calculated {}".format(hex(csk).split('x')[-1], hex(calculated).split('x')[-1]))
        return False
    elif DEBUG:
        print("Valid Checksum 0x{:02x}".format(calculated))

    return True

