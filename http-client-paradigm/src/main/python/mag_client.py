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

URL_ARG_PREFIX = '--url:'
rest_url = "http://192.168.42.9:8080/lis3mdl/cache"  # Default value

for arg in sys.argv:
    if arg[:len(URL_ARG_PREFIX)] == URL_ARG_PREFIX:
        rest_url = arg[len(URL_ARG_PREFIX):]


def fetch_data(uri):
    # print("Using {}".format(uri))
    resp = requests.get(uri)
    if resp.status_code != 200:
        raise Exception('GET {} {}'.format(uri, resp.status_code))
    else:
        json_obj = json.loads(resp.content)
        # print('Status {}\nReceived {}'.format(resp.status_code, json.dumps(json_obj, indent=2)))
        return json_obj


def calculate(mag_x, mag_y, mag_z):
    heading = math.degrees(math.atan2(mag_y, mag_x))
    while heading < 0:
        heading += 360
    pitch = math.degrees(math.atan2(mag_y, mag_z))
    roll = math.degrees(math.atan2(mag_x, mag_z))
    return heading, pitch, roll


keep_looping = True

while keep_looping:
    try:
        response = fetch_data(rest_url)
        # print("MagData: {}".format(json.dumps(response, indent=2)))
        calculated = calculate(response["x"], response["y"], response["z"])
        print("Heading: {}, Pitch: {}, Roll: {}".format(calculated[0], calculated[1], calculated[2]))
    except KeyboardInterrupt:
        print("\n\t\tUser interrupted, exiting.")
        keep_looping = False
        break
    except:
        traceback.print_exc(file=sys.stdout)

print("Bye!")
