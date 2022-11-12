#!/usr/bin/env python3

def sex_to_dec(degrees: str, minutes: str) -> float:
    """
    Convert a sexagesimal number (degrees, decimal minutes) into a decimal number
    :param degrees: degrees, usually an int.
    :param minutes: decimal minutes
    :return: the converted value
    """
    try:
        deg: float = float(degrees)
        min: float = float(minutes)
        min *= (10.0 / 6.0)
        ret = deg + (min / 100.0)
        return ret
    except Exception as error:
        raise Exception(f"Cannot convert {degrees} {minutes}, {repr(error)}")
