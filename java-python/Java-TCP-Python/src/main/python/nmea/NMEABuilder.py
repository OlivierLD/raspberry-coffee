#!/usr/bin/env python3

import checksum  # local script
import prefixes  # local script
from datetime import datetime, timezone
from typing import Dict  # , List, Set, Tuple, Optional


DEBUG: bool = False


def build_ZDA(utc_ms: int = None) -> str:
    """
    Builds the ZDA sentence for given timestamp.
    :param utc_ms: if provided, in ms !!! (python default is seconds)
    :return: the NMEA ZDA sentence

    Structure is:
    $GPZDA,hhmmss.ss,dd,mm,yyyy,xx,yy*CC
    $GPZDA,201530.00,04,07,2002,00,00*60
           |         |  |  |    |  |
           |         |  |  |    |  local zone minutes 0..59
           |         |  |  |    local zone hours -13..13
           |         |  |  year
           |         |  month
           |         day
           HrMinSec(UTC)
    """
    sentence: str = f"{prefixes.DEVICE_PREFIX}ZDA,"

    if utc_ms is None:
        # Take system time instead
        utc_ms = datetime.now(timezone.utc).timestamp() * 1_000  # System "UTC epoch" in ms

    dt_object = datetime.fromtimestamp(utc_ms / 1_000, tz=timezone.utc)  # <- Aha !!
    fmt_date_time: str = dt_object.strftime("%H%M%S.00,%d,%m,%Y")

    if DEBUG:
        print("dt_object =", dt_object, " - ", fmt_date_time)
        print("type(dt_object) =", type(dt_object))

    sentence += (fmt_date_time + ",00,00")  # Et hop !

    cs: int = checksum.calculate_check_sum(sentence)
    str_cs: str = hex(cs).split('x')[-1]  # Just the hex part (no '0x' prefix)
    while len(str_cs) < 2:
        str_cs = '0' + str_cs
    sentence += ("*" + str_cs.upper())

    return "$" + sentence


def build_MTA(temperature: float) -> str:
    """
    Build the MTA String, for the given temperature.
    """
    sentence: str = f"{prefixes.DEVICE_PREFIX}MTA,"
    sentence += f"{temperature:0.1f},C"
    cs: int = checksum.calculate_check_sum(sentence)
    str_cs: str = f"{cs:02X}"  # Should be 2 character long, in upper case.
    while len(str_cs) < 2:
        str_cs = '0' + str_cs
    sentence += ("*" + str_cs.upper())

    return "$" + sentence


def build_MMB(mb_pressure: float) -> str:
    """
    Build MMB sentence. 
    mbPressure: pressure, in mb
    """
    sentence: str = f"{prefixes.DEVICE_PREFIX}MMB,"
    sentence += f"{mb_pressure / 33.8600:0.4f},I,"   # Inches of Hg
    sentence += f"{mb_pressure / 1_000:0.4f},B"      # Bars. 1 mb = 1 hPa

    cs: int = checksum.calculate_check_sum(sentence)
    str_cs: str = f"{cs:02X}"  # Should be 2 character long, in upper case.
    while len(str_cs) < 2:
        str_cs = '0' + str_cs
    sentence += ("*" + str_cs.upper())

    return "$" + sentence


def xdr_value_to_str_5_dec(value: float) -> str:
    return f"{value:0.5f}"


def xdr_value_to_str_4_dec(value: float) -> str:
    return f"{value:0.4f}"


def xdr_value_to_str_1_dec(value: float) -> str:
    return f"{value:0.1f}"


def xdr_value_to_str_no_dec(value: float) -> str:
    return f"{value:0.0f}"


def xdr_default_fmt(value: float) -> str:
    return f"{value}"


