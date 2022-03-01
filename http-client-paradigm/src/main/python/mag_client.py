# Use Python3
#
# May require:
# $ pip3 install requests
# $ pip3 install json
#
# Good requests 101-tutorial at https://realpython.com/api-integration-in-python/
#
# This is a REST Client.
# To start the server, look for "lis3mdl_server.py"
#
import sys
import requests
import json
import math
import traceback
from typing import Any, Tuple

URL_ARG_PREFIX: str = '--url:'
rest_url: str = "http://192.168.42.9:8080/lis3mdl/cache"  # Default value

for arg in sys.argv:
    if arg[:len(URL_ARG_PREFIX)] == URL_ARG_PREFIX:
        rest_url = arg[len(URL_ARG_PREFIX):]


def fetch_data(uri: str) -> Any:
    # print("Using {}".format(uri))
    resp: Any = requests.get(uri)
    if resp.status_code != 200:
        raise Exception('GET {} {}'.format(uri, resp.status_code))
    else:
        json_obj: Any = json.loads(resp.content)
        # print('Status {}\nReceived {}'.format(resp.status_code, json.dumps(json_obj, indent=2)))
        return json_obj


def calculate(mag_x: float, mag_y: float, mag_z: float) -> Tuple[float, float, float]:
    heading: float = math.degrees(math.atan2(mag_y, mag_x))
    while heading < 0:
        heading += 360
    pitch: float = math.degrees(math.atan2(mag_y, mag_z))
    roll: float = math.degrees(math.atan2(mag_x, mag_z))
    return heading, pitch, roll


keep_looping: bool = True

print(f"Will request resource {rest_url}")
while keep_looping:
    try:
        response: Any = fetch_data(rest_url)
        print(f"MagData: {json.dumps(response, indent=2)}")
        calculated: Tuple[float, float, float] = calculate(response["x"], response["y"], response["z"])
        print(f"Heading: {calculated[0]}, Pitch: {calculated[1]}, Roll: {calculated[2]}")
    except KeyboardInterrupt:
        print("\n\t\tUser interrupted, exiting.")
        keep_looping = False
        break
    except Exception as ex:
        traceback.print_exc(file=sys.stdout)

print("Bye!")
