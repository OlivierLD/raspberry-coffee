#!/usr/bin/env python3
import json

detections = ["bagnole", "bagnole", "bidule", "machin", "truc", "bidule"]
detected = {}
for eachObject in detections:
    objName = eachObject
    try:
        member = detected[objName]
    except KeyError:
        member = 0
    detected[objName] = member + 1
print(detected)

print(json.dumps(detected, indent=2))

print('The map is a {}'.format(type(detected)))
for member in detected:
    print("{}: {} (type:{})".format(member, detected[member], type(detected[member])))
