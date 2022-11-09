#!/usr/bin/env python3

import checksum  # local script
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
    sentence: str = "PYZDA,"

    if utc_ms is None:
        # Take system time instead
        utc_ms = datetime.now(timezone.utc).timestamp() * 1000  # System "UTC epoch" in ms

    dt_object = datetime.fromtimestamp(utc_ms / 1000, tz=timezone.utc)  # <- Aha !!
    # fmt_date_time: str = dt_object.strftime("%Y-%m-%d %H:%M:%S")
    fmt_date_time: str = dt_object.strftime("%H%M%S.00,%d,%m,%Y")

    if DEBUG:
        print("dt_object =", dt_object, " - ", fmt_date_time)
        print("type(dt_object) =", type(dt_object))

    sentence += (fmt_date_time + ",00,00")

    # zda += strUTC.substring(8, 17); // Time
    # zda += ",";
    # zda += strUTC.substring(6, 8); // day
    # zda += ",";
    # zda += strUTC.substring(4, 6); // month
    # zda += ",";
    # zda += strUTC.substring(0, 4); // year
    # zda += ",00,00";
    # // Checksum
    # cs = StringParsers.calculateCheckSum(zda);
    # zda += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));
    # return "$" + zda;

    cs: int = checksum.calculate_check_sum(sentence);
    str_cs: str = hex(cs).split('x')[-1]  # Just the hex part (no '0x' prefix)
    while len(str_cs) < 2:
        str_cs = '0' + str_cs
    sentence += ("*" + str_cs.upper())

    return "$" + sentence


def build_MTA(temperature: float) -> str:
    """
    Build the MTA String, for the given temperature.
    """
    sentence: str = "PYMTA,"
    sentence += f"{temperature:0.1f}"
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
    sentence: str = "PYMMB,"
    sentence += f"{mb_pressure / 33.8600:0.4f},I,"   # Inches of Hg
    sentence += f"{mb_pressure / 1_000:0.4f},B"      # Bars. 1 mb = 1 hPa
    cs: int = checksum.calculate_check_sum(sentence)
    str_cs: str = f"{cs:02X}"  # Should be 2 character long, in upper case.
    while len(str_cs) < 2:
        str_cs = '0' + str_cs
    sentence += ("*" + str_cs.upper())

    return "$" + sentence


XDR_Types: Dict[str, Dict] = {
    "TEMPERATURE": { "type": "C", "unit": "C" }, # in Celsius
    "ANGULAR_DISPLACEMENT": { "type": "A", "unit": "D" }, # In degrees
    "LINEAR_DISPLACEMENT": { "type": "D", "unit": "M" }, # In meters
    "FREQUENCY": { "type": "F", "unit": "H" }, # In Hertz
    "FORCE": { "type": "N", "unit": "N" }, # In Newtons
    "PRESSURE_B": { "type": "P", "unit": "B" }, # In Bars
    "PRESSURE_P": { "type": "P", "unit": "P" }, # In Pascals
    "FLOW_RATE": { "type": "R", "unit": "l" }, # In liters
    "TACHOMETER": { "type": "T", "unit": "R" }, # In RPM
    "HUMIDITY": { "type": "H", "unit": "P" }, # In %
    "VOLUME": { "type": "V", "unit": "M" }, # In Cubic meters
    "GENERIC": { "type": "G", "unit": "" },  # No unit
    "CURRENT": { "type": "I", "unit": "A" }, # In Amperes
    "VOLTAGE": { "type": "U", "unit": "V" }, # In Volts
    "SWITCH_OR_VALVE": { "type": "S", "unit": "" }, # No Unit
    "SALINITY": { "type": "L", "unit": "S" } # In Parts per Thousand
}

def build_XDR(*args) -> str:
    sentence: str = "PYXDR"
    for i in range(len(args)):
        # print(f"{i}: arg:{args[i]}")
        xdr_type: dict = XDR_Types[args[i]["type"]]
        # print(f"xdr_type: ${type(xdr_type)}")
        sentence += f",{xdr_type['type']},{args[i]['value']},{xdr_type['unit']},{i}"

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

    xdr_sentence: str = build_XDR({ "value": 123, "type": "TEMPERATURE" }, { "value": 1.01325, "type": "PRESSURE_B" })
    print(f"Generated XDA: {xdr_sentence}")
    xdr_sentence = build_XDR({ "value": 56.78, "type": "HUMIDITY" }, { "value": 12.34, "type": "TEMPERATURE" }, { "value": 101325, "type": "PRESSURE_P" })
    print(f"Generated XDA: {xdr_sentence}")