XDR_Types: Dict[str, Dict] = {
    "TEMPERATURE": { "type": "C", "unit": "C", "to_string": xdr_value_to_str_1_dec },           # in Celsius
    "ANGULAR_DISPLACEMENT": { "type": "A", "unit": "D", "to_string": xdr_value_to_str_no_dec }, # In degrees
    "LINEAR_DISPLACEMENT": { "type": "D", "unit": "M", "to_string": xdr_default_fmt },          # In meters
    "FREQUENCY": { "type": "F", "unit": "H", "to_string": xdr_default_fmt },                    # In Hertz
    "FORCE": { "type": "N", "unit": "N", "to_string": xdr_default_fmt },                        # In Newtons
    "PRESSURE_B": { "type": "P", "unit": "B", "to_string": xdr_value_to_str_4_dec },            # In Bars
    "PRESSURE_P": { "type": "P", "unit": "P", "to_string": xdr_value_to_str_no_dec },           # In Pascals
    "FLOW_RATE": { "type": "R", "unit": "l", "to_string": xdr_default_fmt },                    # In liters
    "TACHOMETER": { "type": "T", "unit": "R", "to_string": xdr_default_fmt },                   # In RPM
    "HUMIDITY": { "type": "H", "unit": "P", "to_string": xdr_value_to_str_1_dec },              # In %
    "VOLUME": { "type": "V", "unit": "M", "to_string": xdr_default_fmt },                       # In Cubic meters
    "GENERIC": { "type": "G", "unit": "", "to_string": xdr_value_to_str_5_dec },                # No unit
    "CURRENT": { "type": "I", "unit": "A", "to_string": xdr_default_fmt },                      # Electric current, in Amperes
    "VOLTAGE": { "type": "U", "unit": "V", "to_string": xdr_default_fmt },                      # In Volts
    "SWITCH_OR_VALVE": { "type": "S", "unit": "", "to_string": xdr_default_fmt },               # No Unit
    "SALINITY": { "type": "L", "unit": "S", "to_string": xdr_default_fmt }                      # In Parts per Thousand
}


def build_XDR(*args) -> str:
    sentence: str = f"{prefixes.DEVICE_PREFIX}XDR"
    for i in range(len(args)):
        # print(f"{i}: arg:{args[i]}")
        xdr_type: dict = XDR_Types[args[i]["type"]]
        # print(f"xdr_type: ${type(xdr_type)}")
        sentence += f",{xdr_type['type']},{ xdr_type['to_string'](args[i]['value']) },{xdr_type['unit']},{i}"

    cs: int = checksum.calculate_check_sum(sentence)
    str_cs: str = f"{cs:02X}"  # Should be 2 character long, in upper case.
    while len(str_cs) < 2:
        str_cs = '0' + str_cs
    sentence += ("*" + str_cs.upper())

    return "$" + sentence


def build_HDM(hdm: float) -> str:
    sentence: str = f"{prefixes.DEVICE_PREFIX}HDM,"

    sentence += f"{int(round(hdm, 0))},M"

    cs: int = checksum.calculate_check_sum(sentence)
    str_cs: str = f"{cs:02X}"  # Should be 2 character long, in upper case.
    while len(str_cs) < 2:
        str_cs = '0' + str_cs
    sentence += ("*" + str_cs.upper())

    return "$" + sentence


def build_HDG(hdm: float) -> str:
    sentence: str = f"{prefixes.DEVICE_PREFIX}HDG,"

    sentence += f"{int(round(hdm, 0))},,,,"

    cs: int = checksum.calculate_check_sum(sentence)
    str_cs: str = f"{cs:02X}"  # Should be 2 character long, in upper case.
    while len(str_cs) < 2:
        str_cs = '0' + str_cs
    sentence += ("*" + str_cs.upper())

    return "$" + sentence


# This is for tests
if __name__ == '__main__':
    print(f"Generated ZDA: {build_ZDA()}")
    
    print(f"Generated MTA: {build_MTA(12.34)}")
    print(f"Generated MTA: {build_MTA(.34)}")
    print(f"Generated MTA: {build_MTA(12.34567)}")

    print(f"Generated MMB: {build_MMB(1013.25)}")

    xdr_sentence: str = build_XDR({ "value": 123, "type": "TEMPERATURE" },
                                  { "value": 1.01325, "type": "PRESSURE_B" })
    print(f"Generated XDR: {xdr_sentence}")
    xdr_sentence = build_XDR({ "value": 56.78, "type": "HUMIDITY" },
                             { "value": 12.34, "type": "TEMPERATURE" },
                             { "value": 101_325, "type": "PRESSURE_P" },
                             { "value": 1.01325, "type": "PRESSURE_B" })
    print(f"Generated XDR: {xdr_sentence}")

    print(f"Generated HDM: {build_HDM(195.4)}")
    print(f"Generated HDM: {build_HDM(195.6)}")

    print(f"Generated HDG: {build_HDG(195.6)}")
