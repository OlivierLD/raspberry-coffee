#!/usr/bin/env python3

import checksum  # local script
from datetime import datetime, timezone

DEBUG: bool = False


def build_ZDA(utc_ms: int = None) -> str:
    """
    Builds the ZDA sentence for given timestamp.
    :param utc_ms: if provided, in ms !!! (python default is seconds)
    :return: the NMEA ZDA sentence
    """

    # Structure is:
    # $GPZDA,hhmmss.ss,dd,mm,yyyy,xx,yy*CC
    # $GPZDA,201530.00,04,07,2002,00,00*60
    #        |         |  |  |    |  |
    #        |         |  |  |    |  local zone minutes 0..59
    #        |         |  |  |    local zone hours -13..13
    #        |         |  |  year
    #        |         |  month
    #        |         day
    #        HrMinSec(UTC)
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


# This is for tests
if __name__ == '__main__':
    print(f"Generated ZDA: {build_ZDA()}")

