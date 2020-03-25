#
# Find Azimuth & Elevation of a geostationary satellite
# Returns the best one (highest elevation) for you position
#

import math

# 2010 48th Ave, SF
obsLat = 37.7489
obsLng = -122.5070

class Satellite(object):
    """Satellite.
    attributes: name, longitude
    """

APAC = Satellite()
APAC.name = "I-4 F1 Asia-Pacific"
APAC.longitude = 143.5

EMEA = Satellite()
EMEA.name = "I-4 F2 EMEA (Europe, Middle East and Africa)"
EMEA.longitude = 63.0

AMERICAS = Satellite()
AMERICAS.name = "I-4 F3 Americas"
AMERICAS.longitude = -97.6

ALPHASAT = Satellite()
ALPHASAT.name = "Alphasat"
ALPHASAT.longitude = 24.9

SATS = [APAC, EMEA, AMERICAS, ALPHASAT]

def azimuth(satLng, earthStationLat, earthStationLng):
    deltaG = math.radians(earthStationLng - satLng)
    earthStationAzimuth = 180 + math.degrees(math.atan(math.tan(deltaG) / math.sin((math.radians(earthStationLat)))))
    if earthStationLat < 0:
        earthStationAzimuth -= 180
    if earthStationAzimuth < 0:
        earthStationAzimuth += 360
    return earthStationAzimuth

R1 = 1 + 35786 / 6378.16

def elevation(satLong, earthStationLat, earthStationLong):
    deltaG = math.radians(earthStationLong - satLong)
    latRad = math.radians(earthStationLat)
    v1 = R1 * math.cos(latRad) * math.cos(deltaG) - 1
    v2 = R1 * math.sqrt(1 - math.cos(latRad) * math.cos(latRad) * math.cos(deltaG) * math.cos(deltaG))
    earthStationElevation = math.degrees(math.atan(v1/v2))
    return earthStationElevation

def tilt(satLong, earthStationLat, earthStationLong):
    deltaG = math.radians(earthStationLong - satLong)
    latRad = math.radians(earthStationLat)
    return math.degrees(math.atan(math.sin(deltaG) / math.tan(latRad)))

def calculate(satLong, earthStationLat, earthStationLong):
    return azimuth(satLong, earthStationLat, earthStationLong),
           elevation(satLong, earthStationLat, earthStationLong),
           tilt(satLong, earthStationLat, earthStationLong)

def findSat(earthStationLat, earthStationLong):
    for sat in SATS:
        aim = calculate(sat.longitude, earthStationLat, earthStationLong)
        if aim[1] > 0:
            return sat, aim
    return

satAim = calculate(-97.6, obsLat, obsLng)
print 'Sat :', satAim

print 'Z   :', satAim[0]
print 'El  :', satAim[1]
print 'Tilt:', satAim[2]

satToUse = findSat(obsLat, obsLng)
print 'Use "', satToUse[0].name, '", Z:', satToUse[1][0], ', El:', satToUse[1][1], ', Tilt:', satToUse[1][2]
